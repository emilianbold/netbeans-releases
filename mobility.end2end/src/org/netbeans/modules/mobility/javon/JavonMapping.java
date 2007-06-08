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

package org.netbeans.modules.mobility.javon;

import java.util.Set;
import org.netbeans.modules.mobility.e2e.classdata.ClassData;
import org.netbeans.modules.mobility.e2e.classdata.ClassDataRegistry;
import org.netbeans.modules.mobility.e2e.classdata.MethodData;

/**
 *
 * @author Michal Skvor
 */
public interface JavonMapping {

    public ClassDataRegistry getRegistry();
    
    public Client getClientMapping();
    
    public Server getServerMapping();
    
    public Service getServiceMapping( String serviceType );
    
    public Set<Service> getServiceMappings();
            
    public String getServletURL();
    
    public void setProperty( String key, Object value );
    
    public Object getProperty( String key );
    
    public static interface Client {
        
        public String getPackageName();
        
        public String getOutputDirectory();
        
        public String getClassName();
    }
    
    public static interface Server {
        
        public String getLocation();
        
        public String getPort();
        
        public String getServletLocation();
        
        public String getProjectName();
        
        public String getPackageName();
        
        public String getOutputDirectory();
        
        public String getClassName();
    }
    
    public static interface Service {
        
        public String getPackageName();
        
        public String getClassName();
        
        public String getType();
        
        public Set<MethodData> getMethods();
        
        public Set<ClassData> getParameterTypes();
        
        public Set<ClassData> getReturnTypes();
        
        public Set<ClassData> getSupportedTypes();
    }
}
