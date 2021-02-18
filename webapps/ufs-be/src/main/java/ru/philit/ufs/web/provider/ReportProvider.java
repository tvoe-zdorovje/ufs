package ru.philit.ufs.web.provider;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.philit.ufs.model.cache.AnnouncementCache;
import ru.philit.ufs.model.cache.MockCache;
import ru.philit.ufs.model.cache.OperationCache;
import ru.philit.ufs.model.cache.UserCache;
import ru.philit.ufs.model.entity.account.AccountOperationRequest;
import ru.philit.ufs.model.entity.oper.GetOperationRequest;
import ru.philit.ufs.model.entity.oper.Operation;
import ru.philit.ufs.model.entity.user.ClientInfo;
import ru.philit.ufs.model.entity.user.Operator;
import ru.philit.ufs.model.entity.user.User;
import ru.philit.ufs.model.entity.user.Workplace;

/**
 * Бизнес-логика работы с данными для отчётов.
 */
@Service
public class ReportProvider {

  private final OperationCache operationCache;
  private final AnnouncementCache announcementCache;
  private final UserCache userCache;
  private final MockCache mockCache;

  /**
   * Конструктор бина.
   */
  @Autowired
  public ReportProvider(OperationCache operationCache, AnnouncementCache announcementCache,
      UserCache userCache, MockCache mockCache) {
    this.operationCache = operationCache;
    this.announcementCache = announcementCache;
    this.userCache = userCache;
    this.mockCache = mockCache;
  }

  /**
   * Получение списка операций для отчёта.
   *
   * @param createdFrom искать операции от даты
   * @param createdTo   искать операции по дату
   * @param clientInfo  задача операции
   * @return список операций клиента
   */
  public List<Operation> getOperations(Date createdFrom, Date createdTo, ClientInfo clientInfo) {
    GetOperationRequest request = new GetOperationRequest();
    request.setCreatedFrom(createdFrom);
    request.setCreatedTo(createdTo);
    return operationCache.getOperations(request, clientInfo);
  }

  /**
   * Получение объекта пользователя.
   *
   * @param sessionId id сессии
   * @return объект пользователя
   */
  public User getUser(String sessionId) {
    return userCache.getUser(sessionId);
  }

  /**
   * Получение объекта оператора.
   *
   * @param userLogin логин пользователя
   * @param clientInfo информация о клиенте
   * @return объект оператора
   */
  public Operator getOperator(String userLogin, ClientInfo clientInfo) {
    return !StringUtils.isEmpty(userLogin) ? userCache.getOperator(userLogin, clientInfo) : null;
  }

  /**
   * Расчёт комиссии по операции.
   *
   * @param accountId номер счёта
   * @param amount сумма операции
   * @param operation операция клиента
   * @param clientInfo информация о клиенте
   * @return значение комиссии
   */
  public BigDecimal getCommission(String accountId, BigDecimal amount,
      Operation operation, ClientInfo clientInfo) {
    return (accountId != null && amount != null && operation != null)
        ? announcementCache.getCommission(
        new AccountOperationRequest(accountId, amount, operation.getTypeCode()), clientInfo)
        : null;
  }

  /**
   * Получение рабочего места.
   *
   * @param workplaceId id рабочего места
   * @return объект рабочего места
   */
  public Workplace getWorkplace(String workplaceId) {
    return mockCache.getWorkplace(workplaceId);
  }
}
