package com.handwriting.activity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.handwriting.R;
import com.handwriting.component.Constants;
import com.handwriting.component.RequestManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class MainActivity extends ActionBarActivity {

    EditText messageEditText;
    TextView sizeTitleTextView, colorTitleTextView, messageLengthTextView;
    Button sendButton, savePictureButton;
    ImageView handWritingImageView;
    Spinner colorSpinner, sizeSpinner;
    ProgressBar pictureProgressBar;

    InputMethodManager inputMethodManager;

    String handwritingColor, handwritingSize, textLength;
    Integer messageLength;
    Map<String, String> colors, sizes;
    Bitmap pictureBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViewProperties();
        initButtonListener();
        initVariables();
    }

    // Initializes the views of the activity
    private void initViewProperties() {

        this.messageEditText = (EditText) findViewById(R.id.messageEditText);
        this.sizeTitleTextView = (TextView) findViewById(R.id.sizeTitleTextView);
        this.colorTitleTextView = (TextView) findViewById(R.id.colorTitleTextView);
        this.messageLengthTextView = (TextView) findViewById(R.id.messageLengthTextView);
        this.sendButton = (Button) findViewById(R.id.sendButton);
        this.savePictureButton = (Button) findViewById(R.id.savePictureButton);
        this.colorSpinner = (Spinner) findViewById(R.id.colorSpinner);
        this.sizeSpinner = (Spinner) findViewById(R.id.sizeSpinner);
        this.handWritingImageView = (ImageView) findViewById(R.id.handWritingImageView);
        this.pictureProgressBar = (ProgressBar) findViewById(R.id.pictureProgressBar);
    }

    // Initializes all the variables
    private void initVariables(){

        // Get InputMethodManager for manage keyboard
        this.inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        this.messageLength = 0;
        Resources res = getResources();
        this.textLength = res.getString(R.string.message_length, 0, 20);
        this.messageLengthTextView.setText(this.textLength);

        // Create a hashmap of key/value
        colors = new HashMap<>();
        colors.put("Black", "#000000");
        colors.put("Grey", "#7A787A");
        colors.put("Red", "#E82610");
        colors.put("Blue", "#3A64D6");
        // Retrieve all the key for Spinner selection
        ArrayList<String> colorsTitles =  new ArrayList<>(colors.keySet());

        sizes = new HashMap<>();
        sizes.put("Small", "50px");
        sizes.put("Normal", "75px");
        sizes.put("Big", "100px");
        ArrayList<String> sizesTitles =  new ArrayList<>(sizes.keySet());

        // Create an adapter for Spinner and define the layout and the array for the selection
        ArrayAdapter<String> colorSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, colorsTitles );
        colorSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.colorSpinner.setAdapter(colorSpinnerAdapter);

        ArrayAdapter<String> sizeSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sizesTitles );
        sizeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.sizeSpinner.setAdapter(sizeSpinnerAdapter);

    }

    // Initializes the listeners on Buttons and Spinners
    private void initButtonListener() {
        this.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Retrieve the message from the EditText
                String message = messageEditText.getText().toString();

                // If the message is null, display a Toast error
                if (message.equals("")) {
                    Toast.makeText(getApplicationContext(), "Empty message. Please write something", Toast.LENGTH_SHORT).show();
                } else {

                    // Display the progressBar for the loading
                    pictureProgressBar.setVisibility(View.VISIBLE);

                    // Get the RequestQueue for calling the API
                    RequestManager.getInstance(getApplicationContext()).getRequestQueue().start();
                    String url = "https://api.handwriting.io/render/png?handwriting_id=2ZK3SNCR0057&handwriting_size=" + handwritingSize + "&handwriting_color=" + handwritingColor.replaceAll("#", "%23") + "&text=" + message.replaceAll(" ", "%20") + "&width=720px&height=360px&line_spacing=1.5&random_seed=-1";

                    // Hides the keyboard after writing
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                    // Create an ImageRequest for retrieving an image specified by the URL
                    ImageRequest request = new ImageRequest(url,
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap bitmap) {
                                    // Hide the loading ProgressBar
                                    pictureProgressBar.setVisibility(View.INVISIBLE);
                                    // Set the bitmap on the ImageView
                                    handWritingImageView.setImageBitmap(bitmap);
                                    // Save the bitmap for the gallery's picture of the phone
                                    pictureBitmap = bitmap;
                                }
                            }, 0, 0, null,
                            new Response.ErrorListener() {
                                public void onErrorResponse(VolleyError error) {
                                    pictureProgressBar.setVisibility(View.INVISIBLE);
                                    handWritingImageView.setImageResource(R.drawable.ic_launcher);
                                    Toast.makeText(getApplicationContext(), "An error has occured. Please try later", Toast.LENGTH_SHORT).show();
                                }
                            }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            return params;
                        }

                        // Defines the request headers with Basic Authentication
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> headers = new HashMap<String, String>();
                            String credentials = Constants.KEY + ":" + Constants.SECRET;
                            String authorization = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                            headers.put("Authorization", authorization);
                            return headers;
                        }
                    };

                    // Add the request to the RequestQueue.
                    RequestManager.getInstance(getApplicationContext()).addRequest(request);
                }
            }
        });


        this.savePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (pictureBitmap == null) {
                    Toast.makeText(getApplicationContext(), "Empty message. Please write something for save a picture", Toast.LENGTH_SHORT).show();
                } else {

                    OutputStream fileOutput = null;
                    String fileDirectory = Environment.getExternalStorageDirectory().toString();
                    // Define a File with directory and filename
                    File file = new File(fileDirectory, "handwriting-" + (new Random().nextInt(1000)) + ".png");

                    try {
                        // Stream that writes bytes to a file
                        fileOutput = new FileOutputStream(file);

                        // Compress the image in PNG format
                        pictureBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutput);
                        fileOutput.flush();
                        fileOutput.close();

                        // Create an access of the saved picture in the gallery's phone pictures
                        MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("ExternalStorage", "Scanned Path " + path);
                                Log.i("ExternalStorage", "Uri " + uri);
                            }
                        });

                        Toast.makeText(getApplicationContext(), "Image saved", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "An error has occured. Please try later", Toast.LENGTH_SHORT).show();
                    }

                }
            }

        });

        this.colorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Retrieve the value selected and set it in a variable
                String colorSelected = (String) parent.getItemAtPosition(position);
                handwritingColor = colors.get(colorSelected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Define the default value
                handwritingColor = colors.get(parent.getItemAtPosition(0));
            }
        });

        this.sizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sizeSelected = (String) parent.getItemAtPosition(position);
                handwritingSize = sizes.get(sizeSelected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                handwritingSize = sizes.get(parent.getItemAtPosition(0));
            }
        });


        this.messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (messageEditText.getText().toString().length() <= 0) {
                    //messageEditText.setError("Sorry...!! Its Mandatory Field");
                    messageLength = 0;
                    messageLengthTextView.setText(textLength);


                } else {
                    messageLength = messageEditText.getText().toString().length();
                    messageLengthTextView.setText(String.valueOf(messageLength) + "/" + 20);
                }
            }

        });
    }

}
