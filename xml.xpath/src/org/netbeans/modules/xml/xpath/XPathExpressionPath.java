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

/*
 * Created on Sep 12, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.xpath;



/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface XPathExpressionPath extends XPathExpression {
	
	/**
     * Gets the steps of the location path.
     * @return the steps
     */
    LocationStep[] getSteps();
    
    
    /**
     * Sets the steps of the location path.
     * @param steps the steps
     */
    void setSteps(LocationStep[] steps);


    /**
     * set root expression of this expression path.
     * @param rootExpression root expression of this expression path.
     */
    void setRootExpression(XPathExpression rootExpression);
    
    /**
     * get root expression of this expression path.
     * @return root expression of this expression path
     * @return
     */
    XPathExpression getRootExpression();
    
    
    /**
     * Describe <code>setSimplePath</code> method here.
     *
     * @param isSimplePath a <code>boolean</code> value
     */
    void setSimplePath(boolean isSimplePath);

    /**
     * Describe <code>isSimplePath</code> method here.
     *
     * @return a <code>boolean</code> value
     */
    boolean isSimplePath();
    
    /**
     * get expression exclusing root expression
     * @return
     */
    String getExpressionStringExcludingRootExpression();
}
