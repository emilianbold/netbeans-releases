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
package org.netbeans.modules.php.editor.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.NamespaceDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.openide.filesystems.FileObject;

/**
 * @author Radek Matous
 */
public class ModelUtils {

    private ModelUtils() {
    }

    public static NamespaceScope getNamespaceScope(NamespaceDeclaration currenNamespace, FileScope fileScope) {
        NamespaceDeclarationInfo ndi = currenNamespace != null ? NamespaceDeclarationInfo.create(currenNamespace) : null;
        NamespaceScope currentScope = ndi != null ? ModelUtils.getFirst(ModelUtils.filter(fileScope.getDeclaredNamespaces(), ndi.getName())) : fileScope.getDefaultDeclaredNamespace();
        return currentScope;
    }

    public static Collection<? extends TypeScope> getDeclaredTypes(FileScope fileScope) {
        List<TypeScope> retval = new ArrayList<TypeScope>();
        Collection<? extends NamespaceScope> declaredNamespaces = fileScope.getDeclaredNamespaces();
        for (NamespaceScope namespace : declaredNamespaces) {
            retval.addAll(namespace.getDeclaredTypes());
        }
        return retval;
    }

    public static Collection<? extends ClassScope> getDeclaredClasses(FileScope fileScope) {
        List<ClassScope> retval = new ArrayList<ClassScope>();
        Collection<? extends NamespaceScope> declaredNamespaces = fileScope.getDeclaredNamespaces();
        for (NamespaceScope namespace : declaredNamespaces) {
            retval.addAll(namespace.getDeclaredClasses());
        }
        return retval;
    }

    public static Collection<? extends InterfaceScope> getDeclaredInterfaces(FileScope fileScope) {
        List<InterfaceScope> retval = new ArrayList<InterfaceScope>();
        Collection<? extends NamespaceScope> declaredNamespaces = fileScope.getDeclaredNamespaces();
        for (NamespaceScope namespace : declaredNamespaces) {
            retval.addAll(namespace.getDeclaredInterfaces());
        }
        return retval;
    }

    public static Collection<? extends ConstantElement> getDeclaredConstants(FileScope fileScope) {
        List<ConstantElement> retval = new ArrayList<ConstantElement>();
        Collection<? extends NamespaceScope> declaredNamespaces = fileScope.getDeclaredNamespaces();
        for (NamespaceScope namespace : declaredNamespaces) {
            retval.addAll(namespace.getDeclaredConstants());
        }
        return retval;
    }

    public static Collection<? extends FunctionScope> getDeclaredFunctions(FileScope fileScope) {
        List<FunctionScope> retval = new ArrayList<FunctionScope>();
        Collection<? extends NamespaceScope> declaredNamespaces = fileScope.getDeclaredNamespaces();
        for (NamespaceScope namespace : declaredNamespaces) {
            retval.addAll(namespace.getDeclaredFunctions());
        }
        return retval;
    }

    public static Collection<? extends VariableName> getDeclaredVariables(FileScope fileScope) {
        List<VariableName> retval = new ArrayList<VariableName>();
        Collection<? extends NamespaceScope> declaredNamespaces = fileScope.getDeclaredNamespaces();
        for (NamespaceScope namespace : declaredNamespaces) {
            retval.addAll(namespace.getDeclaredVariables());
        }
        return retval;
    }

    public static List<? extends ModelElement> getElements(Scope scope, boolean resursively) {
        List<ModelElement> retval = new ArrayList<ModelElement>();
        List<? extends ModelElement> elements = scope.getElements();
        retval.addAll(elements);
        for (ModelElement modelElement : elements) {
            if (modelElement instanceof Scope) {
                retval.addAll(getElements((Scope) modelElement, resursively));
            }
        }
        return retval;
    }

    @NonNull
    public static Collection<? extends TypeScope> resolveType(Model model, VariableBase varBase) {
        Collection<? extends TypeScope> retval = Collections.emptyList();
        VariableScope scp = model.getVariableScope(varBase.getStartOffset());
        if (scp != null) {
            String vartype = VariousUtils.extractTypeFroVariableBase(varBase);
            if (vartype != null) {
                retval = VariousUtils.getType(scp, vartype, varBase.getStartOffset(), true);
            }
        }
        return retval;
    }

    @NonNull
    public static Collection<? extends TypeScope> resolveTypeAfterReferenceToken(Model model, TokenSequence<PHPTokenId> tokenSequence, int offset) {
        tokenSequence.move(offset);
        Collection<? extends TypeScope> retval = Collections.emptyList();
        VariableScope scp = model.getVariableScope(offset);
        if (scp != null) {
                String semiType = VariousUtils.getSemiType(tokenSequence, VariousUtils.State.START, scp);
                if (semiType != null) {
                    return VariousUtils.getType( scp, semiType, offset, true);
                }

        }
        return retval;
    }

    @CheckForNull
    public static <T> T getFirst(Collection<? extends T> all) {
        if (all instanceof List) {
            return all.size() > 0 ? ((List<T>)all).get(0) : null;
        }
        return all.size() > 0 ? all.iterator().next() : null;
    }

    @CheckForNull
    public static <T extends Occurence> T getFirst(Collection<? extends T> all) {
        if (all instanceof List) {
            return all.size() > 0 ? ((List<T>)all).get(0) : null;
        }
        return all.size() > 0 ? all.iterator().next() : null;
    }

    @CheckForNull
    public static <T extends ModelElement> T getLast(List<? extends T> all) {
        return all.size() > 0 ? all.get(all.size()-1) : null;
    }

    @NonNull
    public static <T extends ModelElement> List<? extends T> filter(Collection<T> allElements,
            final QuerySupport.Kind nameKind, final QualifiedName qualifiedName) {
        final QualifiedNameKind kind = qualifiedName.getKind();
        final String name = qualifiedName.toName().toString();
        final String namespaceName = qualifiedName.toNamespaceName().toString();
        return filter(allElements, new ElementFilter<T>() {
            public boolean isAccepted(T element) {
                if (nameKindMatch(element.getName(), nameKind, name)) {
                    switch(kind) {
                        case QUALIFIED:
                            //TODO: not implemented yet behaves like UNQUALIFIED for now
                        case UNQUALIFIED:
                            return true;
                        case FULLYQUALIFIED:
                            return nameKindMatch(element.getNamespaceName().toString(), nameKind, namespaceName);
                    }
                }
                return false;
            }
        });
    }

    @NonNull
    public static <T extends ModelElement> List<? extends T> filter(Collection<T> allElements,
            final String... elementName) {
        return filter(allElements, QuerySupport.Kind.EXACT, elementName);
    }

    @NonNull
    public static <T extends ModelElement> List<? extends T> filter(Collection<T> allElements,
            final QuerySupport.Kind nameKind, final String... elementName) {
        return filter(allElements, new ElementFilter<T>() {
            public boolean isAccepted(T element) {
                return (elementName.length == 0 || nameKindMatch(element.getName(), nameKind, elementName));
            }
        });
    }

    @NonNull
    public static <T extends ModelElement> List<? extends T> filter(Collection<? extends T> allElements,
            FileObject fileObject) {
        List<T> retval = new ArrayList<T>();
        for (T element : allElements) {
            if (element.getFileObject() == fileObject) {
                retval.add(element);
            }
        }
        return retval;
    }
    @NonNull
    public static <T extends ModelElement> T getFirst(Collection<T> allElements,
            final String... elementName) {
        return getFirst(filter(allElements, QuerySupport.Kind.EXACT, elementName));
    }

    @NonNull
    public static <T extends ModelElement> T getFirst(Collection<T> allElements,
            final QuerySupport.Kind nameKind, final String... elementName) {
        return getFirst(filter(allElements, new ElementFilter<T>() {
            public boolean isAccepted(T element) {
                return (elementName.length == 0 || nameKindMatch(element.getName(), nameKind, elementName));
            }
        }));
    }

    @NonNull
    public static <T extends ModelElement> T getFirst(Collection<? extends T> allElements,
            FileObject fileObject) {
        List<T> retval = new ArrayList<T>();
        for (T element : allElements) {
            if (element.getFileObject() == fileObject) {
                retval.add(element);
            }
        }
        return getFirst(retval);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static <T extends ModelElement> Collection<? extends T> merge(Collection<? extends T>... all) {
        List<T> retval = new ArrayList<T>();
        for (Collection<? extends T> list : all) {
            retval.addAll(list);
        }
        return retval;
    }


    //TODO: put it directly to ModelElement
    @CheckForNull
    public static FileScope getFileScope(ModelElement element) {
        FileScope retval = (element instanceof FileScope) ? (FileScope)element : null;
        while (retval == null && element != null) {
            element = element.getInScope();
            retval = (FileScope) ((element instanceof FileScope) ? element : null);
        }
        return retval;
    }

    @CheckForNull
    public static NamespaceScope getNamespaceScope(ModelElement element) {
        NamespaceScope retval = (element instanceof NamespaceScope) ? (NamespaceScope)element : null;
        while (retval == null && element != null) {
            element = element.getInScope();
            retval = (NamespaceScope) ((element instanceof NamespaceScope) ? element : null);
        }
        return retval;
    }

    @CheckForNull
    public static TypeScope getTypeScope(ModelElement element) {
        TypeScope retval = (element instanceof TypeScope) ? (TypeScope)element : null;
        while (retval == null && element != null) {
            element = element.getInScope();
            retval = (TypeScope) ((element instanceof TypeScope) ? element : null);
        }
        return retval;
    }
    @CheckForNull
    public static ClassScope getClassScope(ModelElement element) {
        ClassScope retval = (element instanceof ClassScope) ? (ClassScope)element : null;
        while (retval == null && element != null) {
            element = element.getInScope();
            retval = (ClassScope) ((element instanceof ClassScope) ? element : null);
        }
        return retval;
    }
    @NonNull
    public static IndexScope getIndexScope(ModelElement element) {
        IndexScope retval = (element instanceof IndexScope) ? (IndexScope)element : null;
        ModelElement tmpElement = element;
        while (retval == null && tmpElement != null) {
            tmpElement = tmpElement.getInScope();
            retval = (IndexScope) ((tmpElement instanceof IndexScope) ? tmpElement : null);
        }
        if (retval == null) {
            FileScope fileScope = getFileScope(element);
            assert fileScope != null;
            retval = fileScope.getIndexScope();
        }
        return retval;
    }

    public static <T extends ModelElement> List<? extends T> filter(final Collection<? extends T> instances, final ElementFilter<T> filter) {
        List<T> retval = new ArrayList<T>();
        for (T baseElement : instances) {
            boolean accepted = filter.isAccepted(baseElement);
            if (accepted) {
                retval.add(baseElement);
            }
        }
        return retval;
    }

    public static interface ElementFilter<T extends ModelElement> {
        boolean isAccepted(T element);
    }

    public static boolean nameKindMatch(String text, QuerySupport.Kind nameKind, String... queries) {
        return nameKindMatch(true, text, nameKind, queries);
    }

    private static boolean nameKindMatch(boolean forceCaseInsensitivity, String text, QuerySupport.Kind nameKind, String... queries) {
        for (String query : queries) {
            switch (nameKind) {
                case CAMEL_CASE:
                    if (toCamelCase(text).startsWith(query)) {
                        return true;
                    }
                    break;
                case CASE_INSENSITIVE_PREFIX:
                    if (text.toLowerCase().startsWith(query.toLowerCase())) {
                        return true;
                    }
                    break;
                case CASE_INSENSITIVE_REGEXP:
                    text = text.toLowerCase();
                case REGEXP:
                    //TODO: might be perf. problem if called for large collections
                    // and ever and ever again would be compiled still the same query
                    Pattern p = Pattern.compile(query);
                    if (nameKindMatch(p, text)) {
                        return true;
                    }
                    break;
                case EXACT:
                    boolean retval = (forceCaseInsensitivity) ? text.equalsIgnoreCase(query) : text.equals(query);
                    if (retval) {
                        return true;
                    }
                    break;
                case PREFIX:
                    if (text.startsWith(query)) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    public static String getCamelCaseName(ModelElement element) {
        return toCamelCase(element.getName());
    }
    
    public static String toCamelCase(String plainName) {
        char[] retval = new char[plainName.length()];
        int retvalSize = 0;
        for (int i = 0; i < retval.length; i++) {
            char c = plainName.charAt(i);
            if (Character.isUpperCase(c)) {
                retval[retvalSize] = c;
                retvalSize++;
            }
        }
        return String.valueOf(String.valueOf(retval, 0, retvalSize));
    }

    private static boolean nameKindMatch(Pattern p, String text) {
        return p.matcher(text).matches();
    }
}
