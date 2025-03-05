package com.example.lasttele;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RadioButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class myviewholder extends RecyclerView.ViewHolder  {
    ImageView typecontact;
    TextView contactname,id;
    RadioButton radioButton;

    public myviewholder(@NonNull View itemView) {
        super(itemView);
        typecontact = itemView.findViewById(R.id.typecontact);
        contactname = itemView.findViewById(R.id.contactname);
        id = itemView.findViewById(R.id.id);
        radioButton = itemView.findViewById(R.id.radioButton);
        radioButton.setOnClickListener(v -> {
            int adapterPosition = getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                // Notify the adapter that the selected item has changed
                ((myadapter) ((RecyclerView) itemView.getParent()).getAdapter()).setSelectedPosition(adapterPosition);
            }
        });
    }
}
