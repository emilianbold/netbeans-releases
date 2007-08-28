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

package org.netbeans.modules.form;

import java.awt.Color;
import java.util.*;
import java.util.prefs.Preferences;
import org.openide.util.HelpCtx;
import org.netbeans.modules.form.codestructure.*;
import org.openide.nodes.BeanNode;
import org.openide.util.NbPreferences;

/**
 * Settings for the form editor.
 */

public class FormLoaderSettings  {
    private static final FormLoaderSettings INSTANCE = new FormLoaderSettings();
    public static final String PROP_USE_INDENT_ENGINE = "useIndentEngine"; // NOI18N

    /** Property name of the eventVariableName property */
    public static final String PROP_EVENT_VARIABLE_NAME = "eventVariableName"; // NOI18N

    /** Property name of the event listener code generation style option. */
    public static final String PROP_LISTENER_GENERATION_STYLE = "listenerGenerationStyle"; // NOI18N

    /** Property name of the selectionBorderSize property */
    public static final String PROP_SELECTION_BORDER_SIZE = "selectionBorderSize"; // NOI18N
    /** Property name of the selectionBorderColor property */
    public static final String PROP_SELECTION_BORDER_COLOR = "selectionBorderColor"; // NOI18N
    /** Property name of the connectionBorderColor property */
    public static final String PROP_CONNECTION_BORDER_COLOR = "connectionBorderColor"; // NOI18N
    /** Property name of the dragBorderColor property */
    public static final String PROP_DRAG_BORDER_COLOR = "dragBorderColor"; // NOI18N
    /** Property name of the guidingLineColor property */
    public static final String PROP_GUIDING_LINE_COLOR = "guidingLineColor"; // NOI18N
    /** Property name of the formDesignerBackgroundColor property */
    public static final String PROP_FORMDESIGNER_BACKGROUND_COLOR =
        "formDesignerBackgroundColor"; // NOI18N
    /** Property name of the formDesignerBorderColor property */
    public static final String PROP_FORMDESIGNER_BORDER_COLOR =
        "formDesignerBorderColor"; // NOI18N

    /** Property name of the gridX property */
    public static final String PROP_GRID_X = "gridX"; // NOI18N
    /** Property name of the gridY property */
    public static final String PROP_GRID_Y = "gridY"; // NOI18N
    /** Property name of the applyGridToPosition property */
    public static final String PROP_APPLY_GRID_TO_POSITION = "applyGridToPosition"; // NOI18N
    /** Property name of the applyGridToSize property */
    public static final String PROP_APPLY_GRID_TO_SIZE = "applyGridToSize"; // NOI18N

    /** Property name of the variablesModifier property */
    public static final String PROP_VARIABLES_MODIFIER = "variablesModifier"; // NOI18N
    /** Property name of the variablesLocal property */
    public static final String PROP_VARIABLES_LOCAL = "variablesLocal"; // NOI18N

    /** Property name of the autoSetComponentName property */
    public static final String PROP_AUTO_SET_COMPONENT_NAME = "autoSetComponentName"; // NOI18N
    static final int AUTO_NAMING_DEFAULT = 0;
    static final int AUTO_NAMING_ON = 1;
    static final int AUTO_NAMING_OFF = 2;

    /** Property name of the generateMnemonicsCode property */
    public static final String PROP_GENERATE_MNEMONICS = "generateMnemonicsCode"; // NOI18N
    /** Property name of the showMnemonicsDialog property */
    public static final String PROP_SHOW_MNEMONICS_DIALOG = "showMnemonicsDialog"; // NOI18N

    /** Property name of the displayWritableOnly property */
    public static final String PROP_DISPLAY_WRITABLE_ONLY = "displayWritableOnly"; // NOI18N

    /** Property name of the editorSearchPath property */
    public static final String PROP_EDITOR_SEARCH_PATH = "editorSearchPath"; // NOI18N

    /** Property name of the toolBarPalette property */
    public static final String PROP_PALETTE_IN_TOOLBAR = "toolBarPalette"; // NOI18N
    /** Property name of the foldGeneratedCode property. */
    public static final String PROP_FOLD_GENERATED_CODE = "foldGeneratedCode"; // NOI18N
    /** Property name of the assistantShown property. */
    public static final String PROP_ASSISTANT_SHOWN = "assistantShown"; // NOI18N
    /** Property name of the designerLAF property. */
    public static final String PROP_DESIGNER_LAF = "designerLAF"; // NOI18N

    /** Property name of the layout code target property. */
    public static final String PROP_LAYOUT_CODE_TARGET = "layoutCodeTarget"; // NOI18N

    /** Name of the property for automatic resources/i18n management.
     * The name refers only to i18n for compatibility reasons. */
    public static final String PROP_AUTO_RESOURCING = "i18nAutoMode"; // NOI18N
    static final int AUTO_RESOURCE_DEFAULT = 0;
    static final int AUTO_RESOURCE_ON = 1;
    static final int AUTO_RESOURCE_OFF = 2;
    //    public static final String PROP_CONTAINER_BEANS = "containerBeans"; // NOI18N

    // ------------------------------------------
    /** The color of the drag border on selection border */
    private static java.awt.Color dragBorderColor = java.awt.Color.gray;
    /** Indicates whether to display automatically hint dialog advertising the
     * Mnemonics generation feature. */
    private static boolean showMnemonicsDialog = true;
    /** Array of package names to search for property editors used in Form Editor */
    private static String[] editorSearchPath = null;
    /** Array of items [Class Name, Editor1, Editor2, ...] */
    private static String [][] registeredEditors;
    private static final int MIN_SELECTION_BORDER_SIZE = 1;
    private static final int MAX_SELECTION_BORDER_SIZE = 15;

    private static final int MIN_GRID_X = 2;
    private static final int MIN_GRID_Y = 2;

//    private static Map containerBeans;

    // --------

    public static final Preferences getPreferences() {
        return NbPreferences.forModule(FormLoaderSettings.class);
    }
    
    public static FormLoaderSettings getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------
    // property access methods

    public boolean getUseIndentEngine() {
        return getPreferences().getBoolean(PROP_USE_INDENT_ENGINE, false); 
    }

    public void setUseIndentEngine(boolean value) {
        getPreferences().putBoolean(PROP_USE_INDENT_ENGINE, value);
    }
    
    /** Getter for the sortEventSets option */
    public String getEventVariableName() {
        return getPreferences().get(PROP_EVENT_VARIABLE_NAME, "evt");
    }

    /** Setter for the sortEventSets option */
    public void setEventVariableName(String value) {
        getPreferences().put(PROP_EVENT_VARIABLE_NAME, value);
    }

    /** Getter for the event listener code generation style option. */
    public int getListenerGenerationStyle() {
        return getPreferences().getInt(PROP_LISTENER_GENERATION_STYLE, 0);
    }

    /** Setter for the event listener code generation style option. */
    public void setListenerGenerationStyle(int style) {
        getPreferences().putInt(PROP_LISTENER_GENERATION_STYLE, style);
    }

    /** Getter for the selectionBorderSize option */
    public int getSelectionBorderSize() {
        return getPreferences().getInt(PROP_SELECTION_BORDER_SIZE, 1);
    }

    /** Setter for the selectionBorderSize option */
    public void setSelectionBorderSize(int value) {
        if (value < MIN_SELECTION_BORDER_SIZE)
            value = MIN_SELECTION_BORDER_SIZE;
        else if (value > MAX_SELECTION_BORDER_SIZE)
            value = MAX_SELECTION_BORDER_SIZE;

        
        getPreferences().putInt(PROP_SELECTION_BORDER_SIZE, value);
    }

    /** Getter for the selectionBorderColor option */
    public java.awt.Color getSelectionBorderColor() {
        int rgb = getPreferences().getInt(PROP_SELECTION_BORDER_COLOR, new Color(255, 164, 0).getRGB());                
        return new Color(rgb);
    }

    /** Setter for the selectionBorderColor option */
    public void setSelectionBorderColor(java.awt.Color value) {
        if (value == null) {
            return;
        }
        getPreferences().putInt(PROP_SELECTION_BORDER_COLOR, value.getRGB());
    }

    /** Getter for the connectionBorderColor option */
    public java.awt.Color getConnectionBorderColor() {
        int rgb = getPreferences().getInt(PROP_CONNECTION_BORDER_COLOR, Color.red.getRGB());
        return new Color(rgb);
    }

    /** Setter for the connectionBorderColor option */
    public void setConnectionBorderColor(java.awt.Color value) {
        if (value == null) {
            return;
        }
        getPreferences().putInt(PROP_CONNECTION_BORDER_COLOR, value.getRGB());
    }

    /** Getter for the dragBorderColor option */
    public java.awt.Color getDragBorderColor() {
        int rgb = getPreferences().getInt(PROP_DRAG_BORDER_COLOR, Color.gray.getRGB());
        return new Color(rgb);        
    }

    /** Setter for the dragBorderColor option */
    public void setDragBorderColor(java.awt.Color value) {
        if (value == null) {
            return;
        }        
        getPreferences().putInt(PROP_DRAG_BORDER_COLOR, value.getRGB());
    }
    
    /** Getter for the guidingLineColor option */
    public java.awt.Color getGuidingLineColor() {
        int rgb = getPreferences().getInt(PROP_GUIDING_LINE_COLOR, new Color(143, 171, 196).getRGB());
        return new Color(rgb);        
        
    }

    /** Setter for the dragBorderColor option */
    public void setGuidingLineColor(java.awt.Color value) {
        if (value == null) {
            return;
        }        
        getPreferences().putInt(PROP_GUIDING_LINE_COLOR, value.getRGB());
    }

    /** Getter for the gridX option */
    public int getGridX() {
        return getPreferences().getInt(PROP_GRID_X, 10);
    }

    /** Setter for the gridX option */
    public void setGridX(int value) {
        if (value < MIN_GRID_X) value = MIN_GRID_X;
        getPreferences().putInt(PROP_GRID_X, value);
    }

    /** Getter for the gridY option */
    public int getGridY() {        
        return getPreferences().getInt(PROP_GRID_Y, 10);
    }

    /** Setter for the gridY option */
    public void setGridY(int value) {
        if (value < MIN_GRID_Y) value = MIN_GRID_Y;
        getPreferences().putInt(PROP_GRID_Y, value);
    }

    /** Getter for the applyGridToPosition option */
    public boolean getApplyGridToPosition() {
        return getPreferences().getBoolean(PROP_APPLY_GRID_TO_POSITION, true);
    }

    /** Setter for the applyGridToPosition option */
    public void setApplyGridToPosition(boolean value) {
        getPreferences().putBoolean(PROP_APPLY_GRID_TO_POSITION, value);
    }

    /** Getter for the applyGridToSize option */
    public boolean getApplyGridToSize() {
        return getPreferences().getBoolean(PROP_APPLY_GRID_TO_SIZE, true);
        
    }

    /** Setter for the applyGridToSize option */
    public void setApplyGridToSize(boolean value) {
        getPreferences().putBoolean(PROP_APPLY_GRID_TO_SIZE, value);    
    }

    /** Getter for the variablesLocal option. */
    public boolean getVariablesLocal() {
        return getPreferences().getBoolean(PROP_VARIABLES_LOCAL, false);
    }

    /** Setter for the variablesLocal option. */
    public void setVariablesLocal(boolean value) {
        getPreferences().putBoolean(PROP_VARIABLES_LOCAL, value);
        int variablesModifier = getVariablesModifier();        
        int varType = value ?
            CodeVariable.LOCAL | (variablesModifier & CodeVariable.FINAL)
                               | CodeVariable.EXPLICIT_DECLARATION
            :
            CodeVariable.FIELD | variablesModifier;

        if (value) {            
            variablesModifier &= CodeVariable.FINAL;
            setVariablesModifier(variablesModifier);
        }
    }

    /** Getter for the variablesModifier option */
    public int getVariablesModifier() {
        return getPreferences().getInt(PROP_VARIABLES_MODIFIER, java.lang.reflect.Modifier.PRIVATE);
    }

    /** Setter for the variablesModifier option */
    public void setVariablesModifier(int value) {
        getPreferences().putInt(PROP_VARIABLES_MODIFIER, value);

        int varType;
        if (getVariablesLocal()) {
            varType = CodeVariable.LOCAL | value;
            if ((value & CodeVariable.FINAL) == 0)
                varType |= CodeVariable.EXPLICIT_DECLARATION;
        }
        else varType = CodeVariable.FIELD | value;
    }

    public int getAutoSetComponentName() {
        return getPreferences().getInt(PROP_AUTO_SET_COMPONENT_NAME, AUTO_NAMING_DEFAULT);
    }

    public void setAutoSetComponentName(int value) {
        getPreferences().putInt(PROP_AUTO_SET_COMPONENT_NAME, value);
    }

    /** Getter for the generateMnemonicsCode option */
    public boolean getGenerateMnemonicsCode() {
        return getPreferences().getBoolean(PROP_GENERATE_MNEMONICS, false);
    }

    /** Setter for the generateMnemonicsCode option */
    public void setGenerateMnemonicsCode(boolean value) {
        getPreferences().putBoolean(PROP_GENERATE_MNEMONICS, value);
    }

    /** Getter for the displayWritableOnly option */
    public boolean getDisplayWritableOnly() {
        return getPreferences().getBoolean(PROP_DISPLAY_WRITABLE_ONLY, true);
    }

    /** Setter for the displayWritableOnly option */
    public void setDisplayWritableOnly(boolean value) {
        getPreferences().putBoolean(PROP_DISPLAY_WRITABLE_ONLY, value);
    }

    /** Getter for the editorSearchPath option */
    public String[] getEditorSearchPath() {
        if (editorSearchPath == null) {
            editorSearchPath = translatedEditorSearchPath(
                    toArray(getPreferences().get(PROP_EDITOR_SEARCH_PATH, "org.netbeans.modules.form.editors2 , org.netbeans.modules.swingapp"))); // NOI18N
        }
        return editorSearchPath;
    }

    /** Setter for the editorSearchPath option */
    public void setEditorSearchPath(String[] value) {
        editorSearchPath = value;
        getPreferences().put(PROP_EDITOR_SEARCH_PATH, fromArray(editorSearchPath));//NOI18N
    }

    public boolean isPaletteInToolBar() {
        return getPreferences().getBoolean(PROP_PALETTE_IN_TOOLBAR, false);
    }

    public void setPaletteInToolBar(boolean value) {
        getPreferences().putBoolean(PROP_PALETTE_IN_TOOLBAR, value);    
    }

    /** Getter for the formDesignerBackgroundColor option */
    public java.awt.Color getFormDesignerBackgroundColor() {
        int rgb = getPreferences().getInt(PROP_FORMDESIGNER_BACKGROUND_COLOR , Color.white.getRGB());
        return new Color(rgb);        
        
    }

    /** Setter for the formDesignerBackgroundColor option */
    public void setFormDesignerBackgroundColor(java.awt.Color value) {
        if (value == null)
            return;
        getPreferences().putInt(PROP_FORMDESIGNER_BACKGROUND_COLOR , value.getRGB());    
    }

    /** Getter for the formDesignerBorderColor option */
    public java.awt.Color getFormDesignerBorderColor() {
        int rgb = getPreferences().getInt(PROP_FORMDESIGNER_BORDER_COLOR , new Color(224, 224, 255).getRGB());
        return new Color(rgb);        
        
    }

    /** Setter for the formDesignerBorderColor option */
    public void setFormDesignerBorderColor(java.awt.Color value) {
        if (value == null)
            return;
        getPreferences().putInt(PROP_FORMDESIGNER_BORDER_COLOR , value.getRGB());    
    }
    
    /** Getter for the foldGeneratedCode option */
    public boolean getFoldGeneratedCode() {
        return getPreferences().getBoolean(PROP_FOLD_GENERATED_CODE, true);    
    }

    /** Setter for the foldGeneratedCode option */
    public void setFoldGeneratedCode(boolean value) {
        getPreferences().putBoolean(PROP_FOLD_GENERATED_CODE, value);    
    }

    /** Getter for the assistantShown option */
    public boolean getAssistantShown() {
        return getPreferences().getBoolean(PROP_ASSISTANT_SHOWN, true);    
    }

    /** Setter for the foldGeneratedCode option */
    public void setAssistantShown(boolean value) {
        getPreferences().putBoolean(PROP_ASSISTANT_SHOWN, value);    
    }

    public int getLayoutCodeTarget() {
        return getPreferences().getInt(PROP_LAYOUT_CODE_TARGET, 0);    
    }

    public void setLayoutCodeTarget(int target) {
        getPreferences().putInt(PROP_LAYOUT_CODE_TARGET, target);    
    }

    public int getI18nAutoMode() {
        return getPreferences().getInt(PROP_AUTO_RESOURCING, 0);    
    }

    public void setI18nAutoMode(int mode) {
        getPreferences().putInt(PROP_AUTO_RESOURCING, mode);    
    }

    private static String[][] toArray2(String esp) {
        List<String[]> retval = new ArrayList<String[]> ();//NOI18N
        String[] items = esp.split(" | ");//NOI18N
        for (int i = 0; i < items.length; i++) {
            String s = items[i];
            retval.add(toArray(s));
        }        
        return retval.toArray(new String [][] {{}});
    }
    
    private static String fromArray2(String[][] items) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < items.length; i++) {
            sb.append(fromArray(items[i]));
            if (i < items.length-1) {
                sb.append(" | ");//NOI18N
            }
        }
        return sb.toString();        
    }
        
    private static String[] toArray(String esp) {
        return esp.split(" , ");//NOI18N
    }
    
    private static String fromArray(String[] items) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < items.length; i++) {
            sb.append(items[i]);
            if (i < items.length-1) {
                sb.append(" , ");//NOI18N
            }
        }
        return sb.toString();        
    }
    
    // XXX(-tdt) Hmm, backward compatibility with com.netbeans package name
    // again. The property editor search path is stored in user settings, we
    // must translate    
    private  static String[] translatedEditorSearchPath(String[] eSearchPath) {
        String[] retval = new String[eSearchPath.length];
        for (int i = 0; i < eSearchPath.length; i++) {
            String path = eSearchPath[i];
            path = org.openide.util.Utilities.translate(path + ".BogusClass"); // NOI18N
            path = path.substring(0, path.length() - ".BogusClass".length()); // NOI18N
            retval[i] = path;
        }        
        return retval;
    }

    /** This method must be overriden. It returns display name of this options.
     */
    public String displayName() {
        return FormUtils.getBundleString("CTL_FormSettings"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.configuring"); // NOI18N
    }
    
    private static BeanNode createViewNode() throws java.beans.IntrospectionException {
        return new BeanNode(FormLoaderSettings.getInstance());
    }         
}
