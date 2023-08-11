package com.example.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javamodel.CityTimeZone;
import com.example.javamodel.Helper;
import com.example.newclock.R;

import java.util.ArrayList;
import java.util.List;

public class TimeZonesAdapter extends RecyclerView.Adapter<TimeZonesAdapter.ViewHolder>implements Filterable {
    private List<CityTimeZone> filteredTimeZoneList;

    private List<CityTimeZone> cityTimeZoneList;

    private Context context;

    public TimeZonesAdapter(List<CityTimeZone> cityTimeZoneList, Context context) {
        this.filteredTimeZoneList = cityTimeZoneList;
        this.cityTimeZoneList = cityTimeZoneList;
        this.context = context;
    }

    @NonNull
    @Override
    public TimeZonesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_view_layout, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeZonesAdapter.ViewHolder holder, int position) {

        CityTimeZone cityTimeZone = filteredTimeZoneList.get(position);
        holder.cityName.setText(cityTimeZone.getName());
        holder.cityTime.setText(Helper.convertTimeZoneToTime(cityTimeZone.getName()));
        holder.checkBox.setChecked(cityTimeZone.isSelected());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( holder.checkBox.isChecked()){
                    cityTimeZone.setSelected(false);
                    holder.checkBox.setChecked(false);


                }else {
                    cityTimeZone.setSelected(true);
                    holder.checkBox.setChecked(true);
                }

            }
        });
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                if (checked){
                    cityTimeZone.setSelected(true);

                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return cityTimeZoneList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView cityName;
        public TextView cityTime;
        public CheckBox checkBox;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cityName = itemView.findViewById(R.id.city_text_view);
            cityTime = itemView.findViewById(R.id.time_text_view);
            checkBox = itemView.findViewById(R.id.check_box);

        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                ArrayList<CityTimeZone> filteredList = new ArrayList<>();

                if (constraint.toString().isEmpty()) {
                    filteredList.addAll(cityTimeZoneList);
                }

                if (constraint != null && constraint.length() > 0) {

                    for (int i = 0; i < cityTimeZoneList.size(); i++) {
                        if (cityTimeZoneList.get(i).getName().contains(constraint.toString())) {
                            filteredList.add(cityTimeZoneList.get(i));
                        }
                    }
                    filterResults.count = filteredList.size();
                    filterResults.values = filteredList;
                } else {
                    filterResults.count = cityTimeZoneList.size();
                    filterResults.values = cityTimeZoneList;
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredTimeZoneList.clear();

                filteredTimeZoneList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };

    }

}
