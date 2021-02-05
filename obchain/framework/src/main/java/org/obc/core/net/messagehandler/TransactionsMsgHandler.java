package org.obc.core.net.messagehandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.obc.core.config.args.Args;
import org.obc.core.exception.P2pException;
import org.obc.core.exception.P2pException.TypeEnum;
import org.obc.core.net.obcNetDelegate;
import org.obc.core.net.message.TransactionMessage;
import org.obc.core.net.message.TransactionsMessage;
import org.obc.core.net.message.obcMessage;
import org.obc.core.net.peer.Item;
import org.obc.core.net.peer.PeerConnection;
import org.obc.core.net.service.AdvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.obc.protos.Protocol.Inventory.InventoryType;
import org.obc.protos.Protocol.ReasonCode;
import org.obc.protos.Protocol.Transaction;
import org.obc.protos.Protocol.Transaction.Contract.ContractType;

@Slf4j(topic = "net")
@Component
public class TransactionsMsgHandler implements obcMsgHandler {

  private static int MAX_obc_SIZE = 50_000;
  private static int MAX_SMART_CONTRACT_SUBMIT_SIZE = 100;
  @Autowired
  private obcNetDelegate obcNetDelegate;
  @Autowired
  private AdvService advService;

  //  private static int TIME_OUT = 10 * 60 * 1000;
  private BlockingQueue<obcEvent> smartContractQueue = new LinkedBlockingQueue(MAX_obc_SIZE);

  private BlockingQueue<Runnable> queue = new LinkedBlockingQueue();

  private int threadNum = Args.getInstance().getValidateSignThreadNum();
  private ExecutorService obcHandlePool = new ThreadPoolExecutor(threadNum, threadNum, 0L,
      TimeUnit.MILLISECONDS, queue);

  private ScheduledExecutorService smartContractExecutor = Executors
      .newSingleThreadScheduledExecutor();

  public void init() {
    handleSmartContract();
  }

  public void close() {
    smartContractExecutor.shutdown();
  }

  public boolean isBusy() {
    return queue.size() + smartContractQueue.size() > MAX_obc_SIZE;
  }

  @Override
  public void processMessage(PeerConnection peer, obcMessage msg) throws P2pException {
    TransactionsMessage transactionsMessage = (TransactionsMessage) msg;
    check(peer, transactionsMessage);
    for (Transaction obc : transactionsMessage.getTransactions().getTransactionsList()) {
      int type = obc.getRawData().getContract(0).getType().getNumber();
      if (type == ContractType.TriggerSmartContract_VALUE
          || type == ContractType.CreateSmartContract_VALUE) {
        if (!smartContractQueue.offer(new obcEvent(peer, new TransactionMessage(obc)))) {
          logger.warn("Add smart contract failed, queueSize {}:{}", smartContractQueue.size(),
              queue.size());
        }
      } else {
        obcHandlePool.submit(() -> handleTransaction(peer, new TransactionMessage(obc)));
      }
    }
  }

  private void check(PeerConnection peer, TransactionsMessage msg) throws P2pException {
    for (Transaction obc : msg.getTransactions().getTransactionsList()) {
      Item item = new Item(new TransactionMessage(obc).getMessageId(), InventoryType.obc);
      if (!peer.getAdvInvRequest().containsKey(item)) {
        throw new P2pException(TypeEnum.BAD_MESSAGE,
            "obc: " + msg.getMessageId() + " without request.");
      }
      peer.getAdvInvRequest().remove(item);
    }
  }

  private void handleSmartContract() {
    smartContractExecutor.scheduleWithFixedDelay(() -> {
      try {
        while (queue.size() < MAX_SMART_CONTRACT_SUBMIT_SIZE) {
          obcEvent event = smartContractQueue.take();
          obcHandlePool.submit(() -> handleTransaction(event.getPeer(), event.getMsg()));
        }
      } catch (Exception e) {
        logger.error("Handle smart contract exception.", e);
      }
    }, 1000, 20, TimeUnit.MILLISECONDS);
  }

  private void handleTransaction(PeerConnection peer, TransactionMessage obc) {
    if (peer.isDisconnect()) {
      logger.warn("Drop obc {} from {}, peer is disconnect.", obc.getMessageId(),
          peer.getInetAddress());
      return;
    }

    if (advService.getMessage(new Item(obc.getMessageId(), InventoryType.obc)) != null) {
      return;
    }

    try {
      obcNetDelegate.pushTransaction(obc.getTransactionCapsule());
      advService.broadcast(obc);
    } catch (P2pException e) {
      logger.warn("obc {} from peer {} process failed. type: {}, reason: {}",
          obc.getMessageId(), peer.getInetAddress(), e.getType(), e.getMessage());
      if (e.getType().equals(TypeEnum.BAD_obc)) {
        peer.disconnect(ReasonCode.BAD_TX);
      }
    } catch (Exception e) {
      logger.error("obc {} from peer {} process failed.", obc.getMessageId(), peer.getInetAddress(),
          e);
    }
  }

  class obcEvent {

    @Getter
    private PeerConnection peer;
    @Getter
    private TransactionMessage msg;
    @Getter
    private long time;

    public obcEvent(PeerConnection peer, TransactionMessage msg) {
      this.peer = peer;
      this.msg = msg;
      this.time = System.currentTimeMillis();
    }
  }
}