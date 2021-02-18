package ru.philit.ufs.model.cache;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.xml.datatype.XMLGregorianCalendar;
import ru.philit.ufs.model.entity.esb.asfs.SrvGetOperationRs.SrvGetOperationRsMessage.OperationItem;
import ru.philit.ufs.model.entity.esb.eks.PkgTaskStatusType;
import ru.philit.ufs.model.entity.esb.eks.SrvGetTaskClOperPkgRs.SrvGetTaskClOperPkgRsMessage;
import ru.philit.ufs.model.entity.oper.OperationPackageInfo;

/**
 * Кеш данных Mock приложения.
 */
public interface MockCache {

  Long saveTaskCardDeposit(Long packageId, Long taskId, Object taskBody);

  Long saveTaskCardWithdraw(Long packageId, Long taskId, Object taskBody);

  Long saveTaskAccountDeposit(Long packageId, Long taskId, Object taskBody);

  Long saveTaskAccountWithdraw(Long packageId, Long taskId, Object taskBody);

  Long saveTaskCheckbookIssuing(Long packageId, Long taskId, Object taskBody);

  void saveTaskStatus(Long taskId, PkgTaskStatusType status);

  Long checkPackage(String inn);

  OperationPackageInfo createPackage(String inn, String workplaceId, String userLogin);

  OperationPackageInfo getPackageInfo(Long packageId);

  Map<Long, List<SrvGetTaskClOperPkgRsMessage.PkgItem.ToCardDeposit.TaskItem>>
        searchTasksCardDeposit(Long packageId, PkgTaskStatusType taskStatus, Date fromDate,
        Date toDate, List<Long> taskIds);

  void saveOperation(OperationItem operationItem);

  OperationItem getOperation(String operationId);

  Collection<OperationItem> getOperations(XMLGregorianCalendar createdFrom,
      XMLGregorianCalendar createdTo);
}
