/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Types;
import javax.swing.text.Document;

import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.modules.java.source.parsing.SourceFileObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;

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
        int idx = 0;
        GuardedDocument gdoc = null;
        SourcePositions sp = null;
        try {
            Document doc = copy.getDocument();
            if (doc != null && doc instanceof GuardedDocument) {
                gdoc = (GuardedDocument)doc;
                sp = copy.getTrees().getSourcePositions();
            }
        } catch (IOException ioe) {}
        TreeUtilities utils = copy.getTreeUtilities();
        CompilationUnitTree compilationUnit = copy.getCompilationUnit();
        Tree lastMember = null;
        for (Tree tree : clazz.getMembers()) {
            TreePath path = TreePath.getPath(compilationUnit, tree);
            if ((path == null || !utils.isSynthetic(path)) && ClassMemberComparator.compare(member, tree) < 0) {
                if (gdoc == null)
                    break;
                int pos = (int)(lastMember != null ? sp.getEndPosition(compilationUnit, lastMember) : sp.getStartPosition( compilationUnit,clazz));
                pos = gdoc.getGuardedBlockChain().adjustToBlockEnd(pos);
                if (pos <= sp.getStartPosition(compilationUnit, tree))
                    break;
            }
            idx++;
            lastMember = tree;
        }
        return copy.getTreeMaker().insertClassMember(clazz, idx, member);
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
        return createMethod(method, (DeclaredType)clazz.asType());
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
        return createMethod(method, (DeclaredType)clazz.asType());
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
            params.add(make.Variable(parameterModifiers, formArgName.getSimpleName(), make.Type(formArgType), null));
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
        assert clazz != null && fields != null;
        TreeMaker make = copy.getTreeMaker();
        Set<Modifier> mods = EnumSet.of(clazz.getKind() == ElementKind.ENUM ? Modifier.PRIVATE : Modifier.PUBLIC);
        List<VariableTree> parameters = new ArrayList<VariableTree>();
        List<StatementTree> statements = new ArrayList<StatementTree>();
        ModifiersTree parameterModifiers = make.Modifiers(EnumSet.noneOf(Modifier.class));
        List<ExpressionTree> throwsList = new LinkedList<ExpressionTree>();
        if (constructor != null) {
            ExecutableType constructorType = clazz.getSuperclass().getKind() == TypeKind.DECLARED ? (ExecutableType) copy.getTypes().asMemberOf((DeclaredType) clazz.getSuperclass(), constructor) : null;
            if (!constructor.getParameters().isEmpty()) {
                List<ExpressionTree> arguments = new ArrayList<ExpressionTree>();
                Iterator<? extends VariableElement> parameterElements = constructor.getParameters().iterator();
                Iterator<? extends TypeMirror> parameterTypes = constructorType != null ? constructorType.getParameterTypes().iterator() : null;
                while (parameterElements.hasNext()) {
                    VariableElement ve = parameterElements.next();
                    Name simpleName = ve.getSimpleName();
                    TypeMirror type = parameterTypes != null ? parameterTypes.next() : ve.asType();

                    parameters.add(make.Variable(parameterModifiers, simpleName, make.Type(type), null));
                    arguments.add(make.Identifier(simpleName));
                }
                statements.add(make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier("super"), arguments))); //NOI18N
            }
            constructorType = constructorType != null ? constructorType : (ExecutableType) constructor.asType();
            for (TypeMirror th : constructorType.getThrownTypes()) {
                throwsList.add((ExpressionTree) make.Type(th));
            }
        }
        for (VariableElement ve : fields) {
            TypeMirror type = copy.getTypes().asMemberOf((DeclaredType)clazz.asType(), ve);
            parameters.add(make.Variable(parameterModifiers, ve.getSimpleName(), make.Type(type), null));
            statements.add(make.ExpressionStatement(make.Assignment(make.MemberSelect(make.Identifier("this"), ve.getSimpleName()), make.Identifier(ve.getSimpleName())))); //NOI18N
        }
        BlockTree body = make.Block(statements, false);
        return make.Method(make.Modifiers(mods), "<init>", null, Collections.<TypeParameterTree> emptyList(), parameters, throwsList, body, null); //NOI18N
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
        Set<Modifier> mods = EnumSet.of(copy.getTreeUtilities().isEnum(clazz) ? Modifier.PRIVATE : Modifier.PUBLIC);
        List<VariableTree> parameters = new ArrayList<VariableTree>();
        List<StatementTree> statements = new ArrayList<StatementTree>();
        ModifiersTree parameterModifiers = make.Modifiers(EnumSet.noneOf(Modifier.class));
        for (VariableTree vt : fields) {
            parameters.add(make.Variable(parameterModifiers, vt.getName(), vt.getType(), null));
            statements.add(make.ExpressionStatement(make.Assignment(make.MemberSelect(make.Identifier("this"), vt.getName()), make.Identifier(vt.getName())))); //NOI18N
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
        Set<Modifier> mods = EnumSet.of(Modifier.PUBLIC);
        if (field.getModifiers().contains(Modifier.STATIC))
            mods.add(Modifier.STATIC);
        CharSequence name = field.getSimpleName();
        assert name.length() > 0;
        TypeMirror type = copy.getTypes().asMemberOf((DeclaredType)clazz.asType(), field);
        StringBuilder sb = getCapitalizedName(name);
        sb.insert(0, type.getKind() == TypeKind.BOOLEAN ? "is" : "get"); //NOI18N
        BlockTree body = make.Block(Collections.singletonList(make.Return(make.Identifier(name))), false);
        return make.Method(make.Modifiers(mods), sb, make.Type(type), Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), body, null);
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
        Set<Modifier> mods = EnumSet.of(Modifier.PUBLIC);
        if (field.getModifiers().getFlags().contains(Modifier.STATIC))
            mods.add(Modifier.STATIC);
        CharSequence name = field.getName();
        assert name.length() > 0;
        Tree type = field.getType();
        StringBuilder sb = getCapitalizedName(name);
        sb.insert(0, type.getKind() == Tree.Kind.PRIMITIVE_TYPE && ((PrimitiveTypeTree)type).getPrimitiveTypeKind() == TypeKind.BOOLEAN ? "is" : "get"); //NOI18N
        BlockTree body = make.Block(Collections.singletonList(make.Return(make.Identifier(name))), false);
        return make.Method(make.Modifiers(mods), sb, type, Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), body, null);
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
        Set<Modifier> mods = EnumSet.of(Modifier.PUBLIC);
        boolean isStatic = field.getModifiers().contains(Modifier.STATIC);
        if (isStatic)
            mods.add(Modifier.STATIC);
        CharSequence name = field.getSimpleName();
        assert name.length() > 0;
        TypeMirror type = copy.getTypes().asMemberOf((DeclaredType)clazz.asType(), field);
        StringBuilder sb = getCapitalizedName(name);
        sb.insert(0, "set"); //NOI18N
        List<VariableTree> params = Collections.singletonList(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, make.Type(type), null));
        BlockTree body = make.Block(Collections.singletonList(make.ExpressionStatement(make.Assignment(make.MemberSelect(isStatic? make.Identifier(field.getEnclosingElement().getSimpleName()) : make.Identifier("this"), name), make.Identifier(name)))), false); //NOI18N
        return make.Method(make.Modifiers(mods), sb, make.Type(copy.getTypes().getNoType(TypeKind.VOID)), Collections.<TypeParameterTree>emptyList(), params, Collections.<ExpressionTree>emptyList(), body, null);
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
        StringBuilder sb = getCapitalizedName(name);
        sb.insert(0, "set"); //NOI18N
        List<VariableTree> params = Collections.singletonList(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), name, field.getType(), null));
        BlockTree body = make.Block(Collections.singletonList(make.ExpressionStatement(make.Assignment(make.MemberSelect(isStatic? make.Identifier(clazz.getSimpleName()) : make.Identifier("this"), name), make.Identifier(name)))), false); //NOI18N
        return make.Method(make.Modifiers(mods), sb, make.Type(copy.getTypes().getNoType(TypeKind.VOID)), Collections.<TypeParameterTree>emptyList(), params, Collections.<ExpressionTree>emptyList(), body, null);
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
        TranslateIdentifier translator = new TranslateIdentifier(copy, false, true, null);
        return (T) translator.translate(original);
    }

    public <T extends Tree> T importComments(T original, CompilationUnitTree cut) {
        try {
            JCTree.JCCompilationUnit unit = (JCCompilationUnit) cut;            
            TokenSequence<JavaTokenId> seq = ((SourceFileObject) unit.getSourceFile()).getTokenHierarchy().tokenSequence(JavaTokenId.language());
            TranslateIdentifier translator = new TranslateIdentifier(copy, true, false, seq);
            return (T) translator.translate(original);            
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return original;
    }
    
    
    // private implementation --------------------------------------------------

    private MethodTree createMethod(ExecutableElement element, DeclaredType type) {
        TreeMaker make = copy.getTreeMaker();
        boolean isAbstract = element.getModifiers().contains(Modifier.ABSTRACT);

        BlockTree body;
        if (isAbstract) {
            List<StatementTree> blockStatements = new ArrayList<StatementTree>();
            TypeElement uoe = copy.getElements().getTypeElement("java.lang.UnsupportedOperationException"); //NOI18N
            //TODO: if uoe == null: cannot resolve UnsupportedOperationException for some reason, create a different body in such a case
            if (uoe != null) {
                NewClassTree nue = make.NewClass(null, Collections.<ExpressionTree>emptyList(), make.QualIdent(uoe), Collections.singletonList(make.Literal("Not supported yet.")), null);
                blockStatements.add(make.Throw(nue));
            }
            body = make.Block(blockStatements, false);
        } else {
            List<ExpressionTree> arguments = new ArrayList<ExpressionTree>();
            for (VariableElement ve : element.getParameters()) {
                arguments.add(make.Identifier(ve.getSimpleName()));
            }
            MethodInvocationTree inv = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.Identifier("super"), element.getSimpleName()), arguments); //NOI18N
            StatementTree statement = copy.getTypes().getNoType(TypeKind.VOID) == element.getReturnType() ?
                make.ExpressionStatement(inv) : make.Return(inv);
            body = make.Block(Collections.singletonList(statement), false);
        }

        MethodTree prototype = createMethod(type, element);
        ModifiersTree mt = prototype.getModifiers();

        //add @Override annotation:
        SpecificationVersion thisFOVersion = new SpecificationVersion(SourceLevelQuery.getSourceLevel(copy.getFileObject()));
        SpecificationVersion version15 = new SpecificationVersion("1.5"); //NOI18N

        if (thisFOVersion.compareTo(version15) >= 0) {
            boolean generate = true;

            if (thisFOVersion.compareTo(version15) == 0) {
                generate = !element.getEnclosingElement().getKind().isInterface();
            }

            if (generate) {
               mt = make.addModifiersAnnotation(prototype.getModifiers(), make.Annotation(make.Identifier("Override"), Collections.<ExpressionTree>emptyList()));
            }
        }

        return make.Method(mt, prototype.getName(), prototype.getReturnType(), prototype.getTypeParameters(), prototype.getParameters(), prototype.getThrows(), body, null);
    }

    private static StringBuilder getCapitalizedName(CharSequence cs) {
        StringBuilder sb = new StringBuilder(cs);
        while (sb.length() > 1 && sb.charAt(0) == '_') { //NOI18N
            sb.deleteCharAt(0);
        }

        //Beans naming convention, #165241
        if (sb.length() > 1 && Character.isUpperCase(sb.charAt(1))) {
            return sb;
        }

        if (sb.length() > 0) {
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        }
        return sb;
    }

    private static class ClassMemberComparator {

        public static int compare(Tree tree1, Tree tree2) {
            if (tree1 == tree2)
                return 0;
            return getSortPriority(tree1) - getSortPriority(tree2);
        }

        private static int getSortPriority(Tree tree) {
            int ret = 0;
            ModifiersTree modifiers = null;
            switch (tree.getKind()) {
            case CLASS:
                ret = 4000;
                modifiers = ((ClassTree)tree).getModifiers();
                break;
            case METHOD:
                MethodTree mt = (MethodTree)tree;
                if (mt.getName().contentEquals("<init>"))
                    ret = 200;
                else
                    ret = 300;
                modifiers = mt.getModifiers();
                break;
            case VARIABLE:
                ret = 100;
                modifiers = ((VariableTree)tree).getModifiers();
                break;
            }
            if (modifiers != null) {
                if (!modifiers.getFlags().contains(Modifier.STATIC))
                    ret += 1000;
            }
            return ret;
        }
    }

    /**
     * Tags first method in the list, in order to select it later inside editor
     * @param methods list of methods to be implemented/overriden
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
}
