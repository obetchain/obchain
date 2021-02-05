package org.obc.core.actuator;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

import org.obc.common.utils.ForkController;
import org.obc.core.ChainBaseManager;
import org.obc.core.actuator.Actuator;
import org.obc.core.actuator.TransactionFactory;
import org.obc.core.capsule.TransactionCapsule;
import org.obc.core.exception.ContractValidateException;
import org.obc.core.store.StoreFactory;
import org.obc.protos.Protocol;
import org.obc.protos.Protocol.Transaction.Contract;

@Slf4j(topic = "actuator")
public class ActuatorCreator {

  private ForkController forkController = new ForkController();

  private ChainBaseManager chainBaseManager;

  private ActuatorCreator(StoreFactory storeFactory) {
    chainBaseManager = storeFactory.getChainBaseManager();
    forkController.init(storeFactory.getChainBaseManager());
  }

  public static ActuatorCreator getINSTANCE() {
    if (ActuatorCreatorInner.instance == null) {
      ActuatorCreatorInner.instance = new ActuatorCreator(StoreFactory.getInstance());
    }
    return ActuatorCreatorInner.instance;
  }

  public static void init() {
    ActuatorCreatorInner.instance = new ActuatorCreator(StoreFactory.getInstance());
  }

  /**
   * create actuator.
   */
  public List<Actuator> createActuator(TransactionCapsule transactionCapsule)
      throws ContractValidateException {
    List<Actuator> actuatorList = Lists.newArrayList();
    if (null == transactionCapsule || null == transactionCapsule.getInstance()) {
      logger.info("TransactionCapsule or Transaction is null");
      return actuatorList;
    }

    Protocol.Transaction.raw rawData = transactionCapsule.getInstance().getRawData();
    for (Contract contract : rawData.getContractList()) {
      try {
        actuatorList.add(getActuatorByContract(contract, transactionCapsule));
      } catch (Exception e) {
        logger.error("", e);
        throw new ContractValidateException(e.getMessage());
      }
    }
    return actuatorList;
  }

  private Actuator getActuatorByContract(Contract contract,
      TransactionCapsule tx)
      throws IllegalAccessException, InstantiationException, ContractValidateException {
    Class<? extends Actuator> clazz = TransactionFactory.getActuator(contract.getType());
    if (clazz == null) {
      throw new ContractValidateException("not exist contract " + contract);
    }
    AbstractActuator abstractActuator = (AbstractActuator) clazz.newInstance();
    abstractActuator.setChainBaseManager(chainBaseManager).setContract(contract)
        .setForkUtils(forkController).setTx(tx);
    return abstractActuator;
  }

  private static class ActuatorCreatorInner {

    private static ActuatorCreator instance;
  }
}
