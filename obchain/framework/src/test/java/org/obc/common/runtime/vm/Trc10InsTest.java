package org.obc.common.runtime.vm;

import com.google.protobuf.ByteString;

import static org.obc.core.config.Parameter.ChainConstant.obc_PRECISION;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obc.common.obcApplicationContext;
import org.obc.common.runtime.InternalTransaction;
import org.obc.common.runtime.TvmTestUtils;
import org.obc.common.runtime.vm.DataWord;
import org.obc.common.utils.ByteArray;
import org.obc.common.utils.FileUtil;
import org.obc.core.capsule.AccountCapsule;
import org.obc.core.capsule.AssetIssueCapsule;
import org.obc.core.capsule.BlockCapsule;
import org.obc.core.config.DefaultConfig;
import org.obc.core.config.args.Args;
import org.obc.core.db.TransactionTrace;
import org.obc.core.exception.ContractValidateException;
import org.obc.core.store.StoreFactory;
import org.obc.core.vm.program.Program;
import org.obc.core.vm.program.invoke.ProgramInvoke;
import org.obc.core.vm.program.invoke.ProgramInvokeFactory;
import org.obc.core.vm.repository.Repository;
import org.obc.core.vm.repository.RepositoryImpl;
import org.spongycastle.util.encoders.Hex;
import org.obc.protos.Protocol;

@Slf4j
public class Trc10InsTest {

  private String dbPath;
  private obcApplicationContext context;

  @Before
  public void init() {
    dbPath = "output_" + this.getClass().getName();
    FileUtil.deleteDir(new File(dbPath));
    Args.setParam(new String[]{"--output-directory", dbPath, "--debug"}, "config-localtest.conf");
    context = new obcApplicationContext(DefaultConfig.class);
  }

  // TODO: 2020/11/26
  //  1. convert string to DataWord, leading or ending
  //  2. why asset id in account is not same as in asset issue, bytes vs string
  //  3. for test, covert is hard to use
  @Test
  public void testTokenIssueAndUpdateAsset() throws ContractValidateException {
    // construct ProgramInvoke instance
    Repository deposit = RepositoryImpl.createRoot(StoreFactory.getInstance());
    byte[] ownerAddr = TransactionTrace.convertToobcAddress(
        Hex.decode("abd4b9367799eaa3197fecb144eb71de1e049abc"));
    byte[] contractAddr = TransactionTrace.convertToobcAddress(
        Hex.decode("471fd3ad3e9eeadeec4608b92d16ce6b500704cc"));
    Protocol.Transaction obc = TvmTestUtils.generateTriggerSmartContractAndGetTransaction(
        ownerAddr, contractAddr, new byte[0], 0, 0);
    ProgramInvoke invoke;
    invoke = context.getBean(ProgramInvokeFactory.class).createProgramInvoke(
        InternalTransaction.obcType.obc_CONTRACT_CALL_TYPE,
        InternalTransaction.ExecutorType.ET_NORMAL_TYPE,
        obc,
        0,
        0,
        new BlockCapsule(Protocol.Block.newBuilder().build()).getInstance(),
        deposit,
        System.currentTimeMillis(),
        System.currentTimeMillis() + 50000,
        3_000_000L);

    // add contract account
    deposit.createAccount(contractAddr, Protocol.AccountType.Contract);
    deposit.commit();

    // 1. test token issue
    // confirm contract exist and give it 1024 obcs to issue asset
    Assert.assertNotNull(deposit.getAccount(contractAddr));
    Assert.assertEquals(deposit.getBalance(contractAddr), 0);

    long balanceToAdd = deposit.getDynamicPropertiesStore().getAssetIssueFee();
    deposit.addBalance(contractAddr, balanceToAdd);
    deposit.commit();

    Assert.assertEquals(deposit.getBalance(contractAddr), balanceToAdd);

    long initialTokenId = deposit.getTokenIdNum();
    long initialBalanceOfBlackHole = deposit.getBalance(deposit.getBlackHoleAddress());

    // construct Program instance
    InternalTransaction interobc = new InternalTransaction(
        Protocol.Transaction.getDefaultInstance(),
        InternalTransaction.obcType.obc_UNKNOWN_TYPE);
    Program program = new Program(new byte[0], invoke, interobc);

    // call tokenIssue by Program instance and assert stack top is not zero if call successful
    program.tokenIssue(new DataWord(covertTo32BytesByEndingZero(ByteArray.fromString("Yang"))),
        new DataWord(covertTo32BytesByEndingZero(ByteArray.fromString("YNX"))),
        new DataWord(1000_000L * obc_PRECISION),
        new DataWord(5));
    Assert.assertNotEquals(0, program.stackPop().intValue());

    // check global token id increased
    Assert.assertEquals(initialTokenId + 1, deposit.getTokenIdNum());

    // check asset issue inserted into repository
    final String createdAssetId = String.valueOf(initialTokenId + 1);
    byte[] createdAssetIdData = ByteArray.fromString(createdAssetId);
    AssetIssueCapsule assetIssueCap = deposit.getAssetIssue(createdAssetIdData);
    Assert.assertNotNull(assetIssueCap);

    // check contract account updated
    AccountCapsule ownerAccountCap = deposit.getAccount(contractAddr);
    Assert.assertEquals(ByteString.copyFrom(
        Objects.requireNonNull(ByteArray.fromString(assetIssueCap.getId()))),
        ownerAccountCap.getAssetIssuedID());
    Assert.assertEquals(assetIssueCap.getName(), ownerAccountCap.getAssetIssuedName());
    Assert.assertTrue(ownerAccountCap.getAssetMapV2().entrySet().stream().anyMatch(
        e -> e.getKey().equals(createdAssetId)));

    // check balance of contract account and black hole account
    Assert.assertEquals(initialBalanceOfBlackHole + balanceToAdd,
        deposit.getBalance(deposit.getBlackHoleAddress()));
    Assert.assertEquals(0, ownerAccountCap.getBalance());

    // 2. test update asset
    // save data of url and description to program memory
    String url = "http://test.com";
    String desc = "This is a simple description.";
    byte[] urlData = ByteArray.fromString(url);
    byte[] descData = ByteArray.fromString(desc);
    program.memorySave(new DataWord(0), new DataWord(Objects.requireNonNull(urlData).length));
    program.memorySave(DataWord.WORD_SIZE, urlData);
    program.memorySave(new DataWord(2 * DataWord.WORD_SIZE),
        new DataWord(Objects.requireNonNull(descData).length));
    program.memorySave(3 * DataWord.WORD_SIZE, descData);

    // call updateAsset by Program instance and assert stack top is not zero if call successful
    program.updateAsset(new DataWord(0), new DataWord(2 * DataWord.WORD_SIZE));
    Assert.assertNotEquals(0, program.stackPop().intValue());

    // check asset issue updated
    assetIssueCap = deposit.getAssetIssue(createdAssetIdData);
    Assert.assertEquals(url, assetIssueCap.getUrl().toStringUtf8());
    Assert.assertEquals(desc, assetIssueCap.getDesc().toStringUtf8());
  }

  @After
  public void destroy() {
    Args.clearParam();
    context.destroy();
    if (FileUtil.deleteDir(new File(dbPath))) {
      logger.info("Release resources successful.");
    } else {
      logger.error("Release resources failure.");
    }
  }

  private byte[] covertTo32BytesByEndingZero(byte[] data) {
    if (data == null || data.length > 32) {
      throw new IllegalArgumentException("bytes array length should not bigger than 32");
    }
    if (data.length == 32) {
      return data.clone();
    }
    byte[] newData = new byte[32];
    Arrays.fill(newData, (byte) 0);
    System.arraycopy(data, 0, newData, 0, data.length);
    return newData;
  }
}
