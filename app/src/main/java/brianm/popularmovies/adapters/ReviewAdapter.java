package brianm.popularmovies.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import brianm.popularmovies.R;
import brianm.popularmovies.models.ReviewObject;

/**
 * Created by brianm on 17/04/2016.
 */
public class ReviewAdapter extends ArrayAdapter<ReviewObject> {

  public ReviewAdapter(Context context, int textViewResourceId) {
    super(context, textViewResourceId);
  }

  public ReviewAdapter(Context context, int resource, List<ReviewObject> reviews) {
    super(context, resource, reviews);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    View v = convertView;

    if (v == null) {
      LayoutInflater vi;
      vi = LayoutInflater.from(getContext());
      v = vi.inflate(R.layout.review_list_item, null);
    }

    ReviewObject p = getItem(position);

    if (p != null) {
      TextView author = (TextView) v.findViewById(R.id.reviewAuthor);
      TextView content = (TextView) v.findViewById(R.id.reviewContent);
      TextView url = (TextView) v.findViewById(R.id.reviewUrl);

      if (author != null) {
        author.setText(p.getAuthor());
      }

      if (content != null) {
        content.setText(p.getContent());
      }

      if (url != null) {
        url.setText(p.getUrl());
      }
    }

    return v;
  }
}
