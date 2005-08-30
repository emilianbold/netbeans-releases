/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.util.*;
import java.beans.BeanInfo;
import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.SharedClassObject;
import org.netbeans.modules.form.codestructure.*;

/**
 * Settings for the form editor.
 */

public class FormLoaderSettings extends SystemOption {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 8949624818164732719L;

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

    /** Property name of the generateMnemonicsCode property */
    public static final String PROP_GENERATE_MNEMONICS = "generateMnemonicsCode"; // NOI18N
    /** Property name of the showMnemonicsDialog property */
    public static final String PROP_SHOW_MNEMONICS_DIALOG = "showMnemonicsDialog"; // NOI18N

    /** Property name of the displayWritableOnly property */
    public static final String PROP_DISPLAY_WRITABLE_ONLY = "displayWritableOnly"; // NOI18N

    /** Property name of the editorSearchPath property */
    public static final String PROP_EDITOR_SEARCH_PATH = "editorSearchPath"; // NOI18N
    /** Property name of the registeredEditors property */
    public static final String PROP_REGISTERED_EDITORS = "registeredEditors"; // NOI18N

    /** Property name of the toolBarPalette property */
    public static final String PROP_PALETTE_IN_TOOLBAR = "toolBarPalette"; // NOI18N
    /** Property name of the foldGeneratedCode property. */
    public static final String PROP_FOLD_GENERATED_CODE = "foldGeneratedCode"; // NOI18N

//    public static final String PROP_CONTAINER_BEANS = "containerBeans"; // NOI18N

    // ------------------------------------------
    // properties

    /** The name of the Event variable generated in the event handlers. */
    private static String eventVariableName = "evt"; // NOI18N

    /** Style of code generation of event listeners. Possible values defined
     * by JavaCodeGenerator. */
    private static int listenerGenerationStyle;

    /** The size(in pixels) of the border that marks visual components on a form
     * as selected. */
    private static int selectionBorderSize = 1;

    /** The color of the border boxes on selection border */
    private static java.awt.Color selectionBorderColor =
        new java.awt.Color(255, 164, 0);
    /** The color of the border boxes on connection border */
    private static java.awt.Color connectionBorderColor = java.awt.Color.red;
    /** The color of the drag border on selection border */
    private static java.awt.Color dragBorderColor = java.awt.Color.gray;
    /** The color of FormDesigner window's background */
    private static java.awt.Color formDesignerBackgroundColor = java.awt.Color.white;
    /** The color of border around designed component */
    private static java.awt.Color formDesignerBorderColor =
        new java.awt.Color(224, 224, 255);
    /** The color of guiding lines. */
    private static java.awt.Color guidingLineColor =
        new java.awt.Color(143, 171, 196);

    /** The grid size(in pixels) in x axis. */
    private static int gridX = 10;
    /** The grid size(in pixels) in y axis. */
    private static int gridY = 10;
    /** True if grid should be applied to position of components, false otherwise. */
    private static boolean applyGridToPosition = true;
    /** True if grid should be applied to size of components, false otherwise. */
    private static boolean applyGridToSize = true;

    /** The modifiers of variables generated for component in Form Editor */
    private static int variablesModifier = java.lang.reflect.Modifier.PRIVATE;
    /** The local variable generation state for components in Form Editor. */
    private static boolean variablesLocal = false;

    /** If true, special code using org.openide.awt.Mnemonics is generated for
     * buttons and labels. */
    private static boolean generateMnemonicsCode = false;
    /** Indicates whether to display automatically hint dialog advertising the
     * Mnemonics generation feature. */
    private static boolean showMnemonicsDialog = true;
    
    /** If true, only editable properties are displayed in the ComponentInspector */
    private static boolean displayWritableOnly = true;

    /** If true then the generated code will be folded. */
    private static boolean foldGeneratedCode = true;

    /** Array of package names to search for property editors used in Form Editor */
    private static String [] editorSearchPath =
        { "org.netbeans.modules.form.editors2" }; // NOI18N
    /** Array of items [Class Name, Editor1, Editor2, ...] */
    private static String [][] registeredEditors = new String [][] {{}};

    private static boolean toolBarPalette = false;
    private static boolean showComponentsNames = true;
    private static int paletteIconSize = BeanInfo.ICON_COLOR_16x16;

    private static final int MIN_SELECTION_BORDER_SIZE = 1;
    private static final int MAX_SELECTION_BORDER_SIZE = 15;

    private static final int MIN_GRID_X = 2;
    private static final int MIN_GRID_Y = 2;

//    private static Map containerBeans;

    // --------

    public static FormLoaderSettings getInstance() {
        return (FormLoaderSettings)
            SharedClassObject.findObject(FormLoaderSettings.class, true);
    }

    // ------------------------------------------
    // property access methods

    /** Getter for the sortEventSets option */
    public String getEventVariableName() {
        return eventVariableName;
    }

    /** Setter for the sortEventSets option */
    public void setEventVariableName(String value) {
        if (value == eventVariableName)
            return;
        String oldValue = eventVariableName;
        eventVariableName = value;
        firePropertyChange(PROP_EVENT_VARIABLE_NAME, oldValue, eventVariableName);
    }

    /** Getter for the event listener code generation style option. */
    public int getListenerGenerationStyle() {
        return listenerGenerationStyle;
    }

    /** Setter for the event listener code generation style option. */
    public void setListenerGenerationStyle(int style) {
        int oldValue = listenerGenerationStyle;
        listenerGenerationStyle = style;
        firePropertyChange(PROP_LISTENER_GENERATION_STYLE,
                           new Integer(oldValue), new Integer(style));
    }

    /** Getter for the selectionBorderSize option */
    public int getSelectionBorderSize() {
        return selectionBorderSize;
    }

    /** Setter for the selectionBorderSize option */
    public void setSelectionBorderSize(int value) {
        if (value < MIN_SELECTION_BORDER_SIZE)
            value = MIN_SELECTION_BORDER_SIZE;
        else if (value > MAX_SELECTION_BORDER_SIZE)
            value = MAX_SELECTION_BORDER_SIZE;

        if (value == selectionBorderSize)
            return;

        int oldValue = selectionBorderSize;
        selectionBorderSize = value;
        firePropertyChange(PROP_SELECTION_BORDER_SIZE,
                           new Integer(oldValue),
                           new Integer(selectionBorderSize));
    }

    /** Getter for the selectionBorderColor option */
    public java.awt.Color getSelectionBorderColor() {
        return selectionBorderColor;
    }

    /** Setter for the selectionBorderColor option */
    public void setSelectionBorderColor(java.awt.Color value) {
        if (value.equals(selectionBorderColor))
            return;
        java.awt.Color oldValue = selectionBorderColor;
        selectionBorderColor = value;
        firePropertyChange(PROP_SELECTION_BORDER_COLOR,
                           oldValue,
                           selectionBorderColor);
    }

    /** Getter for the connectionBorderColor option */
    public java.awt.Color getConnectionBorderColor() {
        return connectionBorderColor;
    }

    /** Setter for the connectionBorderColor option */
    public void setConnectionBorderColor(java.awt.Color value) {
        if (value.equals(connectionBorderColor))
            return;
        java.awt.Color oldValue = connectionBorderColor;
        connectionBorderColor = value;
        firePropertyChange(PROP_CONNECTION_BORDER_COLOR,
                           oldValue,
                           connectionBorderColor);
    }

    /** Getter for the dragBorderColor option */
    public java.awt.Color getDragBorderColor() {
        return dragBorderColor;
    }

    /** Setter for the dragBorderColor option */
    public void setDragBorderColor(java.awt.Color value) {
        if (value.equals(dragBorderColor))
            return;
        java.awt.Color oldValue = dragBorderColor;
        dragBorderColor = value;
        firePropertyChange(PROP_DRAG_BORDER_COLOR, oldValue, dragBorderColor);
    }
    
    /** Getter for the guidingLineColor option */
    public java.awt.Color getGuidingLineColor() {
        return guidingLineColor;
    }

    /** Setter for the dragBorderColor option */
    public void setGuidingLineColor(java.awt.Color value) {
        if (value.equals(guidingLineColor))
            return;
        java.awt.Color oldValue = guidingLineColor;
        guidingLineColor = value;
        firePropertyChange(PROP_GUIDING_LINE_COLOR, oldValue, guidingLineColor);
    }

    /** Getter for the gridX option */
    public int getGridX() {
        return gridX;
    }

    /** Setter for the gridX option */
    public void setGridX(int value) {
        if (value < MIN_GRID_X) value = MIN_GRID_X;
        if (value == gridX)
            return;
        int oldValue = gridX;
        gridX = value;
        firePropertyChange(PROP_GRID_X, new Integer(oldValue), new Integer(gridX));
    }

    /** Getter for the gridY option */
    public int getGridY() {
        return gridY;
    }

    /** Setter for the gridY option */
    public void setGridY(int value) {
        if (value < MIN_GRID_Y) value = MIN_GRID_Y;
        if (value == gridY)
            return;
        int oldValue = gridY;
        gridY = value;
        firePropertyChange(PROP_GRID_Y, new Integer(oldValue), new Integer(gridY));
    }

    /** Getter for the applyGridToPosition option */
    public boolean getApplyGridToPosition() {
        return applyGridToPosition;
    }

    /** Setter for the applyGridToPosition option */
    public void setApplyGridToPosition(boolean value) {
        if (value == applyGridToPosition)
            return;
        boolean oldValue = applyGridToPosition;
        applyGridToPosition = value;
        firePropertyChange(PROP_APPLY_GRID_TO_POSITION,
                           oldValue ? Boolean.TRUE : Boolean.FALSE,
                           applyGridToPosition ? Boolean.TRUE : Boolean.FALSE);
    }

    /** Getter for the applyGridToSize option */
    public boolean getApplyGridToSize() {
        return applyGridToSize;
    }

    /** Setter for the applyGridToSize option */
    public void setApplyGridToSize(boolean value) {
        if (value == applyGridToSize)
            return;
        boolean oldValue = applyGridToSize;
        applyGridToSize = value;
        firePropertyChange(PROP_APPLY_GRID_TO_SIZE,
                           oldValue ? Boolean.TRUE : Boolean.FALSE,
                           applyGridToSize ? Boolean.TRUE : Boolean.FALSE);
    }

    /** Getter for the variablesLocal option. */
    public boolean getVariablesLocal() {
        return variablesLocal;
    }

    /** Setter for the variablesLocal option. */
    public void setVariablesLocal(boolean value) {
        boolean oldValue = variablesLocal;
        variablesLocal = value;

        int varType = variablesLocal ?
            CodeVariable.LOCAL | (variablesModifier & CodeVariable.FINAL)
                               | CodeVariable.EXPLICIT_DECLARATION
            :
            CodeVariable.FIELD | variablesModifier;
        CodeStructure.setGlobalDefaultVariableType(varType);

        int oldModif = variablesModifier;
        if (variablesLocal)
            variablesModifier &= CodeVariable.FINAL;

        firePropertyChange(PROP_VARIABLES_LOCAL,
                           oldValue ? Boolean.TRUE : Boolean.FALSE,
                           variablesLocal ? Boolean.TRUE : Boolean.FALSE);
        firePropertyChange(PROP_VARIABLES_MODIFIER,
                           new Integer(oldModif),
                           new Integer(variablesModifier));
    }

    /** Getter for the variablesModifier option */
    public int getVariablesModifier() {
        return variablesModifier;
    }

    /** Setter for the variablesModifier option */
    public void setVariablesModifier(int value) {
        int oldValue = variablesModifier;
        variablesModifier = value;

        int varType;
        if (variablesLocal) {
            varType = CodeVariable.LOCAL | variablesModifier;
            if ((variablesModifier & CodeVariable.FINAL) == 0)
                varType |= CodeVariable.EXPLICIT_DECLARATION;
        }
        else varType = CodeVariable.FIELD | variablesModifier;
        CodeStructure.setGlobalDefaultVariableType(varType);

        firePropertyChange(PROP_VARIABLES_MODIFIER,
                           new Integer(oldValue),
                           new Integer(variablesModifier));
    }

    /** Getter for the generateMnemonicsCode option */
    public boolean getGenerateMnemonicsCode() {
        return generateMnemonicsCode;
    }

    /** Setter for the generateMnemonicsCode option */
    public void setGenerateMnemonicsCode(boolean value) {
        Boolean oldValue = generateMnemonicsCode ? Boolean.TRUE : Boolean.FALSE;
        generateMnemonicsCode = value;
        firePropertyChange(PROP_GENERATE_MNEMONICS,
                           oldValue, 
                           value ? Boolean.TRUE : Boolean.FALSE);
    }

    /** Getter for the showMnemonicsDialog option */
    public boolean getShowMnemonicsDialog() {
        return showMnemonicsDialog;
    }

    /** Setter for the showMnemonicsDialog option */
    public void setShowMnemonicsDialog(boolean value) {
        Boolean oldValue = showMnemonicsDialog ? Boolean.TRUE : Boolean.FALSE;
        showMnemonicsDialog = value;
        firePropertyChange(PROP_SHOW_MNEMONICS_DIALOG,
                           oldValue, 
                           value ? Boolean.TRUE : Boolean.FALSE);
    }

    /** Getter for the displayWritableOnly option */
    public boolean getDisplayWritableOnly() {
        return displayWritableOnly;
    }

    /** Setter for the displayWritableOnly option */
    public void setDisplayWritableOnly(boolean value) {
        Boolean oldValue = displayWritableOnly ? Boolean.TRUE : Boolean.FALSE;
        displayWritableOnly = value;
        firePropertyChange(PROP_DISPLAY_WRITABLE_ONLY,
                           oldValue,
                           displayWritableOnly ? Boolean.TRUE : Boolean.FALSE);
    }

    /** Getter for the editorSearchPath option */
    public String[] getEditorSearchPath() {
        return editorSearchPath;
    }

    /** Setter for the editorSearchPath option */
    public void setEditorSearchPath(String[] value) {
        String[] oldValue = editorSearchPath;
        editorSearchPath = value;
        FormPropertyEditorManager.clearEditorsCache(); // clear the editors cache so that the new editors can be used
        firePropertyChange(PROP_EDITOR_SEARCH_PATH, oldValue, editorSearchPath);
    }

    /** Getter for the registeredEditors option */
    public String[][] getRegisteredEditors() {
        return registeredEditors;
    }

    /** Setter for the registeredEditors option */
    public void setRegisteredEditors(String[][] value) {
        String[][] oldValue = registeredEditors;
        registeredEditors = value;
        FormPropertyEditorManager.clearEditorsCache(); // clear the editors cache so that the new editors can be used
        firePropertyChange(PROP_REGISTERED_EDITORS, oldValue, registeredEditors);
    }

    public String[] getRegisteredEditor(int index) {
        return registeredEditors[index];
    }

    public void setRegisteredEditor(int index, String[] value) {
        registeredEditors[index] = value;
        FormPropertyEditorManager.clearEditorsCache(); // clear the editors cache so that the new editors can be used
        firePropertyChange(PROP_REGISTERED_EDITORS, null, null);
    }

    public boolean isPaletteInToolBar() {
        return toolBarPalette;
    }

    public void setPaletteInToolBar(boolean value) {
        if (toolBarPalette == value)
            return;

        boolean oldValue = toolBarPalette;
        toolBarPalette = value;

        firePropertyChange(PROP_PALETTE_IN_TOOLBAR,
                           oldValue ? Boolean.TRUE : Boolean.FALSE,
                           value ?  Boolean.TRUE : Boolean.FALSE);
    }

    /** Getter for the formDesignerBackgroundColor option */
    public java.awt.Color getFormDesignerBackgroundColor() {
        return formDesignerBackgroundColor;
    }

    /** Setter for the formDesignerBackgroundColor option */
    public void setFormDesignerBackgroundColor(java.awt.Color value) {
        if (value.equals(formDesignerBackgroundColor))
            return;
        java.awt.Color oldValue = formDesignerBackgroundColor;
        formDesignerBackgroundColor = value;
        firePropertyChange(PROP_FORMDESIGNER_BACKGROUND_COLOR,
                           oldValue,
                           formDesignerBackgroundColor);
    }

    /** Getter for the formDesignerBorderColor option */
    public java.awt.Color getFormDesignerBorderColor() {
        return formDesignerBorderColor;
    }

    /** Setter for the formDesignerBorderColor option */
    public void setFormDesignerBorderColor(java.awt.Color value) {
        if (value.equals(formDesignerBorderColor))
            return;
        java.awt.Color oldValue = formDesignerBorderColor;
        formDesignerBorderColor = value;
        firePropertyChange(PROP_FORMDESIGNER_BORDER_COLOR,
                           oldValue,
                           formDesignerBorderColor);
    }
    
    /** Getter for the foldGeneratedCode option */
    public boolean getFoldGeneratedCode() {
        return foldGeneratedCode;
    }

    /** Setter for the foldGeneratedCode option */
    public void setFoldGeneratedCode(boolean value) {
        Boolean oldValue = foldGeneratedCode ? Boolean.TRUE : Boolean.FALSE;
        foldGeneratedCode = value;
        firePropertyChange(PROP_FOLD_GENERATED_CODE,
                           oldValue, 
                           value ? Boolean.TRUE : Boolean.FALSE);
    }

//    public Map getContainerBeans() {
//        return containerBeans;
//    }
//
//    public void setContainerBeans(Map map) {
//        containerBeans = map;
//        firePropertyChange(PROP_CONTAINER_BEANS, null, null);
//    }

    // XXX(-tdt) Hmm, backward compatibility with com.netbeans package name
    // again. The property editor search path is stored in user settings, we
    // must translate
    public void readExternal(java.io.ObjectInput in)
        throws java.io.IOException, ClassNotFoundException
    {
        super.readExternal(in);
        for (int i = 0; i < editorSearchPath.length; i++) {
            String path = editorSearchPath[i];
            path = org.openide.util.Utilities.translate(path + ".BogusClass"); // NOI18N
            path = path.substring(0, path.length() - ".BogusClass".length()); // NOI18N
            editorSearchPath[i] = path;
        }
    }

    /** This method must be overriden. It returns display name of this options.
     */
    public String displayName() {
        return FormUtils.getBundleString("CTL_FormSettings"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.configuring"); // NOI18N
    }
}
