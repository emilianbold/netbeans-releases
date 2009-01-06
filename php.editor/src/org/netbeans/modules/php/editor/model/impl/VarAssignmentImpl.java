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
import org.netbeans.modules.gsf.api.annotations.CheckForNull;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.ModelScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.openide.util.Union2;

/**
 *
 * @author Radek Matous
 */
class VarAssignmentImpl extends ScopeImpl implements VarAssignment {

    private VariableNameImpl var;
    //TODO: typeName should be list or array to keep mixed types
    private Union2<String,List<? extends TypeScope>> typeName;
    private OffsetRange scopeRange;

    VarAssignmentImpl(VariableNameImpl var, ScopeImpl scope, OffsetRange scopeRange,OffsetRange nameRange, Assignment assignment,
            Map<String, VariableNameImpl> allAssignments) {
        this(var, scope, scopeRange, nameRange, VariousUtils.extractVariableTypeFromAssignment(assignment, allAssignments));
    }

    VarAssignmentImpl(VariableNameImpl var, ScopeImpl scope, OffsetRange scopeRange, OffsetRange nameRange, String typeName) {
        super(scope, var.getName(), var.getFile(), nameRange, var.getPhpKind());
        this.var = var;
        this.typeName = Union2.<String,List<? extends TypeScope>>createFirst(typeName);
        this.scopeRange = scopeRange;
    }

    @CheckForNull
    Union2<String,List<? extends TypeScope>> getTypeUnion() {
        return typeName;
    }

    @CheckForNull
    private List<? extends TypeScope> typesFromUnion() {
        Union2<String, List<? extends TypeScope>> typeUnion = getTypeUnion();
        if (typeUnion != null) {
            if (typeUnion.hasSecond() && typeUnion.second() != null) {
                return typeUnion.second();
            }
        }
        return null;
    }

    String typeNameFromUnion() {
        Union2<String, List<? extends TypeScope>> typeUnion = getTypeUnion();
        if (typeUnion != null) {
            if (typeUnion.hasFirst() && typeUnion.first() != null) {
                return typeUnion.first();
            } else if (typeUnion.hasSecond() && typeUnion.second() != null) {
                TypeScope type = ModelUtils.getFirst(typeUnion.second());
                return type.getName();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getVar().getName());
        sb.append(" == ").append(getTypeUnion());
        return sb.toString();
    }

    /*@Override
    StringBuilder golden(int indent) {
        String prefix = "";//NOI18N
        for (int i = 0; i < indent; i++) {
            prefix += "  ";
        }//NOI18N
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(toString()).append("\n");//NOI18N
        return sb;
    }*/

    public List<? extends TypeScope> getTypes() {
        String name = var.getName();
        List<? extends TypeScope> empty = Collections.emptyList();
        ModelScope topScope = ModelUtils.getModelScope(this);
        //TODO: cache the value
        List<? extends TypeScope> types = typesFromUnion();
        if (types != null) {
            return types;
        }
        String tName = typeNameFromUnion();
        if (tName != null) {
            //StackOverflow prevention
            if (tName.indexOf(name) == -1) {
                types = VariousUtils.getType(topScope, (VariableScope) getInScope(),
                        tName, getOffset(), false);
            }
        }
        if (types != null) {
            typeName = Union2.<String, List<? extends TypeScope>>createSecond(types);
            return types;
        } else {
            typeName = null;
        }
        return empty;
    }

    /**
     * @return the var
     */
    VariableNameImpl getVar() {
        return var;
    }

    @Override
    public OffsetRange getBlockRange() {
        return scopeRange;
    }
}
