/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.editor.filecreation;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import org.openide.loaders.ExtensionList;
import org.openide.util.NbPreferences;

/**
 * Settings facade for C/C++ Data Loaders extensions
 * 
 * @author Sergey Grinev
 */
public class ExtensionsSettings {
    private final String name;
    private final String defaultExtension;
    private final ExtensionList defaultExtensionsList;
    private ExtensionList savedExtensionsList;
    
    private ExtensionsSettings(String name, CndHandlableExtensions che) {
        DEFAULT_EXTENSION_PREFIX = "def-ext-"; //NOI18N
        defaultExtensionsList = che.getDefaultExtensionList();
        assert defaultExtensionsList.extensions().hasMoreElements();
        this.defaultExtension = defaultExtensionsList.extensions().nextElement();
        this.name = name;
    }
    
    private ExtensionsSettings(ExtensionsSettings es, String newDefExtPrefix) {
        DEFAULT_EXTENSION_PREFIX = newDefExtPrefix;
        this.defaultExtensionsList = es.defaultExtensionsList;
        this.name = es.name;
        this.defaultExtension = preferences.get(newDefExtPrefix + name, es.defaultExtension);
        this.savedExtensionsList = es.savedExtensionsList;
    }
    
    public static synchronized ExtensionsSettings getInstance(CndHandlableExtensions che) {
        String current = che.getSettingsName();
        ExtensionsSettings es = settingsAccessors.get(current);
        if (es == null) {
            es = new ExtensionsSettings(current, che);
            settingsAccessors.put(current, es);
        }
        return es;
    }
    
    private static final Map<String, ExtensionsSettings> settingsAccessors = new HashMap<String, ExtensionsSettings>();
    private static final Preferences preferences = NbPreferences.forModule(ExtensionsSettings.class);
    private final String DEFAULT_EXTENSION_PREFIX;
    private static final String EXTENSIONS_LIST_PREFIX = "ext-list-"; //NOI18N
    
    public String getDefaultExtension() {
        return preferences.get(DEFAULT_EXTENSION_PREFIX + name, defaultExtension);
    }

    public synchronized void setDefaultExtension(String value) {
        if (!isKnownExtension(value)) {
            ExtensionList current = getExtensionList();
            current.addExtension(value);
            setExtensionList(current);
        }
        preferences.put(DEFAULT_EXTENSION_PREFIX + name, value);
    }

    public synchronized ExtensionList getExtensionList() {
        if (savedExtensionsList == null) {
            String extensions = preferences.get(EXTENSIONS_LIST_PREFIX + name, null); //NOI18N
            if (extensions == null) {
                savedExtensionsList = defaultExtensionsList;
                return defaultExtensionsList;
            } else {
                ExtensionList l = new ExtensionList();
                String[] e = extensions.split(",");
                for (int i = 0; i < e.length; i++) {
                    l.addExtension(e[i]);
                }
                savedExtensionsList = l;
            }
        }
        return savedExtensionsList;
    }

    public synchronized void setExtensionList(ExtensionList value) {
        String st = "";
        Enumeration<String> list = value.extensions();
        while(list.hasMoreElements()) {
            if (st.length() > 0) {
                st += ",";
            }
            st += list.nextElement();
        }
        savedExtensionsList = value;
        preferences.put(EXTENSIONS_LIST_PREFIX + name, st);
    }

    public boolean isKnownExtension(String ext) {
        if (ext == null) {
            return false;
        }
        Enumeration<String> list = getExtensionList().extensions();
        while(list.hasMoreElements()) {
            if (ext.equals(list.nextElement())) {
                return true;
            }
        }
        return false;
    }

    ExtensionsSettings getSpecializedInstance(String newDefExtPrefix) {
        return new ExtensionsSettings(this, newDefExtPrefix);
    }
}
