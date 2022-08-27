package com.buttless.client.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.buttless.client.ActivityAddPoints;
import com.buttless.client.models.ItemRedeemClientData;
import com.buttless.client.R;

import java.util.Collections;
import java.util.List;

public class AdapterRedeemClientAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //declared the activity
    ActivityAddPoints wActivity;
    //declares the context
    private Context context;
    //declares the inflater
    private LayoutInflater inflater;
    //declares the list of data items
    List<ItemRedeemClientData> data = Collections.emptyList();
    //set current to zero
    int currentPos = 0;

    public AdapterRedeemClientAdapter(Context context, List<ItemRedeemClientData> data){
        //assigns the current context
        this.context = context;
        //defines the context of the inflater
        inflater = LayoutInflater.from(context);
        //defines the data
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //select the contents of the list
        View view = inflater.inflate(R.layout.list_add_funds, parent,false);
        //declares the holder
        ItemRedeemClientData holder = new ItemRedeemClientData(view);
        //return the holder
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //declares de holder
        ItemRedeemClientData myHolder = (ItemRedeemClientData) holder;
        //set the current position

        //set the title of card
        myHolder.txtTitle.setText("context.getString(R.string.google_play, dc)");
//        //set the buy label of card
//        myHolder.txtBuy.setText(context.getString(R.string.buy_for, current.itemValue));
//        //set the skuid of card
//        myHolder.txtSkuId.setText(current.itemSkuId);
//        //set the onclick function to every card item
////        myHolder.itemView.setOnClickListener(view -> wActivity.buyItem(data.get(position).itemSkuId, data.get(position).itemPoints));
    }

    @Override
    public int getItemCount() {
        //return data size to count
        return data.size();
    }
}
