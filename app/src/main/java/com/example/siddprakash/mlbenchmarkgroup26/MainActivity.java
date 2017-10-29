package com.example.siddprakash.mlbenchmarkgroup26;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MLBENCHMARK";

    private float dataSplit = 0.5f;
    private int algorithm = 0;

    private EditText dSplit;
    private Spinner algo;
    private Button saveParams;

    private Context context;
    private String jsonTrainData;


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.datasetName);
        tv.setText(stringFromJNI());

        // Get the split percentage value from text input
        dSplit = (EditText) findViewById(R.id.DSplit);
        dSplit.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                dataSplit = Float.valueOf(v.getText().toString());
                Log.d(TAG, "Data Split= "+dataSplit*100+"%");
                return false;
            }
        });

        // Get the type of ML algorithm selected by the user
        algo = (Spinner) findViewById(R.id.ML_spinner);
        algo.setOnItemSelectedListener(new SpinnerActivity());

        // Read the data set file

        saveParams = (Button) findViewById(R.id.algoButton);
        saveParams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               try {
                   jsonTrainData = getJsonFromResource(context, dataSplit);
                   System.out.println(jsonTrainData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        // Create the training - testing split of the data set file


        // Training Code
        // Send the data set file to server


        // Testing Code

    }

    public static String getJsonFromResource(Context context, float dSplit) throws IOException {

        AssetManager text = context.getAssets();
        InputStream inputStream = text.open("breast-cancer-wisconsin.data");
        System.out.println(inputStream);
        BufferedReader r = new BufferedReader( new InputStreamReader( inputStream ) );
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        String jsonString = null;
        int lineCount = 0;
        int limit;
        int count = 0;

        try {
            while ((line = r.readLine()) != null) {

                lineCount++;
            }
        }
        catch (Exception e) {
            Log.e( "GetJsonFromResource", Log.getStackTraceString( e ) );
        }

        limit = (int) (dSplit * lineCount);

        r = new BufferedReader(new InputStreamReader(inputStream));
        try {
            while (count <= limit ) {
                line = r.readLine();
                stringBuilder.append( line );
            }
            jsonString = stringBuilder.toString();
        }
        catch (Exception e) {
            Log.e( "GetJsonFromResource", Log.getStackTraceString( e ) );
        }
        return jsonString;
    }

    public class SpinnerActivity extends MainActivity implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            // An item was selected. You can retrieve the selected item using
            // parent.getItemAtPosition(pos)

            switch (parent.getItemAtPosition(pos).toString()){
                case "Logistic Regression": algorithm = 1;
                    Toast.makeText(parent.getContext(),
                            "ML Algorithm selected : " + parent.getItemAtPosition(pos).toString(),
                            Toast.LENGTH_SHORT).show();
                    break;
                case "Náive Bayes Classifier": algorithm = 2;
                    Toast.makeText(parent.getContext(),
                            "ML Algorithm selected : " + parent.getItemAtPosition(pos).toString(),
                            Toast.LENGTH_SHORT).show();
                    break;
                case "k-Nearest Neighbour": algorithm = 3;
                    Toast.makeText(parent.getContext(),
                            "ML Algorithm selected : " + parent.getItemAtPosition(pos).toString(),
                            Toast.LENGTH_SHORT).show();
                    break;
                case "Support Vector Machine": algorithm = 4;
                    Toast.makeText(parent.getContext(),
                            "ML Algorithm selected : " + parent.getItemAtPosition(pos).toString(),
                            Toast.LENGTH_SHORT).show();
                    break;
                default: Toast.makeText(MainActivity.this, "None", Toast.LENGTH_LONG).show();
            }
            Log.d(TAG, "Algorithm selected: "+algorithm);
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // Another interface callback
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
