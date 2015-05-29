package com.example.pillet.urbapp2.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.pillet.urbapp2.R;
import com.example.pillet.urbapp2.db.GpsGeom;
import com.example.pillet.urbapp2.db.LocalDataSource;
import com.example.pillet.urbapp2.db.Project;
import com.example.pillet.urbapp2.utils.ConvertGeom;
import com.example.pillet.urbapp2.utils.MathOperation;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerClickListener;


public class LoadLocalProjectsActivity extends Activity {

	/**
	 * creating datasource
	 */
	private LocalDataSource datasource;
	/**
	 * Contains all the projects attributes
	 */
	private List<Project> refreshedValues;
	
	/**
	 * COntains all GPSInfo from all Project
	 */
	private List<GpsGeom> allGpsGeom;
	
	/**
	 * Hashmap between unique id of markers and the relative project_id
	 */
	private HashMap<String, Integer> projectMarkers = new HashMap<String, Integer>();
	/**
	 * displayable project list
	 */
	private ListView listeProjects = null;	
    /**
     * The google map object
     */
    private MapView map = null;
    /**
     * The instance of GeoActivity for map activity
     */
    private GeoActivity displayedMap;
    OnMarkerClickListener markerClick;
    private IMapController mapController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_loadlocaldb);
        datasource=MainActivity.datasource;
        datasource.open();

        map = (MapView) findViewById(R.id.mapView);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setClickable(true);

        mapController = map.getController();
        mapController.setZoom(10);
        
        displayedMap = new GeoActivity(true, GeoActivity.defaultPos, map);
        
        listeProjects = (ListView) findViewById(R.id.listView);
        refreshList();
        
        listeProjects.setOnItemClickListener(selectedProject);
        markerClick =new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker,MapView map) {
                Toast.makeText(MainActivity.baseContext, refreshedValues.get(projectMarkers.get(marker.getTitle())).toString(), Toast.LENGTH_LONG).show();
                Intent i = new Intent(getApplicationContext(), LoadLocalPhotosActivity.class);
                i.putExtra("SELECTED_PROJECT_ID", refreshedValues.get(projectMarkers.get(marker.getTitle())).getProjectId());

                ArrayList<GeoPoint> coordProjet = new ArrayList<GeoPoint>();

                for (GpsGeom gg : allGpsGeom) {
                    if (refreshedValues.get(projectMarkers.get(marker.getTitle())).getGpsGeom_id() == gg.getGpsGeomsId()) {
                        coordProjet.addAll(ConvertGeom.gpsGeomToGeoPoint(gg));
                    }
                }
                i.putExtra("PROJECT_COORD", ConvertGeom.GeoPointToGpsGeom(coordProjet));
                startActivityForResult(i, 1);
                return true;
            }
        };
    }
    
    protected void onClose() {      
        datasource.close();
    }
    

	//TODO add description for javadoc
    /**
     * loading the different projects of the local db
     * @return
     */
    public List<Project> recupProject() {
         
         List<Project> values = this.datasource.getAllProjects();
         
         return values;
          
     }
    
	//TODO add description for javadoc
    /**
     * loading the different projects of the local db
     * @return
     */
    public List<GpsGeom> recupGpsGeom() {
         
         List<GpsGeom> values = this.datasource.getAllGpsGeom();
         
         return values;
          
     }
    
    /**
     * creating a list of project and loads in the view
     */
    public void refreshList(){      
    	
    	refreshedValues = recupProject();
    	allGpsGeom = recupGpsGeom();
    	
    	List<String> toList = new ArrayList<String>();
        
        /**
         * Put markers on the map
         */
        Integer i = Integer.valueOf(0);
        for (Project enCours:refreshedValues){
			GeoPoint coordProjet = null;
        	for(GpsGeom gg : allGpsGeom){
        		if(enCours.getGpsGeom_id()==gg.getGpsGeomsId()){
        			coordProjet =  MathOperation.barycenter(ConvertGeom.gpsGeomToGeoPoint(gg));
        		}
        	}
        	
        	Marker marker = displayedMap.addMarkersColored(i, "Cliquez ici pour charger le projet", coordProjet);
            marker.setOnMarkerClickListener(markerClick);
        	projectMarkers.put(marker.getTitle(), i);
        	toList.add(i+" - "+enCours.getProjectName());
        	i++;
        }
        map.invalidate();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, toList);
        listeProjects.setAdapter(adapter);
   }
    
    /**
     *  get the project selected in listview and show its position on the map 
     */
    
    public OnItemClickListener selectedProject = new OnItemClickListener()
    {
		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
			GeoPoint coordProjet = null;
        	List<GpsGeom> allGpsGeom = recupGpsGeom();
        	for(GpsGeom gg : allGpsGeom){
        		if(refreshedValues.get(position).getGpsGeom_id()==gg.getGpsGeomsId()){
        			coordProjet =  MathOperation.barycenter(ConvertGeom.gpsGeomToGeoPoint(gg));
        		}
        	}

			displayedMap = new GeoActivity(false, coordProjet, map);
    		Toast.makeText(getApplicationContext(), coordProjet.toString(), Toast.LENGTH_LONG).show();                  
		}
    };
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
        	if(MainActivity.photo.getUrlTemp() != null){
            //TODO vérifier que l'activité s'est bien terminée
        		setResult(RESULT_OK);
            	finish();
        	}
        }
    }
}