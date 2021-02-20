package ru.philit.ufs.model.converter.esb.asfs;

import java.util.Date;
import java.util.UUID;
import org.mapstruct.AfterMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.ValueMapping;
import org.mapstruct.ValueMappings;
import org.mapstruct.factory.Mappers;
import ru.philit.ufs.model.converter.esb.CommonAdapter;
import ru.philit.ufs.model.entity.account.AccountType;
import ru.philit.ufs.model.entity.common.ExternalEntityList;
import ru.philit.ufs.model.entity.common.OperationTypeCode;
import ru.philit.ufs.model.entity.esb.asfs.HeaderInfoType;
import ru.philit.ufs.model.entity.esb.asfs.OpStatusType;
import ru.philit.ufs.model.entity.esb.asfs.OperTypeLabel;
import ru.philit.ufs.model.entity.esb.asfs.SrvCommitOperationRq;
import ru.philit.ufs.model.entity.esb.asfs.SrvCommitOperationRs;
import ru.philit.ufs.model.entity.esb.asfs.SrvCreateOperationRq;
import ru.philit.ufs.model.entity.esb.asfs.SrvCreateOperationRs;
import ru.philit.ufs.model.entity.esb.asfs.SrvGetOperationRq;
import ru.philit.ufs.model.entity.esb.asfs.SrvGetOperationRs;
import ru.philit.ufs.model.entity.esb.asfs.SrvGetOperationRs.SrvGetOperationRsMessage.OperationItem;
import ru.philit.ufs.model.entity.esb.asfs.SrvRollbackOperationRq;
import ru.philit.ufs.model.entity.esb.asfs.SrvRollbackOperationRs;
import ru.philit.ufs.model.entity.esb.asfs.SrvUpdOperationRq;
import ru.philit.ufs.model.entity.esb.asfs.SrvUpdOperationRs;
import ru.philit.ufs.model.entity.oper.GetOperationRequest;
import ru.philit.ufs.model.entity.oper.Operation;
import ru.philit.ufs.model.entity.oper.OperationStatus;

@Mapper(imports = {Date.class})
public abstract class MapStructOperationAdapter extends CommonAdapter {

  /**
   * Экземпляр реализации адаптера.
   */
  public static MapStructOperationAdapter INSTANCE =
      Mappers.getMapper(MapStructOperationAdapter.class);

  //******** Converters ********

  @ValueMappings({
      @ValueMapping(source = "CANCELLED", target = "CANCELLED"),
      @ValueMapping(source = "COMMITTED", target = "COMMITTED"),
      @ValueMapping(source = "NEW", target = "NEW"),
      @ValueMapping(source = "ADVANCE_RESERVATION", target = "PENDING")
  })
  protected abstract OperationStatus operationStatus(OpStatusType opStatusType);

  @InheritInverseConfiguration
  protected abstract OpStatusType operationStatus(OperationStatus opStatusType);

  protected AccountType accountType(String accountTypeId) {
    return (accountTypeId != null) ? AccountType.getByCode(accountTypeId) : null;
  }

  protected String accountType(AccountType accountType) {
    return accountType.code();
  }

  protected abstract OperationTypeCode operationTypeCode(OperTypeLabel operationType);

  protected abstract OperTypeLabel operationTypeCode(OperationTypeCode operationType);

  //******** Methods *******

  /**
   * Создаёт HeaderInfo для транспортного объекта интеграции с АС ФС.
   */
  protected HeaderInfoType headerInfo() {
    HeaderInfoType headerInfo = new HeaderInfoType();
    headerInfo.setRqUID(UUID.randomUUID().toString());
    headerInfo.setRqTm(xmlCalendar(new Date()));
    headerInfo.setSpName("ufs");
    headerInfo.setSystemId("asfs");
    return headerInfo;
  }

  /**
   * Возвращает объект запроса на подтверждение операции.
   */
  @Mappings({
      @Mapping(expression = "java(headerInfo())", target = "headerInfo"),
      @Mapping(source = "operation.id", target = "srvCommitOperationRqMessage.operationId"),
  })
  public abstract SrvCommitOperationRq requestCommitOperation(Operation operation);

  /**
   * Возвращает объект запроса на резервирование номера кассовой операции с указанными id,
   * workplaceId и operationStatusCode.
   */
  @Mappings({
      @Mapping(expression = "java(headerInfo())", target = "headerInfo"),
      @Mapping(source = "operation.operatorId", target = "srvCreateOperationRqMessage.operatorId"),
      @Mapping(source = "operation.workplaceId",
          target = "srvCreateOperationRqMessage.workPlaceUId"),
      @Mapping(source = "operation.typeCode", target = "srvCreateOperationRqMessage.operationType"),
  })
  public abstract SrvCreateOperationRq requestCreateOperation(Operation operation);

  /**
   * Возвращает объект запроса на сторнирование операции по id.
   */
  @Mappings({
      @Mapping(expression = "java(headerInfo())", target = "headerInfo"),
      @Mapping(source = "operation.id", target = "srvRollbackOperationRqMessage.operationId"),
      @Mapping(source = "operation.rollbackReason",
          target = "srvRollbackOperationRqMessage.rollbackReason")
  })
  public abstract SrvRollbackOperationRq requestRollbackOperation(Operation operation);

  /**
   * Возвращает объект запроса на сохранение атрибутов кассовой операции.
   */
  @Mappings({
      @Mapping(expression = "java(headerInfo())", target = "headerInfo"),
      @Mapping(target = "srvUpdOperationRqMessage.operationId", source = "operation.id"),
      @Mapping(target = "srvUpdOperationRqMessage.cashOrderId", source = "operation.cashOrderId"),
      @Mapping(target = "srvUpdOperationRqMessage.operationNum", source = "operation.docNumber"),
      @Mapping(target = "srvUpdOperationRqMessage.operationStatus", source = "operation.status"),
      @Mapping(target = "srvUpdOperationRqMessage.operationType", source = "operation.typeCode"),
      @Mapping(target = "srvUpdOperationRqMessage.workPlaceUId", source = "operation.workplaceId"),
      @Mapping(target = "srvUpdOperationRqMessage.operatorId", source = "operation.operatorId"),
      @Mapping(target = "srvUpdOperationRqMessage.repId", source = "operation.representativeId"),
      @Mapping(target = "srvUpdOperationRqMessage.senderAccountTypeId",
          source = "operation.senderAccountType"),
      @Mapping(target = "srvUpdOperationRqMessage.senderAccountCurrencyType",
          source = "operation.senderAccountCurrencyType"),
      @Mapping(target = "srvUpdOperationRqMessage.senderBank", source = "operation.senderBank"),
      @Mapping(target = "srvUpdOperationRqMessage.senderBankBIC",
          source = "operation.senderBankBic"),
      @Mapping(target = "srvUpdOperationRqMessage.senderAccountId",
          source = "operation.senderAccountId"),
      @Mapping(target = "srvUpdOperationRqMessage.amount", source = "operation.amount"),
      @Mapping(target = "srvUpdOperationRqMessage.recipientAccountTypeId",
          source = "operation.recipientAccountType"),
      @Mapping(target = "srvUpdOperationRqMessage.recipientAccountCurrencyType",
          source = "operation.recipientAccountCurrencyType"),
      @Mapping(target = "srvUpdOperationRqMessage.recipientBank",
          source = "operation.recipientBank"),
      @Mapping(target = "srvUpdOperationRqMessage.recipientBankBIC",
          source = "operation.recipientBankBic"),
      @Mapping(target = "srvUpdOperationRqMessage.recipientAccountId",
          source = "operation.recipientAccountId"),
      @Mapping(target = "srvUpdOperationRqMessage.currencyType",
          source = "operation.currencyType")
  })
  public abstract SrvUpdOperationRq requestUpdOperation(final Operation operation);

  /**
   * Возвращает объект запроса на получение кассовой операции по id и/или дате создания.
   */
  @Mappings({
      @Mapping(expression = "java(headerInfo())", target = "headerInfo"),
      @Mapping(source = "request.id", target = "srvGetOperationRqMessage.operationId"),
      @Mapping(source = "request.createdFrom", target = "srvGetOperationRqMessage.createdFrom"),
      @Mapping(source = "request.createdTo", target = "srvGetOperationRqMessage.createdTo")
  })
  public abstract SrvGetOperationRq requestGetOperation(GetOperationRequest request);

  /**
   * Преобразует транспортный объект ответа во внутреннюю сущность.
   */
  @Mappings({
      @Mapping(source = "response.headerInfo.rqUID", target = "requestUid"),
      @Mapping(expression = "java(new Date())", target = "receiveDate"),
      @Mapping(source = "response.srvCommitOperationRsMessage.operationId", target = "id"),
      @Mapping(source = "response.srvCommitOperationRsMessage.operationStatus", target = "status"),
      @Mapping(source = "response.srvCommitOperationRsMessage.committedDttm",
          target = "committedDate"),
      @Mapping(source = "response.srvCommitOperationRsMessage.responseCode",
          target = "responseCode"),
  })
  public abstract Operation convert(SrvCommitOperationRs response);

  /**
   * Преобразует транспортный объект ответа во внутреннюю сущность.
   */
  @Mappings({
      @Mapping(source = "response.headerInfo.rqUID", target = "requestUid"),
      @Mapping(expression = "java(new Date())", target = "receiveDate"),
      @Mapping(source = "response.srvCreateOperationRsMessage.operationId", target = "id"),
      @Mapping(source = "response.srvCreateOperationRsMessage.operationStatus", target = "status"),
      @Mapping(source = "response.srvCreateOperationRsMessage.createdDttm",
          target = "createdDate"),
      @Mapping(source = "response.srvCreateOperationRsMessage.responseCode",
          target = "responseCode"),
  })
  public abstract Operation convert(SrvCreateOperationRs response);

  /**
   * Преобразует транспортный объект ответа во внутреннюю сущность.
   */
  @Mappings({
      @Mapping(source = "response.headerInfo.rqUID", target = "requestUid"),
      @Mapping(expression = "java(new Date())", target = "receiveDate"),
      @Mapping(source = "response.srvRollbackOperationRsMessage.operationId", target = "id"),
      @Mapping(source = "response.srvRollbackOperationRsMessage.operationStatus",
          target = "status"),
      @Mapping(source = "response.srvRollbackOperationRsMessage.responseCode",
          target = "responseCode"),
  })
  public abstract Operation convert(SrvRollbackOperationRs response);

  /**
   * Преобразует транспортный объект ответа во внутреннюю сущность.
   */
  @Mappings({
      @Mapping(source = "response.headerInfo.rqUID", target = "requestUid"),
      @Mapping(expression = "java(new Date())", target = "receiveDate"),
      @Mapping(source = "response.srvUpdOperationRsMessage.operationId", target = "id"),
      @Mapping(source = "response.srvUpdOperationRsMessage.operationStatus", target = "status"),
      @Mapping(source = "response.srvUpdOperationRsMessage.responseCode",
          target = "responseCode"),
  })
  public abstract Operation convert(SrvUpdOperationRs response);

  /**
   * Преобразует транспортный объект списка операций во внутреннюю сущность.
   */
  @Mappings({
      @Mapping(source = "response.headerInfo.rqUID", target = "requestUid"),
      @Mapping(expression = "java(new Date())", target = "receiveDate"),
      @Mapping(source = "response.srvGetOperationRsMessage.operationItem", target = "items"),

  })
  public abstract ExternalEntityList<Operation> convert(SrvGetOperationRs response);

  @Mappings({
      @Mapping(source = "operationItem.operationId", target = "id"),
      @Mapping(source = "operationItem.cashOrderId", target = "cashOrderId"),
      @Mapping(source = "operationItem.operationNum", target = "docNumber"),
      @Mapping(source = "operationItem.operationStatus", target = "status"),
      @Mapping(source = "operationItem.operationType", target = "typeCode"),
      @Mapping(source = "operationItem.workPlaceUId", target = "workplaceId"),
      @Mapping(source = "operationItem.operatorId", target = "operatorId"),
      @Mapping(source = "operationItem.repId", target = "representativeId"),
      @Mapping(source = "operationItem.createdDttm", target = "createdDate"),
      @Mapping(source = "operationItem.committedDttm", target = "committedDate"),
      @Mapping(source = "operationItem.senderAccountTypeId", target = "senderAccountType"),
      @Mapping(source = "operationItem.senderAccountCurrencyType",
          target = "senderAccountCurrencyType"),
      @Mapping(source = "operationItem.senderBank", target = "senderBank"),
      @Mapping(source = "operationItem.senderBankBIC", target = "senderBankBic"),
      @Mapping(source = "operationItem.senderAccountId", target = "senderAccountId"),
      @Mapping(source = "operationItem.amount", target = "amount"),
      @Mapping(source = "operationItem.recipientAccountTypeId", target = "recipientAccountType"),
      @Mapping(source = "operationItem.recipientAccountCurrencyType",
          target = "recipientAccountCurrencyType"),
      @Mapping(source = "operationItem.recipientBank", target = "recipientBank"),
      @Mapping(source = "operationItem.recipientBankBIC", target = "recipientBankBic"),
      @Mapping(source = "operationItem.recipientAccountId", target = "recipientAccountId"),
      @Mapping(source = "operationItem.currencyType", target = "currencyType"),
      @Mapping(source = "operationItem.rollbackReason", target = "rollbackReason")
  })
  protected abstract Operation convert(OperationItem operationItem);

  @AfterMapping
  protected void setHeaderInfoToOperations(@MappingTarget ExternalEntityList<Operation> list) {
    for (Operation item : list.getItems()) {
      item.setRequestUid(list.getRequestUid());
      item.setReceiveDate(new Date());
    }
  }
}
