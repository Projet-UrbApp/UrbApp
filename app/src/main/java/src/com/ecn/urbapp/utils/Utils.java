package src.com.ecn.urbapp.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.FragmentManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import src.com.ecn.urbapp.activities.MainActivity;
import src.com.ecn.urbapp.db.DataObject;
import src.com.ecn.urbapp.db.Element;
import src.com.ecn.urbapp.db.GpsGeom;
import src.com.ecn.urbapp.db.Photo;
import src.com.ecn.urbapp.db.PixelGeom;
import src.com.ecn.urbapp.db.Project;
import src.com.ecn.urbapp.dialogs.ConfirmPhotoDialogFragment;

public class Utils{
	/**
	 * Funcion to copy a file
	 * @param src Source file to copy
	 * @param dst Destination ofthe copy
	 */
	public static void copy(File src, File dst){
		try {
		    InputStream in = new FileInputStream(src);
		    OutputStream out = new FileOutputStream(dst);
	
		    // Transfer bytes from in to out
		    byte[] buf = new byte[1024];
		    int len;
		    while ((len = in.read(buf)) > 0) {
		        out.write(buf, 0, len);
		    }
		    in.close();
		    out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Function converting the url gived by android into an usable path to the picture
	 * @param context of the application
	 * @param contentUri system path to the file
	 * @return url to the file
	 */
	public static String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try{ 
		    String[] proj = { MediaStore.Images.Media.DATA };
		    cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
		    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		    cursor.moveToFirst();
		    return cursor.getString(column_index);
		}finally{
		    if (cursor != null) {
		    	cursor.close();
		    }
		}
	}
	
	/**
	 * Function displaying a toast
	 * @param context Context of the Toast
	 * @param text Text write into the toast
	 * @param duration Time during which the toast is displayed
	 */
	public static void showToast(Context context, CharSequence text, int duration){
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
	}

	/**
	 * Function calling a dialog box which ask to the user to confirm the loading of a new project
	 */
	public static void confirm(FragmentManager fm){
		if(MainActivity.photo.getUrlTemp()!=null){
			ConfirmPhotoDialogFragment typedialog = new ConfirmPhotoDialogFragment();
			typedialog.show(fm, "CharacteristicsDialogFragment");
		}
	}
	
	/**
	 * methodes that give a String with the current date YYYY-MM-DD HH:MM:SS
	 */
	//TODO recuperer du serveur
	public static String getCurrentDate(){
		Date actual = new Date();
		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return (String) formater.format(actual);
	}
	
	/**
	 * 
	 * @param list list of elements to search
	 * @param id Id of the element that is finded
	 * @return return the element if it's in the list
	 */
	public static DataObject getElementById(List<? extends DataObject> list, long id){
		DataObject ret = null;
		for(DataObject dobj : list){
			if(dobj instanceof Element){
				if(((Element)dobj).getElement_id()==id){
					ret=dobj;
					break;
				}
			}
			else if(dobj instanceof GpsGeom){
				if(((GpsGeom)dobj).getGpsGeomsId()==id){
					ret=dobj;
					break;
				}
			}
			else if(dobj instanceof Photo){
				if(((Photo)dobj).getPhoto_id()==id){
					ret=dobj;
					break;
				}
			}
			else if(dobj instanceof PixelGeom){
				if(((PixelGeom)dobj).getPixelGeomId()==id){
					ret=dobj;
					break;
				}
			}
			else if(dobj instanceof Project){
				if(((Project)dobj).getProjectId()==id){
					ret=dobj;
					break;
				}
			}
		}
		return ret;		
	}
}