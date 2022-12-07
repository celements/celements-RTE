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

import java.util.List;

import org.xwiki.component.annotation.Component;

import com.celements.rte.RteImplementation;
import com.google.common.collect.ImmutableList;

@Component(RteTinyMce4.NAME)
public class RteTinyMce4 implements RteImplementation {

  public static final String NAME = "tinymce4";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<String> getJavaScriptFiles() {
    return ImmutableList.of(
        ":celRTE/4.9.11/tinymce.min.js",
        ":celRTE/4.9.11/plugins/compat3x/plugin.min.js",
        ":structEditJS/tinyMCE4/loadTinyMCE-async.js");
  }

}
