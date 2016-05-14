package brianm.popularmovies.interfaces;

import brianm.popularmovies.models.MovieObject;

/**
 * Created by brianm on 13/05/2016.
 */
public interface OnMovieChanged {
  void OnSelectionChanged(MovieObject movieObject);
}
