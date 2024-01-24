package com.zinphraek.leprestigehall.utilities.tables.invoice;

public class InvoiceTableColumnData {
  private String columnName; // The display name for the header
  private String columnKey; // The key used to access row data
  private int span; // Grid column span

  public InvoiceTableColumnData() {}

  public InvoiceTableColumnData(String columnName, String columnKey, int span) {
    this.columnName = columnName;
    this.columnKey = columnKey;
    this.span = span;
  }

  public String getColumnKey() {
    return columnKey;
  }

  public void setColumnKey(String columnKey) {
    this.columnKey = columnKey;
  }

  public String getColumnName() {
    return columnName;
  }

  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

  public int getSpan() {
    return span;
  }

  public void setSpan(int span) {
    this.span = span;
  }
}
