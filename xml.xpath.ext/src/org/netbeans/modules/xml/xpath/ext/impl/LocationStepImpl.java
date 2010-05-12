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

package org.netbeans.modules.xml.xpath.ext.impl;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xpath.ext.LocationStep;
import org.netbeans.modules.xml.xpath.ext.StepNodeTest;
import org.netbeans.modules.xml.xpath.ext.StepNodeNameTest;
import org.netbeans.modules.xml.xpath.ext.StepNodeTypeTest;
import org.netbeans.modules.xml.xpath.ext.XPathUtils;
import org.netbeans.modules.xml.xpath.ext.XPathAxis;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.XPathPredicateExpression;
import org.netbeans.modules.xml.xpath.ext.schema.resolver.XPathSchemaContext;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathVisitor;

/**
 * Represents a location path step.
 * 
 * @author Enrico Lelina
 * @author nk160297
 * @version 
 */
public class LocationStepImpl extends XPathExpressionImpl implements LocationStep {
    
    private static XPathAxis[] int2Axis = new XPathAxis[] {
        XPathAxis.SELF, 
        XPathAxis.CHILD, 
        XPathAxis.PARENT, 
        XPathAxis.ANCESTOR, 
        XPathAxis.ATTRIBUTE, 
        XPathAxis.NAMESPACE, 
        XPathAxis.PRECEDING, 
        XPathAxis.FOLLOWING, 
        XPathAxis.DESCENDANT, 
        XPathAxis.ANCESTOR_OR_SELF, 
        XPathAxis.DESCENDANT_OR_SELF, 
        XPathAxis.FOLLOWING_SIBLING, 
        XPathAxis.PRECEDING_SIBLING
    };
    
    /** The axis. */
    private XPathAxis mAxis;
    
    /** The node test. */
    private StepNodeTest mNodeTest;

    /** predicates */
    private XPathPredicateExpression[] mPredicates = null;
    
    private XPathSchemaContext mSchemaContext;
    
    /** Constructor. */
    public LocationStepImpl(XPathModel model) {
        this(model, 0, null, null);
    }

    /**
     * Constructor.
     * @param axis the axis
     * @param nodeTest the node test
     */
    public LocationStepImpl(XPathModel model, int axis, 
            StepNodeTest nodeTest, XPathPredicateExpression[] predicates) {
        super(model);
        assert axis <= int2Axis.length : "The index of axis " + axis + 
                " is out of possible values"; // NOI18N
        //
        setAxis(int2Axis[axis - 1]);
        setNodeTest(nodeTest);
        setPredicates(predicates);
    }
    
    /**
     * Constructor.
     * @param axis the axis
     * @param nodeTest the node test
     */
    public LocationStepImpl(XPathModel model, XPathAxis axis, 
            StepNodeTest nodeTest, XPathPredicateExpression[] predicates) {
        super(model);
        //
        if (axis == null) {
            axis = XPathAxis.CHILD;
        }
        //
        setAxis(axis);
        setNodeTest(nodeTest);
        setPredicates(predicates);
    }
    
    /**
     * Gets the axis.
     * @return the axis
     */
    public XPathAxis getAxis() {
        return mAxis;
    }
    
    /**
     * Sets the axis.
     * @param axis the axis
     */
    public void setAxis(XPathAxis axis) {
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
        mSchemaContext = null; // discard schema context
    }
    
    /**
     * Gets the string representation.
     * @return the string representation
     */
    public String getString() {
        return getString(null);
    }

    public String getString(NamespaceContext nc) {
        StringBuilder sb = new StringBuilder();
        //
        StepNodeTest nodeTest = getNodeTest();
        if (nodeTest instanceof StepNodeNameTest) {
            sb.append(getAxis().getShortForm());
            //
            StepNodeNameTest snnt = (StepNodeNameTest)nodeTest;
            if (snnt.isWildcard()) {
                sb.append("*"); // NOI18N
            } else {
                if (nc == null) {
                    QName nodeName = ((StepNodeNameTest) nodeTest).getNodeName();
                    sb.append(XPathUtils.qNameObjectToString(nodeName));
                } else {
                    QName nodeName = ((StepNodeNameTest) nodeTest).getNodeName();
                    String nsUri = nodeName.getNamespaceURI();
                    if (nsUri == null || nsUri.length() == 0) {
                        String prefix = nodeName.getPrefix();
                        NamespaceContext modelNC = mModel.getNamespaceContext();
                        if (modelNC != null) {
                            nsUri = modelNC.getNamespaceURI(prefix);
                        }
                    }
                    //
                    if (nsUri != null && nsUri.length() != 0) {
                        String newPrefix = nc.getPrefix(nsUri);
                        QName modifiedNodeName =
                                new QName(null, nodeName.getLocalPart(), newPrefix);
                        sb.append(XPathUtils.qNameObjectToString(modifiedNodeName));
                    } else {
                        // Use not modified prefix
                        sb.append(XPathUtils.qNameObjectToString(nodeName));
                    }
                }
            }
        } else if (nodeTest instanceof StepNodeTypeTest) {
            StepNodeTypeTest sntt = (StepNodeTypeTest)nodeTest;
            switch (sntt.getNodeType()) {
            case NODETYPE_NODE:
                switch (getAxis()) {
                case CHILD: // it means that the location step is "node()"
                    sb.append(sntt.getExpressionString());
                    break;
                case SELF:   // it means that the location step is abbreviated step "."
                    sb.append("."); // NOI18N
                    break;
                case PARENT: // it means that the location step is abbreviated step ".."
                    sb.append(".."); // NOI18N
                    break;
                case DESCENDANT_OR_SELF: // it means that the location step is abbreviated step "//"
                    // It doesn't necessary to append anything here because
                    // the double slash "//" abbreviated step is a kind of "empty" step.
                    // It is a step without a content between two slashes.
                    break;
                default:
                    // other axis are not supported here
                }
                break;
            case NODETYPE_COMMENT:
            case NODETYPE_PI:
            case NODETYPE_TEXT:
                sb.append(sntt.getExpressionString());
                break;
            }
        }
        //
        return sb.toString();
    }

    @Override
    public void accept(XPathVisitor visitor) {
        visitor.visit(this);
    }

    public XPathSchemaContext getSchemaContext() {
        if (mSchemaContext == null) {
            boolean success = false;
            if (mModel.getRootExpression() != null) {
                success = mModel.resolveExtReferences(false);
            } else {
                success = mModel.resolveExpressionExtReferences(this);
            }
            //
            // TODO: Nikita. Uncomment for Debugging
            //
//            if (success && mSchemaContext == null) {
//                assert false : "Wrong behavior!"; // NOI18N
//                //
//                // Try again for debugging purposes
//                if (myModel.getRootExpression() != null) {
//                    success = myModel.resolveExtReferences(true);
//                } else {
//                    success = myModel.resolveExpressionExtReferences(this);
//                }
//            }
        }
        return mSchemaContext;
    }

    public void setSchemaContext(XPathSchemaContext newContext) {
        mSchemaContext = newContext;
    }
    
    @Override
    public String toString() {
        return getString();
    }
    
    @Override
    public boolean equals(Object obj) { 
        if (obj instanceof LocationStep) {
            //
            // Compare Node Test
            LocationStep step2 = (LocationStep)obj;
            StepNodeTest snt2 = step2.getNodeTest();
            if (!snt2.equals(mNodeTest)) {
                return false;
            }
            //
            // Compare Axis
            if (step2.getAxis() != mAxis) {
                return false;
            }
            //
            // Compare predicates
            XPathPredicateExpression[] predicates2 = step2.getPredicates();
            if (!XPathUtils.samePredicatesArr(mPredicates, predicates2)) {
                return false;
            }
            //
            return true;
        }
        return false;
    }

}
