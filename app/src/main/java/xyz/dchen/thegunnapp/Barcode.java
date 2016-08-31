package xyz.dchen.thegunnapp;

/**
 * Created by David on 7/18/2016.
 */

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.EnumMap;

public class Barcode extends Fragment {

    Activity mActivity;

    @Override
    public void onAttach(Activity act) {
        super.onAttach(act);

        this.mActivity = act;
    }
    ImageView imageView;
    Button button;
    EditText editText;
    TextView idText;
    SharedPreferences sharedPref;
    @Override
    public View onCreateView(LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.barcode_generator, container, false);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        button = (Button) view.findViewById(R.id.button);
        editText = (EditText) view.findViewById(R.id.editText);
        idText = (TextView) view.findViewById(R.id.idText);
        sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPref.edit();
                generateBarcode(editText.getText().toString());
                editor.putString("lastBarcode", editText.getText().toString().trim());
                editor.commit();
            }
        });
        String lastBarcode = sharedPref.getString("lastBarcode",null);
        editText.setText(lastBarcode);
        generateBarcode(editText.getText().toString());

        return view;
    }
    private void generateBarcode(String id){
        try{
            Bitmap bitmap = null;
            Display display = mActivity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            bitmap = encodeAsBitmap(editText.getText().toString(), BarcodeFormat.CODE_39, (int) (size.x*.9), 300);
            imageView.setImageBitmap(bitmap);
            idText.setText(editText.getText());
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;
    Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height) throws WriterException {
        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        java.util.Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try {
            result = writer.encode(contentsToEncode, format, img_width, img_height, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }
}
