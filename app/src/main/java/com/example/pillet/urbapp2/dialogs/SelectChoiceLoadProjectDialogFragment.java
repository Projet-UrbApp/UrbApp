package com.example.pillet.urbapp2.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.example.pillet.urbapp2.R;
import com.example.pillet.urbapp2.activities.LoadExternalPhotosActivity;
import com.example.pillet.urbapp2.activities.LoadExternalProjectsActivity;
import com.example.pillet.urbapp2.activities.MainActivity;
import com.example.pillet.urbapp2.db.GpsGeom;
import com.example.pillet.urbapp2.db.Project;
import com.example.pillet.urbapp2.utils.ConvertGeom;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class SelectChoiceLoadProjectDialogFragment extends DialogFragment{

    public int position;
    public List<Project> refreshedValues = new ArrayList<>() ;
    public List<GpsGeom> allGpsGeom = new ArrayList<>();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Dialog box = new Dialog(getActivity());
        box.setContentView(R.layout.layout_select_project_dialog);
        box.setTitle("Que voulez vous faire ?");
        box.setCanceledOnTouchOutside(true);
        Button buttonDelete = (Button) box.findViewById(R.id.deleteButton);
        buttonDelete.setOnClickListener(listener_delete);
        Button buttonModify = (Button) box.findViewById(R.id.modifyButton);
        buttonModify.setOnClickListener(listener_modify);
        Button buttonExport = (Button) box.findViewById(R.id.exportButton);
        buttonExport.setOnClickListener(listener_export);
        return box;
    }

    private OnClickListener listener_delete = new OnClickListener() {
        @Override
        public void onClick(View v) {
            MainActivity.datasource.deleteProject(refreshedValues.get(position));
            dismiss();
        }
    };

    private OnClickListener listener_modify = new OnClickListener(){
        @Override
        public void onClick(View v){
            Toast.makeText(MainActivity.baseContext, "Chargement du projet", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(LoadExternalProjectsActivity.baseContext, LoadExternalPhotosActivity.class);
            i.putExtra("SELECTED_PROJECT_ID",refreshedValues.get(position).getProjectId());

            ArrayList<GeoPoint> coordProjet1 = new ArrayList<>();

            for (GpsGeom gg : allGpsGeom){
                if (refreshedValues.get(position).getGpsGeom_id() == gg.getGpsGeomsId()){
                    coordProjet1.addAll(ConvertGeom.gpsGeomToGeoPoint(gg));
                }
            }
            i.putExtra("PROJECT_COORD", ConvertGeom.GeoPointToGpsGeom(coordProjet1));
            startActivityForResult(i, 1);
            dismiss();
        }
    };

    private OnClickListener listener_export = new OnClickListener() {
        @Override
        public void onClick(View v){
            dismiss();
        }
    };

    @Override
    public void onCancel(DialogInterface dialog){
    }
}
