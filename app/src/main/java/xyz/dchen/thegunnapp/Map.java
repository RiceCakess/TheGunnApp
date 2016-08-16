package xyz.dchen.thegunnapp;

/**
 * Created by David on 7/18/2016.
 */
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class Map extends Fragment {

    Activity mActivity;

    @Override
    public void onAttach(Activity act) {
        super.onAttach(act);

        this.mActivity = act;
    }
    public static Map newInstance() {
        return new Map();
    }
    MapImageView imageView;
    @Override
    public View onCreateView(LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        //create necessary view/listadatapers for the two list on Events tab
        View view = inflater.inflate(R.layout.map, container, false);
        imageView = (MapImageView) view.findViewById(R.id.map);
        imageView.setImageResource( R.drawable.gunnmap);
        imageView.setMaxZoom(4f);
        //imageView.setImageDrawable(getResources().getDrawable(R.drawable.gunnmap));
        return view;
    }
}
