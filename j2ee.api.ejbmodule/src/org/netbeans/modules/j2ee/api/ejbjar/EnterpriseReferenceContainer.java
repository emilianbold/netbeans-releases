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

package org.netbeans.modules.j2ee.api.ejbjar;

import java.io.IOException;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.web.dd.EjbLocalRef;
import org.netbeans.api.web.dd.EjbRef;
import org.netbeans.api.web.dd.ResourceRef;

/**
 * Instances of this class should be supplied by projects to indicate that 
 * enterprise resources (J2EE declarative resources such as DataSources, 
 * Enterprise JavaBeans, and JMS queues and topics) can be used. This
 * class will be invoked to incorporate this resource into the J2EE project. 
 * This api is current experimental and subject to change.
 * @author Chris Webster
 */
public abstract class EnterpriseReferenceContainer {
    
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
    public abstract String addEjbReferernce(EjbRef ref, String referenceClassName,  AntArtifact target) throws IOException;
    /**
     * @see #addEjbReference(EjbRef, String, AntArtifact)
     */
    public abstract String addEjbLocalReference(EjbLocalRef localRef, String referencedClassName, AntArtifact target) throws IOException;
    
    /**
     * @return name of the service locator defined for this project or null
     * if service locator is not being used
     */
    public abstract String getServiceLocatorName();
    
    // JMS
    
    /**
     * Add given resource reference into the deployment descriptor.
     * @param ref reference to resource used
     * @param referencingClass class which will use the resource
     * @return unique jndi name used in deployment descriptor
     */
    public abstract String addResourceRef(ResourceRef ref, String referencingClass) throws IOException;
    
    /**
     * Create resource ref instance based on current project type.
     * @param className to determine context from
     */
    public abstract ResourceRef createResourceRef(String className) throws IOException;
}
