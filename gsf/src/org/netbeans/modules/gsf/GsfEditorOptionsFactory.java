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

import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.modules.gsf.api.EditorOptions;
import org.netbeans.modules.gsf.api.EditorOptionsFactory;

/**
 *
 * @author tor
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.gsf.api.EditorOptionsFactory.class)
public class GsfEditorOptionsFactory implements EditorOptionsFactory {
    
    public EditorOptions get(String mimeType) {
        // Cache?
        return new GsfEditorOptions(mimeType);
    }

    private final class GsfEditorOptions extends EditorOptions {
        private final String mimeType;
        private final Preferences prefs;
        
        GsfEditorOptions(String mimeType) {
            this.mimeType = mimeType;
            this.prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
        }

        public int getTabSize() {
            return prefs.getInt(SimpleValueNames.TAB_SIZE, 8);
        }

        public boolean getExpandTabs() {
            return prefs.getBoolean(SimpleValueNames.EXPAND_TABS, true);
        }

        public int getSpacesPerTab() {
            return prefs.getInt(SimpleValueNames.SPACES_PER_TAB, 2);
        }

        public boolean getMatchBrackets() {
            return prefs.getBoolean(SimpleValueNames.COMPLETION_PAIR_CHARACTERS, true);
        }

        public int getRightMargin() {
            return prefs.getInt(SimpleValueNames.TEXT_LIMIT_WIDTH, 80);
        }
    }
}
