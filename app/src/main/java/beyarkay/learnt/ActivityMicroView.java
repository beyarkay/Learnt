package beyarkay.learnt;

// TODO: 2017/08/01 Note about the duplication pair bug - it is only the text fields being refilled witht he same text, not the entire thing.
// TODO: 2017/07/29 Strikethrough text when the group is learnt fully

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ActivityMicroView extends AppCompatActivity {
    LinearLayout groupHolder;
    TextView termTitleET;
    TextView definitionTitleET;
    DBHelper db;
    Set currSet;
    ArrayList<Group> groups = new ArrayList<>();
    String title_type;
    long focusedGroupId;
    Group delGroup;
    int delGroupIndex;
    private UpdateGroupsReceiver receiver;
    SharedPreferences sharedPref;
    long timer = 0;
    String timerName = "";
    private boolean shouldUpdate;

    //Lifecycle Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log("OnCreate of MicroViewer");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_micro_view);
        groupHolder = findViewById(R.id.groupHolder);
        receiver = new UpdateGroupsReceiver(this);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        db = new DBHelper(this);

        termTitleET = findViewById(R.id.term_title_TV);
        termTitleET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTitleClick("Term");
            }
        });
        definitionTitleET = findViewById(R.id.definition_title_TV);
        definitionTitleET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTitleClick("Definition");
            }
        });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        updateDynamicsFromDB();
        setTitle(currSet.getTitle());
        loadUIFromDynamics(15);
        shouldUpdate = false;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStop() {
        View focusedView = groupHolder.getFocusedChild();
        if (focusedView != null) {
            focusedView.clearFocus();
        }
        startTimer("db.updateSetAndItsGroups");
        db.updateSetAndItsGroups(currSet, currSet.getId(), groups);
        stopTimer();
        startSendingNotifications();
        super.onStop();
    }

    @Override
    public void onPause() {
        //TODO maybe need to write to DB here?
        db.updateSetAndItsGroups(currSet, currSet.getId(), groups);
        unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (shouldUpdate) {
            log("gH.gCC()=" + groupHolder.getChildCount());
            updateDynamicsFromDB();
            updateUIFromDynamics(true);
            shouldUpdate = true;
        }
        String groupDeletedI = getString(R.string.group_deleted_intent);
        IntentFilter filter = new IntentFilter();
        filter.addAction(groupDeletedI);
        registerReceiver(receiver, filter);
        // FIXME: 2017/07/02 NPE with .findViewById
        if (groupHolder.getChildCount() > 0) {
            groupHolder.getChildAt(groupHolder.getChildCount() - 1).findViewById(R.id.etTerm).requestFocus();
        }
        showSoftInput(groupHolder);
//        updateHelperText();
        super.onResume();
    }

    //Options Menu or Actionbar Methods
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
//            case R.id.action_temp:
//                db.logAll();
            case R.id.action_settings:
//                startActivity(new Intent(this, ActivitySettings.class));
                Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_micro, menu); // This adds items to the action bar if the action bar is present.


        return true; // Return true to display menu
    }

    //CRUD methods for the GroupViews
    public void onTitleClick(String title) {
        title_type = title;
        AlertDialog.Builder changeTitleDialog = new AlertDialog.Builder(this);
        final EditText et = new EditText(this);
        et.setMaxLines(1);
        et.setSelectAllOnFocus(true);
        showSoftInput(et);
        if (title_type.equals("Term")) {
            et.setText(currSet.getTermTitle());
        } else if (title_type.equals("Definition")) {
            et.setText(currSet.getDefinitionTitle());
        }
        changeTitleDialog.setTitle("Enter a new " + title_type + " Title");
        changeTitleDialog.setView(et, 50, 0, 50, 5);

        changeTitleDialog.setPositiveButton("Change " + title_type + " Title", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (title_type.equals("Term")) {
                    currSet.setTermTitle(et.getText().toString());
                    termTitleET.setText(currSet.getTermTitle());
                } else if (title_type.equals("Definition")) {
                    currSet.setDefinitionTitle(et.getText().toString());
                    definitionTitleET.setText(currSet.getDefinitionTitle());
                }
            }
        });
        changeTitleDialog.show();
        et.requestFocus();
    }

    public void deleteGroupView(long id) {
        delGroup = db.getGroup(id);
        for (int i = 0, groupsSize = groups.size(); i < groupsSize; i++) {
            if (groups.get(i).getId() == id) {
                delGroupIndex = i;
                groupHolder.removeViewAt(i);
                groups.remove(i);
                break;
            }
        }
        db.removeGroup(id);
        Snackbar snackbar = Snackbar
                .make(findViewById(android.R.id.content), "Pair Deleted", Snackbar.LENGTH_LONG)
                .setActionTextColor(getResources().getColor(R.color.secondary_light))
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {        //the user made a mistake
                        if (delGroupIndex < 0) {
                            delGroupIndex = groupHolder.getChildCount() + delGroupIndex;
                        }
                        //Recreate the deleted group
                        delGroup.setId(db.addGroup(delGroup));
                        groups.add(delGroupIndex, delGroup);
                        groupHolder.addView(createGroupView(delGroup), delGroupIndex);
                        delGroupIndex = -1;
                        delGroup = null;
//                        updateHelperText();
                    }
                });
        snackbar.show();
//        updateHelperText();
    }

    public void deleteGroupView(View button) {
        deleteGroupView((long) button.getTag(R.id.TAG_GROUP_ID));
    }

    public View createGroupView(final Group group) {
        View groupView = getLayoutInflater().inflate(R.layout.prefab_group_list_item, null);

        final EditText termET = groupView.findViewById(R.id.etTerm);
        final EditText definitionET = groupView.findViewById(R.id.etDefinition);
        ImageButton delButton = groupView.findViewById(R.id.btnDelete);
        ImageView learntStatus = groupView.findViewById(R.id.learnt_status);

        groupView.setTag(R.id.TAG_GROUP_ID, group.getId());

        //Sorting out termET
        termET.setTag(R.id.TAG_GROUP_ID, group.getId());
        termET.setText(group.getTerm());
        termET.setHint(db.getSet(group.getSetId()).getTermTitle());
        termET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    ActivityMicroView.this.onFocusChange(termET, true);
                }
            }
        });

        //Sorting out definitionET
        definitionET.setTag(R.id.TAG_GROUP_ID, group.getId());
        definitionET.setText(group.getDefinition());
        definitionET.setHint(db.getSet(group.getSetId()).getDefinitionTitle());
        definitionET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    ActivityMicroView.this.onFocusChange(definitionET, false);
                }
            }
        });

        //sorting out the delete button
        delButton.setTag(R.id.TAG_GROUP_ID, group.getId());
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteGroupView(view);
            }
        });

        //sorting out the learnt status ImageView
        learntStatus.setTag(R.id.TAG_GROUP_ID, group.getId());
        setLearntStatus(groupView, group.getLearntState());
        return groupView;
    }

    public void addGroupFabClicked(View button) {   //Called when "ADD NEW PAIR" FAB clicked
        Group g = new Group("", "", currSet.getId());
        g.setId(db.addGroup(g));        //added to the DB
        groups.add(g);                  //added to the dynamic
        groupHolder.addView(createGroupView(g)); //added to the UI
        EditText focusedET = groupHolder.getChildAt(groupHolder.getChildCount() - 1).findViewById(R.id.etTerm);
//        focusedET.requestFocusFromTouch();
        showSoftInput(focusedET);
//        updateHelperText();
    }

    //Relating to the views
    private void onFocusChange(EditText termOrDefinition, boolean isTerm) {
        // FIXME: 2017/07/02 problem found. I thnk the db is changing the groups IDs, possibly when deleting all the groups and adding them again?
        for (Group g : groups) {
            if (g.getId() == (long) termOrDefinition.getTag(R.id.TAG_GROUP_ID)) {
                if (isTerm) {
                    g.setTerm(termOrDefinition.getText().toString());
                } else {
                    g.setDefinition(termOrDefinition.getText().toString());
                }
                if (db.getGroup(g.getId()) != null) {
                    //Check the g.getId() at this point
                    db.updateGroup(g, g.getId());
                }
                break;
            }
        }
    }

    public void showSoftInput(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public ArrayList<Group> getUniqueGroups(ArrayList<Group> keepAll, ArrayList<Group> withDuplicates) {
        ArrayList<Group> unique = new ArrayList<>();
        int count;
        for (Group group1 : keepAll) {
            count = 0;
            for (Group group2 : withDuplicates) {
                if (group1.getId() != group2.getId()) {
                    count++;
                }
            }
            if (count == withDuplicates.size()) {
                unique.add(group1);
            }
        }
        return unique;
    }

    //adjusting the layout
    private void setLearntStatus(View groupView, int learntStatus) {
        ImageView learntStatusIV = groupView.findViewById(R.id.learnt_status);
        learntStatusIV.setBackground(getResources().getDrawable(R.drawable.grey_d_ripple));
        final EditText termET = groupView.findViewById(R.id.etTerm);
        final EditText definitionET = groupView.findViewById(R.id.etDefinition);
        ImageButton delButton = groupView.findViewById(R.id.btnDelete);

        if (learntStatus == LearntDB.GroupsTable.LEARNT_FULLY) {
            learntStatusIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_learnt_status_fully_learnt));
            definitionET.setTextColor(getResources().getColor(R.color.grey_A));
            termET.setTextColor(getResources().getColor(R.color.grey_A));


            definitionET.getBackground().setColorFilter(getResources().getColor(R.color.grey_A), PorterDuff.Mode.SRC_IN);
            termET.getBackground().setColorFilter(getResources().getColor(R.color.grey_A), PorterDuff.Mode.SRC_IN);
            delButton.setBackgroundColor(getResources().getColor(R.color.grey_C));
        } else {
            definitionET.setTextColor(getResources().getColor(R.color.grey_0));
            termET.setTextColor(getResources().getColor(R.color.grey_0));
            delButton.setBackgroundColor(getResources().getColor(R.color.grey_A));
            definitionET.getBackground().clearColorFilter();
            termET.getBackground().clearColorFilter();
            if (learntStatus == LearntDB.GroupsTable.LEARNT_FORWARDS) {
                learntStatusIV.setImageDrawable(getResources().getDrawable(
                        R.drawable.ic_learnt_status_forwards));
            } else if (learntStatus == LearntDB.GroupsTable.LEARNT_BACKWARDS) {
                learntStatusIV.setImageDrawable(getResources().getDrawable(
                        R.drawable.ic_learnt_status_backwards));
            } else if (learntStatus == LearntDB.GroupsTable.LEARNT_NONE) {
                learntStatusIV.setImageDrawable(getResources().getDrawable(
                        R.drawable.ic_learnt_status_none));
            }
        }
    }

    public void onLearntStatusClick(View view) {
        Group g = db.getGroup((long) view.getTag(R.id.TAG_GROUP_ID));
        if (g.getLearntState() == 0) {
            g.setLearntState(3);
        } else {
            g.setLearntState(g.getLearntState() - 1);
        }
        db.updateGroup(g, g.getId());
        updateDynamicsFromDB();
        setLearntStatus((View) view.getParent(), g.getLearntState());
    }

    private boolean requestFocusFor(long focusedGroupId, String termVSdefinition, boolean shouldOpenKeyboard) {
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).getId() == focusedGroupId) {
                groupHolder.getChildAt(i).findViewById(termVSdefinition.equals("term") ? R.id.etTerm : R.id.etDefinition).requestFocus();
                if (shouldOpenKeyboard) {
                    showSoftInput(groupHolder);
//                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                }
                return true;
            }
        }
        return false;
    }

    public void onLoadGroupsButtonClick(View view) {
        //todo addAt back the progress bar
//        ProgressBar pb = (ProgressBar) findViewById(R.id.loading_groups);
        TextView tv = findViewById(R.id.loadGroupsButton);
//        pb.setVisibility(View.VISIBLE);
        tv.setVisibility(View.GONE);

        updateUIFromDynamics(false);
//        pb.setVisibility(View.GONE);
    }

    //Updating automatically
    public void updateDynamicsFromDB() {

//        UpdateDynamicsFromDB task = new UpdateDynamicsFromDB();
//        task.execute();
        startTimer("UpdateDynamicsFromDB");
        if (getIntent().hasExtra(String.valueOf(R.id.TAG_GROUP_ID))) {
            focusedGroupId = getIntent().getLongExtra(String.valueOf(R.id.TAG_GROUP_ID), -1);
            currSet = db.getSet(db.getGroup(focusedGroupId).getSetId());
        } else if (getIntent().hasExtra(String.valueOf(R.id.TAG_SET_ID))) {
            currSet = db.getSet(getIntent().getLongExtra(String.valueOf(R.id.TAG_SET_ID), -1));
        } else {
            throw new NullPointerException();
        }
        groups = db.getGroupsOfSet(currSet.getId());
        if (currSet == null || groups == null) {
            throw new NullPointerException();
        }
        requestFocusFor(focusedGroupId, "term", false);
        stopTimer();
    }

    public void updateUIFromDynamics(boolean updateSimply) {
        startTimer("UIFromDynamics");
        if (updateSimply) {
            setTitle(currSet.getTitle());                    //filling with the Set data
            termTitleET.setText(currSet.getTermTitle());
            definitionTitleET.setText(currSet.getDefinitionTitle());

            for (int i = 0; i < Math.max(groupHolder.getChildCount(), groups.size()); i++) {
                if (groupHolder.getChildCount() < groups.size() && groupHolder.getChildCount() <= i) {
                    groupHolder.addView(createGroupView(groups.get(i)));
                } else if (groups.size() < groupHolder.getChildCount() && groups.size() <= i) {
                    groupHolder.removeViews(i, groupHolder.getChildCount() - groups.size());
                    break;
                } else {
                    View loopGroup = groupHolder.getChildAt(i);
                    Group arrGroup = groups.get(i);
                    if ((long) loopGroup.getTag(R.id.TAG_GROUP_ID) != arrGroup.getId()) {
                        groupHolder.removeViewAt(i);
                        groupHolder.addView(createGroupView(arrGroup));
//                        groupHolder.getChildAt(i).setTag(R.id.TAG_GROUP_ID, groups.get(i).getId());
//                        ((EditText) groupHolder.getChildAt(i).findViewById(R.id.definitionET)).setText(arrGroup.getDefinition());
//                        ((EditText) groupHolder.getChildAt(i).findViewById(R.id.termET)).setText(arrGroup.getTerm());
//                        setLearntStatus(groupHolder.getChildAt(i).findViewById(R.id.learnt_status), arrGroup.getLearntState());
                    }
                }
            }
        } else {
//        if (groupHolder.getChildCount() == 0) {   //this happens each time the app is restarted
//            for (Group group : groupsForNotification) {
//                groupHolder.addView(createGroupView(group));
//            }
//        } else {
            //first get the groupsForNotification into an ArrayList so we can work with it
            ArrayList<Group> UIGroups = new ArrayList<>();
            //Load up UIGroups with all the data
            for (int i = 0; i < groupHolder.getChildCount(); i++) {
                View currGroupView = groupHolder.getChildAt(i);
                Group group = new Group(
                        ((EditText) currGroupView.findViewById(R.id.etTerm)).getText().toString(),
                        ((EditText) currGroupView.findViewById(R.id.etTerm)).getText().toString(),
                        currSet.getId());
//fixme none of these ifs pass through, all are passed over without execution

                Drawable learntStatusDrawable = ((ImageView) currGroupView.findViewById(R.id.learnt_status)).getDrawable();
                if (learntStatusDrawable.equals(getResources().getDrawable(R.drawable.ic_learnt_status_fully_learnt))) {
                    group.setLearntState(LearntDB.GroupsTable.LEARNT_FULLY);
                } else if (learntStatusDrawable.equals(getResources().getDrawable(R.drawable.ic_learnt_status_backwards))) {
                    group.setLearntState(LearntDB.GroupsTable.LEARNT_BACKWARDS);
                } else if (learntStatusDrawable.equals(getResources().getDrawable(R.drawable.ic_learnt_status_forwards))) {
                    group.setLearntState(LearntDB.GroupsTable.LEARNT_FORWARDS);
                } else if (learntStatusDrawable.equals(getResources().getDrawable(R.drawable.ic_learnt_status_none))) {
                    group.setLearntState(LearntDB.GroupsTable.LEARNT_NONE);
                }

                group.setId((long) currGroupView.getTag(R.id.TAG_GROUP_ID));
                UIGroups.add(group);
            }
            //Add some groupsForNotification
            ArrayList<Group> groupsToAdd = getUniqueGroups(groups, UIGroups);
            for (Group group : groupsToAdd) {
                groupHolder.addView(createGroupView(group));
            }
//        if (UIGroups.size() > 0) {
            //Remove the groupsForNotification no longer included in the groupsForNotification arrayList
            ArrayList<Group> toDelete = getUniqueGroups(UIGroups, groups);
            boolean[] indexes = new boolean[UIGroups.size()];

            for (int i = 0; i < UIGroups.size(); i++) {
                for (Group delGroup : toDelete) {
                    if (UIGroups.get(i).getId() == delGroup.getId()) {
                        indexes[i] = true;
                    }
                }
            }
            for (int i = UIGroups.size() - 1; i > -1; i--) {
                if (indexes[i]) {
                    groupHolder.removeViewAt(i);
                }
            }
        }
//        }
//        }
        stopTimer();
    }

    private void loadUIFromDynamics(int numberToLoad) {
        if (groupHolder.getChildCount() == 0) {
            for (int i = 0; i < groups.size() && i < numberToLoad; i++) {
                groupHolder.addView(createGroupView(groups.get(i)));
            }
        }
        if (groups.size() <= numberToLoad || groups.size() == 0) {
            findViewById(R.id.loadGroupsButton).setVisibility(View.GONE);
        }
    }

    private void startSendingNotifications() {
        Intent sendNotificationI = new Intent(this, NotificationTimeRxr.class);
//        sendNotificationI.putExtra("set_id", currSet.getId());
        PendingIntent sendNotificationPI = PendingIntent.getBroadcast(this, 2, sendNotificationI, PendingIntent.FLAG_UPDATE_CURRENT);
        String frequency = sharedPref.getString("pref_notification_frequency", "0");
        if (!frequency.equals("0")) {
            //set up an alarmManager to do a recurring notification
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (frequency.equals("-1")) {
                //if the user says 'never'(-1) then we want to check every minute if they have changed their minds
                frequency = "60";
                log("User doesn't want a notification");
            }
            int frequency_millis = Integer.parseInt(frequency) * 1000;
            am.setRepeating(AlarmManager.RTC_WAKEUP, 5000, frequency_millis, sendNotificationPI);
        } else {
            //send a straight order for a notification (the notif's buttons call the same notification over and over)
            sendBroadcast(sendNotificationI);
        }
    }

    //Logging and debugging
    public void log(String msg) {
        if (ActivityMacroView.shouldLog) {
            Log.i("Learnt.MicroViewAct____", msg);
        }
    }

    public void startTimer(String name) {
        timerName = name;
        timer = System.currentTimeMillis();
    }

    public void stopTimer() {
        log("Timer '" + timerName + "' Started: " + timer +
                "\n  Difference: " + (System.currentTimeMillis() - timer));
    }

    //--------------========================Unimplemented========================--------------

    private void launchMarket() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, " unable to find market app", Toast.LENGTH_LONG).show();
        }
    }

    private void importSets() {
//        final AlertDialog.Builder importSetDialog = new AlertDialog.Builder(this);
//        final EditText editText = new EditText(this);
////        editText.setMaxLines(1);
//        importSetDialog.setTitle("Import pairs");
//        importSetDialog.setMessage("Copy-Paste the text sent to you");
//        importSetDialog.setView(editText, 50, 0, 50, 5);
//
//        importSetDialog.setPositiveButton("Import onto Screen", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                String importString = editText.getText().toString();
//                if (!importString.equals("")) {
//                    ArrayList<Group> g = parseSharedGroups(importString);
//                    for (Group group : g) {
//                        addGroupToDBAndUI(group, -1);
//                    }
//                }
//            }
//        });
//        importSetDialog.show();
    }

    private void shareSet() {
//        // populate the share intent with data
//        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
//        sharingIntent.setType("text/plain");
//        String shareText = "";
//        for (Group g : currSet.getGroups(db)) {
//            shareText += g.toStringForSharing();
//        }
//        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareText);
//        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    public ArrayList<Group> parseSharedGroups(String toParse) {
//        String[] groupStrings = toParse.split("\\{|\\{\\}|\\}");
        ArrayList<Group> groups = new ArrayList<>();
//        Group g;
//        for (String group : groupStrings) {
//            String[] arr = group.split(";");
//            g = new Group(
//                    Integer.parseInt(arr[0]),
//                    arr[1],
//                    arr[2],
//                    arr[3].equals("t"),
//                    arr[4].equals("t"),
//                    Integer.parseInt(arr[5]),
//                    Integer.parseInt(arr[6]),
//                    Integer.parseInt(arr[7]));
//            groupsForNotification.addAt(g);
//        }
        return groups;
    }

}
