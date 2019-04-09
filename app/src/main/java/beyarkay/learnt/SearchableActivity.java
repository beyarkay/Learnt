package beyarkay.learnt;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

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


public class SearchableActivity extends Activity {
    private static final String TAG = "SearchableActivity";
    DBHelper db;
    RecyclerView rvSearchResults;
    ArrayList<Set> sets = new ArrayList<>();
    AdaptorSets adaptorSets;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
        db = new DBHelper(getApplicationContext());
        rvSearchResults = findViewById(R.id.rvSearchResults);
        adaptorSets = new AdaptorSets(sets, db, getApplicationContext());
        rvSearchResults.setAdapter(adaptorSets);
        for (int i = 0; i < 3; i++) {
            Log.d(TAG, "onCreate: Adding set number " + i);
            Set s = new Set("Set number " + i,
                    getResources().getString(R.string.pref_term_title_default),
                    getResources().getString(R.string.pref_definition_title_default));
            sets.add(s);
            adaptorSets.addAt(0, s);
        }
        adaptorSets.notifyDataSetChanged();


        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        } else {
            Log.d(TAG, "onCreate: Bad intent");
            doMySearch("Spanish things");
            Log.d(TAG, "onCreate: Finished the search");
        }
    }

    private void doMySearch(String query) {
        final int[] REQUESTS_TO_MAKE = {11};
        final RequestQueue queue = Volley.newRequestQueue(this);
        final String CLIENT_ID = "hMRQuTa3yQ";
        query = query.replaceAll("\\s", "%20");
        String url = String.format(
                "https://api.quizlet.com/2.0/search/sets?page=1&per_page=%s&client_id=%s&whitespace=1&q=%s",
                REQUESTS_TO_MAKE[0], CLIENT_ID, query);
        Log.d(TAG, "Set Query= " + url);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ObjectMapper map = new ObjectMapper();
                        try {
                            final ArrayList<Set> quizlet_sets = new ArrayList<>();
                            JSONArray jsonSets = response.getJSONArray("sets");
                            for (int i = 0; i < jsonSets.length(); i++) {
                                final Set set = map.readValue(jsonSets.get(i).toString(), Set.class);
                                set.setActivity(Set.ACTIVITY_ARCHIVED);
                                set.setId(db.addSet(set));
                                quizlet_sets.add(set);
                                String url = String.format(
                                        "https://api.quizlet.com/2.0/sets/%s?client_id=%s&whitespace=1",
                                        set.getQuizletId(), CLIENT_ID);
                                Log.d(TAG, "Groups Query= " + url);
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
                                                REQUESTS_TO_MAKE[0]--;
                                                if (REQUESTS_TO_MAKE[0] == 0) {
                                                    Log.d(TAG, "This is the end...");
                                                    // TODO: 2017/12/27 display the sets
                                                    for (int j = 0; j < quizlet_sets.size(); j++) {
                                                        Set s = quizlet_sets.get(j);
                                                        sets.add(s);
                                                        adaptorSets.addAt(j, s);

                                                    }
                                                    adaptorSets.notifyDataSetChanged();
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

}
