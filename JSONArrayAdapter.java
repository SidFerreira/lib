package com.ferreiraz.lib;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sidferreira on 24/02/14.
 */
public class JSONArrayAdapter extends BaseAdapter {

    private Context context;
    protected LayoutInflater layoutInflater;
    protected int layoutResource;
    private JSONArray objects = new JSONArray();
    private int mDropDownResource;
    protected int mainFieldInLayout;
    protected String propertyForMainField = "";

    public JSONArrayAdapter(Context context, int resource) {
        init(context, resource, 0, new JSONArray(), "");
    }

    public JSONArrayAdapter(Context context, int resource, int textViewResourceId) {
        init(context, resource, textViewResourceId, new JSONArray(), "");
    }

    public JSONArrayAdapter(Context context, int resource, int textViewResourceId, JSONArray objects) {
        init(context, resource, textViewResourceId, objects, "");
    }

    public JSONArrayAdapter(Context context, int resource, int textViewResourceId, JSONArray objects, String property) {
        init(context, resource, textViewResourceId, objects, property);
    }

    private void init(Context context, int resource, int textViewResourceId, JSONArray _objects, String property) {
        this.context = context;
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = mDropDownResource = resource;
        objects = _objects;
        mainFieldInLayout = textViewResourceId;
        propertyForMainField = property;
    }

    @Override
    public int getCount() {
        return objects.length();
    }

    @Override
    public JSONObject getItem(int position) {
        try {
            return objects.getJSONObject(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, layoutResource);
    }

    private View createViewFromResource(int position, View convertView, ViewGroup parent,
                                        int resource) {
        View view;
        TextView text;

        if (convertView == null) {
            view = layoutInflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }

        try {
            if (mainFieldInLayout == 0) {
                //  If no custom field is assigned, assume the whole resource is a TextView
                text = (TextView) view;
            } else {
                //  Otherwise, find the TextView field within the layout
                text = (TextView) view.findViewById(mainFieldInLayout);
            }
        } catch (ClassCastException e) {
            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e);
        }

        JSONObject item = getItem(position);

        String itemText = item.toString();
        try {
            if(!propertyForMainField.equals("")) {
                itemText = item.getString(propertyForMainField);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        text.setText(itemText);
        return view;
    }
}
