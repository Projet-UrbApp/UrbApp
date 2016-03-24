package src.com.ecn.urbapp.AR;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Julien Vuillamy on 04/03/16.
 */

public class ARSurfaceView extends GLSurfaceView implements View.OnTouchListener {

    private final static String LOGTAG = "ARSurfaceView";

    //Instance of the renderer rendering this GLView
    private final ARRenderer renderer;

    //Variables used to control touch events
    private int touch_number = 0;
    private float oldDist = 1f;
    private float oldX, oldY;

    public ARSurfaceView(Context context) {
        super(context);

        //Set OpenGL to the version 2
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);
        //Get the GLSurfaceView renderer
        renderer = ARRenderer.getInstance();
        setRenderer(renderer);
        setOnTouchListener(this);
    }

    /**
     * Method called by touch events and used to resize/reshape the current GLOverlay
     * @param v
     * @param event
     * @return True
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        Log.i(LOGTAG, "Number of fingers : " + touch_number);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                touch_number--;
                break;

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                touch_number++;
                if (touch_number == 2) {
                    oldX = event.getX(1);
                    oldY = event.getY(1);
                    oldDist = spacing(event);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (touch_number == 2) { //Two fingers defines a scale or shearing operation
                    float newDist = spacing(event);
                    if (newDist > 5f) {

                        float varDist = newDist - oldDist;
                        if (Math.abs(varDist)> 5f) {
                            //Fingers are separating : scaling operation
                            Log.i(LOGTAG, "SCALE_ACTION");
                            float scale = newDist / oldDist ;
                            renderer.transformSelectedOverlay(TransformType.SCALE, scale);
                            oldX = event.getX(1);
                            oldY = event.getY(1);
                        } else {
                            //Fingers are at constant distance : shearing operation

                            //Variations in the two axis
                            float varX = event.getX(1) - oldX;
                            float varY = event.getY(1) - oldY;

                            if (Math.abs(varY) > 5f) {
                                //Horizontal variation
                                float shear = varY / 10;
                                renderer.transformSelectedOverlay(TransformType.SHEARH, shear);
                            }
                            if (Math.abs(varX) > 3f) {
                                //Vertical variation
                                float shear = varX / 10;
                                renderer.transformSelectedOverlay(TransformType.SHEARV, shear);
                            }
                            oldX = event.getX(1);
                            oldY = event.getY(1);
                        }


                        oldDist = newDist;
                    }
                }
        }
        return true;
    }

    /**
     * Utility to mesure the spacing between two fingers
     * @param event
     * @return
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt((double) x * x + y * y);
    }

}
