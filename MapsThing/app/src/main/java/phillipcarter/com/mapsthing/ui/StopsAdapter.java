package phillipcarter.com.mapsthing.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import phillipcarter.com.mapsthing.R;
import phillipcarter.com.mapsthing.model.Stop;

public class StopsAdapter extends ArrayAdapter<Stop> {
    private List<Stop> mStops;
    private Context mContext;

    public StopsAdapter(Context context, List<Stop> stops) {
        super(context, R.layout.stop_layout);
        mContext = context;
        mStops = stops;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            rowView = inflater.inflate(R.layout.route_layout, null);

            holder = new ViewHolder();
            holder.stopName = (TextView) rowView.findViewById(R.id.route_name);

            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        holder.stopName.setText(mStops.get(position).Name);

        return rowView;
    }

    static class ViewHolder {
        TextView stopName;
    }
}
