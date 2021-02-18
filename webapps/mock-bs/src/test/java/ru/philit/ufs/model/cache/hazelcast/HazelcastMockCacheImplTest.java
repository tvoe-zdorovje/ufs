package ru.philit.ufs.model.cache.hazelcast;

import static org.mockito.Mockito.when;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.IMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.philit.ufs.model.entity.esb.asfs.OpStatusType;
import ru.philit.ufs.model.entity.esb.asfs.OperTypeLabel;
import ru.philit.ufs.model.entity.esb.asfs.SrvGetOperationRs.SrvGetOperationRsMessage.OperationItem;
import ru.philit.ufs.model.entity.esb.eks.PkgTaskStatusType;
import ru.philit.ufs.model.entity.esb.eks.SrvGetTaskClOperPkgRs.SrvGetTaskClOperPkgRsMessage;
import ru.philit.ufs.model.entity.oper.OperationPackageInfo;

public class HazelcastMockCacheImplTest {

  private static final String CTRL_TASK_ELEMENT = "\"pkgTaskId\":10";
  private static final String INN = "77700044422232";
  private static final List<OperationItem> OPERATION_ITEM_LIST;
  private static final ObjectMapper jsonMapper;

  static {
    OPERATION_ITEM_LIST = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      OperationItem operationItem = new OperationItem();
      operationItem.setOperationId("id" + i);
      operationItem.setOperationType(OperTypeLabel.FROM_CARD_WITHDRAW);
      operationItem.setOperationStatus(OpStatusType.ADVANCE_RESERVATION);
      operationItem.setOperatorId("operator" + i);
      operationItem.setWorkPlaceUId("workplace" + i);
      operationItem.setCreatedDttm(xmlCalendar(2021, 2, 2, 11, i));
      OPERATION_ITEM_LIST.add(operationItem);
    }
  }

  static {
    jsonMapper = new ObjectMapper();
    jsonMapper.setSerializationInclusion(Include.NON_NULL);
    jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  static class TestTaskBody {

    public Long pkgTaskId;

    public TestTaskBody(Long pkgTaskId) {
      this.pkgTaskId = pkgTaskId;
    }
  }

  @Mock
  private HazelcastMockServer hazelcastMockServer;

  private HazelcastMockCacheImpl mockCache;

  private IMap<Long, Map<Long, String>> tasksCardDepositByPackageId = new MockIMap<>();
  private IMap<Long, Map<Long, String>> tasksCardWithdrawByPackageId = new MockIMap<>();
  private IMap<Long, Map<Long, String>> tasksAccountDepositByPackageId = new MockIMap<>();
  private IMap<Long, Map<Long, String>> tasksAccountWithdrawByPackageId = new MockIMap<>();
  private IMap<Long, Map<Long, String>> tasksCheckbookIssuingByPackageId = new MockIMap<>();
  private IMap<Long, PkgTaskStatusType> taskStatuses = new MockIMap<>();
  private IMap<Long, OperationPackageInfo> packageById = new MockIMap<>();
  private IMap<String, Long> packageIdByInn = new MockIMap<>();
  private IMap<String, String> operationsById = new MockIMap<>();

  /**
   * Set up test data.
   */
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    mockCache = new HazelcastMockCacheImpl(hazelcastMockServer);
    when(hazelcastMockServer.getTasksCardDepositByPackageId())
        .thenReturn(tasksCardDepositByPackageId);
    when(hazelcastMockServer.getTasksCardWithdrawByPackageId())
        .thenReturn(tasksCardWithdrawByPackageId);
    when(hazelcastMockServer.getTasksAccountDepositByPackageId())
        .thenReturn(tasksAccountDepositByPackageId);
    when(hazelcastMockServer.getTasksAccountWithdrawByPackageId())
        .thenReturn(tasksAccountWithdrawByPackageId);
    when(hazelcastMockServer.getTasksCheckbookIssuingByPackageId())
        .thenReturn(tasksCheckbookIssuingByPackageId);
    when(hazelcastMockServer.getTaskStatuses()).thenReturn(taskStatuses);
    when(hazelcastMockServer.getPackageById()).thenReturn(packageById);
    when(hazelcastMockServer.getPackageIdByInn()).thenReturn(packageIdByInn);
    when(hazelcastMockServer.getOperationsById()).thenReturn(operationsById);
  }

  @Test
  public void testSaveTask() {
    // given
    TestTaskBody taskBody = new TestTaskBody(10L);

    // when
    mockCache.saveTaskCardDeposit(1L, 10L, taskBody);
    // then
    Assert.assertTrue(tasksCardDepositByPackageId.containsKey(1L));
    Assert.assertTrue(tasksCardDepositByPackageId.get(1L).containsKey(10L));
    Assert.assertTrue(tasksCardDepositByPackageId.get(1L).get(10L).contains(CTRL_TASK_ELEMENT));

    // when
    mockCache.saveTaskCardWithdraw(1L, 10L, taskBody);
    // then
    Assert.assertTrue(tasksCardWithdrawByPackageId.containsKey(1L));
    Assert.assertTrue(tasksCardWithdrawByPackageId.get(1L).containsKey(10L));
    Assert.assertTrue(tasksCardDepositByPackageId.get(1L).get(10L).contains(CTRL_TASK_ELEMENT));

    // when
    mockCache.saveTaskAccountDeposit(1L, 10L, taskBody);
    // then
    Assert.assertTrue(tasksAccountDepositByPackageId.containsKey(1L));
    Assert.assertTrue(tasksAccountDepositByPackageId.get(1L).containsKey(10L));
    Assert.assertTrue(tasksCardDepositByPackageId.get(1L).get(10L).contains(CTRL_TASK_ELEMENT));

    // when
    mockCache.saveTaskAccountWithdraw(1L, 10L, taskBody);
    // then
    Assert.assertTrue(tasksAccountWithdrawByPackageId.containsKey(1L));
    Assert.assertTrue(tasksAccountWithdrawByPackageId.get(1L).containsKey(10L));
    Assert.assertTrue(tasksCardDepositByPackageId.get(1L).get(10L).contains(CTRL_TASK_ELEMENT));

    // when
    mockCache.saveTaskCheckbookIssuing(1L, 10L, taskBody);
    // then
    Assert.assertTrue(tasksCheckbookIssuingByPackageId.containsKey(1L));
    Assert.assertTrue(tasksCheckbookIssuingByPackageId.get(1L).containsKey(10L));
    Assert.assertTrue(tasksCardDepositByPackageId.get(1L).get(10L).contains(CTRL_TASK_ELEMENT));
  }

  @Test
  public void testSaveTaskStatus() {
    // when
    mockCache.saveTaskStatus(1L, PkgTaskStatusType.ACTIVE);
    //then
    Assert.assertTrue(taskStatuses.containsKey(1L));
    Assert.assertEquals(taskStatuses.get(1L), PkgTaskStatusType.ACTIVE);
  }

  @Test
  public void testCreatePackage() {
    // when
    Long packageId = mockCache.checkPackage(INN);
    // then
    Assert.assertNull(packageId);

    // when
    OperationPackageInfo packageInfo = mockCache.createPackage(INN, "12345", "Sidorov_SS");
    // then
    Assert.assertNotNull(packageInfo.getId());

    // when
    Long packageId2 = mockCache.checkPackage(INN);
    // then
    Assert.assertEquals(packageInfo.getId(), packageId2);

    // when
    OperationPackageInfo packageInfo2 = mockCache.createPackage(INN, "12345", "Sidorov_SS");
    // then
    Assert.assertNotEquals(packageInfo.getId(), packageInfo2.getId());
  }

  @Test
  public void testSearchTaskCardDeposit() {
    // when
    Map<Long, List<SrvGetTaskClOperPkgRsMessage.PkgItem.ToCardDeposit.TaskItem>> resultMap =
        mockCache.searchTasksCardDeposit(null, null, null, null, null);
    // then
    Assert.assertNotNull(resultMap);
    Assert.assertTrue(resultMap.isEmpty());
  }

  @Test
  public void testSaveOperation() {
    // given
    final OperationItem operationItem = OPERATION_ITEM_LIST.get(0);

    // when
    mockCache.saveOperation(operationItem);
    // then
    Assert.assertTrue(operationsById.containsKey(operationItem.getOperationId()));
    Assert.assertNotNull(operationsById.get(operationItem.getOperationId()));
    Assert.assertNotEquals("", operationsById.get(operationItem.getOperationId()));
  }

  @Test
  public void testGetOperation() throws Exception {
    // given
    final OperationItem operationItem = OPERATION_ITEM_LIST.get(0);

    // when
    final OperationItem nullResult = mockCache.getOperation(operationItem.getOperationId());
    // then
    Assert.assertNull(nullResult);

    // given
    operationsById
        .put(operationItem.getOperationId(), jsonMapper.writeValueAsString(operationItem));

    // when
    final OperationItem result = mockCache.getOperation(operationItem.getOperationId());
    // then
    Assert.assertNotNull(result);
    Assert.assertEquals(operationItem.getOperationId(), result.getOperationId());
    Assert.assertEquals(operationItem.getOperatorId(), result.getOperatorId());
    Assert.assertEquals(operationItem.getWorkPlaceUId(), result.getWorkPlaceUId());
    Assert.assertEquals(operationItem.getOperationStatus(), result.getOperationStatus());
    Assert.assertEquals(operationItem.getOperationType(), result.getOperationType());
    Assert.assertEquals(DatatypeConstants.EQUAL,
        operationItem.getCreatedDttm().compare(operationItem.getCreatedDttm()));
  }

  @Test
  public void testGetOperations() throws Exception {
    // given
    XMLGregorianCalendar createdFrom = null;
    XMLGregorianCalendar createdTo = null;

    // when
    Collection<OperationItem> resultList = mockCache.getOperations(createdFrom, createdTo);
    // then
    Assert.assertNotNull(resultList);
    Assert.assertTrue(resultList.isEmpty());

    // given
    for (OperationItem operationItem : OPERATION_ITEM_LIST) {
      operationsById
          .put(operationItem.getOperationId(), jsonMapper.writeValueAsString(operationItem));
    }

    // when
    resultList = mockCache.getOperations(createdFrom, createdTo);
    // then
    Assert.assertNotNull(resultList);
    Assert.assertFalse(resultList.isEmpty());
    Assert.assertEquals(OPERATION_ITEM_LIST.size(), resultList.size());

    // given
    int itemFromIdx = 1;
    int itemToIdx = 3;
    createdFrom = OPERATION_ITEM_LIST.get(itemFromIdx).getCreatedDttm();
    createdTo = OPERATION_ITEM_LIST.get(itemToIdx).getCreatedDttm();

    // when
    resultList = mockCache.getOperations(createdFrom, createdTo);
    // then
    Assert.assertNotNull(resultList);
    Assert.assertFalse(resultList.isEmpty());
    Assert.assertEquals(itemToIdx - itemFromIdx + 1, resultList.size());
  }

  private static XMLGregorianCalendar xmlCalendar(int year, int month, int day, int hour,
      int minute) {
    try {
      GregorianCalendar calendar = new GregorianCalendar(year, month - 1, day, hour, minute);
      calendar.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
    } catch (DatatypeConfigurationException e) {
      e.printStackTrace();
      return null;
    }
  }
}
