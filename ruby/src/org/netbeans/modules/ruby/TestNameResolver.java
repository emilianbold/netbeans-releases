/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jrubyparser.ast.FCallNode;
import org.jrubyparser.ast.IScopingNode;
import org.jrubyparser.ast.ListNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.jrubyparser.ast.StrNode;

/**
 * Helper for resolving test method names.
 */
final class TestNameResolver {

    private static final String SHOULDA_METHOD = "should"; //NOI18N

    private static final String[] TEST_METHOD_NAMES = {"test", "describe", 
            "specify", "context", SHOULDA_METHOD, "it", "before", "after"}; //NOI18N

    /**
     *@return true if the given name represents a possible name
     * for a test method.
     */
    static boolean isTestMethodName(String name) {
        for (String each : TEST_METHOD_NAMES) {
            if (each.equals(name)) {
                return true;
            }
        }
        return false;
    }

    static boolean isShouldaMethod(String name) {
        return SHOULDA_METHOD.equals(name);
    }

    /**
     * Gets the test name from the given path. Returns <code>null</code>
     * if no name could be resolved.
     * 
     * @param path the path to the the test.
     *
     * @return the name or <code>null</code>.
     */
    static String getTestName(AstPath path) {
        Iterator<Node> it = path.leafToRoot();
        /*
         * method names for shoulda tests need to be constructed from
         * should and context nodes, e.g.
         * context "An Instance" ...
         *  should "respond to :something" ... (the method name here is "An Instance should respond to :something"
         *    context "with a single element" ..
         *        should "return that" ... (the name here is "An Instance with a single element should return that")
         *
         * the name for a should node without context uses the name of the tested class, e.g.
         * class QueueTest
         *  should "be empty" do ..
         *  end
         * end
         * => the method name is "Queue should be empty"
         */
        List<String> shouldaMethodName = new ArrayList<String>();
        // for shoulda tests without a context, the class name
        // needs to be appended - see #151652 for details
        boolean appendClassName = true;
        while (it.hasNext()) {
            Node node = it.next();
            if (node.getNodeType() == NodeType.FCALLNODE) {
                FCallNode fc = (FCallNode) node;
                // Possibly a test node
                // See http://github.com/rails/rails/commit/f74ba37f4e4175d5a1b31da59d161b0020b58e94
                // test_name = "test_#{name.gsub(/[\s]/,'_')}".to_sym
                if ("test".equals(fc.getName())) { // NOI18N
                    String desc = getNodeDesc(fc);
                    if (desc != null) {
                        return "test_" + desc.replace(' ', '_'); // NOI18N
                    }
                    return null;
                    // possibly a shoulda test
                } else if ("should".equals(fc.getName())) { //NOI18N
                    buildShouldaMethod(" should " + getNodeDesc(fc), shouldaMethodName, false);
                } else if ("context".equals(fc.getName())) { //NOI18N
                    String desc = getNodeDesc(fc);
                    if (desc != null) {
                        appendClassName = false;
                    }
                    buildShouldaMethod(desc, shouldaMethodName, true);
                }
            } else if (node.getNodeType() == NodeType.CLASSNODE && appendClassName) {
                String className = getClassNameForShoulda((IScopingNode) node);
                buildShouldaMethod(className, shouldaMethodName, false);
            } else if (node.getNodeType() == NodeType.DEFNNODE || node.getNodeType() == NodeType.DEFSNODE) {
                return AstUtilities.getName(node);
            }
        }

        if (!shouldaMethodName.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String each : shouldaMethodName) {
                sb.append(each);
            }
            return removeLeadingWhiteSpace(sb.toString());
        }

        return null;

    }

    private static String getNodeDesc(FCallNode fc) {
        if (fc.getIterNode() == null) { // "it" without do/end: pending
            return null;
        }

        Node argsNode = fc.getArgsNode();

        if (argsNode instanceof ListNode) {
            ListNode args = (ListNode) argsNode;

            //  describe  ThingsController, "GET #index" do
            // e.g. where the desc string is not first
            for (int i = 0, max = args.size(); i < max; i++) {
                Node n = args.get(i);

                // For dynamically computed strings, we have n instanceof DStrNode
                // but I can't handle these anyway
                if (n instanceof StrNode) {
                    String descBl = ((StrNode) n).getValue();

                    if ((descBl != null) && (descBl.length() > 0)) {
                        // No truncation? See 138259
                        //desc = RubyUtils.truncate(descBl.toString(), MAX_RUBY_LABEL_LENGTH);
                        return descBl.toString();
                    }
                    break;
                }
            }
        }
        return null;

    }

    private static void buildShouldaMethod(String desc, List<String> shouldaMethodName, boolean trim) {
        if (desc == null) {
            return;
        }
        // shoulda removes leading and trailing whitespaces for context nodes, but not
        // for should nodes
        if (trim) {
            desc = desc.trim();
        }
        if (shouldaMethodName.isEmpty()) {
            shouldaMethodName.add(desc);
        } else {
            shouldaMethodName.add(0, " " + desc); //NOI18N
        }
    }

    private static String getClassNameForShoulda(IScopingNode classNode) {
        String testClassName = AstUtilities.getClassOrModuleName(classNode);
        if (testClassName != null && testClassName.indexOf("Test") != -1) { //NOI18N
            return testClassName.substring(0, testClassName.indexOf("Test")); //NOI18N
        }
        return null;
    }

    private static String removeLeadingWhiteSpace(String str) {
        if (str.startsWith(" ")) { //NOI18N
            return str.substring(1);
        }
        return str;
    }
}
