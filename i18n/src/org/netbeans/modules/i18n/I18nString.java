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


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.JPanel;

import org.openide.loaders.DataObject;


/**
 * This object represent i18n values which will be used by actual
 * i18n-izing of found hard coded string. I.e. resource where will be stored 
 * new key-value pair, actual key-value pair and replace code wich will
 * replace found hard coded string.
 *
 * @author  Peter Zavadsky
 */
public abstract class I18nString extends Object {

    /** Name of resource property. */
    public static final String PROP_RESOURCE = "property_resource"; // NOI18N

    /** Name of key property. */
    public static final String PROP_KEY = "property_key"; // NOI18N

    /** Name of value property. */
    public static final String PROP_VALUE = "property_value"; // NOI18N

    /** Name of comment property. */
    public static final String PROP_COMMENT = "property_comment"; // NOI18N

    /** Source data object. */
    protected DataObject sourceDataObject;
    
    /** <code>ResourceHolder</code>. */
    protected final ResourceHolder resourceHolder;
    
    /** The key value according the hard coded string will be i18n-ized. */
    protected String key;
    
    /** The "value" value which will be stored to resource. */
    protected String value;
    
    /** Comment for key-value pair stored in resource. */
    protected String comment;
    
    /** Replace format. */
    protected String replaceFormat;
    
    /** Helper property change support. */
    protected PropertyChangeSupport support;

    
    /** Creates new I18nString. 
     * @param resourceClasses classes which type of resoutce supports this <code>I18nString</code> object,
     *        e.g. <code>PropertiesDataObject</code> type. */
    public I18nString(DataObject sourceDataObject, ResourceHolder resourceHolder) {
        if(sourceDataObject == null || resourceHolder == null)
            throw new IllegalArgumentException();

        this.sourceDataObject = sourceDataObject;
        this.resourceHolder = resourceHolder;
    }

    
    /** Getter for <code>sourceDataObject</code>. */
    public DataObject getSourceDataObject() {
        return sourceDataObject;
    }
    
    /** Getter for <code>resourceHolder</code>. */
    public ResourceHolder getResourceHolder() {
        return resourceHolder;
    }

    /** Getter for supported resource classes. Delegates call to <code>resourceBundle</code> */
    public Class[] getResourceClasses() {
        return resourceHolder.getResourceClasses();
    }
    
    /** Setter for </code>resource</code>. */
    public void setResource(DataObject resource) {
        if(resource == null)
            throw new IllegalArgumentException();
        
        Class clazz = resource.getClass();

        // Check if the class of parameter is valid for this ResourceHolder.
        if(!Arrays.asList(resourceHolder.getResourceClasses()).contains(clazz))
            throw new IllegalArgumentException();

        DataObject oldValue;

        oldValue = this.resourceHolder.getResource();

        if(resource.equals(this.resourceHolder.getResource())) 
            return;

        this.resourceHolder.setResource(resource);
        
        if(support != null)
            support.firePropertyChange(PROP_RESOURCE, oldValue, this.resourceHolder.getResource());
    }
    
    /** Getter for resource. Delegates call to <code>resourceHolder</code>. */
    public DataObject getResource() {
        return resourceHolder.getResource();
    }

    /** Getter for <code>key</code>. */
    public String getKey() {
        return key;
    }

    /** Setter for <code>key</code>. */
    public void setKey(String key) {
        if(this.key != null && this.key.equals(key))
            return;

        if(this.key == null && key == null)
            return;

        String oldValue = this.key;

        this.key = key;
        
        if(support != null)
            support.firePropertyChange(PROP_KEY, oldValue, this.key);
    }
    
    /** Getter for <code>value</code>. */
    public String getValue() {
        return value;
    }

    /** Setter for <code>value</code>. */
    public void setValue(String value) {
        if(this.value != null && this.value.equals(value))
            return;

        if(this.value == null && value == null)
            return;

        String oldValue = this.value;

        this.value = value;
        
        if(support != null)
            support.firePropertyChange(PROP_VALUE, oldValue, this.value);
    }

    /** Getter for <code>comment</code>. */
    public String getComment() {
        return comment;
    }

    /** Setter for <code>comment</code>. */
    public void setComment(String comment) {
        if(this.comment != null && this.comment.equals(comment))
            return;

        if(this.comment == null && comment == null)
            return;

        String oldValue = this.comment;

        this.comment = comment;
        
        if(support != null)
            support.firePropertyChange(PROP_COMMENT, oldValue, this.comment);
    }

    /** Getter for replace format property. */
    public String getReplaceFormat() {
        return replaceFormat;
    }
    
    /** Setter for replace format property. */
    public void setReplaceFormat(String replaceFormat) {
        this.replaceFormat = replaceFormat;
    }

    /** Gets all keys which are stored in underlying resource object. Delegates call to <code>resourceHolder</code>. */
    public String[] getAllKeys() {
        return resourceHolder.getAllKeys();
    }
    
    /** Gets value for specified key. Delegates call to <code>resourceHolder</code>.
     * @return value for key or null if such key os not stored in resource */
    public String getValueForKey(String key) {
        return resourceHolder.getValueForKey(key);
    }
    
    /** Gets comment for specified key. Delagets call to <code>resourceHolder</code>.
     * @return value for key or null if such key is not stored in resource */
    public String getCommentForKey(String key) {
        return resourceHolder.getCommentForKey(key);
    }
    
    /** Adds new property (key-valkue pair) to resource object. Delegates call to <code>resourceHolder</code>. */
    public void addProperty(Object key, Object value, String comment) {
        resourceHolder.addProperty(key, value, comment);
    }
    
    /** Gets template for reosurce data object. Used by instatianing. 
     * @param clazz <code>Class</code> of object to instantaniate. Have to be one of supported classes. */
    public final DataObject getTemplate(Class clazz) throws IOException {
        if(!Arrays.asList(resourceHolder.getResourceClasses()).contains(clazz))
            throw new IllegalArgumentException();
        
        return createTemplate(clazz);
    }
    
    /** Creates templeate of type clazz. Delegates call to <code>resourceHolder</code>. */
    protected DataObject createTemplate(Class clazz) throws IOException {
        return resourceHolder.createTemplate(clazz);
    }

    /** Adds property change listener. */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if(support == null)
            support = new PropertyChangeSupport(this);
        
        support.addPropertyChangeListener(listener);
    }
    
    /** Removes property change listener. */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if(support != null)
            support.removePropertyChangeListener(listener);
    }

    /** Gets if supports customizer for additional source specific values. Override in subclasses if nedded.
     * @return false 
     * @see #getAdditionalCustommizer */
    public boolean hasAdditionalCustomizer() {
        return false;
    }
    
    /** Gets additional customizer. Override in subclasses if needed.
     * @return null 
     * @see #hasAdditionalCustomizer */
    public JPanel getAdditionalCustomizer() {
        return null;
    }
}
