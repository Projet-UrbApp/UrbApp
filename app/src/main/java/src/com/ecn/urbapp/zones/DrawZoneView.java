/*--------------------------------------------------------------------

Copyright Jonathan Cozzo and Patrick Rannou (22/03/2013)

This software is an Android application whose purpose is to select 
and characterize zones on a photography (type, material, color...).

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.

-----------------------------------------------------------------------*/

package src.com.ecn.urbapp.zones;

import java.util.Vector;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import src.com.ecn.urbapp.activities.MainActivity;
import src.com.ecn.urbapp.db.Element;
import src.com.ecn.urbapp.db.PixelGeom;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

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
 * This class is used to draw the zones on the image.
 */

public class DrawZoneView extends Drawable {
	private Zone zone; private Point selected; private Vector<Point> intersections;
	private boolean edit; private boolean create; private Paint paintLastPoint; private Paint paintProjection;
	float ratio = 1;
	WKTReader wktr = new WKTReader();

	/**
	 * Constructor.
	 * @param zone : zone user is currently working on
	 * @param selected : currently selected point
	 */
	public DrawZoneView(Zone zone, Point selected) {
		super();
		this.zone = zone; this.selected = selected; this.intersections = new Vector<Point>();
		this.edit = false; this.create = true;
	}

	/**
	 * Set intersection list in order to display it
	 * @param intersections
	 */
	public void setIntersections(Vector<Point> intersections){
		this.intersections = intersections;
	}

	/**
	 * Set displaying for zone creation state
	 */
	public void onCreateMode(){
		create = true;
		edit = false;
	}

	/**
	 * Set displaying for zone edition state
	 */
	public void onEditMode(){
		create = false;
		edit = true;
	}

	/**
	 * Set displaying for zone selection state 
	 */
	public void onZonePage(){
		create = false;
		edit = false;
	}
	
	/**
	 * Set displaying correcting ratio, in order to adjust points displaying size to image size
	 * @param ratio
	 */
	public void setRatio(float ratio){
		this.ratio = ratio;
	}
	
	/**
	 * This method is called to draw the zones
	 */
	@Override
	public void draw(Canvas canvas) {
		
		/*** Paints definitions ***/
		
		//normal paint, for points and segments
		Paint paintNormal = new Paint();
		paintNormal.setColor(Color.RED);
		paintNormal.setStyle(Paint.Style.FILL);
		paintNormal.setAlpha(255);
		paintNormal.setStrokeWidth(3/ratio);
		
		if(create){
			//last point paint, for creation state, display first point on its own color
			paintLastPoint = new Paint();
			paintLastPoint.setColor(Color.YELLOW);
			paintLastPoint.setStyle(Paint.Style.FILL);
			paintLastPoint.setAlpha(255);
			
			//projection paint, for segment between last and first point
			paintProjection = new Paint(paintLastPoint);
			paintProjection.setPathEffect(new DashPathEffect(new float[] {13/ratio,13/ratio},0));//set dash effect
			paintProjection.setStyle(Paint.Style.STROKE);
			paintProjection.setStrokeWidth(3/ratio);
		}else{
			//on other modes than creation first point is normal
			paintLastPoint = new Paint(paintNormal);
			paintProjection = new Paint(paintNormal);
			paintProjection.setStyle(Paint.Style.STROKE);
		}
		//intersections paint, to circle points and segments involved
		Paint paintIntersections = new Paint();
		paintIntersections.setColor(Color.BLUE);
		paintIntersections.setStyle(Paint.Style.STROKE);
		paintIntersections.setAlpha(255);
		
		//filling zones user is not working on paint
		Paint paintFillZone = new Paint();
		paintFillZone.setColor(Color.BLUE);
		paintFillZone.setStyle(Paint.Style.FILL);
		paintFillZone.setAlpha(20);

		//circling zones user is not working on paint
		Paint paintBorderZone = new Paint();
		paintBorderZone.setColor(Color.WHITE);
		paintBorderZone.setStyle(Paint.Style.STROKE);
		paintBorderZone.setAlpha(255);
		paintBorderZone.setStrokeWidth(1/ratio);
		
		/*** Displaying ***/
		//Get points
		Vector<Point> points = new Vector<Point>(zone.getPoints());//copying the points to display
		Vector<Point> middles = zone.getMiddles();

		//Display normal points (13-pixel size)
		for (int i = 0; i < points.size() - 2; i++) {
			canvas.drawCircle(points.get(i).x, points.get(i).y, 13 / ratio, paintNormal);
		}
		//Display middle points (6-pixel size)
		for (int i = 0; i < middles.size() - 1; i++) {
			canvas.drawCircle(middles.get(i).x, middles.get(i).y, 6 / ratio, paintNormal);
		}
		if (edit || points.size() == 3) {
			canvas.drawCircle(middles.lastElement().x, middles.lastElement().y, 6 / ratio, paintNormal);
		}
		if (points.size() > 1) {
			canvas.drawCircle(points.get(points.size() - 2).x,
					points.get(points.size() - 2).y, 13 / ratio, paintLastPoint);
		}
		if (points.size() == 3) {
			canvas.drawLine((int) points.get(0).x, (int) points.get(0).y,
					(int) points.get(1).x, (int) points.get(1).y, paintNormal);
		} else if (points.size() > 3) {
			int modMaxIter = 0;
			if (create) {
				Path projection = new Path();
				projection.moveTo(points.get(points.size() - 2).x,
						points.get(points.size() - 2).y);
				projection.lineTo(points.get(0).x, points.get(0).y);
				canvas.drawPath(projection, paintProjection);
				modMaxIter--;
			}
			try {
				PixelGeom pgeom = new PixelGeom();
				zone.actualizePolygon();
				pgeom.setPixelGeom_the_geom(zone.getPolygon().toText());
				for (PixelGeom pg : UtilCharacteristicsZone
						.getPixelGeomsFromGeom(wktr.read(pgeom.getPixelGeom_the_geom()), false)) {
					Polygon poly = (Polygon) wktr.read(pg.getPixelGeom_the_geom());
					Coordinate[] points2 = poly.getExteriorRing().getCoordinates();
					for (int j = 0; j < points2.length - 1 + modMaxIter; j++) {
						canvas.drawLine((int) points2[j].x, (int) points2[j].y,
								(int) points2[j + 1].x, (int) points2[j + 1].y, paintNormal);
					}
					for (int k = 0; k < poly.getNumInteriorRing(); k++) {
						points2 = poly.getInteriorRingN(k).getCoordinates();
						for (int j = 0; j < points2.length - 1; j++) {
							canvas.drawLine((int) points2[j].x, (int) points2[j].y,
									(int) points2[j + 1].x, (int) points2[j + 1].y, paintNormal);
						}
					}
				}
			} catch (ParseException e) {
			}
		}
		//selected point display
		try {
			if (selected.x != 0 || selected.y != 0) {
				canvas.drawCircle(selected.x, selected.y, 33 / ratio, paintNormal);
			}
		} catch (Exception e) {
		}

		//display intersections involved points and segments
		if (intersections != null && !intersections.isEmpty()) {
			for (int i = 0; i < intersections.size(); i = i + 2) {
				canvas.drawCircle(intersections.get(i).x, intersections.get(i).y, 13 / ratio, paintIntersections);
				canvas.drawLine(intersections.get(i).x, intersections.get(i).y,
						intersections.get(i + 1).x, intersections.get(i + 1).y, paintIntersections);
				canvas.drawCircle(intersections.get(i + 1).x, intersections.get(i + 1).y, 13 / ratio, paintIntersections);
			}
		}
		try {
			// Create a closed path for the polygon
			for (PixelGeom pgeom : MainActivity.pixelGeom) {
				paintNormal.setColor(Color.RED);
				for (PixelGeom pg : UtilCharacteristicsZone
						.getPixelGeomsFromGeom(wktr.read(pgeom.getPixelGeom_the_geom()), false)) {
					Path polyPath = new Path();
					Polygon poly = (Polygon) wktr.read(pg.getPixelGeom_the_geom());
					Coordinate[] points2 = poly.getExteriorRing().getCoordinates();
					polyPath.moveTo((float) points2[0].x, (float) points2[0].y);
					for (int j = 0; j < points2.length; j++) {
						polyPath.lineTo((float) points2[j].x, (float) points2[j].y);
						if (j != points2.length - 1) {
							canvas.drawLine((int) points2[j].x, (int) points2[j].y,
									(int) points2[j + 1].x, (int) points2[j + 1].y, paintBorderZone);
						}
					}
					for (int k = 0; k < poly.getNumInteriorRing(); k++) {
						polyPath.close();
						points2 = poly.getInteriorRingN(k).getCoordinates();
						for (int j = 0; j < points2.length; j++) {
							polyPath.lineTo((int) points2[j].x, (int) points2[j].y);
							if (j != points2.length - 1) {
								canvas.drawLine((int) points2[j].x, (int) points2[j].y,
										(int) points2[j + 1].x, (int) points2[j + 1].y, paintBorderZone);
							}
						}
					}
					// Draw the polygon
					Paint finalPaintFillZone = new Paint(paintFillZone);
					Element e = UtilCharacteristicsZone.getElementFromPixelGeomId(pgeom.getPixelGeomId());
                    if(e != null){
                    	if(e.getElement_color()!=null){//get colors set in Characteristics Fragment
                            finalPaintFillZone.setColor(Integer.parseInt(e.getElement_color()));
                            finalPaintFillZone.setAlpha(120);//setting color seems to set alpha to 255
                    	}                                
                    }
					canvas.drawPath(polyPath, finalPaintFillZone);
				}
			}
		} catch (ParseException e) {
		}
	}

	/**
	 * Compulsory, but do nothing
	 */
	@Override
	public void setAlpha(int arg0) {
	}

	/**
	 * Compulsory, but do nothing
	 */
	@Override
	public void setColorFilter(ColorFilter arg0) {
	}

	/**
	 * Compulsory, but do nothing
	 */
	@Override
	public int getOpacity() {
		return 0;
	}
}