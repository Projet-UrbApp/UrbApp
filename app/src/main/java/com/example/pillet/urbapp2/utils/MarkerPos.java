package com.example.pillet.urbapp2.utils;

import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.Marker;

/**
* Defines a mix of differents objects, needed for the Async method
* Contains
*                         a marker
*                         the position
*                         the address (facultative first)
* @author Sebastien
*
*/
public class MarkerPos {

        /**
         * The marker on the map
         */
        private Marker marker;
        /**
         * The position of this marker
         */
        private LatLng position;
        /**
         * Physical Address of the marker
         */
        private String adresse="Adresse inconnue";

        /**
         * Constructor for MarkerPos from a marker and position objects
         * @param marker
         * @param position
         */
        public MarkerPos(Marker marker, LatLng position) {
                super();
                this.marker = marker;
                this.position = position;
        }

        /**
         * Copy Constructor
         * @param markpos
         */
        public MarkerPos(MarkerPos markpos) {
                super();
                this.marker = markpos.getMarker();
                this.position = markpos.getPosition();
                this.adresse = markpos.getAdresse();
        }
        /**
         * Get a marker from a MarkerPos object
         * @return Marker
         */
        public Marker getMarker() {
                return marker;
        }
    	//TODO Adddescription for javadoc
        public void setMarker(Marker marker) {
                this.marker = marker;
        }
    	//TODO Adddescription for javadoc
        public LatLng getPosition() {
                return position;
        }
    	//TODO Adddescription for javadoc
        public void setPosition(LatLng position) {
                this.position = position;
        }

    	//TODO Adddescription for javadoc
        public String getAdresse() {
                return adresse;
        }

    	//TODO Adddescription for javadoc
        public void setAdresse(String adresse) {
                this.adresse = adresse;
        }
        
        

}