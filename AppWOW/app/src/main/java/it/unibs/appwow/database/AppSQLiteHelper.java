package it.unibs.appwow.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import it.unibs.appwow.database.AppDB.*;
import it.unibs.appwow.model.Cost;
import it.unibs.appwow.model.parc.Group;

/**
 * Created by Massi on 12/05/2016.
 */
public class AppSQLiteHelper extends SQLiteOpenHelper {

    // database creation (tables) SQL statement
    private static final String TABLE_USERS_CREATE = "CREATE TABLE " + Users.TABLE_USERS + " ( "+
        Users._ID + " INTEGER NOT NULL PRIMARY KEY, " +
        Users.COLUMN_FULLNAME + " TEXT NOT NULL, " +
        Users.COLUMN_EMAIL + " TEXT NOT NULL "+
        " );";

    private static final String TABLE_GROUPS_CREATE = "CREATE TABLE " + Groups.TABLE_GROUPS + " ("+
            Groups._ID + " INTEGER NOT NULL PRIMARY KEY, " +
            Groups.COLUMN_ID_ADMIN + " INTEGER, " +
            Groups.COLUMN_NAME + " TEXT, " +
            Groups.COLUMN_PHOTO + " TEXT, " +
            Groups.COLUMN_CREATED_AT + " NUMERIC, " +
            Groups.COLUMN_UPDATED_AT + " NUMERIC " +
            ");";

    private static final String TABLE_COSTS_CREATE  = "CREATE TABLE " + Costs.TABLE_COSTS + "(" +
            Costs._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
            Costs.COLUMN_ID_GROUP + " INTEGER NOT NULL, " +
            Costs.COLUMN_ID_USER + " INTEGER NOT NULL, " +
            Costs.COLUMN_AMOUNT + " REAL NOT NULL, " +
            Costs.COLUMN_NAME + " TEXT, " +
            Costs.COLUMN_NOTES + " TEXT, " +
            Costs.COLUMN_CREATED_AT + " NUMERIC, " +
            Costs.COLUMN_ARCHIVED_AT + " NUMERIC, " +
            Costs.COLUMN_POSITION + " TEXT, " +
            Costs.COLUMN_AMOUNT_DETAILS + " TEXT, " +
            "FOREIGN KEY (" + Costs.COLUMN_ID_USER + ") REFERENCES " + Users.TABLE_USERS + "(" + Users._ID + ")," +
            "FOREIGN KEY (" + Costs.COLUMN_ID_GROUP + ") REFERENCES " + Groups.TABLE_GROUPS + "(" + Groups._ID + ")" +
            ");";

    private static final String TABLE_BALANCING_CREATE = "CREATE TABLE " + Balancings.TABLE_BALANCINGS + " (" +
            Balancings._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
            Balancings.COLUMN_ID_GROUP + " INTEGER NOT NULL, " +
            Balancings.COLUMN_CREATED_AT + " NUMERIC, " +
            Balancings.COLUMN_COSTS_ID + " TEXT, "  +
            "FOREIGN KEY (" + Balancings.COLUMN_ID_GROUP + ") REFERENCES " + Groups.TABLE_GROUPS + "(" + Groups._ID + ")" +
            ");";

    private static final String TABLE_TRANSACTIONS_CREATE = "CREATE TABLE " + Transactions.TABLE_TRANSACTIONS + " ("+
            Transactions._ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
            Transactions.COLUMN_ID_BALANCING + " INTEGER NOT NULL, " +
            Transactions.COLUMN_ID_FROM + " INTEGER NOT NULL, " +
            Transactions.COLUMN_ID_TO + " INTEGER NOT NULL, " +
            Transactions.COLUMN_AMOUNT + " REAL, " +
            Transactions.COLUMN_PAYED_AT + " NUMERIC DEFAULT NULL, " +
            "FOREIGN KEY (" + Transactions.COLUMN_ID_BALANCING + ") REFERENCES " + Balancings.TABLE_BALANCINGS + "(" + Balancings._ID + ")," +
            "FOREIGN KEY (" + Transactions.COLUMN_ID_FROM + ") REFERENCES " + Users.TABLE_USERS + "(" + Users._ID + ")," +
            "FOREIGN KEY (" + Transactions.COLUMN_ID_TO+ ") REFERENCES " + Users.TABLE_USERS + "(" + Users._ID + ")" +
            ");";
    private static final String TABLE_USER_GROUP_CREATE = "CREATE TABLE " + UserGroup.TABLE_USER_GROUP + "(" +
            UserGroup.COLUMN_ID_USER + " INTEGER NOT NULL, " +
            UserGroup.COLUMN_ID_GROUP + " INTEGER NOT NULL, " +
            UserGroup.COLUMN_AMOUNT + " REAL, "+
            "PRIMARY KEY (" + UserGroup.COLUMN_ID_USER + ", " + UserGroup.COLUMN_ID_GROUP + "), " +
            "FOREIGN KEY (" + UserGroup.COLUMN_ID_USER + ") REFERENCES " + Users.TABLE_USERS + "(" + Users._ID + ")," +
            "FOREIGN KEY (" + UserGroup.COLUMN_ID_GROUP+ ") REFERENCES " + Groups.TABLE_GROUPS + "(" + Groups._ID + ")" +
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
            database.execSQL(TABLE_COSTS_CREATE);
            database.execSQL(TABLE_BALANCING_CREATE);
            database.execSQL(TABLE_TRANSACTIONS_CREATE);
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

    //from Object to database
    // TODO: 12/05/16 da spostare da qualche parte ...
    private ContentValues costToValues(Cost data) {
        ContentValues values = new ContentValues();
        values.put(Costs._ID, data.id);
        values.put(Costs.COLUMN_AMOUNT,data.amount);
        values.put(Costs.COLUMN_NAME,data.name);
        values.put(Costs.COLUMN_NOTES,data.notes);
        values.put(Costs.COLUMN_CREATED_AT,data.createdAt);
        values.put(Costs.COLUMN_ARCHIVED_AT,data.archivedAt);
        values.put(Costs.COLUMN_ID_GROUP,data.idGroup);
        values.put(Costs.COLUMN_ID_USER,data.idUser);
        values.put(Costs.COLUMN_POSITION,data.position);
        values.put(Costs.COLUMN_AMOUNT_DETAILS,data.amountDetails);
        return values;
    }

    private ContentValues groupToValues(Group g) {
        ContentValues values = new ContentValues();
        values.put(Groups._ID, g.getId());
        values.put(Groups.COLUMN_NAME,g.getGroupName());
        values.put(Groups.COLUMN_PHOTO,g.getPhotoUri());
        values.put(Groups.COLUMN_CREATED_AT,g.getCreatedAt());
        values.put(Groups.COLUMN_UPDATED_AT,g.getUpdatedAt());
        values.put(Groups.COLUMN_ID_ADMIN,g.getIdAdmin());
        return values;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
