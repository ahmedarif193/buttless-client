package com.buttless.client.Adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.buttless.client.Data.DataPublic;
import com.buttless.client.R;

import java.util.Collections;
import java.util.List;

public class AdapterActivity extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    List<DataPublic> data = Collections.emptyList();
    DataPublic current;
    int currentPos = 0;
    private String pontos;
    private String caminho;

    public AdapterActivity(Context context, List<DataPublic> data){
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

        myHolder.txtDate.setText(current.historyDate);
        myHolder.txtValue.setText(current.historyValue);

        if (current.historyType.equals("0")){
            myHolder.txtType.setText(R.string.add_balance);
            myHolder.imgHistory.setRotation(180);
        } else if(current.historyType.equals("1")){
            myHolder.txtType.setText(R.string.withdraw_history);
        } else if(current.historyType.equals("2")){
            myHolder.txtType.setText(R.string.refund_history);
            myHolder.imgHistory.setRotation(180);
        }

        if (current.historyStatus.equals("0")){
            myHolder.txtStatus.setText(R.string.pending);
        } else if(current.historyStatus.equals("1")){
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
