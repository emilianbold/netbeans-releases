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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import org.netbeans.modules.php.editor.model.*;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.model.nodes.FunctionDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;

/**
 *
 * @author Radek Matous
 */
final class FileScope extends ModelScopeImpl implements ModelScope, VariableContainerImpl {

    private CachedModelSupport cachedModelSupport;
    private IndexScopeImpl indexScope;
    private Map<ModelElement, List<Occurence>> occurences =
            new HashMap<ModelElement, List<Occurence>>();
    private List<CodeMarkerImpl> codeMarkers = new ArrayList<CodeMarkerImpl>();

    public VariableNameImpl createElement(Program program, Variable node) {
        VariableNameImpl retval = new VariableNameImpl(this, program, node, true);
        addElement(retval);
        return retval;
    }

    ConstantElementImpl createElement(ASTNodeInfo<Scalar> node) {
        ConstantElementImpl retval = new ConstantElementImpl(this, node);
        addElement(retval);
        return retval;
    }

    FunctionScopeImpl createElement(Program program, FunctionDeclaration node) {
        FunctionScopeImpl retval = new FunctionScopeImpl(this, FunctionDeclarationInfo.create(node),
                VariousUtils.getReturnTypeFromPHPDoc(program, node));
        return retval;
    }

    FileScope(CompilationInfo info) {
        super(info, "program", PhpKind.PROGRAM);//NOI18N
        indexScope = (IndexScopeImpl) ModelVisitor.getIndexScope(info);
        cachedModelSupport = new CachedModelSupport(this);
    }

    void addCodeMarker(CodeMarkerImpl codeMarkerImpl) {
        codeMarkers.add(codeMarkerImpl);
    }

    void addOccurence(Occurence occurence) {
        final ModelElement declaration = occurence.getDeclaration();
        addOccurence(declaration, occurence);
    }

    void addOccurence(final ModelElement declaration, Occurence occurence) {
        List<Occurence> ocList = occurences.get(declaration);
        if (ocList == null) {
            ocList = new ArrayList<Occurence>();
            List<Occurence> old = occurences.put(declaration, ocList);
            assert old == null;
        }
        assert occurence != null;
        ocList.add(occurence);
    }

    List<? extends CodeMarker> getMarkers() {
        return codeMarkers;
    }

    /**
     * @return the occurences
     */
    List<Occurence> getOccurences() {
        List<Occurence> ocList = new ArrayList<Occurence>();
        Collection<List<Occurence>> values = occurences.values();
        for (List<Occurence> list : values) {
            ocList.addAll(list);
        }
        return ocList;
    }

    List<Occurence> getAllOccurences(ModelElement declaration) {
        final List<Occurence> retval = occurences.get(declaration);
        return retval != null ? retval : Collections.<Occurence>emptyList();
    }

    List<Occurence> getAllOccurences(Occurence occurence) {
        return getAllOccurences(occurence.getDeclaration());
    }

    public List<? extends ClassScopeImpl> getAllClasses() {
        return getClasses();
    }

    public List<? extends ClassScopeImpl> getClasses(final String... queryName) {
        return getClasses(NameKind.EXACT_NAME, queryName);
    }

    public List<? extends ClassScopeImpl> getClasses(final NameKind nameKind, final String... queryName) {
        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.CLASS) &&
                        (queryName.length == 0 || nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }

    public List<? extends InterfaceScopeImpl> getAllInterfaces() {
        return getInterfaces();
    }

    public List<? extends InterfaceScopeImpl> getInterfaces(final String... queryName) {
        return getInterfaces(NameKind.EXACT_NAME, queryName);
    }

    public List<? extends InterfaceScopeImpl> getInterfaces(final NameKind nameKind, final String... queryName) {
        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.IFACE) &&
                        (queryName.length == 0 || nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }

    public List<? extends ConstantElementImpl> getAllConstants() {
        return getConstants();
    }

    public List<? extends ConstantElementImpl> getConstants(String... queryName) {
        return getConstants(NameKind.EXACT_NAME, queryName);
    }

    public List<? extends ConstantElementImpl> getConstants(final NameKind nameKind, final String... queryName) {
        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.CONSTANT) &&
                        (queryName.length == 0 || nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }

    public List<? extends FunctionScopeImpl> getAllFunctions() {
        return getFunctions();
    }

    public List<? extends FunctionScopeImpl> getFunctions(final String... queryName) {
        return getFunctions(NameKind.EXACT_NAME, queryName);
    }

    public List<? extends FunctionScopeImpl> getFunctions(final NameKind nameKind, final String... queryName) {
        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.FUNCTION) &&
                        (queryName.length == 0 || nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }

    @SuppressWarnings("unchecked")
    public List<? extends TypeScopeImpl> getAllTypes() {
        List<? extends ClassScopeImpl> classes = getAllClasses();
        List<? extends InterfaceScopeImpl> interfaces = getAllInterfaces();
        return ModelUtils.merge(classes, interfaces);
    }

    public List<? extends TypeScopeImpl> getTypes(String... queryName) {
        return getTypes(NameKind.EXACT_NAME, queryName);
    }

    public List<? extends TypeScopeImpl> getTypes(final NameKind nameKind, final String... queryName) {
        return filter(getAllTypes(), new ElementFilter() {

            public boolean isAccepted(ModelElementImpl element) {
                return (queryName.length == 0 || nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }

    public List<? extends VariableName> getAllVariables() {
        return getVariablesImpl();
    }

    public List<? extends VariableName> getVariables(String... queryName) {
        return getVariablesImpl(queryName);
    }

    public List<? extends VariableName> getVariables(final NameKind nameKind, final String... queryName) {
        return getVariablesImpl(nameKind, queryName);
    }

    public List<? extends VariableNameImpl> getAllVariablesImpl() {
        return getVariablesImpl();
    }

    public List<? extends VariableNameImpl> getVariablesImpl(String... queryName) {
        return getVariablesImpl(NameKind.EXACT_NAME, queryName);
    }

    public List<? extends VariableNameImpl> getVariablesImpl(final NameKind nameKind, final String... queryName) {
        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.VARIABLE) &&
                        (queryName.length == 0 || nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }

    /**
     * @return the indexScope
     */
    IndexScopeImpl getIndexScope() {
        return indexScope;
    }

    @Override
    CachedModelSupport getCachedModelSupport() {
        return cachedModelSupport;
    }
}
