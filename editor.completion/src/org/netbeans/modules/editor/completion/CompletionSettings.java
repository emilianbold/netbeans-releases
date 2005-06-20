/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.completion;

import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtSettingsDefaults;
import org.netbeans.editor.ext.ExtSettingsNames;

/**
 * Maintenance of the editor settings related to the code completion.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class CompletionSettings implements SettingsChangeListener {
    
    public static final CompletionSettings INSTANCE = new CompletionSettings();
    
    private static final Object NULL_VALUE = new Object();
    
    private JTextComponent editorComponent;
    
    private Map settingName2value = new HashMap();
    
    private CompletionSettings() {
        Settings.addSettingsChangeListener(this);
    }
    
    public boolean completionAutoPopup() {
        return ((Boolean)getValue(
                ExtSettingsNames.COMPLETION_AUTO_POPUP,
                ExtSettingsDefaults.defaultCompletionAutoPopup)
        ).booleanValue();
    }
    
    public int completionAutoPopupDelay() {
        return ((Integer)getValue(
                ExtSettingsNames.COMPLETION_AUTO_POPUP_DELAY,
                ExtSettingsDefaults.defaultCompletionAutoPopupDelay)
        ).intValue();
    }
    
    public boolean documentationAutoPopup() {
        return ((Boolean)getValue(
                ExtSettingsNames.JAVADOC_AUTO_POPUP,
                ExtSettingsDefaults.defaultJavaDocAutoPopup)
        ).booleanValue();
    }
    
    public int documentationAutoPopupDelay() {
        return ((Integer)getValue(
                ExtSettingsNames.JAVADOC_AUTO_POPUP_DELAY,
                ExtSettingsDefaults.defaultJavaDocAutoPopupDelay)
        ).intValue();
    }
    
    public Dimension completionPopupMaximumSize() {
        return (Dimension)getValue(
                ExtSettingsNames.COMPLETION_PANE_MAX_SIZE,
                ExtSettingsDefaults.defaultCompletionPaneMaxSize);
    }
    
    public Dimension documentationPopupPreferredSize() {
        return (Dimension)getValue(
                ExtSettingsNames.JAVADOC_PREFERRED_SIZE,
                ExtSettingsDefaults.defaultJavaDocPreferredSize);
    }
    
    public Color documentationBackgroundColor() {
        return (Color)CompletionSettings.INSTANCE.getValue(
                ExtSettingsNames.JAVADOC_BG_COLOR,
                ExtSettingsDefaults.defaultJavaDocBGColor);
    }

    public void notifyEditorComponentChange(JTextComponent newEditorComponent) {
        this.editorComponent = newEditorComponent;
        clearSettingValues();
    }
    
    public Object getValue(String settingName) {
        Object value;
        synchronized (this) {
            value = settingName2value.get(settingName);
        }
        
        if (value == null) {
            JTextComponent c = editorComponent;
            if (c != null) {
                Class kitClass = Utilities.getKitClass(c);
                if (kitClass != null) {
                    value = Settings.getValue(kitClass, settingName);
                    if (value == null) {
                        value = NULL_VALUE;
                    }
                }
            }
            
            if (value != null) {
                synchronized (this) {
                    settingName2value.put(settingName, value);
                }
            }
        }
        
        if (value == NULL_VALUE) {
            value = null;
        }
        return value;
    }
    
    public Object getValue(String settingName, Object defaultValue) {
        Object value = getValue(settingName);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }
    
    public void settingsChange(SettingsChangeEvent evt) {
        clearSettingValues();
    }
    
    private synchronized void clearSettingValues() {
        settingName2value.clear();
    }
}
