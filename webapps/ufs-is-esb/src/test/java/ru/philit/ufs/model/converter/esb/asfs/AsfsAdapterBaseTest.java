package ru.philit.ufs.model.converter.esb.asfs;

import org.junit.Assert;
import ru.philit.ufs.model.converter.ConverterBaseTest;
import ru.philit.ufs.model.entity.common.ExternalEntity;
import ru.philit.ufs.model.entity.esb.asfs.HeaderInfoType;

public abstract class AsfsAdapterBaseTest extends ConverterBaseTest {

  /**
   * Возвращает заполненный HeaderInfoType.
   */
  public static HeaderInfoType headerInfo() {
    return AsfsAdapter.headerInfo();
  }

  /**
   * Возвращает заполненный HeaderInfoType.
   *
   * @param rqUid заданный requestId
   */
  public static HeaderInfoType headerInfo(String rqUid) {
    HeaderInfoType headerInfo = AsfsAdapter.headerInfo();
    headerInfo.setRqUID(rqUid);
    return headerInfo;
  }

  protected static void assertHeaderInfo(HeaderInfoType headerInfo) {
    Assert.assertNotNull(headerInfo);
    Assert.assertNotNull(headerInfo.getRqUID());
    Assert.assertNotNull(headerInfo.getRqTm());
    Assert.assertEquals(headerInfo.getSpName(), AsfsAdapter.REQUEST_SYSTEM);
    Assert.assertEquals(headerInfo.getSystemId(), AsfsAdapter.RESPONSE_SYSTEM);
  }

  protected static void assertHeaderInfo(ExternalEntity externalEntity, String rqUid) {
    Assert.assertNotNull(externalEntity.getReceiveDate());
    Assert.assertEquals(rqUid, externalEntity.getRequestUid());
  }
}
