package beyarkay.learnt;

import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Set {
    @JsonIgnore
    private long id;
    @JsonProperty("id")
    private String quizlet_id;
    @JsonProperty("title")
    private String name;
    private String term_title = "Term";
    private String definition_title = "Definition";
    private String prompt_text = "Translation of:";

    private int frequency = 60;
    private double term_definition_first = 0.5;
    private boolean is_selected = false;

    public static final int STATE_ENTER_TITLE = 0;
    public static final int STATE_COLLAPSED = 1;
    public static final int STATE_EXPANDED = 2;
    private int state = Set.STATE_COLLAPSED;

    public static final int ACTIVITY_ARCHIVED = 0;
    public static final int ACTIVITY_INACTIVE = 1;
    public static final int ACTIVITY_ACTIVE = 2;
    private int activity = Set.ACTIVITY_ACTIVE;

    public Set() {
        setActivity(Set.ACTIVITY_ACTIVE);
    }

    public Set(String name, String termTitle, String definitionTitle) {
        setTitle(name);
        setDefinitionTitle(definitionTitle);
        setTermTitle(termTitle);
        setActivity(Set.ACTIVITY_ACTIVE);
        setState(Set.STATE_COLLAPSED);
    }

    public void setTitle(String name) {
        this.name = name;
    }

    public String getQuizletId() {
        return quizlet_id;
    }

    public void setQuizletId(String quizlet_id) {
        this.quizlet_id = quizlet_id;
    }

    public String getTitle() {
        return name;
    }

    public ArrayList<Group> getGroups(DBHelper dbIn) {
        return dbIn.getGroupsOfSet(this.getId());
    }

    void setActivity(int activity) {
        if (activity == Set.ACTIVITY_ACTIVE ||
                activity == Set.ACTIVITY_INACTIVE ||
                activity == Set.ACTIVITY_ARCHIVED) {
            this.activity = activity;
        }
    }

    int getActivity() {
        return activity;
    }

    public void setId(long idIn) {
        id = idIn;
    }

    public long getId() {
        return id;
    }

    int getCount(DBHelper dbIn) {
        return dbIn.getGroupsOfSet(this.getId()).size();
    }

    int getFrequency() {
        return frequency;
    }

    void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    double getTermDefinitionFirst() {
        return term_definition_first;
    }

    void setTermDefinitionFirst(double term_definition_first) {
        this.term_definition_first = term_definition_first;
    }

    String getPromptText() {
        return prompt_text;
    }

    void setPromptText(String prompt_text) {
        this.prompt_text = prompt_text;
    }

    String getTermTitle() {
        return term_title;
    }

    void setTermTitle(String term_title) {
        this.term_title = term_title;
    }

    String getDefinitionTitle() {
        return definition_title;
    }

    void setDefinitionTitle(String definition_title) {
        this.definition_title = definition_title;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
    
    public boolean isSelected() {
        return is_selected;
    }

    public void setSelected(boolean is_selected) {
        this.is_selected = is_selected;
    }

    public String toString(DBHelper dbIn) {
        return "{id=" + getId() +
                ", quizlet_id=" + getQuizletId() +
                ", count=" + getCount(dbIn) +
                ", name=" + getTitle() +
                ", term_t=" + getTermTitle() +
                ", defi_t=" + getDefinitionTitle() +
                ", prompt=" + getPromptText() +
                ", activity=" + getActivity() +
                ", frequency=" + getFrequency() + "}";
    }

    String toVerboseString(DBHelper db) {
        String s = this.toString(db);
        ArrayList<Group> groups = db.getGroupsOfSet(this.getId());
        for (Group g : groups) {
            s += "\n\t>" + g.toString();
        }
        return s;
    }

    private void log(String msg) {
        Log.i("Learnt.Set_____________", msg);
    }

}
