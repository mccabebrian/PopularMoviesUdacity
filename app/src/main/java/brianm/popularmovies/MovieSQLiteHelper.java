package brianm.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by brianm on 18/04/2016.
 */
public class MovieSQLiteHelper extends SQLiteOpenHelper {


  // database version
  private static final int database_VERSION = 3;
  // database name
  private static final String database_NAME = "MovieDB";
  private static final String table_FAVORITES = "favorites";
  private static final String favorites_ID = "id";
  private static final String movie_ID = "movieId";
  private static final String movie_IMAGE_PATH = "imagePath";
  private static final String movie_ORIGINAL_TITLE = "originalTitle";
  private static final String movie_OVERVIEW = "overview";
  private static final String movie_VOTE_AVERAGE = "voteAverage";
  private static final String movie_RELEASE_DATE = "releaseDate";

  private static final String[] COLUMNS = { favorites_ID, movie_ID, movie_IMAGE_PATH, movie_ORIGINAL_TITLE, movie_OVERVIEW, movie_VOTE_AVERAGE, movie_RELEASE_DATE };

  public MovieSQLiteHelper(Context context) {
    super(context, database_NAME, null, database_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    // SQL statement to create book table
    String CREATE_FAVORITES_TABLE = "CREATE TABLE favorites ( " + "id INTEGER PRIMARY KEY AUTOINCREMENT, " + "movieId TEXT, " + "imagePath TEXT, " +
      "originalTitle TEXT, " + "overview TEXT, " + "voteAverage TEXT, " + "releaseDate TEXT )";
    db.execSQL(CREATE_FAVORITES_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // drop books table if already exists
    db.execSQL("DROP TABLE IF EXISTS " + table_FAVORITES);
    String CREATE_FAVORITES_TABLE = "CREATE TABLE favorites ( " + "id INTEGER PRIMARY KEY AUTOINCREMENT, " + "movieId TEXT, " + "imagePath TEXT, " +
      "originalTitle TEXT, " + "overview TEXT, " + "voteAverage TEXT, " + "releaseDate TEXT )";
    db.execSQL(CREATE_FAVORITES_TABLE);

  }

  public void createFavorite(MovieObject movie) {
    // get reference of the BookDB database
    SQLiteDatabase db = this.getWritableDatabase();

    // make values to be inserted
    ContentValues values = new ContentValues();
    values.put(movie_ID, movie.getId());
    values.put(movie_IMAGE_PATH, movie.getImagePath());
    values.put(movie_ORIGINAL_TITLE, movie.getOriginalTitle());
    values.put(movie_OVERVIEW, movie.getOverview());
    values.put(movie_VOTE_AVERAGE, movie.getVoteAverage());
    values.put(movie_RELEASE_DATE, movie.getReleaseDate());

    // insert book
    db.insert(table_FAVORITES, null, values);

    // close database transaction
    db.close();
  }

  public MovieObject readFavorite(int id) {
    // get reference of the BookDB database
    SQLiteDatabase db = this.getReadableDatabase();

    // get book query
    Cursor cursor = db.rawQuery( "select * from " + table_FAVORITES + " where " + movie_ID +" = " + id, null );
    MovieObject movieObject = new MovieObject();
    // if results !=null, parse the first one
    if (cursor != null && cursor.moveToFirst()) {
        movieObject.setId(Integer.parseInt(cursor.getString(0)));
        cursor.close();
    }
    else{
      movieObject = null;
    }

    return movieObject;
  }

  public ArrayList<MovieObject> getAllFavorites() {
    ArrayList<MovieObject> movies = new ArrayList<>();

    // select book query
    String query = "SELECT  * FROM " + table_FAVORITES;

    // get reference of the BookDB database
    SQLiteDatabase db = this.getWritableDatabase();
    Cursor cursor = db.rawQuery(query, null);

    // parse all results
    MovieObject movieObject;
    if (cursor.moveToFirst()) {
      do {
        movieObject = new MovieObject();
        movieObject.setId(Integer.parseInt(cursor.getString(1)));
        movieObject.setImagePath(cursor.getString(2));
        movieObject.setOriginalTitle(cursor.getString(3));
        movieObject.setOverview(cursor.getString(4));
        movieObject.setVoteAverage(cursor.getString(5));
        movieObject.setReleaseDate(cursor.getString(6));

        // Add book to books
        movies.add(movieObject);
      } while (cursor.moveToNext());
    }
    return movies;
  }

  public int updateBook(MovieObject movieObject) {

    // get reference of the BookDB database
    SQLiteDatabase db = this.getWritableDatabase();

    // make values to be inserted
    ContentValues values = new ContentValues();
    values.put("movieId", movieObject.getId()); // get title

    // update
    int i = db.update(table_FAVORITES, values, favorites_ID + " = ?", new String[] { String.valueOf(movieObject.getId()) });

    db.close();
    return i;
  }

  // Deleting single book
  public void deleteBook(MovieObject movieObject) {

    // get reference of the BookDB database
    SQLiteDatabase db = this.getWritableDatabase();

    // delete book
    db.delete(table_FAVORITES, favorites_ID + " = ?", new String[] { String.valueOf(movieObject.getId()) });
    db.close();
  }
}
