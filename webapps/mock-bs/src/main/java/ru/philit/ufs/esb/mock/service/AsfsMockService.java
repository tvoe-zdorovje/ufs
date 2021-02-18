package ru.philit.ufs.esb.mock.service;

import java.lang.reflect.Field;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.philit.ufs.esb.MessageProcessor;
import ru.philit.ufs.esb.mock.client.EsbClient;
import ru.philit.ufs.model.cache.MockCache;
import ru.philit.ufs.model.converter.esb.JaxbConverter;
import ru.philit.ufs.model.entity.esb.asfs.HeaderInfoType;
import ru.philit.ufs.model.entity.esb.asfs.OpStatusType;
import ru.philit.ufs.model.entity.esb.asfs.SrvCommitOperationRq;
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
import ru.philit.ufs.model.entity.esb.asfs.SrvRollbackOperationRs;
import ru.philit.ufs.model.entity.esb.asfs.SrvRollbackOperationRs.SrvRollbackOperationRsMessage;
import ru.philit.ufs.model.entity.esb.asfs.SrvUpdOperationRq;
import ru.philit.ufs.model.entity.esb.asfs.SrvUpdOperationRq.SrvUpdOperationRqMessage;
import ru.philit.ufs.model.entity.esb.asfs.SrvUpdOperationRs;
import ru.philit.ufs.model.entity.esb.asfs.SrvUpdOperationRs.SrvUpdOperationRsMessage;

/**
 * Сервис на обработку запросов к АС ФС.
 */
@Service
public class AsfsMockService extends CommonMockService implements MessageProcessor {

  private static final String CONTEXT_PATH = "ru.philit.ufs.model.entity.esb.asfs";

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final EsbClient esbClient;
  private final MockCache mockCache;

  private final JaxbConverter jaxbConverter = new JaxbConverter(CONTEXT_PATH);

  @Autowired
  public AsfsMockService(EsbClient esbClient, MockCache mockCache) {
    this.esbClient = esbClient;
    this.mockCache = mockCache;
  }

  @PostConstruct
  public void init() {
    esbClient.addMessageProcessor(this);
    logger.info("{} started", this.getClass().getSimpleName());
  }

  @Override
  public boolean processMessage(String message) {
    try {
      Object request = jaxbConverter.getObject(message);
      logger.debug("Received message: {}", request);
      if (request != null) {
        if (request instanceof SrvCommitOperationRq) {
          sendResponse(getResponse((SrvCommitOperationRq) request));

        } else if (request instanceof SrvCreateOperationRq) {
          sendResponse(getResponse((SrvCreateOperationRq) request));

        } else if (request instanceof SrvRollbackOperationRq) {
          sendResponse(getResponse((SrvRollbackOperationRq) request));

        } else if (request instanceof SrvUpdOperationRq) {
          sendResponse(getResponse((SrvUpdOperationRq) request));

        } else if (request instanceof SrvGetOperationRq) {
          sendResponse(getResponse((SrvGetOperationRq) request));

        }
        return true;
      }
    } catch (JAXBException e) {
      logger.debug("this message can not be processed this processor", e);
    }
    return false;
  }

  private void sendResponse(Object responseObject) throws JAXBException {
    String responseMessage = jaxbConverter.getXml(responseObject);
    esbClient.sendMessage(responseMessage);
  }

  private SrvCommitOperationRs getResponse(SrvCommitOperationRq request) {
    final SrvCommitOperationRs response = new SrvCommitOperationRs();
    response.setHeaderInfo(copyHeaderInfo(request.getHeaderInfo()));
    final SrvCommitOperationRsMessage message = new SrvCommitOperationRsMessage();
    response.setSrvCommitOperationRsMessage(message);
    final OperationItem operationItem = mockCache
        .getOperation(request.getSrvCommitOperationRqMessage().getOperationId());
    if (operationItem == null) {
      message.setResponseCode("err");
      logger.warn("Operation [id={}] not found!",
          request.getSrvCommitOperationRqMessage().getOperationId());
      return response;
    }
    if (OpStatusType.NEW.equals(operationItem.getOperationStatus())) {
      operationItem.setOperationStatus(OpStatusType.COMMITTED);
      operationItem.setCommittedDttm(xmlCalendar(new Date()));
      mockCache.saveOperation(operationItem);
    }
    message.setOperationId(operationItem.getOperationId());
    if (OpStatusType.COMMITTED.equals(operationItem.getOperationStatus())) {
      message.setOperationStatus(OpStatusType.COMMITTED);
      message.setCommittedDttm(operationItem.getCommittedDttm());
      message.setResponseCode("ok");
    } else {
      message.setOperationStatus(operationItem.getOperationStatus());
      message.setResponseCode("err");
      logger.warn("Unable to commit operation [id={}]: status is {}",
          operationItem.getOperationId(),
          operationItem.getOperationStatus().value());
    }
    return response;
  }

  private SrvCreateOperationRs getResponse(SrvCreateOperationRq request) {
    final SrvCreateOperationRs response = new SrvCreateOperationRs();
    response.setHeaderInfo(copyHeaderInfo(request.getHeaderInfo()));
    final SrvCreateOperationRsMessage rsMessage = new SrvCreateOperationRsMessage();
    response.setSrvCreateOperationRsMessage(rsMessage);
    final SrvCreateOperationRqMessage rqMsg = request.getSrvCreateOperationRqMessage();
    if (rqMsg == null) {
      rsMessage.setResponseCode("err");
      logger.warn("Request message does not exist!");
      return response;
    }

    if (rqMsg.getOperationType() == null || rqMsg.getOperatorId() == null) {
      rsMessage.setResponseCode("err");
      logger.warn("Request message is invalid: {}", rqMsg);
      return response;
    }

    OperationItem item = new OperationItem();
    item.setOperatorId(rqMsg.getOperatorId());
    item.setWorkPlaceUId(rqMsg.getWorkPlaceUId());
    item.setOperationType(rqMsg.getOperationType());
    item.setCreatedDttm(xmlCalendar(new Date()));
    item.setOperationId(String.valueOf((Math.random() * 1000000)));
    item.setOperationStatus(OpStatusType.ADVANCE_RESERVATION);
    mockCache.saveOperation(item);

    rsMessage.setCreatedDttm(item.getCreatedDttm());
    rsMessage.setOperationId(item.getOperationId());
    rsMessage.setOperationStatus(OpStatusType.ADVANCE_RESERVATION);
    rsMessage.setResponseCode("ok");
    return response;
  }

  private SrvRollbackOperationRs getResponse(SrvRollbackOperationRq request) {
    final SrvRollbackOperationRs response = new SrvRollbackOperationRs();
    response.setHeaderInfo(copyHeaderInfo(request.getHeaderInfo()));
    final SrvRollbackOperationRsMessage message = new SrvRollbackOperationRsMessage();
    response.setSrvRollbackOperationRsMessage(message);

    OperationItem operationItem = mockCache
        .getOperation(request.getSrvRollbackOperationRqMessage().getOperationId());
    if (operationItem == null) {
      message.setResponseCode("err");
      logger.warn("Operation [id={}] not found!",
          request.getSrvRollbackOperationRqMessage().getOperationId());
      return response;
    }
    if (OpStatusType.NEW.equals(operationItem.getOperationStatus())
        || OpStatusType.ADVANCE_RESERVATION.equals(operationItem.getOperationStatus())) {
      operationItem.setOperationStatus(OpStatusType.CANCELLED);
      operationItem
          .setRollbackReason(request.getSrvRollbackOperationRqMessage().getRollbackReason());
      mockCache.saveOperation(operationItem);
    }
    if (OpStatusType.CANCELLED.equals(operationItem.getOperationStatus())) {
      message.setOperationStatus(operationItem.getOperationStatus());
      message.setOperationId(operationItem.getOperationId());
      message.setResponseCode("ok");
    } else {
      message.setResponseCode("err");
      message.setOperationId(operationItem.getOperationId());
      message.setOperationStatus(operationItem.getOperationStatus());
      logger.warn("Unable to rollback operation [id={}]: status is {}",
          operationItem.getOperationId(),
          operationItem.getOperationStatus().value());
    }
    return response;
  }

  private SrvUpdOperationRs getResponse(SrvUpdOperationRq request) {
    final SrvUpdOperationRs response = new SrvUpdOperationRs();
    response.setHeaderInfo(copyHeaderInfo(request.getHeaderInfo()));
    final SrvUpdOperationRsMessage message = new SrvUpdOperationRsMessage();
    response.setSrvUpdOperationRsMessage(message);

    OperationItem operationItem =
        mockCache.getOperation(request.getSrvUpdOperationRqMessage().getOperationId());
    if (operationItem == null) {
      message.setResponseCode("err");
      logger.warn("Operation [id={}] not found!",
          request.getSrvUpdOperationRqMessage().getOperationId());
      return response;

    } else if (OpStatusType.ADVANCE_RESERVATION.equals(operationItem.getOperationStatus())
        || OpStatusType.NEW.equals(operationItem.getOperationStatus())) {

      // FIXME replace on manual mapping
      boolean hasProblem = false;
      for (Field rqField : SrvUpdOperationRqMessage.class.getDeclaredFields()) {
        try {
          rqField.setAccessible(true);
          final Field opField = OperationItem.class.getDeclaredField(rqField.getName());
          opField.setAccessible(true);
          final Object value = rqField.get(request.getSrvUpdOperationRqMessage());
          if (value != null) {
            opField.set(operationItem, value);
          }
        } catch (NoSuchFieldException | IllegalAccessException e) {
          logger.warn("Field mapping error: ", e);
          hasProblem = true;
        }
      }
      if (hasProblem) {
        message.setResponseCode("err");
      } else {
        message.setResponseCode("ok");
      }
      mockCache.saveOperation(operationItem);
    } else {
      message.setResponseCode("err");
      logger
          .warn("Unable to update operation [id={}]: status is {}", operationItem.getOperationId(),
              operationItem.getOperationStatus().value());
    }
    message.setOperationId(operationItem.getOperationId());
    message.setOperationStatus(operationItem.getOperationStatus());
    return response;
  }

  private SrvGetOperationRs getResponse(SrvGetOperationRq request) {
    final SrvGetOperationRs response = new SrvGetOperationRs();
    response.setHeaderInfo(copyHeaderInfo(request.getHeaderInfo()));
    final SrvGetOperationRsMessage message = new SrvGetOperationRsMessage();
    response.setSrvGetOperationRsMessage(message);
    if (request.getSrvGetOperationRqMessage().getOperationId() != null) {
      message.getOperationItem()
          .add(mockCache.getOperation(request.getSrvGetOperationRqMessage().getOperationId()));
    } else {
      message.getOperationItem()
          .addAll(mockCache.getOperations(request.getSrvGetOperationRqMessage()
              .getCreatedFrom(), request.getSrvGetOperationRqMessage().getCreatedTo()));
    }
    return response;
  }

  private HeaderInfoType copyHeaderInfo(HeaderInfoType headerInfo0) {
    HeaderInfoType headerInfo = new HeaderInfoType();
    headerInfo.setRqUID(headerInfo0.getRqUID());
    headerInfo.setRqTm(headerInfo0.getRqTm());
    headerInfo.setSpName(headerInfo0.getSystemId());
    headerInfo.setSystemId(headerInfo0.getSpName());
    return headerInfo;
  }
}
