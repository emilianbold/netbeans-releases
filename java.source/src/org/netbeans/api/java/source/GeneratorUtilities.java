/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.java.source;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.transform.ImmutableTreeTranslator;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;

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
        for (Tree tree : clazz.getMembers()) {
            if (ClassMemberComparator.compare(member, tree) < 0)
                break;
            idx++;
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
        if (constructor != null && !constructor.getParameters().isEmpty()) {
            List<ExpressionTree> arguments = new ArrayList<ExpressionTree>();
            for (VariableElement ve : constructor.getParameters()) {
                parameters.add(make.Variable(parameterModifiers, ve.getSimpleName(), make.Type(ve.asType()), null));
                arguments.add(make.Identifier(ve.getSimpleName())); //NOI18N
            }
            statements.add(make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.Identifier("super"), arguments))); //NOI18N
        }
        for (VariableElement ve : fields) {
            TypeMirror type = copy.getTypes().asMemberOf((DeclaredType)clazz.asType(), ve);
            parameters.add(make.Variable(parameterModifiers, ve.getSimpleName(), make.Type(type), null));
            statements.add(make.ExpressionStatement(make.Assignment(make.MemberSelect(make.Identifier("this"), ve.getSimpleName()), make.Identifier(ve.getSimpleName())))); //NOI18N
        }
        BlockTree body = make.Block(statements, false);
        return make.Method(make.Modifiers(mods), "<init>", null, Collections.<TypeParameterTree> emptyList(), parameters, Collections.<ExpressionTree>emptyList(), body, null); //NOI18N
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
        StringBuilder sb = new StringBuilder();
        sb.append(type.getKind() == TypeKind.BOOLEAN ? "is" : "get").append(Character.toUpperCase(name.charAt(0))).append(name.subSequence(1, name.length())); //NOI18N
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
        StringBuilder sb = new StringBuilder();
        sb.append(type.getKind() == Tree.Kind.PRIMITIVE_TYPE && ((PrimitiveTypeTree)type).getPrimitiveTypeKind() == TypeKind.BOOLEAN ? "is" : "get").append(Character.toUpperCase(name.charAt(0))).append(name.subSequence(1, name.length())); //NOI18N
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
        StringBuilder sb = new StringBuilder();
        sb.append("set").append(Character.toUpperCase(name.charAt(0))).append(name.subSequence(1, name.length())); //NOI18N
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
        StringBuilder sb = new StringBuilder();
        sb.append("set").append(Character.toUpperCase(name.charAt(0))).append(name.subSequence(1, name.length())); //NOI18N
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
    public Tree importFQNs(Tree original) {
        ImmutableTreeTranslator translator = new ImmutableTreeTranslator() {
            @Override
            public MemberSelectTree visitMemberSelect(MemberSelectTree tree, Object o) {
                super.visitMemberSelect(tree, o);
                TypeElement e = copy.getElements().getTypeElement(tree.toString());
                if (e != null) {
                    return (MemberSelectTree) copy.getTreeMaker().QualIdent(e);
                } else {
                    return tree;
                }
            }
        };
        translator.attach(JavaSourceAccessor.INSTANCE.getCommandEnvironment(copy));
        Tree rewritten = translator.translate(original);
        translator.release();
        return rewritten;
    }

    // private implementation --------------------------------------------------
    
    private MethodTree createMethod(ExecutableElement element, DeclaredType type) {
        TreeMaker make = copy.getTreeMaker();
        Set<Modifier> mods = element.getModifiers();
        Set<Modifier> flags = mods.isEmpty() ? EnumSet.noneOf(Modifier.class) : EnumSet.copyOf(mods);
        boolean isAbstract = flags.remove(Modifier.ABSTRACT);
        flags.remove(Modifier.NATIVE);
        
        ExecutableType et = (ExecutableType)element.asType();
        try {
            et = (ExecutableType)copy.getTypes().asMemberOf(type, element);
        } catch (IllegalArgumentException iae) {}
        List<TypeParameterTree> typeParams = new ArrayList<TypeParameterTree>();
        for (TypeParameterElement tpe: element.getTypeParameters()) {
            List<ExpressionTree> bounds = new ArrayList<ExpressionTree>();
            for (TypeMirror bound : tpe.getBounds()) {
                if (bound.getKind() != TypeKind.NULL) {
                    //if the bound is java.lang.Object, do not generate the extends clause:
                    if (bound.getKind() != TypeKind.DECLARED || !"java.lang.Object".contentEquals(((TypeElement)((DeclaredType)bound).asElement()).getQualifiedName())) //NOI18N
                        bounds.add((ExpressionTree)make.Type(bound));
                }
            }            
            typeParams.add(make.TypeParameter(tpe.getSimpleName(), bounds));
        }

        Tree returnType = make.Type(et.getReturnType());

        List<VariableTree> params = new ArrayList<VariableTree>();        
        boolean isVarArgs = element.isVarArgs();
        Iterator<? extends VariableElement> formArgNames = element.getParameters().iterator();
        Iterator<? extends TypeMirror> formArgTypes = et.getParameterTypes().iterator();
        ModifiersTree parameterModifiers = make.Modifiers(EnumSet.noneOf(Modifier.class));
        while (formArgNames.hasNext() && formArgTypes.hasNext()) {
            VariableElement formArgName = formArgNames.next();
            TypeMirror formArgType = formArgTypes.next();
            if (isVarArgs && !formArgNames.hasNext())
                parameterModifiers = make.Modifiers(1L<<34, Collections.<AnnotationTree>emptyList());
            params.add(make.Variable(parameterModifiers, formArgName.getSimpleName(), make.Type(formArgType), null));
        }

        List<ExpressionTree> throwsList = new ArrayList<ExpressionTree>();
        for (TypeMirror tm : et.getThrownTypes()) {
            throwsList.add((ExpressionTree)make.Type(tm));
        }
        
        BlockTree body;
        List<AnnotationTree> annotations = new ArrayList<AnnotationTree>();
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
            
            //add @Override annotation if developing for 1.5:
            if (supportsOverride(copy.getFileObject())) {
                annotations.add(make.Annotation(make.Identifier("Override"), Collections.<ExpressionTree>emptyList())); //NOI18N
            }
        }

        return make.Method(make.Modifiers(flags, annotations), element.getSimpleName(), returnType, typeParams, params, throwsList, body, null);
    }
    
    private static boolean supportsOverride(FileObject fo) {
        SpecificationVersion myVersion = new SpecificationVersion(SourceLevelQuery.getSourceLevel(fo));
        SpecificationVersion version = new SpecificationVersion("1.5"); //NOI18N
        return myVersion.compareTo(version) >= 0;
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
    
}
