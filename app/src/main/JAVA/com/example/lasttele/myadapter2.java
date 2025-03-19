package com.example.lasttele;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
public class myadapter2 extends RecyclerView.Adapter<myviewholder> {
    Context context;
    List <contactList> items;
    private int selectedPosition = -1;

    public myadapter2(Context context, List<contactList> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public myviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new myviewholder(LayoutInflater.from(context).inflate(R.layout.contact_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull myviewholder holder, int position) {
        holder.contactname.setText(items.get(position).getName());
        holder.id.setText(String.valueOf(items.get(position).getId()));  // FIXED
        holder.typecontact.setImageResource(items.get(position).getTypeOfContact());

        holder.radioButton.setChecked(position == selectedPosition); //for button

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Method to update the selected position
    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged(); // Refresh the RecyclerView
    }

    // Method to get the selected contact's ID
    public long getSelectedContactId() {
        if (selectedPosition != -1) {
            return items.get(selectedPosition).getId();
        }
        return -1; // No contact selected
    }

}
