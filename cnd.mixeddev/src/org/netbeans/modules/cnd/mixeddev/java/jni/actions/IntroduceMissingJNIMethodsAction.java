/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.mixeddev.java.jni.actions;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.mixeddev.MixedDevUtils;
import org.netbeans.modules.cnd.mixeddev.java.JNISupport;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaMethodInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.jni.JNIClass;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 * @author Petr Kudryavtsev <petrk@netbeans.org>
 */
// Temporary disabled
//@ActionID(id = "org.netbeans.modules.cnd.mixeddev.java.jni.actions", category = "MixedDevelopment")
//@ActionRegistration(displayName = "#LBL_Action_IntroduceMissingJNIMethods", lazy = true)
//@ActionReferences(value = {@ActionReference(path = "Editors/text/x-java/Popup/MixedDevelopment", position=20)})
//@NbBundle.Messages({"LBL_Action_IntroduceMissingJNIMethods=Introduce Missing JNI Methods"})
public class IntroduceMissingJNIMethodsAction extends AbstractJNIAction {

    @Override
    public String getName() {
        return NbBundle.getMessage(MixedDevUtils.class, "cnd.mixeddev.implement_missing_jni_methods"); // NOI18N
    }

    @Override
    protected boolean isEnabled(Document doc, int caret) {
        return JNISupport.isJNIClass(doc, caret);
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        Pair<Document, Integer> context = extractContext(activatedNodes);
        if (context != null) {
            final Document doc = context.first();
            final int caret = context.second();
            JNIClass cls = JNISupport.getJNIClass(doc, caret);
            assert cls != null;
            List<CsmOffsetable> found = new ArrayList<>();
            List<JavaMethodInfo> missing = new ArrayList<>();
            for (JavaMethodInfo mtd : cls.getJNIMethods()) {
                CsmOffsetable cppSymbol = MixedDevUtils.findCppSymbol(JNISupport.getCppMethodSignatures(mtd));
                if (cppSymbol != null) {
                    found.add(cppSymbol);
                } else {
                    missing.add(mtd);
                }
            }
            if (found.size() > 0 && missing.size() > 0) {
                CsmOffsetable symbol = found.get(0);
                Pair<CsmScope, CsmScope> scopes = getCppFunctionScopes(symbol);
                if (scopes != null) {
                    CsmScope declScope = scopes.first();
                    CsmScope defScope = scopes.second();
                    if (defScope != null) {
                        
                    } else if (declScope != null) {
                        
                    }
                }
            } else if (found.isEmpty()) {
                // TODO: Show error message
            } else if (missing.isEmpty()) {
                // TODO: Show error message
            }
        }
    }
    
    private Pair<CsmScope, CsmScope> getCppFunctionScopes(CsmOffsetable symbol) {
        CsmFunction declaration = null;
        CsmFunctionDefinition definition = null;
        if (CsmKindUtilities.isFunctionDeclaration(symbol)) {
            declaration = (CsmFunction) symbol;
            definition = declaration.getDefinition();
        } else if (CsmKindUtilities.isFunctionDefinition(symbol)) {
            definition = (CsmFunctionDefinition) symbol;
            declaration = definition.getDeclaration();
        }
        CsmScope declScope = null;
        if (declaration != null) {
            declScope = declaration.getScope();
            if (!CsmKindUtilities.isNamespaceDefinition(declScope)) {
                declScope = declaration.getContainingFile();
            }
        }
        CsmScope defScope = null;
        if (definition != null) {
            defScope = definition.getScope();
            if (!CsmKindUtilities.isNamespaceDefinition(defScope)) {
                defScope = definition.getContainingFile();
            }
        }
        if (declScope != null || defScope != null) {
            return Pair.of(declScope, defScope);
        }
        return null;
    }
}
