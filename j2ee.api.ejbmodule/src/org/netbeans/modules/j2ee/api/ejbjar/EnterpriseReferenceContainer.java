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
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
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
    
    /**
     * Add given ejb reference into deployment descriptor. This method should
     * also ensure that the supplied target is added to the class path (as the
     * ejb interfaces will be referenced from this class) as well as the 
     * deployed manifest. The deployed manifest is the generic J2EE compliant
     * strategy, application server specific behavior such as delegating to the
     * parent class loader could also be used. The main point is not to
     * include the target in the deployed archive but instead reference the 
     * interface jar (or standard ejb module) included in the J2EE application.
     * @param ref -- ejb reference this will include the ejb link which assumes
     * root packaging in the containing application. The name of this ref should
     * be considered a hint and made unique within the deployment descriptor.
     * @param referencedClassName -- name of referenced class, this can be used
     * to determine where to add the deployment descriptor entry. This class
     * will be modified with a method or other strategy to obtain the ejb.
     * @param target to include in the build
     * @return actual jndi name used in deployment descriptor
     */
    String addEjbReference(EjbRef ref, FileObject referencingFile, String referencingClass, AntArtifact target) throws IOException;
    
    /**
     * @see #addEjbReference(EjbRef, FileObject, String, AntArtifact)
     */
    String addEjbLocalReference(EjbLocalRef localRef, FileObject referencingFile, String referencingClass, AntArtifact target) throws IOException;
    
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
    String addDestinationRef(MessageDestinationRef ref, FileObject referencingFile, String referencingClass) throws IOException;

    MessageDestinationRef createDestinationRef(String className) throws IOException;
    
    /**
     * Add given resource reference into the deployment descriptor.
     * @param ref reference to resource used
     * @param referencingClass class which will use the resource
     * @return unique jndi name used in deployment descriptor
     */
    String addResourceRef(ResourceRef ref, FileObject referencingFile, String referencingClass) throws IOException;
    
    /**
     * Create resource ref instance based on current project type.
     * @param className to determine context from
     */
    ResourceRef createResourceRef(String className) throws IOException;
}
