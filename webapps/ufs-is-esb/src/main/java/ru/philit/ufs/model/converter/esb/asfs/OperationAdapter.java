package ru.philit.ufs.model.converter.esb.asfs;

import ru.philit.ufs.model.entity.account.AccountType;
import ru.philit.ufs.model.entity.common.ExternalEntityList;
import ru.philit.ufs.model.entity.common.OperationTypeCode;
import ru.philit.ufs.model.entity.esb.asfs.OpStatusType;
import ru.philit.ufs.model.entity.esb.asfs.OperTypeLabel;
import ru.philit.ufs.model.entity.esb.asfs.SrvCommitOperationRq;
import ru.philit.ufs.model.entity.esb.asfs.SrvCommitOperationRq.SrvCommitOperationRqMessage;
import ru.philit.ufs.model.entity.esb.asfs.SrvCommitOperationRs;
import ru.philit.ufs.model.entity.esb.asfs.SrvCommitOperationRs.SrvCommitOperationRsMessage;
import ru.philit.ufs.model.entity.esb.asfs.SrvCreateOperationRq;
import ru.philit.ufs.model.entity.esb.asfs.SrvCreateOperationRq.SrvCreateOperationRqMessage;
import ru.philit.ufs.model.entity.esb.asfs.SrvCreateOperationRs;
import ru.philit.ufs.model.entity.esb.asfs.SrvCreateOperationRs.SrvCreateOperationRsMessage;
import ru.philit.ufs.model.entity.esb.asfs.SrvGetOperationRq;
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

/**
 * Преобразователь между сущностью Account и соответствующим транспортным объектом.
 */
public class OperationAdapter extends AsfsAdapter {

  //******** Converters ********

  private static AccountType accountType(String accountTypeId) {
    return (accountTypeId != null) ? AccountType.getByCode(accountTypeId) : null;
  }

  private static OperationStatus operationStatus(OpStatusType opStatusType) {
    return (opStatusType != null) ? OperationStatus.getByCode(opStatusType.value()) : null;
  }

  private static OpStatusType operationStatus(OperationStatus operationStatus) {
    return (operationStatus != null) ? OpStatusType.fromValue(operationStatus.code()) : null;
  }

  private static OperationTypeCode operationTypeCode(OperTypeLabel operationType) {
    return (operationType != null) ? OperationTypeCode.getByCode(operationType.value()) : null;
  }

  private static OperTypeLabel operationTypeCode(OperationTypeCode operationTypeCode) {
    return (operationTypeCode != null) ? OperTypeLabel.fromValue(operationTypeCode.code()) : null;
  }

  //******** Mappers *******

  private static void map(SrvCommitOperationRsMessage message, Operation operation) {
    operation.setId(message.getOperationId());
    operation.setStatus(operationStatus(message.getOperationStatus()));
    operation.setCommittedDate(date(message.getCommittedDttm()));
    operation.setResponseCode(message.getResponseCode());
  }

  private static void map(SrvCreateOperationRsMessage message, Operation operation) {
    operation.setId(message.getOperationId());
    operation.setStatus(operationStatus(message.getOperationStatus()));
    operation.setCreatedDate(date(message.getCreatedDttm()));
    operation.setResponseCode(message.getResponseCode());
  }

  private static void map(SrvRollbackOperationRsMessage message, Operation operation) {
    operation.setId(message.getOperationId());
    operation.setStatus(operationStatus(message.getOperationStatus()));
    operation.setResponseCode(message.getResponseCode());
  }

  private static void map(SrvUpdOperationRsMessage message, Operation operation) {
    operation.setId(message.getOperationId());
    operation.setStatus(operationStatus(message.getOperationStatus()));
    operation.setResponseCode(message.getResponseCode());
  }

  private static void map(SrvGetOperationRsMessage.OperationItem message, Operation operation) {
    operation.setId(message.getOperationId());
    operation.setCashOrderId(message.getCashOrderId());
    operation.setDocNumber(message.getOperationNum());
    operation.setStatus(operationStatus(message.getOperationStatus()));
    operation.setTypeCode(operationTypeCode(message.getOperationType()));
    operation.setWorkplaceId(message.getWorkPlaceUId());
    operation.setOperatorId(message.getOperatorId());
    operation.setRepresentativeId(message.getRepId());
    operation.setCreatedDate(date(message.getCreatedDttm()));
    operation.setCommittedDate(date(message.getCommittedDttm()));
    operation.setSenderAccountId(message.getSenderAccountId());
    operation.setSenderAccountType(accountType(message.getSenderAccountTypeId()));
    operation.setSenderAccountCurrencyType(message.getSenderAccountCurrencyType());
    operation.setSenderBank(message.getSenderBank());
    operation.setSenderBankBic(message.getSenderBankBIC());
    operation.setAmount(message.getAmount());
    operation.setRecipientAccountId(message.getRecipientAccountId());
    operation.setRecipientAccountType(accountType(message.getRecipientAccountTypeId()));
    operation.setRecipientAccountCurrencyType(message.getRecipientAccountCurrencyType());
    operation.setRecipientBank(message.getRecipientBank());
    operation.setRecipientBankBic(message.getRecipientBankBIC());
    operation.setCurrencyType(message.getCurrencyType());
    operation.setRollbackReason(message.getRollbackReason());
  }

  private static void map(Operation params, SrvUpdOperationRqMessage message) {
    message.setOperationId(params.getId());
    message.setOperationNum(params.getDocNumber());
    message.setOperationStatus(operationStatus(params.getStatus()));
    message.setOperationType(operationTypeCode(params.getTypeCode()));
    message.setWorkPlaceUId(params.getWorkplaceId());
    message.setOperatorId(params.getOperatorId());
    message.setAmount(params.getAmount());
    message.setCashOrderId(params.getCashOrderId());
    message.setCurrencyType(params.getCurrencyType());
    message.setRepId(params.getRepresentativeId());
    message.setSenderAccountId(params.getSenderAccountId());
    if (params.getSenderAccountType() != null) {
      message.setSenderAccountTypeId(params.getSenderAccountType().code());
    }
    message.setSenderAccountCurrencyType(params.getSenderAccountCurrencyType());
    message.setSenderBank(params.getSenderBank());
    message.setSenderBankBIC(params.getSenderBankBic());
    message.setRecipientAccountId(params.getRecipientAccountId());
    if (params.getRecipientAccountType() != null) {
      message.setRecipientAccountTypeId(params.getRecipientAccountType().code());
    }
    message.setRecipientAccountCurrencyType(params.getRecipientAccountCurrencyType());
    message.setRecipientBank(params.getRecipientBank());
    message.setRecipientBankBIC(params.getRecipientBankBic());
  }

  //******** Methods *******

  /**
   * Возвращает объект запроса на подтверждение операции по id.
   */
  public static SrvCommitOperationRq requestCommitOperation(String operationId) {
    final SrvCommitOperationRq request = new SrvCommitOperationRq();
    request.setHeaderInfo(headerInfo());
    request.setSrvCommitOperationRqMessage(new SrvCommitOperationRqMessage());
    request.getSrvCommitOperationRqMessage().setOperationId(operationId);
    return request;
  }

  public static SrvCommitOperationRq requestCommitOperation(Operation operation) {
    return requestCommitOperation(operation.getId());
  }

  /**
   * Возвращает объект запроса на резервирование номера кассовой операции с указанными id,
   * workplaceId и operationStatusCode.
   */
  public static SrvCreateOperationRq requestCreateOperation(Operation operation) {
    final SrvCreateOperationRq request = new SrvCreateOperationRq();
    request.setHeaderInfo(headerInfo());
    request.setSrvCreateOperationRqMessage(new SrvCreateOperationRqMessage());
    request.getSrvCreateOperationRqMessage().setOperatorId(operation.getOperatorId());
    request.getSrvCreateOperationRqMessage().setWorkPlaceUId(operation.getWorkplaceId());
    request.getSrvCreateOperationRqMessage()
        .setOperationType(operationTypeCode(operation.getTypeCode()));
    return request;
  }

  /**
   * Возвращает объект запроса на сторнирование операции по id.
   */
  public static SrvRollbackOperationRq requestRollbackOperation(Operation operation) {
    final SrvRollbackOperationRq request = new SrvRollbackOperationRq();
    request.setHeaderInfo(headerInfo());
    request.setSrvRollbackOperationRqMessage(new SrvRollbackOperationRqMessage());
    request.getSrvRollbackOperationRqMessage().setOperationId(operation.getId());
    request.getSrvRollbackOperationRqMessage().setRollbackReason(operation.getRollbackReason());
    return request;
  }

  /**
   * Возвращает объект запроса на сохранение атрибутов кассовой операции.
   */
  public static SrvUpdOperationRq requestUpdOperation(final Operation params) {
    final SrvUpdOperationRq request = new SrvUpdOperationRq();
    request.setHeaderInfo(headerInfo());
    request.setSrvUpdOperationRqMessage(new SrvUpdOperationRqMessage());
    map(params, request.getSrvUpdOperationRqMessage());
    return request;
  }

  /**
   * Возвращает объект запроса на получение кассовой операции по id и/или дате создания.
   */
  public static SrvGetOperationRq requestGetOperation(GetOperationRequest params) {
    final SrvGetOperationRq request = new SrvGetOperationRq();
    request.setHeaderInfo(headerInfo());
    request.setSrvGetOperationRqMessage(new SrvGetOperationRq.SrvGetOperationRqMessage());
    request.getSrvGetOperationRqMessage().setOperationId(params.getId());
    request.getSrvGetOperationRqMessage().setCreatedFrom(xmlCalendar(params.getCreatedFrom()));
    request.getSrvGetOperationRqMessage().setCreatedTo(xmlCalendar(params.getCreatedTo()));
    return request;
  }

  /**
   * Преобразует транспортный объект ответа во внутреннюю сущность.
   */
  public static Operation convert(SrvCommitOperationRs response) {
    final Operation operation = new Operation();
    map(response.getHeaderInfo(), operation);
    map(response.getSrvCommitOperationRsMessage(), operation);
    return operation;
  }

  /**
   * Преобразует транспортный объект ответа во внутреннюю сущность.
   */
  public static Operation convert(SrvCreateOperationRs response) {
    final Operation operation = new Operation();
    map(response.getHeaderInfo(), operation);
    map(response.getSrvCreateOperationRsMessage(), operation);
    return operation;
  }

  /**
   * Преобразует транспортный объект ответа во внутреннюю сущность.
   */
  public static Operation convert(SrvRollbackOperationRs response) {
    final Operation operation = new Operation();
    map(response.getHeaderInfo(), operation);
    map(response.getSrvRollbackOperationRsMessage(), operation);
    return operation;
  }

  /**
   * Преобразует транспортный объект ответа во внутреннюю сущность.
   */
  public static Operation convert(SrvUpdOperationRs response) {
    final Operation operation = new Operation();
    map(response.getHeaderInfo(), operation);
    map(response.getSrvUpdOperationRsMessage(), operation);
    return operation;
  }

  /**
   * Преобразует транспортный объект списка операций во внутреннюю сущность.
   */
  public static ExternalEntityList<Operation> convert(SrvGetOperationRs response) {
    final ExternalEntityList<Operation> operationList = new ExternalEntityList<>();
    map(response.getHeaderInfo(), operationList);
    for (OperationItem operationItem : response.getSrvGetOperationRsMessage().getOperationItem()) {
      final Operation operation = new Operation();
      map(response.getHeaderInfo(), operation);
      map(operationItem, operation);
      operationList.getItems().add(operation);
    }
    return operationList;
  }
}
