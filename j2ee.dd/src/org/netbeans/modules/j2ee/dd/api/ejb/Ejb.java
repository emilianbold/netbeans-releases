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

package org.netbeans.modules.j2ee.dd.api.ejb;

// 
// This interface has all of the bean info accessor methods.
// 

import org.netbeans.api.web.dd.common.CommonDDBean;
import org.netbeans.api.web.dd.common.ComponentInterface;
import org.netbeans.api.web.dd.common.VersionNotSupportedException;

public interface Ejb extends CommonDDBean, ComponentInterface {
    
        //Entity & Session & Message Driven
        public static final String EJB_NAME = "EjbName";	// NOI18N
        public static final String EJB_CLASS = "EjbClass";	// NOI18N
        public static final String ENV_ENTRY = "EnvEntry";	// NOI18N
        public static final String EJB_REF = "EjbRef";	// NOI18N
	public static final String EJB_LOCAL_REF = "EjbLocalRef";	// NOI18N
        public static final String SERVICE_REF = "ServiceRef";	// NOI18N
	public static final String RESOURCE_REF = "ResourceRef";	// NOI18N
	public static final String RESOURCE_ENV_REF = "ResourceEnvRef";	// NOI18N
        public static final String MESSAGE_DESTINATION_REF = "MessageDestinationRef";	// NOI18N
        public static final String SECURITY_IDENTITY = "SecurityIdentity";	// NOI18N
        
        public String getEjbName();
        
        public void setEjbName(String value);
        
        public String getEjbClass();
        
        public void setEjbClass(String value);
        
        public void setEnvEntry(int index, EnvEntry value);
        
        public EnvEntry getEnvEntry(int index);
        
        public void setEnvEntry(EnvEntry[] value);
        
        public EnvEntry[] getEnvEntry();
        
        public int addEnvEntry(org.netbeans.modules.j2ee.dd.api.ejb.EnvEntry value);
        
        public int removeEnvEntry(org.netbeans.modules.j2ee.dd.api.ejb.EnvEntry value);
        
        public int sizeEnvEntry();
        
        public EnvEntry newEnvEntry();
        
        public void setEjbRef(int index, EjbRef value);
        
        public EjbRef getEjbRef(int index);
        
        public void setEjbRef(EjbRef[] value);
        
        public EjbRef[] getEjbRef();
        
        public int removeEjbRef(org.netbeans.modules.j2ee.dd.api.ejb.EjbRef value);
        
        public int addEjbRef(org.netbeans.modules.j2ee.dd.api.ejb.EjbRef value);
        
        public int sizeEjbRef();
        
        public EjbRef newEjbRef();
        
        public void setEjbLocalRef(int index, EjbLocalRef value);
        
        public EjbLocalRef getEjbLocalRef(int index);
        
        public void setEjbLocalRef(EjbLocalRef[] value);
        
        public EjbLocalRef[] getEjbLocalRef();
        
        public int addEjbLocalRef(org.netbeans.modules.j2ee.dd.api.ejb.EjbLocalRef value);
        
        public int removeEjbLocalRef(org.netbeans.modules.j2ee.dd.api.ejb.EjbLocalRef value);
        
        public int sizeEjbLocalRef();
                
        public EjbLocalRef newEjbLocalRef();
        
        public SecurityIdentity getSecurityIdentity ();
        
        public void setSecurityIdentity (SecurityIdentity value);
        
        public SecurityIdentity newSecurityIdentity();
        
        public void setResourceRef(int index, ResourceRef value);
        
        public ResourceRef getResourceRef(int index);
        
        public void setResourceRef(ResourceRef[] value);
        
        public ResourceRef[] getResourceRef();
        
        public int removeResourceRef(org.netbeans.modules.j2ee.dd.api.ejb.ResourceRef value);

	public int sizeResourceRef();
        
        public int addResourceRef(org.netbeans.modules.j2ee.dd.api.ejb.ResourceRef value);
        
        public ResourceRef newResourceRef();
        
        public void setResourceEnvRef(int index, ResourceEnvRef value);
        
        public ResourceEnvRef getResourceEnvRef(int index);
        
        public void setResourceEnvRef(ResourceEnvRef[] value);
        
        public ResourceEnvRef[] getResourceEnvRef();
        
        public int sizeResourceEnvRef();
        
        public int addResourceEnvRef(org.netbeans.modules.j2ee.dd.api.ejb.ResourceEnvRef value);

	public int removeResourceEnvRef(org.netbeans.modules.j2ee.dd.api.ejb.ResourceEnvRef value);
        
        public ResourceEnvRef newResourceEnvRef();
        
        //2.1
        public void setMessageDestinationRef(int index, MessageDestinationRef value) throws VersionNotSupportedException;

        public MessageDestinationRef getMessageDestinationRef(int index) throws VersionNotSupportedException;

        public void setMessageDestinationRef(MessageDestinationRef[] value) throws VersionNotSupportedException;

        public MessageDestinationRef[] getMessageDestinationRef() throws VersionNotSupportedException;
        
        public int removeMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.ejb.MessageDestinationRef value) throws VersionNotSupportedException;

	public int sizeMessageDestinationRef() throws VersionNotSupportedException;
        
        public int addMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.ejb.MessageDestinationRef value) throws VersionNotSupportedException;

        public MessageDestinationRef newMessageDestinationRef() throws VersionNotSupportedException;
        
        public void setServiceRef(int index, ServiceRef value) throws VersionNotSupportedException;

        public ServiceRef getServiceRef(int index) throws VersionNotSupportedException;

        public void setServiceRef(ServiceRef[] value) throws VersionNotSupportedException;

        public ServiceRef[] getServiceRef() throws VersionNotSupportedException;
        
        public int removeServiceRef(org.netbeans.modules.j2ee.dd.api.ejb.ServiceRef value) throws VersionNotSupportedException;

        public int sizeServiceRef() throws VersionNotSupportedException;

	public int addServiceRef(org.netbeans.modules.j2ee.dd.api.ejb.ServiceRef value) throws VersionNotSupportedException;

        public ServiceRef newServiceRef() throws VersionNotSupportedException;
         
}

