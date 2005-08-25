/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
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

