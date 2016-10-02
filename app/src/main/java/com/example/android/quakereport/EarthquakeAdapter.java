package com.example.android.quakereport;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Yasuaki on 2016/09/06.
 */
public class EarthquakeAdapter extends ArrayAdapter<Earthquake> {

    private static final String LOCATION_SEPARATER = " of ";

    /**
     * カスタムコンストラクタなので、スーパークラスのコンストラクタと異なる
     *
     * @param context     レイアウトファイルをインフレーとするのに使用
     * @param earthquakes A List of AndroidFlavor objects to display in a list
     */
    public EarthquakeAdapter(Context context, List<Earthquake> earthquakes) {
        super(context, 0, earthquakes);
    }

    /**
     * AdapterView に View を提供する (ListView, GridView, etc.)
     *
     * @param position    list item view.に表示されるデータの、リスト内のポジション
     * @param convertView 配置に使用するリサイクルView
     * @param parent      インフレートに使う 親ViewGroup
     * @return AdapterView に渡す View
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);

        }
        //現在位置の Earthquake OBJ を取得
        Earthquake currentEarthquake = getItem(position);

        //マグニチュードを表示
        TextView magView = (TextView) listItemView.findViewById(R.id.magnitude);
        //double を String に変換
        Double originalMagnitude = currentEarthquake.getMagnitude();

        //数値をフォーマットしてセット
        DecimalFormat formatter = new DecimalFormat("0.0");
        String formattedDouble = formatter.format(originalMagnitude);
        magView.setText(formattedDouble);

        //マグニチュード背景のシェイプの色をcodeしていく。
        //背景のグラデーションdrawable を取得
        GradientDrawable magnitudeCircle = (GradientDrawable) magView.getBackground();

        //マグニチュードの数値によって背景色を変えるメソッドを実行
        int magnitudeColor = getMagnitudeColor(currentEarthquake.getMagnitude());
        magnitudeCircle.setColor(magnitudeColor);

        //ロケーションを表示
        String originalLocation = currentEarthquake.getPlace();
        String primaryLocation;
        String locationOffset;

        //1つの String を スプリットする処理
        if (originalLocation.contains(LOCATION_SEPARATER)) {
            String[] splittedLocation = originalLocation.split(LOCATION_SEPARATER);
            primaryLocation = splittedLocation[1];
            locationOffset = splittedLocation[0] + LOCATION_SEPARATER;
        } else {
            locationOffset = getContext().getString(R.string.near_the);
            primaryLocation = originalLocation;
        }

        //ロケーション情報をセット
        TextView offsetLocationView = (TextView) listItemView.findViewById(R.id.location_offset);
        offsetLocationView.setText(locationOffset);

        TextView primaryLocationView = (TextView) listItemView.findViewById(R.id.primary_location);
        primaryLocationView.setText(primaryLocation);

        //時間を表示
        //新規 Date OBJ を生成
        Date dateObject = new Date(currentEarthquake.getTimeInMilliseconds());

        //date を表示させるView参照を取得
        TextView dateView = (TextView) listItemView.findViewById(R.id.date);
        //date String をフォーマットして、View にセット
        String formattedDate = formatDate(dateObject);
        dateView.setText(formattedDate);

        //time を表示させるView参照を取得
        TextView timeView = (TextView) listItemView.findViewById(R.id.time);
        //time String  をフォーマットして、View にセット
        String formattedTime = formatTime(dateObject);
        timeView.setText(formattedTime);

        return listItemView;
    }

    /**
     * Date OBJ を元に、フォーマットした date String を返す
     */
    private String formatDate(Date dateObject) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLL dd, yyyy");
        return dateFormat.format(dateObject);
    }

    private String formatTime(Date dateObject) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        return timeFormat.format(dateObject);
    }

    /**
     * マグニチュードによって、対応する背景色を決めるメソッド
     */
    private int getMagnitudeColor(double magnitude) {
        int magnitudeColorResourceId;

        //Math.floor() で小数点以下を切り捨てる
        //switch は double を扱えないので int にする
        int magnitudeFloor = (int) Math.floor(magnitude);

        switch (magnitudeFloor) {
            case 0:
            case 1:
                magnitudeColorResourceId = R.color.magnitude1;
                break;
            case 2:
                magnitudeColorResourceId = R.color.magnitude2;
                break;
            case 3:
                magnitudeColorResourceId = R.color.magnitude3;
                break;
            case 4:
                magnitudeColorResourceId = R.color.magnitude4;
                break;
            case 5:
                magnitudeColorResourceId = R.color.magnitude5;
                break;
            case 6:
                magnitudeColorResourceId = R.color.magnitude6;
                break;
            case 7:
                magnitudeColorResourceId = R.color.magnitude7;
                break;
            case 8:
                magnitudeColorResourceId = R.color.magnitude8;
                break;
            case 9:
                magnitudeColorResourceId = R.color.magnitude9;
                break;
            default:
                magnitudeColorResourceId = R.color.magnitude10plus;
                break;
        }
        //コンテキスト情報とリソースIDから、実際の色情報を割り出す
        return ContextCompat.getColor(getContext(), magnitudeColorResourceId);
    }

}
