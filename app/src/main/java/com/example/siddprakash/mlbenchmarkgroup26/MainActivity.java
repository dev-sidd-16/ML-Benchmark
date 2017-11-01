package com.example.siddprakash.mlbenchmarkgroup26;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Environment;
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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.gui.*;

import static com.example.siddprakash.mlbenchmarkgroup26.R.string.test;
import static com.example.siddprakash.mlbenchmarkgroup26.R.string.train;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MLBENCHMARK";

    private float dataSplit = 0.5f;
    private int algorithm = 0;

    private EditText dSplit;
    private Spinner algo;
    private Button saveParams;
    private Button trainButton;
    private Button testButton;

    private Context context;
    private String jsonTrainData;

    private static final int REQUEST_ID_READ_PERMISSION = 100;
    private static final int REQUEST_ID_WRITE_PERMISSION = 200;


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

        // Read and Create the training - testing split of the data set file

        saveParams = (Button) findViewById(R.id.algoButton);
        saveParams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Save Params Button Clicked");
               try {
                   jsonTrainData = getJsonFromResource(dataSplit);
                   Log.d(TAG,"JSON DATA: "+jsonTrainData);
                   Toast.makeText(MainActivity.this, "Dataset Split Created", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        // Training Code
        // Send the data set file to server
        trainButton = (Button) findViewById(R.id.trainbutton);

        //        trainButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG,"Training Button Clicked");
//                new HttpAsyncTask().execute("http://hmkcode.appspot.com/jsonservlet");
//            }
//        });


        testButton = (Button) findViewById(R.id.testbutton);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Testing Button Clicked");


                try {
//                    DataSource source = new DataSource(Environment.getExternalStorageDirectory()+"/Android/data/MLBenchmark/breast-cancer-wisconsin.csv");
//

                    BufferedReader source = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory()+"/Android/data/MLBenchmark/breast-cancer-wisconsin.txt"));
                Instances trainingSet = new Instances(source);
                if (trainingSet.classIndex() == -1)
                    trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
                    trainingSet.deleteAttributeAt(0);
                trainingSet.randomize(new java.util.Random(0));
                int trainSize = (int) Math.round(trainingSet.numInstances() * 0.8);
                int testSize = trainingSet.numInstances() - trainSize;
                Instances train = new Instances(trainingSet, 0, trainSize);
                Instances test = new Instances(trainingSet, trainSize, testSize);
//                SMO classifier = new SMO();
//                classifier.buildClassifier(train);
//                weka.core.SerializationHelper.write("abc.model", classifier);
                Classifier cls = (Classifier) weka.core.SerializationHelper.read(Environment.getExternalStorageDirectory()+"/Android/data/MLBenchmark/abc.model");
                Evaluation eval = new Evaluation(train);
                eval.evaluateModel(cls, test);

                System.out.println(eval.toSummaryString("\nResults\n======\n", false));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });




        // Testing Code

    }

    public static String getJsonFromResource(float dSplit) throws IOException {

        File myFile = new File(Environment.getExternalStorageDirectory()+"/Android/data/MLBenchmark/breast-cancer-wisconsin.data");
        FileInputStream fIn = new FileInputStream(myFile);
        BufferedReader r = new BufferedReader( new InputStreamReader( fIn ) );
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
            System.out.println("Number of lines: "+lineCount);
        }
        catch (Exception e) {
            Log.e( "GetJsonFromResource", Log.getStackTraceString( e ) );
        }

        limit = (int) (dSplit * lineCount);

        System.out.println("Number of lines in training limit: "+limit);
        fIn = new FileInputStream(myFile);
        r = new BufferedReader(new InputStreamReader(fIn));
        try {
            while (count <= limit ) {
                line = r.readLine();
                // Create JSONObject(?) of split data set and add the type of ML algorithm
                stringBuilder.append( line );
                count++;
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

    /*
    public static String POST(String url, String trainData){
        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json = "";

            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("name", person.getName());
            jsonObject.accumulate("country", person.getCountry());
            jsonObject.accumulate("twitter", person.getTwitter());

            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();

            // ** Alternative way to convert Person object to JSON string usin Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(person);

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    private class HttpAsyncTask extends AsyncTask<String, void, String>{
        @Override
        protected String doInBackground(String... urls) {
            return POST(urls[0],jsonTrainData);;
        }
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Data Sent for Training!", Toast.LENGTH_LONG).show();
        }
    }

*/
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

}
