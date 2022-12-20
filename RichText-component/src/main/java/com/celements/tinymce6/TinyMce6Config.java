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
package com.celements.tinymce6;

import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.rteConfig.RteConfigRole;
import com.celements.tinymce4.TinyMce4Config;
import com.celements.web.service.IWebUtilsService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

@Component(TinyMce6Config.HINT)
public class TinyMce6Config extends TinyMce4Config {

  /**
   * CAUTION: do not change the HINT it will be used from the vm-scripts
   */
  public static final String HINT = "tinymce6";

  @Requirement
  private RteConfigRole rteConfig;

  @Requirement
  private IWebUtilsService webUtilsService;

  private static final ImmutableList<String> CELIMAGE_TINY6 = ImmutableList.of("image");
  private static final ImmutableList<String> CELLINK_TINY6 = ImmutableList.of("link");
  private static final Map<String, List<String>> BUTTONS_CONVERSIONMAP_TINY6 = initButtonConversionMap();

  private static final ImmutableMap<String, List<String>> initButtonConversionMap() {
    return ImmutableMap.<String, List<String>>builder()
        .put("celimage", CELIMAGE_TINY6)
        .put("advimage", CELIMAGE_TINY6)
        .put("separator", ImmutableList.of(SEPARATOR))
        .put("advlink", CELLINK_TINY6)
        .put("cellink", CELLINK_TINY6)
        .put("tablecontrols", TABLE_CONTROLS)
        .put("justifyleft", ImmutableList.of("alignleft"))
        .put("justifycenter", ImmutableList.of("aligncenter"))
        .put("justifyright", ImmutableList.of("alignright"))
        .put("justifyfull", ImmutableList.of("alignjustify"))
        .put("pasteword", ImmutableList.of("paste"))
        .build();
  }

  @Override
  protected Map<String, List<String>> getButtonsConversionMap() {
    return BUTTONS_CONVERSIONMAP_TINY6;
  }

}
