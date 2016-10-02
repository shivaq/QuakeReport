package com.example.android.quakereport;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

/**
 * Created by Yasuaki on 2016/09/15.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);//Preference の Fragment のレイアウトファイル
    }

//    Activity
    public static class EarthquakePreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {//Preference の チェンジリスナー を実装

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);//Preference の キーやタイプの定義を取得

            //key を元に、preference OBJ を findPreference
            Preference minMagnitude = findPreference(getString(R.string.settings_min_magnitude_key));
            //title の下に サマリーとして key に対する value を表示
            bindPreferenceSummaryToValue(minMagnitude);

            //key を元に、preference OBJ を findPreference
            Preference orderBy = findPreference(getString(R.string.settings_order_by_key));
            bindPreferenceSummaryToValue(orderBy);
        }

        @Override
        //Preference の、変更された値を引数で受ける
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();//変更になった値を String として格納

            //変更された Preference がListPreference かどうかで分岐
            if(preference instanceof ListPreference){
                //ListPreference としての参照を取得
                ListPreference listPreference = (ListPreference) preference;
                //変更した ListPreference のインデックス値を取得
                int prefIndex = listPreference.findIndexOfValue(stringValue);

                if(prefIndex >= 0){
                    //Preference 定義のXML から、android:entries の値を取得。※ここでは array.xml
                    CharSequence[] labels = listPreference.getEntries();
                    //ListPreference のエントリーの配列と、インデックス値を組み合わせてサマリーをセット
                    preference.setSummary(labels[prefIndex]);//
                }
            } else {
                preference.setSummary(stringValue);//この場合はEditTextPreference
            }

            preference.setSummary(stringValue);//サマリーに値をセットし、表示
            return true;
        }

        private void bindPreferenceSummaryToValue(Preference preferenceObj) {
            //変更となった プレファレンスOBJに、リスナーをセット
            preferenceObj.setOnPreferenceChangeListener(this);
            SharedPreferences sharedPreference =
                    PreferenceManager.getDefaultSharedPreferences(preferenceObj.getContext());

            String preferenceString = sharedPreference.getString(preferenceObj.getKey(), "");
            onPreferenceChange(preferenceObj, preferenceString);
        }
    }
}
