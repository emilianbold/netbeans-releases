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

import org.netbeans.modules.xml.xpath.LocationStep;
import org.netbeans.modules.xml.xpath.StepNodeTest;
import org.netbeans.modules.xml.xpath.StepNodeNameTest;
import org.netbeans.modules.xml.xpath.StepNodeTypeTest;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.visitor.XPathVisitor;

/**
 * Represents a location path step.
 * 
 * @author Enrico Lelina
 * @version 
 */
public class LocationStepImpl extends XPathExpressionImpl implements LocationStep {
    
    /** The axis. */
    private int mAxis;
    
    /** The node test. */
    private StepNodeTest mNodeTest;

    /** predicates */
    private XPathPredicateExpression[] mPredicates = null;
    
    /** Constructor. */
    public LocationStepImpl() {
        this(0, null, null);
    }


    /**
     * Constructor.
     * @param axis the axis
     * @param nodeTest the node test
     */
    public LocationStepImpl(int axis, StepNodeTest nodeTest, XPathPredicateExpression[] predicates) {
        setAxis(axis);
        setNodeTest(nodeTest);
        setPredicates(predicates);
    }
    
    
    /**
     * Gets the axis.
     * @return the axis
     */
    public int getAxis() {
        return mAxis;
    }
    
    
    /**
     * Sets the axis.
     * @param axis the axis
     */
    public void setAxis(int axis) {
        mAxis = axis;
    }
                                          
    /**
     * Gets the node test.
     * @return the node test
     */
    public StepNodeTest getNodeTest() {
        return mNodeTest;
    }
    
    
    /**
     * Sets the node test.
     * @param nodeTest the node test
     */
    public void setNodeTest(StepNodeTest nodeTest) {
        mNodeTest = nodeTest;
    }
    
    /**
     * Gets the Predicates
     * @return the predicates
     */
    public XPathPredicateExpression[] getPredicates() {
        return mPredicates;
    }
    
    
    /**
     * Sets the Predicates
     * @param predicates list of predicates
     */
    public void setPredicates(XPathPredicateExpression[] predicates) {
        mPredicates = predicates;
    }
    
    /**
     * Gets the string representation.
     * @return the string representation
     */
    public String getString() {
        StringBuilder sb = new StringBuilder();
        //
        StepNodeTest nodeTest = getNodeTest();
        if (nodeTest instanceof StepNodeNameTest) {
            StepNodeNameTest snnt = (StepNodeNameTest)nodeTest;
            if (snnt.isWildcard()) {
                switch (getAxis()) {
                case LocationStep.AXIS_ATTRIBUTE:
                    sb.append("@*"); // NOI18N
                    break;
                case LocationStep.AXIS_CHILD:
                    sb.append("*"); // NOI18N
                    break;
                default: 
                    // other axis are not supported with the wildcard
                }
            } else {
                // Ignore axis for a while
                // 
                sb.append(((StepNodeNameTest) nodeTest).getNodeName());
            }
        } else if (nodeTest instanceof StepNodeTypeTest) {
            StepNodeTypeTest sntt = (StepNodeTypeTest)nodeTest;
            switch (sntt.getNodeType()) {
            case NODETYPE_NODE:
                switch (getAxis()) {
                case LocationStep.AXIS_SELF:   // it means that the location step is abbreviated step "."
                    sb.append(".");
                    break;
                case LocationStep.AXIS_PARENT: // it means that the location step is abbreviated step ".."
                    sb.append("..");
                    break;
                case LocationStep.AXIS_CHILD: // it means that the location step is "node()"
                    sb.append(sntt.getNodeTypeString());
                    break;
                default:
                    // other axis are not supported here
                }
                break;
            case NODETYPE_COMMENT:
            case NODETYPE_PI:
            case NODETYPE_TEXT:
                sb.append(sntt.getNodeTypeString());
                break;
            }
        }
        //
        return sb.toString();
    }


    public void accept(XPathVisitor visitor) {
        visitor.visit(this);
    }
    
    
}
