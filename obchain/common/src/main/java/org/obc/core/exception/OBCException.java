package org.obc.core.exception;

public class OBCException extends Exception {

  public OBCException() {
    super();
  }

  public OBCException(String message) {
    super(message);
  }

  public OBCException(String message, Throwable cause) {
    super(message, cause);
  }

}
