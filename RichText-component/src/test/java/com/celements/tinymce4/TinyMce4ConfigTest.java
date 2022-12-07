/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.tinymce4;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.common.test.TestMessageTool;
import com.celements.model.reference.RefBuilder;
import com.celements.rteConfig.RteConfigRole;
import com.celements.sajson.JsonBuilder;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.web.Utils;

public class TinyMce4ConfigTest extends AbstractComponentTest {

  private RteConfigRole rteConfigMock;
  private TinyMce4Config tinyMce4Config;
  private IWebUtilsService wUServiceMock;
  private XWiki wiki;

  @Before
  public void setUp_TinyMce4ConfigTest() throws Exception {
    wiki = getWikiMock();
    wUServiceMock = registerComponentMock(IWebUtilsService.class);
    rteConfigMock = registerComponentMock(RteConfigRole.class);
    expect(wiki.getDefaultLanguage(same(getContext()))).andReturn("de").anyTimes();
    expect(wiki.getXWikiPreference(eq("documentBundles"), same(getContext()))).andReturn("")
        .anyTimes();
    expect(wiki.Param(eq("xwiki.documentBundles"))).andReturn("").anyTimes();
    tinyMce4Config = (TinyMce4Config) Utils.getComponent(RteConfigRole.class, TinyMce4Config.HINT);
    expect(wUServiceMock.getAdminMessageTool()).andReturn(getContext().getMessageTool()).anyTimes();
    ((TestMessageTool) getContext().getMessageTool()).injectMessage("test1key",
        "Test 1 Style");
  }

  @Test
  public void test_getRTEConfigsList() {
    final DocumentReference testRteConfDocRef1 = new RefBuilder().wiki(
        getContext().getDatabase()).space("RteConfigs").doc("TestConfig1").build(
            DocumentReference.class);
    final DocumentReference testRteConfDocRef2 = new RefBuilder().with(testRteConfDocRef1).doc(
        "TestConfig2").build(DocumentReference.class);
    final List<DocumentReference> expectedConfigDocList = ImmutableList.of(testRteConfDocRef1,
        testRteConfDocRef2);
    expect(rteConfigMock.getRTEConfigsList()).andReturn(expectedConfigDocList);
    replayDefault();
    assertEquals(expectedConfigDocList, tinyMce4Config.getRTEConfigsList());
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_anyField() {
    final String testPropName = "testProperty";
    final String expectedResult = "the|Expected|Result";
    expect(rteConfigMock.getRTEConfigField(eq(testPropName))).andReturn(expectedResult);
    replayDefault();
    assertEquals(expectedResult, tinyMce4Config.getRTEConfigField(testPropName));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_row_1() {
    final String testPropName = "row_1";
    expect(rteConfigMock.getRTEConfigField(eq(testPropName))).andReturn("the,Expected,Result");
    replayDefault();
    assertEquals("the Expected Result", tinyMce4Config.getRTEConfigField(testPropName));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_row_2() {
    final String testPropName = "row_2";
    expect(rteConfigMock.getRTEConfigField(eq(testPropName))).andReturn("the,Expected,Result");
    replayDefault();
    assertEquals("the Expected Result", tinyMce4Config.getRTEConfigField(testPropName));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_valid_elements() {
    final String testPropName = "valid_elements";
    expect(rteConfigMock.getRTEConfigField(eq(testPropName))).andReturn("#p,a[!href],br");
    replayDefault();
    assertEquals("#p,a[!href],br", tinyMce4Config.getRTEConfigField(testPropName));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_valid_elements_default() {
    final String testPropName = "valid_elements";
    expect(rteConfigMock.getRTEConfigField(eq(testPropName))).andReturn("");
    replayDefault();
    assertEquals(TinyMce4Config.VALID_ELEMENTS_DEF, tinyMce4Config.getRTEConfigField(testPropName));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_invalid_elements() {
    final String testPropName = "invalid_elements";
    expect(rteConfigMock.getRTEConfigField(eq(testPropName))).andReturn("#p,a[!href],br");
    replayDefault();
    assertEquals("#p,a[!href],br", tinyMce4Config.getRTEConfigField(testPropName));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_invalid_elements_default() {
    final String testPropName = "invalid_elements";
    expect(rteConfigMock.getRTEConfigField(eq(testPropName))).andReturn("");
    replayDefault();
    assertEquals(TinyMce4Config.INVALID_ELEMENTS_DEF, tinyMce4Config.getRTEConfigField(
        testPropName));
    verifyDefault();
  }

  @Test
  public void test_rowLayoutConvert_trailingSeparator() {
    replayDefault();
    assertEquals("list | bold italic celimage cellink", tinyMce4Config.rowLayoutConvert(
        "|, list,|,bold,italic,image,link,separator"));
    verifyDefault();
  }

  @Test
  public void test_rowLayoutConvert_doubleSeparator() {
    replayDefault();
    assertEquals("list | bold italic celimage cellink", tinyMce4Config.rowLayoutConvert(
        "list,separator,|,bold,italic,image,link"));
    verifyDefault();
  }

  @Test
  public void test_rowLayoutConvert_skip_empty_elements() {
    replayDefault();
    assertEquals("list | bold italic", tinyMce4Config.rowLayoutConvert(
        "list,separator,,|,,bold,italic,"));
    verifyDefault();
  }

  @Test
  public void test_rowLayoutConvert_skip_additional_spaces() {
    replayDefault();
    assertEquals("list | bold italic", tinyMce4Config.rowLayoutConvert(
        "list,separator, | , bold, italic"));
    verifyDefault();
  }

  @Test
  public void test_rowLayoutConvert_replacements() {
    replayDefault();
    assertEquals("celimage cellink celimage cellink celimage cellink",
        tinyMce4Config.rowLayoutConvert("image,link,celimage,cellink,advimage,advlink"));
    verifyDefault();
  }

  @Test
  public void test_rowLayoutConvert_tablecontrols() {
    replayDefault();
    assertEquals(
        "italic table | tablerowprops tablecellprops | tableinsertrowbefore tableinsertrowafter"
            + " tabledeleterow | tableinsertcolbefore tableinsertcolafter tabledeletecol | "
            + "tablesplitcells tablemergecells bold",
        tinyMce4Config.rowLayoutConvert(
            "italic, tablecontrols, bold"));
    verifyDefault();
  }

  @Test
  public void test_rowLayoutConvert_pasteword() {
    replayDefault();
    assertEquals("italic pastetext paste bold", tinyMce4Config.rowLayoutConvert(
        "italic, pastetext,pasteword, bold"));
    verifyDefault();
  }

  @Test
  public void test_rowLayoutConvert_general() {
    replayDefault();
    assertEquals("list | bold italic celimage cellink | cellink celimage",
        tinyMce4Config.rowLayoutConvert("cancel, save, separator, list,separator,bold,italic,image,"
            + "link;|;advlink;advimage;|"));
    verifyDefault();
  }

  @Test
  public void test_validElementsCheck_default4empty() {
    replayDefault();
    assertEquals(TinyMce4Config.VALID_ELEMENTS_DEF, tinyMce4Config.validElementsCheck(""));
    verifyDefault();
  }

  @Test
  public void test_validElementsCheck_default4null() {
    replayDefault();
    assertEquals(TinyMce4Config.VALID_ELEMENTS_DEF, tinyMce4Config.validElementsCheck(null));
    verifyDefault();
  }

  @Test
  public void test_validElementsCheck_sanity() {
    replayDefault();
    assertFalse(TinyMce4Config.VALID_ELEMENTS_DEF.matches(" "));
    assertFalse(Strings.isNullOrEmpty(TinyMce4Config.VALID_ELEMENTS_DEF));
    verifyDefault();
  }

  @Test
  public void test_invalidElementsCheck_default4empty() {
    replayDefault();
    assertEquals(TinyMce4Config.INVALID_ELEMENTS_DEF, tinyMce4Config.invalidElementsCheck(""));
    verifyDefault();
  }

  @Test
  public void test_invalidElementsCheck_default4null() {
    replayDefault();
    assertEquals(TinyMce4Config.INVALID_ELEMENTS_DEF, tinyMce4Config.invalidElementsCheck(null));
    verifyDefault();
  }

  @Test
  public void test_invalidElementsCheck_sanity() {
    replayDefault();
    assertFalse(TinyMce4Config.INVALID_ELEMENTS_DEF.matches(" "));
    assertFalse(Strings.isNullOrEmpty(TinyMce4Config.INVALID_ELEMENTS_DEF));
    verifyDefault();
  }

  @Test
  public void test_rowLayoutConvert_overwriteWith_none() {
    replayDefault();
    assertEquals("", tinyMce4Config.rowLayoutConvert("none"));
    verifyDefault();
  }

  @Test
  public void test_convertTiny3Style() {
    replayDefault();
    assertEquals(
        "{\"title\" : \"Test 1 Style\", \"inline\" : \"span\", \"classes\" : \"test1css\"}",
        convert2String(tinyMce4Config.convertTiny3Style("test1key=test1css")));
    verifyDefault();
  }

  @Test
  public void test_convertTiny3Style_incomplete() {
    replayDefault();
    assertEquals("", convert2String(tinyMce4Config.convertTiny3Style("test1key")));
    verifyDefault();
  }

  @Test
  public void test_stylesCheck() {
    replayDefault();
    assertEquals(
        "{\"title\" : \"Test 1 Style\", \"inline\" : \"span\", \"classes\" : \"test1css\"},"
            + "{\"title\" : \"test2key\", \"inline\" : \"span\", \"classes\" : \"test2css\"}",
        convert2String(tinyMce4Config.stylesCheck("test1key=test1css;test2key=test2css")));
    verifyDefault();
  }

  @Test
  public void test_stylesCheck_broken() {
    replayDefault();
    assertEquals(
        "{\"title\" : \"dictkey2\", \"inline\" : \"span\", \"classes\" : \"cssClass2\"},"
            + "{\"title\" : \"dictkey1\", \"inline\" : \"span\", \"classes\" : \"cssClass1\"},"
            + "{\"title\" : \"dictkey3\", \"inline\" : \"span\", \"classes\" : \"cssClass3\"},"
            + "{\"title\" : \"dictkey4\", \"inline\" : \"span\", \"classes\" : \"cssClass4\"}",
        convert2String(tinyMce4Config.stylesCheck("dictkey1;dictkey2=cssClass2\n"
            + "dictkey1=cssClass1;;dictkey3=cssClass3;\n"
            + "dictkey4=cssClass4")));
    verifyDefault();
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
        tinyMce4Config.getRteJsonConfigField("style_formats").getJSON());
    verifyDefault();
  }

  private String convert2String(Stream<JsonBuilder> tiny3RuleStream) {
    String tiny3Rule = tiny3RuleStream.map(JsonBuilder::getJSON)
        .collect(Collectors.joining(","));
    return tiny3Rule;
  }

}
