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

/**
 * Represents a node test on type.
 * 
 * @author Enrico Lelina
 * @author nk160297
 * @version 
 */
public class StepNodeTypeTest extends StepNodeTest {

    private static StepNodeTestType[] int2Type = new StepNodeTestType[] {
        StepNodeTestType.NODETYPE_NODE, 
        StepNodeTestType.NODETYPE_TEXT, 
        StepNodeTestType.NODETYPE_COMMENT, 
        StepNodeTestType.NODETYPE_PI
    };
    
    /** The node type. */
    private StepNodeTestType mNodeType;
    
    /**
     * Constructor.
     * @param nodeType the node type
     */
    public StepNodeTypeTest(int intNodeType) {
        super();
        assert intNodeType < int2Type.length : "The index of node test type " + intNodeType + 
                " is out of possible values"; // NOI18N
        mNodeType = int2Type[intNodeType - 1];
    }

    /**
     * Constructor.
     * @param nodeType the node type
     */
    public StepNodeTypeTest(StepNodeTestType nodeType) {
        super();
        mNodeType = nodeType;
    }
    
    /**
     * Gets the node type.
     * @return the node type
     */
    public StepNodeTestType getNodeType() {
        return mNodeType;
    }
    
    public String toString() {
        return getNodeType().getXPathText();
    }
}
