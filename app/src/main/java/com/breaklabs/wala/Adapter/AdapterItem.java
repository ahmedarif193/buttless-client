package com.breaklabs.wala.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.breaklabs.wala.ActivityAddFunds;
import com.breaklabs.wala.Data.DataPublic;
import com.breaklabs.wala.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdapterItem extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //declared the activity
    ActivityAddFunds wActivity;
    //declares the context
    private Context context;
    //declares the inflater
    private LayoutInflater inflater;
    //declares the list of data items
    List<DataPublic> data = Collections.emptyList();
    //declares the current
    DataPublic current;
    //set current to zero
    int currentPos = 0;

    public AdapterItem(Context context, List<DataPublic> data, ActivityAddFunds activity){
        //assigns the current context
        this.context = context;
        //defines the context of the inflater
        inflater = LayoutInflater.from(context);
        //defines the data
        this.data = data;
        //
        this.wActivity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //select the contents of the list
        View view = inflater.inflate(R.layout.list_add_funds, parent,false);
        //declares the holder
        MyHolder holder = new MyHolder(view);
        //return the holder
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //declares de holder
        MyHolder myHolder = (MyHolder) holder;
        //set the current position
        DataPublic current = data.get(position);
        //get the item value and parse it to double
        Double value = Double.parseDouble(current.itemValue);
        //change 0.20 value to your app tax - important: the application fee will be added to the google fee, I recommend leaving it at 20% or less
        Double google = 0.30 + 0.20;
        //calculates the value of the exchange with the fees, for example: if the item costs $5, the app discounts 50% of the value. The user will be paid $2.50 and you earn your 20%. The other 30% stay with Google
        Double valuewgoogle = value * google;
        String dc = new DecimalFormat("##.00").format(valuewgoogle);
        //set the title of card
        myHolder.txtTitle.setText(context.getString(R.string.google_play, dc));
        //set the buy label of card
        myHolder.txtBuy.setText(context.getString(R.string.buy_for, current.itemValue));
        //set the skuid of card
        myHolder.txtSkuId.setText(current.itemSkuId);
        //set the onclick function to every card item
        myHolder.itemView.setOnClickListener(view -> wActivity.buyItem(data.get(position).itemSkuId, data.get(position).itemPoints));
    }

    @Override
    public int getItemCount() {
        //return data size to count
        return data.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        //declare the textviews from recycler
        TextView txtTitle, txtBuy, txtSkuId;

        public MyHolder(View itemView) {
            //declare itemview
            super(itemView);
            //get the textview by id
            txtTitle = itemView.findViewById(R.id.item_title);
            //get the textview by id
            txtBuy = itemView.findViewById(R.id.item_buy);
            //get the textview by id
            txtSkuId = itemView.findViewById(R.id.item_sku_id);
        }
    }
}
