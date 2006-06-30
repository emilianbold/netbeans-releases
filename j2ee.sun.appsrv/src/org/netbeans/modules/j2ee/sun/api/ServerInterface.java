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
    
    void setAttribute(ObjectName name, javax.management.Attribute attribute) throws InstanceNotFoundException,
        AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException, RemoteException;
    
    DeploymentManager getDeploymentManager();
    
    MBeanServerConnection getMBeanServerConnection() throws RemoteException, ServerException;
     
    /*ServerMEJB*/Object getManagement();
}
