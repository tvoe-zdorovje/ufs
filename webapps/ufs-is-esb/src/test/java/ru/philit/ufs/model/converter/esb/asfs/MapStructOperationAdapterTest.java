package ru.philit.ufs.model.converter.esb.asfs;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.xml.datatype.XMLGregorianCalendar;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.philit.ufs.model.converter.ConverterBaseTest;
import ru.philit.ufs.model.entity.account.AccountType;
import ru.philit.ufs.model.entity.common.ExternalEntityList;
import ru.philit.ufs.model.entity.common.OperationTypeCode;
import ru.philit.ufs.model.entity.esb.asfs.OpStatusType;
import ru.philit.ufs.model.entity.esb.asfs.OperTypeLabel;
import ru.philit.ufs.model.entity.esb.asfs.SrvCommitOperationRq;
import ru.philit.ufs.model.entity.esb.asfs.SrvCommitOperationRs;
import ru.philit.ufs.model.entity.esb.asfs.SrvCommitOperationRs.SrvCommitOperationRsMessage;
import ru.philit.ufs.model.entity.esb.asfs.SrvCreateOperationRq;
import ru.philit.ufs.model.entity.esb.asfs.SrvCreateOperationRq.SrvCreateOperationRqMessage;
import ru.philit.ufs.model.entity.esb.asfs.SrvCreateOperationRs;
import ru.philit.ufs.model.entity.esb.asfs.SrvCreateOperationRs.SrvCreateOperationRsMessage;
import ru.philit.ufs.model.entity.esb.asfs.SrvGetOperationRq;
import ru.philit.ufs.model.entity.esb.asfs.SrvGetOperationRq.SrvGetOperationRqMessage;
import ru.philit.ufs.model.entity.esb.asfs.SrvGetOperationRs;
import ru.philit.ufs.model.entity.esb.asfs.SrvGetOperationRs.SrvGetOperationRsMessage;
import ru.philit.ufs.model.entity.esb.asfs.SrvGetOperationRs.SrvGetOperationRsMessage.OperationItem;
import ru.philit.ufs.model.entity.esb.asfs.SrvRollbackOperationRq;
import ru.philit.ufs.model.entity.esb.asfs.SrvRollbackOperationRq.SrvRollbackOperationRqMessage;
import ru.philit.ufs.model.entity.esb.asfs.SrvRollbackOperationRs;
import ru.philit.ufs.model.entity.esb.asfs.SrvRollbackOperationRs.SrvRollbackOperationRsMessage;
import ru.philit.ufs.model.entity.esb.asfs.SrvUpdOperationRq;
import ru.philit.ufs.model.entity.esb.asfs.SrvUpdOperationRq.SrvUpdOperationRqMessage;
import ru.philit.ufs.model.entity.esb.asfs.SrvUpdOperationRs;
import ru.philit.ufs.model.entity.esb.asfs.SrvUpdOperationRs.SrvUpdOperationRsMessage;
import ru.philit.ufs.model.entity.oper.GetOperationRequest;
import ru.philit.ufs.model.entity.oper.Operation;
import ru.philit.ufs.model.entity.oper.OperationStatus;

public class MapStructOperationAdapterTest extends AsfsAdapterBaseTest {

  private static SrvCommitOperationRs responseCommitOp;
  private static SrvCreateOperationRs responseCreateOp;
  private static SrvRollbackOperationRs responseRollbackOp;
  private static SrvUpdOperationRs responseUpdOp;
  private static SrvGetOperationRs responseGetOp;

  private static final MapStructOperationAdapter ADAPTER = MapStructOperationAdapter.INSTANCE;

  private static final String FIX_UUID = "a39e0168-6f95-4297-ad26-c7f3d3cb07a8";
  private static final XMLGregorianCalendar REQ_DATE_TIME =
      ConverterBaseTest.xmlCalendar(2021, 1, 15, 3, 34);
  private static final XMLGregorianCalendar CREATED_CALENDAR =
      ConverterBaseTest.xmlCalendar(2021, 1, 12, 13, 11);
  private static final XMLGregorianCalendar COMMITED_CALENDAR =
      ConverterBaseTest.xmlCalendar(2021, 1, 12, 14, 22);
  private static final Date CREATED_DATE =
      ConverterBaseTest.date(2021, 1, 12, 13, 11);
  private static final Date COMMITED_DATE =
      ConverterBaseTest.date(2021, 1, 12, 14, 22);
  private static final String OPERATION_ID = "88005553535";
  private static final String OPERATION_NUMBER = "2334";
  private static final String OPERATION_STATUS_CODE = OperationStatus.CANCELLED.code();
  private static final String OPERATION_TYPE_CODE = OperationTypeCode.TO_ACCOUNT_DEPOSIT_RUB.code();
  private static final String RESPONSE_CODE = "0";
  private static final String CASH_ORDER_ID = "ca5h0rd3r";
  private static final String WORKPLACE_UID = "w07k9lac3u1d";
  private static final String OPERATOR_ID = "0p3r1t0r1d";
  private static final String REP_ID = "r3p1d";
  private static final String SENDER_ACCOUNT_ID = "100001";
  private static final String SENDER_ACCOUNT_TYPE_ID = AccountType.PAYMENT.code();
  private static final String SENDER_ACCOUNT_CURRENCY_TYPE = "RUB";
  private static final String SENDER_BANK = "#12";
  private static final String SENDER_BANK_BIC = "0044221";
  private static final BigDecimal AMOUNT = BigDecimal.TEN;
  private static final String RECIPIENT_ACCOUNT_ID = "100002";
  private static final String RECIPIENT_ACCOUNT_TYPE_ID = AccountType.BUDGET.code();
  private static final String RECIPIENT_ACCOUNT_CURRENCY_TYPE = "EUR";
  private static final String RECIPIENT_BANK = "#7";
  private static final String RECIPIENT_BANK_BIC = "012345";
  private static final String CURRENCY_TYPE = "USD";
  private static final String ROLLBACK_REASON = "I don't like you.";
  private static final int OPERATION_ITEM_LIST_SIZE = 5;

  /**
   * Set up test data.
   */
  @BeforeClass
  public static void setUp() throws Exception {
    // Commit

    responseCommitOp = new SrvCommitOperationRs();
    responseCommitOp.setHeaderInfo(headerInfo(FIX_UUID));
    responseCommitOp.getHeaderInfo().setRqTm(REQ_DATE_TIME);
    final SrvCommitOperationRsMessage commitOpMessage = new SrvCommitOperationRsMessage();
    commitOpMessage.setOperationId(OPERATION_ID);
    commitOpMessage.setCommittedDttm(COMMITED_CALENDAR);
    commitOpMessage.setOperationStatus(OpStatusType.fromValue(OPERATION_STATUS_CODE));
    commitOpMessage.setResponseCode(RESPONSE_CODE);
    responseCommitOp.setSrvCommitOperationRsMessage(commitOpMessage);

    // Create

    responseCreateOp = new SrvCreateOperationRs();
    responseCreateOp.setHeaderInfo(headerInfo(FIX_UUID));
    responseCreateOp.getHeaderInfo().setRqTm(REQ_DATE_TIME);
    final SrvCreateOperationRsMessage createOpMessage = new SrvCreateOperationRsMessage();
    createOpMessage.setOperationId(OPERATION_ID);
    createOpMessage.setOperationStatus(OpStatusType.fromValue(OPERATION_STATUS_CODE));
    createOpMessage.setCreatedDttm(CREATED_CALENDAR);
    createOpMessage.setResponseCode(RESPONSE_CODE);
    responseCreateOp.setSrvCreateOperationRsMessage(createOpMessage);

    // Rollback

    responseRollbackOp = new SrvRollbackOperationRs();
    responseRollbackOp.setHeaderInfo(headerInfo(FIX_UUID));
    responseRollbackOp.getHeaderInfo().setRqTm(REQ_DATE_TIME);
    final SrvRollbackOperationRsMessage rollbackOpMessage = new SrvRollbackOperationRsMessage();
    rollbackOpMessage.setOperationId(OPERATION_ID);
    rollbackOpMessage.setOperationStatus(OpStatusType.fromValue(OPERATION_STATUS_CODE));
    rollbackOpMessage.setResponseCode(RESPONSE_CODE);
    responseRollbackOp.setSrvRollbackOperationRsMessage(rollbackOpMessage);

    // Update

    responseUpdOp = new SrvUpdOperationRs();
    responseUpdOp.setHeaderInfo(headerInfo(FIX_UUID));
    responseUpdOp.getHeaderInfo().setRqTm(REQ_DATE_TIME);
    final SrvUpdOperationRsMessage updOpMessage = new SrvUpdOperationRsMessage();
    updOpMessage.setOperationId(OPERATION_ID);
    updOpMessage.setOperationStatus(OpStatusType.fromValue(OPERATION_STATUS_CODE));
    updOpMessage.setResponseCode(RESPONSE_CODE);
    responseUpdOp.setSrvUpdOperationRsMessage(updOpMessage);

    // Get

    responseGetOp = new SrvGetOperationRs();
    responseGetOp.setHeaderInfo(headerInfo(FIX_UUID));
    responseGetOp.getHeaderInfo().setRqTm(REQ_DATE_TIME);

    final SrvGetOperationRsMessage getOpMessage = new SrvGetOperationRsMessage();
    final List<OperationItem> operationItemList = getOpMessage.getOperationItem();
    for (int i = 0; i < OPERATION_ITEM_LIST_SIZE; i++) {
      OperationItem operationItem = new OperationItem();
      final String postfix = String.valueOf(i).intern();
      operationItem.setOperationId(OPERATION_ID.concat(postfix));
      operationItem.setCashOrderId(CASH_ORDER_ID.concat(postfix));
      operationItem.setOperationNum(OPERATION_NUMBER.concat(postfix));
      operationItem.setOperationStatus(OpStatusType.fromValue(OPERATION_STATUS_CODE));
      operationItem.setOperationType(OperTypeLabel.fromValue(OPERATION_TYPE_CODE));
      operationItem.setWorkPlaceUId(WORKPLACE_UID.concat(postfix));
      operationItem.setOperatorId(OPERATOR_ID.concat(postfix));
      operationItem.setRepId(REP_ID.concat(postfix));                          // vv REMEMBER THIS!!
      operationItem.setCreatedDttm(cloneDateWithMinutes(CREATED_CALENDAR, i % 60)); // ^^
      operationItem.setCommittedDttm(cloneDateWithMinutes(COMMITED_CALENDAR, (i * 2) % 60));//
      operationItem.setSenderAccountId(SENDER_ACCOUNT_ID.concat(postfix));
      operationItem.setSenderAccountTypeId(SENDER_ACCOUNT_TYPE_ID);
      operationItem.setSenderAccountCurrencyType(SENDER_ACCOUNT_CURRENCY_TYPE.concat(postfix));
      operationItem.setSenderBank(SENDER_BANK.concat(postfix));
      operationItem.setSenderBankBIC(SENDER_BANK_BIC.concat(postfix));
      operationItem.setAmount(AMOUNT.add(BigDecimal.valueOf(i)));
      operationItem.setRecipientAccountId(RECIPIENT_ACCOUNT_ID.concat(postfix));
      operationItem.setRecipientAccountTypeId(RECIPIENT_ACCOUNT_TYPE_ID);
      operationItem.setRecipientAccountCurrencyType(RECIPIENT_ACCOUNT_CURRENCY_TYPE
          .concat(postfix));
      operationItem.setRecipientBank(RECIPIENT_BANK.concat(postfix));
      operationItem.setRecipientBankBIC(RECIPIENT_BANK_BIC.concat(postfix));
      operationItem.setCurrencyType(CURRENCY_TYPE.concat(postfix));
      operationItem.setRollbackReason(ROLLBACK_REASON.concat(postfix));
      operationItemList.add(operationItem);
    }
    responseGetOp.setSrvGetOperationRsMessage(getOpMessage);
  }

  private static XMLGregorianCalendar cloneDateWithMinutes(XMLGregorianCalendar calendar,
      int minutes) {
    Objects.requireNonNull(calendar);
    XMLGregorianCalendar clone = (XMLGregorianCalendar) calendar.clone();
    clone.setMinute(minutes);
    return clone;
  }

  private static Date date(XMLGregorianCalendar xmlCalendar) {
    return (xmlCalendar != null) ? xmlCalendar.toGregorianCalendar().getTime() : null;
  }

  @Test
  public void testRequestCommitOperation() {
    final Operation operation = new Operation();
    operation.setId(OPERATION_ID);
    final SrvCommitOperationRq request = ADAPTER.requestCommitOperation(operation);
    assertHeaderInfo(request.getHeaderInfo());
    Assert.assertNotNull(request.getSrvCommitOperationRqMessage());
    Assert.assertEquals(OPERATION_ID, request.getSrvCommitOperationRqMessage().getOperationId());
  }

  @Test
  public void testRequestCreateOperation() {
    final Operation operation = new Operation();
    operation.setOperatorId(OPERATOR_ID);
    operation.setWorkplaceId(WORKPLACE_UID);
    operation.setTypeCode(OperationTypeCode.getByCode(OPERATION_TYPE_CODE));
    final SrvCreateOperationRq request = ADAPTER.requestCreateOperation(operation);
    assertHeaderInfo(request.getHeaderInfo());
    final SrvCreateOperationRqMessage message = request.getSrvCreateOperationRqMessage();
    Assert.assertNotNull(message);
    Assert.assertEquals(OPERATOR_ID, message.getOperatorId());
    Assert.assertEquals(WORKPLACE_UID, message.getWorkPlaceUId());
    Assert.assertEquals(OperTypeLabel.fromValue(OPERATION_TYPE_CODE), message.getOperationType());

  }

  @Test
  public void testRequestRollbackOperation() {
    final Operation operation = new Operation();
    operation.setId(OPERATION_ID);
    operation.setRollbackReason(ROLLBACK_REASON);
    final SrvRollbackOperationRq request =
        ADAPTER.requestRollbackOperation(operation);
    assertHeaderInfo(request.getHeaderInfo());
    final SrvRollbackOperationRqMessage message = request.getSrvRollbackOperationRqMessage();
    Assert.assertNotNull(message);
    Assert.assertEquals(OPERATION_ID, message.getOperationId());
    Assert.assertEquals(ROLLBACK_REASON, message.getRollbackReason());
  }

  @Test
  public void testRequestUpdOperation() {
    final SrvUpdOperationRq request =
        ADAPTER.requestUpdOperation(createOperation());
    assertHeaderInfo(request.getHeaderInfo());
    final SrvUpdOperationRqMessage message = request.getSrvUpdOperationRqMessage();
    Assert.assertNotNull(message);
    Assert.assertEquals(OPERATION_ID, message.getOperationId());
    Assert.assertEquals(CASH_ORDER_ID, message.getCashOrderId());
    Assert.assertEquals(OPERATION_NUMBER, message.getOperationNum());
    Assert.assertEquals(OPERATION_STATUS_CODE, message.getOperationStatus().value());
    Assert.assertEquals(OPERATION_TYPE_CODE, message.getOperationType().value());
    Assert.assertEquals(WORKPLACE_UID, message.getWorkPlaceUId());
    Assert.assertEquals(OPERATOR_ID, message.getOperatorId());
    Assert.assertEquals(REP_ID, message.getRepId());
    Assert.assertEquals(SENDER_ACCOUNT_ID, message.getSenderAccountId());
    Assert.assertEquals(SENDER_ACCOUNT_TYPE_ID, message.getSenderAccountTypeId());
    Assert.assertEquals(SENDER_ACCOUNT_CURRENCY_TYPE, message.getSenderAccountCurrencyType());
    Assert.assertEquals(SENDER_BANK, message.getSenderBank());
    Assert.assertEquals(SENDER_BANK_BIC, message.getSenderBankBIC());
    Assert.assertEquals(AMOUNT, message.getAmount());
    Assert.assertEquals(RECIPIENT_ACCOUNT_ID, message.getRecipientAccountId());
    Assert.assertEquals(RECIPIENT_ACCOUNT_TYPE_ID, message.getRecipientAccountTypeId());
    Assert.assertEquals(RECIPIENT_ACCOUNT_CURRENCY_TYPE, message.getRecipientAccountCurrencyType());
    Assert.assertEquals(RECIPIENT_BANK, message.getRecipientBank());
    Assert.assertEquals(RECIPIENT_BANK_BIC, message.getRecipientBankBIC());
    Assert.assertEquals(CURRENCY_TYPE, message.getCurrencyType());
  }

  private static Operation createOperation() {
    Operation operation = new Operation();
    operation.setId(OPERATION_ID);
    operation.setCashOrderId(CASH_ORDER_ID);
    operation.setDocNumber(OPERATION_NUMBER);
    operation.setStatus(OperationStatus.getByCode(OPERATION_STATUS_CODE));
    operation.setTypeCode(OperationTypeCode.getByCode(OPERATION_TYPE_CODE));
    operation.setWorkplaceId(WORKPLACE_UID);
    operation.setOperatorId(OPERATOR_ID);
    operation.setRepresentativeId(REP_ID);
    operation.setSenderAccountId(SENDER_ACCOUNT_ID);
    operation.setSenderAccountType(AccountType.getByCode(SENDER_ACCOUNT_TYPE_ID));
    operation.setSenderAccountCurrencyType(SENDER_ACCOUNT_CURRENCY_TYPE);
    operation.setSenderBank(SENDER_BANK);
    operation.setSenderBankBic(SENDER_BANK_BIC);
    operation.setAmount(AMOUNT);
    operation.setRecipientAccountId(RECIPIENT_ACCOUNT_ID);
    operation.setRecipientAccountType(AccountType.getByCode(RECIPIENT_ACCOUNT_TYPE_ID));
    operation.setRecipientAccountCurrencyType(RECIPIENT_ACCOUNT_CURRENCY_TYPE);
    operation.setRecipientBank(RECIPIENT_BANK);
    operation.setRecipientBankBic(RECIPIENT_BANK_BIC);
    operation.setCurrencyType(CURRENCY_TYPE);
    return operation;
  }

  @Test
  public void testRequestGetOperation() {
    final GetOperationRequest getOperationRequest = new GetOperationRequest();
    getOperationRequest.setId(OPERATION_ID);
    getOperationRequest.setCreatedFrom(CREATED_DATE);
    getOperationRequest.setCreatedTo(COMMITED_DATE);
    final SrvGetOperationRq request =
        ADAPTER.requestGetOperation(getOperationRequest);
    assertHeaderInfo(request.getHeaderInfo());
    final SrvGetOperationRqMessage message = request.getSrvGetOperationRqMessage();
    Assert.assertNotNull(message);
    Assert.assertEquals(OPERATION_ID, message.getOperationId());
    Assert.assertEquals(CREATED_CALENDAR, message.getCreatedFrom());
    Assert.assertEquals(COMMITED_CALENDAR, message.getCreatedTo());
  }

  @Test
  public void testConvertSrvCommitOperationRs() {
    final Operation operation = ADAPTER.convert(responseCommitOp);
    assertOperationBase(operation);
    Assert.assertEquals(COMMITED_DATE, operation.getCommittedDate());
  }

  @Test
  public void testConvertSrvCreateOperationRs() {
    final Operation operation = ADAPTER.convert(responseCreateOp);
    assertOperationBase(operation);
    Assert.assertEquals(CREATED_DATE, operation.getCreatedDate());
  }

  @Test
  public void testConvertSrvRollbackOperationRs() {
    final Operation operation = ADAPTER.convert(responseRollbackOp);
    assertOperationBase(operation);
  }

  @Test
  public void testConvertSrvUpdOperationRs() {
    final Operation operation = ADAPTER.convert(responseUpdOp);
    assertOperationBase(operation);
  }

  @Test
  public void testConvertSrvGetOperationRs() {
    final ExternalEntityList<Operation> list = ADAPTER.convert(responseGetOp);
    assertHeaderInfo(list, FIX_UUID);
    final List<Operation> items = list.getItems();
    Assert.assertNotNull(items);
    Assert.assertEquals(OPERATION_ITEM_LIST_SIZE, items.size());
    for (int i = 0; i < items.size(); i++) {
      String postfix = String.valueOf(i);
      Operation operation = items.get(i);
      assertHeaderInfo(operation, FIX_UUID);
      Assert.assertEquals(OPERATION_ID.concat(postfix), operation.getId());
      Assert.assertEquals(CASH_ORDER_ID.concat(postfix), operation.getCashOrderId());
      Assert.assertEquals(OPERATION_NUMBER.concat(postfix), operation.getDocNumber());
      Assert.assertEquals(OperationStatus.getByCode(OPERATION_STATUS_CODE), operation.getStatus());
      Assert
          .assertEquals(OperationTypeCode.getByCode(OPERATION_TYPE_CODE), operation.getTypeCode());
      Assert.assertEquals(WORKPLACE_UID.concat(postfix), operation.getWorkplaceId());
      Assert.assertEquals(OPERATOR_ID.concat(postfix), operation.getOperatorId());
      Assert.assertEquals(REP_ID.concat(postfix), operation.getRepresentativeId());
      Assert.assertEquals(date(cloneDateWithMinutes(CREATED_CALENDAR, i % 60)),
          operation.getCreatedDate());                                  // see setUp();
      Assert.assertEquals(date(cloneDateWithMinutes(COMMITED_CALENDAR, (i * 2) % 60)),
          operation.getCommittedDate());
      Assert.assertEquals(SENDER_ACCOUNT_ID.concat(postfix), operation.getSenderAccountId());
      Assert.assertEquals(AccountType.getByCode(SENDER_ACCOUNT_TYPE_ID),
          operation.getSenderAccountType());
      Assert.assertEquals(SENDER_ACCOUNT_CURRENCY_TYPE.concat(postfix),
          operation.getSenderAccountCurrencyType());
      Assert.assertEquals(SENDER_BANK.concat(postfix), operation.getSenderBank());
      Assert.assertEquals(SENDER_BANK_BIC.concat(postfix), operation.getSenderBankBic());
      Assert.assertEquals(AMOUNT.add(BigDecimal.valueOf(i)), operation.getAmount());
      Assert.assertEquals(RECIPIENT_ACCOUNT_ID.concat(postfix), operation.getRecipientAccountId());
      Assert.assertEquals(AccountType.getByCode(RECIPIENT_ACCOUNT_TYPE_ID),
          operation.getRecipientAccountType());
      Assert.assertEquals(RECIPIENT_ACCOUNT_CURRENCY_TYPE.concat(postfix),
          operation.getRecipientAccountCurrencyType());
      Assert.assertEquals(RECIPIENT_BANK.concat(postfix), operation.getRecipientBank());
      Assert.assertEquals(RECIPIENT_BANK_BIC.concat(postfix), operation.getRecipientBankBic());
      Assert.assertEquals(CURRENCY_TYPE.concat(postfix), operation.getCurrencyType());
      Assert.assertEquals(ROLLBACK_REASON.concat(postfix), operation.getRollbackReason());
    }
  }

  private void assertOperationBase(Operation operation) {
    assertHeaderInfo(operation, FIX_UUID);
    Assert.assertEquals(RESPONSE_CODE, operation.getResponseCode());
    Assert.assertEquals(OPERATION_ID, operation.getId());
    Assert.assertEquals(OPERATION_STATUS_CODE, operation.getStatus().code());
  }
}