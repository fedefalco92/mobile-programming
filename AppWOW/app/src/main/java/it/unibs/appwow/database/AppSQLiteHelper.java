package it.unibs.appwow.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import it.unibs.appwow.database.AppDB.*;

/**
 * This class extends an SQLiteOpenHelper and it provides statements to create db.
 */
class AppSQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG_LOG = AppSQLiteHelper.class.getSimpleName();

    // database creation (tables) SQL statement
    private static final String TABLE_USERS_CREATE = "CREATE TABLE " + Users.TABLE_USERS + " ( "+
        Users._ID + " INTEGER NOT NULL PRIMARY KEY, " +
        Users.COLUMN_FULLNAME + " TEXT NOT NULL, " +
        Users.COLUMN_EMAIL + " TEXT NOT NULL, " +
        Users.COLUMN_CREATED_AT + " NUMERIC, " +
        Users.COLUMN_UPDATED_AT + " NUMERIC " +
        " );";

    private static final String TABLE_GROUPS_CREATE = "CREATE TABLE " + Groups.TABLE_GROUPS + " ("+
            Groups._ID + " INTEGER NOT NULL PRIMARY KEY, " +
            Groups.COLUMN_ID_ADMIN + " INTEGER, " +
            Groups.COLUMN_NAME + " TEXT, " +
            Groups.COLUMN_PHOTO + " TEXT, " +
            Groups.COLUMN_PHOTO_UPDATED_AT + " NUMERIC, " +
            Groups.COLUMN_CREATED_AT + " NUMERIC, " +
            Groups.COLUMN_UPDATED_AT + " NUMERIC, " +
            Groups.COLUMN_HIGHLIGHTED + " INTEGER DEFAULT 0 " +
            ");";

    private static final String TABLE_PAYMENTS_CREATE = "CREATE TABLE " + Payments.TABLE_PAYMENTS + "(" +
            Payments._ID + " INTEGER NOT NULL PRIMARY KEY, " +
            Payments.COLUMN_ID_GROUP + " INTEGER NOT NULL, " +
            Payments.COLUMN_ID_USER + " INTEGER NOT NULL, " +
            Payments.COLUMN_ID_USER_TO + " INTEGER, " +
            Payments.COLUMN_AMOUNT + " REAL NOT NULL, " +
            Payments.COLUMN_CURRENCY + " TEXT DEFAULT \"EUR\" , " +
            Payments.COLUMN_DATE + " NUMERIC, " +
            Payments.COLUMN_FORALL + " INTEGER DEFAULT 0, " +
            Payments.COLUMN_NAME + " TEXT, " +
            Payments.COLUMN_NOTES + " TEXT, " +
            Payments.COLUMN_CREATED_AT + " NUMERIC, " +
            Payments.COLUMN_UPDATED_AT + " NUMERIC, " +
            Payments.COLUMN_IS_EXCHANGE + " INTEGER, " +
            Payments.COLUMN_POSITION + " TEXT, " +
            Payments.COLUMN_POSITION_ID + " TEXT, " +
            Payments.COLUMN_AMOUNT_DETAILS + " TEXT, " +
            "FOREIGN KEY (" + Payments.COLUMN_ID_USER + ") REFERENCES " + Users.TABLE_USERS + "(" + Users._ID + ")," +
            "FOREIGN KEY (" + Payments.COLUMN_ID_GROUP + ") REFERENCES " + Groups.TABLE_GROUPS + "(" + Groups._ID + ") ON UPDATE CASCADE ON DELETE CASCADE" +
            ");";

    private static final String TABLE_DEBTS_CREATE = "CREATE TABLE " + Debts.TABLE_DEBTS + " ("+
            Debts._ID + " INTEGER NOT NULL PRIMARY KEY, " +
            Debts.COLUMN_ID_GROUP + " INTEGER NOT NULL, " +
            Debts.COLUMN_ID_FROM + " INTEGER NOT NULL, " +
            Debts.COLUMN_ID_TO + " INTEGER NOT NULL, " +
            Debts.COLUMN_AMOUNT + " REAL, " +
            "FOREIGN KEY (" + Debts.COLUMN_ID_GROUP + ") REFERENCES " + Groups.TABLE_GROUPS + "(" + Groups._ID + ") ON UPDATE CASCADE ON DELETE CASCADE," +
            "FOREIGN KEY (" + Debts.COLUMN_ID_FROM + ") REFERENCES " + Users.TABLE_USERS + "(" + Users._ID + ")," +
            "FOREIGN KEY (" + Debts.COLUMN_ID_TO+ ") REFERENCES " + Users.TABLE_USERS + "(" + Users._ID + ")" +
            ");";
    private static final String TABLE_USER_GROUP_CREATE = "CREATE TABLE " + UserGroup.TABLE_USER_GROUP + "(" +
            UserGroup.COLUMN_ID_USER + " INTEGER NOT NULL, " +
            UserGroup.COLUMN_ID_GROUP + " INTEGER NOT NULL, " +
            UserGroup.COLUMN_AMOUNT + " REAL, "+
            UserGroup.COLUMN_UPDATED_AT + " NUMERIC, " +
            "PRIMARY KEY (" + UserGroup.COLUMN_ID_USER + ", " + UserGroup.COLUMN_ID_GROUP + "), " +
            "FOREIGN KEY (" + UserGroup.COLUMN_ID_USER + ") REFERENCES " + Users.TABLE_USERS + "(" + Users._ID + ")," +
            "FOREIGN KEY (" + UserGroup.COLUMN_ID_GROUP+ ") REFERENCES " + Groups.TABLE_GROUPS + "(" + Groups._ID + ") ON UPDATE CASCADE ON DELETE CASCADE " +
            ");";


    public AppSQLiteHelper(Context context) {
        super(context, AppDB.DATABASE_NAME, null, AppDB.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        try {
            database.beginTransaction();
            database.execSQL(TABLE_USERS_CREATE);
            database.execSQL(TABLE_GROUPS_CREATE);
            database.execSQL(TABLE_USER_GROUP_CREATE);
            database.execSQL(TABLE_PAYMENTS_CREATE);
            database.execSQL(TABLE_DEBTS_CREATE);
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if(!db.isReadOnly()){
            // Enable foreign key contraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
