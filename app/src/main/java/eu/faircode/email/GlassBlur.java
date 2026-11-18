package eu.faircode.email;

/*
    This file is part of ModernMail, a modification of FairEmail.

    ModernMail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
*/

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RecordingCanvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.RenderEffect;
import android.graphics.RenderNode;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

// ModernMail: tema Cristal con desenfoque ("cristal esmerilado").
//
// La lista de mensajes graba una copia de lo que dibuja en un RenderNode propio. Cada barra flotante
// vuelve a dibujar ese RenderNode desenfocado detrás de ella y le pone encima un velo
// casi opaco. La barra queda siempre opaca: debajo del desenfoque se pinta un fondo
// sólido, así que no aparecen huecos transparentes y la sombra no se ve a través.
//
// Android 12 o superior: desenfoque real en la GPU. En versiones anteriores, o sin
// aceleración por hardware: el mismo velo sobre un fondo sólido, sin desenfoque.
public class GlassBlur {
    private static final int BLUR_DP = 18; // intensidad del difuminado
    private static final float SATURATION = 1.7f; // los colores de debajo se avivan un poco, como en iOS

    // La lista pasa a poder desenfocarse
    static void enableSource(@Nullable FixedRecyclerView rv) {
        if (rv == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
            return;
        if (rv.glassSource == null)
            rv.glassSource = new Source(rv);
        // RecyclerView dibuja sus decoraciones en draw(): tiene que llamarse siempre
        rv.setWillNotDraw(false);
        rv.invalidate();
    }

    // Convierte una barra o un botón flotante en un panel de cristal esmerilado
    static void applyPanel(@Nullable View panel, @Nullable FixedRecyclerView source, boolean dark, int radiusDp) {
        if (panel == null)
            return;
        final Panel drawable = new Panel(panel, source, dark, radiusDp);
        panel.setBackground(drawable);

        final ViewTreeObserver.OnPreDrawListener listener = new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                // Si la barra se ha movido respecto a la lista (animación de ocultar,
                // cabecera que se pliega...), el desenfoque se vuelve a encuadrar
                drawable.checkOffset();
                return true;
            }
        };
        panel.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                v.getViewTreeObserver().addOnPreDrawListener(listener);
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
                v.getViewTreeObserver().removeOnPreDrawListener(listener);
            }
        });
        if (panel.isAttachedToWindow())
            panel.getViewTreeObserver().addOnPreDrawListener(listener);
    }

    // Copia de lo que dibuja la lista, para reutilizarla detrás de las barras
    @RequiresApi(api = Build.VERSION_CODES.S)
    static class Source {
        private final FixedRecyclerView view;
        private final RenderNode node = new RenderNode("ModernMailGlassSource");

        Source(FixedRecyclerView view) {
            this.view = view;
            this.node.setClipToBounds(false);
        }

        // Se llama justo después de que la lista se dibuje. La copia solo referencia lo que
        // la lista ya ha dibujado (las filas), así que cuesta muy poco
        void record(Canvas canvas) {
            int w = view.getWidth();
            int h = view.getHeight();
            if (!canvas.isHardwareAccelerated() || w <= 0 || h <= 0)
                return;
            node.setPosition(0, 0, w, h);
            RecordingCanvas rc = node.beginRecording(w, h);
            try {
                view.drawMirror(rc);
            } finally {
                node.endRecording();
            }
        }
    }

    // Desenfoque de un trozo de la lista, del tamaño de la barra más un margen
    @RequiresApi(api = Build.VERSION_CODES.S)
    private static class Blur {
        private final RenderNode node = new RenderNode("ModernMailGlassPanel");

        Blur(float radius) {
            ColorMatrix saturation = new ColorMatrix();
            saturation.setSaturation(SATURATION);
            node.setRenderEffect(RenderEffect.createColorFilterEffect(
                    new ColorMatrixColorFilter(saturation),
                    RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)));
        }

        void draw(Canvas canvas, Source source, Path clip,
                  int dx, int dy, int w, int h, int margin, Paint fill) {
            int bw = w + 2 * margin;
            int bh = h + 2 * margin;
            node.setPosition(0, 0, bw, bh);
            RecordingCanvas rc = node.beginRecording(bw, bh);
            try {
                // Fondo sólido: donde la lista no pinta (huecos entre bloques, final de la
                // lista) el desenfoque parte del color del fondo, nunca de un hueco vacío
                rc.drawRect(0, 0, bw, bh, fill);
                rc.translate(margin - dx, margin - dy);
                rc.drawRenderNode(source.node);
            } finally {
                node.endRecording();
            }

            int save = canvas.save();
            canvas.clipPath(clip);
            canvas.translate(-margin, -margin);
            canvas.drawRenderNode(node);
            canvas.restoreToCount(save);
        }
    }

    private static class Panel extends Drawable {
        private final View panel;
        private final FixedRecyclerView source;
        private final float radius;
        private final int margin;
        private final Paint basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fillPaint = new Paint();
        private final Paint frostPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint shinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private final Path clip = new Path();
        private final int[] loc = new int[2];
        private final int[] sloc = new int[2];
        private Object blur = null; // Blur, solo en Android 12+
        private int drawnDx = Integer.MIN_VALUE;
        private int drawnDy = Integer.MIN_VALUE;

        Panel(View panel, @Nullable FixedRecyclerView source, boolean dark, int radiusDp) {
            Context context = panel.getContext();
            this.panel = panel;
            this.source = source;
            this.radius = Helper.dp2pixels(context, radiusDp);
            this.margin = Helper.dp2pixels(context, BLUR_DP);
            int px1 = Math.max(1, Helper.dp2pixels(context, 1));

            basePaint.setColor(ContextCompat.getColor(context, dark ? R.color.glassDarkFloating : R.color.glassLightFloating));
            fillPaint.setColor(ContextCompat.getColor(context, dark ? R.color.glassDarkGradBottom : R.color.glassLightGradBottom));
            frostPaint.setColor(ContextCompat.getColor(context, dark ? R.color.glassDarkFrost : R.color.glassLightFrost));

            edgePaint.setStyle(Paint.Style.STROKE);
            edgePaint.setStrokeWidth(px1);
            edgePaint.setColor(ContextCompat.getColor(context, dark ? R.color.glassDarkEdge : R.color.glassLightEdge));

            shinePaint.setStyle(Paint.Style.STROKE);
            shinePaint.setStrokeWidth(px1);
            shinePaint.setColor(ContextCompat.getColor(context, dark ? R.color.glassDarkStroke : R.color.glassLightStroke));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && source != null && source.glassSource != null)
                blur = new Blur(margin);
        }

        private boolean canBlur(Canvas canvas) {
            return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    blur != null && source != null && source.glassSource != null &&
                    canvas.isHardwareAccelerated() &&
                    source.isAttachedToWindow() && source.getWidth() > 0);
        }

        // Posición de la barra respecto a la lista, con animaciones y desplazamientos incluidos
        private boolean offset() {
            if (source == null || !panel.isAttachedToWindow() || !source.isAttachedToWindow())
                return false;
            panel.getLocationInWindow(loc);
            source.getLocationInWindow(sloc);
            return true;
        }

        void checkOffset() {
            if (blur == null || panel.getVisibility() != View.VISIBLE || !offset())
                return;
            if (loc[0] - sloc[0] != drawnDx || loc[1] - sloc[1] != drawnDy)
                invalidateSelf();
        }

        @Override
        protected void onBoundsChange(@NonNull Rect bounds) {
            super.onBoundsChange(bounds);
            clip.reset();
            clip.addRoundRect(new RectF(bounds), radius, radius, Path.Direction.CW);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            Rect b = getBounds();
            if (b.isEmpty())
                return;
            rect.set(b);
            float r = Math.min(radius, Math.min(rect.width(), rect.height()) / 2f);

            // Base sólida: la barra nunca es transparente
            canvas.drawRoundRect(rect, r, r, basePaint);

            // Lo que pasa por debajo, difuminado
            if (canBlur(canvas) && offset()) {
                drawnDx = loc[0] - sloc[0];
                drawnDy = loc[1] - sloc[1];
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    ((Blur) blur).draw(canvas, source.glassSource, clip,
                            drawnDx, drawnDy, b.width(), b.height(), margin, fillPaint);
            }

            // Velo esmerilado casi opaco
            canvas.drawRoundRect(rect, r, r, frostPaint);

            // Canto exterior fino (destaca sobre blanco) y reflejo interior
            float h = edgePaint.getStrokeWidth() / 2f;
            rect.inset(h, h);
            canvas.drawRoundRect(rect, Math.max(0, r - h), Math.max(0, r - h), edgePaint);
            rect.inset(2 * h, 2 * h);
            canvas.drawRoundRect(rect, Math.max(0, r - 3 * h), Math.max(0, r - 3 * h), shinePaint);
        }

        @Override
        public void getOutline(@NonNull Outline outline) {
            Rect b = getBounds();
            outline.setRoundRect(b, Math.min(radius, Math.min(b.width(), b.height()) / 2f));
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }
}
