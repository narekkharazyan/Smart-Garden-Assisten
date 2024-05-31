package com.example.smartgardenassistent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class weather extends AppCompatActivity {
    private EditText weatherarea;
    private Button search;
    private TextView reslut_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        weatherarea = findViewById(R.id.weatherarea);
        search = findViewById(R.id.search);
        reslut_info = findViewById(R.id.reslut_info);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (weatherarea.getText().toString().trim().equals(""))
                    Toast.makeText(weather.this, "Enter Your City!", Toast.LENGTH_SHORT).show();
                else {
                    String city = weatherarea.getText().toString();
                    String key = "3606e013763d61d26057d7d24091ce53";
                    String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + key + "&units=metric";
                    new GetUrlData().execute(url);
                }

            }
        });
    }

    private class GetUrlData extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
            reslut_info.setText("Wait...");
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null)
                    buffer.append(line).append("\n");
                return buffer.toString();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e); //kara ste lini problemy
            } catch (IOException e) {
                throw new RuntimeException(e);

            } finally {
                if (connection != null)
                    connection.disconnect();
                try {
                    if (reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(result);
                reslut_info.setText("Temperature: " + jsonObject.getJSONObject("main").getDouble("temp") + "°C");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }

}

