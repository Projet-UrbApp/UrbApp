package src.com.ecn.urbapp.AR;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Julien Vuillamy on 08/03/16.
 * Inspired by the Texture class from VuforiaSamples
 * https://developer.vuforia.com/downloads/samples
 */

public class Texture {

    private static final String LOGTAG = "Texture";

    public int mWidth ;
    public int mHeight;
    public ByteBuffer mData ;
    public int[] mTextureID= new int[1];

    public static Texture loadTextureFromApk(String fileName, AssetManager assets) {
        InputStream inputStream;
        try
        {
            inputStream = assets.open(fileName, AssetManager.ACCESS_BUFFER);

            BufferedInputStream bufferedStream = new BufferedInputStream(
                    inputStream);
            Bitmap bitMap = BitmapFactory.decodeStream(bufferedStream);

            int[] data = new int[bitMap.getWidth() * bitMap.getHeight()];
            bitMap.getPixels(data, 0, bitMap.getWidth(), 0, 0,
                    bitMap.getWidth(), bitMap.getHeight());

            return loadTextureFromIntBuffer(data, bitMap.getWidth(),
                    bitMap.getHeight());
        } catch (IOException e)
        {
            Log.e(LOGTAG, "Failed to log texture '" + fileName + "' from APK");
            Log.i(LOGTAG, e.getMessage());
            return null;
        }
    }

    public static Texture loadTexture(Bitmap bitmap)
    {

        int[] data = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(data, 0, bitmap.getWidth(), 0, 0,
                bitmap.getWidth(), bitmap.getHeight());

        return loadTextureFromIntBuffer(data, bitmap.getWidth(),
                bitmap.getHeight());
    }
    private static Texture loadTextureFromIntBuffer(int[] data, int width, int height) {
        // Convert:
        int numPixels = width * height;
        byte[] dataBytes = new byte[numPixels * 4];

        for (int p = 0; p < numPixels; ++p)
        {
            int colour = data[p];
            dataBytes[p * 4] = (byte) (colour >>> 16); // R
            dataBytes[p * 4 + 1] = (byte) (colour >>> 8); // G
            dataBytes[p * 4 + 2] = (byte) colour; // B
            dataBytes[p * 4 + 3] = (byte) (colour >>> 24); // A
        }

        Texture texture = new Texture();
        texture.mWidth = width;
        texture.mHeight = height;

        texture.mData = ByteBuffer.allocateDirect(dataBytes.length).order(
                ByteOrder.nativeOrder());

        int rowSize = texture.mWidth * 4;
        for (int r = 0; r < texture.mHeight; r++)
            texture.mData.put(dataBytes, rowSize * (texture.mHeight - 1 - r),
                    rowSize);

        texture.mData.rewind();

        // Cleans variables
        dataBytes = null;
        data = null;

        return texture;
    }


}
