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

var NetBeans = {

    _presets: null,
    _preset: null,
    _presetCustomizer: new NetBeans_PresetCustomizer(),


    initPage: function() {
        this._registerEvents();
        this._showPresets();
    },

    resizePage: function(preset) {
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
    },

    resetPage: function() {
        this._resizeFrame('100', '100', '%');
    },

    showPresetCustomizer: function() {
        this._presetCustomizer.show(this._loadPresets()); // always use copy of presets!
    },

    getPreset: function(preset) {
        if (preset == undefined) {
            return this._preset;
        }
        var tmp = this.getPresets()[preset];
        if (tmp == undefined) {
            return null;
        }
        this._preset = tmp;
        return this._preset;
    },

    getPresets: function() {
        if (this._presets == null) {
            this._presets = this._loadPresets();
        }
        return this._presets;
    },

    setPresets: function(presets) {
        this._presets = presets;
        this._savePresets();
    },

    redrawPresets: function() {
        this._showPresets();
    },


    /*** ~Private ***/

    _registerEvents: function() {
        var that = this;
        document.getElementById('autoPreset').addEventListener('click', function() {
            that.resizePage(null);
        }, false);
        document.getElementById('customizePresets').addEventListener('click', function() {
            that.showPresetCustomizer();
        }, false);
    },

    _showPresets: function() {
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
            button.setAttribute('onclick', 'NetBeans.resizePage(' + p + '); return false;');
            button.appendChild(document.createTextNode(preset.title));
            resizer.appendChild(button);
        }
    },

    _loadPresets: function() {
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
    },

    _savePresets: function() {
        // XXX save presets back to NB
        console.error('Saving presets not implemented');
    },

    _resizeFrame: function(width, height, units) {
        var nbframe = document.getElementById('nbframe');
        var mask = document.getElementById('mask');
        nbframe.style.width = width + units;
        mask.style.marginTop = height + units;
    }

};

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


    this.show = function(presets) {
        this._init();
        this._presets = presets;
        this._putPresets(this._presets);
        this._show();
    }

    /*** ~Private ***/

    this._init = function() {
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

    this._show = function() {
        this._container.style.display = 'block';
    }

    this._hide = function() {
        this._container.style.display = 'none';
    }

    this._registerEvents = function() {
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

    this._putPresets = function(presets) {
        var that = this;
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
            NetBeans_Preset.allTypes().forEach(function(type) {
                var option = document.createElement('option');
                option.setAttribute('value', type.ident);
                if (preset.type === type) {
                    option.setAttribute('selected', 'selected');
                }
                option.appendChild(document.createTextNode(type.title));
                typeSelect.appendChild(option);
            });
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

    this._cleanUp = function() {
        this._presets = null;
        while (this._rowContainer.hasChildNodes()) {
            this._rowContainer.removeChild(this._rowContainer.firstChild);
        }
        this._activePreset = null;
        this._enableButtons();
    }

    this._addPreset = function() {
        var preset = new NetBeans_Preset(NetBeans_Preset.DESKTOP, 'New...', '800', '600', true, false);
        this._presets.push(preset);
        this._putPresets([preset]);
        this._enableButtons();
    }

    this._removePreset = function() {
        // presets
        this._presets.splice(this._presets.indexOf(this._activePreset), 1);
        // ui
        this._rowContainer.removeChild(this._activePreset['_row']);
        this._activePreset = null;
        this._enableButtons();
    }

    this._moveUpPreset = function() {
        // presets
        this._movePreset(this._activePreset, -1);
        // ui
        var row = this._activePreset['_row'];
        var before = row.previousSibling;
        this._rowContainer.removeChild(row);
        this._rowContainer.insertBefore(row, before);
        this._enableButtons();
    }

    this._moveDownPreset = function() {
        // presets
        this._movePreset(this._activePreset, +1);
        // ui
        var row = this._activePreset['_row'];
        var after = row.nextSibling;
        this._rowContainer.removeChild(row);
        insertAfter(row, after);
        this._enableButtons();
    }

    this._movePreset = function(preset, shift) {
        var index = this._presets.indexOf(preset);
        var tmp = this._presets[index];
        this._presets[index] = this._presets[index + shift];
        this._presets[index + shift] = tmp;
    }

    this._save = function() {
        this._hide();
        this._presets.forEach(function(preset) {
            delete preset['_row'];
            delete preset['_error'];
        });
        NetBeans.setPresets(this._presets);
        NetBeans.redrawPresets();
        this._cleanUp();
    }

    this._cancel = function() {
        this._hide();
        this._cleanUp();
    }

    this._rowSelected = function(row) {
        if (this._activePreset != null) {
            if (this._activePreset['_row'] === row) {
                // repeated click => ignore
                return;
            }
            this._activePreset['_row'].className = '';
        }
        // select
        var that = this;
        this._presets.forEach(function(preset) {
            if (preset['_row'] === row) {
                that._activePreset = preset;
                that._activePreset['_row'].className = 'active';
            }
        });
        this._enableButtons();
    }

    this._enableButtons = function() {
        this._enablePresetButtons();
        this._enableMainButtons();
    }

    this._enablePresetButtons = function() {
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

    this._enableMainButtons = function() {
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

    this._typeChanged = function(input) {
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

    this._titleChanged = function(input) {
        var that = this;
        this._checkField(input, 'title', function(value) {
            return that._validateNotEmpty(value);
        });
    }

    this._widthChanged = function(input) {
        var that = this;
        this._checkField(input, 'width', function(value) {
            return that._validateNumber(value);
        });
    }

    this._heightChanged = function(input) {
        var that = this;
        this._checkField(input, 'height', function(value) {
            return that._validateNumber(value);
        });
    }

    this._toolbarChanged = function(input) {
        this._activePreset.toolbar = input.checked;
    }

    this._validateNotEmpty = function(value) {
        return value != null && value.trim().length > 0;
    }

    this._validateNumber = function(value) {
        return value != null && !isNaN(parseFloat(value)) && isFinite(value);
    }

    this._checkField = function(input, key, validation) {
        var value = input.value;
        if (validation(value)) {
            removeCssClass(input, 'error');
            this._activePreset['_error'] = false;
        } else {
            addCssClass(input, 'error');
            this._activePreset['_error'] = true;
        }
        this._activePreset[key] = value;
        this._enableMainButtons();
    }

}


/*** ~Helpers ***/
function insertAfter(newElement, targetElement) {
	var parent = targetElement.parentNode;
	if (parent.lastchild == targetElement) {
		parent.appendChild(newElement);
    } else {
		parent.insertBefore(newElement, targetElement.nextSibling);
    }
}

function addCssClass(element, cssClass) {
    element.className = (element.className + ' ' + cssClass).trim();
}

function removeCssClass(element, cssClass) {
    element.className = element.className.replace(cssClass, '');
}


/*** ~Run ***/
window.onload = function() {
    NetBeans.initPage();
};
