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
package org.netbeans.modules.java.hints.errors;

import java.util.Collection;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.StatementTree;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.java.hints.errors.CreateClassFix.CreateInnerClassFix;
import org.netbeans.modules.java.hints.errors.CreateClassFix.CreateOuterClassFix;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import static org.netbeans.modules.java.hints.errors.CreateElementUtilities.*;
import org.netbeans.modules.java.hints.errors.ErrorFixesFakeHint.FixKind;
import org.netbeans.modules.java.hints.errors.Utilities.MethodArguments;
import org.netbeans.modules.java.hints.introduce.Flow;
import org.openide.util.Pair;

/**
 *
 * @author Jan Lahoda
 */
public final class CreateElement implements ErrorRule<Void> {

    /** Creates a new instance of CreateElement */
    public CreateElement() {
    }

    public Set<String> getCodes() {
        return new HashSet<String>(Arrays.asList("compiler.err.cant.resolve.location", "compiler.err.cant.resolve.location.args", "compiler.err.cant.apply.symbol", "compiler.err.cant.apply.symbol.1", "compiler.err.cant.resolve", "compiler.err.cant.resolve.args", CAST_KEY)); // NOI18N
    }
    public static final String CAST_KEY = "compiler.err.prob.found.req";

    public List<Fix> run(CompilationInfo info, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        try {
            return analyze(info, diagnosticKey, offset);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return null;
        } catch (ClassCastException e) {
            Logger.getLogger(CreateElement.class.getName()).log(Level.FINE, null, e);
            return null;
        }
    }

    static List<Fix> analyze(CompilationInfo info, int offset) throws IOException {
        return analyze(info, null, offset);
    }
    
    static List<Fix> analyze(CompilationInfo info, String diagnosticKey, int offset) throws IOException {
        List<Fix> result = analyzeImpl(info, diagnosticKey, offset);
        
        if (CAST_KEY.equals(diagnosticKey)) {
            result = new ArrayList<>(result);
            
            for (Iterator<Fix> it = result.iterator(); it.hasNext();) {
                Fix f = it.next();
                
                if (!(f instanceof CreateMethodFix)) {
                    it.remove();
                }
            }
        }
        
        return result;
    }
    
    private static List<Fix> analyzeImpl(CompilationInfo info, String diagnosticKey, int offset) throws IOException {
        TreePath errorPath = ErrorHintsProvider.findUnresolvedElement(info, offset);

        if (errorPath == null) {
            return Collections.<Fix>emptyList();
        }
        
        if (CAST_KEY.equals(diagnosticKey) && errorPath.getParentPath() != null && errorPath.getParentPath().getLeaf().getKind() == Kind.METHOD_INVOCATION) {
            MethodInvocationTree mit = (MethodInvocationTree) errorPath.getParentPath().getLeaf();
            errorPath = new TreePath(errorPath.getParentPath(), mit.getMethodSelect());
            offset = (int) info.getTrees().getSourcePositions().getStartPosition(errorPath.getCompilationUnit(), errorPath.getLeaf());
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
        boolean baseType = false;
        boolean lookupMethodInvocation = true;
        boolean lookupNCT = true;

        TreePath path = info.getTreeUtilities().pathFor(Math.max((int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), errorPath.getLeaf()), offset) + 1);

        while(path != null) {
            Tree leaf = path.getLeaf();
            Kind leafKind = leaf.getKind();

            if (!baseType && TreeUtilities.CLASS_TREE_KINDS.contains(leafKind) && parent != null && (((ClassTree)leaf).getExtendsClause() == parent.getLeaf() || ((ClassTree)leaf).getImplementsClause().contains(parent.getLeaf())))
                baseType = true;
            if (parent != null && parent.getLeaf() == errorPath.getLeaf())
                parent = path;
            if (leaf == errorPath.getLeaf() && parent == null)
                parent = path;
            if (TreeUtilities.CLASS_TREE_KINDS.contains(leafKind) && firstClass == null)
                firstClass = path;
            if (leafKind == Kind.METHOD && firstMethod == null && firstClass == null)
                firstMethod = path;
            //static/dynamic initializer:
            if (   leafKind == Kind.BLOCK && TreeUtilities.CLASS_TREE_KINDS.contains(path.getParentPath().getLeaf().getKind())
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

        if (parent == null || parent.getLeaf() == errorPath.getLeaf())
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
        final TypeElement source = firstClass != null ? (TypeElement) info.getTrees().getElement(firstClass) : null;
        Element target = null;
        boolean wasMemberSelect = false;

        if (errorPath.getLeaf().getKind() == Kind.MEMBER_SELECT) {
            TreePath exp = new TreePath(errorPath, ((MemberSelectTree) errorPath.getLeaf()).getExpression());
            TypeMirror targetType = info.getTrees().getTypeMirror(exp);

            if (targetType != null) {
                if (targetType.getKind() == TypeKind.DECLARED) {
                    Element expElement = info.getTrees().getElement(exp);

                    if (isClassLikeElement(expElement)) {
                        modifiers.add(Modifier.STATIC);
                    }

                    Element targetElement = info.getTypes().asElement(targetType);

                    if (isClassLikeElement(targetElement)) {
                        target = (TypeElement) targetElement;
                    }
                } else if (targetType.getKind() == TypeKind.PACKAGE) {
                    target = info.getTrees().getElement(exp);
                }
            }

            wasMemberSelect = true;
        } else {
	    Element enclosingElement = e.getEnclosingElement();
	    if(enclosingElement != null && enclosingElement.getKind() == ElementKind.ANNOTATION_TYPE) //unresolved element inside annot.
			target = enclosingElement;
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

        if (target instanceof TypeElement)
            modifiers.addAll(Utilities.getAccessModifiers(info, source, (TypeElement) target).getRequiredModifiers());
        else
            modifiers.add(Modifier.PUBLIC);

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
            result.addAll(prepareCreateMethodFix(info, methodInvocation, modifiers, (TypeElement) target, simpleName, mit.getArguments(), types));
        }

        Set<ElementKind> fixTypes = EnumSet.noneOf(ElementKind.class);
        TypeMirror[] superType = new TypeMirror[1];
        int[] numTypeParameters = new int[1];
        List<? extends TypeMirror> types = resolveType(fixTypes, info, parent, errorPath.getLeaf(), offset, superType, numTypeParameters);
        ElementKind classType = getClassType(fixTypes);

        if (superType[0] == null && types != null && !types.isEmpty()) superType[0] = types.get(0);
        
        if (target.getKind() == ElementKind.PACKAGE) {
            result.addAll(prepareCreateOuterClassFix(info, null, target, modifiers, simpleName, null, superType[0], classType != null ? classType : ElementKind.CLASS, numTypeParameters[0]));
            return result;
        }
        
        //XXX: should reasonably consider all the found type candidates, not only the one:
        final TypeMirror type = types != null && !types.isEmpty() && types.get(0) != null ? Utilities.resolveCapturedType(info, types.get(0)) : null;
        TypeElement outermostTypeElement = source != null ? info.getElementUtilities().outermostTypeElement(source) : null;

        if (newClass != null) {
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
                    return prepareCreateInnerClassFix(info, newClass, (TypeElement) target, modifiers, simpleName, nct.getArguments(), type, ElementKind.CLASS, numTypeArguments);
                } else {
		    List<Fix> currentResult = new LinkedList<Fix>();

		    currentResult.addAll(prepareCreateOuterClassFix(info, newClass, source, EnumSet.noneOf(Modifier.class), simpleName, nct.getArguments(), type, ElementKind.CLASS, numTypeArguments));
                    if (!baseType || outermostTypeElement != source)
		        currentResult.addAll(prepareCreateInnerClassFix(info, newClass, outermostTypeElement, EnumSet.of(outermostTypeElement != null && outermostTypeElement.getKind().isInterface() ? Modifier.PUBLIC : Modifier.PRIVATE, Modifier.STATIC), simpleName, nct.getArguments(), type, ElementKind.CLASS, numTypeArguments));
		    
                    return currentResult;
                }
            }

            if (nct.getClassBody() != null) {
                return Collections.<Fix>emptyList();
            }

            TypeElement clazzTarget = (TypeElement) clazz;

            result.addAll(prepareCreateMethodFix(info, newClass, Utilities.getAccessModifiers(info, source, clazzTarget).getRequiredModifiers(), clazzTarget, "<init>", nct.getArguments(), null)); //NOI18N
        }

        //field like or class (type):
        if (classType != null && e.asType().getKind() == TypeKind.ERROR) {
            if (wasMemberSelect) {
                 result.addAll(prepareCreateInnerClassFix(info, null, (TypeElement) target, modifiers, simpleName, null, superType[0], classType, numTypeParameters[0]));
            } else {
                result.addAll(prepareCreateOuterClassFix(info, null, source, EnumSet.noneOf(Modifier.class), simpleName, null, superType[0], classType, numTypeParameters[0]));
                if (!baseType || outermostTypeElement != source)
                    result.addAll(prepareCreateInnerClassFix(info, null, outermostTypeElement, EnumSet.of(outermostTypeElement != null && outermostTypeElement.getKind().isInterface() ? Modifier.PUBLIC : Modifier.PRIVATE, Modifier.STATIC), simpleName, null, superType[0], classType, numTypeParameters[0]));
            }
        }

        if (type == null || type.getKind() == TypeKind.VOID || type.getKind() == TypeKind.OTHER || type.getKind() == TypeKind.EXECUTABLE) {
            return result;
        }

        //currently, we cannot handle error types:
        if (Utilities.containsErrorsRecursively(type)) {
            return result;
        }

        Collection<TypeVariable> typeVars = Utilities.containedTypevarsRecursively(type);

        if (!Utilities.allTypeVarsAccessible(typeVars, target)) {
            fixTypes.remove(ElementKind.FIELD);
        }

        if (fixTypes.contains(ElementKind.FIELD) && Utilities.isTargetWritable((TypeElement) target, info)) { //IZ 111048 -- don't offer anything if target file isn't writable
            Element enclosingElement = e.getEnclosingElement();
            if (enclosingElement != null && enclosingElement.getKind() == ElementKind.ANNOTATION_TYPE) {
//                FileObject targetFile = SourceUtils.getFile(target, info.getClasspathInfo());
                FileObject targetFile = SourceUtils.getFile(ElementHandle.create(target), info.getClasspathInfo());
                if (targetFile != null) {
                    result.add(new CreateMethodFix(info, simpleName, modifiers, (TypeElement) target, type, types, Collections.<String>emptyList(), Collections.<TypeMirror>emptyList(), Collections.<String>emptyList(), targetFile));
                }

                return result;
            } else {
                FileObject targetFile = SourceUtils.getFile(ElementHandle.create(target), info.getClasspathInfo());
                if (targetFile != null) {
                    if (target.getKind() == ElementKind.ENUM) {
                        if (source != null) { //TODO: maybe create a constant? - but the test below seems very suspicious:
                        if (source.equals(target)) {
                            result.add(new CreateFieldFix(info, simpleName, modifiers, (TypeElement) target, type, targetFile));
                        } else {
                            result.add(new CreateEnumConstant(info, simpleName, modifiers, (TypeElement) target, type, targetFile));
                        }
                        }
                    } else {
                        if (firstMethod != null && info.getTrees().getElement(firstMethod).getKind() == ElementKind.CONSTRUCTOR && ErrorFixesFakeHint.isCreateFinalFieldsForCtor(ErrorFixesFakeHint.getPreferences(targetFile, FixKind.CREATE_FINAL_FIELD_CTOR))) {
                            boolean hasOtherConstructors = false;
                            for (Tree member : ((ClassTree)firstClass.getLeaf()).getMembers()) {
                                if (member.getKind() == Tree.Kind.METHOD && "<init>".contentEquals(((MethodTree)member).getName()) && firstMethod.getLeaf() != member) { //NOI18N
                                    Iterator<? extends StatementTree> stats = ((MethodTree) member).getBody().getStatements().iterator();
                                    if (stats.hasNext()) {
                                        StatementTree stat = stats.next();
                                        if (stat.getKind() == Tree.Kind.EXPRESSION_STATEMENT) {
                                            ExpressionTree exp = ((ExpressionStatementTree)stat).getExpression();
                                            if (exp.getKind() == Tree.Kind.METHOD_INVOCATION) {
                                                ExpressionTree meth = ((MethodInvocationTree)exp).getMethodSelect();
                                                if (meth.getKind() == Tree.Kind.IDENTIFIER && "this".contentEquals(((IdentifierTree)meth).getName())) { //NOI18N
                                                    continue;
                                                }
                                            }
                                        }
                                    }
                                    hasOtherConstructors = true;
                                    break;
                                }
                            }
                            if (!hasOtherConstructors) {
                                BlockTree constructorBody = ((MethodTree) firstMethod.getLeaf()).getBody();
                                String constructorBodyText = info.getText().substring((int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), constructorBody), (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), constructorBody));
                                String withVariable = "{" + type.toString() + " " + simpleName + "; " + constructorBodyText + "}";
                                BlockTree newBlock = (BlockTree) info.getTreeUtilities().parseStatement(withVariable, new SourcePositions[1]);
                                Scope scope = info.getTrees().getScope(firstMethod);
                                info.getTreeUtilities().attributeTree(newBlock, scope);
                                VariableElement var = (VariableElement) info.getTrees().getElement(new TreePath(new TreePath(firstMethod, newBlock), newBlock.getStatements().get(0)));
                                if (Flow.definitellyAssigned(info, var, Arrays.asList(new TreePath(new TreePath(firstMethod, newBlock), newBlock.getStatements().get(1))), new AtomicBoolean()))
                                    modifiers.add(Modifier.FINAL);
                            }
                        }
                        if (ErrorFixesFakeHint.enabled(ErrorFixesFakeHint.FixKind.CREATE_FINAL_FIELD_CTOR)) {
                            result.add(new CreateFieldFix(info, simpleName, modifiers, (TypeElement) target, type, targetFile));
                        }
                    }
                }
            }
        }

        if (!wasMemberSelect && (fixTypes.contains(ElementKind.LOCAL_VARIABLE) || fixTypes.contains(ElementKind.PARAMETER))) {
            ExecutableElement ee = null;

            if (firstMethod != null) {
                ee = (ExecutableElement) info.getTrees().getElement(firstMethod);
            }

            int identifierPos = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), errorPath.getLeaf());
            if (ee != null && fixTypes.contains(ElementKind.PARAMETER) && !Utilities.isMethodHeaderInsideGuardedBlock(info, (MethodTree) firstMethod.getLeaf()))
                result.add(new AddParameterOrLocalFix(info, type, simpleName, true, identifierPos));
            if ((firstMethod != null || firstInitializer != null) && fixTypes.contains(ElementKind.LOCAL_VARIABLE) && ErrorFixesFakeHint.enabled(ErrorFixesFakeHint.FixKind.CREATE_LOCAL_VARIABLE))
                result.add(new AddParameterOrLocalFix(info, type, simpleName, false, identifierPos));
        }

        return result;
    }

    private static List<Fix> prepareCreateMethodFix(CompilationInfo info, TreePath invocation, Set<Modifier> modifiers, TypeElement target, String simpleName, List<? extends ExpressionTree> arguments, List<? extends TypeMirror> returnTypes) {
        //return type:
        //XXX: should reasonably consider all the found type candidates, not only the one:
        TypeMirror returnType = returnTypes != null ? Utilities.resolveCapturedType(info, returnTypes.get(0)) : null;

        //currently, we cannot handle error types, TYPEVARs and WILDCARDs:
        if (returnType != null && Utilities.containsErrorsRecursively(returnType)) {
            return Collections.<Fix>emptyList();
        }
        
        //create method:
        MethodArguments formalArguments = Utilities.resolveArguments(info, invocation, arguments, target, returnType);

        //currently, we cannot handle error types, TYPEVARs and WILDCARDs:
        if (formalArguments == null) {
            return Collections.<Fix>emptyList();
        }

       	//IZ 111048 -- don't offer anything if target file isn't writable
	if(!Utilities.isTargetWritable(target, info))
	    return Collections.<Fix>emptyList();

        FileObject targetFile = SourceUtils.getFile(ElementHandle.create(target), info.getClasspathInfo());
        if (targetFile == null)
            return Collections.<Fix>emptyList();

        return Collections.<Fix>singletonList(new CreateMethodFix(info, simpleName, modifiers, target, returnType, formalArguments.parameterTypes, formalArguments.parameterNames, formalArguments.typeParameterTypes, formalArguments.typeParameterNames, targetFile));
    }

    private static List<Fix> prepareCreateOuterClassFix(CompilationInfo info, TreePath invocation, Element source, Set<Modifier> modifiers, String simpleName, List<? extends ExpressionTree> realArguments, TypeMirror superType, ElementKind kind, int numTypeParameters) {
        Pair<List<? extends TypeMirror>, List<String>> formalArguments = invocation != null ? Utilities.resolveArguments(info, invocation, realArguments, null) : Pair.<List<? extends TypeMirror>, List<String>>of(null, null);

        if (formalArguments == null) {
            return Collections.<Fix>emptyList();
        }

        ClassPath cp = info.getClasspathInfo().getClassPath(PathKind.SOURCE);
        FileObject root = cp.findOwnerRoot(info.getFileObject());

        if (root == null) { //File not part of any project
            return Collections.<Fix>emptyList();
        }

        PackageElement packageElement = (PackageElement) (source instanceof PackageElement ? source : info.getElementUtilities().outermostTypeElement(source).getEnclosingElement());

        return Collections.<Fix>singletonList(new CreateOuterClassFix(info, root, packageElement.getQualifiedName().toString(), simpleName, modifiers, formalArguments.first(), formalArguments.second(), superType, kind, numTypeParameters));
    }

    private static List<Fix> prepareCreateInnerClassFix(CompilationInfo info, TreePath invocation, TypeElement target, Set<Modifier> modifiers, String simpleName, List<? extends ExpressionTree> realArguments, TypeMirror superType, ElementKind kind, int numTypeParameters) {
        Pair<List<? extends TypeMirror>, List<String>> formalArguments = invocation != null ? Utilities.resolveArguments(info, invocation, realArguments, target) : Pair.<List<? extends TypeMirror>, List<String>>of(null, null);

        if (formalArguments == null) {
            return Collections.<Fix>emptyList();
        }

	//IZ 111048 -- don't offer anything if target file isn't writable
	if (!Utilities.isTargetWritable(target, info))
	    return Collections.<Fix>emptyList();

        FileObject targetFile = SourceUtils.getFile(target, info.getClasspathInfo());

        if (targetFile == null)
            return Collections.<Fix>emptyList();

        return Collections.<Fix>singletonList(new CreateInnerClassFix(info, simpleName, modifiers, target, formalArguments.first(), formalArguments.second(), superType, kind, numTypeParameters, targetFile));
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


}
