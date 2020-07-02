package com.garciaapps.botoesdememe;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Adaptador extends BaseAdapter {
    private Context context;
    private int[] lista;
    private final String[] nomes;
    Resources resources;
    LayoutInflater layoutInflater;
    View viewGrid;

    public Adaptador(Context context, int[] lista, String[] nomes){
        this.context = context;
        this.lista = lista;
        this.nomes = nomes;
        resources = context.getResources();
    }

    @Override
    public int getCount() {
        return nomes.length;
    }

    @Override
    public Object getItem(int i) {
        return nomes[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        /*ImageView iv = new ImageView(context);
        final float scale = resources.getDisplayMetrics().density;//pegando densidade
        int pixelsX = (int) (75 * scale + 0.5f);//converter DP para PX
        int pixelsY = (int) (75 * scale + 0.5f);//converter DP para PX
        iv.setLayoutParams(new GridView.LayoutParams(pixelsX, pixelsY));
        //iv.setImageResource(lista[i]);
        iv.setAdjustViewBounds(true);
        Glide.with(context).load(lista[i]).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(iv);*/

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(view == null){
            viewGrid = new View(context);
            view = layoutInflater.inflate(R.layout.lista_gridview, null);
        }else{
            viewGrid = (View) view;
        }
        ImageView imageView = (ImageView) view.findViewById(R.id.android_gridview_image);
        TextView textView = (TextView) view.findViewById(R.id.android_gridview_text);
        //ImageView.setImageResource(lista[i]);
        Glide.with(context).load(lista[i]).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(imageView);
        textView.setText(nomes[i]);

        return view;
    }

    public void updateItem(int [] lista){
        this.lista = lista;
        notifyDataSetChanged();
    }

}
