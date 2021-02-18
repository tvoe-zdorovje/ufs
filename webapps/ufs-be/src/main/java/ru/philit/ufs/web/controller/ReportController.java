package ru.philit.ufs.web.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.philit.ufs.model.entity.account.Representative;
import ru.philit.ufs.model.entity.oper.Operation;
import ru.philit.ufs.model.entity.user.ClientInfo;
import ru.philit.ufs.model.entity.user.Operator;
import ru.philit.ufs.model.entity.user.User;
import ru.philit.ufs.web.dto.OperationDto;
import ru.philit.ufs.web.dto.OperationJournalDto;
import ru.philit.ufs.web.mapping.OperationJournalMapper;
import ru.philit.ufs.web.provider.ReportProvider;
import ru.philit.ufs.web.provider.RepresentativeProvider;
import ru.philit.ufs.web.view.GetOperationJournalReq;
import ru.philit.ufs.web.view.GetOperationJournalResp;

/**
 * Контроллер запроса данных отчётов.
 */
@RestController
@RequestMapping("/report")
public class ReportController {

  private final ReportProvider reportProvider;
  private final RepresentativeProvider representativeProvider;
  private final OperationJournalMapper operationJournalMapper;

  /**
   * Конструктор бина.
   */
  @Autowired
  public ReportController(
      ReportProvider reportProvider,
      RepresentativeProvider representativeProvider,
      OperationJournalMapper operationJournalMapper
  ) {
    this.reportProvider = reportProvider;
    this.representativeProvider = representativeProvider;
    this.operationJournalMapper = operationJournalMapper;
  }

  /**
   * Получение списка записей журнала операций.
   *
   * @param request    параметры запроса списка
   * @param clientInfo информация о клиенте
   * @return список записей
   */
  @RequestMapping(value = "/operationJournal", method = RequestMethod.POST)
  public GetOperationJournalResp getOperationJournal(
      @RequestBody GetOperationJournalReq request, ClientInfo clientInfo
  ) {
    List<OperationJournalDto> items = new ArrayList<>();

    for (Operation operation : reportProvider.getOperations(
        operationJournalMapper.asEntity(request.getFromDate()),
        operationJournalMapper.asEntity(request.getToDate()),
        clientInfo)) {

      // User user = clientInfo.getUser() WHY NOT??
      User user = reportProvider.getUser(clientInfo.getSessionId());
      BigDecimal commissionAmount = reportProvider.getCommission(
          operation.getSenderAccountId(), operation.getAmount(), operation, clientInfo
      );
      // КодТБ (tbCode), Код (Г)ОСБ (osbCode), Код ВСП (vspCode), Сотрудник (operator)
      Operator operator = reportProvider.getOperator(user.getLogin(), clientInfo);
      // ИНН клиента (inn), Клиент (legalEntityShortName), ФИО клиента/представителя
      Representative representative = representativeProvider
          .getRepresentativeById(operation.getRepresentativeId(), clientInfo);

      items.add(new OperationJournalDto()
          .withOperator(operationJournalMapper.asDto(operator))
          .withUser(operationJournalMapper.asDto(user))
          .withRepresentative(operationJournalMapper.asDto(representative))
          .withOperation(operationJournalMapper.asDto(operation))
          .withCommission(operationJournalMapper.asDto(commissionAmount))
      );
    }
    return new GetOperationJournalResp().withSuccess(items);
  }

  /**
   * Получение списка записей журнала оборотов по операциям.
   *
   * @param request    параметры запроса списка
   * @param clientInfo информация о клиенте
   * @return список записей
   */
  @RequestMapping(value = "/turnoverByOperationJournal", method = RequestMethod.POST)
  public GetOperationJournalResp getTurnoverByOperationJournal(
      @RequestBody GetOperationJournalReq request, ClientInfo clientInfo
  ) {
    // operations by Type+Currency+subbranchCode
    Map<String, OperationDto> operationMap = new HashMap<>();
    // amount by Type+Currency+subbranchCode
    Map<String, BigDecimal> amountMap = new HashMap<>();

    for (Operation operation : reportProvider.getOperations(
        operationJournalMapper.asEntity(request.getFromDate()),
        operationJournalMapper.asEntity(request.getToDate()),
        clientInfo)) {

      String subbranchCode =
          reportProvider.getWorkplace(operation.getWorkplaceId()).getSubbranchCode();
      String key = operation.getTypeCode().code() + operation.getCurrencyType() + subbranchCode;
      if (!operationMap.containsKey(key)) {
        OperationDto dto = new OperationDto();
        dto.setTypeCode(operation.getTypeCode().code());
        dto.setTypeName(operation.getTypeCode().value());
        dto.setCurrencyType(operation.getCurrencyType());
        dto.setSubbranchCode(subbranchCode);
        operationMap.put(key, dto);
      }
      if (amountMap.containsKey(key)) {
        BigDecimal newAmount = amountMap.get(key).add(operation.getAmount());
        amountMap.put(key, newAmount);
      } else {
        amountMap.put(key, operation.getAmount());
      }
    }
    List<OperationJournalDto> items = new ArrayList<>();
    for (Entry<String, OperationDto> opEntry : operationMap.entrySet()) {
      OperationDto operationDto = opEntry.getValue();
      operationDto.setAmount(String.valueOf(amountMap.get(opEntry.getKey())));
      items.add(new OperationJournalDto().withOperation(operationDto));
    }
    return new GetOperationJournalResp().withSuccess(items);
  }
}
