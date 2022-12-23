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
  #tinyReadyPromise;
  #editorCounter;
  #editorInitPromises;
  #tinyConfigLoadedPromise;
  #mceEditorsToInit;
  #tinyConfigObj;

  get tinyReadyPromise() {
    return this.#tinyReadyPromise;
  }

  constructor(beforeTinyInitPromise) {
    this.#mceEditorsToInit = [];
    this.#tinyConfigLoadedPromise = this.#initCelRTE6();
    this.#tinyReadyPromise = this.#getTinyReadyPromise(beforeTinyInitPromise);
    this.#editorCounter = 0;
    this.#editorInitPromises = [];
    this.#tinyConfigLoadedPromise.then((tinyConfig) => {
      this.#filePicker = new CelFilePicker(tinyConfig);
      this.#uploadHandler = new CelUploadHandler(tinyConfig.wiki_attach_path,
        tinyConfig.wiki_imagedownload_path);
    });
  }

  #getTinyReadyPromise(beforeTinyInitPromise) {
    console.debug('getTinyReadyPromise start ');
    return Promise.all([
      this.#tinyConfigLoadedPromise,
      this.#addTinyMceScript(),
      ...beforeTinyInitPromise])
    .then((tinyConfig) => {
      console.debug('getTinyReadyPromise tinymce.init ', tinymce, tinyConfig);
      tinymce.init(tinyConfig);
      console.debug('getTinyReadyPromise tinymce.init done.');
    });
  }

  #addTinyMceScript() {
    return new Promise((resolve) => {
      const jsLazyLoadElem = document.createElement('cel-lazy-load-js');
      jsLazyLoadElem.setAttribute('src', '/file/resources/celRTE/6.3.0/tinymce.min.js');
      jsLazyLoadElem.addEventListener('celements:jsFileLoaded', () => {
        resolve();
        console.debug('addTinyMceScript: tinymce loaded');
      });
      document.body.appendChild(jsLazyLoadElem);
      console.debug('addTinyMceScript: lazy load tinymce started');
    });
  }

  uploadImagesHandler(blobInfo, progress) {
    return this.#uploadHandler.upload({
      'name' : blobInfo.filename(),
      'blob' : blobInfo.blob()
    }, progress);
  }

  celRte_file_picker_handler(callback, value, meta) {
    console.log('celRte_file_picker_handler ', value, meta, callback);
    if (meta.filetype == 'file') {
      this.#filePicker.renderFilePickerInOverlay(false, callback, value);
    }
    if (meta.filetype == 'image') {
      this.#filePicker.renderFilePickerInOverlay(true, callback, value);
    }
    if (meta.filetype == 'media') {
      //TODO
      //callback('movie.mp4', { source2: 'alt.ogg', poster: 'image.jpg' });
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

  #getUninitializedMceEditors(mceParentElem) {
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
    this.#tinyReadyPromise.then((tinyConfig) => {
      console.debug('lazyLoadTinyMCE for', mceParentElem);
      for (const editorAreaId of this.#getUninitializedMceEditors(mceParentElem)) {
        if (!this.#mceEditorsToInit.includes(editorAreaId)) {
          this.#mceEditorsToInit.push(editorAreaId);
          console.log('lazyLoadTinyMCE: mceAddEditor for editorArea', editorAreaId, mceParentElem);
          tinymce.execCommand("mceAddEditor", false, {
            'id' : editorAreaId,
            'options' : tinyConfig
          });
        } else {
          console.debug('lazyLoadTinyMCE: skip ', editorAreaId, mceParentElem);
        }
      }
      console.debug('lazyLoadTinyMCE: finish', mceParentElem);
    }).catch((exp) => {
      console.error("lazyLoadTinyMCE failed. ", exp);
    });
  }

  async #initCelRTE6() {
    console.log('initCelRTE6: start');
    const params = new FormData();
    const hrefSearch = window.location.search;
    const templateRegEx = /^(\?|(.*&)+)?template=([^=&]*).*$/;
    if (hrefSearch.match(templateRegEx)) {
      params.append('template', decodeURIComponent(window.location.search.replace(templateRegEx, '$3')));
    }
    console.log('initCelRTE6: before fetch tinymce');
    const response = await fetch('/ajax/tinymce/Tiny6Config', {
      method: 'POST',
      redirect: 'follow',
      body: params
    });
    if (response.ok) {
      this.#tinyConfigObj = await response.json() ?? {};
      console.log('tinymce6 config loaded: starting tiny');
      this.#tinyConfigObj["setup"] = this.celSetupTinyMCE.bind(this);
      this.#tinyConfigObj["images_upload_handler"] = this.uploadImagesHandler.bind(this);
      this.#tinyConfigObj["file_picker_callback"] = this.celRte_file_picker_handler.bind(this);
      return this.#tinyConfigObj;
    } else {
      throw new Error('fetch failed: ', response.statusText);
    }
  }
}

class TinyMceLazyInitializer {
  #observer;
  #celRteAdaptor;

  constructor(theCelRteAdaptor) {
    this.#celRteAdaptor = theCelRteAdaptor;
  }
  
  initObserver() {
    console.debug("TinyMceLazyInitializer.initObserver: start initObserver");
    const config = { attributes: false, childList: true, subtree: true };  
    this.#observer = new MutationObserver((mutationList) => this.mutationHandler(mutationList));  
    this.#observer.observe(document.body, config);
  }

  mutationHandler(mutationList) {
    for (const mutation of mutationList) {
      if (mutation.type === 'childList') {
        for (const newNode of mutation.addedNodes) {
          if (newNode.nodeType === Node.ELEMENT_NODE) {
            this.#celRteAdaptor.lazyLoadTinyMCE(newNode);
          }
        }
      }
    }
  }
}

class TabEditorTinyPlugin {

  constructor() {
    this.#initTabEditorIfLoaded();
  }  

  #initTabEditorIfLoaded() {
    console.debug('#initTabEditorIfLoaded start');
    this.#afterTabEditorInitializedPromise().then(() => {
      console.log('initTabEditorIfLoaded: TabEditor detected, prepare loading init TabEditor.');
      window.getCelementsTabEditor().celObserve('tabedit:beforeDisplaying',
        this.delayedEditorOpeningPromiseHandler.bind(this));
    });
  }

  #isInTabEditor() {
    return document.querySelectorAll('.celements3_tabMenu').length > 0;
  }

  #afterTabEditorInitializedPromise() {
    if (this.#isInTabEditor()) {
      return new Promise((resolve) => {
        if (typeof window.getCelementsTabEditor === 'function') {
          resolve();
        } else {
          document.addEventListener('load', () => resolve());
        }
      });
    } else {
      return Promise.reject();
    }
  }

  afterTabEditorLoadedPromise() {
    if (this.#isInTabEditor()) {
      return new Promise((resolve) => {
        this.#afterTabEditorInitializedPromise().then(() => {
          window.getCelementsTabEditor().addAfterInitListener(() => {
            resolve();
            console.debug('afterTabEditorLoadedPromise resolved.');
          });
        });
      });
    } else {
      return Promise.resolve();
    }
  }

}

class StructEditorTinyPlugin() {
  constructor() {
    this.#structManager = window.celStructEditorManager;
  }  

  #isInStructEditor() {
    return typeof this.#structManager !== "undefined";
  }

  afterStructEditorInitializedPromise() {
    if (this.#isInStructEditor()) {
      return new Promise((resolve) => {
        if (!this.#structManager.isStartFinished()) {
          this.#structManager.celObserve('structEdit:finishedLoading', () => resolve());
        } else {
          resolve();
        }
      });
    } else {
      return Promise.reject();
    }
  }

}

const tabEditorTinyPlugin = new TabEditorTinyPlugin();
const structEditorTinyPlugin = new StructEditorTinyPlugin();

const celRteAdaptor = new CelRteAdaptor([
  tabEditorTinyPlugin.afterTabEditorLoadedPromise(),
  structEditorTinyPlugin.afterStructEditorInitializedPromise()
]);

//const tinyReadyPromiseBind = celRteAdaptor.tinyReadyPromise.bind(celRteAdaptor);
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
      structManager.celStopObserving('structEdit:finishedLoading', tinyReadyPromiseBind);
      structManager.celObserve('structEdit:finishedLoading', tinyReadyPromiseBind);
    } else {
      console.log('structEditorManager already initialized calling celRteAdaptor.tinyReadyPromise');
  //TODO refactor in a Promise.all for initTiny and script load
      celRteAdaptor.tinyReadyPromise();
    }
  } else {
    console.warn('No struct editor manager found -> Failed to initialize tinymce4.');
  }
  console.log('loadTinyMCE async: end');
})(window.celStructEditorManager);
**/
