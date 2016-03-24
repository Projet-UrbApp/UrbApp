package src.com.ecn.urbapp.activities;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

		setContentView(R.layout.activity_main);

		//Instanciation of the elements  for the database
		composed = new ArrayList<>();
		element = new ArrayList<>();
		elementType = new ArrayList<>();
		gpsGeom = new ArrayList<>();
		material = new ArrayList<>();
		pixelGeom = new ArrayList<>();
		project = new ArrayList<>();
		photo = new Photo();
		
		fragments=new Vector<>();
		//Setting the Context of app
		baseContext = getBaseContext();
		
		alertDialog = new Builder(MainActivity.this);
		isInternetOn();
		
		//Setting the Context of app
		baseContext = getBaseContext();
		
		//initialization of the local database
		datasource = new LocalDataSource(this);


		//Creating Tabs String array
		String[] mtabsList = new String[]{
				getString(R.string.homeFragment),
				getString(R.string.informationFragment),
				getString(R.string.zoneFragment),
				getString(R.string.definitionFragment),
				getString(R.string.saveFragment),
				getString(R.string.AR_Activity)
			};


		//Setting the Activity drawer
		final FragmentManager fragmentManager = getFragmentManager();

		HomeFragment homeFragment = new HomeFragment();
		fragmentManager.beginTransaction().replace(R.id.container,homeFragment).commit();

		final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		final ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerList.setAdapter(new ArrayAdapter<>(this,
				R.layout.drawer_list_item, mtabsList));

		mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				switch (i) {
					case 0:
						HomeFragment homeFragment = new HomeFragment();
						fragmentManager.beginTransaction().replace(R.id.container, homeFragment).commit();
						break;
					case 1:
						InformationFragment informationFragment = new InformationFragment();
						fragmentManager.beginTransaction().replace(R.id.container, informationFragment).commit();
						break;
					case 2:
						ZoneFragment zoneFragment = new ZoneFragment();
						fragmentManager.beginTransaction().replace(R.id.container, zoneFragment).commit();
						break;
					case 3:
						CharacteristicsFragment definitionFragment = new CharacteristicsFragment();
						fragmentManager.beginTransaction().replace(R.id.container, definitionFragment).commit();
						break;
					case 4:
						SaveFragment saveFragment = new SaveFragment();
						fragmentManager.beginTransaction().add(R.id.container, saveFragment).commit();
						break;
					case 5:
						Intent intent = new Intent(baseContext,AugmentedRealityActivity.class);
						startActivity(intent);
						break ;
				}
				mDrawerList.setItemChecked(i, true);
				mDrawerLayout.closeDrawer(mDrawerList);
			}
		});


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
//		MenuInflater inflater =	getMenuInflater();
//		inflater.inflate(R.menu.menu_main, menu);
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