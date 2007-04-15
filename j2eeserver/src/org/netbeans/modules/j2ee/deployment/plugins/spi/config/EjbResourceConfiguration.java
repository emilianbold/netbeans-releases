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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.plugins.spi.config;

import java.io.File;
import org.netbeans.modules.j2ee.dd.api.common.ComponentInterface;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;

/**
 * Configuration for EJB resources.
 * <p>
 * Implementation of this interface should be registered in the {@link ModuleConfiguration}
 * lookup.
 * 
 * @since 1.23
 * @author sherold 
 */
public interface EjbResourceConfiguration {
    
    /**
     * Ensure resource is defined for the specified EJB (for example entity bean).
     *
     * @param ejb       EJB in question
     * @param jndiName  the JNDI name of the resource where the EJB is stored.
     * 
     * @throws ConfigurationException reports errors in setting the EJB resource.
     */
    void ensureResourceDefined(ComponentInterface ejb, String jndiName) throws ConfigurationException;
    
    /**
     * Binds EJB reference name with EJB name.
     * 
     * @param referenceName name used to identify the EJB
     * @param referencedEjbName name of the referenced EJB
     * 
     * @throws ConfigurationException if there is some problem with EJB configuration
     * 
     * @since 1.26
     */
    public void bindEjbReference(String referenceName, String referencedEjbName) throws ConfigurationException;

    /**
     * Binds EJB reference name with EJB name within the EJB scope.
     * 
     * @param ejbName EJB name
     * @param ejbType EJB type - the possible values are 
     *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.SESSION,
     *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.ENTITY and
     *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.MESSAGE_DRIVEN
     * @param referenceName name used to identify the referenced EJB
     * @param referencedEjbName name of the referenced EJB
     * 
     * @throws NullPointerException if any of parameters is null
     * @throws ConfigurationException if there is some problem with EJB configuration
     * @throws IllegalArgumentException if ejbType doesn't have one of allowed values
     * 
     * @since 1.26
     */
    public void bindEjbReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String referencedEjbName) throws ConfigurationException;
    
    
}
