package org.obc.core.db;

import lombok.extern.slf4j.Slf4j;

import org.obc.core.capsule.BytesCapsule;
import org.obc.core.db.obcStoreWithRevoking;
import org.obc.core.db2.common.TxCacheDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class TransactionCache extends obcStoreWithRevoking<BytesCapsule> {

  @Autowired
  public TransactionCache(@Value("trans-cache") String dbName) {
    super(new TxCacheDB(dbName));
  }
}
