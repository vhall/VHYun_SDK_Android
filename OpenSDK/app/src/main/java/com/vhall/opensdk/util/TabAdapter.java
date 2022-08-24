package com.vhall.opensdk.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vhall.opensdk.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zwp on 2019/6/21
 */
public class TabAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<String> idList = new ArrayList<>();
    ItemClickListener itemClickListener;
    ClearClickListener clearClickListener;
    private boolean editAble = false;
    private String activeId = "";

    public TabAdapter(Context context, List<String> idList) {
        this.context = context;
        this.idList = idList;
    }

    public void setIdList(List<String> idList) {
        this.idList = idList;
        notifyDataSetChanged();
    }

    public void setEditAble(boolean editAble) {
        this.editAble = editAble;
        notifyDataSetChanged();
    }

    public void setActiveId(String activeId) {
        this.activeId = activeId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            view = LayoutInflater.from(context).inflate(R.layout.item_doc_tab, parent, false);
            return new HolderTab(view);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_doc_add, parent, false);
            return new HolderAdd(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HolderTab) {
            HolderTab tab = (HolderTab) holder;
            if (activeId.equals(idList.get(position))) {
                holder.itemView.setSelected(true);
            } else {
                holder.itemView.setSelected(false);
            }
            tab.tvTab.setText(idList.get(position));
            if (editAble) {
                tab.ivClear.setVisibility(View.VISIBLE);
            } else {
                tab.ivClear.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (editAble ) {
            if (idList.size() == 0) {
                idList.add("");
            } else {
                if(!idList.get(idList.size() - 1).equals("")){
                    idList.add("");
                }
            }
        }else{
            if(idList.size()>0){
                if(idList.get(idList.size()-1).equals("")){
                    idList.remove(idList.size()-1);
                }
            }
        }
        return idList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (editAble) {
            if (idList.size() == 0) {
                return 2;
            } else if (idList.size() > 0) {
                if (position != idList.size() - 1) {
                    return 1;
                } else {
                    return 2;
                }
            }
        }
        return 1;
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setClearClickListener(ClearClickListener clearClickListener) {
        this.clearClickListener = clearClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(int position);
    }

    public interface ClearClickListener {
        void onClick(View view, int position);
    }

    class HolderTab extends RecyclerView.ViewHolder {
        TextView tvTab;
        ImageView ivClear;

        public HolderTab(View itemView) {
            super(itemView);
            tvTab = itemView.findViewById(R.id.tv_item_doc_cid);
            ivClear = itemView.findViewById(R.id.iv_item_doc_clear);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(getAdapterPosition());
                    }
                }
            });

            ivClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clearClickListener != null) {
                        clearClickListener.onClick(v, getAdapterPosition());
                    }
                }
            });
        }

    }

    class HolderAdd extends RecyclerView.ViewHolder {
        ImageView ivAdd;

        public HolderAdd(View itemView) {
            super(itemView);
            ivAdd = itemView.findViewById(R.id.iv_item_doc_add);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(getAdapterPosition());
                    }
                }
            });
        }
    }

}