/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class EarthquakeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Earthquake>> {

    public static final String LOG_TAG = EarthquakeActivity.class.getName();

    //Loader のID。複数のLoader を使う時のみ必須となる。
    private static final int EARTHQUAKE_LOADER_ID = 1;

    /**
     * URL for earthquake data from the USGS dataset
     */
    //USGS データセットから取得する地震データの ベース URI
    private static final String USGS_REQUEST_URL = "http://earthquake.usgs.gov/fdsnws/event/1/query";

    /**
     * Adapter for the list of earthquakes
     */
    private EarthquakeAdapter mAdapter;

    private TextView mEmptyTextView;
    private ProgressBar mProgressBar;
    private int mProgressStatus = 0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        // Find a reference to the {@link ListView} in the layout
        ListView earthquakeListView = (ListView) findViewById(R.id.list);

        mProgressBar = (ProgressBar) findViewById(R.id.loading_spinner);


        mEmptyTextView = (TextView) findViewById(R.id.empty_text);
        earthquakeListView.setEmptyView(mEmptyTextView);

        //View に 空のArrayList を元にAdapter をセットする
        mAdapter = new EarthquakeAdapter(EarthquakeActivity.this, new ArrayList<Earthquake>());
        earthquakeListView.setAdapter(mAdapter);

        //ListView にアイテムクリックリスナーをセット
        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //クリックされたアイテムを見つける
                Earthquake currentEarthquake = mAdapter.getItem(position);

                //String の url を URI OBJ にパース（変換）する
                Uri earthquakeUri = Uri.parse(currentEarthquake.getUrl());

                //URI を見るための 新規Intent 作成
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);
                startActivity(websiteIntent);
            }
        });


        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //getLoaderManager と同じ機能。import android.support.v4.app.LoaderManager; しているからサポートになってる？
            LoaderManager loaderManager = getSupportLoaderManager();

            //$2: バンドル には nullを渡す
            loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, this);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mEmptyTextView.setText("インターネットコネクションがないです");
        }
        Log.i(LOG_TAG, "initLoader");


    }

    @Override
    public Loader<List<Earthquake>> onCreateLoader(int id, Bundle args) {
        //Loader で、バックグラウンドでやってほしい処理を行い、
        // onLocalFinished でその後始末をUIスレッドで行う
        //EarthquakeLoader を new するときに、データ取得元となる URL を組み上げて、渡す
        Log.i(LOG_TAG, "onCreateLoader");

        //SharedPreferences OBJ を取得
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String minMagnitude = sharedPrefs.getString(//最小 マグニチュードのデフォルト値を取得
                getString(R.string.settings_min_magnitude_key),//strings.xml からキーを取得
                getString(R.string.settings_min_magnitude_default)//デフォルト値を取得
        );


        String orderBy = sharedPrefs.getString(//ソート方法のデフォルト値を取得
                getString(R.string.settings_order_by_key),//キー
                getString(R.string.settings_order_by_default)//デフォルト値
        );

        Uri baseUri = Uri.parse(USGS_REQUEST_URL);//ベースとなる URI を URI OBJ として取得
        Uri.Builder uriBuilder = baseUri.buildUpon();//URI を ビルドするビルダーを生成

        //URI を組み上げていく
        uriBuilder.appendQueryParameter("format", "geojson");
        uriBuilder.appendQueryParameter("limit", "10");
        uriBuilder.appendQueryParameter("minmag", minMagnitude);
        uriBuilder.appendQueryParameter("orderby", orderBy);

        return new EarthquakeLoader(this, uriBuilder.toString());//組み上げた URI を元に Loader に仕事をさせる
    }

    @Override
    public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> result) {
        //前の earthquake data のAdapterをクリア
        mAdapter.clear();

        //バックグラウンドスレッドでのデータ取得の結果に、
        // 有効な List<Earthquake> があれば、Adapter のデータセットに追加
        // ⇒上記処理が、ListView の更新のトリガーとなる
        if (result != null && !result.isEmpty()) {
            mAdapter.addAll(result);//コメントアウトすると、引っ張ってこれなかった場合の状況が見られる
        }
        Log.i(LOG_TAG, "onLoadFinished");

        mProgressBar.setVisibility(View.GONE);

        //onCreate の時にセットすると、毎回文字がちらっと出てしまうので、ロードが終わってからセット
        //ロードされるたびにセットすることとなるが、コストはかからないため、受け入れてよいトレードオフ
        mEmptyTextView.setText("データを引っ張ってこれませんでした。。。");
    }

    @Override
    //Loader からの情報はもう無効だと知らされたから、onLoaderReset はコールされるわけなので、クリアしておく。
    public void onLoaderReset(Loader<List<Earthquake>> loader) {
        mAdapter.clear();
        Log.i(LOG_TAG, "onLoadReset");
    }

}
