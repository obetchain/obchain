package org.obc.core.capsule;

public interface ProtoCapsule<T> {

  byte[] getData();

  T getInstance();
}
