package beyarkay.learnt;

import android.provider.BaseColumns;

public final class LearntDB {
    public static final String TEXT_TYPE = " TEXT";
    public static final String INTEGER_TYPE = " INTEGER";
    public static final String DOUBLE_TYPE = " REAL";
    public static final String COMMA_SEP = ", ";

    private LearntDB() {
    }

    public static class SetsTable implements BaseColumns {
        public static final String TABLE_NAME = "sets";
        public static final String COL_NAME = "name";
        public static final String COL_TERM_TITLE = "term_title";
        public static final String COL_DEFINITION_TITLE = "definition_title";
        public static final String COL_PROMPT_TEXT = "prompt_text";
        public static final String COL_ACTIVITY = "activity";
        public static final String COL_FREQUENCY = "frequency";
        public static final String COL_TERM_DEFINITION_FIRST = "term_definition_first";
        public static final String COL_STATE = "state";
        @Deprecated
        public static final int ACTIVITY_ARCHIVED = 0;
        @Deprecated
        public static final int ACTIVITY_UNARCHIVED = 1;
        @Deprecated
        public static final int ACTIVITY_ACTIVE = 2;


        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                COL_NAME + TEXT_TYPE + COMMA_SEP +
                COL_TERM_TITLE + TEXT_TYPE + COMMA_SEP +
                COL_DEFINITION_TITLE + TEXT_TYPE + COMMA_SEP +
                COL_ACTIVITY + INTEGER_TYPE + COMMA_SEP +
                COL_PROMPT_TEXT + TEXT_TYPE + COMMA_SEP +
                COL_FREQUENCY + INTEGER_TYPE + COMMA_SEP +
                COL_TERM_DEFINITION_FIRST + DOUBLE_TYPE + COMMA_SEP +
                COL_STATE + INTEGER_TYPE +
                " )";
        public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static class GroupsTable implements BaseColumns {
        public static final String TABLE_NAME = "groups";
        public static final String COL_SET_ID = "set_id";
        public static final String COL_TERM = "term";
        public static final String COL_DEFINITION = "definition";
        public static final String COL_LEARNT_STATE = "learnt_state";
        public static final int LEARNT_NONE = 0;
        public static final int LEARNT_BACKWARDS = 1;
        public static final int LEARNT_FORWARDS = 2;
        public static final int LEARNT_FULLY = 3;

        public static final String COL_TIMES_SHOWN_F = "times_shown_f";
        public static final String COL_TIMES_SHOWN_B = "times_shown_b";
        public static final String COL_IS_VALID = "is_valid";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                COL_SET_ID + INTEGER_TYPE + COMMA_SEP +
                COL_TERM + TEXT_TYPE + COMMA_SEP +
                COL_DEFINITION + TEXT_TYPE + COMMA_SEP +
                COL_LEARNT_STATE + INTEGER_TYPE + COMMA_SEP +
                COL_TIMES_SHOWN_F + INTEGER_TYPE + COMMA_SEP +
                COL_TIMES_SHOWN_B + INTEGER_TYPE + COMMA_SEP +
                COL_IS_VALID + INTEGER_TYPE +
                " )";
        public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}

