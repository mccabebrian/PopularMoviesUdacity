package brianm.popularmovies.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import brianm.popularmovies.interfaces.OnMovieChanged;
import brianm.popularmovies.R;
import brianm.popularmovies.adapters.ImageAdapter;
import brianm.popularmovies.helpers.MovieSQLiteHelper;
import brianm.popularmovies.models.MovieObject;

public class MainFragment extends Fragment {

  public ArrayList<MovieObject> movies;
  String sortBy = "popularity.desc";
  ImageAdapter ia;
  GridView gridView;

  static final String SORT_SELECTION = "sortBySelection";
  static final String MOVIES_LIST = "movies";

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState != null) {
      sortBy = savedInstanceState.getString(SORT_SELECTION);
      movies = (ArrayList<MovieObject>) savedInstanceState.getSerializable(MOVIES_LIST);
      initialiseGrid(movies);
    }

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_main, container, false);

    gridView = (GridView) view.findViewById(R.id.grid_view);

    Spinner spinner = (Spinner) view.findViewById(R.id.sort_spinner);

    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
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

    return view;
  }


  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    savedInstanceState.putString(SORT_SELECTION, sortBy);
    savedInstanceState.putSerializable(MOVIES_LIST, movies);

    super.onSaveInstanceState(savedInstanceState);
  }

  public void loadFavorites(){
    MovieSQLiteHelper movieSQLiteHelper = new MovieSQLiteHelper(getActivity());
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
        conn.addRequestProperty("Accept", "application/json");
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

    if(ia != null){
      ia.notifyDataSetChanged();
    }
    ia = new ImageAdapter(getActivity(), movies);
    gridView.clearChoices();
    gridView.setAdapter(ia);
    this.movies = movies;
    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          MovieObject mo = getMovies().get(position);
          Log.e("ReviewId:", "" + mo.getId());
        OnMovieChanged listener = (OnMovieChanged) getActivity();
        listener.OnSelectionChanged(mo);
      }
    });
  }
}
