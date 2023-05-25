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
import static com.celements.rteConfig.RTEConfig.*;
import static com.celements.rteConfig.classes.IRTEConfigClassConfig.*;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.test.AbstractComponentTest;
import com.celements.configuration.CelementsAllPropertiesConfigurationSource;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.util.ModelUtils;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneSearchResult;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.search.lucene.query.QueryRestriction;
import com.google.common.collect.ImmutableList;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.web.Utils;

public class RTEConfigTest extends AbstractComponentTest {

  private RTEConfig config;
  private XWikiDocument webPrefDoc;
  private XWikiDocument pageTypeCfgDoc;
  private IModelAccessFacade modelAccessMock;

  @Before
  public void prepare() throws Exception {
    modelAccessMock = registerComponentMock(IModelAccessFacade.class);
    registerComponentMocks(IPageTypeResolverRole.class, ILuceneSearchService.class);
    registerComponentMock(ConfigurationSource.class, CelementsAllPropertiesConfigurationSource.NAME,
        getConfigurationSource());
    config = (RTEConfig) Utils.getComponent(RteConfigRole.class);
    getContext().setDoc(new XWikiDocument(new DocumentReference(getContext().getDatabase(),
        "TestSpace", "TestDoc")));
    webPrefDoc = new XWikiDocument(config.getWebPrefDoc().get());
    expect(getMock(IPageTypeResolverRole.class).resolvePageTypeRefForCurrentDoc())
        .andReturn(new PageTypeReference("RichText", "xobject", ImmutableList.of("pagetype")))
        .anyTimes();
    pageTypeCfgDoc = new XWikiDocument(new DocumentReference(getContext().getDatabase(),
        "PageTypes", "RichText"));
    expect(modelAccessMock.getDocumentOpt(pageTypeCfgDoc.getDocumentReference()))
        .andReturn(Optional.of(pageTypeCfgDoc)).anyTimes();
  }

  @Test
  public void test_getRTEConfigField_page() throws Exception {
    String objValue = "style=test";
    BaseObject obj = new BaseObject();
    obj.setXClassReference(RTE_CFG_TYPE_PROP_CLASS_REF);
    obj.setStringValue("styles", objValue);
    getContext().getDoc().addXObject(obj);
    replayDefault();
    assertEquals(objValue, config.getRTEConfigField("styles"));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_pageType() throws Exception {
    // PageType
    String objValue = "style=pagetypetest";
    BaseObject obj = new BaseObject();
    obj.setXClassReference(RTE_CFG_TYPE_PROP_CLASS_REF);
    obj.setStringValue("styles", objValue);
    pageTypeCfgDoc.addXObject(obj);
    replayDefault();
    assertEquals(objValue, config.getRTEConfigField("styles"));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_webPreference_obj() throws Exception {
    // WebPreferences
    String objValue = "style=webPrefObjTest";
    BaseObject obj = new BaseObject();
    obj.setXClassReference(RTE_CFG_TYPE_PROP_CLASS_REF);
    obj.setStringValue("styles", objValue);
    webPrefDoc.addXObject(obj);
    expect(modelAccessMock.getDocumentOpt(config.getWebPrefDoc().get()))
        .andReturn(Optional.of(webPrefDoc));
    replayDefault();
    assertEquals(objValue, config.getRTEConfigField("styles"));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_webPreferenceObj() throws Exception {
    // WebPreferences
    String objValue = "style=webPref_PrefObjTest";
    BaseObject obj = new BaseObject();
    obj.setXClassReference(XWIKI_PREF_CLASS_REF);
    obj.setStringValue("rte_styles", objValue);
    webPrefDoc.addXObject(obj);
    expect(modelAccessMock.getDocumentOpt(config.getWebPrefDoc().get()))
        .andReturn(Optional.of(webPrefDoc));
    replayDefault();
    assertEquals(objValue, config.getRTEConfigField("styles"));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_xwikiPreference_obj() throws Exception {
    DocumentReference xwikiPrefDocRef = config.getXWikiPrefDoc().get();
    // WebPreferences
    expect(modelAccessMock.getDocumentOpt(config.getWebPrefDoc().get()))
        .andReturn(Optional.of(webPrefDoc));
    // XWikiPreferences
    String objValue = "style=xwikiPrefObjTest";
    BaseObject obj = new BaseObject();
    obj.setXClassReference(RTE_CFG_TYPE_PROP_CLASS_REF);
    obj.setStringValue("styles", objValue);
    XWikiDocument prefDoc = new XWikiDocument(xwikiPrefDocRef);
    prefDoc.addXObject(obj);
    expect(modelAccessMock.getDocumentOpt(xwikiPrefDocRef)).andReturn(Optional.of(prefDoc));
    replayDefault();
    assertEquals(objValue, config.getRTEConfigField("styles"));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_xwikiPreferenceObj() throws Exception {
    DocumentReference xwikiPrefDocRef = config.getXWikiPrefDoc().get();
    // WebPreferences
    expect(modelAccessMock.getDocumentOpt(config.getWebPrefDoc().get()))
        .andReturn(Optional.of(webPrefDoc));
    // XWikiPreferences
    String objValue = "style=xwikiPref_PrefObjTest";
    BaseObject obj = new BaseObject();
    obj.setXClassReference(XWIKI_PREF_CLASS_REF);
    obj.setStringValue("rte_styles", objValue);
    XWikiDocument prefDoc = new XWikiDocument(xwikiPrefDocRef);
    prefDoc.addXObject(obj);
    expect(modelAccessMock.getDocumentOpt(xwikiPrefDocRef)).andReturn(Optional.of(prefDoc));
    replayDefault();
    assertEquals(objValue, config.getRTEConfigField("styles"));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_disk_properties() throws Exception {
    // WebPreferences
    expect(modelAccessMock.getDocumentOpt(config.getWebPrefDoc().get()))
        .andReturn(Optional.empty());
    // XWikiPreferences
    expect(modelAccessMock.getDocumentOpt(config.getXWikiPrefDoc().get()))
        .andReturn(Optional.empty());
    // xwiki.cfg
    getConfigurationSource().setProperty("celements.rteconfig.styles", "testvalue");
    replayDefault();
    assertEquals("testvalue", config.getRTEConfigField("styles"));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_disk_param() throws Exception {
    // WebPreferences
    expect(modelAccessMock.getDocumentOpt(config.getWebPrefDoc().get()))
        .andReturn(Optional.empty());
    // XWikiPreferences
    expect(modelAccessMock.getDocumentOpt(config.getXWikiPrefDoc().get()))
        .andReturn(Optional.empty());
    // xwiki.cfg
    expect(getWikiMock().Param("celements.rteconfig.styles", null)).andReturn("testvalue");
    replayDefault();
    assertEquals("testvalue", config.getRTEConfigField("styles"));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_default() throws Exception {
    // WebPreferences
    expect(modelAccessMock.getDocumentOpt(config.getWebPrefDoc().get()))
        .andReturn(Optional.empty());
    // XWikiPreferences
    expect(modelAccessMock.getDocumentOpt(config.getXWikiPrefDoc().get()))
        .andReturn(Optional.empty());
    // xwiki.cfg
    expect(getWikiMock().Param("celements.rteconfig.blockformats", null)).andReturn(null);
    replayDefault();
    assertEquals(RTEConfig.RTE_CONFIG_FIELD_DEFAULTS.get("blockformats"),
        config.getRTEConfigField("blockformats"));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigField_none() throws Exception {
    // WebPreferences
    expect(modelAccessMock.getDocumentOpt(config.getWebPrefDoc().get()))
        .andReturn(Optional.of(webPrefDoc));
    // XWikiPreferences
    expect(modelAccessMock.getDocumentOpt(config.getXWikiPrefDoc().get()))
        .andReturn(Optional.empty());
    // xwiki.cfg
    expect(getWikiMock().Param("celements.rteconfig.styles", null)).andReturn(null);
    replayDefault();
    assertEquals("", config.getRTEConfigField("styles"));
    verifyDefault();
  }

  @Test
  public void test_getPreferenceFromConfigObject() throws Exception {
    DocumentReference confDocRef = new DocumentReference("otherdb", "RteConfSpace", "confdocname");
    BaseObject obj = new BaseObject();
    obj.setXClassReference(RTE_CFG_TYPE_CLASS_REF);
    obj.setStringValue(RTEConfig.CONFIG_PROP_NAME, getComponentManager().lookup(ModelUtils.class)
        .serializeRef(confDocRef));
    getContext().getDoc().addXObject(obj);
    BaseObject confObj = new BaseObject();
    confObj.setXClassReference(RTE_CFG_TYPE_PROP_CLASS_REF);
    confObj.setStringValue("testprop", "testvalue");
    XWikiDocument confDoc = new XWikiDocument(confDocRef);
    confDoc.addXObject(confObj);
    expect(modelAccessMock.getDocumentOpt(eq(confDocRef))).andReturn(Optional.of(confDoc));
    replayDefault();
    assertEquals("testvalue", config.getFieldFromObj("testprop", getContext().getDoc()).orElse(""));
    verifyDefault();
  }

  @Test
  public void test_getPreferenceFromConfigObject_fallback() throws Exception {
    BaseObject confObj = new BaseObject();
    confObj.setXClassReference(RTE_CFG_TYPE_PROP_CLASS_REF);
    confObj.setStringValue("testprop", "testvalue");
    getContext().getDoc().addXObject(confObj);
    replayDefault();
    assertEquals("testvalue", config.getFieldFromObj("testprop", getContext().getDoc()).orElse(""));
    verifyDefault();
  }

  @Test
  public void test_getRTEConfigsList() throws Exception {
    List<DocumentReference> result = ImmutableList.of(getContext().getDoc().getDocumentReference());
    LuceneQuery query = new LuceneQuery();
    expect(getMock(ILuceneSearchService.class).createQuery()).andReturn(query);
    expect(getMock(ILuceneSearchService.class).createObjectRestriction(RTE_CFG_TYPE_PROP_CLASS_REF))
        .andReturn(new QueryRestriction(" ", " "));
    LuceneSearchResult resultMock = createDefaultMock(LuceneSearchResult.class);
    expect(getMock(ILuceneSearchService.class).searchWithoutChecks(same(query)))
        .andReturn(resultMock);
    expect(resultMock.streamResults(DocumentReference.class)).andReturn(result.stream());
    replayDefault();
    assertEquals(result, config.getRTEConfigsList());
    verifyDefault();
  }

}
