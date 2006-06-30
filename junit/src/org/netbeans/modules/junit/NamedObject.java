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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit;

/**
 * Object wrapper which allows to assign a name to an object.
 */
public final class NamedObject {

    /** name of the object */
    public String  name;
    /** object wrapper wrapped by this <code>NamedObject</code> */
    public Object  object;

    /**
     * Creates an instance of <code>NamedObject</code>
     *
     * @param  object  object to be wrapped by this object
     * @param  name    name of this object
     */
    public NamedObject(Object object, String name) {
        if ((object == null) || (name == null)) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }
        this.object = object;
        this.name = name;
    }
    
    /**
     * Returns a string representation of this object.
     *
     * @return  name of the object
     */
    public String toString() {
        return name;
    }
    
    /**
     */
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!o.getClass().equals(NamedObject.class)) {
            return false;
        }
        final NamedObject otherNamed = (NamedObject) o;
        return name.equals(otherNamed.name)
               && object.equals(otherNamed.object);
    }
    
    /**
     */
    public int hashCode() {
        return name.hashCode() + object.hashCode();
    }
    
}
