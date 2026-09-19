package com.example.smartpantrymanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PantryAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private final List<PantryItem> items = new ArrayList<>();

    public PantryAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    // Replace the displayed items after reading the database.
    public void setItems(List<PantryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public PantryItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView,
                        ViewGroup parent) {

        // Reuse an existing row where possible.
        if (convertView == null) {
            convertView = inflater.inflate(
                    android.R.layout.simple_list_item_2,
                    parent,
                    false);
        }

        TextView nameText = convertView.findViewById(
                android.R.id.text1);
        TextView quantityText = convertView.findViewById(
                android.R.id.text2);

        PantryItem item = getItem(position);

        // Display 500 instead of 500.0, while preserving decimals.
        String amount = BigDecimal.valueOf(item.getQuantity())
                .stripTrailingZeros()
                .toPlainString();

        nameText.setText(item.getName());
        quantityText.setText(amount + " " + item.getUnit());

        return convertView;
    }
}
