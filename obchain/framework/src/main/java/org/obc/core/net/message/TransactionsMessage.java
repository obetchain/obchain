package org.obc.core.net.message;

import java.util.List;

import org.obc.core.capsule.TransactionCapsule;
import org.obc.core.net.message.MessageTypes;
import org.obc.protos.Protocol;
import org.obc.protos.Protocol.Transaction;

public class TransactionsMessage extends obcMessage {

  private Protocol.Transactions transactions;

  public TransactionsMessage(List<Transaction> obcs) {
    Protocol.Transactions.Builder builder = Protocol.Transactions.newBuilder();
    obcs.forEach(obc -> builder.addTransactions(obc));
    this.transactions = builder.build();
    this.type = MessageTypes.obcS.asByte();
    this.data = this.transactions.toByteArray();
  }

  public TransactionsMessage(byte[] data) throws Exception {
    super(data);
    this.type = MessageTypes.obcS.asByte();
    this.transactions = Protocol.Transactions.parseFrom(getCodedInputStream(data));
    if (isFilter()) {
      compareBytes(data, transactions.toByteArray());
      TransactionCapsule.validContractProto(transactions.getTransactionsList());
    }
  }

  public Protocol.Transactions getTransactions() {
    return transactions;
  }

  @Override
  public String toString() {
    return new StringBuilder().append(super.toString()).append("obc size: ")
        .append(this.transactions.getTransactionsList().size()).toString();
  }

  @Override
  public Class<?> getAnswerMessage() {
    return null;
  }

}
