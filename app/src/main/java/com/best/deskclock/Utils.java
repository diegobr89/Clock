/*
 * Copyright (C) 2015 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package com.best.deskclock;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.appwidget.AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY;
import static android.appwidget.AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD;
import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static android.graphics.Bitmap.Config.ARGB_8888;

import static com.best.deskclock.settings.InterfaceCustomizationActivity.BLACK_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.BLACK_NIGHT_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.BLUE_GRAY_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.BROWN_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.DARK_THEME;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.GREEN_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.INDIGO_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.KEY_AMOLED_DARK_MODE;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.KEY_DEFAULT_DARK_MODE;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.LIGHT_THEME;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.BLUE_GRAY_NIGHT_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.BROWN_NIGHT_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.GREEN_NIGHT_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.INDIGO_NIGHT_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.ORANGE_NIGHT_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.PINK_NIGHT_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.PURPLE_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.PURPLE_NIGHT_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.RED_NIGHT_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.ORANGE_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.PINK_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.RED_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.SYSTEM_THEME;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.YELLOW_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.YELLOW_NIGHT_ACCENT_COLOR;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.ArraySet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.AnyRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableKt;

import com.best.deskclock.alarms.AlarmStateManager;
import com.best.deskclock.data.DataModel;
import com.best.deskclock.provider.AlarmInstance;
import com.best.deskclock.uidata.UiDataModel;
import com.best.deskclock.widget.CollapsingToolbarBaseActivity;
import com.google.android.material.color.MaterialColors;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {

    /**
     * {@link Uri} signifying the "silent" ringtone.
     */
    public static final Uri RINGTONE_SILENT = Uri.EMPTY;

    public static void enforceMainLooper() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalAccessError("May only call from main thread.");
        }
    }

    public static void enforceNotMainLooper() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            throw new IllegalAccessError("May not call from main thread.");
        }
    }

    /**
     * @param resourceId identifies an application resource
     * @return the Uri by which the application resource is accessed
     */
    public static Uri getResourceUri(Context context, @AnyRes int resourceId) {
        return new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(context.getPackageName())
                .path(String.valueOf(resourceId))
                .build();
    }

    /**
     * @param view the scrollable view to test
     * @return {@code true} iff the {@code view} content is currently scrolled to the top
     */
    public static boolean isScrolledToTop(View view) {
        return !view.canScrollVertically(-1);
    }

    /**
     * Calculate the amount by which the radius of a CircleTimerView should be offset by any
     * of the extra painted objects.
     */
    public static float calculateRadiusOffset(float strokeSize, float dotStrokeSize, float markerStrokeSize) {
        return Math.max(strokeSize, Math.max(dotStrokeSize, markerStrokeSize));
    }

    /**
     * Configure the clock that is visible to display seconds. The clock that is not visible never
     * displays seconds to avoid it scheduling unnecessary ticking runnable.
     */
    public static void setClockSecondsEnabled(DataModel.ClockStyle clockStyle, TextClock digitalClock,
                                              AnalogClock analogClock, boolean displaySeconds) {

        switch (clockStyle) {
            case ANALOG -> {
                setTimeFormat(digitalClock, false);
                analogClock.enableSeconds(displaySeconds);
                return;
            }
            case DIGITAL -> {
                analogClock.enableSeconds(false);
                setTimeFormat(digitalClock, displaySeconds);
                return;
            }
        }

        throw new IllegalStateException("unexpected clock style: " + clockStyle);
    }

    /**
     * Set whether the digital or analog clock should be displayed in the application.
     * Returns the view to be displayed.
     */
    public static void setClockStyle(DataModel.ClockStyle clockStyle, View digitalClock, View analogClock) {
        switch (clockStyle) {
            case ANALOG -> {
                final Context context = analogClock.getContext();
                // Optimally adjusts the height and the width of the analog clock when displayed
                // on a tablet or phone in portrait or landscape mode
                if (isTablet(context) || isLandscape(context)) {
                    analogClock.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    analogClock.getLayoutParams().width = LinearLayout.LayoutParams.WRAP_CONTENT;
                } else {
                    analogClock.getLayoutParams().height = toPixel(240, context);
                    analogClock.getLayoutParams().width = toPixel(240, context);
                }

                analogClock.setVisibility(View.VISIBLE);
                digitalClock.setVisibility(View.GONE);
                return;
            }
            case DIGITAL -> {
                digitalClock.setVisibility(View.VISIBLE);
                analogClock.setVisibility(View.GONE);
                return;
            }
        }

        throw new IllegalStateException("unexpected clock style: " + clockStyle);
    }

    /**
     * Update and return the PendingIntent corresponding to the given {@code intent}.
     *
     * @param context the Context in which the PendingIntent should start the service
     * @param intent  an Intent describing the service to be started
     * @return a PendingIntent that will start a service
     */
    public static PendingIntent pendingServiceIntent(Context context, Intent intent) {
        return PendingIntent.getService(context, 0, intent, FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
    }

    /**
     * Update and return the PendingIntent corresponding to the given {@code intent}.
     *
     * @param context the Context in which the PendingIntent should start the activity
     * @param intent  an Intent describing the activity to be started
     * @return a PendingIntent that will start an activity
     */
    public static PendingIntent pendingActivityIntent(Context context, Intent intent) {
        // explicitly set the flag here, as getActivity() documentation states we must do so
        return PendingIntent.getActivity(context, 0, intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
    }

    /**
     * @return The next alarm from {@link AlarmManager}
     */
    public static String getNextAlarm(Context context) {
        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        final AlarmClockInfo info = getNextAlarmClock(am);
        if (info != null) {
            final long triggerTime = info.getTriggerTime();
            final Calendar alarmTime = Calendar.getInstance();
            alarmTime.setTimeInMillis(triggerTime);
            return AlarmUtils.getFormattedTime(context, alarmTime);
        }

        return null;
    }

    /**
     * @return The next alarm title
     */
    public static String getNextAlarmTitle(Context context) {
        AlarmInstance instance = AlarmStateManager.getNextFiringAlarm(context);
        if (instance != null) {
            return AlarmUtils.getAlarmTitle(context, instance);
        }
        return null;
    }

    private static AlarmClockInfo getNextAlarmClock(AlarmManager am) {
        return am.getNextAlarmClock();
    }

    public static void updateNextAlarm(AlarmManager am, AlarmClockInfo info, PendingIntent op) {
        am.setAlarmClock(info, op);
    }

    public static boolean isAlarmWithin24Hours(AlarmInstance alarmInstance) {
        final Calendar nextAlarmTime = alarmInstance.getAlarmTime();
        final long nextAlarmTimeMillis = nextAlarmTime.getTimeInMillis();
        return nextAlarmTimeMillis - System.currentTimeMillis() <= DateUtils.DAY_IN_MILLIS;
    }

    /**
     * Clock views can call this to refresh their alarm to the next upcoming value.
     */
    public static void refreshAlarm(Context context, View clock) {
        final TextView nextAlarmIconView = clock.findViewById(R.id.nextAlarmIcon);
        final TextView nextAlarmView = clock.findViewById(R.id.nextAlarm);
        if (nextAlarmView == null) {
            return;
        }

        final String alarm = getNextAlarm(context);
        if (!TextUtils.isEmpty(alarm)) {
            final String description = context.getString(R.string.next_alarm_description, alarm);
            nextAlarmView.setText(alarm);
            nextAlarmView.setContentDescription(description);
            nextAlarmView.setVisibility(View.VISIBLE);
            nextAlarmIconView.setVisibility(View.VISIBLE);
            nextAlarmIconView.setContentDescription(description);
        } else {
            nextAlarmView.setVisibility(View.GONE);
            nextAlarmIconView.setVisibility(View.GONE);
        }
    }

    public static void setClockIconTypeface(View clock) {
        final TextView nextAlarmIconView = clock.findViewById(R.id.nextAlarmIcon);
        nextAlarmIconView.setTypeface(UiDataModel.getUiDataModel().getAlarmIconTypeface());
    }

    /**
     * Clock views can call this to refresh their date.
     **/
    public static void updateDate(String dateSkeleton, String descriptionSkeleton, View clock) {
        final TextView dateDisplay = clock.findViewById(R.id.date);
        if (dateDisplay == null) {
            return;
        }

        final Locale l = Locale.getDefault();
        String datePattern = DateFormat.getBestDateTimePattern(l, dateSkeleton);
        if (dateDisplay.getContext() instanceof ScreensaverActivity || dateDisplay.getContext() instanceof Screensaver) {
            // Add a "Thin Space" (\u2009) at the end of the date to prevent its display from being cut off on some devices.
            // (The display of the date is only cut off at the end if it is defined in italics in the screensaver settings).
            final boolean isScreensaverDateInItalic = DataModel.getDataModel().isScreensaverDateInItalic();
            final boolean isScreensaverNextAlarmInItalic = DataModel.getDataModel().isScreensaverNextAlarmInItalic();
            if (isScreensaverDateInItalic) {
                datePattern = "\u2009" + DateFormat.getBestDateTimePattern(l, dateSkeleton) + "\u2009";
            } else if (isScreensaverNextAlarmInItalic) {
                datePattern = "\u2009" + DateFormat.getBestDateTimePattern(l, dateSkeleton);
            }
        }

        final String descriptionPattern = DateFormat.getBestDateTimePattern(l, descriptionSkeleton);
        final Date now = new Date();
        dateDisplay.setText(new SimpleDateFormat(datePattern, l).format(now));
        dateDisplay.setVisibility(View.VISIBLE);
        dateDisplay.setContentDescription(new SimpleDateFormat(descriptionPattern, l).format(now));
    }

    /**
     * Formats the time in the TextClock according to the Locale with a special
     * formatting treatment for the am/pm label.
     *
     * @param clock          TextClock to format
     * @param includeSeconds whether or not to include seconds in the clock's time
     */
    public static void setTimeFormat(TextClock clock, boolean includeSeconds) {
        if (clock != null) {
            // Get the best format for 12 hours mode according to the locale
            clock.setFormat12Hour(get12ModeFormat(clock.getContext(), 0.4f, includeSeconds));
            // Get the best format for 24 hours mode according to the locale
            clock.setFormat24Hour(get24ModeFormat(clock.getContext(), includeSeconds));
        }
    }

    /**
     * For screensavers to set whether the digital or analog clock should be displayed.
     * Returns the view to be displayed.
     *
     * @param digitalClock if the view concerned is the digital clock
     * @param analogClock  if the view concerned is the analog clock
     */
    public static void setScreensaverClockStyle(View digitalClock, View analogClock) {
        final DataModel.ClockStyle screensaverClockStyle = DataModel.getDataModel().getScreensaverClockStyle();
        switch (screensaverClockStyle) {
            case ANALOG -> {
                final Context context = analogClock.getContext();
                analogClock.getLayoutParams().height = toPixel(isTablet(context) ? 300 : 220, context);
                analogClock.getLayoutParams().width = toPixel(isTablet(context) ? 300 : 220, context);
                digitalClock.setVisibility(View.GONE);
                analogClock.setVisibility(View.VISIBLE);
                return;
            }
            case DIGITAL -> {
                digitalClock.setVisibility(View.VISIBLE);
                analogClock.setVisibility(View.GONE);
                return;
            }
        }

        throw new IllegalStateException("unexpected clock style: " + screensaverClockStyle);
    }

    /**
     * For screensaver, dim the color.
     */
    public static void dimScreensaverView(Context context, View view, int color) {
        String colorFilter = getScreensaverColorFilter(context, color);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);

        paint.setColorFilter(new PorterDuffColorFilter(Color.parseColor(colorFilter), PorterDuff.Mode.SRC_IN));

        view.setLayerType(View.LAYER_TYPE_HARDWARE, paint);
    }

    /**
     * For screensaver, calculate the color filter to use to dim the color.
     *
     * @param color the color selected in the screensaver color picker
     */
    public static String getScreensaverColorFilter(Context context, int color) {
        final int brightnessPercentage = DataModel.getDataModel().getScreensaverBrightness();

        if (areScreensaverClockDynamicColors()) {
            color = context.getColor(R.color.md_theme_inversePrimary);
        }

        String colorFilter = String.format("%06X", 0xFFFFFF & color);
        // The alpha channel should range from 16 (10 hex) to 192 (C0 hex).
        String alpha = String.format("%02X", 16 + (192 * brightnessPercentage / 100));

        colorFilter = "#" + alpha + colorFilter;

        return colorFilter;
    }

    public static boolean areScreensaverClockDynamicColors() {
        return DataModel.getDataModel().areScreensaverClockDynamicColors();
    }

    /**
     * For screensaver, configure the clock that is visible to display seconds. The clock that is not visible never
     * displays seconds to avoid it scheduling unnecessary ticking runnable.
     *
     * @param digitalClock if the view concerned is the digital clock
     * @param analogClock  if the view concerned is the analog clock
     */
    public static void setScreensaverClockSecondsEnabled(TextClock digitalClock, AnalogClock analogClock) {
        final boolean areScreensaverClockSecondsDisplayed = DataModel.getDataModel().areScreensaverClockSecondsDisplayed();
        final DataModel.ClockStyle screensaverClockStyle = DataModel.getDataModel().getScreensaverClockStyle();
        switch (screensaverClockStyle) {
            case ANALOG -> {
                setScreensaverTimeFormat(digitalClock, false);
                analogClock.enableSeconds(areScreensaverClockSecondsDisplayed);
                return;
            }
            case DIGITAL -> {
                analogClock.enableSeconds(false);
                setScreensaverTimeFormat(digitalClock, areScreensaverClockSecondsDisplayed);
                return;
            }
        }

        throw new IllegalStateException("unexpected clock style: " + screensaverClockStyle);
    }

    /**
     * For screensaver, format the digital clock to be bold and/or italic or not.
     *
     * @param screensaverDigitalClock TextClock to format
     * @param includeSeconds          whether seconds are displayed or not
     */
    public static void setScreensaverTimeFormat(TextClock screensaverDigitalClock, boolean includeSeconds) {
        final boolean isScreensaverDigitalClockInBold = DataModel.getDataModel().isScreensaverDigitalClockInBold();
        final boolean isScreensaverDigitalClockInItalic = DataModel.getDataModel().isScreensaverDigitalClockInItalic();

        if (screensaverDigitalClock == null) {
            return;
        }

        screensaverDigitalClock.setFormat12Hour(get12ModeFormat(screensaverDigitalClock.getContext(), 0.4f, includeSeconds));
        screensaverDigitalClock.setFormat24Hour(get24ModeFormat(screensaverDigitalClock.getContext(), includeSeconds));

        if (isScreensaverDigitalClockInBold && isScreensaverDigitalClockInItalic) {
            screensaverDigitalClock.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
        } else if (isScreensaverDigitalClockInBold) {
            screensaverDigitalClock.setTypeface(Typeface.DEFAULT_BOLD);
        } else if (isScreensaverDigitalClockInItalic) {
            screensaverDigitalClock.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        } else {
            screensaverDigitalClock.setTypeface(Typeface.DEFAULT);
        }
    }

    /**
     * For screensaver, format the date and the next alarm to be bold and/or italic or not.
     *
     * @param date Date to format
     */
    public static void setScreensaverDateFormat(TextView date) {
        final boolean isScreensaverDateInBold = DataModel.getDataModel().isScreensaverDateInBold();
        final boolean isScreensaverDateInItalic = DataModel.getDataModel().isScreensaverDateInItalic();

        if (isScreensaverDateInBold && isScreensaverDateInItalic) {
            date.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
        } else if (isScreensaverDateInBold) {
            date.setTypeface(Typeface.DEFAULT_BOLD);
        } else if (isScreensaverDateInItalic) {
            date.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        } else {
            date.setTypeface(Typeface.DEFAULT);
        }
    }

    /**
     * For screensaver, format the date and the next alarm to be bold and/or italic or not.
     *
     * @param nextAlarm Next alarm to format
     */
    public static void setScreensaverNextAlarmFormat(TextView nextAlarm) {
        final boolean isScreensaverNextAlarmInBold = DataModel.getDataModel().isScreensaverNextAlarmInBold();
        final boolean isScreensaverNextAlarmInItalic = DataModel.getDataModel().isScreensaverNextAlarmInItalic();
        if (nextAlarm == null) {
            return;
        }
        if (isScreensaverNextAlarmInBold && isScreensaverNextAlarmInItalic) {
            nextAlarm.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
        } else if (isScreensaverNextAlarmInBold) {
            nextAlarm.setTypeface(Typeface.DEFAULT_BOLD);
        } else if (isScreensaverNextAlarmInItalic) {
            nextAlarm.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        } else {
            nextAlarm.setTypeface(Typeface.DEFAULT);
        }
    }

    /**
     * For screensaver, set the margins and the style of the clock.
     */
    public static void setScreensaverMarginsAndClockStyle(final Context context, final View clock) {
        final View mainClockView = clock.findViewById(R.id.main_clock);

        // Margins
        final int mainClockMarginLeft = toPixel(isTablet(context) ? 20 : 16, context);
        final int mainClockMarginRight = toPixel(isTablet(context) ? 20 : 16, context);
        final int mainClockMarginTop = toPixel(isTablet(context)
                ? isLandscape(context) ? 32 : 48
                : isLandscape(context) ? 16 : 24, context);
        final int mainClockMarginBottom = toPixel(isTablet(context) ? 20 : 16, context);
        final ViewGroup.MarginLayoutParams paramsForMainClock = (ViewGroup.MarginLayoutParams) mainClockView.getLayoutParams();
        paramsForMainClock.setMargins(mainClockMarginLeft, mainClockMarginTop, mainClockMarginRight, mainClockMarginBottom);
        mainClockView.setLayoutParams(paramsForMainClock);

        final int digitalClockMarginBottom = toPixel(isTablet(context) ? -18 : -8, context);
        final ViewGroup.MarginLayoutParams paramsForDigitalClock = (ViewGroup.MarginLayoutParams) mainClockView.getLayoutParams();
        paramsForMainClock.setMargins(0, 0, 0, digitalClockMarginBottom);
        mainClockView.setLayoutParams(paramsForDigitalClock);

        final int analogClockMarginBottom = toPixel(isLandscape(context)
                ? 5
                : isTablet(context) ? 18 : 14, context);
        final ViewGroup.MarginLayoutParams paramsForAnalogClock = (ViewGroup.MarginLayoutParams) mainClockView.getLayoutParams();
        paramsForMainClock.setMargins(0, 0, 0, analogClockMarginBottom);
        mainClockView.setLayoutParams(paramsForAnalogClock);

        // Style
        final AnalogClock analogClock = mainClockView.findViewById(R.id.analog_clock);
        final TextClock textClock = mainClockView.findViewById(R.id.digital_clock);
        final TextView date = mainClockView.findViewById(R.id.date);
        final TextView nextAlarmIcon = mainClockView.findViewById(R.id.nextAlarmIcon);
        final TextView nextAlarm = mainClockView.findViewById(R.id.nextAlarm);
        final int screenSaverClockColorPicker = DataModel.getDataModel().getScreensaverClockColorPicker();
        final int screensaverDateColorPicker = DataModel.getDataModel().getScreensaverDateColorPicker();
        final int screensaverNextAlarmColorPicker = DataModel.getDataModel().getScreensaverNextAlarmColorPicker();

        setScreensaverClockStyle(textClock, analogClock);
        dimScreensaverView(context, textClock, screenSaverClockColorPicker);
        dimScreensaverView(context, analogClock, screenSaverClockColorPicker);
        dimScreensaverView(context, date, screensaverDateColorPicker);
        dimScreensaverView(context, nextAlarmIcon, screensaverNextAlarmColorPicker);
        dimScreensaverView(context, nextAlarm, screensaverNextAlarmColorPicker);
        setScreensaverClockSecondsEnabled(textClock, analogClock);
        setScreensaverDateFormat(date);
        setClockIconTypeface(nextAlarmIcon);
        setScreensaverNextAlarmFormat(nextAlarm);
    }

    /**
     * @param amPmRatio      a value between 0 and 1 that is the ratio of the relative size of the
     *                       am/pm string to the time string
     * @param includeSeconds whether or not to include seconds in the time string
     * @return format string for 12 hours mode time, not including seconds
     */
    public static CharSequence get12ModeFormat(Context context, float amPmRatio, boolean includeSeconds) {
        String pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(),
                includeSeconds ? "hmsa" : "hma");

        // Replace spaces with "Hair Space"
        pattern = pattern.replaceAll("\\s", "\u200A");

        if (amPmRatio <= 0) {
            pattern = pattern.replaceAll("a", "").trim();
        } else {
            if (context instanceof ScreensaverActivity || context instanceof Screensaver) {
                final boolean isScreensaverDigitalClockInItalic = DataModel.getDataModel().isScreensaverDigitalClockInItalic();
                if (isScreensaverDigitalClockInItalic) {
                    // For screensaver, add a "Hair Space" (\u200A) at the end of the AM/PM to prevent
                    // its display from being cut off on some devices when in italic.
                    pattern = pattern.replaceAll("a", "a" + "\u200A");
                }
            }
        }

        // Build a spannable so that the am/pm will be formatted
        int amPmPos = pattern.indexOf('a');
        if (amPmPos == -1) {
            return pattern;
        }

        final Spannable sp = new SpannableString(pattern);
        sp.setSpan(new RelativeSizeSpan(amPmRatio), amPmPos, amPmPos + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp.setSpan(new StyleSpan(Typeface.NORMAL), amPmPos, amPmPos + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp.setSpan(new TypefaceSpan("sans-serif-bold"), amPmPos, amPmPos + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return sp;
    }

    public static CharSequence get24ModeFormat(Context context, boolean includeSeconds) {
        if (context instanceof ScreensaverActivity || context instanceof Screensaver) {
            final boolean isScreensaverDigitalClockInItalic = DataModel.getDataModel().isScreensaverDigitalClockInItalic();
            if (isScreensaverDigitalClockInItalic) {
                // For screensaver, add a "Hair Space" (\u200A) at the end of the time to prevent
                // its display from being cut off on some devices when in italic.
                return DateFormat.getBestDateTimePattern(Locale.getDefault(), includeSeconds ? "Hms" : "Hm") + "\u2009";
            } else {
                return DateFormat.getBestDateTimePattern(Locale.getDefault(), includeSeconds ? "Hms" : "Hm");
            }
        } else {
            return DateFormat.getBestDateTimePattern(Locale.getDefault(), includeSeconds ? "Hms" : "Hm");
        }
    }

    /**
     * Returns string denoting the timezone hour offset (e.g. GMT -8:00)
     *
     * @param useShortForm Whether to return a short form of the header that rounds to the
     *                     nearest hour and excludes the "GMT" prefix
     */
    public static String getGMTHourOffset(TimeZone timezone, boolean useShortForm) {
        final int gmtOffset = timezone.getRawOffset();
        final long hour = gmtOffset / DateUtils.HOUR_IN_MILLIS;
        final long min = (Math.abs(gmtOffset) % DateUtils.HOUR_IN_MILLIS) / DateUtils.MINUTE_IN_MILLIS;

        if (useShortForm) {
            return String.format(Locale.ENGLISH, "%+d", hour);
        } else {
            return String.format(Locale.ENGLISH, "GMT %+d:%02d", hour, min);
        }
    }

    /**
     * Given a point in time, return the subsequent moment any of the time zones changes days.
     * e.g. Given 8:00pm on 1/1/2016 and time zones in LA and NY this method would return a Date for
     * midnight on 1/2/2016 in the NY timezone since it changes days first.
     *
     * @param time  a point in time from which to compute midnight on the subsequent day
     * @param zones a collection of time zones
     * @return the nearest point in the future at which any of the time zones changes days
     */
    public static Date getNextDay(Date time, Collection<TimeZone> zones) {
        Calendar next = null;
        for (TimeZone tz : zones) {
            final Calendar c = Calendar.getInstance(tz);
            c.setTime(time);

            // Advance to the next day.
            c.add(Calendar.DAY_OF_YEAR, 1);

            // Reset the time to midnight.
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);

            if (next == null || c.compareTo(next) < 0) {
                next = c;
            }
        }

        return next == null ? null : next.getTime();
    }

    public static String getNumberFormattedQuantityString(Context context, int id, int quantity) {
        final String localizedQuantity = NumberFormat.getInstance().format(quantity);
        return context.getResources().getQuantityString(id, quantity, localizedQuantity);
    }

    /**
     * @return {@code true} iff the widget is being hosted in a container where tapping is allowed
     */
    public static boolean isWidgetClickable(AppWidgetManager widgetManager, int widgetId) {
        final Bundle wo = widgetManager.getAppWidgetOptions(widgetId);
        return wo != null && wo.getInt(OPTION_APPWIDGET_HOST_CATEGORY, -1) != WIDGET_CATEGORY_KEYGUARD;
    }

    /**
     * This method assumes the given {@code view} has already been layed out.
     *
     * @return a Bitmap containing an image of the {@code view} at its current size
     */
    public static Bitmap createBitmap(View view) {
        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    /**
     * Convenience method for creating card background.
     */
    public static Drawable cardBackground (Context context) {
        final boolean isCardBackgroundDisplayed = DataModel.getDataModel().isCardBackgroundDisplayed();
        final int radius = toPixel(12, context);
        final GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(radius);
        if (isCardBackgroundDisplayed) {
            gradientDrawable.setColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurface, Color.BLACK));
        } else {
            gradientDrawable.setColor(Color.TRANSPARENT);
        }

        final boolean isCardBorderDisplayed = DataModel.getDataModel().isCardBorderDisplayed();
        if (isCardBorderDisplayed) {
            gradientDrawable.setShape(GradientDrawable.RECTANGLE);
            gradientDrawable.setStroke(toPixel(2, context),
                    MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimary, Color.BLACK)
            );
        }

        return gradientDrawable;
    }

    /**
     * Convenience method for scaling Drawable.
     */
    public static BitmapDrawable toScaledBitmapDrawable(Context context, int drawableResId, float scale) {
        final Drawable drawable = AppCompatResources.getDrawable(context, drawableResId);
        if (drawable == null) return null;
        return new BitmapDrawable(context.getResources(), DrawableKt.toBitmap(drawable,
                (int) (scale * drawable.getIntrinsicHeight()), (int) (scale * drawable.getIntrinsicWidth()), null));
    }

    /**
     * Convenience method for converting dp to pixel.
     */
    public static int toPixel(int dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    /**
     * {@link ArraySet} is @hide prior to {@link Build.VERSION_CODES#M}.
     */
    public static <E> ArraySet<E> newArraySet(Collection<E> collection) {
        final ArraySet<E> arraySet = new ArraySet<>(collection.size());
        arraySet.addAll(collection);
        return arraySet;
    }

    /**
     * @param context from which to query the current device configuration
     * @return {@code true} if the device is currently in portrait or reverse portrait orientation
     */
    public static boolean isPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT;
    }

    /**
     * @param context from which to query the current device configuration
     * @return {@code true} if the device is currently in landscape or reverse landscape orientation
     */
    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE;
    }

    /**
     * @param context from which to query the current device
     * @return {@code true} if the device is a tablet
     */
    public static boolean isTablet(Context context) {
        return context.getResources().getBoolean(R.bool.rotateAlarmAlert);
    }

    public static long now() {
        return DataModel.getDataModel().elapsedRealtime();
    }

    public static long wallClock() {
        return DataModel.getDataModel().currentTimeMillis();
    }

    /**
     * @param context          to obtain strings.
     * @param displayMinutes   whether or not minutes should be included
     * @param isAhead          {@code true} if the time should be marked 'ahead', else 'behind'
     * @param hoursDifferent   the number of hours the time is ahead/behind
     * @param minutesDifferent the number of minutes the time is ahead/behind
     * @return String describing the hours/minutes ahead or behind
     */
    public static String createHoursDifferentString(Context context, boolean displayMinutes,
                                                    boolean isAhead, int hoursDifferent, int minutesDifferent) {

        String timeString;
        if (displayMinutes && hoursDifferent != 0) {
            // Both minutes and hours
            final String hoursShortQuantityString = Utils.getNumberFormattedQuantityString(context, R.plurals.hours_short, Math.abs(hoursDifferent));
            final String minsShortQuantityString = Utils.getNumberFormattedQuantityString(context, R.plurals.minutes_short, Math.abs(minutesDifferent));
            final @StringRes int stringType = isAhead ? R.string.world_hours_minutes_ahead : R.string.world_hours_minutes_behind;
            timeString = context.getString(stringType, hoursShortQuantityString, minsShortQuantityString);
        } else {
            // Minutes alone or hours alone
            final String hoursQuantityString = Utils.getNumberFormattedQuantityString(context, R.plurals.hours, Math.abs(hoursDifferent));
            final String minutesQuantityString = Utils.getNumberFormattedQuantityString(context, R.plurals.minutes, Math.abs(minutesDifferent));
            final @StringRes int stringType = isAhead ? R.string.world_time_ahead : R.string.world_time_behind;
            timeString = context.getString(stringType, displayMinutes ? minutesQuantityString : hoursQuantityString);
        }
        return timeString;
    }

    /**
     * @param context The context from which to obtain strings
     * @param hours   Hours to display (if any)
     * @param minutes Minutes to display (if any)
     * @param seconds Seconds to display
     * @return Provided time formatted as a String
     */
    static String getTimeString(Context context, int hours, int minutes, int seconds) {
        if (hours != 0) {
            return context.getString(R.string.hours_minutes_seconds, hours, minutes, seconds);
        }
        if (minutes != 0) {
            return context.getString(R.string.minutes_seconds, minutes, seconds);
        }
        return context.getString(R.string.seconds, seconds);
    }

    /**
     * Set the vibration duration if the device is equipped with a vibrator and if vibrations are enabled in the settings.
     *
     * @param context to define whether the device is equipped with a vibrator.
     * @param milliseconds Hours to display (if any)
     */
    public static void setVibrationTime(Context context, long milliseconds) {
        final boolean isVibrationsEnabled = DataModel.getDataModel().isVibrationsEnabled();
        final Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator.hasVibrator() && isVibrationsEnabled) {
            vibrator.vibrate(milliseconds);
        }
    }

    /**
     * @return {@code true} if the device is in dark mode.
     * @param res Access application resources.
     */
    public static boolean isNight(final Resources res) {
        return (res.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * Apply the theme and the accent color to the activities.
     */
    public static void applyThemeAndAccentColor(final AppCompatActivity activity) {
        final String theme = DataModel.getDataModel().getTheme();
        final String darkMode = DataModel.getDataModel().getDarkMode();
        final String accentColor = DataModel.getDataModel().getAccentColor();
        final boolean isAutoNightAccentColorEnabled = DataModel.getDataModel().isAutoNightAccentColorEnabled();
        final String nightAccentColor = DataModel.getDataModel().getNightAccentColor();

        if (darkMode.equals(KEY_DEFAULT_DARK_MODE)) {
            switch (theme) {
                case SYSTEM_THEME ->
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                case LIGHT_THEME ->
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                case DARK_THEME ->
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        } else if (darkMode.equals(KEY_AMOLED_DARK_MODE)
                && !theme.equals(SYSTEM_THEME) || !theme.equals(LIGHT_THEME)) {
                activity.setTheme(R.style.AmoledTheme);
        }

        if (isAutoNightAccentColorEnabled) {
            switch (accentColor) {
                case BLACK_ACCENT_COLOR -> activity.setTheme(R.style.BlackAccentColor);
                case BLUE_GRAY_ACCENT_COLOR -> activity.setTheme(R.style.BlueGrayAccentColor);
                case BROWN_ACCENT_COLOR -> activity.setTheme(R.style.BrownAccentColor);
                case GREEN_ACCENT_COLOR -> activity.setTheme(R.style.GreenAccentColor);
                case INDIGO_ACCENT_COLOR -> activity.setTheme(R.style.IndigoAccentColor);
                case ORANGE_ACCENT_COLOR -> activity.setTheme(R.style.OrangeAccentColor);
                case PINK_ACCENT_COLOR -> activity.setTheme(R.style.PinkAccentColor);
                case PURPLE_ACCENT_COLOR -> activity.setTheme(R.style.PurpleAccentColor);
                case RED_ACCENT_COLOR -> activity.setTheme(R.style.RedAccentColor);
                case YELLOW_ACCENT_COLOR -> activity.setTheme(R.style.YellowAccentColor);
            }
        } else {
            if (isNight(activity.getResources())) {
                switch (nightAccentColor) {
                    case BLACK_NIGHT_ACCENT_COLOR -> activity.setTheme(R.style.BlackAccentColor);
                    case BLUE_GRAY_NIGHT_ACCENT_COLOR -> activity.setTheme(R.style.BlueGrayAccentColor);
                    case BROWN_NIGHT_ACCENT_COLOR -> activity.setTheme(R.style.BrownAccentColor);
                    case GREEN_NIGHT_ACCENT_COLOR -> activity.setTheme(R.style.GreenAccentColor);
                    case INDIGO_NIGHT_ACCENT_COLOR -> activity.setTheme(R.style.IndigoAccentColor);
                    case ORANGE_NIGHT_ACCENT_COLOR -> activity.setTheme(R.style.OrangeAccentColor);
                    case PINK_NIGHT_ACCENT_COLOR -> activity.setTheme(R.style.PinkAccentColor);
                    case PURPLE_NIGHT_ACCENT_COLOR -> activity.setTheme(R.style.PurpleAccentColor);
                    case RED_NIGHT_ACCENT_COLOR -> activity.setTheme(R.style.RedAccentColor);
                    case YELLOW_NIGHT_ACCENT_COLOR -> activity.setTheme(R.style.YellowAccentColor);
                }
            } else {
                switch (accentColor) {
                    case BLACK_ACCENT_COLOR -> activity.setTheme(R.style.BlackAccentColor);
                    case BLUE_GRAY_ACCENT_COLOR -> activity.setTheme(R.style.BlueGrayAccentColor);
                    case BROWN_ACCENT_COLOR -> activity.setTheme(R.style.BrownAccentColor);
                    case GREEN_ACCENT_COLOR -> activity.setTheme(R.style.GreenAccentColor);
                    case INDIGO_ACCENT_COLOR -> activity.setTheme(R.style.IndigoAccentColor);
                    case ORANGE_ACCENT_COLOR -> activity.setTheme(R.style.OrangeAccentColor);
                    case PINK_ACCENT_COLOR -> activity.setTheme(R.style.PinkAccentColor);
                    case PURPLE_ACCENT_COLOR -> activity.setTheme(R.style.PurpleAccentColor);
                    case RED_ACCENT_COLOR -> activity.setTheme(R.style.RedAccentColor);
                    case YELLOW_ACCENT_COLOR -> activity.setTheme(R.style.YellowAccentColor);
                }
            }
        }

        if (activity instanceof CollapsingToolbarBaseActivity) {
            if (isNight(activity.getResources()) && darkMode.equals(KEY_AMOLED_DARK_MODE)) {
                activity.getWindow().setNavigationBarColor(Color.BLACK);
                activity.getWindow().getDecorView().setBackgroundColor(Color.BLACK);
            } else {
                activity.getWindow().setNavigationBarColor(
                        MaterialColors.getColor(activity, android.R.attr.colorBackground, Color.BLACK)
                );
            }
        } else {
            if (isNight(activity.getResources()) && darkMode.equals(KEY_AMOLED_DARK_MODE)) {
                activity.getWindow().setNavigationBarColor(Color.BLACK);
                activity.getWindow().getDecorView().setBackgroundColor(Color.BLACK);
            }
        }
    }

    /**
     * @param context The context from which to obtain the duration
     * @param ringtoneUri the ringtone path
     * @return the duration of the ringtone
     */
    public static int getRingtoneDuration(Context context, Uri ringtoneUri) {
        // Using the MediaMetadataRetriever method causes a bug when using the default ringtone:
        // the ringtone stops before the end of the melody.
        // So, we'll use the MediaPlayer class to obtain the ringtone duration.
        // Bug found with debug version on Huawei (Android 12) and Samsung (Android 14) devices.

        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(context, ringtoneUri);
            mediaPlayer.prepare();
            return mediaPlayer.getDuration();
        } catch (IOException e) {
            LogUtils.e("Error while preparing MediaPlayer", e);
            return 0;
        } finally {
            mediaPlayer.release();
        }
    }

    /**
     * Checks if the user is pressing inside of the timer circle or the stopwatch circle.
     */
    public static final class CircleTouchListener implements View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            final int actionMasked = event.getActionMasked();
            if (actionMasked != MotionEvent.ACTION_DOWN) {
                return false;
            }
            final float rX = view.getWidth() / 2f;
            final float rY = (view.getHeight() - view.getPaddingBottom()) / 2f;
            final float r = Math.min(rX, rY);

            final float x = event.getX() - rX;
            final float y = event.getY() - rY;

            final boolean inCircle = Math.pow(x / r, 2.0) + Math.pow(y / r, 2.0) <= 1.0;

            // Consume the event if it is outside the circle
            return !inCircle;
        }
    }

}
