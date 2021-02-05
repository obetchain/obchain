package org.obc.core.net.message;

import org.obc.common.overlay.message.Message;
import org.obc.common.utils.Sha256Hash;
import org.obc.core.capsule.TransactionCapsule;
import org.obc.core.net.message.MessageTypes;
import org.obc.protos.Protocol.Transaction;

public class TransactionMessage extends obcMessage {

  private TransactionCapsule transactionCapsule;

  public TransactionMessage(byte[] data) throws Exception {
    super(data);
    this.transactionCapsule = new TransactionCapsule(getCodedInputStream(data));
    this.type = MessageTypes.obc.asByte();
    if (Message.isFilter()) {
      compareBytes(data, transactionCapsule.getInstance().toByteArray());
      transactionCapsule
          .validContractProto(transactionCapsule.getInstance().getRawData().getContract(0));
    }
  }

  public TransactionMessage(Transaction obc) {
    this.transactionCapsule = new TransactionCapsule(obc);
    this.type = MessageTypes.obc.asByte();
    this.data = obc.toByteArray();
  }

  @Override
  public String toString() {
    return new StringBuilder().append(super.toString())
        .append("messageId: ").append(super.getMessageId()).toString();
  }

  @Override
  public Sha256Hash getMessageId() {
    return this.transactionCapsule.getTransactionId();
  }

  @Override
  public Class<?> getAnswerMessage() {
    return null;
  }

  public TransactionCapsule getTransactionCapsule() {
    return this.transactionCapsule;
  }
}
