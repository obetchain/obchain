package org.obc.core.net.messagehandler;

import java.util.ArrayList;
import org.junit.Test;
import org.obc.core.net.message.InventoryMessage;
import org.obc.core.net.messagehandler.InventoryMsgHandler;
import org.obc.core.net.peer.PeerConnection;
import org.obc.protos.Protocol.Inventory.InventoryType;

public class InventoryMsgHandlerTest {

  private InventoryMsgHandler handler = new InventoryMsgHandler();
  private PeerConnection peer = new PeerConnection();

  @Test
  public void testProcessMessage() {
    InventoryMessage msg = new InventoryMessage(new ArrayList<>(), InventoryType.obc);

    peer.setNeedSyncFromPeer(true);
    peer.setNeedSyncFromUs(true);
    handler.processMessage(peer, msg);

    peer.setNeedSyncFromPeer(true);
    peer.setNeedSyncFromUs(false);
    handler.processMessage(peer, msg);

    peer.setNeedSyncFromPeer(false);
    peer.setNeedSyncFromUs(true);
    handler.processMessage(peer, msg);

  }
}
