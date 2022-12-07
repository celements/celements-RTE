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

import static com.google.common.base.Preconditions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;

import com.celements.model.access.IModelAccessFacade;
import com.celements.model.access.exception.DocumentNotExistsException;
import com.celements.model.context.ModelContext;
import com.celements.model.reference.RefBuilder;
import com.celements.model.util.ModelUtils;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.xobject.XObjectPageTypeUtilsRole;
import com.celements.rteConfig.classes.IRTEConfigClassConfig;
import com.celements.sajson.JsonBuilder;
import com.celements.web.classcollections.IOldCoreClassConfig;
import com.celements.web.service.IWebUtilsService;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

@Component
public class RTEConfig implements RteConfigRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(RTEConfig.class);

  public static final String RTE_CONFIG_TYPE_CLASS_SPACE = "Classes";
  public static final String RTE_CONFIG_TYPE_CLASS_NAME = "RTEConfigTypeClass";
  public static final String CONFIG_CLASS_NAME = RTE_CONFIG_TYPE_CLASS_SPACE + "."
      + RTE_CONFIG_TYPE_CLASS_NAME;
  public static final String CONFIG_PROP_NAME = "rteconfig";

  private static final Map<String, String> RTE_CONFIG_FIELD_DEFAULTS = ImmutableMap
      .<String, String>builder()
      .put("blockformats", "rte_heading1=h1,rte_text=p")
      .build();

  @Requirement
  IRTEConfigClassConfig rteConfigClassConfig;

  @Requirement
  private IPageTypeResolverRole pageTypeResolver;

  @Requirement
  XObjectPageTypeUtilsRole xobjectPageTypeUtils;

  @Requirement
  private QueryManager queryManager;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private IWebUtilsService webUtilsService;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelContext context;

  private XWikiContext getContext() {
    return context.getXWikiContext();
  }

  @Override
  public String getRTEConfigField(@NotEmpty String name) {
    checkNotNull(Strings.emptyToNull(name));
    Optional<XWikiDocument> doc = context.getCurrentDoc().toJavaUtil();
    Optional<SpaceReference> currentSpaceRef = context.getCurrentSpaceRef().toJavaUtil();
    String resultConfig = "";

    // Doc
    if (doc.isPresent()) {
      resultConfig = getPreferenceFromConfigObject(name, doc.get());
      if (Strings.isNullOrEmpty(resultConfig.trim())) {
        resultConfig = getPreferenceFromPreferenceObject(name, getPropClassRef(), doc.get());
      }
    }

    // PageType
    if (Strings.isNullOrEmpty(resultConfig.trim())) {
      resultConfig = getRTEConfigFieldFromPageType(name);
    }

    // WebPreferences
    if (Strings.isNullOrEmpty(resultConfig.trim()) && currentSpaceRef.isPresent()) {
      resultConfig = getRTEConfigFieldFromPreferenceDoc(name, new RefBuilder().with(
          currentSpaceRef.get()).doc("WebPreferences").build(DocumentReference.class));
    }

    // XWikiPreferences
    if (Strings.isNullOrEmpty(resultConfig.trim())) {
      resultConfig = getRTEConfigFieldFromPreferenceDoc(name, new RefBuilder().with(
          context.getWikiRef()).space("XWiki").doc("XWikiPreferences").build(
              DocumentReference.class));
    }

    // xwiki.cfg
    if (Strings.isNullOrEmpty(resultConfig.trim())) {
      resultConfig = getContext().getWiki().Param("celements.rteconfig." + name,
          RTE_CONFIG_FIELD_DEFAULTS.get(name));
    }
    return Strings.nullToEmpty(resultConfig);
  }

  private String getRTEConfigFieldFromPageType(String name) {
    String resultConfig = "";
    PageTypeReference pageTypeRef = pageTypeResolver.resolvePageTypeRefForCurrentDoc();
    DocumentReference pageTypeDocRef = xobjectPageTypeUtils.getDocRefForPageType(pageTypeRef);
    try {
      XWikiDocument pageTypeDoc = modelAccess.getDocument(pageTypeDocRef);
      resultConfig = getPreferenceFromConfigObject(name, pageTypeDoc);
      if ("".equals(resultConfig.trim())) {
        resultConfig = getPreferenceFromPreferenceObject(name, getPropClassRef(), pageTypeDoc);
      }
    } catch (DocumentNotExistsException dneExp) {
      LOGGER.debug("Can't get RTEConfig because PageType doc does not exist. {}", pageTypeDocRef);
    }
    return resultConfig;
  }

  private String getRTEConfigFieldFromPreferenceDoc(String name, DocumentReference docRef) {
    String resultConfig = "";
    try {
      XWikiDocument prefDoc = modelAccess.getDocument(docRef);
      resultConfig = getPreferenceFromConfigObject(name, prefDoc);
      if (Strings.isNullOrEmpty(resultConfig.trim())) {
        resultConfig = getPreferenceFromPreferenceObject(name, getPropClassRef(), prefDoc);
        if (Strings.isNullOrEmpty(resultConfig.trim())) {
          resultConfig = getPreferenceFromPreferenceObject("rte_" + name,
              getXWikiPreferencesClassRef(), prefDoc);
        }
      }
    } catch (DocumentNotExistsException exp) {
      LOGGER.info("Cannot read rte config for '{}'. Preference doc '{}' does not exist.", name,
          docRef);
    }
    return resultConfig;
  }

  String getPreferenceFromConfigObject(String name, XWikiDocument doc) {
    String configDocFN = getPreferenceFromPreferenceObject(CONFIG_PROP_NAME,
        getRteConfigTypeClass(), doc);
    if (!Strings.isNullOrEmpty(configDocFN.trim())) {
      DocumentReference configDocRef = modelUtils.resolveRef(configDocFN, DocumentReference.class);
      try {
        XWikiDocument configDoc = modelAccess.getDocument(configDocRef);
        return getPreferenceFromPreferenceObject(name, getPropClassRef(), configDoc);
      } catch (DocumentNotExistsException e) {
        LOGGER.info("config doc '{}' does not exist.", configDocFN);
      }
    }
    return "";
  }

  String getPreferenceFromPreferenceObject(String name, DocumentReference classRef,
      XWikiDocument doc) {
    BaseObject prefObj = doc.getXObject(classRef);
    if (prefObj != null) {
      return prefObj.getStringValue(name);
    }
    return "";
  }

  @Override
  public List<DocumentReference> getRTEConfigsList() {
    List<DocumentReference> rteConfigsList = new ArrayList<>();
    try {
      List<String> resultList = queryManager.createQuery(getRteConfigsXWQL(), Query.XWQL).execute();
      for (String result : resultList) {
        rteConfigsList.add(modelUtils.resolveRef(result, DocumentReference.class));
      }
    } catch (QueryException exp) {
      LOGGER.error("Failed to get RTE-Configs list.", exp);
    }
    return rteConfigsList;
  }

  String getRteConfigsXWQL() {
    return "from doc.object(" + modelUtils.serializeRefLocal(getPropClassRef()) + ") as rteConfig"
        + " where doc.translation = 0";
  }

  private DocumentReference getRteConfigTypeClass() {
    return new RefBuilder().with(context.getWikiRef()).space(RTE_CONFIG_TYPE_CLASS_SPACE).doc(
        RTE_CONFIG_TYPE_CLASS_NAME).build(DocumentReference.class);
  }

  DocumentReference getPropClassRef() {
    return rteConfigClassConfig.getRTEConfigTypePropertiesClassRef(context.getWikiRef());
  }

  private DocumentReference getXWikiPreferencesClassRef() {
    return new RefBuilder().with(context.getWikiRef()).space(
        IOldCoreClassConfig.XWIKI_PREFERENCES_CLASS_SPACE).doc(
            IOldCoreClassConfig.XWIKI_PREFERENCES_CLASS_DOC)
        .build(DocumentReference.class);
  }

  @Override
  public @NotNull JsonBuilder getRteJsonConfigField(@NotEmpty String name) {
    throw new UnsupportedOperationException();
  }

}
