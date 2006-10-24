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

package org.netbeans.modules.java.editor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.LocaleSupport;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.ext.java.JavaSettingsInitializer;
import org.netbeans.editor.ext.java.JavaSettingsNames;
import org.netbeans.modules.editor.FormatterIndentEngine;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.java.JavaIndentEngine;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.java.NbJavaSettingsInitializer;
import org.netbeans.modules.editor.NbLocalizer;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.java.editor.options.JavaPrintOptions;
import org.netbeans.modules.java.editor.options.JavaOptions;
//import org.netbeans.modules.javacore.IndentationSettingsProvider;
//import org.netbeans.modules.javacore.JMManager;
import org.openide.modules.ModuleInstall;
import org.openide.options.SystemOption;
import org.openide.text.IndentEngine;
import org.openide.text.PrintSettings;
import org.openide.util.SharedClassObject;

/**
 * Module installation class for editor.
 *
 * @author Miloslav Metelka
 */
public class JavaEditorModule extends ModuleInstall {

    private NbLocalizer settingsNamesLocalizer;
    private NbLocalizer optionsLocalizer;
//    private JavaIndentationSettingsProvider jisProvider = null;
    static boolean inited = false;
    
    public static void init(){
        if (inited) return;
        inited = true;
        Settings.addInitializer(new JavaSettingsInitializer(JavaKit.class));
        Settings.addInitializer(new NbJavaSettingsInitializer());
        Settings.reset();
    }
    
    /** Module installed again. */
    public void restored () {
        init();

        PrintSettings ps = (PrintSettings) SharedClassObject.findObject(PrintSettings.class, true);
        ps.addOption((SystemOption)SharedClassObject.findObject(JavaPrintOptions.class, true));

//        JMManager.setDocumentLocksCounter(BaseDocument.THREAD_LOCAL_LOCK_DEPTH);

        settingsNamesLocalizer = new NbLocalizer(JavaSettingsNames.class);
        optionsLocalizer = new NbLocalizer(JavaOptions.class);
        LocaleSupport.addLocalizer(settingsNamesLocalizer);
        LocaleSupport.addLocalizer(optionsLocalizer);
//        if (jisProvider == null) {
//            jisProvider = new JavaIndentationSettingsProvider();
//            JMManager.setIndentationSettingsProvider(jisProvider);
//        }

    }

    /** Called when module is uninstalled. Overrides superclass method. */
    public void uninstalled() {
        
//        if (jisProvider != null) {
//            jisProvider.release();
//            JMManager.setIndentationSettingsProvider(null);
//            jisProvider = null;
//        }
        
        // Options
        PrintSettings ps = (PrintSettings) SharedClassObject.findObject(PrintSettings.class, true);
        ps.removeOption((SystemOption)SharedClassObject.findObject(JavaPrintOptions.class, true));

//        JMManager.setDocumentLocksCounter(null);

        Settings.removeInitializer(JavaSettingsInitializer.NAME);
        Settings.removeInitializer(NbJavaSettingsInitializer.NAME);
        Settings.reset();

        LocaleSupport.removeLocalizer(settingsNamesLocalizer);
        settingsNamesLocalizer = null;
        LocaleSupport.removeLocalizer(optionsLocalizer);
        optionsLocalizer = null;
        
    }
    
//    private static class JavaIndentationSettingsProvider implements IndentationSettingsProvider, PropertyChangeListener{
//        
//        private static final Map indentSettings2propertyName
//                = new HashMap();
//        
//        static {
//            indentSettings2propertyName.put(
//                    JavaIndentEngine.JAVA_FORMAT_LEADING_STAR_IN_COMMENT_PROP,
//                    JavaSettingsNames.JAVA_FORMAT_LEADING_STAR_IN_COMMENT
//            );
//            indentSettings2propertyName.put(
//                    JavaIndentEngine.JAVA_FORMAT_NEWLINE_BEFORE_BRACE_PROP,
//                    JavaSettingsNames.JAVA_FORMAT_NEWLINE_BEFORE_BRACE
//            );
//            indentSettings2propertyName.put(
//                    JavaIndentEngine.JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS_PROP,
//                    JavaSettingsNames.JAVA_FORMAT_SPACE_BEFORE_PARENTHESIS
//            );
//            indentSettings2propertyName.put(
//                    JavaIndentEngine.JAVA_FORMAT_STATEMENT_CONTINUATION_INDENT_PROP,
//                    JavaSettingsNames.JAVA_FORMAT_STATEMENT_CONTINUATION_INDENT
//            );
//            indentSettings2propertyName.put(
//                    FormatterIndentEngine.EXPAND_TABS_PROP,
//                    SettingsNames.EXPAND_TABS
//            );
//            indentSettings2propertyName.put(
//                    FormatterIndentEngine.SPACES_PER_TAB_PROP,
//                    SettingsNames.SPACES_PER_TAB
//            );
//        }
//        
//        private JavaIndentEngine indentEngine = null;
//
//        private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
//        
//        public JavaIndentationSettingsProvider(){
//        }
//        
//        private synchronized JavaIndentEngine getIndentEngine() {
//            if (indentEngine == null) {
//                BaseOptions javaOptions = BaseOptions.getOptions(JavaKit.class);
//                if (javaOptions instanceof JavaOptions) {
//                    IndentEngine eng = javaOptions.getIndentEngine();
//                    if (eng instanceof JavaIndentEngine) {
//                        indentEngine = (JavaIndentEngine)eng;
//                        indentEngine.addPropertyChangeListener(this);
//                    }
//                }
//            }
//            return indentEngine;
//        }
//
//        public Object getPropertyValue(String propertyName) {
//            JavaIndentEngine eng = getIndentEngine();
//            if (eng != null){
//                String settingsPropertyName = (String)indentSettings2propertyName.get(propertyName);
//                if (settingsPropertyName != null) {
//                    return eng.getValue(settingsPropertyName); 
//                }
//            }
//
//            return null;
//        }
//
//        public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
//            getIndentEngine(); // possibly init engine to listen on it
//            pcs.removePropertyChangeListener(l);
//        }
//
//        public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
//            getIndentEngine(); // possibly init engine to listen on it
//            pcs.addPropertyChangeListener(l);
//        }
//
//        public void propertyChange(java.beans.PropertyChangeEvent evt) {
//            if (evt == null) return;
//            pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
//        }
//
//        public synchronized void release() {
//            if (indentEngine != null) {
//                indentEngine.removePropertyChangeListener(this);
//            }
//        }
//
//    }
}
