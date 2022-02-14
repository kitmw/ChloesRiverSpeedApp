package com.example.chloesriverspeedrecorder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.chloesriverspeedrecorder.databinding.FragmentFirstBinding;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private SharedPreferences sharedPref;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.startTimerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((MainActivity) getActivity()).checkTimerRunning()){
                    //timer is running
                    ((MainActivity) getActivity()).stopTimerTask(view);
                } else{
                    ((MainActivity) getActivity()).startTimer(view);
                }
            }
        });

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
        view.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView speedText = view.getRootView().findViewById(R.id.editTextRiverSpeed);
                TextView upstreamHText = view.getRootView().findViewById(R.id.editTextUpRiverHeight);
                TextView tideText = view.getRootView().findViewById(R.id.editTextTideHeight);
                TextView noteText = view.getRootView().findViewById(R.id.editTextNotes);
                SharedPreferences.Editor editor = sharedPref.edit();
                String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());

                Map<String, ?> allEntries = sharedPref.getAll();
                List<Integer> keyList = new ArrayList<Integer>();
                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    keyList.add(Integer.parseInt(entry.getKey()));
                }
                String notesString = ""+noteText.getText();
                notesString = notesString.replaceAll("[^a-zA-Z0-9]","_");
                String newEntry = "<"+ timeStamp + ">SPD<" + speedText.getText() + ">USH<" + upstreamHText.getText() + ">TH<" + tideText.getText() + "><"+notesString+">";
                if(keyList.size()>0) {
                    java.util.Collections.sort(keyList);
                    int lastEntryInt = keyList.get(keyList.size() - 1);
                    String newKey = "" + (lastEntryInt + 1);
                    editor.putString(newKey, newEntry);
                }else{
                    editor.putString("1", newEntry);
                }
                editor.apply();
                Toast confirmDataEntry = Toast.makeText(getActivity(), R.string.string_data_entered, Toast.LENGTH_SHORT);
                confirmDataEntry.show();
            }
        });

    }

    @Override
    public void onResume() {
        ((MainActivity) getActivity()).setRiverScraperListener();
        ((MainActivity) getActivity()).setTideScraperListener();
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}