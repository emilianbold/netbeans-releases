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
 * ClientConfiguration.java
 *
 * Created on June 22, 2005, 1:45 PM
 *
 */
package org.netbeans.modules.mobility.end2end.client.config;

import java.util.Properties;

/**
 *
 * @author Michal Skvor
 */
public class ClientConfiguration {
    
    private String projectName;
    private ClassDescriptor classDescriptor;
    private Properties properties;
    
    public final static String PROP_CREATE_STUBS    = "createStubs";    // NOI18N
    public final static String PROP_MULTIPLE_CALL   = "multipleCall";   // NOI18N
    public final static String PROP_FLOATING_POINT  = "floatingPoint";  // NOI18N
    public final static String PROP_TRACE           = "trace";          // NOI18N
    
    /**
     * Sets name of the client project
     */
    public void setProjectName( final String name ) {
        projectName = name;
    }
    
    /**
     * Returns name of the client name
     *
     * @return name of the project
     */
    public String getProjectName() {
        return projectName;
    }
    
    /**
     * Sets class representing client service class
     * implementation
     *
     * @param clazz class
     */
    public void setClassDescriptor( final ClassDescriptor clazz ) {
        classDescriptor = clazz;
    }
    
    /**
     * Gets ClassData representing client service class
     *
     * @return class
     */
    public ClassDescriptor getClassDescriptor() {
        return classDescriptor;
    }
    
    /**
     * Set client properties
     *
     * @param props properties
     */
    public void setProperties( final Properties props ) {
        properties = props;
    }
    
    /**
     * Gets client properties
     *
     * @return client properties
     */
    public Properties getProperties() {
        return properties;
    }
    
}
