/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
 
package org.netbeans.xtest.xmlserializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import org.netbeans.xtest.xmlserializer.ClassMappingRegistry.ClassElementMapping;


public class GlobalMappingRegistry {
	
	
	// private constructor - only static methods can be used from this class
	private GlobalMappingRegistry() {
	}
	
   
    // global registry collection 
    private static ArrayList globalRegistry = new ArrayList();
	
	

	// register element <-> class relationship
	public static boolean registerClassForElementName(String elementName, Class clazz) throws MappingException {

		if (!XMLSerializable.class.isAssignableFrom(clazz)) {
			throw new MappingException("Cannot register class "+clazz+", because does not implement XMLSerializable interface");
		}

        ClassMappingRegistry.ClassElementMapping aRow = new ClassMappingRegistry.ClassElementMapping(elementName, clazz);
        // check if any of the mapping side is already registered        
        if (aRow.canBeAddedTo(globalRegistry)) {
            globalRegistry.add(aRow);
            return true;
        } else {
            return false;
        }
    }
    


	/*
    public static boolean unregisterRootElement(String elementName) {
       return false; 
    }
     */
            
    public static String getElementNameForClass(Class clazz) {
        Iterator i = globalRegistry.iterator();
        while (i.hasNext()) {
            ClassElementMapping aRow = (ClassElementMapping)i.next();
            if (clazz.equals(aRow.getClazz())) {
                return aRow.getElementName();
            }
        }
        return null;
    }
    


	public static Class getClassForElementName(String elementName) {
        Iterator i =globalRegistry.iterator();
        while (i.hasNext()) {
            ClassMappingRegistry.ClassElementMapping aRow = (ClassMappingRegistry.ClassElementMapping)i.next();
            if (elementName.equals(aRow.getElementName())) {
                return aRow.getClazz();
            }
        }
        return null;
    }
    


	public static void clearRegistry() {
        globalRegistry = new ArrayList();
    }
    


	public static void loadRegistry(String registryResource) throws MappingException {
        // try to load elements/classes registr
        if (registryResource != null) {
            try {
                InputStream inStream = ClassMappingRegistry.class.getResourceAsStream(registryResource);
                Properties registry = new Properties();
                registry.load(inStream);
                // succesfully loaded - continue
                Enumeration elements = registry.propertyNames();
                while (elements.hasMoreElements()) {
                    String element = (String) elements.nextElement();
                    String className = registry.getProperty(element);
                    if (className != null) {
                        try {
                            Class clazz = Class.forName(className);
                            registerClassForElementName(element, clazz);
                        } catch (ClassNotFoundException cnfe) {
                            // class not found - not everything is registered correctly
                            throw new MappingException("Class not found when registering class "+className+" for element name "+element);
                        }
                    }
                }
            } catch (IOException ioe) {
                // file not found or cannot be opened - not everything is registered correctly
                throw new MappingException("IOException caught when loading mapping properties",ioe);
            }
        }
    }
    


	public static void loadDefaultClassesForElementsRegistry() throws MappingException {
        String registryResource = System.getProperty("xtest.xmlserializer.registry.resource");
        if (registryResource == null) {
        	throw new MappingException("Cannot load default registry, system property 'xtest.xmlserializer.registry.resource' is not set");
        }
        loadRegistry(registryResource);        
    }
    
   

}
