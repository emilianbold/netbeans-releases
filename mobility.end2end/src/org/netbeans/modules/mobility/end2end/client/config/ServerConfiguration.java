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
 * ServerConfiguration.java
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
public class ServerConfiguration {
    
    private String projectName;
    private String projectPath;
    private ClassDescriptor classDescriptor;
    private Properties properties;
    
    public static final String PROP_TRACE = "trace"; // NOI18N
    /**
     * Sets name of the server project
     *
     * @param name of the project
     */
    public void setProjectName( final String name ) {
        this.projectName = name;
    }
    
    /**
     * Gets name of the server project
     *
     * @return name of the project
     */
    public String getProjectName() {
        return projectName;
    }
    
    public void setProjectPath( final String path ) {
        this.projectPath = path;
    }
    
    public String getProjectPath() {
        return projectPath;
    }
    
    /**
     * Sets class representing bridge servlet implementation
     *
     * @param clazz representing servlet implementation
     */
    public void setClassDescriptor( final ClassDescriptor clazz ) {
        this.classDescriptor = clazz;
    }
    
    /**
     * Returns @link ClassDescriptor representing bridge servlet implementation
     *
     * @return servlet @link ClassDescriptor implementation
     */
    public ClassDescriptor getClassDescriptor() {
        return classDescriptor;
    }
    
    /**
     * Sets server properties
     *
     * @param props properties to be set
     */
    public void setProperties( final Properties props ) {
        this.properties = props;
    }
    
    /**
     * Returns properties for server configuration
     *
     * @return server configuration properties
     */
    public Properties getProperties() {
        return properties;
    }
    
}
