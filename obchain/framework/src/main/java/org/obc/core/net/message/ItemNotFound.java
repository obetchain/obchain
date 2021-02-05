package org.obc.core.net.message;

import org.obc.core.net.message.MessageTypes;
import org.obc.protos.Protocol;

public class ItemNotFound extends obcMessage {

  private org.obc.protos.Protocol.Items notFound;

  /**
   * means can not find this block or obc.
   */
  public ItemNotFound() {
    Protocol.Items.Builder itemsBuilder = Protocol.Items.newBuilder();
    itemsBuilder.setType(Protocol.Items.ItemType.ERR);
    notFound = itemsBuilder.build();
    this.type = MessageTypes.ITEM_NOT_FOUND.asByte();
    this.data = notFound.toByteArray();
  }

  @Override
  public String toString() {
    return "item not found";
  }

  @Override
  public Class<?> getAnswerMessage() {
    return null;
  }

}
