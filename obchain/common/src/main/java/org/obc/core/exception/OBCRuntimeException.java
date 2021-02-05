package org.obc.core.exception;

public class OBCRuntimeException extends RuntimeException {

  public OBCRuntimeException() {
    super();
  }

  public OBCRuntimeException(String message) {
    super(message);
  }

  public OBCRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  public OBCRuntimeException(Throwable cause) {
    super(cause);
  }

  protected OBCRuntimeException(String message, Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }


}
