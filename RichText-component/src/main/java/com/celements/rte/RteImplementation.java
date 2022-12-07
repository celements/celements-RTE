package com.celements.rte;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface RteImplementation {

  @NotNull
  String getName();

  @NotNull
  List<String> getJavaScriptFiles();

}
