package com.zinphraek.leprestigehall.utilities.exceptions;

/**
 * Custom error for internal service errors
 */
public class ServerError extends RuntimeException {

  public ServerError() {
  }

  public ServerError(String message) {
    super(message);
  }
}
