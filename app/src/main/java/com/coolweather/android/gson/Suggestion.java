package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Suggestion {

    public Comf comf;

    public Sport sport;

    @SerializedName("cw")
    public CarWach carWach;

    public class  Comf{

        @SerializedName("txt")
        public String info;
    }

    public class  Sport{
        @SerializedName("txt")
        public String info;
    }

    public class  CarWach{
        @SerializedName("txt")
        public String info;
    }



}
