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


package org.netbeans.modules.properties.syntax;


import java.awt.Color;
import java.awt.Font;
import java.awt.SystemColor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.modules.properties.TableViewSettings;

import org.openide.util.SharedClassObject;

/**
 * TableViewSettings that delegates to text editor module settings.
 *
 * @author Peter Zavadsky, refactored by Petr Kuzel
 * @see org.netbeans.modules.propeties.BundleEditPanel
 */
public class EditorSettingsCopy extends TableViewSettings implements SettingsChangeListener {

    /** Singleton instance of <code>EditorSettingsCopy</code>. */
    private static EditorSettingsCopy editorSettingsCopy;
    
    /** Value of key color retrieved from settings in editor module. */
    private Color keyColor;
    /** Value of key background retrieved from settings in editor module. */
    private Color keyBackground;
    /** Value of value color retrieved from settings in editor module. */
    private Color valueColor;
    /** Value of value background retrieved from settings in editor module. */
    private Color valueBackground;
    /** Value of highlight color retrieved from settings in editor module. */
    private Color highlightColor;
    /** Value of highlight bacground retrieved from settings in editor module. */
    private Color highlightBackground;
    /** Value of shadow color retrieved from settings in editor module. */
    private Color shadowColor;
    
    /** Key strokes for find next action rerieved from editor module. */
    private KeyStroke[] keyStrokesFindNext;
    /** Key strokes for find previous action retrieved from editor module. */
    private KeyStroke[] keyStrokesFindPrevious;
    /** Key strokes for toggle search highlight action retrieved from editor module. */
    private KeyStroke[] keyStrokesToggleHighlight;

    /** Support for property changes. */
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
        
    /** Flag indicating whether the settings are prepared. */
    private boolean prepared = false;
    
    
    /** Private constructor. */
    private EditorSettingsCopy() {
    }

    
    /** Implements <code>EditorSetings</code> interface method. */
    public Color getKeyColor() {
        prepareSettings();
        if(keyColor == null) {
            keyColor = TableViewSettings.KEY_DEFAULT_COLOR;
        }
        
        return keyColor;
    }
    
    /** Implements <code>EditorSetings</code> interface method. */    
    public Color getKeyBackground() {
        prepareSettings();
        if(keyBackground == null) {
            keyBackground = TableViewSettings.KEY_DEFAULT_BACKGROUND;
        }
        
        return keyBackground;
    }
    
    /** Implements <code>EditorSetings</code> interface method. */
    public Color getValueColor() {
        prepareSettings();
        if(valueColor == null) {
            valueColor = TableViewSettings.VALUE_DEFAULT_COLOR;
        }
        
        return valueColor;
    }
    
    /** Implements <code>EditorSetings</code> interface method. */
    public Color getValueBackground() {
        prepareSettings();
        if(valueBackground == null) {
            valueBackground = TableViewSettings.VALUE_DEFAULT_BACKGROUND;
        }
        
        return valueBackground;
    }
    
    /** Implements <code>EditorSetings</code> interface method. */
    public Color getHighlightColor() {
        prepareSettings();
        if(highlightColor == null) {
            highlightColor = TableViewSettings.HIGHLIGHT_DEFAULT_COLOR;
        }
        
        return highlightColor;
    }
    
    /** Implements <code>EditorSetings</code> interface method. */ 
    public Color getHighlightBackground() {
        prepareSettings();
        if(highlightBackground == null) {
            highlightBackground = TableViewSettings.HIGHLIGHT_DEFAULT_BACKGROUND;
        }
        
        return highlightBackground;
    }
    
    /** Implements <code>EditorSetings</code> inaterface method. */ 
    public Color getShadowColor() {
        prepareSettings();
        if(shadowColor == null) {
            shadowColor = TableViewSettings.SHADOW_DEFAULT_COLOR;
        }
        
        return shadowColor;
    }

    public Font getFont() {
        prepareSettings();
        Font font = SettingsUtil.getColoring(PropertiesKit.class, 
                                             SettingsNames.DEFAULT_COLORING, 
                                             false).getFont();
        return font;            
    }    


    /** Implements <code>EditorSetings</code> interface method. */     
    public KeyStroke[] getKeyStrokesFindNext() {
        prepareSettings();
        if(keyStrokesFindNext == null || keyStrokesFindNext.length == 0) {
            keyStrokesFindNext = TableViewSettings.FIND_NEXT_DEFAULT_KEYSTROKES;
        }
        
        return keyStrokesFindNext;
    }
    
    /** Implements <code>EditorSetings</code> interface method. */     
    public KeyStroke[] getKeyStrokesFindPrevious() {
        prepareSettings();
        if(keyStrokesFindPrevious == null || keyStrokesFindPrevious.length == 0) {
            keyStrokesFindPrevious = TableViewSettings.FIND_PREVIOUS_DEFAULT_KEYSTROKES;
        }
        
        return keyStrokesFindPrevious;
    }
    
    /** Implements <code>EditorSetings</code> interface method. */
    public KeyStroke[] getKeyStrokesToggleHighlight() {
        prepareSettings();
        if(keyStrokesToggleHighlight == null || keyStrokesToggleHighlight.length == 0) {
            keyStrokesToggleHighlight = TableViewSettings.TOGGLE_HIGHLIGHT_DEFAULT_KEYSTROKES;
        }
        
        return keyStrokesToggleHighlight;
    }

    /** Implements <code>EditorSetings</code> interface method. */    
    public void settingsUpdated() {
        if (prepared) {
        support.firePropertyChange(new PropertyChangeEvent(this, null, null, null));
    }
    }

    /** Implements <code>EditorSetings</code> interface method. */     
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /** Implements <code>EditorSetings</code> interface method. */    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    /** 
     * Gets only instance of <code>EditorSettindsCopy</code> that is also
     * registered at layer to access it declaratively.
     */
    public synchronized static EditorSettingsCopy getLayerInstance() {
            if(editorSettingsCopy == null) {
                editorSettingsCopy = new EditorSettingsCopy();
            }

        return editorSettingsCopy;
    }

    /** Prepares settings. */
    private void prepareSettings() {
        if (prepared) return;
        
        // Set listening on changes of settings.
        Settings.addSettingsChangeListener(this);

        // Init settings.                            
        updateSettings();
        prepared = true;
    }
    
    
    /**
     * Handle settings change.
     */
    public void settingsChange(SettingsChangeEvent evt) {
        // maybe could be refined
        updateSettings();
    }
    
    /** Updates settings from properties options. Only editor module dependent code. */
    private void updateSettings() {
        if(updateColors())
            updateKeyStrokes();
    }

    /** Updates colors.
     * @return <code>true</code> if colors updated succesfully or <code>false</code> otherwise. */
    private boolean updateColors() {
        PropertiesOptions propertiesOptions = (PropertiesOptions)SharedClassObject.findObject(PropertiesOptions.class, false);
        if(propertiesOptions == null) {
            return false;
        }
        
        // Update colors.
        Map map = propertiesOptions.getColoringMap();
        Coloring keyColoring = (Coloring)map.get(PropertiesTokenContext.contextPath.getFullTokenName(
            PropertiesTokenContext.KEY));
        keyColor = keyColoring.getForeColor();
        keyBackground = keyColoring.getBackColor();
        
        Coloring valueColoring = (Coloring)map.get(PropertiesTokenContext.contextPath.getFullTokenName(
            PropertiesTokenContext.VALUE));
        valueColor = valueColoring.getForeColor();
        valueBackground = valueColoring.getBackColor();
        
        Coloring highlightColoring = (Coloring)map.get(SettingsNames.HIGHLIGHT_SEARCH_COLORING);
        highlightColor = highlightColoring.getForeColor();
        highlightBackground = highlightColoring.getBackColor();

        shadowColor = propertiesOptions.getShadowTableCell();

        // If there is not the colors specified use default inherited colors.
        Color defaultForeground = ((Coloring)map.get("default")).getForeColor(); // NOI18N
        Color defaultBackground = ((Coloring)map.get("default")).getBackColor(); // NOI18N
        
        if(keyColor == null) keyColor = defaultForeground;
        if(keyBackground == null) keyBackground = defaultBackground;
        if(valueColor == null) valueColor = defaultForeground;
        if(valueBackground == null) valueBackground = defaultBackground;
        if(highlightColor == null) highlightColor = new Color(SystemColor.textHighlightText.getRGB());
        if(highlightBackground == null) highlightBackground = new Color(SystemColor.textHighlight.getRGB());
        if(shadowColor == null) shadowColor = new Color(SystemColor.controlHighlight.getRGB());
        
        return true;
    }

    /** Updates keystrokes. Dependent code. */
    private void updateKeyStrokes() {
        // Update keyStrokes.
        // Retrieve key bindings for Propeties Kit and super kits.
        Settings.KitAndValue kv[] = Settings.getValueHierarchy(
            PropertiesKit.class, SettingsNames.KEY_BINDING_LIST);

        // Go through all levels (PropertiesKit and its supeclasses) and collect key bindings.
        HashSet nextKS = new HashSet();
        HashSet prevKS = new HashSet();
        HashSet toggleKS = new HashSet();
        // Loop thru each keylist for the kit class.
        for (int i = kv.length - 1; i >= 0; i--) {
            List keyList = (List)kv[i].value;
            
            JTextComponent.KeyBinding[] bindings = new JTextComponent.KeyBinding[keyList.size()];
            
            keyList.toArray(bindings);
            
            // Loop thru all bindings in the kit class.
            for(int j=0; j<bindings.length; j++) {
                JTextComponent.KeyBinding binding = bindings[j];
                if(binding == null) 
                    continue;
                
                // Find key keystrokes for find next action.
                if(binding.actionName.equals(BaseKit.findNextAction)) {
                    if(binding instanceof MultiKeyBinding && ((MultiKeyBinding)binding).keys != null)
                        for (int k=0; k<((MultiKeyBinding)binding).keys.length; k++)
                            nextKS.add(((MultiKeyBinding)binding).keys[k]);
                    else
                        nextKS.add(binding.key);
                }
                // Find key keystrokes for find previous action.
                if(binding.actionName.equals(BaseKit.findPreviousAction)) {
                    if(binding instanceof MultiKeyBinding && ((MultiKeyBinding)binding).keys != null)
                        for (int k=0; k<((MultiKeyBinding)binding).keys.length; k++)
                            prevKS.add(((MultiKeyBinding)binding).keys[k]);
                    else
                        prevKS.add(binding.key);
                }
                // Find key keystrokes for toggle highlight action.
                if(binding.actionName.equals(BaseKit.toggleHighlightSearchAction)) {
                    if(binding instanceof MultiKeyBinding && ((MultiKeyBinding)binding).keys != null)
                        for (int k=0; k<((MultiKeyBinding)binding).keys.length; k++)
                            toggleKS.add(((MultiKeyBinding)binding).keys[k]);
                    else
                        toggleKS.add(binding.key);
                }
                
            } // End of inner loop.
        } // End of outer loop.
        
        // Copy found values to our variables.
        nextKS.toArray(keyStrokesFindNext = new KeyStroke[nextKS.size()]);
        prevKS.toArray(keyStrokesFindPrevious = new KeyStroke[prevKS.size()]);
        toggleKS.toArray(keyStrokesToggleHighlight = new KeyStroke[toggleKS.size()]);

        // notify listeners about update
        settingsUpdated();
    }
    
}
