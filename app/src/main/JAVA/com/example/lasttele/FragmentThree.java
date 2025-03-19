package com.example.lasttele;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;

public class FragmentThree extends Fragment {

    private OnCloseButtonClickListener closeButtonListener;

    // Define an interface for communication
    public interface OnCloseButtonClickListener {
        void onCloseButtonClicked();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_three, container, false);

        Button closeButton = view.findViewById(R.id.close);
        closeButton.setOnClickListener(v -> {
            if (closeButtonListener != null) {
                closeButtonListener.onCloseButtonClicked();
            }
        });

        return view;
    }

    // Set the listener
    public void setOnCloseButtonClickListener(OnCloseButtonClickListener listener) {
        this.closeButtonListener = listener;
    }
}