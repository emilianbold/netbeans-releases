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
