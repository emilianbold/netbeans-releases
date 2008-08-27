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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.editing;

import java.util.EnumSet;
import java.util.Set;
import java.util.prefs.Preferences;
import org.mozilla.nb.javascript.Context;
import org.netbeans.modules.javascript.editing.options.JsOptionsController;
import org.openide.util.NbPreferences;

/**
 * This class manages a user-chosen selection of browser versions;
 * it looks up and persists this data, and answers browser-compatibility
 * queries.
 * 
 * @author Tor Norbye
 */
public class SupportedBrowsers {

    static {
        JsOptionsController.Accessor.DEFAULT = new JsOptionsController.Accessor() {

            @Override
            public void setLanguageVersion(SupportedBrowsers supported, int version) {
                supported.setLanguageVersion(version);
            }

            @Override
            public void setSupported(SupportedBrowsers supported, EnumSet<BrowserVersion> versions) {
                supported.setSupported(versions);
            }
        };
    }

    private static final String SUPPORTED_KEY = "supported";
    private static final String LANGUAGE_KEY = "language";
    private static final String BROWSERS = "browsers";
    private Set<BrowserVersion> supported;
    private int languageVersion;
    
    static SupportedBrowsers instance = new SupportedBrowsers();
    
    public static SupportedBrowsers getInstance() {
        return instance;
    }
    
    SupportedBrowsers() {
        final Preferences preferences = getPreferences();
        String browsers = preferences.get(SUPPORTED_KEY, null);
        if (browsers == null || browsers.length() == 0) {
            supported = EnumSet.allOf(BrowserVersion.class);
        } else {
            supported = BrowserVersion.fromFlags(browsers);
        }
        String languageStr = preferences.get(LANGUAGE_KEY, null);
        if (languageStr == null || "default".equals(languageStr)) { // NOI18N
            languageVersion = Context.VERSION_DEFAULT;
        } else {
            if ("1.0".equals(languageStr)) { // NOI18N
                languageVersion = Context.VERSION_1_0;
            } else if ("1.1".equals(languageStr)) { // NOI18N
                languageVersion = Context.VERSION_1_1;
            } else if ("1.2".equals(languageStr)) { // NOI18N
                languageVersion = Context.VERSION_1_2;
            } else if ("1.3".equals(languageStr)) { // NOI18N
                languageVersion = Context.VERSION_1_3;
            } else if ("1.4".equals(languageStr)) { // NOI18N
                languageVersion = Context.VERSION_1_4;
            } else if ("1.5".equals(languageStr)) { // NOI18N
                languageVersion = Context.VERSION_1_5;
            } else if ("1.6".equals(languageStr)) { // NOI18N
                languageVersion = Context.VERSION_1_6;
            } else if ("1.7".equals(languageStr)) { // NOI18N
                languageVersion = Context.VERSION_1_7;
            } else if ("1.8".equals(languageStr)) { // NOI18N
                languageVersion = Context.VERSION_1_8;
            } else {
                languageVersion = Context.VERSION_DEFAULT;
            }
        }
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule(SupportedBrowsers.class).node(BROWSERS);
    }
    
    public boolean isSupported(BrowserVersion version) {
        return supported.contains(version);
    }
    
    public boolean isSupported(EnumSet<BrowserVersion> versions) {
        return versions.containsAll(supported);
    }

    void setSupported(EnumSet<BrowserVersion> versions) {
        if (supported.equals(versions)) {
            return;
        }
        supported = versions;

        String flags = BrowserVersion.toFlags(versions);
        getPreferences().put(SUPPORTED_KEY,flags);
    }
    
    /** Return one of the VERSION_ constants in {@link org.mozilla.nb.javascript.Context}. */
    public int getLanguageVersion() {
        return languageVersion;
    }
    
    void setLanguageVersion(int version) {
        this.languageVersion = version;
        
        String value;
        switch (version) {
            case Context.VERSION_1_0:
                value = "1.0"; break; // NOI18N
            case Context.VERSION_1_1:
                value = "1.1"; break; // NOI18N
            case Context.VERSION_1_2:
                value = "1.2"; break; // NOI18N
            case Context.VERSION_1_3:
                value = "1.3"; break; // NOI18N
            case Context.VERSION_1_4:
                value = "1.4"; break; // NOI18N
            case Context.VERSION_1_5:
                value = "1.5"; break; // NOI18N
            case Context.VERSION_1_6:
                value = "1.6"; break; // NOI18N
            case Context.VERSION_1_7:
                value = "1.7"; break; // NOI18N
            case Context.VERSION_1_8:
                value = "1.8"; break; // NOI18N
            case Context.VERSION_DEFAULT:
                value = "default"; break; // NOI18N
            default:
                assert false : version;
                return;
        }
        
        getPreferences().put(LANGUAGE_KEY, value);
    }
}
