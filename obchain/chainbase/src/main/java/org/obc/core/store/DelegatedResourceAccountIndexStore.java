package org.obc.core.store;

import org.apache.commons.lang3.ArrayUtils;
import org.obc.core.capsule.DelegatedResourceAccountIndexCapsule;
import org.obc.core.db.obcStoreWithRevoking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DelegatedResourceAccountIndexStore extends
    obcStoreWithRevoking<DelegatedResourceAccountIndexCapsule> {

  @Autowired
  public DelegatedResourceAccountIndexStore(@Value("DelegatedResourceAccountIndex") String dbName) {
    super(dbName);
  }

  @Override
  public DelegatedResourceAccountIndexCapsule get(byte[] key) {

    byte[] value = revokingDB.getUnchecked(key);
    return ArrayUtils.isEmpty(value) ? null : new DelegatedResourceAccountIndexCapsule(value);
  }

}