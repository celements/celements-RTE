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


import "../celDynJS/DynamicLoader/celLazyLoader.mjs?version=202212031733";
import { CelUploadHandler }
  from "../celDynJS/upload/fileUpload.mjs?version=202212020804";
import { CelFilePicker }
  from "../celDynJS/ImageAndFilePicker/ImageAndFilePicker.mjs?version=202212121000";

class CelRteAdaptor {
  #uploadHandler;
  #filePicker;
  #tinyConfigPromise;
  #tinyMceScriptPromise;
  #editorCounter;
  #editorInitPromises;
  #mceEditorsToInit;

  constructor(options) {
    this.#mceEditorsToInit = [];
    this.#tinyConfigPromise = this.initTinyMceV6();
//    this.#tinyConfigPromise = this.initCelRTE6();
    this.#tinyMceScriptPromise = this.addTinyMceScript();
    this.tinyReadyPromise().then((tinyConfigObj) => {
      console.debug('initCelRTE6 then: tinymce.init');
      tinymce.init(tinyConfigObj);
      console.debug('initCelRTE6 then: tinymce.init finished');
    });
    this.#editorCounter = 0;
    this.#editorInitPromises = [];
    this.#filePicker = new CelFilePicker(options);
    this.#uploadHandler = new CelUploadHandler(options.wiki_attach_path,
      options.wiki_imagedownload_path);
  }

  tinyReadyPromise() {
    return Promise.all([this.#tinyConfigPromise, this.#tinyMceScriptPromise]);
  }

  addTinyMceScript() {
    return new Promise((resolve) => {
      const jsLazyLoadElem = document.createElement('cel-lazy-load-js');
      jsLazyLoadElem.setAttribute('src', '/file/resources/celRTE/6.3.0/tinymce.min.js');
      document.body.addEventListener('celements:jsFileLoaded', () => resolve());
      document.body.appendChild(jsLazyLoadElem);
    });
  }

  uploadImagesHandler(blobInfo, progress) {
    return this.#uploadHandler.upload({
      'name' : blobInfo.filename(),
      'blob' : blobInfo.blob()
    }, progress);
  }

  celRte_file_picker_handler(callback, value, meta) {
    // Provide file and text for the link dialog
    console.log('celRte_file_picker_handler ', value, meta, callback);
  
    if (meta.filetype == 'file') {
      this.#filePicker.renderFilePickerInOverlay(false, callback, value);
    }
  
    // Provide image and alt text for the image dialog
    if (meta.filetype == 'image') {
      this.#filePicker.renderFilePickerInOverlay(true, callback, value);
    }
  
    // Provide alternative source and posted for the media dialog
    if (meta.filetype == 'media') {
      callback('movie.mp4', { source2: 'alt.ogg', poster: 'image.jpg' });
    }
  }
 
  delayedEditorOpeningPromiseHandler(event) {
    console.debug('delayedEditorOpeningPromiseHandler: start ', event.memo);
    const mceParentElem = event.memo.tabBodyId || "tabMenuPanel";
    const editorFinishPromise = this.#editorInitPromises;
    event.memo.beforePromises.push(editorFinishPromise);
    console.debug('delayedEditorOpeningPromiseHandler: end ', mceParentElem);
  }

  celSetupTinyMCE(editor) {
    this.#editorInitPromises.push(new Promise((resolve) => {
      console.debug("celSetupTinyMCE: register 'init' listener for editor", editor.id);
      editor.on('init', (ev) => {
        console.debug("celSetupTinyMCE: on 'init' for editor done.", editor.id);
        resolve(ev.target);
      });
    }));
    console.debug('celSetupTinyMCE finish');
  }

  getUninitializedMceEditors(mceParentElem) {
    console.debug('getUninitializedMceEditors: start ', mceParentElem);
    const mceEditorsToInit = [];
    for (const editorArea of mceParentElem.querySelectorAll('textarea.mceEditor')) {
      if (!editorArea.id) {
        editorArea.writeAttribute('id', editorArea.name + 'Editor' + (++this.#editorCounter));
      }
      const notInitialized = !tinymce.get(editorArea.id);
      console.log('getUninitializedMceEditors: found new editorArea ', editorArea.id,
          notInitialized);
      if (notInitialized) {
        mceEditorsToInit.push(editorArea.id);
      }
    }
    console.debug('getUninitializedMceEditors: returns ', mceParentElem, mceEditorsToInit);
    return mceEditorsToInit;
  }

  lazyLoadTinyMCE(mceParentElem) {
    this.tinyReadyPromise().then(() => {
      console.debug('lazyLoadTinyMCE for', mceParentElem);
      for (const editorAreaId of this.getUninitializedMceEditors(mceParentElem)) {
        console.debug('lazyLoadTinyMCE: mceAddEditor for editorArea', editorAreaId, mceParentElem);
        if (!this.#mceEditorsToInit.includes(editorAreaId)) {
          this.#mceEditorsToInit.push(editorAreaId);
          tinymce.execCommand("mceAddEditor", false, editorAreaId);
        } else {
          console.log('lazyLoadTinyMCE: skip ', editorAreaId, mceParentElem);
        }
      }
      console.debug('lazyLoadTinyMCE: finish', mceParentElem);
    }).catch((exp) => {
      console.error("lazyLoadTinyMCE failed. ", exp);
    });
  }

  async initCelRTE6() {
    console.log('initCelRTE6: start');
    const params = new FormData();
    const hrefSearch = window.location.search;
    const templateRegEx = /^(\?|(.*&)+)?template=([^=&]*).*$/;
    if (hrefSearch.match(templateRegEx)) {
      params.append('template', decodeURIComponent(window.location.search.replace(templateRegEx, '$3')));
    }
    console.log('initCelRTE6: before Ajax tinymce');
    const response = await fetch('/ajax/tinymce/Tiny6Config', {
      method: 'POST',
      redirect: 'follow',
      body: params
    });
    if (response.ok) {
      const tinyConfigObj = await response.json() ?? {};
      console.log('tinymce6 config loaded: starting tiny');
      tinyConfigObj["setup"] = this.celSetupTinyMCE.bind(this);
      return tinyConfigObj;
    } else {
      throw new Error('fetch failed: ', response.statusText);
    }
  }

  initTinyMceV6(event) {
    console.log('init TinyMCE v6 start ...', event.eventName, event);
    return new Promise((resolve) => {
      resolve({
        "selector" : "textarea.tinyMCE,textarea.mceEditor", "language" : "de",
        "valid_elements" : "b/strong,caption,hr[class|width|size|noshade],+a[href|class|target|onclick|name|id|title|rel|hreflang],br,i/em,#p[style|class|name|id],#h?[align<center?justify?left?right|class|style|id],-span[class|style|id|title],textformat[blockindent|indent|leading|leftmargin|rightmargin|tabstops],sub[class],sup[class],img[width|height|class|align|style|src|border=0|alt|id|title|usemap],table[align<center?left?right|bgcolor|border|cellpadding|cellspacing|class|height|width|style|id|title],tbody[align<center?char?justify?left?right|class|valign<baseline?bottom?middle?top],#td[align<center?char?justify?left?right|bgcolor|class|colspan|headers|height|nowrap<nowrap|style|rowspan|scope<col?colgroup?row?rowgroup|valign<baseline?bottom?middle?top|width],#th[align<center?char?justify?left?right|bgcolor|class|colspan|headers|height|rowspan|scope<col?colgroup?row?rowgroup|valign<baseline?bottom?middle?top|style|width],thead[align<center?char?justify?left?right|class|valign<baseline?bottom?middle?top],-tr[align<center?char?justify?left?right|bgcolor|class|style|rowspan|valign<baseline?bottom?middle?top|id],-ol[class|type|compact],-ul[class|type|compact],#li[class]",
        "invalid_elements" : "blockquote,body,button,center,cite,code,col,colgroup,dd,del,dfn,dir,div,dl,dt,fieldset,font,form,frame,frameset,head,html,iframe,input,ins,kbd,isindex,label,legend,link,map,menu,meta,noframes,noscript,object,optgroup,option,param,pre/listing/plaintext/xmp,q,s,samp,script,select,small,strike,textarea,tfoot,tt,u,var",
        "height" : "500", "width" : "1000", "menubar" : false, "branding" : false,
        "plugins" : " preview searchreplace autolink directionality visualblocks visualchars fullscreen link image template codesample table charmap pagebreak nonbreaking anchor insertdatetime advlist lists wordcount help code media",
        "toolbar"  : [
          "image | link",
          "removeformat formatselect bold italic underline | alignleft aligncenter alignright alignjustify | bullist | unlink insertimage",
          "pastetext paste | table | tablerowprops tablecellprops | tableinsertrowbefore tableinsertrowafter tabledeleterow | tableinsertcolbefore tableinsertcolafter tabledeletecol | tablesplitcells tablemergecells | template | code",
          "media"
        ], "celanim_slideshow" : true, "cel_crop" : true,
        "gallerylist" : " (Gallery8)=Gallery.Gallery8, (Gallery9)=Gallery.Gallery9,Album1  (Gallery11)=Gallery.Gallery11,Bilderimport (TestgalerieBilderimport)=Gallery.TestgalerieBilderimport,Bringold demo Alt (Bringold-Demo)=Gallery.Bringold-Demo,Japan :-) (Gallery1)=Gallery.Gallery1,Mit Meta! (Gallery6)=Gallery.Gallery6,Neugier (TestgallerySilvia)=Gallery.TestgallerySilvia,Test Titel (Gallery7)=Gallery.Gallery7,Testfotoalbum (Celementsgalerie)=Gallery.Celementsgalerie,UmlautTest (UmlautTest)=Gallery.UmlautTest,Webseiten synventis (CMSsynventisReferenzseiten)=Gallery.CMSsynventisReferenzseiten",
        "content_css" : [
          "/file/resources/celRes/celements2%2Dcontent.css?version=20221118165414",
          "/file/BellisLayout/WebHome/Bellis%2Dcontent.css?version=20160122155749"
        ],
        "wiki_linkpicker_space" : "Content",  "wiki_linkpicker_baseurl" : "/untitled1",
        "entity_encoding" : "raw", "autoresize_bottom_margin" : 1,
        "autoresize_min_height" : 0, "style_formats" : [], "image_advtab": true, "image_uploadtab" : true,
        "images_upload_handler": this.uploadImagesHandler.bind(this),
        "file_picker_callback" :  this.celRte_file_picker_handler.bind(this),
        "automatic_uploads": true
      });
    });
  }
}

class TinyMceLazyInitializer {
  #observer;
  #celRteAdaptor;

  constructor(celRteAdaptor) {
    this.#celRteAdaptor = celRteAdaptor;
  }
  
  initObserver() {
    console.debug("TinyMceLazyInitializer.initObserver: start initObserver");
    const config = { attributes: false, childList: true, subtree: true };  
    this.#observer = new MutationObserver((mutationList) => this.mutationHandler(mutationList));  
    this.#observer.observe(document.body, config);
  }

  mutationHandler(mutationList) {
    console.debug('TinyMceLazyInitializer.mutationHandler: start mutationHandler');
    for (const mutation of mutationList) {
      if (mutation.type === 'childList') {
        for (const newNode of mutation.addedNodes) {
          if (newNode.nodeType === Node.ELEMENT_NODE) {
            console.debug('mutationHandler for node', newNode, newNode.nodeType);
            this.#celRteAdaptor.lazyLoadTinyMCE(newNode);
          }
        }
      }
    }
  }
}

const celRteAdaptor = new CelRteAdaptor({
  "wiki_attach_path" : "/Content_attachments/FileBaseDoc",
  "wiki_imagedownload_path" : "/download/Content_attachments/FileBaseDoc",
  "filebaseFN" : "Content_attachments.FileBaseDoc"
});
//const initCelRTE6Bind = celRteAdaptor.initCelRTE6.bind(celRteAdaptor);
new TinyMceLazyInitializer(celRteAdaptor).initObserver();

/**
 * loading in struct layout editor
 **/
//XXX structEdit:finishLoading still needed with MutationObserver???
/**
(function(structManager){
  console.log('loadTinyMCE async: start');
  if (structManager) {
    if (!structManager.isStartFinished()) {
      console.log('structEditorManager not initialized: register for finishLoading');
  //TODO refactor in a Promise.all for initTiny and script load
      structManager.celStopObserving('structEdit:finishedLoading', initCelRTE6Bind);
      structManager.celObserve('structEdit:finishedLoading', initCelRTE6Bind);
    } else {
      console.log('structEditorManager already initialized calling celRteAdaptor.initCelRTE6');
  //TODO refactor in a Promise.all for initTiny and script load
      celRteAdaptor.initCelRTE6();
    }
  } else {
    console.warn('No struct editor manager found -> Failed to initialize tinymce4.');
  }
  console.log('loadTinyMCE async: end');
})(window.celStructEditorManager);
**/

if (typeof window.getCelementsTabEditor === 'function') {
  window.getCelementsTabEditor().celObserve('tabedit:beforeDisplaying',
    celRteAdaptor.delayedEditorOpeningPromiseHandler.bind(celRteAdaptor));
  celRteAdaptor.lazyLoadTinyMCE(document.body);
  //XXX addAfterInitListener still needed with MutationObserver???
  //getCelementsTabEditor().addAfterInitListener(initCelRTE6Bind);
}
