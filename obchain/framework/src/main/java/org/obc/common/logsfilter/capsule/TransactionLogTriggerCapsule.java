package org.obc.common.logsfilter.capsule;

import static org.obc.protos.Protocol.Transaction.Contract.ContractType.TransferAssetContract;
import static org.obc.protos.Protocol.Transaction.Contract.ContractType.TransferContract;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.obc.common.logsfilter.EventPluginLoader;
import org.obc.common.logsfilter.trigger.InternalTransactionPojo;
import org.obc.common.logsfilter.trigger.TransactionLogTrigger;
import org.obc.common.runtime.InternalTransaction;
import org.obc.common.runtime.ProgramResult;
import org.obc.common.utils.StringUtil;
import org.obc.core.capsule.BlockCapsule;
import org.obc.core.capsule.TransactionCapsule;
import org.obc.core.db.TransactionTrace;
import org.spongycastle.util.encoders.Hex;
import org.obc.protos.Protocol;
import org.obc.protos.contract.AssetIssueContractOuterClass.TransferAssetContract;
import org.obc.protos.contract.BalanceContract.TransferContract;

@Slf4j
public class TransactionLogTriggerCapsule extends TriggerCapsule {

  @Getter
  @Setter
  private TransactionLogTrigger transactionLogTrigger;

  public TransactionLogTriggerCapsule(TransactionCapsule obcCapsule, BlockCapsule blockCapsule) {
    transactionLogTrigger = new TransactionLogTrigger();
    if (Objects.nonNull(blockCapsule)) {
      transactionLogTrigger.setBlockHash(blockCapsule.getBlockId().toString());
    }
    transactionLogTrigger.setTransactionId(obcCapsule.getTransactionId().toString());
    transactionLogTrigger.setTimeStamp(blockCapsule.getTimeStamp());
    transactionLogTrigger.setBlockNumber(obcCapsule.getBlockNum());
    transactionLogTrigger.setData(Hex.toHexString(obcCapsule
        .getInstance().getRawData().getData().toByteArray()));

    TransactionTrace obcTrace = obcCapsule.getobcTrace();

    //result
    if (Objects.nonNull(obcCapsule.getContractRet())) {
      transactionLogTrigger.setResult(obcCapsule.getContractRet().toString());
    }

    if (Objects.nonNull(obcCapsule.getInstance().getRawData())) {
      // fee limit
      transactionLogTrigger.setFeeLimit(obcCapsule.getInstance().getRawData().getFeeLimit());

      Protocol.Transaction.Contract contract = obcCapsule.getInstance().getRawData().getContract(0);
      Any contractParameter = null;
      // contract type
      if (Objects.nonNull(contract)) {
        Protocol.Transaction.Contract.ContractType contractType = contract.getType();
        if (Objects.nonNull(contractType)) {
          transactionLogTrigger.setContractType(contractType.toString());
        }

        contractParameter = contract.getParameter();

        transactionLogTrigger.setContractCallValue(TransactionCapsule.getCallValue(contract));
      }

      if (Objects.nonNull(contractParameter) && Objects.nonNull(contract)) {
        try {
          if (contract.getType() == TransferContract) {
            TransferContract contractTransfer = contractParameter.unpack(TransferContract.class);

            if (Objects.nonNull(contractTransfer)) {
              transactionLogTrigger.setAssetName("obc");

              if (Objects.nonNull(contractTransfer.getOwnerAddress())) {
                transactionLogTrigger.setFromAddress(StringUtil
                    .encode58Check(contractTransfer.getOwnerAddress().toByteArray()));
              }

              if (Objects.nonNull(contractTransfer.getToAddress())) {
                transactionLogTrigger.setToAddress(
                    StringUtil.encode58Check(contractTransfer.getToAddress().toByteArray()));
              }

              transactionLogTrigger.setAssetAmount(contractTransfer.getAmount());
            }

          } else if (contract.getType() == TransferAssetContract) {
            TransferAssetContract contractTransfer = contractParameter
                .unpack(TransferAssetContract.class);

            if (Objects.nonNull(contractTransfer)) {
              if (Objects.nonNull(contractTransfer.getAssetName())) {
                transactionLogTrigger.setAssetName(contractTransfer.getAssetName().toStringUtf8());
              }

              if (Objects.nonNull(contractTransfer.getOwnerAddress())) {
                transactionLogTrigger.setFromAddress(
                    StringUtil.encode58Check(contractTransfer.getOwnerAddress().toByteArray()));
              }

              if (Objects.nonNull(contractTransfer.getToAddress())) {
                transactionLogTrigger.setToAddress(StringUtil
                    .encode58Check(contractTransfer.getToAddress().toByteArray()));
              }
              transactionLogTrigger.setAssetAmount(contractTransfer.getAmount());
            }
          }
        } catch (Exception e) {
          logger.error("failed to load transferAssetContract, error'{}'", e);
        }
      }
    }

    // receipt
    if (Objects.nonNull(obcTrace) && Objects.nonNull(obcTrace.getReceipt())) {
      transactionLogTrigger.setEnergyFee(obcTrace.getReceipt().getEnergyFee());
      transactionLogTrigger.setOriginEnergyUsage(obcTrace.getReceipt().getOriginEnergyUsage());
      transactionLogTrigger.setEnergyUsageTotal(obcTrace.getReceipt().getEnergyUsageTotal());
      transactionLogTrigger.setNetUsage(obcTrace.getReceipt().getNetUsage());
      transactionLogTrigger.setNetFee(obcTrace.getReceipt().getNetFee());
      transactionLogTrigger.setEnergyUsage(obcTrace.getReceipt().getEnergyUsage());
    }

    // program result
    if (Objects.nonNull(obcTrace) && Objects.nonNull(obcTrace.getRuntime()) && Objects
        .nonNull(obcTrace.getRuntime().getResult())) {
      ProgramResult programResult = obcTrace.getRuntime().getResult();
      ByteString contractResult = ByteString.copyFrom(programResult.getHReturn());
      ByteString contractAddress = ByteString.copyFrom(programResult.getContractAddress());

      if (Objects.nonNull(contractResult) && contractResult.size() > 0) {
        transactionLogTrigger.setContractResult(Hex.toHexString(contractResult.toByteArray()));
      }

      if (Objects.nonNull(contractAddress) && contractAddress.size() > 0) {
        transactionLogTrigger
            .setContractAddress(StringUtil.encode58Check((contractAddress.toByteArray())));
      }

      // internal transaction
      transactionLogTrigger.setInternalTransactionList(
          getInternalTransactionList(programResult.getInternalTransactions()));
    }
  }

  public void setLatestSolidifiedBlockNumber(long latestSolidifiedBlockNumber) {
    transactionLogTrigger.setLatestSolidifiedBlockNumber(latestSolidifiedBlockNumber);
  }

  private List<InternalTransactionPojo> getInternalTransactionList(
      List<InternalTransaction> internalTransactionList) {
    List<InternalTransactionPojo> pojoList = new ArrayList<>();

    internalTransactionList.forEach(internalTransaction -> {
      InternalTransactionPojo item = new InternalTransactionPojo();

      item.setHash(Hex.toHexString(internalTransaction.getHash()));
      item.setCallValue(internalTransaction.getValue());
      item.setTokenInfo(internalTransaction.getTokenInfo());
      item.setCaller_address(Hex.toHexString(internalTransaction.getSender()));
      item.setTransferTo_address(Hex.toHexString(internalTransaction.getTransferToAddress()));
      item.setData(Hex.toHexString(internalTransaction.getData()));
      item.setRejected(internalTransaction.isRejected());
      item.setNote(internalTransaction.getNote());

      pojoList.add(item);
    });

    return pojoList;
  }

  @Override
  public void processTrigger() {
    EventPluginLoader.getInstance().postTransactionTrigger(transactionLogTrigger);
  }
}
