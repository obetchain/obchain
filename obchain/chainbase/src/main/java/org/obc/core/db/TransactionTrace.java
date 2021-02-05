package org.obc.core.db;

import static org.obc.common.runtime.InternalTransaction.obcType.obc_CONTRACT_CALL_TYPE;
import static org.obc.common.runtime.InternalTransaction.obcType.obc_CONTRACT_CREATION_TYPE;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.obc.common.parameter.CommonParameter;
import org.obc.common.runtime.ProgramResult;
import org.obc.common.runtime.Runtime;
import org.obc.common.runtime.InternalTransaction.obcType;
import org.obc.common.runtime.vm.DataWord;
import org.obc.common.utils.DecodeUtil;
import org.obc.common.utils.ForkController;
import org.obc.common.utils.Sha256Hash;
import org.obc.common.utils.StringUtil;
import org.obc.common.utils.WalletUtil;
import org.obc.core.Constant;
import org.obc.core.capsule.AccountCapsule;
import org.obc.core.capsule.BlockCapsule;
import org.obc.core.capsule.ContractCapsule;
import org.obc.core.capsule.ReceiptCapsule;
import org.obc.core.capsule.TransactionCapsule;
import org.obc.core.exception.BalanceInsufficientException;
import org.obc.core.exception.ContractExeException;
import org.obc.core.exception.ContractValidateException;
import org.obc.core.exception.ReceiptCheckErrException;
import org.obc.core.exception.VMIllegalException;
import org.obc.core.store.*;
import org.spongycastle.util.encoders.Hex;
import org.springframework.util.StringUtils;
import org.obc.protos.Protocol.Transaction;
import org.obc.protos.Protocol.Transaction.Contract.ContractType;
import org.obc.protos.Protocol.Transaction.Result.contractResult;
import org.obc.protos.contract.SmartContractOuterClass.SmartContract.ABI;
import org.obc.protos.contract.SmartContractOuterClass.TriggerSmartContract;

@Slf4j(topic = "TransactionTrace")
public class TransactionTrace {

  private TransactionCapsule obc;

  private ReceiptCapsule receipt;

  private StoreFactory storeFactory;

  private DynamicPropertiesStore dynamicPropertiesStore;

  private ContractStore contractStore;

  private AccountStore accountStore;

  private CodeStore codeStore;

  private EnergyProcessor energyProcessor;

  private obcType obcType;

  private long txStartTimeInMs;

  private Runtime runtime;

  private ForkController forkController;

  private VotesStore votesStore;

  private DelegationStore delegationStore;

  @Getter
  private TransactionContext transactionContext;
  @Getter
  @Setter
  private TimeResultType timeResultType = TimeResultType.NORMAL;

  public TransactionTrace(TransactionCapsule obc, StoreFactory storeFactory,
      Runtime runtime) {
    this.obc = obc;
    Transaction.Contract.ContractType contractType = this.obc.getInstance().getRawData()
        .getContract(0).getType();
    switch (contractType.getNumber()) {
      case ContractType.TriggerSmartContract_VALUE:
        obcType = obc_CONTRACT_CALL_TYPE;
        break;
      case ContractType.CreateSmartContract_VALUE:
        obcType = obc_CONTRACT_CREATION_TYPE;
        break;
      default:
        obcType = obcType.obc_PRECOMPILED_TYPE;
    }
    this.storeFactory = storeFactory;
    this.dynamicPropertiesStore = storeFactory.getChainBaseManager().getDynamicPropertiesStore();
    this.contractStore = storeFactory.getChainBaseManager().getContractStore();
    this.codeStore = storeFactory.getChainBaseManager().getCodeStore();
    this.accountStore = storeFactory.getChainBaseManager().getAccountStore();

    this.receipt = new ReceiptCapsule(Sha256Hash.ZERO_HASH);
    this.energyProcessor = new EnergyProcessor(dynamicPropertiesStore, accountStore);
    this.runtime = runtime;
    this.forkController = new ForkController();
    forkController.init(storeFactory.getChainBaseManager());

    this.votesStore = storeFactory.getChainBaseManager().getVotesStore();
    this.delegationStore = storeFactory.getChainBaseManager().getDelegationStore();
  }

  public TransactionCapsule getobc() {
    return obc;
  }

  private boolean needVM() {
    return this.obcType == obc_CONTRACT_CALL_TYPE
        || this.obcType == obc_CONTRACT_CREATION_TYPE;
  }

  public void init(BlockCapsule blockCap) {
    init(blockCap, false);
  }

  //pre transaction check
  public void init(BlockCapsule blockCap, boolean eventPluginLoaded) {
    txStartTimeInMs = System.currentTimeMillis();
    transactionContext = new TransactionContext(blockCap, obc, storeFactory, false,
        eventPluginLoaded);
  }

  public void checkIsConstant() throws ContractValidateException, VMIllegalException {
    if (dynamicPropertiesStore.getAllowTvmConstantinople() == 1) {
      return;
    }
    TriggerSmartContract triggerContractFromTransaction = ContractCapsule
        .getTriggerContractFromTransaction(this.getobc().getInstance());
    if (obc_CONTRACT_CALL_TYPE == this.obcType) {
      ContractCapsule contract = contractStore
          .get(triggerContractFromTransaction.getContractAddress().toByteArray());
      if (contract == null) {
        logger.info("contract: {} is not in contract store", StringUtil
            .encode58Check(triggerContractFromTransaction.getContractAddress().toByteArray()));
        throw new ContractValidateException("contract: " + StringUtil
            .encode58Check(triggerContractFromTransaction.getContractAddress().toByteArray())
            + " is not in contract store");
      }
      ABI abi = contract.getInstance().getAbi();
      if (WalletUtil.isConstant(abi, triggerContractFromTransaction)) {
        throw new VMIllegalException("cannot call constant method");
      }
    }
  }

  //set bill
  public void setBill(long energyUsage) {
    if (energyUsage < 0) {
      energyUsage = 0L;
    }
    receipt.setEnergyUsageTotal(energyUsage);
  }

  //set net bill
  public void setNetBill(long netUsage, long netFee) {
    receipt.setNetUsage(netUsage);
    receipt.setNetFee(netFee);
  }

  public void addNetBill(long netFee) {
    receipt.addNetFee(netFee);
  }

  public void exec()
      throws ContractExeException, ContractValidateException, VMIllegalException {
    /*  VM execute  */
    runtime.execute(transactionContext);
    setBill(transactionContext.getProgramResult().getEnergyUsed());

    if (obcType.obc_PRECOMPILED_TYPE != obcType) {
      if (contractResult.OUT_OF_TIME
          .equals(receipt.getResult())) {
        setTimeResultType(TimeResultType.OUT_OF_TIME);
      } else if (System.currentTimeMillis() - txStartTimeInMs
          > CommonParameter.getInstance()
          .getLongRunningTime()) {
        setTimeResultType(TimeResultType.LONG_RUNNING);
      }
    }
  }

  public void finalization() throws ContractExeException {
    try {
      pay();
    } catch (BalanceInsufficientException e) {
      throw new ContractExeException(e.getMessage());
    }
    if (StringUtils.isEmpty(transactionContext.getProgramResult().getRuntimeError())) {
      for (DataWord contract : transactionContext.getProgramResult().getDeleteAccounts()) {
        deleteContract(convertToobcAddress((contract.getLast20Bytes())));
      }
      for (DataWord address : transactionContext.getProgramResult().getDeleteVotes()) {
        votesStore.delete(convertToobcAddress((address.getLast20Bytes())));
      }
      for (DataWord address : transactionContext.getProgramResult().getDeleteDelegation()) {
        deleteDelegationByAddress(convertToobcAddress((address.getLast20Bytes())));
      }
    }
  }

  /**
   * pay actually bill(include ENERGY and storage).
   */
  public void pay() throws BalanceInsufficientException {
    byte[] originAccount;
    byte[] callerAccount;
    long percent = 0;
    long originEnergyLimit = 0;
    switch (obcType) {
      case obc_CONTRACT_CREATION_TYPE:
        callerAccount = TransactionCapsule.getOwner(obc.getInstance().getRawData().getContract(0));
        originAccount = callerAccount;
        break;
      case obc_CONTRACT_CALL_TYPE:
        TriggerSmartContract callContract = ContractCapsule
            .getTriggerContractFromTransaction(obc.getInstance());
        ContractCapsule contractCapsule =
            contractStore.get(callContract.getContractAddress().toByteArray());

        callerAccount = callContract.getOwnerAddress().toByteArray();
        originAccount = contractCapsule.getOriginAddress();
        percent = Math
            .max(Constant.ONE_HUNDRED - contractCapsule.getConsumeUserResourcePercent(), 0);
        percent = Math.min(percent, Constant.ONE_HUNDRED);
        originEnergyLimit = contractCapsule.getOriginEnergyLimit();
        break;
      default:
        return;
    }

    // originAccount Percent = 30%
    AccountCapsule origin = accountStore.get(originAccount);
    AccountCapsule caller = accountStore.get(callerAccount);
    receipt.payEnergyBill(
        dynamicPropertiesStore, accountStore, forkController,
        origin,
        caller,
        percent, originEnergyLimit,
        energyProcessor,
        EnergyProcessor.getHeadSlot(dynamicPropertiesStore));
  }

  public boolean checkNeedRetry() {
    if (!needVM()) {
      return false;
    }
    return obc.getContractRet() != contractResult.OUT_OF_TIME && receipt.getResult()
        == contractResult.OUT_OF_TIME;
  }

  public void check() throws ReceiptCheckErrException {
    if (!needVM()) {
      return;
    }
    if (Objects.isNull(obc.getContractRet())) {
      throw new ReceiptCheckErrException("null resultCode");
    }
    if (!obc.getContractRet().equals(receipt.getResult())) {
      logger.info(
          "this tx id: {}, the resultCode in received block: {}, the resultCode in self: {}",
          Hex.toHexString(obc.getTransactionId().getBytes()), obc.getContractRet(),
          receipt.getResult());
      throw new ReceiptCheckErrException("Different resultCode");
    }
  }

  public ReceiptCapsule getReceipt() {
    return receipt;
  }

  public void setResult() {
    if (!needVM()) {
      return;
    }
    receipt.setResult(transactionContext.getProgramResult().getResultCode());
  }

  public String getRuntimeError() {
    return transactionContext.getProgramResult().getRuntimeError();
  }

  public ProgramResult getRuntimeResult() {
    return transactionContext.getProgramResult();
  }

  public Runtime getRuntime() {
    return runtime;
  }

  public void deleteContract(byte[] address) {
    codeStore.delete(address);
    accountStore.delete(address);
    contractStore.delete(address);
  }

  public static byte[] convertToobcAddress(byte[] address) {
    if (address.length == 20) {
      byte[] newAddress = new byte[21];
      byte[] temp = new byte[]{DecodeUtil.addressPreFixByte};
      System.arraycopy(temp, 0, newAddress, 0, temp.length);
      System.arraycopy(address, 0, newAddress, temp.length, address.length);
      address = newAddress;
    }
    return address;
  }

  public void deleteDelegationByAddress(byte[] address){
    delegationStore.delete(address); //begin Cycle
    delegationStore.delete(("lastWithdraw-" + Hex.toHexString(address)).getBytes()); //last Withdraw cycle
    delegationStore.delete(("end-" + Hex.toHexString(address)).getBytes()); //end cycle
  }


  public enum TimeResultType {
    NORMAL,
    LONG_RUNNING,
    OUT_OF_TIME
  }
}
