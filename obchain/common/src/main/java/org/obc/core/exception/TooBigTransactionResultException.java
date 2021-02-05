package org.obc.core.exception;

public class TooBigTransactionResultException extends obcException {

  public TooBigTransactionResultException() {
    super("too big transaction result");
  }

  public TooBigTransactionResultException(String message) {
    super(message);
  }
}
