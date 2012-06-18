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

    presets: null,
    preset: null,


    initPage: function() {
        this._showPresets();
    },

    resizePage: function(preset) {
        var data = this.getPreset(preset);
        if (data == null) {
            console.error('Preset [' + preset + '] not found.');
            return;
        }
        var nbframe = document.getElementById('nbframe');
        var mask = document.getElementById('mask');
        nbframe.style.width = data['width'];
        mask.style.marginTop = data['height'];
    },

    getPresets: function() {
        if (this.presets == null) {
            this.presets = this._loadPresets();
        }
        return this.presets;
    },

    getPreset: function(preset) {
        if (preset == undefined) {
            return this.preset;
        }
        var tmp = this.getPresets()[preset];
        if (tmp == undefined) {
            return null;
        }
        this.preset = tmp;
        return this.preset;
    },

    /*** ~Private ***/

    _showPresets: function() {
        var s = '';
        var presets = this.getPresets();
        for (p in presets) {
            var preset = presets[p];
            s += '<a href="#" onclick="NetBeans.resizePage(\'' + preset.name + '\'); return false;\">' + preset.title + '</a>';
        }
        var resizer = document.getElementById('resizer');
        resizer.innerHTML = s;
    },

    _loadPresets: function() {
        // XXX load presets from NB
        var _presets = [
            new NetBeans_Preset('auto', 'Auto', '100%', '100%', true, true),
            new NetBeans_Preset('desktop', 'Desktop', '1440px', '900px', true, true),
            new NetBeans_Preset('tablet-landscape', 'Tablet Landscape', '1039px', '768px', true, true),
            new NetBeans_Preset('tablet-portrait', 'Tablet Portrait', '783px', '1024px', true, true),
            new NetBeans_Preset('smartphone-landscape', 'Smartphone Landscape', '495px', '320px', true, true),
            new NetBeans_Preset('smartphone-portrait', 'Smartphone Portrait', '335px', '480px', true, true)
        ];
        var presets = {};
        for (i = 0; i < _presets.length; ++i) {
            var p = _presets[i];
            presets[p.name] = p;
        }
        return presets;
    },

    _savePreset: function(preset) {
        // XXX save presets back to NB
        alert('Saving preset: ' + preset);
    }

};

/*** ~Inner classes ***/

function NetBeans_Preset(name, title, width, height, visible, internal) {
    this.name = name;
    this.title = title;
    this.width = width;
    this.height = height;
    this.visible = visible;
    this.internal = internal;
}
