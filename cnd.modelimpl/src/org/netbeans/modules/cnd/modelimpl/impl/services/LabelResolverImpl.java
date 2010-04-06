/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmGotoStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmIfStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmLabel;
import org.netbeans.modules.cnd.api.model.deep.CsmLoopStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmSwitchStatement;
import org.netbeans.modules.cnd.api.model.xref.CsmLabelResolver;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceSupport;
import org.openide.util.CharSequences;

/**
 *
 * @author Alexander Simon
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.api.model.xref.CsmLabelResolver.class)
public final class LabelResolverImpl extends CsmLabelResolver {

    public LabelResolverImpl() {
    }

    @Override
    public Collection<CsmReference> getLabels(CsmFunctionDefinition referencedFunction, CharSequence label, Set<LabelKind> kinds) {
        Context res = new Context(referencedFunction, label, kinds);
        if(referencedFunction != null) {
            processInnerStatements(referencedFunction.getBody(), res);
        }
        return res.collection;
    }

    private void processInnerStatements(CsmStatement statement, Context res) {
        if (statement != null) {
            switch (statement.getKind()) {
                case LABEL:
                    res.addLabelDefinition((CsmLabel) statement);
                    break;
                case GOTO:
                    res.addLabelReference((CsmGotoStatement) statement);
                    break;
                case COMPOUND:
                    for (CsmStatement stmt : ((CsmCompoundStatement) statement).getStatements()) {
                        processInnerStatements(stmt, res);
                    }
                    break;
                case WHILE:
                case DO_WHILE:
                case FOR:
                    processInnerStatements(((CsmLoopStatement) statement).getBody(), res);
                    break;
                case IF:
                    processInnerStatements(((CsmIfStatement) statement).getThen(), res);
                    processInnerStatements(((CsmIfStatement) statement).getElse(), res);
                    break;
                case SWITCH:
                    processInnerStatements(((CsmSwitchStatement) statement).getBody(), res);
                    break;
                case CASE:
                case BREAK:
                case DEFAULT:
                case EXPRESSION:
                case CONTINUE:
                case RETURN:
                case DECLARATION:
                case TRY_CATCH:
                case CATCH:
                case THROW:
            }
        }
    }
    
    private static final class Context{
        private final Collection<CsmReference> collection = new ArrayList<CsmReference>();
        private final CharSequence label;
        private final Set<LabelKind> kinds;
//        private CsmFunctionDefinition owner;
        private Context(CsmFunctionDefinition owner, CharSequence label, Set<LabelKind> kinds){
            this.label = label;
            this.kinds = kinds;
//            this.owner = owner;
        }
        private void addLabelDefinition(CsmLabel stmt){
            if (kinds.contains(LabelKind.Definiton)) {
                if (label == null || CharSequences.comparator().compare(label, stmt.getLabel()) == 0){
                    collection.add(CsmReferenceSupport.createObjectReference(stmt));
                }
            }
        }
        private void addLabelReference(CsmGotoStatement stmt){
            if (kinds.contains(LabelKind.Reference)) {
                if (label == null || CharSequences.comparator().compare(label, stmt.getLabel()) == 0){
                    collection.add(CsmReferenceSupport.createObjectReference(stmt));
                }
            }
        }
    }
}
