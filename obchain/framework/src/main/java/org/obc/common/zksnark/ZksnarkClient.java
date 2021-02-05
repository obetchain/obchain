package org.obc.common.zksnark;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannelBuilder;

import org.obc.core.capsule.TransactionCapsule;
import org.obc.api.obcZksnarkGrpc;
import org.obc.api.ZksnarkGrpcAPI.ZksnarkRequest;
import org.obc.api.ZksnarkGrpcAPI.ZksnarkResponse.Code;
import org.obc.protos.Protocol.Transaction;

public class ZksnarkClient {

  public static final ZksnarkClient instance = new ZksnarkClient();

  private obcZksnarkGrpc.obcZksnarkBlockingStub blockingStub;

  public ZksnarkClient() {
    blockingStub = obcZksnarkGrpc.newBlockingStub(ManagedChannelBuilder
        .forTarget("127.0.0.1:60051")
        .usePlaintext()
        .build());
  }

  public static ZksnarkClient getInstance() {
    return instance;
  }

  public boolean checkZksnarkProof(Transaction transaction, byte[] sighash, long valueBalance) {
    String txId = new TransactionCapsule(transaction).getTransactionId().toString();
    ZksnarkRequest request = ZksnarkRequest.newBuilder()
        .setTransaction(transaction)
        .setTxId(txId)
        .setSighash(ByteString.copyFrom(sighash))
        .setValueBalance(valueBalance)
        .build();
    return blockingStub.checkZksnarkProof(request).getCode() == Code.SUCCESS;
  }
}
