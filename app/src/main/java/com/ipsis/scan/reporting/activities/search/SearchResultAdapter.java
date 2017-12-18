package com.ipsis.scan.reporting.activities.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.ipsis.scan.R;

import java.util.ArrayList;

/**
 * Created by pobouteau on 9/20/16.
 */

public class SearchResultAdapter extends ArrayAdapter<SearchResult> {
    public SearchResultAdapter(Context context, ArrayList<SearchResult> searchResults) {
        super(context, 0, searchResults);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_search_layout, parent, false);
        }

        SearchResultViewHolder viewHolder = (SearchResultViewHolder) convertView.getTag();
        if(viewHolder == null){
            viewHolder = new SearchResultViewHolder();
            viewHolder.titleTextView = (TextView) convertView.findViewById(R.id.rowResultTextView);
            viewHolder.iconTextView = (TextView) convertView.findViewById(R.id.iconTextView);
            convertView.setTag(viewHolder);
        }

        if (position < getCount()) {
            SearchResult searchResult = getItem(position);
            viewHolder.titleTextView.setText(searchResult.getTitle());
            viewHolder.iconTextView.setText(searchResult.getIcon());
        }

        return convertView;
    }

    private class SearchResultViewHolder {
        public TextView titleTextView;
        public TextView iconTextView;
    }
}
