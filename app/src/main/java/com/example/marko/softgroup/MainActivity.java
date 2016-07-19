package com.example.marko.softgroup;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText inputTxt;
    Button translateBtn, detectBtn;
    TextView outputTxt;
    RetrieveFeedTask retrieveFeedTask;
    Map<String, String> languages;
    GetLanguagesTask getLanguagesTask;
    List<String> names;
    Spinner inputSpinner, outputSpinner;
    DetectedLanguageTask detectedLanguageTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputTxt = (EditText) findViewById(R.id.input_edit_txt);
        outputTxt = (TextView) findViewById(R.id.output_txt);

        detectBtn = (Button) findViewById(R.id.detectBtn);
        translateBtn = (Button) findViewById(R.id.translateBtn);

        inputSpinner = (Spinner) findViewById(R.id.spinner_input);
        outputSpinner = (Spinner) findViewById(R.id.spinner_output);


        getSpinnerArray();

        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (names.size() == 0)
                    getSpinnerArray();
                else if (isNetworkAvailable(MainActivity.this)) {
                    String url = "https://translate.yandex.net/api/v1.5/tr.json/detect?key=";
                    url += getString(R.string.API_KEY);
                    url += "&text=";
                    url += inputTxt.getText();
                    if (!inputTxt.getText().toString().isEmpty()) {
                        detectedLanguageTask = new DetectedLanguageTask();
                        detectedLanguageTask.execute(url);
                    }
                } else
                    writeInternetConnectionWarning();
            }
        });

        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (names.size() == 0)
                    getSpinnerArray();
                else if (isNetworkAvailable(MainActivity.this)) {
                    String url = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=";
                    url += getString(R.string.API_KEY);
                    url += "&text=";
                    url += inputTxt.getText();
                    url += "&lang=";
                    url += languages.get(inputSpinner.getSelectedItem());
                    url += "-";
                    url += languages.get(outputSpinner.getSelectedItem());
                    url += "&format=plain";
                    if (!inputTxt.getText().toString().isEmpty()) {
                        retrieveFeedTask = new RetrieveFeedTask();
                        retrieveFeedTask.execute(url);
                    }
                } else
                    writeInternetConnectionWarning();
            }
        });

        inputSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (outputSpinner.getSelectedItemPosition() == position)
                    changeSpinnerItem(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        outputSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (inputSpinner.getSelectedItemPosition() == position)
                    changeSpinnerItem(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void getSpinnerArray() {
        if (isNetworkAvailable(MainActivity.this)) {
            String url = "https://translate.yandex.net/api/v1.5/tr.json/getLangs?key=" + getString(R.string.API_KEY) + "&ui=uk";
            getLanguagesTask = new GetLanguagesTask();
            getLanguagesTask.execute(url);
        } else
            writeInternetConnectionWarning();
    }

    private void writeInternetConnectionWarning() {
        Toast.makeText(MainActivity.this, "Будь ласка перевірте підключення до інтернету", Toast.LENGTH_LONG).show();
    }

    class GetLanguagesTask extends AsyncTask<String, String, Map<String, String>> {

        @Override
        protected Map<String, String> doInBackground(String... urls) {
            TranslationService translationService = new TranslationService();

            try {
                languages = translationService.getLanguagesArray(urls[0]);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return languages;
        }

        @Override
        protected void onPostExecute(Map<String, String> map) {
            names = new ArrayList<>(map.keySet());
            ArrayAdapter<String> inputAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.spinner_item, names);
            inputSpinner.setAdapter(inputAdapter);
            ArrayAdapter<String> outputAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.spinner_item, names);
            outputSpinner.setAdapter(outputAdapter);
            changeSpinnerItem(true);
            super.onPostExecute(map);
        }
    }

    class RetrieveFeedTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            TranslationService translationService = new TranslationService();
            String value = null;
            try {
                value = translationService.getTranslate(urls[0]);
            } catch (JSONException e) {
                publishProgress("Cannot get response");
            } catch (IOException e) {
                publishProgress("Unknown error");
            }

            return value;
        }

        @Override
        protected void onPostExecute(String s) {
            outputTxt.setText(Html.fromHtml(s));
            super.onPostExecute(s);
        }
    }

    class DetectedLanguageTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            TranslationService translationService = new TranslationService();
            String value = null;
            try {
                value = translationService.detectedLanguage(urls[0]);
            } catch (JSONException e) {
                publishProgress("Cannot get response");
            } catch (IOException e) {
                publishProgress("Unknown error");
            }

            return value;
        }

        @Override
        protected void onPostExecute(String s) {
            String name = null;
            for (Map.Entry<String, String> entry : languages.entrySet()) {
                if (entry.getValue().equals(s)) {
                    name = entry.getKey();
                }
            }

            if (name != null) {
                inputSpinner.setSelection(names.indexOf(name));
                Toast.makeText(MainActivity.this, "Визначена мова: " + name, Toast.LENGTH_LONG).show();
                if (inputSpinner.getSelectedItemPosition() == outputSpinner.getSelectedItemPosition())
                    changeSpinnerItem(true);
            }
            super.onPostExecute(s);
        }
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void changeSpinnerItem(boolean firstSpinnerSelected) {
        int position;
        if (firstSpinnerSelected) {
            position = outputSpinner.getSelectedItemPosition();
            if (position < names.size() - 1)
                outputSpinner.setSelection(position + 1);
            else
                outputSpinner.setSelection(position - 1);
        } else {
            position = inputSpinner.getSelectedItemPosition();
            if (position < names.size() - 1)
                inputSpinner.setSelection(position + 1);
            else
                inputSpinner.setSelection(position - 1);
        }
    }
}
