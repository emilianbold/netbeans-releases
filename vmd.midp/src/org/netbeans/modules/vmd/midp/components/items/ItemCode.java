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
package org.netbeans.modules.vmd.midp.components.items;

import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.codegen.Parameter;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.codegen.MidpCodeSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midp.components.sources.ItemCommandEventSourceCD;

import java.util.List;

/**
 * @author David Kaspar
 */
public class ItemCode {

    static final String PARAM_COMMAND = "itemCommand"; // NOI18N
    static final String PARAM_ITEM_COMMAND_LISTENER = "itemCommandListener"; // NOI18N
    static final String PARAM_LAYOUT = "layout"; // NOI18N
    static final String PARAM_APPEARANCE_MODE = "appearanceMode"; // NOI18N
    static final String PARAM_DEFAULT_COMMAND = "defaultCommand"; // NOI18N

    public static Parameter createCommandParameter () {
        return new CommandParameter ();
    }

    public static Parameter createItemCommandListenerParameter () {
        return new ItemCommandListenerParameter ();
    }

    public static Parameter createItemLayoutParameter () {
        return new ItemLayoutParameter ();
    }

    public static Parameter createAppearanceModeParameter () {
        return new AppearanceModeParameter ();
    }

    public static Parameter createDefaultCommandParameter () {
        return new DefaultCommandParameter ();
    }

    private static class CommandParameter implements Parameter {

        public String getParameterName () {
            return PARAM_COMMAND;
        }

        public int getParameterPriority () {
            return 0;
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue propertyValue = component.readProperty (ItemCD.PROP_COMMANDS);
            List<PropertyValue> array = propertyValue.getArray ();
            DesignComponent commandEventSource = array.get (index).getComponent ();
            MidpCodeSupport.generateCodeForPropertyValue (section.getWriter (), commandEventSource.readProperty (CommandEventSourceCD.PROP_COMMAND));
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            return ! component.isDefaultValue (ItemCD.PROP_COMMANDS);
        }

        public int getCount (DesignComponent component) {
            PropertyValue propertyValue = component.readProperty (ItemCD.PROP_COMMANDS);
            List<PropertyValue> array = propertyValue.getArray ();
            return array.size ();
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            return true;
        }

    }

    private static class ItemCommandListenerParameter extends MidpParameter {

        private ItemCommandListenerParameter () {
            super (PARAM_ITEM_COMMAND_LISTENER);
        }

        @Override
        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            MidpCodeSupport.generateCodeForPropertyValue (section.getWriter (), component.readProperty (ItemCD.PROP_ITEM_COMMAND_LISTENER));
        }

        @Override
        public boolean isRequiredToBeSet (DesignComponent component) {
            return ! component.isDefaultValue (ItemCD.PROP_COMMANDS);
        }

    }

    private static class ItemLayoutParameter extends MidpParameter {

        protected ItemLayoutParameter () {
            super (PARAM_LAYOUT);
        }

        @Override
        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue propertyValue = component.readProperty (ItemCD.PROP_LAYOUT);
            if (propertyValue.getKind () == PropertyValue.Kind.VALUE) {
                int value = MidpTypes.getInteger (propertyValue);
                switch (value & 3) {
                    case ItemCD.VALUE_LAYOUT_DEFAULT:
                        section.getWriter ().write ("ImageItem.LAYOUT_DEFAULT"); // NOI18N
                        break;
                    case ItemCD.VALUE_LAYOUT_LEFT:
                        section.getWriter ().write ("ImageItem.LAYOUT_LEFT"); // NOI18N
                        break;
                    case ItemCD.VALUE_LAYOUT_RIGHT:
                        section.getWriter ().write ("ImageItem.LAYOUT_RIGHT"); // NOI18N
                        break;
                    case ItemCD.VALUE_LAYOUT_CENTER:
                        section.getWriter ().write ("ImageItem.LAYOUT_CENTER"); // NOI18N
                        break;
                    default:
                        throw Debug.illegalState ();
                }
                if ((value & ItemCD.VALUE_LAYOUT_TOP) != 0)
                    section.getWriter ().write (" | Item.LAYOUT_TOP"); // NOI18N
                if ((value & ItemCD.VALUE_LAYOUT_BOTTOM) != 0)
                    section.getWriter ().write (" | Item.LAYOUT_BOTTOM"); // NOI18N
                if ((value & ItemCD.VALUE_LAYOUT_VCENTER) != 0)
                    section.getWriter ().write (" | Item.LAYOUT_VCENTER"); // NOI18N
                if ((value & ItemCD.VALUE_LAYOUT_NEWLINE_BEFORE) != 0)
                    section.getWriter ().write (" | ImageItem.LAYOUT_NEWLINE_BEFORE"); // NOI18N
                if ((value & ItemCD.VALUE_LAYOUT_NEWLINE_AFTER) != 0)
                    section.getWriter ().write (" | ImageItem.LAYOUT_NEWLINE_AFTER"); // NOI18N
                if ((value & ItemCD.VALUE_LAYOUT_SHRINK) != 0)
                    section.getWriter ().write (" | Item.LAYOUT_SHRINK"); // NOI18N
                if ((value & ItemCD.VALUE_LAYOUT_VSHRINK) != 0)
                    section.getWriter ().write (" | Item.LAYOUT_VSHRINK"); // NOI18N
                if ((value & ItemCD.VALUE_LAYOUT_EXPAND) != 0)
                    section.getWriter ().write (" | Item.LAYOUT_EXPAND"); // NOI18N
                if ((value & ItemCD.VALUE_LAYOUT_VEXPAND) != 0)
                    section.getWriter ().write (" | Item.LAYOUT_VEXPAND"); // NOI18N
                if ((value & ItemCD.VALUE_LAYOUT_2) != 0)
                    section.getWriter ().write (" | Item.LAYOUT_2"); // NOI18N
                return;
            }
            super.generateParameterCode (component, section, index);
        }

    }

    private static class AppearanceModeParameter extends MidpParameter {

        protected AppearanceModeParameter () {
            super (PARAM_APPEARANCE_MODE);
        }

        @Override
        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue propertyValue = component.readProperty (ItemCD.PROP_APPEARANCE_MODE);
            if (propertyValue.getKind () == PropertyValue.Kind.VALUE) {
                int value = MidpTypes.getInteger (propertyValue);
                switch (value) {
                    case ItemCD.VALUE_PLAIN:
                        section.getWriter ().write ("Item.PLAIN"); // NOI18N
                        return;
                    case ItemCD.VALUE_BUTTON:
                        section.getWriter ().write ("Item.BUTTON"); // NOI18N
                        return;
                    case ItemCD.VALUE_HYPERLINK:
                        section.getWriter ().write ("Item.HYPERLINK"); // NOI18N
                        return;
                    default:
                        throw Debug.illegalState ();
                }
            }
            super.generateParameterCode (component, section, index);
        }
    }

    private static class DefaultCommandParameter implements Parameter {

        public String getParameterName () {
            return PARAM_DEFAULT_COMMAND;
        }

        public int getParameterPriority () {
            return 0;
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            MidpCodeSupport.generateCodeForPropertyValue (section.getWriter (), getDefaultCommand (component));
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            return getDefaultCommand (component).getKind () != PropertyValue.Kind.NULL;
        }

        public int getCount (DesignComponent component) {
            return -1;
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            throw Debug.illegalState ();
        }

        private PropertyValue getDefaultCommand (DesignComponent component) {
            DesignComponent source = component.readProperty (ItemCD.PROP_DEFAULT_COMMAND).getComponent ();
            return source != null ? source.readProperty (ItemCommandEventSourceCD.PROP_COMMAND) : PropertyValue.createNull ();
        }

    }

}
