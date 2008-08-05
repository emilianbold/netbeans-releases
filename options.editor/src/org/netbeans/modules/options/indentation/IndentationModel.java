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
    
    private boolean         originalOverrideGlobalOptions;
    private boolean         originalExpandedTabs;
    private int             originalSpacesPerTab = 0;
    private int             originalTabSize = 0;
    private int             originalRightMargin = 0;
    
    private boolean         changed = false;
    
    private final Preferences preferences;
    private final String mimeType;

    IndentationModel (Preferences prefs, String mimeType) {
        this.preferences = prefs;
        this.mimeType = mimeType; // can be null, which indicates that the model is used for 'all languages'

        // save original values
        originalOverrideGlobalOptions = isOverrideGlobalOptions();
        originalExpandedTabs = isExpandTabs();
        originalSpacesPerTab = getSpacesPerTab();
        originalTabSize = getTabSize();
        originalRightMargin = getRightMargin();
    }
    
    boolean isOverrideGlobalOptions() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Reading " + SimpleValueNames.OVERRIDE_GLOBAL_FORMATTING_OPTIONS + "=" + preferences.get(SimpleValueNames.OVERRIDE_GLOBAL_FORMATTING_OPTIONS, null));
        }
        return preferences.getBoolean(SimpleValueNames.OVERRIDE_GLOBAL_FORMATTING_OPTIONS, false);
    }
    
    void setOverrideGlobalOptions(boolean override) {
        preferences.putBoolean(SimpleValueNames.OVERRIDE_GLOBAL_FORMATTING_OPTIONS, override);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Writing " + SimpleValueNames.OVERRIDE_GLOBAL_FORMATTING_OPTIONS + "=" + override);
        }
        updateChanged ();
    }
    
    boolean isExpandTabs() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Reading " + SimpleValueNames.EXPAND_TABS + "=" + preferences.get(SimpleValueNames.EXPAND_TABS, null));
        }
        return preferences.getBoolean(SimpleValueNames.EXPAND_TABS, false);
    }
    
    void setExpandTabs(boolean expand) {
        preferences.putBoolean(SimpleValueNames.EXPAND_TABS, expand);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Writing " + SimpleValueNames.EXPAND_TABS + "=" + expand);
        }
        updateChanged ();
    }
    
    int getSpacesPerTab() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Reading " + SimpleValueNames.SPACES_PER_TAB + "=" + preferences.get(SimpleValueNames.SPACES_PER_TAB, null));
        }
        return preferences.getInt(SimpleValueNames.SPACES_PER_TAB, 4);
    }
    
    void setSpacesPerTab(int spaces) {
        assert spaces > 0 : "Invalid 'spaces per tab': " + spaces; //NOI18N
        preferences.putInt(SimpleValueNames.SPACES_PER_TAB, spaces);
        preferences.putInt(SimpleValueNames.INDENT_SHIFT_WIDTH, spaces);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Writing " + SimpleValueNames.SPACES_PER_TAB + "=" + spaces + " and " + SimpleValueNames.INDENT_SHIFT_WIDTH + "=" + spaces);
        }
        updateChanged();
    }

    int getTabSize() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Reading" + SimpleValueNames.TAB_SIZE + "=" + preferences.get(SimpleValueNames.TAB_SIZE, null));
        }
        return preferences.getInt(SimpleValueNames.TAB_SIZE, 4);
    }
    
    void setTabSize(int tabSize) {
        assert tabSize > 0 : "Invalid 'tab size': " + tabSize; //NOI18N
        preferences.putInt(SimpleValueNames.TAB_SIZE, tabSize);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Writing " + SimpleValueNames.TAB_SIZE + "=" + tabSize);
        }
        updateChanged ();
    }

    Integer getRightMargin () {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Reading " + SimpleValueNames.TEXT_LIMIT_WIDTH + "=" + preferences.get(SimpleValueNames.TEXT_LIMIT_WIDTH, null));
        }
        return preferences.getInt(SimpleValueNames.TEXT_LIMIT_WIDTH, 80);
    }
    
    void setRightMargin(int textLimit) {
        assert textLimit > 0 : "Invalid 'text limit width': " + textLimit; //NOI18N
        preferences.putInt(SimpleValueNames.TEXT_LIMIT_WIDTH, textLimit);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Writing " + SimpleValueNames.TEXT_LIMIT_WIDTH + "=" + textLimit);
        }
        updateChanged ();
    }

    boolean isChanged () {
        return changed;
    }

    void applyChanges() {
        if (!changed) return; // no changes
        
        if (mimeType == null) {
            // we are manipulating all languages
            applyParameterToAll(SimpleValueNames.EXPAND_TABS, isExpandTabs(), "setExpandTabs", Boolean.TYPE); //NOI18N
            applyParameterToAll(SimpleValueNames.SPACES_PER_TAB, getSpacesPerTab(), "setSpacesPerTab", Integer.TYPE); //NOI18N
            applyParameterToAll(SimpleValueNames.INDENT_SHIFT_WIDTH, getSpacesPerTab(), null, Integer.TYPE); //NOI18N
            applyParameterToAll(SimpleValueNames.TAB_SIZE, getTabSize(), "setTabSize", Integer.TYPE); //NOI18N
            applyParameterToAll(SimpleValueNames.TEXT_LIMIT_WIDTH, getRightMargin(), "setTextLimitWidth", Integer.TYPE); //NOI18N
        } else {
            // we are manipulating only mimeType
            applyParameter(mimeType, SimpleValueNames.OVERRIDE_GLOBAL_FORMATTING_OPTIONS, isOverrideGlobalOptions(), null, Boolean.TYPE, null); //NOI18N
            if (isOverrideGlobalOptions()) {
                applyParameter(mimeType, SimpleValueNames.EXPAND_TABS, isExpandTabs(), "setExpandTabs", Boolean.TYPE, null); //NOI18N
                applyParameter(mimeType, SimpleValueNames.SPACES_PER_TAB, getSpacesPerTab(), "setSpacesPerTab", Integer.TYPE, null); //NOI18N
                applyParameter(mimeType, SimpleValueNames.INDENT_SHIFT_WIDTH, getSpacesPerTab(), null, Integer.TYPE, null); //NOI18N
                applyParameter(mimeType, SimpleValueNames.TAB_SIZE, getTabSize(), "setTabSize", Integer.TYPE, null); //NOI18N
                applyParameter(mimeType, SimpleValueNames.TEXT_LIMIT_WIDTH, getRightMargin(), "setTextLimitWidth", Integer.TYPE, null); //NOI18N
            } else {
                preferences.remove(SimpleValueNames.EXPAND_TABS);
                preferences.remove(SimpleValueNames.SPACES_PER_TAB);
                preferences.remove(SimpleValueNames.INDENT_SHIFT_WIDTH);
                preferences.remove(SimpleValueNames.TAB_SIZE);
                preferences.remove(SimpleValueNames.TEXT_LIMIT_WIDTH);
            }
        }
        
    }
    
    void revertChanges () {
        if (!changed) return; // no changes
        if (isOverrideGlobalOptions() != originalOverrideGlobalOptions) {
            setOverrideGlobalOptions(originalOverrideGlobalOptions);
        }
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
                isOverrideGlobalOptions() != originalOverrideGlobalOptions ||
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
        mimeTypes.add(""); //NOI18N
        
        for(String m : mimeTypes) {
            applyParameter(m, settingName, settingValue, baseOptionsSetter, settingType, mimeTypeBoundEngines);
        }

        // There can be other engines that are not currently hooked up with
        // BaseOptions/mime-type.
        if (baseOptionsSetter != null) {
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
    }
    
    private void applyParameter (
        String mimeType,
        String settingName,
        Object settingValue,
        String baseOptionsSetter, 
        Class settingType,
        Set<IndentEngine> mimeTypeBoundEngines
    ) {
        Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);

        if (prefs != null) {
            if (settingValue instanceof Boolean) {
                prefs.putBoolean(settingName, (Boolean) settingValue);
            } else if (settingValue instanceof Integer) {
                prefs.putInt(settingName, (Integer) settingValue);
            } else {
                assert false : "Unexpected setting value: settingName='" + settingName + "', settingValue=" + settingValue; //NOI18N
            }
        } else {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("!!! no Preferences for '" + mimeType + "'");
            }
        }

        if (mimeType.length() > 0 && baseOptionsSetter != null) {
            IndentEngine indentEngine = hacksForBaseOptionsAndIndentEngine(mimeType, baseOptionsSetter, settingValue, settingType);
            if (indentEngine != null && mimeTypeBoundEngines != null) {
                mimeTypeBoundEngines.add(indentEngine);
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


