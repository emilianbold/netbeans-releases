/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.options.generaleditor;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.SettingsNames;
import org.netbeans.modules.editor.options.AllOptionsFolder;
import org.netbeans.modules.editor.options.BaseOptions;


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
        if (javaOptions == null)
            javaOptions = getOptions ("text/x-java");
        if (javaOptions == null) return;
        Map javaFoldingMap = javaOptions.getCodeFoldingProps ();

        javaFoldingMap.put (
            SettingsNames.CODE_FOLDING_ENABLE,
            Boolean.valueOf (showCodeFolding)
        );
        javaFoldingMap.put (
            "code-folding-collapse-import",
            Boolean.valueOf (foldImports)
        );
        javaFoldingMap.put (
            "code-folding-collapse-initial-comment",
            Boolean.valueOf (foldInitialComent)
        );
        javaFoldingMap.put (
            "code-folding-collapse-innerclass",
            Boolean.valueOf (foldInnerClasses)
        );
        javaFoldingMap.put (
            "code-folding-collapse-javadoc",
            Boolean.valueOf (foldJavaDoc)
        );
        javaFoldingMap.put (
            "code-folding-collapse-method",
            Boolean.valueOf (foldMethods)
        );
        javaOptions.setCodeFoldingProps (javaFoldingMap);
        
        Iterator it = AllOptionsFolder.getDefault ().getInstalledOptions ().
            iterator ();
        while (it.hasNext ()) {
            Class optionsClass = (Class) it.next ();
            BaseOptions baseOptions = (BaseOptions) BaseOptions.findObject 
                (optionsClass, true);
            Map m = baseOptions.getCodeFoldingProps ();
            m.put (
                SettingsNames.CODE_FOLDING_ENABLE,
                Boolean.valueOf (showCodeFolding)
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
    
    boolean isShowDeprecatedMembers () {
        return getParameter ("getShowDeprecatedMembers", true);
    }
    
    boolean isCompletionInstantSubstitution () {
        return getParameter ("getCompletionInstantSubstitution", true);
    }
    
    boolean isCompletionCaseSensitive () {
        return getParameter ("getCompletionCaseSensitive", true);
    }
    
    void setCompletionOptions (
        boolean pairCharacterCompletion,
        boolean completionAutoPopup,
        boolean showDeprecatedMembers,
        boolean completionInstantSubstitution,
        boolean completionCaseSensitive
    ) {
        Iterator it = AllOptionsFolder.getDefault ().getInstalledOptions ().
            iterator ();
        while (it.hasNext ()) {
            Class optionsClass = (Class) it.next ();
            BaseOptions baseOptions = (BaseOptions) BaseOptions.findObject 
                (optionsClass, true);
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
        }
    }
    
    
    // private helper methods ..................................................
    
    private boolean getFoldingParameter (
        String parameterName, 
        boolean defaultValue
    ) {
        BaseOptions options = getOptions ("text/x-java");
        if (options == null)
            options = getOptions ("text/plain");
        if (options == null) return defaultValue;
        Map javaFoldingMap = options.getCodeFoldingProps ();
        Boolean b = (Boolean) javaFoldingMap.get (parameterName);
        if (b != null) return b.booleanValue ();
        return defaultValue;
    }
    
    private BaseOptions javaOptions;
    
    private boolean getParameter (String parameterName, boolean defaultValue) {
        if (javaOptions == null) {
            javaOptions = getOptions ("text/x-java");
            if (javaOptions == null)
                javaOptions = getOptions ("text/plain");
        }
        if (javaOptions == null) return defaultValue;
        try {
            Method method = javaOptions.getClass ().getMethod (
                parameterName,
                new Class [0]
            );
            return ((Boolean) method.invoke (javaOptions, new Object [0])).
                booleanValue ();
        } catch (Exception ex) {
        }
        return defaultValue;
    }
    
    private static BaseOptions getOptions (String mimeType) {
        return (BaseOptions) MimeLookup.getLookup(MimePath.parse(mimeType)).lookup(BaseOptions.class);
    }
}


