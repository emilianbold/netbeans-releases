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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.apt.impl.support;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * iterator which encapsulates two lists ans start index of combined collection
 * @author Vladimir Voskresensky
 */
class PathsCollectionIterator implements Iterator<String> {
    private final List<String> col1;
    private final List<String> col2;
    private int startIndex;
    
    public PathsCollectionIterator(List<String> col1, List<String> col2) {
        this(col1, col2, 0);
    }
    
    public PathsCollectionIterator(List<String> col1, List<String> col2, int startIndex) {
        this.col1 = col1;
        this.col2 = col2;
        this.startIndex = startIndex;
    }

    public boolean hasNext() {
        return startIndex < col1.size() + col2.size();
    }

    public String next() {
        if (hasNext()) {
            int index = startIndex++;
            if (index < col1.size()) {
                return col1.get(index);
            } else {
                return col2.get(index - col1.size());
            }
        } else {
            throw new NoSuchElementException();
        }
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported."); // NOI18N
    }
}
