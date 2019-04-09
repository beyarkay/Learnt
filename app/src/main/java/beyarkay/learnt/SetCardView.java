package beyarkay.learnt;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

public class SetCardView extends CardView {
    @Deprecated
    String title;
    @Deprecated
    long setId;
    CardView cv;
    ImageButton imgbtnExpandSet;
    TextView tvSetTitle;
    EditText etSetTitle;
    Switch toggle;
    TextView tvTermTitle;
    TextView tvDefinitionTitle;
    ListView lvTerms;
    ListView lvDefinitions;
    ImageView ivCaret;
    Set set;


    DBHelper db;

    public SetCardView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SetCardView,
                0, 0);


        LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        li.inflate(R.layout.prefab_set_card, this);


        db = new DBHelper(getContext());

        setSetId(a.getInt(R.styleable.SetCardView_setId, -1));
        if (getSet().getId() == -1) {
            setTitle(a.getString(R.styleable.SetCardView_title));
            setIsSelected(a.getBoolean(R.styleable.SetCardView_isSelected, false));
            setActivity(a.getInt(R.styleable.SetCardView_activation, 0));
            setState(a.getInt(R.styleable.SetCardView_state, 1));
        }
        a.recycle();
        cv = findViewById(R.id.cardView);
        imgbtnExpandSet = findViewById(R.id.imgbtnExpandSet);
        tvSetTitle = findViewById(R.id.tvSetTitle);
        etSetTitle = findViewById(R.id.etSetTitle);
        toggle = findViewById(R.id.toggle);
        tvTermTitle = findViewById(R.id.tvTermTitle);
        tvDefinitionTitle = findViewById(R.id.tvDefinitionTitle);
        ivCaret = findViewById(R.id.ivCaret);
        TextView previous;

        ArrayList<Group> groups = set.getGroups(db);
        for (int i = 0; i < groups.size(); i++) {
            addGroupToLVs(groups.get(i), lvTerms.getChildCount() - 1);
        }
    }

    @Deprecated
    public long getSetId() {
        return setId;
    }

    @Deprecated
    public String getTitle() {
        return title;
    }

    public void setSetId(long setId) {
        this.setId = setId;
        this.set = db.getSet(setId);

        this.setTitle(set.getTitle());
        this.setActivity(set.getActivity());
        this.setIsSelected(false);
        this.setState(1);
        lvTerms.removeAllViews();
        lvDefinitions.removeAllViews();
        ArrayList<Group> groups = db.getGroupsOfSet(setId);
        for (Group g : groups) {
            lvTerms.addView(createTV(g.getTerm(), g.getLearntState() == LearntDB.GroupsTable.LEARNT_FULLY));
            lvDefinitions.addView(createTV(g.getDefinition(), g.getLearntState() == LearntDB.GroupsTable.LEARNT_FULLY));
        }
        invalidate();
        requestLayout();
    }

    //Misc. with UI
    public void setTitle(String title) {
        this.title = title + "(" + set.getCount(db) + ")";
        set.setTitle(title);

        db.updateSet(set, set.getId());
        invalidate();
        requestLayout();
    }

    public void setIsSelected(boolean is_selected) {
        set.setSelected(is_selected);

        findViewById(R.id.rvSetsHolder).setBackgroundColor(getResources().getColor(
                set.isSelected() ? R.color.grey_D : R.color.grey_F));
        ((TextView) findViewById(R.id.tvTermTitle)).setTextColor(getResources().getColor(
                set.isSelected() ? R.color.secondary_dark : R.color.text_normal));
        ((TextView) findViewById(R.id.tvDefinitionTitle)).setTextColor(getResources().getColor(
                set.isSelected() ? R.color.secondary_dark : R.color.text_normal));

        for (int i = 0; i < lvTerms.getChildCount(); i++) {
            ((TextView) lvTerms.getChildAt(i)).setTextColor(getResources().getColor(
                    set.isSelected() ? R.color.secondary_dark : R.color.text_normal));
            ((TextView) lvDefinitions.getChildAt(i)).setTextColor(getResources().getColor(
                    set.isSelected() ? R.color.secondary_dark : R.color.text_normal));
        }
        invalidate();
        requestLayout();
    }

    public void setActivity(int activity) {
        set.setActivity(activity);
        toggle.setChecked(activity == Set.ACTIVITY_ACTIVE);
        invalidate();
        requestLayout();
    }

    public void setState(int state) {
        set.setState(state);

        if (set.getState() == Set.STATE_ENTER_TITLE) {
            etSetTitle.setVisibility(VISIBLE);
            tvSetTitle.setVisibility(GONE);
            toggle.setVisibility(INVISIBLE);
        } else if (set.getState() == Set.STATE_COLLAPSED) {
            lvTerms.setVisibility(GONE);
            tvTermTitle.setVisibility(GONE);

            lvDefinitions.setVisibility(GONE);
            tvDefinitionTitle.setVisibility(GONE);

            ivCaret.setImageResource(R.drawable.ic_expand_less_white_24dp);
            findViewById(R.id.space_centre).setVisibility(GONE);
            etSetTitle.setVisibility(GONE);
            tvSetTitle.setVisibility(VISIBLE);
            toggle.setVisibility(VISIBLE);

        } else if (set.getState() == Set.STATE_EXPANDED) {
            tvDefinitionTitle.setVisibility(VISIBLE);
            lvDefinitions.setVisibility(VISIBLE);

            tvTermTitle.setVisibility(VISIBLE);
            lvTerms.setVisibility(VISIBLE);

            ivCaret.setImageResource(R.drawable.ic_expand_more_white_24dp);
            findViewById(R.id.space_centre).setVisibility(VISIBLE);
            etSetTitle.setVisibility(GONE);
            tvSetTitle.setVisibility(VISIBLE);
            toggle.setVisibility(VISIBLE);

        }
        invalidate();
        requestLayout();
    }

    public Set getSet() {
        return set;
    }

    private TextView createTV(String text, boolean isGrey) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTextColor(isGrey ? getResources().getColor(R.color.text_greyed_out) : getResources().getColor(R.color.text_normal));
        return tv;
    }

    private void addGroupToLVs(Group g, int position) {
        // TODO: 2018/02/10 probably could optimise this
        TextView tvTerm = new TextView(getContext());
        tvTerm.setTextColor(getResources().getColor(R.color.text_normal));
        tvTerm.setTypeface(tvTerm.getTypeface(), Typeface.BOLD);
        tvTerm.setText(g.getTerm());
        lvTerms.addView(tvTerm, position);

        TextView tvDefinition = new TextView(getContext());
        tvDefinition.setTextColor(getResources().getColor(R.color.text_normal));
        tvDefinition.setTypeface(tvDefinition.getTypeface(), Typeface.BOLD);
        tvDefinition.setText(g.getTerm());
        lvDefinitions.addView(tvDefinition, position);
    }

}
