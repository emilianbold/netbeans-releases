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


package org.netbeans.modules.i18n.java;


import java.io.IOException;
import java.text.MessageFormat;


import org.netbeans.modules.i18n.I18nUtil;
import org.netbeans.modules.i18n.ResourceHolder;

import org.netbeans.modules.properties.BundleStructure;
import org.netbeans.modules.properties.Element;
import org.netbeans.modules.properties.PropertiesDataObject;
import org.netbeans.modules.properties.PropertiesStructure;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;


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
            int keyIndex = bundleStructure.getKeyIndexByName(key);
            if(keyIndex == -1)
                return null;

            Element.ItemElem item = bundleStructure.getItem(i, keyIndex);
            if(item != null)
                return item;
        }

        return null;            
    }

    /** Implements superclass abstract method. Adds new property (key-valkue pair) to resource object. 
     * @param key key value
     * @param value "value" value
     * @param comment comment, can be null
     */
    public void addProperty(Object key, Object value, String comment) {
        if(resource == null || key == null)
            return;

        try {
            BundleStructure bundleStructure = ((PropertiesDataObject)resource).getBundleStructure();

            for(int i=0; i<bundleStructure.getEntryCount(); i++) {
                PropertiesStructure propStructure = bundleStructure.getNthEntry(i).getHandler().getStructure();
                Element.ItemElem item = propStructure.getItem(key.toString());

                if(item == null) {
                    // Item doesn't exist in this entry -> create it.
                    propStructure.addItem(key.toString(), (value == null) ? null : value.toString(), comment);
                } else if(!item.getValue().equals(value) && I18nUtil.getOptions().isReplaceResourceValue()) {
                    item.setValue((String)value);
                    item.setComment(comment);
                }
            }

        } catch(NullPointerException npe) {
            if(Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                npe.printStackTrace();
            TopManager.getDefault().notifyException(npe);
        }

    }


    /** Implements superclass abstract method. Creates template of type clazz 
     * which have to be of <code>PropertiesDataObject</code> type in our case. */
    protected DataObject createTemplate(Class clazz) throws IOException {
        FileSystem defaultFS = TopManager.getDefault().getRepository().getDefaultFileSystem();

        FileObject fileObject = defaultFS.findResource("Templates/Other/properties.properties"); // NOI18N

        if(fileObject == null)
            throw new IOException(I18nUtil.getBundle().getString("EXC_TemplateNotFound"));

        try {
            return DataObject.find(fileObject);
        } catch(DataObjectNotFoundException e) {
            throw new IOException(I18nUtil.getBundle().getString("EXC_TemplateNotFound"));
        }
    }
}
