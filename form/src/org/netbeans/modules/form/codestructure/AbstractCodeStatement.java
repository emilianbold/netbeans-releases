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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.codestructure;

import java.util.Iterator;

/**
 * Abstract class providing common implementation of UsingCodeObject
 * interface for further CodeStatement implementations. No other
 * UsingCodeObject implementation for statement should be introduced.
 *
 * @author Tomas Pavek
 */

abstract class AbstractCodeStatement implements CodeStatement {

    protected CodeExpression parentExpression;

    protected AbstractCodeStatement(CodeExpression parentExpression) {
        this.parentExpression = parentExpression;
    }

    public CodeExpression getParentExpression() {
        return parentExpression;
    }

    // --------
    // UsingCodeObject implementation

    // notifying about registering this object in used object
    public void usageRegistered(UsedCodeObject usedObject) {
    }

    // notifying about removing the used object from structure
    public boolean usedObjectRemoved(UsedCodeObject usedObject) {
        return false;
    }

    public UsedCodeObject getDefiningObject() {
        return getParentExpression();
    }

    public Iterator getUsedObjectsIterator() {
        return new UsedObjectsIterator();
    }

    // --------

    private class UsedObjectsIterator implements Iterator {
        int index;
        CodeExpression[] parameters;

        UsedObjectsIterator() {
            index = getParentExpression() != null ? -1 : 0;
            parameters = getStatementParameters();
            if (parameters == null)
                parameters = CodeStructure.EMPTY_PARAMS;
        }

        public boolean hasNext() {
            return index < parameters.length;
        }

        public Object next() {
            if (!hasNext())
                throw new java.util.NoSuchElementException();

            Object obj = index > -1 ? parameters[index] : getParentExpression();
            index++;
            return obj;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
