/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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

    public Iterator getUsedObjectsIterator() {
        return new UsedObjectsIterator();
    }

    // --------

    class UsedObjectsIterator implements Iterator {
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
