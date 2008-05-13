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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;

import org.openide.text.IndentEngine;
import org.openide.util.Lookup;


final class IndentationModel {

    private static final Logger LOG = Logger.getLogger(IndentationModel.class.getName());
    
    private boolean         originalExpandedTabs;
    private int             originalSpacesPerTab = 0;
    private int             originalTabSize = 0;
    private int             originalRightMargin = 0;
    
    private boolean         changed = false;

    private static final String EXAMPLE_MIME_TYPE = "text/xml"; //NOI18N
    
    IndentationModel () {
        // save original values
        originalExpandedTabs = isExpandTabs();
        originalSpacesPerTab = getSpacesPerTab();
        originalTabSize = getTabSize();
        originalRightMargin = getRightMargin();
    }
    
    boolean isExpandTabs() {
        Preferences prefs = MimeLookup.getLookup(EXAMPLE_MIME_TYPE).lookup(Preferences.class);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("~~~ " + SimpleValueNames.EXPAND_TABS + "=" + prefs.get(SimpleValueNames.EXPAND_TABS, null));
        }
        return prefs.getBoolean(SimpleValueNames.EXPAND_TABS, false);
    }
    
    void setExpandTabs(boolean expand) {
        Preferences prefs = MimeLookup.getLookup(EXAMPLE_MIME_TYPE).lookup(Preferences.class);
        prefs.putBoolean(SimpleValueNames.EXPAND_TABS, expand);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("~~~ setting " + SimpleValueNames.EXPAND_TABS + "=" + expand);
        }
        updateChanged ();
    }
    
    int getSpacesPerTab() {
        Preferences prefs = MimeLookup.getLookup(EXAMPLE_MIME_TYPE).lookup(Preferences.class);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("~~~ " + SimpleValueNames.SPACES_PER_TAB + "=" + prefs.get(SimpleValueNames.SPACES_PER_TAB, null));
        }
        return prefs.getInt(SimpleValueNames.SPACES_PER_TAB, 4);
    }
    
    void setSpacesPerTab(int spaces) {
        assert spaces > 0 : "Invalid 'spaces per tab': " + spaces; //NOI18N
        Preferences prefs = MimeLookup.getLookup(EXAMPLE_MIME_TYPE).lookup(Preferences.class);
        prefs.putInt(SimpleValueNames.SPACES_PER_TAB, spaces);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("~~~ setting " + SimpleValueNames.SPACES_PER_TAB + "=" + spaces);
        }
        updateChanged();
    }

    int getTabSize() {
        Preferences prefs = MimeLookup.getLookup(EXAMPLE_MIME_TYPE).lookup(Preferences.class);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("~~~ " + SimpleValueNames.TAB_SIZE + "=" + prefs.get(SimpleValueNames.TAB_SIZE, null));
        }
        return prefs.getInt(SimpleValueNames.TAB_SIZE, 4);
    }
    
    void setTabSize(int tabSize) {
        assert tabSize > 0 : "Invalid 'tab size': " + tabSize; //NOI18N
        Preferences prefs = MimeLookup.getLookup(EXAMPLE_MIME_TYPE).lookup(Preferences.class);
        prefs.putInt(SimpleValueNames.TAB_SIZE, tabSize);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("~~~ setting " + SimpleValueNames.TAB_SIZE + "=" + tabSize);
        }
        updateChanged ();
    }

    Integer getRightMargin () {
        Preferences prefs = MimeLookup.getLookup(EXAMPLE_MIME_TYPE).lookup(Preferences.class);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("~~~ " + SimpleValueNames.TEXT_LIMIT_WIDTH + "=" + prefs.get(SimpleValueNames.TEXT_LIMIT_WIDTH, null));
        }
        return prefs.getInt(SimpleValueNames.TEXT_LIMIT_WIDTH, 80);
    }
    
    void setRightMargin(int textLimit) {
        assert textLimit > 0 : "Invalid 'text limit width': " + textLimit; //NOI18N
        Preferences prefs = MimeLookup.getLookup(EXAMPLE_MIME_TYPE).lookup(Preferences.class);
        prefs.putInt(SimpleValueNames.TEXT_LIMIT_WIDTH, textLimit);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("~~~ setting " + SimpleValueNames.TEXT_LIMIT_WIDTH + "=" + textLimit);
        }
        updateChanged ();
    }

    boolean isChanged () {
        return changed;
    }

    void applyChanges() {
        if (!changed) return; // no changes
        applyParameterToAll(SimpleValueNames.EXPAND_TABS, isExpandTabs(), "setExpandTabs", Boolean.TYPE); //NOI18N
        applyParameterToAll(SimpleValueNames.SPACES_PER_TAB, getSpacesPerTab(), "setSpacesPerTab", Integer.TYPE); //NOI18N
        applyParameterToAll(SimpleValueNames.TAB_SIZE, getTabSize(), "setTabSize", Integer.TYPE); //NOI18N
        applyParameterToAll(SimpleValueNames.TEXT_LIMIT_WIDTH, getRightMargin(), "setTextLimitWidth", Integer.TYPE); //NOI18N
        
    }
    
    void revertChanges () {
        if (!changed) return; // no changes
        if (isExpandTabs() != originalExpandedTabs) {
            setExpandTabs(originalExpandedTabs);
        }
        if (getSpacesPerTab() != originalSpacesPerTab && originalSpacesPerTab > 0) {
            setSpacesPerTab(originalSpacesPerTab);
        }
        if (getTabSize() != originalTabSize && originalTabSize > 0) {
            setTabSize(originalTabSize);
        }
        if (getRightMargin() != originalRightMargin && originalRightMargin > 0) {
            setRightMargin(originalRightMargin);
        }
    }
    
    // private helper methods ..................................................

    private void updateChanged () {
        changed = 
                isExpandTabs() != originalExpandedTabs ||
                getSpacesPerTab() != originalSpacesPerTab ||
                getTabSize() != originalTabSize ||
                getRightMargin() != originalRightMargin;
    }
    
    private void applyParameterToAll (
        String settingName,
        Object settingValue,
        String baseOptionsSetter, 
        Class settingType
    ) {
        HashSet<IndentEngine> mimeTypeBoundEngines = new HashSet<IndentEngine>();
        Set<String> mimeTypes = new HashSet<String>(EditorSettings.getDefault().getMimeTypes());
        mimeTypes.add("");
        
        for(String mimeType : mimeTypes) {
            Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
            
            if (prefs == null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("!!! no Preferences for '" + mimeType + "'");
                }
                continue;
            }
            
            if (settingValue instanceof Boolean) {
                prefs.putBoolean(settingName, (Boolean) settingValue);
            } else if (settingValue instanceof Integer) {
                prefs.putInt(settingName, (Integer) settingValue);
            } else {
                assert false : "Unexpected setting value: settingName='" + settingName + "', settingValue=" + settingValue; //NOI18N
            }
            
            if (mimeType.length() > 0) {
                IndentEngine indentEngine = hacksForBaseOptionsAndIndentEngine(mimeType, settingName, settingValue, settingType);
                if (indentEngine != null) {
                    mimeTypeBoundEngines.add(indentEngine);
                }
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
                        baseOptionsSetter,
                        new Class [] { settingType }
                    );
                    method.invoke(indentEngine, new Object [] { settingValue });
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    // XXX: HACK
    private IndentEngine hacksForBaseOptionsAndIndentEngine(String mimeType, String setter, Object settingValue, Class settingType) {
        // try to load BaseOptions class
        ClassLoader classLoader = Lookup.getDefault().lookup(ClassLoader.class);
        Class<?> baseOptionsClass = null;
        try {
            baseOptionsClass = classLoader.loadClass("org.netbeans.modules.editor.options.BaseOptions"); //NOI18N
        } catch (Exception e) {
            // no BaseOption, no module uses deprecated settings
            return null;
        }
        
        // load BaseOptions instance for the mimeType
        Object baseOptions = MimeLookup.getLookup(MimePath.parse(mimeType)).lookup(baseOptionsClass);
        if (baseOptions == null) {
            return null;
        }

        // get IndentEngine instance used by the BaseOptions
        IndentEngine indentEngine = null;
        try {
            Method getIEMethod = baseOptionsClass.getMethod("getIndentEngine"); //NOI18N
            indentEngine = (IndentEngine) getIEMethod.invoke(baseOptions);
        } catch (Exception ex) {
        }

        if (indentEngine == null) {
            return null;
        }

        // some magic for Java and JSP, which I don't really understand
        if (baseOptions.getClass().getName().equals("org.netbeans.modules.java.editor.options.JavaOptions") && //NOI18N
            !indentEngine.getClass().getName().equals("org.netbeans.modules.editor.java.JavaIndentEngine")) //NOI18N
        {
            try {
                Class<?> javaIndentEngineClass = classLoader.loadClass("org.netbeans.modules.editor.java.JavaIndentEngine"); //NOI18N
                indentEngine = (IndentEngine) Lookup.getDefault().lookup(javaIndentEngineClass);
                Method setIEMethod = baseOptionsClass.getMethod("setIndentEngine", IndentEngine.class); //NOI18N
                setIEMethod.invoke(baseOptions, indentEngine);
            } catch (Exception ex) {
            }
        }
        if (baseOptions.getClass().getName().equals("org.netbeans.modules.web.core.syntax.JSPOptions") && //NOI18N
            !indentEngine.getClass().getName().equals("org.netbeans.modules.web.core.syntax.JspIndentEngine")) //NOI18N
        {
            try {
                Class<?> jspIndentEngineClass = classLoader.loadClass("org.netbeans.modules.web.core.syntax.JspIndentEngine"); //NOI18N
                indentEngine = (IndentEngine) Lookup.getDefault ().lookup (jspIndentEngineClass);
                Method setIEMethod = baseOptionsClass.getMethod("setIndentEngine", IndentEngine.class); //NOI18N
                setIEMethod.invoke(baseOptions, indentEngine);
            } catch (Exception ex) {
            }
        }

        // update the setting value on BaseOptions 
        try {
            Method method = baseOptions.getClass ().getMethod (
                setter,
                new Class [] { settingType }
            );
            method.invoke (baseOptions, new Object [] { settingValue });
        } catch (Exception ex) {
            // ignore
        }

        // update the setting value on IndentEngine
        try {
            Method method = indentEngine.getClass ().getMethod (
                setter,
                new Class [] { settingType }
            );
            method.invoke (indentEngine, new Object [] { settingValue });
        } catch (Exception ex) {
            // ignore
        }
        
        return indentEngine;
    }
    
}


