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

import static com.celements.common.test.CelementsTestUtils.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;

import com.celements.common.test.AbstractComponentTest;
import com.xpn.xwiki.web.Utils;

public class RTEConfigClassConfigTest extends AbstractComponentTest {

  private RTEConfigClassConfig rteConfigClassCfg;

  @Before
  public void setUp_RTEConfigClassConfigTest() throws Exception {
    rteConfigClassCfg = (RTEConfigClassConfig) Utils.getComponent(IRTEConfigClassConfig.class);
  }

  @Test
  public void testGetRTEConfigTypePropertiesClassRefEntityReference() {
    EntityReference inRef = new EntityReference("xwikiDb", EntityType.WIKI);
    replayDefault();
    DocumentReference classRef = rteConfigClassCfg.getRTEConfigTypePropertiesClassRef(inRef);
    assertEquals("xwikiDb", classRef.extractReference(EntityType.WIKI).getName());
    assertEquals(IRTEConfigClassConfig.RTE_CONFIG_TYPE_PRPOP_CLASS_SPACE, classRef.extractReference(
        EntityType.SPACE).getName());
    assertEquals(IRTEConfigClassConfig.RTE_CONFIG_TYPE_PRPOP_CLASS_DOC, classRef.getName());
    verifyDefault();
  }

  @Test
  public void testGetRTEConfigTypePropertiesClassRefWikiReference() {
    WikiReference inRef = new WikiReference("xwikiDb");
    replayDefault();
    DocumentReference classRef = rteConfigClassCfg.getRTEConfigTypePropertiesClassRef(inRef);
    assertEquals("xwikiDb", classRef.extractReference(EntityType.WIKI).getName());
    assertEquals(IRTEConfigClassConfig.RTE_CONFIG_TYPE_PRPOP_CLASS_SPACE, classRef.extractReference(
        EntityType.SPACE).getName());
    assertEquals(IRTEConfigClassConfig.RTE_CONFIG_TYPE_PRPOP_CLASS_DOC, classRef.getName());
    verifyDefault();
  }

}
