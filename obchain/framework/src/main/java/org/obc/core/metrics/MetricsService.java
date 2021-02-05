package org.obc.core.metrics;

import lombok.extern.slf4j.Slf4j;

import org.obc.core.capsule.BlockCapsule;
import org.obc.core.metrics.blockchain.BlockChainMetricManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j(topic = "metrics")
@Component
public class MetricsService {

  @Autowired
  private BlockChainMetricManager blockChainMetricManager;

  /**
   * apply block.
   *
   * @param block BlockCapsule
   */
  public void applyBlock(BlockCapsule block) {
    try {
      blockChainMetricManager.applyBlock(block);
    } catch (Exception e) {
      logger.warn("record block failed, {}, reason: {}.",
          block.getBlockId().toString(), e.getMessage());
    }
  }

  /**
   * fail process block.
   *
   * @param blockNum long
   * @param errorInfo String
   */
  public void failProcessBlock(long blockNum, String errorInfo) {
    try {
      blockChainMetricManager.setFailProcessBlockNum(blockNum);
      blockChainMetricManager.setFailProcessBlockReason(errorInfo);
    } catch (Exception e) {
      logger.warn("record fail process block failed, {}, reason: {}.",
          blockNum, errorInfo);
    }
  }

  /**
   * get metrics info.
   *
   * @return MetricsInfo
   */
  public MetricsInfo getMetricsInfo() {
    return new MetricsInfo();
  }

}
