/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n;


import java.io.IOException;
import java.util.Arrays;

import org.openide.loaders.DataObject;


/**
 * Abstract class which implementation's are wrappers for holders of properties resources. 
 * Typically such object is <code>PropertiesDataObject</code> which represents bundle
 * of .properties files. But it can be also some object representing other type of resources
 * e.g. subclass of <code> ListResourceBundle</code> class or in case of i18n-ing JSP pages 
 * object representing tag library etc.
 * 
 * @author  Peter Zavadsky
 */
public abstract class ResourceHolder {
    
    /** Instance of resource bundle object. */
    protected DataObject resource;
    
    /** Classes which instances as resources can be hold by this <code>ResourceHolder</code>. */
    protected final Class[] resourceClasses;

    
    /** Construct resource holder. */
    public ResourceHolder(Class[] resourceClasses) {
        if(resourceClasses == null || resourceClasses.length == 0)
            throw new IllegalArgumentException();
        
        this.resourceClasses = resourceClasses;
    }
    
    
    /** Setter for </code>resource</code>. */
    public void setResource(DataObject resource) {
        Class clazz = resource.getClass();

        // Check if the class of parameter is valid for this ResourceHolder.
        if(!Arrays.asList(resourceClasses).contains(clazz))
            throw new IllegalArgumentException();

        DataObject oldValue = this.resource;
        
        if(!resource.equals(this.resource))
            this.resource = resource;
    }
    
    /** Getter for </code>resource</code>. */
    public DataObject getResource() {
        return resource;
    }

    /** Getter for supported <code>resourceClasses</code>. */
    public Class[] getResourceClasses() {
        return resourceClasses;
    }
    
    /** Gets all keys which are stored in underlying resource object. */
    public abstract String[] getAllKeys();
    
    /** Gets value for specified key. 
     * @return value for key or null if such key os not stored in resource */
    public abstract String getValueForKey(String key);
    
    /** Gets comment for specified key. 
     * @return value for key or null if such key os not stored in resource */
    public abstract String getCommentForKey(String key);
    
    /** Adds new property (key-valkue pair) to resource object. */
    public abstract void addProperty(Object key, Object value, String comment);
    
    /** Gets template for reosurce data object. Used by instatianing. 
     * @param clazz <code>Class</code> of object to instantaniate. Have to be one of supported classes. */
    public final DataObject getTemplate(Class clazz) throws IOException {
        if(!Arrays.asList(resourceClasses).contains(clazz))
            throw new IllegalArgumentException();
        
        return createTemplate(clazz);
    }
    
    /** Creates template of type clazz. */
    protected abstract DataObject createTemplate(Class clazz) throws IOException;

}

