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

package org.netbeans.modules.xml.xpath.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathOperationOrFuntion;


/**
 * 
 * @author radval
 *
 */
public abstract class XPathOperatorOrFunctionImpl extends XPathExpressionImpl implements XPathOperationOrFuntion {

	/** List of child expressions. */
    protected List mChildren;
    
    
    /** Constructor. */
    protected XPathOperatorOrFunctionImpl() {
        mChildren = new ArrayList();
    }
    
    /**
     * Gets the list of child expressions.
     * @return a collection of child expressions
     */
    public Collection getChildren() {
        return Collections.unmodifiableCollection(mChildren);
    }
    
    
    /**
     * Gets the number of children expressions.
     * @return the count of children expressions
     */
    public int getChildCount() {
        return mChildren.size();
    }
    
    
    /**
     * Gets the child expression at the specified location.
     * @param index the index of the child to get
     * @return the child
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    public XPathExpression getChild(int index)
        throws IndexOutOfBoundsException {
        return (XPathExpression) mChildren.get(index);
    }
    
    
    /**
     * Adds a child expression.
     * @param child to be added
     */
    public void addChild(XPathExpression child) {
        mChildren.add(child);
    }
    
    
    /**
     * Removes a child expression.
     * @param child to be removed
     * @return <code>true</code> if the child was found and removed
     */
    public boolean removeChild(XPathExpression child) {
        return mChildren.remove(child);
    }
    
    
    /**
     * Removes all the child expressions.
     */
    public void clearChildren() {
        mChildren.clear();
    }

}
