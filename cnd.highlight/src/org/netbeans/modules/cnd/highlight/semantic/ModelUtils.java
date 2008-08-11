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
package org.netbeans.modules.cnd.highlight.semantic;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;

/**
 *
 * @author Sergey Grinev
 */
public class ModelUtils {

    /*package*/ static List<? extends CsmOffsetable> collect(final CsmFile csmFile, final ReferenceCollector collector) {
        CsmFileReferences.getDefault().accept(csmFile, new CsmFileReferences.Visitor() {
                public void visit(CsmReference ref, CsmReference prev, CsmReference parent) {
                    collector.visit(ref, csmFile);
                }
        });
        return collector.getBlocks();
    }

    /*package*/ static List<CsmOffsetable> getInactiveCodeBlocks(CsmFile file) {
        return CsmFileInfoQuery.getDefault().getUnusedCodeBlocks(file);
    }

    /*package*/ static List<CsmReference> getMacroBlocks(CsmFile file) {
        return CsmFileInfoQuery.getDefault().getMacroUsages(file);
    }

    private static abstract class AbstractReferenceCollector implements ReferenceCollector {
        protected final List<CsmReference> list;
        public AbstractReferenceCollector() {
            list = new ArrayList<CsmReference>();
        }
        public void clear() {
            list.clear();
        }
        public List<? extends CsmOffsetable> getBlocks() {
            return list;
        }
    }

    /*package*/ static class FieldReferenceCollector extends AbstractReferenceCollector {
        public String getEntityName() {
            return "class-fields"; // NOI18N
        }
        public void visit(CsmReference ref, CsmFile file) {
            CsmObject obj = ref.getReferencedObject();
            if (CsmKindUtilities.isField(obj)) {
                list.add(ref);
            }
        }
    }

    /*package*/ static class TypedefReferenceCollector extends AbstractReferenceCollector {
        public String getEntityName() {
            return "typedefs"; // NOI18N
        }
        public void visit(CsmReference ref, CsmFile file) {
            CsmObject obj = ref.getReferencedObject();
            if (CsmKindUtilities.isTypedef(obj)) {
                list.add(ref);
            }
        }
    }

    /*package*/ static class FunctionReferenceCollector extends AbstractReferenceCollector {
        public String getEntityName() {
            return "functions-names"; // NOI18N
        }
        public void visit(CsmReference ref, CsmFile file) {
            if (isWanted(ref, file)) {
                list.add(ref);
            }
        }
        private boolean isWanted(CsmReference ref, CsmFile file) {
            CsmObject csmObject = ref.getReferencedObject();
            if (CsmKindUtilities.isFunctionDeclaration(csmObject)) {
                // check if we are in the function declaration
                CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) csmObject;
                if (decl.getContainingFile().equals(file) &&
                        decl.getStartOffset() <= ref.getStartOffset() &&
                        decl.getEndOffset() >= ref.getEndOffset()) {
                    return true;
                }
                // check if we are in function definition name => go to declaration
                // else it is more useful to jump to definition of function
                CsmFunctionDefinition definition = ((CsmFunction) csmObject).getDefinition();
                if (definition != null) {
                    if (file.equals(definition.getContainingFile()) &&
                            definition.getStartOffset() <= ref.getStartOffset() &&
                            ref.getStartOffset() <= definition.getBody().getStartOffset()) {
                        // it is ok to jump to declaration
                        return true;
                    }
                }
            } else if (CsmKindUtilities.isFunctionDefinition(csmObject)) {
                CsmFunctionDefinition definition = (CsmFunctionDefinition) csmObject;
                if (file.equals(definition.getContainingFile()) &&
                        definition.getStartOffset() <= ref.getStartOffset() &&
                        ref.getStartOffset() <= definition.getBody().getStartOffset()) {
                    // it is ok to jump to declaration
                    return true;
                }
            }
            return false;
        }
    }

    /*package*/ static class UnusedVariableCollector implements ReferenceCollector {
        private final Set<VariableInfo> set;
        public UnusedVariableCollector() {
            set = new LinkedHashSet<VariableInfo>();
        }
        public String getEntityName() {
            return "unused-variables"; // NOI18N
        }
        public void clear() {
            set.clear();
        }
        public void visit(CsmReference ref, CsmFile file) {
            CsmObject obj = ref.getReferencedObject();
            if (CsmKindUtilities.isLocalVariable(obj) && !CsmKindUtilities.isParameter(obj)) {
                CsmVariable var = (CsmVariable)obj;
                if (var.getContainingFile().equals(file)) {
                    VariableInfo info = new VariableInfo(var);
                    if (set.remove(info)) {
                        info.setUsed(true);
                    }
                    set.add(info);
                }
            }
        }
        public List<? extends CsmOffsetable> getBlocks() {
            List<VariableInfo> result = new ArrayList<VariableInfo>();
            for (VariableInfo info : set) {
                if (!info.isUsed()) {
                    result.add(info);
                }
            }
            return result;
        }
    }

    private static class VariableInfo implements CsmOffsetable {

        private final int startOffset;
        private final int endOffset;
        private boolean used;

        public VariableInfo(CsmVariable variable) {
            this.startOffset = variable.getStartOffset();
            this.endOffset = variable.getEndOffset();
            this.used = false;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

        public Position getStartPosition() {
            throw new UnsupportedOperationException("Not implemented"); // NOI18N
        }

        public Position getEndPosition() {
            throw new UnsupportedOperationException("Not implemented"); // NOI18N
        }

        public CsmFile getContainingFile() {
            throw new UnsupportedOperationException("Not implemented"); // NOI18N
        }

        public CharSequence getText() {
            throw new UnsupportedOperationException("Not implemented"); // NOI18N
        }

        public boolean isUsed() {
            return used;
        }

        public void setUsed(boolean used) {
            this.used = used;
        }

        @Override
        public int hashCode() {
            // all variables come from the same file,
            // so we don't compare containingFile
            return 7 * getStartOffset() + 13 * getEndOffset();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof VariableInfo) {
                VariableInfo that = (VariableInfo)obj;
                // all variables come from the same file,
                // so we don't compare containingFile
                return getStartOffset() == that.getStartOffset()
                        && getEndOffset() == that.getEndOffset();
            } else {
                return false;
            }
        }

    }

}
