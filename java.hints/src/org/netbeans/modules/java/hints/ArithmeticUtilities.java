/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.util.EnumSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.java.source.CompilationInfo;
import static com.sun.source.tree.Tree.Kind.*;

/**XXX: should be tested (currently tested only indirectly)
 *
 * @author lahvac
 */
public class ArithmeticUtilities {

    public static Number compute(CompilationInfo info, TreePath tp, boolean resolveCompileTimeConstants) {
        return new VisitorImpl(info, resolveCompileTimeConstants).scan(tp, null);
    }

    private static final class VisitorImpl extends TreePathScanner<Number, Void> {

	private static final Set<Kind> ACCEPTED_KINDS = EnumSet.of(
		MULTIPLY, DIVIDE, REMAINDER, PLUS, MINUS,
		LEFT_SHIFT, RIGHT_SHIFT, UNSIGNED_RIGHT_SHIFT, AND, XOR,
		OR, UNARY_MINUS, UNARY_PLUS, PARENTHESIZED, IDENTIFIER,
		MEMBER_SELECT, INT_LITERAL, LONG_LITERAL, FLOAT_LITERAL, DOUBLE_LITERAL);

        private final CompilationInfo info;
        private final boolean resolveCompileTimeConstants;

        public VisitorImpl(CompilationInfo info, boolean resolveCompileTimeConstants) {
            this.info = info;
            this.resolveCompileTimeConstants = resolveCompileTimeConstants;
        }

	@Override
	public Number scan(Tree tree, Void p) {
	    if (tree == null) return null;
	    if (!ACCEPTED_KINDS.contains(tree.getKind())) return null;
	    return super.scan(tree, p);
	}

        @Override
        public Number visitLiteral(LiteralTree node, Void p) {
            if (node.getValue() instanceof Number) {
                return (Number) node.getValue();
            }

            return super.visitLiteral(node, p);
        }

        @Override
        public Number visitIdentifier(IdentifierTree node, Void p) {
            return resolve();
        }

        @Override
        public Number visitMemberSelect(MemberSelectTree node, Void p) {
            return resolve();
        }

        private Number resolve() {
            if (!resolveCompileTimeConstants) {
                return null;
            }

            Element el = info.getTrees().getElement(getCurrentPath());

            if (el == null || el.getKind() != ElementKind.FIELD) {
                return null;
            }

            Object obj = ((VariableElement) el).getConstantValue();

            if (!(obj instanceof Number)) {
                return null;
            }

            return (Number) obj;
        }
        
        @Override
        public Number visitBinary(BinaryTree node, Void p) {
            Number left  = scan(node.getLeftOperand(), p);
            Number right = scan(node.getRightOperand(), p);

            if (left != null && right != null) {
                Number result = null;
                switch (node.getKind()) {
                    case MULTIPLY:
                            if (left instanceof Double || right instanceof Double) {
                                result = left.doubleValue() * right.doubleValue();
                            } else if (left instanceof Float || right instanceof Float) {
                                result = left.floatValue() * right.floatValue();
                            } else if (left instanceof Long || right instanceof Long) {
                                result = left.longValue() * right.longValue();
                            } else if (left instanceof Integer || right instanceof Integer) {
                                result = left.intValue() * right.intValue();
                            } else {
                                throw new IllegalStateException("left=" + left.getClass() + ", right=" + right.getClass());
                            }
                            break;

                    case DIVIDE:
                            if (left instanceof Double || right instanceof Double) {
                                result = left.doubleValue() / right.doubleValue();
                            } else if (left instanceof Float || right instanceof Float) {
                                result = left.floatValue() / right.floatValue();
                            } else if (left instanceof Long || right instanceof Long) {
                                result = left.longValue() / right.longValue();
                            } else if (left instanceof Integer || right instanceof Integer) {
                                result = left.intValue() / right.intValue();
                            } else {
                                throw new IllegalStateException("left=" + left.getClass() + ", right=" + right.getClass());
                            }
                            break;

                    case REMAINDER:
                            if (left instanceof Double || right instanceof Double) {
                                result = left.doubleValue() % right.doubleValue();
                            } else if (left instanceof Float || right instanceof Float) {
                                result = left.floatValue() % right.floatValue();
                            } else if (left instanceof Long || right instanceof Long) {
                                result = left.longValue() % right.longValue();
                            } else if (left instanceof Integer || right instanceof Integer) {
                                result = left.intValue() % right.intValue();
                            } else {
                                throw new IllegalStateException("left=" + left.getClass() + ", right=" + right.getClass());
                            }
                            break;

                    case PLUS:
                            if (left instanceof Double || right instanceof Double) {
                                result = left.doubleValue() + right.doubleValue();
                            } else if (left instanceof Float || right instanceof Float) {
                                result = left.floatValue() + right.floatValue();
                            } else if (left instanceof Long || right instanceof Long) {
                                result = left.longValue() + right.longValue();
                            } else if (left instanceof Integer || right instanceof Integer) {
                                result = left.intValue() + right.intValue();
                            } else {
                                throw new IllegalStateException("left=" + left.getClass() + ", right=" + right.getClass());
                            }
                            break;

                    case MINUS:
                            if (left instanceof Double || right instanceof Double) {
                                result = left.doubleValue() - right.doubleValue();
                            } else if (left instanceof Float || right instanceof Float) {
                                result = left.floatValue() - right.floatValue();
                            } else if (left instanceof Long || right instanceof Long) {
                                result = left.longValue() - right.longValue();
                            } else if (left instanceof Integer || right instanceof Integer) {
                                result = left.intValue() - right.intValue();
                            } else {
                                throw new IllegalStateException("left=" + left.getClass() + ", right=" + right.getClass());
                            }
                            break;

                    case LEFT_SHIFT:
                            if (left instanceof Long || right instanceof Long) {
                                result = left.longValue() << right.longValue();
                            } else if (left instanceof Integer || right instanceof Integer) {
                                result = left.intValue() << right.intValue();
                            } else {
                                throw new IllegalStateException("left=" + left.getClass() + ", right=" + right.getClass());
                            }
                            break;

                    case RIGHT_SHIFT:
                            if (left instanceof Long || right instanceof Long) {
                                result = left.longValue() >> right.longValue();
                            } else if (left instanceof Integer || right instanceof Integer) {
                                result = left.intValue() >> right.intValue();
                            } else {
                                throw new IllegalStateException("left=" + left.getClass() + ", right=" + right.getClass());
                            }
                            break;

                    case UNSIGNED_RIGHT_SHIFT:
                            if (left instanceof Long || right instanceof Long) {
                                result = left.longValue() >>> right.longValue();
                            } else if (left instanceof Integer || right instanceof Integer) {
                                result = left.intValue() >>> right.intValue();
                            } else {
                                throw new IllegalStateException("left=" + left.getClass() + ", right=" + right.getClass());
                            }
                            break;

                    case AND:
                            if (left instanceof Long || right instanceof Long) {
                                result = left.longValue() & right.longValue();
                            } else if (left instanceof Integer || right instanceof Integer) {
                                result = left.intValue() & right.intValue();
                            } else {
                                throw new IllegalStateException("left=" + left.getClass() + ", right=" + right.getClass());
                            }
                            break;

                    case XOR:
                            if (left instanceof Long || right instanceof Long) {
                                result = left.longValue() ^ right.longValue();
                            } else if (left instanceof Integer || right instanceof Integer) {
                                result = left.intValue() ^ right.intValue();
                            } else {
                                throw new IllegalStateException("left=" + left.getClass() + ", right=" + right.getClass());
                            }
                            break;

                    case OR:
                            if (left instanceof Long || right instanceof Long) {
                                result = left.longValue() | right.longValue();
                            } else if (left instanceof Integer || right instanceof Integer) {
                                result = left.intValue() | right.intValue();
                            } else {
                                throw new IllegalStateException("left=" + left.getClass() + ", right=" + right.getClass());
                            }
                            break;
                }

                return result;
            }

            return null;
        }

        @Override
        public Number visitUnary(UnaryTree node, Void p) {
            Number op  = scan(node.getExpression(), p);

            if (op != null) {
                Number result = null;
                switch (node.getKind()) {
                    case UNARY_MINUS:
                            if (op instanceof Double) {
                                result = -op.doubleValue();
                            } else if (op instanceof Float) {
                                result = -op.floatValue();
                            } else if (op instanceof Long) {
                                result = -op.longValue();
                            } else if (op instanceof Integer) {
                                result = -op.intValue();
                            } else {
                                throw new IllegalStateException("op=" + op.getClass());
                            }
                            break;
                    case UNARY_PLUS:
                        result = op;
                        break;
                }

                return result;
            }

            return super.visitUnary(node, p);
        }
    }
}
