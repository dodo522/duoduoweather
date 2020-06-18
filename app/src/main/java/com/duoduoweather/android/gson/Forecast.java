package com.duoduoweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2020/6/17.
 */

public class Forecast {
    public String data;

    @SerializedName("tmp")
    public Temperature mTemperature;

    @SerializedName("cond")
    public More mMore;

    public class Temperature
    {
        public String max;
        public String min;
    }

    public class More
    {
        @SerializedName("txt_d")
        public String info;
    }
}
