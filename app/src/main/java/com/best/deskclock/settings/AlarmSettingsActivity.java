// SPDX-License-Identifier: GPL-3.0-only

package com.best.deskclock.settings;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import com.best.deskclock.R;
import com.best.deskclock.Utils;
import com.best.deskclock.data.DataModel;
import com.best.deskclock.data.Weekdays;
import com.best.deskclock.ringtone.RingtonePickerActivity;
import com.best.deskclock.widget.CollapsingToolbarBaseActivity;

public class AlarmSettingsActivity extends CollapsingToolbarBaseActivity {

    private static final String PREFS_FRAGMENT_TAG = "alarm_settings_fragment";

    public static final String KEY_DEFAULT_ALARM_RINGTONE = "key_default_alarm_ringtone";
    public static final String KEY_AUTO_SILENCE = "key_auto_silence";
    public static final String KEY_ALARM_SNOOZE = "key_snooze_duration";
    public static final String KEY_ALARM_VOLUME_SETTING = "key_volume_setting";
    public static final String KEY_ALARM_CRESCENDO = "key_alarm_crescendo_duration";
    public static final String KEY_SWIPE_ACTION = "key_swipe_action";
    public static final String KEY_VOLUME_BUTTONS = "key_volume_button_setting";
    public static final String DEFAULT_VOLUME_BEHAVIOR = "-1";
    public static final String VOLUME_BEHAVIOR_CHANGE_VOLUME = "0";
    public static final String VOLUME_BEHAVIOR_SNOOZE = "1";
    public static final String VOLUME_BEHAVIOR_DISMISS = "2";
    public static final String KEY_POWER_BUTTON = "key_power_button";
    public static final String DEFAULT_POWER_BEHAVIOR = "0";
    public static final String POWER_BEHAVIOR_SNOOZE = "1";
    public static final String POWER_BEHAVIOR_DISMISS = "2";
    public static final String KEY_FLIP_ACTION = "key_flip_action";
    public static final String KEY_SHAKE_ACTION = "key_shake_action";
    public static final String KEY_WEEK_START = "key_week_start";
    public static final String KEY_ALARM_NOTIFICATION_REMINDER_TIME = "key_alarm_notification_reminder_time";
    public static final String KEY_ENABLE_ALARM_VIBRATIONS_BY_DEFAULT = "key_enable_alarm_vibrations_by_default";
    public static final String KEY_ENABLE_DELETE_OCCASIONAL_ALARM_BY_DEFAULT = "key_enable_delete_occasional_alarm_by_default";
    public static final String KEY_MATERIAL_TIME_PICKER_STYLE = "key_material_time_picker_style";
    public static final String MATERIAL_TIME_PICKER_ANALOG_STYLE = "analog";
    public static final String KEY_ALARM_DISPLAY_CUSTOMIZATION = "key_alarm_display_customization";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new PrefsFragment(), PREFS_FRAGMENT_TAG)
                    .disallowAddToBackStack()
                    .commit();
        }
    }

    public static class PrefsFragment extends ScreenFragment implements
            Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

        Preference mAlarmRingtonePref;
        ListPreference mAutoSilencePref;
        ListPreference mAlarmSnoozePref;
        ListPreference mAlarmCrescendoPref;
        SwitchPreferenceCompat mSwipeActionPref;
        ListPreference mVolumeButtonsPref;
        ListPreference mPowerButtonPref;
        ListPreference mFlipActionPref;
        ListPreference mShakeActionPref;
        ListPreference mWeekStartPref;
        ListPreference mAlarmNotificationReminderTimePref;
        SwitchPreferenceCompat mEnableAlarmVibrationsByDefaultPref;
        SwitchPreferenceCompat mDeleteOccasionalAlarmByDefaultPref;
        ListPreference mMaterialTimePickerStylePref;
        Preference mAlarmDisplayCustomizationPref;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings_alarm);

            mAlarmRingtonePref = findPreference(KEY_DEFAULT_ALARM_RINGTONE);
            mAutoSilencePref = findPreference(KEY_AUTO_SILENCE);
            mAlarmSnoozePref = findPreference(KEY_ALARM_SNOOZE);
            mAlarmCrescendoPref = findPreference(KEY_ALARM_CRESCENDO);
            mSwipeActionPref = findPreference(KEY_SWIPE_ACTION);
            mVolumeButtonsPref = findPreference(KEY_VOLUME_BUTTONS);
            mPowerButtonPref = findPreference(KEY_POWER_BUTTON);
            mFlipActionPref = findPreference(KEY_FLIP_ACTION);
            mShakeActionPref = findPreference(KEY_SHAKE_ACTION);
            mWeekStartPref = findPreference(KEY_WEEK_START);
            mAlarmNotificationReminderTimePref = findPreference(KEY_ALARM_NOTIFICATION_REMINDER_TIME);
            mEnableAlarmVibrationsByDefaultPref = findPreference(KEY_ENABLE_ALARM_VIBRATIONS_BY_DEFAULT);
            mDeleteOccasionalAlarmByDefaultPref = findPreference(KEY_ENABLE_DELETE_OCCASIONAL_ALARM_BY_DEFAULT);
            mMaterialTimePickerStylePref = findPreference(KEY_MATERIAL_TIME_PICKER_STYLE);
            mAlarmDisplayCustomizationPref = findPreference(KEY_ALARM_DISPLAY_CUSTOMIZATION);

            hidePreferences();
        }

        @Override
        public void onResume() {
            super.onResume();

            refresh();
        }

        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            switch (pref.getKey()) {
                case KEY_DEFAULT_ALARM_RINGTONE ->
                        pref.setSummary(DataModel.getDataModel().getAlarmRingtoneTitle());

                case KEY_AUTO_SILENCE -> {
                    final String delay = (String) newValue;
                    updateAutoSnoozeSummary((ListPreference) pref, delay);
                }

                case KEY_SWIPE_ACTION, KEY_ENABLE_ALARM_VIBRATIONS_BY_DEFAULT,
                     KEY_ENABLE_DELETE_OCCASIONAL_ALARM_BY_DEFAULT ->
                        Utils.setVibrationTime(requireContext(), 50);

                case KEY_ALARM_SNOOZE, KEY_ALARM_CRESCENDO, KEY_VOLUME_BUTTONS,
                     KEY_POWER_BUTTON, KEY_FLIP_ACTION, KEY_SHAKE_ACTION,
                     KEY_ALARM_NOTIFICATION_REMINDER_TIME, KEY_MATERIAL_TIME_PICKER_STYLE -> {
                    final ListPreference preference = (ListPreference) pref;
                    final int index = preference.findIndexOfValue((String) newValue);
                    preference.setSummary(preference.getEntries()[index]);
                }

                case KEY_WEEK_START -> {
                    final ListPreference preference = (ListPreference) pref;
                    final int index = preference.findIndexOfValue((String) newValue);
                    preference.setSummary(preference.getEntries()[index]);
                    // Set result so DeskClock knows to refresh itself
                    requireActivity().setResult(RESULT_OK);
                }
            }

            return true;
        }

        @Override
        public boolean onPreferenceClick(@NonNull Preference pref) {
            final Context context = getActivity();
            if (context == null) {
                return false;
            }

            switch (pref.getKey()) {
                case KEY_DEFAULT_ALARM_RINGTONE ->
                    startActivity(RingtonePickerActivity.createAlarmRingtonePickerIntentForSettings(context));

                case KEY_ALARM_DISPLAY_CUSTOMIZATION ->
                    startActivity(new Intent(context, AlarmDisplayCustomizationActivity.class));
            }

            return true;
        }

        private void hidePreferences() {
            final boolean hasVibrator = ((Vibrator) mEnableAlarmVibrationsByDefaultPref.getContext()
                    .getSystemService(VIBRATOR_SERVICE)).hasVibrator();
            mEnableAlarmVibrationsByDefaultPref.setVisible(hasVibrator);
        }

        private void refresh() {
            mAlarmRingtonePref.setOnPreferenceClickListener(this);
            mAlarmRingtonePref.setSummary(DataModel.getDataModel().getAlarmRingtoneTitle());

            String delay = mAutoSilencePref.getValue();
            updateAutoSnoozeSummary(mAutoSilencePref, delay);
            mAutoSilencePref.setOnPreferenceChangeListener(this);

            mAlarmSnoozePref.setOnPreferenceChangeListener(this);
            mAlarmSnoozePref.setSummary(mAlarmSnoozePref.getEntry());

            mAlarmCrescendoPref.setOnPreferenceChangeListener(this);
            mAlarmCrescendoPref.setSummary(mAlarmCrescendoPref.getEntry());

            mSwipeActionPref.setChecked(DataModel.getDataModel().isSwipeActionEnabled());
            mSwipeActionPref.setOnPreferenceChangeListener(this);

            mVolumeButtonsPref.setOnPreferenceChangeListener(this);
            mVolumeButtonsPref.setSummary(mVolumeButtonsPref.getEntry());

            mPowerButtonPref.setOnPreferenceChangeListener(this);
            mPowerButtonPref.setSummary(mPowerButtonPref.getEntry());

            setupFlipOrShakeAction(mFlipActionPref);
            setupFlipOrShakeAction(mShakeActionPref);

            // Set the default first day of the week programmatically
            final Weekdays.Order weekdayOrder = DataModel.getDataModel().getWeekdayOrder();
            final Integer firstDay = weekdayOrder.getCalendarDays().get(0);
            final String value = String.valueOf(firstDay);
            final int index = mWeekStartPref.findIndexOfValue(value);
            mWeekStartPref.setValueIndex(index);
            mWeekStartPref.setSummary(mWeekStartPref.getEntries()[index]);
            mWeekStartPref.setOnPreferenceChangeListener(this);

            mAlarmNotificationReminderTimePref.setOnPreferenceChangeListener(this);
            mAlarmNotificationReminderTimePref.setSummary(mAlarmNotificationReminderTimePref.getEntry());

            mEnableAlarmVibrationsByDefaultPref.setChecked(DataModel.getDataModel().areAlarmVibrationsEnabledByDefault());
            mEnableAlarmVibrationsByDefaultPref.setOnPreferenceChangeListener(this);

            mDeleteOccasionalAlarmByDefaultPref.setChecked(DataModel.getDataModel().isOccasionalAlarmDeletedByDefault());
            mDeleteOccasionalAlarmByDefaultPref.setOnPreferenceChangeListener(this);

            mMaterialTimePickerStylePref.setOnPreferenceChangeListener(this);
            mMaterialTimePickerStylePref.setSummary(mMaterialTimePickerStylePref.getEntry());

            mAlarmDisplayCustomizationPref.setOnPreferenceClickListener(this);
        }

        private void updateAutoSnoozeSummary(ListPreference listPref, String delay) {
            int i = Integer.parseInt(delay);
            if (i == -1) {
                listPref.setSummary(R.string.auto_silence_never);
            } else if (i == -2) {
                listPref.setSummary(R.string.auto_silence_at_the_end_of_the_ringtone);
            } else {
                listPref.setSummary(Utils.getNumberFormattedQuantityString(requireActivity(),
                        R.plurals.auto_silence_summary, i));
            }
        }

        private void setupFlipOrShakeAction(ListPreference preference) {
            if (preference != null) {
                SensorManager sensorManager = (SensorManager) requireActivity()
                        .getSystemService(Context.SENSOR_SERVICE);
                if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
                    preference.setValue("0");  // Turn it off
                    preference.setVisible(false);
                } else {
                    preference.setSummary(preference.getEntry());
                    preference.setOnPreferenceChangeListener(this);
                }
            }
        }
    }
}
