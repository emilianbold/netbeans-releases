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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.filecreation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import org.netbeans.modules.cnd.loaders.CndAbstractDataLoader;
import org.openide.util.NbPreferences;

/**
 *
 * @author Sergey Grinev
 */
class ExtensionsSettings {
    private final String name;
    private final String defaultExtension;
    private final List<String> defaultExtensionsList = new ArrayList<String>();
    
    private ExtensionsSettings(String name, CndAbstractDataLoader dataLoader) {
        Enumeration<String> def = dataLoader.getExtensions().extensions();
        while (def.hasMoreElements()) {
            defaultExtensionsList.add(def.nextElement());
        }
        assert defaultExtensionsList.size() > 0;
        this.defaultExtension = defaultExtensionsList.get(0);
        this.name = name;
    }
    
    public static synchronized ExtensionsSettings getInstance(CndAbstractDataLoader dataLoader) {
        String current = dataLoader.getRepresentationClassName();
        ExtensionsSettings es = settingsAccessors.get(current);
        if (es == null) {
            es = new ExtensionsSettings(current, dataLoader);
            settingsAccessors.put(current, es);
        }
        return es;
    }
    
    private static final Map<String, ExtensionsSettings> settingsAccessors = new HashMap<String, ExtensionsSettings>();
    private static final Preferences preferences = NbPreferences.forModule(ExtensionsSettings.class);
    private static final String DEFAULT_EXTENSION_PREFIX = "def-ext-"; //NOI18N
    private static final String EXTENSIONS_LIST_PREFIX = "ext-list-"; //NOI18N
    
    public String getDefaultExtension() {
        return preferences.get(DEFAULT_EXTENSION_PREFIX + name, defaultExtension);
    }

    public void setDefaultExtension(String value) {
        preferences.put(DEFAULT_EXTENSION_PREFIX + name, value);
    }

    public List<String> getExtensionList() {
        String extensions = preferences.get(EXTENSIONS_LIST_PREFIX + name, null); //NOI18N
        if (extensions == null) {
            return defaultExtensionsList;
        } else {
            return Arrays.asList(extensions.split(","));
        }
    }

    public void setExtensionList(List<String> value) {
        String st = "";
        for (String string : value) {
            if (st.length() > 0) {
                st += ",";
            }
            st += string;
        }

        preferences.put(EXTENSIONS_LIST_PREFIX + name, st);
    }
}
