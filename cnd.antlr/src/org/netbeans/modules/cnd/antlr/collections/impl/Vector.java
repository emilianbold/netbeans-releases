/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.antlr.collections.impl;

/* ANTLR Translator Generator
 * Project led by Terence Parr at http://www.cs.usfca.edu
 * Software rights: http://www.antlr.org/license.html
 */

import java.util.Enumeration;

public class Vector implements Cloneable {
    protected Object[] data;
    protected int lastElement = -1;

    public Vector() {
        this(10);
    }

    public Vector(int size) {
        data = new Object[size];
    }

    public synchronized void appendElement(Object o) {
        ensureCapacity(lastElement + 2);
        data[++lastElement] = o;
    }

    /**
     * Returns the current capacity of the vector.
     */
    public int capacity() {
        return data.length;
    }

    @Override
    public Object clone() {
        Vector v = null;
        try {
            v = (Vector)super.clone();
        }
        catch (CloneNotSupportedException e) {
            System.err.println("cannot clone Vector.super");
            return null;
        }
        v.data = new Object[size()];
        System.arraycopy(data, 0, v.data, 0, size());
        return v;
    }

    /**
     * Returns the element at the specified index.
     * @param index the index of the desired element
     * @exception ArrayIndexOutOfBoundsException If an invalid
     * index was given.
     */
    public synchronized Object elementAt(int i) {
        if (i >= data.length) {
            throw new ArrayIndexOutOfBoundsException(i + " >= " + data.length);
        }
        if (i < 0) {
            throw new ArrayIndexOutOfBoundsException(i + " < 0 ");
        }
        return data[i];
    }

    public synchronized Enumeration elements() {
        return new VectorEnumerator(this);
    }

    public synchronized void ensureCapacity(int minIndex) {
        if (minIndex + 1 > data.length) {
            Object oldData[] = data;
            int n = data.length * 2;
            if (minIndex + 1 > n) {
                n = minIndex + 1;
            }
            data = new Object[n];
            System.arraycopy(oldData, 0, data, 0, oldData.length);
        }
    }

    public synchronized boolean removeElement(Object o) {
        // find element
        int i;
        for (i = 0; i <= lastElement && data[i] != o; i++) {
        }
        if (i <= lastElement) { // if found it
            data[i] = null;		// kill ref for GC
            int above = lastElement - i;
            if (above > 0) {
                System.arraycopy(data, i + 1, data, i, above);
            }
            lastElement--;
            return true;
        }
        else {
            return false;
        }
    }

    public synchronized void setElementAt(Object obj, int i) {
        if (i >= data.length) {
            throw new ArrayIndexOutOfBoundsException(i + " >= " + data.length);
        }
        data[i] = obj;
        // track last element in the vector so we can append things
        if (i > lastElement) {
            lastElement = i;
        }
    }

    // return number of slots in the vector; e.g., you can set
    // the 30th element and size() will return 31.
    public int size() {
        return lastElement + 1;
    }
}
