package src.com.ecn.urbapp.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import android.util.Log;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.widget.ListView;
import android.widget.Toast;

import com.example.pillet.urbapp2.R;
import src.com.ecn.urbapp.db.GpsGeom;
import src.com.ecn.urbapp.db.Photo;
import src.com.ecn.urbapp.syncToExt.Sync;
import src.com.ecn.urbapp.utils.ConvertGeom;
import src.com.ecn.urbapp.utils.CustomListViewAdapter;
import src.com.ecn.urbapp.utils.ImageDownloader;
import src.com.ecn.urbapp.utils.MathOperation;
import src.com.ecn.urbapp.utils.RowItem;
import src.com.ecn.urbapp.utils.Utils;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Polygon;
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
	private static final String TAG = "externalPhotos";
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

	private Polygon polygon;

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

		IMapController mapController = map.getController();
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
	}

	/**
	 * click twice on marker to load photo
	 */
	public OnMarkerClickListener markerClick  = new OnMarkerClickListener() {
		@Override
		public boolean onMarkerClick(Marker marker, MapView mapView) {
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

	protected void onClose(){
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
		synchronized(refreshedValues){
			int i=1;
			for (Photo image:refreshedValues) {
				/**
				 * Download each photo and register it on tablet
				 */
				String imageStoredUrl = imageDownloader.download(MainActivity.serverURL+"images/", image.getPhoto_url());
				RowItem item = new RowItem(Environment.getExternalStorageDirectory()+"/featureapp/"+image.getPhoto_url(),"Photo n°"+i,image.getPhoto_description(),image.getPhoto_id());
				rowItems.add(item);
				i++;
			}
		}
		CustomListViewAdapter adapter = new CustomListViewAdapter(this, R.layout.layout_photolistview, rowItems);
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

			Marker marker = new Marker(map);
			marker.setPosition(GPSCentered);
			marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
			marker.setIcon(this.getResources().getDrawable(R.drawable.marker_icon));
			marker.setTitle("Cliquez ici pour charger la photo");
			marker.setPanToView(true);
			marker.setOnMarkerClickListener(markerClick);
			map.getController().setCenter(GPSCentered);
			map.getController().setZoom(map.getMaxZoomLevel());
			map.getOverlays().add(marker);

			/**
			 * Adding the line in the map
			 */
			drawPolygon(photoGPS, false);
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
			map.getController().setCenter(GPSCentered);
			map.getController().setZoom(map.getMaxZoomLevel());
			map.invalidate();

			for (Photo actualPhoto :Sync.refreshedValuesPhoto) {
				if ((int)actualPhoto.getPhoto_id() == position)
					MainActivity.photo = actualPhoto;
			}
			MainActivity.photo.setUrlTemp(Environment.getExternalStorageDirectory() + "/featureapp/" + MainActivity.photo.getPhoto_url());
			setResult(RESULT_OK);
			finish();
			Toast.makeText(MainActivity.baseContext, "Chargement de la photo", Toast.LENGTH_SHORT).show();
		}
	};

	/**
	 * Due to context problem this activity has its on drawPolygon method
	 * @param points points of the polygon
	 * @param refresh to redraw the polygon
	 */
	public void drawPolygon(ArrayList<GeoPoint> points, Boolean refresh){
		if (points.size()>=2){
			if (polygon!=null && refresh)
				map.getOverlays().remove(polygon);
			//Instantiates a new Polygon object and adds points to define a rectangle
			polygon = new Polygon(this);
			polygon.setPoints(points);
			if(polygon!=null && refresh)
				map.getOverlays().remove(polygon);
			// Add polygon as an overlay drawn before the marker
			map.getOverlays().add(0,polygon);
		}
	}
}