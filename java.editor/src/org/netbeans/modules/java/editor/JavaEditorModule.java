/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.editor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.LocaleSupport;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.editor.ext.java.JavaSettingsInitializer;
import org.netbeans.editor.ext.java.JavaSettingsNames;
import org.netbeans.modules.editor.java.JavaIndentEngine;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.java.NbJavaSettingsInitializer;
import org.netbeans.modules.editor.NbLocalizer;
import org.netbeans.modules.java.editor.options.JavaPrintOptions;
import org.netbeans.modules.java.editor.options.JavaOptions;
import org.netbeans.modules.javacore.IndentationSettingsProvider;
import org.netbeans.modules.javacore.JMManager;
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
    private IndentationSettingsProvider isProvider = null;
    private static JavaIndentEngine indentEng = null;

    /** Module installed again. */
    public void restored () {
        Settings.addInitializer(new JavaSettingsInitializer(JavaKit.class));
        Settings.addInitializer(new NbJavaSettingsInitializer());
        Settings.reset();

        PrintSettings ps = (PrintSettings) SharedClassObject.findObject(PrintSettings.class, true);
        ps.addOption((SystemOption)SharedClassObject.findObject(JavaPrintOptions.class, true));

        JMManager.setDocumentLocksCounter(BaseDocument.THREAD_LOCAL_LOCK_DEPTH);

        settingsNamesLocalizer = new NbLocalizer(JavaSettingsNames.class);
        optionsLocalizer = new NbLocalizer(JavaOptions.class);
        LocaleSupport.addLocalizer(settingsNamesLocalizer);
        LocaleSupport.addLocalizer(optionsLocalizer);
        if (isProvider == null) {
            isProvider = new JavaIndentationSettingsProvider();
        }
        JMManager.setIndentationSettingsProvider(isProvider);

    }

    /** Called when module is uninstalled. Overrides superclass method. */
    public void uninstalled() {
        
        JMManager.setIndentationSettingsProvider(null);
        if (indentEng != null && isProvider != null){
            indentEng.removePropertyChangeListener((PropertyChangeListener)isProvider);
        }
        
        // Options
        PrintSettings ps = (PrintSettings) SharedClassObject.findObject(PrintSettings.class, true);
        ps.removeOption((SystemOption)SharedClassObject.findObject(JavaPrintOptions.class, true));

        JMManager.setDocumentLocksCounter(null);

        Settings.removeInitializer(JavaSettingsInitializer.NAME);
        Settings.removeInitializer(NbJavaSettingsInitializer.NAME);
        Settings.reset();

        LocaleSupport.removeLocalizer(settingsNamesLocalizer);
        settingsNamesLocalizer = null;
        LocaleSupport.removeLocalizer(optionsLocalizer);
        optionsLocalizer = null;
        
    }
    
    private static class JavaIndentationSettingsProvider implements IndentationSettingsProvider, PropertyChangeListener{
        
        PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        public JavaIndentationSettingsProvider(){
            java.util.Enumeration enum = IndentEngine.indentEngines();
            while (enum.hasMoreElements()){
                Object indent = enum.nextElement();
                if (indent instanceof JavaIndentEngine){
                    indentEng = (JavaIndentEngine) indent;
                    break;
                }
            }
            if (indentEng != null){
                indentEng.addPropertyChangeListener(this);
            }
        }

        public Object getPropertyValue(String propertyName) {
            if (indentEng!=null){
                return indentEng.getValue(propertyName);
            }else{
                return null;
            }
        }

        public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }

        public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }

        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (evt == null) return;
            pcs.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
        
    }
}
