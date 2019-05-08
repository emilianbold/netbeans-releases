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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.completion.csm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.completion.impl.xref.FileReferencesContext;

/**
 *
 */
public class CsmDeclarationResolver {

    /** Creates a new instance of CsmDeclarationResolver */
    private CsmDeclarationResolver() {
    }

    // ==================== help static methods ================================

    public static CsmDeclaration findDeclaration(CsmObject obj) {
        if (obj == null) {
            return null;
        }
        CsmClassifier clazz = null;
        if (CsmKindUtilities.isVariable(obj)) {
            CsmVariable var = (CsmVariable)obj;
            // pass for further handling as type object
            obj = var.getType();
        }
        if (CsmKindUtilities.isType(obj)) {
//            clazz = ((CsmType)obj).getClassifier();
        } else if (CsmKindUtilities.isClassForwardDeclaration(obj)) {
            clazz = ((CsmClassForwardDeclaration)obj).getCsmClass();
        } else if (CsmKindUtilities.isEnumForwardDeclaration(obj)) {
            clazz = ((CsmEnumForwardDeclaration) obj).getCsmEnum();
        } else if (CsmKindUtilities.isClass(obj)) {
            clazz = (CsmClassifier)obj;
        } else if (CsmKindUtilities.isInheritance(obj)) {
            clazz = ((CsmInheritance)obj).getClassifier();
        }

        return clazz;
    }

    public static CsmDeclaration findTopFileDeclaration(CsmFile file, int offset) {
        assert (file != null) : "can't be null file in findTopFileDeclaration";
        CsmFilter filter = CsmSelect.getFilterBuilder().createOffsetFilter(offset);
        for (Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(file, filter); it.hasNext();) {
            CsmDeclaration decl = it.next();
            assert (decl != null) : "can't be null declaration";
            if (isFromIncludedFile(decl, file)) {
                // we support #include directives inside classes and namespace definitions, so
                // checked on can be from other file sometimes
                continue;
            }
            if (CsmOffsetUtilities.isInObject(decl, offset)) {
                // we are inside declaration
                return decl;
            }
        }
        return null;
    }

    public static CsmObject findInnerFileObject(CsmFile file, int offset, CsmContext context, FileReferencesContext fileContext) {
        assert (file != null) : "can't be null file in findTopFileDeclaration";
        // add file scope to context
        CsmContextUtilities.updateContext(file, offset, context);
        CsmObject lastObject = null;
        if (fileContext != null && !fileContext.isCleaned()) {
            fileContext.advance(offset);
            lastObject = fileContext.findInnerFileDeclaration(offset);
            if (lastObject == null) {
                return fileContext.findInnerFileObject(offset);
            } else {
                if (CsmOffsetUtilities.isInObject(lastObject, offset)) {
                    return findInnerDeclaration(file, (CsmDeclaration)lastObject, context, offset);
                }
                // found old invalid object, so clear cache and use not cached algorithm.
                fileContext.advance(offset-1);
            }
        }
        // check file declarations
        CsmFilter filter = CsmSelect.getFilterBuilder().createOffsetFilter(offset);
        Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(file, filter);
        lastObject = findInnerDeclaration(file, it, context, offset);
        // check includes if needed
        if (lastObject == null) {
            Iterator<CsmInclude> it1 = CsmSelect.getIncludes(file, filter);
            lastObject = CsmOffsetUtilities.findObject(it1, context, offset);
        }
        // check macros if needed
        if (lastObject == null) {
            Iterator<CsmMacro> it1 = CsmSelect.getMacros(file, filter);
            lastObject = CsmOffsetUtilities.findObject(it1, context, offset);
        }
        return lastObject;
    }

    private static CsmDeclaration findInnerDeclaration(CsmFile contextFile, final Iterator<? extends CsmDeclaration> it, final CsmContext context, final int offset) {
        CsmDeclaration innerDecl = null;
        if (it != null) {
            // continue till has next and not yet found
            while (it.hasNext()) {
                CsmDeclaration decl = (CsmDeclaration) it.next();
                assert (decl != null) : "can't be null declaration";
                if (isFromIncludedFile(decl, contextFile)) {
                    // we support #include directives inside classes and namespace definitions, so
                    // checked on can be from other file sometimes
                    continue;
                }
                if (CsmOffsetUtilities.isInObject(decl, offset)) {
                    if (!CsmKindUtilities.isFunction(decl) || CsmOffsetUtilities.isInFunctionScope((CsmFunction)decl, offset)) {
                        // add declaration scope to context
                        CsmContextUtilities.updateContext(decl, offset, context);
                        // we are inside declaration, but try to search deeper
                        innerDecl = findInnerDeclaration(contextFile, decl, offset, context);
                    } else {
                        context.setLastObject(decl);
                    }
                    innerDecl = innerDecl != null ? innerDecl : decl;
                }
                if (CsmOffsetUtilities.isBeforeObject(decl, offset)) {
                    // we can break loop, because list of declarations is sorted by offset
                    // and we analyzed all which contain object
                    break;
                }
            }
        }
        return innerDecl;
    }

    private static CsmDeclaration findInnerDeclaration(CsmFile contextFile, CsmDeclaration decl, final CsmContext context, final int offset) {
        CsmDeclaration innerDecl = null;
        assert (decl != null) : "can't be null declaration";
        if (!CsmKindUtilities.isFunction(decl) || CsmOffsetUtilities.isInFunctionScope((CsmFunction)decl, offset)) {
            // add declaration scope to context
            CsmContextUtilities.updateContext(decl, offset, context);
            // we are inside declaration, but try to search deeper
            innerDecl = findInnerDeclaration(contextFile, decl, offset, context);
        } else {
            context.setLastObject(decl);
        }
        innerDecl = innerDecl != null ? innerDecl : decl;
        // we can break loop, because list of declarations is sorted
        // by offset and we found already one of container declaration
        return innerDecl;
    }

    private static class OffsetableComparator<T extends CsmOffsetable> implements Comparator<T> {

        @Override
        public int compare(CsmOffsetable o1, CsmOffsetable o2) {
            int diff = o1.getStartOffset() - o2.getStartOffset();
            if (diff == 0) {
                return o1.getEndOffset() - o2.getEndOffset();
            } else {
                return diff;
            }
        }
    }

    private static final Comparator<CsmOffsetable> OFFSETABLE_COMPARATOR = new OffsetableComparator<CsmOffsetable>();

    // must check before call, that offset is inside outDecl
    private static CsmDeclaration findInnerDeclaration(CsmFile contextFile, CsmDeclaration outDecl, int offset, CsmContext context) {
        assert (CsmOffsetUtilities.isInObject(outDecl, offset)) : "must be in outDecl object!";
        Iterator<? extends CsmDeclaration> it = null;
        if (CsmKindUtilities.isNamespace(outDecl)) {
            CsmNamespace ns = (CsmNamespace)outDecl;
            it = ns.getDeclarations().iterator();
        } else if (CsmKindUtilities.isNamespaceDefinition(outDecl)) {
            it = ((CsmNamespaceDefinition) outDecl).getDeclarations().iterator();
        } else if (CsmKindUtilities.isClass(outDecl)) {
            CsmClass cl  = (CsmClass)outDecl;
            List<CsmOffsetableDeclaration> list = new ArrayList<CsmOffsetableDeclaration>();
            list.addAll(cl.getMembers());
            if (!cl.getFriends().isEmpty()) {
                // combine friends with members for search
                list.addAll(cl.getFriends());
                Collections.sort(list, OFFSETABLE_COMPARATOR);
            }
            it = list.iterator();
        } else if (CsmKindUtilities.isEnum(outDecl)) {
            CsmEnum en = (CsmEnum)outDecl;
            it = en.getEnumerators().iterator();
        } else if (CsmKindUtilities.isTypedef(outDecl) || CsmKindUtilities.isTypeAlias(outDecl)) {
            CsmTypedef td = (CsmTypedef) outDecl;
            if (td.isTypeUnnamed() || td.getName().length() == 0) {
                outDecl = td.getType().getClassifier();
                if (CsmOffsetUtilities.isInObject(outDecl, offset)) {
                    // add declaration scope to context
                    CsmContextUtilities.updateContext(outDecl, offset, context);
                    return findInnerDeclaration(contextFile, outDecl, offset, context);
                }
            }
        }
        return findInnerDeclaration(contextFile, it, context, offset);
    }

    private static boolean isFromIncludedFile(CsmDeclaration decl, CsmFile file) {
        if (CsmKindUtilities.isOffsetable(decl)) {
            if (!file.equals(((CsmOffsetable) decl).getContainingFile())) {
                return true;
            }
        }
        return false;
    }

}
