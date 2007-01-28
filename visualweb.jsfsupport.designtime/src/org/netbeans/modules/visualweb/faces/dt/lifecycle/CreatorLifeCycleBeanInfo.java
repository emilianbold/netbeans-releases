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

package org.netbeans.modules.visualweb.faces.dt.lifecycle;

import com.sun.rave.faces.lifecycle.CreatorLifeCycle;
import com.sun.rave.faces.lifecycle.LifeCycleListener;
import java.awt.Image;
import java.beans.BeanDescriptor;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

public class CreatorLifeCycleBeanInfo extends SimpleBeanInfo {

    private static EventSetDescriptor[] eventSetDescriptors;
    private static MethodDescriptor[] methodDescriptors;
    private static PropertyDescriptor[] propertyDescriptors;

    public CreatorLifeCycleBeanInfo() throws NoSuchMethodException {}

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor descriptor = new BeanDescriptor(CreatorLifeCycle.class, null);
        descriptor.setValue("trayComponent", Boolean.FALSE); //NOI18N
        descriptor.setValue("instanceName", "lifeCycle"); //NOI18N
        return descriptor;
    }

    public int getDefaultEventIndex() {
        return 10;
    }

    //public int getDefaultPropertyIndex() {}

    public EventSetDescriptor[] getEventSetDescriptors() {
        try {
            if (eventSetDescriptors == null) {
                eventSetDescriptors = new EventSetDescriptor[] {
                    new EventSetDescriptor(CreatorLifeCycle.class, "lifeCycle", //NOI18N
                    LifeCycleListener.class,
                    new String[] {"preRestoreView"       , "postRestoreView", //NOI18N
                                  "preApplyRequestValues", "postApplyRequestValues", //NOI18N
                                  "preProcessValidations", "postProcessValidations", //NOI18N
                                  "preUpdateModelValues" , "postUpdateModelValues", //NOI18N
                                  "preInvokeApplication" , "postInvokeApplication", //NOI18N
                                  "preRenderResponse"    , "postRenderResponse"}, //NOI18N
                    "addLifeCycleListener", "removeLifeCycleListener") //NOI18N
                };
            }
            return eventSetDescriptors;
        } catch (IntrospectionException e) {
            System.err.println(">>>>>>" + e); //NOI18N
            return null;
        }
    }

    public Image getIcon(int iconKind) {
        return loadImage("CreatorLifeCycleIconColor16.gif"); //NOI18N
    }

    public synchronized MethodDescriptor[] getMethodDescriptors() {
        return new MethodDescriptor[0];
    }

    public synchronized PropertyDescriptor[] getPropertyDescriptors() {
        return new PropertyDescriptor[0];
    }
}
