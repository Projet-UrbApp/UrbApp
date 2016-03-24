package com.ecn.urbapp.listener;

import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.widget.Toast;

import com.ecn.urbapp.activities.MainActivity;
import com.ecn.urbapp.fragments.HomeFragment;
import com.ecn.urbapp.utils.Utils;

/**
 * @author	COHENDET S�bastien
 * 			DAVID Nicolas
 * 			GUILBART Gabriel
 * 			PALOMINOS Sylvain
 * 			PARTY Jules
 * 			RAMBEAU Merwan
 * 
 * MyTabListener class
 * 
 * This class is used to define the different actions for the tab of the ActionBar.
 * 			
 */

public class MyTabListener implements TabListener{

	/**
	 * It's the fragment concerned by the listener
	 */
	private Fragment f;
	
	/**
	 * Main constructor
	 * @param f is the fragment concerned by the listener
	 */
	public MyTabListener(Fragment f){
		this.f=f;
	}

	//TODO Adddescription for javadoc
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
	}

	//TODO Adddescription for javadoc
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {

        
        if(f.getClass()==HomeFragment.class && MainActivity.photo.getPhoto_url()!=null){
        	MainActivity.photo.setUrlTemp(MainActivity.photo.getPhoto_url());
        	MainActivity.photo.setPhoto_url(null);
        	//TODO delete
        	//MainActivity.start=true;
        }
        else if(MainActivity.photo.getPhoto_url()==null){
        	String url =  MainActivity.photo.getUrlTemp();
        	if(url!=null){
        		MainActivity.photo.setPhoto_url(url.split("/")[url.split("/").length-1]);
        	}
        }
        
        
		if (f.getClass()==HomeFragment.class && !f.isVisible() || MainActivity.photo.getPhoto_id()!=0){
			ft.replace(android.R.id.content, f);
		}
		else{
			CharSequence text = "Veuillez charger une image ou un projet avant de commencer à travailler";
			Utils.showToast(MainActivity.baseContext, text, Toast.LENGTH_SHORT);
		}
	}

	//TODO Adddescription for javadoc
	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
	}
	
}