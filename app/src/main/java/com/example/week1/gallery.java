package com.example.week1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

public class gallery extends Fragment{

    ImageAdapter gAdapter;
    String name;
    Button button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        View galleryView = inflater.inflate(R.layout.tab2_gallery, container, false);

        GridView gridView = (GridView) galleryView.findViewById(R.id.grid_View);


        gAdapter = new ImageAdapter(getContext());
        gridView.setAdapter(gAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity().getApplicationContext(), FullImageActivity.class);
                i.putExtra("id", position);
                startActivity(i);
            }
        });

        return galleryView;

    }

    //fragment static method의 객체 생성용 instance
    public static gallery newInstance() {
        Bundle args = new Bundle();

        gallery fragment = new gallery();
        fragment.setArguments(args);
        return fragment;
    }
}
