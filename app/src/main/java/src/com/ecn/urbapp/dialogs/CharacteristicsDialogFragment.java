package src.com.ecn.urbapp.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import com.example.pillet.urbapp2.R;
import src.com.ecn.urbapp.activities.MainActivity;
import src.com.ecn.urbapp.db.ElementType;
import src.com.ecn.urbapp.db.Material;
import src.com.ecn.urbapp.fragments.CharacteristicsFragment;
import src.com.ecn.urbapp.utils.colorpicker.AmbilWarnaDialog;
import src.com.ecn.urbapp.zones.UtilCharacteristicsZone;

/**
 * This class creates the dialog that ask the user to choose the characteristics of the
 * zone
 * 
 * @author Jules Party
 * 
 */
public class CharacteristicsDialogFragment extends DialogFragment {

	/**
	 * The Dialog instance that allow the user to characterize Elements.
	 */
	private Dialog box;
	/**
	 * The Spinner instance used to select the type of the Element(s) to characterize.
	 */
	private Spinner spinType;
	/**
	 * The Spinner instance used to select the material of the Element(s) to characterize.
	 */
	private Spinner spinMaterial;
	/**
	 * The View instance used to show the color of the Element(s) to characterize.
	 */
	private View colorView;
	/**
	 * Dialog used to choose a color.
	 */
	private AmbilWarnaDialog colorDialog;
	/**
	 * The Color chosen in the colorView.
	 */
	private int chosenColor;
	/**
	 * True if a new color has been chosen
	 */
	private boolean newColor = false;

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		box = new Dialog(getActivity());
		box.setContentView(R.layout.layout_definition_dialog);
		box.setTitle(R.string.definition_dialog_title);
		box.setCanceledOnTouchOutside(true);
		spinType = (Spinner) box.findViewById(R.id.typeZone);
		spinMaterial = (Spinner) box.findViewById(R.id.materialZone);
		Button validate = (Button) box.findViewById(R.id.definition_button_validate);
		validate.setOnClickListener(validation);
		colorView = box.findViewById(R.id.color);
		if (UtilCharacteristicsZone.getColorForSelectedElements() != 0) {
			colorView.setBackgroundColor(UtilCharacteristicsZone.getColorForSelectedElements());
		} else {
			colorView.setBackgroundDrawable(getResources().getDrawable(R.drawable.back_color_definition));
		}
		colorView.setOnClickListener(openColorDialog);
		Map<String, String> summary = UtilCharacteristicsZone.getDefinitionForSelectedElements(getResources());
		String type = summary.get(getString(R.string.type));
		String material = summary.get(getString(R.string.materials));
		List<String> list = new ArrayList<String>();
		for (ElementType et : MainActivity.elementType) {
			list.add(et.getElementType_name());
		}
		list.add(0, getResources().getString(R.string.nullString));
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
		spinType.setAdapter(adapter);
		int position = -1;
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
		adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, list);
		spinMaterial.setAdapter(adapter);
		position = list.indexOf(material);
		if (position != -1) {
			spinMaterial.setSelection(position);
		}
		return box;

	}

	/**
	 * Listener that add the chosen characteristics to all the selected elements
	 * and close the dialog.
	 */
	private OnClickListener validation = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
		String selection;
		selection = (String) spinType.getSelectedItem();
		if (!selection.equals("")) {
			UtilCharacteristicsZone.setTypeForSelectedElements(selection);
		}
		selection = (String) spinMaterial.getSelectedItem();
		if (!selection.equals("")) {
			UtilCharacteristicsZone.setMaterialForSelectedElements(selection);
		}
		if (newColor) {
			UtilCharacteristicsZone.setColorForSelectedElements(chosenColor);
		}
		UtilCharacteristicsZone.unselectAll();
		CharacteristicsFragment.getMyImage().invalidate();
		box.dismiss();
		}
	};

	/**
	 * Listener that open an AmbilWarnaDialog to chose a color.
	 */
	private OnClickListener openColorDialog = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
		// Create the color picker dialog (with the actual color of the selected zone)
		colorDialog = new AmbilWarnaDialog(getActivity(),
				UtilCharacteristicsZone.getColorForSelectedElements(),
				colorListener);

		// Add a title to the dialog
		colorDialog.getDialog().setTitle(R.string.definition_dialog_color);

		colorDialog.getDialog().show();
		}
	};
	
	/**
	 * Listener for AmbilWarnaListener that save the chosen color in the attribute chosenColor and change the color of the colorView.
	 */
	AmbilWarnaDialog.OnAmbilWarnaListener colorListener = new AmbilWarnaDialog.OnAmbilWarnaListener() {
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

	@Override
	public void onCancel(DialogInterface dialog) {
	}
}