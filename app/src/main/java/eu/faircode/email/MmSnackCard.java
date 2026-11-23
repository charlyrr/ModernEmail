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
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.LayoutDirection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

// ModernMail: fondo de los avisos flotantes (Deshacer...).
// Una tarjeta redondeada con sombra propia, difuminada alrededor de toda la tarjeta
// (la sombra de Android cae casi solo hacia abajo y sobre blanco apenas se ve), un canto
// fino y, en los avisos de deshacer, una línea de cuenta atrás en el borde inferior.
// Alrededor de la tarjeta queda un margen transparente donde cabe la sombra.
public class MmSnackCard extends Drawable {
    final Rect inset = new Rect();

    private final float radius;
    private final float progressHeight;
    private final boolean shadowLayer;
    private final Paint softShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edgeShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint edge = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF card = new RectF();
    private final RectF tmp = new RectF();
    private final Path clip = new Path();
    private float progress = -1f;

    MmSnackCard(Context context, boolean dark, int cardColor, int edgeColor, int accent) {
        radius = Helper.dp2pixels(context, 22);
        progressHeight = Helper.dp2pixels(context, 3);
        inset.set(
                Helper.dp2pixels(context, 16),
                Helper.dp2pixels(context, 12),
                Helper.dp2pixels(context, 16),
                Helper.dp2pixels(context, 24));

        // Sombras dibujadas por la propia tarjeta (Android 9+ con aceleración por hardware)
        shadowLayer = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P);

        // Sombra amplia y suave, algo desplazada hacia abajo: la tarjeta "flota"
        softShadow.setColor(cardColor);
        softShadow.setShadowLayer(Helper.dp2pixels(context, 10), 0, Helper.dp2pixels(context, 6),
                dark ? 0x99000000 : 0x3d000000);

        // Sombra corta y cercana: marca el contorno sobre fondos del mismo color
        edgeShadow.setColor(cardColor);
        edgeShadow.setShadowLayer(Helper.dp2pixels(context, 2), 0, Helper.dp2pixels(context, 1),
                dark ? 0x80000000 : 0x24000000);

        fill.setColor(cardColor);

        edge.setStyle(Paint.Style.STROKE);
        edge.setStrokeWidth(Math.max(1, Helper.dp2pixels(context, 1) / 2));
        edge.setColor(edgeColor);

        progressPaint.setColor(ColorUtils.setAlphaComponent(accent, 0xcc));
    }

    boolean usesOwnShadow() {
        return shadowLayer;
    }

    // Solo la tarjeta es "sólida": los toques en el margen de la sombra pasan a lo de debajo
    boolean contains(float x, float y) {
        return card.contains(x, y);
    }

    // 1 = recién aparecido, 0 = se acabó el tiempo; < 0 = sin cuenta atrás
    void setProgress(float progress) {
        this.progress = progress;
        invalidateSelf();
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        super.onBoundsChange(bounds);
        card.set(bounds.left + inset.left, bounds.top + inset.top,
                bounds.right - inset.right, bounds.bottom - inset.bottom);
        clip.reset();
        if (card.width() > 0 && card.height() > 0)
            clip.addRoundRect(card, radius(), radius(), Path.Direction.CW);
    }

    private float radius() {
        return Math.min(radius, Math.min(card.width(), card.height()) / 2f);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (card.width() <= 0 || card.height() <= 0)
            return;
        float r = radius();

        if (shadowLayer) {
            canvas.drawRoundRect(card, r, r, softShadow);
            canvas.drawRoundRect(card, r, r, edgeShadow);
        }
        canvas.drawRoundRect(card, r, r, fill);

        float h = edge.getStrokeWidth() / 2f;
        tmp.set(card);
        tmp.inset(h, h);
        canvas.drawRoundRect(tmp, Math.max(0, r - h), Math.max(0, r - h), edge);

        if (progress >= 0) {
            float w = card.width() * Math.min(1f, progress);
            boolean rtl = (getLayoutDirection() == LayoutDirection.RTL);
            int save = canvas.save();
            canvas.clipPath(clip);
            if (rtl)
                canvas.drawRect(card.right - w, card.bottom - progressHeight, card.right, card.bottom, progressPaint);
            else
                canvas.drawRect(card.left, card.bottom - progressHeight, card.left + w, card.bottom, progressPaint);
            canvas.restoreToCount(save);
        }
    }

    @Override
    public void getOutline(@NonNull Outline outline) {
        // Para la sombra de Android en versiones anteriores a la 9
        outline.setRoundRect(
                Math.round(card.left), Math.round(card.top),
                Math.round(card.right), Math.round(card.bottom), radius());
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
