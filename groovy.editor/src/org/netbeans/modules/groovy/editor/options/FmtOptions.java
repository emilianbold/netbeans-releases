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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.groovy.editor.options;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.modules.groovy.editor.Formatter;
import org.netbeans.modules.groovy.editor.lexer.GroovyTokenId;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.Exceptions;

import static org.netbeans.modules.groovy.editor.options.CodeStyle.*;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author phrebejk
 * @author Martin Adamek
 */
public class FmtOptions {

    public static final String expandTabToSpaces = "expandTabToSpaces"; //NOI18N
    public static final String tabSize = "tabSize"; //NOI18N
    public static final String indentSize = "indentSize"; //NOI18N
    public static final String continuationIndentSize = "continuationIndentSize"; //NOI18N
    public static final String reformatComments = "reformatComments"; //NOI18N
    public static final String indentHtml = "indentHtml"; //NOI18N
    public static final String rightMargin = "rightMargin"; //NOI18N
    
    public static CodeStyleProducer codeStyleProducer;
        
    public static Preferences lastValues;
    
    private static Class<? extends EditorKit> kitClass;

    private static final String DEFAULT_PROFILE = "default"; // NOI18N
    
    private FmtOptions() {}

    public static int getDefaultAsInt(String key) {
        return Integer.parseInt(defaults.get(key));
    }
    
    public static boolean getDefaultAsBoolean(String key) {
        return Boolean.parseBoolean(defaults.get(key));
    }
        
    public static String getDefaultAsString(String key) {
        return defaults.get(key);
    }
    
    public static Preferences getPreferences(String profileId) {
        return NbPreferences.forModule(CodeStyle.class).node("CodeStyle").node(profileId);
    }

    public static boolean getGlobalExpandTabToSpaces() {
        org.netbeans.editor.Formatter f = (org.netbeans.editor.Formatter)Settings.getValue(getKitClass(), "formatter");
        if (f != null)
            return f.expandTabs();
        return getDefaultAsBoolean(expandTabToSpaces);
    }
    
    public static int getGlobalTabSize() {
        Integer i = (Integer)Settings.getValue(getKitClass(), SettingsNames.TAB_SIZE);
        return i != null ? i.intValue() : getDefaultAsInt(tabSize);
    }

    public static int getGlobalRightMargin() {
        Integer i = (Integer)Settings.getValue(getKitClass(), SettingsNames.TEXT_LIMIT_WIDTH);
        return i != null ? i.intValue() : getDefaultAsInt(rightMargin);
    }
    
    public static Class<? extends EditorKit> getKitClass() {
        if (kitClass == null) {
            EditorKit kit = MimeLookup.getLookup(MimePath.get(GroovyTokenId.GROOVY_MIME_TYPE)).lookup(EditorKit.class);
            kitClass = kit != null ? kit.getClass() : EditorKit.class;
        }
        return kitClass;
    }
    
    
    public static void flush() {
        try {
            getPreferences( getCurrentProfileId()).flush();
        }
        catch(BackingStoreException e) {
            Exceptions.printStackTrace(e);
        }
    }
    
    public static String getCurrentProfileId() {
        return DEFAULT_PROFILE;
    }
    
    public static CodeStyle createCodeStyle(Preferences p) {
        CodeStyle.getDefault(null);
        return codeStyleProducer.create(p);
    }
    
    public static boolean isInteger(String optionID) {
        String value = defaults.get(optionID);
        
        try {
            Integer.parseInt(value);
            return true;            
        } catch (NumberFormatException numberFormatException) {
            return false;
        }
    }
    
    public static String getLastValue(String optionID) {
        Preferences p = lastValues == null ? getPreferences(getCurrentProfileId()) : lastValues;
        return p.get(optionID, getDefaultAsString(optionID));
    }
 
    // Private section ---------------------------------------------------------
    
    private static final String TRUE = "true";      // NOI18N
    private static final String FALSE = "false";    // NOI18N
    
    private static Map<String,String> defaults;
    
    static {
        createDefaults();
    }
    
    private static void createDefaults() {
        String defaultValues[][] = {
            { expandTabToSpaces, TRUE}, //NOI18N
            { tabSize, "4"}, //NOI18N
            { indentSize, "4"}, //NOI18N
            { continuationIndentSize, "4"}, //NOI18N
            { reformatComments, FALSE }, //NOI18N
            { indentHtml, TRUE }, //NOI18N
            { rightMargin, "80"}, //NOI18N
        };
        
        defaults = new HashMap<String,String>();
        
        for (java.lang.String[] strings : defaultValues) {
            defaults.put(strings[0], strings[1]);
        }

    }
 
    
    // Support section ---------------------------------------------------------
      
   
    public static interface CodeStyleProducer {
        
        public CodeStyle create( Preferences preferences );
    
    }
}
