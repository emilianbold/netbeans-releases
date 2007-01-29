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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.web.ui.dt.component.util;

import com.sun.rave.designtime.Constants;
import com.sun.rave.faces.event.Action;

import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import javax.faces.validator.Validator;

/**
 * Miscellaneous design-time utility methods
 *
 * @author Edwin Goei
 * @author gjmurphy
 */
public class DesignUtil {

    private static Pattern numericalSuffixPattern = Pattern.compile("\\d*$");

    /**
     * A utility method that locates the numerical suffix of a typical bean
     * instance name, and returns it. If no numerical suffix is found the
     * empty string is returned.
     */
    public static String getNumericalSuffix(String name) {
        Matcher matcher = numericalSuffixPattern.matcher(name);
        matcher.find();
        return matcher.group();
    }

    /**
     * A utility method that creates <code>validate</code> and <code>valueChange</code>
     * event descriptors for input components (components that implement {@link
     * javax.faces.component.EditableValueHolder}). The events are associated with
     * their respective JSF handler methods (<code>Validator.validate()</code> and
     * <code>ValueChangeListener.processValueChange()</coded>), and with their respective
     * properties (<code>valueChangeListener</code> and <code>validator</code>). As
     * a result, if the user selects a new handler for the event in the properties sheet,
     * a method with the correct signature will be created, and the corresponding
     * property's value will be set to a method binding expression that points to
     * the newly created method.
     */
    public static EventSetDescriptor[] generateInputEventSetDescriptors(BeanInfo beanInfo) {
        try {
            PropertyDescriptor valueChangeDescriptor = null;
            PropertyDescriptor validateDescriptor = null;
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < propertyDescriptors.length && (valueChangeDescriptor == null || validateDescriptor == null); i++) {
                if (propertyDescriptors[i].getName().equals("valueChangeListener")) //NOI18N
                    valueChangeDescriptor = propertyDescriptors[i];
                else if (propertyDescriptors[i].getName().equals("validator")) //NOI18N
                    validateDescriptor = propertyDescriptors[i];
            }
            EventSetDescriptor valueChangeEventDescriptor =
                new EventSetDescriptor("valueChangeListener", ValueChangeListener.class,  //NOI18N
                    new Method[] {
                        ValueChangeListener.class.getMethod("processValueChange",  //NOI18N
                            new Class[] {ValueChangeEvent.class})},
                    null, null);
            valueChangeEventDescriptor.setDisplayName(
                    DesignMessageUtil.getMessage(DesignUtil.class, "DesignUtil.event.valueChange")); //NOI18N
            valueChangeEventDescriptor.setValue(Constants.EventSetDescriptor.BINDING_PROPERTY,
                    valueChangeDescriptor);
            valueChangeEventDescriptor.setValue(Constants.EventDescriptor.PARAMETER_NAMES,
                    new String[] { "event" }); //NOI18N
            valueChangeEventDescriptor.setShortDescription(valueChangeDescriptor.getShortDescription());
            EventSetDescriptor validateEventDescriptor =
                new EventSetDescriptor("validator", Validator.class, //NOI18N
                    new Method[] {
                        Validator.class.getMethod("validate",  //NOI18N
                            new Class[] {FacesContext.class, UIComponent.class, Object.class})},
                    null, null);
            validateEventDescriptor.setDisplayName(
                    DesignMessageUtil.getMessage(DesignUtil.class, "DesignUtil.event.validate"));
            validateEventDescriptor.setValue(Constants.EventSetDescriptor.BINDING_PROPERTY,
                    validateDescriptor);
            validateEventDescriptor.setValue(Constants.EventDescriptor.PARAMETER_NAMES, 
                    new String[] { "context", "component", "value" }); //NOI18N
            validateEventDescriptor.setValue(Constants.EventDescriptor.REQUIRED_IMPORTS, 
                    new String[] { "javax.faces.application.FacesMessage", "javax.faces.validator.ValidatorException" }); //NOI18N
            validateEventDescriptor.setShortDescription(validateDescriptor.getShortDescription());
            return new EventSetDescriptor[] {valueChangeEventDescriptor, validateEventDescriptor};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    /**
     * A utility method that creates an <code>action</code>  event descriptors for 
     * command components (components that implement {@link
     * javax.faces.component.ActionSource}). The event is associated with its
     * respective JSF handler method (<code>ActionListener.processAction()</coded>),
     * and with its respective property (<code>action</code>). As a result, if 
     * the user selects a new handler for the event in the properties sheet,
     * a method with the correct signature will be created, and the corresponding
     * property's value will be set to a method binding expression that points to
     * the newly created method.
     */
    public static EventSetDescriptor[] generateCommandEventSetDescriptors(BeanInfo beanInfo) {
        try {
            PropertyDescriptor actionDescriptor = null;
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < propertyDescriptors.length && actionDescriptor == null; i++) {
                if (propertyDescriptors[i].getName().equals("action")) //NOI18N
                    actionDescriptor = propertyDescriptors[i];
            }
            EventSetDescriptor actionEventDescriptor = 
                    new EventSetDescriptor("action", Action.class,  //NOI18N
                        new Method[] {Action.class.getMethod("action", new Class[] {})},  //NOI18N
                        null, null);
            actionEventDescriptor.setDisplayName(
                    DesignMessageUtil.getMessage(DesignUtil.class, "DesignUtil.event.action"));
            actionEventDescriptor.setValue(Constants.EventSetDescriptor.BINDING_PROPERTY,
                    actionDescriptor);
            actionEventDescriptor.setShortDescription(actionDescriptor.getShortDescription());
            return new EventSetDescriptor[] {actionEventDescriptor};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
}
