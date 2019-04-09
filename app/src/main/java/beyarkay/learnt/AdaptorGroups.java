package beyarkay.learnt;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class AdaptorGroups extends ArrayAdapter<Group> {
    private static final String TAG = "AdaptorGroups";
    private final Context context;
    private final ArrayList<Group> groups;

    public AdaptorGroups(Context context, ArrayList<Group> groups) {
        super(context, R.layout.prefab_group_mini, groups);
        this.context = context;
        this.groups = groups;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Log.d(TAG, "groups.get(" + position + ")=" + groups.get(position));
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // TODO: 2018/02/18 follow the advice given by this website: https://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder

        View vGroup = inflater.inflate(R.layout.prefab_group_mini, parent, false);
        TextView tvTerm = vGroup.findViewById(R.id.tvTerm);
        tvTerm.setText(groups.get(position).getTerm());
        TextView tvDefinition = vGroup.findViewById(R.id.tvDefinition);
        tvDefinition.setText(groups.get(position).getDefinition());

        if (groups.get(position).getLearntState() == LearntDB.GroupsTable.LEARNT_FULLY) {
            tvTerm.setPaintFlags(tvTerm.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvTerm.setTextColor(context.getResources().getColor(R.color.text_greyed_out));

            tvDefinition.setPaintFlags(tvDefinition.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvDefinition.setTextColor(context.getResources().getColor(R.color.text_greyed_out));
        } else {
            tvTerm.setPaintFlags(tvTerm.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            tvTerm.setTextColor(context.getResources().getColor(R.color.text_normal));

            tvDefinition.setPaintFlags(tvDefinition.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            tvDefinition.setTextColor(context.getResources().getColor(R.color.text_normal));
        }
        return vGroup;
    }
}
