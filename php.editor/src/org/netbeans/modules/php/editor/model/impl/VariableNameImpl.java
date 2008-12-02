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

package org.netbeans.modules.php.editor.model.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.index.IndexedVariable;
import org.netbeans.modules.php.editor.model.PhpKind;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

/**
 * @author Radek Matous
 */
class VariableNameImpl extends ScopeImpl implements VariableName {
    private boolean globallyVisible;
    VariableNameImpl(IndexScopeImpl inScope, IndexedVariable indexedVariable) {
        this(inScope, indexedVariable.getName(),
                Union2.<String/*url*/, FileObject>createFirst(indexedVariable.getFilenameUrl()),
                new OffsetRange(indexedVariable.getOffset(),indexedVariable.getOffset()+indexedVariable.getName().length()), true);
    }
    VarAssignmentImpl createElement(ScopeImpl scope, Variable varNode, Assignment assignment, Map<String, VariableNameImpl> allAssignments) {
        VarAssignmentImpl retval = new VarAssignmentImpl(this, scope, varNode,assignment, allAssignments);
        addElement(retval);
        return retval;
    }

    VariableNameImpl(ScopeImpl inScope, Program program, Variable variable, boolean globallyVisible) {
        this(inScope, toName(variable), inScope.getFile(), toOffsetRange(variable), globallyVisible);
    }
    private VariableNameImpl(ScopeImpl inScope, String name, Union2<String/*url*/, FileObject> file, OffsetRange offsetRange, boolean globallyVisible) {
        super(inScope, name, file, offsetRange, PhpKind.VARIABLE);
        this.globallyVisible = globallyVisible;
    }

    static String toName(Variable node) {
        return CodeUtils.extractVariableName(node);
    }

    static OffsetRange toOffsetRange(Variable node) {
        Expression name = node.getName();
        //TODO: dangerous never ending loop
        while ((name instanceof Variable)) {
            while (name instanceof ArrayAccess) {
                ArrayAccess access = (ArrayAccess) name;
                name = access.getName();
            }
            if (name instanceof Variable) {
                Variable var = (Variable) name;
                name = var.getName();
            }
        }
        return new OffsetRange(name.getStartOffset(), name.getEndOffset());
    }

    public List<? extends VarAssignmentImpl> getAssignments() {
        return filter(getElements(), new ElementFilter() {
            public boolean isAccepted(ModelElementImpl element) {
                return true;
            }
        });
    }

    public VarAssignment findAssignment(int offset) {
        VarAssignmentImpl retval = null;
        List<? extends VarAssignmentImpl> assignments = getAssignments();
        if (assignments.size() == 1) {
            retval = assignments.get(0);
        } else {
            for (VarAssignmentImpl varAssignmentImpl : assignments) {
                if (varAssignmentImpl.getOffset() <= offset) {
                    if (retval == null || retval.getOffset() <= varAssignmentImpl.getOffset()) {
                        retval = varAssignmentImpl;
                    }
                }
            }
        }
        return retval;
    }

    @Override
    public String getNormalizedName() {
        String in = getIn();
        return (in != null && !isGloballyVisible()) ? in+getName() : getName();
    }

    public List<? extends TypeScope> getTypes(int offset) {
        List<? extends TypeScope> empty = Collections.emptyList();
        VarAssignment assignment = findAssignment(offset);
        return (assignment != null) ? assignment.getTypes() : empty;
    }

    public boolean isGloballyVisible() {
        return globallyVisible;
    }

    /**
     * @param globallyVisible the globallyVisible to set
     */
    void setGloballyVisible(boolean globallyVisible) {
        this.globallyVisible = globallyVisible;
    }
}
