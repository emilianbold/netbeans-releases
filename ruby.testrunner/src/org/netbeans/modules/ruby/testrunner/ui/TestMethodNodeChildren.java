/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.ruby.testrunner.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

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

        int nodesCount = trouble.exceptionClsName != null ? 1 : 0;                     //exception class name
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
        if (trouble.exceptionClsName != null) {
            children[index++] = new CallstackFrameNode(topFrameInfo,
                    trouble.exceptionClsName);
        }
        for (int i = 0; index < nodesCount; i++) {
            if (i == 0 && nodesCount >= 1) {
                children[index++] = new CallstackFrameNode(trouble.stackTrace[1], trouble.stackTrace[0]);
            } else {
                children[index++] = new CallstackFrameNode(trouble.stackTrace[i]);
            }
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
//                topNodeDispName.append(NESTED_EXCEPTION_PREFIX);
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
