package com.zinphraek.leprestigehall.utilities.helpers;

import org.springframework.data.domain.Sort;

public class CustomPage {

  private int pageNumber = 0;
  private int pageSize = 10;
  private Sort.Direction sortDirection = Sort.Direction.ASC;
  private String sortBy = "id";

  public CustomPage(int pageNumber, int pageSize, Sort.Direction sortDirection, String sortBy) {
    this.pageNumber = pageNumber;
    this.pageSize = pageSize;
    this.sortDirection = sortDirection;
    this.sortBy = sortBy;
  }

  public CustomPage() {}

  public int getPageNumber() {
    return pageNumber;
  }

  public void setPageNumber(int pageNumber) {
    this.pageNumber = pageNumber;
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public Sort.Direction getSortDirection() {
    return sortDirection;
  }

  public void setSortDirection(Sort.Direction sortDirection) {
    this.sortDirection = sortDirection;
  }

  public String getSortBy() {
    return sortBy;
  }

  public void setSortBy(String sortBy) {
    this.sortBy = sortBy;
  }
}
