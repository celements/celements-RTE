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
package com.celements.rteConfig.classes;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.classes.AbstractClassCollection;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.BaseClass;

@Component("celements.rteConfigClasses")
public class RTEConfigClasses extends AbstractClassCollection {

  /**
   * @Deprecated instead use IRTEConfigClassConfig.RTE_CONFIG_TYPE_PRPOP_CLASS_DOC
   **/
  @Deprecated
  public static final String RTE_CONFIG_TYPE_PRPOP_CLASS_DOC = "RTEConfigTypePropertiesClass";
  /**
   * @Deprecated instead use IRTEConfigClassConfig.RTE_CONFIG_TYPE_PRPOP_CLASS_SPACE
   **/
  @Deprecated
  public static final String RTE_CONFIG_TYPE_PRPOP_CLASS_SPACE = "Classes";
  public static final String RTE_CONFIG_TYPE_PRPOP_CLASS = RTE_CONFIG_TYPE_PRPOP_CLASS_SPACE + "."
      + RTE_CONFIG_TYPE_PRPOP_CLASS_DOC;

  public static final String RTE_CONFIG_TEMPLATE_PRPOP_CLASS_SPACE = "RTEConfigClasses";
  public static final String RTE_CONFIG_TEMPLATE_PRPOP_CLASS_DOC = "RTEConfigTemplate";

  @Requirement
  IRTEConfigClassConfig rteConfigClassCfg;

  @Override
  public String getConfigName() {
    return "rteConfigClasses";
  }

  @Override
  protected void initClasses() throws XWikiException {
    getRTEConfigTemplateClass();
    getRTEConfigTypePropertiesClass();
  }

  public DocumentReference getRTEConfigTemplateClassRef(String wikiName) {
    return new DocumentReference(wikiName, RTE_CONFIG_TEMPLATE_PRPOP_CLASS_SPACE,
        RTE_CONFIG_TEMPLATE_PRPOP_CLASS_DOC);
  }

  private BaseClass getRTEConfigTemplateClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = getRTEConfigTemplateClassRef(getContext().getDatabase());

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + getRTEConfigTemplateClassRef(getContext().getDatabase())
          + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addTextField("templateName", "RichTextEditor Template Name", 30);
    needsUpdate |= bclass.addTextField("templateUrl", "RichTextEditor Template URL", 30);
    needsUpdate |= bclass.addTextField("templateDesc", "RichTextEditor Template" + " Description",
        30);

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

  private BaseClass getRTEConfigTypePropertiesClass() throws XWikiException {
    XWikiDocument doc;
    boolean needsUpdate = false;
    DocumentReference classRef = rteConfigClassCfg.getRTEConfigTypePropertiesClassRef(
        new WikiReference(getContext().getDatabase()));

    try {
      doc = getContext().getWiki().getDocument(classRef, getContext());
    } catch (XWikiException exp) {
      LOGGER.error("Failed to get " + RTE_CONFIG_TYPE_PRPOP_CLASS + " class document. ", exp);
      doc = new XWikiDocument(classRef);
      needsUpdate = true;
    }

    BaseClass bclass = doc.getXClass();
    bclass.setXClassReference(classRef);
    needsUpdate |= bclass.addTextAreaField("styles", "RichTextEditor Styles", 80, 15);
    needsUpdate |= bclass.addTextField("plugins", "RichTextEditor Additional Plugins", 30);
    needsUpdate |= bclass.addTextField("row_1", "RichTextEditor Layout Row 1", 30);
    needsUpdate |= bclass.addTextField("row_2", "RichTextEditor Layout Row 2", 30);
    needsUpdate |= bclass.addTextField("row_3", "RichTextEditor Layout Row 3", 30);
    needsUpdate |= bclass.addTextField("blockformats", "RichTextEditor Block Formats", 30);
    needsUpdate |= bclass.addTextAreaField("valid_elements", "RichTextEditor valid elements config",
        80, 15);
    needsUpdate |= bclass.addTextAreaField("invalid_elements",
        "RichTextEditor invalid elements config", 80, 15);
    needsUpdate |= bclass.addTextField("link_picker_spaces", "Spaces listed in Link Picker", 30);

    if (!"internal".equals(bclass.getCustomMapping())) {
      needsUpdate = true;
      bclass.setCustomMapping("internal");
    }

    setContentAndSaveClassDocument(doc, needsUpdate);
    return bclass;
  }

}
