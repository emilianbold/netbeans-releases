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

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.php.editor.model.*;
import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.openide.util.Union2;

/**
 *
 * @author Radek Matous
 */
class  AssignmentImpl<Container extends ModelElementImpl>  extends ScopeImpl {
    private Container container;
    //TODO: typeName should be list or array to keep mixed types
    private Union2<String,Collection<? extends TypeScope>> typeName;
    private OffsetRange scopeRange;
    private boolean arrayAccess;
    private boolean conditionalBlock;

    AssignmentImpl(Container container, Scope scope, OffsetRange scopeRange,OffsetRange nameRange, Assignment assignment,
            Map<String, AssignmentImpl> allAssignments) {
        this(container, scope, scopeRange, nameRange, VariousUtils.extractVariableTypeFromAssignment(assignment, allAssignments));
        if (assignment.getLeftHandSide() instanceof ArrayAccess) {
            arrayAccess = true;
        }
    }

    AssignmentImpl(Container container, Scope scope, OffsetRange scopeRange, OffsetRange nameRange, String typeName) {
        super(scope, container.getName(), container.getFile(), nameRange, container.getPhpElementKind());
        this.container = container;
        this.typeName = Union2.<String, Collection<? extends TypeScope>>createFirst(typeName);
        this.scopeRange = scopeRange;
    }

    @CheckForNull
    Union2<String,Collection<? extends TypeScope>> getTypeUnion() {
        return typeName;
    }

    boolean canBeProcessed(String tName) {
        return canBeProcessed(tName, getName()) && canBeProcessed(tName, getName().substring(1));
    }

    static boolean canBeProcessed(String tName, String name) {
        return tName.length() > 0 && tName.indexOf(name) == -1;
    }

    @CheckForNull
    private Collection<? extends TypeScope> typesFromUnion() {
        Union2<String, Collection<? extends TypeScope>> typeUnion = getTypeUnion();
        if (typeUnion != null) {
            if (typeUnion.hasSecond() && typeUnion.second() != null) {
                return typeUnion.second();
            }
        }
        return null;
    }

    String typeNameFromUnion() {
        Union2<String, Collection<? extends TypeScope>> typeUnion = getTypeUnion();
        if (typeUnion != null) {
            if (typeUnion.hasFirst() && typeUnion.first() != null) {
                return typeUnion.first();
            } else if (typeUnion.hasSecond() && typeUnion.second() != null) {
                TypeScope type = ModelUtils.getFirst(typeUnion.second());
                return type != null ? type.getName() : null;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(" == ").append(getTypeUnion());
        return sb.toString();
    }

    public Collection<? extends String> getTypeNames() {
        final String tName = typeNameFromUnion();
        if (tName != null) {
            return Collections.singleton(tName);
        }
        return Collections.emptyList();
    }
    
    public Collection<? extends TypeScope> getTypes() {
        List<? extends TypeScope> empty = Collections.emptyList();
        Collection<? extends TypeScope> types = typesFromUnion();
        if (types != null) {
            return types;
        }
        String tName = typeNameFromUnion();
        if (tName != null) {
            //StackOverflow prevention
            if (canBeProcessed(tName)) {
                types = VariousUtils.getType( (VariableScope) getInScope(),
                        tName, getOffset(), false);
            }
        }
        if (types != null) {
            if (types.isEmpty() && tName != null && !tName.contains("@")) {//NOI18N
                return empty;
            }
            typeName = Union2.<String, Collection<? extends TypeScope>>createSecond(types);
            return types;
        } else {
            typeName = null;
        }
        return empty;
    }

    Container getContainer() {
        return container;
    }

    @Override
    public OffsetRange getBlockRange() {
        return scopeRange;
    }

    @Override
    public String getNormalizedName() {
        return getClass().getName()+":"+ toString() + ":" + String.valueOf(getOffset());//NOI18N
    }

    public boolean isArrayAccess() {
        final String tpName = typeNameFromUnion();
        return arrayAccess || (tpName != null && tpName.equals("array"));//NOI18N
    }

    public void setAsArrayAccess(boolean arrayAccess) {
        this.arrayAccess = arrayAccess;
    }

    /**
     * @return the conditionalBlock
     */
    public boolean isConditionalBlock() {
        return conditionalBlock;
    }

    /**
     * @param conditionalBlock the conditionalBlock to set
     */
    public void setConditionalBlock(boolean conditionalBlock) {
        this.conditionalBlock = conditionalBlock;
    }
}
