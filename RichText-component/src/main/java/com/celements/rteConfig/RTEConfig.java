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

import static com.celements.rteConfig.classes.IRTEConfigClassConfig.*;
import static com.google.common.base.Preconditions.*;
import static com.google.common.base.Predicates.*;
import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;

import com.celements.common.MoreOptional;
import com.celements.configuration.CelementsAllPropertiesConfigurationSource;
import com.celements.configuration.ConfigSourceUtils;
import com.celements.model.access.IModelAccessFacade;
import com.celements.model.context.ModelContext;
import com.celements.model.object.xwiki.XWikiObjectFetcher;
import com.celements.model.reference.RefBuilder;
import com.celements.model.util.ModelUtils;
import com.celements.pagetype.PageTypeReference;
import com.celements.pagetype.service.IPageTypeResolverRole;
import com.celements.pagetype.xobject.XObjectPageTypeUtilsRole;
import com.celements.sajson.JsonBuilder;
import com.celements.search.lucene.ILuceneSearchService;
import com.celements.search.lucene.LuceneSearchException;
import com.celements.search.lucene.query.LuceneQuery;
import com.celements.web.CelConstant;
import com.celements.web.classcollections.IOldCoreClassConfig;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.xpn.xwiki.doc.XWikiDocument;

@Component
public class RTEConfig implements RteConfigRole {

  private static final Logger LOGGER = LoggerFactory.getLogger(RTEConfig.class);

  public static final String CONFIG_PROP_NAME = "rteconfig";

  static final Map<String, String> RTE_CONFIG_FIELD_DEFAULTS = ImmutableMap
      .<String, String>builder()
      .put("blockformats", "rte_heading1=h1,rte_text=p")
      .build();
  static final ClassReference XWIKI_PREF_CLASS_REF = new ClassReference(
      IOldCoreClassConfig.XWIKI_PREFERENCES_CLASS_SPACE,
      IOldCoreClassConfig.XWIKI_PREFERENCES_CLASS_DOC);

  @Requirement
  private IPageTypeResolverRole pageTypeResolver;

  @Requirement
  private XObjectPageTypeUtilsRole xobjectPageTypeUtils;

  @Requirement
  private ILuceneSearchService searchService;

  @Requirement(CelementsAllPropertiesConfigurationSource.NAME)
  private ConfigurationSource propCfgSrc;

  @Requirement
  private ModelUtils modelUtils;

  @Requirement
  private IModelAccessFacade modelAccess;

  @Requirement
  private ModelContext context;

  @Override
  public String getRTEConfigField(@NotEmpty String name) {
    checkNotNull(Strings.emptyToNull(name));
    return findFirstNonBlank(Stream.of(
        () -> getFieldFromCurrentDoc(name),
        () -> getFieldFromPageType(name),
        () -> getFieldFromPrefDoc(name, getWebPrefDoc()),
        () -> getFieldFromPrefDoc(name, getXWikiPrefDoc()),
        () -> getFieldFromDisk(name),
        () -> getFieldDefault(name)))
            .orElse("");
  }

  private Optional<String> getFieldFromCurrentDoc(String name) {
    return context.getDocument().flatMap(doc -> getFieldFromObj(name, doc));
  }

  private Optional<String> getFieldFromPageType(String name) {
    PageTypeReference pageTypeRef = pageTypeResolver.resolvePageTypeRefForCurrentDoc();
    DocumentReference pageTypeDocRef = xobjectPageTypeUtils.getDocRefForPageType(pageTypeRef);
    return modelAccess.getDocumentOpt(pageTypeDocRef)
        .flatMap(pageTypeDoc -> getFieldFromObj(name, pageTypeDoc));
  }

  Optional<String> getFieldFromPrefDoc(String name, Optional<DocumentReference> docRef) {
    return docRef.flatMap(modelAccess::getDocumentOpt)
        .flatMap(doc -> getFieldFromObj(name, doc)
            .or(() -> getStringValue(doc, XWIKI_PREF_CLASS_REF, "rte_" + name)));
  }

  Optional<String> getFieldFromObj(String name, XWikiDocument doc) {
    return getStringValue(doc, RTE_CFG_TYPE_CLASS_REF, CONFIG_PROP_NAME)
        .flatMap(this::resolve)
        .flatMap(modelAccess::getDocumentOpt)
        .flatMap(configDoc -> getStringValue(configDoc, RTE_CFG_TYPE_PROP_CLASS_REF, name))
        .or(() -> getStringValue(doc, RTE_CFG_TYPE_PROP_CLASS_REF, name));
  }

  Optional<String> getStringValue(XWikiDocument doc, ClassReference classRef, String name) {
    return XWikiObjectFetcher.on(doc).filter(classRef).findFirst()
        .map(prefObj -> prefObj.getStringValue(name))
        .filter(StringUtils::isNotBlank);
  }

  private Optional<String> getFieldFromDisk(String name) {
    String key = "celements.rteconfig." + name;
    return Optional.ofNullable(ConfigSourceUtils
        .getStringProperty(propCfgSrc, key).toJavaUtil()
        .orElseGet(() -> context.getXWikiContext().getWiki().Param(key, null)));
  }

  private Optional<String> getFieldDefault(String name) {
    return Optional.ofNullable(RTE_CONFIG_FIELD_DEFAULTS.get(name));
  }

  @Override
  public List<DocumentReference> getRTEConfigsList() {
    try {
      LuceneQuery query = searchService.createQuery();
      query.setWiki(context.getWikiRef());
      query.add(searchService.createObjectRestriction(RTE_CFG_TYPE_PROP_CLASS_REF));
      return searchService.searchWithoutChecks(query)
          .streamResults(DocumentReference.class)
          .distinct()
          .collect(toList());
    } catch (LuceneSearchException exp) {
      LOGGER.error("Failed to get RTE-Configs list.", exp);
      return new ArrayList<>();
    }
  }

  @Override
  public @NotNull JsonBuilder getRteJsonConfigField(@NotEmpty String name) {
    throw new UnsupportedOperationException();
  }

  Optional<DocumentReference> getWebPrefDoc() {
    return new RefBuilder()
        .with(context.getSpaceRef().orElse(null))
        .doc(CelConstant.WEB_PREF_DOC_NAME)
        .buildOpt(DocumentReference.class);
  }

  Optional<DocumentReference> getXWikiPrefDoc() {
    return new RefBuilder()
        .with(context.getWikiRef())
        .space(CelConstant.XWIKI_SPACE)
        .doc(CelConstant.XWIKI_PREF_DOC_NAME)
        .buildOpt(DocumentReference.class);
  }

  private Optional<DocumentReference> resolve(String fullName) {
    try {
      return Optional.ofNullable(modelUtils.resolveRef(fullName, DocumentReference.class));
    } catch (IllegalArgumentException iae) {
      return Optional.empty();
    }
  }

  private static Optional<String> findFirstNonBlank(Stream<Supplier<Optional<String>>> stream) {
    return stream.map(Supplier::get)
        .flatMap(MoreOptional::stream)
        .map(s -> Strings.nullToEmpty(s).trim())
        .filter(not(String::isEmpty))
        .findFirst();
  }

}
