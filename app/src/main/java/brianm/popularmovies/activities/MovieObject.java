package brianm.popularmovies.activities;

import java.io.Serializable;

/**
 * Created by brianm on 21/02/2016.
 */
public class MovieObject implements Serializable{
  String imagePath;
  String originalTitle;
  String overview;
  String voteAverage;
  String releaseDate;
  int id;

  public MovieObject(){

  }

  public MovieObject(String imagePath, String originalTitle, String overview, String voteAverage, String releaseDate, int id){
    this.imagePath = imagePath;
    this.originalTitle = originalTitle;
    this.overview = overview;
    this.voteAverage = voteAverage;
    this.releaseDate = releaseDate;
    this.id = id;
  }

  public String getImagePath(){
    return imagePath;
  }

  public void setImagePath(String imagePath) {
    this.imagePath = imagePath;
  }

  public String getReleaseDate() {
    return releaseDate;
  }

  public void setReleaseDate(String releaseDate) {
    this.releaseDate = releaseDate;
  }

  public String getOriginalTitle() {
    return originalTitle;
  }

  public void setOriginalTitle(String originalTitle) {
    this.originalTitle = originalTitle;
  }

  public String getOverview() {
    return overview;
  }

  public void setOverview(String overview) {
    this.overview = overview;
  }

  public String getVoteAverage() {
    return voteAverage;
  }

  public void setVoteAverage(String voteAverage) {
    this.voteAverage = voteAverage;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }
}
