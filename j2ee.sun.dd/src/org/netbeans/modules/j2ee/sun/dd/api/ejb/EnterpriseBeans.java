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
 * EnterpriseBeans.java
 *
 * Created on November 17, 2004, 4:38 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface EnterpriseBeans extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String NAME = "Name";	// NOI18N
    public static final String UNIQUE_ID = "UniqueId";	// NOI18N
    public static final String EJB = "Ejb";	// NOI18N
    public static final String PM_DESCRIPTORS = "PmDescriptors";	// NOI18N
    public static final String CMP_RESOURCE = "CmpResource";	// NOI18N
    public static final String MESSAGE_DESTINATION = "MessageDestination";	// NOI18N
    public static final String WEBSERVICE_DESCRIPTION = "WebserviceDescription";	// NOI18N
        
    public String getName();
    public void setName(String value);
    
    public String getUniqueId();
    public void setUniqueId(String value);
    
    public Ejb[] getEjb(); 
    public Ejb getEjb(int index);
    public void setEjb(Ejb[] value);
    public void setEjb(int index, Ejb value);
    public int addEjb(Ejb value);
    public int removeEjb(Ejb value);
    public int sizeEjb();
    public Ejb newEjb();
    
    public PmDescriptors getPmDescriptors();
    public void setPmDescriptors(PmDescriptors value);
    public PmDescriptors newPmDescriptors(); 
    
    public CmpResource getCmpResource();
    public void setCmpResource(CmpResource value); 
    public CmpResource newCmpResource();
    
    public MessageDestination[] getMessageDestination(); 
    public MessageDestination getMessageDestination(int index);
    public void setMessageDestination(MessageDestination[] value);
    public void setMessageDestination(int index, MessageDestination value);
    public int addMessageDestination(MessageDestination value);
    public int removeMessageDestination(MessageDestination value);
    public int sizeMessageDestination(); 
    
    public WebserviceDescription[] getWebserviceDescription(); 
    public WebserviceDescription getWebserviceDescription(int index);
    public void setWebserviceDescription(WebserviceDescription[] value);
    public void setWebserviceDescription(int index, WebserviceDescription value);
    public int addWebserviceDescription(WebserviceDescription value);
    public int removeWebserviceDescription(WebserviceDescription value);
    public int sizeWebserviceDescription(); 
}
