package src.com.ecn.urbapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.pillet.urbapp2.R;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.HINT;
import com.qualcomm.vuforia.ImageTargetBuilder;
import com.qualcomm.vuforia.ObjectTracker;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vec2I;
import com.qualcomm.vuforia.VideoBackgroundConfig;
import com.qualcomm.vuforia.VideoMode;
import com.qualcomm.vuforia.Vuforia;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import src.com.ecn.urbapp.AR.ARRenderer;
import src.com.ecn.urbapp.AR.ARSurfaceView;
import src.com.ecn.urbapp.AR.Texture;


/**
 * Created by Julien Vuillamy on 21/03/16.
 */

public class AugmentedRealityActivity extends Activity implements Vuforia.UpdateCallbackInterface {

    private final static String LOGTAG = "AugmentedRealityActivity";

    private static boolean isVuforiaInit = false;
    private static boolean isCameraInit = false ;
    //Integer to give different name to trackers
    private static int targetBuilderCounter = 0;

    //GLSurfaceView used to render the Augmented Reality with Vuforia
    private ARSurfaceView ARView;

    //References to UI items
    private Button buttonStartAR;
    private Button buttonRotate;
    private TextView textInstructions;

    //Set used to keep created trackers
    private DataSet trackerDataset;

    //Used to refresh the dataset when new tracker is created
    private static boolean targetHasChanged = false;

    //Image Intent result
    private static final int SELECT_PICTURE = 1;
    private Bitmap bitmap;

    //AsyncTask to initialize Vuforia
    private class VuforiaInit extends AsyncTask<Void, Integer, Void> {

        private final static String LOGTAG = "VuforiaInit";
        private int progress = -1;

        @Override
        protected Void doInBackground(Void... params) {

            do {
                progress = Vuforia.init();
                Log.d(LOGTAG, "Init progress : " + progress);
                publishProgress(progress);
            }
            while (progress >= 0 && progress < 100);
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(LOGTAG, " : onCreate");
        super.onCreate(savedInstanceState);

        //Set activity fullscreen in landscape
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Initialize Vuforia if not already done
        if (!isVuforiaInit) {

            //Vuforia developer licence key
            Vuforia.setInitParameters(this, Vuforia.GL_20, "Af6cidT/////AAAAAZcuaZYUvUzfrOdxGxxWztBOG1CZpB/j3qoC9FuLlFKcuLnwam5Wd2CvTgj9PTWJ8EdO5l4DGVTZpOVV7y8ZOWsa3GjgcVQVEO163IxAkQYpbV+plZp2oHh76NjMSeeV2I/KMzwF+8DyCkN2d6lK4wELjf9/pfIS+6hJvjw4eYN5YvPKxN7XrAeFwaPVrMQhIgYPq8r9mTvfMXSsr6e7gH9l/hnh/NKcNKn6VdoSiJkcv3ER787/4HOfmJdlimn4YmrG+Q/5CiQM/rAj8utDbQaWyLWJGHmZHFIIzahVqPsxwB/bBNkcjGW/sQQNrJdX663bBqRLAa2Qi8hhlDHR20XbAAprBZJMf5Pk66aWuV7J");

            VuforiaInit vuforiaInit = new VuforiaInit();
            vuforiaInit.execute();
            //Waiting for Vuforia to initialize
            try {
                vuforiaInit.get();
            } catch (InterruptedException e) {
                Log.e(LOGTAG, "Vuforia didn't initialize in time !");
            } catch (ExecutionException e) {
                Log.e(LOGTAG, "Error initializing Vuforia !");
            }
            Vuforia.registerCallback(this);
            initTrackers();

            //Initialize the camera
            int camera = CameraDevice.CAMERA.CAMERA_DEFAULT;
            isCameraInit = start_camera(camera);
        }

        isVuforiaInit = Vuforia.isInitialized();
        Vuforia.setHint(HINT.HINT_MAX_SIMULTANEOUS_IMAGE_TARGETS, 5);
        //Building the Augmented Reality View
        ARView = new ARSurfaceView(this);
        ARView.setVisibility(ViewGroup.VISIBLE);
        setContentView(ARView);

        //Adding the action overlay with validation and image adding buttons
        RelativeLayout action_overlay = (RelativeLayout) getLayoutInflater().inflate(R.layout.augmented_reality, null);
        addContentView(action_overlay, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        buttonStartAR = (Button) findViewById(R.id.startAR);
        buttonRotate = (Button) findViewById(R.id.rotate);
        buttonStartAR.setVisibility(View.INVISIBLE);
        buttonRotate.setVisibility(View.INVISIBLE);

        textInstructions = (TextView) findViewById(R.id.textView);
        textInstructions.setVisibility(View.INVISIBLE);
    }

    /**
     * Method called by the submit button from the camera_overlay.xml
     *
     * @param view
     */
    public void onSubmitButtonClicked(View view) {
        Log.d(LOGTAG, "SubmitButton clicked");
        try {
            String trackerName = buildTracker();
            ARRenderer.getInstance().setTracked(trackerName);
        } catch (NullPointerException e) {
            Log.e(LOGTAG, "Unable to create tracker");
        }
    }

    /**
     * Method called by the rotate button from the camera_overlay.xml
     *
     * @param view
     */
    public void onRotateButtonClicked(View view) {
        if (bitmap != null) {
            Matrix m = new Matrix();
            m.postRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            Texture t = Texture.loadTexture(bitmap);
            ARRenderer.getInstance().replaceOverlay(t);
        }
    }

    /**
     * Method called by the add image
     *
     * @param view
     */
    public void onAddImageButtonClicked(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    /**
     * Method called when the select image intent is over.
     *
     * @param requestCode
     * @param resultCode
     * @param data        Data retrieved from the Intent
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
            try {
                InputStream stream;
                stream = getContentResolver().openInputStream(data.getData());

                bitmap = BitmapFactory.decodeStream(stream);
                Texture t = Texture.loadTexture(bitmap);
                ARRenderer.getInstance().createOverlay(t);

                buttonRotate.setVisibility(View.VISIBLE);
                buttonStartAR.setVisibility(View.VISIBLE);
                textInstructions.setVisibility(View.VISIBLE);

                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }


        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        Log.d(LOGTAG, ": onResume");
        super.onResume();
        Vuforia.onResume();

        if (isVuforiaInit && !isCameraInit) {
            CameraDevice.getInstance().start();
            CameraDevice.getInstance().init();
        }

    }

    @Override
    protected void onPause() {
        Log.d(LOGTAG, ": onPause");
        super.onPause();
        Vuforia.onPause();

        if (isCameraInit) {
            CameraDevice.getInstance().stop();
            CameraDevice.getInstance().deinit();
            isCameraInit = false ;
        }
    }

    /**
     * Called on every new frame
     *
     * @param state
     */
    @Override
    public void QCAR_onUpdate(State state) {
        if (targetHasChanged) {
            TrackerManager trackerManager = TrackerManager.getInstance();
            ObjectTracker objectTracker = (ObjectTracker) trackerManager
                    .getTracker(ObjectTracker.getClassType());

            Log.d(LOGTAG, "Attempting to transfer the trackable source to the dataset");

            // Deactivate current dataset
            objectTracker.deactivateDataSet(objectTracker.getActiveDataSet());

            // Add new trackable source
            Trackable trackable = trackerDataset
                    .createTrackable(objectTracker.getImageTargetBuilder().getTrackableSource());
            // Reactivate current dataset
            objectTracker.activateDataSet(trackerDataset);
            trackable.startExtendedTracking();
            targetHasChanged = false;

        }
    }

    /**
     * Initialization of the camera
     *
     * @param camera
     */
    private boolean start_camera(int camera) {

        Log.d(LOGTAG, "Camera Initialization");

        //Récupération de la taille de l'écran
        DisplayMetrics dm = getScreenDimensions();
        int screenHeight = dm.heightPixels;
        int screenWidth = dm.widthPixels;

        Log.d(LOGTAG, "Screen size : " + screenHeight + "," + screenWidth + "");

        //Récupération de la caméra
        CameraDevice cameraDevice = CameraDevice.getInstance();
        cameraDevice.init(camera);
        cameraDevice.selectVideoMode(CameraDevice.MODE.MODE_DEFAULT);


        //Parametrage de la camera
        VideoMode vm = cameraDevice.getVideoMode(CameraDevice.CAMERA.CAMERA_DEFAULT);
        VideoBackgroundConfig config = new VideoBackgroundConfig();
        config.setEnabled(true);
        config.setPosition(new Vec2I(0, 0));

        int xSize, ySize;

        xSize = screenWidth;
        ySize = (int) (vm.getHeight() * (screenWidth / (float) vm
                .getWidth()));

        if (ySize < screenHeight) {
            xSize = (int) (screenHeight * (vm.getWidth() / (float) vm
                    .getHeight()));
            ySize = screenHeight;
        }

        Log.d(LOGTAG, "Camera size : " + xSize + "," + ySize);
        config.setSize(new Vec2I(xSize, ySize));

        Renderer.getInstance().setVideoBackgroundConfig(config);

        cameraDevice.start();

        cameraDevice.setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

        isVuforiaInit = true;
        return true ;
    }

    private DisplayMetrics getScreenDimensions() {
        // Query display dimensions:
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

    /**
     * Build a new tracker and triggers the reload of the database in QCAR_onUpdate
     *
     * @return Name given to the tracker
     */
    public String buildTracker() {

        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager.getTracker(ObjectTracker.getClassType());

        if (objectTracker != null) {
            ImageTargetBuilder targetBuilder = objectTracker.getImageTargetBuilder();
            if (targetBuilder != null) {
                objectTracker.stop();
                Log.d(LOGTAG, "Image quality :" + targetBuilder.getFrameQuality());
                String name;

                do {
                    name = "UserTarget-" + targetBuilderCounter;
                    targetBuilderCounter++;
                } while (!targetBuilder.build(name, 320.f));

                targetHasChanged = true;
                objectTracker.start();
                return name;
            } else {
                Log.e(LOGTAG, "Could not get ImageTargetBuilder");
                throw new NullPointerException();
            }
        } else {
            Log.e(LOGTAG, "Could not get ObjectTracker");
            throw new NullPointerException();
        }
    }

    /**
     * Create a new object tracker and start scanning to build a user-defined tracker
     */
    private void initTrackers() {

        Log.d(LOGTAG, ": initAR");
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager.initTracker(ObjectTracker.getClassType());

        if (objectTracker != null) {
            Log.d(LOGTAG, "ObjectTracker initialized !");

            trackerDataset = objectTracker.createDataSet();
            ImageTargetBuilder imageTargetBuilder = objectTracker.getImageTargetBuilder();
            imageTargetBuilder.startScan();
        } else
            Log.e(LOGTAG, "ObjectTracker not initialized");
    }

}
