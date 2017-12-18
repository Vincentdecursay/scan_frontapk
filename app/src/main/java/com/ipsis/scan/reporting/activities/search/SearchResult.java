package com.ipsis.scan.reporting.activities.search;

import com.ipsis.scan.reporting.edition.model.MissionLocation;

/**
 * Created by pobouteau on 9/20/16.
 */

public class SearchResult {
    private String title;
    private String icon;
    private Object tag;

    public SearchResult(MissionLocation title) {
        this.title = title.getLocation();
        this.icon = title.getLine();
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }
}
