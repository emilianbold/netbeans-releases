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

package org.netbeans.modules.gsf.testrunner.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.gsf.testrunner.api.TestRunnerNodeFactory;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Marian Petras
 */
final class TestMethodNodeChildren extends Children.Array {

    /** */
    private final Testcase testcase;

    /** Creates a new instance of TestMethodNodeChildren */
    public TestMethodNodeChildren(final Testcase testcase) {
        this.testcase = testcase;
    }

    /**
     */
    @Override
    protected void addNotify() {
        Trouble trouble = testcase.getTrouble();

        int nodesCount = trouble.getExceptionClsName() != null ? 1 : 0;                     //exception class name
        if (trouble.getMessage() != null) {
            nodesCount++;
        }
        if (trouble.getStackTrace() != null) {
            nodesCount += trouble.getStackTrace().length;
        }
        
        String topFrameInfo = (trouble.getStackTrace() != null)
                                    && (trouble.getStackTrace().length != 0)
                                            ? trouble.getStackTrace()[0]
                                            : null;

        Node[] children = new Node[nodesCount];
        int index = 0;
        TestRunnerNodeFactory nodeFactory = testcase.getSession().getNodeFactory();
        if (trouble.getMessage() != null) {
            children[index++] = nodeFactory.createCallstackFrameNode(topFrameInfo,trouble.getMessage());
        }
        if (trouble.getExceptionClsName() != null) {
            children[index++] = nodeFactory.createCallstackFrameNode(topFrameInfo,trouble.getExceptionClsName());
        }
        for (int i = 0; index < nodesCount; i++) {
            if (i == 0 && nodesCount >= 2) {
                children[index++] = nodeFactory.createCallstackFrameNode(trouble.getStackTrace()[1], trouble.getStackTrace()[0]);
            } else {
                children[index++] = nodeFactory.createCallstackFrameNode(trouble.getStackTrace()[i], null);
            }
        }
        
        if (trouble.getNestedTrouble() != null) {
            List<Node> childrenList = new ArrayList<Node>(nodesCount * 3);
            childrenList.addAll(Arrays.asList(children));
            
            trouble = trouble.getNestedTrouble();
            do {
                String[] stackTrace = trouble.getStackTrace();
                topFrameInfo = (stackTrace != null) && (stackTrace.length != 0)
                               ? stackTrace[0]
                               : null;
                StringBuilder topNodeDispName = new StringBuilder(200);
//                topNodeDispName.append(NESTED_EXCEPTION_PREFIX);
                topNodeDispName.append(trouble.getExceptionClsName());
                if (trouble.getMessage() != null) {
                    topNodeDispName.append(": ")                        //NOI18N
                                   .append(trouble.getMessage());
                }
                childrenList.add(nodeFactory.createCallstackFrameNode(topFrameInfo,
                                                        topNodeDispName.toString()));
                if (stackTrace != null) {
                    for (String frameInfo : stackTrace) {
                        childrenList.add(nodeFactory.createCallstackFrameNode(frameInfo, null));
                    }
                }
            } while ((trouble = trouble.getNestedTrouble()) != null);
            
            children = childrenList.toArray(new Node[childrenList.size()]);
        }
        
        add(children);
    }
}
