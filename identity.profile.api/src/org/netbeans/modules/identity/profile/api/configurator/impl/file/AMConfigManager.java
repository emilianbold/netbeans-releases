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

package org.netbeans.modules.identity.profile.api.configurator.impl.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.netbeans.modules.identity.profile.api.configurator.impl.file.jaxb.AMConfigType;
import org.netbeans.modules.identity.profile.api.configurator.impl.file.jaxb.ObjectFactory;

/**
 * This class manages a cache of JAXB representations of the amconfig.xml
 * files to avoid unnecessary reloads.
 *
 * Created on July 24, 2006, 2:42 PM
 *
 * @author ptliu
 */
class AMConfigManager {
    private static final String AMCONFIG_CACHE_PATH = "amserver";       //NOI18N
    
    private static final String AMCONFIG_CACHE_FILE = "amconfig.xml";   //NOI18N
    
    private static final String JAXB_CONTEXT =
            "org.netbeans.modules.identity.profile.api.configurator.impl.file.jaxb";    //NOI18N
    
    private static AMConfigManager instance;
    
    private HashMap<String, WeakReference> amConfigMap;
    
    private AMConfigManager() {
        amConfigMap = new HashMap<String, WeakReference>();
    }
    
    public static AMConfigManager getDefault() {
        if (instance == null) {
            instance = new AMConfigManager();
        }
   
        return instance;
    }
    
    public JAXBElement<AMConfigType> getAMConfig(String path) {
        String normalizedPath = path.replace('\\', '/');    
        WeakReference reference = amConfigMap.get(normalizedPath);
        
        if (reference == null || reference.get() == null) {
            JAXBElement<AMConfigType> amConfig = getAMConfigInternal(path);
            reference = new WeakReference(amConfig);
            amConfigMap.put(normalizedPath, reference);
        }
      
        return (JAXBElement<AMConfigType>) reference.get();
    }
    
    public void saveAMConfig(JAXBElement<AMConfigType> amConfig, String path) {
        try {
            createMarshaller().marshal(amConfig, new FileOutputStream(
                    getConfigFile(path, true)));
        } catch (JAXBException excp) {
            excp.printStackTrace();
        } catch (FileNotFoundException fexcp) {
            fexcp.printStackTrace();
        }
    }
    
    private JAXBElement<AMConfigType> getAMConfigInternal(String path) {
        try {
            
            //Find the amconfig.xml file in the path
            // Put it directly under conf directory instead of conf/amserver.
            File configFile = getConfigFile(path, false);
            //System.out.println("config file value is " + configFile);
            
            JAXBElement<AMConfigType> amConfig = null;
            
            if (!configFile.exists()) {
                ObjectFactory objFactory = new ObjectFactory();
                AMConfigType amconfigType = objFactory.createAMConfigType();
                amConfig = (JAXBElement<AMConfigType>)objFactory.createAMConfig(amconfigType);
            } else {
                Unmarshaller unmarshaller = createUnmarshaller();
                amConfig = (JAXBElement<AMConfigType>)unmarshaller.unmarshal(configFile);
            }
            
            return amConfig;
        } catch (JAXBException excp) {
            excp.printStackTrace();
        }
        
        return null;
    }
    
    private Unmarshaller createUnmarshaller() {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(JAXB_CONTEXT);
            
            return jaxbContext.createUnmarshaller();
        } catch (JAXBException ex) {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    private Marshaller createMarshaller() {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(JAXB_CONTEXT);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            
            return marshaller;
        } catch (JAXBException ex) {
            ex.printStackTrace();
        }
        
        return null;
    }
    
    private static File getConfigFile(String path, boolean create) {
        File amconfigPath = new File(path.trim() + File.separator +
                AMCONFIG_CACHE_PATH);
        if (!amconfigPath.exists() && create)
            amconfigPath.mkdir();
        return new File(amconfigPath, File.separator + AMCONFIG_CACHE_FILE);
    }
    
    /**
     * This is a special classloader that is designed to bypass NB's public
     * package and friend mechanism.  The issue here is that when creating
     * a JAXBContext, we need to set the thread's context classloader to
     * a classloader that is capable of loading the com.sun.xml.bind.v2.ContextFactory
     * class and classes in the org.netbeans.modules.identity.profile.api.configurator.impl.file.jaxb
     * package.  The former requires the classloader for the jaxws20 module.
     * The latter requires the classloader for the profileapi module.
     * 
     * Ideally, we would simply use the classloader for the profileapi module
     * since it has a dependency to jaxws20 module.  However, this won't work
     * due to the restriction of the public package and friend mechanism because
     * the com.sun.xml.bind.v2.ContextFactory is not public and hence invisible to the 
     * profileapi classloader.  Using the jaxws20 classloader won't work either
     * because the org.netbeans.modules.identity.profile.api.configurator.impl.file.jaxb
     * package is not visible to it.
     *
     * This classloader works by attempting to load classes from both the jaxws20 and
     * profileapi classloaders.
     *
     */
    private static class BypassClassLoader extends ClassLoader {
        
        private ClassLoader[] parents;
        
        /**
         * Creates a new instance of BypassClassLoader
         */
        public BypassClassLoader() {
            super();
            
            this.parents = new ClassLoader[] {
                this.getClass().getClassLoader(),
                JAXBContext.class.getClassLoader()};
        }
        
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            ClassNotFoundException cnfe = null;
            Class clazz = null;
            
            for (ClassLoader cl : parents) {
                try {
                    clazz = cl.loadClass(name);
                    
                    if (clazz != null) break;
                } catch (ClassNotFoundException ex) {
                    cnfe = ex;
                }
            }
            
            if (clazz == null && cnfe != null) throw cnfe;
            
            if (resolve) resolveClass(clazz);
            
            return clazz;
        }
    }
}
