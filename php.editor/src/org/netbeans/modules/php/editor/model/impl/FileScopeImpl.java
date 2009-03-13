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
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.model.nodes.FunctionDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

/**
 *
 * @author Radek Matous
 */
final class FileScopeImpl extends ScopeImpl implements FileScope, VariableContainerImpl {

    private CachingSupport cachedModelSupport;
    private ParserResult info;
    private Map<ModelElement, List<Occurence>> occurences =
            new HashMap<ModelElement, List<Occurence>>();
    private List<CodeMarkerImpl> codeMarkers = new ArrayList<CodeMarkerImpl>();


    public VariableNameImpl createElement(Program program, Variable node) {
        VariableNameImpl retval = new VariableNameImpl(this, program, node, true);
        return retval;
    }

    ConstantElementImpl createElement(ASTNodeInfo<Scalar> node) {
        ConstantElementImpl retval = new ConstantElementImpl(this, node);
        return retval;
    }

    FunctionScopeImpl createElement(Program program, FunctionDeclaration node) {
        FunctionScopeImpl retval = new FunctionScopeImpl(this, FunctionDeclarationInfo.create(node),
                VariousUtils.getReturnTypeFromPHPDoc(program, node));
        return retval;
    }

    FileScopeImpl(ParserResult info) {
        this(info, "program", PhpKind.PROGRAM);//NOI18N
        cachedModelSupport = new CachingSupport(this);
    }

    private FileScopeImpl(ParserResult info, String name, PhpKind kind) {
        super(null, name, Union2.<String, FileObject>createSecond(info != null ? info.getSnapshot().getSource().getFileObject() : null), new OffsetRange(0, 0), kind);//NOI18N
        this.info = info;
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

    public Collection<? extends ClassScopeImpl> getDeclaredClasses() {
        return filter(getElements(), new ElementFilter<ModelElement>() {
            public boolean isAccepted(ModelElement element) {
                return element.getPhpKind().equals(PhpKind.CLASS);
            }
        });
    }

    /*public List<? extends ClassScopeImpl> findDeclaredClasses(final String... queryName) {
        return findDeclaredClasses(QuerySupport.Kind.EXACT_NAME, queryName);
    }

    public List<? extends ClassScopeImpl> findDeclaredClasses(final QuerySupport.Kind nameKind, final String... queryName) {
        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.CLASS) &&
                        (queryName.length == 0 || nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }*/

    public Collection<? extends InterfaceScope> getDeclaredInterfaces() {
        return filter(getElements(), new ElementFilter() {
            public boolean isAccepted(ModelElement element) {
                return element.getPhpKind().equals(PhpKind.IFACE);
            }
        });
    }

    /*public List<? extends InterfaceScopeImpl> findDeclaredInterfaces(final String... queryName) {
        return findDeclaredInterfaces(QuerySupport.Kind.EXACT_NAME, queryName);
    }

    public List<? extends InterfaceScopeImpl> findDeclaredInterfaces(final QuerySupport.Kind nameKind, final String... queryName) {
        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.IFACE) &&
                        (queryName.length == 0 || nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }*/

    public Collection<? extends ConstantElement> getDeclaredConstants() {
        return filter(getElements(), new ElementFilter() {
            public boolean isAccepted(ModelElement element) {
                return element.getPhpKind().equals(PhpKind.CONSTANT);
            }
        });
    }

    /*public List<? extends ConstantElementImpl> findDeclaredConstants(String... queryName) {
        return findDeclaredConstants(QuerySupport.Kind.EXACT_NAME, queryName);
    }

    public List<? extends ConstantElementImpl> findDeclaredConstants(final QuerySupport.Kind nameKind, final String... queryName) {
        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.CONSTANT) &&
                        (queryName.length == 0 || nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }*/

    public Collection<? extends FunctionScope> getDeclaredFunctions() {
        return filter(getElements(), new ElementFilter() {
            public boolean isAccepted(ModelElement element) {
                return element.getPhpKind().equals(PhpKind.FUNCTION);
            }
        });
    }


    @SuppressWarnings("unchecked")
    public Collection<? extends TypeScope> getDeclaredTypes() {
        Collection<? extends ClassScope> classes = getDeclaredClasses();
        Collection<? extends InterfaceScope> interfaces = getDeclaredInterfaces();
        return ModelUtils.merge(classes, interfaces);
    }


    public Collection<? extends VariableName> getDeclaredVariables() {
        return getVariablesImpl();
    }


    public Collection<? extends VariableName> getAllVariablesImpl() {
        return getVariablesImpl();
    }

    public Collection<? extends VariableName> getVariablesImpl(String... queryName) {
        return getVariablesImpl(QuerySupport.Kind.EXACT, queryName);
    }

    public Collection<? extends VariableName> getVariablesImpl(final QuerySupport.Kind nameKind, final String... queryName) {
        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElement element) {
                return element.getPhpKind().equals(PhpKind.VARIABLE) &&
                        (queryName.length == 0 || nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }

    /**
     * @return the indexScope
     */
    public IndexScope getIndexScope() {
        return ModelVisitor.getIndexScope(info);
    }

    @NonNull
    public CachingSupport getCachingSupport() {
        return cachedModelSupport;
    }
}
