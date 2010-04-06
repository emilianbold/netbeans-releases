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
 * Represents a step in a location path.
 * 
 * @author Enrico Lelina
 * @author nk160297
 * @version 
 */
public interface LocationStep extends XPathExpression, XPathSchemaContextHolder {

    String STEP_SEPARATOR = "/";
    
    /**
     * Gets the axis.
     * @return the axis
     */
    XPathAxis getAxis();
    
    /**
     * Sets the axis.
     * @param axis the axis
     */
    void setAxis(XPathAxis axis);
    
    /**
     * Gets the node test.
     * @return the node test
     */
    StepNodeTest getNodeTest();
    
    /**
     * Sets the node test.
     * @param nodeTest the node test
     */
    void setNodeTest(StepNodeTest nodeTest);
    
    /**
     * Gets the string representation.
     * @return the string representation
     */
    String getString();

    /**
     * Does almost the same as the previous method but use the specified
     * namespace context while building locaiton steps' prefixes.
     * 
     * @return the string representation
     */
    String getString(NamespaceContext nc);

    XPathPredicateExpression[] getPredicates();

    void setPredicates(XPathPredicateExpression[] predicates);
    
}
