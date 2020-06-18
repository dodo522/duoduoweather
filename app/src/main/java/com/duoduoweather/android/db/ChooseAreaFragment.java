package com.duoduoweather.android.db;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.duoduoweather.android.MainActivity;
import com.duoduoweather.android.R;
import com.duoduoweather.android.WeatherActivity;
import com.duoduoweather.android.util.HttpUtil;
import com.duoduoweather.android.util.Utility;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;
import org.litepal.exceptions.DataSupportException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2020/6/16.
 */

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY= 1;
    public static final int LEVEL_COUNTRY = 2;
    private ProgressDialog mProgressDialog;
    private TextView mTextView;
    private Button mBackButton;
    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private List<String> mDataList = new ArrayList<>();

    //省列表
    private List<Province> mProvinceList;
    //市列表
    private List<City> mCityList;
    //区列表
    private List<Country> mCountryList;

    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;//选中的级别

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.choose_area,container,false);
        mTextView = (TextView)view.findViewById(R.id.title_text);
        mBackButton  = (Button) view.findViewById(R.id.back_button);
        mListView = (ListView)view.findViewById(R.id.list_view);
        mAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,mDataList);
        mListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVINCE)
                {
                    selectedProvince = mProvinceList.get(position);
                    queryCities();
                }else if (currentLevel == LEVEL_CITY)
                {
                    selectedCity = mCityList.get(position);
                    queryCounties();
                }else if(currentLevel == LEVEL_COUNTRY){
                    String weatherId = mCountryList.get(position).getWeatherId();
                    if(getActivity() instanceof MainActivity){
                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra("weather_id",weatherId);
                    startActivity(intent);
                    getActivity().finish();
                }else if (getActivity() instanceof WeatherActivity){
                        WeatherActivity activity = (WeatherActivity)getActivity();
                        activity.mDrawerLayout.closeDrawers();
                        activity.mSwipeRefreshLayout.setRefreshing(true);
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        mBackButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if(currentLevel == LEVEL_COUNTRY)
                {
                    queryCities();
                }else if(currentLevel == LEVEL_CITY)
                {
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查寻到再到服务器上查询
     */
    private void queryProvinces()
    {
        mTextView.setText("中国");
        mBackButton.setVisibility(View.GONE);
        mProvinceList = LitePal.findAll(Province.class);
        if(mProvinceList.size()>0)
        {
            mDataList.clear();
            for(Province province:mProvinceList)
            {
                mDataList.add(province.getProvinceName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else
        {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    private void queryCities()
    {
        mTextView.setText(selectedProvince.getProvinceName());
        mBackButton.setVisibility(View.VISIBLE);
        mCityList=LitePal.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(mCityList.size()>0)
        {
            mDataList.clear();
            for(City city:mCityList)
            {
                mDataList.add(city.getCityName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }
        else
        {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }

    private void queryCounties()
    {
        mTextView.setText(selectedCity.getCityName());
        mBackButton.setVisibility(View.VISIBLE);
        mCountryList = LitePal.where("cityid = ?",String.valueOf(selectedCity.getId())).find(Country.class);
        if(mCountryList.size()>0)
        {
            mDataList.clear();
            for(Country country : mCountryList)
            {
                mDataList.add(country.getCountryName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel = LEVEL_COUNTRY;
        }else
        {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"country");
        }
    }

    private void queryFromServer(String address,final String type)
    {
        showProgressDiaglog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if("province".equals(type))
                {
                    result = Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if("country".equals(type)){
                    result = Utility.handleCountryResponse(responseText,selectedCity.getId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("country".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }

            }

            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getActivity(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });

            }

        });
    }

    private void showProgressDiaglog()
    {
        if(mProgressDialog == null){
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("正在加载");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    private void closeProgressDialog()
    {
        if(mProgressDialog != null){
            mProgressDialog.dismiss();
        }
    }
}
