package src.com.ecn.urbapp.fragments;

import java.util.ArrayList;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.pillet.urbapp2.R;
import src.com.ecn.urbapp.activities.MainActivity;
import src.com.ecn.urbapp.db.Composed;
import src.com.ecn.urbapp.db.Element;
import src.com.ecn.urbapp.db.ElementType;
import src.com.ecn.urbapp.db.GpsGeom;
import src.com.ecn.urbapp.db.Material;
import src.com.ecn.urbapp.db.PixelGeom;
import src.com.ecn.urbapp.db.Project;
import src.com.ecn.urbapp.syncToExt.Sync;

/**
 * @author	COHENDET Sebastien
 * 			DAVID Nicolas
 * 			GUILBART Gabriel
 * 			PALOMINOS Sylvain
 * 			PARTY Jules
 * 			RAMBEAU Merwan
 * 
 * SaveFragment class
 * 
 * This is the fragment used to save the project.
 * 			
 */
public class SaveFragment extends Fragment{

	private static final String TAG = "saveFragment";
	private Button saveToLocal = null;
	private Button saveToExt = null;
	private Button maxID = null;
	public static ProgressDialog dialog;
	Sync synchroExt = new Sync();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.layout_save, null);
		
		saveToLocal = (Button)v.findViewById(R.id.save_button_ync);
		saveToLocal.setOnClickListener(OnClickSaveToLocal);
		
		return v;
	}
	
    private OnClickListener OnClickSaveToLocal = new OnClickListener(){
    	public void onClick(View view){
			/**
			 * now we create the connection with the database
			 */
    		if(verificationBeforeSave()){
    			
    			/**
    			 * Launch the dialog to make user waits
    			 */
    			dialog = ProgressDialog.show(getActivity(), "", "Chargement. Veuillez patienter...", true);
    			
        		MainActivity.datasource.open();
        		
        		Boolean upload_photo = false;
    			if(MainActivity.photo.getPhoto_derniereModif() == 0) //new photo
    				upload_photo = true;
    			
        		Sync.getMaxId();
    			/**
    			 * first we need to put the date in Photo
    			 */
    			MainActivity.photo.setPhoto_derniereModif(synchroExt.maxId.get("date"));

        		/**
        		 * Sync local
        		 */
				boolean export = synchroExt.doSyncToExt(upload_photo);

				if(export){
					Sync.getMaxId();
					saveProjectListToLocal(MainActivity.project);
					saveGpsGeomListToLocal(MainActivity.gpsGeom);
					savePixelGeomListToLocal(MainActivity.pixelGeom);
					MainActivity.photo.saveToLocal(MainActivity.datasource);
					saveComposedListToLocal(MainActivity.composed);
					saveElementListToLocal(MainActivity.element);
					MainActivity.datasource.close();
				}
    		}
    		else{
    			Context context = MainActivity.baseContext;
    			CharSequence text = "Veuillez remplir l'ensemble des champs avant de sauvegarder";
    			int duration = Toast.LENGTH_SHORT;
    			Toast toast = Toast.makeText(context, text, duration);
    			toast.show();
    		}
    	}
    };
	
	public void saveProjectListToLocal(ArrayList<Project> l1){
		for (Project p : l1){
			p.saveToLocal(MainActivity.datasource);
		}
	}
	
	public void saveElementListToLocal(ArrayList<Element> l1){
		for (Element p : l1){
			p.saveToLocal(MainActivity.datasource);
		}
	}
	
	public void saveGpsGeomListToLocal(ArrayList<GpsGeom> l1){
		for (GpsGeom p : l1){
			p.saveToLocal(MainActivity.datasource);
		}
	}
	
	public void savePixelGeomListToLocal(ArrayList<PixelGeom> l1){
		for (PixelGeom p : l1){
			p.saveToLocal(MainActivity.datasource);
		}
	}
	
	public void saveElementTypeListToLocal(ArrayList<ElementType> l1){
		for (ElementType p : l1){
			p.saveToLocal(MainActivity.datasource);
		}
	}
	
	public void saveMaterialListToLocal(ArrayList<Material> l1){
		for (Material p : l1){
			p.saveToLocal(MainActivity.datasource);
		}
	}
	
	public void saveComposedListToLocal(ArrayList<Composed> l1){
		for (Composed p : l1){
			p.saveToLocal(MainActivity.datasource);
		}
	}
	
	/**
	 * Function verificating if all of the needed field are filled
	 * 
	 * @return true if all the field a set, else return false;
	 */
	public boolean verificationBeforeSave(){
		boolean ret = false;
		if(		MainActivity.photo.getGpsGeom_id()!=0 &&
				!MainActivity.photo.getPhoto_author().equals("") &&
				!MainActivity.photo.getPhoto_description().equals("")){
			Log.i(TAG,MainActivity.photo.toString());
			if(		!MainActivity.element.isEmpty()&&
					!MainActivity.gpsGeom.isEmpty()&&
					!MainActivity.pixelGeom.isEmpty()&&
					!MainActivity.project.isEmpty()){
				ret=true;
				Log.i(TAG,MainActivity.project.get(0).toString());
				if(ret){
					for(Element el : MainActivity.element){
						if(	!(	el.getElementType_id()!=0&&
								el.getGpsGeom_id()!=0&&
								el.getMaterial_id()!=0&&
								el.getPhoto_id()!=0&&
								el.getPixelGeom_id()!=0)){
							ret=false;
						}
						Log.i(TAG,el.toString());
					}
				}
				if(ret){
					ret=false;
					for(Composed c : MainActivity.composed){
						if(c.getPhoto_id()==MainActivity.photo.getPhoto_id()){
							ret=true;
						}
						Log.i(TAG,c.toString());
					}
				}
			}
		}
		Log.i(TAG,"element="+MainActivity.element.isEmpty()+" gpsgeom="+MainActivity.gpsGeom.isEmpty()+" pixelgeom="+MainActivity.pixelGeom.isEmpty()+" project="+MainActivity.project.isEmpty());
		return ret;
	}
}