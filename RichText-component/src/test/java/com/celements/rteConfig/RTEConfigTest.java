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
package com.celements.rteConfig;

import static com.celements.common.test.CelementsTestUtils.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.reference.RefBuilder;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.rteConfig.classes.IRTEConfigClassConfig;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class RTEConfigTest extends AbstractComponentTest {

  private static final String RICH_TEXT_PT_NAME = "RichText";
  XWikiContext context;
  RTEConfig config;
  private DocumentReference curDocRef;
  private IModelAccessFacade modelAccessMock;
  private XWikiDocument curDoc;
  private IPageTypeResolverRole pageTypeResvMock;
  private DocumentReference richTextPageTypeDocRef;
  private XWikiDocument richTextPageTypeDoc;

  @Before
  public void setUp_RTEConfigTest() throws Exception {
    pageTypeResvMock = registerComponentMock(IPageTypeResolverRole.class);
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    context = getContext();
    config = (RTEConfig) Utils.getComponent(RteConfigRole.class);
    curDocRef = new RefBuilder().wiki(context.getDatabase()).space("TestSpace").doc(
        "TestDoc").build(DocumentReference.class);
    curDoc = new XWikiDocument(curDocRef);
    PageTypeReference pageTypeRef = new PageTypeReference(RICH_TEXT_PT_NAME, "xobject",
        ImmutableList.of("pagetype"));
    expect(pageTypeResvMock.resolvePageTypeRefForCurrentDoc()).andReturn(pageTypeRef).anyTimes();
    richTextPageTypeDocRef = new RefBuilder().wiki(getContext().getDatabase()).space(
        "PageTypes").doc(RICH_TEXT_PT_NAME).build(DocumentReference.class);
    richTextPageTypeDoc = new XWikiDocument(richTextPageTypeDocRef);
    expect(modelAccessMock.getDocument(richTextPageTypeDocRef)).andReturn(
        richTextPageTypeDoc).anyTimes();
    expect(modelAccessMock.exists(eq(richTextPageTypeDocRef))).andReturn(true).anyTimes();
  }

  @Test
  public void test_getPropClassRef() {
    DocumentReference rtePropClassRef = new RefBuilder().wiki(getContext().getDatabase()).space(
        IRTEConfigClassConfig.RTE_CONFIG_TYPE_PRPOP_CLASS_SPACE).doc(
            IRTEConfigClassConfig.RTE_CONFIG_TYPE_PRPOP_CLASS_DOC).build(DocumentReference.class);
    replayDefault();
    assertEquals(rtePropClassRef, config.getPropClassRef());
    verifyDefault();
  }

  @Test
  public void testGetRTEConfigField_page() throws Exception {
    String objValue = "style=test";
    BaseObject obj = new BaseObject();
    obj.setXClassReference(config.getPropClassRef());
    obj.setStringValue("styles", objValue);
    curDoc.addXObject(obj);
    context.setDoc(curDoc);
    replayDefault();
    assertEquals(objValue, config.getRTEConfigField("styles"));
    verifyDefault();
  }

  @Test
  public void testGetRTEConfigField_pageType() throws Exception {
    // Doc
    context.setDoc(curDoc);
    // PageType
    String objValue = "style=pagetypetest";
    BaseObject obj = new BaseObject();
    obj.setXClassReference(config.getPropClassRef());
    obj.setStringValue("styles", objValue);
    richTextPageTypeDoc.addXObject(obj);
    replayDefault();
    assertEquals(objValue, config.getRTEConfigField("styles"));
    verifyDefault();
  }

  @Test
  public void testGetRTEConfigField_webPreference_noPagetype_obj() throws Exception {
    DocumentReference testPrefDocRef = new RefBuilder().wiki(getContext().getDatabase()).space(
        "TestSpace").doc("WebPreferences").build(DocumentReference.class);
    // Doc
    context.setDoc(curDoc);
    // WebPreferences
    String objValue = "style=webPrefObjTest";
    BaseObject obj = new BaseObject();
    obj.setXClassReference(config.getPropClassRef());
    obj.setStringValue("styles", objValue);
    XWikiDocument prefDoc = new XWikiDocument(testPrefDocRef);
    prefDoc.addXObject(obj);
    expect(modelAccessMock.getDocument(eq(testPrefDocRef))).andReturn(prefDoc).once();
    replayDefault();
    assertEquals(objValue, config.getRTEConfigField("styles"));
    verifyDefault();
  }

  @Test
  public void testGetRTEConfigField_webPreference_hasPagetype_obj() throws Exception {
    DocumentReference testPrefDocRef = new RefBuilder().wiki(getContext().getDatabase()).space(
        "TestSpace").doc("WebPreferences").build(DocumentReference.class);
    // Doc
    context.setDoc(curDoc);
    // WebPreferences
    String objValue = "style=webPrefObjEmptyPageTypeTest";
    BaseObject obj = new BaseObject();
    obj.setXClassReference(config.getPropClassRef());
    obj.setStringValue("styles", objValue);
    XWikiDocument prefDoc = new XWikiDocument(testPrefDocRef);
    prefDoc.addXObject(obj);
    expect(modelAccessMock.getDocument(eq(testPrefDocRef))).andReturn(prefDoc).once();
    replayDefault();
    assertEquals(objValue, config.getRTEConfigField("styles"));
    verifyDefault();
  }

  @Test
  public void testGetRTEConfigField_webPreferenceObj() throws Exception {
    DocumentReference testPrefDocRef = new RefBuilder().wiki(getContext().getDatabase()).space(
        "TestSpace").doc("WebPreferences").build(DocumentReference.class);
    // Doc
    context.setDoc(curDoc);
    // WebPreferences
    String objValue = "style=webPref_PrefObjTest";
    BaseObject obj = new BaseObject();
    obj.setXClassReference(new RefBuilder().space("XWiki").doc("XWikiPreferences").build(
        EntityReference.class));
    obj.setStringValue("rte_styles", objValue);
    XWikiDocument prefDoc = new XWikiDocument(testPrefDocRef);
    prefDoc.addXObject(obj);
    expect(modelAccessMock.getDocument(eq(testPrefDocRef))).andReturn(prefDoc).once();
    replayDefault();
    assertEquals(objValue, config.getRTEConfigField("styles"));
    verifyDefault();
  }

  @Test
  public void testGetRTEConfigField_xwikiPreference_obj() throws Exception {
    DocumentReference testPrefDocRef = new RefBuilder().wiki(getContext().getDatabase()).space(
        "TestSpace").doc("WebPreferences").build(DocumentReference.class);
    DocumentReference xwikiPrefDocRef = new RefBuilder().wiki(getContext().getDatabase()).space(
        "XWiki").doc("XWikiPreferences").build(DocumentReference.class);
    // Doc
    context.setDoc(curDoc);
    // WebPreferences
    XWikiDocument webPrefDoc = new XWikiDocument(testPrefDocRef);
    expect(modelAccessMock.getDocument(eq(testPrefDocRef))).andReturn(webPrefDoc).once();
    // XWikiPreferences
    String objValue = "style=xwikiPrefObjTest";
    BaseObject obj = new BaseObject();
    obj.setXClassReference(config.getPropClassRef());
    obj.setStringValue("styles", objValue);
    XWikiDocument prefDoc = new XWikiDocument(xwikiPrefDocRef);
    prefDoc.addXObject(obj);
    expect(modelAccessMock.getDocument(eq(xwikiPrefDocRef))).andReturn(prefDoc).once();
    replayDefault();
    assertEquals(objValue, config.getRTEConfigField("styles"));
    verifyDefault();
  }

  @Test
  public void testGetRTEConfigField_xwikiPreferenceObj() throws Exception {
    DocumentReference testPrefDocRef = new RefBuilder().wiki(getContext().getDatabase()).space(
        "TestSpace").doc("WebPreferences").build(DocumentReference.class);
    DocumentReference xwikiPrefDocRef = new RefBuilder().wiki(getContext().getDatabase()).space(
        "XWiki").doc("XWikiPreferences").build(DocumentReference.class);
    // Doc
    context.setDoc(curDoc);
    // WebPreferences
    XWikiDocument webPrefDoc = new XWikiDocument(testPrefDocRef);
    expect(modelAccessMock.getDocument(eq(testPrefDocRef))).andReturn(webPrefDoc).once();
    // XWikiPreferences
    String objValue = "style=xwikiPref_PrefObjTest";
    BaseObject obj = new BaseObject();
    obj.setXClassReference(new RefBuilder().space("XWiki").doc("XWikiPreferences").build(
        EntityReference.class));
    obj.setStringValue("rte_styles", objValue);
    XWikiDocument prefDoc = new XWikiDocument(xwikiPrefDocRef);
    prefDoc.addXObject(obj);
    expect(modelAccessMock.getDocument(eq(xwikiPrefDocRef))).andReturn(prefDoc).once();
    replayDefault();
    assertEquals(objValue, config.getRTEConfigField("styles"));
    verifyDefault();
  }

  @Test
  public void testGetRTEConfigField_null() throws Exception {
    DocumentReference testPrefDocRef = new RefBuilder().wiki(getContext().getDatabase()).space(
        "TestSpace").doc("WebPreferences").build(DocumentReference.class);
    DocumentReference xwikiPrefDocRef = new RefBuilder().wiki(getContext().getDatabase()).space(
        "XWiki").doc("XWikiPreferences").build(DocumentReference.class);
    // Doc
    context.setDoc(curDoc);
    // WebPreferences
    XWikiDocument webPrefDoc = new XWikiDocument(testPrefDocRef);
    expect(modelAccessMock.getDocument(eq(testPrefDocRef))).andReturn(webPrefDoc).once();
    // XWikiPreferences
    XWikiDocument prefDoc = new XWikiDocument(xwikiPrefDocRef);
    expect(modelAccessMock.getDocument(eq(xwikiPrefDocRef))).andReturn(prefDoc).once();
    // xwiki.cfg
    expect(getWikiMock().Param("celements.rteconfig.styles", null)).andReturn(null);
    replayDefault();
    assertEquals("", config.getRTEConfigField("styles"));
    verifyDefault();
  }

  @Test
  public void testGetPreferenceFromConfigObject() throws Exception {
    String confDocName = "confdocname";
    DocumentReference confDocRef = new RefBuilder().wiki(getContext().getDatabase()).space(
        "RteConfSpace").doc(confDocName).build(DocumentReference.class);
    BaseObject obj = new BaseObject();
    obj.setXClassReference(new RefBuilder().space(RTEConfig.RTE_CONFIG_TYPE_CLASS_SPACE).doc(
        RTEConfig.RTE_CONFIG_TYPE_CLASS_NAME).build(EntityReference.class));
    obj.setStringValue(RTEConfig.CONFIG_PROP_NAME, "RteConfSpace." + confDocName);
    curDoc.addXObject(obj);

    BaseObject confObj = new BaseObject();
    confObj.setXClassReference(config.getPropClassRef());
    confObj.setStringValue("testprop", "testvalue");
    XWikiDocument confDoc = new XWikiDocument(confDocRef);
    confDoc.addXObject(confObj);
    expect(modelAccessMock.getDocument(eq(confDocRef))).andReturn(confDoc).once();
    replayDefault();
    assertEquals("testvalue", config.getPreferenceFromConfigObject("testprop", curDoc));
    verifyDefault();
  }

  @Test
  public void testGetPreferenceFromPreferenceObject_noObj() {
    DocumentReference classDocRef = new RefBuilder().wiki(getContext().getDatabase()).space(
        "Classes").doc("ClassName").build(DocumentReference.class);
    assertEquals("", config.getPreferenceFromPreferenceObject("testprop", classDocRef, curDoc));
  }

  @Test
  public void testGetPreferenceFromPreferenceObject() {
    DocumentReference testPrefDocRef = new RefBuilder().wiki(getContext().getDatabase()).space(
        "Test").doc("WebPreferences").build(DocumentReference.class);
    DocumentReference classDocRef = new RefBuilder().wiki(getContext().getDatabase()).space(
        "Classes").doc("ClassName").build(DocumentReference.class);
    BaseObject obj = new BaseObject();
    obj.setXClassReference(classDocRef);
    obj.setStringValue("testprop", "testvalue");
    XWikiDocument prefDoc = new XWikiDocument(testPrefDocRef);
    prefDoc.addXObject(obj);
    assertEquals("testvalue", config.getPreferenceFromPreferenceObject("testprop", classDocRef,
        prefDoc));
  }

  @Test
  public void test_getRteConfigsXWQL() {
    assertEquals("from doc.object(Classes.RTEConfigTypePropertiesClass) as rteConfig"
        + " where doc.translation = 0", config.getRteConfigsXWQL());
  }

}
