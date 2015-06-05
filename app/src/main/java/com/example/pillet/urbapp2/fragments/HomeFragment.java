package com.example.pillet.urbapp2.fragments;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.pillet.urbapp2.R;
import com.example.pillet.urbapp2.activities.LoadExternalProjectsActivity;
import com.example.pillet.urbapp2.activities.LoadLocalProjectsActivity;
import com.example.pillet.urbapp2.activities.MainActivity;
import com.example.pillet.urbapp2.db.ElementType;
import com.example.pillet.urbapp2.db.Material;
import com.example.pillet.urbapp2.db.MySQLiteHelper;
import com.example.pillet.urbapp2.syncToExt.Sync;
import com.example.pillet.urbapp2.utils.Cst;
import com.example.pillet.urbapp2.utils.Utils;


/**
 * This is the fragment used to make the user choose between the differents type of project.
 * 
 * @author	COHENDET Sébastien
 * 			DAVID Nicolas
 * 			GUILBART Gabriel
 * 			PALOMINOS Sylvain
 * 			PARTY Jules
 * 			RAMBEAU Merwan
 * 			
 */

@SuppressLint("SimpleDateFormat")
public class HomeFragment extends Fragment implements OnClickListener{

	/**
	 * Image button to launch the photo native application
	 */
	private ImageView imageTakePhoto;
	/**
	 * Image button to launch the native document browser application
	 */
	private ImageView imagePhoto;
	/**
	 * Image button launching the activity LoadLocalProject
	 */
	private ImageView imageLoadLocal;
	/**
	 * Image button launching the activity LoadDistantProject
	 */
	private ImageView imageLoadDistant;
	/**
	 * The button for synchronizing materials and types from server
	 */
	private Button syncMat = null;
	/** 
	 * The button for exporting materials in XML file
	 */
	private Button exportMat = null;

	public static ProgressDialog dialogMater;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.layout_home, null);

		
		
		imageTakePhoto = (ImageView) v.findViewById(R.id.home_image_newProject_takePhoto);
		imageTakePhoto.setOnClickListener(this);
		
		imagePhoto = (ImageView) v.findViewById(R.id.home_image_newProject_photo);
		imagePhoto.setOnClickListener(this);
		
		imageLoadLocal = (ImageView) v.findViewById(R.id.home_image_loadLocalProject);
		imageLoadLocal.setOnClickListener(this);
		
		imageLoadDistant = (ImageView) v.findViewById(R.id.home_image_loadDistantProject);
		imageLoadDistant.setOnClickListener(this);
		
		syncMat = (Button) v.findViewById(R.id.home_syncMatAndTypes);
		syncMat.setOnClickListener(this);
		syncMat.setVisibility(View.GONE);//need to fix the bug, don't delete but update the table
		
		exportMat = (Button) v.findViewById(R.id.home_exportButton);
		exportMat.setOnClickListener(this);
		
		return v;
	}

	@Override
	public void onClick(View v) {
		Intent i;
		int id = v.getId();
		if (id == R.id.home_image_newProject_takePhoto) {
			Utils.showToast(MainActivity.baseContext, "Lancement de l'appareil photo", Toast.LENGTH_SHORT);
			//Setting the directory for the save of the picture to featureapp
			File folder = new File(Environment.getExternalStorageDirectory(), "featureapp/");
			folder.mkdirs();
			//Setting the name of the picture to "Photo_currentDate.jpg"
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			String currentDateandTime = sdf.format(new Date());
			File photo = new File(Environment.getExternalStorageDirectory(),"featureapp/Photo_"+currentDateandTime+".jpg");
			MainActivity.photo.setUrlTemp(photo.getAbsolutePath());
			//Launching of the photo application
			i= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
			getActivity().startActivityForResult(i, Cst.CODE_TAKE_PICTURE);
		} else if (id == R.id.home_image_newProject_photo) {
			Utils.showToast(MainActivity.baseContext, "Lancement de la galerie", Toast.LENGTH_SHORT);
			i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
			getActivity().startActivityForResult(i, Cst.CODE_LOAD_PICTURE);
		} else if (id == R.id.home_image_loadLocalProject) {
			if(MainActivity.internet){
				i = new Intent(this.getActivity(), LoadExternalProjectsActivity.class);
				getActivity().startActivityForResult(i,Cst.CODE_LOAD_EXTERNAL_PROJECT);
			}
			else{
				i = new Intent(this.getActivity(), LoadLocalProjectsActivity.class);
				getActivity().startActivityForResult(i,Cst.CODE_LOAD_LOCAL_PROJECT);
			}
		} else if (id == R.id.home_image_loadDistantProject) {
			i = new Intent(this.getActivity(), LoadExternalProjectsActivity.class);
			getActivity().startActivityForResult(i,Cst.CODE_LOAD_EXTERNAL_PROJECT);
		} else if (id == R.id.home_syncMatAndTypes) {
			/**
			 * Launch the dialog to make user waits
			 */
			dialogMater = ProgressDialog.show(getActivity(), "", 
			        "Chargement. Veuillez patienter...", true);
			MainActivity.datasource.open();
			String delete_mat = "DELETE FROM "+MySQLiteHelper.TABLE_MATERIAL;
			String delete_elmtTypes = "DELETE FROM "+MySQLiteHelper.TABLE_ELEMENTTYPE;
			MainActivity.datasource.getDatabase().rawQuery(delete_mat, null);
			MainActivity.datasource.getDatabase().rawQuery(delete_elmtTypes, null);
			MainActivity.elementType.clear();
			MainActivity.material.clear();
			Sync s = new Sync();
			s.getTypeAndMaterialsFromExt();
			saveElementTypeListToLocal(MainActivity.elementType);
			saveMaterialListToLocal(MainActivity.material);
			MainActivity.datasource.close();
		} else if (id == R.id.home_exportButton) {			
			try {
				
				MainActivity.datasource.open();
				String delete_mat = "DELETE FROM " + MySQLiteHelper.TABLE_MATERIAL;
				MainActivity.datasource.getDatabase().rawQuery(delete_mat,  null);
				MainActivity.material.clear();
				Sync s = new Sync();
				s.getTypeAndMaterialsFromExt();
				saveMaterialListToLocal(MainActivity.material);
				
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		 
				// root node
				Document doc = docBuilder.newDocument();
				Element rootElement = doc.createElement("materiaux");
				doc.appendChild(rootElement);
				 
				for (Material m : MainActivity.material) {
					
					// materiau node
					Element materiau = doc.createElement("materiau");
					rootElement.appendChild(materiau);
					
					// set attribute id
					Attr attr = doc.createAttribute("id");
					attr.setValue(Long.toString(m.getMaterial_id()));
					materiau.setAttributeNode(attr);					
			 
					// name node
					Element nom = doc.createElement("nom");
					nom.appendChild(doc.createTextNode(m.getMaterial_name()));
					materiau.appendChild(nom);					
			 
					// conductivity node
					Element conductivite = doc.createElement("conductivite");
					conductivite.appendChild(doc.createTextNode(Float.toString(m.getMaterial_conduct())));
					materiau.appendChild(conductivite);
			 
					// heat capacity node
					Element capacite_thermique = doc.createElement("capacite_thermique");
					capacite_thermique.appendChild(doc.createTextNode(Long.toString(m.getMaterial_heat_capa())));
					materiau.appendChild(capacite_thermique);
			 
					// mass density node
					Element masse_volumique = doc.createElement("masse_volumique");
					masse_volumique.appendChild(doc.createTextNode(Long.toString(m.getMaterial_mass_density())));
					materiau.appendChild(masse_volumique);
				}
					
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("/storage/sdcard0/Liste_Materiaux.xml"));
			transformer.transform(source, result);


			} catch (ParserConfigurationException | TransformerException pce) {
				pce.printStackTrace();
		  	}
			Utils.showToast(MainActivity.baseContext, "Fichier créer sous : /storage/sdcard0/Liste_Materiaux.xml", Toast.LENGTH_LONG);
			MainActivity.datasource.close();
		}
	}
	public void saveElementTypeListToLocal(ArrayList<ElementType> li){
		for (ElementType elmtT : li){
			elmtT.saveToLocal(MainActivity.datasource);
		}
	}
	
	public void saveMaterialListToLocal(ArrayList<Material> li){
		for (Material mat : li){
			mat.saveToLocal(MainActivity.datasource);
		}
	}
}