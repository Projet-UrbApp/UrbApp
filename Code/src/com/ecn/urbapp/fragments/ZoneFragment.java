package com.ecn.urbapp.fragments;

import java.util.ArrayList;
import java.util.Vector;

import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.ecn.urbapp.R;
import com.ecn.urbapp.activities.MainActivity;
import com.ecn.urbapp.db.Element;
import com.ecn.urbapp.db.PixelGeom;
import com.ecn.urbapp.dialogs.AddZoneDialogFragment;
import com.ecn.urbapp.dialogs.TopologyExceptionDialogFragment;
import com.ecn.urbapp.utils.ConvertGeom;
import com.ecn.urbapp.zones.BitmapLoader;
import com.ecn.urbapp.zones.DrawZoneView;
import com.ecn.urbapp.zones.UtilCharacteristicsZone;
import com.ecn.urbapp.zones.Zone;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.io.ParseException;

/**
 * @author	COHENDET Sébastien
 * 			DAVID Nicolas
 * 			GUILBART Gabriel
 * 			PALOMINOS Sylvain
 * 			PARTY Jules
 * 			RAMBEAU Merwan
 * 
 * ZoneFragment class
 * 
 * This is the fragment used to define the different zones.
 */

public class ZoneFragment extends Fragment implements OnClickListener, OnTouchListener{
	
	/**
	 * Constant field defining the radius tolerance on touch reference, in pixels
	 */
	private final int REFERENCE_TOUCH_RADIUS_TOLERANCE = 30;
	
	/**
	 * Constant field defining the reference height to correct the size of point for the zone creation, in pixels
	 */
	private final int REFERENCE_HEIGHT = 600;
	
	/**
	 * Constant field defining the reference width to correct the size of point for the zone creation, in pixels
	 */
	private final int REFERENCE_WIDTH = 1200;
	
	/**
	 * Constant field defining the reference time length to force selection, in pixels
	 */
	private final int REFERENCE_TIME = 150;
	
	/**
	 * Field defining the actual state of definition of zones
	 * Possible states : selection, creation and edition
	 */
	public static int state;

	/**
	 * Button cancel, to cancel creation or edition
	 */
	private Button cancel;
	
	/**
	 * Button back, to cancel last point creation or edition
	 */
	private Button back;
	
	/**
	 * Button validate, to save last changes made in a zone
	 */
	private Button validate;
	
	/**
	 * Button delete, to delete what is selected, zone or point
	 */
	private Button delete;

	/**
	 * Image displayed
	 */
	private static ImageView myImage;
	
	/**
	 * Matrix to transform touch events coordinates in image base
	 */
	private Matrix matrix;
	
	/**
	 * Temporary zone for edition
	 */
	private Zone zoneCache ; 
	

	/**
	 * Temporary pixelGeom for edition
	 */
	public static PixelGeom geomCache;
	
	/**
	 * Zone the user is currently modifying, edition or creation
	 * It's a reference used by DrawZoneView in displaying, use .set instead of =
	 */
	private Zone zone;
	
	/**
	 * Point selected
	 * It's a reference used by DrawZoneView in displaying, use .set instead of =
	 */
	private Point selected;
	
	/**
	 * Tolerance range on selection. Based in radius tolerance reference, adjusted to image size. In pixels 
	 */
	private float touchRadiusTolerance;
	
	/**
	 * view containing the draw elements
	 */
	private DrawZoneView drawzoneview;
	
	/**
	 * image height of the picture
	 */
	private int imageHeight;
	
	/**
	 * image with of the picture
	 */
	private int imageWidth;
	
	/**
	 * Constant value : state
	 */
	public static final int IMAGE_CREATION = 2;
	/**
	 * Constant value : state
	 */
	public static final int IMAGE_EDITION = 3;
	/**
	 * Constant value : state
	 */
	public static final int IMAGE_SELECTION= 1;
	
	/**
	 * Point selection indicator, works in both creation and edition modes
	 */
	private boolean POINT_SELECTED = false;
	
	/**
	 * Times Android consecutively detects a move action
	 */
	private int moving;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	/**
	 * All buttons clicks handling method. Different behavior depending on state (selection, creation, edition) 
	 */
	@Override
	public void onClick(View v) {
		if (state == IMAGE_CREATION) {
			int id = v.getId();
			if (id == R.id.zone_button_back) {
				if(!zone.back()){
					Toast.makeText(getActivity(), R.string.no_back, Toast.LENGTH_SHORT).show();
				}
				refreshDisplay();
			} else if (id == R.id.zone_button_cancel) {
				state = IMAGE_SELECTION;
				exitAction();
			} else if (id == R.id.zone_button_delete) {
				if(POINT_SELECTED){
					if (!zone.deletePoint(selected)){//A "middle" point for insertions can't be deleted 
						Toast.makeText(getActivity(), R.string.point_deleting_impossible, Toast.LENGTH_SHORT).show();
					}
					selected.set(0,0);//No selected point now.
					refreshDisplay();
					delete.setEnabled(false);
					POINT_SELECTED = false;
				}
			} else if (id == R.id.zone_button_validate) {
				validateCreation();
			}
		} else if (state == IMAGE_EDITION) {
			int id = v.getId();
			if (id == R.id.zone_button_delete) {
				//Point selected case
				if(POINT_SELECTED){
					if (!zone.deletePoint(selected)){//A "middle" point for insertions can't be deleted 
						Toast.makeText(getActivity(), R.string.point_deleting_impossible, Toast.LENGTH_SHORT).show();
					}
					selected.set(0,0);
					refreshDisplay();
					delete.setEnabled(false);
					POINT_SELECTED = false;
				}
				//Zone selected case
				else{
					int pos;
					for(pos=0; pos<MainActivity.pixelGeom.size(); pos++){
						if(MainActivity.pixelGeom.get(pos).getPixelGeomId()==geomCache.getPixelGeomId()){
							for(int i=0; i<MainActivity.element.size(); i++){
								if(MainActivity.element.get(i).getPixelGeom_id()==MainActivity.pixelGeom.get(pos).getPixelGeomId()){
									MainActivity.element.remove(i);
									break;
								}
							}
							MainActivity.pixelGeom.remove(pos);
							break;
						}
					}
					//Deleting zone leads to zone selection page
					state = IMAGE_SELECTION;
					exitAction();
				}
			} else if (id == R.id.zone_button_back) {
				if(!zone.back()){
					Toast.makeText(getActivity(), R.string.no_back, Toast.LENGTH_SHORT).show();
				}
				refreshDisplay();
			} else if (id == R.id.zone_button_cancel) {
				exitAction();
				state = IMAGE_SELECTION;
			} else if (id == R.id.zone_button_validate) {
				if(!zone.getPoints().isEmpty()){
					MainActivity.pixelGeom.remove(geomCache);
					AddZoneDialogFragment azdf = new AddZoneDialogFragment();
					azdf.show(getFragmentManager(), "AddZoneDialogFragment");
				} else {
					state = IMAGE_SELECTION;
					exitAction();
				}
			}
		}
	}

	/**
	 * Fragment initialization creating view step. Create all the buttons, set listeners and load picture.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.layout_zone, null);

		/*** Handling buttons ***/
		back = (Button) v.findViewById(R.id.zone_button_back);
		cancel = (Button) v.findViewById(R.id.zone_button_cancel);
		validate = (Button) v.findViewById(R.id.zone_button_validate);
		delete = (Button) v.findViewById(R.id.zone_button_delete);

		back.setOnClickListener(this);
		cancel.setOnClickListener(this);
		validate.setOnClickListener(this);
		delete.setOnClickListener(this);
		
		validate.setEnabled(false);
		back.setEnabled(false);
		cancel.setEnabled(false);
		delete.setEnabled(false);

		/*** Creating caches references ***/
		//Set these references, no =
		zone = new Zone(); selected = new Point(0,0);
		
		/*** Image loading ***/
		
		myImage = (ImageView) v.findViewById(R.id.image_zone);//Layouts' ImageView
		
		drawzoneview = new DrawZoneView(zone, selected) ;

		DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();//Get screen size

		//Creating drawable layers. Use picture device's URL. The picture is resized to fill screen availabilities
		Drawable[] drawables = {
			new BitmapDrawable(
				getResources(),
				BitmapLoader.decodeSampledBitmapFromFile(
						Environment.getExternalStorageDirectory()+"/featureapp/"+MainActivity.photo.getPhoto_url(),metrics.widthPixels, metrics.heightPixels - 174)),drawzoneview
		};
		myImage.setImageDrawable(new LayerDrawable(drawables));
		
		//Add listener on the image
		myImage.setOnTouchListener(this);
		
		return v;
	}
	
	/**
	 * Fragment start operations. Adjust pixels references to image real size. 
	 */
	@Override
	public void onStart(){
		super.onStart();
		
		state=IMAGE_SELECTION;
		
		imageHeight = myImage.getDrawable().getIntrinsicHeight(); 
		imageWidth = myImage.getDrawable().getIntrinsicWidth();
		
		//Calculate height and width ratios between referenced image and current image sizes and choose the smallest.
		//The chosen ratio is use to adjust points and lines displaying and touch radius tolerance.
		//These data are defined for a reference image size, in pixels.
		//In the adjustments reference pixel sizes are divided by this ratio.
		float ratioW =((float)REFERENCE_WIDTH/imageWidth);
		float ratioH =((float)REFERENCE_HEIGHT/imageHeight);
		float ratio = ratioW < ratioH ? ratioW : ratioH ;
			
		drawzoneview.setRatio(ratio);
		touchRadiusTolerance = REFERENCE_TOUCH_RADIUS_TOLERANCE/ratio;
	}
	
	/**
	 * Common action to do on exit (cancel or validation) to selection state.
	 * Refresh displaying and reset cache values.
	 */
	private void exitAction(){
		drawzoneview.onZonePage();
		
		//Disable all buttons
		validate.setEnabled(false);
		back.setEnabled(false);
		cancel.setEnabled(false);
		delete.setEnabled(false);
		
		//Reset all caches
		zone.setZone(new Zone());
		selected.set(0,0);
		drawzoneview.setIntersections(new Vector<Point>());
		
		//Call DrawZoneView onDraw method
		myImage.invalidate();
	}
	
	/**
	 * Validation of the create of the drawn zone
	 */
	private void validateCreation(){
		AddZoneDialogFragment azdf = new AddZoneDialogFragment();
		azdf.show(getFragmentManager(), "AddZoneDialogFragment");
	}
	
	/**
	 * The function return the point touch by the user
	 * If this point is outside the picture points coordinates are projected on the picture.
	 * @param event
	 * @return The touched point
	 */
	public Point getTouchedPoint(MotionEvent event){
		float[] coord = {event.getX(),event.getY()};//get touched point coord

		getMatrix();
		matrix.mapPoints(coord);//apply matrix transformation on points coord
		int pointX = (int)coord[0]; int pointY = (int)coord[1];
		
		//Outside picture point checking and correction
		if(pointX<0){
			pointX=0;
		}else{
			if(pointX>imageWidth){
				pointX=imageWidth;
			}
		}
		if(pointY<0){
			pointY=0;
		}else{
			if(pointY>imageHeight){
				pointY=imageHeight;
			}
		}
		
		return(new Point(pointX,pointY));
	}
	
	/**
	 * Set the matrix for the image
	 */
	public void getMatrix(){
		matrix = new Matrix();
		myImage.getImageMatrix().invert(matrix);
	}
	
	/**
	 * refresh of the display : buttons availabilities and refreshing on image draws
	 */
	public void refreshDisplay(){
		/*** All the points.size() comparisons are written with a +1 to represent the looping point ***/
		Vector<Point> points = zone.getPoints();
		//One point or more
		if(! points.isEmpty()){
			back.setEnabled(false);
			validate.setEnabled(false);
			
		//Two points or more
			if(points.size()>1+1){
				back.setEnabled(true);
				
		//Three points or more
		//It's possible to validate if the polygon isn't self-intersecting					
		//Cannot be intersections with less than 4 points but needed for refreshing displaying
				if(points.size()>2+1){
					validate.setEnabled(true);
					
					/*** Intersections checking ***/
					//Intersections are sequences of 4 points in a list

					zone.actualizePolygon();
					MultiPolygon polys = zone.getPolygon();
					for (int i=0; i<polys.getNumGeometries(); i++) {
						Vector<Point> toCheck = new Vector<Point>();
						Polygon poly = (Polygon) polys.getGeometryN(i);
						for (Coordinate c : poly.getExteriorRing().getCoordinates()) {
							toCheck.add(new Point((int) c.x, (int) c.y));
						}
						Vector<Point> intersections = new Vector<Point>(Zone.isSelfIntersecting(toCheck));

						if(!intersections.isEmpty()){
							validate.setEnabled(false);
							
						//Multiple lists (zones with holes) checking	
						} else {
							for (int j = 0; j<poly.getNumInteriorRing(); j++) {
								toCheck = new Vector<Point>();
								for (Coordinate c : poly.getInteriorRingN(j).getCoordinates()) {
									toCheck.add(new Point((int) c.x, (int) c.y));
								}
								intersections = new Vector<Point>(Zone.isSelfIntersecting(toCheck));
								if (!intersections.isEmpty()) {
									validate.setEnabled(false);
									break;
								}
							}
						}
						
					//Send intersections' list for displaying
					drawzoneview.setIntersections(intersections);
					}
					
				}
			}
		}
		//Whatever points number
		myImage.invalidate();
	}

	/**
	 * All touches handling method. Override Android API method.
	 * Different behavior depending on state (selection, creation, edition).
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		/*** Zone selection case ***/
		if(state==IMAGE_SELECTION){
			if (event.getAction() == MotionEvent.ACTION_UP) { 
				if(!hasZoneSelected(event)){//Try to select a zone. Works if the user touches, and a long time, a zone.
					//If no selected zone, it's a zone creation
		    		getMatrix();
					zone.addPoint2(getTouchedPoint(event));//Add the first point
					
					//Switching to creation mode
					state = IMAGE_CREATION; drawzoneview.onCreateMode();
					validate.setEnabled(false);
					back.setEnabled(false);
					cancel.setEnabled(true);
				}
			}
		}
		
		/*** Zone creation or edition cases ***/
		else{
			//User has just touched down the picture. 
			//Trying to select a existing (and potential) point. Skipped if a point is already selected.
			if(event.getAction() == MotionEvent.ACTION_DOWN && !POINT_SELECTED){
				//Reset all actions values
				moving = 0;//ACTION_MOVE occurrences
				selected.set(0, 0);//no selected point
				
				//Get point corrected coordinates
				Point touch = getTouchedPoint(event);
				
				//Check if the touched point is on a zone
				for(Point p : zone.getPoints()){
					//All coordinates are positive
					float dx=Math.abs(p.x-touch.x);
					float dy=Math.abs(p.y-touch.y);
					if((dx*dx+dy*dy)<touchRadiusTolerance*touchRadiusTolerance){
						selected.set(p.x,p.y);
					}
				}
				
				//Check if the touched point is a middle point
				if(selected.x == 0 && selected.y == 0){
					for(Point p : zone.getMiddles()){
						//All coordinates are positive
						float dx=Math.abs(p.x-touch.x);
						float dy=Math.abs(p.y-touch.y);
						if((dx*dx+dy*dy)<touchRadiusTolerance*touchRadiusTolerance){
							selected.set(p.x,p.y);
						}
					}
				}
			}
			//User has just released his finger from the picture
			if (event.getAction() == MotionEvent.ACTION_UP) {
				
				//If a point was selected, and for probable deletion, user touched elsewhere instead of deleting. 
				//Release the selected point. 
				if(POINT_SELECTED){
					selected.set(0, 0);
					POINT_SELECTED = false; delete.setEnabled(false);
				}
				//A point may have been selected, but in a moving or selection purpose
				else{
					//The user didn't selected a point
					if(selected.x==0 && selected.y==0){
						//In zone creation mode, user is currently adding a point
						if(state == IMAGE_CREATION){
							zone.addPoint2(getTouchedPoint(event));	
						//In zone edition mode, user is trying to select an other zone. 	
						}else{
							//If the user is touching a long time, and not by moving, a zone, switch zone selected.  
							if(moving < 2){
								hasZoneSelected(event);
							}
							//Else, do nothing, it's a mistake.
						}
					}
					
					//The user selected a point.
					else{
						//The user was creating a zone and touched a point shortly.
						//The user is probably trying to validate by looping the polygon
						if(state == IMAGE_CREATION && event.getEventTime()-event.getDownTime()<REFERENCE_TIME){
							//If it's currently possible to validate the creation ?
							if(zone.getPoints().size()>2+1 && validate.isEnabled()){
								float dx=Math.abs(zone.getPoints().get(0).x-selected.x);
								float dy=Math.abs(zone.getPoints().get(0).y-selected.y);
								//Did the user selected the first point ?
								if((dx*dx+dy*dy)<touchRadiusTolerance*touchRadiusTolerance){
									validateCreation();
								}	
							}
							//It's not currently possible to validate. Inform user.
							else{
								Toast.makeText(getActivity(), R.string.validation_not_available, Toast.LENGTH_SHORT).show();
								selected.set(0, 0);
							}
						}
						//The user was not trying to loop the polygon...
						else{
							Point touch = getTouchedPoint(event);
							//If the user was moving a point, update its coordinates
							if(moving > 2){//Check real movements. It's humanly impossible to not move when selecting a point by a long touch.
								zone.updatePoint(selected, touch);
								zone.endMove(touch);
								selected.set(0, 0);//No selected point anymore
							}
							//If the user wasn't moving, it touched a point a long time, it tried to select this point
							else{
								POINT_SELECTED = true; delete.setEnabled(true);
								moving=0;
							}
						}
					}
				}
			}
			//User made a move
			if (event.getAction() == MotionEvent.ACTION_MOVE && !POINT_SELECTED) {
				moving ++;//Increase moving count
				//If a point was selected
				if(selected.x!=0 || selected.y!=0){
					Point touch = getTouchedPoint(event);
					//If it was a real movement, not a point selection trying
					if (moving==3){
						if (! zone.updatePoint(selected, touch)){//Is it a normal point ?
							//If not it's a "middle" point
							zone.updateMiddle(selected, touch);//The middle point is upgraded to normal
							zone.startMove(null);//Register the move beginning. Since it's a middle there is no point to go back to.
						}else{
							zone.startMove(selected);//Register the move beginning, the point to go back to.
						}
						selected.set(touch.x,touch.y);//Update selected point
					}
					else{
						if(moving>3){//The movement already started, no action registration, just updating displaying
							zone.updatePoint(selected, touch);
							selected.set(touch.x,touch.y);
						}
						//If it was not a real movement (or not yet), no action.
					}
				}
				//If no point was selected, it's a mistake. Do nothing.
			}
		}
		refreshDisplay();
		return true;//return event consumption
	}
	
	/**
	 * 
	 * @param i
	 */
	public void selectGeom(long i){
		if(state==IMAGE_CREATION){
			state = IMAGE_SELECTION;
			exitAction();
		}
		else if(state==IMAGE_EDITION){
            exitAction();
		}
			Zone z=null;
			for(PixelGeom pg : MainActivity.pixelGeom){
				z=ConvertGeom.pixelGeomToZone(pg);
			}
			zoneCache = z;
			zone.setZone(z);

			for(int j=0; j<MainActivity.pixelGeom.size(); j++){
				if(MainActivity.pixelGeom.get(j).getPixelGeom_the_geom().equals(ConvertGeom.ZoneToPixelGeom(zoneCache))){
					geomCache = MainActivity.pixelGeom.get(j);
				}
			}
			state = IMAGE_EDITION;	drawzoneview.onEditMode();
			validate.setEnabled(true);
			back.setEnabled(false);
			cancel.setEnabled(true);
			delete.setEnabled(true);
			refreshDisplay();
	}

	/**
	 * Add the equivalent of the attribute zone in MainActivity.pixelGeom.
	 * Intersect this new PixelGeom wih older if the boolean in parameter is true. 
	 * @param tryIntersect intersect the zone to add with existing zones if true
	 */
	public void addZone(boolean tryIntersect) {
		PixelGeom pgeom = new PixelGeom();
		zone.actualizePolygon();
		pgeom.setPixelGeom_the_geom(zone.getPolygon().toText());
		try {
			if (tryIntersect) {
				ArrayList<PixelGeom> lpg = new ArrayList<PixelGeom>();
				for (PixelGeom pg : MainActivity.pixelGeom) {
					lpg.add(pg);
				}
				ArrayList<Element> le = new ArrayList<Element>();
				for (Element elt : MainActivity.element) {
					le.add(elt);
				}
				try {
					UtilCharacteristicsZone.addNewPixelGeom(pgeom, null);
					state = IMAGE_SELECTION;
					exitAction();
					zone.clearBacks();//remove list of actions backs
				} catch(TopologyException e) {
					MainActivity.pixelGeom = lpg;
					MainActivity.element = le;
					TopologyExceptionDialogFragment diag = new TopologyExceptionDialogFragment();
					diag.show(getFragmentManager(), "TopologyExceptionDialogFragment");
				}
			} else {
				UtilCharacteristicsZone.addPixelGeom(pgeom, null);
				state = IMAGE_SELECTION;
				exitAction();
				zone.clearBacks();//remove list of actions backs
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Is user selecting a zone ?
	 * @param event : the user action object
	 * @return yes if zone selected, no otherwise. The selected zone is saved in a fragment's attribute.
	 */
	private boolean hasZoneSelected(MotionEvent event){
		getMatrix();
		Point touch = getTouchedPoint(event);
		
		boolean flag=false;
		Zone z=null;
		if(event.getEventTime()-event.getDownTime()>REFERENCE_TIME){
			for(PixelGeom pg: MainActivity.pixelGeom){
				if(ConvertGeom.pixelGeomToZone(pg).containPoint(touch)){
					flag=true;
					geomCache = pg;
					z=ConvertGeom.pixelGeomToZone(pg);
					break;
				}
			}
		}
		if(flag){
			zoneCache = z;
			zone.setZone(z);
			state = IMAGE_EDITION;	drawzoneview.onEditMode();
			validate.setEnabled(true);
			back.setEnabled(false);
			cancel.setEnabled(true);
			delete.setEnabled(true);
			refreshDisplay();
			return true;
		}
		else{
			return false;
		}
	}
}