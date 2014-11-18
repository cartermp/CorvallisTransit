package phillipcarter.com.mapsthing.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import phillipcarter.com.mapsthing.R;
import phillipcarter.com.mapsthing.model.Route;

public class RoutesAdapter extends ArrayAdapter<Route> {
    private List<Route> mRoutes;
    private Context mContext;

    public RoutesAdapter(Context context, List<Route> routes) {
        super(context, R.layout.route_layout, routes);
        mContext = context;
        mRoutes = routes;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            rowView = inflater.inflate(R.layout.route_layout, null);

            holder = new ViewHolder();
            holder.routeName = (TextView) rowView.findViewById(R.id.route_name);
            holder.routeFullName = (TextView) rowView.findViewById(R.id.route_full_name);

            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        holder.routeName.setText(mRoutes.get(position).Name);
        holder.routeFullName.setText(mRoutes.get(position).AdditionalName);

        return rowView;
    }

    static class ViewHolder {
        TextView routeName;
        TextView routeFullName;
    }
}
