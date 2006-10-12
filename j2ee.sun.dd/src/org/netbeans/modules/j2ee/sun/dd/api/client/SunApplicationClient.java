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
 * SunApplicationClient.java
 *
 * Created on February 10, 2006, 11:12 AM
 *
 */

package org.netbeans.modules.j2ee.sun.dd.api.client;

import org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef;

import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;

/**
 *
 * @author Nitya Doraisamy
 */
public interface SunApplicationClient extends org.netbeans.modules.j2ee.sun.dd.api.RootInterface {
    public static final String VERSION_5_0_0 = "5.00"; //NOI18N
    public static final String VERSION_1_4_1 = "1.41"; //NOI18N
    public static final String VERSION_1_4_0 = "1.40"; //NOI18N
    public static final String VERSION_1_3_0 = "1.30"; //NOI18N
    
    public static final String EJB_REF = "EjbRef";	// NOI18N
    public static final String RESOURCE_REF = "ResourceRef";	// NOI18N
    public static final String RESOURCE_ENV_REF = "ResourceEnvRef";	// NOI18N
    public static final String SERVICE_REF = "ServiceRef";	// NOI18N
    public static final String MESSAGE_DESTINATION_REF = "MessageDestinationRef";	// NOI18N
    public static final String MESSAGE_DESTINATION = "MessageDestination";	// NOI18N
    public static final String JAVA_WEB_START_ACCESS = "JavaWebStartAccess";	// NOI18N
    
    public void setEjbRef(int index, EjbRef value);
    public EjbRef getEjbRef(int index);
    public int sizeEjbRef();
    public void setEjbRef(EjbRef[] value);
    public EjbRef[] getEjbRef();
    public int addEjbRef(EjbRef value);
    public int removeEjbRef(EjbRef value);
    public EjbRef newEjbRef();
    
    public void setResourceRef(int index, ResourceRef value);
    public ResourceRef getResourceRef(int index);
    public int sizeResourceRef();
    public void setResourceRef(ResourceRef[] value);
    public ResourceRef[] getResourceRef();
    public int addResourceRef(ResourceRef value);
    public int removeResourceRef(ResourceRef value);
    public ResourceRef newResourceRef();
    
    public void setResourceEnvRef(int index, ResourceEnvRef value);
    public ResourceEnvRef getResourceEnvRef(int index);
    public int sizeResourceEnvRef();
    public void setResourceEnvRef(ResourceEnvRef[] value);
    public ResourceEnvRef[] getResourceEnvRef();
    public int addResourceEnvRef(ResourceEnvRef value);
    public int removeResourceEnvRef(ResourceEnvRef value);
    public ResourceEnvRef newResourceEnvRef();
    
    //Following not in 1_3-0
    public void setServiceRef(int index, ServiceRef value);
    public ServiceRef getServiceRef(int index);
    public int sizeServiceRef();
    public void setServiceRef(ServiceRef[] value);
    public ServiceRef[] getServiceRef();
    public int addServiceRef(ServiceRef value);
    public int removeServiceRef(ServiceRef value);
    public ServiceRef newServiceRef();
    
    public void setMessageDestination(int index, MessageDestination value);
    public MessageDestination getMessageDestination(int index);
    public int sizeMessageDestination();
    public void setMessageDestination(MessageDestination[] value);
    public MessageDestination[] getMessageDestination();
    public int addMessageDestination(MessageDestination value);
    public int removeMessageDestination(MessageDestination value);
    public MessageDestination newMessageDestination();
    
    //Following are new in 5_0-0
    public void setMessageDestinationRef(int index, MessageDestinationRef value) throws VersionNotSupportedException;
    public MessageDestinationRef getMessageDestinationRef(int index) throws VersionNotSupportedException;
    public int sizeMessageDestinationRef() throws VersionNotSupportedException;
    public void setMessageDestinationRef(MessageDestinationRef[] value) throws VersionNotSupportedException;
    public MessageDestinationRef[] getMessageDestinationRef() throws VersionNotSupportedException;
    public int addMessageDestinationRef(MessageDestinationRef value) throws VersionNotSupportedException;
    public int removeMessageDestinationRef(MessageDestinationRef value) throws VersionNotSupportedException;
    public MessageDestinationRef newMessageDestinationRef() throws VersionNotSupportedException;
    
    public void setJavaWebStartAccess(JavaWebStartAccess value) throws VersionNotSupportedException;
    public JavaWebStartAccess getJavaWebStartAccess() throws VersionNotSupportedException;
    public JavaWebStartAccess newJavaWebStartAccess() throws VersionNotSupportedException;
        
}
