package ru.philit.ufs.web.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import ru.philit.ufs.model.entity.account.Representative;
import ru.philit.ufs.model.entity.common.OperationTypeCode;
import ru.philit.ufs.model.entity.oper.Operation;
import ru.philit.ufs.model.entity.oper.OperationPackage;
import ru.philit.ufs.model.entity.oper.OperationTaskCardDeposit;
import ru.philit.ufs.model.entity.oper.OperationTaskStatus;
import ru.philit.ufs.model.entity.user.ClientInfo;
import ru.philit.ufs.model.entity.user.Operator;
import ru.philit.ufs.model.entity.user.Subbranch;
import ru.philit.ufs.model.entity.user.User;
import ru.philit.ufs.model.entity.user.Workplace;
import ru.philit.ufs.web.dto.OperationDto;
import ru.philit.ufs.web.dto.OperationJournalDto;
import ru.philit.ufs.web.mapping.OperationJournalMapper;
import ru.philit.ufs.web.mapping.impl.OperationJournalMapperImpl;
import ru.philit.ufs.web.provider.ReportProvider;
import ru.philit.ufs.web.provider.RepresentativeProvider;
import ru.philit.ufs.web.view.GetOperationJournalReq;
import ru.philit.ufs.web.view.GetOperationJournalResp;

public class ReportControllerTest extends RestControllerTest {

  @Mock
  private ReportProvider reportProvider;
  @Mock
  private RepresentativeProvider representativeProvider;
  @Spy
  private OperationJournalMapper operationJournalMapper = new OperationJournalMapperImpl();

  /**
   * Set up test controller.
   */
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    standaloneSetup(new ReportController(reportProvider, representativeProvider,
        operationJournalMapper));
  }

  @Test
  public void testGetOperationJournal() throws Exception {
    GetOperationJournalReq request = new GetOperationJournalReq();
    request.setFromDate("30.05.2017");
    request.setToDate("31.05.2017");

    OperationPackage operationPackage = new OperationPackage(2121);
    operationPackage.setUserLogin("Sidorov_SS");
    OperationTaskCardDeposit task = new OperationTaskCardDeposit();
    task.setId(555555L);
    task.setStatus(OperationTaskStatus.COMPLETED);
    task.setStatusChangedDate(new Date());
    task.setInn("777000555222111");
    task.setLegalEntityShortName("short name");
    task.setAmount(BigDecimal.valueOf(1400));
    operationPackage.getToCardDeposits().add(task);
    Operation operation = new Operation();
    operation.setAmount(BigDecimal.valueOf(1400));
    operation.setTypeCode(OperationTypeCode.TO_CARD_DEPOSIT);
    when(reportProvider.getOperations(any(Date.class), any(Date.class), any(ClientInfo.class)))
        .thenReturn(Collections.singletonList(operation));
    User sidorovUser = new User("Sidorov_SS");
    sidorovUser.setPosition("оператор");
    when(reportProvider.getUser(anyString()))
        .thenReturn(sidorovUser);
    Subbranch sidorovSubbranch = new Subbranch("1234", "13", "8593", null, "0102", "1385930102", 1L,
        "0278000222", "044525225", "ПАО \"Сбербанк\"", "30165465190064106565",
        "location1", "locationType1");
    Operator sidorovOperator = new Operator("111", "Сидоров С.С.", sidorovSubbranch, "Сидоров",
        "Сидор", "Сидорович", "email@noname.no", "+79001001010", "+79002002020");
    when(reportProvider.getOperator(anyString(), any(ClientInfo.class)))
        .thenReturn(sidorovOperator);
    when(reportProvider.getCommission(anyString(), any(BigDecimal.class),
        any(Operation.class), any(ClientInfo.class))).thenReturn(BigDecimal.valueOf(12.5));

    Representative representative = new Representative();
    representative.setLastName("Петров");
    representative.setFirstName("Петр");
    representative.setPatronymic("Петрович");
    when(representativeProvider.getRepresentativeById(anyString(), any(ClientInfo.class)))
        .thenReturn(representative);

    String responseJson = performAndGetContent(post("/report/operationJournal")
        .content(toRequest(request)));
    GetOperationJournalResp response = toResponse(responseJson, GetOperationJournalResp.class);

    assertNotNull(response.getData());
    assertTrue(response.getData() instanceof List);
    assertEquals(((List) response.getData()).size(), 1);
    assertEquals(((List) response.getData()).get(0).getClass(), OperationJournalDto.class);
    OperationJournalDto controlDto = (OperationJournalDto) ((List) response.getData()).get(0);
    assertNotNull(controlDto.getOperation());
    assertEquals(controlDto.getOperation().getAmount(), "1400");
    assertNotNull(controlDto.getRepresentative());
    assertEquals(controlDto.getRepresentative().getFullName(), "Петров Петр Петрович");
  }

  @Test
  public void getTurnoverByOperationJournal() throws Exception {
    GetOperationJournalReq request = new GetOperationJournalReq();
    request.setFromDate("30.05.2017");
    request.setToDate("31.05.2017");

    List<Operation> operationList = new ArrayList<>();
    int identicalOpsNum = 3;
    for (OperationTypeCode type : OperationTypeCode.values()) {
      for (int i = 0; i < identicalOpsNum; i++) {
        Operation operation = new Operation();
        operation.setWorkplaceId("workplace" + i);
        operation.setCurrencyType("RUB");
        operation.setAmount(BigDecimal.valueOf(type.ordinal()));
        operation.setTypeCode(type);
        operationList.add(operation);
      }
    }
    when(reportProvider.getOperations(any(Date.class), any(Date.class), any(ClientInfo.class)))
        .thenReturn(operationList);
    Workplace workplace = new Workplace();
    workplace.setSubbranchCode("subbranch");
    when(reportProvider.getWorkplace(anyString())).thenReturn(workplace);

    String responseJson = performAndGetContent(post("/report/turnoverByOperationJournal")
        .content(toRequest(request)));
    GetOperationJournalResp response = toResponse(responseJson, GetOperationJournalResp.class);

    final List<OperationJournalDto> data = response.getData();
    assertNotNull(data);
    assertEquals(data.size(), OperationTypeCode.values().length);
    assertEquals(data.get(0).getClass(), OperationJournalDto.class);

    Map<OperationTypeCode, OperationDto> operationByTypeMap = new HashMap<>();
    for (OperationJournalDto journalDto : data) {
      OperationDto operation = journalDto.getOperation();
      operationByTypeMap.put(OperationTypeCode.getByCode(operation.getTypeCode()), operation);
    }
    for (OperationTypeCode type : OperationTypeCode.values()) {
      OperationDto operation = operationByTypeMap.get(type);
      assertNotNull(operation);
      assertEquals("subbranch", operation.getSubbranchCode());
      assertEquals("RUB", operation.getCurrencyType());
      assertEquals(String.valueOf(type.ordinal() * identicalOpsNum), operation.getAmount());
    }
  }
}
