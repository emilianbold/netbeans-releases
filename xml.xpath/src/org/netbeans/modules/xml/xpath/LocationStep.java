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

package org.netbeans.modules.xml.xpath;


/**
 * Represents a step in a location path.
 * 
 * @author Enrico Lelina
 * @version $Revision$
 */
public interface LocationStep extends XPathExpression {

    /** Axis: self */
    public static final int AXIS_SELF = 1;

    /** Axis: child */
    public static final int AXIS_CHILD = 2;

    /** Axis: parent */
    public static final int AXIS_PARENT = 3;

    /** Axis: ancestor */
    public static final int AXIS_ANCESTOR = 4;

    /** Axis: attribute */
    public static final int AXIS_ATTRIBUTE = 5;

    /** Axis: namespace */
    public static final int AXIS_NAMESPACE = 6;

    /** Axis: preceding */
    public static final int AXIS_PRECEDING = 7;

    /** Axis: following */
    public static final int AXIS_FOLLOWING = 8;

    /** Axis: descendant */
    public static final int AXIS_DESCENDANT = 9;

    /** Axis: ancestor or self */
    public static final int AXIS_ANCESTOR_OR_SELF = 10;

    /** Axis: descendant or self */
    public static final int AXIS_DESCENDANT_OR_SELF = 11;

    /** Axis: following sibling */
    public static final int AXIS_FOLLOWING_SIBLING = 12;

    /** Axis: preceding sibling */
    public static final int AXIS_PRECEDING_SIBLING = 13;
    
    /** Node type test: node */
    public static final int NODETYPE_NODE = 1;
    
    /** Node type test: text */
    public static final int NODETYPE_TEXT = 2;
    
    /** Node type test: comment */
    public static final int NODETYPE_COMMENT = 3;
    
    /** Node type test: processing instruction */
    public static final int NODETYPE_PI = 4;
    
    
    /**
     * Gets the axis.
     * @return the axis
     */
    int getAxis();
    
    
    /**
     * Sets the axis.
     * @param axis the axis
     */
    void setAxis(int axis);
    
    
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

    XPathPredicateExpression[] getPredicates();

    void setPredicates(XPathPredicateExpression[] predicates);
}
