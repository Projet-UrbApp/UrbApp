package com.ecn.urbapp.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.ecn.urbapp.R;

/**
 * This class creates the dialog that ask the user to choose the characteristics of the
 * zone
 * 
 * @author Jules Party
 * 
 */
public class TopologyExceptionDialogFragment extends DialogFragment {

	private Dialog box;
	private Button bpok;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		box = new Dialog(getActivity());
		box.setContentView(R.layout.layout_topology_exception_dialog);
		box.setTitle(R.string.topology_exception);
		box.setCanceledOnTouchOutside(true);
		bpok = (Button)box.findViewById(R.id.confirm_ok_topo);
		bpok.setOnClickListener(listener_ok);
		return box;
	}
	
	private OnClickListener listener_ok = new OnClickListener() {
		
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