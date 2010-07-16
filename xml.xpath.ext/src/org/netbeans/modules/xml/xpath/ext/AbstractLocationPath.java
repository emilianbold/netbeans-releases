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

package org.netbeans.modules.xml.xpath.ext;

import javax.xml.namespace.NamespaceContext;

/**
 *
 * @author nk160297
 */
public interface AbstractLocationPath extends XPathSchemaContextHolder {

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
     * Gets the string representation of the expression's part.
     * It truncates the step's tail up to the specified index. 
     * Eventually the step with the index is included and is in the end 
     * of the result string. Use general getExpressionString() method if 
     * you need getting the whole text of the path.
     * 
     * @return the string representation
     */
    String getExpressionString(int lastStepIndex);
    
    /**
     * Does almost the same as the previous method but use the specified
     * namespace context while building locaiton steps' prefixes.
     *
     * @return the string representation
     */
    String getExpressionString(int lastStepIndex, NamespaceContext ns);

}
