package org.obc.core.services.filter;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.io.File;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.obc.common.Application;
import org.obc.common.ApplicationFactory;
import org.obc.common.obcApplicationContext;
import org.obc.common.utils.FileUtil;
import org.obc.core.Constant;
import org.obc.core.config.DefaultConfig;
import org.obc.core.config.args.Args;
import org.obc.core.services.RpcApiService;
import org.obc.core.services.interfaceOnPBFT.RpcApiServiceOnPBFT;
import org.obc.core.services.interfaceOnSolidity.RpcApiServiceOnSolidity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.obc.api.DatabaseGrpc;
import org.obc.api.GrpcAPI;
import org.obc.api.WalletGrpc;
import org.obc.api.WalletSolidityGrpc;

public class LiteFnQueryGrpcInterceptorTest {

  private static final Logger logger = LoggerFactory.getLogger("Test");

  private obcApplicationContext context;
  private ManagedChannel channelFull = null;
  private ManagedChannel channelpBFT = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity = null;
  private WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubpBFT = null;
  private DatabaseGrpc.DatabaseBlockingStub databaseBlockingStub = null;
  private RpcApiService rpcApiService;
  private RpcApiServiceOnSolidity rpcApiServiceOnSolidity;
  private RpcApiServiceOnPBFT rpcApiServiceOnPBFT;
  private Application appTest;

  private String dbPath = "output_grpc_filter_test";

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  /**
   * init logic.
   */
  @Before
  public void init() {
    Args.setParam(new String[]{"-d", dbPath}, Constant.TEST_CONF);
    String fullnode = String.format("%s:%d", Args.getInstance().getNodeDiscoveryBindIp(),
            Args.getInstance().getRpcPort());
    String pBFTNode = String.format("%s:%d", Args.getInstance().getNodeDiscoveryBindIp(),
            Args.getInstance().getRpcOnPBFTPort());
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
            .usePlaintext(true)
            .build();
    channelpBFT = ManagedChannelBuilder.forTarget(pBFTNode)
            .usePlaintext(true)
            .build();
    context = new obcApplicationContext(DefaultConfig.class);
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    blockingStubSolidity = WalletSolidityGrpc.newBlockingStub(channelFull);
    blockingStubpBFT = WalletSolidityGrpc.newBlockingStub(channelpBFT);
    blockingStubSolidity = WalletSolidityGrpc.newBlockingStub(channelFull);
    databaseBlockingStub = DatabaseGrpc.newBlockingStub(channelFull);
    rpcApiService = context.getBean(RpcApiService.class);
    rpcApiServiceOnSolidity = context.getBean(RpcApiServiceOnSolidity.class);
    rpcApiServiceOnPBFT = context.getBean(RpcApiServiceOnPBFT.class);
    appTest = ApplicationFactory.create(context);
    appTest.addService(rpcApiService);
    appTest.addService(rpcApiServiceOnSolidity);
    appTest.addService(rpcApiServiceOnPBFT);
    appTest.initServices(Args.getInstance());
    appTest.startServices();
    appTest.startup();
  }

  /**
   * destroy the context.
   */
  @After
  public void destroy() {
    Args.clearParam();
    appTest.shutdownServices();
    appTest.shutdown();
    context.destroy();
    if (FileUtil.deleteDir(new File(dbPath))) {
      logger.info("Release resources successful.");
    } else {
      logger.info("Release resources failure.");
    }
  }

  @Test
  public void testGrpcApiThrowStatusRuntimeException() {
    final GrpcAPI.NumberMessage message = GrpcAPI.NumberMessage.newBuilder().setNum(0).build();
    Args.getInstance().setLiteFullNode(true);
    thrown.expect(StatusRuntimeException.class);
    thrown.expectMessage("UNAVAILABLE: this API is closed because this node is a lite fullnode");
    blockingStubFull.getBlockByNum(message);
  }

  @Test
  public void testpBFTGrpcApiThrowStatusRuntimeException() {
    final GrpcAPI.NumberMessage message = GrpcAPI.NumberMessage.newBuilder().setNum(0).build();
    Args.getInstance().setLiteFullNode(true);
    thrown.expect(StatusRuntimeException.class);
    thrown.expectMessage("UNAVAILABLE: this API is closed because this node is a lite fullnode");
    blockingStubpBFT.getBlockByNum(message);
  }

  @Test
  public void testGrpcInterceptor() {
    GrpcAPI.NumberMessage message = GrpcAPI.NumberMessage.newBuilder().setNum(0).build();
    Args.getInstance().setLiteFullNode(false);
    Assert.assertNotNull(blockingStubFull.getBlockByNum(message));
  }
}
