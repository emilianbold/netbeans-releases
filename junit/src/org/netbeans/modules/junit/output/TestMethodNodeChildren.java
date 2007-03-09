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

package org.netbeans.modules.junit.output;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import static org.netbeans.modules.junit.output.RegexpUtils.NESTED_EXCEPTION_PREFIX;

/**
 *
 * @author Marian Petras
 */
final class TestMethodNodeChildren extends Children.Array {

    /** */
    private final Report.Testcase testcase;

    /** Creates a new instance of TestMethodNodeChildren */
    public TestMethodNodeChildren(final Report.Testcase testcase) {
        this.testcase = testcase;
    }

    /**
     */
    protected void addNotify() {
        Report.Trouble trouble = testcase.trouble;

        int nodesCount = 1;                     //exception class name
        if (trouble.message != null) {
            nodesCount++;
        }
        if (trouble.stackTrace != null) {
            nodesCount += trouble.stackTrace.length;
        }
        
        String topFrameInfo = (trouble.stackTrace != null)
                                    && (trouble.stackTrace.length != 0)
                                            ? trouble.stackTrace[0]
                                            : null;

        Node[] children = new Node[nodesCount];
        int index = 0;
        if (trouble.message != null) {
            children[index++] = new CallstackFrameNode(topFrameInfo,
                                                       trouble.message);
        }
        children[index++] = new CallstackFrameNode(topFrameInfo,
                                                   trouble.exceptionClsName);
        for (int i = 0; index < nodesCount; i++) {
            children[index++] = new CallstackFrameNode(trouble.stackTrace[i]);
        }
        
        if (trouble.nestedTrouble != null) {
            List<Node> childrenList = new ArrayList<Node>(nodesCount * 3);
            childrenList.addAll(Arrays.asList(children));
            
            trouble = trouble.nestedTrouble;
            do {
                String[] stackTrace = trouble.stackTrace;
                topFrameInfo = (stackTrace != null) && (stackTrace.length != 0)
                               ? stackTrace[0]
                               : null;
                StringBuilder topNodeDispName = new StringBuilder(200);
                topNodeDispName.append(NESTED_EXCEPTION_PREFIX);
                topNodeDispName.append(trouble.exceptionClsName);
                if (trouble.message != null) {
                    topNodeDispName.append(": ")                        //NOI18N
                                   .append(trouble.message);
                }
                childrenList.add(new CallstackFrameNode(topFrameInfo,
                                                        topNodeDispName.toString()));
                if (stackTrace != null) {
                    for (String frameInfo : stackTrace) {
                        childrenList.add(new CallstackFrameNode(frameInfo));
                    }
                }
            } while ((trouble = trouble.nestedTrouble) != null);
            
            children = childrenList.toArray(new Node[childrenList.size()]);
        }
        
        add(children);
    }
    
    /**
     */
    protected void removeNotify() {
        remove(getNodes());
    }
    
}
