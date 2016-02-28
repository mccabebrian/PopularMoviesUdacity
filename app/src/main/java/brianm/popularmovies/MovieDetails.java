package brianm.popularmovies;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MovieDetails extends Activity {
  TextView title;
  ImageView movieImage;
  TextView release;
  TextView rating;
  TextView overview;

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

    release.setText(mo.getReleaseDate());
    rating.setText(mo.getVoteAverage() + "/10");
    overview.setText(mo.getOverview());
  }

}
