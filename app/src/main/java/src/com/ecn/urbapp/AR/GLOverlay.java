package src.com.ecn.urbapp.AR;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.qualcomm.vuforia.Vec4F;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Julien Vuillamy on 08/03/16.
 *
 * This class implements the GL rendering of a texture using Vuforia's tracker data.
 */

public class GLOverlay {

    private final static String LOGTAG = "GLOverlay";

    //Classes to identify Shader of Program loading problems
    public final static class GLShaderException extends Exception {
        public GLShaderException(String message) {
            super(message);
        }
    }
    public final static class GLProgramException extends Exception {
        public GLProgramException(String message) {
            super(message);
        }
    }

    private final static int BYTES_PER_FLOAT = 4 ;

    //Texture rendered on this view
    private final Texture texture ;

    //Tracker currently tracking this view
    private String trackerName ;

    //Vertices, texture coordinates and vector
    private float frameVertices[];
    private float frameTexCoords[];

    //Vector controling the transparency of an instance
    private Vec4F transColor ;

    //Program handle
    private int programHandle;

    //GL Attribute handles
    private int vertexHandle ;
    private int textureCoordHandle ;
    private int mvpMatrixHandle ;
    private int texSampler2DHandle;
    private int colorHandle;

    private boolean isTracked ;
    private boolean isInit ;

    //GLOverlay resize/reshaping values
    private float shearV = 0f ;
    private float shearH = 0f;
    private float scale = 0.1f ;

    public GLOverlay(Texture t) {
        texture = t;
        transColor = new Vec4F();
        isInit = false ;

        //A new GLOverlay is initialized as not tracked
        isTracked = false ;
    }

    /**
     * Creates the GL Vertices according to the user transformations
     */
    public void createObjectVertices () {
        frameVertices = new float[] {
                -texture.mWidth *scale,  texture.mHeight*scale, 100.0f+shearH+shearV,
                -texture.mWidth *scale, - texture.mHeight *scale, 100.0f-shearH+shearV,
                texture.mWidth *scale,  texture.mHeight *scale, 100.0f+shearH-shearV,

                -texture.mWidth *scale,- texture.mHeight *scale,100.0f-shearH+shearV,
                texture.mWidth *scale,- texture.mHeight *scale,100.0f-shearH-shearV,
                texture.mWidth*scale,  texture.mHeight *scale,100.0f+shearH-shearV
        };
    }

    /**
     * Initializes the view by creating the GL bindings, loading textures and creating the GL program
     * @return True if correctly initialized
     */
    public boolean init() {

        if (isInit)
            return true ;

        //Loading texture
        GLES20.glGenTextures(1, texture.mTextureID, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.mTextureID[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_TEXTURE_2D);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                texture.mWidth, texture.mHeight, 0, GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE, texture.mData);

        try {
            //Load shaders
            final int vertexShaderHandle = loadShader(GLES20.GL_VERTEX_SHADER, getVertexShader());
            final int fragmentShaderHandle = loadShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader());

            //Create program
            programHandle = createProgram(vertexShaderHandle, fragmentShaderHandle);
        }
        catch (GLShaderException e) {
            Log.e(LOGTAG, e.getMessage());
            return false ;
        }
        catch (GLProgramException e) {
            Log.e(LOGTAG, e.getMessage());
            return false ;
        }

        //Getting handles from program
        vertexHandle = GLES20.glGetAttribLocation(programHandle, "vertexPosition");
        textureCoordHandle = GLES20.glGetAttribLocation(programHandle, "vertexTexCoord");
        colorHandle = GLES20.glGetUniformLocation(programHandle, "colorKey");
        mvpMatrixHandle = GLES20.glGetUniformLocation(programHandle, "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(programHandle, "texSampler2D");



        if (vertexHandle == -1 || textureCoordHandle == -1
                || mvpMatrixHandle == -1)
            return false ;




        //Create vertices,textures coordinates and vector indices
        createObjectVertices();

        frameTexCoords = new float[] {
                0.0f, 1.0f,
                0.0f,0.0f,
                1.0f,1.0f,

                0.0f,0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f
        };

        float[] tempColor = new float[] {1.0f,1.0f,1.0f,0.7f};
        transColor.setData(tempColor);

//        //Set GL Flags

        isInit = true ;
        return true ;
    }

    /**
     * Called on every frame, renders the view in the middle of the screen if the overlay is not tracked,
     * renders using the Vuforia modelView matrix to correctly position the overlay otherwise
     */
    public void render() {

        if (!isInit)
            isInit = init();

//      Calculated the Model-View-Projection matrix
        float[] mvpMatrix = new float[16];

        float[] projectionMatrix = ARRenderer.getInstance().getProjection();
        float[] modelViewMatrix = ARRenderer.getInstance().getModelView(isTracked);

        //Extra precaution : in case the modelViewMatrix is null, abort rendering
        if (modelViewMatrix == null)
            return ;

        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);


        //Set GL Flags
        GLES20.glFrontFace(GLES20.GL_CCW);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //Set the shader program
        GLES20.glUseProgram(programHandle);

        //Set the vertex handle
        FloatBuffer verticesBuffer = ByteBuffer.allocateDirect(frameVertices.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        verticesBuffer.put(frameVertices).position(0);
        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT, false, 0, verticesBuffer);

        //Set the texture coordinate handle
        FloatBuffer texCoordBuffer = ByteBuffer.allocateDirect(frameTexCoords.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        texCoordBuffer.put(frameTexCoords).position(0);
        GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer);

        GLES20.glEnableVertexAttribArray(vertexHandle);
        GLES20.glEnableVertexAttribArray(textureCoordHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                texture.mTextureID[0]);

        //Set the ModelViewProjection matrix to the mvpHandle

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glUniform1i(texSampler2DHandle, 0);

        GLES20.glUniform4fv(colorHandle, 1, transColor.getData(), 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,6);

        GLES20.glDisableVertexAttribArray(vertexHandle);
        GLES20.glDisableVertexAttribArray(textureCoordHandle);

        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

    }

    /**
     * Method to create the vertexShader String
     * @return Vertex Shader
     */
    private String getVertexShader() {
        return  "attribute vec4 vertexPosition ; \n" +
                "attribute vec2 vertexTexCoord; \n" +
                "varying vec2 texCoord; \n" +
                "uniform mat4 modelViewProjectionMatrix; \n" +
                "void main() \n" +
                "{ \n" +
                "   gl_Position = modelViewProjectionMatrix * vertexPosition; \n" +
                "   texCoord= vertexTexCoord; \n" +
                "} \n";
    }

    /**
     * Method to create the fragmentShader String
     * @return Framgment Shader
     */
    private String getFragmentShader () {
        return  "precision mediump float ; \n" +
                "varying vec2 texCoord; \n" +
                "uniform vec4 colorKey; \n" +
                "uniform sampler2D texSampler2D; \n" +
                "void main() \n" +
                "{ \n" +
                "   vec4 texColor = texture2D(texSampler2D, texCoord); \n" +
                "   gl_FragColor = colorKey * texColor; \n" +
                "} \n" ;
    }

    /**
     * Creates a GL Shader according to its type and source and returns a shaderHandle
     * @param shaderType VertexShader or FragmentShader
     * @param shaderSource Shader string corresponding to the shaderType
     * @return Created ShaderHandle
     * @throws GLShaderException
     */
    private int loadShader(int shaderType, String shaderSource)
            throws GLShaderException {

        int shaderHandle ;

        shaderHandle = GLES20.glCreateShader(shaderType);
        if (shaderHandle != 0) {
            GLES20.glShaderSource(shaderHandle, shaderSource);
            GLES20.glCompileShader(shaderHandle);
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
            if (compileStatus[0] == 0) {
                String error = GLES20.glGetShaderInfoLog(shaderHandle);
                GLES20.glDeleteShader(shaderHandle);

                throw new GLShaderException("Error compiling shader : " + error);
            }
        } else
            throw new GLShaderException("Error creating shader");

        return shaderHandle;
    }

    /**
     * Binds a vertexShader and a fragmentShader into a GL programHandle
     * @param vertexShaderhandle
     * @param fragmentShaderhandle
     * @return programHandle
     * @throws GLProgramException
     */
    private int createProgram(int vertexShaderhandle, int fragmentShaderhandle)
            throws GLProgramException {

        int programHandle = GLES20.glCreateProgram();

        if (programHandle != 0) {

            GLES20.glAttachShader(programHandle, vertexShaderhandle);
            GLES20.glAttachShader(programHandle, fragmentShaderhandle);

            GLES20.glLinkProgram(programHandle);

            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            if (linkStatus[0] == 0) {
                String error = GLES20.glGetProgramInfoLog(programHandle);
                GLES20.glDeleteProgram(programHandle);

                throw new GLProgramException("Error linking program : " + error);
            }
        }
        else
            throw new GLProgramException("Error creating program.");

        return programHandle;
    }

    /**
     * Setter for boolean isTracked and pass in the tracker name
     * @param tN Tracker name for this overlay
     */
    public void setTracked(String tN)
    {
        isTracked = true ;
        trackerName = tN ;
    }

    /**
     * Getter for the boolean isTracked
     * @return isTracked
     */
    public boolean isTracked() { return isTracked;}

    /**
     * Transforms the overlay according to the TransformType and value
     * @param t Transformation : SCALE, SHEARH, or SHEARV
     * @param value value of the transformation
     */
    public void transformObject(TransformType t, float value)
    {
        switch (t)
        {
            case SCALE:
                scale = (float) Math.max((double) (scale * value), 0.001f);
                break ;
            case SHEARH:
                shearH += value;
                break ;
            case SHEARV:
                shearV +=value;
                break ;
        }
        createObjectVertices();
    }

    /**
     * Getter for the tracker name
     * @return trackerName
     */
    public String getTrackerName()
    {
        return trackerName;
    }
}
