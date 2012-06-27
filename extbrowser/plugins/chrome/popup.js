/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

// references from bg page
var NetBeans_Presets = chrome.extension.getBackgroundPage().NetBeans_Presets;

/**
 * Window presets menu.
 */
var NetBeans_PresetMenu = {};
// menu container
NetBeans_PresetMenu._container = null;
// menu presets
NetBeans_PresetMenu._presets = null;
// show the menu
NetBeans_PresetMenu.show = function(presets) {
    this._init();
    this._presets = presets;
    this._putPresets(this._presets);
}
NetBeans_PresetMenu.hide = function() {
    window.close();
}
NetBeans_PresetMenu.resetPage = function() {
    var that = this;
    chrome.windows.getLastFocused(function(win) {
        var opt = {};
        opt.state = 'maximized';
        chrome.windows.update(win.id, opt);
        that.hide();
    });
}
NetBeans_PresetMenu.resizePage = function(preset) {
    if (preset == null) {
        this.resetPage();
        return;
    }
    var data = NetBeans_Presets.getPreset(preset);
    if (data == null) {
        console.error('Preset [' + preset + '] not found.');
        return;
    }
    this._resizePage(data['width'], data['height']);
}
/*** ~Private ***/
// menu init
NetBeans_PresetMenu._init = function() {
    if (this._container != null) {
        return;
    }
    this._container = document.getElementById('presetMenu');
    this._registerEvents();
}
// register events
NetBeans_PresetMenu._registerEvents = function() {
    var that = this;
    document.getElementById('autoPresetMenu').addEventListener('click', function() {
        that.resetPage();
    }, false);
    document.getElementById('customizePresetsMenu').addEventListener('click', function() {
        that._showPresetCustomizer();
    }, false);
}
// clean and put presets to the menu (first, presets from toolbar)
NetBeans_PresetMenu._putPresets = function() {
    var menu = document.getElementById('menuPresets');
    // clean
    menu.innerHTML = '';
    this._putPresetsInternal(true);
    this._putPresetsInternal(false);
}
// put presets to the menu (internal)
NetBeans_PresetMenu._putPresetsInternal = function(toolbar) {
    var menu = document.getElementById('menuPresets');
    for (p in this._presets) {
        var preset = this._presets[p];
        if (preset.toolbar != toolbar) {
            continue;
        }
        // item
        var item = document.createElement('a');
        item.setAttribute('href', '#');
        item.setAttribute('title', preset.title + ' (' + preset.width + ' x ' + preset.height + ')');
        item.setAttribute('onclick', 'NetBeans_PresetMenu.resizePage(' + p + ');');
        // type
        var typeDiv = document.createElement('div');
        typeDiv.setAttribute('class', 'type');
        typeDiv.appendChild(document.createTextNode(preset.type.title));
        item.appendChild(typeDiv);
        // label
        var labelDiv = document.createElement('div');
        labelDiv.setAttribute('class', 'label');
        // label - size
        var sizeDiv = document.createElement('div');
        sizeDiv.setAttribute('class', 'size');
        sizeDiv.appendChild(document.createTextNode(preset.width + ' x ' + preset.height));
        labelDiv.appendChild(sizeDiv);
        // label - title
        var titleDiv = document.createElement('div');
        titleDiv.setAttribute('class', 'title');
        titleDiv.appendChild(document.createTextNode(preset.title));
        labelDiv.appendChild(titleDiv);
        item.appendChild(labelDiv);
        // append item
        menu.appendChild(item);
        menu.appendChild(document.createElement('hr'));
    }
}
// resize page
NetBeans_PresetMenu._resizePage = function(width, height) {
    var that = this;
    chrome.windows.getLastFocused(function(win) {
        var opt = {};
        opt.state = 'normal';
		opt.width = parseInt(width);
		opt.height = parseInt(height);
        chrome.windows.update(win.id, opt);
        that.hide();
    });
}
// show preset customizer
NetBeans_PresetMenu._showPresetCustomizer = function() {
    chrome.tabs.create({'url': 'options.html'});
    this.hide();
}

// run!
window.onload = function() {
    NetBeans_PresetMenu.show(NetBeans_Presets.getPresets());
}
