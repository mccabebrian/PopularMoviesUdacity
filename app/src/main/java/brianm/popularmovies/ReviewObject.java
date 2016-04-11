package brianm.popularmovies;

import java.io.Serializable;

/**
 * Created by brianm on 11/04/2016.
 */
public class ReviewObject implements Serializable {
  String id;
  String content;
  String url;
  String author;

  public ReviewObject(String id, String content, String url, String author) {
    this.id = id;
    this.content = content;
    this.url = url;
    this.author = author;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }
}
