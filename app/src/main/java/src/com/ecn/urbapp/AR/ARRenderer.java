package src.com.ecn.urbapp.AR;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.Vuforia;

import java.util.Stack;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.qualcomm.vuforia.Tool.convertPose2GLMatrix;
import static com.qualcomm.vuforia.Tool.getProjectionGL;


/**
 * Created by Julien Vuillamy on 02/03/16.
 * This class implements the GL Renderer. It renders multiple GLOverlays to track multiple AR images simultaneously.
 * Inspired by the UserDefinedTargetRenderer class from VuforiaSamples
 * https://developer.vuforia.com/downloads/samples
 */

public class ARRenderer implements GLSurfaceView.Renderer{

    private static final String LOGTAG = "ARRenderer" ;

    //Single instance of the class
    private static ARRenderer arRenderer ;

    private static Stack<GLOverlay> overlays ;

    private static float[] projectionMatrix ;

    private static float[] modelViewMatrix;
    private static float[] identityModelView;

    private ARRenderer() {
        super();
        overlays = new Stack<>();
    }

    public static ARRenderer getInstance() {
        if (arRenderer == null)
            arRenderer = new ARRenderer();

        return arRenderer;
    }

    @Override
    public void onSurfaceCreated(GL10 unusedGL, EGLConfig unusedconfig) {

        Log.d(LOGTAG, ": onSurfaceCreate");
        Vuforia.onSurfaceCreated();

        setModelViewMatrix();
    }

    @Override
    public void onSurfaceChanged(GL10 unusedGL, int width, int height) {
        Log.d(LOGTAG, " : onSurfaceChanged");
        Vuforia.onSurfaceChanged(width, height);

        CameraCalibration camCal = CameraDevice.getInstance().getCameraCalibration();
        projectionMatrix = getProjectionGL(camCal, 0.1f, 2000.f).getData();

    }

    @Override
    public void onDrawFrame(GL10 unused_gl) {

        //Clear with white color
        GLES20.glClearColor(1, 1, 1, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Renderer renderer = Renderer.getInstance();
        State state = renderer.begin();

        //Render camera background
        renderer.drawVideoBackground();

        if (!overlays.isEmpty()) {
        for (int i = 0; i < state.getNumTrackableResults(); i++) {
            //Get the tracker result
            Log.d(LOGTAG,"Number of Trackables : " + state.getNumTrackables());
            Log.d(LOGTAG,"Number of Trackable results : " + state.getNumTrackableResults());

            TrackableResult trackableResult = state.getTrackableResult(i);
            trackableResult.getTrackable().startExtendedTracking();

            //Get the modelView matrix
            modelViewMatrix = convertPose2GLMatrix(trackableResult.getPose()).getData();

            GLOverlay overlay = findOverlayfromTrackerName(trackableResult.getTrackable().getName());
            if (overlay != null) {
                overlay.render();
            }
        }
        //Render first overlay if not tracked
        if (!overlays.peek().isTracked())
            overlays.peek().render();
        }
        Renderer.getInstance().end();

        //Reset the modelViewMatrix to avoid any rendering with wrong modelViewMatrix
        modelViewMatrix = null ;
    }

    private void setModelViewMatrix(){
        identityModelView = new float[16];
        Matrix.setIdentityM(identityModelView, 0);
        identityModelView[5] = -1;
    }

    public void replaceOverlay(Texture t)
    {
        if (t == null) {
            Log.e(LOGTAG, "Cannot create overlay with null texture");
            return;
        }
        overlays.pop();
        overlays.add(new GLOverlay(t));
    }
    public void createOverlay(Texture t) {
        Log.i(LOGTAG, ": createOverlay");

        if (t == null) {
            Log.e(LOGTAG, "Cannot create overlay with null texture");
            return;
        }

        if (!overlays.empty() && !overlays.peek().isTracked())
        {
            //The old top overlay is removed if not being tracked
            overlays.pop();
        }
        //Add the new overlay on top of the stack
        overlays.add(new GLOverlay(t));
    }

    //Always transforms the top of the overlays stack
    public void transformSelectedOverlay(TransformType transformType, float value) {
        if (!overlays.isEmpty())
            overlays.peek().transformObject(transformType,value);
    }

    public void setTracked(String trackerName) {
        if (!overlays.empty())
            overlays.peek().setTracked(trackerName);
    }

    public float[] getProjection() {
        return projectionMatrix;
    }

    public float[] getModelView(boolean isTracked) {
        if (isTracked)
            return modelViewMatrix;

        else
            return identityModelView;
    }

    public GLOverlay findOverlayfromTrackerName(String tn)
    {
        for (GLOverlay gl : overlays)
        {
            if (gl.isTracked() &&  gl.getTrackerName().equals(tn))
                return gl ;
        }
        return null;
    }
}
