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

/* $Id$ */

package org.netbeans.modules.form;

import java.awt.Image;
import java.beans.*;
import java.lang.reflect.Modifier;
import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.windows.Workspace;
import org.openide.windows.WindowManager;

/** A BeanInfo for FormLoaderSettings.
 * @author Ian Formanek
 * @version 0.11, May 22, 1998
 */
public class FormLoaderSettingsBeanInfo extends SimpleBeanInfo {

    /** Icons for url data loader. */
    private static Image icon;
    private static Image icon32;

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    private static java.util.ResourceBundle formBundle = org.openide.util.NbBundle.getBundle(FormLoaderSettingsBeanInfo.class);

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[] {
                new PropertyDescriptor(FormLoaderSettings.PROP_INDENT_AWT_HIERARCHY, FormLoaderSettings.class,
                                       "getIndentAWTHierarchy", "setIndentAWTHierarchy"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_SORT_EVENT_SETS, FormLoaderSettings.class,
                                       "getSortEventSets", "setSortEventSets"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_EVENT_VARIABLE_NAME, FormLoaderSettings.class,
                                       "getEventVariableName", "setEventVariableName"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_SHORT_BEAN_NAMES, FormLoaderSettings.class,
                                       "getShortBeanNames", "setShortBeanNames"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_SELECTION_BORDER_SIZE, FormLoaderSettings.class,
                                       "getSelectionBorderSize", "setSelectionBorderSize"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_SELECTION_BORDER_COLOR, FormLoaderSettings.class,
                                       "getSelectionBorderColor", "setSelectionBorderColor"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_CONNECTION_BORDER_COLOR, FormLoaderSettings.class,
                                       "getConnectionBorderColor", "setConnectionBorderColor"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_DRAG_BORDER_COLOR, FormLoaderSettings.class,
                                       "getDragBorderColor", "setDragBorderColor"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_SHOW_GRID, FormLoaderSettings.class,
                                       "getShowGrid", "setShowGrid"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_GRID_X, FormLoaderSettings.class,
                                       "getGridX", "setGridX"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_GRID_Y, FormLoaderSettings.class,
                                       "getGridY", "setGridY"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_APPLY_GRID_TO_POSITION, FormLoaderSettings.class,
                                       "getApplyGridToPosition", "setApplyGridToPosition"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_APPLY_GRID_TO_SIZE, FormLoaderSettings.class,
                                       "getApplyGridToSize", "setApplyGridToSize"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_VARIABLES_MODIFIER, FormLoaderSettings.class,
                                       "getVariablesModifier", "setVariablesModifier"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_EDITOR_SEARCH_PATH, FormLoaderSettings.class,
                                       "getEditorSearchPath", "setEditorSearchPath"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_REGISTERED_EDITORS, FormLoaderSettings.class,
                                       "getRegisteredEditors", "setRegisteredEditors"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_OUTPUT_LEVEL, FormLoaderSettings.class,
                                       "getOutputLevel", "setOutputLevel"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_NULL_LAYOUT, FormLoaderSettings.class,
                                       "isNullLayout", "setNullLayout"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_WORKSPACE, FormLoaderSettings.class,
                                       "getWorkspace", "setWorkspace"), // NOI18N
            };

            desc[0].setDisplayName(formBundle.getString("PROP_INDENT_AWT_HIERARCHY"));
            desc[0].setShortDescription(formBundle.getString("HINT_INDENT_AWT_HIERARCHY"));
            desc[1].setDisplayName(formBundle.getString("PROP_SORT_EVENT_SETS"));
            desc[1].setShortDescription(formBundle.getString("HINT_SORT_EVENT_SETS"));
            desc[1].setExpert(true);
            desc[1].setHidden(true);
            desc[2].setDisplayName(formBundle.getString("PROP_EVENT_VARIABLE_NAME"));
            desc[2].setShortDescription(formBundle.getString("HINT_EVENT_VARIABLE_NAME"));
            desc[3].setDisplayName(formBundle.getString("PROP_SHORT_BEAN_NAMES"));
            desc[3].setShortDescription(formBundle.getString("HINT_SHORT_BEAN_NAMES"));
            desc[3].setHidden(true);
            desc[3].setExpert(true);
            desc[4].setDisplayName(formBundle.getString("PROP_SELECTION_BORDER_SIZE"));
            desc[4].setShortDescription(formBundle.getString("HINT_SELECTION_BORDER_SIZE"));
            desc[4].setExpert(true);
            desc[5].setDisplayName(formBundle.getString("PROP_SELECTION_BORDER_COLOR"));
            desc[5].setShortDescription(formBundle.getString("HINT_SELECTION_BORDER_COLOR"));
            desc[5].setExpert(true);
            desc[6].setDisplayName(formBundle.getString("PROP_CONNECTION_BORDER_COLOR"));
            desc[6].setShortDescription(formBundle.getString("HINT_CONNECTION_BORDER_COLOR"));
            desc[6].setExpert(true);
            desc[7].setDisplayName(formBundle.getString("PROP_DRAG_BORDER_COLOR"));
            desc[7].setShortDescription(formBundle.getString("HINT_DRAG_BORDER_COLOR"));
            desc[7].setExpert(true);
            desc[8].setDisplayName(formBundle.getString("PROP_SHOW_GRID"));
            desc[8].setShortDescription(formBundle.getString("HINT_SHOW_GRID"));
            desc[9].setDisplayName(formBundle.getString("PROP_GRID_X"));
            desc[9].setShortDescription(formBundle.getString("HINT_GRID_X"));
            desc[10].setDisplayName(formBundle.getString("PROP_GRID_Y"));
            desc[10].setShortDescription(formBundle.getString("HINT_GRID_Y"));
            desc[11].setDisplayName(formBundle.getString("PROP_APPLY_GRID_TO_POSITION"));
            desc[11].setShortDescription(formBundle.getString("HINT_APPLY_GRID_TO_POSITION"));
            desc[11].setExpert(true);
            desc[12].setDisplayName(formBundle.getString("PROP_APPLY_GRID_TO_SIZE"));
            desc[12].setShortDescription(formBundle.getString("HINT_APPLY_GRID_TO_SIZE"));
            desc[12].setExpert(true);
            desc[13].setDisplayName(formBundle.getString("PROP_VARIABLES_MODIFIER"));
            desc[13].setShortDescription(formBundle.getString("HINT_VARIABLES_MODIFIER"));
            desc[13].setPropertyEditorClass(FieldModifierPropertyEditor.class);
            desc[14].setDisplayName(formBundle.getString("PROP_EDITOR_SEARCH_PATH"));
            desc[14].setShortDescription(formBundle.getString("HINT_EDITOR_SEARCH_PATH"));
            desc[15].setDisplayName(formBundle.getString("PROP_REGISTERED_EDITORS"));
            desc[15].setShortDescription(formBundle.getString("HINT_REGISTERED_EDITORS"));
            desc[16].setDisplayName(formBundle.getString("PROP_OUTPUT_LEVEL"));
            desc[16].setShortDescription(formBundle.getString("HINT_OUTPUT_LEVEL"));
            desc[16].setPropertyEditorClass(FormLoaderSettingsBeanInfo.OutputLevelEditor.class);
            desc[17].setDisplayName(formBundle.getString("PROP_NULL_LAYOUT"));
            desc[17].setShortDescription(formBundle.getString("HINT_NULL_LAYOUT"));
            desc[18].setDisplayName(formBundle.getString("PROP_WORKSPACE"));
            desc[18].setShortDescription(formBundle.getString("HINT_WORKSPACE"));
            desc[18].setPropertyEditorClass(WorkspaceEditor.class);


        } catch (IntrospectionException ex) {
            throw new InternalError();
        }
    }


    /** Descriptor of valid properties
     * @return array of properties
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        return desc;
    }

    /** Returns the FormLoaderSettings' icon */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
            (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null)
                icon = loadImage("/org/netbeans/modules/form/resources/formSettings.gif"); // NOI18N
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage("/org/netbeans/modules/form/resources/formSettings32.gif"); // NOI18N
            return icon32;
        }
    }

    /** Property editor for variables modifiers.
     */
    final public static class FieldModifierPropertyEditor extends org.openide.explorer.propertysheet.editors.ModifierEditor {
        static final long serialVersionUID =7628317154007139777L;
        /** Construct new editor with mask for fields. */
        public FieldModifierPropertyEditor() {
            super(Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE | Modifier.STATIC |
                  Modifier.FINAL | Modifier.TRANSIENT | Modifier.VOLATILE);
        }
    }


    final public static class OutputLevelEditor extends java.beans.PropertyEditorSupport {
        /** Display Names for alignment. */
        private static final String[] names = {
            formBundle.getString("VALUE_OutputLevel_Minimum"),
            formBundle.getString("VALUE_OutputLevel_Normal"),
            formBundle.getString("VALUE_OutputLevel_Maximum"),
        };

        /** @return names of the possible directions */
        public String[] getTags() {
            return names;
        }

        /** @return text for the current value */
        public String getAsText() {
            int value =((Integer)getValue()).intValue();
            if ((value < 0) ||(value > 2)) return null;
            return names [value];
        }

        /** Setter.
         * @param str string equal to one value from directions array
         */
        public void setAsText(String str) {
            for (int i = 0; i <= 2; i++) {
                if (names[i].equals(str)) {
                    setValue(new Integer(i));
                    return;
                }
            }
        }

    }

    final public static class WorkspaceEditor extends java.beans.PropertyEditorSupport {

        /** Mapping between programmatic and display names of workspaces */
        private Map namesMap;
        /** Validity flag - true if namesMap has been initialized already */
        private boolean namesInitialized = false;

        /*
         * @return The property value as a human editable string.
         * <p>   Returns null if the value can't be expressed as an editable string.
         * <p>   If a non-null value is returned, then the PropertyEditor should
         *       be prepared to parse that string back in setAsText().
         */
        public String getAsText() {
            if (!namesInitialized) {
                namesInitialized = true;
                initializeNamesMap(
                    TopManager.getDefault().getWindowManager().getWorkspaces()
                    );
            }
            String value =(String)getValue();
            String displayName =(String)namesMap.get(value);
            return(displayName == null) ? value : displayName;
        }

        /* Set the property value by parsing a given String.  May raise
         * java.lang.IllegalArgumentException if either the String is
         * badly formatted or if this kind of property can't be expressed
         * as text.
         * @param text  The string to be parsed.
         */
        public void setAsText(String text) throws java.lang.IllegalArgumentException {
            String programmaticName = findProgrammaticName(text);
            setValue((programmaticName == null) ? text : programmaticName);
        }

        /*
         * If the property value must be one of a set of known tagged values,
         * then this method should return an array of the tag values.  This can
         * be used to represent(for example) enum values.  If a PropertyEditor
         * supports tags, then it should support the use of setAsText with
         * a tag value as a way of setting the value.
         *
         * @return The tag values for this property.  May be null if this
         *   property cannot be represented as a tagged value.
         *
         */
        public String[] getTags() {
            WindowManager wm = TopManager.getDefault().getWindowManager();
            Workspace[] wss = wm.getWorkspaces();
            if (!namesInitialized) {
                namesInitialized = true;
                initializeNamesMap(wss);
            }
            // exclude browsing, running and debugging workspaces
            java.util.Vector tagList = new java.util.Vector();
            for (int i = wss.length; --i >= 0;) {
                String name = wss[i].getName();
                if (!("Browsing".equals(name) || "Running".equals(name) || "Debugging".equals(name))) { // NOI18N
                    tagList.add(name);
                }
            }
            // XXX(-tdt)
            // tagList.add(NbBundle.getBundle(WorkspaceEditor.class).getString("VALUE_WORKSPACE_NONE"));
            String[] names = new String [tagList.size() + 1];
            for (int i=0, n = tagList.size(); i < n; i++)
                names[i] =(String)namesMap.get(tagList.get(i));
            names[tagList.size()] = NbBundle.getBundle(WorkspaceEditor.class).getString("VALUE_WORKSPACE_NONE");
            return names;
        }

        /** Initializes name mapping with given workspace set.
         * Result is stored in nameMap private variable. */
        private void initializeNamesMap(Workspace[] wss) {
            // fill name mapping with proper values
            namesMap = new HashMap(wss.length * 2);
            for (int i = 0; i < wss.length; i++) {
                // create new string for each display name to be able to search
                // using '==' operator in findProgrammaticName(String displayName) method
                namesMap.put(wss[i].getName(), new String(wss[i].getDisplayName()));;
            }
        }

        /** @return Returns programmatic name of the workspace for given
         * display name of the workspace. Uses special features of namesMap mapping
         * to perform succesfull search. */
        private String findProgrammaticName(String displayName) {
            for (Iterator iter = namesMap.entrySet().iterator(); iter.hasNext();) {
                Map.Entry curEntry =(Map.Entry)iter.next();
                if (displayName == curEntry.getValue()) {
                    return(String)curEntry.getKey();
                }
            }
            return null;
        }

    }

}
