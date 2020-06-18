package com.duoduoweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2020/6/17.
 */

public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weather_id;

    public  Update mUpdate;

    public class Update
    {
        @SerializedName("loc")
        public String updateTime;
    }


}
