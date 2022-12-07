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

import static com.google.common.base.MoreObjects.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.script.service.ScriptService;

import com.celements.emptycheck.internal.IDefaultEmptyDocStrategyRole;
import com.celements.marshalling.ComponentMarshaller;
import com.celements.marshalling.Marshaller;
import com.celements.rte.RteImplementation;
import com.celements.sajson.JsonBuilder;
import com.google.common.base.Strings;

@Component("rteconfig")
public class RTEConfigScriptService implements ScriptService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RTEConfigScriptService.class);

  private static final String RTE_CONFIG_ACTIV_HINT = "RTE_CONFIG_ACTIV_HINT";

  private final Marshaller<RteImplementation> rteImplMarshaller = new ComponentMarshaller<>(
      RteImplementation.class);

  @Requirement
  private IDefaultEmptyDocStrategyRole defaultEmptyDocStrategyRole;

  @Requirement
  private Map<String, RteConfigRole> rteConfigMap;

  @Requirement
  private RteImplementation defaultRteImpl;

  @Requirement
  private ConfigurationSource cfgSrc;

  @Requirement
  private Execution execution;

  @NotNull
  public String getRTEConfigField(@Nullable String name) {
    if (!Strings.isNullOrEmpty(name)) {
      try {
        return getActiveRteConfig().getRTEConfigField(name);
      } catch (Exception exp) {
        LOGGER.error("getRTEConfigField for name [" + name + "] failed.", exp);
      }
    }
    return "";
  }

  @NotNull
  public JsonBuilder getRteJsonConfigField(@Nullable String name) {
    if (!Strings.isNullOrEmpty(name)) {
      try {
        return getActiveRteConfig().getRteJsonConfigField(name);
      } catch (Exception exp) {
        LOGGER.error("getRteJsonConfigField for name [" + name + "] failed.", exp);
      }
    }
    return new JsonBuilder();
  }

  public boolean isEmptyRTEString(@Nullable String rteContent) {
    if (!Strings.isNullOrEmpty(rteContent)) {
      return defaultEmptyDocStrategyRole.isEmptyRTEString(rteContent);
    }
    return true;
  }

  @NotNull
  public List<DocumentReference> getRTEConfigsList() {
    try {
      return getActiveRteConfig().getRTEConfigsList();
    } catch (Exception exp) {
      LOGGER.error("getRTEConfigsList failed.", exp);
    }
    return Collections.emptyList();
  }

  public void setRteConfigHint(@Nullable String rteConfigHint) {
    execution.getContext().setProperty(RTE_CONFIG_ACTIV_HINT, rteConfigHint);
  }

  @NotNull
  public String getRteConfigHint() {
    return (String) firstNonNull(execution.getContext().getProperty(RTE_CONFIG_ACTIV_HINT),
        "default");
  }

  @NotNull
  RteConfigRole getActiveRteConfig() {
    return firstNonNull(rteConfigMap.get(getRteConfigHint()), rteConfigMap.get("default"));
  }

  public RteImplementation getRteImplementation() {
    String rteImplHint = cfgSrc.getProperty("celements.rte.impl", "").toLowerCase();
    return rteImplMarshaller.resolve(rteImplHint).or(defaultRteImpl);
  }

}
