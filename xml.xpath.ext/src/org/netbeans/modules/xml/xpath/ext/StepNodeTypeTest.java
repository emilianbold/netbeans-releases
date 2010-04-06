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
public class StepNodeTypeTest implements StepNodeTest {

    private static StepNodeTestType[] int2Type = new StepNodeTestType[] {
        StepNodeTestType.NODETYPE_NODE, 
        StepNodeTestType.NODETYPE_TEXT, 
        StepNodeTestType.NODETYPE_COMMENT, 
        StepNodeTestType.NODETYPE_PI
    };
    
    /** The node type. */
    private StepNodeTestType mNodeType;
    
    // The instruciton relates to the processing_instruction() only!
    private String mInstruction; 
    
    /**
     * Constructor.
     * @param nodeType the node type
     */
    public StepNodeTypeTest(int intNodeType, String instruction) {
        super();
        assert intNodeType <= int2Type.length : "The index of node test type " + intNodeType + 
                " is out of possible values"; // NOI18N
        mNodeType = int2Type[intNodeType - 1];
        mInstruction = instruction;
    }

    /**
     * Constructor.
     * @param nodeType the node type
     */
    public StepNodeTypeTest(StepNodeTestType nodeType, String instruction) {
        super();
        mNodeType = nodeType;
        mInstruction = instruction;
    }
    
    /**
     * Gets the node type.
     * @return the node type
     */
    public StepNodeTestType getNodeType() {
        return mNodeType;
    }
    
    public String getInstruction() {
        return mInstruction;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StepNodeTypeTest) {
            StepNodeTypeTest otherStep = (StepNodeTypeTest)obj;
            if (otherStep.mNodeType == mNodeType) {
                if (mNodeType == StepNodeTestType.NODETYPE_PI) {
                    if (otherStep.mInstruction == null) {
                        return mInstruction == null;
                    } else {
                        return otherStep.mInstruction.equals(mInstruction);
                    }
                } else {
                    // Ignore the instruction if another type is used.
                    return true;
                }
            }
        }
        return false;
    }
    
    public String getExpressionString() {
        StepNodeTestType type = getNodeType();
        if (type == StepNodeTestType.NODETYPE_PI && 
                mInstruction != null && mInstruction.length() != 0) {
            return type.getXPathText() + "(" + mInstruction + ")";
        }
        return type.getXPathText() + "()";
    }
    
    @Override
    public String toString() {
        return getExpressionString();
    }

}
