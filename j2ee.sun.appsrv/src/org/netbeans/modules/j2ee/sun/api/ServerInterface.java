/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * ServerInterface.java
 *
 * Created on October 26, 2004, 11:50 AM
 */

package org.netbeans.modules.j2ee.sun.api;

import javax.management.MBeanInfo;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.IntrospectionException;
import javax.management.InstanceNotFoundException;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.AttributeList;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Set;

import javax.enterprise.deploy.spi.DeploymentManager;
//import org.netbeans.modules.j2ee.sun.share.management.ServerMEJB;
/**
 *
 * @author  Nitya Doraisamy
 */
public interface ServerInterface {
    
    Object getAttribute(ObjectName name, String attribute) throws MBeanException,
        AttributeNotFoundException, InstanceNotFoundException, ReflectionException, RemoteException;
    
    
    AttributeList getAttributes(ObjectName name, String[] attributes) throws
        ReflectionException, InstanceNotFoundException, RemoteException;
    
    MBeanInfo getMBeanInfo(ObjectName name) throws IntrospectionException, InstanceNotFoundException,
        ReflectionException, RemoteException;
    
    Object invoke(ObjectName name, String operationName, Object[] params, String[] signature) throws InstanceNotFoundException,
        MBeanException, ReflectionException, RemoteException;
    
    Set queryNames(ObjectName name, javax.management.QueryExp query) throws RemoteException;
    
    void setAttribute(ObjectName name, javax.management.Attribute attribute) throws InstanceNotFoundException,
        AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException, RemoteException;
    
    AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException,
        ReflectionException, RemoteException;
    
    DeploymentManager getDeploymentManager();
    
    MBeanServerConnection getMBeanServerConnection() throws RemoteException, ServerException;
     
    /*ServerMEJB*/Object getManagement();
}
