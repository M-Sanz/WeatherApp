package com.example.weather;

public class WeatherRVModal {
    private String time;
    private  String temperature;
    private  String icon;
    private  String windSpeed;

    public WeatherRVModal(String time, String temperature,  String windSpeed) {
        this.time = time;
        this.temperature = temperature;
        this.windSpeed = windSpeed;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(String windSpeed) {
        this.windSpeed = windSpeed;
    }
}
