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
package org.netbeans.modules.refactoring.java.plugins;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import javax.lang.model.element.*;
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
import org.openide.util.NbBundle;

/**
 * Refactoring used for changing method signature. It changes method declaration
 * and also all its references (callers).
 *
 * @author  Pavel Flaska
 * @author  Tomas Hurka
 * @author  Jan Becicka
 */
public class ChangeParametersPlugin extends JavaRefactoringPlugin implements ProgressListener {
    
    private ChangeParametersRefactoring refactoring;
    private TreePathHandle treePathHandle;
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
        //TODO:
        return null;
    }

    @Override
    public Problem fastCheckParameters(CompilationController javac) throws IOException {
        javac.toPhase(JavaSource.Phase.RESOLVED);
        if (((ExecutableElement) treePathHandle.resolveElement(javac)).isVarArgs()) {
            int i=refactoring.getParameterInfo().length;
            for (ParameterInfo in: refactoring.getParameterInfo()) {
                i--;
                if (in.getType() != null && in.getType().endsWith("...") && i!=0) {//NOI18N
                    return new Problem(true, org.openide.util.NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_VarargsFinalPosition", new Object[] {}));
                }
            }
        }
        return null;    
    }

    private Set<ElementHandle<ExecutableElement>> allMethods;
    
    private Set<FileObject> getRelevantFiles() {
        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        final Set<FileObject> set = new HashSet<FileObject>();
        JavaSource source = JavaSource.create(cpInfo, refactoring.getRefactoringSource().lookup(TreePathHandle.class).getFileObject());
        
        try {
            source.runUserActionTask(new CancellableTask<CompilationController>() {
                
                public void cancel() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                
                public void run(CompilationController info) throws Exception {
                    final ClassIndex idx = info.getClasspathInfo().getClassIndex();
                    info.toPhase(JavaSource.Phase.RESOLVED);

                    //add all references of overriding methods
                        TreePathHandle treePathHandle = refactoring.getRefactoringSource().lookup(TreePathHandle.class);
                        Element el = treePathHandle.resolveElement(info);
                    ElementHandle<TypeElement>  enclosingType = ElementHandle.create(SourceUtils.getEnclosingTypeElement(el));
                        allMethods = new HashSet<ElementHandle<ExecutableElement>>();
                        allMethods.add(ElementHandle.create((ExecutableElement)el));
                        for (ExecutableElement e:RetoucheUtils.getOverridingMethods((ExecutableElement)el, info)) {
                            set.add(SourceUtils.getFile(e, info.getClasspathInfo()));
                            ElementHandle<TypeElement> encl = ElementHandle.create(SourceUtils.getEnclosingTypeElement(e));
                            set.addAll(idx.getResources(encl, EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
                            allMethods.add(ElementHandle.create(e));
                        }
                        //add all references of overriden methods
                        for (ExecutableElement e:RetoucheUtils.getOverridenMethods((ExecutableElement)el, info)) {
                            set.add(SourceUtils.getFile(e, info.getClasspathInfo()));
                            ElementHandle<TypeElement> encl = ElementHandle.create(SourceUtils.getEnclosingTypeElement(e));
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
        fireProgressListenerStart(ProgressEvent.START, a.size());
        if (!a.isEmpty()) {
            TransformTask transform = new TransformTask(new ChangeParamsTransformer(refactoring, allMethods), treePathHandle);
            createAndAddElements(a, transform, elements, refactoring);
        }
        fireProgressListenerStop();
        return null;
//        JavaMetamodel.getManager().getProgressSupport().addProgressListener(this);
//        Problem problem = null;
//        try {
//            // get the original access modifier and check, if the original
//            // modifier is weaker than the new modifier. If so, set checkMod
//            // to true and in following code check, if all usages will have 
//            // sufficient access privileges.
//            int origAccessMods = method.getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE);
//            boolean checkMod = compareModifiers(origAccessMods, modifier) == -1;
//            // get all the callers and usages of the callable and add them
//            // the the collection of refactored elements
//            referencesIterator = ((CallableFeatureImpl) method).findDependencies(true, true, true).iterator();
//            elements.add(refactoring, new SignatureElement(method, paramTable, modifier));
//            int parNum = ((CallableFeature) refactoring.getRefactoredObject()).getParameters().size();
//            while (referencesIterator.hasNext()) {
//                if (cancelRequest) {
//                    return null;
//                }
//                Object ref = referencesIterator.next();
//                if (ref instanceof Invocation) {
//                    // Callers. Refactored method has to have the same number
//                    // of parameters as its caller. (see issue #53041 for details)
//                    if (((Invocation) ref).getParameters().size() == parNum) {
//                        if (problem == null && checkMod) {
//                            // check the access to refactored method
//                            Feature f = JavaModelUtil.getDeclaringFeature((Invocation)ref);
//                            if (Modifier.isPrivate(modifier)) { // changing to private
//                                if (!Utilities.compareObjects(getOutermostClass(f), getOutermostClass(method))) {
//                                    String msg = getString("ERR_StrongAccMod"); // NOI18N
//                                    problem = new Problem(false, new MessageFormat(msg).format(new Object[] { "private" })); // NOI18N
//                                }
//                            } else if (Modifier.isProtected(modifier)) { // changing to protected
//                                if (!method.getResource().getPackageName().equals(f.getResource().getPackageName())) {
//                                    String msg = getString("ERR_StrongAccMod"); // NOI18N
//                                    problem = new Problem(false, new MessageFormat(msg).format(new Object[] { "protected" })); // NOI18N
//                                }
//                            } else if (modifier == 0) { // default modifier check
//                                if (!f.getResource().getPackageName().equals(method.getResource().getPackageName())) {
//                                    String msg = getString("ERR_StrongAccMod"); // NOI18N
//                                    problem = new Problem(false, new MessageFormat(msg).format(new Object[] { "default" })); // NOI18N
//                                }
//                            }
//                        }
//                        elements.add(refactoring, new CallerElement((Invocation) ref, paramTable));
//                    }
//                } else {
//                    // declaration/declarations (in case of overriden or overrides)
//                    elements.add(refactoring, new SignatureElement((CallableFeature) ref, paramTable, modifier));
//                }
//            }
//            return problem;
//        } 
//        finally {
//            referencesIterator = null;
//            JavaMetamodel.getManager().getProgressSupport().removeProgressListener(this);
//        }
    }
    

//    RefObject selectedObject;

//    RefObject selectedObject;
//    // refatorected object - method or constructor
//    CallableFeature method;
//    // table of all the changes - it contains all the new parameters and also
//    // changes in order
//    ParameterInfo[] paramTable;
//    // new modifier
//    int modifier;
//    
//    
//    
//    public Problem checkParameters() {
//        return setParameters(refactoring.getParameterInfo(), refactoring.getModifiers());
//    }
//    
//    public Problem fastCheckParameters() {
//        return checkParameters(refactoring.getParameterInfo(), refactoring.getModifiers());
//        
//    }
//    

    protected JavaSource getJavaSource(JavaRefactoringPlugin.Phase p) {
        switch(p) {
            case CHECKPARAMETERS:
            case FASTCHECKPARAMETERS:    
            case PRECHECK:
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
            preCheckProblem = createProblem(preCheckProblem, true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_ChangeParamsWrongType"));
            return preCheckProblem;
        }
        
        FileObject fo = SourceUtils.getFile(el,info.getClasspathInfo());
        if (RetoucheUtils.isFromLibrary(el, info.getClasspathInfo())) { //NOI18N
            preCheckProblem = createProblem(preCheckProblem, true, getCannotRefactor(fo));
        }
        
        if (!RetoucheUtils.isElementInOpenProject(fo)) {
            preCheckProblem =new Problem(true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_ProjectNotOpened"));
            return preCheckProblem;
        }
        
        if (SourceUtils.getEnclosingTypeElement(el).getKind() == ElementKind.ANNOTATION_TYPE) {
            preCheckProblem =new Problem(true, NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_MethodsInAnnotationsNotSupported"));
            return preCheckProblem;
        }
                    
//                    Collection overridesMethod = null;
//                    Collection overridenMethod = null;
//                    
//                    method = (CallableFeature)selectedObject;
//                    // for the method, check, if the method overrides another method and
//                    // if the method is overriden by another method
//                    // todo (#pf): what about constructors?
//                    List pars = (List) method.getParameters();
//                    List typeList = new ArrayList();
//                    for (Iterator parIt = pars.iterator(); parIt.hasNext(); ) {
//                        Parameter par = (Parameter) parIt.next();
//                        typeList.add(par.getType());
//                    }
//                    if (method instanceof Method) {
//                        overridesMethod = CheckUtils.overrides((Method) method,
//                                method.getName(), typeList, true);
//                        overridenMethod = CheckUtils.isOverridden((Method) method,
//                                method.getName(), typeList);
//                    }
//                    // for 4.0, we not support methods with variable arguments. It is a
//                    // fatal error for the time being.
//                    if (CheckUtils.hasVarArgs(method)) {
//                        String msg = getString("ERR_HasVarArg");// NOI18N
//                        result = createProblem(result, true, msg);
//                    }
//                    if (overridesMethod != null) {
//                        for (Iterator iter = overridesMethod.iterator(); iter.hasNext(); ){
//                            String msg = new MessageFormat(getString("ERR_MethodOverrides")).format( // NOI18N
//                                    new Object[] { getDefClassName(((Method) iter.next()).getDeclaringClass()) }
//                            );
//                            result = createProblem(result, false, msg);
//                        }
//                    }
//                    if (overridenMethod != null) {
//                        for (Iterator iter = overridenMethod.iterator(); iter.hasNext(); ){
//                            String msg = new MessageFormat(getString("ERR_MethodIsOverridden")).format( // NOI18N
//                                    new Object[] { getDefClassName(((Method) iter.next()).getDeclaringClass()) }
//                            );
//                            result = createProblem(result, false, msg);
//                        }
//                    }
        fireProgressListenerStop();
        return preCheckProblem;
    }
    
    private static final String getCannotRefactor(FileObject r) {
        return new MessageFormat(NbBundle.getMessage(ChangeParametersPlugin.class, "ERR_CannotRefactorFile")).format(new Object[] {r.getName()});
    }
    
//    private Problem checkParameters(ParameterInfo[] aParamTable, int modifier) {
//        if ((method.getModifiers() & modifier) == 0) {
//            ClassDefinition cd = method.getDeclaringClass();
//            if (cd instanceof JavaClass) {
//                if (((JavaClass)cd).isInterface()) {
//                    return new Problem(true, getString("ERR_CannotChangeModifiers")); //NOI18N
//                }
//            }
//        }
//        ParameterInfo[] oldParamTable = paramTable;
//        int oldModifier = this.modifier;
//        paramTable = aParamTable;
//        this.modifier = modifier;
//        // checks
//        Problem emptyArgs = checkParameterAttributes();
//        // Note (#pf): little hacking - because of reverse problems sorting 
//        // (last fatal reported is first in the chain, not first fatal reported),
//        // we have to use two problem chains. First for empty arguments and 
//        // second for invalid identifiers. Then they are joined together 
//        // at the end of method.
//        Problem result = null;
//        // check, if there is is varArg. Check also the parameter's name clash
//        Set paramNames = new HashSet(3);
//        for (int i = 0; i < paramTable.length; i++) {
//            Type type = paramTable[i].getType();
//            if (type != null && type.getName() != null && type.getName().endsWith("...")) { // NOI18N
//                // varargs are not supported in change parameters in 4.0.
//                result = createProblem(result, true, getString("ERR_HasVarArg"));
//            }
//            String name = null;
//            int orIdx = paramTable[i].getOriginalIndex();
//            if (orIdx > -1) { 
//                // existing parameter
//                name = ((Parameter) method.getParameters().get(orIdx)).getName();
//            } else {
//                // new parameter
//                name = paramTable[i].getName();
//            }
//            if (name!=null) {
//                if (!Utilities.isJavaIdentifier(name)) {
//                    String msg = new MessageFormat(getString("ERR_InvalidIdentifier")).format( //NOI18N
//                        new Object[] {name}
//                    );
//                    result = createProblem(result, true, msg);
//                }   
//            }
//            if (name != null && paramNames.add(name) == false) {
//                // set already contains the same name!
//                result = createProblem(result, true,
//                    new MessageFormat(getString("ERR_DuplicateName")).format( // NOI18N
//                        new Object[] { name }
//                ));
//            }
//            if (orIdx == -1 && name != null && CheckUtils.getAllVariableNames(method).contains(name)) {
//                result = createProblem(result, true,
//                    new MessageFormat(getString("ERR_NameAlreadyUsed")).format( // NOI18N
//                        new Object[] { name }
//                ));
//            }
//        }
//        
//        // if there exists duplicate method (i.e. the method with the
//        // name and parameters, create fatal problem - be careful and
//        // do not create problem when found method is the same -
//        // example: you have method 'int swap(int a, int b)'. When
//        // user only swaps parameters in the method (i.e. new signature is
//        // 'swap(int b, int a)', you have to allow him to do it!
//        if (method instanceof Method) {
//            Method duplicateMethod = methodClashes(paramTable);
//            if (duplicateMethod != null && !method.equals(duplicateMethod)) {
//                result = createProblem(result, true,
//                    new MessageFormat(getString("ERR_existingMethod")).format( // NOI18N
//                        new Object[] {
//                            duplicateMethod.getName(),
//                            getDefClassName(duplicateMethod.getDeclaringClass())
//                        }
//                    )
//                );
//            }
//        // otherwise it has to be constructor
//        } else {
//            Constructor duplicateConstr = constructorClashes(paramTable);
//            if (duplicateConstr != null && !method.equals(duplicateConstr)) {
//                result = createProblem(result, true,
//                    new MessageFormat(getString("ERR_existingConstr")).format( // NOI18N
//                        new Object[] {
//                            getDefClassName(duplicateConstr.getDeclaringClass())
//                        }
//                    )
//                );
//            }
//        }
//        // sorting problems in correct order (different of createProblem method)
//        if (emptyArgs != null) {
//            Problem toAdd = emptyArgs;
//            while (toAdd.getNext() != null) {
//                toAdd = toAdd.getNext();
//            }
//            toAdd.setNext(result);
//            result = toAdd;
//        }
//        paramTable=oldParamTable;
//        this.modifier=oldModifier;
//        return result;
//    }
//    
//    /**
//     * Sets the parameters for refactoring. You can add new parameters or change
//     * order of parameters or change existing parameter.
//     *
//     * @param  aParamTable  table representing new parameters list
//     * @param  modifier  new modifier for the method
//     *
//     * @return  problem or a chain of problems
//     */
//    private Problem setParameters(ParameterInfo[] aParamTable, int modifier) {
//        paramTable = aParamTable;
//        this.modifier = modifier;
//        
//        return checkParameters(aParamTable, modifier);
//    }
//
//    /**
//     * Prepares the collection of refactoring elements. It will be added
//     * to elements bag, which is provided as a parameter.
//     *
//     * @param  elements bag to which the elements will be appended to
//     * @return chain of problems
//     */
//    public Problem prepare(RefactoringElementsBag elements) {
//        JavaMetamodel.getManager().getProgressSupport().addProgressListener(this);
//        Problem problem = null;
//        try {
//            // get the original access modifier and check, if the original
//            // modifier is weaker than the new modifier. If so, set checkMod
//            // to true and in following code check, if all usages will have 
//            // sufficient access privileges.
//            int origAccessMods = method.getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE);
//            boolean checkMod = compareModifiers(origAccessMods, modifier) == -1;
//            // get all the callers and usages of the callable and add them
//            // the the collection of refactored elements
//            referencesIterator = ((CallableFeatureImpl) method).findDependencies(true, true, true).iterator();
//            elements.add(refactoring, new SignatureElement(method, paramTable, modifier));
//            int parNum = ((CallableFeature) refactoring.getRefactoredObject()).getParameters().size();
//            while (referencesIterator.hasNext()) {
//                if (cancelRequest) {
//                    return null;
//                }
//                Object ref = referencesIterator.next();
//                if (ref instanceof Invocation) {
//                    // Callers. Refactored method has to have the same number
//                    // of parameters as its caller. (see issue #53041 for details)
//                    if (((Invocation) ref).getParameters().size() == parNum) {
//                        if (problem == null && checkMod) {
//                            // check the access to refactored method
//                            Feature f = JavaModelUtil.getDeclaringFeature((Invocation)ref);
//                            if (Modifier.isPrivate(modifier)) { // changing to private
//                                if (!Utilities.compareObjects(getOutermostClass(f), getOutermostClass(method))) {
//                                    String msg = getString("ERR_StrongAccMod"); // NOI18N
//                                    problem = new Problem(false, new MessageFormat(msg).format(new Object[] { "private" })); // NOI18N
//                                }
//                            } else if (Modifier.isProtected(modifier)) { // changing to protected
//                                if (!method.getResource().getPackageName().equals(f.getResource().getPackageName())) {
//                                    String msg = getString("ERR_StrongAccMod"); // NOI18N
//                                    problem = new Problem(false, new MessageFormat(msg).format(new Object[] { "protected" })); // NOI18N
//                                }
//                            } else if (modifier == 0) { // default modifier check
//                                if (!f.getResource().getPackageName().equals(method.getResource().getPackageName())) {
//                                    String msg = getString("ERR_StrongAccMod"); // NOI18N
//                                    problem = new Problem(false, new MessageFormat(msg).format(new Object[] { "default" })); // NOI18N
//                                }
//                            }
//                        }
//                        elements.add(refactoring, new CallerElement((Invocation) ref, paramTable));
//                    }
//                } else {
//                    // declaration/declarations (in case of overriden or overrides)
//                    elements.add(refactoring, new SignatureElement((CallableFeature) ref, paramTable, modifier));
//                }
//            }
//            return problem;
//        } 
//        finally {
//            referencesIterator = null;
//            JavaMetamodel.getManager().getProgressSupport().removeProgressListener(this);
//        }
//    }
//    
//    ////////////////////////////////////////////////////////////////////////////
//    
//    /**
//     * Instance of this class represents call to refactored method, i.e. method,
//     * whose signature is changed.
//     */
//    static class CallerElement extends SimpleRefactoringElementImpl {
//        private PositionBounds bounds;
//        private final Invocation element;
//        private ParameterInfo[] paramTable;
//        private final String text;
//        private final String displayText;
//        
//        // contains bounds for all original parameters. Used for obtaining original
//        // parameter value, when the parameter was moved to another place.
//        private List paramList;
//        // cache
//        private RefObject comp = null;
//
//        /**
//         * Creates new CallerElement. It represents Invocation to be
//         * refactored when signature is changed.
//         *
//         * @param element represents method invocation
//         * @param paramTable array of reordered, changed and added parameters
//         */    
//        public CallerElement(Invocation element,
//                             ParameterInfo[] paramTable)
//        {
//            paramList = Collections.EMPTY_LIST;
//            this.element = element;
//            this.paramTable = paramTable;
//            this.bounds = null;
//            Element disp = getDisplayElement(element);
//            int b = disp.getStartOffset();
//            int e = disp.getEndOffset();
//            int bb = element.getPartStartOffset(ElementPartKindEnum.NAME);
//            int be = element.getEndOffset();
//            String src = element.getResource().getSourceText();
//            text = src.substring(b, e);
//            displayText = src.substring(b, bb) + "<b>" + src.substring(bb, be) + "</b>" + src.substring(be, e); //NOI18N
//        }
//        
//        /**
//         * Returns the text describing the change provided by element.
//         *
//         * @return  the text describing element functionality
//         */        
//        public String getText() { return text; }
//
//        /**
//         * Returns text containing the description of the element
//         * (i.e. 'Change declaration' and the current declaration of the method.
//         *
//         * @return description text with method declaration change text
//         */
//        public String getDisplayText() { return displayText; }
//
//        /**
//         * Performs change on element. It is method invocation, it goes through
//         * the parameters provide in constructor in an array and creates new
//         * parameters in model.
//         */
//        public void performChange() {
//            List parameters = element.getParameters();
//            Element[] origParameters = (Element[]) parameters.toArray(new Element[0]);
//            parameters.clear();
//            for (int i = 0; i < paramTable.length; i++) {
//                ParameterInfo parInfo = paramTable[i];
//                int origIndex = parInfo.getOriginalIndex();
//                Element par;
//                if (origIndex == -1) {
//                    JavaModelPackage jmp = (JavaModelPackage) getJavaElement().refImmediatePackage();
//                    par = jmp.getMultipartId().createMultipartId(parInfo.getDefaultValue(), null, null);
//                }
//                else {
//                    par = origParameters[origIndex];
//                }
//                parameters.add(par);
//            }
//        }
//
//        /** 
//         * Returns Java element associated with this refactoring element.
//         *
//         * @return MDR Java element.
//         */
//        public Element getJavaElement() {
//            if (comp == null) {
//                comp = element;
//                while (!((comp instanceof Feature) || (comp instanceof Resource))) {
//                    comp = (RefObject) comp.refImmediateComposite();
//                }
//            }
//            return (Element) comp;
//        }
//
//        /**
//         *
//         * @return bounds bordering the element
//         */    
//        public PositionBounds getPosition() {
//            if (bounds == null) {
//                bounds = JavaMetamodel.getManager().getElementPosition(element);
//            }
//            return bounds;
//        }
//
//        ////////////////////////////////////////////////////////////////////////////
//        // PRIVATE MEMBERS
//        ////////////////////////////////////////////////////////////////////////////
//        private static Element getDisplayElement(Element obj) {
//            Element result = obj;
//            while (!((result instanceof Feature) || (result.refImmediateComposite() instanceof StatementBlock))) {
//                result = (Element) result.refImmediateComposite();
//            }
//            return result;
//        }
//
//        public FileObject getParentFile() {
//            return null;
//        }
//    }
//    
//    ////////////////////////////////////////////////////////////////////////////
//    
//    /**
//     * Instance of this class is part of the change method signature refactoring.
//     * It is responsible for changing method declaration - it doesn't change
//     * references.
//     */
//    static class SignatureElement extends SimpleRefactoringElementImpl {
//        
//        private PositionBounds bounds;
//        private final CallableFeature element;
//        private ParameterInfo[] paramTable;
//        private int modifier;
//        private final String displayText;
//        private final String text;
//
//        /**
//         * Creates refactoring elements, which is responsible for changing
//         * method (constructor) declaration.
//         *
//         * @param element     element, which will be changed
//         * @param paramTable  array of Lists, which contains information about
//         *                    parameters changes
//         * @param modifier    contains information about modifier change
//         */
//        public SignatureElement(CallableFeature element,
//                                ParameterInfo[] paramTable,
//                                int modifier)
//        {
//            this.element = element;
//            this.paramTable = paramTable;
//            this.bounds = null;
//            this.modifier = modifier;
//
//            // initialize text and displayText
//            String decl = ChangeParametersPlugin.getString("LBL_chngsigdecl"); //NOI18N
//            Object[] args = new Object[2];
//            String key = element instanceof Method ? "LBL_Method" : "LBL_Constructor"; //NOI18N
//            args[0] = ChangeParametersPlugin.getString(key);
//            int b = element.getPartStartOffset(ElementPartKindEnum.HEADER);
//            int e = element.getPartEndOffset(ElementPartKindEnum.HEADER);
//            args[1] = element.getResource().getSourceText().substring(b, e);
//            text = MessageFormat.format(decl, args);
//            args[1] = "<b>" + args[1] + "</b>"; //NOI18N
//            displayText = MessageFormat.format(decl, args);
//        }
//
//        /**
//         * Returns the text describing the change provided by element.
//         *
//         * @return  the text describing element functionality
//         */        
//        public String getText() { return text; }
//
//        /**
//         * Returns text containing the description of the element
//         * (i.e. 'Change declaration' and the current declaration of the method.
//         *
//         * @return description text with method declaration change text
//         */
//        public String getDisplayText() { return displayText; }
//
//        /**
//         * Performs the change on header. Change the parameter if the modifier
//         * was change and also do the parameters changes in header.
//         */
//        public void performChange() {
//            List parameters = element.getParameters();
//            Parameter[] origParameters = (Parameter[]) parameters.toArray(new Parameter[0]);
//            // set new access modifier
//            int oldMod = element.getModifiers();
//            if ((oldMod & modifier) == 0) {
//                int newMod = oldMod & ~(Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE) | modifier;
//                element.setModifiers(newMod);
//            }
//            parameters.clear();
//            JavaModelPackage modelPackage = (JavaModelPackage) getJavaElement().refImmediatePackage();
//            for (int i = 0; i < paramTable.length; i++) {
//                ParameterInfo parInfo = paramTable[i];
//                int origIndex = parInfo.getOriginalIndex();
//                Parameter parameter = null;
//                // for the new parameter
//                if (origIndex == -1) {
//                    parameter = modelPackage.getParameter().createParameter();
//                    parameter.setName(parInfo.getName());
//                    parameter.setType(parInfo.getType());
//                }
//                else {
//                    parameter = origParameters[origIndex];
//                    Object val;
//                    if ((val = parInfo.getName()) != null)
//                        parameter.setName((String) val);
//                    if ((val = parInfo.getType()) != null)
//                        parameter.setType((Type) val);
//                }
//                parameters.add(parameter);
//            }
//        }
//        
//        /**
//         * Do undo on the element.
//         */
//        public void undoChange() {
//            throw new UnsupportedOperationException();
//        }
//
//        /**
//         * Returns refactored java element. (method or constructor)
//         *
//         * @return  java element, which is refactored. It is always
//         *          CallableFeature, i.e. method or constructor .
//         */        
//        public Element getJavaElement() { return element; }
//
//        /**
//         * Returns bounds of the declarion, e.g. testMethod(int a, int b).
//         *
//         * @return bounds of the method declaration
//         */        
//        public PositionBounds getPosition() {
//            if (bounds == null) {
//                bounds = JavaMetamodel.getManager().getElementPosition(element);
//            }
//            return bounds;
//        }
//
//        public FileObject getParentFile() {
//            return null;
//        }
//
//    }
//    
//    ////////////////////////////////////////////////////////////////////////////
//    // PRIVATE MEMBERS
//    ////////////////////////////////////////////////////////////////////////////
//    private Problem checkParameterAttributes() {
//        // check, if the default values for all new added parameters are present
//        Problem p = null;
//        
//        for (int i = 0; i < paramTable.length; i++) {
//            int origIndex = paramTable[i].getOriginalIndex();
//     
//            // check parameter name
//            String s;
//            s = paramTable[i].getName();
//            if (origIndex == -1 && (s == null || s.length() < 1))
//                p = createProblem(p, true, newParMessage("ERR_parname")); // NOI18N
//
//            // check parameter type
//            Type t = paramTable[i].getType();
//            if (origIndex == -1 && t == null)
//                p = createProblem(p, true, newParMessage("ERR_partype")); // NOI18N
//
//            // check the default value
//            s = paramTable[i].getDefaultValue();
//            if (origIndex == -1 && (s == null || s.length() < 1))
//                p = createProblem(p, true, newParMessage("ERR_pardefv")); // NOI18N
//        }
//        return p;
//    }
//    
//    private Method methodClashes(ParameterInfo[] parInfo) {
//        // check, if there is any existing method with the same signature
//        if (!(method instanceof Method))
//            return null;
//        List paramTypes = new ArrayList(parInfo.length);
//        List parameters = method.getParameters();
//        for (int i = 0; i < parInfo.length; i++) {
//            Type t = parInfo[i].getType();
//            if (t == null) t = ((Parameter) parameters.get(parInfo[i].getOriginalIndex())).getType();
//            paramTypes.add(t);
//        }
//        return method.getDeclaringClass().getMethod(method.getName(), paramTypes, false);
//    }
//
//    private Constructor constructorClashes(ParameterInfo[] parInfo) {
//        // check, if there is any existing constructor with the same signature
//        if (!(method instanceof Constructor))
//            return null;
//        List paramTypes = new ArrayList(parInfo.length);
//        List parameters = method.getParameters();
//        for (int i = 0; i < parInfo.length; i++) {
//            Type t = parInfo[i].getType();
//            if (t == null) t = ((Parameter) parameters.get(parInfo[i].getOriginalIndex())).getType();
//            paramTypes.add(t);
//        }
//        return method.getDeclaringClass().getConstructor(paramTypes,false);
//    }
//    
//    private static String newParMessage(String par) {
//        return new MessageFormat(
//                getString("ERR_newpar")).format(new Object[] { getString(par) } // NOI18N
//            );
//    }
//    
//    private static String getString(String key) {
//        return NbBundle.getMessage(ChangeParametersRefactoring.class, key);
//    }
//
//    private String getDefClassName(ClassDefinition dc) {
//        if (dc instanceof JavaClass) {
//            return ((JavaClass) dc).getName();
//        } 
//        else {
//            return "";
//        }
//    }
//
//    /**
//     * Compares the strength of access modifiers.
//     *
//     * @param    first access modifier
//     * @param    second access modifier
//     * @return   0 if modifiers equals,
//     *          -1 if first is weaker then second (e.g. first = PUBLIC, second = PRIVATE)
//     *           1 otherwise (second is weaker then first)
//     */
//    private static int compareModifiers(int first, int second) {
//        // nothing to compute when both modifiers are the same
//        if (first == second)
//            return 0;
//            
//        int[] mods = new int[] { first, second };
//        // use constans in order of their strentgh, i.e.
//        // 0... PUBLIC, 1... PROTECTED, 2... DEFAULT (no modifier), 3... PRIVATE
//        for (int i = 0; i < 2; i++) {
//            // Don't be angry when you will read next line, please. ;-)
//            mods[i] = Modifier.isPublic(mods[i]) ? 0 : Modifier.isProtected(mods[i]) ? 1 : Modifier.isPrivate(mods[i]) ? 3 : 2;
//        }
//        return mods[0] > mods[1] ? 1 : -1;
//    }
//
//    /**
//     * Returns the outermost class for a feature.
//     *
//     * @param   feature
//     * @return  outermost class
//     */
//    private static JavaClass getOutermostClass(Feature feature) {
//        Object o = feature.refImmediateComposite();
//        Object lastComposite = null;
//        while (o != null && !(o instanceof Resource)) {
//            lastComposite = o;
//            o = ((RefObject) o).refImmediateComposite();
//        }
//        if (lastComposite instanceof JavaClass) {
//            return (JavaClass) lastComposite;
//        } else {
//            return null;
//        }
//    }
    
    public void start(ProgressEvent event) {
        fireProgressListenerStart(event.getOperationType(), event.getCount());
    }
    
    public void step(ProgressEvent event) {
        fireProgressListenerStep();
    }
    
    public void stop(ProgressEvent event) {
        fireProgressListenerStop();
    }

}
