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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.sun.data.provider.impl;

import com.sun.data.provider.DataProviderException;
import com.sun.data.provider.FieldKey;
import java.util.ResourceBundle;

/**
 * <p>This {@link com.sun.data.provider.DataProvider} wraps access to a single
 * Java Object.  The {@link FieldKey}s correspond to the JavaBean properties and
 * optionally the public member fields of the Java Object.</p>
 *
 * <p>NOTE about Serializable:  This class wraps access to any Java Object. The
 * Object can be swapped out using the <code>setObject(Object)</code> method.
 * For this class to remain Serializable, the contained Object must also be
 * Serializable.</p>
 *
 * @author Joe Nuxoll
 *         Winston Prakash (bug fixes)
 */
public class ObjectDataProvider extends AbstractDataProvider {


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>Resource bundle containing our localized messages.</p>
     */
    private transient ResourceBundle bundle = null;


    /**
     * Storage for the includeFields property.  By default, this is true.
     */
    private boolean includeFields = true;


    /**
     * <p>The object being wrapped by this
     * {@link com.sun.data.provider.DataProvider}.</p>
     *
     * <p>NOTE about Serializable:  This class wraps access to any Java Object.
     * The Object can be swapped out using the <code>setObject(Object)</code>
     * method.  For this class to remain Serializable, the contained Object
     * must also be Serializable.</p>
     */
    private Object object;


    // ----------------------------------------------------------- Constructors


    /**
     * Constructs a new ObjectDataProvider with default settings, and no
     * contained Object.  Use setObject to set the contained Object.
     */
    public ObjectDataProvider() {

        this(null, false);

    }


    /**
     * Constructs a new ObjectDataProvider to wrap the specified Object.
     *
     * @param object The Object to wrap as a DataProvider
     */
    public ObjectDataProvider(Object object) {

        this(object, false);

    }


    /**
     * Creates a new ObjectDataProvider to wrap the specified Object.  The
     * public fields will be included if 'includeFields' is set to true.
     *
     * @param object The Object to wrap as a DataProvider
     * @param includeFields true to include the public fields, false to only
     *        surface the public properties as FieldKeys.
     */
    public ObjectDataProvider(Object object, boolean includeFields) {

        this.includeFields = includeFields;
        setObject(object);

    }


    // ------------------------------------------------------------- Properties


    /**
     * @return The Object being represented by this DataProvider
     */
    public Object getObject() {
        return object;
    }


    /**
     * Sets the Object to be wrapped by this DataProvider.  Calling this method
     * will result in the passed object being introspected to populate the list
     * of public properties and fields to make up the list of FieldKeys.
     *
     * @param object The Object to be wrapped by this DataProvider
     */
    public void setObject(Object object) {
        this.object = object;
        this.support = null;

    }


    /**
     * @return The boolean state of the includeFields property
     */
    public boolean isIncludeFields() {

        return includeFields;

    }


    /**
     * <p>Sets the includeFields property.  This affects the set of {@link
     * FieldKey}s that this {@link com.sun.data.provider.DataProvider} emits.
     * If includeFields is set to true (the default), then public fields will
     * be included in the list of available keys (intermixed with the public
     * properties).  If it is set to false, then only the public properties
     * will be available.</p>
     *
     * @param includeFields <code>true</code> to include the public fields, or
     *        <code>false</code> to exclude them (and only show public
     *        properties)
     */
    public void setIncludeFields(boolean includeFields) {

        this.includeFields = includeFields;
        this.support = null;

    }


    // ---------------------------------------------------- DataProvider Methods


    /** {@inheritDoc} */
    public FieldKey getFieldKey(String fieldId) throws DataProviderException {
        FieldKey fieldKey = null;
        if (getSupport() != null) {
            fieldKey = getSupport().getFieldKey(fieldId);
        }
        if (fieldKey != null){
            return fieldKey;
        } else{    
            throw new IllegalArgumentException(fieldId);
        }

    }


    /** {@inheritDoc} */
    public FieldKey[] getFieldKeys() throws DataProviderException {

        if (getSupport() != null) {
            return getSupport().getFieldKeys();
        }
        return FieldKey.EMPTY_ARRAY;

    }


    /** {@inheritDoc} */
    public Class getType(FieldKey fieldKey) throws DataProviderException {
        if ((getSupport() == null) || (getSupport().getFieldKey(fieldKey.getFieldId()) == null)) {
            throw new IllegalArgumentException(fieldKey.toString());
        }else{
            return getSupport().getType(fieldKey);
        }
    }


    /** {@inheritDoc} */
    public Object getValue(FieldKey fieldKey) throws DataProviderException {
       
        if ((getSupport() == null) || (getSupport().getFieldKey(fieldKey.getFieldId()) == null)) {
            throw new IllegalArgumentException(fieldKey.toString());
        }
        
       if( java.beans.Beans.isDesignTime() && object == null ) {
            // Fill the object with design time fake data
            object = AbstractDataProvider.getFakeData( object.getClass() );
        }
        
        return getSupport().getValue(fieldKey, object);
    }


    /** {@inheritDoc} */
    public void setValue(FieldKey fieldKey, Object value) throws DataProviderException {
      
        if ((getSupport() == null) || (getSupport().getFieldKey(fieldKey.getFieldId()) == null)) {
            throw new IllegalArgumentException(fieldKey.toString());
        }
        if (getSupport().isReadOnly(fieldKey)) {
            throw new IllegalStateException(fieldKey.toString() + " " + getBundle().getString("IS_READ_ONLY"));
        }
        Object previous = getSupport().getValue(fieldKey, object);
        getSupport().setValue(fieldKey, object, value);
        fireValueChanged(fieldKey, previous, value);

    }


    /** {@inheritDoc} */
    public boolean isReadOnly(FieldKey fieldKey) throws DataProviderException {
        if ((getSupport() == null) || (getSupport().getFieldKey(fieldKey.getFieldId()) == null)) {
            throw new IllegalArgumentException(fieldKey.toString());
        }
        return getSupport().isReadOnly(fieldKey);

    }


    // --------------------------------------------------------- Private Methods


    /**
     * <p>Return the resource bundle containing our localized messages.</p>
     */
    private ResourceBundle getBundle() {

        if (bundle == null) {
            bundle = ResourceBundle.getBundle("com/sun/data/provider/impl/Bundle");
        }
        return bundle;

    }


    /**
     * <p>The cached support object for field key manipulation.  Must be
     * transient because its content is not Serializable.</p>
     */
    private transient ObjectFieldKeySupport support = null;


    /**
     * <p>Return the {@link ObjectFieldKeySupport} instance for the
     * object class we are wrapping.</p>
     */
    private ObjectFieldKeySupport getSupport() {

        if ((support == null) && (object != null)) {
            support = new ObjectFieldKeySupport(object.getClass(), includeFields);
        }
        return support;

    }


}
