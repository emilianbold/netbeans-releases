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
import java.util.Collection;
import java.util.HashSet;
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
    private int             originalSpacesPerTab = 0;
    private int             originalTabSize = 0;
    private int             originalRightMargin = 0;
    
    private boolean         changed = false;

    
    IndentationModel () {
        // save original values
        originalExpandedTabs = isExpandTabs ();
        originalSpacesPerTab = getSpacesPerTab ().intValue ();
        originalTabSize = getTabSize().intValue ();
        originalRightMargin = getRightMargin().intValue ();
    }
    
    boolean isExpandTabs () {
        return ((Boolean) getParameter ("isExpandTabs", Boolean.FALSE)).
            booleanValue ();
    }
    
    void setExpandTabs (boolean expand) {
        setParameter ("setExpandTabs", Boolean.valueOf (expand), Boolean.TYPE);
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

    Integer getTabSize () {
        BaseOptions options = getExampleBaseOptions();
        if (options != null) {
            return options.getTabSize();
        } else {
            return 4;
        }
    }
    
    void setTabSize (Integer size) {
	if (size.intValue () > 0) {
            BaseOptions options = getExampleBaseOptions();
            if (options != null) {
                options.setTabSize(size);
            }
        }
        updateChanged ();
    }

    Integer getRightMargin () {
        BaseOptions options = getExampleBaseOptions();
        if (options != null) {
            return options.getTextLimitWidth();
        } else {
            return 120;
        }
    }
    
    void setRightMargin (Integer margin) {
	if (margin.intValue () > 0) {
            BaseOptions options = getExampleBaseOptions();
            if (options != null) {
                options.setTextLimitWidth(margin);
            }
        }
        updateChanged ();
    }

    boolean isChanged () {
        return changed;
    }

    void applyChanges() {
        if (!changed) return; // no changes
        applyParameterToAll ("setExpandTabs", Boolean.valueOf(isExpandTabs ()), Boolean.TYPE); //NOI18N
        applyParameterToAll ("setSpacesPerTab", getSpacesPerTab (), Integer.TYPE); //NOI18N
        applyParameterToAll ("setTabSize", getTabSize(), Integer.TYPE); //NOI18N
        applyParameterToAll ("setRightMargin", getRightMargin(), Integer.TYPE); //NOI18N
    }
    
    void revertChanges () {
        if (!changed) return; // no changes
        if (isExpandTabs() != originalExpandedTabs) {
            setExpandTabs(originalExpandedTabs);
        }
        if (getSpacesPerTab().intValue() != originalSpacesPerTab && originalSpacesPerTab > 0) {
            setSpacesPerTab(new Integer(originalSpacesPerTab));
        }
        if (getTabSize().intValue() != originalTabSize && originalTabSize > 0) {
            setTabSize(new Integer(originalTabSize));
        }
        if (getRightMargin().intValue() != originalRightMargin && originalRightMargin > 0) {
            setRightMargin(new Integer(originalRightMargin));
        }
    }
    
    // private helper methods ..................................................

    private void updateChanged () {
        changed = 
            isExpandTabs () != originalExpandedTabs ||
            getSpacesPerTab ().intValue () != originalSpacesPerTab ||
            getTabSize().intValue() != originalTabSize ||
            getRightMargin().intValue() != originalRightMargin;
    }
    
    private BaseOptions exampleBaseOptions = null;
    private IndentEngine exampleIndentEngine = null;
    
    private BaseOptions getExampleBaseOptions() {
        if (exampleBaseOptions == null) {
            BaseOptions options = MimeLookup.getLookup(MimePath.parse("text/xml")).lookup(BaseOptions.class); //NOI18N
            if (options == null) {
                options = MimeLookup.getLookup(MimePath.parse("text/plain")).lookup(BaseOptions.class); //NOI18N
            }
            exampleBaseOptions = options;
        }
        return exampleBaseOptions;
    }
    private IndentEngine getExampleIndentEngine() {
        if (exampleIndentEngine == null) {
            BaseOptions options = getExampleBaseOptions();
            exampleIndentEngine = options == null ? null : options.getIndentEngine ();
        }
        return exampleIndentEngine;
    }
    
    private Object getParameter (String parameterName, Object defaultValue) {
        IndentEngine eng = getExampleIndentEngine();
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
        IndentEngine eng = getExampleIndentEngine();
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
        HashSet<IndentEngine> mimeTypeBoundEngines = new HashSet<IndentEngine>();
        Set<String> mimeTypes = EditorSettings.getDefault().getMimeTypes();
        for(String mimeType : mimeTypes) {
            BaseOptions baseOptions = MimeLookup.getLookup(MimePath.parse(mimeType)).lookup(BaseOptions.class);
            
            if (baseOptions == null) {
                continue;
            }
            
            IndentEngine indentEngine = baseOptions.getIndentEngine ();
            mimeTypeBoundEngines.add(indentEngine);
            
            try {
                ClassLoader classLoader = Lookup.getDefault().lookup(ClassLoader.class);
                // XXX: HACK
                if (baseOptions.getClass().getName().equals("org.netbeans.modules.java.editor.options.JavaOptions") && //NOI18N
                    !indentEngine.getClass().getName().equals("org.netbeans.modules.editor.java.JavaIndentEngine")) //NOI18N
                {
                    Class javaIndentEngineClass = classLoader.loadClass("org.netbeans.modules.editor.java.JavaIndentEngine"); //NOI18N
                    indentEngine = (IndentEngine) Lookup.getDefault ().lookup (javaIndentEngineClass);
                    baseOptions.setIndentEngine(indentEngine);
                }
                if (baseOptions.getClass().getName().equals("org.netbeans.modules.web.core.syntax.JSPOptions") && //NOI18N
                    !indentEngine.getClass().getName().equals("org.netbeans.modules.web.core.syntax.JspIndentEngine")) //NOI18N
                {
                    Class jspIndentEngineClass = classLoader.loadClass("org.netbeans.modules.web.core.syntax.JspIndentEngine"); //NOI18N
                    indentEngine = (IndentEngine) Lookup.getDefault ().lookup (jspIndentEngineClass);
                    baseOptions.setIndentEngine(indentEngine);
                }
                
                if (parameterName.equals("setTabSize")) { //NOI18N
                    baseOptions.setTabSize((Integer)parameterValue);
                } else if (parameterName.equals("setRightMargin")) { //NOI18N
                    baseOptions.setTextLimitWidth((Integer)parameterValue);
                } else {
                    Method method = indentEngine.getClass ().getMethod (
                        parameterName,
                        new Class [] {parameterType}
                    );
                    method.invoke (indentEngine, new Object [] {parameterValue});
                }
            } catch (Exception ex) {
            }
        }
        
        // There can be other engines that are not currently hooked up with
        // and BaseOptions/mime-type.
        
        Collection allEngines = Lookup.getDefault().lookupAll(IndentEngine.class);
        for (Iterator it = allEngines.iterator(); it.hasNext(); ) {
            IndentEngine indentEngine = (IndentEngine) it.next();
            if (!mimeTypeBoundEngines.contains(indentEngine)) {
                try {
                    Method method = indentEngine.getClass().getMethod(
                        parameterName,
                        new Class [] { parameterType }
                    );
                    method.invoke(indentEngine, new Object [] { parameterValue });
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }
    
}


