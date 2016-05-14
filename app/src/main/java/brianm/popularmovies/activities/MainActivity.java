package brianm.popularmovies.activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;

import brianm.popularmovies.R;
import brianm.popularmovies.fragments.MainFragment;
import brianm.popularmovies.fragments.MovieDetails;
import brianm.popularmovies.interfaces.OnMovieChanged;
import brianm.popularmovies.models.MovieObject;

public class MainActivity extends Activity implements OnMovieChanged {

  Context context;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    context = this;

    if (findViewById(R.id.fragment_container) != null){

      if (savedInstanceState != null){
        return;
      }

      MainFragment mainFragment = new MainFragment();
      mainFragment.setArguments(getIntent().getExtras());
      getFragmentManager().beginTransaction()
        .add(R.id.fragment_container, mainFragment)
        .commit();
    }
  }

  @Override
  public void OnSelectionChanged(MovieObject movieObject) {
    MovieDetails movieDetails = (MovieDetails) getFragmentManager()
      .findFragmentById(R.id.description_fragment);

    if (movieDetails != null){
      movieDetails.setMovieObject(movieObject);
    } else {
      MovieDetails newMovieDetails = new MovieDetails();
      Bundle mo = new Bundle();

      mo.putSerializable("mo", movieObject);
      newMovieDetails.setArguments(mo);
      FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

      fragmentTransaction.replace(R.id.fragment_container,newMovieDetails);
      fragmentTransaction.addToBackStack(null);
      fragmentTransaction.commit();
    }
  }
}
