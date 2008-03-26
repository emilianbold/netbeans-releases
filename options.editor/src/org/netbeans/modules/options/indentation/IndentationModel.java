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
        applyParameterToAll ("setTextLimitWidth", getRightMargin(), Integer.TYPE); //NOI18N
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
            
            ClassLoader classLoader = Lookup.getDefault().lookup(ClassLoader.class);
            // XXX: HACK
            if (baseOptions.getClass().getName().equals("org.netbeans.modules.java.editor.options.JavaOptions") && //NOI18N
                !indentEngine.getClass().getName().equals("org.netbeans.modules.editor.java.JavaIndentEngine")) //NOI18N
            {
                try {
                    Class javaIndentEngineClass = classLoader.loadClass("org.netbeans.modules.editor.java.JavaIndentEngine"); //NOI18N
                    indentEngine = (IndentEngine) Lookup.getDefault().lookup(javaIndentEngineClass);
                    baseOptions.setIndentEngine(indentEngine);
                } catch (Exception ex) {
                }
            }
            if (baseOptions.getClass().getName().equals("org.netbeans.modules.web.core.syntax.JSPOptions") && //NOI18N
                !indentEngine.getClass().getName().equals("org.netbeans.modules.web.core.syntax.JspIndentEngine")) //NOI18N
            {
                try {
                    Class jspIndentEngineClass = classLoader.loadClass("org.netbeans.modules.web.core.syntax.JspIndentEngine"); //NOI18N
                    indentEngine = (IndentEngine) Lookup.getDefault ().lookup (jspIndentEngineClass);
                    baseOptions.setIndentEngine(indentEngine);
                } catch (Exception ex) {
                }
            }

            // update BaseOptions
            try {
                Method method = baseOptions.getClass ().getMethod (
                    parameterName,
                    new Class [] { parameterType }
                );
                method.invoke (baseOptions, new Object [] { parameterValue });
            } catch (Exception ex) {
                // ignore
            }
            
            // update IndentEngine
            try {
                Method method = indentEngine.getClass ().getMethod (
                    parameterName,
                    new Class [] { parameterType }
                );
                method.invoke (indentEngine, new Object [] { parameterValue });
            } catch (Exception ex) {
                // ignore
            }
        }
        
        // There can be other engines that are not currently hooked up with
        // BaseOptions/mime-type.
        
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


