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


import "../celDynJS/DynamicLoader/celLazyLoader.mjs?version=202301052318";
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
  #tinyDefaults = Object.freeze({
    'menubar' : false,
    'branding' : false,
    'entity_encoding' : 'raw',
    'image_advtab' : true,
    'automatic_uploads' : true,
    'image_uploadtab' : true,
    'autoresize_bottom_margin' : 1,
    'autoresize_min_height' : 0
  });

  constructor() {
    this.#editorCounter = 0;
    this.#editorInitPromises = [];
  }

  start(beforeTinyInitPromiseArray) {
    this.#tinyConfigLoadedPromise = this.#initCelRTE6();
    this.#tinyReadyPromise = this.#getTinyReadyPromise(beforeTinyInitPromiseArray);
    this.#setupFilePickerAndUploadHandler(this.#tinyConfigLoadedPromise);
  }

  async #setupFilePickerAndUploadHandler(tinyConfigLoadedPromise) {
    const tinyConfig = await tinyConfigLoadedPromise;
    this.#filePicker = new CelFilePicker(tinyConfig);
    this.#uploadHandler = new CelUploadHandler(tinyConfig.attach_path, tinyConfig.download_path);
  }

  get editorInitPromises() {
    return this.#editorInitPromises;
  }

  get tinyReadyPromise() {
    return this.#tinyReadyPromise;
  }

  async #getTinyReadyPromise(beforeTinyInitPromiseArray) {
    console.trace('getTinyReadyPromise start');
    const tinyConfig = await this.#tinyConfigLoadedPromise;
    await Promise.all([
        this.#tinyConfigLoadedPromise,
        this.#addTinyMceScript(),
        ...beforeTinyInitPromiseArray
    ]);
    console.debug('getTinyReadyPromise tinymce.init', tinymce, tinyConfig);
    tinymce.init(tinyConfig);
    console.debug('getTinyReadyPromise tinymce.init done.');
    return tinyConfig;
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

  filePickerHandler(callback, value, meta) {
    console.log('filePickerHandler', value, meta, callback);
    if (meta.filetype === 'file') {
      this.#filePicker.renderFilePickerInOverlay(false, callback, value);
    } else if (meta.filetype === 'image') {
      this.#filePicker.renderFilePickerInOverlay(true, callback, value);
    } else {
      throw new Exception("unsupported filetype '" + meta.filetype + "'");
    }
  }
 
  tinyMceSetupDoneHandler(editor) {
    this.#editorInitPromises.push(new Promise((resolve) => {
      console.debug("tinyMceSetupDoneHandler: register 'init' listener for editor", editor.id);
      editor.on('init', (ev) => {
        console.debug("tinyMceSetupDoneHandler: on 'init' for editor done.", editor.id);
      [...editor.getElement().classList]
        .filter(cssClass => cssClass.startsWith('celEditorBody_'))
        .forEach(cssClass => editor.getBody().classList.add(cssClass));
        document.getElementById(editor.id).setAttribute('cel-rte-state', 'initialized');
        resolve(ev.target);
      });
    }));
    console.trace('tinyMceSetupDoneHandler finish');
  }

  #getUninitializedMceEditors(mceParentElem) {
    console.trace('getUninitializedMceEditors: start', mceParentElem);
    const mceEditorsToInit = [];
    for (const editorArea of mceParentElem.querySelectorAll(
        'textarea.mceEditor:not([data-cel-rte-state])')) {
      if (!editorArea.id) {
        editorArea.setAttribute('id', editorArea.name + 'Editor' + (++this.#editorCounter));
      }
      if (!tinymce.get(editorArea.id)) {
        console.debug('getUninitializedMceEditors: found new editorArea', editorArea.id);
        mceEditorsToInit.push(editorArea);
      } else {
        console.trace('getUninitializedMceEditors: skip already initialized rte',
          editorArea.id);
      }
    }
    console.trace('getUninitializedMceEditors: returns', mceParentElem, mceEditorsToInit);
    return mceEditorsToInit;
  }

  async lazyLoadTinyMCE(mceParentElem) {
    try {
      const tinyConfig = await this.#tinyReadyPromise;
      console.debug('lazyLoadTinyMCE for', mceParentElem);
      for (const editorArea of this.#getUninitializedMceEditors(mceParentElem)) {
        console.log('lazyLoadTinyMCE: mceAddEditor for editorArea', editorArea.id, mceParentElem);
        editorArea.setAttribute('cel-rte-state', 'initializing');
        tinymce.execCommand("mceAddEditor", false, {
          'id' : editorArea.id,
          'options' : tinyConfig
        });
      }
      console.trace('lazyLoadTinyMCE: finish', mceParentElem);
    } catch (exp) {
      console.error("lazyLoadTinyMCE failed. ", exp);
    }
  }

  async #initCelRTE6() {
    console.log('initCelRTE6: start');
    const params = new FormData();
    const hrefSearch = window.location.search;
    const templateRegEx = /^(\?|(.*&)+)?template=([^=&]*).*$/;
    if (hrefSearch.match(templateRegEx)) {
      params.append('template', decodeURIComponent(
        window.location.search.replace(templateRegEx, '$3')));
    }
    console.log('initCelRTE6: before fetch tinymce');
    const response = await fetch('/ajax/tinymce/Tiny6Config', {
      method: 'POST',
      redirect: 'follow',
      body: params
    });
    if (response.ok) {
      const tinyConfigObj = Object.assign({}, this.#tinyDefaults, await response.json());
      console.log('tinymce6 config loaded: starting tiny');
      tinyConfigObj["setup"] = this.tinyMceSetupDoneHandler.bind(this);
      tinyConfigObj["images_upload_handler"] = this.uploadImagesHandler.bind(this);
      tinyConfigObj["file_picker_callback"] = this.filePickerHandler.bind(this);
      return tinyConfigObj;
    } else {
      throw new Error('fetch failed:', response.statusText);
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
    console.trace("TinyMceLazyInitializer.initObserver: start initObserver");
    this.#observer = new MutationObserver(mutationList =>
      mutationList.flatMap(mutation => [...mutation.addedNodes])
      .filter(newNode => (newNode.nodeType === Node.ELEMENT_NODE))
      .forEach(newNode => this.#celRteAdaptor.lazyLoadTinyMCE(newNode)));
    this.#observer.observe(document.body, {
      attributes: false,
      childList: true,
      subtree: true
    });
  }

}

class TabEditorTinyPlugin {
  #rteAdaptor;

  constructor(rteAdaptor) {
    this.#rteAdaptor = rteAdaptor;
    this.#initTabEditorIfLoaded();
  }  

  delayedEditorOpeningPromiseHandler(event) {
    console.trace('delayedEditorOpeningPromiseHandler: start', event.memo);
    const mceParentElem = event.memo.tabBodyId || "tabMenuPanel";
    const editorFinishPromise = this.#rteAdaptor.editorInitPromises;
    event.memo.beforePromises.push(editorFinishPromise);
    console.trace('delayedEditorOpeningPromiseHandler: end', mceParentElem);
  }

  async #initTabEditorIfLoaded() {
    try {
      console.trace('#initTabEditorIfLoaded start');
      await this.#afterTabEditorInitializedPromise();
      console.log('initTabEditorIfLoaded: TabEditor detected, prepare loading init TabEditor.');
      window.getCelementsTabEditor().celObserve('tabedit:beforeDisplaying',
        this.delayedEditorOpeningPromiseHandler.bind(this));
    } catch (error) {
      console.debug('#initTabEditorIfLoaded', error);
    }
  }

  #isInTabEditor() {
    return document.querySelectorAll('.celements3_tabMenu').length > 0;
  }

  #afterTabEditorInitializedPromise() {
    if (!this.#isInTabEditor()) {
      return Promise.reject('Not in Tab Editor');
    } else if (typeof window.getCelementsTabEditor === 'function') {
      return Promise.resolve();
    } else {
      return new Promise(resolve => document.addEventListener('load', event => resolve(event)));
    }
  }

  async afterTabEditorLoadedPromise() {
    if (this.#isInTabEditor()) {
      await this.#afterTabEditorInitializedPromise();
      return new Promise(resolve => window.getCelementsTabEditor().addAfterInitListener(() => {
        resolve();
        console.debug('afterTabEditorLoadedPromise resolved.');
      }));
    }
  }

}

class StructEditorTinyPlugin {
  #structManager;

  constructor() {
    this.#structManager = window.celStructEditorManager;
  }  

  #isInStructEditor() {
    return typeof this.#structManager !== "undefined";
  }

  afterStructEditorLoadedPromise() {
    if (this.#isInStructEditor() && !this.#structManager.isStartFinished()) {
      return new Promise(resolve => this.#structManager.celObserve(
        'structEdit:finishedLoading', event => resolve(event)));
    } else {
      console.debug('afterStructEditorLoadedPromise: no structEditor found, skip init tiny');
      return Promise.resolve();
    }
  }

}

const celRteAdaptor = new CelRteAdaptor();

const tabEditorTinyPlugin = new TabEditorTinyPlugin(celRteAdaptor);
const structEditorTinyPlugin = new StructEditorTinyPlugin();

celRteAdaptor.start([
  tabEditorTinyPlugin.afterTabEditorLoadedPromise(),
  structEditorTinyPlugin.afterStructEditorLoadedPromise()
]);

new TinyMceLazyInitializer(celRteAdaptor).initObserver();
