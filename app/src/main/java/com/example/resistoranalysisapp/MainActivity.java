// Import necessary libraries and packages
package com.example.resistoranalysisapp;

import static org.xmlpull.v1.XmlPullParser.TYPES;

import android.Manifest;
import android.app.ActionBar;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    // The link is to the server.
    static final String calculateUrl = "https://bluepill.ecn.purdue.edu/~jjhuang/calculateResistance.php";
    private final int CAMERA_RETURN = 600;
    RelativeLayout layout;
    // Used to store image taken by user
    Bitmap bitmap;
    Boolean notReceived = true;
    int bandNum = 5; // Default band number
    PopupWindow popupWindow;
    static String color1;
    static String color2;
    static String color3;
    static String color4;
    static String color5;
    static String color6;
    final double RESISTOR_MULTIPLIER[] = { 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000,  0.1, 0.01};
    final double TOLERANCE[] = {1, 2, 0.5, 0.25, 0.10, 0.05, 5, 10};
    final Map<Integer, String> MULTIPLIERS = Stream.of(new Object[][] {
            {12, "T"},
            {9, "G"},
            {6, "M"},
            {3, "k"},
            {0, " "},
    }).collect(Collectors.toMap(data -> (Integer) data[0], data -> (String) data[1]));
    @SuppressLint("StaticFieldLeak")
    static Spinner spinner1;
    @SuppressLint("StaticFieldLeak")
    static Spinner spinner2;
    @SuppressLint("StaticFieldLeak")
    static Spinner spinner3;
    @SuppressLint("StaticFieldLeak")
    static Spinner spinner4;
    @SuppressLint("StaticFieldLeak")
    static Spinner spinner5;
    @SuppressLint("StaticFieldLeak")
    static Spinner spinner6;

    Uri imageUri;
    // To check the iterations of color-change
    static int ctr = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set 'activity_main.xml' as resource file for screen
        setContentView(R.layout.activity_main);

        layout = (RelativeLayout) findViewById(R.id.relative);
        layout.getForeground().setAlpha(0);

        // Ask for permission to use Camera upon opening app if not already given
        String permission[] = {Manifest.permission.CAMERA};

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permission, 400);
        }
    }

    // After "Retake" or "GetStarted" button's are pressed
    public void TakeImage(View view) {
//        String tagValue = view.getTag().toString();
//        switch (tagValue) {
//            case "-1":
                // Set Band Number Value
        @SuppressLint("CutPasteId") RadioButton radio_4 = findViewById(R.id.radio_4);
        @SuppressLint("CutPasteId") RadioButton radio_6 = findViewById(R.id.radio_6);
        if (radio_4.isChecked()) {
            bandNum = 4;
        } else if (radio_6.isChecked()) {
            bandNum = 6;
        } else {
            bandNum = 5;
        }
//                break;
//            case "4":
//                bandNum = 4;
//                break;
//            case "5":
//                bandNum = 5;
//                break;
//            case "6":
//                bandNum = 6;
//                break;
//        }

        // Reset band changed counter
        ctr = 0;
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Pop up to let user know how to update permissions
            CreatePopUpWindow("close", R.layout.no_permission_popup);
        } else {
            // Upon clicking "Get Started" button, popup instructions are opened
            // Set layout value for later use in createPopUp
            layout = findViewById(R.id.relative);
            CreatePopUpWindow("camera", R.layout.instructions_popup);
        }
    }

    public void TipsPopup(View view) {
        CreatePopUpWindow("tips", R.layout.tips_popup);
    }

    public void Calculate(View view) {
        // TO DO: send image to server and receive data
        //Pop up that says "Calculating:"
        CreatePopUpWindow("loading", R.layout.loading_popup);

        // Creates request for a string response to "calculateUrl"
        StringRequest stringRequest = new StringRequest(Request.Method.POST, calculateUrl,
                // Here we put the returned R_value onto the screen}),
                response -> {
                    //Enable Calculate button
                    String[] result = response.split(",");
                    if (result.length == 1) {
                        if (response.trim().equals("-1")) {
                            // Failed to detect resistor:
                            // Close Loading Popup Window
                            popupWindow.dismiss();

                            CreatePopUpWindow("failed_detect", R.layout.failed_result_popup);
                        }
                        else {
                            // Image Failed to Parse Error
                            //layout.getForeground().setAlpha(0);
                            try {
                                TimeUnit.SECONDS.sleep(1);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            popupWindow.dismiss();

                            CreatePopUpWindow("failed_upload", R.layout.failed_upload_popup);

                            Log.d("myTag", response);
                        }
                    } else {
                        // Close Loading Popup
                        layout.getForeground().setAlpha(0);
                        popupWindow.dismiss();

                        // Set activity to activity_calc
                        //setContentView(R.layout.activity_calc);
                        setResultPage();

                        // Set foreground to be transparent
                        layout = findViewById(R.id.relative);
                        layout.getForeground().setAlpha(0);

                        // Parse Information
                        TextView finalResult = findViewById(R.id.final_val);
                        finalResult.setText(result[0]);

                        // Set band colors in image dynamically:
                        color1 = result[1];
                        color2 = result[2];
                        color3 = result[3];
                        if (bandNum == 4) {
                            color4 = result[4].substring(0, 7);
                            String[] color_list = new String[]{color1, color2, color3, color4};
                            changeColor(color_list);
                        }
                        if (bandNum == 5) {
                            color4 = result[4];
                            color5 = result[5].substring(0, 7);
                            String[] color_list = new String[]{color1, color2, color3, color4, color5};
                            changeColor(color_list);
                        }
                        if (bandNum == 6) {
                            color4 = result[4];
                            color5 = result[5];
                            color6 = result[6].substring(0, 7);
                            String[] color_list = new String[]{color1, color2, color3, color4, color5, color6};
                            changeColor(color_list);
                        }
                        //System.out.printf("%s,%s,%s,%s,%s\n",color1,color2,color3,color4,color5);



                        //Allowing user to change
                        spinner1 = (Spinner) findViewById(R.id.color1_spinner);
                        spinner2 = (Spinner) findViewById(R.id.color2_spinner);
                        spinner3 = (Spinner) findViewById(R.id.color3_spinner);
                        spinner4 = (Spinner) findViewById(R.id.color4_spinner);
                        if (bandNum >= 5){
                            spinner5 = (Spinner) findViewById(R.id.color5_spinner);
                        }
                        if (bandNum == 6){
                            spinner6 = (Spinner) findViewById(R.id.color6_spinner);
                        }


                        // Create an ArrayAdapter using the string array and a default spinner layout.
                        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                                this,
                                R.array.color_array,
                                R.layout.spinner_list
//                                android.R.layout.simple_spinner_item
                        );

                        ArrayAdapter<CharSequence> adapter_no_percent = ArrayAdapter.createFromResource(
                                this,
                                R.array.color_array_no_percent,
                                R.layout.spinner_list
                        );
                        ArrayAdapter<CharSequence> adapter_no_tolerance = ArrayAdapter.createFromResource(
                                this,
                                R.array.color_array_no_tolerance,
                                R.layout.spinner_list
                        );
                        ArrayAdapter<CharSequence> adapter_temp = ArrayAdapter.createFromResource(
                                this,
                                R.array.color_array_temp,
                                R.layout.spinner_list
                        );

                        // Specify the layout to use when the list of choices appears.
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter_no_percent.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter_no_tolerance.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter_temp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        // Apply the adapter to the spinner.
                        spinner1.setAdapter(adapter_no_percent);
                        spinner2.setAdapter(adapter_no_percent);

                        if (bandNum == 4) {
                            spinner3.setAdapter(adapter);
                            spinner4.setAdapter(adapter_no_tolerance);
                        } else if (bandNum == 6) {
                            spinner3.setAdapter(adapter_no_percent);
                            spinner4.setAdapter(adapter);
                            spinner5.setAdapter(adapter_no_tolerance);
                            spinner6.setAdapter(adapter_temp);
                        } else {
                            spinner3.setAdapter(adapter_no_percent);
                            spinner4.setAdapter(adapter);
                            spinner5.setAdapter(adapter_no_tolerance);
                        }


                        spinner1.setOnItemSelectedListener(this);
                        spinner2.setOnItemSelectedListener(this);
                        spinner3.setOnItemSelectedListener(this);
                        spinner4.setOnItemSelectedListener(this);
                        if (bandNum == 5) {
                            spinner5.setOnItemSelectedListener(this);
                        }
                        if (bandNum == 6) {
                            spinner5.setOnItemSelectedListener(this);
                            spinner6.setOnItemSelectedListener(this);
                        }
                    }
                    //System.out.printf("%s,%s,%s,%s,%s\n",color1,color2,color3,color4,color5);
                },
                error -> {
                    String error1 = error.toString();
                    Log.d("myTag", "ERROR: " + error1);
                    layout.getForeground().setAlpha(0);
                    popupWindow.dismiss();
                }) {
            // getParams() used by php to get data passed to server
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                // Need to convert image to base64 string to pass over internet (or corruption could occur)
                String imageString = imageToString(bitmap);
                params.put("image", imageString);
                params.put("bandNum", Integer.toString(bandNum));

                return params;
            }
        };

        // Add stringRequest to queue
        // Must set "Network" permissions in AndroidManifest.xml
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);

    }
    @SuppressLint("DefaultLocale")
    private String calculateResistance(String[] color_list) {
        String starting_value = "";
        float raw_value = 0;

        // Set multiplier and tolerance value for different band number resistors
        int multiplier_band = 3;
        int tolerance_band = 4;
        if (bandNum == 4) {
            multiplier_band = 2;
            tolerance_band = 3;
        }

        String[] n_color_array = getResources().getStringArray(R.array.color_hex);
        String[] n_color_tolerance = getResources().getStringArray(R.array.color_tolerance_hex);
        String[] color_tolerance = new String[8];
        String[] color_array = new String[12];
        for (int i = 1; i < 13; i++) {
            color_array[i-1] = n_color_array[i];
        }
        for (int i = 1; i < 9; i++) {
            color_tolerance[i-1] = n_color_tolerance[i];
        }
        double tolerance = 0;
        // Color_list os an array of colors
        for (int i = 0; i < (color_list.length); i++) {
            if (i < multiplier_band){
                starting_value += String.valueOf(Arrays.asList(color_array).indexOf(color_list[i]));
            }
            if (i == multiplier_band){
                raw_value = Integer.valueOf(starting_value);
                raw_value *= RESISTOR_MULTIPLIER[Arrays.asList(color_array).indexOf(color_list[i])];
            }
            if (i == tolerance_band){
                System.out.println(color_list[i]);
                System.out.println(color_tolerance);
                System.out.println(Arrays.asList(color_tolerance).indexOf(color_list[i]));
                try {
                    tolerance = TOLERANCE[Arrays.asList(color_tolerance).indexOf(color_list[i])];
                }
                catch (Exception e) {
                    System.out.println("A color exsisting outside the range was given.");
                }


            }
        }
        String formatted_value = "";
        int keys[] = {12, 9, 6, 3, 0};
        for (int i: keys) {
            float value = (float) (raw_value/(Math.pow(10,i)));
            if( value > 1){
                formatted_value += String.format("%.2f", value) + MULTIPLIERS.get(i) + "Ω";
                break;
            }
        }
        if (formatted_value.equals("")) {
            formatted_value = String.format("%.2f", raw_value) + "Ω";
        }
        formatted_value += "  ±" + String.valueOf(tolerance) + "%";
        return formatted_value;
    }

    private void changeColor(String[] color_list) {
        String r_val = calculateResistance(color_list);

        TextView resistance = findViewById(R.id.final_val);
        resistance.setText(r_val);

        View band1 = findViewById(R.id.band1);
        View spinner1 = findViewById(R.id.color1_spinner);
        GradientDrawable bgShape1 = (GradientDrawable) band1.getBackground();
        GradientDrawable bgShape1_spnr = (GradientDrawable) spinner1.getBackground();
        bgShape1.setColor(Color.parseColor(color_list[0]));
        bgShape1_spnr.setColor(Color.parseColor(color_list[0]));

        View band2 = findViewById(R.id.band2);
        View spinner2 = findViewById(R.id.color2_spinner);
        GradientDrawable bgShape2 = (GradientDrawable) band2.getBackground();
        GradientDrawable bgShape2_spnr = (GradientDrawable) spinner2.getBackground();
        bgShape2.setColor(Color.parseColor(color_list[1]));
        bgShape2_spnr.setColor(Color.parseColor(color_list[1]));

        View band3 = findViewById(R.id.band3);
        View spinner3 = findViewById(R.id.color3_spinner);
        GradientDrawable bgShape3 = (GradientDrawable) band3.getBackground();
        GradientDrawable bgShape3_spnr = (GradientDrawable) spinner3.getBackground();
        bgShape3.setColor(Color.parseColor(color_list[2]));
        bgShape3_spnr.setColor(Color.parseColor(color_list[2]));

        View band4 = findViewById(R.id.band4);
        View spinner4 = findViewById(R.id.color4_spinner);
        GradientDrawable bgShape4 = (GradientDrawable) band4.getBackground();
        GradientDrawable bgShape4_spnr = (GradientDrawable) spinner4.getBackground();
        bgShape4.setColor(Color.parseColor(color_list[3]));
        bgShape4_spnr.setColor(Color.parseColor(color_list[3]));

        if (bandNum == 5 || bandNum == 6) {
            View band5 = findViewById(R.id.band5);
            View spinner5 = findViewById(R.id.color5_spinner);
            GradientDrawable bgShape5 = (GradientDrawable) band5.getBackground();
            GradientDrawable bgShape5_spnr = (GradientDrawable) spinner5.getBackground();
            bgShape5.setColor(Color.parseColor(color_list[4]));
            bgShape5_spnr.setColor(Color.parseColor(color_list[4]));
        }

        if (bandNum == 6) {
            System.out.println(color_list);
            View band6 = findViewById(R.id.band6);
            View spinner6 = findViewById(R.id.color6_spinner);
            GradientDrawable bgShape6 = (GradientDrawable) band6.getBackground();
            GradientDrawable bgShape6_spnr = (GradientDrawable) spinner6.getBackground();
            bgShape6.setColor(Color.parseColor(color_list[5]));
            bgShape6_spnr.setColor(Color.parseColor(color_list[5]));
        }
    }

    // Converts bitmap image to Base64 string to pass to server
    private String imageToString(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // JPEG might change
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return (Base64.encodeToString(imageBytes, Base64.DEFAULT));
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        //Note: Need to add date and time to title to avoid issue with duplicate entry handling in insertImage
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "IMG_Title" + new Date().getTime(), null);
        return Uri.parse(path);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // When image result is received from camera
        if (requestCode == CAMERA_RETURN) {
            if (resultCode == 0) {
                // When image result is not received from camera, go to main page
                setContentView(R.layout.activity_main);
                layout = findViewById(R.id.relative);
                layout.getForeground().setAlpha(0);
            } else {
                // When image result is received from camera
                notReceived = false;
                // Creates bitmap from photo taken
                if(data != null) {}
                imageUri = data.getData();
                //launchImageCropper(imageUri);
                bitmap = (Bitmap) data.getExtras().get("data");
                imageUri = getImageUri(MainActivity.this, bitmap);

                Intent intent = new Intent(MainActivity.this, CropperActivity.class);
                intent.putExtra("DATA", imageUri.toString());
                intent.putExtra("bandNum", bandNum);
                startActivityForResult(intent, 101);
            }
        }
        else if(resultCode == -1 && requestCode==101) {
            String result = data.getStringExtra("RESULT");
            Uri resultUri=null;
            Log.d("myTag", "First!!");
            if (result != null) {
                resultUri = Uri.parse(result);
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                }
                catch(Exception e)
                {
                    Log.d("myTag", String.valueOf(e));
                    Log.d("myTag", "in here");
                }
                // Sets layout to activity_result.xml
                setContentView(R.layout.activity_result);
                setBandNumRadioButton();
                layout = findViewById(R.id.relative);

                // Inputs photo cropped to page
                ImageView imageView = findViewById(R.id.imageView2);
                imageView.setImageBitmap(bitmap);
                layout.getForeground().setAlpha(0);

            }
            else {
                // Sets layout to activity_result.xml
                Log.d("myTag", "Went in here****");
                setContentView(R.layout.activity_result);
                setBandNumRadioButton();
                layout = findViewById(R.id.relative);

                // Inputs photo taken to page (has not been cropped)
                ImageView imageView = findViewById(R.id.imageView2);
                imageView.setImageBitmap(bitmap);
                layout.getForeground().setAlpha(0);
            }
        }
        else if(requestCode==101) {
            Log.d("myTag", "Went in here2****");
            setContentView(R.layout.activity_main);

            layout = (RelativeLayout) findViewById(R.id.relative);
            layout.getForeground().setAlpha(0);
        }
    }

    private void CreatePopUpWindow(String option, int popUp) {
        // To make the background blur
        layout.getForeground().setAlpha(220);
        // Get inflater service to put up popup
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        // Inflate popup from "instructions_popup.xml"
        View popUpView = inflater.inflate(popUp, null);
        // Set values for PopupWindow
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        boolean focusable = true;

        // Create New PopupWindow with previous values and inflater
        popupWindow = new PopupWindow(popUpView, width, height, focusable);

        // Post popup to current layout
        layout.post(new Runnable() {
            @Override
            public void run() {
                popupWindow.showAtLocation(layout, Gravity.BOTTOM, 0, 0);
            }
        });

        if (option != "loading") {
            // Get GotIt button by id
            TextView GotIt = popUpView.findViewById(R.id.gotIt);
            GotIt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // To make the background un-blur
                    layout.getForeground().setAlpha(0);
                    popupWindow.dismiss();

                    if (option == "camera" || option == "failed_detect") {
                        // Create an Intent to launch the device's camera application
                        Intent intent = new Intent();
                        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
//
//                        // Start the camera app
                        startActivityForResult(intent, CAMERA_RETURN);
                    }
                }
            });

            popUpView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // To make the background un-blur
                    layout.getForeground().setAlpha(0);
                    popupWindow.dismiss();

                    return true;
                }
            });
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Matching the id
        String[] color_array = getResources().getStringArray(R.array.color_hex);
        String[] color_array_tolerance = getResources().getStringArray(R.array.color_tolerance_hex);
        String[] color_array_temp = getResources().getStringArray(R.array.color_temp_hex);
        String temp_color = "";
        int count = -1;
        if (parent.getId() == spinner1.getId()) {
            ctr++;
            if (ctr > bandNum) {
                count = 1;
                if (position != 0) color1 = color_array[position];
            }
        } else if (parent.getId() == spinner2.getId()) {
            ctr++;
            if (ctr > bandNum) {
                count = 2;
                if (position != 0) color2 = color_array[position];
            }
        } else if (parent.getId() == spinner3.getId()) {
            ctr++;
            if (ctr > bandNum) {
                count = 3;
                if (position != 0) color3 = color_array[position];
            }
        } else if (parent.getId() == spinner4.getId()) {
            ctr++;
            if (ctr > bandNum && position != 0) {
                if (bandNum == 4) {
                    color4 = color_array_tolerance[position];
                }
                else {
                    color4 = color_array[position];
                }
            }
        } else if (parent.getId() == spinner5.getId()) {
            ctr++;
            if (ctr > bandNum && position != 0) color5 = color_array_tolerance[position];
        } else if (parent.getId() == spinner6.getId()) {
            ctr++;
            if (ctr > bandNum && position != 0) color6 = color_array_temp[position];
        }
        String[] color_list;
        if (bandNum == 4){
            color_list = new String[]{color1, color2, color3, color4};
        }
        else if (bandNum == 6){
            color_list = new String[]{color1, color2, color3, color4, color5, color6};
        }
        else {
            color_list = new String[]{color1, color2, color3, color4, color5};
        }
        changeColor(color_list);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        System.out.println("here");
        System.out.printf("%s,%s,%s,%s,%s\n",color1,color2,color3,color4,color5);
    }

    private void setResultPage() {


        if (bandNum == 4) {
            setContentView(R.layout.four_band_result);
            setBandNumRadioButton();

        } else if (bandNum == 6) {
            setContentView(R.layout.six_band_result);
            setBandNumRadioButton();
        } else {
            setContentView(R.layout.activity_calc);
        }
    }

    private void setBandNumRadioButton() {
        if (bandNum == 4) {
            RadioGroup radio_group = findViewById(R.id.radio_group);
            radio_group.check(R.id.radio_4);
        } else if (bandNum == 6) {
            RadioGroup radio_group = findViewById(R.id.radio_group);
            radio_group.check(R.id.radio_6);
        }
    }

}