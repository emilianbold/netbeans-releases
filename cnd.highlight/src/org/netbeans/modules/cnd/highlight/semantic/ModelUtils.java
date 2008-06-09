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
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;

/**
 *
 * @author Sergey Grinev
 */
public class ModelUtils {

    private interface Validator {

        boolean validate(CsmReference ref);
    }

    private static List<CsmReference> getBlocksFromReferences(CsmFile file, final Validator validator) {
        final List<CsmReference> out = new ArrayList<CsmReference>();
        CsmFileReferences.getDefault().accept(file,
                new CsmFileReferences.Visitor() {

                    public void visit(CsmReference ref) {
                        if (validator.validate(ref)) {
                            out.add(ref);
                        }
                    }
                });
        return out;
    }

    // Data Providers
    /*package*/ static List<? extends CsmOffsetable> getFieldsBlocks(CsmFile file) {
        return getBlocksFromReferences(file, new Validator() {

            public boolean validate(CsmReference ref) {
                CsmObject obj = ref.getReferencedObject();
                return obj != null && CsmKindUtilities.isField(obj);
            }
        });
    }

    /*package*/ static List<CsmReference> getFunctionNames(final CsmFile csmFile) {
        return getBlocksFromReferences(csmFile, new Validator() {

            public boolean validate(CsmReference ref) {
                CsmObject csmObject = ref.getReferencedObject();
                if (CsmKindUtilities.isFunctionDeclaration(csmObject)) {
                    // check if we are in the function declaration
                    CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) csmObject;
                    if (decl.getContainingFile().equals(csmFile) &&
                            decl.getStartOffset() <= ref.getStartOffset() &&
                            decl.getEndOffset() >= ref.getEndOffset()) {
                        return true;
                    }
                    // check if we are in function definition name => go to declaration
                    // else it is more useful to jump to definition of function
                    CsmFunctionDefinition definition = ((CsmFunction) csmObject).getDefinition();
                    if (definition != null) {
                        if (csmFile.equals(definition.getContainingFile()) &&
                                definition.getStartOffset() <= ref.getStartOffset() &&
                                ref.getStartOffset() <= definition.getBody().getStartOffset()) {
                            // it is ok to jump to declaration
                            return true;
                        }
                    }
                } else if (CsmKindUtilities.isFunctionDefinition(csmObject)) {
                    CsmFunctionDefinition definition = (CsmFunctionDefinition) csmObject;
                    if (csmFile.equals(definition.getContainingFile()) &&
                            definition.getStartOffset() <= ref.getStartOffset() &&
                            ref.getStartOffset() <= definition.getBody().getStartOffset()) {
                        // it is ok to jump to declaration
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /*package*/ static List<CsmOffsetable> getInactiveCodeBlocks(CsmFile file) {
        return CsmFileInfoQuery.getDefault().getUnusedCodeBlocks(file);
    }

    /*package*/ static List<CsmReference> getMacroBlocks(CsmFile file) {
        return CsmFileInfoQuery.getDefault().getMacroUsages(file);
    }

    /*package*/ static List<? extends CsmOffsetable> getTypedefBlocks(CsmFile file) {
        return getBlocksFromReferences(file, new Validator() {

            public boolean validate(CsmReference ref) {
                CsmObject obj = ref.getReferencedObject();
                return obj != null && CsmKindUtilities.isTypedef(obj);
            }
        });
    }
}
