package com.example.pillet.urbapp2.dialogs;

import java.util.ArrayList;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.pillet.urbapp2.R;
import com.example.pillet.urbapp2.activities.MainActivity;
import com.example.pillet.urbapp2.db.Composed;
import com.example.pillet.urbapp2.db.Element;
import com.example.pillet.urbapp2.db.GpsGeom;
import com.example.pillet.urbapp2.db.PixelGeom;
import com.example.pillet.urbapp2.db.Project;

/**
 * This class creates the dialog that ask the user to choose the characteristics of the
 * zone
 * 
 * @author Jules Party
 * 
 */
public class ConfirmPhotoDialogFragment extends DialogFragment {

	//TODO Adddescription for javadoc
	private Dialog box;
	//TODO Adddescription for javadoc
	private Button bpok;
	//TODO Adddescription for javadoc
	private Button quit;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		box = new Dialog(getActivity());
		box.setContentView(R.layout.layout_confirm_dialog);
		box.setTitle(R.string.confirm_photo);
		box.setCanceledOnTouchOutside(true);
		bpok = (Button)box.findViewById(R.id.confirm_ok);
		bpok.setOnClickListener(listener_ok);
		quit = (Button)box.findViewById(R.id.confirm_quit);
		quit.setOnClickListener(listener_quit);
		return box;
	}

	//TODO Adddescription for javadoc
	private OnClickListener listener_ok = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			MainActivity.composed= new ArrayList<Composed>();
			MainActivity.element= new ArrayList<Element>();
			MainActivity.gpsGeom= new ArrayList<GpsGeom>();
			MainActivity.pixelGeom= new ArrayList<PixelGeom>();
			MainActivity.project= new ArrayList<Project>();
		
			box.dismiss();
		}
	};

	//TODO Adddescription for javadoc
	private OnClickListener listener_quit = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			box.dismiss();
		}
	};
	/**
	 * Action realized when the user cancel the dialog (touch outside the dialog
	 * or press the back button)
	 */
	@Override
	public void onCancel(DialogInterface dialog) {
	}
}