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
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.NameKind.Exact;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.ConstantElement;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.FieldElement;
import org.netbeans.modules.php.editor.api.elements.FunctionElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.model.IndexScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.Occurence;

/**
 *
 * @author Radek Matous
 */
class OccurenceImpl implements Occurence {
    private OffsetRange occurenceRange;
    private PhpElement declaration;
    private Collection<? extends PhpElement> allDeclarations;
    private FileScopeImpl fileScope;
    private PhpElement gotDeclaration;


    public OccurenceImpl(Collection<? extends PhpElement> allDeclarations, OffsetRange occurenceRange,FileScopeImpl fileScope) {
        this(allDeclarations, ModelUtils.getFirst(allDeclarations), occurenceRange, fileScope);
    }

    public OccurenceImpl(Collection<? extends PhpElement> allDeclarations, PhpElement declaration, OffsetRange occurenceRange,FileScopeImpl fileScope) {
        if ((declaration instanceof MethodScope) && ((MethodScope)declaration).isConstructor()) {
            ModelElement modelElement = (ModelElement) declaration;
            this.declaration = modelElement.getInScope();
            setGotoDeclaration(modelElement);
        } else {
            this.allDeclarations = allDeclarations;
            this.declaration = declaration;
        }
        this.occurenceRange = occurenceRange;
        this.fileScope = fileScope;
    }

    public OccurenceImpl(PhpElement declaration, OffsetRange occurenceRange, FileScopeImpl fileScope) {
        this.occurenceRange = occurenceRange;
        this.declaration = declaration;
        this.fileScope = fileScope;
    }

    public PhpElement geElement() {
        return declaration;
    }

    public OffsetRange getOccurenceRange() {
        return occurenceRange;
    }

    public int getOffset() {
        return getOccurenceRange().getStart();
    }

    @SuppressWarnings("unchecked")
    public Collection<? extends PhpElement> getAllDeclarations() {
        if ((gotDeclaration != null)) {
            return Collections.<ModelElement>emptyList();
        }
        if (allDeclarations == null) {
            allDeclarations = Collections.<ModelElement>emptyList();
            final PhpElement element = geElement();
            ElementQuery elementQuery = element.getElementQuery();
            switch (element.getPhpElementKind()) {
                case CONSTANT:
                    if (element instanceof ModelElement) {
                        ModelElement modelElement = (ModelElement) element;
                        IndexScope indexScope = ModelUtils.getIndexScope(modelElement);
                        allDeclarations = indexScope.findConstants(modelElement.getName());
                    } else {
                        ConstantElement constant = (ConstantElement) element;
                        allDeclarations = elementQuery.getConstants(NameKind.exact(constant.getFullyQualifiedName()));
                    }
                    break;
                case FUNCTION:
                    if (element instanceof ModelElement) {
                        ModelElement modelElement = (ModelElement) element;
                        IndexScope indexScope = ModelUtils.getIndexScope(modelElement);
                        allDeclarations = indexScope.findFunctions(modelElement.getName());
                    } else {
                        FunctionElement functions = (FunctionElement) element;
                        allDeclarations = elementQuery.getFunctions(NameKind.exact(functions.getFullyQualifiedName()));
                    }
                    break;
                case CLASS:
                    if (element instanceof ModelElement) {
                        ModelElement modelElement = (ModelElement) element;
                        IndexScope indexScope = ModelUtils.getIndexScope(modelElement);
                        allDeclarations = indexScope.findClasses(modelElement.getName());
                    } else {
                        ClassElement classes = (ClassElement) element;
                        allDeclarations = elementQuery.getClasses(NameKind.exact(classes.getFullyQualifiedName()));
                    }
                    break;
                case IFACE:
                    if (element instanceof ModelElement) {
                        ModelElement modelElement = (ModelElement) element;
                        IndexScope indexScope = ModelUtils.getIndexScope(modelElement);
                        allDeclarations = indexScope.findInterfaces(modelElement.getName());
                    } else {
                        InterfaceElement ifaces = (InterfaceElement) element;
                        allDeclarations = elementQuery.getInterfaces(NameKind.exact(ifaces.getFullyQualifiedName()));
                    }
                    break;
                case METHOD:
                    if (element instanceof ModelElement) {
                        ModelElement modelElement = (ModelElement) element;
                        IndexScope indexScope = ModelUtils.getIndexScope(modelElement);
                        allDeclarations = indexScope.findMethods((TypeScopeImpl) modelElement.getInScope(),
                                modelElement.getName());
                    } else {
                        MethodElement methods = (MethodElement) element;
                        if (elementQuery.getQueryScope().isIndexScope()) {
                            ElementQuery.Index index = (Index) elementQuery;
                            Exact methodName = NameKind.exact(methods.getName());
                            allDeclarations = ElementFilter.forName(methodName).filter(index.getAllMethods(methods.getType()));
                        } else {
                            assert false;
                        }
                    }

                    break;
                case FIELD:
                    if (element instanceof ModelElement) {
                        ModelElement modelElement = (ModelElement) element;
                        IndexScope indexScope = ModelUtils.getIndexScope(modelElement);
                        allDeclarations = indexScope.findFields((ClassScopeImpl) modelElement.getInScope(),
                            geElement().getName());
                    } else {
                        FieldElement field = (FieldElement) element;
                        if (elementQuery.getQueryScope().isIndexScope()) {
                            ElementQuery.Index index = (Index) elementQuery;
                            Exact fieldName = NameKind.exact(field.getName());
                            final TypeElement type = field.getType();
                            allDeclarations = ElementFilter.forName(fieldName).filter(index.getAlllFields(type));
                        } else {
                            assert false;
                        }
                    }

                    break;
                case TYPE_CONSTANT:
                    //TODO: not implemented yet
                case VARIABLE:
                case INCLUDE:
                    allDeclarations = Collections.<PhpElement>singletonList(declaration);
                    break;
                default:
                    throw new UnsupportedOperationException(geElement().getPhpElementKind().toString());
            }
        }
        return this.allDeclarations;
    }

    public List<Occurence> getAllOccurences() {
        return ModelVisitor.getAllOccurences(fileScope,this);
    }

    public PhpElement getDeclaration() {
        return geElement();
    }

    public void setGotoDeclaration(ModelElement gotDeclaration) {
        this.gotDeclaration = gotDeclaration;
    }

    public PhpElement gotoDeclaratin() {
        return (gotDeclaration != null) ? gotDeclaration : getDeclaration();
    }

    public boolean gotoDeclarationEnabled() {
        return true;
    }
}
