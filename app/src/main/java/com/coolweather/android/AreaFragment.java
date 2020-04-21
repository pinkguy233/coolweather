package com.coolweather.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.coolweather.android.entity.City;
import com.coolweather.android.entity.County;
import com.coolweather.android.entity.Province;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AreaFragment extends Fragment {

    /**
     * 当前选中的级别 省
     */
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_RITY = 1;
    public  static final  int LEVEL_COUNTY = 2;

    private Button backButton;
    private TextView  titleText;
    private ListView listView;

    private List<String> dataList =new ArrayList<>();
    private ArrayAdapter<String> adapter;

    /**
     * 当前选中的级别
     */
    private int currentLevel;

    /**
     * 省列表
     */
    private List<Province> provinceList;

    /**
     * 市列表
     */
    private List<City> cityList;

    /**
     * 县列表
     */
    private List<County> countyList;

    /**
     * 选中的省
     */
    private Province selectProvince;

    /**
     * 选中的市
     */
    private City selectCity;

    /**
     * 选中的县
     */
    private County selectCounty;


    private ProgressDialog progressDialog;

    private static final String TAG = "AreaFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //获取布局
       View view =  inflater.inflate(R.layout.choose_area,container,false);
        //初始化 布局中的控件
       titleText = view.findViewById(R.id.title_text);
       backButton = view.findViewById(R.id.back_button);
       listView = view.findViewById(R.id.list_view);

        LitePal.getDatabase();


       //填充 listview
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        queryProvinses();
        return  view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {


        //返回按钮
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentLevel == LEVEL_RITY){
                    queryProvinses();
                }else if (currentLevel == LEVEL_COUNTY){
                    queryCities();
                }
            }
        });


        //条目点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.d(TAG, "onItemClick: "+position);
                Log.d(TAG, "onActivityCreated: 当前选中状态是："+currentLevel);
                switch (currentLevel){
                    case LEVEL_PROVINCE:
                        selectProvince  = provinceList.get(position);  //获取当前选中的省
                        queryCities();
                        break;
                    case LEVEL_RITY:
                        selectCity  =  cityList.get(position);  //获取当前选中的市
                        queryCounties();
                        break;
                    case LEVEL_COUNTY:
                        selectCounty  = countyList.get(position);  //获取当前选中的县
                        Log.d(TAG, "onItemClick: "+selectCounty);
                        Intent intent = new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",selectCounty.getWeatherId());
                        startActivity(intent);
                        getActivity().finish();
                        break;
                }
            }
        });
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * 初始化 省界面
     */
    private void queryProvinses(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE); //隐藏s
        provinceList = DataSupport.findAll(Province.class);//查询所有

        if (provinceList.size() > 0){
            dataList.clear(); //移除集合中所有数据
            for (Province province:provinceList){
            dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged(); //通知Activity更新ListView
            listView.setSelection(0); //默认选中文本
            currentLevel = LEVEL_PROVINCE;

        }else {
            String address = "http://guolin.tech/api/china";

            //
            queryFromServer(address,"province");
        }
    }

    /**
     * 初始化 市区界面
     */
    private void queryCities(){
        titleText.setText(selectProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);


        cityList  = DataSupport.where("provinceid = ?", String.valueOf(selectProvince.getId())).find(City.class);

        if (cityList.size() > 0){
            dataList.clear();
            for (City city :cityList){
                dataList.add(city.getCityName());
            }
            currentLevel = LEVEL_RITY;
            adapter.notifyDataSetChanged();
            listView.setSelection(0);

        }else {
            String address = "http://guolin.tech/api/china/"+selectProvince.getId();
            queryFromServer(address,"city");
        }
    }


    private void queryCounties(){

        titleText.setText(selectCity.getCityName());
        backButton.setVisibility(View.VISIBLE);

        Log.d(TAG, "queryCounties: "+selectCity.getId());

        countyList  = DataSupport.where("cityid = ?", String.valueOf(selectCity.getId())).find(County.class);

        if (countyList.size() > 0){
            dataList.clear();
            for (County county :countyList){

                dataList.add(county.getCountyName());
            }
            currentLevel = LEVEL_COUNTY ;
            adapter.notifyDataSetChanged();
            listView.setSelection(0);



        }else {
            int PirovinceId = selectProvince.getId();
            int CityId = selectCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+PirovinceId+"/"+CityId;
            queryFromServer(address,"county");
        }



    }

    /**
     *进度条对话框
     */
    private void showProgressDialog(){
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closePrgressdialog(){
        if (progressDialog != null) progressDialog.dismiss();
    }


    /**
     *  从服务器获取数据
     * @param address 地址
     * @param type 类型
     */
    private void queryFromServer(String address, final String type){

        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String body = response.body().string();

                Log.d(TAG, "onResponse: body-"+body);

                boolean result = false;
                if ("province".equals(type)){
                   result = Utility.handleProvinceResponse(body);
                 }else  if ("city".equals(type)){
                    result = Utility.handleCityResponse(body,selectProvince.getId());
                }else  if ("county".equals(type)){
                    result = Utility.handleCountyResponse(body,selectCity.getId());
                }

                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closePrgressdialog();
                            if ("province".equals(type)){
                                queryProvinses();
                            }else  if ("city".equals(type)){
                                queryCities();
                            }else  if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }

            }

            @Override
            public void onFailure(Call call, IOException e) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closePrgressdialog();
                        Toast.makeText(getActivity(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }




        });




    }




}
