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
import src.com.ecn.urbapp.db.LocalDataSource;
import src.com.ecn.urbapp.db.Photo;
import src.com.ecn.urbapp.utils.ConvertGeom;
import src.com.ecn.urbapp.utils.CustomListViewAdapter;
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

public class LoadLocalPhotosActivity extends Activity{
	private static final String TAG = "localPhotos";
	/**
	 * creating datasource
	 */
	private LocalDataSource datasource;
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
	private MapView map = null;
	/**
	 * The instance of GeoActivity for map activity
	 */
	GeoActivity displayedMap;
	private Polygon polygon;

	//TODO add description for javadoc
	private ArrayList<RowItem> rowItems;

	//TODO add description for javadoc
	long project_id;

	/**
	 * Barycenter in string, from loadLocalPrject
	 */
	String project_barycenter;

	private IMapController mapController;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_loadlocalphotos);
		datasource=MainActivity.datasource;
		datasource.open();

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

		project_barycenter = getIntent().getExtras().getString("PROJECT_COORD");
		GpsGeom barycenter = new GpsGeom();
		barycenter.setGpsGeomCoord(project_barycenter);
		displayedMap = new GeoActivity(false, MathOperation.barycenter(ConvertGeom.gpsGeomToGeoPoint(barycenter)), map);
		listePhotos = (ListView) findViewById(R.id.listViewPhotos);
		refreshListPhoto();

		listePhotos.setOnItemClickListener(selectedPhoto);
	}

	public OnMarkerClickListener markerClick = new OnMarkerClickListener() {
		@Override
		public boolean onMarkerClick(Marker marker, MapView mapView) {
			Toast.makeText(MainActivity.baseContext, "Chargement de la photo", Toast.LENGTH_SHORT).show();
			//TODO Sebastien has to make it more readable
			MainActivity.datasource.instanciatePhoto(refreshedValues.get(photosMarkers.get(marker.getTitle())).getPhoto_id());
			//TODO do a better way to have the path !
			MainActivity.photo.setUrlTemp(Environment.getExternalStorageDirectory()+"/featureapp/"+refreshedValues.get(photosMarkers.get(marker.getTitle())).getPhoto_url());
			setResult(RESULT_OK);
			return true;
		}
	};
	protected void onClose() {      
		datasource.close();
		Utils.confirm(getFragmentManager());
	}

	//TODO add description for javadoc
	/**
	 * loading the different projects of the local db
	 * @return
	 */
	public List<Photo> recupPhoto() {
		//List<Photo> values = this.datasource.getAllPhotos();
		//TODO CATCH EXCEPTION
		List<Photo> values = this.datasource.getAllPhotolinkedtoProject(project_id);
		return values;
	}

	/**
	 * loading the different projects of the local db
	 * @return
	 */
	public List<GpsGeom> recupGpsGeom() {
		//List<Photo> values = this.datasource.getAllPhotos();
		//TODO CATCH EXCEPTION
		List<GpsGeom> values = this.datasource.getAllGpsGeom();
		return values;
	}

	/**
	 * creating a list of project and loads in the view
	 */
	public void refreshListPhoto(){
		refreshedValues = recupPhoto();
		List<GpsGeom> allGpsGeom = recupGpsGeom();
		rowItems = new ArrayList<RowItem>();
		int cajou=0;
		for (Photo image:refreshedValues) {
			RowItem item = new RowItem(Environment.getExternalStorageDirectory()+"/featureapp/"+image.getPhoto_url(),"Photo n°"+cajou,image.getPhoto_description());
			rowItems.add(item);
			cajou++;
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
			Marker marker = new Marker(map);
			marker.setPosition(GPSCentered);
			marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
			marker.setIcon(this.getResources().getDrawable(R.drawable.marker_icon));
			marker.setTitle("Cliquez ici pour valider la photo");
			marker.setPanToView(true);
			marker.setOnMarkerClickListener(markerClick);
			map.getController().setCenter(GPSCentered);
			map.getController().setZoom(map.getMaxZoomLevel());
			map.getOverlays().add(marker);
			/**
			 * Adding the line in the map
			 */
			drawPolygon(photoGPS, false);
			photosMarkers.put(marker.getTitle(), i);
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
			Log.i(TAG,"Click on list");
			List<GpsGeom> allGpsGeom = recupGpsGeom();
			ArrayList<GeoPoint> photoGPS = null;
			for(GpsGeom gg : allGpsGeom){
				if(gg.getGpsGeomsId()==refreshedValues.get(position).getGpsGeom_id()){
					photoGPS = ConvertGeom.gpsGeomToGeoPoint(gg);
				}
			}
			GeoPoint GPSCentered = MathOperation.barycenter(photoGPS);
			displayedMap = new GeoActivity(false, GPSCentered, map);
			map.invalidate();
		}
	};

	/**
	 * Due to context problem this activity has its on drawPolygon method
	 * @param points
	 * @param refresh
	 */
	public void drawPolygon(ArrayList<GeoPoint> points, Boolean refresh){
		if (points.size()>=2) {
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