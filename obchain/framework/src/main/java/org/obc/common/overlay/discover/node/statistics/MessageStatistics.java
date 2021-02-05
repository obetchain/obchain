package org.obc.common.overlay.discover.node.statistics;

import lombok.extern.slf4j.Slf4j;

import org.obc.common.net.udp.message.UdpMessageTypeEnum;
import org.obc.common.overlay.message.Message;
import org.obc.core.net.message.FetchInvDataMessage;
import org.obc.core.net.message.InventoryMessage;
import org.obc.core.net.message.MessageTypes;
import org.obc.core.net.message.TransactionsMessage;

@Slf4j
public class MessageStatistics {

  //udp discovery
  public final MessageCount discoverInPing = new MessageCount();
  public final MessageCount discoverOutPing = new MessageCount();
  public final MessageCount discoverInPong = new MessageCount();
  public final MessageCount discoverOutPong = new MessageCount();
  public final MessageCount discoverInFindNode = new MessageCount();
  public final MessageCount discoverOutFindNode = new MessageCount();
  public final MessageCount discoverInNeighbours = new MessageCount();
  public final MessageCount discoverOutNeighbours = new MessageCount();

  //tcp p2p
  public final MessageCount p2pInHello = new MessageCount();
  public final MessageCount p2pOutHello = new MessageCount();
  public final MessageCount p2pInPing = new MessageCount();
  public final MessageCount p2pOutPing = new MessageCount();
  public final MessageCount p2pInPong = new MessageCount();
  public final MessageCount p2pOutPong = new MessageCount();
  public final MessageCount p2pInDisconnect = new MessageCount();
  public final MessageCount p2pOutDisconnect = new MessageCount();

  //tcp obc
  public final MessageCount obcInMessage = new MessageCount();
  public final MessageCount obcOutMessage = new MessageCount();

  public final MessageCount obcInSyncBlockChain = new MessageCount();
  public final MessageCount obcOutSyncBlockChain = new MessageCount();
  public final MessageCount obcInBlockChainInventory = new MessageCount();
  public final MessageCount obcOutBlockChainInventory = new MessageCount();

  public final MessageCount obcInobcInventory = new MessageCount();
  public final MessageCount obcOutobcInventory = new MessageCount();
  public final MessageCount obcInobcInventoryElement = new MessageCount();
  public final MessageCount obcOutobcInventoryElement = new MessageCount();

  public final MessageCount obcInBlockInventory = new MessageCount();
  public final MessageCount obcOutBlockInventory = new MessageCount();
  public final MessageCount obcInBlockInventoryElement = new MessageCount();
  public final MessageCount obcOutBlockInventoryElement = new MessageCount();

  public final MessageCount obcInobcFetchInvData = new MessageCount();
  public final MessageCount obcOutobcFetchInvData = new MessageCount();
  public final MessageCount obcInobcFetchInvDataElement = new MessageCount();
  public final MessageCount obcOutobcFetchInvDataElement = new MessageCount();

  public final MessageCount obcInBlockFetchInvData = new MessageCount();
  public final MessageCount obcOutBlockFetchInvData = new MessageCount();
  public final MessageCount obcInBlockFetchInvDataElement = new MessageCount();
  public final MessageCount obcOutBlockFetchInvDataElement = new MessageCount();


  public final MessageCount obcInobc = new MessageCount();
  public final MessageCount obcOutobc = new MessageCount();
  public final MessageCount obcInobcs = new MessageCount();
  public final MessageCount obcOutobcs = new MessageCount();
  public final MessageCount obcInBlock = new MessageCount();
  public final MessageCount obcOutBlock = new MessageCount();
  public final MessageCount obcOutAdvBlock = new MessageCount();

  public void addUdpInMessage(UdpMessageTypeEnum type) {
    addUdpMessage(type, true);
  }

  public void addUdpOutMessage(UdpMessageTypeEnum type) {
    addUdpMessage(type, false);
  }

  public void addTcpInMessage(Message msg) {
    addTcpMessage(msg, true);
  }

  public void addTcpOutMessage(Message msg) {
    addTcpMessage(msg, false);
  }

  private void addUdpMessage(UdpMessageTypeEnum type, boolean flag) {
    switch (type) {
      case DISCOVER_PING:
        if (flag) {
          discoverInPing.add();
        } else {
          discoverOutPing.add();
        }
        break;
      case DISCOVER_PONG:
        if (flag) {
          discoverInPong.add();
        } else {
          discoverOutPong.add();
        }
        break;
      case DISCOVER_FIND_NODE:
        if (flag) {
          discoverInFindNode.add();
        } else {
          discoverOutFindNode.add();
        }
        break;
      case DISCOVER_NEIGHBORS:
        if (flag) {
          discoverInNeighbours.add();
        } else {
          discoverOutNeighbours.add();
        }
        break;
      default:
        break;
    }
  }

  private void addTcpMessage(Message msg, boolean flag) {

    if (flag) {
      obcInMessage.add();
    } else {
      obcOutMessage.add();
    }

    switch (msg.getType()) {
      case P2P_HELLO:
        if (flag) {
          p2pInHello.add();
        } else {
          p2pOutHello.add();
        }
        break;
      case P2P_PING:
        if (flag) {
          p2pInPing.add();
        } else {
          p2pOutPing.add();
        }
        break;
      case P2P_PONG:
        if (flag) {
          p2pInPong.add();
        } else {
          p2pOutPong.add();
        }
        break;
      case P2P_DISCONNECT:
        if (flag) {
          p2pInDisconnect.add();
        } else {
          p2pOutDisconnect.add();
        }
        break;
      case SYNC_BLOCK_CHAIN:
        if (flag) {
          obcInSyncBlockChain.add();
        } else {
          obcOutSyncBlockChain.add();
        }
        break;
      case BLOCK_CHAIN_INVENTORY:
        if (flag) {
          obcInBlockChainInventory.add();
        } else {
          obcOutBlockChainInventory.add();
        }
        break;
      case INVENTORY:
        InventoryMessage inventoryMessage = (InventoryMessage) msg;
        int inventorySize = inventoryMessage.getInventory().getIdsCount();
        messageProcess(inventoryMessage.getInvMessageType(),
                obcInobcInventory,obcInobcInventoryElement,obcInBlockInventory,
                obcInBlockInventoryElement,obcOutobcInventory,obcOutobcInventoryElement,
                obcOutBlockInventory,obcOutBlockInventoryElement,
                flag, inventorySize);
        break;
      case FETCH_INV_DATA:
        FetchInvDataMessage fetchInvDataMessage = (FetchInvDataMessage) msg;
        int fetchSize = fetchInvDataMessage.getInventory().getIdsCount();
        messageProcess(fetchInvDataMessage.getInvMessageType(),
                obcInobcFetchInvData,obcInobcFetchInvDataElement,obcInBlockFetchInvData,
                obcInBlockFetchInvDataElement,obcOutobcFetchInvData,obcOutobcFetchInvDataElement,
                obcOutBlockFetchInvData,obcOutBlockFetchInvDataElement,
                flag, fetchSize);
        break;
      case obcS:
        TransactionsMessage transactionsMessage = (TransactionsMessage) msg;
        if (flag) {
          obcInobcs.add();
          obcInobc.add(transactionsMessage.getTransactions().getTransactionsCount());
        } else {
          obcOutobcs.add();
          obcOutobc.add(transactionsMessage.getTransactions().getTransactionsCount());
        }
        break;
      case obc:
        if (flag) {
          obcInMessage.add();
        } else {
          obcOutMessage.add();
        }
        break;
      case BLOCK:
        if (flag) {
          obcInBlock.add();
        }
        obcOutBlock.add();
        break;
      default:
        break;
    }
  }
  
  
  private void messageProcess(MessageTypes messageType,
                              MessageCount inobc,
                              MessageCount inobcEle,
                              MessageCount inBlock,
                              MessageCount inBlockEle,
                              MessageCount outobc,
                              MessageCount outobcEle,
                              MessageCount outBlock,
                              MessageCount outBlockEle,
                              boolean flag, int size) {
    if (flag) {
      if (messageType == MessageTypes.obc) {
        inobc.add();
        inobcEle.add(size);
      } else {
        inBlock.add();
        inBlockEle.add(size);
      }
    } else {
      if (messageType == MessageTypes.obc) {
        outobc.add();
        outobcEle.add(size);
      } else {
        outBlock.add();
        outBlockEle.add(size);
      }
    }
  }

}
