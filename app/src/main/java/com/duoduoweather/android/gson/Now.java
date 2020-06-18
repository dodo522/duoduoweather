package com.duoduoweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2020/6/17.
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More mMore;

    public class More
    {
        @SerializedName("txt")
        public String info;
    }
}
