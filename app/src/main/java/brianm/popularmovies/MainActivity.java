package brianm.popularmovies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

  public ArrayList<MovieObject> movies;
  ProgressDialog progress;
  String sortBy = "popularity.desc";
  ImageAdapter ia;

  static final String SORT_SELECTION = "sortBySelection";
  static final String MOVIES_LIST = "movies";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    if (savedInstanceState != null) {
      sortBy = savedInstanceState.getString(SORT_SELECTION);
      movies = (ArrayList<MovieObject>) savedInstanceState.getSerializable(MOVIES_LIST);
      initialiseGrid(movies);
    }
    else{
      sortBy = "popularity.desc";
    }

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    Spinner spinner = (Spinner) findViewById(R.id.sort_spinner);

    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
      R.array.sort_by_array, android.R.layout.simple_spinner_item);


    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
          sortBy = "popularity.desc";

        } else if(position == 1){
          sortBy = "vote_average.desc";
          movies = new ArrayList<>();
        }
        else{
          loadFavorites();
          return;
        }
        new HTTPManager().execute();
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });

    setSupportActionBar(toolbar);

  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    savedInstanceState.putString(SORT_SELECTION, sortBy);
    savedInstanceState.putSerializable(MOVIES_LIST, movies);

    super.onSaveInstanceState(savedInstanceState);
  }

  public void loadFavorites(){
    MovieSQLiteHelper movieSQLiteHelper = new MovieSQLiteHelper(this);
    initialiseGrid(movieSQLiteHelper.getAllFavorites());
  }

  public ArrayList<MovieObject> getMovies(){
    return movies;
  }

  public class HTTPManager extends AsyncTask {

    private final String TMDB_API_KEY = "9fc5e9741ff75ca6ca76e3ee9bf9b0a5";
    private static final String DEBUG_TAG = "TMDBQueryManager";

    @Override
    protected void onPreExecute() {
      //progress = ProgressDialog.show(MainActivity.this, "Please wait...",
        //"Loading Movies", true);
      movies = new ArrayList<>();
    }

    @Override
    protected ArrayList<MovieObject> doInBackground(Object... params) {
      try {
        return searchIMDB();
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    protected void onPostExecute(Object result) {
      //progress.dismiss();
      initialiseGrid(movies);
    }

    public ArrayList<MovieObject> searchIMDB() throws IOException {
      // Build URL
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("http://api.themoviedb.org/3/discover/movie?sort_by=" + sortBy + "&");
      stringBuilder.append("api_key=" + TMDB_API_KEY);
      Log.e("sb", stringBuilder.toString());
      URL url = new URL(stringBuilder.toString());
      InputStream stream = null;
      try {
        // Establish a connection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.addRequestProperty("Accept", "application/json"); // Required to get TMDB to play nicely.
        conn.setDoInput(true);
        conn.connect();

        int responseCode = conn.getResponseCode();
        Log.d(DEBUG_TAG, "The response code is: " + responseCode + " " + conn.getResponseMessage());

        stream = conn.getInputStream();
        return parseResult(stringify(stream));
      } finally {
        if (stream != null) {
          stream.close();
        }
      }
    }

    private ArrayList<MovieObject> parseResult(String result) {
      String streamAsString = result;
      ArrayList<MovieObject> results = new ArrayList<>();
      try {
        JSONObject jsonObject = new JSONObject(streamAsString);
        JSONArray array = (JSONArray) jsonObject.get("results");
        for (int i = 0; i < array.length(); i++) {
          JSONObject jsonMovieObject = array.getJSONObject(i);
          String originalTitle = jsonMovieObject.getString("original_title");
          String overview = jsonMovieObject.getString("overview");
          String voteAverage = jsonMovieObject.getString("vote_average");
          String releaseDate = jsonMovieObject.getString("release_date");
          int id = jsonMovieObject.getInt("id");
          MovieObject movieBuilder = new MovieObject(jsonMovieObject.getString("poster_path"), originalTitle, overview, voteAverage, releaseDate, id);

          movies.add(movieBuilder);
        }
      } catch (JSONException e) {
        System.err.println(e);
        Log.d(DEBUG_TAG, "Error parsing JSON. String was: " + streamAsString);
      }
      return results;
    }

    public String stringify(InputStream stream) throws IOException, UnsupportedEncodingException {
      Reader reader;
      reader = new InputStreamReader(stream, "UTF-8");
      BufferedReader bufferedReader = new BufferedReader(reader);
      return bufferedReader.readLine();
    }
  }

  public void initialiseGrid(ArrayList<MovieObject> movies){
    GridView gridView = (GridView) findViewById(R.id.grid_view);
    if(ia != null){
      ia.notifyDataSetChanged();
    }
    ia = new ImageAdapter(MainActivity.this, movies);
    gridView.clearChoices();
    gridView.setAdapter(ia);
    this.movies = movies;
    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          MovieObject mo = getMovies().get(position);
          Log.e("ReviewId:", ""+mo.getId());
          Intent intent = new Intent(MainActivity.this, MovieDetails.class);
          intent.putExtra("Array", mo);
          startActivity(intent);
      }
    });
  }
}
