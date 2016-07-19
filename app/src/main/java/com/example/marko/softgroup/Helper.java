package com.example.marko.softgroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Marko on 7/18/2016.
 */
public class Helper {

    public String getTranslate(String url) throws JSONException, IOException {
        JSONArray jsonArray = null;
        String value = null;

        URL urlHttp = new URL(url);
        HttpURLConnection urlConnection = (HttpsURLConnection) urlHttp.openConnection();

        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
        String json = reader.readLine();

        JSONObject jsonObject = new JSONObject(json);
        jsonArray = jsonObject.getJSONArray("text");
        value = jsonArray.get(0).toString();

        return value;
    }

    public Map<String, String> getLanguagesArray(String url) throws JSONException, IOException {

        URL urlHttp = new URL(url);
        HttpURLConnection urlConnection = (HttpsURLConnection) urlHttp.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
        String json = reader.readLine();
        JSONObject jsonObject = new JSONObject(json).getJSONObject("langs");
        JSONArray jsonArray = jsonObject.names();
        HashMap<String, String> hashMap = new HashMap<>();
        for (int i = 0; jsonArray.length() > i; i++) {
            String name, value;
            name = jsonObject.get(jsonArray.getString(i)).toString().trim();
            value = jsonArray.getString(i).trim();
            hashMap.put(name, value);
        }
        Map<String, String> map = new TreeMap<String, String>(hashMap);
        return map;
    }

    public String detectedLanguage(String url) throws JSONException, IOException {
        URL urlHttp = new URL(url);
        HttpURLConnection urlConnection = (HttpsURLConnection) urlHttp.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
        String json = reader.readLine();
        JSONObject jsonObject = new JSONObject(json);
        String language = jsonObject.getString("lang");
        return language;
    }
}
