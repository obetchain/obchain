package org.obc.consensus.pbft;

import com.google.protobuf.ByteString;
import java.util.Deque;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

import org.obc.consensus.pbft.message.PbftMessage;
import org.obc.core.ChainBaseManager;
import org.obc.core.capsule.PbftSignCapsule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.obc.protos.Protocol.PBFTMessage.Raw;

@Slf4j(topic = "pbft")
@Component
public class PbftMessageAction {

  @Autowired
  private ChainBaseManager chainBaseManager;

  public void action(PbftMessage message, List<ByteString> dataSignList) {
    switch (message.getDataType()) {
      case BLOCK: {
        long blockNum = message.getNumber();
        chainBaseManager.getCommonDataBase().saveLatestPbftBlockNum(blockNum);
        Raw raw = message.getPbftMessage().getRawData();
        chainBaseManager.getPbftSignDataStore()
            .putBlockSignData(blockNum, new PbftSignCapsule(raw.toByteString(), dataSignList));
        logger.info("commit msg block num is:{}", blockNum);
      }
      break;
      case SRL: {
        try {
          Raw raw = message.getPbftMessage().getRawData();
          chainBaseManager.getPbftSignDataStore().putSrSignData(message.getEpoch(),
              new PbftSignCapsule(raw.toByteString(), dataSignList));
          logger.info("sr commit msg :{}, epoch:{}", message.getNumber(), message.getEpoch());
        } catch (Exception e) {
          logger.error("process the sr list error!", e);
        }
      }
      break;
      default:
        break;
    }
  }

}