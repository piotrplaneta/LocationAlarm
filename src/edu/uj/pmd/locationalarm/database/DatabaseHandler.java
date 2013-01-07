package edu.uj.pmd.locationalarm.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * User: piotrplaneta
 * Date: 29.12.2012
 * Time: 16:10
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "locationAlarmFavorites";

    private static final String TABLE_FAVORITES = "favoriteDestinations";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_LATITUDE = "latitude";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_FAVORITES + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"+ KEY_NAME + " TEXT," + KEY_LONGITUDE + " REAL,"
                + KEY_LATITUDE + " REAL" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }

    public void addFavoriteDestination(Destination destination) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, destination.getName());
        values.put(KEY_LONGITUDE, destination.getLongitude());
        values.put(KEY_LATITUDE, destination.getLatitude());

        database.insert(TABLE_FAVORITES, null, values);
        database.close();
    }

    public Destination getFavoriteDestination(int id) {
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(TABLE_FAVORITES, new String[] { KEY_ID,
                KEY_NAME, KEY_LONGITUDE, KEY_LATITUDE }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Destination destination = new Destination(Integer.parseInt(cursor.getString(0)), cursor.getString(1),
                cursor.getDouble(2), cursor.getDouble(3));

        cursor.close();

        database.close();
        return destination;
    }

    public List<Destination> getAllFavoriteDestinations() {
        List<Destination> favoriteDestinationsList = new ArrayList<Destination>();

        String selectQuery = "SELECT  * FROM " + TABLE_FAVORITES;

        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Destination destination = new Destination();
                destination.setId(Integer.parseInt(cursor.getString(0)));
                destination.setName(cursor.getString(1));
                destination.setLongitude(cursor.getDouble(2));
                destination.setLatitude(cursor.getDouble(3));

                favoriteDestinationsList.add(destination);
            } while (cursor.moveToNext());
        }
        cursor.close();

        database.close();
        return favoriteDestinationsList;
    }

    public int getFavoriteDestinationsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_FAVORITES;
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery(countQuery, null);


        int count = cursor.getCount();

        cursor.close();
        database.close();
        return count;
    }

    public void deleteFavoriteDestination(Destination destination) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES, KEY_ID + " = ?",
                new String[] { String.valueOf(destination.getId()) });
        db.close();
    }
}
