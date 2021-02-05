package org.obc.core.services.interfaceOnPBFT;

import lombok.extern.slf4j.Slf4j;

import org.obc.core.db2.core.Chainbase;
import org.obc.core.services.WalletOnCursor;
import org.springframework.stereotype.Component;

@Slf4j(topic = "API")
@Component
public class WalletOnPBFT extends WalletOnCursor {

  public WalletOnPBFT() {
    super.cursor = Chainbase.Cursor.PBFT;
  }
}
