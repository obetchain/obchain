package org.obc.common;

import org.obc.common.overlay.discover.DiscoverServer;
import org.obc.common.overlay.discover.node.NodeManager;
import org.obc.common.overlay.server.ChannelManager;
import org.obc.core.db.Manager;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class OBCApplicationContext extends AnnotationConfigApplicationContext {

  public OBCApplicationContext() {
  }

  public OBCApplicationContext(DefaultListableBeanFactory beanFactory) {
    super(beanFactory);
  }

  public OBCApplicationContext(Class<?>... annotatedClasses) {
    super(annotatedClasses);
  }

  public OBCApplicationContext(String... basePackages) {
    super(basePackages);
  }

  @Override
  public void destroy() {

    Application appT = ApplicationFactory.create(this);
    appT.shutdownServices();
    appT.shutdown();

    DiscoverServer discoverServer = getBean(DiscoverServer.class);
    discoverServer.close();
    ChannelManager channelManager = getBean(ChannelManager.class);
    channelManager.close();
    NodeManager nodeManager = getBean(NodeManager.class);
    nodeManager.close();

    Manager dbManager = getBean(Manager.class);
    dbManager.stopRePushThread();
    dbManager.stopRePushTriggerThread();
    super.destroy();
  }
}
