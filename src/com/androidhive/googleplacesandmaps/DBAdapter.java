package com.androidhive.googleplacesandmaps;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class DBAdapter {

	class Row extends Object {
		public String getPlaceID() {
			return placeID;
		}

		public void setPlaceID(String placeID) {
			this.placeID = placeID;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public double getRating() {
			return rating;
		}

		public void setRating(double rating) {
			this.rating = rating;
		}

		public String getComment() {
			return comment;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}

		public String placeID;
		public String name;
		public double rating;
		public String comment;
	}

	private static final String DATABASE_NAME = "PLACESDB";
	private static final String DATABASE_TABLE = "PLACES";
	private static final String COLUMN_PLACEID = "placeID";
	private static final String COLUMN_NAME = "name";
	private static final String COLUMN_RATING = "rating";
	private static final String COLUMN_COMMENT = "comment";
	private static final int DATABASE_VERSION = 1;

	private static final String TAG = "NotesDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private final Context mCtx;
	private static boolean databaseExist;
	private static String BasePathSD;

	private static final String DATABASE_CREATE = "create table PLACES (placeID text primary key not null, "
			+ "name text not null,"
			+ " rating int not null, "
			+ "comment text not null);";

	public static boolean mExternalStorageAvailable = false;
	public static boolean mExternalStorageWriteable = false;

	public void checkMediaAvailability() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context, String path) {
			super(context, path + DATABASE_NAME, null, DATABASE_VERSION);
		}

		DatabaseHelper(Context context) {

			super(context, Environment.getExternalStorageDirectory()
					+ File.separator + "/kebapp/" + File.separator
					+ DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			if (!databaseExist)
				db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS PLACES");
			onCreate(db);
		}
	}

	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public DBAdapter(Context ctx) {
		// db = ctx.openOrCreateDatabase(DATABASE_NAME, DATABASE_VERSION, null);
		// db.execSQL(DATABASE_CREATE);
		this.mCtx = ctx;
		File dbFile = mCtx.getDatabasePath(DATABASE_NAME);
		System.out.println(dbFile.toString());
		databaseExist = dbFile.exists();

	}

	/**
	 * Open the places database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public DBAdapter open() throws SQLException {
		checkMediaAvailability();
		if (mExternalStorageWriteable) {
			BasePathSD = Environment.getExternalStorageDirectory()
					+ File.separator + "/kebapp/" + File.separator;
			mDbHelper = new DatabaseHelper(mCtx, BasePathSD);
		} else {
			mDbHelper = new DatabaseHelper(mCtx);
		}

		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public void createRow(String placeID, String name, double rating,
			String comment) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(COLUMN_PLACEID, placeID);
		initialValues.put(COLUMN_NAME, name);
		initialValues.put(COLUMN_RATING, rating);
		initialValues.put(COLUMN_COMMENT, comment);
		mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	public boolean rowExists(String placeID) {

		Cursor cursor = mDb.query(true, DATABASE_TABLE, new String[] {
				COLUMN_PLACEID },COLUMN_PLACEID + "=?", new String[] { placeID }, null, null,
				null, null, null);

		boolean exists = (cursor.getCount() > 0);
		cursor.close();
		return exists;
	}

	/**
	 * Delete the row with the given placeId
	 * 
	 * @param placeId
	 *            of note to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteRowById(String placeID) {
		return mDb.delete(DATABASE_TABLE, COLUMN_PLACEID + "=?",
				new String[] { placeID }) > 0;
	}

	public boolean deleteRowByName(String name) {
		return mDb.delete(DATABASE_TABLE, COLUMN_NAME + "=?",
				new String[] { name }) > 0;
	}

	public List<Row> getAllRows() {
		ArrayList<Row> ret = new ArrayList<Row>();
		try {
			Cursor c = mDb.query(DATABASE_TABLE, new String[] { COLUMN_PLACEID,
					COLUMN_NAME, COLUMN_RATING, COLUMN_COMMENT }, null, null,
					null, null, null);
			int numRows = c.getCount();
			c.moveToFirst();
			for (int i = 0; i < numRows; ++i) {
				Row row = new Row();
				// row._Id = c.getInt(0);
				row.placeID = c.getString(0);
				row.name = c.getString(1);
				row.rating = c.getDouble(2);
				row.comment = c.getString(3);
				ret.add(row);
				c.moveToNext();
			}
		} catch (SQLException e) {
			Log.e("Exception on query", e.toString());
		}
		return ret;
	}

	public Row getRow(String placeID) {
		Row row = new Row();
		Cursor c = mDb.query(true, DATABASE_TABLE, new String[] {
				COLUMN_PLACEID, COLUMN_NAME, COLUMN_RATING, COLUMN_COMMENT },
				COLUMN_PLACEID + "=?", new String[] { placeID }, null, null,
				null, null, null);

		if (c.getCount() > 0) {
			c.moveToFirst();
			// row._Id = c.getInt(0);
			row.placeID = c.getString(0);
			row.name = c.getString(1);
			row.rating = c.getDouble(2);
			row.comment = c.getString(3);
			return row;
		} else {
			// row._Id = -1;
			row.placeID = null;
			row.name = null;
			row.rating = 0;
			row.comment = null;
		}
		return row;
	}

	/**
	 * Update the place using the details provided. The place to be updated is
	 * specified using the rowId, and it is altered to use the name, rating and
	 * comment values passed in
	 * 
	 * @param rowId
	 *            id of place to update
	 * @param name
	 *            value to set place name to
	 * @param rating
	 *            value to set place rating to
	 * @param comment
	 *            value to set place comment to
	 * @return true if the note was successfully updated, false otherwise
	 */
	public boolean updateRowRatingById(String placeID, double rating) {
		ContentValues args = new ContentValues();
		args.put(COLUMN_RATING, rating);
		return mDb.update(DATABASE_TABLE, args, COLUMN_PLACEID + "=?",
				new String[] { placeID }) > 0;
	}

	public boolean updateRowRatingByName(String name, double rating) {
		ContentValues args = new ContentValues();
		args.put(COLUMN_RATING, rating);
		return mDb.update(DATABASE_TABLE, args, COLUMN_NAME + "=?",
				new String[] { name }) > 0;
	}

	public boolean updateRowCommentById(String placeID, String comment) {
		ContentValues args = new ContentValues();
		args.put(COLUMN_COMMENT, comment);
		return mDb.update(DATABASE_TABLE, args, COLUMN_PLACEID + "=?",
				new String[] { placeID }) > 0;
	}

	public boolean updateRowCommentByName(String name, String comment) {
		ContentValues args = new ContentValues();
		args.put(COLUMN_COMMENT, comment);
		return mDb.update(DATABASE_TABLE, args, COLUMN_NAME + "=?",
				new String[] { name }) > 0;
	}

	public boolean deleteAllRows() {
		return mDb.delete(DATABASE_TABLE, null, null) > 0;
	}

	public Cursor getAllRowsCursor() {
		try {
			return mDb.query(DATABASE_TABLE, new String[] { COLUMN_PLACEID,
					COLUMN_NAME, COLUMN_RATING, COLUMN_COMMENT }, null, null,
					null, null, null);
		} catch (SQLException e) {
			Log.e("Exception on query", e.toString());
			return null;
		}
	}
	
	
	public void printAllRows(){
		for(Row row : getAllRows()){
			System.out.println("placeID: "+ row.placeID + " Name: " + row.getName()+ " Rating: " + row.getRating() +" Comment: " + row.getComment());
		}
	}

}