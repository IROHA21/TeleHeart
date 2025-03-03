package com.example.lasttele;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
public class myadapter extends RecyclerView.Adapter<myviewholder> {
    Context context;
    List <contactList> items;


    public myadapter(Context context, List<contactList> items) {
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
        holder.id.setText(items.get(position).getId());
        holder.typecontact.setImageResource(items.get(position).getTypeOfContact());
    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
