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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby;

import java.util.List;

import org.jruby.ast.Node;
import org.jruby.lexer.yacc.ISourcePosition;
import org.netbeans.api.gsf.Element;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.api.gsf.PositionManager;
import org.netbeans.modules.ruby.elements.AstElement;


/**
 *
 * @author Tor Norbye
 */
public class RubyPositionManager implements PositionManager {
    /**
     * Creates a new instance of JRubyPositionManager
     */
    public RubyPositionManager() {
    }

    public OffsetRange getOffsetRange(Element file, Element object) {
        if (object instanceof AstElement) {
            Node target = ((AstElement)object).getNode();
            ISourcePosition pos = target.getPosition();

            return new OffsetRange(pos.getStartOffset(), pos.getEndOffset());
        } else {
            throw new IllegalArgumentException((("Foreign element: " + object + " of type " +
                object) != null) ? object.getClass().getName() : "null");
        }
    }

    /**
     * Find the position closest to the given offset in the AST. Place the path from the leaf up to the path in the
     * passed in path list.
     * @todo Build up an AstPath instead!
     */
    @SuppressWarnings("unchecked")
    public static Node findPathTo(Node node, List<Node> path, int offset) {
        Node result = find(node, path, offset);
        path.add(node);

        return result;
    }

    @SuppressWarnings("unchecked")
    private static Node find(Node node, List<Node> path, int offset) {
        ISourcePosition pos = node.getPosition();
        int begin = pos.getStartOffset();
        int end = pos.getEndOffset();

        if ((offset >= begin) && (offset <= end)) {
            List<Node> children = (List<Node>)node.childNodes();

            for (Node child : children) {
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
    @SuppressWarnings("unchecked")
    public static boolean find(Node node, List<Node> path, Node target) {
        if (node == target) {
            return true;
        }

        List<Node> children = (List<Node>)node.childNodes();

        for (Node child : children) {
            boolean found = find(child, path, target);

            if (found) {
                path.add(child);

                return found;
            }
        }

        return false;
    }
}
