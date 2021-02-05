package org.obc.core.services.ratelimiter.adapter;

import org.obc.core.services.ratelimiter.RuntimeData;

public interface IRateLimiter {

  boolean acquire(RuntimeData data);

}
