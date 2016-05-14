package brianm.popularmovies.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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

import brianm.popularmovies.R;
import brianm.popularmovies.adapters.ReviewAdapter;
import brianm.popularmovies.helpers.MovieSQLiteHelper;
import brianm.popularmovies.activities.MovieObject;
import brianm.popularmovies.activities.ReviewObject;

public class MovieDetails extends Fragment {

  TextView title;
  ImageView movieImage;
  TextView release;
  TextView rating;
  TextView overview;
  int movieId;
  Button favoriteButton;
  MovieObject mo;
  MovieSQLiteHelper movieSQLiteHelper;
  Button trailer;
  static ArrayList<String> keys;
  ListView reviewList;

  public ArrayList<ReviewObject> reviews;

  static final String KEYS = "keys";
  static final String REVIEWS = "reviews";

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState != null) {
      keys = savedInstanceState.getStringArrayList(KEYS);
      reviews = (ArrayList<ReviewObject>) savedInstanceState.getSerializable(REVIEWS);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    if (savedInstanceState != null){
    }
    View view = inflater.inflate(R.layout.activity_movie_details, container, false);

    title = (TextView) view.findViewById(R.id.titleText);
    movieImage = (ImageView) view.findViewById(R.id.thumbnail);
    release = (TextView) view.findViewById(R.id.release);
    rating = (TextView) view.findViewById(R.id.rating);
    overview = (TextView) view.findViewById(R.id.overview);
    trailer = (Button) view.findViewById(R.id.trailers);
    reviewList = (ListView) view.findViewById(R.id.reviews);
    favoriteButton = (Button) view.findViewById(R.id.favorites);

    Bundle bundle=getArguments();

    if(bundle != null) {
      MovieObject movieObject = (MovieObject) bundle.getSerializable("mo");

      setMovieObject(movieObject);
    }

    return view;
  }

  public void setMovieObject(MovieObject movieObject){

    movieSQLiteHelper = new MovieSQLiteHelper(getActivity());

    this.mo = movieObject;

    if(!(mo == null)) {
      initialiseFavoriteButton();

      title.setText(mo.getOriginalTitle());
      Picasso.with(getActivity())
        .load("http://image.tmdb.org/t/p/w185/" + mo.getImagePath())
        .into(movieImage);

      movieId = mo.getId();
      release.setText(mo.getReleaseDate());
      rating.setText(mo.getVoteAverage() + "/10");
      overview.setText(mo.getOverview());
      new HTTPManager().execute();
      new TrailerManager().execute();

      trailer.setEnabled(false);
    }
  }

  public void watchYoutubeVideo(String key){
    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + key)));
  }

  public void initialiseFavoriteButton(){

    MovieObject movieObject = movieSQLiteHelper.readFavorite(mo.getId());
    if(movieObject != null){
      favoriteButton.setEnabled(false);
      disableFavoriteButton();
    }
    else{
      favoriteButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          createFavorite();
          disableFavoriteButton();
        }
      });
    }
  }

  public void disableFavoriteButton(){
    favoriteButton.setEnabled(false);
    favoriteButton.setBackgroundColor(Color.parseColor("#d7f1ff"));
  }

  public void disableTrailerButton(){
    trailer.setEnabled(false);
  }

  public void createFavorite(){
    movieSQLiteHelper.createFavorite(mo);
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    savedInstanceState.putStringArrayList(KEYS, keys);
    savedInstanceState.putSerializable(REVIEWS, reviews);

    super.onSaveInstanceState(savedInstanceState);
  }

  public class HTTPManager extends AsyncTask {

    private final String TMDB_API_KEY = "";
    private static final String DEBUG_TAG = "TMDBQueryManager";

    @Override
    protected void onPreExecute() {
      reviews = new ArrayList<>();
    }

    @Override
    protected ArrayList<ReviewObject> doInBackground(Object... params) {
      try {
        return searchIMDB();
      } catch (IOException e) {
        Log.e("exception:", e.getMessage());
        return null;
      }
    }

    @Override
    protected void onPostExecute(Object result) {
      ListAdapter reviewAdapter = new ReviewAdapter(getActivity(), R.layout.review_list_item, reviews);

      reviewList.setAdapter(reviewAdapter);
    }

    public ArrayList<ReviewObject> searchIMDB() throws IOException {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("http://api.themoviedb.org/3/movie/"+movieId+"/reviews?");
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

    private ArrayList<ReviewObject> parseResult(String result) {
      String streamAsString = result;
      ArrayList<ReviewObject> results = new ArrayList<>();
      try {
        JSONObject jsonObject = new JSONObject(streamAsString);
        JSONArray array = (JSONArray) jsonObject.get("results");
        for (int i = 0; i < array.length(); i++) {
          JSONObject jsonReviewObject = array.getJSONObject(i);
          String id = jsonReviewObject.getString("id");
          String author = jsonReviewObject.getString("author");
          String content = jsonReviewObject.getString("content");
          String url = jsonReviewObject.getString("url");
          ReviewObject reviewObject = new ReviewObject(id, content, url, author);

          reviews.add(reviewObject);
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

  public class TrailerManager extends AsyncTask {

    private final String TMDB_API_KEY = "";
    private static final String DEBUG_TAG = "TMDBQueryManager";

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected ArrayList<String> doInBackground(Object... params) {
      try {
        return searchIMDB();
      } catch (IOException e) {
        Log.e("exception:", e.getMessage());
        return null;
      }
    }

    @Override
    protected void onPostExecute(Object result) {
      keys = (ArrayList<String>) result;
      trailer.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if(keys != null && (keys.size() > 0)) {
            watchYoutubeVideo(keys.get(0));
          }else{
            disableTrailerButton();
          }
        }
      });
      trailer.setEnabled(true);
    }

    public ArrayList<String> searchIMDB() throws IOException {
      StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("http://api.themoviedb.org/3/movie/"+movieId+"/videos?");
      stringBuilder.append("api_key=" + TMDB_API_KEY);
      Log.e("sb", stringBuilder.toString());
      URL url = new URL(stringBuilder.toString());
      InputStream stream = null;
      try {
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

    private ArrayList<String> parseResult(String result) {
      String streamAsString = result;
      ArrayList<String> results = new ArrayList<>();
      try {
        JSONObject jsonObject = new JSONObject(streamAsString);
        JSONArray array = (JSONArray) jsonObject.get("results");
        for (int i = 0; i < array.length(); i++) {
          JSONObject jsonReviewObject = array.getJSONObject(i);
          String key = jsonReviewObject.getString("key");

          results.add(key);
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




}
