/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
