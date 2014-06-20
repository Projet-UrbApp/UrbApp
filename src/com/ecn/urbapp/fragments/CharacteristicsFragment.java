package com.ecn.urbapp.fragments;

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

import com.ecn.urbapp.R;
import com.ecn.urbapp.activities.MainActivity;
import com.ecn.urbapp.db.Element;
import com.ecn.urbapp.db.PixelGeom;
import com.ecn.urbapp.dialogs.CharacteristicsDialogFragment;
import com.ecn.urbapp.dialogs.SummaryDialogFragment;
import com.ecn.urbapp.dialogs.UnionDialogFragment;
import com.ecn.urbapp.utils.DrawImageView;
import com.ecn.urbapp.zones.BitmapLoader;
import com.ecn.urbapp.zones.UtilCharacteristicsZone;

/**
 * @author	COHENDET Sébastien
 * 			DAVID Nicolas
 * 			GUILBART Gabriel
 * 			PALOMINOS Sylvain
 * 			PARTY Jules
 * 			RAMBEAU Merwan
 * 
 * CharacteristicsFragment class
 * 
 * This is the fragment used to define the differents characteristics of the zone.
 * 			
 */
public class CharacteristicsFragment extends Fragment {

	/** Image containing the photo and to drawing of the zones */
	private static ImageView myImage = null;

	/** Button to fill the characteristic the selected zones */
	private Button define = null;

	/** Button to delete all the characteristics of the selected zones */
	private Button delete = null;

	/** Button to show a summary of the characteristics of the selected zones */
	private Button recap = null;

	/** Button to group all selected zones */
	private Button union = null;

	/** Button to unlink all selected zones */
	private Button unlink = null;

	/**
	 * Returns the Image used in this project.
	 */
	public static ImageView getMyImage() {
		return myImage;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.layout_definition, null);

		myImage = (ImageView) v.findViewById(R.id.definition_image);
		define = (Button) v.findViewById(R.id.definition_button_define);
		delete = (Button) v.findViewById(R.id.definition_button_delete);
		recap = (Button) v.findViewById(R.id.definition_button_recap);
		union = (Button) v.findViewById(R.id.definition_button_union);
		unlink = (Button) v.findViewById(R.id.definition_button_divide);

		DrawImageView view = new DrawImageView();
	
		DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
		
		Drawable[] drawables = {
				new BitmapDrawable(
					getResources(),
					BitmapLoader.decodeSampledBitmapFromFile(
							Environment.getExternalStorageDirectory()+"/featureapp/"+MainActivity.photo.getPhoto_url(), metrics.widthPixels, metrics.heightPixels - 174)), view
		};//TODO 174 corresponds to menu bar + buttons bar. Calculate this value ! Maybe by charging a small picture before to know ImageView size
		myImage.setImageDrawable(new LayerDrawable(drawables));
		
		myImage.setOnTouchListener(touchListenerSelectImage);
		
	    define.setOnClickListener(clickListenerDefine);
	    delete.setOnClickListener(clickListenerDelete);
	    recap.setOnClickListener(clickListenerRecap);
	    union.setOnClickListener(clickListenerUnion);
	    unlink.setOnClickListener(clickListenerUnlink);

		return v;
	}

	/**
	 * Listener that allows the selection of the zones by clicking on them.
	 * The convertTouchPoint method comes from the FeatureApp project.
	 */
	private OnTouchListener touchListenerSelectImage = new View.OnTouchListener() {

		/**
		 * This matrix is used to convert from application coordinates to
		 * pixels from the image
		 */
		protected Matrix matrix;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// If the user touch inside a zone, select the zone
			UtilCharacteristicsZone.select(UtilCharacteristicsZone.isInsidePixelGeom(this.convertTouchPoint(event.getX(), event.getY())));

			// Ask to draw again
			myImage.invalidate();
			return false;
		}

		/**
		 * Function used to convert from touch coordinates to image pixels
		 *
		 * @param x
		 *            abscissa of the touched point
		 * @param y
		 *            ordinate of the touched point
		 * @return coordinate of the touched point in pixels
		 */
		public Point convertTouchPoint(float x, float y) {
			// Get the image matrix (if needed)
			if (this.matrix == null) {
				this.matrix = new Matrix();
				((ImageView) getView().findViewById(R.id.definition_image)).getImageMatrix().invert(this.matrix);
			}

			// Get the touch point coordinates
			float[] point = { x, y };

			// Converting the point in image coordinate system
			this.matrix.mapPoints(point);
			return new Point(((int) point[0]), ((int) point[1]));
		}
	};

	/**
	 * Open Dialog windows to characterize zones. These classes are issued of the original FeatureApp project.
	 */
	private OnClickListener clickListenerDefine = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (!UtilCharacteristicsZone.getAllSelectedElements().isEmpty()) {
				// Show the dialog to choose the characteristics
				CharacteristicsDialogFragment typedialog = new CharacteristicsDialogFragment();
				typedialog.show(getFragmentManager(), "CharacteristicsDialogFragment");
			}
		}
	};

	/**
	 * Delete all characteristics of the selected zones. No confirmation is asked to the user.
	 */
	private OnClickListener clickListenerDelete = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// Unset the type of the zone
			UtilCharacteristicsZone.setTypeForSelectedElements(null);
			// Unset the material of the zone
			UtilCharacteristicsZone.setMaterialForSelectedElements(null);
			// Unset the color of the zone
			UtilCharacteristicsZone.setColorForSelectedElements(0);
			// Unselect all the zones and draw the image again
			UtilCharacteristicsZone.unselectAll();
			CharacteristicsFragment.getMyImage().invalidate();
		}
	};

	/**
	 * Open a Dialog window that summarize the characteristics of the selected zones.
	 */
	private OnClickListener clickListenerRecap = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
				SummaryDialogFragment summarydialog = new SummaryDialogFragment();
				summarydialog.show(getFragmentManager(), "TypeFragment");
		}
	};

	/**
	 * Open a Dialog window that give to the user two choice to bind zones.
	 */
	private OnClickListener clickListenerUnion = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
				UnionDialogFragment summarydialog = new UnionDialogFragment();
				summarydialog.show(getFragmentManager(), "UnionDialogFragment");
		}
	};

	/**
	 * Unlink the selected zones.
	 */
	private OnClickListener clickListenerUnlink = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Vector<Element> selectedElements = UtilCharacteristicsZone.getAllSelectedElements();
			for(Element elt : selectedElements){
				elt.setLinkedElement(new Vector<Element>());
			}
			UtilCharacteristicsZone.unselectAll();
		}
	};

	@Override
	public void onStop() {
		super.onStop();
		UtilCharacteristicsZone.unselectAll();
	}
}