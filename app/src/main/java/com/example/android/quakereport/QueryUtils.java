package com.example.android.quakereport;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * USGS へリクエストし、取得した地震データに関するヘルパーメソッド
 */
public final class QueryUtils {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = QueryUtils.class.getSimpleName();
    private  Context mContext;


    /**
     * このクラスは static な変数及びメソッドを保持するためだけのもの。
     * なので、QueryUtils というクラス名で直接アクセスできる。
     * ※よって、QueryUtils の OBJ インスタンスは不要。コンストラクタもprivateで空っぽでよい。
     */
    private QueryUtils() {
    }

    //createUrl(), makeHttpRequest(), extractFeatureFromJson() をそれぞれ本メソッド内でコール
    //上記メソッドは全て private。本メソッドは public。本メソッドのみ他のクラスからコールして使用。
    public static List<Earthquake> fetchEarthquakeData(String requestUrl) {

        //プログレスバーのテストをするときは下記スニペットをアクティブにする
/*        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        //String を URL OBJ に変換する
        URL url = createUrl(requestUrl);

        //URL OBJ を使って HTTP リクエストを実施し、JSONレスポンスを受け取る
        String jsonResponse = null;

        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        //受け取った JSON レスポンス から、欲しいフィールドを取り出して、ArrayListに add を繰り返した結果を取得
        List<Earthquake> earthquakes = extractFeatureFromJson(jsonResponse);

        Log.i(LOG_TAG, "fetchEarthquakeData");
        // Return the {@link Earthquake}
        return earthquakes;
    }


    /**
     * String URL を URL OBJ に変換して返す
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * 引数に与えられた URL に対する HTTP リクエストを作成
     * String として返す
     */
    private static String makeHttpRequest(URL url) throws IOException {

        String jsonResponse = "";

        //URL がnull の場合、メソッドから抜ける
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();//新規 HttpURLConnection を取得
            urlConnection.setReadTimeout(10000/*milliseconds*/);
            urlConnection.setConnectTimeout(15000/*milliseconds*/);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();//リクエスト実行

            //レスポンスコードを確認。 ⇒200 なら読み込み開始。
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON result.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                //input stream をクローズする際も、IOException がスローされる可能性あり。
                //よって、本メソッドのシグニチャにて、IOException を指定してある。
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * {@link InputStream} を サーバからのJSONレスポンスを含む String に変換する
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {

            //InputStreamReader は一度に一文字ずつ読み取る
            InputStreamReader inputStreamReader =
                    new InputStreamReader(inputStream, Charset.forName("UTF-8"));

            //HD から一文字ずつ読むと凄く時間かかるので、貯めてから一塊で読むようにする
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * JSON レスポンスを パースして組み上げた Earthquake OBJ のリストを返す
     */
    public static List<Earthquake> extractFeatureFromJson(String earthquakeJSON) {
        //渡された String としての JSON が null だった場合に抜ける
        if (TextUtils.isEmpty(earthquakeJSON)) {
            return null;
        }

        //空のArrayList を作成。これから add していく。
        List<Earthquake> earthquakes = new ArrayList<>();

        /**
         * SAMPLE_JSON_RESPONSE をパースする。JSON のフォーマット方法に問題があれば、
         * JSONException 例外OBJ がスローされる。
         * アプリがクラッシュしないよう、例外をキャッチしてログに出力している。
         */
        try {
            JSONObject jsonRootObj = new JSONObject(earthquakeJSON);
            JSONArray jsonArray = jsonRootObj.optJSONArray("features");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONObject properties = jsonObject.optJSONObject("properties");

                //表示したいデータを抜き出す
                double mag = properties.getDouble("mag");
                String place = properties.optString("place");
                long mTimeInMilliseconds = properties.getLong("time");
                String url = properties.getString("url");

                //抜き出したデータを元に、Earthquake OBJ を作成
                Earthquake earthquake = new Earthquake(mag, place, mTimeInMilliseconds, url);

                //Earthquake OBJ を ArrayList に add
                earthquakes.add(earthquake);
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the earthquake JSON results", e);
        }
        return earthquakes;//Earthquake のリストを返す
    }

}
