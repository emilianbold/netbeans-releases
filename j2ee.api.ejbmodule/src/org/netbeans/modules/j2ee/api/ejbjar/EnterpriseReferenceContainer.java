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

package org.netbeans.modules.j2ee.api.ejbjar;

import java.io.IOException;
import org.openide.filesystems.FileObject;

/**
 * Instances of this class should be supplied by projects to indicate that
 * enterprise resources (J2EE declarative resources such as DataSources,
 * Enterprise JavaBeans, and JMS queues and topics) can be used. This
 * class will be invoked to incorporate this resource into the J2EE project. 
 * This api is current experimental and subject to change.
 * @author Chris Webster
 */
public interface EnterpriseReferenceContainer {
    
    String addEjbReference(EjbReference ref, String ejbRefName, FileObject referencingFile, String referencingClass) throws IOException;
    
    String addEjbLocalReference(EjbReference ref, String ejbRefName, FileObject referencingFile, String referencingClass) throws IOException;

    /**
     * @return name of the service locator defined for this project or null
     * if service locator is not being used
     */
    String getServiceLocatorName();
    
    /**
     * set name of service locator fo this project. 
     * @param serviceLocator used in this project
     */
    void setServiceLocatorName(String serviceLocator) throws IOException;
    
    /**
     * Add given message destination reference into the deployment descriptor
     * @param ref to destination
     * @param referencingClass class using the destination
     * @return unique jndi name used in the deployment descriptor
     */
    String addDestinationRef(MessageDestinationReference ref, FileObject referencingFile, String referencingClass) throws IOException;

    /**
     * Add given resource reference into the deployment descriptor.
     * @param ref reference to resource used
     * @param referencingClass class which will use the resource
     * @return unique jndi name used in deployment descriptor
     */
    String addResourceRef(ResourceReference ref, FileObject referencingFile, String referencingClass) throws IOException;
    
}
