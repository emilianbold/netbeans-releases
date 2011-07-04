/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib.api;

import java.io.PrintWriter;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 *
 * @author mfukala@netbeans.org
 */
public class NodeUtil {

    private static final String INDENT = "    ";

    
    public static Node query(Node base, String path) {
        return query(base, path, false);
    }

    /** find an Node according to the given tree path
     * example of path: declaration/property|1/color -- find a second color property  (index from zero)
     */
    public static Node query(Node base, String path, boolean caseInsensitive) {
        StringTokenizer st = new StringTokenizer(path, "/");
        Node found = base;
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            int indexDelim = token.indexOf('|');

            String nodeName = indexDelim >= 0 ? token.substring(0, indexDelim) : token;
            if (caseInsensitive) {
                nodeName = nodeName.toLowerCase(Locale.ENGLISH);
            }
            String sindex = indexDelim >= 0 ? token.substring(indexDelim + 1, token.length()) : "0";
            int index = Integer.parseInt(sindex);

            int count = 0;
            Node foundLocal = null;
            for (Node child : found.children()) {
                String childName = child.name();
                if ((caseInsensitive ? childName = childName.toLowerCase(Locale.ENGLISH) : childName).equals(nodeName) && count++ == index) {
                    foundLocal = child;
                    break;
                }
            }
            if (foundLocal != null) {
                found = foundLocal;

                if (!st.hasMoreTokens()) {
                    //last token, we may return
                    assert found.name().equals(nodeName);
                    return found;
                }

            } else {
                return null; //no found
            }
        }

        return null;
    }
    
    public static void dumpTree(Node node) {
        PrintWriter pw = new PrintWriter(System.out);
        dumpTree(node, pw);
        pw.flush();
    }
    
    public static void dumpTree(Node node, PrintWriter pw) {
        dump(node, 0, pw);
        
    }

    private static void dump(Node tree, int level, PrintWriter pw) {
        for (int i = 0; i < level; i++) {
            pw.print(INDENT);
        }
        treeToString(tree, pw);
        for (Node c : tree.children()) {
            dump(c, level + 1, pw);
        }
    }

    private static void treeToString(Node tree, PrintWriter b)  {
        b.print(tree.name());
        b.print(' ');
        b.print('[');
        b.print(tree.type().name());
        b.print(']');
        b.print('(');
        b.print(Integer.toString(tree.from()));
        b.print('-');
        b.print(Integer.toString(tree.to()));
        b.print(')');
        b.println();
    }
}
