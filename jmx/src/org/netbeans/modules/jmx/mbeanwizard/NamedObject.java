/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.mbeanwizard;

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
