package com.celements.rteConfig;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.common.test.AbstractComponentTest;
import com.celements.emptycheck.internal.IDefaultEmptyDocStrategyRole;
import com.celements.model.reference.RefBuilder;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.web.Utils;

public class RTEConfigScriptServiceTest extends AbstractComponentTest {

  private IDefaultEmptyDocStrategyRole emptyStrategyMock;
  private RTEConfigScriptService rteConfigScriptSrv;
  private RteConfigRole rteConfigSrvMock;

  @Before
  public void setUp_RTEConfigScriptServiceTest() throws Exception {
    emptyStrategyMock = registerComponentMock(IDefaultEmptyDocStrategyRole.class);
    rteConfigSrvMock = registerComponentMock(RteConfigRole.class, "rteConfigMock");
    rteConfigScriptSrv = (RTEConfigScriptService) Utils.getComponent(ScriptService.class,
        "rteconfig");
  }

  @Test
  public void test_setRteConfigHint() {
    final String rteConfigHint1 = "rteConfigMock";
    final String rteConfigHint2 = "default";
    replayDefault();
    rteConfigScriptSrv.setRteConfigHint(rteConfigHint1);
    assertEquals(rteConfigHint1, rteConfigScriptSrv.getRteConfigHint());
    rteConfigScriptSrv.setRteConfigHint(rteConfigHint2);
    assertEquals(rteConfigHint2, rteConfigScriptSrv.getRteConfigHint());
    verifyDefault();
  }

  @Test
  public void test_getRteConfigHint_default() {
    final String rteConfigHint = "default";
    replayDefault();
    assertEquals(rteConfigHint, rteConfigScriptSrv.getRteConfigHint());
    rteConfigScriptSrv.setRteConfigHint(null);
    assertEquals(rteConfigHint, rteConfigScriptSrv.getRteConfigHint());
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField() {
    rteConfigScriptSrv.setRteConfigHint("rteConfigMock");
    final String testPropName = "testProperty";
    final String expectedResult = "the|Expected|Result";
    expect(rteConfigSrvMock.getRTEConfigField(eq(testPropName))).andReturn(expectedResult);
    replayDefault();
    assertEquals(expectedResult, rteConfigScriptSrv.getRTEConfigField(testPropName));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_runtimeException() {
    rteConfigScriptSrv.setRteConfigHint("rteConfigMock");
    final String testPropName = "testProperty";
    expect(rteConfigSrvMock.getRTEConfigField(eq(testPropName))).andThrow(
        new IllegalArgumentException("Runtime Exception must not be thrown to script"));
    replayDefault();
    try {
      final String rteConfigField = rteConfigScriptSrv.getRTEConfigField(testPropName);
      assertNotNull("Must be @NotNull", rteConfigField);
      assertEquals("", rteConfigField);
    } catch (Exception exp) {
      fail(exp.getMessage());
    }
    verifyDefault();
  }

  @Test
  public void test_isEmptyRTEString() {
    final String rteContent = "<p>The non empty Content</p>";
    expect(emptyStrategyMock.isEmptyRTEString(eq(rteContent))).andReturn(false);
    replayDefault();
    assertFalse(rteConfigScriptSrv.isEmptyRTEString(rteContent));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigsList() {
    rteConfigScriptSrv.setRteConfigHint("rteConfigMock");
    final DocumentReference testRteConfDocRef1 = new RefBuilder().wiki(
        getContext().getDatabase()).space("RteConfigs").doc("TestConfig1").build(
            DocumentReference.class);
    final DocumentReference testRteConfDocRef2 = new RefBuilder().with(testRteConfDocRef1).doc(
        "TestConfig2").build(DocumentReference.class);
    final List<DocumentReference> expectedResult = ImmutableList.of(testRteConfDocRef1,
        testRteConfDocRef2);
    expect(rteConfigSrvMock.getRTEConfigsList()).andReturn(expectedResult);
    replayDefault();
    assertEquals(expectedResult, rteConfigScriptSrv.getRTEConfigsList());
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigsList_Exception() {
    rteConfigScriptSrv.setRteConfigHint("rteConfigMock");
    expect(rteConfigSrvMock.getRTEConfigsList()).andThrow(new IllegalArgumentException(
        "Runtime Exception must not be thrown to script"));
    replayDefault();
    try {
      final List<DocumentReference> rteConfigsList = rteConfigScriptSrv.getRTEConfigsList();
      assertNotNull("Must be @NotNull", rteConfigsList);
      assertEquals(Collections.emptyList(), rteConfigsList);
    } catch (Exception exp) {
      fail(exp.getMessage());
    }
    verifyDefault();
  }

  @Test
  public void test_getActiveRteConfig() {
    final String rteConfigHint1 = "rteConfigNotExistMock";
    replayDefault();
    rteConfigScriptSrv.setRteConfigHint(rteConfigHint1);
    try {
      assertNotNull("Must be @NotNull", rteConfigScriptSrv.getActiveRteConfig());
    } catch (Exception exp) {
      fail(exp.getMessage());
    }
    verifyDefault();
  }

}
