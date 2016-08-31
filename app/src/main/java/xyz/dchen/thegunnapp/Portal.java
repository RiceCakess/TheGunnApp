package xyz.dchen.thegunnapp;

/**
 * Created by David on 7/18/2016.
 */
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class Portal extends Fragment {

    Activity mActivity;

    @Override
    public void onAttach(Activity act) {
        super.onAttach(act);

        this.mActivity = act;
    }
    @Override
    public View onCreateView(LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        //create necessary view/listadatapers for the two list on Events tab
       View view = inflater.inflate(R.layout.portal, container, false);
        ImageView img = (ImageView) view.findViewById(R.id.imageView);
        img.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://id.pausd.org"));
                startActivity(browserIntent);
            }
        });
        return view;
    }
}
