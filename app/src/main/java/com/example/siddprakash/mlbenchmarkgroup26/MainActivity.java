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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


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
    private String trainData;
    private String trainFile;

    private static final String uploadFilePath = Environment.getExternalStorageDirectory() + "/Android/Data/MLBenchmark/";
    private static final String downloadFilePath = Environment.getExternalStorageDirectory() + "/Android/Data/MLBenchmark/";
    public static final String upLoadURL = "http://10.218.110.136/CSE535Fall17Folder/UploadToServer.php";
    public static final String downloadUrl = "http://10.218.110.136/CSE535Fall17Folder/";



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
        trainFile = "trainFile"+algorithm+".csv";

        saveParams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Save Params Button Clicked");
               try {
                   trainData = createPartition(dataSplit, trainFile);
                   System.out.println(trainData);
                   Toast.makeText(MainActivity.this, "Split Dataset Created", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        // Training Code
        // Send the data set file to server
        trainButton = (Button) findViewById(R.id.trainbutton);
        trainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Training Button Clicked");
                UploadTask uploadTask = new UploadTask(MainActivity.this);
                uploadTask.execute(uploadFilePath + trainFile);

            }
        });

        // Testing Code

        testButton = (Button) findViewById(R.id.testbutton);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Testing Button Clicked");
                try {

                    Instances train = getDataFromResource(dataSplit, "Train");
                    Instances test = getDataFromResource(dataSplit, "Test");


                    //SMO classifier = new SMO();
                    //classifier.buildClassifier(train);
                    //weka.core.SerializationHelper.write("abc.model", classifier);
                    String model;
                    switch (algorithm){
                        case 2: model = "nb.model";
                            break;
                        case 3: model = "knn.model";
                            break;
                        case 4: model = "svm.model";
                            break;
                        case 1:

                        default: model = "lr.model";
                            break;
                    }

//                    Classifier cls = (Classifier) weka.core.SerializationHelper.read(Environment.getExternalStorageDirectory()+"/Android/data/MLBenchmark/"+model);
                    Classifier cls = (Classifier) weka.core.SerializationHelper.read(Environment.getExternalStorageDirectory()+"/Android/data/MLBenchmark/abc.model");
                    Evaluation eval = new Evaluation(train);
                    eval.evaluateModel(cls, test);

                    System.out.println(eval.toSummaryString("\nResults\n======\n", false));

                    double trr = eval.trueNegativeRate(0) * 100;
                    double tar = eval.truePositiveRate(0) * 100;
                    double frr = eval.falseNegativeRate(0) * 100;
                    double far = eval.falsePositiveRate(0) * 100;
                    double hter = (frr + far) / 2.0;

                    System.out.println("True Accept Rate (TAR): "+tar+" %");
                    System.out.println("True Reject Rate (TAR): "+trr+" %");
                    System.out.println("False Accept Rate (TAR): "+far+" %");
                    System.out.println("False Reject Rate (TAR): "+frr+" %");
                    System.out.println("HTER: "+hter+" %");


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private String createPartition(float dataSplit, String fName) throws IOException {

        File myFile = new File(Environment.getExternalStorageDirectory()+"/Android/data/MLBenchmark/breast-cancer-wisconsin.data");
        FileInputStream fIn = new FileInputStream(myFile);
        BufferedReader r = new BufferedReader( new InputStreamReader( fIn ) );

        String line;
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

        limit = (int) (dataSplit * lineCount);

        System.out.println("Number of lines in training limit: "+limit);
        fIn = new FileInputStream(myFile);
        r = new BufferedReader(new InputStreamReader(fIn));

        File outFile = new File(Environment.getExternalStorageDirectory()+"/Android/data/MLBenchmark/"+fName);
        FileOutputStream fOut = new FileOutputStream(outFile);
        PrintWriter pw = new PrintWriter(fOut);

        try {
            while (count <= limit ) {
                line = r.readLine();
                pw.println(line);
                count++;
            }
        }
        catch (Exception e) {
            Log.e( "GetJsonFromResource", Log.getStackTraceString( e ) );
        }
        pw.flush();
        pw.close();
        fOut.close();

        return "success";

    }

    public static Instances getDataFromResource(float dSplit, String status) throws IOException {

        //TODO: Convert breast-cancer-wisconsin.data to breast-cancer-wisconsin.txt

        File myFile = new File(Environment.getExternalStorageDirectory()+"/Android/data/MLBenchmark/breast-cancer-wisconsin.txt");
        FileInputStream fIn = new FileInputStream(myFile);
        BufferedReader r = new BufferedReader( new InputStreamReader( fIn ) );

        Instances trainingSet = new Instances(r);
        if (trainingSet.classIndex() == -1)
            trainingSet.setClassIndex(trainingSet.numAttributes() - 1);

        trainingSet.deleteAttributeAt(0);
        trainingSet.randomize(new java.util.Random(0));

        int trainSize = (int) Math.round(trainingSet.numInstances() * dSplit);
        int testSize = trainingSet.numInstances() - trainSize;
        Instances train = new Instances(trainingSet, 0, trainSize);
        Instances test = new Instances(trainingSet, trainSize, testSize);

        if(status.equalsIgnoreCase("Train"))
            return train;
        else
            return test;
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

    public class UploadTask extends AsyncTask<String, String, String> {
        private Context context;

        public UploadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection conn = null;
            int serverResponseCode = 0;


            try {

                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int maxBufferSize = 1 * 1024 * 1024;
                File sourceFile = new File(uploadFilePath + trainFile);
                Log.i("File Uri ", sUrl[0]);
                Log.i("File ", sourceFile.toString());

                if (!sourceFile.isFile()) {

                    Log.e("Upload File :", uploadFilePath + trainFile + " does not exist");

                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Upload File :" + uploadFilePath + trainFile + " does not exist", Toast.LENGTH_LONG).show();
                        }
                    });

                    return "Source File not exist";

                } else {
                    try {

                        FileInputStream fileInputStream = new FileInputStream(sourceFile);


                        URL url = new URL(upLoadURL);
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true);
                        conn.setDoOutput(true);
                        conn.setUseCaches(false);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Connection", "Keep-Alive");
                        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                        conn.setRequestProperty("uploaded_file", uploadFilePath + "" + trainFile);


                        dos = new DataOutputStream(conn.getOutputStream());

                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=" + "uploaded_file;filename="
                                + uploadFilePath + "" + trainFile + "" + lineEnd);

                        dos.writeBytes(lineEnd);

                        bytesAvailable = fileInputStream.available();

                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];

                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        while (bytesRead > 0) {

                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        }

                        dos.writeBytes(lineEnd);
                        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                        serverResponseCode = conn.getResponseCode();
                        String serverResponseMessage = conn.getResponseMessage();

                        conn.connect();
                        serverResponseCode = conn.getResponseCode();
                        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                            return "Server returned HTTP " + conn.getResponseCode()
                                    + " " + conn.getResponseMessage();
                        }


                        Log.i("Upload File", "HTTP Response is : "
                                + serverResponseMessage + ": " + serverResponseCode);

                        if (serverResponseCode == 200) {

                            runOnUiThread(new Runnable() {
                                              public void run() {
                                                  Toast.makeText(getApplicationContext(),
                                                          "File Upload Completed", Toast.LENGTH_LONG).show();
                                              }
                                          }

                            );
                        }

                        fileInputStream.close();
                        dos.flush();
                        dos.close();

                    } catch (MalformedURLException ex) {

                        ex.printStackTrace();
                        String temp = "value displayed";
                        Log.i("Response Code:" + serverResponseCode, temp);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "MalformedURLException Exception : check script url.", Toast.LENGTH_LONG).show();
                            }
                        });

                        Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
                    } catch (Exception e) {

                        e.printStackTrace();
                        String temp = "value displayed";
                        Log.i("Response Code:" + serverResponseCode, temp);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Got Exception : see logcat", Toast.LENGTH_LONG).show();
                            }
                        });
                        Log.e("Upload file to server", "Exception : "
                                + e.getMessage(), e);
                    }
                    return ("String response code:" + serverResponseCode);

                }
            } catch (Exception ex) {
            }
            return "success";
        }


    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

}
