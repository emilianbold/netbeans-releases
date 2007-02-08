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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.refactoring.java.plugins;

import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.ExtractInterfaceRefactoring;
import org.netbeans.modules.refactoring.java.plugins.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;

public class ExtractInterfaceRefactoringPlugin extends JavaRefactoringPlugin {
    
    private ExtractInterfaceRefactoring refactoring;
    
    ExtractInterfaceRefactoringPlugin(ExtractInterfaceRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    public Problem preCheck() {
        //TODO: implement me
        return null;
    }

    public Problem checkParameters() {
        //TODO: implement me
        return null;
    }

    public Problem fastCheckParameters() {
        //TODO: implement me
        return null;
    }

    public Problem prepare(RefactoringElementsBag refactoringElements) {
        //TODO: implement me
        return null;
    }

}
///** Plugin that implements the core functionality of Extract Interface refactoring.
// *
// * @author Martin Matula
// */
//public class ExtractInterfaceRefactoringPlugin extends JavaRefactoringPlugin {
//    /** Reference to the parent refactoring instance */
//    private final ExtractInterfaceRefactoring refactoring;
//    
//    /** Creates a new instance of ExtractInterfaceRefactoringPlugin
//     * @param refactoring Parent refactoring instance.
//     */
//    ExtractInterfaceRefactoringPlugin(ExtractInterfaceRefactoring refactoring) {
//        this.refactoring = refactoring;
//    }
//    
//    /** Checks pre-conditions of the refactoring.
//     * @return Problems found or <code>null</code>.
//     */
//    public Problem preCheck() {
//        // fire operation start on the registered progress listeners (1 step)
//        fireProgressListenerStart(AbstractRefactoring.PRE_CHECK, 1);
//        try {
//            JavaClass sourceType = refactoring.getSourceType();
//            
//            // check whether the element is valid
//            Problem result = isElementAvail(sourceType);
//            if (result != null) {
//                // fatal error -> don't continue with further checks
//                return result;
//            }
//            if (!CheckUtils.isElementInOpenProject(sourceType)) {
//                return new Problem(true, NbBundle.getMessage(JavaRefactoringPlugin.class, "ERR_ProjectNotOpened"));
//            }
//            
//            // check whether the element is an unresolved class
//            if (sourceType instanceof UnresolvedClass) {
//                // fatal error -> return
//                return new Problem(true, NbBundle.getMessage(JavaRefactoringPlugin.class, "ERR_ElementNotAvailable")); // NOI18N
//            }
//            
//            // increase progress (step 1)
//            fireProgressListenerStep();
//
//            // all checks passed -> return null
//            return null;
//        } finally {
//            // fire operation end on the registered progress listeners
//            fireProgressListenerStop();
//        }
//    }
//    
//    public Problem fastCheckParameters() {
//        Problem result = null;
//        
//        JavaClass sourceType = refactoring.getSourceType();
//        String oldName = sourceType.getSimpleName();
//        String newName = refactoring.getIfcName();
//        
//        if (!org.openide.util.Utilities.isJavaIdentifier(newName)) {
//            result = createProblem(result, true, NbBundle.getMessage(RenameRefactoring.class, "ERR_InvalidIdentifier", newName)); // NOI18N
//            return result;
//        }
//        
//        Resource resource = sourceType.getResource();
//        FileObject primFile = JavaModel.getFileObject(resource);
//        FileObject folder = primFile.getParent();
//        FileObject[] children = folder.getChildren();
//        for (int x = 0; x < children.length; x++) {
//            if (!children[x].isVirtual() && children[x].getName().equals(newName) && "java".equals(children[x].getExt())) { // NOI18N
//                result = createProblem(result, true, NbBundle.getMessage(RenameRefactoring.class, "ERR_ClassClash", newName, resource.getPackageName())); // NOI18N
//                return result;
//            }
//        }
//
//        return null;
//    }
//
//    public Problem checkParameters() {
//        // TODO: check whether the selected members are public and non-static in case of methods, static in other cases
//        // TODO: check whether all members belong to the source type
//        return null;
//    }
//
//    public Problem prepare(RefactoringElementsBag refactoringElements) {
//        NamedElement[] members = refactoring.getMembers();
//
//        List typeParams = findUsedGenericTypes(members, refactoring.getSourceType());
//        
//        CreateIfcElement createIfcElement = new CreateIfcElement(refactoring.getSourceType().getResource(), refactoring.getIfcName(), typeParams);
//        refactoringElements.add(refactoring, createIfcElement);
//        refactoringElements.add(refactoring, new AddImplementsElement(refactoring.getSourceType(), refactoring.getIfcName(), typeParams));
//        for (int i = 0; i < members.length; i++) {
//            refactoringElements.add(refactoring, new GenerateMemberElement(members[i], createIfcElement));
//        }
//
//        return null;
//    }
//    
//    /**
//     * Returns list of TypeParameters from javaClass that are used by the passed members elements.
//     */
//    static List findUsedGenericTypes(NamedElement[] members, JavaClass javaClass) {
//        List typePars = javaClass.getTypeParameters();
//        if (typePars.isEmpty())
//            return Collections.EMPTY_LIST;
//        Map nameToType = new HashMap();
//        for (Iterator iter = typePars.iterator(); iter.hasNext(); ) {
//            TypeParameter tp = (TypeParameter) iter.next();
//            nameToType.put(tp.getName(), tp);
//        }
//        Set set = new HashSet();
//        Set redefined = new HashSet();
//        for (int x = 0; x < members.length; x++) {
//            findUsedGenericTypes(members[x], set, redefined, nameToType);
//        }
//        List result = new ArrayList(set.size());
//        for (Iterator iter = typePars.iterator(); iter.hasNext(); ) {
//            TypeParameter tp = (TypeParameter) iter.next();
//            if (set.contains(tp.getName()))
//                result.add(tp);
//        }
//        return result;
//    }
//    
//    private static void findUsedGenericTypes(Element elem, Set result, Set hidden, Map nameToType) {
//        if (elem instanceof GenericElement) {
//            GenericElement gelem = (GenericElement) elem;
//            List tpList = gelem.getTypeParameters();
//            if (!tpList.isEmpty()) {
//                boolean extendedHiddenSetCreated = false;
//                for (Iterator iter = tpList.iterator(); iter.hasNext(); ) {
//                    TypeParameter tp = (TypeParameter) iter.next();
//                    String name = tp.getName();
//                    if (nameToType.get(name) != null && !hidden.contains(name)) {
//                        if (!extendedHiddenSetCreated) {
//                            hidden = new HashSet(hidden);
//                            extendedHiddenSetCreated = true;
//                        }
//                        hidden.add(name);
//                    }
//                } // for
//            } // if
//        } // if
//        if (elem instanceof MultipartId) {
//            MultipartId id = (MultipartId) elem;
//            if (id.getElement() instanceof TypeParameter) {
//                String tpName = id.getName();
//                if (nameToType.get(tpName) != null && !hidden.contains(tpName))
//                    result.add(tpName);
//            }
//        }
//        for (Iterator iter = elem.getChildren().iterator(); iter.hasNext(); ) {
//            findUsedGenericTypes((Element) iter.next(), result, hidden, nameToType);
//        }
//    }
//    
//    // --- REFACTORING ELEMENTS ------------------------------------------------
//    
//    private static class CreateIfcElement extends SimpleRefactoringElementImpl {
//        private final String ifcName;
//        private final Resource source;
//        private final String text;
//        private final List typeParams;
//        
//        private JavaClass newIfc = null;
//        
//        CreateIfcElement(Resource source, String ifcName, List typeParams) {
//            this.source = source;
//            this.ifcName = ifcName;
//            this.typeParams = typeParams;
//            this.text = NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "TXT_ExtractInterface_CreateIfc", ifcName); // NOI18N
//        }
//
//        public void performChange() {
//            ExternalChange ec = new ExternalChange() {
//                private FileSystem fs;
//                private String newIfcName;
//                private String folderName;
//                
//                public void performExternalChange() {
//                    try {
//                        FileObject tempFO = Repository.getDefault().getDefaultFileSystem().findResource("Templates/Classes/Interface.java"); // NOI18N
//                        
//                        FileObject folderFO;
//                        if (fs == null) {
//                            FileObject sourceFO = JavaModel.getFileObject(source);
//                            folderFO = sourceFO.getParent();
//                            folderName = folderFO.getPath();
//                            fs = folderFO.getFileSystem();
//                        } else {
//                            folderFO = fs.findResource(folderName);
//                        }
//                            
//                        DataFolder folder = (DataFolder) DataObject.find(folderFO);
//                        DataObject template = DataObject.find(tempFO);
//                        DataObject newIfcDO = template.createFromTemplate(folder, ifcName);
//                        UndoWatcher.watch(newIfcDO);
//                        FileObject newIfcFO = newIfcDO.getPrimaryFile();
//                        newIfcName = newIfcFO.getPath();
//                        newIfc = (JavaClass) JavaMetamodel.getManager().getResource(newIfcFO).getClassifiers().iterator().next();
//                    } catch (DataObjectNotFoundException e) {
//                        ErrorManager.getDefault().notify(e);
//                    } catch (IOException e) {
//                        ErrorManager.getDefault().notify(e);
//                    }
//                }
//                
//                public void undoExternalChange() {
//                    try {
//                        FileObject newIfcFO = fs.findResource(newIfcName);
//                        DataObject newIfcDO = DataObject.find(newIfcFO);
//                        newIfcDO.delete();
//                    } catch (DataObjectNotFoundException e) {
//                        ErrorManager.getDefault().notify(e);
//                    } catch (IOException e) {
//                        ErrorManager.getDefault().notify(e);
//                    }
//                }
//            };
//            ec.performExternalChange();
//            JavaMetamodel.getManager().registerUndoElement(ec);
//            
//            List ifcTypeParams = newIfc.getTypeParameters();
//            for (Iterator iter = typeParams.iterator(); iter.hasNext(); ) {
//                ifcTypeParams.add(((TypeParameter) iter.next()).duplicate());
//            }
//        }
//        
//        JavaClass getNewInterface() {
//            return newIfc;
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
//            return null;
//        }
//
//        public Element getJavaElement() {
//            return (Element) source.refImmediateComposite();
//        }
//
//        public PositionBounds getPosition() {
//            return null;
//        }
//    }
//    
//    private static class AddImplementsElement extends SimpleRefactoringElementImpl {
//        private final JavaClass sourceType;
//        private final String ifcName;
//        private final List typeParams;
//        private final String text;
//        
//        AddImplementsElement(JavaClass sourceType, String ifcName, List typeParams) {
//            this.sourceType = sourceType;
//            this.ifcName = ifcName;
//            this.typeParams = typeParams;
//            this.text = NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, "TXT_ExtractInterface_AddImplements", ifcName); // NOI18N
//        }
//        
//        public void performChange() {
//            JavaModelPackage extent = (JavaModelPackage) sourceType.refImmediatePackage();
//            MultipartIdClass idProxy = extent.getMultipartId();
//            MultipartId mpi = idProxy.createMultipartId(ifcName, null, null);
//            if (typeParams != null && typeParams.size() > 0) {
//                List typeArgs = mpi.getTypeArguments();
//                for (Iterator iter = typeParams.iterator(); iter.hasNext(); ) {
//                    typeArgs.add(idProxy.createMultipartId(((TypeParameter) iter.next()).getName(), null, null));
//                }
//            }
//            sourceType.getInterfaceNames().add(mpi);
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
//            return JavaMetamodel.getManager().getFileObject(sourceType.getResource());
//        }
//
//        public Element getJavaElement() {
//            return sourceType;
//        }
//
//        public PositionBounds getPosition() {
//            return JavaMetamodel.getManager().getElementPosition(sourceType);
//        }
//    }
//    
//    private static class GenerateMemberElement extends SimpleRefactoringElementImpl {
//        private final NamedElement elementToGenerate;
//        private final CreateIfcElement cie;
//        private final String text;
//        
//        GenerateMemberElement(NamedElement elementToGenerate, CreateIfcElement element) {
//            this.elementToGenerate = elementToGenerate;
//            this.cie = element;
//            this.text = NbBundle.getMessage(ExtractInterfaceRefactoringPlugin.class, getBundleKey(elementToGenerate), UIUtilities.getDisplayText(elementToGenerate));
//        }
//        
//        private static String getBundleKey(NamedElement element) {
//            if (element instanceof Method) {
//                Object comp = element.refImmediateComposite();
//                if (comp instanceof JavaClass && ((JavaClass) comp).isInterface()) {
//                    return "TXT_ExtractInterface_MoveMethod"; // NOI18N
//                } else {
//                    return "TXT_ExtractInterface_Method"; // NOI18N
//                }
//            } else if (element instanceof Field) {
//                return "TXT_ExtractInterface_Field"; // NOI18N
//            } else if (element instanceof JavaClass) {
//                return "TXT_ExtractInterface_Class"; // NOI18N
//            } else if (element instanceof MultipartId) {
//                return "TXT_ExtractInterface_Implements"; // NOI18N
//            }
//            throw new IllegalArgumentException("Wrong type of element: " + element.getClass().getName()); // NOI18N
//        }
//
//        public void performChange() {
//            if (cie.getNewInterface() == null) return;
//            
//            if (elementToGenerate instanceof Method) {
//                JavaModelPackage extent = (JavaModelPackage) cie.getNewInterface().refImmediatePackage();
//                Method oldMethod = (Method) elementToGenerate;
//                Method method = extent.getMethod().createMethod(
//                        oldMethod.getName(),
//                        Utilities.duplicateList(oldMethod.getAnnotations(), extent),
//                        0,
//                        oldMethod.getJavadocText(),
//                        null,
//                        null,
//                        null,
//                        Utilities.duplicateList(oldMethod.getTypeParameters(), extent),
//                        Utilities.duplicateList(oldMethod.getParameters(), extent),
//                        Utilities.duplicateList(oldMethod.getExceptionNames(), extent),
//                        (TypeReference) oldMethod.getTypeName().duplicate(),
//                        oldMethod.getDimCount()
//                        );
//                cie.getNewInterface().getFeatures().add(method);
//                ((MetadataElement) method).fixImports(cie.getNewInterface(), elementToGenerate);
//                
//                Object comp = oldMethod.refImmediateComposite();
//                if (comp instanceof JavaClass) {
//                    JavaClass jc = (JavaClass)comp;
//                    if (jc.isInterface()) {
//                        jc.replaceChild(oldMethod, null);
//                    }
//                }
//            } else {
//                // remove element from the original type
//                Element parent = (Element) elementToGenerate.refImmediateComposite();
//                parent.replaceChild(elementToGenerate, null);
//                // add the element to the interface
//                if (elementToGenerate instanceof MultipartId) {
//                    cie.getNewInterface().getInterfaceNames().add(JavaModelUtil.resolveImportsForClass(cie.getNewInterface(), (JavaClass) ((MultipartId) elementToGenerate).getElement()));
//                    elementToGenerate.refDelete();
//                } else {
//                    Feature feature = (Feature) elementToGenerate;
//                    feature.setModifiers(feature.getModifiers() & ~(Modifier.FINAL | Modifier.STATIC | Modifier.PUBLIC));
//                    cie.getNewInterface().getContents().add(elementToGenerate);
//                    ((MetadataElement) elementToGenerate).fixImports(cie.getNewInterface(), elementToGenerate);
//                }
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
//            return JavaMetamodel.getManager().getFileObject(elementToGenerate.getResource());
//        }
//
//        public Element getJavaElement() {
//            return (Element) JavaModelUtil.getDeclaringFeature(elementToGenerate);
//        }
//
//        public PositionBounds getPosition() {
//            return JavaMetamodel.getManager().getElementPosition(elementToGenerate);
//        }
//    }
//}
