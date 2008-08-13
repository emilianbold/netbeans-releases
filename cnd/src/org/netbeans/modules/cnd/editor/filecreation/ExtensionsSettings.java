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

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Settings facade for C/C++ Data Loaders extensions
 * 
 * @author Sergey Grinev
 */
public class ExtensionsSettings {

    private final String name;
    private CndExtensionList savedExtensionsList;

    private ExtensionsSettings(String name, CndHandlableExtensions che) {
        this.name = name;
        defaultExtensionKey = "def-ext-" + name; //NOI18N

        String extensions = preferences.get(EXTENSIONS_LIST_PREFIX + name, null); //NOI18N
        if (extensions == null) {
            savedExtensionsList = (CndExtensionList) che.getDefaultExtensionList();
            String dext = savedExtensionsList.extensions().nextElement();
            setDefaultExtension(dext, false);
        } else {
            savedExtensionsList = new CndExtensionList(extensions.split(DELIMITER));
        }
        assert savedExtensionsList.extensions().hasMoreElements();

    }

//    // shortcut for MIMEResolver
//    private ExtensionsSettings(String name) {
//        this.name = name;
//        defaultExtensionKey = "def-ext-" + name; //NOI18N
//
//        String extensions = preferences.get(EXTENSIONS_LIST_PREFIX + name, null); //NOI18N
//        if (extensions == null) {
//            String[] ar = getCndDefaultExtensions(name);
//            savedExtensionsList = arrayToExtensionList(ar);
//            setDefaultExtension(ar[0], false);
//        } else {
//            savedExtensionsList = new CndExtensionList(extensions.split(DELIMITER));
//        }
//        assert savedExtensionsList.extensions().hasMoreElements();
//    }

    /**
     * backdoor to speedup MIMEResolver
     */
    public static boolean isRegistered(String extension, String esName) {
        String extensions = preferences.get(EXTENSIONS_LIST_PREFIX + esName, null);
        String[] ar = extensions == null ? getCndDefaultExtensions(esName) : extensions.split(DELIMITER);
        for (int i = 0; i < ar.length; i++) {
            String string = ar[i];
            if (string.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    private ExtensionsSettings(ExtensionsSettings es, String newDefExtPrefix) {
        this.name = es.name;
        defaultExtensionKey = newDefExtPrefix + name;
        this.savedExtensionsList = es.savedExtensionsList;
        String dext = getDefaultExtension(es.getDefaultExtension());
        setDefaultExtension(isKnownExtension(dext) ? dext : es.getDefaultExtension());
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
    private final String defaultExtensionKey;
    private static final String EXTENSIONS_LIST_PREFIX = "ext-list-"; //NOI18N
    private static final String DELIMITER = ","; //NOI18N

    public String getDefaultExtension() {
        String ext = getDefaultExtension("");
        assert ext.length() > 0; // def ext must be set
        return ext;
    }

    private String getDefaultExtension(String def) {
        return preferences.get(defaultExtensionKey, def);
    }

    public synchronized void setDefaultExtension(String value) {
        setDefaultExtension(value, true);
    }
    
    public void addExtension(String value) {
        if (!isKnownExtension(value)) {
            CndExtensionList el = getExtensionList();
            el.addExtension(value);
            setExtensionList(el);
        }
    }

    private void setDefaultExtension(String value, boolean addIfMissed) {
        if (addIfMissed) { 
            addExtension(value);
        }
        preferences.put(defaultExtensionKey, value);
    }

    public synchronized CndExtensionList getExtensionList() {
        return savedExtensionsList;
    }

    public synchronized void setExtensionList(CndExtensionList value) {
        String st = "";
        Enumeration<String> list = value.extensions();
        assert list.hasMoreElements(); // setting empty extension list is an error and should be verified on higher level
        while (list.hasMoreElements()) {
            if (st.length() > 0) {
                st += DELIMITER;
            }
            st += list.nextElement();
        }
        savedExtensionsList = value;
        if (!isKnownExtension(getDefaultExtension())) {
            setDefaultExtension(savedExtensionsList.extensions().nextElement(), false);
        }
        preferences.put(EXTENSIONS_LIST_PREFIX + name, st);
    }

    public boolean isKnownExtension(String ext) {
        if (ext == null) {
            return false;
        }
        return getExtensionList().isRegistered(ext);
    }

    ExtensionsSettings getSpecializedInstance(String newDefExtPrefix) {
        return new ExtensionsSettings(this, newDefExtPrefix);
    }

    private static CndExtensionList arrayToExtensionList(String[] ar) {
        return new CndExtensionList(ar);
    }
    public static final String HEADER = "CndHeader"; //NOI18N
    public static final String C_FILE = "CndCFile"; //NOI18N
    public static final String CPP_FILE = "CndCppFile"; //NOI18N
    public static final String MAKEFILE = "CndMakefile"; //NOI18N
    public static final String SHELL = "CndShellFile"; //NOI18N
    public static final String FORTRAN = "CndFortranFile"; //NOI18N
    public static final String ASM = "CndAsmFile"; //NOI18N

    private static final Map<String, String[]> defaultExtensions = new HashMap<String, String[]>();
    
    static {
        defaultExtensions.put(HEADER, new String[]{"h", "H", "hpp", "hxx", "SUNWCCh", "tcc"}); //NOI18N
        defaultExtensions.put(C_FILE, new String[]{"c", "i", "m"}); //NOI18N
        defaultExtensions.put(CPP_FILE, new String[]{"cpp", "cc", "c++", "cxx", "C", "mm"}); //NOI18N
        defaultExtensions.put(MAKEFILE, new String[]{"mk"}); //NOI18N
        defaultExtensions.put(ASM, new String[]{"s", "as", "asm"}); //NOI18N
        defaultExtensions.put(SHELL, new String[]{"bash", "csh", "ksh", "sh", "zsh", "bat", "cmd"}); //NOI18N
        defaultExtensions.put(FORTRAN, new String[]{"f", "F", "f90", "F90", "f95", "F95", "f03", "F03", "for", "il", "mod"}); //NOI18N
    }

    private static String[] getCndDefaultExtensions(String id) {
        return defaultExtensions.get(id);
    }

    public static CndExtensionList getDefaultExtensionList(String id) {
        return arrayToExtensionList(getCndDefaultExtensions(id));
    }
}
