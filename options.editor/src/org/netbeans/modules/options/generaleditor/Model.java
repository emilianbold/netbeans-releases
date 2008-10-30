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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.options.generaleditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;


public class Model {

    // code folding options

    boolean isShowCodeFolding () {
        return getParameter (SimpleValueNames.CODE_FOLDING_ENABLE, true);
    }

    boolean isFoldImports () {
        return getParameter("code-folding-collapse-import", false); //NOI18N
    }
    
    boolean isFoldInitialComment () {
        return getParameter("code-folding-collapse-initial-comment", false); //NOI18N
    }
    
    boolean isFoldInnerClasses () {
        return getParameter("code-folding-collapse-innerclass", false); //NOI18N
    }
    
    boolean isFoldJavaDocComments () {
        return getParameter ("code-folding-collapse-javadoc", false); //NOI18N
    }
    
    boolean isFoldMethods () {
        return getParameter("code-folding-collapse-method", false); //NOI18N
    }
    
    void setFoldingOptions (
        boolean showCodeFolding,
        boolean foldImports,
        boolean foldInitialComent,
        boolean foldInnerClasses,
        boolean foldJavaDoc,
        boolean foldMethods
    ) {
        Set<String> mimeTypes = EditorSettings.getDefault().getAllMimeTypes();
        for(String mimeType : mimeTypes) {
            Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
            
            prefs.putBoolean(SimpleValueNames.CODE_FOLDING_ENABLE, Boolean.valueOf(showCodeFolding));
            prefs.putBoolean("code-folding-collapse-import", foldImports); //NOI18N
            prefs.putBoolean("code-folding-collapse-initial-comment", foldInitialComent); //NOI18N
            prefs.putBoolean("code-folding-collapse-innerclass", foldInnerClasses); //NOI18N
            prefs.putBoolean("code-folding-collapse-javadoc", foldJavaDoc); //NOI18N
            prefs.putBoolean("code-folding-collapse-method", foldMethods); //NOI18N
        }
    }
    
    Boolean isCamelCaseJavaNavigation() {
        Preferences p = getJavaModulePreferenes();
        if ( p == null ) {
            return null;
        }
        return p.getBoolean("useCamelCaseStyleNavigation", true) ? Boolean.TRUE : Boolean.FALSE; // NOI18N
    }
    
    void setCamelCaseNavigation(boolean value) {
        Preferences p = getJavaModulePreferenes();
        if ( p == null ) {
            return;
        }
        p.putBoolean("useCamelCaseStyleNavigation", value); // NOI18N
    }
        
    // private helper methods ..................................................
    
    private static final List<String> PRIVILEDGED_MIME_TYPES = Arrays.asList(new String [] {
        "text/x-java", //NOI18N
        "text/x-c++", //NOI18N
        "text/x-c", //NOI18N
        "text/x-ruby", //NOI18N
        "text/x-php5", //NOI18N
    });
    
    private boolean getParameter(String parameterName, boolean defaultValue) {
        Set<String> mimeTypes = EditorSettings.getDefault().getAllMimeTypes();
        List<String> list = new ArrayList<String>(PRIVILEDGED_MIME_TYPES);
        list.addAll(mimeTypes);
        
        for(String mimeType : list) {
            Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
            
            String value = prefs.get(parameterName, null);
            if (value != null) {
                return prefs.getBoolean(parameterName, false);
            }
        }
        
        return defaultValue;
    }
    
    private Preferences getJavaModulePreferenes() {
        try {
            ClassLoader cl = Lookup.getDefault().lookup(ClassLoader.class);
            Class accpClass = cl.loadClass("org.netbeans.modules.editor.java.AbstractCamelCasePosition"); // NOI18N
            if (accpClass == null) {
                return null;
            }
            return NbPreferences.forModule(accpClass);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
    
}


