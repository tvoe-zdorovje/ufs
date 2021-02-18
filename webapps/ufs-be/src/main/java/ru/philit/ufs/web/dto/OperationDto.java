package ru.philit.ufs.web.dto;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Объект операции.
 */
@EqualsAndHashCode(of = {"id"})
@ToString
@Getter
@Setter
@SuppressWarnings("serial")
public class OperationDto implements Serializable {

  /**
   * Идентификатор операции.
   */
  private String id;
  /**
   * Название типа операции.
   */
  private String typeName;
  /**
   * Код типа операции.
   */
  private String typeCode;
  /**
   * Статус.
   */
  private String status;
  /**
   * Сумма операции.
   */
  private String amount;
  /**
   * Валюта операции.
   */
  private String currencyType;
  /**
   * Идентификатор рабочего места.
   */
  private String workplaceId;
  /**
   * Код отделения.
   */
  private String subbranchCode;
  /**
   * Дата создания.
   */
  private String createdDate;
  /**
   * Дата выполнения.
   */
  private String committedDate;
  /**
   * Причина возврата/отказа.
   */
  private String rollbackReason;

}
