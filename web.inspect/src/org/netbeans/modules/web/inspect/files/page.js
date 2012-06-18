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

/*** ~Main object ***/

function NetBeans_Page() {
    this._presets = null;
    this._preset = null;
    // XXX how to create and gc the preset customizer properly?
    this._presetCustomizer = new NetBeans_PresetCustomizer();
}
NetBeans_Page.prototype.initPage = function() {
    this._registerEvents();
    this._showPresets();
}
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
NetBeans_Page.prototype.resetPage = function() {
    this._resizeFrame('100', '100', '%');
}
NetBeans_Page.prototype.showPresetCustomizer = function() {
    this._presetCustomizer.show(this._loadPresets()); // always use copy of presets!
}
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
NetBeans_Page.prototype.getPresets = function() {
    if (this._presets == null) {
        this._presets = this._loadPresets();
    }
    return this._presets;
}
NetBeans_Page.prototype.setPresets = function(presets) {
    this._presets = presets;
    this._savePresets();
}
NetBeans_Page.prototype.redrawPresets = function() {
    this._showPresets();
}
/*** ~Private ***/
NetBeans_Page.prototype._registerEvents = function() {
    var that = this;
    document.getElementById('autoPreset').addEventListener('click', function() {
        that.resizePage(null);
    }, false);
    document.getElementById('customizePresets').addEventListener('click', function() {
        that.showPresetCustomizer();
    }, false);
}
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
NetBeans_Page.prototype._loadPresets = function() {
    // XXX load presets from NB
    if (this._presets != null) {
        return this._presets.slice(0);
    }
    return [
        new NetBeans_Preset(NetBeans_Preset.DESKTOP, 'Desktop', '1440', '900', true, true),
        new NetBeans_Preset(NetBeans_Preset.TABLET, 'Tablet Landscape', '1039', '768', true, true),
        new NetBeans_Preset(NetBeans_Preset.TABLET, 'Tablet Portrait', '783', '1024', true, true),
        new NetBeans_Preset(NetBeans_Preset.SMARTPHONE, 'Smartphone Landscape', '495', '320', true, true),
        new NetBeans_Preset(NetBeans_Preset.SMARTPHONE, 'Smartphone Portrait', '335', '480', true, true)
    ];
}
NetBeans_Page.prototype._savePresets = function() {
    // XXX save presets back to NB
    console.error('Saving presets not implemented');
}
NetBeans_Page.prototype._resizeFrame = function(width, height, units) {
    var nbframe = document.getElementById('nbframe');
    var mask = document.getElementById('mask');
    nbframe.style.width = width + units;
    mask.style.marginTop = height + units;
}

/*** ~Inner classes ***/

function NetBeans_Preset(type, title, width, height, toolbar, internal) {
    this.type = type;
    this.title = title;
    this.width = width;
    this.height = height;
    this.toolbar = toolbar;
    this.internal = internal;
}
NetBeans_Preset.DESKTOP = {
    ident: 'DESKTOP',
    title: 'Desktop' // XXX i18n
};
NetBeans_Preset.TABLET = {
    ident: 'TABLET',
    title: 'Tablet'
};
NetBeans_Preset.SMARTPHONE = {
    ident: 'SMARTPHONE',
    title: 'Smartphone'
};
NetBeans_Preset.allTypes = function() {
    return [NetBeans_Preset.DESKTOP, NetBeans_Preset.TABLET, NetBeans_Preset.SMARTPHONE];
}
NetBeans_Preset.typeForIdent = function(ident) {
    var allTypes = NetBeans_Preset.allTypes();
    for (i in allTypes) {
        if (allTypes[i].ident == ident) {
            return allTypes[i];
        }
    }
    return null;
}

function NetBeans_PresetCustomizer() {
    this._container = null;
    this._rowContainer = null;
    this._removePresetButton = null;
    this._moveUpPresetButton = null;
    this._moveDownPresetButton = null;
    this._okButton = null;
    this._presets = null;
    this._activePreset = null;
}
NetBeans_PresetCustomizer.prototype.show = function(presets) {
    this._init();
    this._presets = presets;
    this._putPresets(this._presets);
    this._show();
}
/*** ~Private ***/
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
NetBeans_PresetCustomizer.prototype._show = function() {
    NetBeans_Disabler.on();
    var left = Math.max(window.innerWidth / 2 - this._container.clientWidth / 2, 0);
    var top = Math.max(window.innerHeight / 2 - this._container.clientHeight / 2, 0);
    this._container.style.left = left + 'px';
    this._container.style.top = top + 'px';
    this._container.style.visibility = 'visible';
}
NetBeans_PresetCustomizer.prototype._hide = function() {
    NetBeans_Disabler.off();
    this._container.style.visibility = 'hidden';
}
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
        preset['_error'] = false;
    }
}
NetBeans_PresetCustomizer.prototype._cleanUp = function() {
    this._presets = null;
    while (this._rowContainer.hasChildNodes()) {
        this._rowContainer.removeChild(this._rowContainer.firstChild);
    }
    this._activePreset = null;
    this._enableButtons();
}
NetBeans_PresetCustomizer.prototype._addPreset = function() {
    var preset = new NetBeans_Preset(NetBeans_Preset.DESKTOP, 'New...', '800', '600', true, false);
    this._presets.push(preset);
    this._putPresets([preset]);
    this._enableButtons();
}
NetBeans_PresetCustomizer.prototype._removePreset = function() {
    // presets
    this._presets.splice(this._presets.indexOf(this._activePreset), 1);
    // ui
    this._rowContainer.removeChild(this._activePreset['_row']);
    this._activePreset = null;
    this._enableButtons();
}
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
NetBeans_PresetCustomizer.prototype._movePreset = function(preset, shift) {
    var index = this._presets.indexOf(preset);
    var tmp = this._presets[index];
    this._presets[index] = this._presets[index + shift];
    this._presets[index + shift] = tmp;
}
NetBeans_PresetCustomizer.prototype._save = function() {
    this._hide();
    for (i in this._presets) {
        var preset = this._presets[i];
        delete preset['_row'];
        delete preset['_error'];
    }
    NetBeans_Page.setPresets(this._presets);
    NetBeans_Page.redrawPresets();
    this._cleanUp();
}
NetBeans_PresetCustomizer.prototype._cancel = function() {
    this._hide();
    this._cleanUp();
}
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
NetBeans_PresetCustomizer.prototype._enableButtons = function() {
    this._enablePresetButtons();
    this._enableMainButtons();
}
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
NetBeans_PresetCustomizer.prototype._enableMainButtons = function() {
    var anyError = false;
    for (i in this._presets) {
        if (this._presets[i]['_error']) {
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
NetBeans_PresetCustomizer.prototype._titleChanged = function(input) {
    var that = this;
    this._checkField(input, 'title', function(value) {
        return that._validateNotEmpty(value);
    });
}
NetBeans_PresetCustomizer.prototype._widthChanged = function(input) {
    var that = this;
    this._checkField(input, 'width', function(value) {
        return that._validateNumber(value);
    });
}
NetBeans_PresetCustomizer.prototype._heightChanged = function(input) {
    var that = this;
    this._checkField(input, 'height', function(value) {
        return that._validateNumber(value);
    });
}
NetBeans_PresetCustomizer.prototype._toolbarChanged = function(input) {
    this._activePreset.toolbar = input.checked;
}
NetBeans_PresetCustomizer.prototype._validateNotEmpty = function(value) {
    return value != null && value.trim().length > 0;
}
NetBeans_PresetCustomizer.prototype._validateNumber = function(value) {
    return value != null && !isNaN(parseFloat(value)) && isFinite(value);
}
NetBeans_PresetCustomizer.prototype._checkField = function(input, key, validation) {
    var value = input.value;
    if (validation(value)) {
        nbRemoveCssClass(input, 'error');
        this._activePreset['_error'] = false;
    } else {
        nbAddCssClass(input, 'error');
        this._activePreset['_error'] = true;
    }
    this._activePreset[key] = value;
    this._enableMainButtons();
}

function NetBeans_Disabler() {
}
NetBeans_Disabler.on = function() {
    document.getElementById('disabler').style.visibility = 'visible';
}
NetBeans_Disabler.off = function() {
    document.getElementById('disabler').style.visibility = 'hidden';
}


/*** ~Helpers ***/

function nbInsertAfter(newElement, targetElement) {
	var parent = targetElement.parentNode;
	if (parent.lastchild == targetElement) {
		parent.appendChild(newElement);
    } else {
		parent.insertBefore(newElement, targetElement.nextSibling);
    }
}

function nbAddCssClass(element, cssClass) {
    element.className = (element.className + ' ' + cssClass).trim();
}

function nbRemoveCssClass(element, cssClass) {
    element.className = element.className.replace(cssClass, '');
}

/*** ~Run app! ***/

var NetBeans_Page = new NetBeans_Page();
NetBeans_Page.initPage();
