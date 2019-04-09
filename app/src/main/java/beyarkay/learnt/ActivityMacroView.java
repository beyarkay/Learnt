package beyarkay.learnt;
// TODO: 2017/07/07 Change DB to have set-specific preferences

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class ActivityMacroView extends AppCompatActivity {
    SharedPreferences sharedPref;
    DBHelper db;
    FloatingActionButton fab;
    Set sEnterTitle;
    AlarmManager alarmMgr;
    PendingIntent piSendNotification;
    MenuItem miDelete;
    MenuItem miEdit;
    MenuItem miSettings;
    MenuItem miSearch;
    ProgressBar pbSearchResults;
    ArrayList<Set> activeSets = new ArrayList<>();
    ArrayList<Set> resultsSets = new ArrayList<>();
    RecyclerView rvSetsHolder;
    AdaptorSets asActive;
    AdaptorSets asResults;
    private static final String TAG = "ActMacroView";
    public static boolean shouldLog = true;
    int tabbage = 0;
    private int fabState = FAB_STATE_ADD;    //will either be {addAt, forward, edit}
    int selectedSetCards = 0;
    long chosenSetId;
    final int NUM_REQUESTS_TO_MAKE = 11;
    final String CLIENT_ID = "hMRQuTa3yQ";
    private static final int FAB_STATE_ADD = 0;
    private static final int FAB_STATE_FORWARD = 1;
    private static final int FAB_STATE_DELETE = 2;
    private static final int FAB_STATE_CONFIRM = 3;
    private static final int FAB_STATE_SEARCH = 4;

    @Override
    public void onPause() {
        super.onPause();
        updateDBFromSets();
//        triggerNotifications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUIandArrayFromDB();
        setFabState(FAB_STATE_ADD);
        fab.requestFocus();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_macro_view);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        db = new DBHelper(this);
        fab = findViewById(R.id.setSelectorFab);
        setFabState(FAB_STATE_ADD);
        pbSearchResults = findViewById(R.id.pbSearchResults);
        selectedSetCards = 0;


        //setting up rvSetsHolder
        rvSetsHolder = findViewById(R.id.rvSetsHolder);
        rvSetsHolder.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rvSetsHolder.setLayoutManager(llm);
        rvSetsHolder.setItemAnimator(new DefaultItemAnimator());
        //setting up the adaptors and data for rvSetsHolder
        asActive = new AdaptorSets(activeSets, db, this);
        asResults = new AdaptorSets(resultsSets, db, this);
        rvSetsHolder.setAdapter(asActive);
        activeSets = db.getNotArchivedSets();
        if (activeSets.size() == 0) {
//        add some dummy data for debugging
            db.removeAllGroups();
            db.removeSet("");
            db.addDummyData();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_macro, menu);
        // Get the SearchView and set the searchable configuration
        MenuItem miSearch = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) miSearch.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setIconifiedByDefault(true);
        searchView.setSubmitButtonEnabled(false);
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);

        miDelete = menu.findItem(R.id.action_delete_set);
        miEdit = menu.findItem(R.id.action_edit_set_title);
        miSettings = menu.findItem(R.id.action_settings);

//        miSearch.expandActionView();
        MenuItemCompat.setOnActionExpandListener(miSearch, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                ArrayList<Set> temp = db.getNotArchivedSets();
                if (temp.size() == activeSets.size()) {
                    for (int i = 0; i < activeSets.size(); i++) {
                        if (activeSets.get(i).getId() != temp.get(i).getId()) {
                            activeSets = db.getNotArchivedSets();
                            rvSetsHolder.setAdapter(asActive);
                            updateUIandArrayFromDB();
                            break;
                        }
                    }
                } else {
                    activeSets = db.getNotArchivedSets();
                    rvSetsHolder.setAdapter(asActive);
                    updateUIandArrayFromDB();
                }
                return true;
            }
        });
        return true;    //returning true will actually display the menu
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                setFabState(FAB_STATE_SEARCH);
                return true;
//            case R.id.action_delete_set:
            case R.id.action_edit_set_title:
                if (selectedSetCards == 1) {
                    changeSelectedSetName();
                }
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, ActivitySettings.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onBackPressed() {
        switch (fabState) {
            case FAB_STATE_FORWARD:
                setFabState(FAB_STATE_ADD);
//            activeSetsHolder.removeViewAt(activeSetsHolder.getChildCount() - 1);
                db.removeSet(activeSets.get(activeSets.size() - 1).getId());
                activeSets.remove(activeSets.size() - 1);
                asActive.notifyItemRemoved(activeSets.size() - 1);
                break;
            case FAB_STATE_DELETE:
                setFabState(FAB_STATE_ADD);
                break;
            default:
                super.onBackPressed();
                break;
        }
    }

    /*
    For Searching through the Quizlet API
     */
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            setFabState(FAB_STATE_SEARCH);
            fab.hide();
            String query = intent.getStringExtra(SearchManager.QUERY);
            asResults.sets.clear();
            rvSetsHolder.setAdapter(asResults);
            //Actually perform the search
            searchQuizletFor(query);
        }
    }

    private void searchQuizletFor(String query) {
        pbSearchResults.setIndeterminate(true);
        pbSearchResults.setVisibility(View.VISIBLE);
        pbSearchResults.setMax(NUM_REQUESTS_TO_MAKE + 1);
        final int[] requests_left = {NUM_REQUESTS_TO_MAKE};
        final RequestQueue queue = Volley.newRequestQueue(this);
        query = query.replaceAll("\\s", "%20");
        String url = String.format(
                "https://api.quizlet.com/2.0/search/sets?page=1&per_page=%s&client_id=%s&whitespace=1&q=%s",
                requests_left[0], CLIENT_ID, query);
        Log.d(TAG, "Set Query= " + url);
        resultsSets.clear();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ObjectMapper map = new ObjectMapper();
                        try {
//                            final ArrayList<Set> resultsSets = new ArrayList<>();
                            JSONArray jsonSets = response.getJSONArray("sets");
                            pbSearchResults.setIndeterminate(false);
                            pbSearchResults.setProgress(1);
                            for (int i = 0; i < jsonSets.length(); i++) {
                                final Set set = map.readValue(jsonSets.get(i).toString(), Set.class);
                                set.setActivity(Set.ACTIVITY_ARCHIVED);
                                set.setId(db.addSet(set));
                                resultsSets.add(set);
                                String url = String.format(
                                        "https://api.quizlet.com/2.0/sets/%s?client_id=%s&whitespace=1",
                                        set.getQuizletId(), CLIENT_ID);
                                Log.d(TAG, "Groups Query= " + url);
                                final int finalI = i;
                                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                                        (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                try {
                                                    ObjectMapper map = new ObjectMapper();
                                                    JSONArray jsonTerms = response.getJSONArray("terms");
                                                    for (int j = 0; j < jsonTerms.length(); j++) {
                                                        Group g = map.readValue(jsonTerms.get(j).toString(), Group.class);
                                                        g.setSetId(set.getId());
                                                        g.setId(db.addGroup(g));
                                                    }
                                                } catch (JSONException | IOException e) {
                                                    e.printStackTrace();
                                                }
                                                requests_left[0]--;
                                                pbSearchResults.incrementProgressBy(1);
                                                if (requests_left[0] == 0) {
                                                    pbSearchResults.setIndeterminate(true);
                                                    asResults.sets = resultsSets;
                                                    asResults.notifyDataSetChanged();
                                                    pbSearchResults.setVisibility(View.GONE);
                                                }
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                            }
                                        });
                                queue.add(jsonObjectRequest);
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                            Log.d(TAG, "Error of" + e.toString());
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.d(TAG, "\nError of " + error.toString());
                    }
                });
// Add the request to the RequestQueue.
        Log.d(TAG, "sending off request...");
        queue.add(jsObjRequest);
    }

    /*
    FAB Methods
     */
    public void onFabClick(View view) {
        EditText etSetTitle;
        try {
            etSetTitle = rvSetsHolder.getChildAt(rvSetsHolder.getChildCount() - 1).findViewById(R.id.etSetTitle);
        } catch (NullPointerException npe) {
            etSetTitle = null;
        }
        switch (fabState) {
            case FAB_STATE_ADD:
                sEnterTitle = new Set(
                        "",
                        getApplicationContext().getResources().getString(R.string.pref_term_title_default),
                        getApplicationContext().getResources().getString(R.string.pref_definition_title_default)
                );
                sEnterTitle.setState(Set.STATE_ENTER_TITLE);
                // TODO: 2018/02/24 setActivity doesn't actually change the toggle on the SCV
                sEnterTitle.setActivity(Set.ACTIVITY_ACTIVE);
                sEnterTitle.setId(db.addSet(sEnterTitle));
                asActive.addAt(asActive.getItemCount(), sEnterTitle);
                rvSetsHolder.smoothScrollToPosition(asActive.getItemCount());
                asActive.notifyDataSetChanged();
//                showSoftInput(((AdaptorSets.SCViewHolder) rvSetsHolder.findViewHolderForAdapterPosition(asActive.getItemCount() - 1)).etSetTitle);
                fab.setImageResource(R.drawable.ic_arrow_forward_white_24dp);
                setFabState(FAB_STATE_FORWARD);
                break;
            case FAB_STATE_FORWARD:
                etSetTitle.clearFocus();
                sEnterTitle.setTitle(etSetTitle.getText().toString());
                sEnterTitle.setState(Set.STATE_COLLAPSED);
                db.updateSet(sEnterTitle, sEnterTitle.getId());

                Intent iMicroViewActivity = new Intent(this, ActivityMicroView.class);
                iMicroViewActivity.putExtra(String.valueOf(R.id.TAG_SET_ID), sEnterTitle.getId());
                startActivity(iMicroViewActivity);
                setFabState(FAB_STATE_ADD);
                break;
            case FAB_STATE_CONFIRM:
                AdaptorSets.SCViewHolder scViewHolder;
                for (int i = 0; i < asActive.sets.size(); i++) {
                    if (asActive.sets.get(i).getState() == Set.STATE_ENTER_TITLE) {
                        scViewHolder = ((AdaptorSets.SCViewHolder) rvSetsHolder.findViewHolderForAdapterPosition(i));
                        scViewHolder.etSetTitle.clearFocus();
                        asActive.sets.get(i).setTitle(scViewHolder.etSetTitle.getText().toString());
                        asActive.sets.get(i).setState(Set.STATE_COLLAPSED);
                        db.updateSet(asActive.sets.get(i), asActive.sets.get(i).getId());
                        scViewHolder.updateState();
                        updateUIandArrayFromDB();
                        break;
                    }
                }
                selectedSetCards = 0;
                setFabState(FAB_STATE_ADD);
                break;
            case FAB_STATE_DELETE:
                for (int i = activeSets.size() - 1; i > -1; i--) {
                    Set set = activeSets.get(i);
                    if (set.isSelected()) {
                        db.removeSet(set.getId());
                    }
                }
                updateUIandArrayFromDB();
                asActive.notifyDataSetChanged();
                triggerNotifications();
                setFabState(FAB_STATE_ADD);
                selectedSetCards = 0;
                break;
            case FAB_STATE_SEARCH:
                //todo fire off the Intent that the search bar actually uses
//                Intent iSearch = new Intent();
        }
    }

    public void setFabState(int state) {
        switch (state) {
            case FAB_STATE_ADD:             //the default FAB state, give the user the option to addAt a new set
                fab.setImageResource(R.drawable.ic_add_white_24dp);
                fab.show();
                fabState = FAB_STATE_ADD;
                break;
            case FAB_STATE_DELETE:       //Set FAB to DELETE when selecting multiple sets
                fab.setImageResource(R.drawable.ic_delete_white_24dp);
                fab.show();
                fabState = FAB_STATE_DELETE;
                break;
            case FAB_STATE_FORWARD:         //to go from creating a new set to editing that set
                fab.show();
                fabState = FAB_STATE_FORWARD;
                break;
            case FAB_STATE_CONFIRM:         //to confirm changes made to a set's name
                fab.setImageResource(R.drawable.ic_done_white_24dp);
                fab.show();
                fabState = FAB_STATE_CONFIRM;
                break;
            case FAB_STATE_SEARCH:
                fab.setImageResource(R.drawable.ic_search_white_24dp);
                fab.hide();
                fabState = FAB_STATE_SEARCH;
                break;
        }
        updateMenuBar();
    }

    /*
    Editing the sets
     */
    private void changeSelectedSetName() {
        for (int i = 0; i < asActive.getItemCount(); i++) {
            Set set = asActive.sets.get(i);
            if (set.isSelected()) {
                set.setState(Set.STATE_ENTER_TITLE);
                set.setSelected(false);
                ((AdaptorSets.SCViewHolder) rvSetsHolder.findViewHolderForAdapterPosition(i)).updateIsSelected();
                ((AdaptorSets.SCViewHolder) rvSetsHolder.findViewHolderForAdapterPosition(i)).updateState();
                showSoftInput(((AdaptorSets.SCViewHolder) rvSetsHolder.findViewHolderForAdapterPosition(i)).etSetTitle);
                setFabState(FAB_STATE_CONFIRM);
                break;
            }
        }

    }

    public ArrayList<Set> getSelectedSets() {
        ArrayList<Set> selectedSets = new ArrayList<>();
        for (int i = 0; i < activeSets.size(); i++) {
            if (activeSets.get(i).isSelected()) {
                selectedSets.add(activeSets.get(i));
            }
        }
        return selectedSets;
    }

    public void updateSelectedSets() {
        if (selectedSetCards > 0) {
            setFabState(FAB_STATE_DELETE);
        } else {
            setFabState(FAB_STATE_ADD);
        }
    }

    /*
    Updating the UI
     */
    public void showSoftInput(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            try {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateMenuBar() {
        if (miSearch != null) {
//            miSettings.setVisible(fabState != FAB_STATE_CONFIRM);
            miEdit.setVisible(getSelectedSets().size() == 1);
        }
    }

    public void resetToNormal() {
        setFabState(FAB_STATE_ADD);
        super.onBackPressed();
        activeSets = db.getNotArchivedSets();
        rvSetsHolder.setAdapter(asActive);
        updateUIandArrayFromDB();
    }

    /*
    Querying the DB
     */
    public void updateUIandArrayFromDB() {
        activeSets = db.getNotArchivedSets();
//        asActive.notifyDataSetChanged();
        for (int i = asActive.getItemCount() - 1; i >= 0; i--) {
            asActive.removeAt(i);
        }
        for (int i = 0; i < activeSets.size(); i++) {
            activeSets.get(i).setState(Set.STATE_COLLAPSED);
            asActive.addAt(i, activeSets.get(i));
        }
    }

    private void updateDBFromSets() {
        for (Set s : activeSets) {
            db.updateSet(s, s.getId());
        }
    }

    /*
    Starting the Notifications going
     */
    public void triggerNotifications() {
        Intent iSendNotification = new Intent(this, NotificationTimeRxr.class);
        piSendNotification = PendingIntent.getBroadcast(this, 2, iSendNotification, PendingIntent.FLAG_UPDATE_CURRENT);
        long frequency = Long.parseLong(sharedPref.getString("pref_notification_frequency", "0"));
        if (frequency != 0L) {
            frequency = (frequency == -1L ? 60 : frequency);
            alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(),
                    frequency * 1000, piSendNotification);
        } else { //send a straight order for a notification (the notif's buttons call the same notification over and over)
            sendBroadcast(iSendNotification);
        }
    }

    /*
    Debugging
     */
    public void log(String msg) {
        if (shouldLog) {
            String tabs = "";
            for (int i = 0; i < tabbage; i++) {
                tabs += "  ";
            }
            Log.i("Learnt.MacroViewAct____", tabs + msg);
        }
    }

}
