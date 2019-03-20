package com.liazbd.converter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.liazbd.converter.utils.ConnectionDetector;
import com.liazbd.converter.utils.JSONParser;
import com.liazbd.converter.utils.URLs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MainActivity extends AppCompatActivity {

    List<String> countryList = new ArrayList<>();
    List<String> taxDataList = new ArrayList<>();
    List<String> rateKeyList = new ArrayList<>();
    List<String> rateValueList = new ArrayList<>();

    private Spinner spinnerCountry;
    private LinearLayout layoutRadioButton;
    private RadioGroup layoutRadioGroup;
    private TextView finalAmount;
    private EditText inputAmount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init view element
        spinnerCountry = (Spinner) findViewById(R.id.spinner_country);
        layoutRadioButton = (LinearLayout) findViewById(R.id.layout_radio_item);
        finalAmount = (TextView) findViewById(R.id.final_amount_view);
        inputAmount = (EditText) findViewById(R.id.input_amount);
        layoutRadioGroup = new RadioGroup(this);
        layoutRadioGroup.setOrientation(LinearLayout.VERTICAL);

        //Fetch Data from Server
        if(ConnectionDetector.checkingInternet(this))
              new FetchData(this).execute();
          else
              Toast.makeText(this, "Please connect with a network.", Toast.LENGTH_LONG).show();

        //spinner change event
          spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 try {
                  setRadioGroupView(position);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //input amount change event
        inputAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int radioButtonPosition = layoutRadioGroup.getCheckedRadioButtonId();
                calculateFinalAmount(charSequence.toString(), rateValueList.get(radioButtonPosition));
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


    }

    class FetchData extends AsyncTask<Void, Integer, String> {
        private Activity activity;
        private ProgressDialog dialog;
        private Context context;

        public FetchData(Activity activity){
            this.activity = activity;
            this.context = activity;
            this.dialog = new ProgressDialog(this.activity);
            this.dialog.setMessage("Fetching data...");
            if(!this.dialog.isShowing()){
                this.dialog.show();
            }
        }

        protected void onPreExecute (){
            super.onPreExecute();
        }

        protected String doInBackground(Void...arg0) {
            String result = "";
            HttpUrl urlBuilder = new HttpUrl.Builder()
                    .scheme(URLs.URL_SCHEME)
                    .host(URLs.URL)
                    .build();
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(urlBuilder)
                    .addHeader("Accept", "application/json")
                    .method("GET", null)
                    .build();
            okhttp3.Response response = null;
            try {
                response = client.newCall(request).execute();
                result = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        protected void onProgressUpdate(Integer...a){
            super.onProgressUpdate(a);
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (this.dialog.isShowing())
                this.dialog.dismiss();

           try {
                JSONObject resulObject = new JSONObject(result);
               JSONArray ratesArray = resulObject.getJSONArray("rates");
               for (int i=0; i<ratesArray.length(); i++) {
                   JSONObject ratesObject = ratesArray.getJSONObject(i);
                   countryList.add(ratesObject.getString("name"));
                   taxDataList.add(ratesObject.get("periods").toString());
               }
               setSpinner(this.context);
               setRadioGroupView(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void setSpinner(Context context){
        ArrayAdapter adapter = new ArrayAdapter(context, R.layout.spinner_item, countryList);
        spinnerCountry.setAdapter(adapter);
    }

    private void setRadioGroupView(int index){
        rateKeyList.clear();
        rateValueList.clear();
        try {
            JSONArray ratesData = new JSONArray(taxDataList.get(index));
            JSONObject ratesObject = ratesData.getJSONObject(0);
            JSONObject jsonRates = ratesObject.getJSONObject("rates");
            rateKeyList = JSONParser.getObjectKeys(jsonRates);
            rateValueList = JSONParser.getObjectValues(jsonRates);
         }catch (JSONException e) {
             e.printStackTrace();
         }
        int radioButtonListSize = rateKeyList.size();
        layoutRadioGroup.removeAllViews();
        for (int i = 0; i < radioButtonListSize; i++) {
            RadioButton rdbtn = new RadioButton(getApplicationContext());
            rdbtn.setId(i);
            rdbtn.setText(rateKeyList.get(i));
            if (i == 0)
                rdbtn.setChecked(true);
            layoutRadioGroup.addView(rdbtn);
        }

        layoutRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (inputAmount.getText().toString().isEmpty())
                    return;

           calculateFinalAmount(inputAmount.getText().toString(),rateValueList.get(i));
            }
        });

        ((ViewGroup) findViewById(R.id.radio_group)).addView(layoutRadioGroup);

    }

    private void calculateFinalAmount(String inputAmout, String taxAmount){
        double finalBalance = 0;
        if (inputAmout.length() > 0) {
            double inputValue = Double.parseDouble(inputAmout);
            finalBalance = inputValue + Double.parseDouble(taxAmount);
        } else {
            finalBalance = 0;
        }
        finalAmount.setText("Total Amount with Tax = " + finalBalance);
    }

}
