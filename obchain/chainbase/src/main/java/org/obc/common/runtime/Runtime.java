package org.obc.common.runtime;

import org.obc.core.db.TransactionContext;
import org.obc.core.exception.ContractExeException;
import org.obc.core.exception.ContractValidateException;


public interface Runtime {

  void execute(TransactionContext context)
      throws ContractValidateException, ContractExeException;

  ProgramResult getResult();

  String getRuntimeError();

}
