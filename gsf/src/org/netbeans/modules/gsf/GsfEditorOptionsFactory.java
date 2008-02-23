/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.gsf;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.gsf.api.EditorOptions;
import org.netbeans.modules.gsf.api.EditorOptionsFactory;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.modules.editor.options.BaseOptions;
import org.openide.util.Lookup;

/**
 *
 * @author tor
 */
public class GsfEditorOptionsFactory implements EditorOptionsFactory {
    
    public EditorOptions get(String mimeType) {
        // Cache?
        return new GsfEditorOptions(mimeType);
    }

    private class GsfEditorOptions extends EditorOptions {
        private String mimeType;
        private BaseOptions options;
        
        GsfEditorOptions(String mimeType) {
            this.mimeType = mimeType;
            Lookup lookup = MimeLookup.getLookup(MimePath.parse(mimeType));
            options = lookup.lookup(BaseOptions.class);
        }

        public int getTabSize() {
            Object o = options.getSettingValue(SettingsNames.TAB_SIZE);
            if (o instanceof Integer) {
                return ((Integer)o).intValue();
            }
            return 8;
        }

        public boolean getExpandTabs() {
            Object o = options.getSettingValue(SettingsNames.EXPAND_TABS);
            if (o instanceof Boolean) {
                return ((Boolean)o).booleanValue();
            }
            return true;
        }

        public int getSpacesPerTab() {
            if (options != null) {
                Object o = options.getSettingValue(SettingsNames.SPACES_PER_TAB);
                if (o instanceof Integer) {
                    return ((Integer)o).intValue();
                }
            }
            return 2;
        }

        public boolean getMatchBrackets() {
            if (options != null) {
                Object o = options.getSettingValue(ExtSettingsNames.PAIR_CHARACTERS_COMPLETION);
                if (o instanceof Boolean) {
                    return ((Boolean)o).booleanValue();
                }
            }
            return true;
        }

        public int getRightMargin() {
            if (options != null) {
                Object o = options.getSettingValue(SettingsNames.TEXT_LIMIT_WIDTH);
                if (o instanceof Integer) {
                    return ((Integer)o).intValue();
                }
            }
            return 80;
        }
    }
}
