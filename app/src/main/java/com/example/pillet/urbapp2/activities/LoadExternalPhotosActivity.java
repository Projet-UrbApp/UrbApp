package com.example.pillet.urbapp2.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.pillet.urbapp2.R;
import com.example.pillet.urbapp2.db.GpsGeom;
import com.example.pillet.urbapp2.db.Photo;
import com.example.pillet.urbapp2.syncToExt.Sync;
import com.example.pillet.urbapp2.utils.ConvertGeom;
import com.example.pillet.urbapp2.utils.CustomListViewAdapter;
import com.example.pillet.urbapp2.utils.ImageDownloader;
import com.example.pillet.urbapp2.utils.MathOperation;
import com.example.pillet.urbapp2.utils.RowItem;
import com.example.pillet.urbapp2.utils.Utils;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerClickListener;

/**
 * Selection of the photo in the current project from server
 * @author Sebastien
 *
 */
public class LoadExternalPhotosActivity extends Activity{
	/**
	 * Contains all the projects attributes
	 */
	private List<Photo> refreshedValues;

	/**
	 * Hashmap between unique id of markers and the relative project_id
	 */
	private HashMap<String, Integer> photosMarkers = new HashMap<String, Integer>();
	/**
	 * displayable photos list
	 */
	private ListView listePhotos = null;	
	/**
	 * The google map object
	 */
	private MapView map;
	/**
	 * The instance of  for map activity
	 */
	 protected GeoActivity displayedMap;

	/**
	 * The rows of the custom ListView (with images)
	 */
	private ArrayList<RowItem> rowItems;


	/**
	 * Contains the project id selected by user in previous activity
	 */
	long project_id;

	/**
	 * Barycenter in string, from loadLocalPrject
	 */
	String project_barycenter;
	
	/**
	 * all GpsGeom
	 */
	private List<GpsGeom> allGpsGeom;
	
	/**
	 * Instantiate the imageDowloader
	 */
	private ImageDownloader imageDownloader = new ImageDownloader();
	private IMapController mapController;
	OnMarkerClickListener markerClick;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_loadexternalphotos);

		/**
		 * extras that contains the project_id
		 */

		project_id = getIntent().getExtras().getLong("SELECTED_PROJECT_ID");

		map = (MapView) findViewById(R.id.mapView);
		map.setTileSource(TileSourceFactory.MAPNIK);
		map.setBuiltInZoomControls(true);
		map.setMultiTouchControls(true);
		map.setClickable(true);

		mapController = map.getController();
		mapController.setZoom(10);
		
		displayedMap = new GeoActivity(true, GeoActivity.defaultPos, map);

		project_barycenter = getIntent().getExtras().getString("PROJECT_COORD");
		GpsGeom barycenter = new GpsGeom();
		barycenter.setGpsGeomCoord(project_barycenter);
		displayedMap = new GeoActivity(false, MathOperation.barycenter(ConvertGeom.gpsGeomToGeoPoint(barycenter)), map);

		/**
		 * Define the listeners for switch satellite/plan/hybrid
		 */


		listePhotos = (ListView) findViewById(R.id.listViewPhotos);
		refreshListPhoto();


		listePhotos.setOnItemClickListener(selectedPhoto);
		markerClick  = new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker,MapView map) {
				
				for (Photo actualPhoto:Sync.refreshedValuesPhoto) {
					if ((int)actualPhoto.getPhoto_id() == photosMarkers.get(marker.getTitle()))
						MainActivity.photo = actualPhoto;
				}

				MainActivity.photo.setUrlTemp(Environment.getExternalStorageDirectory() + "/featureapp/" + MainActivity.photo.getPhoto_url());
				
				setResult(RESULT_OK);
				finish();

				Toast.makeText(MainActivity.baseContext, "Chargement de la photo", Toast.LENGTH_SHORT).show();
				return true;
			}
		};
	}

	protected void onClose() {      
		Utils.confirm(getFragmentManager());
	}

	
	/**
	 * creating a list of project and loads in the view
	 */
	public void refreshListPhoto(){      

		Sync Synchro = new Sync();
    	if (Synchro.getPhotosFromExt(project_id)){
    		try{
    			refreshedValues=Sync.refreshedValuesPhoto;
    			allGpsGeom=Sync.allGpsGeom;
    		}
    		catch (Exception e){
    			Log.e("DFHUPLOAD", "Pb de data");
    		}
    	}
    	
		rowItems = new ArrayList<RowItem>();
		
		synchronized (refreshedValues) {
			int i=0;
			for (Photo image:refreshedValues) {
				/**
				 * Download each photo and register it on tablet
				 */
				String imageStoredUrl = imageDownloader.download(MainActivity.serverURL+"images/", image.getPhoto_url());

				
				RowItem item = new RowItem(Environment.getExternalStorageDirectory()+"/featureapp/"+image.getPhoto_url(),"Photo n°"+i,image.getPhoto_description());
				rowItems.add(item);
				i++;
			}
		}
		
		CustomListViewAdapter adapter = new CustomListViewAdapter(this,
				R.layout.layout_photolistview, rowItems);
		listePhotos.setAdapter(adapter);

		/**
		 * Put markers on the map
		 */
		Integer i = Integer.valueOf(0);
		for (Photo enCours:refreshedValues){
			
			//TODO request for GPSGeom
			ArrayList<GeoPoint> photoGPS = null;
			for(GpsGeom gg : allGpsGeom){
				if(gg.getGpsGeomsId()==enCours.getGpsGeom_id()){
					photoGPS = ConvertGeom.gpsGeomToGeoPoint(gg);
				}
			}
			//end of fake photoGPS values
			
			GeoPoint GPSCentered = MathOperation.barycenter(photoGPS);
					
			Marker marker = displayedMap.addMarkersColored(i, "Cliquez ici pour valider cette photo", GPSCentered);
			marker.setOnMarkerClickListener(markerClick);

			/**
			 * Adding the line in the map
			 */
			
			displayedMap.drawPolygon(photoGPS, false);

			photosMarkers.put(marker.getTitle(), (int) enCours.getPhoto_id());
			i++;
		}
		map.invalidate();
	}    

	/**
	 *  get the project selected in listview and show its position on the map 
	 */
	public OnItemClickListener selectedPhoto = new OnItemClickListener()
	{
		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position,
				long id) {
			List<GpsGeom> allGpsGeom = Sync.allGpsGeom;
			ArrayList<GeoPoint> photoGPS = null;
			for(GpsGeom gg : allGpsGeom){
				if(gg.getGpsGeomsId()==refreshedValues.get(position).getGpsGeom_id()){
					photoGPS = ConvertGeom.gpsGeomToGeoPoint(gg);
				}
			}
			GeoPoint GPSCentered = MathOperation.barycenter(photoGPS);
			displayedMap = new GeoActivity(false, GPSCentered, map);
		}
	};
}