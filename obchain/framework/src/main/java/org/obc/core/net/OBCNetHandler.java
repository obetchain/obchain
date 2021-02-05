package org.obc.core.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.obc.common.overlay.server.Channel;
import org.obc.common.overlay.server.MessageQueue;
import org.obc.core.net.message.obcMessage;
import org.obc.core.net.peer.PeerConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class obcNetHandler extends SimpleChannelInboundHandler<obcMessage> {

  protected PeerConnection peer;

  private MessageQueue msgQueue;

  @Autowired
  private obcNetService obcNetService;

  @Override
  public void channelRead0(final ChannelHandlerContext ctx, obcMessage msg) throws Exception {
    msgQueue.receivedMessage(msg);
    obcNetService.onMessage(peer, msg);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    peer.processException(cause);
  }

  public void setMsgQueue(MessageQueue msgQueue) {
    this.msgQueue = msgQueue;
  }

  public void setChannel(Channel channel) {
    this.peer = (PeerConnection) channel;
  }

}