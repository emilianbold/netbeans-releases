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
package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring.ParameterInfo;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 * Refactoring used for changing method signature. It changes method declaration
 * and also all its references (callers).
 *
 * @author  Pavel Flaska
 * @author  Tomas Hurka
 * @author  Jan Becicka
 * @author  Ralph Ruijs
 */
public class ChangeParametersPlugin extends JavaRefactoringPlugin {
    
    private ChangeParametersRefactoring refactoring;
    private TreePathHandle treePathHandle;
    private RenameRefactoring[] renameDelegates;
    private boolean inited;
    
    /**
     * Creates a new instance of change parameters refactoring.
     *
     * @param method  refactored object, i.e. method or constructor
     */
    public ChangeParametersPlugin(ChangeParametersRefactoring refactoring) {
        this.refactoring = refactoring;
        this.treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
    }
    
    @Override
    public Problem checkParameters() {
        
        initDelegates();
        
        Problem p = null;
        for (RenameRefactoring renameRefactoring : renameDelegates) {
            p = chainProblems(p, renameRefactoring.fastCheckParameters());
            if (p != null && p.isFatal()) {
                return p;
            }
        }
        return p;
    }

    @Override
    public Problem fastCheckParameters(final CompilationController javac) throws IOException {
        javac.toPhase(JavaSource.Phase.RESOLVED);
        ParameterInfo paramTable[] = refactoring.getParameterInfo();
        final ExecutableElement method = (ExecutableElement) treePathHandle.resolveElement(javac);
        Problem p=null;
        boolean isConstructor = method.getKind() == ElementKind.CONSTRUCTOR;
        
        // Check Method Name
        final String methodName = refactoring.getMethodName();
        if(methodName != null && !Utilities.isJavaIdentifier(methodName)) {
            p = createProblem(p, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_InvalidIdentifier", methodName)); // NOI18N
        }
        
        TypeElement enclosingTypeElement = javac.getElementUtilities().enclosingTypeElement(method);
        List<? extends Element> allMembers = javac.getElements().getAllMembers(enclosingTypeElement);
        // Check return type
        if(!isConstructor) {
            String returnType = refactoring.getReturnType();
            TypeMirror parseType;
            if (returnType != null && returnType.length() < 1) {
                p = createProblem(p, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_NoReturn", returnType)); // NOI18N
            } else if (returnType != null) {
                TypeElement typeElement = javac.getElements().getTypeElement(returnType);
                parseType = typeElement == null ? null : typeElement.asType();
                if(parseType == null) {
                    boolean isGenericType = false;
                    List<? extends TypeParameterElement> typeParameters = method.getTypeParameters();
                    for (TypeParameterElement typeParameterElement : typeParameters) {
                        TypeParameterTree tpTree = (TypeParameterTree) javac.getTrees().getTree(typeParameterElement);
                        if(returnType.equals(tpTree.getName().toString())) {
                            isGenericType = true;
                        }
                    }
                    if(!isGenericType) {
                        parseType = javac.getTreeUtilities().parseType(returnType, enclosingTypeElement);
                        if(parseType == null || parseType.getKind() == TypeKind.ERROR) {
                            p = createProblem(p, false, NbBundle.getMessage(ChangeParametersPlugin.class, "WRN_canNotResolveReturn", returnType)); // NOI18N
                        }
                    }
                }
            }
            // check duplicate method signature
            List<ExecutableElement> methods = ElementFilter.methodsIn(allMembers);
            for (ExecutableElement exMethod : methods) {
                if(!exMethod.equals(method)) {
                    if(exMethod.getSimpleName().equals(method.getSimpleName())
                            && exMethod.getParameters().size() == paramTable.length) {
                        boolean sameParameters = true;
                        for (int j = 0; j < exMethod.getParameters().size(); j++) {
                            TypeMirror exType = ((VariableElement)exMethod.getParameters().get(j)).asType();
                            String type = paramTable[j].getType();
                            TypeMirror paramType = javac.getTreeUtilities().parseType(type, enclosingTypeElement);
                            if(!javac.getTypes().isSameType(exType, paramType)) {
                                sameParameters = false;
                            }
                        }
                        if(sameParameters) {
                            p = createProblem(p, false, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_existingMethod", exMethod.toString(), enclosingTypeElement.getQualifiedName())); // NOI18N
                        }
                    }
                }
            }
        } else {
            List<ExecutableElement> constructors = ElementFilter.constructorsIn(allMembers);
            for (ExecutableElement constructor : constructors) {
                if(!constructor.equals(method)) {
                    if(constructor.getParameters().size() == paramTable.length) {
                        boolean sameParameters = true;
                        for (int j = 0; j < constructor.getParameters().size(); j++) {
                            TypeMirror exType = ((VariableElement)constructor.getParameters().get(j)).asType();
                            String type = paramTable[j].getType();
                            TypeMirror paramType = javac.getTreeUtilities().parseType(type, enclosingTypeElement);
                            if(!javac.getTypes().isSameType(exType, paramType)) {
                                sameParameters = false;
                            }
                        }
                        if(sameParameters) {
                            p = createProblem(p, false, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_existingConstructor", constructor.toString(), enclosingTypeElement.getQualifiedName())); // NOI18N
                        }
                    }
                }
            }
        }
        for (int i = 0; i< paramTable.length; i++) {
            ParameterInfo parameterInfo = paramTable[i];
            
            int originalIndex = parameterInfo.getOriginalIndex();
            final VariableElement parameterElement = originalIndex == -1 ? null : method.getParameters().get(originalIndex);
            
            // check parameter name
            String s;
            s = parameterInfo.getName();
            if ((s == null || s.length() < 1))
                p = createProblem(p, true, newParMessage("ERR_parname")); // NOI18N
            else {
                if (!Utilities.isJavaIdentifier(s)) {
                    p = createProblem(p, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_InvalidIdentifier",s)); // NOI18N
                }
            }
            for (int j = 0; j < paramTable.length; j++) {
                ParameterInfo pInfo = paramTable[j];
                if(pInfo.getName().equals(s) && i != j) {
                    p = createProblem(p, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_ParamAlreadyUsed", s)); // NOI18N
                }
            }

            TreeScanner<Boolean, String> scanner = new TreeScanner<Boolean, String>() {

                @Override
                public Boolean visitVariable(VariableTree vt, String p) {
                    super.visitVariable(vt, p);
                    TreePath path = javac.getTrees().getPath(javac.getCompilationUnit(), vt);
                    Element element = javac.getTrees().getElement(path);
                    
                    return vt.getName().contentEquals(p) && !element.equals(parameterElement);
                }

                @Override
                public Boolean reduce(Boolean left, Boolean right) {
                    return (left == null ? false : left) || (right == null ? false : right);
                }
            };

            if (scanner.scan(javac.getTrees().getTree(method), s)) {
                if (!isParameterBeingRemoved(method, s, paramTable)) {
                    p = createProblem(p, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_NameAlreadyUsed", s)); // NOI18N
                }
            }

            // check parameter type
            String t = parameterInfo.getType();
            TypeMirror parseType = null;
            if (t == null || t.length() < 1) {
                p = createProblem(p, true, newParMessage("ERR_partype")); // NOI18N
            } else {
                TypeElement typeElement = javac.getElements().getTypeElement(t);
                parseType = typeElement == null ? null : typeElement.asType();
                if(parseType == null) {
                    boolean isGenericType = false;
                    List<? extends TypeParameterElement> typeParameters = method.getTypeParameters();
                    for (TypeParameterElement typeParameterElement : typeParameters) {
                        TypeParameterTree tpTree = (TypeParameterTree) javac.getTrees().getTree(typeParameterElement);
                        if(t.equals(tpTree.getName().toString())) {
                            isGenericType = true;
                        }
                    }
                    if(!isGenericType) {
                        parseType = javac.getTreeUtilities().parseType(t, enclosingTypeElement);
                        if(parseType == null || parseType.getKind() == TypeKind.ERROR) {
                            p = createProblem(p, false, NbBundle.getMessage(ChangeParametersPlugin.class, "WRN_canNotResolve", t, s)); // NOI18N
                        }
                    }
                }
            }

            if (originalIndex == -1) {
                // check the default value
                s = parameterInfo.getDefaultValue();
                if ((s == null || s.length() < 1)) {
                    p = createProblem(p, true, newParMessage("ERR_pardefv")); // NOI18N
                }
            } else {
                // check if the changed type is assigneble
                if(parseType != null) {
                    if(!javac.getTypes().isAssignable(parseType, parameterElement.asType())) {
                        p = createProblem(p, false, NbBundle.getMessage(ChangeParametersPlugin.class, "WRN_isNotAssignable", parameterElement.asType().toString(), t)); // NOI18N
                    }
                }
            }
            
            // check ...
            if (parameterInfo.getType() != null && parameterInfo.getType().endsWith("...") && i != paramTable.length - 1) {//NOI18N
                p = createProblem(p, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_VarargsFinalPosition", new Object[]{})); // NOI18N
            }
        }
        return p;    
    }

    private boolean isParameterBeingRemoved(ExecutableElement method, String s, ParameterInfo[] paramTable) {
        boolean beingRemoved = false;
        for (int j = 0; j < method.getParameters().size(); j++) {
            VariableElement variable = method.getParameters().get(j);
            if (variable.getSimpleName().contentEquals(s)) {

                boolean isInNewList = false;
                for (ParameterInfo parameterInfo : paramTable) {
                    if (parameterInfo.getOriginalIndex() == j) {
                        isInNewList = true;
                    }
                }
                beingRemoved = !isInNewList;
                break;
            }
        }
        return beingRemoved;
    }

    private static String newParMessage(String par) {
        return new MessageFormat(
                getString("ERR_newpar")).format(new Object[] { getString(par) } // NOI18N
            );
    }
    
    private static String getString(String key) {
        return NbBundle.getMessage(ChangeParametersPlugin.class, key);
    }

    private Set<ElementHandle<ExecutableElement>> allMethods;
    
    private Set<FileObject> getRelevantFiles() {
        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        final Set<FileObject> set = new HashSet<FileObject>();
        JavaSource source = JavaSource.create(cpInfo, refactoring.getRefactoringSource().lookup(TreePathHandle.class).getFileObject());
        
        try {
            source.runUserActionTask(new CancellableTask<CompilationController>() {
                
                public void cancel() {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                
                public void run(CompilationController info) throws Exception {
                    final ClassIndex idx = info.getClasspathInfo().getClassIndex();
                    info.toPhase(JavaSource.Phase.RESOLVED);
                    final ElementUtilities elmUtils = info.getElementUtilities();

                    //add all references of overriding methods
                    Element el = treePathHandle.resolveElement(info);
                    ElementHandle<TypeElement>  enclosingType = ElementHandle.create(elmUtils.enclosingTypeElement(el));
                        allMethods = new HashSet<ElementHandle<ExecutableElement>>();
                        allMethods.add(ElementHandle.create((ExecutableElement)el));
                        for (ExecutableElement e:RetoucheUtils.getOverridingMethods((ExecutableElement)el, info)) {
                            set.add(SourceUtils.getFile(e, info.getClasspathInfo()));
                            ElementHandle<TypeElement> encl = ElementHandle.create(elmUtils.enclosingTypeElement(e));
                            set.addAll(idx.getResources(encl, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                            allMethods.add(ElementHandle.create(e));
                        }
                        //add all references of overriden methods
                        for (ExecutableElement e:RetoucheUtils.getOverridenMethods((ExecutableElement)el, info)) {
                            set.add(SourceUtils.getFile(e, info.getClasspathInfo()));
                            ElementHandle<TypeElement> encl = ElementHandle.create(elmUtils.enclosingTypeElement(e));
                            set.addAll(idx.getResources(encl, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                            allMethods.add(ElementHandle.create(e));
                        }
                        set.addAll(idx.getResources(enclosingType, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                        set.add(SourceUtils.getFile(el, info.getClasspathInfo()));
                }
            }, true);
        } catch (IOException ioe) {
            throw (RuntimeException) new RuntimeException().initCause(ioe);
        }
        return set;
    }
    
    
    public Problem prepare(RefactoringElementsBag elements) {
        Set<FileObject> a = getRelevantFiles();
        fireProgressListenerStart(ProgressEvent.START, a.size() + 1);
        initDelegates();
        fireProgressListenerStep();
        Problem problem = null;
        ChangeParamsTransformer changeParamsTransformer = new ChangeParamsTransformer(refactoring, allMethods);
        if (!a.isEmpty()) {
            for (RenameRefactoring renameRefactoring : renameDelegates) {
                problem = chainProblems(problem, renameRefactoring.prepare(elements.getSession()));
                if (problem != null && problem.isFatal()) {
                    fireProgressListenerStop();
                    return problem;
                }
            }
            TransformTask transform = new TransformTask(changeParamsTransformer, treePathHandle);
            problem = createAndAddElements(a, transform, elements, refactoring);
        }
        fireProgressListenerStop();
        return problem != null ? problem : changeParamsTransformer.getProblem();
    }
    
    protected JavaSource getJavaSource(JavaRefactoringPlugin.Phase p) {
        switch(p) {
            case CHECKPARAMETERS:
            case FASTCHECKPARAMETERS:    
            case PRECHECK:
            case PREPARE:
                ClasspathInfo cpInfo = getClasspathInfo(refactoring);
                return JavaSource.create(cpInfo, treePathHandle.getFileObject());
        }
        return null;
    }
    /**
     * Returns list of problems. For the change method signature, there are two
     * possible warnings - if the method is overriden or if it overrides
     * another method.
     *
     * @return  overrides or overriden problem or both
     */
    @Override
    public Problem preCheck(CompilationController info) throws IOException {
        fireProgressListenerStart(refactoring.PRE_CHECK, 4);
        Problem preCheckProblem = null;
        info.toPhase(JavaSource.Phase.RESOLVED);
        preCheckProblem = isElementAvail(treePathHandle, info);
        if (preCheckProblem != null) {
            return preCheckProblem;
        }
        Element el = treePathHandle.resolveElement(info);
        if (!(el.getKind() == ElementKind.METHOD || el.getKind() == ElementKind.CONSTRUCTOR)) {
            preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_ChangeParamsWrongType")); // NOI18N
            return preCheckProblem;
        }

        preCheckProblem = JavaPluginUtils.isSourceElement(el, info);
        if (preCheckProblem != null) {
            return preCheckProblem;
        }

        if (info.getElementUtilities().enclosingTypeElement(el).getKind() == ElementKind.ANNOTATION_TYPE) {
            preCheckProblem =new Problem(true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_MethodsInAnnotationsNotSupported")); // NOI18N
            return preCheckProblem;
        }
        
        for (ExecutableElement e : RetoucheUtils.getOverridenMethods((ExecutableElement) el, info)) {
            if (RetoucheUtils.isFromLibrary(e, info.getClasspathInfo())) { //NOI18N
                preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_CannnotRefactorLibrary", el)); // NOI18N
            }
        }
                    
        fireProgressListenerStop();
        return preCheckProblem;
    }
    
    private void initDelegates() {
        if (inited) {
            return;
        }
        final LinkedList<RenameRefactoring> renameRefactoringsList = new LinkedList<RenameRefactoring>();

        try {
            getJavaSource(Phase.PREPARE).runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController javac) throws Exception {
                    javac.toPhase(JavaSource.Phase.RESOLVED);
                    ExecutableElement method = (ExecutableElement) treePathHandle.resolveElement(javac);
                    List<? extends VariableElement> parameters = method.getParameters();

                    ParameterInfo paramTable[] = refactoring.getParameterInfo();
                    for (int i = 0; i < paramTable.length; i++) {
                        ParameterInfo param = paramTable[i];
                        int origIndex = param.getOriginalIndex();
                        if (origIndex != -1) {
                            VariableElement variable = parameters.get(origIndex);
                            if(!variable.getSimpleName().contentEquals(param.getName())) {
                                TreePath path = javac.getTrees().getPath(variable);
                                RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(TreePathHandle.create(path, javac)));
                                renameRefactoring.setNewName(param.getName());
                                renameRefactoring.setSearchInComments(true);
                                renameRefactoringsList.add(renameRefactoring);
                            }
                        }
                    }
                    if(refactoring.getMethodName() != null && !method.getSimpleName().toString().equals(refactoring.getMethodName())) {
                        RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(TreePathHandle.create(method, javac)));
                        renameRefactoring.setNewName(refactoring.getMethodName());
                        renameRefactoring.setSearchInComments(true);
                        renameRefactoringsList.add(renameRefactoring);
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        renameDelegates = renameRefactoringsList.toArray(new RenameRefactoring[0]);
        inited = true;
    }

    private static Problem chainProblems(Problem p, Problem p1) {
        Problem problem;

        if (p == null) {
            return p1;
        }
        if (p1 == null) {
            return p;
        }
        problem = p;
        while (problem.getNext() != null) {
            problem = problem.getNext();
        }
        problem.setNext(p1);
        return p;
    }
}
