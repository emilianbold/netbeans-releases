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
