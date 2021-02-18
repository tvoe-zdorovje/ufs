package ru.philit.ufs.esb.mock.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import ru.philit.ufs.esb.mock.client.EsbClient;
import ru.philit.ufs.model.cache.MockCache;
import ru.philit.ufs.model.entity.esb.asfs.OpStatusType;
import ru.philit.ufs.model.entity.esb.asfs.SrvGetOperationRs.SrvGetOperationRsMessage.OperationItem;

public class AsfsMockServiceTest {

  @Mock
  private EsbClient esbClient;
  @Mock
  private MockCache mockCache;

  private AsfsMockService service;

  /**
   * Set up test data.
   */
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
        OperationItem operationItem = new OperationItem();
        operationItem.setOperationId((String) invocationOnMock.getArguments()[0]);
        operationItem.setOperationStatus(OpStatusType.NEW);
        return operationItem;
      }
    }).when(mockCache).getOperation(anyString());

    service = new AsfsMockService(esbClient, mockCache);
  }

  @Test
  public void testInit() {
    service.init();
  }

  @Test
  public void processMessage_SrvCommitOperationRq() {
    String requestMessage = "<SrvCommitOperationRq><HeaderInfo/><SrvCommitOperationRqMessage/>"
        + "</SrvCommitOperationRq>";
    assertTrue(service.processMessage(requestMessage));
  }

  @Test
  public void processMessage_SrvCreateOperationRq() {
    String requestMessage = "<SrvCreateOperationRq><HeaderInfo/><SrvCreateOperationRqMessage/>"
        + "</SrvCreateOperationRq>";
    assertTrue(service.processMessage(requestMessage));
  }

  @Test
  public void processMessage_SrvRollbackOperationRq() {
    String requestMessage = "<SrvRollbackOperationRq><HeaderInfo/><SrvRollbackOperationRqMessage/>"
        + "</SrvRollbackOperationRq>";
    assertTrue(service.processMessage(requestMessage));
  }

  @Test
  public void processMessage_SrvUpdOperationRq() {
    String requestMessage = "<SrvUpdOperationRq><HeaderInfo/><SrvUpdOperationRqMessage>"
        + "<operationId>opId</operationId><operationStatus>New</operationStatus>"
        + "<operationType>FromCardWithdraw</operationType><currencyType>USD</currencyType>"
        + "</SrvUpdOperationRqMessage></SrvUpdOperationRq>";
    assertTrue(service.processMessage(requestMessage));
  }

  @Test
  public void processMessage_SrvGetOperationRq() {
    String requestMessage = "<SrvGetOperationRq><HeaderInfo/><SrvGetOperationRqMessage/>"
        + "</SrvGetOperationRq>";
    assertTrue(service.processMessage(requestMessage));
  }

  @Test
  public void testProcessMessage_Other() throws Exception {
    String requestMessage = "Not valid xml";
    assertFalse(service.processMessage(requestMessage));
  }
}