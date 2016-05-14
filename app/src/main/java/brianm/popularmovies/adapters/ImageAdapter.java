package brianm.popularmovies.adapters;

/**
 * Created by brianm on 20/02/2016.
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import brianm.popularmovies.R;
import brianm.popularmovies.models.MovieObject;

public class ImageAdapter extends BaseAdapter {
  private Context mContext;
  private final LayoutInflater mInflater;
  ArrayList<MovieObject> movies;

  public Integer[] mThumbIds = {
    R.drawable.deadpool, R.drawable.jw,
    R.drawable.mm, R.drawable.spectre,
    R.drawable.spectre, R.drawable.mm,
    R.drawable.jw, R.drawable.deadpool,
    R.drawable.mm, R.drawable.spectre,
    R.drawable.deadpool, R.drawable.mm,
    R.drawable.spectre, R.drawable.deadpool,
    R.drawable.jw
  };

  public ImageAdapter(Context c, ArrayList<MovieObject> movies){
    mContext = c;
    mInflater = LayoutInflater.from(c);
    this.movies = movies;
  }

  @Override
  public int getCount() {
    return movies.size();
  }

  @Override
  public Object getItem(int position) {
    return mThumbIds[position];
  }

  @Override
  public long getItemId(int position) {
    return 0;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View v = convertView;

    if (v == null) {
      v = mInflater.inflate(R.layout.grid_item, parent, false);
      v.setTag(R.id.picture, v.findViewById(R.id.picture));
    }

    ImageView imageView;
    imageView = (ImageView) v.getTag(R.id.picture);
    Picasso.with(mContext)
      .load("http://image.tmdb.org/t/p/w185/" + movies.get(position).getImagePath())
      .placeholder(R.drawable.noimg)
      .error(R.drawable.noimg)
      .into(imageView);
    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
    imageView.setAdjustViewBounds(true);
    return v;
  }

}