package com.celements.tinymce6;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.celements.common.test.AbstractComponentTest;
import com.celements.common.test.TestMessageTool;
import com.celements.rteConfig.RteConfigRole;
import com.celements.web.service.IWebUtilsService;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.web.Utils;

public class TinyMce6ConfigTest extends AbstractComponentTest {

  private RteConfigRole rteConfigMock;
  private TinyMce6Config tinyMce6Config;
  private XWiki wiki;
  private IWebUtilsService wUServiceMock;

  @Before
  public void setUp_TinyMce6ConfigTest() throws Exception {
    wiki = getWikiMock();
    wUServiceMock = registerComponentMock(IWebUtilsService.class);
    rteConfigMock = registerComponentMock(RteConfigRole.class);
    expect(wiki.getDefaultLanguage(same(getContext()))).andReturn("de").anyTimes();
    expect(wiki.getXWikiPreference(eq("documentBundles"), same(getContext()))).andReturn("")
        .anyTimes();
    expect(wiki.Param(eq("xwiki.documentBundles"))).andReturn("").anyTimes();
    expect(wUServiceMock.getAdminMessageTool()).andReturn(getContext().getMessageTool()).anyTimes();
    ((TestMessageTool) getContext().getMessageTool()).injectMessage("test1key",
        "Test 1 Style");
    tinyMce6Config = (TinyMce6Config) Utils.getComponent(RteConfigRole.class, TinyMce6Config.HINT);
  }

  @Test
  public void test_getRteJsonConfigField_style_formats() {
    expect(rteConfigMock.getRTEConfigField("style_formats"))
        .andReturn("");
    expect(rteConfigMock.getRTEConfigField("styles"))
        .andReturn("test1key=test1css;test2key=test2css");
    replayDefault();
    assertEquals(
        "[{\"title\" : \"Test 1 Style\", \"inline\" : \"span\", \"classes\" : \"test1css\"},"
            + " {\"title\" : \"test2key\", \"inline\" : \"span\", \"classes\" : \"test2css\"}]",
        tinyMce6Config.getRteJsonConfigField("style_formats").getJSON());
    verifyDefault();
  }

  @Test
  public void test_getRteJsonConfigField_style_formats_empty() {
    expect(rteConfigMock.getRTEConfigField("style_formats"))
        .andReturn("");
    expect(rteConfigMock.getRTEConfigField("styles"))
        .andReturn("");
    replayDefault();
    assertEquals("[]", tinyMce6Config.getRteJsonConfigField("style_formats").getJSON());
    verifyDefault();
  }

}
