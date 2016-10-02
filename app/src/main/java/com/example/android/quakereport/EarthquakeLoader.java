package com.example.android.quakereport;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.util.List;

/**
 * Created by Yasuaki on 2016/09/13.
 */

//AsyntTaskLoader のジェネリクスにて、ロードされるデータのデータ型を指定
public class EarthquakeLoader extends AsyncTaskLoader<List<Earthquake>> {

    private static final String LOG_TAG = EarthquakeLoader.class.getName();

    //クエリに使う URL
    private String mUrl;

    /**
     * @param context Activity のコンテキスト
     * @param url データのロード元の URL を渡す
     */
    public EarthquakeLoader(Context context, String url){
        super(context);
        mUrl = url;
    }

    //loadInBackground() をトリガーするには forceLoad が必要なので、オーバーライド
    @Override
    protected void onStartLoading() {
        forceLoad();
        Log.i(LOG_TAG,"onStartLoading");
    }

    @Override
    public List<Earthquake> loadInBackground() {
        if(mUrl == null){
            return null;
        }

        List<Earthquake> earthquakes = QueryUtils.fetchEarthquakeData(mUrl);
        Log.i(LOG_TAG,"loadInBackground");
        return earthquakes;
    }
}
