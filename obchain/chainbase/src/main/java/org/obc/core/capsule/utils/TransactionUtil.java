/*
 * java-obc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * java-obc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obc.core.capsule.utils;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.obc.common.parameter.CommonParameter;
import org.obc.common.runtime.InternalTransaction;
import org.obc.common.runtime.ProgramResult;
import org.obc.common.runtime.vm.LogInfo;
import org.obc.common.utils.DecodeUtil;
import org.obc.core.capsule.BlockCapsule;
import org.obc.core.capsule.ReceiptCapsule;
import org.obc.core.capsule.TransactionCapsule;
import org.obc.core.capsule.TransactionInfoCapsule;
import org.obc.core.db.TransactionTrace;
import org.obc.protos.Protocol;
import org.obc.protos.Protocol.Transaction;
import org.obc.protos.Protocol.Transaction.Contract;
import org.obc.protos.Protocol.TransactionInfo;
import org.obc.protos.Protocol.TransactionInfo.Log;
import org.obc.protos.Protocol.TransactionInfo.code;
import org.obc.protos.contract.BalanceContract.TransferContract;

@Slf4j(topic = "capsule")
public class TransactionUtil {

  public static Transaction newGenesisTransaction(byte[] key, long value)
      throws IllegalArgumentException {

    if (!DecodeUtil.addressValid(key)) {
      throw new IllegalArgumentException("Invalid address");
    }
    TransferContract transferContract = TransferContract.newBuilder()
        .setAmount(value)
        .setOwnerAddress(ByteString.copyFrom("0x000000000000000000000".getBytes()))
        .setToAddress(ByteString.copyFrom(key))
        .build();

    return new TransactionCapsule(transferContract,
        Contract.ContractType.TransferContract).getInstance();
  }

  public static TransactionInfoCapsule buildTransactionInfoInstance(TransactionCapsule obcCap,
      BlockCapsule block, TransactionTrace trace) {

    TransactionInfo.Builder builder = TransactionInfo.newBuilder();
    ReceiptCapsule traceReceipt = trace.getReceipt();
    builder.setResult(code.SUCESS);
    if (StringUtils.isNoneEmpty(trace.getRuntimeError()) || Objects
        .nonNull(trace.getRuntimeResult().getException())) {
      builder.setResult(code.FAILED);
      builder.setResMessage(ByteString.copyFromUtf8(trace.getRuntimeError()));
    }
    builder.setId(ByteString.copyFrom(obcCap.getTransactionId().getBytes()));
    ProgramResult programResult = trace.getRuntimeResult();
    long fee =
        programResult.getRet().getFee() + traceReceipt.getEnergyFee()
            + traceReceipt.getNetFee() + traceReceipt.getMultiSignFee();
    ByteString contractResult = ByteString.copyFrom(programResult.getHReturn());
    ByteString ContractAddress = ByteString.copyFrom(programResult.getContractAddress());

    builder.setFee(fee);
    builder.addContractResult(contractResult);
    builder.setContractAddress(ContractAddress);
    builder.setUnfreezeAmount(programResult.getRet().getUnfreezeAmount());
    builder.setAssetIssueID(programResult.getRet().getAssetIssueID());
    builder.setExchangeId(programResult.getRet().getExchangeId());
    builder.setWithdrawAmount(programResult.getRet().getWithdrawAmount());
    builder.setExchangeReceivedAmount(programResult.getRet().getExchangeReceivedAmount());
    builder.setExchangeInjectAnotherAmount(programResult.getRet().getExchangeInjectAnotherAmount());
    builder.setExchangeWithdrawAnotherAmount(
        programResult.getRet().getExchangeWithdrawAnotherAmount());
    builder.setShieldedTransactionFee(programResult.getRet().getShieldedTransactionFee());
    builder.setOrderId(programResult.getRet().getOrderId());
    builder.addAllOrderDetails(programResult.getRet().getOrderDetailsList());

    List<Log> logList = new ArrayList<>();
    programResult.getLogInfoList().forEach(
        logInfo -> {
          logList.add(LogInfo.buildLog(logInfo));
        }
    );
    builder.addAllLog(logList);

    if (Objects.nonNull(block)) {
      builder.setBlockNumber(block.getInstance().getBlockHeader().getRawData().getNumber());
      builder.setBlockTimeStamp(block.getInstance().getBlockHeader().getRawData().getTimestamp());
    }

    builder.setReceipt(traceReceipt.getReceipt());

    if (CommonParameter.getInstance().isSaveInternalTx() && null != programResult
        .getInternalTransactions()) {
      for (InternalTransaction internalTransaction : programResult
          .getInternalTransactions()) {
        Protocol.InternalTransaction.Builder internalobcBuilder = Protocol.InternalTransaction
            .newBuilder();
        // set hash
        internalobcBuilder.setHash(ByteString.copyFrom(internalTransaction.getHash()));
        // set caller
        internalobcBuilder.setCallerAddress(ByteString.copyFrom(internalTransaction.getSender()));
        // set TransferTo
        internalobcBuilder
            .setTransferToAddress(ByteString.copyFrom(internalTransaction.getTransferToAddress()));
        //TODO: "for loop" below in future for multiple token case, we only have one for now.
        Protocol.InternalTransaction.CallValueInfo.Builder callValueInfoBuilder =
            Protocol.InternalTransaction.CallValueInfo.newBuilder();
        // obc will not be set token name
        callValueInfoBuilder.setCallValue(internalTransaction.getValue());
        // Just one transferBuilder for now.
        internalobcBuilder.addCallValueInfo(callValueInfoBuilder);
        internalTransaction.getTokenInfo().forEach((tokenId, amount) -> {
          internalobcBuilder.addCallValueInfo(
              Protocol.InternalTransaction.CallValueInfo.newBuilder().setTokenId(tokenId)
                  .setCallValue(amount));
        });
        // Token for loop end here
        internalobcBuilder.setNote(ByteString.copyFrom(internalTransaction.getNote().getBytes()));
        internalobcBuilder.setRejected(internalTransaction.isRejected());
        builder.addInternalTransactions(internalobcBuilder);
      }
    }

    return new TransactionInfoCapsule(builder.build());
  }

  public static boolean isNumber(byte[] id) {
    if (ArrayUtils.isEmpty(id)) {
      return false;
    }
    for (byte b : id) {
      if (b < '0' || b > '9') {
        return false;
      }
    }

    return !(id.length > 1 && id[0] == '0');
  }
}
