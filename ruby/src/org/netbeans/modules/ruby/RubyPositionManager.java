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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.ruby;

import java.util.List;

import org.jruby.ast.Node;
import org.jruby.lexer.yacc.ISourcePosition;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.PositionManager;
import org.netbeans.modules.ruby.elements.AstElement;
import org.netbeans.modules.ruby.elements.RubyElement;

/**
 * @author Tor Norbye
 */
public class RubyPositionManager implements PositionManager {

    public OffsetRange getOffsetRange(CompilationInfo info, ElementHandle objectHandle) {
        RubyElement object = RubyParser.resolveHandle(info, objectHandle);
        if (object instanceof AstElement) {
            Node target = ((AstElement)object).getNode();
            ISourcePosition pos = target.getPosition();

            return new OffsetRange(pos.getStartOffset(), pos.getEndOffset());
        } else {
            if (objectHandle instanceof AstElement) {
                AstElement el = (AstElement)objectHandle;
                if (el.getNode() != null) {
                    return AstUtilities.getRange(el.getNode());
                }
            }
            throw new IllegalArgumentException("Foreign element: " + object + " of type " +
                    (object != null ? object.getClass().getName() : "null"));
        }
    }

    /**
     * Find the position closest to the given offset in the AST. Place the path from the leaf up to the path in the
     * passed in path list.
     * @todo Build up an AstPath instead!
     */
    public static Node findPathTo(Node node, List<Node> path, int offset) {
        Node result = find(node, path, offset);
        path.add(node);

        return result;
    }

    private static Node find(Node node, List<Node> path, int offset) {
        ISourcePosition pos = node.getPosition();
        int begin = pos.getStartOffset();
        int end = pos.getEndOffset();

        if ((offset >= begin) && (offset <= end)) {
            List<Node> children = (List<Node>)node.childNodes();

            for (Node child : children) {
                if (child.isInvisible()) {
                    continue;
                }
                Node found = find(child, path, offset);

                if (found != null) {
                    path.add(child);

                    return found;
                }
            }

            return node;
        } else {
            List<Node> children = (List<Node>)node.childNodes();

            for (Node child : children) {
                if (child.isInvisible()) {
                    continue;
                }
                Node found = find(child, path, offset);

                if (found != null) {
                    path.add(child);

                    return found;
                }
            }

            return null;
        }
    }

    /**
     * Find the path to the given node in the AST
     */
    public static boolean find(Node node, List<Node> path, Node target) {
        if (node == target) {
            return true;
        }

        List<Node> children = (List<Node>)node.childNodes();

        for (Node child : children) {
            if (child.isInvisible()) {
                continue;
            }
            boolean found = find(child, path, target);

            if (found) {
                path.add(child);

                return found;
            }
        }

        return false;
    }

    public boolean isTranslatingSource() {
        return false;
    }

    public int getLexicalOffset(ParserResult result, int astOffset) {
        return astOffset;
    }

    public int getAstOffset(ParserResult result, int lexicalOffset) {
        return lexicalOffset;
    }
}
