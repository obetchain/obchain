package org.obc.core.consensus;

import lombok.extern.slf4j.Slf4j;

import org.obc.common.backup.BackupManager;
import org.obc.common.backup.BackupManager.BackupStatusEnum;
import org.obc.consensus.Consensus;
import org.obc.consensus.base.BlockHandle;
import org.obc.consensus.base.State;
import org.obc.consensus.base.Param.Miner;
import org.obc.core.capsule.BlockCapsule;
import org.obc.core.db.Manager;
import org.obc.core.net.obcNetService;
import org.obc.core.net.message.BlockMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j(topic = "consensus")
@Component
public class BlockHandleImpl implements BlockHandle {

  @Autowired
  private Manager manager;

  @Autowired
  private BackupManager backupManager;

  @Autowired
  private obcNetService obcNetService;

  @Autowired
  private Consensus consensus;

  @Override
  public State getState() {
    if (!backupManager.getStatus().equals(BackupStatusEnum.MASTER)) {
      return State.BACKUP_IS_NOT_MASTER;
    }
    return State.OK;
  }

  public Object getLock() {
    return manager;
  }

  public BlockCapsule produce(Miner miner, long blockTime, long timeout) {
    BlockCapsule blockCapsule = manager.generateBlock(miner, blockTime, timeout);
    if (blockCapsule == null) {
      return null;
    }
    try {
      consensus.receiveBlock(blockCapsule);
      BlockMessage blockMessage = new BlockMessage(blockCapsule);
      obcNetService.fastForward(blockMessage);
      manager.pushBlock(blockCapsule);
      obcNetService.broadcast(blockMessage);
    } catch (Exception e) {
      logger.error("Handle block {} failed.", blockCapsule.getBlockId().getString(), e);
      return null;
    }
    return blockCapsule;
  }
}
