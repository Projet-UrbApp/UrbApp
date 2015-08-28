package src.com.ecn.urbapp.activities;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import com.example.pillet.urbapp2.R;
import src.com.ecn.urbapp.db.Composed;
import src.com.ecn.urbapp.db.Element;
import src.com.ecn.urbapp.db.ElementType;
import src.com.ecn.urbapp.db.GpsGeom;
import src.com.ecn.urbapp.db.LocalDataSource;
import src.com.ecn.urbapp.db.Material;
import src.com.ecn.urbapp.db.Photo;
import src.com.ecn.urbapp.db.PixelGeom;
import src.com.ecn.urbapp.db.Project;
import src.com.ecn.urbapp.fragments.CharacteristicsFragment;
import src.com.ecn.urbapp.fragments.HomeFragment;
import src.com.ecn.urbapp.fragments.InformationFragment;
import src.com.ecn.urbapp.fragments.SaveFragment;
import src.com.ecn.urbapp.fragments.ZoneFragment;
import src.com.ecn.urbapp.listener.MyTabListener;
import src.com.ecn.urbapp.syncToExt.Sync;
import src.com.ecn.urbapp.utils.ConnexionCheck;
import src.com.ecn.urbapp.utils.Cst;
import src.com.ecn.urbapp.utils.Utils;

/**
 * @author	COHENDET Sébastien
 * 			DAVID Nicolas
 * 			GUILBART Gabriel
 * 			PALOMINOS Sylvain
 * 			PARTY Jules
 * 			RAMBEAU Merwan
 * 
 * MainActivity class
 * 
 * This is the main activity of the application.
 * It contains an action bar filled with the different fragments
 * 			
 */
//TODO pass photo into data bundle
//TODO verify for the local save

public class MainActivity extends Activity {
	private static final String TAG = "mainActivity";
	/**
	 * Attribut represent the action bar of the application
	 */
	private ActionBar bar;
	
	/**
	 * Attribut representing the local database
	 */
	public static LocalDataSource datasource;

	/**
	 * BaseContext to get the static context of app anywhere (for file)
	 */
    public static Context baseContext;

	/**
	 * Dialog box displayed in the entire screen
	 */
	private static Builder alertDialog;

	/**
	 * Link to ask google to create a specific connexion code to check if there is no portal between android and server
	 */
    public static final String CONNECTIVITY_URL="http://clients3.google.com/generate_204";
    
    /**
     * Server address
     */
    public static String serverURL="http://urbapp.ser-info-02.ec-nantes.fr/";

	/**
	 * List of all the fragments displayed in the action bar
	 */
	private Vector<Fragment> fragments=null;
	
	/**ArrayList for the elements of the database**/

	/**
	 * ArrayList of the database element Composed
	 */
	public static ArrayList<Composed> composed=null;
	/**
	 * ArrayList of the database element Element
	 */
	public static ArrayList<Element> element=null;
	/**
	 * ArrayList of the database element ElementType
	 */
	public static ArrayList<ElementType> elementType=null;
	/**
	 * ArrayList of the database element GpsGeom
	 */
	public static ArrayList<GpsGeom> gpsGeom=null;
	/**
	 * ArrayList of the database element Material
	 */
	public static ArrayList<Material> material=null;
	/**
	 * ArrayList of the database element PixelGeom
	 */
	public static ArrayList<PixelGeom> pixelGeom=null;
	/**
	 * ArrayList of the database element Project
	 */
	public static ArrayList<Project> project=null;
	/**
	 * Database element Photo
	 */
	public static Photo photo=null;

	/**
	 * Boolean to check if internet is on
	 */
	
	public static boolean internet = true;
	/**
	 * Static reference to the ZoneFragment.
	 */
	public static ZoneFragment zone;

	/**
	 * Getter for the ZoneFragment.
	 */
	public static ZoneFragment getZoneFragment() {
		return zone;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		
		//Instanciation of the elements  for the database
		composed = new ArrayList<Composed>();
		element = new ArrayList<Element>();
		elementType = new ArrayList<ElementType>();
		gpsGeom = new ArrayList<GpsGeom>();
		material = new ArrayList<Material>();
		pixelGeom = new ArrayList<PixelGeom>();
		project = new ArrayList<Project>();
		photo = new Photo();
		
		fragments=new Vector<Fragment>();
		//Setting the Context of app
		baseContext = getBaseContext();
		
		alertDialog = new Builder(MainActivity.this);
		isInternetOn();
		
		//Setting the Context of app
		baseContext = getBaseContext();
		
		//initialization of the local database
		datasource = new LocalDataSource(this);
		
		//Setting the Activity bar
		bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		bar.setDisplayHomeAsUpEnabled(true);
		
		//Home tab
		Tab tabHome =  bar.newTab();
		tabHome.setText(R.string.homeFragment);
		HomeFragment home = new HomeFragment();
		tabHome.setTabListener(new MyTabListener(home));
		bar.addTab(tabHome);
		fragments.add(home);
		
		//Information tab
		Tab tabInformation =  bar.newTab();
		tabInformation.setText(R.string.informationFragment);
		InformationFragment information = new InformationFragment();
		tabInformation.setTabListener((new MyTabListener(information)));
		bar.addTab(tabInformation);
		fragments.add(information);
		
		//Zone tab
		Tab tabZone =  bar.newTab();
		tabZone.setText(R.string.zoneFragment);
		zone = new ZoneFragment();
		tabZone.setTabListener(new MyTabListener(zone));
		bar.addTab(tabZone);
		fragments.add(zone);
		
		//Definition tab
		Tab tabDefinition =  bar.newTab();
		tabDefinition.setText(R.string.definitionFragment);
		CharacteristicsFragment definition = new CharacteristicsFragment();
		tabDefinition.setTabListener(new MyTabListener(definition));
		bar.addTab(tabDefinition);
		fragments.add(definition);
		
		//Save tab
		Tab tabSave =  bar.newTab();
		tabSave.setText(R.string.saveFragment);
		SaveFragment save = new SaveFragment();
		tabSave.setTabListener(new MyTabListener(save));
		bar.addTab(tabSave);
		fragments.add(save);
		
		//TODO coordinate with the remote databasetellip
		datasource.open();
		datasource.getAllElementType();
		datasource.getAllMaterial();
		if (this.elementType.isEmpty()||this.material.isEmpty()){
			Sync s = new Sync();
			s.getTypeAndMaterialsFromExt();
			for (ElementType elmtT : this.elementType){
				elmtT.saveToLocal(MainActivity.datasource);
			}
			for (Material elmtT : this.material){
				elmtT.saveToLocal(MainActivity.datasource);
			}
		}
		datasource.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu, this adds items to the action bar if it is present.
		MenuInflater inflater =	getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	//TODO transfert to Utils.COnnexionChecked
	/**
	 * Method to check if internet is available (and no portal !)
	 */
	public final void isInternetOn() {
		ConnectivityManager con=(ConnectivityManager)getSystemService(Activity.CONNECTIVITY_SERVICE);
		boolean wifi=con.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
		boolean mobile = false;
		try{
			mobile=con.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
		}
		catch (NullPointerException e){
			mobile=false;
		}
		boolean internet=wifi|mobile;
		if (internet)
			new ConnexionCheck().Connectivity();
	}

	//TODO transfert to Utils.COnnexionChecked
	/**
	 * Method if no internet connectivity to print a Dialog.
	 */
	public static void errorConnect() {
		alertDialog.setTitle("Pas de connexion internet de disponible. Relancer l'application, une fois internet fonctionnel");
		alertDialog.show();		
		internet=false;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//TODO verify resultcode for local projectload
		Utils.showToast(MainActivity.baseContext, "Retour à l'application", Toast.LENGTH_SHORT);
		if (resultCode == RESULT_OK) {
			switch(requestCode){
				case Cst.CODE_TAKE_PICTURE:
					Utils.confirm(getFragmentManager());
	
					String pathImage = MainActivity.photo.getUrlTemp();
					MainActivity.photo.setPhoto_url(pathImage.split("/")[pathImage.split("/").length-1]);
					MainActivity.photo.setPhoto_id(1);
					MainActivity.photo.setUrlTemp(null);
					
					getActionBar().setSelectedNavigationItem(1);
				break;
				case Cst.CODE_LOAD_LOCAL_PROJECT:
					if(MainActivity.project.isEmpty()){
						datasource.instanciateAllElement();
						datasource.instanciateAllGpsGeom();
						datasource.instanciateAllProject();
						datasource.instanciateAllpixelGeom();
						
						for(Element e : element){
							e.setRegistredInLocal(true);
						}
						for(GpsGeom g : gpsGeom){
							g.setRegistredInLocal(true);
						}
						for(Project p : project){
							p.setRegistredInLocal(true);
						}
						for(PixelGeom p : pixelGeom){
							p.setRegistredInLocal(true);
						}
						
						MainActivity.photo.setRegistredInLocal(true);
						MainActivity.photo.setUrlTemp(null);
						
						getActionBar().setSelectedNavigationItem(2);
					}
				break;
				case Cst.CODE_LOAD_EXTERNAL_PROJECT:
					
					datasource.instanciateAllElement();
					datasource.instanciateAllGpsGeom();
					datasource.instanciateAllProject();
					datasource.instanciateAllpixelGeom();

					ArrayList<Element> element = Sync.allElement;
					ArrayList<GpsGeom> gpsGeom = Sync.allGpsGeom;
					ArrayList<Project> project = Sync.refreshedValues;
					ArrayList<PixelGeom> pixelGeom = Sync.allPixelGeom;
					MainActivity.composed = Sync.allComposed;

					for(Element e : element){
						if(Utils.getElementById(MainActivity.element, e.getElement_id()) != null)
							e.setRegistredInLocal(true);
					}
					for(GpsGeom g : gpsGeom){
						if(Utils.getElementById(MainActivity.gpsGeom, g.getGpsGeomsId()) != null)
							g.setRegistredInLocal(true);
					}
					for(Project p : project){
						if(Utils.getElementById(MainActivity.project, p.getProjectId()) != null)
							p.setRegistredInLocal(true);
					}
					for(PixelGeom p : pixelGeom){
						if(Utils.getElementById(MainActivity.pixelGeom, p.getPixelGeomId()) != null)
							p.setRegistredInLocal(true);
					}

					MainActivity.element=element;
					MainActivity.gpsGeom=gpsGeom;
					MainActivity.project=project;
					MainActivity.pixelGeom=pixelGeom;

					MainActivity.photo.setRegistredInLocal(true);
					MainActivity.photo.setUrlTemp(null);

					getActionBar().setSelectedNavigationItem(2);
				break;
				case Cst.CODE_LOAD_PICTURE:
					Utils.confirm(getFragmentManager());
	
					String url = Utils.getRealPathFromURI(baseContext, data.getData());
					MainActivity.photo.setPhoto_url(url.split("/")[url.split("/").length-1]);
					MainActivity.photo.setPhoto_id(1);
					MainActivity.photo.setUrlTemp(null);
					
					if(!url.equals(Environment.getExternalStorageDirectory()+"/featureapp/"+MainActivity.photo.getPhoto_url())){
						Utils.copy(new File(url), new File(Environment.getExternalStorageDirectory()+"/featureapp/"+MainActivity.photo.getPhoto_url()));
					}
					
					getActionBar().setSelectedNavigationItem(1);
				break;
				case 10:
					getActionBar().setSelectedNavigationItem(1);
				break;
			}
        }
    }
	
	/**
	 * Function called when the back button of the screen is called. It will display the previous fragment.
	 */
	@Override
	public void onBackPressed(){

		int i=0;
		for(Fragment f : fragments){
			if(f.isVisible()){
				break;
			}
			i++;
		}
		if(i>0){
			getActionBar().selectTab(getActionBar().getTabAt(i-1));
		}
	}
}