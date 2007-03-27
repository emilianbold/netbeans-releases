/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.refactoring.java.api;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.util.lookup.Lookups;



/*
 * UseSuperTypeRefactoring.java
 *
 * @author Bharath Ravi Kumar
 *
 * Created on June 20, 2005
 */

/**
 * Replaces the type usages in a project with those
 * of the super type, where applicable
 */
public final class UseSuperTypeRefactoring extends AbstractRefactoring{
    
    private static final String JAVA_LANG_OBJECT = "java.lang.Object";
    private final TreePathHandle javaClassHandle;
    private ElementHandle superType;
    
    //Forced to create an array since the ComboBoxModel (for the panel)
    //takes only a vector or an array.
    private ElementHandle[] candidateSuperTypes;
    
    /**
     * Creates a new instance of UseSuperTypeRefactoring
     * @param Object The class whose occurences must be replaced by
     * that of it's supertype
     */
    public UseSuperTypeRefactoring(TreePathHandle javaClassHandle) {
        super(Lookups.fixed(javaClassHandle));
        this.javaClassHandle = javaClassHandle;
        deriveSuperTypes(javaClassHandle);
    }
    
    /**
     * Returns the type whose occurence must be replaced by that of it's supertype.
     * @return The array of elements to be safely deleted
     */
    public TreePathHandle getTypeElement(){
        return javaClassHandle;
    }
    
    /**
     * Sets the SuperType to be used by this refactoring
     * @param superClass The SuperType to be used by this refactoring
     */
    public void setTargetSuperType(ElementHandle superClass) {
        this.superType = superClass;
    }
    
    /**
     * Returns the SuperType used by this refactoring
     * @return superClass The SuperType used by this refactoring
     */
    public ElementHandle getTargetSuperType() {
        return this.superType;
    }
    
    /**
     * Returns the possible SuperTypes that could be used for the initial Type
     * @return The list of possible SuperTypes for the current type
     */
    public ElementHandle[] getCandidateSuperTypes(){
        return candidateSuperTypes;
    }
    
    //private helper methods follow
    
    private void deriveSuperTypes(final TreePathHandle javaClassHandle) {
        
        
        JavaSource javaSrc = JavaSource.forFileObject(javaClassHandle.
                getFileObject());
        try{
            javaSrc.runUserActionTask(new CancellableTask<CompilationController>() {
                
                public void cancel() {
                }
                
                public void run(CompilationController complController) throws IOException {
                    
                    complController.toPhase(Phase.ELEMENTS_RESOLVED);
                    TypeElement javaClassElement = (TypeElement) 
                            javaClassHandle.resolveElement(complController);
                    Set<TypeElement> intermediateSuperIFs = 
                            getAllSuperTypes(javaClassElement);
                    HashSet<ElementHandle> finalSuperIfSet =
                            new HashSet<ElementHandle>(intermediateSuperIFs.size());
                    for(TypeElement typeElem : intermediateSuperIFs){
                        finalSuperIfSet.add(ElementHandle.create(typeElem));
                    }
                    //Now, add java.lang.Object to candidate super types
//                    TypeMirror typeMirror = complController.getTreeUtilities().
//                            parseType(JAVA_LANG_OBJECT, javaClassElement);
//                    if(TypeKind.DECLARED.equals(typeMirror.getKind())){
//                        Element objectTypeElement = ((DeclaredType) typeMirror).
//                                asElement();
//                         finalSuperIfSet.add(ElementHandle.create(objectTypeElement));
//                    }
                    candidateSuperTypes = finalSuperIfSet.toArray(new ElementHandle[0]);
                }
            }, false);
        }catch(IOException ioex){
            ioex.printStackTrace();
        }
        return ;
    }
    
    //    --private helper methods follow--
    
    /* Checks each Object in the collection that's
     * passed as the second parameter, converts it to a raw type from
     * a ParameterizedType, if necessary, and adds it to the candidateSuperTypesList
     */
    //TODO: Rewrite this for retouche
    private void reduceParamTypes(Collection candidateSuperTypeList, Collection javaClassList) {
        //        Iterator interfacesIterator = javaClassList.iterator();
        //        while(interfacesIterator.hasNext()){
        //            Object superClass = (Object) interfacesIterator.next();
        //            if(superClass instanceof ParameterizedType)
        //                superClass = ((ParameterizedType)superClass).getDefinition();
        //            candidateSuperTypeList.add(superClass);
        //        }
    }
    
    private Set<TypeElement> getAllSuperTypes(TypeElement subTypeElement){
        HashSet<TypeElement> finalSuperIFs = new HashSet<TypeElement>();
        
        //Required to avoid repetitive Depth first search in getAllSuperInterfaces
        HashSet<TypeElement> workingSet = new HashSet<TypeElement>();
        
        //Setup required for getAllSuperInterfaces
        workingSet.add(subTypeElement);
        getAllSuperInterfaces(subTypeElement, workingSet, finalSuperIFs);
        
        if(isDeclaredType(subTypeElement.getSuperclass())){
            DeclaredType tempDeclType = (DeclaredType) subTypeElement.getSuperclass();
            TypeElement tempType = (TypeElement) tempDeclType.asElement();
            
            while (tempType != null) {
                finalSuperIFs.add(tempType);
                workingSet.add(tempType);
                getAllSuperInterfaces(tempType, workingSet,
                        finalSuperIFs);
                
                if(!isDeclaredType(tempType.getSuperclass()))
                    break;
                tempDeclType = (DeclaredType) tempType.getSuperclass();
                tempType = (TypeElement) tempDeclType.asElement();
            }
        }
        return finalSuperIFs;
    }
    
    private void getAllSuperInterfaces(TypeElement subType,
            Collection<TypeElement> uniqueIFs, Collection<TypeElement> finalIFCollection){
        
        Iterator subTypeIFs = subType.getInterfaces().iterator();
        while(subTypeIFs.hasNext()){
            DeclaredType declType = (DeclaredType) subTypeIFs.next();
            TypeElement superIF = (TypeElement) declType.asElement();
            finalIFCollection.add(superIF);
            if(!uniqueIFs.contains(superIF)){
                getAllSuperInterfaces(superIF, uniqueIFs, finalIFCollection);
            }
        }
        return;
    }
    
    private boolean isDeclaredType(TypeMirror type){
        return TypeKind.DECLARED.equals(type.getKind());
    }
    
}