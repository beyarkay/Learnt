package beyarkay.learnt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Group {
    @JsonIgnore
    private long id = -1;
    @JsonProperty("id")
    private String quizlet_id;
    private String term;
    private String definition;
    private int learnt_state = LEARNT_NONE;
    public static final int LEARNT_NONE = 0;
    public static final int LEARNT_BACKWARDS = 1;
    public static final int LEARNT_FORWARDS = 2;
    public static final int LEARNT_FULLY = 3;
    private int times_shown_f = 0;
    private int times_shown_b = 0;
    private long setId = -1;

    public Group(String term, String definition, long setId) {
        this.term = term;
        this.definition = definition;
        this.setId = setId;
    }

    public Group() {
    }

    //Setters

    public void setQuizletId(String quizlet_id) {
        this.quizlet_id = quizlet_id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public void setSetId(long setId) {
        this.setId = setId;
    }

    public void setTimesShownF(int times_shown_f) {
        this.times_shown_f = times_shown_f;
    }

    public void incrementTimesShownF() {
        setTimesShownF(getTimesShownF() + 1);
    }

    public void incrementTimesShownB() {
        setTimesShownB(getTimesShownB() + 1);
    }

    //Getters

    public String getQuizletId() {
        return quizlet_id;
    }

    public String getTerm() {
        return term;
    }

    public String getDefinition() {
        return definition;
    }

    public long getId() {
        return id;
    }

    public int getLearntState() {
        return learnt_state;
    }

    public void setLearntState(int learnt_state) {
        this.learnt_state = learnt_state;
    }

    public int getTimesShownB() {
        return times_shown_b;
    }

    public void setTimesShownB(int times_shown_b) {
        this.times_shown_b = times_shown_b;
    }

    public int getTimesShownF() {
        return times_shown_f;
    }

    public long getSetId() {
        return setId;
    }

    public String toString() {
        return "Group=[id=" + getId()
                + ", term=" + getTerm()
                + ", definition=" + getDefinition()
                + ", learnt_state=" + getLearntState()
                + ", times_shown_f=" + getTimesShownF()
                + ", times_shown_b=" + getTimesShownB()
                + ", is_valid=" + isValid()
                + ", setId=" + getSetId() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        final Group other = (Group) obj;
        return !this.getTerm().equals(other.getTerm()) ||
                !this.getDefinition().equals(other.getDefinition()) ||
                this.getTimesShownF() != other.getTimesShownF() ||
                this.getTimesShownB() != other.getTimesShownB() ||
                this.getSetId() != other.getSetId();
    }

    public boolean isValid() {
        return getTerm() != null &&
                getDefinition() != null &&
                !getTerm().equals("") &&
                !getDefinition().equals("");
    }

}
