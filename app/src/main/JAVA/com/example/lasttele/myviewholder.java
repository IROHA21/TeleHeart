package com.example.lasttele;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class myviewholder extends RecyclerView.ViewHolder  {
    ImageView typecontact;
    TextView contactname,id;
    public myviewholder(@NonNull View itemView) {
        super(itemView);
        typecontact = itemView.findViewById(R.id.typecontact);
        contactname = itemView.findViewById(R.id.contactname);
        id = itemView.findViewById(R.id.id);

    }
}
