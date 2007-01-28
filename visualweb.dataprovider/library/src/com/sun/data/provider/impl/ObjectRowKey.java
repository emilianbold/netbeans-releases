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

import com.sun.data.provider.RowKey;

/**
 * <p>ObjectRowKey uses an object as the identifier for a data row in a
 * {@link com.sun.data.provider.TableDataProvider}.</p>
 *
 * @author Joe Nuxoll
 */
public class ObjectRowKey extends RowKey {

    /**
     * Constructs an ObjectRowKey using the specified object
     *
     * @param object The desired object
     */
    public ObjectRowKey(Object object) {
        super(object != null ? String.valueOf(object.hashCode()) : String.valueOf(object));
        this.object = object;
    }

    /**
     * Returns the object of this ObjectRowKey
     *
     * @return This ObjectRowKey's object value
     */
    public Object getObject() {
        return object;
    }

    /**
     * Standard equals implementation.  This method compares the ObjectRowKey
     * object values for equality (== || .equals()).  If the passed Object is
     * not an ObjectRowKey instance, the superclass (RowKey) gets a chance to
     * evaluate the Object for equality.
     *
     * @param o the Object to check equality
     * @return true if equal, false if not
     * @see Object#equals(Object)
     */
    public boolean equals(Object o) {
        if (o instanceof ObjectRowKey) {
            Object orkO = ((ObjectRowKey)o).getObject();
            return orkO == object || (orkO != null && orkO.equals(object));
        }
        return super.equals(o);
    }

    /**
     * <p>Standard implementation of compareTo(Object).  This checks for
     * equality first (using equals(Object)), then compares the stored object
     * in the ObjectRowKeys.  If the contained object does not implement
     * Comparable, the superclass version of compareTo(Object) is invoked.</p>
     *
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        if (object instanceof Comparable && o instanceof ObjectRowKey) {
            Object ork = ((ObjectRowKey)o).getObject();
            return ((Comparable)object).compareTo(ork);
        }
        return super.compareTo(o);
    }

    private Object object;
}
