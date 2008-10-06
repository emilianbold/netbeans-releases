/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.junit.output;

import java.util.ArrayList;
import java.util.Collection;
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
    /** */
    private final Collection<Node> childNodes;

    private TestMethodNodeChildren(final Collection<Node> nodes,
                                   final Report.Testcase testcase) {
        super(nodes);
        this.childNodes = nodes;
        this.testcase = testcase;
    }

    /** Creates a new instance of TestMethodNodeChildren */
    public TestMethodNodeChildren(final Report.Testcase testcase) {
        this(new ArrayList<Node>(getChildrenCount(testcase)), testcase);
    }

    /**
     * Count the number of created children if the constructor would be passed
     * the given {@code Testcase}. The return value of {@code 0} means that
     * there is no reason for creating an instance of
     * {@code TestMethodNodeChildren}.
     * @param  testcase  test for which the number of children should be counted
     * @return  number of children if the constructor would be passed the given
     *          {@code Testcase} as an argument
     */
    static int getChildrenCount(Report.Testcase testcase) {
        Report.Trouble trouble = testcase.trouble;
        if (trouble == null) {
            return 0;
        }

        int count = 0;
        if (trouble.message != null) {
            count++;
        }
        if (trouble.exceptionClsName != null) {
            count++;
        }
        if (trouble.stackTrace != null) {
            count += trouble.stackTrace.length;
        }
        return count;
    }

    /**
     */
    @Override
    protected void addNotify() {
        Report.Trouble trouble = testcase.trouble;
        if (trouble.message != null) {
            childNodes.add(new CallstackFrameNode(trouble,
                                                  trouble.message));
        }
        if (trouble.exceptionClsName != null) {
            childNodes.add(new CallstackFrameNode(trouble,
                                                  trouble.exceptionClsName));
        }
        if (trouble.stackTrace != null) {
            for (String frameInfo : trouble.stackTrace) {
                childNodes.add(new CallstackFrameNode(frameInfo));
            }
        }
        
        if (trouble.nestedTrouble != null) {
            trouble = trouble.nestedTrouble;
            do {
                String[] stackTrace = trouble.stackTrace;
                StringBuilder topNodeDispName = new StringBuilder(200);
                topNodeDispName.append(NESTED_EXCEPTION_PREFIX);
                topNodeDispName.append(trouble.exceptionClsName);
                if (trouble.message != null) {
                    topNodeDispName.append(": ")                        //NOI18N
                                   .append(trouble.message);
                }
                childNodes.add(new CallstackFrameNode(trouble,
                                                      topNodeDispName.toString()));
                if (stackTrace != null) {
                    for (String frameInfo : stackTrace) {
                        childNodes.add(new CallstackFrameNode(frameInfo));
                    }
                }
            } while ((trouble = trouble.nestedTrouble) != null);
        }
        
        super.addNotify();
    }
}
