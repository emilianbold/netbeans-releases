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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.java.api.PullUpRefactoring;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle;


/** Plugin that implements the core functionality of Pull Up refactoring.
 *
 * @author Martin Matula
 * @author Jan Becicka
 */
public final class PullUpRefactoringPlugin extends JavaRefactoringPlugin {
    /** Reference to the parent refactoring instance */
    private final PullUpRefactoring refactoring;
    private TreePathHandle treePathHandle;
    
    
    /** Creates a new instance of PullUpRefactoringPlugin
     * @param refactoring Parent refactoring instance.
     */
    PullUpRefactoringPlugin(PullUpRefactoring refactoring) {
        this.refactoring = refactoring;
        this.treePathHandle = refactoring.getSourceType();
    }

    protected JavaSource getJavaSource(Phase p) {
        switch (p) {
        default: 
            return JavaSource.forFileObject(treePathHandle.getFileObject());
        }
    }
    
    @Override
    protected Problem preCheck(CompilationController cc) throws IOException {
        fireProgressListenerStart(AbstractRefactoring.PRE_CHECK, 4);
        try {
            cc.toPhase(JavaSource.Phase.RESOLVED);
            Problem problem = isElementAvail(treePathHandle, cc);
            if (problem != null) {
                // fatal error -> don't continue with further checks
                return problem;
            }
            if (!RetoucheUtils.isElementInOpenProject(treePathHandle.getFileObject())) {
                return new Problem(true, NbBundle.getMessage(PullUpRefactoringPlugin.class, "ERR_ProjectNotOpened"));
            }


            // increase progress (step 1)
            fireProgressListenerStep();
            TypeElement e  = (TypeElement) treePathHandle.resolveElement(cc);
            if (RetoucheUtils.getSuperTypes(e, cc, true).isEmpty()) {
                return new Problem(true, NbBundle.getMessage(PullUpRefactoringPlugin.class, "ERR_PullUp_NoSuperTypes")); // NOI18N
            }
            // increase progress (step 2)
            fireProgressListenerStep();
            // #2 - check if there are any members to pull up
            Element el = treePathHandle.resolveElement(cc);
            for (Element element : el.getEnclosedElements()) {
                if (element.getKind() != ElementKind.CONSTRUCTOR) {
                    return null;
                }
            }
            problem = new Problem(true, NbBundle.getMessage(PullUpRefactoringPlugin.class, "ERR_PullUp_NoMembers")); // NOI18N
            // increase progress (step 3)
            fireProgressListenerStep();
            return problem;
        } finally {
            fireProgressListenerStop();
        }
    }
    
    
    @Override
    public Problem fastCheckParameters() {
        MemberInfo[] info = refactoring.getMembers();
        // #1 - check whether there are any members to pull up
        if (info.length == 0) {
            return new Problem(true, NbBundle.getMessage(PullUpRefactoringPlugin.class, "ERR_PullUp_NoMembersSelected")); // NOI18N
        }
        
        if (info.length > 1) {
            for (int i=0; i<info.length - 1; i++) {
                for (int j = i + 1; j < info.length; j++) {
                    if (info[i].equals(info[j])) {
                        return new Problem(true, NbBundle.getMessage(PullUpRefactoringPlugin.class, "ERR_CannotPullupDuplicateMembers"));
                    }
                }
            }
        }
        
        // #2 - check if the targed type is not null
        if (refactoring.getTargetType() == null) {
            return new Problem(true, NbBundle.getMessage(PullUpRefactoringPlugin.class, "ERR_PullUp_NoTargetType")); // NOI18N
        }
        
        return null;
    }

    @Override
    protected Problem checkParameters(CompilationController cc) throws IOException {
        fireProgressListenerStart(AbstractRefactoring.PRE_CHECK, 4);
        try {
            cc.toPhase(JavaSource.Phase.RESOLVED);
            TypeElement sourceType = (TypeElement) refactoring.getSourceType().resolveElement(cc);
            Collection<TypeElement> supers = RetoucheUtils.getSuperTypes(sourceType, cc);
            TypeElement targetType = refactoring.getTargetType().resolve(cc);
            MemberInfo<ElementHandle<? extends Element>>[] members = refactoring.getMembers();

            fireProgressListenerStart(AbstractRefactoring.PARAMETERS_CHECK, members.length + 1);
            // #1 - check whether the target type is a legal super type
            if (!supers.contains(targetType)) {
                return new Problem(true, NbBundle.getMessage(PullUpRefactoringPlugin.class, "ERR_PullUp_IllegalTargetType")); // NOI18N
            }

            fireProgressListenerStep();

            // TODO: what the hell is this check
            // #2 - check whether all the members are legal members that can be pulled up
            //                    HashSet visitedSources = new HashSet();
            //                    //            HashSet allMembers = new HashSet(Arrays.asList(members));
            Problem problems = null;
            //                    visitedSources.add(refactoring.getSourceType());
            for (int i = 0; i < members.length; i++) {
                Element cls;
                Element member = members[i].getElementHandle().resolve(cc);
                if (members[i].getGroup()!=MemberInfo.Group.IMPLEMENTS) {
                    //                            // member is a feature (inner class, field or method)
                    //                            cls = member.getEnclosingElement();
                    //                        } else {
                    //                            // member is an interface from implements clause
                    //                            MultipartId ifcName = (MultipartId) member;
                    //                            // get parent of the element (should be class if this is really
                    //                            // a name from implements clause
                    //                            Object parent = ifcName.refImmediateComposite();
                    //                            // if parent is not a class, member is invalid
                    //                            if (!(parent instanceof JavaClass)) {
                    //                                cls = null;
                    //                            } else {
                    //                                // check if the parent class contains this MultipartId
                    //                                // in interfaceNames
                    //                                if (!((JavaClass) parent).getInterfaceNames().contains(ifcName)) {
                    //                                    cls = null;
                    //                                } else {
                    //                                    cls = (ClassDefinition) parent;
                    //                                }
                    //                            }
                    //                        }
                    //                        // if the declaring class has not been visited yet, perform checks on it
                    //                        if (visitedSources.add(cls)) {
                    //                            // if the declaring class of a feature is not a JavaClass,
                    //                            // or if it is not from the set of source type's supertypes
                    //                            // or if the declaring class is not a subtype of target class
                    //                            // then this member is illegal
                    //                            if (!(cls instanceof JavaClass) || !supers.contains(cls) || cls.equals(targetType) || !cls.isSubTypeOf(targetType)) {
                    //                                return createProblem(problems, true, NbBundle.getMessage(PullUpRefactoringPlugin.class, "ERR_PullUp_IllegalMember", member.getName())); // NOI18N
                    //                            }
                    //                        }
                    // #3 - check if the member already exists in the target class

                    if (RetoucheUtils.elementExistsIn(targetType, member, cc)) {
                        return createProblem(problems, true, NbBundle.getMessage(PullUpRefactoringPlugin.class, "ERR_PullUp_MemberAlreadyExists", member.getSimpleName())); // NOI18N
                    }

                    // #4 - check if the field does not use something that is not going to be pulled up
                    //                Resource sourceResource = refactoring.getSourceType().getResource();
                    //                Resource targetResource = targetType.getResource();
                    //                if (!sourceResource.equals(targetResource)) {
                    //                    problems = checkUsedByElement(member, allMembers, problems,
                    //                            !sourceResource.equals(targetResource),
                    //                            !sourceResource.getPackageName().equals(targetResource.getPackageName()));
                    //                }

                    fireProgressListenerStep();
                }

                // TODO: implement non-fatal checks
            }
        } finally {
            fireProgressListenerStop();
        }
        return null;
    }
    
    public Problem prepare(RefactoringElementsBag refactoringElements) {
//        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        
        Set<FileObject> a = new HashSet<FileObject>();
        a.addAll(RetoucheUtils.getSuperTypesFiles(refactoring.getSourceType()));
        a.add(RetoucheUtils.getFileObject(treePathHandle));
        fireProgressListenerStart(ProgressEvent.START, a.size());
        TransformTask task = new TransformTask(new PullUpTransformer(refactoring), treePathHandle);
        createAndAddElements(a, task, refactoringElements, refactoring);
        fireProgressListenerStop();
        return null;
    }

    protected FileObject getFileObject() {
        return treePathHandle.getFileObject();
    }
    
    //    public Problem prepare(RefactoringElementsBag refactoringElements) {
    
    
    
    //    public Problem prepare(RefactoringElementsBag refactoringElements) {
    
    
    
    //    public Problem prepare(RefactoringElementsBag refactoringElements) {
    //        PullUpRefactoring.MemberInfo[] members = refactoring.getMembers();
    //
    //        boolean makeTargetTypeAbstract = false;
    //
    //        for (int i = 0; i < members.length; i++) {
    //            int modifiers = (members[i].member instanceof MultipartId) ? 0 : ((Feature) members[i].member).getModifiers();
    //            int newmodifiers = 0;
    //            if (refactoring.getTargetType().isInterface()) {
    //                newmodifiers = (modifiers | Modifier.PUBLIC) & ~Modifier.PRIVATE & ~Modifier.PROTECTED;
    //            } else {
    //                if (!(Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers))) {
    //                    newmodifiers = (modifiers | Modifier.PROTECTED) & ~Modifier.PRIVATE;
    //                }
    //            }
    //            if (members[i].makeAbstract) {
    //                refactoringElements.add(refactoring, new AddAbstractMethodElement((Method) members[i].member, refactoring.getTargetType(), newmodifiers));
    //                makeTargetTypeAbstract = true;
    //                if (newmodifiers != 0) {
    //                    refactoringElements.add(refactoring, new ChangeModElement((Feature) members[i].member, newmodifiers));
    //                }
    //            } else {
    //                refactoringElements.add(refactoring, new MoveMemberElement(members[i].member, refactoring.getTargetType(), newmodifiers));
    //                if (Modifier.isAbstract(modifiers)) {
    //                    makeTargetTypeAbstract = true;
    //                }
    //            }
    //        }
    //
    //        if (makeTargetTypeAbstract && !Modifier.isAbstract(refactoring.getTargetType().getModifiers()) && !refactoring.getTargetType().isInterface()) {
    //            refactoringElements.add(refactoring, new ChangeModElement(refactoring.getTargetType(), refactoring.getTargetType().getModifiers() | Modifier.ABSTRACT));
    //        }
    //
    //        UndoWatcher.watch(((JMManager) JMManager.getManager()).getDataObject(refactoring.getTargetType().getResource()));
    //
    //
    //        // TODO: add refactoring element for changing modifiers of the target class
    //        // if necessary
    //
    //        return null;
    //    }
    //
    //    // --- REFACTORING ELEMENTS ------------------------------------------------
    //
    //    /** Refactoring element that takes care of adding an abstract method declaration
    //     * to the target class.
    //     */
    //    private static class AddAbstractMethodElement extends SimpleRefactoringElementImpl {
    //        private final Method methodToAdd;
    //        private final JavaClass target;
    //        private final int newModifiers;
    //        private final String text;
    //
    //        /** Creates a new instance of this refactoring element.
    //         * @methodToAdd Method in the source class that should be declared as abstract
    //         *      in the target class.
    //         * @target Target class.
    //         * @newModifiers New modifiers of the method or 0 if the modifiers should be
    //         *      the same as for the original method (+ <code>abstract</code> modifier).
    //         */
    //        AddAbstractMethodElement(Method methodToAdd, JavaClass target, int newModifiers) {
    //            this.methodToAdd = methodToAdd;
    //            this.target = target;
    //            this.newModifiers = newModifiers;
    //            this.text = NbBundle.getMessage(PullUpRefactoringPlugin.class, "TXT_PullUp_AddMethod", UIUtilities.getDisplayText(methodToAdd)); // NOI18N
    //        }
    //
    //        public void performChange() {
    //            // get extent of the target method
    //            JavaModelPackage extent = (JavaModelPackage) target.refImmediatePackage();
    //            // create the abstract method in this extent (duplicating the header
    //            // of the existing method
    //            Method newMethod = extent.getMethod().createMethod(
    //                    methodToAdd.getName(),
    //                    Utilities.duplicateList(methodToAdd.getAnnotations(), extent),
    //                    (newModifiers == 0 ? methodToAdd.getModifiers() : newModifiers) | Modifier.ABSTRACT,
    //                    methodToAdd.getJavadocText(),
    //                    null,
    //                    null,
    //                    null,
    //                    Utilities.duplicateList(methodToAdd.getTypeParameters(), extent),
    //                    Utilities.duplicateList(methodToAdd.getParameters(), extent),
    //                    Utilities.duplicateList(methodToAdd.getExceptionNames(), extent),
    //                    (TypeReference) ((MetadataElement) methodToAdd.getTypeName()).duplicate(extent),
    //                    methodToAdd.getDimCount()
    //                );
    //            // add the new method to the target class
    //            if (target.isInterface()) {
    //                newMethod.setModifiers(newMethod.getModifiers() & ~Modifier.ABSTRACT & ~Modifier.PUBLIC & ~Modifier.PROTECTED & ~Modifier.PRIVATE);
    //            }
    //            target.getContents().add(newMethod);
    //        }
    //
    //        public String getText() {
    //            return text;
    //        }
    //
    //        public String getDisplayText() {
    //            return text;
    //        }
    //
    //        public FileObject getParentFile() {
    //            return JavaMetamodel.getManager().getFileObject(target.getResource());
    //        }
    //
    //        public Element getJavaElement() {
    //            return target;
    //        }
    //
    //        public PositionBounds getPosition() {
    //            return null;
    //        }
    //    }
    //
    //    /** Refactoring element that takes care of moving an element to the target type.
    //     */
    //    private static class MoveMemberElement extends SimpleRefactoringElementImpl {
    //        private final NamedElement elementToMove;
    //        private final JavaClass target;
    //        private final int newModifiers;
    //        private final String text;
    //
    //        /** Creates a new instance of this refactoring element.
    //         * @elementToMove Element to be moved to the target type.
    //         * @target The target type the element should be moved to.
    //         * @newModifiers New modifiers of the element or 0 if the modifiers should
    //         *      remain unchanged.
    //         */
    //        MoveMemberElement(NamedElement elementToMove, JavaClass target, int newModifiers) {
    //            this.elementToMove = elementToMove;
    //            this.target = target;
    //            this.newModifiers = newModifiers;
    //            this.text = NbBundle.getMessage(PullUpRefactoringPlugin.class, "TXT_PullUp_Member", UIUtilities.getDisplayText(elementToMove)); // NOI18N
    //        }
    //
    //        public void performChange() {
    //            JavaModelPackage targetExtent = (JavaModelPackage) target.refImmediatePackage();
    //            JavaClass elementParent;
    //            Element newElement;
    //            boolean deleteElementToMove = false;
    //            // processing is different for Feature (field, inner class, method)
    //            // and MultipartId (interface in the implements clause)
    //            if (elementToMove instanceof Feature) {
    //                // get the declaring class of the element
    //                elementParent = (JavaClass) ((Feature) elementToMove).getDeclaringClass();
    //                // check if the declaring type is in the same extent as the target type
    //                if (targetExtent.equals(elementParent.refImmediatePackage())) {
    //                    // if so, a simple move is possible
    //                    elementParent.getFeatures().remove(elementToMove);
    //                    newElement = elementToMove;
    //                } else {
    //                    // otherwise we need to create a copy of the element in the target extent
    //                    newElement = ((MetadataElement) elementToMove).duplicate(targetExtent);
    //                    // and delete the original element in the source extent
    //                    deleteElementToMove = true;
    //                }
    //                // add the element to the target class
    //                target.getContents().add(newElement);
    //                // change modifiers if necessary
    //                if (newModifiers != 0) {
    //                    ((Feature) newElement).setModifiers(newModifiers);
    //                }
    //            } else {
    //                // get parent type of the element
    //                elementParent = (JavaClass) elementToMove.refImmediateComposite();
    //                // check if the target extent is the same as the source extent
    //                if (targetExtent.equals(elementParent.refImmediatePackage())) {
    //                    // yes -> simple move
    //                    elementParent.getInterfaceNames().remove(elementToMove);
    //                    newElement = elementToMove;
    //                } else {
    //                    // no -> a new copy in the target extent needs to be created
    //                    newElement = ((MetadataElement) elementToMove).duplicate(targetExtent);
    //                    deleteElementToMove = true;
    //                }
    //                // add the new element to the implements clause of the target type
    //                target.getInterfaceNames().add(newElement);
    //            }
    //            ((MetadataElement) newElement).fixImports(target, elementToMove);
    //            if (deleteElementToMove) {
    //                elementToMove.refDelete();
    //            }
    //        }
    //
    //        public String getText() {
    //            return text;
    //        }
    //
    //        public String getDisplayText() {
    //            return text;
    //        }
    //
    //        public FileObject getParentFile() {
    //            return JavaMetamodel.getManager().getFileObject(elementToMove.getResource());
    //        }
    //
    //        public Element getJavaElement() {
    //            return JavaModelUtil.getDeclaringFeature(elementToMove);
    //        }
    //
    //        public PositionBounds getPosition() {
    //            return JavaMetamodel.getManager().getElementPosition(elementToMove);
    //        }
    //    }
    //
    //    // --- HELPER METHODS ------------------------------------------------------
    //
    //    // checks if the source type or any of its supertypes has any members that could
    //    // be pulled up
    //    private static boolean hasMembers(JavaClass sourceType, JavaClass[] supertypes) {
    //        boolean result = Utilities.hasMembers(sourceType);
    //
    //        for (int i = 0; i < (supertypes.length - 1) && !result; i++) {
    //            result = Utilities.hasMembers(supertypes[i]);
    //        }
    //
    //        return result;
    //    }
    //
    //    // checks if the element is used by other element which will not be pulled up
    //    private Problem checkUsedByElement(Element element, Set allMembers, Problem problems, boolean resourceChange, boolean packageChange) {
    //        if (element instanceof MultipartId) {
    //            // TODO: check with import management tool, whether an import should be added
    //        } else if (element instanceof MethodInvocation
    //                || element instanceof NewClassExpression
    //                || element instanceof VariableAccess) {
    //            NamedElement referencedElement = ((ElementReference) element).getElement();
    //            if (referencedElement instanceof Feature) {
    //                Feature referencedFeature = (Feature) referencedElement;
    //                int modifiers = referencedFeature.getModifiers();
    //                ClassDefinition declClass = referencedFeature.getDeclaringClass();
    //                PrimaryExpression parentClass;
    //                if (element instanceof VariableAccess) {
    //                    parentClass = ((VariableAccess) element).getParentClass();
    //                } else if (element instanceof MethodInvocation) {
    //                    parentClass = ((MethodInvocation) element).getParentClass();
    //                } else {
    //                    if ((declClass instanceof JavaClass) && !Modifier.isStatic(((JavaClass) declClass).getModifiers())) {
    //                        parentClass = ((NewClassExpression) element).getEnclosingClass();
    //                    } else {
    //                        parentClass = null;
    //                    }
    //                }
    //                boolean isSameInstance = (parentClass == null) || (parentClass instanceof ThisExpression);
    //                if (Modifier.isPrivate(modifiers)) {
    //                    if (resourceChange || isSameInstance) {
    //                        problems = createProblem(problems, false, "will not be accessible"); // NOI18N
    //                    }
    //                } else if (packageChange && !Modifier.isPublic(modifiers)) {
    //                    if (Modifier.isProtected(modifiers) && (referencedElement instanceof Method)) {
    //                        // TODO: check if the method is not defined also in target or one of its supers ->
    //                        // in that case do not generate a problem
    //                    }
    //                    problems = createProblem(problems, false, "will not be accessible - is protected/package private"); // NOI18N
    //                }
    //            }
    //        }
    //
    //        for (Iterator it = element.getChildren().iterator(); it.hasNext();) {
    //            problems = checkUsedByElement((Element) it.next(), allMembers, problems, resourceChange, packageChange);
    //        }
    //
    //        return problems;
    //    }

}
