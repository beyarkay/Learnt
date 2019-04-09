package beyarkay.learnt;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class AdaptorSets extends RecyclerView.Adapter<AdaptorSets.SCViewHolder> {
    private static final String TAG = "AdaptorSets";
    public List<Set> sets;
    private DBHelper db;
    private Context context;
    private Animation animShow, animHide;


    public AdaptorSets(List<Set> sets, DBHelper db, Context context) {
        this.sets = sets;
        this.db = db;
        this.context = context;
        animShow = AnimationUtils.loadAnimation(context, R.anim.slide_down_to_show);
        animHide = AnimationUtils.loadAnimation(context, R.anim.slide_up_to_hide);
    }

    @Override
    public int getItemCount() {
        return sets.size();
    }

    @Override
    public void onBindViewHolder(SCViewHolder scvHolder, int position) {
        Set set = sets.get(position);
        scvHolder.set = set;
        scvHolder.tvSetTitle.setText(set.getTitle());
        scvHolder.etSetTitle.setText(set.getTitle());
        scvHolder.tvTermTitle.setText(set.getTermTitle());
        scvHolder.tvDefinitionTitle.setText(set.getDefinitionTitle());
        scvHolder.tvSetCount.setText(String.valueOf(set.getCount(db)));
//        scvHolder.toggle.setChecked(set.getActivity() == Set.ACTIVITY_ACTIVE);
        scvHolder.updateActivity();
        ArrayList<Group> groups = set.getGroups(db);

        //Making sure the ListView has the correct height for it's items
        int numberOfItems = groups.size();
        if (numberOfItems > 0) {
            scvHolder.tvTermTitle.setText(set.getTermTitle());
            scvHolder.tvTermTitle.setPadding(0, 0, 0, 0);
            scvHolder.tvDefinitionTitle.setText(set.getDefinitionTitle());
            scvHolder.llTandDs.removeAllViews();
            for (Group g : groups) {
                scvHolder.llTandDs.addView(createGroupView(g, scvHolder.llTandDs));
            }
        } else {
            scvHolder.tvTermTitle.setText(R.string.empty_set_error_string);
            scvHolder.tvTermTitle.setPadding(0, 4, 0, 4);
            scvHolder.tvDefinitionTitle.setText(R.string.empty_string);
        }

        scvHolder.updateIsSelected();
        scvHolder.updateState();
    }

    public class SCViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public Set set;
        CardView cardView;
        TextView tvSetTitle;
        EditText etSetTitle;
        Switch toggle;
        TextView tvTermTitle;
        TextView tvDefinitionTitle;
        RelativeLayout rlCardView;
        Space space;
        TextView tvSetCount;
        LinearLayout llTandDs;
        ImageView ivCaret;
        long lastLongClick = 0;


        public SCViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            cardView = itemView.findViewById(R.id.cardView);
            tvSetTitle = itemView.findViewById(R.id.tvSetTitle);
            etSetTitle = itemView.findViewById(R.id.etSetTitle);
            toggle = itemView.findViewById(R.id.toggle);
            tvTermTitle = itemView.findViewById(R.id.tvTermTitle);
            tvDefinitionTitle = itemView.findViewById(R.id.tvDefinitionTitle);
            rlCardView = itemView.findViewById(R.id.rlCardView);
            space = itemView.findViewById(R.id.space_centre);
            tvSetCount = itemView.findViewById(R.id.tvSetCount);
            llTandDs = itemView.findViewById(R.id.llTandD);
            ivCaret = itemView.findViewById(R.id.ivCaret);

            View.OnClickListener caretClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // toggles between Set.STATE_EXPANDED and Set.STATE_CONTRACTED
                    if (set.getState() != Set.STATE_ENTER_TITLE) {
                        set.setState(set.getState() % 2 + 1);
                        updateState();
                    }
                }
            };
            ivCaret.setOnClickListener(caretClickListener);
            tvSetCount.setOnClickListener(caretClickListener);
            toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    set.setActivity(isChecked ? Set.ACTIVITY_ACTIVE : Set.ACTIVITY_INACTIVE);
                    db.updateSet(set, set.getId());
                    ((ActivityMacroView) context).triggerNotifications();
                }
            });
        }

        public void updateState() {
            if (set.getState() == Set.STATE_ENTER_TITLE) {
                etSetTitle.setVisibility(VISIBLE);
                tvSetTitle.setVisibility(INVISIBLE);
                toggle.setEnabled(false);

                ivCaret.setImageAlpha(0);
                tvSetCount.setTextColor(context.getResources().getColor(R.color.grey_D));

                tvTermTitle.setVisibility(GONE);
                tvDefinitionTitle.setVisibility(GONE);
                llTandDs.setVisibility(GONE);
            } else if (set.getState() == Set.STATE_COLLAPSED) {
                llTandDs.setVisibility(GONE);
                tvDefinitionTitle.setVisibility(GONE);
                tvTermTitle.setVisibility(GONE);

                ivCaret.setImageAlpha(255);
                tvSetCount.setTextColor(context.getResources().getColor(R.color.grey_F));

                space.setVisibility(GONE);
                etSetTitle.setVisibility(GONE);
                tvSetTitle.setVisibility(VISIBLE);
                toggle.setEnabled(true);

            } else if (set.getState() == Set.STATE_EXPANDED) {
                llTandDs.setVisibility(VISIBLE);
                tvTermTitle.setVisibility(VISIBLE);
                tvDefinitionTitle.setVisibility(VISIBLE);

//                ivCaret.setImageAlpha(0);
                tvSetCount.setTextColor(context.getResources().getColor(R.color.grey_F));

                space.setVisibility(VISIBLE);
                etSetTitle.setVisibility(GONE);
                tvSetTitle.setVisibility(VISIBLE);
                toggle.setEnabled(true);
            }
        }

        public void updateActivity() {
            toggle.setChecked(set.getActivity() == Set.ACTIVITY_ACTIVE);
//            ((ActivityMacroView) context).triggerNotifications();
        }

        public void updateIsSelected() {
            Context context = cardView.getContext();
            if (set.isSelected()) {
                tvSetCount.setBackgroundColor(context.getResources().getColor(R.color.grey_C));
                ivCaret.setBackgroundColor(context.getResources().getColor(R.color.grey_C));
                rlCardView.setBackgroundColor(context.getResources().getColor(R.color.grey_D));
                tvTermTitle.setTextColor(context.getResources().getColor(R.color.secondary));
                tvDefinitionTitle.setTextColor(context.getResources().getColor(R.color.secondary));
            } else {
                tvSetCount.setBackgroundColor(context.getResources().getColor(R.color.grey_D));
                ivCaret.setBackgroundColor(context.getResources().getColor(R.color.grey_D));
                rlCardView.setBackgroundColor(context.getResources().getColor(R.color.transparent));
                tvTermTitle.setTextColor(context.getResources().getColor(R.color.text_normal));
                tvDefinitionTitle.setTextColor(context.getResources().getColor(R.color.text_normal));
            }
        }

        @Override
        public void onClick(View view) {
//            Log.d(TAG, "onClick: of view=" + view.toString());

            if (((ActivityMacroView) context).selectedSetCards > 0) {
                onLongClick(view);
            } else {
                Intent iMicroViewActivity = new Intent(context, ActivityMicroView.class);
                iMicroViewActivity.putExtra(String.valueOf(R.id.TAG_SET_ID), set.getId());
                context.startActivity(iMicroViewActivity);
            }
        }

        @Override
        public boolean onLongClick(View view) {
//            Log.d(TAG, "onLongClick: of view=" + view.toString());
            set.setSelected(!set.isSelected());
            if (set.isSelected()) {
                ((ActivityMacroView) context).selectedSetCards++;
            } else {
                ((ActivityMacroView) context).selectedSetCards--;
            }
            ((ActivityMacroView) context).updateSelectedSets();

            updateIsSelected();
            lastLongClick = System.currentTimeMillis();
            return true;
        }
    }

    @Override
    public SCViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.prefab_set_card, viewGroup, false);
        return new SCViewHolder(itemView);
    }

    public void addAt(int position, Set set) {
        sets.add(position, set);
        notifyItemInserted(position);
    }

    public void removeAt(int position) {
        sets.remove(position);
        notifyItemRemoved(position);
//        Log.d(TAG, "AdaptorSets.UI_Sets.sizer()=" + sets.size());

    }

    private View createGroupView(Group g, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View vGroup = inflater.inflate(R.layout.prefab_group_mini, parent, false);
        TextView tvTerm = vGroup.findViewById(R.id.tvTerm);
        tvTerm.setText(g.getTerm());
        TextView tvDefinition = vGroup.findViewById(R.id.tvDefinition);
        tvDefinition.setText(g.getDefinition());
        if (g.getLearntState() == LearntDB.GroupsTable.LEARNT_FULLY) {
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