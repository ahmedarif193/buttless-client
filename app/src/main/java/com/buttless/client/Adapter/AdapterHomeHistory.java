package com.buttless.client.Adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.buttless.client.models.DataPublic;
import com.buttless.client.R;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class AdapterHomeHistory extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    List<DataPublic> data = Collections.emptyList();
    DataPublic current;
    int currentPos = 0;
    private String pontos;
    private String caminho;

    public AdapterHomeHistory(Context context, List<DataPublic> data){
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_home, parent,false);
        MyHolder holder = new MyHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        MyHolder myHolder = (MyHolder) holder;
        DataPublic current = data.get(position);

        if(current.historyDate != null && !current.historyDate.isEmpty() && current.historyDate != "null" ){
            Log.d("API123 requestUserPoints current.historyDate", "hmmmmmmmmmm"+current.historyDate.length());
            Date date_creation = new Date(Long.parseLong(current.historyDate));
            SimpleDateFormat format_date = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");

            myHolder.txtDate.setText(format_date.format(date_creation));
        }

        myHolder.txtValue.setText(current.historyValue + " points");
        Log.d("API123 requestUserPoints current.historyValue", current.historyValue);

        if (current.historyStatus.equals("false")){
            myHolder.txtStatus.setText(R.string.pending);
        } else if(current.historyStatus.equals("true")){
            myHolder.txtStatus.setText(R.string.approved);
            myHolder.txtStatus.setTextColor(context.getResources().getColor(R.color.green_900));
        } else if(current.historyStatus.equals("2")){
            myHolder.txtStatus.setText(R.string.cancelled);
            myHolder.txtStatus.setTextColor(context.getResources().getColor(R.color.red_900));
        } else if(current.historyStatus.equals("3")){
            myHolder.txtStatus.setText(R.string.waiting_for_google);
        } else if(current.historyStatus.equals("4")){
            myHolder.txtStatus.setText(R.string.refund);
        } else if(current.historyStatus.equals("5")){
            myHolder.txtStatus.setText(R.string.waiting_payment);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        TextView txtType;
        TextView txtDate;
        TextView txtStatus;
        TextView txtValue;
        ImageView imgHistory;

        public MyHolder(View itemView) {
            super(itemView);
            txtType = itemView.findViewById(R.id.history_item_type);
            txtDate = itemView.findViewById(R.id.history_item_date);
            txtValue = itemView.findViewById(R.id.history_item_value);
            txtStatus = itemView.findViewById(R.id.history_item_status);
            imgHistory = itemView.findViewById(R.id.history_item_image);
        }
    }
}
