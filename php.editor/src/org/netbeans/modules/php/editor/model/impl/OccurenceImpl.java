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
import java.util.List;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.Occurence;

/**
 *
 * @author Radek Matous
 */
class OccurenceImpl implements Occurence {
    private OffsetRange occurenceRange;
    private ModelElement declaration;
    private Collection<? extends ModelElement> allDeclarations;
    private FileScopeImpl fileScope;
    private ModelElement gotDeclaration;


    public OccurenceImpl(Collection<? extends ModelElement> allDeclarations, OffsetRange occurenceRange,FileScopeImpl fileScope) {
        this(allDeclarations, ModelUtils.getFirst(allDeclarations), occurenceRange, fileScope);
    }

    public OccurenceImpl(Collection<? extends ModelElement> allDeclarations, ModelElement declaration, OffsetRange occurenceRange,FileScopeImpl fileScope) {
        this.allDeclarations = allDeclarations;
        this.declaration = declaration;
        //TODO: wrong bugfix when sometimes is offered just one declaration
        if (this.allDeclarations.size() == 1) {
            this.allDeclarations = null;
        }
        this.occurenceRange = occurenceRange;
        this.fileScope = fileScope;
    }

    public OccurenceImpl(ModelElement declaration, OffsetRange occurenceRange, FileScopeImpl fileScope) {
        this.occurenceRange = occurenceRange;
        this.declaration = declaration;
        this.fileScope = fileScope;
    }

    public ModelElement geModelElement() {
        return declaration;
    }

    public OffsetRange getOccurenceRange() {
        return occurenceRange;
    }

    public int getOffset() {
        return getOccurenceRange().getStart();
    }

    @SuppressWarnings("unchecked")
    public Collection<? extends ModelElement> getAllDeclarations() {
        if ((gotDeclaration != null)) {
            return Collections.<ModelElement>emptyList();
        }
        if (allDeclarations == null) {
            allDeclarations = Collections.<ModelElement>emptyList();
            IndexScopeImpl indexScope = (IndexScopeImpl) ModelUtils.getIndexScope(geModelElement());
            switch (geModelElement().getPhpKind()) {
                case CONSTANT:
                    allDeclarations = indexScope.findConstants(geModelElement().getName());
                    break;
                case FUNCTION:
                    allDeclarations = indexScope.findFunctions(geModelElement().getName());
                    break;
                case CLASS:
                    allDeclarations = indexScope.findClasses(geModelElement().getName());
                    break;
                case IFACE:
                    allDeclarations = indexScope.findInterfaces(geModelElement().getName());
                    break;
                case METHOD:
                    allDeclarations = indexScope.findMethods((TypeScopeImpl) geModelElement().getInScope(),
                            geModelElement().getName());
                    break;
                case FIELD:
                    allDeclarations = indexScope.findFields((ClassScopeImpl) geModelElement().getInScope(),
                            geModelElement().getName());
                    break;
                case CLASS_CONSTANT:
                    //TODO: not implemented yet
                case VARIABLE:
                case INCLUDE:
                    allDeclarations = Collections.<ModelElement>singletonList(declaration);
                    break;
                default:
                    throw new UnsupportedOperationException(geModelElement().getPhpKind().toString());
            }
        }
        return this.allDeclarations;
    }

    public List<Occurence> getAllOccurences() {
        return ModelVisitor.getAllOccurences(fileScope,this);
    }

    public ModelElement getDeclaration() {
        return geModelElement();
    }

    public void setGotoDeclaration(ModelElement gotDeclaration) {
        this.gotDeclaration = gotDeclaration;
    }

    public ModelElement gotoDeclaratin() {
        return (gotDeclaration != null) ? gotDeclaration : getDeclaration();
    }
}
