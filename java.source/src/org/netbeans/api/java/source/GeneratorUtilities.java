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

package org.netbeans.api.java.source;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Scope.Entry;
import com.sun.tools.javac.code.Scope.StarImportScope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.jvm.ClassReader;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.text.Document;
import javax.tools.JavaFileObject;

import com.sun.source.tree.ErroneousTree;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.modules.java.source.builder.CommentHandlerService;
import org.netbeans.modules.java.source.builder.CommentSetImpl;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.SourceFileObject;
import org.netbeans.modules.java.source.query.CommentSet.RelativePosition;
import org.netbeans.modules.java.source.save.DiffContext;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 * @since 0.20
 */
public final class GeneratorUtilities {

    private WorkingCopy copy;

    private  GeneratorUtilities(WorkingCopy copy) {
        this.copy = copy;
    }

    /**
     * Returns the instance of this class
     *
     * @param copy
     * @return the {@link GeneratorUtilities} instance
     * @since 0.20
     */
    public static GeneratorUtilities get(WorkingCopy copy) {
        return new GeneratorUtilities(copy);
    }

    /**
     * Create a new CompilationUnitTree from a template.
     *
     * @param sourceRoot a source root under which the new file is created
     * @param path a relative path to file separated by '/'
     * @param kind the kind of Element to use for the template, can be null or
     * CLASS, INTERFACE, ANNOTATION_TYPE, ENUM, PACKAGE
     * @return new CompilationUnitTree created from a template
     * @throws IOException when an exception occurs while creating the template
     * @since 0.101
     */
    public CompilationUnitTree createFromTemplate(FileObject sourceRoot, String path, ElementKind kind) throws IOException {
        String[] nameComponent = FileObjects.getFolderAndBaseName(path, '/');
        JavaFileObject sourceFile = FileObjects.templateFileObject(sourceRoot, nameComponent[0], nameComponent[1]);
        FileObject template = FileUtil.getConfigFile(copy.template(kind));
        FileObject targetFile = copy.doCreateFromTemplate(template, sourceFile);
        CompilationUnitTree templateCUT = copy.impl.getJavacTask().parse(FileObjects.nbFileObject(targetFile, targetFile.getParent())).iterator().next();
        CompilationUnitTree importComments = GeneratorUtilities.get(copy).importComments(templateCUT, templateCUT);
        CompilationUnitTree result = copy.getTreeMaker().CompilationUnit(importComments.getPackageAnnotations(),
                sourceRoot,
                path,
                importComments.getImports(),
                importComments.getTypeDecls());
        return result;
    }

    /**
     * Inserts a member to a class. Using the rules specified in the {@link CodeStyle}
     * it finds the proper place for the member and calls {@link TreeMaker.insertClassMember}
     *
     * @param clazz the class to insert the member to
     * @param member the member to add
     * @return the modified class
     * @since 0.20
     */
    public ClassTree insertClassMember(ClassTree clazz, Tree member) {
        assert clazz != null && member != null;
        Document doc = null;
        try {
            doc = copy.getDocument();
            if (doc == null) {
                DataObject data = DataObject.find(copy.getFileObject());
                EditorCookie cookie = data.getCookie(EditorCookie.class);
                doc = cookie.openDocument();
            }
        } catch (IOException ioe) {}
        CodeStyle codeStyle = DiffContext.getCodeStyle(copy);
        ClassMemberComparator comparator = new ClassMemberComparator(codeStyle);
        SourcePositions sp = copy.getTrees().getSourcePositions();
        TreeUtilities utils = copy.getTreeUtilities();
        CompilationUnitTree compilationUnit = copy.getCompilationUnit();
        Tree lastMember = null;
        int idx = -1;
        int gsidx = -1;
        String[] gsnames = codeStyle.keepGettersAndSettersTogether() ? correspondingGSNames(member) : null;
        int i = 0;
        for (Tree tree : clazz.getMembers()) {
            if (!utils.isSynthetic(compilationUnit, tree)) {
                if (gsnames != null && gsidx < 0) {
                    for (String name : gsnames) {
                        if (name.equals(name(tree))) {
                            if (isSetter(tree)) {
                                gsidx = codeStyle.sortMembersInGroupsAlphabetically() ? i : i + 1;
                            } else if (isGetter(tree) || isBooleanGetter(tree)) {
                                gsidx = i + 1;
                            }
                        }
                    }
                }
                if (idx < 0 && (codeStyle.getClassMemberInsertionPoint() == CodeStyle.InsertionPoint.FIRST_IN_CATEGORY && comparator.compare(member, tree) <= 0
                        || comparator.compare(member, tree) < 0)) {
                    if (doc == null || !(doc instanceof GuardedDocument)) {
                        idx = i;
                        continue;
                    }
                    int pos = (int)(lastMember != null ? sp.getEndPosition(compilationUnit, lastMember) : sp.getStartPosition( compilationUnit,clazz));
                    pos = ((GuardedDocument)doc).getGuardedBlockChain().adjustToBlockEnd(pos);
                    long treePos = sp.getStartPosition(compilationUnit, tree);
                    if (treePos < 0 || pos <= treePos) {
                        idx = i;
                    }
                }
            }
            i++;
            lastMember = tree;
        }
        if (idx < 0) {
            idx = i;
        }
        return copy.getTreeMaker().insertClassMember(clazz, gsidx < 0 ? idx : gsidx, member);
    }

    /**
     * Inserts members to a class. Using the rules specified in the {@link CodeStyle}
     * it finds the proper place for each of the members and calls {@link TreeMaker.insertClassMember}
     *
     * @param clazz the class to insert the members to
     * @param members the members to insert
     * @return the modified class
     * @since 0.20
     */
    public ClassTree insertClassMembers(ClassTree clazz, Iterable<? extends Tree> members) {
        assert members != null;
        for (Tree member : members)
            clazz = insertClassMember(clazz, member);
        return clazz;
    }

    /**
     * Creates implementations of the all abstract methods within a class.
     *
     * @param clazz the class to create the implementations within
     * @return the abstract method implementations
     * @since 0.20
     */
    public List<? extends MethodTree> createAllAbstractMethodImplementations(TypeElement clazz) {
        return createAbstractMethodImplementations(clazz, copy.getElementUtilities().findUnimplementedMethods(clazz));
    }

    /**
     * Creates implementations of abstract methods within a class.
     *
     * @param clazz the class to create the implementations within
     * @param methods the abstract methods to implement
     * @return the abstract method implementations
     * @since 0.20
     */
    public List<? extends MethodTree> createAbstractMethodImplementations(TypeElement clazz, Iterable<? extends ExecutableElement> methods) {
        assert methods != null;
        List<MethodTree> ret = new ArrayList<MethodTree>();
        for(ExecutableElement method : methods)
            ret.add(createAbstractMethodImplementation(clazz, method));
        
        tagFirst(ret);
        return ret;
    }

    /**
     * Creates an implementation of an abstract method within a class.
     *
     * @param clazz the class to create the implementation within
     * @param method the abstract method to implement
     * @return the abstract method implementation
     * @since 0.20
     */
    public MethodTree createAbstractMethodImplementation(TypeElement clazz, ExecutableElement method) {
        assert clazz != null && method != null;
        return createMethod(method, clazz);
    }

    /**
     * Creates overriding methods within a class.
     *
     * @param clazz the class to create the methods within
     * @param methods the methods to override
     * @return the overriding methods
     * @since 0.20
     */
    public List<? extends MethodTree> createOverridingMethods(TypeElement clazz, Iterable<? extends ExecutableElement> methods) {
        assert methods != null;
        List<MethodTree> ret = new ArrayList<MethodTree>();
        for(ExecutableElement method : methods)
            ret.add(createOverridingMethod(clazz, method));

        tagFirst(ret);
        return ret;
    }

    /**
     * Creates an overriding method within a class.
     *
     * @param clazz the class to create the method within
     * @param method the method to override
     * @return the overriding method
     * @since 0.20
     */
    public MethodTree createOverridingMethod(TypeElement clazz, ExecutableElement method) {
        assert clazz != null && method != null;
        return createMethod(method, clazz);
    }

    /**Create a new method tree for the given method element. The method will be created as if it were member of {@link asMemberOf} type
     * (see also {@link Types#asMemberOf(javax.lang.model.type.DeclaredType,javax.lang.model.element.Element)}).
     * The new method will have an empty body.
     *
     * @param asMemberOf create the method as if it were member of this type
     * @param method method to create
     * @return a newly created method
     * @see Types#asMemberOf(javax.lang.model.type.DeclaredType,javax.lang.model.element.Element)
     * @since 0.34
     */
    public MethodTree createMethod(DeclaredType asMemberOf, ExecutableElement method) {
        TreeMaker make = copy.getTreeMaker();
        Set<Modifier> mods = method.getModifiers();
        Set<Modifier> flags = mods.isEmpty() ? EnumSet.noneOf(Modifier.class) : EnumSet.copyOf(mods);
        flags.remove(Modifier.ABSTRACT);
        flags.remove(Modifier.NATIVE);

        ExecutableType et = (ExecutableType) method.asType();
        try {
            et = (ExecutableType) copy.getTypes().asMemberOf(asMemberOf, method);
        } catch (IllegalArgumentException iae) {
        }
        List<TypeParameterTree> typeParams = new ArrayList<TypeParameterTree>();
        for (TypeVariable typeVariable : et.getTypeVariables()) {
            List<ExpressionTree> bounds = new ArrayList<ExpressionTree>();
            TypeMirror bound = typeVariable.getUpperBound();
            if (bound.getKind() != TypeKind.NULL) {
                if (bound.getKind() == TypeKind.DECLARED) {
                    ClassSymbol boundSymbol = (ClassSymbol) ((DeclaredType) bound).asElement();
                    if (boundSymbol.getSimpleName().length() == 0 && (boundSymbol.flags() & Flags.COMPOUND) != 0) {
                        bounds.add((ExpressionTree) make.Type(boundSymbol.getSuperclass()));
                        for (Type iface : boundSymbol.getInterfaces()) {
                            bounds.add((ExpressionTree) make.Type(iface));
                        }
                    } else if (!boundSymbol.getQualifiedName().contentEquals("java.lang.Object")) { //NOI18N
                        //if the bound is java.lang.Object, do not generate the extends clause:

                        bounds.add((ExpressionTree) make.Type(bound));
                    }
                } else {
                    bounds.add((ExpressionTree) make.Type(bound));
                }
            }
            typeParams.add(make.TypeParameter(typeVariable.asElement().getSimpleName(), bounds));
        }

        Tree returnType = make.Type(et.getReturnType());

        List<VariableTree> params = new ArrayList<VariableTree>();
        boolean isVarArgs = method.isVarArgs();
        Iterator<? extends VariableElement> formArgNames = method.getParameters().iterator();
        Iterator<? extends TypeMirror> formArgTypes = et.getParameterTypes().iterator();
        ModifiersTree parameterModifiers = make.Modifiers(EnumSet.noneOf(Modifier.class));
        while (formArgNames.hasNext() && formArgTypes.hasNext()) {
            VariableElement formArgName = formArgNames.next();
            TypeMirror formArgType = formArgTypes.next();
            if (isVarArgs && !formArgNames.hasNext()) {
                parameterModifiers = make.Modifiers(1L << 34,
                        Collections.<AnnotationTree>emptyList());
            }
            params.add(make.Variable(parameterModifiers, formArgName.getSimpleName(), resolveWildcard(formArgType), null));
        }

        List<ExpressionTree> throwsList = new ArrayList<ExpressionTree>();
        for (TypeMirror tm : et.getThrownTypes()) {
            throwsList.add((ExpressionTree) make.Type(tm));
        }

        ModifiersTree mt = make.Modifiers(flags, Collections.<AnnotationTree>emptyList());

        return make.Method(mt, method.getSimpleName(), returnType, typeParams, params, throwsList, "{}", null);
    }

    /**
     * Creates a class constructor.
     *
     * @param clazz the class to create the constructor for
     * @param fields fields to be initialized by the constructor
     * @param constructor inherited constructor to be called
     * @return the constructor
     * @since 0.20
     */
    public MethodTree createConstructor(TypeElement clazz, Iterable<? extends VariableElement> fields, ExecutableElement constructor) {
        return createConstructor(clazz, fields, constructor, false);
    }
    
    /**
     * Creates a class default constructor. Fields and the inherited constructor
     * are initialized/called with default values.
     *
     * @param clazz the class to create the constructor for
     * @param fields fields to be initialized by the constructor
     * @param constructor inherited constructor to be called
     * @return the constructor
     * @since 0.126
     */
    public MethodTree createDefaultConstructor(TypeElement clazz, Iterable<? extends VariableElement> fields, ExecutableElement constructor) {
        return createConstructor(clazz, fields, constructor, true);
    }

    private MethodTree createConstructor(TypeElement clazz, Iterable<? extends VariableElement> fields, ExecutableElement constructor, boolean isDefault) {
        assert clazz != null && fields != null;
        TreeMaker make = copy.getTreeMaker();
        Set<Modifier> mods = EnumSet.of(clazz.getKind() == ElementKind.ENUM ? Modifier.PRIVATE : Modifier.PUBLIC);
        List<VariableTree> parameters = new ArrayList<VariableTree>();
        LinkedList<StatementTree> statements = new LinkedList<StatementTree>();
        ModifiersTree parameterModifiers = make.Modifiers(EnumSet.noneOf(Modifier.class));
        List<ExpressionTree> throwsList = new LinkedList<ExpressionTree>();
        List<TypeParameterTree> typeParams = new LinkedList<TypeParameterTree>();
        for (VariableElement ve : fields) {
            TypeMirror type = copy.getTypes().asMemberOf((DeclaredType)clazz.asType(), ve);
            if (isDefault) {
                statements.add(make.ExpressionStatement(make.Assignment(make.MemberSelect(make.Identifier("this"), ve.getSimpleName()), make.Literal(defaultValue(type))))); //NOI18N
            } else {
                parameters.add(make.Variable(parameterModifiers, ve.getSimpleName(), make.Type(type), null));
                statements.add(make.ExpressionStatement(make.Assignment(make.MemberSelect(make.Identifier("this"), ve.getSimpleName()), make.Identifier(ve.getSimpleName())))); //NOI18N
            }
        }
        if (constructor != null) {
            ExecutableType constructorType = clazz.getSuperclass().getKind() == TypeKind.DECLARED && ((DeclaredType) clazz.getSuperclass()).asElement() == constructor.getEnclosingElement() ? (ExecutableType) copy.getTypes().asMemberOf((DeclaredType) clazz.getSuperclass(), constructor) : null;
            if (!constructor.getParameters().isEmpty()) {
                List<ExpressionTree> arguments = new ArrayList<ExpressionTree>();
                Iterator<? extends VariableElement> parameterElements = constructor.getParameters().iterator();
                Iterator<? extends TypeMirror> parameterTypes = constructorType != null ? constructorType.getParameterTypes().iterator() : null;
                while (parameterElements.hasNext()) {
                    VariableElement ve = parameterElements.next();
                    Name simpleName = ve.getSimpleName();
                    TypeMirror type = parameterTypes != null ? parameterTypes.next() : ve.asType();
                    if (isDefault) {
                        arguments.add(make.Literal(defaultValue(type)));
                    } else {
                        parameters.add(make.Variable(parameterModifiers, simpleName, make.Type(type), null));
                        arguments.add(make.Identifier(simpleName));
                    }
                }
                statements.addFirst(make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier("super"), arguments))); //NOI18N
            }
            constructorType = constructorType != null ? constructorType : (ExecutableType) constructor.asType();
            for (TypeMirror th : constructorType.getThrownTypes()) {
                throwsList.add((ExpressionTree) make.Type(th));
            }
            for (TypeParameterElement typeParameterElement : constructor.getTypeParameters()) {
                List<ExpressionTree> boundsList = new LinkedList<ExpressionTree>();
                for (TypeMirror bound : typeParameterElement.getBounds()) {
                    boundsList.add((ExpressionTree) make.Type(bound));
                }
                typeParams.add(make.TypeParameter(typeParameterElement.getSimpleName(), boundsList));
            }
        }
        BlockTree body = make.Block(statements, false);
        return make.Method(make.Modifiers(mods), "<init>", null, typeParams, parameters, throwsList, body, null, constructor!= null ? constructor.isVarArgs() : false); //NOI18N
    }

    /**
     * Creates a class constructor.
     *
     * @param clazz the class to create the constructor for
     * @param fields fields to be initialized by the constructor
     * @return the constructor
     * @since 0.20
     */
    public MethodTree createConstructor(ClassTree clazz, Iterable<? extends VariableTree> fields) {
        assert clazz != null && fields != null;
        TreeMaker make = copy.getTreeMaker();
        CodeStyle cs = DiffContext.getCodeStyle(copy);
        Set<Modifier> mods = EnumSet.of(copy.getTreeUtilities().isEnum(clazz) ? Modifier.PRIVATE : Modifier.PUBLIC);
        List<VariableTree> parameters = new ArrayList<VariableTree>();
        List<StatementTree> statements = new ArrayList<StatementTree>();
        ModifiersTree parameterModifiers = make.Modifiers(EnumSet.noneOf(Modifier.class));
        for (VariableTree vt : fields) {
            String paramName = addParamPrefixSuffix(removeFieldPrefixSuffix(vt, cs), cs);
            parameters.add(make.Variable(parameterModifiers, paramName, vt.getType(), null));
            statements.add(make.ExpressionStatement(make.Assignment(make.MemberSelect(make.Identifier("this"), vt.getName()), make.Identifier(paramName)))); //NOI18N
        }
        BlockTree body = make.Block(statements, false);
        return make.Method(make.Modifiers(mods), "<init>", null, Collections.<TypeParameterTree> emptyList(), parameters, Collections.<ExpressionTree>emptyList(), body, null); //NOI18N
    }

    /**
     * Creates a getter method for a field.
     *
     * @param clazz the class to create the getter within
     * @param field field to create getter for
     * @return the getter method
     * @since 0.20
     */
    public MethodTree createGetter(TypeElement clazz, VariableElement field) {
        assert clazz != null && field != null;
        TreeMaker make = copy.getTreeMaker();
        CodeStyle cs = DiffContext.getCodeStyle(copy);
        Set<Modifier> mods = EnumSet.of(Modifier.PUBLIC);
        boolean isStatic = field.getModifiers().contains(Modifier.STATIC);
        if (isStatic) {
            mods.add(Modifier.STATIC);
        }
        TypeMirror type = copy.getTypes().asMemberOf((DeclaredType)clazz.asType(), field);
        String getterName = CodeStyleUtils.computeGetterName(field.getSimpleName(), type.getKind() == TypeKind.BOOLEAN, isStatic, cs);
        BlockTree body = make.Block(Collections.singletonList(make.Return(make.Identifier(field.getSimpleName()))), false);
        return make.Method(make.Modifiers(mods), getterName, make.Type(type), Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), body, null);
    }

    /**
     * Creates a getter method for a field.
     *
     * @param field field to create getter for
     * @return the getter method
     * @since 0.20
     */
    public MethodTree createGetter(VariableTree field) {
        assert field != null;
        TreeMaker make = copy.getTreeMaker();
        CodeStyle cs = DiffContext.getCodeStyle(copy);
        Set<Modifier> mods = EnumSet.of(Modifier.PUBLIC);
        boolean isStatic = field.getModifiers().getFlags().contains(Modifier.STATIC);
        if (isStatic) {
            mods.add(Modifier.STATIC);
        }
        Tree type = field.getType();
        boolean isBoolean = type.getKind() == Tree.Kind.PRIMITIVE_TYPE && ((PrimitiveTypeTree) type).getPrimitiveTypeKind() == TypeKind.BOOLEAN;
        String getterName = CodeStyleUtils.computeGetterName(field.getName(), isBoolean, isStatic, cs);
        BlockTree body = make.Block(Collections.singletonList(make.Return(make.Identifier(field.getName()))), false);
        return make.Method(make.Modifiers(mods), getterName, type, Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), body, null);
    }

    /**
     * Creates a setter method for a field.
     *
     * @param clazz the class to create the setter within
     * @param field field to create setter for
     * @return the setter method
     * @since 0.20
     */
    public MethodTree createSetter(TypeElement clazz, VariableElement field) {
        assert clazz != null && field != null;
        TreeMaker make = copy.getTreeMaker();
        CodeStyle cs = DiffContext.getCodeStyle(copy);
        Set<Modifier> mods = EnumSet.of(Modifier.PUBLIC);
        boolean isStatic = field.getModifiers().contains(Modifier.STATIC);
        if (isStatic) {
            mods.add(Modifier.STATIC);
        }
        CharSequence name = field.getSimpleName();
        assert name.length() > 0;
        TypeMirror type = copy.getTypes().asMemberOf((DeclaredType)clazz.asType(), field);
        String setterName = CodeStyleUtils.computeSetterName(field.getSimpleName(), isStatic, cs);
        String paramName = addParamPrefixSuffix(removeFieldPrefixSuffix(field, cs), cs);
        List<VariableTree> params = Collections.singletonList(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), paramName, make.Type(type), null));
        BlockTree body = make.Block(Collections.singletonList(make.ExpressionStatement(make.Assignment(make.MemberSelect(isStatic? make.Identifier(field.getEnclosingElement().getSimpleName()) : make.Identifier("this"), name), make.Identifier(paramName)))), false); //NOI18N
        return make.Method(make.Modifiers(mods), setterName, make.Type(copy.getTypes().getNoType(TypeKind.VOID)), Collections.<TypeParameterTree>emptyList(), params, Collections.<ExpressionTree>emptyList(), body, null);
    }

    /**
     * Creates a setter method for a field.
     *
     * @param clazz the class to create the setter within
     * @param field field to create setter for
     * @return the setter method
     * @since 0.20
     */
    public MethodTree createSetter(ClassTree clazz, VariableTree field) {
        assert clazz != null && field != null;
        TreeMaker make = copy.getTreeMaker();
        Set<Modifier> mods = EnumSet.of(Modifier.PUBLIC);
        boolean isStatic = field.getModifiers().getFlags().contains(Modifier.STATIC);
        if (isStatic)
            mods.add(Modifier.STATIC);
        CharSequence name = field.getName();
        assert name.length() > 0;
        CodeStyle cs = DiffContext.getCodeStyle(copy);
        String propName = removeFieldPrefixSuffix(field, cs);
        String setterName = CodeStyleUtils.computeSetterName(field.getName(), isStatic, cs);
        String paramName = addParamPrefixSuffix(propName, cs);
        List<VariableTree> params = Collections.singletonList(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), paramName, field.getType(), null));
        BlockTree body = make.Block(Collections.singletonList(make.ExpressionStatement(make.Assignment(make.MemberSelect(isStatic? make.Identifier(clazz.getSimpleName()) : make.Identifier("this"), name), make.Identifier(paramName)))), false); //NOI18N
        return make.Method(make.Modifiers(mods), setterName, make.Type(copy.getTypes().getNoType(TypeKind.VOID)), Collections.<TypeParameterTree>emptyList(), params, Collections.<ExpressionTree>emptyList(), body, null);
    }
    
    /**
     * Adds import statements for given elements to a compilation unit. The import section of the
     * given compilation unit is modified according to the rules specified in the {@link CodeStyle}.
     * <p><strong>Use TreeMaker.QualIdent, TreeMaker.Type or GeneratorUtilities.importFQNs
     * instead of this method if possible. These methods will correctly resolve imports according
     * to the user's preferences.</strong></p>
     *
     * @param cut the compilation unit to insert imports to
     * @param toImport the elements to import. 
     * @return the modified compilation unit
     * @since 0.86
     */
    public CompilationUnitTree addImports(CompilationUnitTree cut, Set<? extends Element> toImport) {
        assert cut != null && toImport != null && toImport.size() > 0;

        ArrayList<Element> elementsToImport = new ArrayList<Element>(toImport.size());
        Set<String> staticImportNames = new HashSet<String>();
        for (Element e : toImport) {
            switch (e.getKind()) {
                case METHOD:
                case ENUM_CONSTANT:
                case FIELD:
                    StringBuilder name = new StringBuilder(((TypeElement)e.getEnclosingElement()).getQualifiedName()).append('.').append(e.getSimpleName());
                    if (!staticImportNames.add(name.toString()))
                        break;
                default:
                    elementsToImport.add(e);
            }
        }

        Trees trees = copy.getTrees();
        Elements elements = copy.getElements();
        ElementUtilities elementUtilities = copy.getElementUtilities();
                
        CodeStyle cs = DiffContext.getCodeStyle(copy);
        
        // check weather any conversions to star imports are needed
        int treshold = cs.countForUsingStarImport();
        int staticTreshold = cs.countForUsingStaticStarImport();        
        Map<PackageElement, Integer> pkgCounts = new LinkedHashMap<PackageElement, Integer>();
        PackageElement pkg = elements.getPackageElement("java.lang"); //NOI18N
        if (pkg != null)
            pkgCounts.put(pkg, -2);
        ExpressionTree packageName = cut.getPackageName();
        pkg = packageName != null ? (PackageElement)trees.getElement(TreePath.getPath(cut, packageName)) : null;
        if (pkg == null && packageName != null)
            pkg = elements.getPackageElement(elements.getName(packageName.toString()));
        if (pkg == null)
            pkg = elements.getPackageElement(elements.getName("")); //NOI18N
        pkgCounts.put(pkg, -2);
        Map<TypeElement, Integer> typeCounts = new LinkedHashMap<TypeElement, Integer>();
        Scope scope = trees.getScope(new TreePath(cut));
        StarImportScope importScope = new StarImportScope((Symbol)pkg);
        if (((JCCompilationUnit)cut).starImportScope != null)
            importScope.importAll(((JCCompilationUnit)cut).starImportScope);
        for (Element e : elementsToImport) {
            boolean isStatic = false;
            Element el = null;
            switch (e.getKind()) {
                case PACKAGE:
                    el = e;
                    break;
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                    if (e.getEnclosingElement().getKind() == ElementKind.PACKAGE)
                        el = e.getEnclosingElement();
                    break;
                case METHOD:
                case ENUM_CONSTANT:
                case FIELD:
                    isStatic = true;
                    el = e.getEnclosingElement();
                    assert e.getModifiers().contains(Modifier.STATIC) : "Only static members could be imported: " + e; //NOI18N
                    assert trees.isAccessible(scope, e, (DeclaredType)el.asType()) : "Only accessible members could be imported: " + e + "\nEnclosing element: " + el + "\nScope: " + scope; //NOI18N
                    break;
                default:
                    assert false : "Illegal element kind: " + e.getKind(); //NOI18N
            }
            if (el != null) {
                Integer cnt = isStatic ? typeCounts.get((TypeElement)el) : pkgCounts.get((PackageElement)el);
                if (cnt == null)
                    cnt = 0;
                if (cnt >= 0) {
                    if (el == e) {
                        cnt = -1;
                    } else {
                        cnt++;
                        if (isStatic) {
                            if (cnt >= staticTreshold)
                                cnt = -1;
                        } else if (cnt >= treshold || checkPackagesForStarImport(((PackageElement)el).getQualifiedName().toString(), cs)) {
                            cnt = -1;
                        }
                    }
                }
                if (isStatic) {
                    typeCounts.put((TypeElement)el, cnt);
                } else {
                    pkgCounts.put((PackageElement)el, cnt);
                }
            }
        }
        List<ImportTree> imports = new ArrayList<ImportTree>(cut.getImports());
        for (ImportTree imp : imports) {
            Element e = getImportedElement(cut, imp);
            if (imp.isStatic()) {
                if (e.getKind().isClass() || e.getKind().isInterface()) {
                    Element el = e;
                    while (el != null) {
                        Integer cnt = typeCounts.get((TypeElement)el);
                        if (cnt != null) {
                            typeCounts.put((TypeElement)el, -2);
                        }
                        TypeMirror tm = ((TypeElement)el).getSuperclass();
                        el = tm.getKind() == TypeKind.DECLARED ? ((DeclaredType)tm).asElement() : null;
                    }
                } else {
                    Element el = elementUtilities.enclosingTypeElement(e);
                    if (el != null) {
                        Integer cnt = typeCounts.get((TypeElement)el);
                        if (cnt != null) {
                            if (cnt >= 0) {
                                cnt++;
                                if (cnt >= staticTreshold)
                                    cnt = -1;
                            }
                            typeCounts.put((TypeElement)el, cnt);
                        }
                    }
                }
            } else {
                Element el = e.getKind() == ElementKind.PACKAGE ? e : (e.getKind().isClass() || e.getKind().isInterface()) && e.getEnclosingElement().getKind() == ElementKind.PACKAGE ? e.getEnclosingElement() : null;
                if (el != null) {
                    Integer cnt = pkgCounts.get((PackageElement)el);
                    if (cnt != null) {
                        if (el == e) {
                            cnt = -2;
                        } else if (cnt >= 0) {
                            cnt++;
                            if (cnt >= treshold)
                                cnt = -1;
                        }
                        pkgCounts.put((PackageElement)el, cnt);
                    }
                }
            }
        }
        
        // check for possible name clashes originating from adding the package imports
        Set<Element> explicitNamedImports = new HashSet<Element>();
        for (Element element : elementsToImport) {
            if (element.getKind().isClass() || element.getKind().isInterface()) {
                for (Entry e = importScope.lookup((com.sun.tools.javac.util.Name)element.getSimpleName()); e.scope != null; e = e.next()) {
                    if (e.sym.getKind().isClass() || e.sym.getKind().isInterface()) {
                        if (e.sym != element) {
                            explicitNamedImports.add(element);
                            break;
                        }
                    }
                }
            }
        }
        Map<Name, TypeElement> usedTypes = null;
        for (Map.Entry<PackageElement, Integer> entry : pkgCounts.entrySet()) {
            if (entry.getValue() == -1) {
                for (Element element : entry.getKey().getEnclosedElements()) {
                    if (element.getKind().isClass() || element.getKind().isInterface()) {
                        Entry starEntry = importScope.lookup((com.sun.tools.javac.util.Name)element.getSimpleName());
                        if (starEntry.scope != null) {
                            TypeElement te = null;
                            for (Element e : elementsToImport) {
                                if ((e.getKind().isClass() || e.getKind().isInterface()) && element.getSimpleName() == e.getSimpleName()) {
                                    te = (TypeElement) e;
                                    break;
                                }                                    
                            }
                            if (te != null) {
                                explicitNamedImports.add(te);
                            } else {
                                if (usedTypes == null) {
                                    usedTypes = getUsedTypes(cut);
                                }
                                te = usedTypes.get(element.getSimpleName());
                                if (te != null) {
                                    elementsToImport.add(te);
                                    explicitNamedImports.add(te);
                                }
                            }
                        }
                    }
                }
            }
            if (entry.getValue() < 0 && entry.getKey() instanceof Symbol)
                importScope.importAll(((Symbol)entry.getKey()).members());
        }

        // sort the elements to import
        ImportsComparator comparator = new ImportsComparator(cs);
        Collections.sort(elementsToImport, comparator);
        
        // merge the elements to import with the existing import statemetns
        TreeMaker make = copy.getTreeMaker();
        int currentToImport = elementsToImport.size() - 1;
        int currentExisting = imports.size() - 1;
        while (currentToImport >= 0) {
            Element currentToImportElement = elementsToImport.get(currentToImport);
            boolean isStatic = false;
            Element el = null;
            switch (currentToImportElement.getKind()) {
                case PACKAGE:
                    el = currentToImportElement;
                    break;
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                    if (currentToImportElement.getEnclosingElement().getKind() == ElementKind.PACKAGE)
                        el = currentToImportElement.getEnclosingElement();
                    break;
                case METHOD:
                case ENUM_CONSTANT:
                case FIELD:
                    isStatic = true;
                    el = currentToImportElement.getEnclosingElement();
                    break;
            }
            Integer cnt = el == null ? Integer.valueOf(0) : isStatic ? typeCounts.get((TypeElement)el) : pkgCounts.get((PackageElement)el);
            if (explicitNamedImports.contains(currentToImportElement))
                cnt = 0;
            if (cnt == -2) {
                currentToImport--;
            } else {
                if (cnt == -1) {
                    currentToImportElement = el;
                    if (isStatic) {
                        typeCounts.put((TypeElement)el, -2);
                    } else {
                        pkgCounts.put((PackageElement)el, -2);
                    }
                }
                boolean isStar = currentToImportElement.getKind() == ElementKind.PACKAGE
                        || isStatic && (currentToImportElement.getKind().isClass() || currentToImportElement.getKind().isInterface());
                while (currentExisting >= 0) {
                    ImportTree imp = imports.get(currentExisting);
                    Element impElement = getImportedElement(cut, imp);
                    el = imp.isStatic()
                            ? impElement.getKind().isClass() || impElement.getKind().isInterface() ? impElement : elementUtilities.enclosingTypeElement(impElement)
                            : impElement.getKind() == ElementKind.PACKAGE ? impElement : (impElement.getKind().isClass() || impElement.getKind().isInterface()) && impElement.getEnclosingElement().getKind() == ElementKind.PACKAGE ? impElement.getEnclosingElement() : null;
                    if (isStatic == imp.isStatic() && (currentToImportElement == impElement || isStar && currentToImportElement == el)) {
                        imports.remove(currentExisting);                        
                    } else if (comparator.compare(currentToImportElement, imp) > 0) {
                        break;
                    }
                    currentExisting--;
                }
                ExpressionTree qualIdent = qualIdentFor(currentToImportElement);
                if (isStar)
                    qualIdent = make.MemberSelect(qualIdent, elements.getName("*")); //NOI18N
                imports.add(currentExisting + 1, make.Import(qualIdent, isStatic));
                currentToImport--;
            }
        }
        
        // return a copy of the unit with changed imports section
        return make.CompilationUnit(cut.getPackageAnnotations(), cut.getPackageName(), imports, cut.getTypeDecls(), cut.getSourceFile());
    }

    /**
     * Take a tree as a parameter, replace resolved fully qualified names with
     * simple names and add imports to compilation unit during task commit.
     *
     * @param  original  resolved FQNs in the tree will be imported
     * @return the new tree containing simple names (QualIdents). Imports for
     *         them will be added during task commit.
     */
    public <T extends Tree> T importFQNs(T original) {
        return TranslateIdentifier.importFQNs(copy, original);
    }

    public <T extends Tree> T importComments(T original, CompilationUnitTree cut) {
        return importComments(copy, original, cut);
    }

    static <T extends Tree> T importComments(CompilationInfo info, T original, CompilationUnitTree cut) {
        try {
            CommentSetImpl comments = CommentHandlerService.instance(info.impl.getJavacTask().getContext()).getComments(original);

            if (comments.areCommentsMapped()) {
                //optimalization, if comments are already mapped, do not even try to
                //map them again, would not be attached anyway:
                return original;
            }
            
            JCTree.JCCompilationUnit unit = (JCCompilationUnit) cut;
            TokenSequence<JavaTokenId> seq = ((SourceFileObject) unit.getSourceFile()).getTokenHierarchy().tokenSequence(JavaTokenId.language());
            TreePath tp = TreePath.getPath(cut, original);
            Tree toMap = (tp != null && original.getKind() != Kind.COMPILATION_UNIT) ? tp.getParentPath().getLeaf() : original;
            AssignComments translator = new AssignComments(info, original, seq, unit);
            
            translator.scan(toMap, null);

            return original;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return original;
    }

    /**
     * Copy comments from source tree to target tree.
     *
     * @param source tree to copy comments from
     * @param target tree to copy comments to
     * @param preceding true iff preceding comments should be copied
     * @since 0.51
     */
    public void copyComments(Tree source, Tree target, boolean preceding) {
        CommentHandlerService handler = CommentHandlerService.instance(copy.impl.getJavacTask().getContext());
        CommentSetImpl s = handler.getComments(source);

        TreeUtilities.ensureCommentsMapped(copy, source, s);

        CommentSetImpl t = handler.getComments(target);

        if (preceding) {
            t.addComments(RelativePosition.PRECEDING, s.getComments(RelativePosition.PRECEDING));
        } else {
            t.addComments(RelativePosition.INLINE, s.getComments(RelativePosition.INLINE));
            t.addComments(RelativePosition.TRAILING, s.getComments(RelativePosition.TRAILING));
        }
    }

    /**Ensures that the given {@code modifiers} contains annotation of the given type,
     * which has attribute name {@code attributeName}, which contains values {@code attributeValuesToAdd}.
     * The annotation or the attribute will be added as needed, as will be the attribute value
     * converted from a single value into an array.
     *
     * The typical trees passed as {@code attributeValuesToAdd} are:
     * <table border="1">
     *     <tr>
     *         <th>attribute type</th>
     *         <th>expected tree type</th>
     *     </tr>
     *     <tr>
     *         <td>primitive type</td>
     *         <td>{@link LiteralTree} created by {@link TreeMaker#Literal(java.lang.Object) }</td>
     *     </tr>
     *     <tr>
     *         <td>{@code java.lang.String}</td>
     *         <td>{@link LiteralTree} created by {@link TreeMaker#Literal(java.lang.Object) }</td>
     *     </tr>
     *     <tr>
     *         <td>{@code java.lang.Class}</td>
     *         <td>{@link MemberSelectTree} created by {@link TreeMaker#MemberSelect(com.sun.source.tree.ExpressionTree, java.lang.CharSequence)  },
     *             with identifier {@code class} and expression created by {@link TreeMaker#QualIdent(javax.lang.model.element.Element) }</td>
     *     </tr>
     *     <tr>
     *         <td>enum constant</td>
     *         <td>{@link MemberSelectTree}, with identifier representing the enum constant
     *             and expression created by {@link TreeMaker#QualIdent(javax.lang.model.element.Element) }</td>
     *     </tr>
     *     <tr>
     *         <td>annotation type</td>
     *         <td>{@link AnnotationTree} created by {@link TreeMaker#Annotation(com.sun.source.tree.Tree, java.util.List) }</td>
     *     </tr>
     *     <tr>
     *         <td>array (of a supported type)</td>
     *         <td>{@link NewArrayTree} created by {@link TreeMaker#NewArray(com.sun.source.tree.Tree, java.util.List, java.util.List) },
     *             where {@code elemtype} is {@code null}, {@code dimensions} is {@code Collections.<ExpressionTree>emptyList()},
     *             {@code initializers} should contain the elements that should appear in the array</td>
     *     </tr>
     * </table>
     *
     * @param modifiers into which the values should be added
     * @param annotation the annotation type that should be added or augmented
     * @param attributeName the attribute that should be added or augmented
     * @param attributeValuesToAdd values that should be added into the given attribute of the given annotation
     * @return {@code modifiers} augmented in such a way that it contains the given annotation, with the given values
     * @since 0.99
     */
    public ModifiersTree appendToAnnotationValue(ModifiersTree modifiers, TypeElement annotation, String attributeName, ExpressionTree... attributeValuesToAdd) {
        return (ModifiersTree) appendToAnnotationValue((Tree) modifiers, annotation, attributeName, attributeValuesToAdd);
    }

    /**Ensures that the given {@code compilationUnit} contains annotation of the given type,
     * which has attribute name {@code attributeName}, which contains values {@code attributeValuesToAdd}.
     * The annotation or the attribute will be added as needed, as will be the attribute value
     * converted from a single value into an array. This method is intended to be called on
     * {@link CompilationUnitTree} from {@code package-info.java}.
     *
     * The typical trees passed as {@code attributeValuesToAdd} are:
     * <table border="1">
     *     <tr>
     *         <th>attribute type</th>
     *         <th>expected tree type</th>
     *     </tr>
     *     <tr>
     *         <td>primitive type</td>
     *         <td>{@link LiteralTree} created by {@link TreeMaker#Literal(java.lang.Object) }</td>
     *     </tr>
     *     <tr>
     *         <td>{@code java.lang.String}</td>
     *         <td>{@link LiteralTree} created by {@link TreeMaker#Literal(java.lang.Object) }</td>
     *     </tr>
     *     <tr>
     *         <td>{@code java.lang.Class}</td>
     *         <td>{@link MemberSelectTree} created by {@link TreeMaker#MemberSelect(com.sun.source.tree.ExpressionTree, java.lang.CharSequence)  },
     *             with identifier {@code class} and expression created by {@link TreeMaker#QualIdent(javax.lang.model.element.Element) }</td>
     *     </tr>
     *     <tr>
     *         <td>enum constant</td>
     *         <td>{@link MemberSelectTree}, with identifier representing the enum constant
     *             and expression created by {@link TreeMaker#QualIdent(javax.lang.model.element.Element) }</td>
     *     </tr>
     *     <tr>
     *         <td>annotation type</td>
     *         <td>{@link AnnotationTree} created by {@link TreeMaker#Annotation(com.sun.source.tree.Tree, java.util.List) }</td>
     *     </tr>
     *     <tr>
     *         <td>array (of a supported type)</td>
     *         <td>{@link NewArrayTree} created by {@link TreeMaker#NewArray(com.sun.source.tree.Tree, java.util.List, java.util.List) },
     *             where {@code elemtype} is {@code null}, {@code dimensions} is {@code Collections.<ExpressionTree>emptyList()},
     *             {@code initializers} should contain the elements that should appear in the array</td>
     *     </tr>
     * </table>
     *
     * @param compilationUnit into which the values should be added
     * @param annotation the annotation type that should be added or augmented
     * @param attributeName the attribute that should be added or augmented
     * @param attributeValuesToAdd values that should be added into the given attribute of the given annotation
     * @return {@code compilationUnit} augmented in such a way that it contains the given annotation, with the given values
     * @since 0.99
     */
    public CompilationUnitTree appendToAnnotationValue(CompilationUnitTree compilationUnit, TypeElement annotation, String attributeName, ExpressionTree... attributeValuesToAdd) {
        return (CompilationUnitTree) appendToAnnotationValue((Tree) compilationUnit, annotation, attributeName, attributeValuesToAdd);
    }

    private Tree appendToAnnotationValue(Tree/*CompilationUnitTree|ModifiersTree*/ modifiers, TypeElement annotation, String attributeName, ExpressionTree... attributeValuesToAdd) {
        TreeMaker make = copy.getTreeMaker();

        //check for already existing SuppressWarnings annotation:
        List<? extends AnnotationTree> annotations = null;

        if (modifiers.getKind() == Kind.MODIFIERS) {
            annotations = ((ModifiersTree) modifiers).getAnnotations();
        } else if (modifiers.getKind() == Kind.COMPILATION_UNIT) {
            annotations = ((CompilationUnitTree) modifiers).getPackageAnnotations();
        } else {
            throw new IllegalStateException();
        }

        for (AnnotationTree at : annotations) {
            TreePath tp = new TreePath(new TreePath(copy.getCompilationUnit()), at.getAnnotationType());
            Element  e  = copy.getTrees().getElement(tp);

            if (annotation.equals(e)) {
                //found SuppressWarnings:
                List<? extends ExpressionTree> arguments = at.getArguments();

                for (ExpressionTree et : arguments) {
                    ExpressionTree expression;

                    if (et.getKind() == Kind.ASSIGNMENT) {
                        AssignmentTree assignment = (AssignmentTree) et;

                        if (!((IdentifierTree) assignment.getVariable()).getName().contentEquals(attributeName)) continue;

                        expression = assignment.getExpression();
                    } else if ("value".equals(attributeName)) {
                        expression = et;
                    } else {
                        continue;
                    }

                    List<? extends ExpressionTree> currentValues;

                    if (expression.getKind() == Kind.NEW_ARRAY) {
                        currentValues = ((NewArrayTree) expression).getInitializers();
                    } else {
                        currentValues = Collections.singletonList(expression);
                    }

                    assert currentValues != null;

                    List<ExpressionTree> values = new ArrayList<ExpressionTree>(currentValues);

                    values.addAll(Arrays.asList(attributeValuesToAdd));

                    NewArrayTree newAssignment = make.NewArray(null, Collections.<ExpressionTree>emptyList(), values);

                    return copy.getTreeUtilities().translate(modifiers, Collections.singletonMap(expression, newAssignment));
                }

                AnnotationTree newAnnotation = make.addAnnotationAttrValue(at, make.Assignment(make.Identifier(attributeName), make.NewArray(null, Collections.<ExpressionTree>emptyList(), Arrays.asList(attributeValuesToAdd))));

                return copy.getTreeUtilities().translate(modifiers, Collections.singletonMap(at, newAnnotation));
            }
        }

        ExpressionTree attribute;

        if (attributeValuesToAdd.length > 1 ) {
            attribute = make.NewArray(null, Collections.<ExpressionTree>emptyList(), Arrays.asList(attributeValuesToAdd));
        }
        else {
            attribute = attributeValuesToAdd[0];
        }

        ExpressionTree attributeAssignmentTree;

        if ("value".equals(attributeName)) {
            attributeAssignmentTree = attribute;
        } else {
            attributeAssignmentTree = make.Assignment(make.Identifier(attributeName), attribute);
        }
        
        AnnotationTree newAnnotation = make.Annotation(make.QualIdent(annotation), Collections.singletonList(attributeAssignmentTree));
        
        if (modifiers.getKind() == Kind.MODIFIERS) {
            return make.addModifiersAnnotation((ModifiersTree) modifiers, newAnnotation);
        } else if (modifiers.getKind() == Kind.COMPILATION_UNIT) {
            return make.addPackageAnnotation((CompilationUnitTree) modifiers, newAnnotation);
        } else {
            throw new IllegalStateException();
        }
    }
    
    // private implementation --------------------------------------------------

    private MethodTree createMethod(final ExecutableElement element, final TypeElement clazz) {
        final TreeMaker make = copy.getTreeMaker();
        MethodTree prototype = createMethod((DeclaredType)clazz.asType(), element);
        ModifiersTree mt = prototype.getModifiers();

        if (supportsOverride(copy)) {
            //add @Override annotation:
            if (copy.getSourceVersion().compareTo(SourceVersion.RELEASE_5) >= 0) {
                boolean generate = true;

                if (copy.getSourceVersion().compareTo(SourceVersion.RELEASE_5) == 0) {
                    generate = !element.getEnclosingElement().getKind().isInterface();
                }

                if (generate) {
                   mt = make.addModifiersAnnotation(prototype.getModifiers(), make.Annotation(make.Identifier("Override"), Collections.<ExpressionTree>emptyList()));
                }
            }
        }
        
        boolean isAbstract = element.getModifiers().contains(Modifier.ABSTRACT);
        String bodyTemplate = null;
        try {
            bodyTemplate = "{" + readFromTemplate(isAbstract ? GENERATED_METHOD_BODY : OVERRIDDEN_METHOD_BODY, createBindings(clazz, element)) + "\n}"; //NOI18N
        } catch (Exception e) {
            bodyTemplate = "{}"; //NOI18N
        }
        
        MethodTree method = make.Method(mt, prototype.getName(), prototype.getReturnType(), prototype.getTypeParameters(), prototype.getParameters(), prototype.getThrows(), bodyTemplate, null);
        if (containsErrors(method.getBody())) {
            copy.rewrite(method.getBody(), make.Block(Collections.<StatementTree>emptyList(), false));
        } else {
            Trees trees = copy.getTrees();
            TreePath path = trees.getPath(clazz);
            if (path == null) {
                path = new TreePath(copy.getCompilationUnit());
            }
            Scope s = trees.getScope(path);
            BlockTree body = method.getBody();
            copy.getTreeUtilities().attributeTree(body, s);
            body = importFQNs(body);
            copy.rewrite(method.getBody(), body);
        }
        
        return method;
    }

    private static Object defaultValue(TypeMirror type) {
        switch(type.getKind()) {
            case BOOLEAN:
                return false;
            case BYTE:
            case CHAR:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case SHORT:
                return 0;
        }
        return null;
    }

    private static boolean supportsOverride(CompilationInfo info) {
        return info.getElements().getTypeElement("java.lang.Override") != null;
    }

    private Tree resolveWildcard(TypeMirror type) {
        TreeMaker make = copy.getTreeMaker();
        Tree result;

        if (type != null && type.getKind() == TypeKind.WILDCARD) {
            WildcardType wt = (WildcardType) type;
            TypeMirror bound = wt.getSuperBound();

            if (bound == null) {
                bound = wt.getExtendsBound();
            }

            if (bound == null) {
                return make.Type("java.lang.Object");
            }

            result = make.Type(bound);
        } else {
            result = make.Type(type);
        }

        final Map<Tree, Tree> translate = new IdentityHashMap<Tree, Tree>();
        new TreeScanner<Void, Void>() {
            @Override public Void visitWildcard(WildcardTree node, Void p) {
                Tree bound = node.getBound();

                if (bound != null && (bound.getKind() == Kind.EXTENDS_WILDCARD || bound.getKind() == Kind.SUPER_WILDCARD)) {
                    translate.put(bound, ((WildcardTree) bound).getBound());
                }
                return super.visitWildcard(node, p);
            }
        }.scan(result, null);

        return copy.getTreeUtilities().translate(result, translate);
    }
    
    private Element getImportedElement(CompilationUnitTree cut, ImportTree imp) {
        Trees trees = copy.getTrees();
        Tree qualIdent = imp.getQualifiedIdentifier();        
        if (qualIdent.getKind() != Tree.Kind.MEMBER_SELECT) {
            Element element = trees.getElement(TreePath.getPath(cut, qualIdent));
            if (element == null) {
                String fqn = qualIdent.toString();
                if (fqn.endsWith(".*")) //NOI18N
                    fqn = fqn.substring(0, fqn.length() - 2);
                element = getElementByFQN(fqn);
            }
            return element;
        }
        Name name = ((MemberSelectTree)qualIdent).getIdentifier();
        if ("*".contentEquals(name)) { //NOI18N
            Element element = trees.getElement(TreePath.getPath(cut, ((MemberSelectTree)qualIdent).getExpression()));
            if (element == null)
                element = getElementByFQN(((MemberSelectTree)qualIdent).getExpression().toString());
            return element;
        }
        if (imp.isStatic()) {
            Element parent = trees.getElement(TreePath.getPath(cut, ((MemberSelectTree)qualIdent).getExpression()));
            if (parent == null)
                parent = getElementByFQN(((MemberSelectTree)qualIdent).getExpression().toString());
            if (parent != null && (parent.getKind().isClass() || parent.getKind().isInterface())) {
                Scope s = trees.getScope(new TreePath(cut));
                for (Element e : parent.getEnclosedElements()) {
                    if (name == e.getSimpleName() && e.getModifiers().contains(Modifier.STATIC) && trees.isAccessible(s, e, (DeclaredType)parent.asType()))
                        return e;
                }
                return parent;
            }
        }
        Element element = trees.getElement(TreePath.getPath(cut, qualIdent));
        if (element == null)
            element = getElementByFQN(qualIdent.toString());
        return element;
    }
    
    private Element getElementByFQN(String fqn) {
        Elements elements = copy.getElements();
        Element element = elements.getTypeElement(fqn);
        if (element == null)
            element = elements.getPackageElement(fqn);
        if (element == null)
            element = ClassReader.instance(copy.impl.getJavacTask().getContext()).enterClass((com.sun.tools.javac.util.Name)elements.getName(fqn));
        return element;
    }
    
    private Map<Name, TypeElement> getUsedTypes(final CompilationUnitTree cut) {
        final Trees trees = copy.getTrees();
        final Map<Name, TypeElement> map = new HashMap<Name, TypeElement>();
        new TreePathScanner<Void, Void>() {

            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                if (!map.containsKey(node.getName())) {
                    Element element = trees.getElement(getCurrentPath());
                    if (element != null && (element.getKind().isClass() || element.getKind().isInterface()) && element.asType().getKind() != TypeKind.ERROR) {
                        map.put(node.getName(), (TypeElement) element);
                    }
                }
                return super.visitIdentifier(node, p);
            }

            @Override
            public Void visitCompilationUnit(CompilationUnitTree node, Void p) {
                scan(node.getPackageAnnotations(), p);
                return scan(node.getTypeDecls(), p);
            }
        }.scan(cut, null);
        return map;
    }
    
    private ExpressionTree qualIdentFor(Element e) {
        TreeMaker tm = copy.getTreeMaker();
        if (e.getKind() == ElementKind.PACKAGE) {
            String name = ((PackageElement)e).getQualifiedName().toString();
            if (e instanceof Symbol) {
                int lastDot = name.lastIndexOf('.');
                if (lastDot < 0)
                    return tm.Identifier(e);
                return tm.MemberSelect(qualIdentFor(name.substring(0, lastDot)), e);
            }
            return qualIdentFor(name);
        }
        Element ee = e.getEnclosingElement();
        if (e instanceof Symbol)
            return ee.getSimpleName().length() > 0 ? tm.MemberSelect(qualIdentFor(ee), e) : tm.Identifier(e);
        return ee.getSimpleName().length() > 0 ? tm.MemberSelect(qualIdentFor(ee), e.getSimpleName()) : tm.Identifier(e.getSimpleName());
    }
    
    private ExpressionTree qualIdentFor(String name) {
        Elements elements = copy.getElements();
        TreeMaker tm = copy.getTreeMaker();
        int lastDot = name.lastIndexOf('.');
        if (lastDot < 0)
            return tm.Identifier(elements.getName(name));
        return tm.MemberSelect(qualIdentFor(name.substring(0, lastDot)), elements.getName(name.substring(lastDot + 1)));
    }

    private Map<String, Object> createBindings(TypeElement clazz, ExecutableElement element) {
        Map<String, Object> bindings = new HashMap<String, Object>();
        bindings.put(CLASS_NAME, clazz.getQualifiedName().toString());
        bindings.put(SIMPLE_CLASS_NAME, clazz.getSimpleName().toString());
        bindings.put(METHOD_NAME, element.getSimpleName().toString());
        bindings.put(METHOD_RETURN_TYPE, element.getReturnType().toString()); //NOI18N
        Object value;
        switch(element.getReturnType().getKind()) {
            case BOOLEAN:
                value = "false"; //NOI18N
                break;
            case BYTE:
            case CHAR:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case SHORT:
                value = 0;
                break;
            default:
                value = "null"; //NOI18N
        }
        bindings.put(DEFAULT_RETURN_TYPE_VALUE, value);
        StringBuilder sb = new StringBuilder();
        if (element.isDefault() && element.getEnclosingElement().getKind().isInterface()) {
            Types types = copy.getTypes();
            TypeMirror enclType = element.getEnclosingElement().asType();
            if (!types.isSubtype(clazz.getSuperclass(), enclType)) {
                for (TypeMirror iface : clazz.getInterfaces()) {
                    if (types.isSubtype(iface, enclType)) {
                        sb.append(((DeclaredType)iface).asElement().getSimpleName()).append('.');
                        break;
                    }
                }
            }
        }
        sb.append("super.").append(element.getSimpleName()).append('('); //NOI18N
        for (Iterator<? extends VariableElement> it = element.getParameters().iterator(); it.hasNext();) {
            VariableElement ve = it.next();
            sb.append(ve.getSimpleName());
            if (it.hasNext())
                sb.append(","); //NOI18N
        }
        sb.append(')'); //NOI18N
        bindings.put(SUPER_METHOD_CALL, sb);
        return bindings;
    }

    private static String name(Tree tree) {
        switch (tree.getKind()) {
            case VARIABLE:
                return ((VariableTree)tree).getName().toString();
            case METHOD:
                return ((MethodTree)tree).getName().toString();
            case CLASS:
                return ((ClassTree)tree).getSimpleName().toString();
        }
        return ""; //NOI18N
    }

    private static String[] correspondingGSNames(Tree member) {
        if (isSetter(member)) {
            String name = name(member);
            VariableTree param = ((MethodTree)member).getParameters().get(0);
            if (param.getType().getKind() == Tree.Kind.PRIMITIVE_TYPE && ((PrimitiveTypeTree)param.getType()).getPrimitiveTypeKind() == TypeKind.BOOLEAN) {
                return new String[] {'g' + name.substring(1), "is" + name.substring(3)};
            }
            return new String[] {'g' + name.substring(1)};
        }
        if (isGetter(member)) {
            return new String[] {'s' + name(member).substring(1)};
        }
        if (isBooleanGetter(member)) {
            return new String[] {"set" + name(member).substring(2)}; //NOI18N
        }
        return null;
    }

    private static boolean isSetter(Tree member) {
        return member.getKind() == Tree.Kind.METHOD
                && name(member).startsWith("set") //NOI18N
                && ((MethodTree)member).getParameters().size() == 1
                && ((MethodTree)member).getReturnType().getKind() == Tree.Kind.PRIMITIVE_TYPE
                && ((PrimitiveTypeTree)((MethodTree)member).getReturnType()).getPrimitiveTypeKind() == TypeKind.VOID;
    }

    private static boolean isGetter(Tree member) {
        return member.getKind() == Tree.Kind.METHOD
                && name(member).startsWith("get") //NOI18N
                && ((MethodTree)member).getParameters().isEmpty()
                && (((MethodTree)member).getReturnType().getKind() != Tree.Kind.PRIMITIVE_TYPE
                || ((PrimitiveTypeTree)((MethodTree)member).getReturnType()).getPrimitiveTypeKind() != TypeKind.VOID);
    }

    private static boolean isBooleanGetter(Tree member) {
        return member.getKind() == Tree.Kind.METHOD
                && name(member).startsWith("is") //NOI18N
                && ((MethodTree)member).getParameters().isEmpty()
                && ((MethodTree)member).getReturnType().getKind() == Tree.Kind.PRIMITIVE_TYPE
                && ((PrimitiveTypeTree)((MethodTree)member).getReturnType()).getPrimitiveTypeKind() == TypeKind.BOOLEAN;
    }

    private static String removeFieldPrefixSuffix(VariableElement var, CodeStyle cs) {
        boolean isStatic = var.getModifiers().contains(Modifier.STATIC);
        return CodeStyleUtils.removePrefixSuffix(var.getSimpleName(),
                isStatic ? cs.getStaticFieldNamePrefix() : cs.getFieldNamePrefix(),
                isStatic ? cs.getStaticFieldNameSuffix() : cs.getFieldNameSuffix());
    }

    private static String removeFieldPrefixSuffix(VariableTree var, CodeStyle cs) {
        boolean isStatic = var.getModifiers().getFlags().contains(Modifier.STATIC);
        return CodeStyleUtils.removePrefixSuffix(var.getName(),
                isStatic ? cs.getStaticFieldNamePrefix() : cs.getFieldNamePrefix(),
                isStatic ? cs.getStaticFieldNameSuffix() : cs.getFieldNameSuffix());
    }

    private static String addParamPrefixSuffix(CharSequence name, CodeStyle cs) {
        return CodeStyleUtils.addPrefixSuffix(name,
                cs.getParameterNamePrefix(),
                cs.getParameterNameSuffix());
    }

    private static class ClassMemberComparator implements Comparator<Tree> {

        private final CodeStyle.MemberGroups groups;
        private final boolean sortMembersAlpha;
        private final boolean keepGASTogether;

        public ClassMemberComparator(CodeStyle cs) {
            this.groups = cs.getClassMemberGroups();
            this.sortMembersAlpha = cs.sortMembersInGroupsAlphabetically();
            this.keepGASTogether = cs.keepGettersAndSettersTogether();
        }

        @Override
        public int compare(Tree tree1, Tree tree2) {
            if (tree1 == tree2)
                return 0;
            int diff = groups.getGroupId(tree1) - groups.getGroupId(tree2);
            if (diff == 0 && sortMembersAlpha) {
                String name1 = name(tree1);
                String name2 = name(tree2);
                if (keepGASTogether) {
                    if (isSetter(tree1)) {
                        name1 = "g" + name1.substring(1) + "+1"; //NOI18N
                    }
                    if (isSetter(tree2)) {
                        name2 = "g" + name2.substring(1) + "+1"; //NOI18N
                    }
                }
                diff = name1.compareTo(name2);
            }
            return diff;
        }
    }

    private static class ImportsComparator implements Comparator<Object> {

        private final CodeStyle.ImportGroups groups;
        
        private ImportsComparator(CodeStyle cs) {
            this.groups = cs.getImportGroups();
        }

        @Override
        public int compare(Object o1, Object o2) {
            if (o1 == o2)
                return 0;
            
            boolean isStatic1 = false;
            StringBuilder sb1 = new StringBuilder();
            if (o1 instanceof ImportTree) {
                isStatic1 = ((ImportTree)o1).isStatic();
                sb1.append(((ImportTree)o1).getQualifiedIdentifier().toString());
            } else if (o1 instanceof Element) {
                Element e1 = (Element)o1;
                if (e1.getKind().isField() || e1.getKind() == ElementKind.METHOD) {
                    sb1.append('.').append(e1.getSimpleName());
                    e1 = e1.getEnclosingElement();
                    isStatic1 = true;
                }
                if (e1.getKind().isClass() || e1.getKind().isInterface()) {
                    sb1.insert(0, ((TypeElement)e1).getQualifiedName());
                } else if (e1.getKind() == ElementKind.PACKAGE) {
                    sb1.insert(0, ((PackageElement)e1).getQualifiedName());
                }
            }
            String s1 = sb1.toString();
                
            boolean isStatic2 = false;
            StringBuilder sb2 = new StringBuilder();
            if (o2 instanceof ImportTree) {
                isStatic2 = ((ImportTree)o2).isStatic();
                sb2.append(((ImportTree)o2).getQualifiedIdentifier().toString());
            } else if (o2 instanceof Element) {
                Element e2 = (Element)o2;
                if (e2.getKind().isField() || e2.getKind() == ElementKind.METHOD) {
                    sb2.append('.').append(e2.getSimpleName());
                    e2 = e2.getEnclosingElement();
                    isStatic2 = true;
                }
                if (e2.getKind().isClass() || e2.getKind().isInterface()) {
                    sb2.insert(0, ((TypeElement)e2).getQualifiedName());
                } else if (e2.getKind() == ElementKind.PACKAGE) {
                    sb2.insert(0, ((PackageElement)e2).getQualifiedName());
                }
            }
            String s2 = sb2.toString();

            int bal = groups.getGroupId(s1, isStatic1) - groups.getGroupId(s2, isStatic2);

            return bal == 0 ? s1.compareTo(s2) : bal;
        }
    }
    
    /**
     * Tags first method in the list, in order to select it later inside editor
     * @param methods list of methods to be implemented/overridden
     */
    private void tagFirst(List<MethodTree> methods) {
        //tag first method body, if any
        if (methods.size() > 0) {
            BlockTree body = methods.get(0).getBody();
            if (body != null && !body.getStatements().isEmpty()) {
                copy.tag(body.getStatements().get(0), "methodBodyTag"); // NOI18N
            }
        }
    }
    
    static boolean checkPackagesForStarImport(String pkgName, CodeStyle cs) {
        for (String s : cs.getPackagesForStarImport()) {
            if (s.endsWith(".*")) { //NOI18N
                s = s.substring(0, s.length() - 2);
                if (pkgName.startsWith(s))
                    return true;
            } else if (pkgName.equals(s)) {
                return true;
            }           
        }
        return false;
    }
    
    private static final String GENERATED_METHOD_BODY = "Templates/Classes/Code/GeneratedMethodBody"; //NOI18N
    private static final String OVERRIDDEN_METHOD_BODY = "Templates/Classes/Code/OverriddenMethodBody"; //NOI18N
    private static final String METHOD_RETURN_TYPE = "method_return_type"; //NOI18N
    private static final String DEFAULT_RETURN_TYPE_VALUE = "default_return_value"; //NOI18N
    private static final String SUPER_METHOD_CALL = "super_method_call"; //NOI18N
    private static final String METHOD_NAME = "method_name"; //NOI18N
    private static final String CLASS_NAME = "class_name"; //NOI18N
    private static final String SIMPLE_CLASS_NAME = "simple_class_name"; //NOI18N
    private static final String SCRIPT_ENGINE_ATTR = "javax.script.ScriptEngine"; //NOI18N    
    private static final String STRING_OUTPUT_MODE_ATTR = "com.sun.script.freemarker.stringOut"; //NOI18N
    private static ScriptEngineManager manager;

    private static String readFromTemplate(String pathToTemplate, Map<String, Object> values) throws IOException, ScriptException {
        FileObject template = FileUtil.getConfigFile(pathToTemplate);
        Charset sourceEnc = FileEncodingQuery.getEncoding(template);

        ScriptEngine eng = engine(template);
        Bindings bind = eng.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        bind.putAll(values);

        Reader is = null;
        try {
            eng.getContext().setAttribute(FileObject.class.getName(), template, ScriptContext.ENGINE_SCOPE);
            eng.getContext().setAttribute(ScriptEngine.FILENAME, template.getNameExt(), ScriptContext.ENGINE_SCOPE);
            eng.getContext().setAttribute(STRING_OUTPUT_MODE_ATTR, true, ScriptContext.ENGINE_SCOPE);
            is = new InputStreamReader(template.getInputStream(), sourceEnc);
            return (String)eng.eval(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private static ScriptEngine engine(FileObject fo) {
        Object obj = fo.getAttribute(SCRIPT_ENGINE_ATTR); // NOI18N
        if (obj instanceof ScriptEngine) {
            return (ScriptEngine) obj;
        }
        if (obj instanceof String) {
            synchronized (GeneratorUtilities.class) {
                if (manager == null) {
                    ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
                    manager = new ScriptEngineManager(loader != null ? loader : Thread.currentThread().getContextClassLoader());
                }
            }
            return manager.getEngineByName((String) obj);
        }
        return null;
    }
    
    private static boolean containsErrors(Tree tree) {
        Boolean b = new TreeScanner<Boolean, Boolean>() {
            @Override
            public Boolean visitErroneous(ErroneousTree node, Boolean p) {
                return true;
            }

            @Override
            public Boolean reduce(Boolean r1, Boolean r2) {
                if (r1 == null)
                    r1 = false;
                if (r2 == null)
                    r2 = false;
                return r1 || r2;
            }

            @Override
            public Boolean scan(Tree node, Boolean p) {
                return p ? p : super.scan(node, p);
            }
        }.scan(tree, false);
        return b != null ? b : false;
    }
}
