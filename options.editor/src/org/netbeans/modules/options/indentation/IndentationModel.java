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

package org.netbeans.modules.options.indentation;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;

import org.openide.text.IndentEngine;
import org.openide.util.Lookup;


class IndentationModel {

    private boolean         originalExpandedTabs;
    private boolean         originalAddStar;
    private boolean         originalNewLine;
    private boolean         originalSpace;
    private int             originalStatementIndent = 0;
    private int             originalIndent = 0;
    
    private boolean         changed = false;

    
    IndentationModel () {
        // save original values
        originalExpandedTabs = isExpandTabs ();
        originalAddStar = getJavaFormatLeadingStarInComment ();
        originalNewLine = getJavaFormatNewlineBeforeBrace ();
        originalSpace = getJavaFormatSpaceBeforeParenthesis ();
        originalStatementIndent = getJavaFormatStatementContinuationIndent ().
                intValue ();
        originalIndent = getSpacesPerTab ().intValue ();
    }
    
    boolean isExpandTabs () {
        return ((Boolean) getParameter ("isExpandTabs", Boolean.FALSE)).
            booleanValue ();
    }
    
    void setExpandTabs (boolean expand) {
        setParameter ("setExpandTabs", Boolean.valueOf (expand), Boolean.TYPE);
        updateChanged ();
    }
    
    boolean getJavaFormatLeadingStarInComment () {
        return ((Boolean) getParameter (
            "getJavaFormatLeadingStarInComment", Boolean.FALSE
        )).booleanValue ();
    }
    
    void setJavaFormatLeadingStarInComment (boolean star) {
        setParameter (
            "setJavaFormatLeadingStarInComment", 
            Boolean.valueOf (star), 
            Boolean.TYPE
        );
        updateChanged ();
    }
    
    boolean getJavaFormatSpaceBeforeParenthesis () {
        return ((Boolean) getParameter (
            "getJavaFormatSpaceBeforeParenthesis", Boolean.FALSE
        )).booleanValue ();
    }
    
    void setJavaFormatSpaceBeforeParenthesis (boolean space) {
        setParameter (
            "setJavaFormatSpaceBeforeParenthesis", 
            Boolean.valueOf (space), 
            Boolean.TYPE
        );
        updateChanged ();
    }
    
    boolean getJavaFormatNewlineBeforeBrace () {
        return ((Boolean) getParameter (
            "getJavaFormatNewlineBeforeBrace", Boolean.FALSE
        )).booleanValue ();
    }
    
    void setJavaFormatNewlineBeforeBrace (boolean newLine) {
        setParameter (
            "setJavaFormatNewlineBeforeBrace", 
            Boolean.valueOf (newLine), 
            Boolean.TYPE
        );
        updateChanged ();
    }
    
    Integer getJavaFormatStatementContinuationIndent () {
        return (Integer) getParameter (
            "getJavaFormatStatementContinuationIndent", new Integer (4)
        );
    }
    
    void setJavaFormatStatementContinuationIndent (Integer continuation) {
	if (continuation.intValue () > 0)
            setParameter (
                "setJavaFormatStatementContinuationIndent", 
                continuation, 
                Integer.TYPE
            );
        updateChanged ();
    }
    
    Integer getSpacesPerTab () {
        return (Integer) getParameter (
            "getSpacesPerTab", new Integer (4)
        );
    }
    
    void setSpacesPerTab (Integer spaces) {
	if (spaces.intValue () > 0)
            setParameter (
                "setSpacesPerTab", 
                spaces, 
                Integer.TYPE
            );
        updateChanged ();
    }

    boolean isChanged () {
        return changed;
    }

    void applyChanges() {
        applyParameterToAll ("setJavaFormatLeadingStarInComment", Boolean.valueOf (getJavaFormatLeadingStarInComment ()), Boolean.TYPE); //NOI18N
        applyParameterToAll ("setJavaFormatNewlineBeforeBrace", Boolean.valueOf (getJavaFormatNewlineBeforeBrace ()), Boolean.TYPE); //NOI18N
        applyParameterToAll ("setJavaFormatSpaceBeforeParenthesis", Boolean.valueOf (getJavaFormatSpaceBeforeParenthesis ()), Boolean.TYPE); //NOI18N
        applyParameterToAll ("setJavaFormatStatementContinuationIndent", getJavaFormatStatementContinuationIndent (), Integer.TYPE); //NOI18N
        applyParameterToAll ("setExpandTabs", Boolean.valueOf(isExpandTabs ()), Boolean.TYPE); //NOI18N
        applyParameterToAll ("setSpacesPerTab", getSpacesPerTab (), Integer.TYPE); //NOI18N
    }
    
    void revertChanges () {
        if (!changed) return; // no changes
        if (getJavaFormatLeadingStarInComment () != originalAddStar)
            setJavaFormatLeadingStarInComment (originalAddStar);
        if (getJavaFormatNewlineBeforeBrace () != originalNewLine)
            setJavaFormatNewlineBeforeBrace (originalNewLine);
        if (getJavaFormatSpaceBeforeParenthesis () != originalSpace)
            setJavaFormatSpaceBeforeParenthesis (originalSpace);
        if (isExpandTabs () != originalExpandedTabs)
            setExpandTabs (originalExpandedTabs);
        if (getJavaFormatStatementContinuationIndent ().intValue () != 
                originalStatementIndent &&
            originalStatementIndent > 0
        )
            setJavaFormatStatementContinuationIndent 
                (new Integer (originalStatementIndent));
        if (getSpacesPerTab ().intValue () != 
                originalIndent &&
            originalIndent > 0
        )
            setSpacesPerTab 
                (new Integer (originalIndent));
    }
    
    // private helper methods ..................................................

    private void updateChanged () {
        changed = 
                isExpandTabs () != originalExpandedTabs ||
                getJavaFormatLeadingStarInComment () != originalAddStar ||
                getJavaFormatNewlineBeforeBrace () != originalNewLine ||
                getJavaFormatSpaceBeforeParenthesis () != originalSpace ||
                getJavaFormatStatementContinuationIndent ().intValue () != 
                    originalStatementIndent ||
                getSpacesPerTab ().intValue () != originalIndent;
    }
    
    private IndentEngine javaIndentEngine = null;
    
    private IndentEngine getDefaultIndentEngine() {
        if (javaIndentEngine == null) {
            BaseOptions options = getOptions ("text/x-java");
            if (options == null) {
                options = getOptions ("text/plain");
            }
            javaIndentEngine = options == null ? null : options.getIndentEngine ();
        }
        return javaIndentEngine;
    }
    
    private Object getParameter (String parameterName, Object defaultValue) {
        IndentEngine eng = getDefaultIndentEngine();
        if (eng != null) {
            try {
                Method method = eng.getClass ().getMethod (
                    parameterName,
                    new Class [0]
                );
                return method.invoke (eng, new Object [0]);
            } catch (Exception ex) {
            }
        }
        return defaultValue;
    }
    
    private void setParameter (
        String parameterName, 
        Object parameterValue,
        Class parameterType
    ) {
        IndentEngine eng = getDefaultIndentEngine();
        if (eng != null) {
            try {
                Method method = eng.getClass ().getMethod (
                    parameterName,
                    new Class [] {parameterType}
                );
                method.invoke (eng, new Object [] {parameterValue});
            } catch (Exception ex) {
            }
        }
    }
    
    private void applyParameterToAll (
        String parameterName, 
        Object parameterValue,
        Class parameterType
    ) {
        Set mimeTypes = EditorSettings.getDefault().getMimeTypes();
        for(Iterator i = mimeTypes.iterator(); i.hasNext(); ) {
            String mimeType = (String) i.next();
            BaseOptions baseOptions = (BaseOptions) MimeLookup.getLookup(MimePath.parse(mimeType)).lookup(BaseOptions.class);
            IndentEngine indentEngine = baseOptions.getIndentEngine ();
            try {
                // HACK
                if (baseOptions.getClass ().getName ().equals ("org.netbeans.modules.java.editor.options.JavaOptions") &&
                    !indentEngine.getClass ().getName ().equals ("org.netbeans.modules.editor.java.JavaIndentEngine")
                ) {
                    Class javaIndentEngineClass = getClassLoader ().loadClass 
                        ("org.netbeans.modules.editor.java.JavaIndentEngine");
                    indentEngine = (IndentEngine) Lookup.getDefault ().lookup 
                        (javaIndentEngineClass);
                    baseOptions.setIndentEngine (indentEngine);
                }
                if (baseOptions.getClass ().getName ().equals ("org.netbeans.modules.web.core.syntax.JSPOptions") &&
                    !indentEngine.getClass ().getName ().equals ("org.netbeans.modules.web.core.syntax.JspIndentEngine")
                ) {
                    Class jspIndentEngineClass = getClassLoader ().loadClass 
                        ("org.netbeans.modules.web.core.syntax.JspIndentEngine");
                    indentEngine = (IndentEngine) Lookup.getDefault ().lookup 
                        (jspIndentEngineClass);
                    baseOptions.setIndentEngine (indentEngine);
                }
                Method method = indentEngine.getClass ().getMethod (
                    parameterName,
                    new Class [] {parameterType}
                );
                method.invoke (indentEngine, new Object [] {parameterValue});
            } catch (Exception ex) {
            }
        }
    }
    
    private ClassLoader classLoader;
    private ClassLoader getClassLoader () {
        if (classLoader == null)
            classLoader = (ClassLoader) Lookup.getDefault ().lookup 
                (ClassLoader.class);
        return classLoader;
    }
    
    private static BaseOptions getOptions (String mimeType) {
        return (BaseOptions) MimeLookup.getLookup(MimePath.parse(mimeType)).lookup(BaseOptions.class);
    }
}


