/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.mobility.e2e.mapping;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.mobility.e2e.classdata.ClassData;
import org.netbeans.modules.mobility.e2e.classdata.ClassDataRegistry;
import org.netbeans.modules.mobility.e2e.classdata.MethodData;
import org.netbeans.modules.mobility.e2e.classdata.MethodParameter;
import org.netbeans.modules.mobility.javon.JavonMapping;

/**
 *
 * @author Michal Skvor
 */
public class JavonMappingImpl implements JavonMapping {
    
    private Client clientMapping;
    private Server serverMapping;
    
    private Service serviceMapping;    
    private Map<String, Service> serviceMappings;
    
    private ClassDataRegistry registry;
    
    private Map<String, Object> properties;

    private String servletURL;
    
    /**
     * Creates new JavonMapping class with specified ClassDataRegistry
     * 
     * @param registry to be used for this mapping
     */
    public JavonMappingImpl( ClassDataRegistry registry ) {
        this.registry = registry;
        
        serviceMappings = new HashMap<String, Service>();
        properties = new HashMap<String, Object>();
    }
    
    private JavonMappingImpl() {}
    
    /**
     * Return ClassDataRegistry bounded with this JavonMapping
     * 
     * @return ClassDataRegistry instance
     */
    public ClassDataRegistry getRegistry() {
        return registry;
    }
    
    /**
     * Set client mapping
     * 
     * @param clientMapping mapping for client
     */
    public void setClientMapping( Client clientMapping ) {
        this.clientMapping = clientMapping;
    }
    
    /**
     * Get client mapping
     * 
     * @return client mapping
     */
    public Client getClientMapping() {
        return clientMapping;
    }
    
    /**
     * Set server mapping
     * 
     * @param serverMapping mapping for server
     */
    public void setServerMapping( Server serverMapping ) {
        this.serverMapping = serverMapping;
    }
    
    /**
     * Get server mapping
     *
     * @return maping for server
     */
    public JavonMapping.Server getServerMapping() {
        return serverMapping;
    }
    
    /**
     * 
     * @param serviceMapping 
     */
    public void addServiceMaping( Service serviceMapping ) {
        serviceMappings.put( serviceMapping.getType(), serviceMapping );
    }
    
    /**
     * 
     * @param serviceType 
     * @return 
     */
    public JavonMapping.Service getServiceMapping( String serviceType ) {
        return serviceMappings.get( serviceType );
    }
    
    /**
     * 
     * @return 
     */
    public Set<JavonMapping.Service> getServiceMappings() {
        return new HashSet<JavonMapping.Service>( serviceMappings.values());
    }
            
    /**
     * Set the URL by which the server is accessed.
     *
     * @param servletURL the URL.
     */
    public void setServletURL( String servletURL ) {
        this.servletURL = servletURL;
    }

    /**
     * Gets the URL by which the server is accessed.
     *
     * @return the URL.
     */
    public String getServletURL() {
        return servletURL;
    }
    
    /**
     * Put user defined property
     * 
     * @param key key of the property
     * @param value value of the property
     */
    public void setProperty( String key, Object value ) {
        properties.put( key, value );
    }
    
    /**
     * 
     * @param key 
     * @return 
     */
    public Object getProperty( String key ) {
        return properties.get( key );
    }
    
    /**
     * Represents client mapping
     */
    public static class Client implements JavonMapping.Client {
        
        private String packageName;
        private String outputDirectory;
        private String className;
        
        /**
         * 
         * @param packageName 
         */
        public void setPackageName( String packageName ) {
            this.packageName = packageName;
        }
        
        /**
         * 
         * @return 
         */
        public String getPackageName() {
            return packageName;
        }
        
        /**
         * 
         * @param outputDirectory 
         */
        public void setOutputDirectory( String outputDirectory ) {
            this.outputDirectory = outputDirectory;
        }
        
        /**
         * 
         * @return 
         */
        public String getOutputDirectory() {
            return outputDirectory;
        }
        
        /**
         * 
         * @param className 
         */
        public void setClassName( String className ) {
            this.className = className;
        }
        
        /**
         * 
         * @return 
         */
        public String getClassName() {
            return className;
        }
    }
    
    /**
     * Represents server mapping
     */
    public static class Server implements JavonMapping.Server {
        
        private String location;
        private String port;
        private String servletLocation;
        
        private String projectName;
        private String packageName;
        private String className;
        private String outputDirectory;
        
        /**
         * @param location
         */
        public void setLocation( String location ) {
            this.location = location;
        }
        
        public String getLocation() {
            return location;
        }

        public void setPort( String port ) {
            this.port = port;
        }
        
        public String getPort() {
            return port;
        }

        public void setServletLocation( String location ) {
            this.servletLocation = location;
        }
        
        public String getServletLocation() {
            return servletLocation;
        }
        
        /**
         * 
         * @param projectName 
         */
        public void setProjectName( String projectName ) {
            this.projectName = projectName;
        }
        
        /**
         * 
         * @return 
         */
        public String getProjectName() {
            return projectName;
        }
        
        /**
         * 
         * @param packageName 
         */
        public void setPackageName( String packageName ) {
            this.packageName = packageName;
        }
        
        /**
         * 
         * @return 
         */
        public String getPackageName() {
            return packageName;
        }
        
        /**
         * 
         * @param className 
         */
        public void setClassName( String className ) {
            this.className = className;
        }
        
        /**
         * 
         * @return 
         */
        public String getClassName() {
            return className;
        }
        
        /**
         * 
         * @param outputDirectory 
         */
        public void setOutputDirectory( String outputDirectory ) {
            this.outputDirectory = outputDirectory;
        }
        
        /**
         * 
         * @return 
         */
        public String getOutputDirectory() {
            return outputDirectory;
        }
    }
    
    /**
     * Represents mapping of the service
     */
    public static class Service implements JavonMapping.Service {
        
        /** */
        public static final String STUB         = "stub";
        /** */
        public static final String SYNCHRONOUS  = "synchronous";
        /** */
        public static final String GROUPING     = "grouping";
        
        private String packageName;
        private String className;
        private Set<MethodData> methods = new HashSet<MethodData>();
                
        /**
         * Set package name for this service
         * 
         * @param packageName 
         */
        public void setPackageName( String packageName ) {
            this.packageName = packageName;
        }

        /**
         * Returns package name of this service
         * 
         * @return package name of the service
         */
        public String getPackageName() {
            return packageName;
        }

        /**
         * Set name for this service
         * 
         * @param className simple name of the service
         */
        public void setClassName( String className ) {
            this.className = className;
        }
        
        /**
         * Return simple name of this service
         * 
         * @return simple name of this service
         */
        public String getClassName() {
            return className;
        }
        
        /**
         * Returns fully qualified name for this service
         * 
         * @return fully qualified name of the service
         */
        public String getType() {
            if( packageName.length() > 0 )
                return packageName + "." + className;
            return className;
        }
        
        /**
         * Gets the list of exported methods as a Set
         *
         * @return an unmodifiable set of methods
         */
        public Set<MethodData> getMethods() {
            return Collections.unmodifiableSet( methods );
        }
        
        /**
         * @param methodData
         */
        public void addMethod( MethodData methodData ) {
            methods.add( methodData );
        }
        
        /**
         * Returns all used parameter types in service
         * 
         * @return Set of parameters 
         */
        public Set<ClassData> getParameterTypes() {
            Set<ClassData> paramTypes = new HashSet<ClassData>();
            for( MethodData method : methods ) {
//                System.err.println(" - method = " + method.getName());
                for( MethodParameter param : method.getParameters()) {
                    ClassData cd = param.getType();
                    paramTypes.addAll( cd.getSerializer().getReferencedTypes( cd, paramTypes ));
                }
            }
            return Collections.unmodifiableSet( paramTypes );
        }
        
        /**
         * Returns all used return types in service
         * 
         * @return Set of return types
         */
        public Set<ClassData> getReturnTypes() {
            Set<ClassData> returnTypes = new HashSet<ClassData>();
            for( MethodData method : methods ) {
                ClassData cd = method.getReturnType();
                returnTypes.addAll( cd.getSerializer().getReferencedTypes( cd, returnTypes ));
            }
            return Collections.unmodifiableSet( returnTypes );
        }
        
        /**
         * 
         * @return 
         */
        public Set<ClassData> getSupportedTypes() {
            Set<ClassData> result = new HashSet<ClassData>();
            result.addAll( getParameterTypes());
            result.addAll( getReturnTypes());
            return result;
        }
    }
}
