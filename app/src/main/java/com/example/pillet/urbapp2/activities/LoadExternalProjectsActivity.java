package com.example.pillet.urbapp2.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.Color;
import android.util.Log;
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
import com.example.pillet.urbapp2.syncToExt.Sync;
import com.example.pillet.urbapp2.utils.ConvertGeom;
import com.example.pillet.urbapp2.utils.MathOperation;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerClickListener;

/**
 * 
 * @author Sebastien
 *
 */
public class LoadExternalProjectsActivity extends Activity {
    private static final String TAG = "externalProjects";
	/**
	 * creating datasource
	 */
	private LocalDataSource datasource;
	/**
	 * Contains all the projects attributes
	 */
	public List<Project> refreshedValues;
	
	/**
	 * COntains all GPSInfo from all Project
	 */
	public List<GpsGeom> allGpsGeom;
	
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
    public GeoActivity displayedMap;
    private int firstClick =-1;
    private int secondClick = -1;
    private int numberClicked = 0;
    private int save=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_loadexternaldb);
        datasource=MainActivity.datasource;
        datasource.open();

        map = (MapView) findViewById(R.id.mapView);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        map.setClickable(true);

        IMapController mapController = map.getController();
        mapController.setZoom(6);
        
        displayedMap = new GeoActivity(true, GeoActivity.defaultPos, map);
        
        listeProjects = (ListView) findViewById(R.id.listView);
        refreshList();
        
        listeProjects.setOnItemClickListener(selectedProject);

    }

    /**
     * A click on a marker will highlight the respective project name on the list
     * two clicks will load the project
     */
    public OnMarkerClickListener markerClick  = new OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker, MapView mapView) {

            listeProjects.getChildAt(projectMarkers.get(marker.getTitle())).setBackgroundColor(Color.LTGRAY);
            if(save != -1 && save != projectMarkers.get(marker.getTitle())){
                listeProjects.getChildAt(save).setBackgroundColor(Color.argb(0, 238, 238, 238));
            }
            save = projectMarkers.get(marker.getTitle());
            if(numberClicked==2)
                numberClicked =0;
            if (numberClicked==0){
                Toast.makeText(MainActivity.baseContext, "Appuyer une seconde fois pour charger le projet", Toast.LENGTH_SHORT).show();
                firstClick=projectMarkers.get(marker.getTitle());
            }
            if(numberClicked==1)
                secondClick=projectMarkers.get(marker.getTitle());
            numberClicked++;
            if (secondClick == firstClick) {

                Toast.makeText(MainActivity.baseContext, "Chargement du projet", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplicationContext(), LoadExternalPhotosActivity.class);
                i.putExtra("SELECTED_PROJECT_ID", refreshedValues.get(projectMarkers.get(marker.getTitle())).getProjectId());

                ArrayList<GeoPoint> coordProjet = new ArrayList<GeoPoint>();

                for (GpsGeom gg : allGpsGeom) {
                    if (refreshedValues.get(projectMarkers.get(marker.getTitle())).getGpsGeom_id() == gg.getGpsGeomsId()) {
                        coordProjet.addAll(ConvertGeom.gpsGeomToGeoPoint(gg));
                    }
                }
                i.putExtra("PROJECT_COORD", ConvertGeom.GeoPointToGpsGeom(coordProjet));
                startActivityForResult(i, 1);
                firstClick=-1;
                secondClick=-1;
                numberClicked=0;
            }
            return true;
        }
    };

    protected void onClose() {      
        datasource.close();
    }
    
    /**
     * creating a list of project and loads in the view
     */
    public void refreshList(){      
    	
    	Sync Synchro = new Sync();
    	if (Synchro.getProjectsFromExt()){
    		try{
    			refreshedValues=Sync.refreshedValues;
    			allGpsGeom=Sync.allGpsGeom;
    		}
    		catch (Exception e){
    			Log.e("DFHUPLOAD", "Pb de data");
    		}
    	}
    	
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
        	
        	Marker marker = new Marker(map);//displayedMap.addMarkersColored(i, "Cliquez ici pour charger le projet", coordProjet);
            marker.setPosition(coordProjet);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setIcon(this.getResources().getDrawable(R.drawable.marker_icon));
            marker.setTitle("Cliquez ici pour charger le projet : " + i);
            marker.setOnMarkerClickListener(markerClick);
            map.getOverlays().add(marker);
            map.getController().setCenter(coordProjet);
            map.getController().setZoom(map.getMaxZoomLevel());
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
     *  two click on the same project will load the project
     *  you can click once  and then on the map marker and vice versa
     */
    
    public OnItemClickListener selectedProject = new OnItemClickListener()
    {
		@Override
		public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {

            arg0.getChildAt(position).setBackgroundColor(Color.LTGRAY);
            if(save != -1 && save != position){
                arg0.getChildAt(save).setBackgroundColor(Color.argb(0, 238, 238, 238));
            }
            save = position;

            GeoPoint coordProjet = null;
        	for(GpsGeom gg : allGpsGeom){
        		if(refreshedValues.get(position).getGpsGeom_id()==gg.getGpsGeomsId()){
        			coordProjet = MathOperation.barycenter(ConvertGeom.gpsGeomToGeoPoint(gg));
        		}
            }
            map.getController().setCenter(coordProjet);
            map.getController().setZoom(map.getMaxZoomLevel());
            map.invalidate();

            if(numberClicked==2)
                numberClicked =0;
            if (numberClicked==0){
                Toast.makeText(MainActivity.baseContext, "Appuyer une seconde fois pour charger le projet", Toast.LENGTH_SHORT).show();
                firstClick=position;
            }
            if(numberClicked==1)
                secondClick=position;
            numberClicked++;
            if (secondClick == firstClick) {

                Toast.makeText(MainActivity.baseContext, "Chargement du projet", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplicationContext(), LoadExternalPhotosActivity.class);
                i.putExtra("SELECTED_PROJECT_ID", refreshedValues.get(position).getProjectId());

                ArrayList<GeoPoint> coordProjet1 = new ArrayList<GeoPoint>();

                for (GpsGeom gg : allGpsGeom) {
                    if (refreshedValues.get(position).getGpsGeom_id() == gg.getGpsGeomsId()) {
                        coordProjet1.addAll(ConvertGeom.gpsGeomToGeoPoint(gg));
                    }
                }
                i.putExtra("PROJECT_COORD", ConvertGeom.GeoPointToGpsGeom(coordProjet1));
                startActivityForResult(i, 1);
                firstClick=-1;
                secondClick=-1;
                numberClicked=0;
            }
		}};
    
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