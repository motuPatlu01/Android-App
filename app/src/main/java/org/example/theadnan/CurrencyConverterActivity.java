package org.example.theadnan;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple Currency Converter Activity.
 */
public class CurrencyConverterActivity extends AppCompatActivity {

    private EditText etAmount;
    private Spinner spinnerFrom, spinnerTo;
    private Button btnConvert;
    private TextView tvResult;

    // Hardcoded rates for demo purposes
    private final Map<String, Double> rates = new HashMap<String, Double>() {{
        put("USD", 1.0);
        put("EUR", 0.92);
        put("GBP", 0.79);
        put("INR", 83.0);
        put("BDT", 121.0);
        put("JPY", 150.0);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_converter);

        etAmount = findViewById(R.id.etAmount);
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        btnConvert = findViewById(R.id.btnConvert);
        tvResult = findViewById(R.id.tvResult);

        String[] currencies = rates.keySet().toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, currencies);
        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        btnConvert.setOnClickListener(v -> convert());
    }

    private void convert() {
        String amountStr = etAmount.getText().toString();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String from = spinnerFrom.getSelectedItem().toString();
        String to = spinnerTo.getSelectedItem().toString();

        double fromRate = rates.get(from);
        double toRate = rates.get(to);

        double result = (amount / fromRate) * toRate;
        tvResult.setText(String.format("Result: %.2f %s", result, to));
    }
}
