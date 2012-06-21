/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ('GPL') or the Common
 * Development and Distribution License('CDDL') (collectively, the
 * 'License'). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the 'Classpath' exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * 'Portions Copyrighted [year] [name of copyright owner]'
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * '[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license.' If you do not indicate a
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

/**
 * The main object, manipulating with the page.
 */
function NetBeans_Page() {
    // all presets
    this._presets = null;
    // active/current preset
    this._preset = null;
    // preset menu
    this._presetMenu = new NetBeans_PresetMenu(); // XXX how to create and gc the preset menu properly?
}
// page init
NetBeans_Page.prototype.initPage = function() {
    this._registerEvents();
    this._showPresets();
}
// resize page to the given preset, can be null (it means "fit to size")
NetBeans_Page.prototype.resizePage = function(preset) {
    if (preset == null) {
        this.resetPage();
        return;
    }
    var data = this.getPreset(preset);
    if (data == null) {
        console.error('Preset [' + preset + '] not found.');
        return;
    }
    this._resizeFrame(data['width'], data['height'], 'px');
}
// fit page to the window size
NetBeans_Page.prototype.resetPage = function() {
    this._resizeFrame('100', '100', '%');
}
// show menu for presets
NetBeans_Page.prototype.showPresetMenu = function() {
    this._presetMenu.show(this._loadPresets()); // always use copy of presets!
}
// get preset, can be null (the current preset is returned in such case)
NetBeans_Page.prototype.getPreset = function(preset) {
    if (preset == undefined) {
        return this._preset;
    }
    var tmp = this.getPresets()[preset];
    if (tmp == undefined) {
        return null;
    }
    this._preset = tmp;
    return this._preset;
}
// get all presets
NetBeans_Page.prototype.getPresets = function() {
    if (this._presets == null) {
        this._presets = this._loadPresets();
    }
    return this._presets;
}
// set (and save) new presets
NetBeans_Page.prototype.setPresets = function(presets) {
    this._presets = presets;
    this._savePresets();
}
// redraw presets (in the toolbar)
NetBeans_Page.prototype.redrawPresets = function() {
    this._showPresets();
}
// udpates Selection Mode checkbox in the toolbar
NetBeans_Page.prototype.setSelectionMode = function(checked) {
    var selectionMode = document.getElementById('selectionModeCheckbox');
    selectionMode.checked = checked;
}
/*** ~Private ***/
// register events
NetBeans_Page.prototype._registerEvents = function() {
    var that = this;
    document.getElementById('autoPresetButton').addEventListener('click', function() {
        that.resizePage(null);
    }, false);
    document.getElementById('presetMenuButton').addEventListener('click', function() {
        that.showPresetMenu();
    }, false);
    var selectionModeCheckBox = document.getElementById('selectionModeCheckbox');
    selectionModeCheckBox.setAttribute(':netbeans_generated', 'true');
    var selectionModeDiv = document.getElementById('selectionModeDiv');
    selectionModeDiv.addEventListener('click', function(e) {
        if (e.target !== selectionModeCheckBox) {
            selectionModeCheckBox.checked = !selectionModeCheckBox.checked;
        }
        that._updateSelectionMode();
    }, false);
}
// notifies IDE about Selection Mode modification
NetBeans_Page.prototype._updateSelectionMode = function() {
    var selectionMode = document.getElementById('selectionModeCheckbox');
    selectionMode.setAttribute('selection_mode', selectionMode.checked);
}
// show presets in the toolbar
NetBeans_Page.prototype._showPresets = function() {
    var presets = this.getPresets();
    var resizer = document.getElementById('presets');
    // clean
    resizer.innerHTML = '';
    // add buttons
    for (p in presets) {
        var preset = presets[p];
        if (!preset.toolbar) {
            continue;
        }
        var button = document.createElement('a');
        button.setAttribute('href', '#');
        button.setAttribute('class', 'button');
        button.setAttribute('title', preset.title + ' (' + preset.width + ' x ' + preset.height + ')');
        button.setAttribute('onclick', 'NetBeans_Page.resizePage(' + p + '); return false;');
        button.appendChild(document.createTextNode(preset.title));
        resizer.appendChild(button);
    }
}
// load presets from the central storage
NetBeans_Page.prototype._loadPresets = function() {
    // XXX load presets from NB
    if (this._presets != null) {
        return this._presets.slice(0);
    }
    return [
        new NetBeans_Preset(NetBeans_Preset.DESKTOP, 'Widescreen', '1680', '1050', true, true),
        new NetBeans_Preset(NetBeans_Preset.DESKTOP, 'Desktop', '1280', '1024', true, true),
        new NetBeans_Preset(NetBeans_Preset.NETBOOK, 'Netbook', '1024', '600', true, true),
        new NetBeans_Preset(NetBeans_Preset.TABLET_LANDSCAPE, 'Tablet Landscape', '1024', '768', true, true),
        new NetBeans_Preset(NetBeans_Preset.TABLET_PORTRAIT, 'Tablet Portrait', '768', '1024', true, true),
        new NetBeans_Preset(NetBeans_Preset.SMARTPHONE_LANDSCAPE, 'Smartphone Landscape', '480', '320', true, true),
        new NetBeans_Preset(NetBeans_Preset.SMARTPHONE_PORTRAIT, 'Smartphone Portrait', '320', '480', true, true)
    ];
}
// save presets to the central storage
NetBeans_Page.prototype._savePresets = function() {
    // XXX save presets back to NB
    console.error('Saving presets not implemented');
}
// resize frame (and the mask)
NetBeans_Page.prototype._resizeFrame = function(width, height, units) {
    var nbframe = document.getElementById('nbframe');
    var mask = document.getElementById('mask');
    nbframe.style.width = width + units;
    mask.style.marginTop = height + units;
}

/**
 * Class representing window preset.
 *
 * Internal presets cannot be removed.
 */
function NetBeans_Preset(type, title, width, height, toolbar, internal) {
    // type
    this.type = type;
    // title
    this.title = title;
    // width (in px)
    this.width = width;
    // height (in px)
    this.height = height;
    // show in toolbar
    this.toolbar = toolbar;
    // internal or not?
    this.internal = internal;
}
// preset type for Desktops
NetBeans_Preset.DESKTOP = {
    ident: 'DESKTOP',
    title: 'Desktop' // XXX i18n
};
// preset type for Netbooks
NetBeans_Preset.NETBOOK = {
    ident: 'NETBOOK',
    title: 'Netbook'
};
// preset type for Tablets (Landscape)
NetBeans_Preset.TABLET_LANDSCAPE = {
    ident: 'TABLET_LANDSCAPE',
    title: 'Tablet Landscape'
};
// preset type for Tablets (Portrait)
NetBeans_Preset.TABLET_PORTRAIT = {
    ident: 'TABLET_PORTRAIT',
    title: 'Tablet Portrait'
};
// preset type for Smartphones  (Landscape)
NetBeans_Preset.SMARTPHONE_LANDSCAPE = {
    ident: 'SMARTPHONE_LANDSCAPE',
    title: 'Smartphone Landscape'
};
// preset type for Smartphones  (Portrait)
NetBeans_Preset.SMARTPHONE_PORTRAIT = {
    ident: 'SMARTPHONE_PORTRAIT',
    title: 'Smartphone Portrait'
};
// get a list of all preset types
NetBeans_Preset.allTypes = function() {
    return [
        NetBeans_Preset.DESKTOP,
        NetBeans_Preset.NETBOOK,
        NetBeans_Preset.TABLET_LANDSCAPE,
        NetBeans_Preset.TABLET_PORTRAIT,
        NetBeans_Preset.SMARTPHONE_LANDSCAPE,
        NetBeans_Preset.SMARTPHONE_PORTRAIT
    ];
}
// get preset type for the given ident, or null if not found
NetBeans_Preset.typeForIdent = function(ident) {
    var allTypes = NetBeans_Preset.allTypes();
    for (i in allTypes) {
        if (allTypes[i].ident == ident) {
            return allTypes[i];
        }
    }
    return null;
}

/**
 * Window presets menu.
 */
function NetBeans_PresetMenu() {
    // menu container
    this._container = null;
    // menu presets
    this._presets = null;
    // hide timeout
    this._hideTimeout = null;
    // preset menu button
    this._presetMenuButton = null;
    // preset customizer
    this._presetCustomizer = new NetBeans_PresetCustomizer();
}
// show the menu
NetBeans_PresetMenu.prototype.show = function(presets) {
    this._init();
    this._presets = presets;
    this._putPresets(this._presets);
    this._show();
}
// hide the menu
NetBeans_PresetMenu.prototype.hide = function() {
    this._hide();
    this._hideTimeout = null;
}
/*** ~Private ***/
// menu init
NetBeans_PresetMenu.prototype._init = function() {
    if (this._container != null) {
        return;
    }
    this._container = document.getElementById('presetMenu');
    this._presetMenuButton = document.getElementById('presetMenuButton');
    this._registerEvents();
}
// show menu
NetBeans_PresetMenu.prototype._show = function() {
    this._container.style.left = this._presetMenuButton.offsetLeft + 'px';
    this._container.style.visibility = 'visible';
    this._hideLater(2000);
}
// hide menu
NetBeans_PresetMenu.prototype._hide = function() {
    this._container.style.visibility = 'hidden';
}
// clear the hide timeout
NetBeans_PresetMenu.prototype._hideStop = function() {
    clearTimeout(this._hideTimeout);
}
// hide menu after the given timeout, can be undefined (it means 1 second in such case)
NetBeans_PresetMenu.prototype._hideLater = function(timeout) {
    if (timeout == undefined) {
        timeout = 1000;
    }
    var that = this;
    this._hideTimeout = setTimeout(function() {
        that.hide();
    }, timeout);
}
// register events
NetBeans_PresetMenu.prototype._registerEvents = function() {
    var that = this;
    this._container.addEventListener('mouseover', function() {
        that._hideStop();
    }, false);
    this._container.addEventListener('mouseout', function() {
        that._hideLater();
    }, false);
    this._container.addEventListener('click', function() {
        that.hide();
    }, false);
    document.getElementById('autoPresetMenu').addEventListener('click', function() {
        NetBeans_Page.resetPage();
    }, false);
    document.getElementById('customizePresetsMenu').addEventListener('click', function() {
        that._presetCustomizer.show(that._presets);
    }, false);
}
// clean and put presets to the menu (first, presets from toolbar)
NetBeans_PresetMenu.prototype._putPresets = function() {
    var menu = document.getElementById('menuPresets');
    // clean
    menu.innerHTML = '';
    this._putPresetsInternal(true);
    this._putPresetsInternal(false);
}
// put presets to the menu (internal)
NetBeans_PresetMenu.prototype._putPresetsInternal = function(toolbar) {
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
        item.setAttribute('onclick', 'NetBeans_Page.resizePage(' + p + '); return false;');
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

/**
 * Preset customizer.
 */
function NetBeans_PresetCustomizer() {
    // customizer container
    this._container = null;
    // presets container
    this._rowContainer = null;
    // remove button
    this._removePresetButton = null;
    // move up button
    this._moveUpPresetButton = null;
    // move down button
    this._moveDownPresetButton = null;
    // OK button
    this._okButton = null;
    // presets
    this._presets = null;
    // active/selected preset
    this._activePreset = null;
}
// show customizer
NetBeans_PresetCustomizer.prototype.show = function(presets) {
    this._init();
    this._presets = presets;
    this._putPresets(this._presets);
    this._show();
}
/*** ~Private ***/
// customizer init
NetBeans_PresetCustomizer.prototype._init = function() {
    if (this._container != null) {
        return;
    }
    this._container = document.getElementById('presetCustomizer');
    this._rowContainer = document.getElementById('presetCustomizerTable').getElementsByTagName('tbody')[0];
    this._removePresetButton = document.getElementById('removePreset');
    this._moveUpPresetButton = document.getElementById('moveUpPreset');
    this._moveDownPresetButton = document.getElementById('moveDownPreset');
    this._okButton = document.getElementById('presetCustomizerOk');
    this._registerEvents();
}
// show customizer
NetBeans_PresetCustomizer.prototype._show = function() {
    NetBeans_Disabler.on();
    var left = Math.max(window.innerWidth / 2 - this._container.clientWidth / 2, 0);
    var top = Math.max(window.innerHeight / 2 - this._container.clientHeight / 2, 0);
    this._container.style.left = left + 'px';
    this._container.style.top = top + 'px';
    this._container.style.visibility = 'visible';
}
// hide customizer
NetBeans_PresetCustomizer.prototype._hide = function() {
    NetBeans_Disabler.off();
    this._container.style.visibility = 'hidden';
}
// register events
NetBeans_PresetCustomizer.prototype._registerEvents = function() {
    var that = this;
    document.getElementById('closePresetCustomizer').addEventListener('click', function() {
        that._cancel();
    }, false);
    document.getElementById('addPreset').addEventListener('click', function() {
        that._addPreset();
    }, false);
    this._removePresetButton.addEventListener('click', function() {
        that._removePreset();
    }, false);
    this._moveUpPresetButton.addEventListener('click', function() {
        that._moveUpPreset();
    }, false);
    this._moveDownPresetButton.addEventListener('click', function() {
        that._moveDownPreset();
    }, false);
    this._okButton.addEventListener('click', function() {
        that._save();
    }, false);
    document.getElementById('presetCustomizerCancel').addEventListener('click', function() {
        that._cancel();
    }, false);
    document.getElementById('presetCustomizerHelp').addEventListener('click', function() {
        alert('[not implemented]');
    }, false);
}
// put presets to the customizer
NetBeans_PresetCustomizer.prototype._putPresets = function(presets) {
    var that = this;
    var allPresetTypes = NetBeans_Preset.allTypes();
    for (p in presets) {
        var preset = presets[p];
        // row
        var row = document.createElement('tr');
        row.addEventListener('click', function() {
            that._rowSelected(this);
        }, true);
        // type
        var type = document.createElement('td');
        var typeSelect = document.createElement('select');
        for (i in allPresetTypes) {
            var presetType = allPresetTypes[i];
            var option = document.createElement('option');
            option.setAttribute('value', presetType.ident);
            if (preset.type === presetType) {
                option.setAttribute('selected', 'selected');
            }
            option.appendChild(document.createTextNode(presetType.title));
            typeSelect.appendChild(option);
        }
        typeSelect.addEventListener('change', function() {
            that._typeChanged(this);
        }, false);
        type.appendChild(typeSelect);
        row.appendChild(type);
        // name
        var title = document.createElement('td');
        var titleInput = document.createElement('input');
        titleInput.setAttribute('value', preset.title);
        titleInput.addEventListener('keyup', function() {
            that._titleChanged(this);
        }, false);
        title.appendChild(titleInput);
        row.appendChild(title);
        // width
        var witdh = document.createElement('td');
        var widthInput = document.createElement('input');
        widthInput.setAttribute('value', preset.width);
        widthInput.className = 'number';
        widthInput.addEventListener('keyup', function() {
            that._widthChanged(this);
        }, false);
        witdh.appendChild(widthInput);
        row.appendChild(witdh);
        // height
        var height = document.createElement('td');
        var heightInput = document.createElement('input');
        heightInput.setAttribute('value', preset.height);
        heightInput.className = 'number';
        heightInput.addEventListener('keyup', function() {
            that._heightChanged(this);
        }, false);
        height.appendChild(heightInput);
        row.appendChild(height);
        // toolbar
        var toolbar = document.createElement('td');
        toolbar.setAttribute('class', 'toolbar');
        var toolbarCheckbox = document.createElement('input');
        toolbarCheckbox.setAttribute('type', 'checkbox');
        if (preset.toolbar) {
            toolbarCheckbox.setAttribute('checked', 'checked');
        }
        toolbarCheckbox.addEventListener('click', function() {
            that._toolbarChanged(this);
        }, false);
        toolbar.appendChild(toolbarCheckbox);
        row.appendChild(toolbar);
        // append row
        this._rowContainer.appendChild(row);
        preset['_row'] = row;
        preset['_errors'] = [];
    }
}
// cleanup (remove presets from customizer)
NetBeans_PresetCustomizer.prototype._cleanUp = function() {
    this._presets = null;
    while (this._rowContainer.hasChildNodes()) {
        this._rowContainer.removeChild(this._rowContainer.firstChild);
    }
    this._activePreset = null;
    this._enableButtons();
}
// add a new preset
NetBeans_PresetCustomizer.prototype._addPreset = function() {
    var preset = new NetBeans_Preset(NetBeans_Preset.DESKTOP, 'New...', '800', '600', true, false);
    this._presets.push(preset);
    this._putPresets([preset]);
    this._enableButtons();
}
// remove the active preset
NetBeans_PresetCustomizer.prototype._removePreset = function() {
    // presets
    this._presets.splice(this._presets.indexOf(this._activePreset), 1);
    // ui
    this._rowContainer.removeChild(this._activePreset['_row']);
    this._activePreset = null;
    this._enableButtons();
}
// move the active preset up
NetBeans_PresetCustomizer.prototype._moveUpPreset = function() {
    // presets
    this._movePreset(this._activePreset, -1);
    // ui
    var row = this._activePreset['_row'];
    var before = row.previousSibling;
    this._rowContainer.removeChild(row);
    this._rowContainer.insertBefore(row, before);
    this._enableButtons();
}
// move the active preset down
NetBeans_PresetCustomizer.prototype._moveDownPreset = function() {
    // presets
    this._movePreset(this._activePreset, +1);
    // ui
    var row = this._activePreset['_row'];
    var after = row.nextSibling;
    this._rowContainer.removeChild(row);
    nbInsertAfter(row, after);
    this._enableButtons();
}
// move the preset up or down
NetBeans_PresetCustomizer.prototype._movePreset = function(preset, shift) {
    var index = this._presets.indexOf(preset);
    var tmp = this._presets[index];
    this._presets[index] = this._presets[index + shift];
    this._presets[index + shift] = tmp;
}
// save presets to the centrak storage and redraw them
NetBeans_PresetCustomizer.prototype._save = function() {
    this._hide();
    for (i in this._presets) {
        var preset = this._presets[i];
        delete preset['_row'];
        delete preset['_errors'];
    }
    NetBeans_Page.setPresets(this._presets);
    NetBeans_Page.redrawPresets();
    this._cleanUp();
}
// cancel customizer
NetBeans_PresetCustomizer.prototype._cancel = function() {
    this._hide();
    this._cleanUp();
}
// callback when row is selected
NetBeans_PresetCustomizer.prototype._rowSelected = function(row) {
    if (this._activePreset != null) {
        if (this._activePreset['_row'] === row) {
            // repeated click => ignore
            return;
        }
        this._activePreset['_row'].className = '';
    }
    // select
    var that = this;
    for (i in this._presets) {
        var preset = this._presets[i];
        if (preset['_row'] === row) {
            that._activePreset = preset;
            that._activePreset['_row'].className = 'active';
        }
    }
    this._enableButtons();
}
// enable/disable action buttons (based on the active preset)
NetBeans_PresetCustomizer.prototype._enableButtons = function() {
    this._enablePresetButtons();
    this._enableMainButtons();
}
// enable/disable preset buttons (based on the active preset)
NetBeans_PresetCustomizer.prototype._enablePresetButtons = function() {
    if (this._activePreset != null) {
        // any preset selected
        if (this._activePreset.internal) {
            this._removePresetButton.setAttribute('disabled', 'disabled');
        } else {
            this._removePresetButton.removeAttribute('disabled');
        }
        if (this._activePreset['_row'] !== this._rowContainer.firstChild) {
            this._moveUpPresetButton.removeAttribute('disabled');
        } else {
            this._moveUpPresetButton.setAttribute('disabled', 'disabled');
        }
        if (this._activePreset['_row'] !== this._rowContainer.lastChild) {
            this._moveDownPresetButton.removeAttribute('disabled');
        } else {
            this._moveDownPresetButton.setAttribute('disabled', 'disabled');
        }
    } else {
        this._removePresetButton.setAttribute('disabled', 'disabled');
        this._moveUpPresetButton.setAttribute('disabled', 'disabled');
        this._moveDownPresetButton.setAttribute('disabled', 'disabled');
    }
}
// enable/disable customizer buttons
NetBeans_PresetCustomizer.prototype._enableMainButtons = function() {
    var anyError = false;
    for (i in this._presets) {
        if (this._presets[i]['_errors'].length) {
            anyError = true;
            break;
        }
    }
    if (anyError) {
        this._okButton.setAttribute('disabled', 'disabled');
    } else {
        this._okButton.removeAttribute('disabled');
    }
}
// callback when preset type changes
NetBeans_PresetCustomizer.prototype._typeChanged = function(input) {
    if (this._activePreset == null) {
        // select change event fired before row click event => select the closest row
        var row = input;
        while (true) {
            row = row.parentNode;
            if (row.tagName.toLowerCase() == 'tr') {
                break;
            }
        }
        this._rowSelected(row);
    }
    this._activePreset.type = NetBeans_Preset.typeForIdent(input.value);
}
// callback when preset title changes
NetBeans_PresetCustomizer.prototype._titleChanged = function(input) {
    var that = this;
    this._checkField(input, 'title', function(value) {
        return that._validateNotEmpty(value);
    });
}
// callback when preset width changes
NetBeans_PresetCustomizer.prototype._widthChanged = function(input) {
    var that = this;
    this._checkField(input, 'width', function(value) {
        return that._validateNumber(value);
    });
}
// callback when preset height changes
NetBeans_PresetCustomizer.prototype._heightChanged = function(input) {
    var that = this;
    this._checkField(input, 'height', function(value) {
        return that._validateNumber(value);
    });
}
// callback when preset toolbar changes
NetBeans_PresetCustomizer.prototype._toolbarChanged = function(input) {
    this._activePreset.toolbar = input.checked;
}
// check whether the value is not empty
NetBeans_PresetCustomizer.prototype._validateNotEmpty = function(value) {
    return value != null && value.trim().length > 0;
}
// check whether the value is a number
NetBeans_PresetCustomizer.prototype._validateNumber = function(value) {
    return value != null && value.search(/^[1-9][0-9]*$/) != -1;
}
// check the given input, for the given key with the given validation callback
NetBeans_PresetCustomizer.prototype._checkField = function(input, key, validation) {
    var value = input.value;
    var index = this._activePreset['_errors'].indexOf(key);
    if (validation(value)) {
        nbRemoveCssClass(input, 'error');
        if (index != -1) {
            this._activePreset['_errors'].splice(index, 1);
        }
    } else {
        nbAddCssClass(input, 'error');
        if (index == -1) {
            this._activePreset['_errors'].push(key);
        }
    }
    this._activePreset[key] = value;
    this._enableMainButtons();
}

/**
 * "Disable" all elements in the page.
 *
 * Puts extra DIV over the page which "disables" any page elements.
 */
function NetBeans_Disabler() {
}
// show disabler
NetBeans_Disabler.on = function() {
    document.getElementById('disabler').style.visibility = 'visible';
}
// hide disabler
NetBeans_Disabler.off = function() {
    document.getElementById('disabler').style.visibility = 'hidden';
}

/*** ~Helpers ***/
// mirror function to element.insertBefore()
function nbInsertAfter(newElement, targetElement) {
	var parent = targetElement.parentNode;
	if (parent.lastchild == targetElement) {
		parent.appendChild(newElement);
    } else {
		parent.insertBefore(newElement, targetElement.nextSibling);
    }
}
// add CSS class to the given element
function nbAddCssClass(element, cssClass) {
    var className = element.className;
    if (className.indexOf(cssClass) != -1) {
        // already has this class
        return;
    }
    element.className = (element.className.trim() + ' ' + cssClass);
}
// remove CSS class to the given element
function nbRemoveCssClass(element, cssClass) {
    element.className = element.className.replace(cssClass, '').trim();
}

/*** ~Run app! ***/
var NetBeans_Page = new NetBeans_Page();
NetBeans_Page.initPage();
