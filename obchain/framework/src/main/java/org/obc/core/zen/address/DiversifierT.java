package org.obc.core.zen.address;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.obc.common.zksnark.JLibrustzcash;
import org.obc.core.Constant;
import org.obc.core.exception.ZksnarkException;
import org.obc.keystore.Wallet;

@AllArgsConstructor
public class DiversifierT {

  @Setter
  @Getter
  private byte[] data = new byte[Constant.ZC_DIVERSIFIER_SIZE];

  public DiversifierT() {
  }

  public static DiversifierT random() throws ZksnarkException {
    byte[] d;
    while (true) {
      d = Wallet.generateRandomBytes(Constant.ZC_DIVERSIFIER_SIZE);
      if (JLibrustzcash.librustzcashCheckDiversifier(d)) {
        break;
      }
    }
    return new DiversifierT(d);
  }
}
