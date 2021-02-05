package org.obc.consensus.base;

import org.obc.consensus.base.Param.Miner;
import org.obc.core.capsule.BlockCapsule;

public interface BlockHandle {

  State getState();

  Object getLock();

  BlockCapsule produce(Miner miner, long blockTime, long timeout);

}