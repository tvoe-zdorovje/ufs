package ru.philit.ufs.esb.mock.service;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;

import com.hazelcast.core.Hazelcast;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.dao.support.DataAccessUtils;
import ru.philit.ufs.config.property.HazelcastServerProperties;
import ru.philit.ufs.esb.mock.client.EsbClient;
import ru.philit.ufs.model.cache.MockCache;
import ru.philit.ufs.model.cache.hazelcast.HazelcastMockCacheImpl;
import ru.philit.ufs.model.cache.hazelcast.HazelcastMockServer;
import ru.philit.ufs.model.converter.esb.JaxbConverter;
import ru.philit.ufs.model.entity.esb.asfs.OpStatusType;
import ru.philit.ufs.model.entity.esb.asfs.OperTypeLabel;
import ru.philit.ufs.model.entity.esb.asfs.SrvCommitOperationRs;
import ru.philit.ufs.model.entity.esb.asfs.SrvCommitOperationRs.SrvCommitOperationRsMessage;
import ru.philit.ufs.model.entity.esb.asfs.SrvCreateOperationRs;
import ru.philit.ufs.model.entity.esb.asfs.SrvCreateOperationRs.SrvCreateOperationRsMessage;
import ru.philit.ufs.model.entity.esb.asfs.SrvGetOperationRs;
import ru.philit.ufs.model.entity.esb.asfs.SrvGetOperationRs.SrvGetOperationRsMessage.OperationItem;
import ru.philit.ufs.model.entity.esb.asfs.SrvRollbackOperationRs;
import ru.philit.ufs.model.entity.esb.asfs.SrvRollbackOperationRs.SrvRollbackOperationRsMessage;
import ru.philit.ufs.model.entity.esb.asfs.SrvUpdOperationRs;
import ru.philit.ufs.model.entity.esb.asfs.SrvUpdOperationRs.SrvUpdOperationRsMessage;

public class OperationsCacheServiceTest {

  // service generated IDs
  List<String> cachedOperationIdList = new ArrayList<>();

  @Mock
  private EsbClient esbClient;
  private String sentMessage;

  private AsfsMockService service;

  private final JaxbConverter jaxbConverter =
      new JaxbConverter("ru.philit.ufs.model.entity.esb.asfs");

  /**
   * Set up test data.
   */
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
        sentMessage = (String) invocationOnMock.getArguments()[0];
        return null;
      }
    }).when(esbClient).sendMessage(anyString());

    HazelcastMockServer hazelcastMockServer = new HazelcastMockServer(getServerProperties());
    MockCache mockCache = new HazelcastMockCacheImpl(hazelcastMockServer);
    service = new AsfsMockService(esbClient, mockCache);
    hazelcastMockServer.init();
    service.init();
  }

  private HazelcastServerProperties getServerProperties() {
    HazelcastServerProperties properties = new HazelcastServerProperties();
    properties.getGroup().setName("taskGroup");
    properties.getGroup().setPassword("task");
    properties.getInstance().setHost("localhost");
    return properties;
  }

  @After
  public void tearDown() throws Exception {
    Hazelcast.shutdownAll();
  }

  @Test
  public void test() throws Exception {
    // create three operations
    testCreateOperation();

    // update first and second operations (status = NEW)
    testUpdOperation();

    // commit first operation
    testCommitOperation();

    // rollback second operation
    testRollbackOperation();

    // get and test first; get and minor test all
    testGetOperation();

    // test invalid

    testCreateOperationInvalid();

    testUpdOperationInvalid();

    testCommitOperationInvalid();

    testRollbackOperationInvalid();

    // check consistency
    testGetOperation();
  }

  private void testCreateOperation() throws JAXBException {
    final String reqId = "create";
    final String spName = "asfs";
    final String sysId = "ufs";

    String request1 = "<SrvCreateOperationRq><HeaderInfo><rqUID>" + reqId + "</rqUID>"
        + "<spName>" + spName + "</spName><systemId>" + sysId + "</systemId></HeaderInfo>"
        + "<SrvCreateOperationRqMessage><operationType>FromCardWithdraw</operationType>"
        + "<workPlace_UId>workplace1</workPlace_UId><operatorId>operator1</operatorId>"
        + "</SrvCreateOperationRqMessage></SrvCreateOperationRq>";

    Assert.assertTrue(service.processMessage(request1));
    Assert.assertNotNull(sentMessage);

    Object responseObj = jaxbConverter.getObject(sentMessage);
    Assert.assertEquals(SrvCreateOperationRs.class, responseObj.getClass());
    SrvCreateOperationRs operationRs = (SrvCreateOperationRs) responseObj;
    Assert.assertEquals(reqId, operationRs.getHeaderInfo().getRqUID());
    Assert.assertEquals(spName, operationRs.getHeaderInfo().getSystemId());
    Assert.assertEquals(sysId, operationRs.getHeaderInfo().getSpName());
    SrvCreateOperationRsMessage rsMsg = operationRs.getSrvCreateOperationRsMessage();
    Assert.assertEquals("ok", rsMsg.getResponseCode());
    Assert.assertNotNull(rsMsg.getOperationId());
    Assert.assertNotNull(rsMsg.getCreatedDttm());
    Assert.assertEquals(OpStatusType.ADVANCE_RESERVATION, rsMsg.getOperationStatus());
    cachedOperationIdList.add(rsMsg.getOperationId());

    String request2 = "<SrvCreateOperationRq><HeaderInfo/>"
        + "<SrvCreateOperationRqMessage><operationType>ToAccountDepositRub</operationType>"
        + "<workPlace_UId>workplace2</workPlace_UId><operatorId>operator2</operatorId>"
        + "</SrvCreateOperationRqMessage></SrvCreateOperationRq>";

    Assert.assertTrue(service.processMessage(request2));
    Assert.assertNotNull(sentMessage);

    responseObj = jaxbConverter.getObject(sentMessage);
    Assert.assertEquals(SrvCreateOperationRs.class, responseObj.getClass());
    rsMsg = ((SrvCreateOperationRs) responseObj).getSrvCreateOperationRsMessage();
    Assert.assertEquals("ok", rsMsg.getResponseCode());
    Assert.assertNotNull(rsMsg.getOperationId());
    Assert.assertNotNull(rsMsg.getCreatedDttm());
    Assert.assertEquals(OpStatusType.ADVANCE_RESERVATION, rsMsg.getOperationStatus());
    cachedOperationIdList.add(rsMsg.getOperationId());

    String request3 = "<SrvCreateOperationRq><HeaderInfo/>"
        + "<SrvCreateOperationRqMessage><operationType>ToCardDeposit</operationType>"
        + "<workPlace_UId>workPlaceId2</workPlace_UId><operatorId>operator3</operatorId>"
        + "</SrvCreateOperationRqMessage></SrvCreateOperationRq>";

    Assert.assertTrue(service.processMessage(request3));
    Assert.assertNotNull(sentMessage);

    responseObj = jaxbConverter.getObject(sentMessage);
    Assert.assertEquals(SrvCreateOperationRs.class, responseObj.getClass());
    rsMsg = ((SrvCreateOperationRs) responseObj).getSrvCreateOperationRsMessage();
    Assert.assertEquals("ok", rsMsg.getResponseCode());
    Assert.assertNotNull(rsMsg.getOperationId());
    Assert.assertNotNull(rsMsg.getCreatedDttm());
    Assert.assertEquals(OpStatusType.ADVANCE_RESERVATION, rsMsg.getOperationStatus());
    cachedOperationIdList.add(rsMsg.getOperationId());
  }

  private void testUpdOperation() throws JAXBException {
    final String reqId = "update";
    final String spName = "asfs";
    final String sysId = "ufs";

    String operationId = cachedOperationIdList.get(0);
    String request1 = "<SrvUpdOperationRq>" + "<HeaderInfo><rqUID>" + reqId + "</rqUID>"
        + "<spName>" + spName + "</spName><systemId>" + sysId + "</systemId></HeaderInfo>"
        + "<SrvUpdOperationRqMessage><operationId>" + operationId + "</operationId>"
        + "<cashOrderId>cashOrderId1</cashOrderId><operationNum>operationNum1</operationNum>"
        + "<operationStatus>New</operationStatus><operationType>FromCardWithdraw</operationType>"
        + "<operatorId>operator1</operatorId><repId>repId1</repId>"
        + "<senderAccountTypeId>senderTypeId1</senderAccountTypeId><senderAccountCurrencyType>"
        + "senderCurrencyType1</senderAccountCurrencyType><senderBank>senderBank1</senderBank>"
        + "<senderBankBIC>senderBankBIC1</senderBankBIC><senderAccountId>senderId1"
        + "</senderAccountId><amount>1</amount><recipientAccountTypeId>recipientTypeId1"
        + "</recipientAccountTypeId><recipientAccountCurrencyType>recipientCurrencyType1"
        + "</recipientAccountCurrencyType><recipientBank>recipientBank1</recipientBank>"
        + "<recipientBankBIC>recipientBankBIC1</recipientBankBIC><recipientAccountId>recipientId1"
        + "</recipientAccountId><currencyType>currencyType1</currencyType><workPlace_UId>"
        + "workPlaceId1</workPlace_UId></SrvUpdOperationRqMessage></SrvUpdOperationRq>";

    Assert.assertTrue(service.processMessage(request1));
    Assert.assertNotNull(sentMessage);

    Object responseObj = jaxbConverter.getObject(sentMessage);
    Assert.assertEquals(SrvUpdOperationRs.class, responseObj.getClass());
    SrvUpdOperationRs operationRs = (SrvUpdOperationRs) responseObj;
    Assert.assertEquals(reqId, operationRs.getHeaderInfo().getRqUID());
    Assert.assertEquals(spName, operationRs.getHeaderInfo().getSystemId());
    Assert.assertEquals(sysId, operationRs.getHeaderInfo().getSpName());
    SrvUpdOperationRsMessage rsMsg = operationRs.getSrvUpdOperationRsMessage();
    Assert.assertEquals("ok", rsMsg.getResponseCode());
    Assert.assertEquals(operationId, rsMsg.getOperationId());
    Assert.assertEquals(OpStatusType.NEW, rsMsg.getOperationStatus());

    operationId = cachedOperationIdList.get(1);
    String request2 = "<SrvUpdOperationRq>" + "<HeaderInfo/>"
        + "<SrvUpdOperationRqMessage><operationId>" + operationId + "</operationId>"
        + "<cashOrderId>cashOrderId2</cashOrderId><operationNum>operationNum2</operationNum>"
        + "<operationStatus>New</operationStatus><operationType>FromCardWithdraw</operationType>"
        + "<operatorId>operator2</operatorId><repId>repId2</repId><currencyType>currencyType2"
        + "</currencyType></SrvUpdOperationRqMessage></SrvUpdOperationRq>";

    Assert.assertTrue(service.processMessage(request2));
    Assert.assertNotNull(sentMessage);

    responseObj = jaxbConverter.getObject(sentMessage);
    Assert.assertEquals(SrvUpdOperationRs.class, responseObj.getClass());
    rsMsg = ((SrvUpdOperationRs) responseObj).getSrvUpdOperationRsMessage();
    Assert.assertEquals("ok", rsMsg.getResponseCode());
    Assert.assertEquals(operationId, rsMsg.getOperationId());
    Assert.assertEquals(OpStatusType.NEW, rsMsg.getOperationStatus());
  }

  private void testCommitOperation() throws JAXBException {
    final String reqId = "commit";
    final String spName = "asfs";
    final String sysId = "ufs";

    final String operationId = cachedOperationIdList.get(0);
    String request = "<SrvCommitOperationRq><HeaderInfo><rqUID>" + reqId + "</rqUID>"
        + "<spName>" + spName + "</spName><systemId>" + sysId + "</systemId></HeaderInfo>"
        + "<SrvCommitOperationRqMessage><operationId>" + operationId
        + "</operationId></SrvCommitOperationRqMessage></SrvCommitOperationRq>";

    Assert.assertTrue(service.processMessage(request));
    Assert.assertNotNull(sentMessage);

    Object responseObj = jaxbConverter.getObject(sentMessage);
    Assert.assertEquals(SrvCommitOperationRs.class, responseObj.getClass());
    SrvCommitOperationRs operationRs = (SrvCommitOperationRs) responseObj;
    Assert.assertEquals(reqId, operationRs.getHeaderInfo().getRqUID());
    Assert.assertEquals(spName, operationRs.getHeaderInfo().getSystemId());
    Assert.assertEquals(sysId, operationRs.getHeaderInfo().getSpName());
    SrvCommitOperationRsMessage rsMsg = operationRs.getSrvCommitOperationRsMessage();
    Assert.assertEquals("ok", rsMsg.getResponseCode());
    Assert.assertEquals(operationId, rsMsg.getOperationId());
    Assert.assertEquals(OpStatusType.COMMITTED, rsMsg.getOperationStatus());
    Assert.assertNotNull(rsMsg.getCommittedDttm());
  }

  private void testRollbackOperation() throws JAXBException {
    final String reqId = "rollback";
    final String spName = "asfs";
    final String sysId = "ufs";

    String request = "<SrvRollbackOperationRq>" + "<HeaderInfo><rqUID>" + reqId + "</rqUID>"
        + "<spName>" + spName + "</spName><systemId>" + sysId + "</systemId></HeaderInfo>"
        + "<SrvRollbackOperationRqMessage><operationId>" + cachedOperationIdList.get(1)
        + "</operationId><rollbackReason>rollbackReason</rollbackReason>"
        + "</SrvRollbackOperationRqMessage></SrvRollbackOperationRq>";

    Assert.assertTrue(service.processMessage(request));
    Assert.assertNotNull(sentMessage);

    Object responseObj = jaxbConverter.getObject(sentMessage);
    Assert.assertEquals(SrvRollbackOperationRs.class, responseObj.getClass());
    SrvRollbackOperationRs operationRs = (SrvRollbackOperationRs) responseObj;
    Assert.assertEquals(reqId, operationRs.getHeaderInfo().getRqUID());
    Assert.assertEquals(spName, operationRs.getHeaderInfo().getSystemId());
    Assert.assertEquals(sysId, operationRs.getHeaderInfo().getSpName());
    SrvRollbackOperationRsMessage rsMsg = operationRs.getSrvRollbackOperationRsMessage();
    Assert.assertEquals("ok", rsMsg.getResponseCode());
    Assert.assertNotNull(rsMsg.getOperationId());
    Assert.assertEquals(OpStatusType.CANCELLED, rsMsg.getOperationStatus());
  }

  private void testGetOperation() throws JAXBException {
    final String reqId = "get";
    final String spName = "asfs";
    final String sysId = "ufs";

    // first

    String operationId = cachedOperationIdList.get(0);
    String request1 = "<SrvGetOperationRq><HeaderInfo><rqUID>" + reqId + "</rqUID>"
        + "<spName>" + spName + "</spName><systemId>" + sysId + "</systemId></HeaderInfo>"
        + "<SrvGetOperationRqMessage><operationId>" + operationId + "</operationId>"
        + "</SrvGetOperationRqMessage></SrvGetOperationRq>";

    Assert.assertTrue(service.processMessage(request1));
    Assert.assertNotNull(sentMessage);

    Object responseObj = jaxbConverter.getObject(sentMessage);
    Assert.assertEquals(SrvGetOperationRs.class, responseObj.getClass());
    SrvGetOperationRs operationRs = (SrvGetOperationRs) responseObj;
    Assert.assertEquals(reqId, operationRs.getHeaderInfo().getRqUID());
    Assert.assertEquals(spName, operationRs.getHeaderInfo().getSystemId());
    Assert.assertEquals(sysId, operationRs.getHeaderInfo().getSpName());
    List<OperationItem> operationItemList = operationRs.getSrvGetOperationRsMessage()
        .getOperationItem();

    OperationItem operationItem = DataAccessUtils.singleResult(operationItemList);
    Assert.assertEquals(operationId, operationItem.getOperationId());
    Assert.assertEquals("cashOrderId1", operationItem.getCashOrderId());
    Assert.assertEquals("operationNum1", operationItem.getOperationNum());
    Assert.assertEquals("operator1", operationItem.getOperatorId());
    Assert.assertEquals("workPlaceId1", operationItem.getWorkPlaceUId());
    Assert.assertEquals(OpStatusType.COMMITTED, operationItem.getOperationStatus());
    Assert.assertEquals(OperTypeLabel.FROM_CARD_WITHDRAW, operationItem.getOperationType());
    Assert.assertEquals("repId1", operationItem.getRepId());
    Assert.assertEquals("senderId1", operationItem.getSenderAccountId());
    Assert.assertEquals("senderTypeId1", operationItem.getSenderAccountTypeId());
    Assert.assertEquals("senderCurrencyType1", operationItem.getSenderAccountCurrencyType());
    Assert.assertEquals("senderBank1", operationItem.getSenderBank());
    Assert.assertEquals("senderBankBIC1", operationItem.getSenderBankBIC());
    Assert.assertEquals(BigDecimal.ONE, operationItem.getAmount());
    Assert.assertEquals("recipientId1", operationItem.getRecipientAccountId());
    Assert.assertEquals("recipientTypeId1", operationItem.getRecipientAccountTypeId());
    Assert.assertEquals("recipientCurrencyType1", operationItem.getRecipientAccountCurrencyType());
    Assert.assertEquals("recipientBank1", operationItem.getRecipientBank());
    Assert.assertEquals("recipientBankBIC1", operationItem.getRecipientBankBIC());
    Assert.assertEquals("currencyType1", operationItem.getCurrencyType());
    Assert.assertNull(operationItem.getRollbackReason());
    Assert.assertNotNull(operationItem.getCreatedDttm());
    Assert.assertNotNull(operationItem.getCommittedDttm());

    // second

    operationId = cachedOperationIdList.get(1);
    String request2 = "<SrvGetOperationRq><HeaderInfo><rqUID>" + reqId + "</rqUID>"
        + "<spName>" + spName + "</spName><systemId>" + sysId + "</systemId></HeaderInfo>"
        + "<SrvGetOperationRqMessage><operationId>" + operationId + "</operationId>"
        + "</SrvGetOperationRqMessage></SrvGetOperationRq>";

    Assert.assertTrue(service.processMessage(request2));
    Assert.assertNotNull(sentMessage);

    responseObj = jaxbConverter.getObject(sentMessage);
    Assert.assertEquals(SrvGetOperationRs.class, responseObj.getClass());
    operationItemList = ((SrvGetOperationRs) responseObj).getSrvGetOperationRsMessage()
        .getOperationItem();
    operationItem = DataAccessUtils.singleResult(operationItemList);
    Assert.assertEquals(operationId, operationItem.getOperationId());
    Assert.assertEquals(OpStatusType.CANCELLED, operationItem.getOperationStatus());
    Assert.assertEquals("rollbackReason", operationItem.getRollbackReason());

    // third

    operationId = cachedOperationIdList.get(2);
    String request3 = "<SrvGetOperationRq><HeaderInfo><rqUID>" + reqId + "</rqUID>"
        + "<spName>" + spName + "</spName><systemId>" + sysId + "</systemId></HeaderInfo>"
        + "<SrvGetOperationRqMessage><operationId>" + operationId + "</operationId>"
        + "</SrvGetOperationRqMessage></SrvGetOperationRq>";

    Assert.assertTrue(service.processMessage(request3));
    Assert.assertNotNull(sentMessage);

    responseObj = jaxbConverter.getObject(sentMessage);
    Assert.assertEquals(SrvGetOperationRs.class, responseObj.getClass());
    operationItemList = ((SrvGetOperationRs) responseObj).getSrvGetOperationRsMessage()
        .getOperationItem();
    operationItem = DataAccessUtils.singleResult(operationItemList);
    Assert.assertEquals(operationId, operationItem.getOperationId());
    Assert.assertEquals(OpStatusType.ADVANCE_RESERVATION, operationItem.getOperationStatus());

    // get all

    String request4 = "<SrvGetOperationRq><HeaderInfo/>"
        + "<SrvGetOperationRqMessage></SrvGetOperationRqMessage></SrvGetOperationRq>";

    Assert.assertTrue(service.processMessage(request4));
    Assert.assertNotNull(sentMessage);

    responseObj = jaxbConverter.getObject(sentMessage);
    Assert.assertEquals(SrvGetOperationRs.class, responseObj.getClass());
    operationItemList =
        ((SrvGetOperationRs) responseObj)
            .getSrvGetOperationRsMessage()
            .getOperationItem();
    Assert.assertEquals(cachedOperationIdList.size(), operationItemList.size());
    for (OperationItem item : operationItemList) {
      Assert.assertNotNull(item);
    }
  }

  // === TEST INVALID ===

  private void testCreateOperationInvalid() throws JAXBException {
    String request1 =
        "<SrvCreateOperationRq><HeaderInfo/><SrvCreateOperationRqMessage><operationType/>"
            + "<workPlace_UId>workplace1</workPlace_UId><operatorId>operator1</operatorId>"
            + "</SrvCreateOperationRqMessage></SrvCreateOperationRq>";

    Assert.assertTrue(service.processMessage(request1));
    Assert.assertNotNull(sentMessage);

    Object responseObj = jaxbConverter.getObject(sentMessage);
    Assert.assertEquals(SrvCreateOperationRs.class, responseObj.getClass());
    SrvCreateOperationRsMessage rsMsg = ((SrvCreateOperationRs) responseObj)
        .getSrvCreateOperationRsMessage();
    Assert.assertEquals("err", rsMsg.getResponseCode());
  }

  private void testUpdOperationInvalid() throws JAXBException {
    String operationId = "non-existent";
    String request1 = "<SrvUpdOperationRq>" + "<HeaderInfo/>"
        + "<SrvUpdOperationRqMessage><operationId>" + operationId + "</operationId>"
        + "<cashOrderId>cashOrderId1</cashOrderId><operationNum>operationNum1</operationNum>"
        + "<operationStatus>New</operationStatus></SrvUpdOperationRqMessage></SrvUpdOperationRq>";

    Assert.assertTrue(service.processMessage(request1));
    Assert.assertNotNull(sentMessage);

    Object responseObj = jaxbConverter.getObject(sentMessage);
    Assert.assertEquals(SrvUpdOperationRs.class, responseObj.getClass());
    SrvUpdOperationRsMessage rsMsg = ((SrvUpdOperationRs) responseObj)
        .getSrvUpdOperationRsMessage();
    Assert.assertEquals("err", rsMsg.getResponseCode());

    // update commited

    operationId = cachedOperationIdList.get(0);
    String request2 = "<SrvUpdOperationRq>" + "<HeaderInfo/>"
        + "<SrvUpdOperationRqMessage><operationId>" + operationId + "</operationId>"
        + "<cashOrderId>cashOrderId1</cashOrderId><operationNum>operationNum1</operationNum>"
        + "<operationStatus>New</operationStatus></SrvUpdOperationRqMessage></SrvUpdOperationRq>";

    Assert.assertTrue(service.processMessage(request2));
    Assert.assertNotNull(sentMessage);

    responseObj = jaxbConverter.getObject(sentMessage);
    Assert.assertEquals(SrvUpdOperationRs.class, responseObj.getClass());
    rsMsg = ((SrvUpdOperationRs) responseObj).getSrvUpdOperationRsMessage();
    Assert.assertEquals("err", rsMsg.getResponseCode());

    // update cancelled

    operationId = "non-existent";
    String request3 = "<SrvUpdOperationRq>" + "<HeaderInfo/>"
        + "<SrvUpdOperationRqMessage><operationId>" + operationId + "</operationId>"
        + "<cashOrderId>cashOrderId1</cashOrderId><operationNum>operationNum1</operationNum>"
        + "<operationStatus>New</operationStatus></SrvUpdOperationRqMessage></SrvUpdOperationRq>";

    Assert.assertTrue(service.processMessage(request3));
    Assert.assertNotNull(sentMessage);

    responseObj = jaxbConverter.getObject(sentMessage);
    Assert.assertEquals(SrvUpdOperationRs.class, responseObj.getClass());
    rsMsg = ((SrvUpdOperationRs) responseObj).getSrvUpdOperationRsMessage();
    Assert.assertEquals("err", rsMsg.getResponseCode());
  }

  private void testCommitOperationInvalid() throws JAXBException {
    String operationId = "non-existent";
    String request1 = "<SrvCommitOperationRq><HeaderInfo/>"
        + "<SrvCommitOperationRqMessage><operationId>" + operationId
        + "</operationId></SrvCommitOperationRqMessage></SrvCommitOperationRq>";

    Assert.assertTrue(service.processMessage(request1));
    Assert.assertNotNull(sentMessage);

    Object responseObj = jaxbConverter.getObject(sentMessage);
    Assert.assertEquals(SrvCommitOperationRs.class, responseObj.getClass());
    SrvCommitOperationRsMessage rsMsg = ((SrvCommitOperationRs) responseObj)
        .getSrvCommitOperationRsMessage();
    Assert.assertEquals("err", rsMsg.getResponseCode());

    // commit cancelled
    operationId = cachedOperationIdList.get(1);
    String request2 = "<SrvCommitOperationRq><HeaderInfo/>"
        + "<SrvCommitOperationRqMessage><operationId>" + operationId
        + "</operationId></SrvCommitOperationRqMessage></SrvCommitOperationRq>";

    Assert.assertTrue(service.processMessage(request2));
    Assert.assertNotNull(sentMessage);

    responseObj = jaxbConverter.getObject(sentMessage);
    Assert.assertEquals(SrvCommitOperationRs.class, responseObj.getClass());
    rsMsg = ((SrvCommitOperationRs) responseObj).getSrvCommitOperationRsMessage();
    Assert.assertEquals("err", rsMsg.getResponseCode());
    Assert.assertEquals(operationId, rsMsg.getOperationId());
    Assert.assertEquals(OpStatusType.CANCELLED, rsMsg.getOperationStatus());

    // commit advance reserved
    operationId = cachedOperationIdList.get(2);
    String request3 = "<SrvCommitOperationRq><HeaderInfo/>"
        + "<SrvCommitOperationRqMessage><operationId>" + operationId
        + "</operationId></SrvCommitOperationRqMessage></SrvCommitOperationRq>";

    Assert.assertTrue(service.processMessage(request3));
    Assert.assertNotNull(sentMessage);

    responseObj = jaxbConverter.getObject(sentMessage);
    Assert.assertEquals(SrvCommitOperationRs.class, responseObj.getClass());
    rsMsg = ((SrvCommitOperationRs) responseObj).getSrvCommitOperationRsMessage();
    Assert.assertEquals("err", rsMsg.getResponseCode());
    Assert.assertEquals(operationId, rsMsg.getOperationId());
    Assert.assertEquals(OpStatusType.ADVANCE_RESERVATION, rsMsg.getOperationStatus());
  }

  private void testRollbackOperationInvalid() throws JAXBException {
    String operationId = "non-exitstent";
    String request1 = "<SrvRollbackOperationRq>" + "<HeaderInfo/>"
        + "<SrvRollbackOperationRqMessage><operationId>" + operationId
        + "</operationId><rollbackReason>rollbackReason</rollbackReason>"
        + "</SrvRollbackOperationRqMessage></SrvRollbackOperationRq>";

    Assert.assertTrue(service.processMessage(request1));
    Assert.assertNotNull(sentMessage);

    Object responseObj = jaxbConverter.getObject(sentMessage);
    Assert.assertEquals(SrvRollbackOperationRs.class, responseObj.getClass());
    SrvRollbackOperationRsMessage rsMsg = ((SrvRollbackOperationRs) responseObj)
        .getSrvRollbackOperationRsMessage();
    Assert.assertEquals("err", rsMsg.getResponseCode());

    // rollback commited

    operationId = cachedOperationIdList.get(0);
    String request2 = "<SrvRollbackOperationRq>" + "<HeaderInfo/>"
        + "<SrvRollbackOperationRqMessage><operationId>" + operationId
        + "</operationId><rollbackReason>rollbackReason</rollbackReason>"
        + "</SrvRollbackOperationRqMessage></SrvRollbackOperationRq>";

    Assert.assertTrue(service.processMessage(request2));
    Assert.assertNotNull(sentMessage);

    responseObj = jaxbConverter.getObject(sentMessage);
    Assert.assertEquals(SrvRollbackOperationRs.class, responseObj.getClass());
    rsMsg = ((SrvRollbackOperationRs) responseObj).getSrvRollbackOperationRsMessage();
    Assert.assertEquals("err", rsMsg.getResponseCode());
    Assert.assertEquals(operationId, rsMsg.getOperationId());
    Assert.assertEquals(OpStatusType.COMMITTED, rsMsg.getOperationStatus());
  }
}
