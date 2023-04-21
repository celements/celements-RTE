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

(function(window, undefined) {
  "use strict";
  
  var tinyConfigLoaded = false;
  var editorCounter = 0;

  var initCelRTE4 = function() {
    console.log('initCelRTE4: start');
    var params = {
        xpage : 'celements_ajax',
        ajax_mode : 'tinymce/Tiny4Config'
     };
    var hrefSearch = window.location.search;
    var templateRegEx = new RegExp('^(\\?|(.*&)+)?template=([^=&]*).*$');
    if (hrefSearch.match(templateRegEx)) {
      params['template'] = unescape(window.location.search.replace(templateRegEx, '$3'));
    }
    console.log('initCelRTE4: before Ajax tinymce');
    new Ajax.Request(getCelHost(), {
      method: 'post',
      parameters: params,
      onSuccess: function(transport) {
        var tinyConfigJSON = transport.responseText;
        console.log('tinymce4 config loaded: starting tiny');
        if (tinyConfigJSON.isJSON()) {
          var tinyConfigObj = tinyConfigJSON.evalJSON();
          tinyConfigObj["setup"] = celSetupTinyMCE;
          console.debug('initCelRTE4: tinymce.init');
          tinymce.init(tinyConfigObj);
          tinyConfigLoaded = true;
          console.debug('initCelRTE4: tinymce.init finished');
        } else {
          console.error('TinyConfig is no json!', tinyConfigJSON);
        }
      }
    });
  };
  
  var celSetupTinyMCE = function(editor) {
    console.log('celSetupTinyMCE start');
    editor.on('init', celFinishTinyMCEStart);
    console.log('celSetupTinyMCE finish');
  };

  /**
   * loading in struct layout editor
   **/
  (function(structManager){
    console.log('loadTinyMCE async: start');
    if (structManager) {
      if (!structManager.isStartFinished()) {
        console.log('structEditorManager not initialized: register for finishLoading');
        structManager.celStopObserving('structEdit:finishedLoading', initCelRTE4);
        structManager.celObserve('structEdit:finishedLoading', initCelRTE4);
      } else {
        console.log('structEditorManager already initialized: initCelRTE4');
        initCelRTE4();
      }
    } else {
      console.warn('No struct editor manager found -> Failed to initialize tinymce4.');
    }
    console.log('loadTinyMCE async: end');
  })(window.celStructEditorManager);

  /**
   * loading in overlay TabEditor
   **/
  var celFinishTinyMCEStart = function(event) {
    try {
      const editor = event.target;
      console.debug('celFinishTinyMCEStart: start', event);
      console.debug('DEBUG editor.getElement().className', editor.getElement().className,
        editor.getContainer().className);
      $$('body')[0].fire('celRTE:finishedInit', {
        'editor' : editor
      });
      console.debug('celFinishTinyMCEStart: finish', event);
    } catch (exp) {
      console.error('celFinishTinyMCEStart failed', event, exp);
    }
  };

  const lazyLoadTinyMCE = function(mceParentElem) {
    try {
      if (tinyConfigLoaded) {
        getUninitializedMceEditors(mceParentElem).forEach(editorAreaId => {
          console.debug('lazyLoadTinyMCE: mceAddEditor for editorArea', editorAreaId, mceParentElem);
          tinymce.execCommand("mceAddEditor", false, editorAreaId);
        });
        console.debug('lazyLoadTinyMCE: finish', mceParentElem);
      } else {
        console.warn('lazyLoadTinyMCE: skipped, tinyConfig not yet loaded', mceParentElem);
      }
    } catch (exp) {
      console.error("lazyLoadTinyMCE failed. ", exp);
    }
  };

  var getUninitializedMceEditors = function(mceParentElem) {
    console.log('getUninitializedMceEditors: start ', mceParentElem);
    var mceEditorsToInit = new Array();
    $(mceParentElem).select('textarea.mceEditor').each(function(editorArea) {
      if (!editorArea.id) {
        editorArea.writeAttribute('id', editorArea.name + 'Editor' + (++editorCounter));
      }
      var notInitialized = !tinymce.get(editorArea.id);
      console.log('getUninitializedMceEditors: found new editorArea ', editorArea.id,
          notInitialized);
      if (notInitialized) {
        mceEditorsToInit.push(editorArea.id);
      }
    });
    console.log('getUninitializedMceEditors: returns ', mceParentElem, mceEditorsToInit);
    return mceEditorsToInit;
  };

  var delayedEditorOpeningHandler = function(event) {
    console.log('delayedEditorOpeningHandler: start ', event.memo);
    var mceParentElem = event.memo.tabBodyId || "tabMenuPanel";
    var mceEditorAreaAvailable = (getUninitializedMceEditors(mceParentElem).size() > 0);
    if (mceEditorAreaAvailable) {
      console.debug('delayedEditorOpeningHandler: stopping display event');
      event.stop();
      $$('body')[0].observe('celRTE:finishedInit', function() {
        console.debug('delayedEditorOpeningHandler: start display effect');
        event.memo.effect.start();
      });
    }
  };
  var initCelRTE4Listener = function() {
    console.log('initCelRTE4Listener: before initCelRTE4');
    initCelRTE4();
  };

  $j(document).ready(() => {
    console.log("tinymce4: register document ready...");
    $(document.body).observe('celements:contentChanged', event => lazyLoadTinyMCE(event.target));
    if ($('tabMenuPanel')) {
      $('tabMenuPanel').observe('tabedit:finishedLoadingDisplayNow',
          delayedEditorOpeningHandler);
      $('tabMenuPanel').observe('tabedit:tabLoadingFinished',
          event => lazyLoadTinyMCE(event.memo.newTabBodyId));
      console.log('loadTinyMCE-async on ready: before register initCelRTE4Listener');
      getCelementsTabEditor().addAfterInitListener(initCelRTE4Listener);
    }
  });
  
})(window);
