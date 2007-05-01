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
package org.netbeans.modules.etl.model.impl;

import java.util.UUID;

import org.netbeans.modules.etl.model.ETLObject;


/**
 * This class implements ETLObject implements some base functionalies
 * 
 * @author Sudhendra Seshachala
 * @version $Revision$
 */
public class ETLObjectImpl implements ETLObject {
    
	private String name;
	private String id;
	
	/**
     * Constructs an instance.
     * 
     * @param name the name
     */
    public ETLObjectImpl(String name) {
        this(null, name);
    }

    /**
     * Constructs an instance.
     * 
     * @param id id
     * @param name name
     */
    public ETLObjectImpl(String id, String name)  {
        if (null != name) {
            setName(name);
        }

        if ((null == id) || (id.trim().length() == 0)) {
            id = ( "{" + UUID.randomUUID().toString() + "}");
        } else {
            setOID(id);
        }

    }

    /**
     * Overrides default implementation to correctly compare ETLObjectImpl instances.
     * 
     * @param obj Object to be compared
     * @return true if objects are functionally identical, as defined in the method; false
     *         otherwise
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ETLObject)) {
            return false;
        }

        ETLObject that = (ETLObject) obj;
        if (null != getName()) {
            return getName().equals(that.getName());
        }
 
        return false;
    }


    /**
     * @see org.netbeans.modules.etl.model.ETLObject#getName
     */
    public String getName()  {
        return this.name;
    }

    /**
     * @see org.netbeans.modules.etl.model.ETLObject#getOID
     */
    public String getObjectId()  {
        return this.id;
    }

   /**
     * @see java.lang.Object#hashCode
     */
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * @see org.netbeans.modules.etl.model.ETLObject#setName
     */
    public void setName(String value)  {
        this.name = value;
    }

    /**
     * @see org.netbeans.modules.etl.model.ETLObject#setOID
     */
    public void setOID(String value)  {
        this.id = value;
    }

    /**
     * @return a string
     */
    public String toString() {
        return this.name;
    }

}

