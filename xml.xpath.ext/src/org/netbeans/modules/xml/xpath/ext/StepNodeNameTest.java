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

import javax.xml.namespace.QName;

/**
 * Represents a node test on name.
 * 
 * @author Enrico Lelina
 * @version 
 */
public class StepNodeNameTest extends StepNodeTest {
    
    /** The node name. */
    private QName mNodeName;
    
    /**
     * Constructor.
     * @param nodeName the node name
     */
    public StepNodeNameTest(QName nodeName) {
        super();
        mNodeName = nodeName;
    }
    
    /**
     * Gets the node name.
     * @return the node name
     */
    public QName getNodeName() {
        return mNodeName;
    }
    
    public boolean isWildcard() {
        return mNodeName.getLocalPart().equals("*"); // NOI18N
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StepNodeNameTest) {
            QName nodeName2 = ((StepNodeNameTest)obj).getNodeName();
            return XPathUtils.equalsIgnorNsUri(nodeName2, mNodeName);
        }
        return false;
    }

    @Override
    public String toString() {
        return XPathUtils.qNameObjectToString(mNodeName);
    }
}
