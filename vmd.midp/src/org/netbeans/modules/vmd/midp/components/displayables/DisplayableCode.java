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
package org.netbeans.modules.vmd.midp.components.displayables;

import org.netbeans.modules.vmd.api.codegen.MultiGuardedSection;
import org.netbeans.modules.vmd.api.codegen.Parameter;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.codegen.MidpCodeSupport;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;

import java.util.List;

/**
 * @author David Kaspar
 */
public class DisplayableCode {

    public static final String PARAM_COMMAND = "command"; // NOI18N
    public static final String PARAM_COMMAND_LISTENER = "commandListener"; // NOI18N

    public static Parameter createCommandParameter () {
        return new CommandParameter();
    }

    public static Parameter createCommandListenerParameter () {
        return new CommandListenerParameter ();
    }

    public static class CommandParameter implements Parameter {

        public String getParameterName () {
            return PARAM_COMMAND;
        }

        public int getParameterPriority () {
            return 0;
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue propertyValue = component.readProperty (DisplayableCD.PROP_COMMANDS);
            List<PropertyValue> array = propertyValue.getArray ();
            DesignComponent commandEventSource = array.get (index).getComponent ();
            MidpCodeSupport.generateCodeForPropertyValue (section.getWriter (), commandEventSource.readProperty (CommandEventSourceCD.PROP_COMMAND));
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            return ! component.isDefaultValue (DisplayableCD.PROP_COMMANDS);
        }

        public int getCount (DesignComponent component) {
            PropertyValue propertyValue = component.readProperty (DisplayableCD.PROP_COMMANDS);
            List<PropertyValue> array = propertyValue.getArray ();
            return array.size ();
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            return true;
        }

    }

    private static class CommandListenerParameter implements Parameter {

        private CommandListenerParameter () {
        }

        public String getParameterName () {
            return PARAM_COMMAND_LISTENER;
        }

        public int getParameterPriority () {
            return 0;
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            MidpCodeSupport.generateCodeForPropertyValue (section.getWriter (), component.readProperty (DisplayableCD.PROP_COMMAND_LISTENER));
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            return ! component.isDefaultValue (DisplayableCD.PROP_COMMANDS);
        }

        public int getCount (DesignComponent component) {
            return -1;
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            throw Debug.illegalState ();
        }

    }

}
