package org.obc.core.store;

import com.google.common.collect.Streams;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.obc.core.capsule.ExchangeCapsule;
import org.obc.core.db.obcStoreWithRevoking;
import org.obc.core.exception.ItemNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ExchangeStore extends obcStoreWithRevoking<ExchangeCapsule> {

  @Autowired
  protected ExchangeStore(@Value("exchange") String dbName) {
    super(dbName);
  }

  @Override
  public ExchangeCapsule get(byte[] key) throws ItemNotFoundException {
    byte[] value = revokingDB.get(key);
    return new ExchangeCapsule(value);
  }

  /**
   * get all exchanges.
   */
  public List<ExchangeCapsule> getAllExchanges() {
    return Streams.stream(iterator())
        .map(Map.Entry::getValue)
        .sorted(
            (ExchangeCapsule a, ExchangeCapsule b) -> a.getCreateTime() <= b.getCreateTime() ? 1
                : -1)
        .collect(Collectors.toList());
  }
}