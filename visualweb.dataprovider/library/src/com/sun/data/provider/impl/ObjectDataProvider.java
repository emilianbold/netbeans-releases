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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
