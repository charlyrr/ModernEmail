package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2026 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;

public class ViewCardOptional extends CardView {
    private boolean cards;
    private boolean compact;
    private int padding;
    private int margin;
    private int hmargin;
    private int vmargin;
    private int ident;
    private Integer color = null;

    // ModernMail: filas agrupadas en bloques redondeados (lista de mensajes)
    private boolean grouped = false;
    private Boolean groupFirst = null;
    private Boolean groupLast = null;
    private View divider = null;

    public ViewCardOptional(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ViewCardOptional(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ViewCardOptional(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        cards = prefs.getBoolean("cards", true);
        compact = prefs.getBoolean("compact", false);
        padding = prefs.getInt("view_padding", compact || !cards ? 0 : 1);

        margin = Helper.dp2pixels(context, (padding + (cards ? 1 : 0)) * 3);

        // Estética moderna: más aire lateral, separación vertical fina y esquinas suaves
        hmargin = margin + Helper.dp2pixels(context, cards ? 4 : 0);
        vmargin = (cards ? Math.max(Helper.dp2pixels(context, 2), margin / 2) : margin);
        int radius = Helper.dp2pixels(context, compact ? 10 : 14);

        setRadius(cards ? Math.max(margin, radius) : 0);
        setCardElevation(0);
        setCardBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onAttachedToWindow() {
        if (cards) {
            ViewGroup.MarginLayoutParams lparam = (ViewGroup.MarginLayoutParams) getLayoutParams();
            if (grouped) {
                int side = Helper.dp2pixels(getContext(), 16);
                lparam.setMargins(side, 0, side, 0);
            } else
                lparam.setMargins(hmargin, vmargin, hmargin, vmargin);
            setLayoutParams(lparam);
        }

        if (grouped)
            setContentPadding(0, 0, 0, 0);
        else
            setContentPadding(margin, margin, margin, margin);

        super.onAttachedToWindow();
    }

    // La fila forma parte de un bloque: sin márgenes verticales ni esquinas propias
    void setGrouped(boolean grouped) {
        this.grouped = (grouped && cards);
        if (this.grouped) {
            setRadius(0);
            divider = findViewById(R.id.vwRowDivider);
        }
    }

    // Posición dentro del bloque: redondea sólo las esquinas exteriores
    void setGroup(final boolean first, final boolean last) {
        if (!grouped)
            return;
        if (groupFirst != null && groupFirst == first && groupLast == last)
            return;
        groupFirst = first;
        groupLast = last;

        final int radius = Helper.dp2pixels(getContext(), 14);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                // El contorno se prolonga fuera de la fila por el lado que no se redondea
                outline.setRoundRect(
                        0, first ? 0 : -radius,
                        view.getWidth(), view.getHeight() + (last ? 0 : radius),
                        radius);
            }
        });
        setClipToOutline(true);
        invalidateOutline();

        if (divider != null)
            divider.setVisibility(last ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void setCardBackgroundColor(int color) {
        if (this.color == null || this.color != color) {
            this.color = color;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean cards = prefs.getBoolean("cards", true);
            if (cards && color == Color.TRANSPARENT)
                color = Helper.resolveColor(getContext(), R.attr.colorCardBackground);

            super.setCardBackgroundColor(color);
        }
    }
}
