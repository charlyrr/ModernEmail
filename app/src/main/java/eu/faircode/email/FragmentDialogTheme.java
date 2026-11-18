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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

public class FragmentDialogTheme extends FragmentDialogBase {
    private RadioGroup rgTheme;
    private SwitchCompat swReverse;
    private RadioButton rbThemeYou;
    private RadioButton rbThemeYouMono;
    private TextView tvYou;
    private RadioGroup rgThemeOptions;
    private TextView tvSystem;
    private SwitchCompat swBlack;
    private SwitchCompat swBeige;
    private SwitchCompat swHtmlLight;
    private SwitchCompat swComposerLight;
    private Button btnMore;
    private TextView tvMore;
    private Group grpDebug;

    private void eval() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean cards = prefs.getBoolean("cards", true);

        int checkedId = rgTheme.getCheckedRadioButtonId();
        boolean modern = (getFamily(checkedId) != null);
        boolean grey = (checkedId == R.id.rbThemeGrey);
        boolean solarized = (checkedId == R.id.rbThemeSolarized);
        boolean blank = (checkedId == R.id.rbThemeBlank);
        boolean bw = (checkedId == R.id.rbThemeBlackOrWhite);
        boolean bandw = (checkedId == R.id.rbThemeBlackAndWhite);
        boolean mono = (checkedId == R.id.rbThemeYouMono);
        boolean you = (checkedId == R.id.rbThemeYou || mono);
        boolean colored = (modern || grey || bw || solarized || you ||
                checkedId == R.id.rbThemeBlueOrange ||
                checkedId == R.id.rbThemeRedGreen ||
                checkedId == R.id.rbThemeYellowPurple);
        int optionId = rgThemeOptions.getCheckedRadioButtonId();

        swReverse.setEnabled(colored && !modern && !grey && !solarized && !bw && !mono);

        rgThemeOptions.setEnabled(colored);
        for (int i = 0; i < rgThemeOptions.getChildCount(); i++)
            rgThemeOptions.getChildAt(i).setEnabled(colored);

        tvSystem.setEnabled(colored && optionId == R.id.rbThemeSystem);

        swBlack.setEnabled(colored && !grey && !bw && !solarized && optionId != R.id.rbThemeLight);
        swBeige.setEnabled(cards && !modern && !bandw && !solarized && optionId != R.id.rbThemeDark);

        swHtmlLight.setEnabled(colored ? optionId != R.id.rbThemeLight : !blank);
        swComposerLight.setEnabled(colored ? optionId != R.id.rbThemeLight : !blank);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        boolean settings = (args != null && args.getBoolean("settings"));

        final Context context = getContext();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String theme = prefs.getString("theme", "minimal_system");
        boolean default_light = prefs.getBoolean("default_light", false);
        boolean composer_light = prefs.getBoolean("composer_light", false);
        boolean debug = prefs.getBoolean("debug", false);

        View dview = LayoutInflater.from(context).inflate(R.layout.dialog_theme, null);
        rgTheme = dview.findViewById(R.id.rgTheme);
        rbThemeYou = dview.findViewById(R.id.rbThemeYou);
        rbThemeYouMono = dview.findViewById(R.id.rbThemeYouMono);
        tvYou = dview.findViewById(R.id.tvYou);
        swReverse = dview.findViewById(R.id.swReverse);
        rgThemeOptions = dview.findViewById(R.id.rgThemeOptions);
        tvSystem = dview.findViewById(R.id.tvSystem);
        swBlack = dview.findViewById(R.id.swBlack);
        swBeige = dview.findViewById(R.id.swBeige);
        swHtmlLight = dview.findViewById(R.id.swHtmlLight);
        swComposerLight = dview.findViewById(R.id.swComposerLight);
        btnMore = dview.findViewById(R.id.btnMore);
        tvMore = dview.findViewById(R.id.tvMore);
        grpDebug = dview.findViewById(R.id.grpDebug);

        rgTheme.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                eval();
            }
        });

        rbThemeYou.setVisibility(Build.VERSION.SDK_INT < Build.VERSION_CODES.S ? View.GONE : View.VISIBLE);
        rbThemeYouMono.setVisibility(Build.VERSION.SDK_INT < Build.VERSION_CODES.S ? View.GONE : View.VISIBLE);
        tvYou.setVisibility(Build.VERSION.SDK_INT < Build.VERSION_CODES.S ? View.GONE : View.VISIBLE);

        // ModernMail: sólo se ofrecen los temas propios; los originales siguen
        // funcionando si ya estaban elegidos, pero no aparecen en la lista
        for (int id : new int[]{
                R.id.rbThemeModern, R.id.rbThemeBlueOrange, R.id.rbThemeRedGreen,
                R.id.rbThemeYellowPurple, R.id.rbThemeGrey, R.id.rbThemeSolarized,
                R.id.rbThemeBlank, R.id.rbThemeBlackOrWhite, R.id.rbThemeBlackAndWhite,
                R.id.rbThemeYou, R.id.rbThemeYouMono}) {
            View v = dview.findViewById(id);
            if (v != null)
                v.setVisibility(View.GONE);
        }
        tvYou.setVisibility(View.GONE);
        swReverse.setVisibility(View.GONE);
        swBeige.setVisibility(View.GONE);

        tvYou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
        });

        swReverse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                eval();
            }
        });

        rgThemeOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                eval();
            }
        });

        swBlack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                eval();
            }
        });

        swBeige.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                eval();
            }
        });

        boolean reversed =
                (theme.contains("reversed") ||
                        theme.startsWith("orange_blue") ||
                        theme.startsWith("purple_yellow") ||
                        theme.startsWith("green_red"));
        boolean dark = (theme.endsWith("dark") || theme.equals("black"));
        boolean system = (theme.endsWith("system") || theme.endsWith("system_black"));
        boolean black = (!"black".equals(theme) && theme.endsWith("black"));
        boolean beige = prefs.getBoolean("beige", true);

        swReverse.setChecked(reversed);

        if (system)
            rgThemeOptions.check(R.id.rbThemeSystem);
        else if (dark || black)
            rgThemeOptions.check(R.id.rbThemeDark);
        else
            rgThemeOptions.check(R.id.rbThemeLight);

        swBlack.setChecked(black);
        swBeige.setChecked(beige);
        swHtmlLight.setChecked(default_light);
        swComposerLight.setChecked(composer_light);

        Integer familyId = getFamilyButton(theme);
        if (familyId != null)
            rgTheme.check(familyId);
        else switch (theme) {
            case "light":
            case "dark":
            case "system":
            case "blue_orange_system":
            case "blue_orange_system_black":
            case "blue_orange_light":
            case "blue_orange_dark":
            case "blue_orange_black":
            case "orange_blue_system":
            case "orange_blue_system_black":
            case "orange_blue_light":
            case "orange_blue_dark":
            case "orange_blue_black":
                rgTheme.check(R.id.rbThemeBlueOrange);
                break;
            case "red_green_system":
            case "red_green_system_black":
            case "red_green_light":
            case "red_green_dark":
            case "red_green_black":
            case "green_red_system":
            case "green_red_system_black":
            case "green_red_light":
            case "green_red_dark":
            case "green_red_black":
                rgTheme.check(R.id.rbThemeRedGreen);
                break;
            case "yellow_purple_system":
            case "yellow_purple_system_black":
            case "yellow_purple_light":
            case "yellow_purple_dark":
            case "yellow_purple_black":
            case "purple_yellow_system":
            case "purple_yellow_system_black":
            case "purple_yellow_light":
            case "purple_yellow_dark":
            case "purple_yellow_black":
                rgTheme.check(R.id.rbThemeYellowPurple);
                break;

            case "grey_system":
            case "grey_light":
            case "grey_dark":
                rgTheme.check(R.id.rbThemeGrey);
                break;

            case "solarized":
            case "solarized_light":
            case "solarized_dark":
            case "solarized_system":
                rgTheme.check(R.id.rbThemeSolarized);
                break;

            case "blank":
                rgTheme.check(R.id.rbThemeBlank);
                break;

            case "black":
            case "white":
            case "bw_system":
                rgTheme.check(R.id.rbThemeBlackOrWhite);
                break;
            case "black_and_white":
                rgTheme.check(R.id.rbThemeBlackAndWhite);
                break;

            case "you_light":
            case "you_dark":
            case "you_black":
            case "you_system":
            case "you_system_black":
            case "you_reversed_light":
            case "you_reversed_dark":
            case "you_reversed_black":
            case "you_reversed_system":
            case "you_reversed_system_black":
                rgTheme.check(R.id.rbThemeYou);
                break;

            case "you_mono_light":
            case "you_mono_dark":
            case "you_mono_black":
            case "you_mono_system":
            case "you_mono_system_black":
            case "you_mono_reversed_light":
            case "you_mono_reversed_dark":
            case "you_mono_reversed_black":
            case "you_mono_reversed_system":
            case "you_mono_reversed_system_black":
                rgTheme.check(R.id.rbThemeYouMono);
                break;
        }

        tvMore.setPaintFlags(tvMore.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 164);
            }
        });

        btnMore.setVisibility(settings ? View.GONE : View.VISIBLE);
        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                v.getContext().startActivity(new Intent(v.getContext(), ActivitySetup.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("tab", "display"));
            }
        });

        if (grpDebug != null)
            grpDebug.setVisibility(debug || BuildConfig.DEBUG ? View.VISIBLE : View.GONE);

        return new AlertDialog.Builder(context)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().getIntent().putExtra("tab", "display");

                        int optionId = rgThemeOptions.getCheckedRadioButtonId();
                        boolean reverse = (swReverse.isEnabled() && swReverse.isChecked());
                        boolean dark = (rgThemeOptions.isEnabled() && optionId == R.id.rbThemeDark);
                        boolean system = (rgThemeOptions.isEnabled() && optionId == R.id.rbThemeSystem);
                        boolean black = (swBlack.isEnabled() && swBlack.isChecked());

                        SharedPreferences.Editor editor = prefs.edit();

                        editor.remove("highlight_color");

                        int checkedRadioButtonId = rgTheme.getCheckedRadioButtonId();
                        String family = getFamily(checkedRadioButtonId);
                        if (family != null)
                            applyPreset(editor, family);
                        if (family != null) {
                            if (system)
                                editor.putString("theme", family + "_system" + (black ? "_black" : "")).apply();
                            else
                                editor.putString("theme",
                                        family + (black ? "_black" : dark ? "_dark" : "_light")).apply();
                        } else if (checkedRadioButtonId == R.id.rbThemeBlueOrange) {
                            if (system)
                                editor.putString("theme",
                                        (reverse ? "orange_blue_system" : "blue_orange_system") +
                                                (black ? "_black" : "")).apply();
                            else
                                editor.putString("theme",
                                        (reverse ? "orange_blue" : "blue_orange") +
                                                (black ? "_black" : dark ? "_dark" : "_light")).apply();
                        } else if (checkedRadioButtonId == R.id.rbThemeRedGreen) {
                            if (system)
                                editor.putString("theme",
                                        (reverse ? "green_red_system" : "red_green_system") +
                                                (black ? "_black" : "")).apply();
                            else
                                editor.putString("theme",
                                        (reverse ? "green_red" : "red_green") +
                                                (black ? "_black" : dark ? "_dark" : "_light")).apply();
                        } else if (checkedRadioButtonId == R.id.rbThemeYellowPurple) {
                            if (system)
                                editor.putString("theme",
                                        (reverse ? "purple_yellow_system" : "yellow_purple_system") +
                                                (black ? "_black" : "")).apply();
                            else
                                editor.putString("theme",
                                        (reverse ? "purple_yellow" : "yellow_purple") +
                                                (black ? "_black" : dark ? "_dark" : "_light")).apply();
                        } else if (checkedRadioButtonId == R.id.rbThemeGrey) {
                            if (system)
                                editor.putString("theme", "grey_system").apply();
                            else
                                editor.putString("theme",
                                        "grey" + (dark ? "_dark" : "_light")).apply();
                        } else if (checkedRadioButtonId == R.id.rbThemeSolarized) {
                            if (system)
                                editor.putString("theme", "solarized_system").apply();
                            else
                                editor.putString("theme",
                                        "solarized" + (dark ? "_dark" : "_light")).apply();
                        } else if (checkedRadioButtonId == R.id.rbThemeBlank)
                            editor.putString("theme", "blank").apply();
                        else if (checkedRadioButtonId == R.id.rbThemeBlackOrWhite) {
                            if (system)
                                editor.putString("theme", "bw_system").apply();
                            else
                                editor.putString("theme", (dark ? "black" : "white")).apply();
                        } else if (checkedRadioButtonId == R.id.rbThemeBlackAndWhite) {
                            editor.putString("theme", "black_and_white").apply();
                        } else if (checkedRadioButtonId == R.id.rbThemeYou) {
                            if (system)
                                editor.putString("theme",
                                        (reverse ? "you_reversed_system" : "you_system") +
                                                (black ? "_black" : "")).apply();
                            else
                                editor.putString("theme",
                                        (reverse ? "you_reversed" : "you") +
                                                (black ? "_black" : dark ? "_dark" : "_light")).apply();
                        } else if (checkedRadioButtonId == R.id.rbThemeYouMono) {
                            if (system)
                                editor.putString("theme",
                                        (reverse ? "you_mono_reversed_system" : "you_mono_system") +
                                                (black ? "_black" : "")).apply();
                            else
                                editor.putString("theme",
                                        (reverse ? "you_mono_reversed" : "you_mono") +
                                                (black ? "_black" : dark ? "_dark" : "_light")).apply();
                        }

                        editor.putBoolean("beige", swBeige.isChecked());
                        editor.putBoolean("default_light", swHtmlLight.isChecked());
                        editor.putBoolean("composer_light", swComposerLight.isChecked());

                        editor.apply();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    static int getTheme(Context context) {
        ensurePreset(context);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String theme = prefs.getString("theme", "minimal_system");
        boolean composer_light = prefs.getBoolean("composer_light", false);

        boolean night = Helper.isNight(context);
        boolean light = (composer_light && context instanceof ActivityCompose);
        if (light)
            night = false;
        EntityLog.log(context, "Activity theme=" + theme + " light=" + light + " night=" + night);

        // Familias de temas de ModernEmail: <familia>_light/_dark/_black/_system/_system_black
        for (String family : FAMILIES)
            if (theme.startsWith(family + "_")) {
                int[] styles = getFamilyStyles(family);
                String mode = theme.substring(family.length() + 1);
                switch (mode) {
                    case "light":
                        return styles[0];
                    case "dark":
                        return (light ? styles[0] : styles[1]);
                    case "black":
                        return (light ? styles[0] : styles[2]);
                    case "system_black":
                        return (night ? styles[2] : styles[0]);
                    case "system":
                    default:
                        return (night ? styles[1] : styles[0]);
                }
            }

        switch (theme) {
            // Light
            case "light":
            case "blue_orange_light":
                return R.style.AppThemeBlueOrangeLight;
            case "orange_blue_light":
                return R.style.AppThemeOrangeBlueLight;

            case "yellow_purple_light":
                return R.style.AppThemeYellowPurpleLight;
            case "purple_yellow_light":
                return R.style.AppThemePurpleYellowLight;

            case "red_green_light":
                return R.style.AppThemeRedGreenLight;
            case "green_red_light":
                return R.style.AppThemeGreenRedLight;

            // Dark
            case "dark":
            case "blue_orange_dark":
                if (light)
                    return R.style.AppThemeBlueOrangeLight;
                else
                    return R.style.AppThemeBlueOrangeDark;
            case "orange_blue_dark":
                if (light)
                    return R.style.AppThemeOrangeBlueLight;
                else
                    return R.style.AppThemeOrangeBlueDark;

            case "yellow_purple_dark":
                if (light)
                    return R.style.AppThemeYellowPurpleLight;
                else
                    return R.style.AppThemeYellowPurpleDark;
            case "purple_yellow_dark":
                if (light)
                    return R.style.AppThemePurpleYellowLight;
                else
                    return R.style.AppThemePurpleYellowDark;

            case "red_green_dark":
                if (light)
                    return R.style.AppThemeRedGreenLight;
                else
                    return R.style.AppThemeRedGreenDark;
            case "green_red_dark":
                if (light)
                    return R.style.AppThemeGreenRedLight;
                else
                    return R.style.AppThemeGreenRedDark;

                // Black
            case "blue_orange_black":
                if (light)
                    return R.style.AppThemeBlueOrangeLight;
                else
                    return R.style.AppThemeBlueOrangeBlack;
            case "orange_blue_black":
                if (light)
                    return R.style.AppThemeOrangeBlueLight;
                else
                    return R.style.AppThemeOrangeBlueBlack;
            case "yellow_purple_black":
                if (light)
                    return R.style.AppThemeYellowPurpleLight;
                else
                    return R.style.AppThemeYellowPurpleBlack;
            case "purple_yellow_black":
                if (light)
                    return R.style.AppThemePurpleYellowLight;
                else
                    return R.style.AppThemePurpleYellowBlack;
            case "red_green_black":
                if (light)
                    return R.style.AppThemeRedGreenLight;
                else
                    return R.style.AppThemeRedGreenBlack;
            case "green_red_black":
                if (light)
                    return R.style.AppThemeGreenRedLight;
                else
                    return R.style.AppThemeGreenRedBlack;

                // Grey
            case "grey_light":
                return R.style.AppThemeGreySteelBlueLight;
            case "grey_dark":
                if (light)
                    return R.style.AppThemeGreySteelBlueLight;
                else
                    return R.style.AppThemeGreySteelBlueDark;

                // Solarized
            case "solarized_light":
                return R.style.AppThemeSolarizedLight;
            case "solarized":
            case "solarized_dark":
                if (light)
                    return R.style.AppThemeSolarizedLight;
                else
                    return R.style.AppThemeSolarizedDark;

                // Black
            case "blank":
                return R.style.AppThemeBlank;

            case "black":
                if (light)
                    return R.style.AppThemeGreySteelBlueLight;
                else
                    return R.style.AppThemeBlack;

            case "white":
                if (light)
                    return R.style.AppThemeGreySteelBlueLight;
                else
                    return R.style.AppThemeWhite;

            case "black_and_white":
                if (light)
                    return R.style.AppThemeGreySteelBlueLight;
                else
                    return R.style.AppThemeBlackAndWhite;

                // System
            case "system":
            case "blue_orange_system":
                return (night
                        ? R.style.AppThemeBlueOrangeDark : R.style.AppThemeBlueOrangeLight);
            case "blue_orange_system_black":
                return (night
                        ? R.style.AppThemeBlueOrangeBlack : R.style.AppThemeBlueOrangeLight);
            case "orange_blue_system":
                return (night
                        ? R.style.AppThemeOrangeBlueDark : R.style.AppThemeOrangeBlueLight);
            case "orange_blue_system_black":
                return (night
                        ? R.style.AppThemeOrangeBlueBlack : R.style.AppThemeOrangeBlueLight);
            case "yellow_purple_system":
                return (night
                        ? R.style.AppThemeYellowPurpleDark : R.style.AppThemeYellowPurpleLight);
            case "yellow_purple_system_black":
                return (night
                        ? R.style.AppThemeYellowPurpleBlack : R.style.AppThemeYellowPurpleLight);
            case "purple_yellow_system":
                return (night
                        ? R.style.AppThemePurpleYellowDark : R.style.AppThemePurpleYellowLight);
            case "purple_yellow_system_black":
                return (night
                        ? R.style.AppThemePurpleYellowBlack : R.style.AppThemePurpleYellowLight);
            case "red_green_system":
                return (night
                        ? R.style.AppThemeRedGreenDark : R.style.AppThemeRedGreenLight);
            case "red_green_system_black":
                return (night
                        ? R.style.AppThemeRedGreenBlack : R.style.AppThemeRedGreenLight);
            case "green_red_system":
                return (night
                        ? R.style.AppThemeGreenRedDark : R.style.AppThemeGreenRedLight);
            case "green_red_system_black":
                return (night
                        ? R.style.AppThemeGreenRedBlack : R.style.AppThemeGreenRedLight);
            case "grey_system":
                return (night
                        ? R.style.AppThemeGreySteelBlueDark : R.style.AppThemeGreySteelBlueLight);
            case "solarized_system":
                return (night
                        ? R.style.AppThemeSolarizedDark : R.style.AppThemeSolarizedLight);
            case "bw_system":
                return (night
                        ? R.style.AppThemeBlack : R.style.AppThemeWhite);

            // Material You

            case "you_light":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return R.style.AppThemeYouLight;
            case "you_dark":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (light ? R.style.AppThemeYouLight : R.style.AppThemeYouDark);
            case "you_black":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (light ? R.style.AppThemeYouLight : R.style.AppThemeYouBlack);
            case "you_system":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (night ? R.style.AppThemeYouDark : R.style.AppThemeYouLight);
            case "you_system_black":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (night ? R.style.AppThemeYouBlack : R.style.AppThemeYouLight);

            case "you_reversed_light":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return R.style.AppThemeYouReversedLight;
            case "you_reversed_dark":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (light ? R.style.AppThemeYouReversedLight : R.style.AppThemeYouReversedDark);
            case "you_reversed_black":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (light ? R.style.AppThemeYouReversedLight : R.style.AppThemeYouReversedBlack);
            case "you_reversed_system":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (night ? R.style.AppThemeYouReversedDark : R.style.AppThemeYouReversedLight);
            case "you_reversed_system_black":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (night ? R.style.AppThemeYouReversedBlack : R.style.AppThemeYouReversedLight);

                // Material You monochrome

            case "you_mono_light":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return R.style.AppThemeYouMonoLight;
            case "you_mono_dark":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (light ? R.style.AppThemeYouMonoLight : R.style.AppThemeYouMonoDark);
            case "you_mono_black":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (light ? R.style.AppThemeYouMonoLight : R.style.AppThemeYouMonoBlack);
            case "you_mono_system":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (night ? R.style.AppThemeYouMonoDark : R.style.AppThemeYouMonoLight);
            case "you_mono_system_black":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (night ? R.style.AppThemeYouMonoBlack : R.style.AppThemeYouMonoLight);

            case "you_mono_reversed_light":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return R.style.AppThemeYouMonoReversedLight;
            case "you_mono_reversed_dark":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (light ? R.style.AppThemeYouMonoReversedLight : R.style.AppThemeYouMonoReversedDark);
            case "you_mono_reversed_black":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (light ? R.style.AppThemeYouMonoReversedLight : R.style.AppThemeYouMonoReversedBlack);
            case "you_mono_reversed_system":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (night ? R.style.AppThemeYouMonoReversedDark : R.style.AppThemeYouMonoReversedLight);
            case "you_mono_reversed_system_black":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (night ? R.style.AppThemeYouMonoReversedBlack : R.style.AppThemeYouMonoReversedLight);

            default:
                if (!theme.startsWith("you_") && !theme.startsWith("you_mono_"))
                    Log.e("Unknown theme=" + theme);
                return (night ? R.style.AppThemeMinimalDark : R.style.AppThemeMinimalLight);
        }
    }

    // Temas de ModernEmail: prefijo guardado en la preferencia "theme"
    static final String[] FAMILIES = new String[]{"modern", "minimal", "violet", "azure", "glass"};

    private static String getFamily(int buttonId) {
        if (buttonId == R.id.rbThemeModern)
            return "modern";
        if (buttonId == R.id.rbThemeMinimal)
            return "minimal";
        if (buttonId == R.id.rbThemeViolet)
            return "violet";
        if (buttonId == R.id.rbThemeAzure)
            return "azure";
        if (buttonId == R.id.rbThemeGlass)
            return "glass";
        return null;
    }

    private static Integer getFamilyButton(String theme) {
        if (theme.startsWith("modern_"))
            return R.id.rbThemeModern;
        if (theme.startsWith("minimal_"))
            return R.id.rbThemeMinimal;
        if (theme.startsWith("violet_"))
            return R.id.rbThemeViolet;
        if (theme.startsWith("azure_"))
            return R.id.rbThemeAzure;
        if (theme.startsWith("glass_"))
            return R.id.rbThemeGlass;
        return null;
    }

    // Ajustes de visualización que acompañan a cada tema de ModernMail
    static void applyPreset(SharedPreferences.Editor editor, String family) {
        boolean azure = "azure".equals(family);

        // Lista agrupada en bloques por día
        editor.putBoolean("cards", true);
        editor.putInt("spacing", 0);
        editor.putBoolean("date", true);
        editor.putBoolean("date_fixed", false);
        editor.putBoolean("date_week", false);
        editor.putBoolean("dividers", true);
        editor.putBoolean("beige", false);

        // Fila de mensaje
        editor.putBoolean("avatars", true);
        editor.putBoolean("circular", true);
        editor.putBoolean("generated_icons", true);
        editor.putBoolean("preview", true);
        editor.putInt("preview_lines", azure ? 1 : 2);
        editor.putBoolean("preview_italic", false);
        editor.putBoolean("subject_italic", false);
        editor.putBoolean("highlight_unread", false);
        editor.putBoolean("highlight_subject", false);
        editor.putString("subject_ellipsize", "end");
        editor.putString("sender_ellipsize", "end");
        editor.putInt("view_zoom", 0);
        editor.putBoolean("authentication_indicator", false);
        editor.putInt("account_color", 0);
        editor.putBoolean("color_stripe", false);
        editor.putBoolean("quick_filter", false);

        // Lectura de un correo
        editor.putBoolean("reformatted_hint", false);
        editor.putBoolean("quick_scroll", false);
        editor.putBoolean("actionbar_color", false);

        // Redactar: la barra de envío mantiene la forma redondeada (sin color de identidad)
        editor.putBoolean("send_nav_color", false);
    }

    // Versión de los ajustes de diseño: al subirla se vuelven a aplicar una vez
    private static final int PRESET_VERSION = 5;

    // Aplica los ajustes del tema propio en uso (o del tema Aire si no hay ninguno)
    static void applyCurrentPreset(SharedPreferences prefs, SharedPreferences.Editor editor) {
        String theme = prefs.getString("theme", "minimal_system");
        String family = null;
        for (String f : FAMILIES)
            if (theme.startsWith(f + "_") && !"modern".equals(f))
                family = f;

        if (family == null) {
            family = "minimal";
            editor.putString("theme", "minimal_system");
        }
        applyPreset(editor, family);
        editor.putInt("mm_preset", PRESET_VERSION);
    }

    // Garantiza que la app usa uno de los temas propios con sus ajustes
    static void ensurePreset(Context context) {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.getInt("mm_preset", 0) >= PRESET_VERSION)
                return;

            SharedPreferences.Editor editor = prefs.edit();
            applyCurrentPreset(prefs, editor);
            editor.apply();
        } catch (Throwable ex) {
            Log.w(ex);
        }
    }

    // {claro, oscuro, negro}
    private static int[] getFamilyStyles(String family) {
        switch (family) {
            case "minimal":
                return new int[]{R.style.AppThemeMinimalLight, R.style.AppThemeMinimalDark, R.style.AppThemeMinimalBlack};
            case "violet":
                return new int[]{R.style.AppThemeVioletLight, R.style.AppThemeVioletDark, R.style.AppThemeVioletBlack};
            case "azure":
                return new int[]{R.style.AppThemeAzureLight, R.style.AppThemeAzureDark, R.style.AppThemeAzureBlack};
            case "glass":
                return new int[]{R.style.AppThemeGlassLight, R.style.AppThemeGlassDark, R.style.AppThemeGlassBlack};
            case "modern":
            default:
                return new int[]{R.style.AppThemeModernLight, R.style.AppThemeModernDark, R.style.AppThemeModernBlack};
        }
    }

    // {fondo lista claro, tarjeta clara, fondo lista oscuro, tarjeta oscura}
    private static int[] getFamilyBackgrounds(String family) {
        switch (family) {
            case "minimal":
                return new int[]{R.color.minimalLightBackground, R.color.minimalLightCard, R.color.minimalDarkBackground, R.color.minimalDarkCard};
            case "violet":
                return new int[]{R.color.violetLightBackground, R.color.violetLightCard, R.color.violetDarkBackground, R.color.violetDarkCard};
            case "azure":
                return new int[]{R.color.azureLightBackground, R.color.azureLightCard, R.color.azureDarkBackground, R.color.azureDarkCard};
            case "glass":
                return new int[]{R.color.glassLightBackground, R.color.glassLightCard, R.color.glassDarkBackground, R.color.glassDarkCard};
            case "modern":
            default:
                return new int[]{R.color.modernLightBackground, R.color.modernLightCard, R.color.modernDarkBackground, R.color.modernDarkCard};
        }
    }

    static void setBackground(Context context, View view, boolean compose) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean cards = prefs.getBoolean("cards", true);
        boolean beige = prefs.getBoolean("beige", true);
        boolean tabular_card_bg = prefs.getBoolean("tabular_card_bg", false);
        String theme = prefs.getString("theme", "minimal_system");
        boolean dark = Helper.isDarkTheme(context);
        boolean black = (theme.endsWith("black") || "black_and_white".equals(theme));
        boolean solarized = theme.startsWith("solarized");
        boolean you = theme.startsWith("you_");
        String family = null;
        for (String f : FAMILIES)
            if (theme.startsWith(f + "_"))
                family = f;

        Integer color = null;
        if (cards) {
            if (family != null) {
                int[] bg = getFamilyBackgrounds(family);
                if (!dark)
                    color = ContextCompat.getColor(context, compose ? bg[1] : bg[0]);
                else if (!black)
                    color = ContextCompat.getColor(context, compose ? bg[3] : bg[2]);
            } else if (you && !black && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (!dark && beige)
                    color = ContextCompat.getColor(context, R.color.lightColorBackground_cards_beige);
                else
                    color = ContextCompat.getColor(context, dark
                            ? android.R.color.system_background_dark
                            : android.R.color.system_background_light);
            } else {
                if (compose) {
                    if (!dark || solarized)
                        color = Helper.resolveColor(context, R.attr.colorCardBackground);
                } else {
                    if (!dark && !solarized)
                        color = ContextCompat.getColor(context, beige
                                ? R.color.lightColorBackground_cards_beige
                                : R.color.lightColorBackground_cards);
                }
            }
        } else {
            if (tabular_card_bg)
                color = Helper.resolveColor(context, R.attr.colorCardBackground);
        }

        if (color == null)
            if (dark && black)
                color = Color.BLACK;
            else {
                TypedValue a = new TypedValue();
                context.getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
                if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT)
                    color = a.data;
                else
                    color = Color.parseColor(dark ? "#121316" : "#FAF9FD");
            }

        // ModernMail: el tema Cristal usa un degradado suave en lugar de un color plano
        if (!compose && family != null && family.equals("glass") && !(dark && black)) {
            view.setBackground(getGlassBackground(context, dark));
            return;
        }

        view.setBackgroundColor(color);
    }

    static boolean isGlass(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("theme", "minimal_system").startsWith("glass_");
    }

    static android.graphics.drawable.Drawable getGlassBackground(Context context, boolean dark) {
        return new android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        ContextCompat.getColor(context, dark ? R.color.glassDarkGradTop : R.color.glassLightGradTop),
                        ContextCompat.getColor(context, dark ? R.color.glassDarkGradBottom : R.color.glassLightGradBottom)});
    }
}
