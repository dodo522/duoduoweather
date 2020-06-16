package com.duoduoweather.android.db;

import org.litepal.crud.LitePalSupport;

/**
 * Created by Administrator on 2020/6/16.
 */

public class City extends LitePalSupport {
        private int id;
        private String cityName;
        private int cityCode;
        private int provinceId;

        public int getId() {
            return id;
        }

        public String getCityName() {
            return cityName;
        }

        public int getCityCode(int id) {
            return cityCode;
        }

        public int getProvinceId() {
            return provinceId;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setCityName(String cityName) {
            this.cityName = cityName;
        }

        public void setCityCode(int cityCode) {
            this.cityCode = cityCode;
        }

        public void setProvinceId(int provinceId) {
            this.provinceId = provinceId;
        }
}
