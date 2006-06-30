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


package org.netbeans.modules.j2ee.sun.share.config;

import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.schema2beans.*;
import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class DDProxy extends DDCommon implements PropertyChangeListener {

    DDProxy(DDCommon parent, BaseBean bean, String dtdName, ModuleDDSupport support) {
        super(parent,bean,support,dtdName);
        bean.addPropertyChangeListener(this);
    }

    final boolean isProxy() { 
        return true;
    }
    
    void cancelProxy() {
        bean.removePropertyChangeListener(this);
    }
    
    public void propertyChange(PropertyChangeEvent event) {
        
//        System.out.println("Proxy event");
        Object oldValue = event.getOldValue();
//        System.out.println("Old value" + oldValue);
        Object newValue = event.getNewValue();
//        System.out.println("New value" + newValue);
        String name = event.getPropertyName();
//        System.out.println("Name " + name);
//        System.out.println("Source " + support.getBean((BaseBean)event.getSource()));
        
        support.processEvent(oldValue,newValue,support.getBean(name,bean).proxy,event);
    }
    
}

