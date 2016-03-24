package src.com.ecn.urbapp.utils;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pillet.urbapp2.R;
 
/**
 * Configures the view of photos in Local/External loading
 * @author Sebastien
 *
 */
public class CustomListViewAdapter extends ArrayAdapter<RowItem> {

	/**
	 * Context to show elements on screen
	 */
    private Context context;

	/**
	 * Constructor
	 * @param context
	 * @param resourceId
	 * @param items
	 */
    public CustomListViewAdapter(Context context, int resourceId,
            List<RowItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

	/**
	 * private view holder class
	 * @author Sebastien
	 *
	 */
    private class ViewHolder {
        ImageView imageView;
        TextView txtTitle;
        TextView txtDesc;
    }

    /**
     * getView methods
     * @param position
     * @param convertView
     * @param parent
     * @return view
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        RowItem rowItem = getItem(position);
 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.layout_photolistview, null);
            holder = new ViewHolder();
            holder.txtDesc = (TextView) convertView.findViewById(R.id.photolistview_description);
            holder.txtTitle = (TextView) convertView.findViewById(R.id.photolistview_titre);
            holder.imageView = (ImageView) convertView.findViewById(R.id.photolistview_img);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
 
        holder.txtDesc.setText(rowItem.getDesc());
        holder.txtTitle.setText(rowItem.getTitle());
        
        /**
         * Load Pictures from local data
         */
    	Bitmap myShrinkedBitmap = ShrinkBitmap(rowItem.getImagePath(), 100, 100);
    	holder.imageView.setImageBitmap(myShrinkedBitmap);
	     
        return convertView;
    }
    
    /**
     * To reduce image quality to prevents OutOfMemory error
     * @param file
     * @param width
     * @param height
     * @return
     */
    Bitmap ShrinkBitmap(String file, int width, int height){
    	   
        BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
           bmpFactoryOptions.inJustDecodeBounds = true;
           Bitmap bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);
            
           int heightRatio = (int)Math.ceil(bmpFactoryOptions.outHeight/(float)height);
           int widthRatio = (int)Math.ceil(bmpFactoryOptions.outWidth/(float)width);
            
           if (heightRatio > 1 || widthRatio > 1)
           {
            if (heightRatio > widthRatio)
            {
             bmpFactoryOptions.inSampleSize = heightRatio;
            } else {
             bmpFactoryOptions.inSampleSize = widthRatio;
            }
           }
            
           bmpFactoryOptions.inJustDecodeBounds = false;
           bitmap = BitmapFactory.decodeFile(file, bmpFactoryOptions);
        return bitmap;
       }
}