package com.buttless.client.models;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.buttless.client.R;

public class ItemRedeemClientData extends RecyclerView.ViewHolder{
    //declare the textviews from recycler
    public TextView txtTitle, txtBuy, txtSkuId;

    public ItemRedeemClientData(View itemView) {
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