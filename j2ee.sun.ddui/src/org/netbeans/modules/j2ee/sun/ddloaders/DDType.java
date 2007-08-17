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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.sun.ddloaders;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.deploy.shared.ModuleType;
import org.openide.ErrorManager;

/**
 *
 * @author Peter Williams
 */
public final class DDType {

    private static final String NAME_SUNAPPCLIENT = "sun-application-client.xml"; // NOI18N
    private static final String NAME_SUNAPPLICATION = "sun-application.xml"; // NOI18N
    private static final String NAME_SUNCMPMAPPING = "sun-cmp-mappings.xml"; // NOI18N
    private static final String NAME_SUNEJBJAR = "sun-ejb-jar.xml"; // NOI18N
    private static final String NAME_SUNWEBAPP = "sun-web.xml"; // NOI18N
    private static final String NAME_SUNRESOURCE = "sun-resources.xml"; // NOI18N
    
    // Type declarations for the different descriptor types.
    public static DDType DD_SUN_WEB_APP = new DDType(NAME_SUNWEBAPP, ModuleType.WAR, DDViewFactory.SunWebDDViewFactory.class);
    public static DDType DD_SUN_EJB_JAR = new DDType(NAME_SUNEJBJAR, ModuleType.EJB, DDViewFactory.SunEjbJarDDViewFactory.class);
    public static DDType DD_SUN_APP_CLIENT = new DDType(NAME_SUNAPPCLIENT, ModuleType.CAR, DDViewFactory.SunAppClientDDViewFactory.class);
    public static DDType DD_SUN_APPLICATION = new DDType(NAME_SUNAPPLICATION, ModuleType.EAR, DDViewFactory.SunApplicationDDViewFactory.class);
    public static DDType DD_SUN_CMP_MAPPINGS = new DDType(NAME_SUNCMPMAPPING, ModuleType.EJB, DDViewFactory.SunCmpMappingsDDViewFactory.class);
    public static DDType DD_SUN_RESOURCE = new DDType(NAME_SUNRESOURCE, null, DDViewFactory.SunResourceDDViewFactory.class);

    // Various indexes for finding a DDType object
    private static Map<String, DDType> fileToTypeMap = new HashMap<String, DDType>(11);

    static {
        fileToTypeMap.put(NAME_SUNWEBAPP, DD_SUN_WEB_APP);
        fileToTypeMap.put(NAME_SUNEJBJAR, DD_SUN_EJB_JAR);
        fileToTypeMap.put(NAME_SUNAPPLICATION, DD_SUN_APPLICATION);
        fileToTypeMap.put(NAME_SUNAPPCLIENT, DD_SUN_APP_CLIENT);
        fileToTypeMap.put(NAME_SUNCMPMAPPING, DD_SUN_CMP_MAPPINGS);
        fileToTypeMap.put(NAME_SUNRESOURCE, DD_SUN_RESOURCE);
    }
    
    public static DDType getDDType(String fileName) {
        return fileToTypeMap.get(fileName);
    }
    
    // Internal data
    private final String descriptorName;
    private final ModuleType moduleType;
    private final Class viewFactoryClass;
    
    private DDType(final String ddName, final ModuleType type, final Class vfc) {
        descriptorName = ddName;
        moduleType = type;
        viewFactoryClass = vfc;
    }
    
    public String getDescriptorFileName() {
        return this.descriptorName;
    }
    
    public ModuleType getEditorModuleType() {
        return moduleType;
    }
    
    public DDViewFactory createViewFactory() {
        DDViewFactory factory = null;
        try {
            factory = (DDViewFactory) viewFactoryClass.newInstance();
        } catch(InstantiationException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        } catch(IllegalAccessException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return factory;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(obj == this) {
            return true;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }

        final DDType other = (DDType) obj;
        if(!viewFactoryClass.equals(other.viewFactoryClass)) {
            return false;
        }
        if(!moduleType.equals(other.moduleType)) {
            return false;
        }
        if(!descriptorName.equals(other.descriptorName)) {
            return false;
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (descriptorName != null ? descriptorName.hashCode() : 0);
        hash = 37 * hash + (viewFactoryClass != null ? viewFactoryClass.hashCode() : 0);
        return hash;
    }

}
