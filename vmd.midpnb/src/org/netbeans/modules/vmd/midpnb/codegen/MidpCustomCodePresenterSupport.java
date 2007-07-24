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
 *
 */

package org.netbeans.modules.vmd.midpnb.codegen;

import org.netbeans.modules.vmd.api.codegen.*;
import org.netbeans.modules.vmd.api.model.DescriptorRegistry;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.midp.codegen.MidpCodeSupport;
import org.netbeans.modules.vmd.midp.codegen.MidpParameter;
import org.netbeans.modules.vmd.midp.components.MidpTypes;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCD;
import org.netbeans.modules.vmd.midp.components.displayables.DisplayableCode;
import org.netbeans.modules.vmd.midp.components.general.ClassCD;
import org.netbeans.modules.vmd.midp.components.sources.CommandEventSourceCD;
import org.netbeans.modules.vmd.midpnb.components.commands.*;
import org.netbeans.modules.vmd.midpnb.components.displayables.PIMBrowserCD;
import org.netbeans.modules.vmd.midpnb.components.displayables.SplashScreenCD;
import org.netbeans.modules.vmd.midpnb.components.sources.SVGMenuElementEventSourceCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGMenuCD;
import org.netbeans.modules.vmd.midpnb.components.svg.SVGSplashScreenCD;

import javax.swing.text.StyledDocument;
import java.util.Arrays;
import java.util.List;

/**
 * @author David Kaspar
 */
public final class MidpCustomCodePresenterSupport {

    public static final String PARAM_DISPLAY = "display"; // NOI18N
    public static final String PARAM_TIMEOUT = "timeout"; // NOI18N
    public static final String PARAM_SVG_TIMEOUT = "timeout"; // NOI18N
    public static final String PARAM_SVG_MENU_ELEMENT = "menuElement"; // NOI18N
    public static final String PARAM_PIM_LIST_TYPE = "pimListType"; //NOI18N

    private static final String SVG_MENU_ACTION_METHOD_SUFFIX = "Action"; // NOI18N

    private static final Parameter PARAMETER_DISPLAY = new DisplayParameter ();
    private static final Parameter PARAMETER_TIMEOUT = new TimeoutParameter ();
    private static final Parameter PARAMETER_SVG_TIMEOUT = new SVGTimeoutParameter ();
    private static final Parameter PARAMETER_WAITSCREEN_COMMAND = new WaitScreenCommandParameter ();
    private static final Parameter PARAMETER_SPLASHSCREEN_COMMAND = new SplashScreenCommandParameter ();
    private static final Parameter PARAMETER_SVG_WAITSCREEN_COMMAND = new SVGWaitScreenCommandParameter ();
    private static final Parameter PARAMETER_SVG_SPLASHSCREEN_COMMAND = new SVGSplashScreenCommandParameter ();
    private static final Parameter PARAMETER_SVG_MENU_ELEMENT = new SVGMenuElementParameter ();
    private static final Parameter PARAMETER_PIM_LIST_TYPES = new PIMListTypesParameter();

    private MidpCustomCodePresenterSupport () {
    }

    public static Parameter createDisplayParameter () {
        return PARAMETER_DISPLAY;
    }
    
    public static Parameter createPIMListTypesParameter() {
        return PARAMETER_PIM_LIST_TYPES;
    }
    
    public static Parameter createTimeoutParameter () {
        return PARAMETER_TIMEOUT;
    }

    public static Parameter createSVGTimeoutParameter () {
        return PARAMETER_SVG_TIMEOUT;
    }

    public static Parameter createWaitScreenCommandParameter () {
        return PARAMETER_WAITSCREEN_COMMAND;
    }

    public static Parameter createSplashScreenCommandParameter () {
        return PARAMETER_SPLASHSCREEN_COMMAND;
    }

    public static Parameter createSVGWaitScreenCommandParameter () {
        return PARAMETER_SVG_WAITSCREEN_COMMAND;
    }

    public static Parameter createSVGSplashScreenCommandParameter () {
        return PARAMETER_SVG_SPLASHSCREEN_COMMAND;
    }

    public static Parameter createSVGMenuElementParameter () {
        return PARAMETER_SVG_MENU_ELEMENT;
    }

    public static Presenter createSVGMenuEventHandlerCodeNamePresenter () {
        return new SVGMenuActionCodeNamePresenter ();
    }

    public static Presenter createSVGMenuCodePresenter () {
        return new SVGMenuActionCodeClassLevelPresenter ();
    }

    public static String getSVGMenuActionMethodAccessCode (DesignComponent menu) {
        if (menu == null || ! menu.getDocument ().getDescriptorRegistry ().isInHierarchy (SVGMenuCD.TYPEID, menu.getType ()))
            return null;
        return MidpTypes.getString (menu.readProperty (ClassCD.PROP_INSTANCE_NAME)) + SVG_MENU_ACTION_METHOD_SUFFIX;
    }

    private static class DisplayParameter implements Parameter {

        public String getParameterName () {
            return PARAM_DISPLAY;
        }

        public int getParameterPriority () {
            return 0;
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            section.getWriter ().write ("getDisplay ()"); // NOI18N
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            return false;
        }

        public int getCount (DesignComponent component) {
            return -1;
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            return false;
        }

    }
    
    private static class PIMListTypesParameter implements Parameter {

        public String getParameterName () {
            return PARAM_PIM_LIST_TYPE;
        }

        public int getParameterPriority () {
            return 0;
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            Integer pimListType = (Integer) component.readProperty(PIMBrowserCD.PROP_PIM_TYPE).getPrimitiveValue();
            String listTypeParameter = null;  
            switch (pimListType) {
                case PIMBrowserCD.VALUE_CONTACT_LIST:  
                    listTypeParameter = "PIM.CONTACT_LIST"; //NOI18N
                    break;
                case PIMBrowserCD.VALUE_EVENT_LIST:
                    listTypeParameter = "PIM.EVENT_LIST"; //NOI18N
                    break;
                case PIMBrowserCD.VALUE_TODO_LIST :
                    listTypeParameter = "PIM.TODO_LIST" ; //NOI18N
                    break;
                default:
                    throw new IllegalStateException("Illegal value of pimListParameter"); // NOI18N
            } 
            
            section.getWriter().write(listTypeParameter); // NOI18N
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            return true;
        }

        public int getCount (DesignComponent component) {
            return 1;
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            return true;
        }
    }
    
    private static class TimeoutParameter extends MidpParameter {

        protected TimeoutParameter () {
            super (PARAM_TIMEOUT);
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue value = component.readProperty (SplashScreenCD.PROP_TIMEOUT);
            if (value.getKind () == PropertyValue.Kind.VALUE)
                if (MidpTypes.getInteger (value) == 0) {
                    section.getWriter ().write ("SplashScreen.TIMEOUT"); // NOI18N
                    return;
                }
            super.generateParameterCode (component, section, index);
        }
    }

    private static class SVGTimeoutParameter extends MidpParameter {

        protected SVGTimeoutParameter () {
            super (PARAM_SVG_TIMEOUT);
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            PropertyValue value = component.readProperty (SVGSplashScreenCD.PROP_TIMEOUT);
            if (value.getKind () == PropertyValue.Kind.VALUE)
                if (MidpTypes.getInteger (value) == 0) {
                    section.getWriter ().write ("SVGSplashScreen.TIMEOUT"); // NOI18N
                    return;
                }
            super.generateParameterCode (component, section, index);
        }
    }

    private static final class WaitScreenCommandParameter extends DisplayableCode.CommandParameter {

        public int getParameterPriority () {
            return super.getParameterPriority () + 1;
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            DescriptorRegistry descriptorRegistry = component.getDocument ().getDescriptorRegistry ();
            List<PropertyValue> array = component.readProperty (DisplayableCD.PROP_COMMANDS).getArray ();
            DesignComponent commandEventSource = array.get (index).getComponent ();
            DesignComponent command = commandEventSource.readProperty (CommandEventSourceCD.PROP_COMMAND).getComponent ();
            if (command != null && descriptorRegistry.isInHierarchy (WaitScreenSuccessCommandCD.TYPEID, command.getType ()))
                return false;
            if (command != null && descriptorRegistry.isInHierarchy (WaitScreenFailureCommandCD.TYPEID, command.getType ()))
                return false;
            return super.isRequiredToBeSet (command, index);
        }

    }

    private static final class SplashScreenCommandParameter extends DisplayableCode.CommandParameter {

        public int getParameterPriority () {
            return super.getParameterPriority () + 1;
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            DescriptorRegistry descriptorRegistry = component.getDocument ().getDescriptorRegistry ();
            List<PropertyValue> array = component.readProperty (DisplayableCD.PROP_COMMANDS).getArray ();
            DesignComponent commandEventSource = array.get (index).getComponent ();
            DesignComponent command = commandEventSource.readProperty (CommandEventSourceCD.PROP_COMMAND).getComponent ();
            if (command != null && descriptorRegistry.isInHierarchy (SplashScreenDismissCommandCD.TYPEID, command.getType ()))
                return false;
            return super.isRequiredToBeSet (command, index);
        }

    }

    private static final class SVGWaitScreenCommandParameter extends DisplayableCode.CommandParameter {

        public int getParameterPriority () {
            return super.getParameterPriority () + 1;
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            DescriptorRegistry descriptorRegistry = component.getDocument ().getDescriptorRegistry ();
            List<PropertyValue> array = component.readProperty (DisplayableCD.PROP_COMMANDS).getArray ();
            DesignComponent commandEventSource = array.get (index).getComponent ();
            DesignComponent command = commandEventSource.readProperty (CommandEventSourceCD.PROP_COMMAND).getComponent ();
            if (command != null && descriptorRegistry.isInHierarchy (SVGWaitScreenSuccessCommandCD.TYPEID, command.getType ()))
                return false;
            if (command != null && descriptorRegistry.isInHierarchy (SVGWaitScreenFailureCommandCD.TYPEID, command.getType ()))
                return false;
            return super.isRequiredToBeSet (command, index);
        }

    }

    private static final class SVGSplashScreenCommandParameter extends DisplayableCode.CommandParameter {

        public int getParameterPriority () {
            return super.getParameterPriority () + 1;
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            DescriptorRegistry descriptorRegistry = component.getDocument ().getDescriptorRegistry ();
            List<PropertyValue> array = component.readProperty (DisplayableCD.PROP_COMMANDS).getArray ();
            DesignComponent commandEventSource = array.get (index).getComponent ();
            DesignComponent command = commandEventSource.readProperty (CommandEventSourceCD.PROP_COMMAND).getComponent ();
            if (command != null && descriptorRegistry.isInHierarchy (SVGSplashScreenDismissCommandCD.TYPEID, command.getType ()))
                return false;
            return super.isRequiredToBeSet (command, index);
        }

    }

    private static final class SVGMenuElementParameter implements Parameter {

        public String getParameterName () {
            return PARAM_SVG_MENU_ELEMENT;
        }

        public int getParameterPriority () {
            return 0;
        }

        public void generateParameterCode (DesignComponent component, MultiGuardedSection section, int index) {
            List<PropertyValue> elements = component.readProperty (SVGMenuCD.PROP_ELEMENTS).getArray ();
            PropertyValue value = elements.get (index);
            DesignComponent element = value.getComponent ();
            PropertyValue string = element.readProperty (SVGMenuElementEventSourceCD.PROP_STRING);
            MidpCodeSupport.generateCodeForPropertyValue (section.getWriter (), string);
        }

        public boolean isRequiredToBeSet (DesignComponent component) {
            List<PropertyValue> array = component.readProperty (SVGMenuCD.PROP_ELEMENTS).getArray ();
            return array != null  &&  array.size () != 0;
        }

        public int getCount (DesignComponent component) {
            List<PropertyValue> array = component.readProperty (SVGMenuCD.PROP_ELEMENTS).getArray ();
            return array != null ? array.size () : 0;
        }

        public boolean isRequiredToBeSet (DesignComponent component, int index) {
            return true;
        }

    }

    private static final class SVGMenuActionCodeNamePresenter extends CodeNamePresenter {

        public List<String> getReservedNames () {
            return getReservedNamesFor (MidpTypes.getString (getComponent ().readProperty (ClassCD.PROP_INSTANCE_NAME)));
        }

        public List<String> getReservedNamesFor (String suggestedMainName) {
            return Arrays.asList (suggestedMainName + SVG_MENU_ACTION_METHOD_SUFFIX);
        }

    }

    private static final class SVGMenuActionCodeClassLevelPresenter extends CodeClassLevelPresenter.Adapter {

        protected void generateClassBodyCode (StyledDocument document) {
            DesignComponent menu = getComponent ();
            List<PropertyValue> array = menu.readProperty (SVGMenuCD.PROP_ELEMENTS).getArray ();

            MultiGuardedSection section = MultiGuardedSection.create (document, menu.getComponentID () + "-action"); // NOI18N
            String menuName = CodeReferencePresenter.generateDirectAccessCode (menu);
            String methodName = menuName + SVG_MENU_ACTION_METHOD_SUFFIX;
            section.getWriter ().write ("//<editor-fold defaultstate=\"collapsed\" desc=\" Generated Method: " + methodName + " \">\n"); // NOI18N
            section.getWriter ().write ("/**\n * Performs an action assigned to the selected SVG menu element in the " + menuName + " component.\n */\n"); // NOI18N
            section.getWriter ().write ("public void " + methodName + " () {\n").commit (); // NOI18N
            section.switchToEditable (menu.getComponentID () + "-preAction"); // NOI18N
            section.getWriter ().write (" // enter pre-action user code here\n").commit (); // NOI18N
            section.switchToGuarded ();

            String menuInstanceName = CodeReferencePresenter.generateAccessCode (menu);
            boolean indexBased = MidpTypes.getBoolean (menu.readProperty (SVGMenuCD.PROP_INDEX_BASED_SWITCH));
            if (! indexBased) {
                section.getWriter ().write ("String __selectedElement = " + menuInstanceName + ".getMenuElementID (" + menuInstanceName + ".getSelectedIndex ());\n"); // NOI18N
            }

            if (array.size () > 0) {
                if (indexBased) {
                    section.getWriter ().write ("switch (" + menuInstanceName + ".getSelectedIndex ()) {\n"); // NOI18N
                } else {
                    section.getWriter ().write ("if (__selectedElement != null) {\n"); // NOI18N
                }

                for (int i = 0; i < array.size (); i ++) {
                    PropertyValue value = array.get (i);
                    DesignComponent source = value.getComponent ();

                    if (indexBased) {
                        section.getWriter ().write ("case " + i + ":\n"); // NOI18N
                    } else {
                        if (i > 0)
                            section.getWriter ().write ("} else "); // NOI18N
                        section.getWriter ().write ("if (__selectedElement.equals ("); // NOI18N
                        MidpCodeSupport.generateCodeForPropertyValue (section.getWriter (), source.readProperty (SVGMenuElementEventSourceCD.PROP_STRING));
                        section.getWriter ().write (")) {\n"); // NOI18N
                    }

                    CodeMultiGuardedLevelPresenter.generateMultiGuardedSectionCode (section, source);

                    if (indexBased)
                        section.getWriter ().write ("break;\n"); // NOI18N
                }

                if (! indexBased)
                    section.getWriter ().write ("}\n"); // NOI18N
                section.getWriter ().write ("}\n"); // NOI18N
            }

            section.getWriter ().commit ();
            section.switchToEditable (menu.getComponentID () + "-postAction"); // NOI18N
            section.getWriter ().write (" // enter post-action user code here\n").commit (); // NOI18N
            section.switchToGuarded ();
            section.getWriter ().write ("}\n"); // NOI18N
            section.getWriter ().write ("//</editor-fold>\n").commit (); // NOI18N
            section.close ();
        }

    }


}
