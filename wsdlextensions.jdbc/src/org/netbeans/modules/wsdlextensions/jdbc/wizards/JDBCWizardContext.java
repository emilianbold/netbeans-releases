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

/*
 * 
 * Copyright 2005 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.wsdlextensions.jdbc.wizards;

import org.openide.WizardDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Provides a context for reading and storing intermediate values, properties, etc., among
 * components in a wizard.
 */
public class JDBCWizardContext extends HashMap {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /** Default reserved key to reference wizard descriptor. */
    public static final String WIZARD_DESCRIPTOR = "wizDesc";

    /** Key name used to reference collaboration name in wizard context. */
    public static final String COLLABORATION_NAME = "collaboration_name";

    public static final String TARGETFOLDER_PATH = "targetfolder_path";

    public static final String SELECTEDTABLES = "selected_tables";
    
    public static final String SELECTEDPROCEDURES = "selected_procedures";
    
    public static final String CONNECTION_INFO="connection_info";

    public static final String DBTYPE = "db_type";
    
    public static final String CONNECTION="connection";
    
    public static final String SQL_FROM_PREPARED_STMT ="sql";
    
    public static final String PREPARED_STMT ="prepared_stmt";
    
    public static final String SQL_FROM_SQLFILE ="sql_text";
    
    public static final String INSERT_SELECTED ="insert";

    public static final String UPDATE_SELECTED ="update";
    
    public static final String FIND_SELECTED ="find";
    
    public static final String DELETE_SELECTED ="delete";
    
    public static final String POLL_SELECTED ="poll";
    
    public static final String UPDATE_WHERE ="update_where";
    
    public static final String FIND_WHERE ="find_where";
    
    public static final String DELETE_WHERE ="delete_where";
    
    public static final String POLL_WHERE ="poll_where";
    /** List of current reserved keys for this context. */
    protected List reservedKeys;

    /** Creates a new instance of JDBCWizardContext */
    public JDBCWizardContext() {
    }

    /**
     * Sets the property associated with the given key. Supplying a null value for key, or
     * attempting to clear a property associated with a reserved key, results in an
     * IllegalArgumentException.
     * 
     * @param key key of property to be cleared
     */
    public void clearProperty(final String key) {
        if (key == null) {
            throw new IllegalArgumentException("Must supply non-null ref for key.");
        }

        if (this.isReservedKey(key)) {
            throw new IllegalArgumentException("Cannot use clear property using reserved key: " + key.trim()
                    + "; use appropriate clear method instead.");
        }

        this.remove(key);
    }

    /**
     * Clears the current wizard descriptor instance, if any.
     */
    public void clearWizardDescriptor() {
        this.remove(JDBCWizardContext.WIZARD_DESCRIPTOR);
    }

    /**
     * Gets the property, if any, associated with the given key.
     * 
     * @param key key of property to get
     * @return associated property, or null if none exists
     */
    public Object getProperty(final String key) {
        return this.get(key);
    }

    /**
     * Gets List of current reserved keys for this context.
     * 
     * @return List of reserved keys
     */
    public List getReservedKeys() {
        this.createReservedKeys();
        return Collections.unmodifiableList(this.reservedKeys);
    }

    /**
     * Indicates the wizard option last selected by the user, provided a wizard descriptor has been
     * set in this context. If no wizard descriptor is set, throws java.lang.IllegalStateException.
     * 
     * @return Object representing selected wizard option.
     * @see org.openide.WizardDescriptor#PREVIOUS_OPTION
     * @see org.openide.WizardDescriptor#NEXT_OPTION
     * @see org.openide.WizardDescriptor#FINISH_OPTION
     * @see org.openide.WizardDescriptor#CANCEL_OPTION
     * @see org.openide.WizardDescriptor#CLOSED_OPTION
     */
    public Object getSelectedOption() {
        final WizardDescriptor desc = this.getWizardDescriptor();
        return desc != null ? desc.getValue() : null;
    }

    /**
     * Gets wizard descriptor, if any, from this context.
     * 
     * @return WizardDescriptor instance, or null if not found.
     */
    public WizardDescriptor getWizardDescriptor() {
        final Object o = this.get(JDBCWizardContext.WIZARD_DESCRIPTOR);
        return o instanceof WizardDescriptor ? (WizardDescriptor) o : null;
    }

    /**
     * Indicates whether the given string is a reserved key;
     * 
     * @param key String to be tested
     * @return true if key is reserved; false otherwise
     */
    public boolean isReservedKey(final String key) {
        return this.getReservedKeys().contains(key);
    }

    /**
     * Sets the property associated with the given key. Null values for either argument results in
     * an IllegalArgumentException.
     * 
     * @param key key of property to be associated
     * @param value property to be associated
     */
    public void setProperty(final String key, final Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Must supply non-null ref for key.");
        }

        if (this.isReservedKey(key)) {
            throw new IllegalArgumentException("Cannot use set property using reserved key: " + key.trim()
                    + "; use appropriate setter instead.");
        }

        this.put(key, value);
    }

    /**
     * Sets wizard descriptor in this context to the given instance.
     * 
     * @param desc WizardDescriptor instance to be set
     */
    public void setWizardDescriptor(final WizardDescriptor desc) {
        if (desc == null) {
            throw new IllegalArgumentException("Must supply non-null ref for desc.");
        }

        this.put(JDBCWizardContext.WIZARD_DESCRIPTOR, desc);
    }

    /**
     * Creates list of reserved keys associated with this context instance.
     */
    protected void createReservedKeys() {
        if (this.reservedKeys == null) {
            this.reservedKeys = new ArrayList();
        } else {
            this.reservedKeys.clear();
        }

        this.reservedKeys.add(JDBCWizardContext.WIZARD_DESCRIPTOR);
    }
}
