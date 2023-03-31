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

import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.ClassReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;

@ComponentRole
public interface IRTEConfigClassConfig {

  String RTE_CONFIG_CLASS_SPACE = "Classes";
  String RTE_CONFIG_TYPE_CLASS_NAME = "RTEConfigTypeClass";
  ClassReference RTE_CFG_TYPE_CLASS_REF = new ClassReference(
      RTE_CONFIG_CLASS_SPACE, RTE_CONFIG_TYPE_CLASS_NAME);

  String RTE_CONFIG_TYPE_PRPOP_CLASS_DOC = "RTEConfigTypePropertiesClass";
  String RTE_CONFIG_TYPE_PRPOP_CLASS_SPACE = RTE_CONFIG_CLASS_SPACE;
  ClassReference RTE_CFG_TYPE_PROP_CLASS_REF = new ClassReference(
      RTE_CONFIG_CLASS_SPACE, RTE_CONFIG_TYPE_PRPOP_CLASS_DOC);

  /**
   * @deprecated since 5.10 instead use {@link #RTE_CFG_TYPE_PROP_CLASS_REF}
   */
  @Deprecated
  DocumentReference getRTEConfigTypePropertiesClassRef(EntityReference inRef);

  /**
   * @deprecated since 5.10 instead use {@link #RTE_CFG_TYPE_PROP_CLASS_REF}
   */
  @Deprecated
  DocumentReference getRTEConfigTypePropertiesClassRef(WikiReference wikiRef);

}
