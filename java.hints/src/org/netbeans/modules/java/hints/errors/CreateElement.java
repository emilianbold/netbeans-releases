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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.BlockTree;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
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
        return new HashSet<String>(Arrays.asList("compiler.err.cant.resolve.location", "compiler.err.cant.resolve.location.args", "compiler.err.cant.apply.symbol", "compiler.err.cant.resolve", "compiler.err.cant.resolve.args")); // NOI18N
    }

    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        try {
            return analyze(info, offset);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return null;
        } catch (ClassCastException e) {
            Logger.getLogger(CreateElement.class.getName()).log(Level.FINE, null, e);
            return null;
        }
    }

    static List<Fix> analyze(CompilationInfo info, int offset) throws IOException {
        TreePath errorPath = ErrorHintsProvider.findUnresolvedElement(info, offset);

        if (errorPath == null) {
            return Collections.<Fix>emptyList();
        }

        if (info.getElements().getTypeElement("java.lang.Object") == null) { // NOI18N
            // broken java platform
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
        Name name = e.getSimpleName();
        if (name == null) {
            if (ErrorHintsProvider.ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ErrorHintsProvider.ERR.log(ErrorManager.INFORMATIONAL, "e.simpleName=null"); // NOI18N
                ErrorHintsProvider.ERR.log(ErrorManager.INFORMATIONAL, "offset=" + offset); // NOI18N
                ErrorHintsProvider.ERR.log(ErrorManager.INFORMATIONAL, "errorTree=" + errorPath.getLeaf()); // NOI18N
            }

            return Collections.<Fix>emptyList();
        }
        String simpleName = name.toString();
        TypeElement source = (TypeElement) info.getTrees().getElement(firstClass);
        TypeElement target = null;
        boolean wasMemberSelect = false;

        if (errorPath.getLeaf().getKind() == Kind.MEMBER_SELECT) {
            TreePath exp = new TreePath(errorPath, ((MemberSelectTree) errorPath.getLeaf()).getExpression());
            TypeMirror targetType = info.getTrees().getTypeMirror(exp);

            if (targetType != null && targetType.getKind() == TypeKind.DECLARED) {
                Element expElement = info.getTrees().getElement(exp);

                if (isClassLikeElement(expElement)) {
                    modifiers.add(Modifier.STATIC);
                }

                Element targetElement = info.getTypes().asElement(targetType);

                if (isClassLikeElement(targetElement)) {
                    target = (TypeElement) targetElement;
                }
            }

            wasMemberSelect = true;
        } else {
	    Element enclosingElement = e.getEnclosingElement();
	    if(enclosingElement != null && enclosingElement.getKind() == ElementKind.ANNOTATION_TYPE) //unresolved element inside annot.
			target = (TypeElement) enclosingElement;
	    else

		if (errorPath.getLeaf().getKind() == Kind.IDENTIFIER) {
		    //TODO: Handle Annotations
                target = source;

                if (firstMethod != null) {
                    if (((MethodTree)firstMethod.getLeaf()).getModifiers().getFlags().contains(Modifier.STATIC)) {
                        modifiers.add(Modifier.STATIC);
                    }
                } else {
                    if (firstInitializer != null) {
                        if (((BlockTree) firstInitializer.getLeaf()).isStatic()) {
                            modifiers.add(Modifier.STATIC);
                        }
                    } else {
                        //TODO: otherwise.
                    }
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

        List<Fix> result = new ArrayList<Fix>();

        if (methodInvocation != null) {
            //create method:
            MethodInvocationTree mit = (MethodInvocationTree) methodInvocation.getLeaf();
            //return type:
            Set<ElementKind> fixTypes = EnumSet.noneOf(ElementKind.class);
            List<? extends TypeMirror> types = resolveType(fixTypes, info, methodInvocation.getParentPath(), methodInvocation.getLeaf(), offset, null, null);

            if (types == null || types.isEmpty()) {
                return Collections.<Fix>emptyList();
            }
            result.addAll(prepareCreateMethodFix(info, methodInvocation, modifiers, target, simpleName, mit.getArguments(), types));
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

            TypeElement clazzTarget = (TypeElement) clazz;

            result.addAll(prepareCreateMethodFix(info, newClass, getAccessModifiers(info, source, clazzTarget), clazzTarget, "<init>", nct.getArguments(), null)); //NOI18N
        }

        //field like or class (type):
        Set<ElementKind> fixTypes = EnumSet.noneOf(ElementKind.class);
        TypeMirror[] superType = new TypeMirror[1];
        int[] numTypeParameters = new int[1];
        List<? extends TypeMirror> types = resolveType(fixTypes, info, parent, errorPath.getLeaf(), offset, superType, numTypeParameters);
        ElementKind classType = getClassType(fixTypes);

        if (classType != null) {
            if (wasMemberSelect) {
                result.addAll(prepareCreateInnerClassFix(info, null, target, modifiers, simpleName, null, superType[0], classType, numTypeParameters[0]));
            } else {
                result.addAll(prepareCreateOuterClassFix(info, null, source, EnumSet.noneOf(Modifier.class), simpleName, null, superType[0], classType, numTypeParameters[0]));
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
            Element enclosingElement = e.getEnclosingElement();
            if (enclosingElement != null && enclosingElement.getKind() == ElementKind.ANNOTATION_TYPE) {
//                FileObject targetFile = SourceUtils.getFile(target, info.getClasspathInfo());
                FileObject targetFile = SourceUtils.getFile(ElementHandle.create(target), info.getClasspathInfo());
                if (targetFile != null) {
                    result.add(new CreateMethodFix(info, simpleName, modifiers, target, type, types, Collections.<String>emptyList(), targetFile));
                }

                return result;
            } else {
                FileObject targetFile = SourceUtils.getFile(ElementHandle.create(target), info.getClasspathInfo());
                if (targetFile != null) {
                    if (target.getKind() == ElementKind.ENUM) {
                        if (source.equals(target)) {
                            result.add(new CreateFieldFix(info, simpleName, modifiers, target, type, targetFile));
                        } else {
                            result.add(new CreateEnumConstant(info, simpleName, modifiers, target, type, targetFile));
                        }
                    } else {
                        if (firstMethod != null && info.getTrees().getElement(firstMethod).getKind() == ElementKind.CONSTRUCTOR && ErrorFixesFakeHint.isCreateFinalFieldsForCtor()) {
                            modifiers.add(Modifier.FINAL);
                        }
                        if (ErrorFixesFakeHint.enabled(ErrorFixesFakeHint.FixKind.CREATE_FINAL_FIELD_CTOR)) {
                            result.add(new CreateFieldFix(info, simpleName, modifiers, target, type, targetFile));
                        }
                    }
                }
            }
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
                if (fixTypes.contains(ElementKind.LOCAL_VARIABLE) && ErrorFixesFakeHint.enabled(ErrorFixesFakeHint.FixKind.CREATE_LOCAL_VARIABLE))
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

        FileObject targetFile = SourceUtils.getFile(ElementHandle.create(target), info.getClasspathInfo());
        if (targetFile == null)
            return Collections.<Fix>emptyList();

        return Collections.<Fix>singletonList(new CreateMethodFix(info, simpleName, modifiers, target, returnType, formalArguments.getA(), formalArguments.getB(), targetFile));
    }

    private static Pair<List<? extends TypeMirror>, List<String>> resolveArguments(CompilationInfo info, TreePath invocation, List<? extends ExpressionTree> realArguments) {
        List<TypeMirror> argumentTypes = new LinkedList<TypeMirror>();
        List<String>     argumentNames = new LinkedList<String>();
        Set<String>      usedArgumentNames = new HashSet<String>();

        for (ExpressionTree arg : realArguments) {
            TypeMirror tm = info.getTrees().getTypeMirror(new TreePath(invocation, arg));

            //anonymous class?
            tm = Utilities.convertIfAnonymous(tm);

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

        if (root == null) { //File not part of any project
            return Collections.<Fix>emptyList();
        }

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

        FileObject targetFile = SourceUtils.getFile(target, info.getClasspathInfo());

        if (targetFile == null)
            return Collections.<Fix>emptyList();

        return Collections.<Fix>singletonList(new CreateInnerClassFix(info, simpleName, modifiers, target, formalArguments.getA(), formalArguments.getB(), superType, kind, numTypeParameters, targetFile));
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

    private static boolean isClassLikeElement(Element expElement) {
        return expElement != null && (expElement.getKind().isClass() || expElement.getKind().isInterface());
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


    static EnumSet<Modifier> getAccessModifiers(CompilationInfo info, TypeElement source, TypeElement target) {
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
