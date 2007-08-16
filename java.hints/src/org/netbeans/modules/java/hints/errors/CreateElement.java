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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.hints.errors.CreateClassFix.CreateInnerClassFix;
import org.netbeans.modules.java.hints.errors.CreateClassFix.CreateOuterClassFix;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.netbeans.modules.java.hints.infrastructure.Pair;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import static org.netbeans.modules.java.hints.errors.CreateElementUtilities.*;

/**
 *
 * @author Jan Lahoda
 */
public final class CreateElement implements ErrorRule<Void> {

    /** Creates a new instance of CreateElement */
    public CreateElement() {
    }
    
    public Set<String> getCodes() {
        return new HashSet<String>(Arrays.asList("compiler.err.cant.resolve.location", "compiler.err.cant.apply.symbol", "compiler.err.cant.resolve")); // NOI18N
    }
    
    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        try {
            return analyze(info, offset);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }
    
    static List<Fix> analyze(CompilationInfo info, int offset) throws IOException {
        TreePath errorPath = ErrorHintsProvider.findUnresolvedElement(info, offset);
        
        if (errorPath == null) {
            return Collections.<Fix>emptyList();
        }
        
        TreePath parent = null;
        TreePath firstClass = null;
        TreePath firstMethod = null;
        TreePath firstInitializer = null;
        TreePath methodInvocation = null;
        TreePath newClass = null;
        boolean lookupMethodInvocation = true;
        boolean lookupNCT = true;
        
        TreePath path = info.getTreeUtilities().pathFor(offset + 1);
        
        while(path != null) {
            Tree leaf = path.getLeaf();
            Kind leafKind = leaf.getKind();
            
            if (parent != null && parent.getLeaf() == errorPath.getLeaf())
                parent = path;
            if (leaf == errorPath.getLeaf() && parent == null)
                parent = path;
            if (leafKind == Kind.CLASS && firstClass == null)
                firstClass = path;
            if (leafKind == Kind.METHOD && firstMethod == null && firstClass == null)
                firstMethod = path;
            //static/dynamic initializer:
            if (   leafKind == Kind.BLOCK && path.getParentPath().getLeaf().getKind() == Kind.CLASS
                && firstMethod == null && firstClass == null)
                firstInitializer = path;
            
            if (lookupMethodInvocation && leafKind == Kind.METHOD_INVOCATION) {
                methodInvocation = path;
            }
            
            if (lookupNCT && leafKind == Kind.NEW_CLASS) {
                newClass = path;
            }
            
            if (leafKind == Kind.MEMBER_SELECT) {
                lookupMethodInvocation = leaf == errorPath.getLeaf();
            }
            
            if (leafKind != Kind.MEMBER_SELECT && leafKind != Kind.IDENTIFIER) {
                lookupMethodInvocation = false;
            }
            
            if (leafKind != Kind.MEMBER_SELECT && leafKind != Kind.IDENTIFIER && leafKind != Kind.PARAMETERIZED_TYPE) {
                lookupNCT = false;
            }
            
            path = path.getParentPath();
        }
        
        if (parent == null || parent.getLeaf() == errorPath.getLeaf() || firstClass == null)
            return Collections.<Fix>emptyList();
        
        Element e = info.getTrees().getElement(errorPath);
        
        if (e == null) {
            return Collections.<Fix>emptyList();
        }
        
        Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
        String simpleName = e.getSimpleName().toString();
        TypeElement source = (TypeElement) info.getTrees().getElement(firstClass);
        TypeElement target = null;
        boolean wasMemberSelect = false;
        
        if (errorPath.getLeaf().getKind() == Kind.MEMBER_SELECT) {
            TreePath exp = new TreePath(errorPath, ((MemberSelectTree) errorPath.getLeaf()).getExpression());
            Element targetElement = info.getTrees().getElement(exp);
            TypeMirror targetType = info.getTrees().getTypeMirror(exp);
            
            if (targetElement != null && targetType != null && targetType.getKind() != TypeKind.ERROR) {
                switch (targetElement.getKind()) {
                    case CLASS:
                    case INTERFACE:
                    case ENUM:
                    case ANNOTATION_TYPE:
                        //situation like <something>.ClassName.<identifier>,
                        //targetElement representing <something>.ClassName:
                        //the new element needs to be static
                        target = (TypeElement) targetElement;
                        modifiers.add(Modifier.STATIC);
                        break;
                        
                    case FIELD:
                    case ENUM_CONSTANT:
                    case LOCAL_VARIABLE:
                    case PARAMETER:
                    case EXCEPTION_PARAMETER:
                        TypeMirror tm = targetElement.asType();
                        if (tm.getKind() == TypeKind.DECLARED) {
                            target = (TypeElement)((DeclaredType)tm).asElement();
                        }
                        break;
                    case METHOD:
                        Element el = info.getTypes().asElement(((ExecutableElement) targetElement).getReturnType());
                        
                        if (el != null && (el.getKind().isClass() || el.getKind().isInterface())) {
                            target = (TypeElement) el;
                        }
                        
                        break;
                    case CONSTRUCTOR:
                        target = (TypeElement) targetElement.getEnclosingElement();
                        break;
                    //TODO: type parameter?
                }
            }
            
            wasMemberSelect = true;
        } else {
            if (errorPath.getLeaf().getKind() == Kind.IDENTIFIER) {
                target = source;
                
                if (firstMethod != null) {
                    if (((MethodTree)firstMethod.getLeaf()).getModifiers().getFlags().contains(Modifier.STATIC)) {
                        modifiers.add(Modifier.STATIC);
                    }
                } else {
                    //TODO: outside of any method...
                }
            }
        }
        
        if (target == null) {
            if (ErrorHintsProvider.ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ErrorHintsProvider.ERR.log(ErrorManager.INFORMATIONAL, "target=null"); // NOI18N
                ErrorHintsProvider.ERR.log(ErrorManager.INFORMATIONAL, "offset=" + offset); // NOI18N
                ErrorHintsProvider.ERR.log(ErrorManager.INFORMATIONAL, "errorTree=" + errorPath.getLeaf()); // NOI18N
            }
            
            return Collections.<Fix>emptyList();
        }
        
        modifiers.addAll(getAccessModifiers(info, source, target));
        
        if (methodInvocation != null) {
            //create method:
            MethodInvocationTree mit = (MethodInvocationTree) methodInvocation.getLeaf();
            //return type:
            Set<ElementKind> fixTypes = EnumSet.noneOf(ElementKind.class);
            List<? extends TypeMirror> types = resolveType(fixTypes, info, methodInvocation.getParentPath(), methodInvocation.getLeaf(), offset, null, null);
            
            if (types == null || types.isEmpty()) {
                return Collections.<Fix>emptyList();
            }
            return prepareCreateMethodFix(info, methodInvocation, modifiers, target, simpleName, mit.getArguments(), types);
        }
        
        if (newClass != null) {
            //create method:
            NewClassTree nct = (NewClassTree) newClass.getLeaf();
            Element clazz = info.getTrees().getElement(new TreePath(newClass, nct.getIdentifier()));
            
            if (clazz == null || clazz.asType().getKind() == TypeKind.ERROR || (!clazz.getKind().isClass() && !clazz.getKind().isInterface())) {
                //the class does not exist...
                ExpressionTree ident = nct.getIdentifier();
                int numTypeArguments = 0;
                
                if (ident.getKind() == Kind.PARAMETERIZED_TYPE) {
                    numTypeArguments = ((ParameterizedTypeTree) ident).getTypeArguments().size();
                }
                
                if (wasMemberSelect) {
                    return prepareCreateInnerClassFix(info, newClass, target, modifiers, simpleName, nct.getArguments(), null, ElementKind.CLASS, numTypeArguments);
                } else {
                    return prepareCreateOuterClassFix(info, newClass, source, EnumSet.noneOf(Modifier.class), simpleName, nct.getArguments(), null, ElementKind.CLASS, numTypeArguments);
                }
            }
            
            if (nct.getClassBody() != null) {
                return Collections.<Fix>emptyList();
            }
            
            target = (TypeElement) clazz;
            
            return prepareCreateMethodFix(info, newClass, getAccessModifiers(info, source, target), target, "<init>", nct.getArguments(), null);
        }
        
        //field like or class (type):
        List<Fix> result = new ArrayList<Fix>();

        Set<ElementKind> fixTypes = EnumSet.noneOf(ElementKind.class);
        TypeMirror[] typeParameterBound = new TypeMirror[1];
        int[] numTypeParameters = new int[1];
        List<? extends TypeMirror> types = resolveType(fixTypes, info, parent, errorPath.getLeaf(), offset, typeParameterBound, numTypeParameters);
        ElementKind classType = getClassType(fixTypes);
        
        if (classType != null) {
            if (wasMemberSelect) {
                result.addAll(prepareCreateInnerClassFix(info, null, target, modifiers, simpleName, null, typeParameterBound[0], classType, numTypeParameters[0]));
            } else {
                result.addAll(prepareCreateOuterClassFix(info, null, source, EnumSet.noneOf(Modifier.class), simpleName, null, typeParameterBound[0], classType, numTypeParameters[0]));
            }
        }
        
        if (types == null || types.isEmpty()) {
            return result;
        }

        //XXX: should reasonably consider all the found type candidates, not only the one:
        TypeMirror type = types.get(0);

        if (type == null || type.getKind() == TypeKind.VOID || type.getKind() == TypeKind.EXECUTABLE) {
            return result;
        }

        //currently, we cannot handle error types, TYPEVARs and WILDCARDs:
        if (containsErrorsOrTypevarsRecursively(type)) {
            return result;
        }

        if (fixTypes.contains(ElementKind.FIELD) && isTargetWritable(target, info)) { //IZ 111048 -- don't offer anything if target file isn't writable
            result.add(new CreateFieldFix(info, simpleName, modifiers, target, type));
        }

        if (!wasMemberSelect && (fixTypes.contains(ElementKind.LOCAL_VARIABLE) || types.contains(ElementKind.PARAMETER))) {
            ExecutableElement ee = null;

            if (firstMethod != null) {
                ee = (ExecutableElement) info.getTrees().getElement(firstMethod);
            }

            if ((ee != null) && type != null) {
                int identifierPos = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), errorPath.getLeaf());
                if (ee != null && fixTypes.contains(ElementKind.PARAMETER) && !Utilities.isMethodHeaderInsideGuardedBlock(info, (MethodTree) firstMethod.getLeaf()))
                    result.add(new AddParameterOrLocalFix(info, type, simpleName, true, identifierPos));
                if (fixTypes.contains(ElementKind.LOCAL_VARIABLE))
                    result.add(new AddParameterOrLocalFix(info, type, simpleName, false, identifierPos));
            }
        }
        
        return result;
    }
    
    private static List<Fix> prepareCreateMethodFix(CompilationInfo info, TreePath invocation, Set<Modifier> modifiers, TypeElement target, String simpleName, List<? extends ExpressionTree> arguments, List<? extends TypeMirror> returnTypes) {
        //create method:
        Pair<List<? extends TypeMirror>, List<String>> formalArguments = resolveArguments(info, invocation, arguments);
        
        //return type:
        //XXX: should reasonably consider all the found type candidates, not only the one:
        TypeMirror returnType = returnTypes != null ? returnTypes.get(0) : null;
        
        //currently, we cannot handle error types, TYPEVARs and WILDCARDs:
        if (formalArguments == null || returnType != null && containsErrorsOrTypevarsRecursively(returnType)) {
            return Collections.<Fix>emptyList();
        }
	
	//IZ 111048 -- don't offer anything if target file isn't writable
	if(!isTargetWritable(target, info))
	    return Collections.<Fix>emptyList();
        
        return Collections.<Fix>singletonList(new CreateMethodFix(info, simpleName, modifiers, target, returnType, formalArguments.getA(), formalArguments.getB()));
    }
    
    private static Pair<List<? extends TypeMirror>, List<String>> resolveArguments(CompilationInfo info, TreePath invocation, List<? extends ExpressionTree> realArguments) {
        List<TypeMirror> argumentTypes = new LinkedList<TypeMirror>();
        List<String>     argumentNames = new LinkedList<String>();
        Set<String>      usedArgumentNames = new HashSet<String>();
        
        for (ExpressionTree arg : realArguments) {
            TypeMirror tm = info.getTrees().getTypeMirror(new TreePath(invocation, arg));
            
            if (tm == null || containsErrorsOrTypevarsRecursively(tm)) {
                return null;
            }
            
            if (tm.getKind() == TypeKind.NULL) {
                tm = info.getElements().getTypeElement("java.lang.Object").asType(); // NOI18N
            }
            
            argumentTypes.add(tm);
            
            String proposedName = org.netbeans.modules.java.hints.errors.Utilities.getName(arg);
            
            if (proposedName == null) {
                proposedName = org.netbeans.modules.java.hints.errors.Utilities.getName(tm);
            }
            
            if (proposedName == null) {
                proposedName = "arg"; // NOI18N
            }
            
            if (usedArgumentNames.contains(proposedName)) {
                int num = 0;
                
                while (usedArgumentNames.contains(proposedName + num)) {
                    num++;
                }
                
                proposedName = proposedName + num;
            }
            
            usedArgumentNames.add(proposedName);
            
            argumentNames.add(proposedName);
        }
        
        return new Pair<List<? extends TypeMirror>, List<String>>(argumentTypes, argumentNames);
    }
    
    private static List<Fix> prepareCreateOuterClassFix(CompilationInfo info, TreePath invocation, TypeElement source, Set<Modifier> modifiers, String simpleName, List<? extends ExpressionTree> realArguments, TypeMirror superType, ElementKind kind, int numTypeParameters) {
        Pair<List<? extends TypeMirror>, List<String>> formalArguments = invocation != null ? resolveArguments(info, invocation, realArguments) : new Pair<List<? extends TypeMirror>, List<String>>(null, null);
        
        if (formalArguments == null) {
            return Collections.<Fix>emptyList();
        }
        
        ClassPath cp = info.getClasspathInfo().getClassPath(PathKind.SOURCE);
        FileObject root = cp.findOwnerRoot(info.getFileObject());
        TypeElement outer = info.getElementUtilities().outermostTypeElement(source);
        PackageElement packageElement = (PackageElement) outer.getEnclosingElement();
        
        return Collections.<Fix>singletonList(new CreateOuterClassFix(info, root, packageElement.getQualifiedName().toString(), simpleName, modifiers, formalArguments.getA(), formalArguments.getB(), superType, kind, numTypeParameters));
    }
    
    private static List<Fix> prepareCreateInnerClassFix(CompilationInfo info, TreePath invocation, TypeElement target, Set<Modifier> modifiers, String simpleName, List<? extends ExpressionTree> realArguments, TypeMirror superType, ElementKind kind, int numTypeParameters) {
        Pair<List<? extends TypeMirror>, List<String>> formalArguments = invocation != null ? resolveArguments(info, invocation, realArguments) : new Pair<List<? extends TypeMirror>, List<String>>(null, null);
        
        if (formalArguments == null) {
            return Collections.<Fix>emptyList();
        }
	
	//IZ 111048 -- don't offer anything if target file isn't writable
	if (!isTargetWritable(target, info))
	    return Collections.<Fix>emptyList();
        
        return Collections.<Fix>singletonList(new CreateInnerClassFix(info, simpleName, modifiers, target, formalArguments.getA(), formalArguments.getB(), superType, kind, numTypeParameters));
    }
    
    private static ElementKind getClassType(Set<ElementKind> types) {
        if (types.contains(ElementKind.CLASS))
            return ElementKind.CLASS;
        if (types.contains(ElementKind.ANNOTATION_TYPE))
            return ElementKind.ANNOTATION_TYPE;
        if (types.contains(ElementKind.INTERFACE))
            return ElementKind.INTERFACE;
        if (types.contains(ElementKind.ENUM))
            return ElementKind.ENUM;
        
        return null;
    }
    
    public void cancel() {
        //XXX: not done yet
    }
    
    public String getId() {
        return CreateElement.class.getName();
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(CreateElement.class, "LBL_Create_Field");
    }
    
    public String getDescription() {
        return NbBundle.getMessage(CreateElement.class, "DSC_Create_Field");
    }
    
    //XXX: currently we cannot fix:
    //xxx = new ArrayList<Unknown>();
    //=>
    //ArrayList<Unknown> xxx;
    //xxx = new ArrayList<Unknown>();
    private static boolean containsErrorsOrTypevarsRecursively(TypeMirror tm) {
        switch (tm.getKind()) {
            case WILDCARD:
            case TYPEVAR:
            case ERROR:
                return true;
            case DECLARED:
                DeclaredType type = (DeclaredType) tm;
                
                for (TypeMirror t : type.getTypeArguments()) {
                    if (containsErrorsOrTypevarsRecursively(t))
                        return true;
                }
                
                return false;
            case ARRAY:
                return containsErrorsOrTypevarsRecursively(((ArrayType) tm).getComponentType());
            default:
                return false;
        }
    }
    
    /**
     * Detects if targets file is non-null and writable
     * @return true if target's file is writable
     */ 
    private static boolean isTargetWritable(TypeElement target, CompilationInfo info) {
	FileObject fo = SourceUtils.getFile(ElementHandle.create(target.getEnclosingElement()), info.getClasspathInfo());
	if(fo != null && fo.canWrite())
	    return true;
	else
	    return false;
    }
    
    
    private static EnumSet<Modifier> getAccessModifiers(CompilationInfo info, TypeElement source, TypeElement target) {
        if (target.getKind().isInterface()) {
            return EnumSet.of(Modifier.PUBLIC);
        }
        
        TypeElement outterMostSource = info.getElementUtilities().outermostTypeElement(source);
        TypeElement outterMostTarget = info.getElementUtilities().outermostTypeElement(target);
        
        if (outterMostSource.equals(outterMostTarget)) {
            return EnumSet.of(Modifier.PRIVATE);
        }
        
        Element sourcePackage = outterMostSource.getEnclosingElement();
        Element targetPackage = outterMostTarget.getEnclosingElement();
        
        if (sourcePackage.equals(targetPackage)) {
            return EnumSet.noneOf(Modifier.class);
        }
        
        //TODO: protected?
        return EnumSet.of(Modifier.PUBLIC);
    }
    
}
