package ru.philit.ufs.model.cache;

import java.math.BigDecimal;
import ru.philit.ufs.model.entity.account.Card;
import ru.philit.ufs.model.entity.user.User;
import ru.philit.ufs.model.entity.user.Workplace;

/**
 * Интерфейс доступа к временному кешу данных.
 */
public interface MockCache {

  User getUser(String userLogin, String password);

  Card getCreditCard();

  Workplace getWorkplace(String workplaceId);

  boolean checkOverLimit(BigDecimal amount);

}
