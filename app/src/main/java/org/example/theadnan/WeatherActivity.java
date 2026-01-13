package org.example.theadnan;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * WeatherActivity: Fetches weather data using Open-Meteo (Free, No API Key required).
 */
public class WeatherActivity extends AppCompatActivity {

    private EditText etCityName;
    private Button btnGetWeather;
    private CardView cardWeatherInfo;
    private TextView tvCityDisplay, tvTemperature, tvCondition, tvHumidity, tvWindSpeed;
    private ProgressBar progressBar;

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        etCityName = findViewById(R.id.etCityName);
        btnGetWeather = findViewById(R.id.btnGetWeather);
        cardWeatherInfo = findViewById(R.id.cardWeatherInfo);
        tvCityDisplay = findViewById(R.id.tvCityDisplay);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvCondition = findViewById(R.id.tvCondition);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWindSpeed = findViewById(R.id.tvWindSpeed);
        progressBar = findViewById(R.id.progressBar);

        btnGetWeather.setOnClickListener(v -> {
            String city = etCityName.getText().toString().trim();
            if (!city.isEmpty()) {
                searchCity(city);
            } else {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchCity(String city) {
        progressBar.setVisibility(View.VISIBLE);
        cardWeatherInfo.setVisibility(View.GONE);

        String geocodeUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" + city + "&count=1&language=en&format=json";

        Request request = new Request.Builder().url(geocodeUrl).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showError("Connection failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject json = new JSONObject(responseData);
                        if (json.has("results")) {
                            JSONArray results = json.getJSONArray("results");
                            JSONObject firstResult = results.getJSONObject(0);
                            double lat = firstResult.getDouble("latitude");
                            double lon = firstResult.getDouble("longitude");
                            String name = firstResult.getString("name");
                            String country = firstResult.optString("country", "");
                            
                            fetchWeatherData(lat, lon, name + (country.isEmpty() ? "" : ", " + country));
                        } else {
                            showError("City not found");
                        }
                    } catch (Exception e) {
                        showError("Parsing error");
                    }
                } else {
                    showError("Search failed");
                }
            }
        });
    }

    private void fetchWeatherData(double lat, double lon, String displayName) {
        String weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon + "&current_weather=true";

        Request request = new Request.Builder().url(weatherUrl).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                showError("Weather data failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject json = new JSONObject(responseData);
                        JSONObject current = json.getJSONObject("current_weather");
                        
                        double temp = current.getDouble("temperature");
                        double wind = current.getDouble("windspeed");
                        int weatherCode = current.getInt("weathercode");

                        runOnUiThread(() -> displayWeather(displayName, temp, wind, weatherCode));
                    } catch (Exception e) {
                        showError("Data error");
                    }
                } else {
                    showError("Failed to get weather");
                }
            }
        });
    }

    private void displayWeather(String name, double temp, double wind, int code) {
        tvCityDisplay.setText(name);
        tvTemperature.setText(String.format("Temperature: %.1f°C", temp));
        tvCondition.setText("Condition: " + getWeatherDescription(code));
        tvHumidity.setText("Wind Direction: " + wind + "°"); // Open-Meteo simple current weather doesn't have humidity in basic call
        tvWindSpeed.setText("Wind Speed: " + wind + " km/h");

        progressBar.setVisibility(View.GONE);
        cardWeatherInfo.setVisibility(View.VISIBLE);
    }

    private String getWeatherDescription(int code) {
        switch (code) {
            case 0: return "Clear sky";
            case 1: case 2: case 3: return "Mainly clear / Part clouds";
            case 45: case 48: return "Fog";
            case 51: case 53: case 55: return "Drizzle";
            case 61: case 63: case 65: return "Rain";
            case 71: case 73: case 75: return "Snow fall";
            case 95: return "Thunderstorm";
            default: return "Overcast";
        }
    }

    private void showError(String msg) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(WeatherActivity.this, msg, Toast.LENGTH_SHORT).show();
        });
    }
}
