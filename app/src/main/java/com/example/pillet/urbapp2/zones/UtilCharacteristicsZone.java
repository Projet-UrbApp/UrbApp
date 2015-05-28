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

package com.example.pillet.urbapp2.zones;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import android.content.res.Resources;
import android.graphics.Point;

import com.example.pillet.urbapp2.R;
import com.example.pillet.urbapp2.activities.MainActivity;
import com.example.pillet.urbapp2.db.Element;
import com.example.pillet.urbapp2.db.ElementType;
import com.example.pillet.urbapp2.db.Material;
import com.example.pillet.urbapp2.db.PixelGeom;
import com.example.pillet.urbapp2.utils.GetId;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * This class regroup some useful methods for operation on PixelGeoms and
 * Elements
 * 
 * @author patrick, Jules Party
 * 
 */
public final class UtilCharacteristicsZone {

	private static GeometryFactory gf = new GeometryFactory();
	private static WKTReader wktr = new WKTReader(gf);

	/**
	 * Set the type of all the selected Elements
	 *
	 * @param type
	 *            the type to set
	 */
	public static void setTypeForSelectedElements(String type) {
		for (Element e : getAllSelectedElements()) {
			if(type==null){
				e.setElementType_id(0);
			}
			else{
				for(ElementType et : MainActivity.elementType){
					if(et.getElementType_name().equals(type)){
						e.setElementType_id(et.getElementType_id());
					}
				}
			}
		}
	}

	/**
	 * Set the material of all the selected Elements
	 *
	 * @param material
	 *            the material to set
	 */
	public static void setMaterialForSelectedElements(String material) {
		for (Element e : getAllSelectedElements()) {
			if(material==null){
				e.setMaterial_id(0);
			}
			else{
				for(Material m : MainActivity.material){
					if(m.getMaterial_name().equals(material)){
						e.setMaterial_id(m.getMaterial_id());
					}
				}
			}
		}
	}

	/**
	 * Set the color of all the selected Elements
	 *
	 * @param color
	 *            the color to set
	 */
	public static void setColorForSelectedElements(int color) {
		for (Element e : getAllSelectedElements()) {
			e.setElement_color(""+color);
		}
	}

	/**
	 * Return the color of all the selected Elements as an Integer (or 0 when
	 * the Elements have not the same color)
	 * 
	 * @return the color as an Integer
	 */
	public static Integer getColorForSelectedElements() {
		Vector<Element> element = getAllSelectedElements();
		if (element != null && !element.isEmpty()) {
			int color=0;
			if(element.get(0).getElement_color()!=null){
				color = Integer.parseInt(element.get(0).getElement_color());
			}
			for (Element e : element) {
				if(e.getElement_color() == null || Integer.parseInt(e.getElement_color()) != color) {
					color = 0;
					break;
				}
			}
			return color;
		} else {
			return 0;
		}
	}

	/**
	 * This method return the ID of the first PixelGeom that
	 * contains the point in parameter and -1 if no PixelGeom is appropriate
	 * 
	 * @param point
	 * @return the ID of the smallest PixelGeom that contains the point and -1
	 *         otherwise
	 */
	public static long isInsidePixelGeom(Point point) {
		long resultID = -1;
		for (PixelGeom pgeom : MainActivity.pixelGeom) {
			Coordinate coord = new Coordinate(point.x, point.y);
			com.vividsolutions.jts.geom.Point geomPoint = gf.createPoint(coord);
			try {
				if (geomPoint.within(wktr.read(pgeom.getPixelGeom_the_geom()))) {
					if (resultID == -1) {
						resultID = pgeom.getPixelGeomId();
					} else  if (wktr.read(pgeom.getPixelGeom_the_geom()).getArea()
							< wktr.read(UtilCharacteristicsZone.getPixelGeomFromId(resultID).getPixelGeom_the_geom()).getArea()) {
						resultID = pgeom.getPixelGeomId();
					}
				}
			} catch (ParseException e) {
			}
		}
		return resultID;
	}

	/**
	 * Unselect all the Elements
	 */
	public static void unselectAll() {
		for (Element elt : MainActivity.element) {
			elt.setSelected(false);
		}
	}

	/**
	 * Select the Element which is linked to the PixelGeom whose ID is in
	 * parameter (if the ID is positive) and all the linked Elements.
	 * If the ID is negative, it unselects all the Elements.
	 * 
	 * @param pixelGeomID
	 *            the id of the PixelGeom to select
	 */
	public static void select(long pixelGeomID) {
		if (pixelGeomID >= 0) {
			Element elementSelected = UtilCharacteristicsZone.getElementFromPixelGeomId(pixelGeomID);
			elementSelected.setSelected(!elementSelected.isSelected());
			for (Element elt : elementSelected.getLinkedElement()) {
				elt.setSelected(elementSelected.isSelected());
			}
		} else {
			unselectAll();
		}
	}

	/**
	 * Return a vector with all the selected Elements
	 * 
	 * @return vector with all the selected Elements
	 */
	public static Vector<Element> getAllSelectedElements() {
		Vector<Element> selectedElements = new Vector<Element>();
		for(Element elt: MainActivity.element){
			if(elt.isSelected()){
				selectedElements.add(elt);
			}
		}
		return selectedElements;
	}

	/**
	 * Return a vector with all the PixelGeoms binded to selected Elements
	 * 
	 * @return vector with all the PixelGeoms binded to selected Elements
	 */
	public static Vector<PixelGeom> getAllSelectedPixelGeoms() {
		Vector<PixelGeom> selectedPixelGeoms = new Vector<PixelGeom>();
		for(Element elt: UtilCharacteristicsZone.getAllSelectedElements()){
			selectedPixelGeoms.add(UtilCharacteristicsZone.getPixelGeomFromId(elt.getPixelGeom_id()));
		}
		return selectedPixelGeoms;
	}

	/**
	 * This method return a HashMap with two keys : type and materials, and
	 * whose values are the type and material of the selected Elements. If they
	 * don't have the same definition the String "" is used as value.
	 * 
	 * @param res
	 */
	public static Map<String, String> getDefinitionForSelectedElements(Resources res) {
		Vector<Element> selectedElements = getAllSelectedElements();
		String type = null;
		String material = null;
		for (Element e : selectedElements) {
			long elementTypeId = e.getElementType_id(); 
			if (elementTypeId != 0) {
				for (ElementType et : MainActivity.elementType) {
					if (et.getElementType_id() == elementTypeId) {
						if (type == null || type == et.getElementType_name()) {
							type = et.getElementType_name();
						} else {
							type = "";
							break;
						}
					}
				}
			} else if (type != null && type != "") {
				type = "";
			}
			long materialId = e.getMaterial_id(); 
			if (materialId != 0) {
				for (Material  m : MainActivity.material) {
					if (m.getMaterial_id() == materialId) {
						if (material == null || material == m.getMaterial_name()) {
							material = m.getMaterial_name();
						} else {
							material = "";
							break;
						}
					}
				}
			} else if (material != null && material != "") {
				material = "";
			}
			if (type == "" && material == "") {
				break;
			}
		}
		type = type == null ? "" : type;
		material = material == null ? "" : material;
		HashMap<String, String> summary = new HashMap<String, String>();
		summary.put(res.getString(R.string.type), type);
		summary.put(res.getString(R.string.materials), material);
		return summary;
	}

	/**
	 * This method add the PixelGeom in parameter to the list of PixelGeom from
	 * MainActivity. If the PixelGeom intersects with existing PixelGeoms, the
	 * method calculates the intersections between those PixelGeoms and add them
	 * so that no PixelGeom covers another one. The method also add the Element
	 * linked to the PixelGeom. This element will have the same characteristics
	 * that the Element in parameter, unless the PixelGeom intersects the
	 * PixelGeom from an existing Elements that is already characterized.
	 * 
	 * @param pixelGeom
	 *            the PixelGeom to add
	 * @param ref
	 *            the Element defining the characteristics of the PixelGeom
	 * @throws TopologyException
	 *             when the JTS library does not manage to compute the
	 *             intersection
	 * @throws ParseException
	 *             when a PixelGeom cannot be interpreted into a Geometry
	 */
	public static void addNewPixelGeom(PixelGeom pixelGeom, Element ref)
			throws TopologyException, ParseException {
		List<Long> pixelGeomIdToRemove = new ArrayList<Long>();
		List<PixelGeom> pixelGeomToAdd = new ArrayList<PixelGeom>();
		Map<PixelGeom, Element> linkedElements = new HashMap<PixelGeom, Element>();
		Geometry geom = getUnionOfExistingPixelGeomInside(pixelGeom);
		if (geom != null) {
			pixelGeom = createHoleForPixelGeomToAdd(pixelGeom, geom);
		}
		findNextIntersection(pixelGeom, ref, pixelGeomIdToRemove, pixelGeomToAdd, linkedElements);
		if (pixelGeomToAdd.isEmpty()) {
			addPixelGeom(pixelGeom, ref);
		} else {
			removeAllFromMainActivity(pixelGeomIdToRemove);
			addAllPixelGeom(pixelGeomToAdd, linkedElements);
		}
	}
	
	/**
	 * Complete the lists in parameter with the different PixelGeom and Element to add and to remove to practice the next intersection
	 *  
	 * @param pixelGeom the PixelGeom to insert
	 * @param ref the element containing the information linked to the PixelGeom
	 * @param pixelGeomIdToRemove the list of ID from the PixelGeom that must be removed
	 * @param pixelGeomToAdd the list of PixelGeom that must be added
	 * @param linkedElements the map associated PixelGeom to add and the element that characterized
	 * @throws ParseException if a PixelGeom cannot be interpreted into a Geometry
	 */
	private static void findNextIntersection(PixelGeom pixelGeom, Element ref, List<Long> pixelGeomIdToRemove, List<PixelGeom> pixelGeomToAdd, Map<PixelGeom, Element> linkedElements) throws ParseException {
		Geometry geom;
		for (PixelGeom oldPixelGeom : MainActivity.pixelGeom) {
			if (wktr.read(pixelGeom.getPixelGeom_the_geom()).within(wktr.read(oldPixelGeom.getPixelGeom_the_geom()))) {
				pixelGeomIdToRemove.add(oldPixelGeom.getPixelGeomId());
				oldPixelGeom = createHoleForPixelGeomToAdd(oldPixelGeom, wktr.read(pixelGeom.getPixelGeom_the_geom()));
				pixelGeomToAdd.add(oldPixelGeom);
				pixelGeomToAdd.add(pixelGeom);
				break;
			} else if (wktr.read(pixelGeom.getPixelGeom_the_geom()).intersects(wktr.read(oldPixelGeom.getPixelGeom_the_geom()))
					&& !wktr.read(pixelGeom.getPixelGeom_the_geom()).touches(wktr.read(oldPixelGeom.getPixelGeom_the_geom()))) {
				pixelGeomIdToRemove.add(oldPixelGeom.getPixelGeomId());
				Element elt = getElementFromPixelGeomId(oldPixelGeom.getPixelGeomId());
				geom = wktr.read(pixelGeom.getPixelGeom_the_geom()).intersection(wktr.read(oldPixelGeom.getPixelGeom_the_geom()));
				for (PixelGeom pg : getPixelGeomsFromGeom(geom, true)) {
					linkedElements.put(pg, elt);
					pixelGeomToAdd.add(pg);
				}
				geom = wktr.read(pixelGeom.getPixelGeom_the_geom()).difference(wktr.read(oldPixelGeom.getPixelGeom_the_geom()));
				for (PixelGeom pg : getPixelGeomsFromGeom(geom, true)) {
					linkedElements.put(pg, ref);
					pixelGeomToAdd.add(pg);
				}
				geom = wktr.read(oldPixelGeom.getPixelGeom_the_geom()).difference(wktr.read(pixelGeom.getPixelGeom_the_geom()));
				for (PixelGeom pg : getPixelGeomsFromGeom(geom, true)) {
					linkedElements.put(pg, elt);
					pixelGeomToAdd.add(pg);
				}
				break;
			}
		}
	}
	
	/**
	 * Return the union of all the PixelGeom entirely contained in the PixelGeom in parameter
	 * 
	 * @param pixelGeom the PixelGeom defining the surface to test
	 * @return the union of all the PixelGeom entirely contained in the PixelGeom in parameter
	 * @throws ParseException if a PixelGeom cannot be interpreted into a Geometry
	 */
	private static Geometry getUnionOfExistingPixelGeomInside(PixelGeom pixelGeom) throws ParseException {
		Geometry geom = null;
		for (PixelGeom oldPixelGeom : MainActivity.pixelGeom) {
			if (wktr.read(pixelGeom.getPixelGeom_the_geom()).contains(
					wktr.read(oldPixelGeom.getPixelGeom_the_geom()))) {
				if (geom == null) {
					geom = gf.createGeometry(wktr.read(
							oldPixelGeom.getPixelGeom_the_geom()));
				} else {
					geom = geom.union(wktr.read(
							oldPixelGeom.getPixelGeom_the_geom()));
				}
			}
		}
		return geom;
	}
	
	/**
	 * Return the PixelGeom pgeom in parameter with a hole defined by
	 * the Geometry hole in parameter. pgeom is not modified.
	 * 
	 * @param pgeom the PixelGeom to pierce
	 * @param hole define the hole using the JTS Geometry type
	 * @return the pierced PixelGeom
	 * @throws ParseException if a PixelGeom cannot be interpreted into a Geometry
	 */
	private static PixelGeom createHoleForPixelGeomToAdd(PixelGeom pgeom, Geometry hole) throws ParseException {
		if (hole instanceof GeometryCollection) {
			GeometryCollection geomColl = (GeometryCollection) hole;
			for (int i = 0; i < geomColl.getNumGeometries(); i++) {
				if (geomColl.getGeometryN(i) instanceof Polygon) {
					//PixelGeom pg = getPixelGeomFromGeom(geomColl.getGeometryN(i), true);
					return createHoleForPixelGeomToAdd(pgeom, geomColl.getGeometryN(i));
				}
			}
		} else if (hole instanceof Polygon) {
			PixelGeom pg = getPixelGeomFromGeom(hole, true);
			return createHole(pgeom, pg);
		}
		return pgeom;
	}

	/**
	 * Return the PixelGeom whose id is given in parameter.
	 * 
	 * @param pixelGeomId the id of the PixelGeom to get
	 * @return the PixelGeom corresponding to the ID in parameter 
	 */
	public static PixelGeom getPixelGeomFromId(Long pixelGeomId) {
		for (PixelGeom pg : MainActivity.pixelGeom) {
			if (pg.getPixelGeomId() == pixelGeomId) {
				return pg;
			}
		}
		return null;
	}

	/**
	 * Return the Element whose id is given in parameter.
	 * 
	 * @param elementId the id of the Element to get
	 * @return the Element corresponding to the ID in parameter 
	 */
	public static Element getElementFromId(Long elementId) {
		for (Element elt : MainActivity.element) {
			if (elt.getElement_id() == elementId) {
				return elt;
			}
		}
		return null;
	}

	/**
	 * Return the (first) Element which has the id is given in parameter as
	 * pixelGeom_id.
	 * 
	 * @param pixelGeomId
	 *            the id of the PixelGeom used by the Element to get
	 * @return the Element corresponding to the pixelGeom_id in parameter
	 */
	public static Element getElementFromPixelGeomId(Long pixelGeomId) {
		for (Element element : MainActivity.element) {
			if (element.getPixelGeom_id() == pixelGeomId) {
				return element;
			}
		}
		return null;
	}

	/**
	 * Remove all the PixelGeom from the MainActivity List whose ID are included
	 * in the List in parameter.
	 * 
	 * @param pixelGeomIdToRemove the list of ids to remove
	 */
	private static void removeAllFromMainActivity(List<Long> pixelGeomIdToRemove) {
		for (Long pgeomId : pixelGeomIdToRemove) {
			Element elt = getElementFromPixelGeomId(pgeomId);
			PixelGeom pgeom = getPixelGeomFromId(pgeomId);
			MainActivity.pixelGeom.remove(pgeom);
			MainActivity.element.remove(elt);
		}
	}
	
	/**
	 * Add all the PixelGeom from the list in parameter
	 * @param pixelGeomToAdd
	 * @param linkedElements
	 * @throws TopologyException
	 * @throws ParseException
	 */
	private static void addAllPixelGeom(List<PixelGeom> pixelGeomToAdd, Map<PixelGeom, Element> linkedElements) throws TopologyException, ParseException {
		for (PixelGeom pg : pixelGeomToAdd) {
			addNewPixelGeom(pg, linkedElements.get(pg));
		}
	}
	
	/**
	 * This method add the PixelGeom in parameter to the list of PixelGeom from
	 * MainActivity. The method also add the Element linked to the PixelGeom.
	 * This element will have the same characteristics that the Element in
	 * parameter. This method should be used only if the PixelGeom does not
	 * intersect any existing PixelGeom.
	 * 
	 * @param pgeom
	 *            the PixelGeom to add
	 * @param elt
	 *            the Element defining the characteristics of the PixelGeom
	 * @throws ParseException if a PixelGeom cannot be interpreted into a Geometry
	 */
	public static void addPixelGeom(PixelGeom pgeom, Element elt) throws ParseException {
		pgeom.setPixelGeomId(GetId.PixelGeom());
		boolean flag=false;
		for(Element e :MainActivity.element){
			if(e.getPixelGeom_id()==pgeom.getPixelGeomId()){
				flag=true;
			}
		}
		if(!flag){
			Element element = new Element();
			element.setElement_id(GetId.Element());
			element.setPhoto_id(MainActivity.photo.getPhoto_id());
			element.setPixelGeom_id(pgeom.getPixelGeomId());
			element.setGpsGeom_id(MainActivity.photo.getGpsGeom_id());
			if (elt != null) {
				element.setElement_color(elt.getElement_color());
				element.setElementType_id(elt.getElementType_id());
				element.setMaterial_id(elt.getMaterial_id());
			}
			if (MainActivity.element.size() < element.getElement_id()) {
				MainActivity.element.add(element);
			} else {
				MainActivity.element.add((int) element.getElement_id() -1, element);
			}
		}
		if (MainActivity.pixelGeom.size() < pgeom.getPixelGeomId()) {
			MainActivity.pixelGeom.add(pgeom);
		} else {
			MainActivity.pixelGeom.add((int) pgeom.getPixelGeomId() - 1, pgeom);
		}
	}

	/**
	 * Transform the Geometry in parameter into a list of the Polygons composing
	 * the Geometry, possible LineString or Point are ignored. The Polygons are
	 * then converted into PixelGeoms and returned.
	 * 
	 * @param geom
	 *            the Geometry to convert into PixelGeom(s)
	 * @param checkPoint true if the value of Coordinates must be checked and convert from double to int
	 * @return the equivalent PixelGeom 
	 */
	public static List<PixelGeom> getPixelGeomsFromGeom(Geometry geom, boolean checkPoint) {
		List<PixelGeom> result = new ArrayList<PixelGeom>();
		if (geom instanceof GeometryCollection) {
			GeometryCollection geomColl = (GeometryCollection) geom;
			for (int i = 0; i < geomColl.getNumGeometries(); i++) {
				result.addAll(getPixelGeomsFromGeom(geomColl.getGeometryN(i), checkPoint));
			}
		} else if (geom instanceof Polygon) {
			result.add(getPixelGeomFromGeom(geom, checkPoint));
		}
		return result;
	}

	/**
	 * Transform the Geometry in parameter into a PixelGeom by directly
	 * transform it into its WKT representation.
	 * 
	 * @param geom
	 *            the Geometry to convert into PixelGeom
	 * @param checkPoint true if the value of Coordinates must be checked and convert from double to int
	 * @return the equivalent PixelGeom
	 */
	private static PixelGeom getPixelGeomFromGeom(Geometry geom, boolean checkPoint) {
		PixelGeom pg = new PixelGeom();
		if (checkPoint) {
			Polygon poly = intPolygon((Polygon) geom);
			pg.setPixelGeom_the_geom(poly.toText());
		} else {
			pg.setPixelGeom_the_geom(geom.toText());
		}
		return pg;
	}

	/**
	 * Convert all the coordinate of the Polygon in Integer so that it can be
	 * draw in an Android Application. Also check the order of the points to
	 * properly draw hole.
	 * 
	 * @param geom
	 *            the Polygon to convert
	 * @return the equivalent Polygon with Integer coordinates
	 */
	private static Polygon intPolygon(Polygon geom) {
		Coordinate[] coords = geom.getExteriorRing().getCoordinates();
		int dim = coords.length;
		for (int i = 0; i < dim; i++) {
			coords[i] = new Coordinate((int) coords[i].x, (int) coords[i].y);
		}
		LinearRing shell = gf.createLinearRing(coords);
		if ((coords[0].x - coords[1].x) * (coords[2].y - coords[0].y)
				-  (coords[0].y - coords[1].y) * (coords[2].x - coords[0].x) < 0) {
			shell = gf.createLinearRing(shell.reverse().getCoordinates());
		}
		LinearRing[] holes = new LinearRing[geom.getNumInteriorRing()];
		for (int j = 0; j < geom.getNumInteriorRing(); j++) {
			coords = geom.getInteriorRingN(j).getCoordinates();
			dim = coords.length;
			for (int i = 0; i < dim; i++) {
				coords[i] = new Coordinate((int) coords[i].x, (int) coords[i].y);
			}
			holes[j] = gf.createLinearRing(coords);
			if ((coords[0].x - coords[1].x) * (coords[2].y - coords[0].y)
					-  (coords[0].y - coords[1].y) * (coords[2].x - coords[0].x) > 0) {
				holes[j] = gf.createLinearRing(holes[j].reverse().getCoordinates());
			}
		}
		return gf.createPolygon(shell, holes);
	}

	/**
	 * Return the PixelGeom pgeomShell in parameter with a new hole defined by
	 * the PixelGeom pgeomHole in parameter. The method does not checked that
	 * the hole can be added.
	 * 
	 * @param pgeomShell
	 *            the PixelGeom to pierce
	 * @param pgeomHole
	 *            define the edge of the hole
	 * @return the pierced PixelGeom
	 * @throws ParseException
	 *             if a PixelGeom cannot be interpreted into a Geometry
	 */
	public static PixelGeom createHole(PixelGeom pgeomShell, PixelGeom pgeomHole)
			throws ParseException {
		Polygon polyShell = null;
		Geometry geomShell = wktr.read(pgeomShell.getPixelGeom_the_geom());
		if (geomShell instanceof MultiPolygon) {
			polyShell = (Polygon) ((MultiPolygon) geomShell).getGeometryN(0);
		} else if (geomShell instanceof Polygon) {
			polyShell = (Polygon) geomShell;
		}
		Polygon polyHole = (Polygon) wktr.read(pgeomHole.getPixelGeom_the_geom());
		LinearRing shell = gf.createLinearRing(polyShell.getExteriorRing()
				.getCoordinates());
		int nbrHoles = polyShell.getNumInteriorRing();
		LinearRing[] holes = new LinearRing[nbrHoles + 1];
		for (int i = 0; i < nbrHoles; i++) {
			holes[i] = gf.createLinearRing(polyShell.getInteriorRingN(i).getCoordinates());
		}
		Coordinate[] coordinates = polyHole.getExteriorRing().getCoordinates();
		LinearRing lr = gf.createLinearRing(coordinates);
		holes[nbrHoles] = lr;
		PixelGeom result = new PixelGeom();
		Polygon poly = intPolygon(gf.createPolygon(shell, holes));
		result.setPixelGeom_the_geom(poly.toText());
		return result;
	}

	/**
	 * Return the union of the elements from the Vector of PixelGeom as a Geometry.
	 * 
	 * @param selectedPixelGeom the PixelGeom to transform
	 * @return the union of the elements from the Vector of PixelGeom as a Geometry
	 * @throws ParseException if a PixelGeom cannot be interpreted into a Geometry
	 */
	public static Geometry getGeometryUnion(Vector<PixelGeom> selectedPixelGeom) throws ParseException {
		Geometry newGeom = null;
		for (PixelGeom pg : selectedPixelGeom) {
			if (newGeom == null) {
				newGeom = wktr.read(pg.getPixelGeom_the_geom());
			} else {
				newGeom = newGeom.union(wktr.read(pg.getPixelGeom_the_geom()));
			}
		}
		return newGeom;
	}

	public static void union(Vector<PixelGeom> selectedPixelGeom) throws ParseException {
		Geometry geom = getGeometryUnion(selectedPixelGeom);
		PixelGeom pg = getPixelGeomFromGeom(geom, false);
		ArrayList<Long> pgeomId = new ArrayList<Long>();
		for (PixelGeom pgeom : selectedPixelGeom) {
			pgeomId.add(pgeom.getPixelGeomId());
		}
		removeAllFromMainActivity(pgeomId);
		addPixelGeom(pg, null);
	}
}