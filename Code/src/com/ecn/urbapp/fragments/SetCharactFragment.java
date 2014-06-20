/*package com.ecn.urbapp.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.ecn.urbapp.R;
import com.ecn.urbapp.activities.MainActivity;
import com.ecn.urbapp.db.Element;
import com.ecn.urbapp.db.ElementType;
import com.ecn.urbapp.db.Material;
import com.ecn.urbapp.db.PixelGeom;
import com.ecn.urbapp.dialogs.SummaryDialogFragment;
import com.ecn.urbapp.utils.colorpicker.AmbilWarnaDialog;
import com.ecn.urbapp.utils.colorpicker.AmbilWarnaDialog.OnAmbilWarnaListener;
import com.ecn.urbapp.zones.UtilCharacteristicsZone;

public class SetCharactFragment extends Fragment{
	*//**
	 * The Spinner instance used to select the type of the Element(s) to characterize.
	 *//*
	private Spinner spinType;
	*//**
	 * The Spinner instance used to select the material of the Element(s) to characterize.
	 *//*
	private Spinner spinMaterial;
	*//**
	 * The View instance used to show the color of the Element(s) to characterize.
	 *//*
	private Button colorView;
	*//**
	 * Dialog used to choose a color.
	 *//*
	private AmbilWarnaDialog colorDialog;
	*//**
	 * The Color chosen in the colorView.
	 *//*
	private int chosenColor;
	*//**
	 * Should be set to true if this dialog has been opened from a SummaryDialodFragment
	 *//*
	private boolean fromRecap = false;
	*//**
	 * True if a new color has been chosen
	 *//*
	private boolean newColor = false;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.layout_definition_dialog, null);
		
		spinType = (Spinner) v.findViewById(R.id.typeZone);
		spinMaterial = (Spinner) v.findViewById(R.id.materialZone);
		//colorView = (Button)v.findViewById(R.id.definition_button_color);
		if (UtilCharacteristicsZone.getColorForSelectedZones() != 0) {
			colorView.setBackgroundColor(UtilCharacteristicsZone.getColorForSelectedZones());
		}
		colorView.setOnClickListener(openColorDialog);
		Map<String, HashMap<String, Float>> summary = UtilCharacteristicsZone.getStatsForSelectedZones(getResources());
		HashMap<String, Float> types = summary.get(getString(R.string.type));
		HashMap<String, Float> materials = summary.get(getString(R.string.materials));
		List<String> list = new ArrayList<String>();
		for (ElementType et : MainActivity.elementType) {
			list.add(et.getElementType_name());
		}
		list.add(0, getResources().getString(R.string.nullString));
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
		spinType.setAdapter(adapter);
		int position = -1;
		String type;
		if (types.keySet().size() == 1) {
			type = (String) types.keySet().toArray()[0];
			if (type.equals(getResources().getString(R.string.not_defined))) {
				type = "";
			}
		} else {
			type = getResources().getString(R.string.nullString);
		}
		position = list.indexOf(type);
		if (position != -1) {
			spinType.setSelection(position);
		}
		position = -1;
		list = new ArrayList<String>();
		for (Material mat : MainActivity.material) {
			list.add(mat.getMaterial_name());
		}
		list.add(0, getResources().getString(R.string.nullString));
		String material;
		if (materials.keySet().size() == 1) {
			material = (String) materials.keySet().toArray()[0];
			if (material.equals(getResources().getString(R.string.not_defined))) {
				material = "";
			}
		} else {
			material = getResources().getString(R.string.nullString);
		}
		adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
		spinMaterial.setAdapter(adapter);
		position = list.indexOf(material);
		if (position != -1) {
			spinMaterial.setSelection(position);
		}
		return v;
	}

	*//**
	 * Listener that add the chosen characteristics to all the selected elements
	 * and close the dialog.
	 *//*
	public void validation(){
		String selection;
		if(ZoneFragment.state==ZoneFragment.IMAGE_CREATION){
			selection = (String) spinType.getSelectedItem();
			if (!selection.equals("")) {
				for(ElementType et : MainActivity.elementType){
					if(et.getElementType_name().equals(selection)){
//						ZoneFragment.elementTemp.setElementType_id(et.getElementType_id());
					}
				}
			}
			selection = (String) spinMaterial.getSelectedItem();
			if (!selection.equals("")) {
				for(Material et : MainActivity.material){
					if(et.getMaterial_name().equals(selection)){
						ZoneFragment.elementTemp.setMaterial_id(et.getMaterial_id());
					}
				}
			}
			if (newColor) {
				ZoneFragment.elementTemp.setElement_color(""+chosenColor);
			}
			//CharacteristicsFragment.getMyImage().invalidate();
			if (fromRecap) {
				SummaryDialogFragment summarydialog = new SummaryDialogFragment();
				summarydialog.show(getFragmentManager(), "TypeFragment");
			}
		}
		else if(ZoneFragment.state==ZoneFragment.IMAGE_EDITION){
			selection = (String) spinType.getSelectedItem();
			if (!selection.equals("")) {
				UtilCharacteristicsZone.setTypeForSelectedZones(selection);
			}
			selection = (String) spinMaterial.getSelectedItem();
			if (!selection.equals("")) {
				UtilCharacteristicsZone.setMaterialForSelectedZones(selection);
			}
			if (newColor) {
				UtilCharacteristicsZone.setColorForSelectedZones(chosenColor);
			}
			//CharacteristicsFragment.getMyImage().invalidate();
			if (fromRecap) {
				SummaryDialogFragment summarydialog = new SummaryDialogFragment();
				summarydialog.show(getFragmentManager(), "TypeFragment");
			}
		}
		resetAffichage();
	};

	*//**
	 * Listener that open an AmbilWarnaDialog to chose a color.
	 *//*
	private OnClickListener openColorDialog = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// Create the color picker dialog (with the actual color of the selected zone)
			colorDialog = new AmbilWarnaDialog(getActivity(),
					UtilCharacteristicsZone.getColorForSelectedZones(),colorListener);

			// Add a title to the dialog
			colorDialog.getDialog().setTitle(R.string.definition_dialog_color);

			colorDialog.getDialog().show();
		}
	};
	
	*//**
	 * Listener for AmbilWarnaListener that save the chosen color in the attribute chosenColor and change the color of the colorView.
	 *//*
	OnAmbilWarnaListener colorListener = new OnAmbilWarnaListener() {
		@Override
		public void onOk(AmbilWarnaDialog dialog, int color) {
			// Modify the color of the zone
			chosenColor = color;
			newColor = true;
			colorView.setBackgroundColor(color);
		}

		@Override
		public void onCancel(AmbilWarnaDialog dialog) {
		}
	};

	*//**
	 * Set the fromRecap attribute to true. It means that this box is opened
	 * from a SummaryDialogFragment, and thus that a SummaryDialogFragment
	 * should be opened again when this Dialog is closed.
	 *//*
	public void setFromSummary() {
		fromRecap = true;
	}
	
	public void setAffichage(PixelGeom pg){
		for(Element e : MainActivity.element){
			if(e.getPixelGeom_id()==pg.getPixelGeomId()){
				if(e.getMaterial_id()!=0)
					spinMaterial.setSelection((int)e.getMaterial_id());
				if(e.getElementType_id()!=0)
					spinType.setSelection((int)e.getElementType_id());
				if(e.getElement_color()!=null)
					colorView.setBackgroundColor(Integer.parseInt(e.getElement_color()));
			}
		}
	}
	
	public void resetAffichage(){
		spinMaterial.setSelection(0);
		spinType.setSelection(0);
		colorView.setBackgroundColor(Color.GRAY);
	}
}*/