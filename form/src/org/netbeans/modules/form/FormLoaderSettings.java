/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.form;

import java.lang.reflect.Modifier;
import java.util.ArrayList;

import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;

/** Settings for form data loader.
 *
 * @author Ian Formanek
 */
public class FormLoaderSettings extends SystemOption {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 8949624818164732719L;

    /** Property name of the workspace property */
    public static final String PROP_WORKSPACE = "workspace"; // NOI18N
    /** Property name of the indentAWTHierarchy property */
    public static final String PROP_INDENT_AWT_HIERARCHY = "indentAWTHierarchy"; // NOI18N
    /** Property name of the eventVariableName property */
    public static final String PROP_EVENT_VARIABLE_NAME = "eventVariableName"; // NOI18N

    /** Property name of the selectionBorderSize property */
    public static final String PROP_SELECTION_BORDER_SIZE = "selectionBorderSize"; // NOI18N
    /** Property name of the selectionBorderColor property */
    public static final String PROP_SELECTION_BORDER_COLOR = "selectionBorderColor"; // NOI18N
    /** Property name of the connectionBorderColor property */
    public static final String PROP_CONNECTION_BORDER_COLOR = "connectionBorderColor"; // NOI18N
    /** Property name of the dragBorderColor property */
    public static final String PROP_DRAG_BORDER_COLOR = "dragBorderColor"; // NOI18N
    /** Property name of the showGrid property */
//    public static final String PROP_SHOW_GRID = "showGrid"; // NOI18N
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
    /** Property name of the displayWritableOnly property */
    public static final String PROP_DISPLAY_WRITABLE_ONLY = "displayWritableOnly"; // NOI18N

    /** Property name of the editorSearchPath property */
    public static final String PROP_EDITOR_SEARCH_PATH = "editorSearchPath"; // NOI18N
    /** Property name of the registeredEditors property */
    public static final String PROP_REGISTERED_EDITORS = "registeredEditors"; // NOI18N

    /** outputLevel property name */
    public static final String PROP_OUTPUT_LEVEL = "outputLevel"; // NOI18N

    /** Property name of the nullLayout property */
//    public static final String PROP_NULL_LAYOUT = "nullLayout"; // NOI18N

    public static final String PROP_PALETTE_TABS_VISIBLE = "paletteTabsVisible"; // NOI18N

    /** Minimum output detail level */
    public static final int OUTPUT_MINIMUM = 0;
    /** Normal output detail level */
    public static final int OUTPUT_NORMAL = 1;
    /** Maximum output detail level */
    public static final int OUTPUT_MAXIMUM = 2;

    // ------------------------------------------
    // properties

    private static String workspace = FormEditor.GUI_EDITING_WORKSPACE_NAME;
    /** If true, the generated code for AWT components' hierarchy
     * is indented to reflect the hierarchy(i.e. the code for subcomponents of
     * Container is indented to the right).
     */
    private static boolean indentAWTHierarchy = false;

    /** The name of the Event variable generated in the event handlers. */
    private static String eventVariableName = "evt"; // NOI18N
    /** The size(in pixels) of the border that marks visual components on a form
     * as selected. */
    private static int selectionBorderSize = 2;
    /** The color of the border boxes on selection border */
    private static java.awt.Color selectionBorderColor = new java.awt.Color(0, 0, 192);
    /** The color of the border boxes on connection border */
    private static java.awt.Color connectionBorderColor = java.awt.Color.red;
    /** The color of the drag border on selection border */
    private static java.awt.Color dragBorderColor = java.awt.Color.gray;
    /** True if grid should be used, false otherwise. */
//    private static boolean showGrid = true;
    /** The grid size(in pixels) in x axis. */
    private static int gridX = 10;
    /** The grid size(in pixels) in y axis. */
    private static int gridY = 10;
    /** True if grid should be applied to position of components, false otherwise. */
    private static boolean applyGridToPosition = true;
    /** True if grid should be applied to size of components, false otherwise. */
    private static boolean applyGridToSize = true;
    /** The modifiers of variables generated for component in Form Editor */
    private static int variablesModifier = Modifier.PRIVATE;

    /** If true, only editable properties are displayed in the ComponentInspector */
    private static boolean displayWritableOnly = true;
    /** Array of package names to search for property editors used in Form Editor */
    private static String [] editorSearchPath;
    /** Array of items [Class Name, Editor1, Editor2, ...] */
    private static String [][] registeredEditors = new String [][] {
        { "byte", "sun.beans.editors.ByteEditor" }, // NOI18N
        { "short", "sun.beans.editors.ShortEditor" }, // NOI18N
        { "integer", "sun.beans.editors.IntEditor" }, // NOI18N
        { "long" ,"sun.beans.editors.LongEditor" }, // NOI18N
        { "boolean", "sun.beans.editors.BoolEditor" }, // NOI18N
        { "float", "sun.beans.editors.FloatEditor" }, // NOI18N
        { "double", "sun.beans.editors.DoubleEditor" }, // NOI18N
        { String[].class.getName(), "org.openide.explorer.propertysheet.editors.StringArrayEditor"}, // NOI18N
    };

//    private static int outputLevel = OUTPUT_NORMAL;

//    private static boolean nullLayout = true;

    private static int emptyFormType = 0;

    private static boolean paletteTabsVisible = true;

    private static final int MIN_SELECTION_BORDER_SIZE = 1;
    private static final int MAX_SELECTION_BORDER_SIZE = 15;

    private static final int MIN_GRID_X = 2;
    private static final int MIN_GRID_Y = 2;

    static {
        String[] defaultPath = java.beans.PropertyEditorManager.getEditorSearchPath();
        editorSearchPath = new String[defaultPath.length + 1];
        System.arraycopy(defaultPath, 0, editorSearchPath, 0, defaultPath.length);
        editorSearchPath[editorSearchPath.length-1] = "org.netbeans.modules.form.editors2"; // NOI18N
    }

    // ------------------------------------------
    // property access methods

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String newWorkspace) {
        workspace = newWorkspace;
    }

    public int getEmptyFormType() {
        return emptyFormType;
    }

    public void setEmptyFormType(int value) {
        emptyFormType = value;
    }

    /** Getter for the IndentAWTHierarchy option */
    public boolean getIndentAWTHierarchy() {
        return indentAWTHierarchy;
    }

    /** Setter for the IndentAWTHierarchy option */
    public void setIndentAWTHierarchy(boolean value) {
        if (value == indentAWTHierarchy)
            return;
        indentAWTHierarchy = value;
        firePropertyChange(PROP_INDENT_AWT_HIERARCHY,
                           new Boolean(!indentAWTHierarchy), new Boolean(indentAWTHierarchy));
    }

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

    /** Getter for the selectionBorderSize option */
    public int getSelectionBorderSize() {
        return selectionBorderSize;
    }

    /** Setter for the selectionBorderSize option */
    public void setSelectionBorderSize(int value) {
        if (value < MIN_SELECTION_BORDER_SIZE) value = MIN_SELECTION_BORDER_SIZE;
        else if (value > MAX_SELECTION_BORDER_SIZE) value = MAX_SELECTION_BORDER_SIZE;

        if (value == selectionBorderSize)
            return;
        int oldValue = selectionBorderSize;
        selectionBorderSize = value;
        firePropertyChange(PROP_SELECTION_BORDER_SIZE, new Integer(oldValue), new Integer(selectionBorderSize));
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
        firePropertyChange(PROP_SELECTION_BORDER_COLOR, oldValue, selectionBorderColor);
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
        firePropertyChange(PROP_CONNECTION_BORDER_COLOR, oldValue, connectionBorderColor);
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

    /** Getter for the showGrid option */
/*    public boolean getShowGrid() {
        return showGrid;
    } */

    /** Setter for the showGrid option */
/*    public void setShowGrid(boolean value) {
        if (value == showGrid)
            return;
        boolean oldValue = showGrid;
        showGrid = value;
        firePropertyChange(PROP_SHOW_GRID, new Boolean(oldValue), new Boolean(showGrid));
    } */

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
        firePropertyChange(PROP_APPLY_GRID_TO_POSITION, new Boolean(oldValue), new Boolean(applyGridToPosition));
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
        firePropertyChange(PROP_APPLY_GRID_TO_SIZE, new Boolean(oldValue), new Boolean(applyGridToSize));
    }

    /** Getter for the variablesModifier option */
    public int getVariablesModifier() {
        return variablesModifier;
    }

    /** Setter for the variablesModifier option */
    public void setVariablesModifier(int value) {
        int oldValue = variablesModifier;
        variablesModifier = value;
        firePropertyChange(PROP_VARIABLES_MODIFIER, new Integer(oldValue), new Integer(variablesModifier));
    }

    /** Getter for the displayWritableOnly option */
    public boolean getDisplayWritableOnly() {
        return displayWritableOnly;
    }

    /** Setter for the displayWritableOnly option */
    public void setDisplayWritableOnly(boolean value) {
        Boolean oldValue = new Boolean(displayWritableOnly);
        displayWritableOnly = value;
        firePropertyChange(PROP_DISPLAY_WRITABLE_ONLY, oldValue, new Boolean(displayWritableOnly));
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

    /** Getter for OutputLevel property.
     * @return The level of output
     */
//    public int getOutputLevel() {
//        return outputLevel;
//    }

    /** Setter for OutputLevel property.
     * @param value The new level of output
     */
//    public void setOutputLevel(int value) {
//        if (outputLevel == value) return;
//        int oldValue = outputLevel;
//        outputLevel = value;
//        // fire the PropertyChange
//        firePropertyChange(PROP_OUTPUT_LEVEL, new Integer(oldValue), new Integer(outputLevel));
//    }

    /** Getter for nullLayout property.
     * @return True, if null layout should be generated, false if org.netbeans.lib.awtextra.AbsoluteLayout should be used
     */
//    public boolean isNullLayout() {
//        return nullLayout;
//    }

    /** Setter for nullLayout property.
     * @param value True, if null layout should be generated, false if org.netbeans.lib.awtextra.AbsoluteLayout should be used
     */
//    public void setNullLayout(boolean value) {
//        if (nullLayout == value) return;
//        boolean oldValue = nullLayout;
//        nullLayout = value;
//        // fire the PropertyChange
//        firePropertyChange(PROP_NULL_LAYOUT, new Boolean(oldValue), new Boolean(nullLayout));
//    }

    public boolean getPaletteTabsVisible() {
        return paletteTabsVisible;
    }

    public void setPaletteTabsVisible(boolean value) {
        if (value == paletteTabsVisible) return;
        paletteTabsVisible = value;
        // fire the PropertyChange
        firePropertyChange(PROP_PALETTE_TABS_VISIBLE, new Boolean(!value), new Boolean(value));
    }

    // XXX(-tdt) Hmm, backward compatibility with com.netbeans package name
    // again.  The property editor search path is stored in user settings, we
    // must translate
    
    public void readExternal(java.io.ObjectInput in)
        throws java.io.IOException, ClassNotFoundException
    {
        super.readExternal(in);
        for (int i = 0; i < editorSearchPath.length; i++) {
            String path = editorSearchPath[i];
            path = org.openide.util.Utilities.translate(path + ".BogusClass"); // NOI18N
            path = path.substring(0, path.length() - ".BogusClass".length()); //  NOI18N
            editorSearchPath[i] = path;
        }
    }
    
    /** This method must be overriden. It returns display name of this options.
     */
    public String displayName() {
        return org.openide.util.NbBundle.getBundle(FormLoaderSettings.class).getString("CTL_FormSettings");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(FormLoaderSettings.class);
    }

}
