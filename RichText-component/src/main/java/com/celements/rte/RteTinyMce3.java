package com.celements.rte;

import java.util.List;

import org.xwiki.component.annotation.Component;

@Component(RteTinyMce3.NAME)
public class RteTinyMce3 implements RteImplementation {

  public static final String NAME = "tinymce3";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<String> getJavaScriptFiles() {
    return List.of(
        ":celRTE/tiny_mce_src.js",
        ":celJS/celTabMenu/loadTinyMCE-async.js");
  }

}
