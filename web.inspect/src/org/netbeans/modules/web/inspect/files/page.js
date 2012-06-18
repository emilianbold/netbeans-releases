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
        this._presetCustomizer.show(this._loadPresets()); // always use copy!
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
            var button = document.createElement('a');
            button.setAttribute('href', '#');
            button.setAttribute('onclick', 'NetBeans.resizePage(' + p + '); return false;');
            button.appendChild(document.createTextNode(preset.title));
            resizer.appendChild(button);
        }
    },

    _loadPresets: function() {
        // XXX load presets from NB
        return [
            new NetBeans_Preset('Desktop', '1440', '900', true, true),
            new NetBeans_Preset('Tablet Landscape', '1039', '768', true, true),
            new NetBeans_Preset('Tablet Portrait', '783', '1024', true, true),
            new NetBeans_Preset('Smartphone Landscape', '495', '320', true, true),
            new NetBeans_Preset('Smartphone Portrait', '335', '480', true, true)
        ];
    },

    _savePresets: function() {
        // XXX save presets back to NB
        alert('Saving presets');
    },

    _resizeFrame: function(width, height, units) {
        var nbframe = document.getElementById('nbframe');
        var mask = document.getElementById('mask');
        nbframe.style.width = width + units;
        mask.style.marginTop = height + units;
    }

};

/*** ~Inner classes ***/

function NetBeans_Preset(title, width, height, toolbar, internal) {
    this.title = title;
    this.width = width;
    this.height = height;
    this.toolbar = toolbar;
    this.internal = internal;
}

function NetBeans_PresetCustomizer() {

    this._container = null;
    this._rowContainer = null;
    this._presets = null;


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
        document.getElementById('presetCustomizerOk').addEventListener('click', function() {
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
        for (p in presets) {
            var preset = presets[p];
            // row
            var row = document.createElement('tr');
            // type
            var type = document.createElement('td');
            type.appendChild(document.createTextNode('???'));
            row.appendChild(type);
            // name
            var title = document.createElement('td');
            title.appendChild(document.createTextNode(preset.title));
            row.appendChild(title);
            // width
            var witdh = document.createElement('td');
            witdh.appendChild(document.createTextNode(preset.width));
            row.appendChild(witdh);
            // height
            var height = document.createElement('td');
            height.appendChild(document.createTextNode(preset.height));
            row.appendChild(height);
            // toolbar
            var toolbar = document.createElement('td');
            toolbar.appendChild(document.createTextNode(preset.toolbar ? 'yes' : 'no'));
            row.appendChild(toolbar);
            // append row
            this._rowContainer.appendChild(row);
        }
    }

    this._cleanPresets = function() {
        this._presets = null;
        while (this._rowContainer.hasChildNodes()) {
            this._rowContainer.removeChild(this._rowContainer.firstChild);
        }
    }

    this._addPreset = function() {
        var preset = new NetBeans_Preset('New...', '800', '600', true, false);
        this._presets.push(preset);
        this._putPresets([preset]);
    }

    this._save = function() {
        this._hide();
        NetBeans.setPresets(this._presets);
        NetBeans.redrawPresets();
        this._cleanPresets();
    }

    this._cancel = function() {
        this._hide();
        this._cleanPresets();
    }

}

/*** ~Run ***/
window.onload = function() {
    NetBeans.initPage();
};
