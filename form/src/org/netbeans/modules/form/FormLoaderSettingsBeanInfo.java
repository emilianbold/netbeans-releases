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

import java.awt.Image;
import java.beans.*;
import java.lang.reflect.Modifier;
import java.util.*;
import org.netbeans.modules.form.editors.ModifierEditor;
import org.openide.util.NbBundle;

import org.openide.util.Utilities;

/**
 * A BeanInfo for FormLoaderSettings.
 */

public class FormLoaderSettingsBeanInfo extends SimpleBeanInfo {

    /** The icons for Settings */
    private static String iconURL =
        "org/netbeans/modules/form/resources/formSettings.gif"; // NOI18N
    private static String icon32URL =
        "org/netbeans/modules/form/resources/formSettings32.gif"; // NOI18N

    /** Descriptor of valid properties
     * @return array of properties
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor[] desc = new PropertyDescriptor[] {
                new PropertyDescriptor(FormLoaderSettings.PROP_USE_INDENT_ENGINE,
                                       FormLoaderSettings.class,
                                       "getUseIndentEngine", // NOI18N
                                       "setUseIndentEngine"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_EVENT_VARIABLE_NAME,
                                       FormLoaderSettings.class,
                                       "getEventVariableName", // NOI18N
                                       "setEventVariableName"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_LISTENER_GENERATION_STYLE,
                                       FormLoaderSettings.class,
                                       "getListenerGenerationStyle", // NOI18N
                                       "setListenerGenerationStyle"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_LAYOUT_CODE_TARGET,
                                       FormLoaderSettings.class,
                                       "getLayoutCodeTarget", // NOI18N
                                       "setLayoutCodeTarget"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_SELECTION_BORDER_SIZE,
                                       FormLoaderSettings.class,
                                       "getSelectionBorderSize", // NOI18N
                                       "setSelectionBorderSize"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_SELECTION_BORDER_COLOR,
                                       FormLoaderSettings.class,
                                       "getSelectionBorderColor", // NOI18N
                                       "setSelectionBorderColor"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_CONNECTION_BORDER_COLOR,
                                       FormLoaderSettings.class,
                                       "getConnectionBorderColor", // NOI18N
                                       "setConnectionBorderColor"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_DRAG_BORDER_COLOR,
                                       FormLoaderSettings.class,
                                       "getDragBorderColor", // NOI18N
                                       "setDragBorderColor"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_GUIDING_LINE_COLOR,
                                       FormLoaderSettings.class,
                                       "getGuidingLineColor", // NOI18N
                                       "setGuidingLineColor"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_GRID_X,
                                       FormLoaderSettings.class,
                                       "getGridX", "setGridX"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_GRID_Y,
                                       FormLoaderSettings.class,
                                       "getGridY", "setGridY"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_APPLY_GRID_TO_POSITION,
                                       FormLoaderSettings.class,
                                       "getApplyGridToPosition", // NOI18N
                                       "setApplyGridToPosition"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_APPLY_GRID_TO_SIZE,
                                       FormLoaderSettings.class,
                                       "getApplyGridToSize", // NOI18N
                                       "setApplyGridToSize"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_VARIABLES_MODIFIER,
                                       FormLoaderSettings.class,
                                       "getVariablesModifier", // NOI18N
                                       "setVariablesModifier"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_EDITOR_SEARCH_PATH,
                                       FormLoaderSettings.class,
                                       "getEditorSearchPath", // NOI18N
                                       "setEditorSearchPath"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_PALETTE_IN_TOOLBAR,
                                       FormLoaderSettings.class,
                                       "isPaletteInToolBar", // NOI18N
                                       "setPaletteInToolBar"), // NOI18N
//                new PropertyDescriptor(FormLoaderSettings.PROP_CONTAINER_BEANS,
//                                       FormLoaderSettings.class,
//                                       "getContainerBeans", // NOI18N
//                                       "setContainerBeans"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_FORMDESIGNER_BACKGROUND_COLOR,
                                       FormLoaderSettings.class,
                                       "getFormDesignerBackgroundColor", // NOI18N
                                       "setFormDesignerBackgroundColor"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_FORMDESIGNER_BORDER_COLOR,
                                       FormLoaderSettings.class,
                                       "getFormDesignerBorderColor", // NOI18N
                                       "setFormDesignerBorderColor"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_VARIABLES_LOCAL,
                                       FormLoaderSettings.class,
                                       "getVariablesLocal", // NOI18N
                                       "setVariablesLocal"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_DISPLAY_WRITABLE_ONLY,
                                       FormLoaderSettings.class,
                                       "getDisplayWritableOnly", // NOI18N
                                       "setDisplayWritableOnly"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_GENERATE_MNEMONICS,
                                       FormLoaderSettings.class,
                                       "getGenerateMnemonicsCode", // NOI18N
                                       "setGenerateMnemonicsCode"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_FOLD_GENERATED_CODE,
                                       FormLoaderSettings.class,
                                       "getFoldGeneratedCode", // NOI18N
                                       "setFoldGeneratedCode"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_ASSISTANT_SHOWN,
                                       FormLoaderSettings.class,
                                       "getAssistantShown", // NOI18N
                                       "setAssistantShown"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_AUTO_RESOURCING,
                                       FormLoaderSettings.class,
                                       "getI18nAutoMode", // NOI18N
                                       "setI18nAutoMode"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_AUTO_SET_COMPONENT_NAME,
                                       FormLoaderSettings.class,
                                       "getAutoSetComponentName", // NOI18N
                                       "setAutoSetComponentName") // NOI18N
            };

            ResourceBundle bundle = FormUtils.getBundle();
            int i = -1;

            desc[++i].setDisplayName(bundle.getString("PROP_USE_INDENT_ENGINE")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_USE_INDENT_ENGINE")); // NOI18N

            desc[++i].setDisplayName(bundle.getString("PROP_EVENT_VARIABLE_NAME")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_EVENT_VARIABLE_NAME")); // NOI18N
            desc[i].setExpert(true);

            desc[++i].setDisplayName(bundle.getString("PROP_LISTENER_GENERATION_STYLE")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_LISTENER_GENERATION_STYLE")); // NOI18N
            desc[i].setPropertyEditorClass(ListenerGenerationStyleEditor.class);
            desc[i].setExpert(true);

            desc[++i].setDisplayName(bundle.getString("PROP_LAYOUT_CODE_TARGET")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_LAYOUT_CODE_TARGET")); // NOI18N
            desc[i].setPropertyEditorClass(LayoutCodeTargetEditor.class);
            desc[i].setExpert(true);

            desc[++i].setDisplayName(bundle.getString("PROP_SELECTION_BORDER_SIZE")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_SELECTION_BORDER_SIZE")); // NOI18N

            desc[++i].setDisplayName(bundle.getString("PROP_SELECTION_BORDER_COLOR")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_SELECTION_BORDER_COLOR")); // NOI18N

            desc[++i].setDisplayName(bundle.getString("PROP_CONNECTION_BORDER_COLOR")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_CONNECTION_BORDER_COLOR")); // NOI18N

            desc[++i].setDisplayName(bundle.getString("PROP_DRAG_BORDER_COLOR")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_DRAG_BORDER_COLOR")); // NOI18N
            
            desc[++i].setDisplayName(bundle.getString("PROP_GUIDING_LINE_COLOR")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_GUIDING_LINE_COLOR")); // NOI18N

            desc[++i].setDisplayName(bundle.getString("PROP_GRID_X")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_GRID_X")); // NOI18N
            desc[i].setExpert(true);

            desc[++i].setDisplayName(bundle.getString("PROP_GRID_Y")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_GRID_Y")); // NOI18N
            desc[i].setExpert(true);

            desc[++i].setDisplayName(bundle.getString("PROP_APPLY_GRID_TO_POSITION")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_APPLY_GRID_TO_POSITION")); // NOI18N
            desc[i].setExpert(true);

            desc[++i].setDisplayName(bundle.getString("PROP_APPLY_GRID_TO_SIZE")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_APPLY_GRID_TO_SIZE")); // NOI18N
            desc[i].setExpert(true);

            desc[++i].setDisplayName(bundle.getString("PROP_VARIABLES_MODIFIER")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_VARIABLES_MODIFIER")); // NOI18N
            desc[i].setPropertyEditorClass(FieldModifierPropertyEditor.class);
            desc[i].setExpert(true);

            desc[++i].setDisplayName(bundle.getString("PROP_EDITOR_SEARCH_PATH")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_EDITOR_SEARCH_PATH")); // NOI18N
            desc[i].setExpert(true);

            desc[++i].setDisplayName(bundle.getString("PROP_PALETTE_IN_TOOLBAR")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_PALETTE_IN_TOOLBAR")); // NOI18N

            desc[++i].setDisplayName(bundle.getString("PROP_FORMDESIGNER_BACKGROUND_COLOR")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_FORMDESIGNER_BACKGROUND_COLOR")); // NOI18N

            desc[++i].setDisplayName(bundle.getString("PROP_FORMDESIGNER_BORDER_COLOR")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_FORMDESIGNER_BORDER_COLOR")); // NOI18N

            desc[++i].setDisplayName(bundle.getString("PROP_VARIABLES_LOCAL")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_VARIABLES_LOCAL")); // NOI18N
            desc[i].setExpert(true);

            desc[++i].setHidden(true);

            desc[++i].setDisplayName(bundle.getString("PROP_GENERATE_MNEMONICS")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_GENERATE_MNEMONICS")); // NOI18N
            desc[i].setExpert(true);
            
            desc[++i].setDisplayName(bundle.getString("PROP_FOLD_GENERATED_CODE")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_FOLD_GENERATED_CODE")); // NOI18N
            desc[i].setExpert(true);

            desc[++i].setDisplayName(bundle.getString("PROP_ASSISTANT_SHOWN")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_ASSISTANT_SHOWN")); // NOI18N
            desc[i].setPreferred(true);

            desc[++i].setDisplayName(bundle.getString("PROP_AUTO_RESOURCE")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_AUTO_RESOURCE_GLOBAL")); // NOI18N
            desc[i].setPropertyEditorClass(ResourceModeEditor.class);
            desc[i].setPreferred(true);

            desc[++i].setDisplayName(bundle.getString("PROP_AUTO_SET_COMPONENT_NAME")); // NOI18N
            desc[i].setShortDescription(bundle.getString("HINT_AUTO_SET_COMPONENT_NAME")); // NOI18N
            desc[i].setPropertyEditorClass(AutoNamingEditor.class);
            desc[i].setPreferred(true);

            return desc;
        }
        catch (IntrospectionException ex) {
            throw new InternalError();
        }
    }

    /** Returns the FormLoaderSettings' icon */
    public Image getIcon(int type) {
        return Utilities.loadImage(
                   type == java.beans.BeanInfo.ICON_COLOR_16x16
                       || type == java.beans.BeanInfo.ICON_MONO_16x16 ?
                   iconURL : icon32URL);
    }

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor retval = new BeanDescriptor(FormLoaderSettings.class , null );
        retval.setDisplayName(NbBundle.getMessage(FormLoaderSettings.class, "CTL_FormSettings")); // NOI18N
        return retval;
    }

    // --------

    /** Property editor for variables modifiers.
     */
    final public static class FieldModifierPropertyEditor extends ModifierEditor {
        static final long serialVersionUID =7628317154007139777L;
        /** Construct new editor with mask for fields. */
        public FieldModifierPropertyEditor() {
            super(Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE
                  | Modifier.STATIC | Modifier.FINAL | Modifier.TRANSIENT
                  | Modifier.VOLATILE);
        }
    }


    public final static class ListenerGenerationStyleEditor
                      extends org.netbeans.modules.form.editors.EnumEditor
    {
        public ListenerGenerationStyleEditor() {
            super(new Object[] {
                FormUtils.getBundleString("CTL_LISTENER_ANONYMOUS_CLASSES"), // NOI18N
                new Integer(JavaCodeGenerator.ANONYMOUS_INNERCLASSES),
                "", // NOI18N
                FormUtils.getBundleString("CTL_LISTENER_CEDL_INNERCLASS"), // NOI18N
                new Integer(JavaCodeGenerator.CEDL_INNERCLASS),
                "", // NOI18N
                FormUtils.getBundleString("CTL_LISTENER_CEDL_MAINCLASS"), // NOI18N
                new Integer(JavaCodeGenerator.CEDL_MAINCLASS),
                "" // NOI18N
            });
        }
    }

    public final static class LayoutCodeTargetEditor
                      extends org.netbeans.modules.form.editors.EnumEditor
    {
        public LayoutCodeTargetEditor() {
            this(false);
        }
        public LayoutCodeTargetEditor(boolean specific) {
            super(specific ?
                new Object[] {
                    FormUtils.getBundleString("CTL_LAYOUT_CODE_JDK6"), // NOI18N
                    new Integer(JavaCodeGenerator.LAYOUT_CODE_JDK6),
                    "", // NOI18N
                    FormUtils.getBundleString("CTL_LAYOUT_CODE_LIBRARY"), // NOI18N
                    new Integer(JavaCodeGenerator.LAYOUT_CODE_LIBRARY),
                    "" // NOI18N
                }
                :
                new Object[] {
                    FormUtils.getBundleString("CTL_LAYOUT_CODE_AUTO"), // NOI18N
                    new Integer(JavaCodeGenerator.LAYOUT_CODE_AUTO),
                    "", // NOI18N
                    FormUtils.getBundleString("CTL_LAYOUT_CODE_JDK6"), // NOI18N
                    new Integer(JavaCodeGenerator.LAYOUT_CODE_JDK6),
                    "", // NOI18N
                    FormUtils.getBundleString("CTL_LAYOUT_CODE_LIBRARY"), // NOI18N
                    new Integer(JavaCodeGenerator.LAYOUT_CODE_LIBRARY),
                    "" // NOI18N
                });
        }
    }

    public final static class ResourceModeEditor
                      extends org.netbeans.modules.form.editors.EnumEditor
    {
        public ResourceModeEditor() {
            super(new Object[] {
                    FormUtils.getBundleString("CTL_AUTO_RESOURCE_DEFAULT"), // NOI18N
                    new Integer(FormLoaderSettings.AUTO_RESOURCE_DEFAULT),
                    "", // NOI18N
                    FormUtils.getBundleString("CTL_AUTO_RESOURCE_ON"), // NOI18N
                    new Integer(FormLoaderSettings.AUTO_RESOURCE_ON),
                    "", // NOI18N
                    FormUtils.getBundleString("CTL_AUTO_RESOURCE_OFF"), // NOI18N
                    new Integer(FormLoaderSettings.AUTO_RESOURCE_OFF),
                    "" // NOI18N
                });
        }
    }

    public final static class AutoNamingEditor extends org.netbeans.modules.form.editors.EnumEditor {
        public AutoNamingEditor() {
            super(new Object[] {
                FormUtils.getBundleString("CTL_AUTO_NAMING_DEFAULT"), // NOI18N
                new Integer(FormLoaderSettings.AUTO_NAMING_DEFAULT),
                "", // NOI18N
                FormUtils.getBundleString("CTL_AUTO_NAMING_ON"), // NOI18N
                new Integer(FormLoaderSettings.AUTO_NAMING_ON),
                "", // NOI18N
                FormUtils.getBundleString("CTL_AUTO_NAMING_OFF"), // NOI18N
                new Integer(FormLoaderSettings.AUTO_NAMING_OFF),
                "" // NOI18N
            });
        }
    }
}
