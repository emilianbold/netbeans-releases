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

import java.util.*;

/**
 * Default implementation class of CodeExpression interface. No other class
 * should implement CodeExpression or extend this class. All extensibility
 * is done through the CodeExpressionOrigin implementations. CodeExpression
 * is kept as an interface for compatibility and usability reasons.
 *
 * @author Tomas Pavek
 */

final class DefaultCodeExpression implements CodeExpression {

    protected CodeStructure codeStructure;

    private boolean nameReserved;

    private CodeExpressionOrigin expressionOrigin;

    private CodeObjectUsage expressionUsage;


    public DefaultCodeExpression(CodeStructure codeStructure, CodeExpressionOrigin origin)
    {
        this.codeStructure = codeStructure;
        setOrigin(origin);
    }

    DefaultCodeExpression(CodeStructure codeStructure) {
        this.codeStructure = codeStructure;
    }

    // -------

    public CodeStructure getCodeStructure() {
        return codeStructure;
    }

    public CodeVariable getVariable() {
        return codeStructure.getVariable(this);
    }

    public CodeExpressionOrigin getOrigin() {
        return expressionOrigin;
    }

    public void setOrigin(CodeExpressionOrigin newOrigin) {
        CodeExpressionOrigin oldOrigin = expressionOrigin;
        CodeExpression registerParent = null;
        List registerParams = null;

        if (oldOrigin != null) {
            if (newOrigin != null) { // changing one origin to another
                CodeExpression oldParent = oldOrigin.getParentExpression();
                CodeExpression newParent = newOrigin.getParentExpression();
                if (oldParent != null && oldParent != newParent)
                    CodeStructure.unregisterObjectUsage(this, oldParent);
                if (newParent != null && newParent != oldParent)
                    registerParent = newParent;

                CodeExpression[] oldParams = oldOrigin.getCreationParameters();
                CodeExpression[] newParams = newOrigin.getCreationParameters();

                for (int i=0; i < oldParams.length; i++) {
                    CodeExpression oldPar = oldParams[i];
                    if (i < newParams.length && oldPar == newParams[i])
                        continue;
                    int j = 0;
                    while (j < newParams.length) {
                        if (oldPar == newParams[j])
                            break;
                        j++;
                    }
                    if (j == newParams.length)
                        CodeStructure.unregisterObjectUsage(this, oldPar);
                }

                for (int i=0; i < newParams.length; i++) {
                    CodeExpression newPar = newParams[i];
                    if (i < oldParams.length && newPar == oldParams[i])
                        continue;
                    int j = 0;
                    while (j < oldParams.length) {
                        if (newPar == oldParams[j])
                            break;
                        j++;
                    }
                    if (j == oldParams.length) {
                        if (registerParams == null)
                            registerParams = new ArrayList();
                        registerParams.add(newPar);
                    }
                }
            }
            else CodeStructure.unregisterUsingCodeObject(this);
        }

        expressionOrigin = newOrigin;

        if (newOrigin != null) {
            if (oldOrigin != null) {
                if (registerParent != null)
                    registerParent.addUsingObject(
                        this, UsedCodeObject.DEFINING, CodeExpression.class);

                if (registerParams != null)
                    for (int i=0, n=registerParams.size(); i < n; i++) {
                        CodeExpression param = (CodeExpression)
                                               registerParams.get(i);
                        param.addUsingObject(this,
                                             UsedCodeObject.USING,
                                             CodeExpression.class);
                    }
            }
            else CodeStructure.registerUsingCodeObject(this);
        }
    }

    // --------
    // UsedCodeObject implementation - registering objects that use
    // this expression

    public void addUsingObject(UsingCodeObject usingObject,
                               int useType,
                               Object useCategory)
    {
        getExpressionUsage().addUsingObject(usingObject, useType, useCategory);
    }

    public boolean removeUsingObject(UsingCodeObject usingObject) {
        getExpressionUsage().removeUsingObject(usingObject);
        boolean stillUsed = !getExpressionUsage().isEmpty();
        if (!stillUsed) // the elment is no longer used in the structure
            codeStructure.removeExpressionUsingVariable(this);
        return stillUsed;
    }

    public Iterator getUsingObjectsIterator(int useType, Object useCategory) {
        return getExpressionUsage().getUsingObjectsIterator(useType, useCategory);
    }

    private CodeObjectUsage getExpressionUsage() {
        if (expressionUsage == null)
            expressionUsage = new CodeObjectUsage(this);
        return expressionUsage;
    }

    // ---------
    // UsingCodeObject implementation - handling objects used by this expression

    // notifying about registering this object in used object
    public void usageRegistered(UsedCodeObject usedObject) {
    }

    // notifying about removing the used object from structure
    public boolean usedObjectRemoved(UsedCodeObject usedObject) {
//        if (!(usedObject instanceof CodeExpression))
//            return true;
        // an used expression was removed - this expression will be too ...
        codeStructure.removeExpressionUsingVariable(this);
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
            index = getOrigin().getParentExpression() != null ? -1 : 0;
            parameters = getOrigin().getCreationParameters();
            if (parameters == null)
                parameters = CodeStructure.EMPTY_PARAMS;
        }

        public boolean hasNext() {
            return index < parameters.length;
        }

        public Object next() {
            if (!hasNext())
                throw new java.util.NoSuchElementException();

            Object obj = index > -1 ? parameters[index] :
                                      getOrigin().getParentExpression();
            index++;
            return obj;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
