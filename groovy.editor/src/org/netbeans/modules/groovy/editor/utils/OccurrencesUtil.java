/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.editor.utils;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.groovy.editor.api.ASTUtils;

/**
 *
 * @author Martin Janicek
 */
public class OccurrencesUtil {

    private final BaseDocument doc;
    private final int cursorOffset;

    
    private OccurrencesUtil(BaseDocument doc, int cursorOffset) {
        this.doc = doc;
        this.cursorOffset = cursorOffset;
    }

    public static OccurrencesUtil create(final BaseDocument doc, final int cursorOffset) {
        return new OccurrencesUtil(doc, cursorOffset);
    }


    
    public boolean isCaretOnClassNode(ClassNode superType) {
        if (getClassNodeRange(superType) != OffsetRange.NONE) {
            return true;
        }
        return false;
    }

    private OffsetRange getClassNodeRange(ClassNode superType) {
        int offset = ASTUtils.getOffset(doc, superType.getLineNumber(), superType.getColumnNumber());
        OffsetRange range = ASTUtils.getNextIdentifierByName(doc, superType.getNameWithoutPackage(), offset);
        if (range.containsInclusive(cursorOffset)) {
            return range;
        }
        return OffsetRange.NONE;
    }

    public boolean isCaretOnReturnType(MethodNode method) {
        if (getMethodReturnType(method) != OffsetRange.NONE) {
            return true;
        }
        return false;
    }

    public OffsetRange getMethodReturnType(MethodNode method) {
        int offset = ASTUtils.getOffset(doc, method.getLineNumber(), method.getColumnNumber());
        if (!method.isDynamicReturnType()) {
            OffsetRange range = ASTUtils.getNextIdentifierByName(doc, method.getReturnType().getNameWithoutPackage(), offset);
            if (range.containsInclusive(cursorOffset)) {
                return range;
            }
        }
        return OffsetRange.NONE;
    }

    public boolean isCaretOnFieldType(FieldNode field) {
        if (getFieldRange(field) != OffsetRange.NONE) {
            return true;
        }
        return false;
    }

    private OffsetRange getFieldRange(FieldNode field) {
        int offset = ASTUtils.getOffset(doc, field.getLineNumber(), field.getColumnNumber());
        if (!field.isDynamicTyped()) {
            OffsetRange range = ASTUtils.getNextIdentifierByName(doc, field.getType().getNameWithoutPackage(), offset);
            if (range.containsInclusive(cursorOffset)) {
                return range;
            }
        }
        return OffsetRange.NONE;
    }

    public boolean isCaretOnParamType(Parameter param) {
        if (getParameterRange(param) != OffsetRange.NONE) {
            return true;
        }
        return false;
    }

    private OffsetRange getParameterRange(Parameter param) {
        int offset = ASTUtils.getOffset(doc, param.getLineNumber(), param.getColumnNumber());
        if (!param.isDynamicTyped()) {
            OffsetRange range = ASTUtils.getNextIdentifierByName(doc, param.getType().getNameWithoutPackage(), offset);
            if (range.containsInclusive(cursorOffset)) {
                return range;
            }
        }
        return OffsetRange.NONE;
    }

    public boolean isCaretOnGenericType(ClassNode classNode) {
        GenericsType[] genericsTypes = classNode.getGenericsTypes();
        if (genericsTypes != null && genericsTypes.length > 0) {
            for (GenericsType genericsType : genericsTypes) {
                if (getGenericTypeRange(genericsType) != OffsetRange.NONE) {
                    return true;
                }
            }
        }
        return false;
    }

    public ClassNode getGenericType(ClassNode classNode) {
        GenericsType[] genericsTypes = classNode.getGenericsTypes();
        if (genericsTypes != null && genericsTypes.length > 0) {
            for (GenericsType genericsType : genericsTypes) {
                if (isCaretOnGenericType(genericsType)) {
                    return genericsType.getType();
                }
            }
        }
        return null;
    }

    private boolean isCaretOnGenericType(GenericsType genericsType) {
        if (getGenericTypeRange(genericsType) != OffsetRange.NONE) {
            return true;
        }
        return false;
    }

    private OffsetRange getGenericTypeRange(GenericsType genericType) {
        final int offset = ASTUtils.getOffset(doc, genericType.getLineNumber(), genericType.getColumnNumber());
        final OffsetRange range = ASTUtils.getNextIdentifierByName(doc, genericType.getType().getNameWithoutPackage(), offset);
        if (range.containsInclusive(cursorOffset)) {
            return range;
        }
        return OffsetRange.NONE;
    }
}
