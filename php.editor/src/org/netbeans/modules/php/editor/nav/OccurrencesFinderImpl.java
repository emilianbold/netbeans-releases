/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.nav;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.gsf.api.ColoringAttributes;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OccurrencesFinder;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.php.editor.nav.SemiAttribute.AttributedElement;
import org.netbeans.modules.php.editor.nav.SemiAttribute.AttributedElement.Kind;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;

/**
 *
 * @author Jan Lahoda
 */
public class OccurrencesFinderImpl implements OccurrencesFinder {

    private int offset;
    private Map<OffsetRange, ColoringAttributes> range2Attribs;
    
    public void setCaretPosition(int position) {
        this.offset = position;
        this.range2Attribs = new HashMap<OffsetRange, ColoringAttributes>();
    }

    public Map<OffsetRange, ColoringAttributes> getOccurrences() {
        return range2Attribs;
    }

    public void cancel() {
    }

    public void run(CompilationInfo parameter) throws Exception {
        for (OffsetRange r : compute(parameter, offset)) {
            range2Attribs.put(r, ColoringAttributes.MARK_OCCURRENCES);
        }
    }
    
    static Collection<OffsetRange> compute(CompilationInfo parameter, int offset) {
        final List<OffsetRange> result = new LinkedList<OffsetRange>();
        List<ASTNode> path = NavUtils.underCaret(parameter, offset);
        final SemiAttribute a = SemiAttribute.semiAttribute(parameter);
        final AttributedElement el = NavUtils.findElement(parameter, path, offset, a);
        
        if (el == null) {
            return result;
        }
        
        final List<ASTNode> usages = new LinkedList<ASTNode>();
        
        new DefaultVisitor() {
           @Override
            public void visit(FunctionDeclaration node) {
                if (el == a.getElement(node)) {
                    usages.add(node.getFunctionName());
                }
                super.visit(node);
            }

            @Override
            public void visit(ClassDeclaration node) {
                if (el == a.getElement(node)) {
                    usages.add(node.getName());
                }
                super.visit(node);
            }

            @Override
            public void visit(FunctionInvocation node) {
                if (el == a.getElement(node)) {
                    usages.add(node.getFunctionName());
                }
                super.visit(node);
            }

            @Override
            public void visit(Variable node) {
                if (el == a.getElement(node)) {
                    usages.add(node);
                }
                super.visit(node);
            }
            @Override
            public void visit(ArrayAccess node) {
                if (el == a.getElement(node)) {
                    usages.add(node);
                }
                super.visit(node);
            }

            @Override
            public void visit(Scalar scalar) {
                if (el == a.getElement(scalar)) {
                    usages.add(scalar);
                }
                super.visit(scalar);
            }

            @Override
            public void visit(ClassInstanceCreation node) {
                if (el == a.getElement(node)) {
                    usages.add(node.getClassName());
                }
                super.visit(node);
            }
        }.scan(Utils.getRoot(parameter));
        
        for (ASTNode n : usages) {
            OffsetRange forNode = forNode(n, el.getKind());
            if (forNode != null) {
                result.add(forNode);
            }
        }
        
        return result;
    }

        private static OffsetRange forNode(ASTNode n, Kind kind) {
            OffsetRange retval = null;
            if (n instanceof Scalar && ((Scalar) n).getScalarType() == Scalar.Type.STRING && NavUtils.isQuoted(((Scalar) n).getStringValue())) {
                retval = new OffsetRange(n.getStartOffset() + 1, n.getEndOffset() - 1);
            } else if (n instanceof Variable && ((Variable)n).isDollared()) {
                retval = new OffsetRange(n.getStartOffset()+1, n.getEndOffset());
            } else if (n instanceof ArrayAccess && kind == Kind.VARIABLE) {
                ArrayAccess arrayAccess = (ArrayAccess) n;
                Expression index = arrayAccess.getIndex();
                retval = forNode(index, kind);
            } else {
                retval = new OffsetRange(n.getStartOffset(), n.getEndOffset());
            }
            return retval;
        }

}
