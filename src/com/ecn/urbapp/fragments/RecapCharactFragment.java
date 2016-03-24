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

package com.ecn.urbapp.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.ecn.urbapp.R;
import com.ecn.urbapp.activities.MainActivity;
import com.ecn.urbapp.db.Element;
import com.ecn.urbapp.db.PixelGeom;
import com.ecn.urbapp.zones.UtilCharacteristicsZone;

/**
 * This class creates the dialog that indicate which pixelgeoms is not defined
 * 
 * @author Jules Party
 * 
 */
public class RecapCharactFragment extends Fragment {
	
	private long pgeomIdToSelect;
	
	private ZoneFragment zf;

	public void setZoneFragment(ZoneFragment frag){
		zf=frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.layout_definition_dialog_recap, null);
		return v;
	}
	
	public void refresh(){
		LinearLayout recapList = (LinearLayout) getView().findViewById(R.id.definition_recap_linear_layout);
		recapList.removeAllViews();
		for (PixelGeom pgeom : MainActivity.pixelGeom) {
			Element element = UtilCharacteristicsZone.getElementFromPixelGeomId(pgeom.getPixelGeomId());
			if (element.getElementType_id() == 0 || element.getMaterial_id() == 0 || element.getElement_color() == null) {
				pgeomIdToSelect = pgeom.getPixelGeomId();
				Button button = new Button(getActivity());
				button.setText("La zone n°" + pgeom.getPixelGeomId() + " n'est pas entièrement défini.");
				button.setOnClickListener(new OnClickListener() {

					//private long idToSelect = pgeomIdToSelect;

					@Override
					public void onClick(View v) {
						
						if(zf!=null){
							zf.selectGeom(pgeomIdToSelect);
						}
						//TODO call selection int the zone fragment
						/*UtilCharacteristicsZone.unselectAll();
						UtilCharacteristicsZone.getPixelGeomFromId(idToSelect).selected = true;
						// Show the dialog to choose the characteristics
						CharacteristicsDialogFragment typedialog = new CharacteristicsDialogFragment();
						// Say the charasteristics dialog to re-open the summary dialog when exit
						typedialog.setFromSummary();
						typedialog.show(getFragmentManager(), "CharacteristicsDialogFragment");*/
					}
				});
				recapList.addView(button);
			}
		}
	}
}