package com.coolweather.android.util;

import android.text.TextUtils;
import android.util.Log;

import com.coolweather.android.entity.City;
import com.coolweather.android.entity.County;
import com.coolweather.android.entity.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {


    private static final String TAG = "Utility";
    /**
     * 解析和处理服务器返回的省级数据
     * @param response 传入的市级数据
     * @return 数据是否存在
     */
    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try {

                Log.d(TAG, "handleProvinceResponse: response :"+response);

                JSONArray allProvince = new JSONArray(response);
                for (int i = 0;i < allProvince.length();i++){
                    //获取对应的json对象
                    JSONObject provinceObject = allProvince.getJSONObject(i);

                    Province province = new Province();
                    Log.d(TAG, "handleProvinceResponse: "+province.toString());
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.setProvinceName(provinceObject.getString("name"));
                    province.save();  //添加到数据库
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return false;
    }


    /**
     * 解析和处理服务器返回的市级数据
     * @param response  传入的市级数据
     * @param provinceId 所属省级id
     * @return数据是否存在
     */
    public  static boolean  handleCityResponse(String response,int provinceId){
        if (TextUtils.isEmpty(response)) return false;

        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i =0;i< jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                City city = new City();
                city.setCityCode(jsonObject.getInt("id"));
                city.setCityName(jsonObject.getString("name"));
                city.setProvinceId(provinceId);
                city.save();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }




    /**
     * 解析和处理服务器返回的县级数据
     * @param response
     * @param cityId
     * @return
     */
    public static boolean handleCountyResponse(String response,int cityId){
        if (TextUtils.isEmpty(response)) return false;

        try {
            JSONArray allCounty = new JSONArray(response);
            for (int i = 0; i < allCounty.length();i++){
                JSONObject countyObj =  allCounty.getJSONObject(i);
                County county = new County();
                county.setCityId(cityId);
                county.setWeatherId(countyObj.getString("weather_id"));
                county.setCountyName(countyObj.getString("name"));
                county.save();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }


}
