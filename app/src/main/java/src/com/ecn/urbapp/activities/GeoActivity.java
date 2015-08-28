package src.com.ecn.urbapp.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.pillet.urbapp2.R;
import src.com.ecn.urbapp.db.Element;
import src.com.ecn.urbapp.db.GpsGeom;
import src.com.ecn.urbapp.db.Project;
import src.com.ecn.urbapp.dialogs.NbPointsGeoDialog;
import src.com.ecn.urbapp.utils.ConvertGeom;
import src.com.ecn.urbapp.utils.GetId;
import src.com.ecn.urbapp.utils.MarkerPos;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Marker.OnMarkerDragListener;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IMapController;

/**
* Implements the Google Maps in a fragment.
* User can select up to 2 or 4 points, linked by line.
* Each point is georeferenced in GPS and a marker shows the real address.
* Can be called from an other activity (so no more markers)
* @author Sébastien
*
*/
public class GeoActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
													 GoogleApiClient.OnConnectionFailedListener,
													 View.OnClickListener,
													 MapEventsReceiver,LocationListener{
	private static final String TAG = "GeoActivity";

	/**
	 * The button for validating the selection
	 */
	private Button validate = null;
	
	/**
	 * The openStreetMap map object
	 */
	private MapView mapView =null;
	/**
	 * Contains the GPS position of the user
	 */
	private GoogleApiClient mGoogleApiClient = null;
	private LocationRequest mLocationRequest = null;
	/**
	 * number of markers to display (4 for a zone, 2 for a facade)
	 */
	public static int nbPoints;
	
	/**
	 * France GPS centered
	 */
	public static final GeoPoint defaultPos=new GeoPoint(46.52863469527167,2.00896484375);
	/**
	 * For the localisation of tablets
	 */
	private Boolean needCurrentPos;
	/**
	* Global constants
	* Define a request code to send to Google Play services
	* This code is returned in Activity.onActivityResult
	*/
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	/**
	* for addresses loading
	*/
	private ProgressBar mActivityIndicator;
	
	/**
	* Contains the list of markers defined by user
	*/
	public ArrayList<Marker> markers = new ArrayList<>();
	
	/**
	* polygone/line option to display the selected area
	*/
	public Polygon polygon;

	/**
	 * Defines all the colors for markers
	 */
	public static final int[] markersColor = {
			R.drawable.marker_red,
			R.drawable.marker_yellow,
			R.drawable.marker_green,
			R.drawable.marker_purple,
		};

	/**
	 * Defines the interface that control the map (essentially zoom buttons)
	 */
	private IMapController mapController;
	/**
	 * Overlay used to display location of the user on the map
	 */
	private ItemizedIconOverlay locationOverlay;

	List<OverlayItem> items = new ArrayList<>();
	/*----------------------------------------------------------------------------*/
    //Dialogs used in this activity
	/**
	* Define a DialogFragment that displays the error dialog
	* @author Sebastien
	*
	*/
	public static class ErrorDialogFragment extends DialogFragment {
	    // Global field to contain the error dialog
	    private Dialog mDialog;
	    // 
	    /**
	     * Default constructor. Sets the dialog field to null. For displaying all the issue with Google Play Service
	     */
	    public ErrorDialogFragment() {
	        super();
	        mDialog = null;
	    }
	    /**
	     * Set the dialog to display
	     * @param dialog
	     */
	    public void setDialog(Dialog dialog) {
	        mDialog = dialog;
	    }
	    // Return a Dialog to the DialogFragment.
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        return mDialog;
	    }
	}
    /**
	* Show a dialog returned by Google Play services for the
	* connection error code
	*
	* @param errorCode An error code returned from onConnectionFailed
	*/
    private void showErrorDialog(int errorCode) {
        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
				errorCode,
				this,
				CONNECTION_FAILURE_RESOLUTION_REQUEST);
        // If Google Play services can provide an error dialog
        if (errorDialog != null) {
            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();
            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);
            // Show the error dialog in the DialogFragment
            errorFragment.show(getFragmentManager(), "Localisation");
        }
    }
    /**
	* To check if Google Play Services is installed and up to date ! Propose to update if needed
	* @return boolean if is really connected
	*/
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(MainActivity.baseContext);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates","Google Play services is available.");
            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
                // Get the error code
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);
            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getFragmentManager(),"Location Updates");
            }
        }
                return false;
    }
	/**
	 * Displays a message if user has disabled GPS. Invite to put it on (shortcut to settings)
	 */
	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Votre GPS semble désactivé, souhaitez-vous l'activer ?")
				.setCancelable(false)
				.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
					@Override
					public void onClick( final DialogInterface dialog, final int id) {
						startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
					}
				})
				.setNegativeButton("Non", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int id) {
						dialog.cancel();
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}
	/*-------------------------------------------------------------------------------------*/
    /**
     * For intern implementation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.layout_geo);

    	if (servicesConnected()){
    		
    		needCurrentPos=true;
    		validate = (Button)findViewById(R.id.validate);

    		validate.setOnClickListener(this);

    		//for reverse adresses
    		mActivityIndicator = (ProgressBar) findViewById(R.id.address_progress);

    		// Get a handle to the Map Fragment
			DefaultResourceProxyImpl resourceProxy = new ResourceProxyImpl(getApplicationContext());

			mapView = (MapView) findViewById(R.id.mapView);
			mapView.setTileSource(TileSourceFactory.MAPNIK);
			mapView.setBuiltInZoomControls(true);
			mapView.setMultiTouchControls(true);
			mapView.setClickable(true);

			mapController = mapView.getController();
			mapController.setZoom(10);

			MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this,this);
			locationOverlay = new ItemizedIconOverlay<>(items, new Glistener(), resourceProxy);

			mapView.getOverlays().add(0, mapEventsOverlay);
			mapView.getOverlays().add(locationOverlay);
			mapView.invalidate();

			mGoogleApiClient = new GoogleApiClient.Builder(this)
					.addApi(LocationServices.API)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.build();

			LocationRequest locationRequest = new LocationRequest();
			locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

			geoActivityInit(true, defaultPos, mapView);

    		int i=0;
    		for (GpsGeom gps:MainActivity.gpsGeom){
    			for(GeoPoint point:ConvertGeom.gpsGeomToGeoPoint(gps)) {
    				if(i<nbPoints){
    					Marker newMarker = new Marker(mapView);
						newMarker.setPosition(point);
						newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
						newMarker.setIcon(this.getResources().getDrawable(R.drawable.marker_icon));
						newMarker.setDraggable(true);
						getAddress(new MarkerPos(newMarker, point));

    					markers.add(newMarker);
    				}
    				i++;
    			}
    		}
    	}
	}
	/**
	 * Handle results returned to the FragmentActivity
	 * by Google Play services
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Decide what to do based on the original request code
		switch (requestCode) {
			case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
			* If the result code is Activity.RESULT_OK, try
			* to connect again
			*/
				switch (resultCode) {
					case Activity.RESULT_OK :
				/*
				* Try the request again
				*/
						break;
				}
		}
	}
	/*-------------------------------------------------------------------------------*/
	// Constructors and initiator of geoActivity
    /**
     * Constructor of GeoActivity (needed in case of extern implementation)
     * @param needCurrentPos
     * @param pos
     * @param map
     */
    public GeoActivity(Boolean needCurrentPos, GeoPoint pos, MapView map){
    	if (servicesConnected()){
    		this.mapView = map;
    		this.needCurrentPos = needCurrentPos;
    		geoActivityInit(needCurrentPos, pos, map);
    	}
    }
    
    /**
     * Default constructor
     */
    public GeoActivity(){
    	
    }
    
    /**
     * Initiate the geoActivity
     * @param needCurrentPos
     * @param pos
     * @param map
     */
    public void geoActivityInit(Boolean needCurrentPos, GeoPoint pos, MapView map){
    	if (needCurrentPos) {
    		/*
    		 * Create a new location client, using the enclosing class to
    		 * handle callbacks.
    		 */
    		mLocationRequest = new LocationRequest();
    		// Connect the client.
    		//TODO check the threat order
    		//CONNECTION TODO
    	}
		map.getController().setCenter(pos);
		map.getController().setZoom(7);
    	//check
    }
	/*-----------------------------------------------------------------------------------*/
    /**
	* Listener for validation the selection
	*/
    public void onClick (View v) {
    	
    	if ( (NbPointsGeoDialog.selected == 1 && markers.size() < 3) || (NbPointsGeoDialog.selected == 2 && markers.size() < 2) ) {
    		Toast.makeText(getBaseContext(), "Vous n'avez pas renseigné assez de points", Toast.LENGTH_SHORT).show();
    	}
    	
    	else if (NbPointsGeoDialog.selected == 1) {
    		
    		try {
	    		ArrayList<GeoPoint> ll = new ArrayList<GeoPoint>();
	    		for(Marker m : markers){
	    			ll.add(m.getPosition());
	    		}
	    		GpsGeom gg = new GpsGeom();
	    		gg.setGpsGeomCoord(ConvertGeom.GeoPointToGpsGeom(ll));
	    		gg.setGpsGeomId(GetId.GpsGeom());
	    		/**
	    		 * we need to save the address in the photo_adresse attribute
	    		 */
	    		MainActivity.photo.setPhoto_adresse(markers.get(markers.size()-1).getSnippet());
	    		if(MainActivity.gpsGeom.size()>=gg.getGpsGeomsId()){
	    			MainActivity.gpsGeom.add((int)gg.getGpsGeomsId(), gg);
	    		}
	    		else{
	    			MainActivity.gpsGeom.add(gg);
	    		}
	    		MainActivity.photo.setGpsGeom_id(gg.getGpsGeomsId());
	    		for(Project p : MainActivity.project){
	    			p.setGpsGeom_id(gg.getGpsGeomsId());
	    		}
	            for(Element el: MainActivity.element){
	            	el.setGpsGeom_id(gg.getGpsGeomsId());
	            }
	    	}
	    	catch (ArrayIndexOutOfBoundsException e) {
	    		Log.e(getLocalClassName(), "Pas de points !");
	    	}
	    	finish();
    	}
    	
    	else if (NbPointsGeoDialog.selected == 2){
	    		
	    	try {
	    		ArrayList<GeoPoint> ll = new ArrayList<GeoPoint>();
	    		for(Marker m : markers){
	    			ll.add(m.getPosition());
	    		}
	    		GpsGeom gg = new GpsGeom();
	    		gg.setGpsGeomCoord(ConvertGeom.GeoPointToGpsGeom(ll));
	    		gg.setGpsGeomId(GetId.GpsGeom());
	    		/**
	    		 * we need to save the address in the photo_adresse attribute
	    		 */
	    		MainActivity.photo.setPhoto_adresse(markers.get(markers.size()-1).getSnippet());
	    		if(MainActivity.gpsGeom.size()>=gg.getGpsGeomsId()){
	    			MainActivity.gpsGeom.add((int)gg.getGpsGeomsId(), gg);
	    		}
	    		else{
	    			MainActivity.gpsGeom.add(gg);
	    		}
	    		MainActivity.photo.setGpsGeom_id(gg.getGpsGeomsId());
	    		for(Project p : MainActivity.project){
	    			p.setGpsGeom_id(gg.getGpsGeomsId());
	    		}
	            for(Element el: MainActivity.element){
	            	el.setGpsGeom_id(gg.getGpsGeomsId());
	            }
	    	}
	    	catch (ArrayIndexOutOfBoundsException e) {
	    		Log.e(getLocalClassName(), "Pas de points !");
	    	}
	    	finish();
    	}
    }
	/*-------------------------------------------------------------------------*/
    // Map event listener implementation
    /**
	* Listener for adding points, gps referenced. Make a polyline with it.
	*/
	@Override
	public boolean singleTapConfirmedHelper(GeoPoint p) {
		/**
		 * We prevents to pu more than the max nb of markers
		 */
		GeoPoint loc  = (GeoPoint) locationOverlay.getItem(0).getPoint();
		if(NbPointsGeoDialog.selected == 1) {
			// seting up the added marker
			Marker marker = new Marker(mapView);
			marker.setPosition(p);
			marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
			marker.setIcon(this.getResources().getDrawable(R.drawable.marker_icon));
			marker.setDraggable(true);
			marker.setPanToView(true);
			marker.setOnMarkerDragListener(markerDrag);
			mapView.getOverlays().add(marker);
			getAddress(new MarkerPos(marker, p));
			markers.add(marker);
			/**
			* Drawing on map purpose
			*/
		   markerToArray(markers);
		}
		if(NbPointsGeoDialog.selected == 2 && markers.size()<nbPoints) {
			// setting up the added marker
			Marker marker = new Marker(mapView);
			marker.setPosition(p);
			marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
			marker.setIcon(this.getResources().getDrawable(R.drawable.marker_icon));
			marker.setDraggable(true);
			marker.setPanToView(true);
			marker.setOnMarkerDragListener(markerDrag);
			getAddress(new MarkerPos(marker, p));
			mapView.getOverlays().add(marker);
			markers.add(marker);
			/**
			 * Drawing on map purpose
			 */
			markerToArray(markers);
		}
		else if(NbPointsGeoDialog.selected == 2 && markers.size() == 2) {
			Toast.makeText(getApplicationContext(), "Nombre de points maximum atteint", Toast.LENGTH_SHORT).show();
		}
		return true;
	}

	/**
	 * Long press event is not consumed and can't be handled by other objects
	 * @param p
	 * @return event consumption
	 */
	@Override
	public boolean longPressHelper(GeoPoint p)
	{
		return false;
	}
	/*----------------------------------------------------------------------*/
	//connection to location services methods
	/**
	 * Called when the activity is started.
	 */
	@Override
	protected void onStart() {
		super.onStart();
		mGoogleApiClient.connect();
	}

	/**
	 * Called when the Activity is no longer visible.
	 */
	@Override
	protected void onStop() {
		if (needCurrentPos) {
			// Disconnecting the client invalidates it.
			mGoogleApiClient.disconnect();
		}
		super.onStop();
	}

	/**
	 * Called when location has changed to do an update of the displayed map
	 * @param location
	 */
	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			int lat = (int) (location.getLatitude() * 1E6);
			int lng = (int) (location.getLongitude() * 1E6);
			GeoPoint gpt = new GeoPoint(lat, lng);
			items.clear();
			mapController.setCenter(gpt);
			mapController.setZoom(mapView.getMaxZoomLevel());
			locationOverlay.addItem( new OverlayItem(getString(R.string.location), (getString(R.string.location)), gpt));
			mapView.invalidate();
		}
	}

    /**
	* Called by Location Services when the request to connect the
	* client finishes successfully. At this point, you can
	* request the current location or start periodic updates
	*/
    @Override
    public void onConnected(Bundle dataBundle) {
		mLocationRequest = LocationRequest.create();
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		//mLocationRequest.setInterval(10000); //actualize position every 10 second
		LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

	@Override
	public void onConnectionSuspended(int i) {
		Toast.makeText(MainActivity.baseContext, "Connection perdue", Toast.LENGTH_LONG).show();
	}

	/**
	 * Called by Location Services if the connection to the
	 * location client drops because of an error.
	 */
	public void onDisconnected() {
		// Display the connection status
		Toast.makeText(this, "Disconnected. Please re-connect.",Toast.LENGTH_SHORT).show();
	}

	/**
	 * Called by Location Services if the attempt to
	 * Location Services fails.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
		* Google Play services can resolve some errors it detects.
		* If the error has a resolution, try sending an Intent to
		* start a Google Play services activity that can resolve
		* error.
		*/
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(
						this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
				* Thrown if Google Play services canceled the original
				* PendingIntent
				*/
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
            /*
			* If no resolution is available, display a dialog to the
			* user with the error.
			*/
			showErrorDialog(connectionResult.getErrorCode());
		}
	}
	/*-----------------------------------------------------------------------------*/
    //for adresses
    /**
	* A subclass of AsyncTask that calls getFromLocation() in the
	* background. The class definition has these generic types:
	* MarkerPos - Contain the current position, the marker to
	* change and space for the address (String)
	* Void - indicates that progress units are not used
	* MarkerPos - THe whole thing passed to onPostExecute()
	*/
     private class GetAddressTask extends AsyncTask<MarkerPos, Void, MarkerPos> {
         Context mContext;
         public GetAddressTask(Context context) {
             super();
             mContext = context;
         }
         /**
		* Get a Geocoder instance, get the latitude and longitude
		* look up the address, and return it
		*
		* @param params One or more GeoPoint objects
		* @return A string containing the address of the current
		* location, or an empty string if no address can be found,
		* or an error message
		*/
         @Override
         protected MarkerPos doInBackground(MarkerPos... params) {
             GeocoderNominatim geocoder = new GeocoderNominatim(mContext, Locale.getDefault());
             // Get the current location from the input parameter list
             MarkerPos markpos = new MarkerPos(params[0]);
             GeoPoint loc = markpos.getPosition();
             // Create a list to contain the result address
             List<Address> addresses = null;
             try {
                /*
				* Return 1 address.
				*/
                 addresses = geocoder.getFromLocation(loc.getLatitude(),
                         loc.getLongitude(), 1);
             } catch (IOException e1) {
             Log.e("LocationSampleActivity","IO Exception in getFromLocation()");
             e1.printStackTrace();
             markpos.setAdresse("Impossible d'avoir l'adresse. Vérifier connexion réseau");
             return markpos;
             } catch (IllegalArgumentException e2) {
             // Error message to post in the log
             String errorString = "Illegal arguments " +
                     Double.toString(loc.getLatitude()) +
                     " , " +
                     Double.toString(loc.getLongitude())+
                     " passed to address service";
             Log.e("LocationSampleActivity", errorString);
             e2.printStackTrace();
             markpos.setAdresse(errorString);
             return markpos;
             }
             // If the reverse geocode returned an address
             if (addresses != null && addresses.size() > 0) {
                 // Get the first address
                 Address address = addresses.get(0);
                 /*
				* Format the first line of address (if available),
				* city, and country name.
				*/
                 String addressText = String.format(
                         "%s, %s, %s",
                         // If there's a street address, add it
                         address.getMaxAddressLineIndex() > 0 ?
                                 address.getAddressLine(0) : "",
                         // Locality is usually a city
                         address.getLocality(),
                         // The country of the address
                         address.getCountryName());
                 // Return the text
                 markpos.setAdresse(addressText);
                 return markpos;
             } else {
                     markpos.setAdresse("No address found");
                     return markpos;
             }
         }
         
 		 /**
         * A method that's called once doInBackground() completes. Turn
         * off the indeterminate activity indicator and set
         * the text of the UI element that shows the address. If the
         * lookup failed, display the error message.
         */
         @SuppressWarnings("null")
		@Override
         protected void onPostExecute(MarkerPos markpos) {
	         // Set activity indicator visibility to "gone"
	         mActivityIndicator.setVisibility(View.GONE);
	         // Register the results of the lookup.
	         markpos.getMarker().setSnippet(markpos.getAdresse());
	         markpos.getMarker().showInfoWindow();
	         markerToArray(markers);
         }        
     }
     
    /**
	* get hte address from the location of a marker
	* @param markpos The view object associated with this method,
	* in this case a Button.
	*/
     public void getAddress(MarkerPos markpos) {
         // Ensure that a Geocoder services is available
         if (Build.VERSION.SDK_INT >=
                 Build.VERSION_CODES.GINGERBREAD
                             &&
                 Geocoder.isPresent()) {
             // Show the activity indicator
             mActivityIndicator.setVisibility(View.VISIBLE);
             /*
			* Reverse geocoding is long-running and synchronous.
			* Run it on a background thread.
			* Pass the current location to the background task.
			* When the task finishes,
			* onPostExecute() displays the address.
			*/
             (new GetAddressTask(this)).execute(markpos);
         }
     }
	/*--------------------------------------------------------------------------------*/
	/**
	* Dragging markers listeners.
	* Force the rerending of address and of the polygone
	*/
	public OnMarkerDragListener markerDrag = new OnMarkerDragListener() {
        @Override
        public void onMarkerDragStart(Marker marker) {
            marker.closeInfoWindow();
        }
        @Override
        public void onMarkerDragEnd(Marker marker) {
            MarkerPos markpos = new MarkerPos(marker, marker.getPosition());
            getAddress(markpos);                        
        }
        @SuppressWarnings("null")
		@Override
        public void onMarkerDrag(Marker marker) {
        	markerToArray(markers);
        }
	};
	/*----------------------------------------------------------------------*/
    //add a marker on the displayed map
    /**
     * Method to add a personalized marker, with a color chose automatically and a number in it.
     * @param number to display in the marker 
     * @param title the tittle text to display in marker
     * @param position of the marker in the map
     * @return the marker itself
     */

    public Marker addMarkersColored(int number, String title, GeoPoint position){
    	Bitmap.Config conf = Bitmap.Config.ARGB_8888;
    	Bitmap bmp = Bitmap.createBitmap(34, 41, conf);
    	bmp.setDensity(Bitmap.DENSITY_NONE);
    	Canvas canvas1 = new Canvas(bmp);
    	// paint defines the text color,
    	// stroke width, size
    	Paint color = new Paint();
    	String text = String.valueOf(number);
    	int fontSize = 28-6*text.length();
    	fontSize = (fontSize <0) ? 2 : fontSize;
    	color.setTextSize(fontSize);
    	color.setColor(Color.BLACK);
    	//modify canvas
    	canvas1.drawBitmap(BitmapFactory.decodeResource(MainActivity.baseContext.getResources(),markersColor[number%markersColor.length]), 0,0, color);
    	int posX = 14 -3*text.length();
    	posX = (posX <0) ? 2 : posX;
    	int posY = 29-3*text.length();
    	posY = (posY <0) ? 2 : posY;
    	canvas1.drawText(text, posX, posY, color);
    	Marker marker = new Marker(mapView);
    	marker.setIcon(new BitmapDrawable(bmp));// .icon(BitmapDescriptorFactory.fromBitmap(bmp))
		marker.setPosition(position);
		marker.setTitle(title);
    	return marker;
    }
    /**
     * Draw polygon on the map
     * @param points
     * @param refresh for the refresh content or not (yes in that activity)
     */
    public void drawPolygon(ArrayList<GeoPoint> points, Boolean refresh){
    	if (points.size()>=2) {
    		if (polygon!=null && refresh)
				mapView.getOverlays().remove(polygon);
			//Instantiates a new Polygon object and adds points to define a rectangle
			polygon = new Polygon(this);
			polygon.setPoints(points);
			if(polygon!=null && refresh)
				mapView.getOverlays().remove(polygon);
			// Add polygon as an overlay drawn before the marker
			mapView.getOverlays().add(1,polygon);
       }
    }
    /** build a polygon from arraylist of markers
     * @param markers
     */
    public void markerToArray(ArrayList<Marker> markers){
    	ArrayList<GeoPoint> points = new ArrayList<GeoPoint>();
        for (Marker mark:markers){
        	if(mark!=null){
        		points.add(mark.getPosition());
        	}
        }
        drawPolygon(points, true);
    }
	/*----------------------------------------------------------------------*/
	/**
	 * class that will handle some gesture event to itemizedOverlay like markers
	 */
	class Glistener implements ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
		/**
		 * long press event is not consumed
		 * @param index
		 * @param item
		 * @return event consumption
		 */
		@Override
		public boolean onItemLongPress(int index, OverlayItem item) {
			return false;
		}

		/**
		 * single tap event is allowed
 		 * @param index
		 * @param item
		 * @return event consumption
		 */
		@Override
		public boolean onItemSingleTapUp(int index, OverlayItem item) {
			return true;
		}
	}
}