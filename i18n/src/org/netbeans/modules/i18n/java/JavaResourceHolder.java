/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n.java;


import java.io.IOException;

import org.netbeans.modules.i18n.ResourceHolder;

import org.netbeans.modules.properties.BundleStructure;
import org.netbeans.modules.properties.Element;
import org.netbeans.modules.properties.PropertiesDataObject;
import org.netbeans.modules.properties.PropertiesStructure;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 * Resource holder java sources. Now supports .properties bundle files only.
 *
 * @author  Peter Zavadsky
 */
public class JavaResourceHolder extends ResourceHolder {

    /** Constructor. */
    public JavaResourceHolder() {
        super(new Class[] {PropertiesDataObject.class});
    }

    /** Implements superclass abstract method.
    /* Gets all keys which are stored in underlying resource object. */
    public String[] getAllKeys() {
        if(resource == null)
            return new String[0];

        return ((PropertiesDataObject)resource).getBundleStructure().getKeys();
    }

    /** Implements superclass abstract method. Gets value for specified key. 
     * @return value for key or null if such key os not stored in resource */
    public String getValueForKey(String key) {
        if(resource == null)
            return null;

        Element.ItemElem item = getItem(key);
        return item == null ? null : item.getValue();
    }

    /** Implemenst superclass abstract method. Gets comment for specified key. 
     * @return value for key or null if such key os not stored in resource */
    public String getCommentForKey(String key) {
        if(resource == null)
            return null;

        Element.ItemElem item = getItem(key);
        return item == null ? null : item.getComment();
    }

    /** Helper method. */
    private Element.ItemElem getItem(String key) {
        BundleStructure bundleStructure = ((PropertiesDataObject)resource).getBundleStructure();
        if (bundleStructure == null)
            return null;

        // Get item from the first file entry which contains the key.
        // Is looks in default (=primary) entry first.
        for(int i=0; i<bundleStructure.getEntryCount(); i++) {
            Element.ItemElem item = bundleStructure.getItem(i, key);
            if(item != null) return item;
        }

        return null;            
    }
    
    /** Implements superclass abstract method. Adds new property (key-valkue pair) to resource object. 
     * @param key key value, if it is <code>null</code> nothing is done
     * @param value 'value' value, can be <code>null</code>
     * @param comment comment, can be <code>null</code>
     * @param forceNewValue if there already exists a key forces to reset its value
     */
    public void addProperty(Object key, Object value, String comment, boolean forceNewValue) {
        if(resource == null || key == null) return;

        String keyValue     = key.toString();
        String valueValue   = value == null ? "" : value.toString();
        String commentValue = comment;
        
        // write to bundle primary file
        BundleStructure bundleStructure = ((PropertiesDataObject)resource).getBundleStructure();
        PropertiesStructure propStructure = bundleStructure.getNthEntry(0).getHandler().getStructure();
        Element.ItemElem item = propStructure.getItem(keyValue);

        if(item == null) {
            // Item doesn't exist in this entry -> create it.
            propStructure.addItem(keyValue, valueValue, commentValue);
        } else if(!item.getValue().equals(valueValue) && forceNewValue) {
            item.setValue(valueValue);
            item.setComment(commentValue);
        }
    }


    /** Implements superclass abstract method. Creates template of type clazz 
     * which have to be of <code>PropertiesDataObject</code> type in our case. */
    protected DataObject createTemplate(Class clazz) throws IOException {
        FileSystem defaultFS = Repository.getDefault().getDefaultFileSystem();

        FileObject fileObject = defaultFS.findResource("Templates/Other/properties.properties"); // NOI18N

        if(fileObject == null)
            throw new IOException(Util.getString("EXC_TemplateNotFound"));

        try {
            return DataObject.find(fileObject);
        } catch(DataObjectNotFoundException e) {
            throw new IOException(Util.getString("EXC_TemplateNotFound"));
        }
    }
}
