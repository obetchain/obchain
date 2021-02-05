package org.obc.core.actuator;

import org.obc.core.exception.ContractExeException;
import org.obc.core.exception.ContractValidateException;

public interface Actuator2 {

  void execute(Object object) throws ContractExeException;

  void validate(Object object) throws ContractValidateException;
}