package com.example.chloesriverspeedrecorder;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chloesriverspeedrecorder.databinding.FragmentSecondBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private SharedPreferences sharedPref;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        drawTable(view);


        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });

        binding.deleteStoredData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, ?> allEntries = sharedPref.getAll();
                SharedPreferences.Editor editor = sharedPref.edit();
                List<String> keyList = new ArrayList<String>();
                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    keyList.add(entry.getKey());
                }
                java.util.Collections.sort(keyList);
                ViewGroup dataGroup = view.getRootView().findViewById(R.id.dataView);
                for(int index = 0; index < dataGroup.getChildCount(); index++) {
                    View nextChild = (dataGroup).getChildAt(index);
                    ColorDrawable cd = (ColorDrawable)nextChild.getBackground();
                    if (cd!=null) {
                        int colorCode = cd.getColor();
                        if (colorCode == -7829368) {
                            editor.remove(keyList.get(index));
                        }
                    }
                }
                editor.apply();
                allEntries = sharedPref.getAll(); // for debug
                drawTable(view);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void drawTable(View view){
        LinearLayout dataLayout = view.getRootView().findViewById(R.id.dataView);
        dataLayout.removeAllViews();
//        int numKids = dataLayout.getChildCount();
//        for(int i=1;i<numKids;i++){
//            View thisText = dataLayout.getChildAt(i);
//            thisText.setVisibility(View.GONE);
//        }

        Map<String, ?> allEntries = sharedPref.getAll();
        List<String> keyList = new ArrayList<String>();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            keyList.add(entry.getKey());
        }
        java.util.Collections.sort(keyList);
        for (String key : keyList){
            TextView thisDataTextView = new TextView(view.getContext());
            thisDataTextView.setText(sharedPref.getString(key,null));
            thisDataTextView.setId(Integer.parseInt(key));
            dataLayout.addView(thisDataTextView);
            thisDataTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ColorDrawable cd = (ColorDrawable) thisDataTextView.getBackground();
                    if (cd==null){
                        thisDataTextView.setBackgroundColor(-7829368);
                        return;
                    }
                    int colorCode = cd.getColor();
                    if (colorCode == -1) {
                        thisDataTextView.setBackgroundColor(-7829368);
                    } else {
                        thisDataTextView.setBackgroundColor(-1);
                    }
                }
            });
        }
    }

}