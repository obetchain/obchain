package org.obc.core.net.messagehandler;

import org.obc.core.exception.P2pException;
import org.obc.core.net.message.obcMessage;
import org.obc.core.net.peer.PeerConnection;

public interface OBCMsgHandler {

  void processMessage(PeerConnection peer, obcMessage msg) throws P2pException;

}
