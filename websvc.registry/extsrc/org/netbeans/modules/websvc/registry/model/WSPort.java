/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry.model;

import com.sun.xml.rpc.processor.model.Port;
import com.sun.xml.rpc.processor.model.Operation;
import com.sun.xml.rpc.processor.model.java.JavaMethod;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class is meant to hold the pertinent data from the class
 * com.sun.xml.rpc.processor.model.Port.  This class will serve as a
 * JavaBean.  This class will be persisted with the WebServiceData.
 * @author  David Botterill
 */
public class WSPort {
    
    private String name;
    private String address;
    private String javaInterfaceName;
    private ArrayList methods = new ArrayList();
    
    
    /** Creates a new instance of WSPort */
    public WSPort() {
    }
    
    public WSPort(Port inPort) {
        if(null == inPort) return;
        this.address = inPort.getAddress();
        this.javaInterfaceName = inPort.getJavaInterface().getRealName();
        this.name = inPort.getName().getLocalPart();
        Iterator opIter = inPort.getOperations();
        while(opIter.hasNext()){
            JavaMethod javaMethod = (JavaMethod)((Operation)opIter.next()).getJavaMethod();
            this.addMethod(javaMethod);
        }
        
    }
    
    /**
     * Getter for property name.
     * @return Value of property name.
     */
    public java.lang.String getName() {
        return name;
    }
    
    /**
     * Setter for property name.
     * @param name New value of property name.
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }
    
    /**
     * Getter for property address.
     * @return Value of property address.
     */
    public java.lang.String getAddress() {
        return address;
    }
    
    /**
     * Setter for property address.
     * @param address New value of property address.
     */
    public void setAddress(java.lang.String address) {
        this.address = address;
    }
    
    /**
     * Getter for property javaInterfaceName.
     * @return Value of property javaInterfaceName.
     */
    public java.lang.String getJavaInterfaceName() {
        return javaInterfaceName;
    }
    
    /**
     * Setter for property javaInterfaceName.
     * @param javaInterfaceName New value of property javaInterfaceName.
     */
    public void setJavaInterfaceName(java.lang.String javaInterfaceName) {
        this.javaInterfaceName = javaInterfaceName;
    }
    
    public void addMethod(JavaMethod inMethod) {
        methods.add(inMethod);
    }
    /**
     * Getter for property methods.
     * @return Value of property methods.
     */
    public java.util.ArrayList getMethods() {
        return methods;
    }
    
    /**
     * Setter for property methods.
     * @param methods New value of property methods.
     */
    public void setMethods(java.util.ArrayList methods) {
        this.methods = methods;
    }
    
}
