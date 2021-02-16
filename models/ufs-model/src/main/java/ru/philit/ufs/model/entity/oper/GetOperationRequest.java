package ru.philit.ufs.model.entity.oper;

import java.io.Serializable;
import java.util.Date;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Getter
@Setter
public class GetOperationRequest implements Serializable {

  private String id;
  private Date createdFrom;
  private Date createdTo;
}
