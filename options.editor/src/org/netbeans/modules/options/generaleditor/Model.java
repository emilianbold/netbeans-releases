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

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.SettingsNames;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;


public class Model {

    // code folding options

    boolean isShowCodeFolding () {
        return getFoldingParameter (SettingsNames.CODE_FOLDING_ENABLE, true);
    }

    boolean isFoldImports () {
        return getFoldingParameter ("code-folding-collapse-import", false);
    }
    
    boolean isFoldInitialComment () {
        return getFoldingParameter ("code-folding-collapse-initial-comment", false);
    }
    
    boolean isFoldInnerClasses () {
        return getFoldingParameter ("code-folding-collapse-innerclass", false);
    }
    
    boolean isFoldJavaDocComments () {
        return getFoldingParameter ("code-folding-collapse-javadoc", false);
    }
    
    boolean isFoldMethods () {
        return getFoldingParameter ("code-folding-collapse-method", false);
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
            BaseOptions baseOptions = getOptions(mimeType);
            
            if (baseOptions == null) {
                continue;
            }
            
            Map<String, Boolean> m = baseOptions.getCodeFoldingProps ();

            m.put (
                SettingsNames.CODE_FOLDING_ENABLE,
                Boolean.valueOf (showCodeFolding)
            );
            m.put (
                "code-folding-collapse-import",
                Boolean.valueOf (foldImports)
            );
            m.put (
                "code-folding-collapse-initial-comment",
                Boolean.valueOf (foldInitialComent)
            );
            m.put (
                "code-folding-collapse-innerclass",
                Boolean.valueOf (foldInnerClasses)
            );
            m.put (
                "code-folding-collapse-javadoc",
                Boolean.valueOf (foldJavaDoc)
            );
            m.put (
                "code-folding-collapse-method",
                Boolean.valueOf (foldMethods)
            );
            
            baseOptions.setCodeFoldingProps (m);
        }
    }
    
    
    // code completion options 
    
    boolean isPairCharacterCompletion () {
        return getParameter ("getPairCharactersCompletion", true);
    }
    
    boolean isCompletionAutoPopup () {
        return getParameter ("getCompletionAutoPopup", true);
    }
    
    boolean isDocumentationAutoPopup () {
        return getParameter ("getJavaDocAutoPopup", true);
    }
    
    boolean isShowDeprecatedMembers () {
        return getParameter ("getShowDeprecatedMembers", true);
    }
    
    boolean isCompletionInstantSubstitution () {
        return getParameter ("getCompletionInstantSubstitution", true);
    }
    
    boolean isCompletionCaseSensitive () {
        return getParameter ("getCompletionCaseSensitive", true);
    }

    boolean isGuessMethodArguments () {
        return getParameter ("getGuessMethodArguments", true);
    }
    
    void setCompletionOptions (
        boolean pairCharacterCompletion,
        boolean completionAutoPopup,
        boolean documentationAutoPopup,
        boolean showDeprecatedMembers,
        boolean completionInstantSubstitution,
        boolean completionCaseSensitive,
        boolean guessMethodArguments
    ) {
        Set mimeTypes = EditorSettings.getDefault().getMimeTypes();
        for(Iterator i = mimeTypes.iterator(); i.hasNext(); ) {
            String mimeType = (String) i.next();
            BaseOptions baseOptions = getOptions(mimeType);

            if (baseOptions == null) {
                continue;
            }
            
            try {
                Method method = baseOptions.getClass ().getMethod (
                    "setPairCharactersCompletion",
                    new Class [] {Boolean.TYPE}
                );
                method.invoke (
                    baseOptions, 
                    new Object [] {Boolean.valueOf (pairCharacterCompletion)}
                );
            } catch (Exception ex) {
            }
            try {
                Method method = baseOptions.getClass ().getMethod (
                    "setCompletionAutoPopup",
                    new Class [] {Boolean.TYPE}
                );
                method.invoke (
                    baseOptions, 
                    new Object [] {Boolean.valueOf (completionAutoPopup)}
                );
            } catch (Exception ex) {
            }
            try {
                Method method = baseOptions.getClass ().getMethod (
                    "setJavaDocAutoPopup",
                    new Class [] {Boolean.TYPE}
                );
                method.invoke (
                    baseOptions, 
                    new Object [] {Boolean.valueOf (documentationAutoPopup)}
                );
            } catch (Exception ex) {
            }
            try {
                Method method = baseOptions.getClass ().getMethod (
                    "setShowDeprecatedMembers",
                    new Class [] {Boolean.TYPE}
                );
                method.invoke (
                    baseOptions, 
                    new Object [] {Boolean.valueOf (showDeprecatedMembers)}
                );
            } catch (Exception ex) {
            }
            try {
                Method method = baseOptions.getClass ().getMethod (
                    "setCompletionInstantSubstitution",
                    new Class [] {Boolean.TYPE}
                );
                method.invoke (
                    baseOptions, 
                    new Object [] {Boolean.valueOf (completionInstantSubstitution)}
                );
            } catch (Exception ex) {
            }
            try {
                Method method = baseOptions.getClass ().getMethod (
                    "setCompletionCaseSensitive",
                    new Class [] {Boolean.TYPE}
                );
                method.invoke (
                    baseOptions, 
                    new Object [] {Boolean.valueOf (completionCaseSensitive)}
                );
            } catch (Exception ex) {
            }
            try {
                Method method = baseOptions.getClass ().getMethod (
                    "setGuessMethodArguments",
                    new Class [] {Boolean.TYPE}
                );
                method.invoke (
                    baseOptions, 
                    new Object [] {Boolean.valueOf (guessMethodArguments)}
                );
            } catch (Exception ex) {
            }
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
    
    private boolean getFoldingParameter (
        String parameterName, 
        boolean defaultValue
    ) {
        boolean successfullRead = false;
        Set<String> mimeTypes = EditorSettings.getDefault().getAllMimeTypes();
        
        for(String mimeType : mimeTypes) {
            BaseOptions options = getOptions(mimeType);
            
            if (options == null) {
                continue;
            }
        
            Map foldingParams = options.getCodeFoldingProps();
            Boolean value = (Boolean) foldingParams.get(parameterName);
            
            if (value != null && value.booleanValue()) {
                return true;
            } else {
                successfullRead = true;
            }
        }
        
        return successfullRead ? false : defaultValue;
    }
    
    private boolean getParameter (String parameterName, boolean defaultValue) {
        boolean successfullRead = false;
        Set<String> mimeTypes = EditorSettings.getDefault().getAllMimeTypes();
        
        for(String mimeType : mimeTypes) {
            BaseOptions options = getOptions(mimeType);
            
            if (options == null) {
                continue;
            }
            
            try {
                Method method = options.getClass ().getMethod (
                    parameterName,
                    new Class [0]
                );
                boolean value = ((Boolean) method.invoke(options, new Object[0])).booleanValue();
                if (value) {
                    return true;
                } else {
                    successfullRead = true;
                }
            } catch (Exception ex) {
            }
        }
        
        return successfullRead ? false : defaultValue;
    }
    
    private static BaseOptions getOptions (String mimeType) {
        return MimeLookup.getLookup(MimePath.parse(mimeType)).lookup(BaseOptions.class);
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


