package brianm.popularmovies;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
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

public class MovieDetails extends Activity {

  TextView title;
  ImageView movieImage;
  TextView release;
  TextView rating;
  TextView overview;
  int movieId;

  public ArrayList<ReviewObject> reviews;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_movie_details);

    title = (TextView) findViewById(R.id.titleText);
    movieImage = (ImageView) findViewById(R.id.thumbnail);
    release = (TextView) findViewById(R.id.release);
    rating = (TextView) findViewById(R.id.rating);
    overview = (TextView) findViewById(R.id.overview);

    MovieObject mo = (MovieObject) getIntent().getSerializableExtra("Array");

    Log.e("Overview", mo.getOverview());

    title.setText(mo.getOriginalTitle());
    Picasso.with(this)
      .load("http://image.tmdb.org/t/p/w185/" + mo.getImagePath())
      .into(movieImage);

    movieId = mo.getId();
    release.setText(mo.getReleaseDate());
    rating.setText(mo.getVoteAverage() + "/10");
    overview.setText(mo.getOverview());
    new HTTPManager().execute();

  }

  public ArrayList<ReviewObject> getReviews(){
    return reviews;
  }

  public class HTTPManager extends AsyncTask {

    private final String TMDB_API_KEY = "9fc5e9741ff75ca6ca76e3ee9bf9b0a5";
    private static final String DEBUG_TAG = "TMDBQueryManager";

    @Override
    protected void onPreExecute() {
      //progress = ProgressDialog.show(MainActivity.this, "Please wait...",
      //"Loading Movies", true);
      reviews = new ArrayList<>();
    }

    @Override
    protected ArrayList<ReviewObject> doInBackground(Object... params) {
      try {
        return searchIMDB();
      } catch (IOException e) {
        return null;
      }
    }

    @Override
    protected void onPostExecute(Object result) {
      Log.d("test string", result.toString());
      //progress.dismiss();
      ReviewObject ro = getReviews().get(0);
      Log.e("review1: ", ro.getContent());
    }

    public ArrayList<ReviewObject> searchIMDB() throws IOException {
      // Build URL
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
          Log.e("content", content);
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
      Reader reader = null;
      reader = new InputStreamReader(stream, "UTF-8");
      BufferedReader bufferedReader = new BufferedReader(reader);
      return bufferedReader.readLine();
    }
  }

}
