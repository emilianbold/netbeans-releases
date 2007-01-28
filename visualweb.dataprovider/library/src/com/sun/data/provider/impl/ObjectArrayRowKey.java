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
 * <p>ObjectArrayRowKey uses an object array as the identifier for a data row
 * in a {@link com.sun.data.provider.TableDataProvider}.</p>
 *
 * @author Joe Nuxoll
 */
public class ObjectArrayRowKey extends RowKey {

    /**
     * Constructs an ObjectArrayRowKey using the specified array of objects
     *
     * @param objects The desired array of objects
     */
    public ObjectArrayRowKey(Object[] objects) {
        super(objects != null ? String.valueOf(objects.hashCode()) : String.valueOf(objects));
        this.objects = objects;
    }

    /**
     * Returns the Object[] of this ObjectArrayRowKey
     *
     * @return This ObjectArrayRowKey's object array value
     */
    public Object[] getObjects() {
        return objects;
    }

    /**
     * Returns the pattern:
     *
     * object1hash|object2hash|object3hash
     *
     * {@inheritDoc}
     */
    public String getRowId() {
        if (objects != null) {
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < objects.length; i++) {
                buf.append(objects[i] != null
                    ? String.valueOf(objects[i].hashCode())
                    : String.valueOf(objects[i]));
                if (i < objects.length - 1) {
                    buf.append("|");
                }
            }
            return buf.toString();
        }
        return super.getRowId();
    }

    /**
     * Standard equals implementation.  This method compares the ObjectArrayRowKey
     * object values for == equality.  If the passed Object is not an
     * ObjectArrayRowKey instance, the superclass (RowKey) gets a chance to evaluate
     * the Object for equality.
     *
     * @param o the Object to check equality
     * @return true if equal, false if not
     * @see Object#equals(Object)
     */
    public boolean equals(Object o) {
        if (o instanceof ObjectArrayRowKey) {
            Object[] orkOs = ((ObjectArrayRowKey)o).getObjects();
            if (orkOs == null || orkOs.length != objects.length) {
                return false;
            }
            for (int i = 0; i < orkOs.length; i++) {
                Object o1 = orkOs[i];
                Object o2 = objects[i];
                if (o1 != o2 && (o1 != null && !o1.equals(o2))) {
                    return false;
                }
            }
            return true;
        }
        return super.equals(o);
    }

//    /**
//     * <p>Standard implementation of compareTo(Object).  This compares the
//     * stored object arrays in the ObjectArrayRowKeys.  If the contained
//     * objects do not implement Comparable, the superclass version of
//     * compareTo(Object) is invoked.</p>
//     *
//     * {@inheritDoc}
//     */
//    public int compareTo(Object o) {

//        if (o instanceof ObjectArrayRowKey) {
//            Object[] orkOs = ((ObjectArrayRowKey)o).getObjects();
//            if (orkOs == null || orkOs.length != objects.length) {
//                return false;
//            }
//            for (int i = 0; i < orkOs.length; i++) {
//                Object o1 = orkOs[i];
//                Object o2 = objects[i];
//                if (o1 != o2 && (o1 != null && !o1.equals(o2))) {
//                    return false;
//                }
//            }
//            return true;
//        }
//        return super.equals(o);

//        if (object instanceof Comparable && o instanceof ObjectRowKey) {
//            Object ork = ((ObjectRowKey)o).getObject();
//            return ((Comparable)object).compareTo(ork);
//        }
//        return super.compareTo(o);
//    }

    private Object[] objects;
}
