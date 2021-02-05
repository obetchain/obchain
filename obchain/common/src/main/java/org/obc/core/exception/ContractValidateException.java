package org.obc.core.exception;

public class ContractValidateException extends obcException {

  public ContractValidateException() {
    super();
  }

  public ContractValidateException(String message) {
    super(message);
  }

  public ContractValidateException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
