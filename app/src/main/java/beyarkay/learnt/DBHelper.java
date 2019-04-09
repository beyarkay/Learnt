package beyarkay.learnt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;

class DBHelper extends SQLiteAssetHelper {
    private static final int DATABASE_VERSION = 16;
    private static final String DATABASE_NAME = "GroupsDB";
    Context mContext;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //    @Override
//    public void onCreate(SQLiteDatabase db) {
//        db.execSQL(LearntDB.GroupsTable.SQL_CREATE_TABLE);
//        db.execSQL(LearntDB.SetsTable.SQL_CREATE_TABLE);
//    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        // TODO: 2017/07/07 addAt something here...
        /*
        Copy all data over to new tables
         */
        db.execSQL(LearntDB.SetsTable.SQL_DROP_TABLE);
        db.execSQL(LearntDB.GroupsTable.SQL_DROP_TABLE);
        db.execSQL(LearntDB.SetsTable.SQL_CREATE_TABLE);
        db.execSQL(LearntDB.GroupsTable.SQL_CREATE_TABLE);

    }

    public int getDatabaseVersion() {
        return DATABASE_VERSION;
    }

    //SETS
    Set getSet(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + LearntDB.SetsTable.TABLE_NAME +
                " WHERE " + LearntDB.SetsTable.TABLE_NAME + "." + LearntDB.SetsTable._ID +
                "=" + id, null);
        Set set = new Set();
        if (c.moveToFirst()) {
            set.setId(c.getLong(c.getColumnIndex(LearntDB.SetsTable._ID)));
            set.setTitle(c.getString(c.getColumnIndex(LearntDB.SetsTable.COL_NAME)));
            set.setTermTitle(c.getString(c.getColumnIndex(LearntDB.SetsTable.COL_TERM_TITLE)));
            set.setDefinitionTitle(c.getString(c.getColumnIndex(LearntDB.SetsTable.COL_DEFINITION_TITLE)));
            set.setActivity(c.getInt(c.getColumnIndex(LearntDB.SetsTable.COL_ACTIVITY)));
            set.setPromptText(c.getString(c.getColumnIndex(LearntDB.SetsTable.COL_PROMPT_TEXT)));
            set.setFrequency(c.getInt(c.getColumnIndex(LearntDB.SetsTable.COL_PROMPT_TEXT)));
            set.setTermDefinitionFirst(c.getDouble(c.getColumnIndex(LearntDB.SetsTable.COL_PROMPT_TEXT)));
            set.setState(c.getInt(c.getColumnIndex(LearntDB.SetsTable.COL_PROMPT_TEXT)));
        } else {
            return null;
        }
        return set;
    }

    ArrayList<Group> getGroupsForNotifications(int direction) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Group> groups = new ArrayList<>();

        Cursor c;
        if (direction == LearntDB.GroupsTable.LEARNT_FORWARDS) {
            c = db.rawQuery(
                    "SELECT " + LearntDB.GroupsTable.TABLE_NAME + "." + LearntDB.GroupsTable._ID +
                            " FROM " + LearntDB.GroupsTable.TABLE_NAME +
                            " INNER JOIN " + LearntDB.SetsTable.TABLE_NAME +
                            " ON " + LearntDB.SetsTable.TABLE_NAME + "." + LearntDB.SetsTable._ID +
                            " = " + LearntDB.GroupsTable.TABLE_NAME + "." + LearntDB.GroupsTable.COL_SET_ID +
                            " WHERE " + LearntDB.SetsTable.TABLE_NAME + "." + LearntDB.SetsTable.COL_ACTIVITY +
                            " = " + Set.ACTIVITY_ACTIVE +
                            " AND " + LearntDB.GroupsTable.TABLE_NAME + "." + LearntDB.GroupsTable.COL_LEARNT_STATE +
                            " != " + LearntDB.GroupsTable.LEARNT_FULLY +
                            " AND " + LearntDB.GroupsTable.TABLE_NAME + "." + LearntDB.GroupsTable.COL_LEARNT_STATE +
                            " != " + LearntDB.GroupsTable.LEARNT_FORWARDS, null);
        } else if (direction == LearntDB.GroupsTable.LEARNT_BACKWARDS) {
            c = db.rawQuery("SELECT " + LearntDB.GroupsTable.TABLE_NAME + "." + LearntDB.GroupsTable._ID +
                    " FROM " + LearntDB.GroupsTable.TABLE_NAME +
                    " INNER JOIN " + LearntDB.SetsTable.TABLE_NAME +
                    " ON " + LearntDB.SetsTable.TABLE_NAME + "." + LearntDB.SetsTable._ID +
                    " = " + LearntDB.GroupsTable.TABLE_NAME + "." + LearntDB.GroupsTable.COL_SET_ID +
                    " WHERE " + LearntDB.SetsTable.TABLE_NAME + "." + LearntDB.SetsTable.COL_ACTIVITY +
                    " = " + Set.ACTIVITY_ACTIVE +
                    " AND " + LearntDB.GroupsTable.TABLE_NAME + "." + LearntDB.GroupsTable.COL_LEARNT_STATE +
                    " != " + LearntDB.GroupsTable.LEARNT_FULLY +
                    " AND " + LearntDB.GroupsTable.TABLE_NAME + "." + LearntDB.GroupsTable.COL_LEARNT_STATE +
                    " != " + LearntDB.GroupsTable.LEARNT_BACKWARDS, null);
        } else {
            throw new IllegalArgumentException("getGroupsForNotifications(int) expects an int either LDB.GT.LF or LDB.GT.LB");
        }
        if (c.moveToFirst()) {
            do {
                Group g = getGroup(c.getLong(c.getColumnIndex(LearntDB.GroupsTable._ID)));
                if (g.isValid()) {
                    groups.add(g);
                }
            } while (c.moveToNext());
        }
        c.close();
        return groups;
    }

    long addSet(Set newSet) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(LearntDB.SetsTable.COL_NAME, newSet.getTitle());
        values.put(LearntDB.SetsTable.COL_TERM_TITLE, newSet.getTermTitle());
        values.put(LearntDB.SetsTable.COL_DEFINITION_TITLE, newSet.getDefinitionTitle());
        values.put(LearntDB.SetsTable.COL_PROMPT_TEXT, newSet.getPromptText());
        values.put(LearntDB.SetsTable.COL_ACTIVITY, newSet.getActivity());
        values.put(LearntDB.SetsTable.COL_FREQUENCY, newSet.getFrequency());
        values.put(LearntDB.SetsTable.COL_TERM_DEFINITION_FIRST, newSet.getTermDefinitionFirst());
        values.put(LearntDB.SetsTable.COL_STATE, newSet.getState());
        return db.insert(LearntDB.SetsTable.TABLE_NAME, null, values);
    }

    ArrayList<Set> getNotArchivedSets() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Set> sets = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT " + LearntDB.SetsTable.TABLE_NAME + "." + LearntDB.SetsTable._ID +
                " FROM " + LearntDB.SetsTable.TABLE_NAME +
                " WHERE " + LearntDB.SetsTable.COL_ACTIVITY +
                " != " + Set.ACTIVITY_ARCHIVED, null);
        if (cursor.moveToFirst()) {
            do {
                sets.add(getSet(cursor.getLong(0)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return sets;
    }

    @Deprecated
    ArrayList<Set> getArchivedSets() {
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<Set> sets = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT " + LearntDB.SetsTable.TABLE_NAME + "." + LearntDB.SetsTable._ID +
                " FROM " + LearntDB.SetsTable.TABLE_NAME +
                " WHERE " + LearntDB.SetsTable.COL_ACTIVITY +
                " = " + Set.ACTIVITY_ARCHIVED, null);
        if (cursor.moveToFirst()) {
            do {
                sets.add(getSet(cursor.getLong(0)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return sets;
    }

    void updateSet(Set set, long setId) {
        SQLiteDatabase db = this.getReadableDatabase();

        if (this.getSet(setId) == null) {
            addSet(set);
            throw new NullPointerException();
        } else {
            ContentValues values = new ContentValues();
            values.put(LearntDB.SetsTable.COL_NAME, set.getTitle());
            values.put(LearntDB.SetsTable.COL_TERM_TITLE, set.getTermTitle());
            values.put(LearntDB.SetsTable.COL_DEFINITION_TITLE, set.getDefinitionTitle());
            values.put(LearntDB.SetsTable.COL_ACTIVITY, set.getActivity());
            values.put(LearntDB.SetsTable.COL_PROMPT_TEXT, set.getPromptText());
            values.put(LearntDB.SetsTable.COL_FREQUENCY, set.getFrequency());
            values.put(LearntDB.SetsTable.COL_TERM_DEFINITION_FIRST, set.getTermDefinitionFirst());
            db.update(LearntDB.SetsTable.TABLE_NAME, values, LearntDB.SetsTable.TABLE_NAME + "." + LearntDB.SetsTable._ID + "=" + setId, null);
        }
    }

    public void updateSetAndItsGroups(Set set, long setId, ArrayList<Group> groups) {
        // FIXME: 2017/07/31 might be the thing causing the delays
        updateSet(set, setId);
        for (Group g : groups) {
            g.setSetId(setId);
            if (getGroup(g.getId()) == null) {
                addGroup(g);
            } else {
                updateGroup(g, g.getId());
            }
        }
    }

    void removeSet(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE FROM " + LearntDB.SetsTable.TABLE_NAME +
                " WHERE " + LearntDB.SetsTable.TABLE_NAME + "." + LearntDB.SetsTable._ID +
                "=" + id);
        db.execSQL("DELETE FROM " + LearntDB.GroupsTable.TABLE_NAME +
                " WHERE " + LearntDB.GroupsTable.TABLE_NAME + "." + LearntDB.GroupsTable.COL_SET_ID +
                "=" + id);
    }

    @Deprecated
    public void removeSet(String optionalName) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (optionalName.equals("")) {
            db.execSQL("DELETE FROM " + LearntDB.SetsTable.TABLE_NAME);
        } else {
            db.execSQL("DELETE FROM " + LearntDB.SetsTable.TABLE_NAME + " WHERE " + LearntDB.SetsTable.COL_NAME + "='" + optionalName + "'");
        }
    }

    public void recreateAllTables() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DROP TABLE " + LearntDB.SetsTable.TABLE_NAME);
        db.execSQL("DROP TABLE " + LearntDB.GroupsTable.TABLE_NAME);
        db.execSQL(LearntDB.GroupsTable.SQL_CREATE_TABLE);
        db.execSQL(LearntDB.SetsTable.SQL_CREATE_TABLE);

    }

    //GROUPS
    Group getGroup(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + LearntDB.GroupsTable.TABLE_NAME + " WHERE " + LearntDB.GroupsTable.TABLE_NAME + "." + LearntDB.GroupsTable._ID + "=" + id, null);
        if (cursor.moveToFirst()) {
            Group group = new Group();
            group.setId(cursor.getLong(cursor.getColumnIndex(LearntDB.GroupsTable._ID)));
            group.setSetId(cursor.getLong(cursor.getColumnIndex(LearntDB.GroupsTable.COL_SET_ID)));
            group.setTerm(cursor.getString(cursor.getColumnIndex(LearntDB.GroupsTable.COL_TERM)));
            group.setDefinition(cursor.getString(cursor.getColumnIndex(LearntDB.GroupsTable.COL_DEFINITION)));
            group.setLearntState(cursor.getInt(cursor.getColumnIndex(LearntDB.GroupsTable.COL_LEARNT_STATE)));
            group.setTimesShownF(cursor.getInt(cursor.getColumnIndex(LearntDB.GroupsTable.COL_TIMES_SHOWN_F)));
            group.setTimesShownB(cursor.getInt(cursor.getColumnIndex(LearntDB.GroupsTable.COL_TIMES_SHOWN_B)));
            cursor.close();
            return group;
        } else {
            return null;
        }

    }

    public void removeAllGroups() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("DELETE FROM " + LearntDB.GroupsTable.TABLE_NAME);
    }

    void updateGroup(Group groupToWrite, long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        //Check getGroup(id) at this point
        Group DBgroup = getGroup(id);
        if (DBgroup == null) {
            throw new NullPointerException();
        }

        if (!DBgroup.getTerm().equals(groupToWrite.getTerm())) {
            values.put(LearntDB.GroupsTable.COL_TERM, groupToWrite.getTerm());
        }
        if (!DBgroup.getDefinition().equals(groupToWrite.getDefinition())) {
            values.put(LearntDB.GroupsTable.COL_DEFINITION, groupToWrite.getDefinition());
        }
        if (DBgroup.getLearntState() != groupToWrite.getLearntState()) {
            values.put(LearntDB.GroupsTable.COL_LEARNT_STATE, groupToWrite.getLearntState());
        }
        if (DBgroup.getTimesShownF() != groupToWrite.getTimesShownF()) {
            values.put(LearntDB.GroupsTable.COL_TIMES_SHOWN_F, groupToWrite.getTimesShownF());
        }
        if (DBgroup.getTimesShownB() != groupToWrite.getTimesShownB()) {
            values.put(LearntDB.GroupsTable.COL_TIMES_SHOWN_F, groupToWrite.getTimesShownB());
        }
        if (DBgroup.isValid() != groupToWrite.isValid()) {
            values.put(LearntDB.GroupsTable.COL_IS_VALID, groupToWrite.isValid());
        }
        if (DBgroup.getSetId() != groupToWrite.getSetId()) {
            values.put(LearntDB.GroupsTable.COL_SET_ID, groupToWrite.getSetId());
        }
        if (values.size() != 0) {
            db.update(LearntDB.GroupsTable.TABLE_NAME, values, LearntDB.GroupsTable._ID + "=" + id, null);
        }
    }

    long addGroup(Group newGroup) {
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(LearntDB.GroupsTable.COL_TERM, newGroup.getTerm());
        values.put(LearntDB.GroupsTable.COL_DEFINITION, newGroup.getDefinition());
        values.put(LearntDB.GroupsTable.COL_LEARNT_STATE, newGroup.getLearntState());
        values.put(LearntDB.GroupsTable.COL_TIMES_SHOWN_F, newGroup.getTimesShownF());
        values.put(LearntDB.GroupsTable.COL_TIMES_SHOWN_F, newGroup.getTimesShownB());
        values.put(LearntDB.GroupsTable.COL_IS_VALID, newGroup.isValid());
        values.put(LearntDB.GroupsTable.COL_SET_ID, newGroup.getSetId());

        return db.insert(LearntDB.GroupsTable.TABLE_NAME, null, values);
    }

    ArrayList<Group> getGroupsOfSet(long setId) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Group> groups = new ArrayList<>();
        Cursor c;
//        if (includLearntGroups) {
//            c = db.rawQuery(
//                    "SELECT " + LearntDB.GroupsTable._ID +
//                            " FROM " + LearntDB.GroupsTable.TABLE_NAME +
//                            " WHERE " + LearntDB.GroupsTable.TABLE_NAME + "." + LearntDB.GroupsTable.COL_SET_ID + " = " + setId, null);
//        } else {
        c = db.rawQuery(
                "SELECT " + LearntDB.GroupsTable._ID +
                        " FROM " + LearntDB.GroupsTable.TABLE_NAME +
                        " WHERE " + LearntDB.GroupsTable.TABLE_NAME + "." + LearntDB.GroupsTable.COL_SET_ID + " = " + setId/* +
                        " AND " + LearntDB.GroupsTable.COL_LEARNT_STATE + " != " + LearntDB.GroupsTable.LEARNT_FULLY*/, null);
//        }
//        if (c.getCount() > 700) {
//            db.execSQL("DELETE FROM " + LearntDB.GroupsTable.TABLE_NAME + " WHERE " + LearntDB.SetsTable.TABLE_NAME + "." + LearntDB.SetsTable._ID + " = " + setId);
//            Log.e("Learnt.DBHelper________", "Had to delete the groups in the DB with LearntDB.SetsTable._ID=" + setId);
//        }
        if (c.moveToFirst()) {
            do {
                groups.add(this.getGroup(c.getLong(c.getColumnIndex(LearntDB.GroupsTable._ID))));
            } while (c.moveToNext());
        }
        c.close();
        return groups;
    }

    private void log(String msg) {
        Log.i("Learnt.DBHelper________", msg);
    }

    @Override
    public String toString() {
        String returner = "Full DB:";
        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor gC = db.rawQuery("SELECT " + LearntDB.GroupsTable._ID + " FROM " + LearntDB.GroupsTable.TABLE_NAME, null);
        Cursor sC = db.rawQuery("SELECT " + LearntDB.SetsTable._ID + " FROM " + LearntDB.SetsTable.TABLE_NAME, null);

//        if (gC.moveToFirst()) {
//            returner += "\n" + LearntDB.GroupsTable.TABLE_NAME + ":";
//            do {
//                returner += "\n\t" + this.getGroup(gC.getLong(0)).toString();
//            } while (gC.moveToNext());
//        }
        if (sC.moveToFirst()) {
            returner += "\n" + LearntDB.SetsTable.TABLE_NAME + ":";
            do {
                returner += "\n\t" + this.getSet(sC.getLong(0)).toVerboseString(this);
            } while (sC.moveToNext());
        }
        return returner;
    }

    public void beginTransaction() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.beginTransaction();
    }

    public void endTransaction() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.endTransaction();
    }

    public boolean setExists(String setName, String dbName) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery("SELECT " + LearntDB.SetsTable.TABLE_NAME + "." + LearntDB.SetsTable._ID +
                    " FROM " + dbName +
                    " WHERE " + LearntDB.SetsTable.TABLE_NAME + "." + LearntDB.SetsTable.COL_NAME +
                    " LIKE '" + setName + "'", null);
            return c.moveToFirst();
        } catch (android.database.sqlite.SQLiteException e) {
            return false;
        }
    }

    public void removeGroup(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        if (id >= 0) {
            db.execSQL("DELETE FROM " + LearntDB.GroupsTable.TABLE_NAME +
                    " WHERE " + LearntDB.GroupsTable.TABLE_NAME + "." + LearntDB.GroupsTable._ID +
                    "=" + id);
        }
    }

    public void addDummyData() {
        final int NUM_SETS = (int) (Math.random() * 5) + 3;
        int NUM_GROUPS;
        for (int j = 0; j < NUM_SETS; j++) {
            Set set = new Set();
            set.setTitle("Set_" + ((int) (Math.random() * 1000)));
            set.setId(addSet(set));
            NUM_GROUPS = (int) (Math.random() * 100);
            for (int i = 0; i < NUM_GROUPS; i++) {
                String s = ((int) (Math.random() * 1000)) + "";
                Group g = new Group();
                g.setTerm(s);
                g.setDefinition("_" + s);
                g.setSetId(set.getId());
                g.setId(addGroup(g));
            }
        }

    }
}