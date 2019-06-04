package com.example.traveltracker;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ProfileFragment extends Fragment {

    String name;
    String bio;

    EditText nameInput;
    EditText bioInput;

    Button edit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        nameInput = (EditText) view.findViewById(R.id.nameInput);
        bioInput = (EditText) view.findViewById(R.id.bioInput);

        edit = (Button) view.findViewById(R.id.Editname);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = nameInput.getText().toString();
                bio = bioInput.getText().toString();
                showToast(name);
            }
        });

        return view;
    }

    private void showToast (String text){
        Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
    }
}