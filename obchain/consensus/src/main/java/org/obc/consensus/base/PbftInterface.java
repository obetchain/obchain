package org.obc.consensus.base;

import org.obc.consensus.pbft.message.PbftBaseMessage;
import org.obc.core.capsule.BlockCapsule;

public interface PbftInterface {

  boolean isSyncing();

  void forwardMessage(PbftBaseMessage message);

  BlockCapsule getBlock(long blockNum) throws Exception;

}