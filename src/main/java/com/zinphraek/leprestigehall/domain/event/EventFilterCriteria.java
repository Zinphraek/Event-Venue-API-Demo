package com.zinphraek.leprestigehall.domain.event;

public class EventFilterCriteria {

  private String title;

  private int maxDislikes;

  private int maxLikes;

  private int minDislikes;

  private int minLikes;

  public EventFilterCriteria() {
  }

  public EventFilterCriteria(String title, int maxDislikes, int maxLikes, int minDislikes,
                             int minLikes) {
    this.title = title;
    this.maxDislikes = maxDislikes;
    this.maxLikes = maxLikes;
    this.minDislikes = minDislikes;
    this.minLikes = minLikes;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Integer getMaxDislikes() {
    if (maxDislikes == 0) return null;
    return maxDislikes;
  }

  public void setMaxDislikes(int maxDislikes) {
    this.maxDislikes = maxDislikes;
  }

  public Integer getMaxLikes() {
    if (maxLikes == 0) return null;
    return maxLikes;
  }

  public void setMaxLikes(int maxLikes) {
    this.maxLikes = maxLikes;
  }

  public Integer getMinDislikes() {
    if (minDislikes == 0) return null;
    return minDislikes;
  }

  public void setMinDislikes(int minDislikes) {
    this.minDislikes = minDislikes;
  }

  public Integer getMinLikes() {
    if (minLikes == 0) return null;
    return minLikes;
  }

  public void setMinLikes(int minLikes) {
    this.minLikes = minLikes;
  }
}
