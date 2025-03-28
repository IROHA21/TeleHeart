package com.moon.TeleHeart.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.moon.TeleHeart.R;

public class account2 extends Fragment {

    private OnCloseButtonClickListener closeButtonListener;

    // Define an interface for communication
    public interface OnCloseButtonClickListener {
        void onCloseButtonClicked();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account2, container, false);

        Button closeButton = view.findViewById(R.id.close2);
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