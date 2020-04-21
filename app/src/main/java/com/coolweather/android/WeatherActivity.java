package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {


    private static final String TAG = "WeatherActivity";

    private LinearLayout linearLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingPicImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21){
          View decorView =  getWindow().getDecorView();
          decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
          getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);
        initView();
    }




    /**
     * 初始化 控件
     */
    public void initView() {
        linearLayout = findViewById(R.id.linearLayout);
        titleCity = linearLayout.findViewById(R.id.title_city);
        titleUpdateTime = linearLayout.findViewById(R.id.title_update_time);

        degreeText = linearLayout.findViewById(R.id.degre_text);
        weatherInfoText = linearLayout.findViewById(R.id.weather_info_text);

        aqiText = linearLayout.findViewById(R.id.aqi_text);
        pm25Text = linearLayout.findViewById(R.id.pm25_texts);

        forecastLayout = linearLayout.findViewById(R.id.forecast_layout);

        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);

        bingPicImg = findViewById(R.id.bing_pic_img);




        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = sp.getString("weather", null);

        String bingPic = sp.getString("bing_pic", null);

        if (bingPic == null){
            loadBingPic();
        }else {
            Glide.with(this).load(bingPic).into(bingPicImg);
        }


        if (weatherString == null){
            Intent intent = getIntent();
            String weather_id = intent.getStringExtra("weather_id");
            //解析服务器json数据，并且 展示ui
            requestWeather(weather_id);
        }else {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            initUI(weather);
        }
    }


    /**
     * 加载每日必应
     */
    private void  loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";

        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor =  PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
               editor.putString("bing_pic",bingPic);
                editor.apply();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });

            }
        });


    }

    /**
     * 解析服务器数据
     *
     * @param weaterId
     */
    public void requestWeather(final String weaterId) {

        String url = "http://guolin.tech/api/weather?cityid=" + weaterId + "&key=eff1f84056a64097ac8fb78eca604b45";

        Log.d(TAG, "requestWeather: url====" + url);

        HttpUtil.sendOkHttpRequest(url, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: 获取天气信息失败");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String responseText = response.body().string();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(responseText);
                        final Weather weather = Utility.handleWeatherResponse(responseText);

                        if (weather != null) {
                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                            SharedPreferences.Editor edit = sp.edit();
                            edit.putString("weather", responseText);
                            edit.apply();
                            Toast.makeText(getApplicationContext(), "存储成功", Toast.LENGTH_SHORT).show();
                            initUI(weather);
                        }

                    }
                });


            }
        });


    }

    /**
     * 初始化ui
     *
     * @param weather
     */
    public void initUI(final Weather weather) {

        if (this instanceof Activity) {

            String cityName = weather.basic.cityName;
            String[] split = weather.basic.update.UpdateTime.split(" ");
            String temperature = weather.now.temperature + "°C";
            String weatherinfo = weather.now.info;

            forecastLayout.removeAllViews();
            for (Forecast forecast : weather.forecastList) {
                View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
                TextView dataText = view.findViewById(R.id.date_text);
                TextView infoText = view.findViewById(R.id.info_text);
                TextView maxText = view.findViewById(R.id.max_text);
                TextView minText = view.findViewById(R.id.min_text);

                dataText.setText(forecast.date);
                infoText.setText(forecast.more.info);
                maxText.setText(forecast.temperature.max);
                minText.setText(forecast.temperature.min);
                forecastLayout.addView(view);
            }

            if (weather.aqi != null) {
                    aqiText.setText(weather.aqi.city.aqi);
                pm25Text.setText(weather.aqi.city.pm25);
            }

            titleCity.setText(cityName);
            titleUpdateTime.setText(split[1]);
            degreeText.setText(temperature);
            weatherInfoText.setText(weatherinfo);

            String comfort = "舒适度：" + weather.suggestion.comf.info;
            String carWash = "洗车指数：" + weather.suggestion.carWach.info;
            String sport = "运动建议: " + weather.suggestion.sport.info;

            comfortText.setText(comfort);
            carWashText.setText(carWash);
            sportText.setText(sport);

            linearLayout.setVisibility(View.VISIBLE);
        }


    }


}

