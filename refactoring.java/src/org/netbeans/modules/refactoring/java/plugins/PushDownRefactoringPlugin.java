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

import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.*;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.PushDownRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 * Plugin that implements the core functionality of Push Down refactoring.
 *
 * @author Pavel Flaska
 * @author Jan Becicka
 */
public final class PushDownRefactoringPlugin extends JavaRefactoringPlugin {
    
    /** Reference to the parent refactoring instance */
    private final PushDownRefactoring refactoring;
    private TreePathHandle treePathHandle;
    
    /** Creates a new instance of PushDownRefactoringPlugin */
    public PushDownRefactoringPlugin(PushDownRefactoring refactoring) {
        this.refactoring = refactoring;
        treePathHandle = refactoring.getSourceType();
    }
    
    protected JavaSource getJavaSource(Phase p) {
        //TODO: wrong classpath
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
            Problem precheckProblem = isElementAvail(treePathHandle, cc);
            if (precheckProblem != null) {
                // fatal error -> don't continue with further checks
                return precheckProblem;
            }
            if (!RetoucheUtils.isElementInOpenProject(treePathHandle.getFileObject())) {
                return new Problem(true, NbBundle.getMessage(PushDownRefactoringPlugin.class, "ERR_ProjectNotOpened"));
            }


            // increase progress (step 1)
            fireProgressListenerStep();
            ElementHandle<TypeElement> eh = ElementHandle.create((TypeElement) treePathHandle.resolveElement(cc));
            Set<FileObject> resources = cc.getClasspathInfo().getClassIndex().getResources(eh, EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.of(ClassIndex.SearchScope.SOURCE));
            if (resources.isEmpty()) {
                return new Problem(true, NbBundle.getMessage(PushDownRefactoringPlugin.class, "ERR_PushDOwn_NoSubtype")); // NOI18N
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
            precheckProblem = new Problem(true, NbBundle.getMessage(PushDownRefactoringPlugin.class, "ERR_PushDown_NoMembers")); // NOI18N
            // increase progress (step 3)
            fireProgressListenerStep();
            return precheckProblem;
        } finally {
            fireProgressListenerStop();
        }
    }

    @Override
    public Problem checkParameters() {
        return null;
    }


    @Override
    protected Problem fastCheckParameters(CompilationController info) {
        // #1 - check whether there are any members to pull up
        if (refactoring.getMembers().length == 0) {
            return new Problem(true, NbBundle.getMessage(PushDownRefactoringPlugin.class, "ERR_PushDown_NoMembersSelected")); // NOI18N
        }
        return null;
    }
   
    private Set<FileObject> getRelevantFiles(TreePathHandle handle) {
        ClasspathInfo cpInfo = getClasspathInfo(refactoring);
        ClassIndex idx = cpInfo.getClassIndex();
        Set<FileObject> set = new HashSet<FileObject>();
        set.add(RetoucheUtils.getFileObject(handle));
        set.addAll(idx.getResources(RetoucheUtils.getElementHandle(handle), EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS),EnumSet.of(ClassIndex.SearchScope.SOURCE)));
        return set;
    }    
    
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        Set<FileObject> a = getRelevantFiles(treePathHandle);
        fireProgressListenerStart(ProgressEvent.START, a.size());
        TransformTask task = new TransformTask(new PushDownTransformer(refactoring.getMembers()), treePathHandle);
        createAndAddElements(a, task, refactoringElements, refactoring);
        fireProgressListenerStop();
        return null;    
    }

    protected FileObject getFileObject() {
        return treePathHandle.getFileObject();
    }
    
//    public JavaClass[] collectSubtypes() {
    
//    public JavaClass[] collectSubtypes() {
    
//    public JavaClass[] collectSubtypes() {
//        if (subtypes == null) {
//            if (sourceType != null) {
//                Collection c = sourceType.findSubTypes(false);
//                subtypes = (JavaClass[]) c.toArray(new JavaClass[c.size()]);
//            } else {
//                subtypes = new JavaClass[0];
//            }
//        }
//        return subtypes;
//    }
    

//    public Problem prepare(RefactoringElementsBag refactoringElements) {
//        PushDownRefactoring.MemberInfo[] members = refactoring.getMembers();
//        List featuresToMove = new ArrayList();
//        JavaClass[] subtypes = refactoring.collectSubtypes();
//        for (int i = 0; i < members.length; i++) {
//            // go through all subtypes where method should be copied
//            if (members[i].member instanceof Feature) {
//                featuresToMove.add(members[i].member);
//            }
//            for (int j = 0; j < subtypes.length; j++) {
//                boolean enabled = true;
//                for (Iterator it = subtypes[j].getFeatures().iterator(); it.hasNext();) {
//                    Feature f = (Feature) it.next();
//                    if (CheckUtils.membersEqual(f, members[i].member)) {
//                        enabled = false;
//                        break;
//                    }
//                }
//                refactoringElements.add(refactoring, new CopyMemberElement(members[i].member, subtypes[j], 0, enabled));
//                UndoWatcher.watch(((JMManager) JMManager.getManager()).getDataObject(subtypes[j].getResource()));
//            }
//            // register remove element, which removes original method which
//            // will be copied to subtypes. If user checks make it abstract
//            // checkbox, the element will not remove the method, instead it,
//            // it will change method to be declared as abstract
//            refactoringElements.add(refactoring, new RemoveOriginalElement(members[i].member, members[i].makeAbstract));
//        }
//
//        collectRelaxModifierElements(refactoring, refactoringElements, refactoring.getSourceType(), featuresToMove);
//        return null;
//    }
//    
//    // --- REFACTORING ELEMENTS ------------------------------------------------
//    /** Refactoring element that takes care of moving an element to the target type.
//     */
//    private static class RemoveOriginalElement extends SimpleRefactoringElementImpl {
//        private final NamedElement elementToRemove;
//        private final String text;
//        private final boolean makeItAbstract;
//        
//        RemoveOriginalElement(NamedElement elementToRemove, boolean makeItAbstract) {
//            this.elementToRemove = elementToRemove;
//            this.makeItAbstract = makeItAbstract;
//            this.text = NbBundle.getMessage(PushDownRefactoringPlugin.class, 
//                    makeItAbstract ? "TXT_PushDown_Abstract": "TXT_PushDown_Remove",  // NOI18N
//                    UIUtilities.getDisplayText(elementToRemove));
//        }
//        
//        public void performChange() {
//            if (makeItAbstract) {
//                Method m = (Method) elementToRemove;
//                m.setModifiers(m.getModifiers() | Modifier.ABSTRACT);
//                m.setBodyText(null);
//            } else
//                elementToRemove.refDelete();
//        }
//
//        public String getText() {
//            return text;
//        }
//
//        public PositionBounds getPosition() {
//            return JavaMetamodel.getManager().getElementPosition(elementToRemove);
//        }
//
//        public FileObject getParentFile() {
//            return JavaMetamodel.getManager().getFileObject(elementToRemove.getResource());
//        }
//
//        public Element getJavaElement() {
//            return (Element) elementToRemove.refImmediateComposite();
//        }
//
//        public String getDisplayText() {
//            return text;
//        }
//    }
//    
//    private static class CopyMemberElement extends SimpleRefactoringElementImpl {
//        private final NamedElement elementToCopy;
//        private final JavaClass target;
//        private final int newModifiers;
//        private final String text;
//        
//        /**
//         * Creates a new instance of this refactoring element.
//         * @elementToCopy Element to be moved to the target type.
//         * @target The target type the element should be moved to.
//         * @newModifiers New modifiers of the element or 0 if the modifiers should
//         *      remain unchanged.
//         */
//        CopyMemberElement(NamedElement elementToCopy, JavaClass target, int newModifiers, boolean enabled) {
//            this.elementToCopy = elementToCopy;
//            this.target = target;
//            this.newModifiers = newModifiers;
//            this.text = NbBundle.getMessage(PushDownRefactoringPlugin.class, "TXT_PushDown_Member", // NOI18N
//                    UIUtilities.getDisplayText(elementToCopy),
//                    UIUtilities.getDisplayText(target)); 
//            setEnabled(enabled);
//        }
//
//        public void performChange() {
//            JavaModelPackage targetExtent = (JavaModelPackage) target.refImmediatePackage();
//            JavaClass elementParent;
//            Element newElement;
//            // processing is different for Feature (field, inner class, method)
//            // and MultipartId (interface in the implements clause)
//            if (elementToCopy instanceof Feature) {
//                // get the declaring class of the element
//                elementParent = (JavaClass) ((Feature) elementToCopy).getDeclaringClass();
//                // we need to create a copy of the element in the target extent
//                newElement = ((MetadataElement) elementToCopy).duplicate(targetExtent);
//                // add the element to the target class
//                target.getContents().add(newElement);
//                // change modifiers if necessary
//                if (newModifiers != 0) {
//                    ((Feature) newElement).setModifiers(newModifiers);
//                }
//            } else {
//                // get parent type of the element
//                elementParent = (JavaClass) elementToCopy.refImmediateComposite();
//                newElement = ((MetadataElement) elementToCopy).duplicate(targetExtent);
//                // add the new element to the implements clause of the target type
//                target.getInterfaceNames().add(newElement);
//            }
//            ((MetadataElement) newElement).fixImports(target, elementToCopy);
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
//            return JavaMetamodel.getManager().getFileObject(elementToCopy.getResource());
//        }
//
//        public Element getJavaElement() {
//            return target;
//        }
//
//        public PositionBounds getPosition() {
//            return JavaMetamodel.getManager().getElementPosition(elementToCopy);
//        }
//    }
//    
//    private static class RelaxAccessModifier extends SimpleRefactoringElementImpl {
//        private final String text;
//        private final Feature feature;
//        
//        RelaxAccessModifier(Feature feature) {
//            this.feature = feature;
//            boolean isPrivate = Modifier.isPrivate(feature.getModifiers());
//            this.text = new MessageFormat(NbBundle.getMessage(PushDownRefactoringPlugin.class, "TXT_PushDown_RelaxAccessModifier")).format(
//                new Object[] {
//                    isPrivate ? NbBundle.getMessage(PushDownRefactoringPlugin.class, "LBL_PushDown_private") : 
//                        NbBundle.getMessage(PushDownRefactoringPlugin.class, "LBL_PushDown_package_private"),
//                    NbBundle.getMessage(PushDownRefactoringPlugin.class, "LBL_PushDown_protected")
//                }
//            );
//        }
//        
//        public void performChange() {
//            feature.setModifiers((feature.getModifiers() & ~Modifier.PRIVATE) | Modifier.PROTECTED);
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
//            return JavaMetamodel.getManager().getFileObject(feature.getResource());
//        }
//
//        public Element getJavaElement() {
//            return feature;
//        }
//
//        public PositionBounds getPosition() {
//            return JavaMetamodel.getManager().getElementPosition(feature);
//        }
//    }
//    
//    // --- HELPER METHODS ------------------------------------------------------
//    
//    // checks if the source type or any of its supertypes has any members that could
//    // be pushed down
//    private static boolean hasMembers(JavaClass sourceType, JavaClass[] supertypes) {
//        boolean result = hasMembers(sourceType);
//        
//        for (int i = 0; i < (supertypes.length - 1) && !result; i++) {
//            result = hasMembers(supertypes[i]);
//        }
//        
//        return result;
//    }
//    
//    // checks if the passed type has any members that could be pushed down
//    private static boolean hasMembers(JavaClass type) {
//        for (Iterator it = type.getFeatures().iterator(); it.hasNext();) {
//            Feature feature = (Feature) it.next();
//            if (feature instanceof Field || feature instanceof Method || feature instanceof JavaClass) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    static void collectRelaxModifierElements(AbstractRefactoring refactoring, RefactoringElementsBag refactoringElements, JavaClass jc, List members) {
//        Iterator iter;
//        Set accessedMembers = new HashSet();
//        Set membersToCheck = new HashSet();
//        Set membersToMove = new HashSet();
//        for (iter = members.iterator(); iter.hasNext(); ) {
//            membersToMove.add(iter.next());
//        }
//        for (iter = jc.getFeatures().iterator(); iter.hasNext(); ) {
//            Feature f = (Feature) iter.next();
//            int mods = f.getModifiers();
//            if (Modifier.isPrivate(mods) || (!Modifier.isProtected(mods) && !Modifier.isPublic(mods))) {
//                membersToCheck.add(f);
//            }
//        }
//        for (iter = membersToMove.iterator(); iter.hasNext(); ) {
//            traverseAccessedMembers((Feature) iter.next(), accessedMembers, membersToCheck);
//        }
//        for (iter = jc.getFeatures().iterator(); iter.hasNext(); ) {
//            Feature f = (Feature) iter.next();
//            if (accessedMembers.contains(f) && !membersToMove.contains(f)) {
//                refactoringElements.add(refactoring, new RelaxAccessModifier(f));
//            }
//        }
//    }
//    
//    // computes which member elements from the 'membersToCheck' set are accessed by the passed 'element or some of its children
//    static void traverseAccessedMembers(Element element, Set accessedMembers, Set membersToCheck) {
//        if (element instanceof MethodInvocation || element instanceof VariableAccess || element instanceof TypeReference) {
//            NamedElement nelem = ((ElementReference) element).getElement();
//            if (membersToCheck.contains(nelem)) {
//                accessedMembers.add(nelem);
//            }
//        }
//        
//        for (Iterator it = element.getChildren().iterator(); it.hasNext();) {
//            traverseAccessedMembers((Element) it.next(), accessedMembers, membersToCheck);
//        }
//    }
}
