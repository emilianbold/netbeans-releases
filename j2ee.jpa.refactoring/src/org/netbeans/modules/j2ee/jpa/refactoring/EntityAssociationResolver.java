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


package org.netbeans.modules.j2ee.jpa.refactoring;


import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.OneToMany;
import org.openide.util.Parameters;

/**
 * This class resolves non-type safe associations between entities. Entities 
 * might have non-type safe associations via annotations that 
 * can have attributes such as "mappedBy" that specifies a field in another
 * entity.
 *
 * @author Erno Mononen
 */
public class EntityAssociationResolver {
    
    static final String ONE_TO_ONE = "javax.persistence.OneToOne"; //NO18N
    static final String ONE_TO_MANY = "javax.persistence.OneToMany"; //NO18N
    static final String MANY_TO_ONE = "javax.persistence.ManyToOne"; //NO18N
    static final String MANY_TO_MANY = "javax.persistence.ManyToMany"; //NO18N
    
    // supported annotations
    private static final List<String> ANNOTATIONS = Arrays.asList(
            new String[]{ONE_TO_ONE, ONE_TO_MANY, MANY_TO_ONE, MANY_TO_MANY});
    
    
    static final String MAPPED_BY = "mappedBy"; //NO18N
    static final String TARGET_ENTITY = "targetEntity"; //NO18N
    
    private final MetadataModel<EntityMappingsMetadata> entityMappingsModel;
    /**
     * The property being refactored.
     */ 
    private final TreePathHandle refactoringSource;
    
    /**
     * Constructs a new EntityAssociationResolver. 
     * 
     * @param refactoringSource the property being refactored.
     * @param entityMappingsModel 
     * 
     */ 
    public EntityAssociationResolver(TreePathHandle refactoringSource, MetadataModel<EntityMappingsMetadata> entityMappingsModel) {
        Parameters.notNull("entityMappingsModel", entityMappingsModel); //NO18N
        Parameters.notNull("refactoringSource", refactoringSource); //NO18N
        this.entityMappingsModel = entityMappingsModel;
        this.refactoringSource = refactoringSource;
    }
    
    
    /**
     * Resolves the references to the property being refactored.
     * 
     * @return the references or an empty list if there were none.
     */ 
    public List<EntityAnnotationReference> resolveReferences() throws IOException{
        
        final List<EntityAnnotationReference> result = new ArrayList<EntityAnnotationReference>();
        final List<Reference> references = getTarget();
        
        entityMappingsModel.runReadAction(new MetadataModelAction<EntityMappingsMetadata, Void>(){
            
            public Void run(EntityMappingsMetadata metadata) throws Exception {
                
                for (Reference reference : references){
                    Entity entity = getByClass(metadata.getRoot().getEntity(), reference.getClassName());
                    if (entity == null){
                        continue;
                    }
                    result.addAll(getOneToMany(entity, reference));
                }
                return null;
            }
            
        });
        return result;
    }
    
    private List<EntityAnnotationReference> getOneToMany(Entity entity, Reference reference) throws IOException{
        List<EntityAnnotationReference> result = new ArrayList<EntityAnnotationReference>();
        for (OneToMany oneToMany : entity.getAttributes().getOneToMany()){
            if (oneToMany.getName().equals(reference.getPropertyName())){
                TreePathHandle handle = RefactoringUtil.getTreePathHandle(reference.getPropertyName(), reference.getClassName(), refactoringSource.getFileObject());
                result.add(new EntityAnnotationReference(reference.getClassName(),
                        ONE_TO_MANY, MAPPED_BY, reference.getSourceProperty(), handle));
            }
        }
        
        return result;
    }
    
    private Entity getByClass(Entity[] entities, String clazz){
        for (Entity entity : entities){
            if (entity.getClass2().equals(clazz)){
                return entity;
            }
        }
        return null;
    }
    
    List<Reference> getTarget() throws IOException{
        
        final List<Reference> result = new ArrayList<Reference>();
        
        JavaSource source = JavaSource.forFileObject(refactoringSource.getFileObject());
        
        source.runUserActionTask(new CancellableTask<CompilationController>(){
            
            public void cancel() {
            }
            
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                Element refactoringTargetProperty = refactoringSource.resolveElement(info);
                
                String sourceClass = refactoringTargetProperty.getEnclosingElement().asType().toString();
                String propertyName = refactoringTargetProperty.getSimpleName().toString();
                String targetClass = refactoringTargetProperty.asType().toString();
                
                TypeElement te = info.getElements().getTypeElement(targetClass);
                for (Element element : te.getEnclosedElements()){
                    if (element.getKind().equals(ElementKind.FIELD)){
                        Tree propertyTree = info.getTrees().getTree(element);
                        if (Tree.Kind.VARIABLE == propertyTree.getKind()){
                            VariableTree vtt = (VariableTree) propertyTree;
                            // handle generic collections
                            if (Tree.Kind.PARAMETERIZED_TYPE == vtt.getType().getKind()){
                                ParameterizedTypeTree ptt = (ParameterizedTypeTree) vtt.getType();
                                for (Tree typeArg : ptt.getTypeArguments()){
                                    IdentifierTree it = (IdentifierTree) typeArg;
                                    TypeMirror type = info.getTreeUtilities().parseType(it.getName().toString(), te);
                                    if (sourceClass.equals(type.toString())){
                                        result.add(new Reference(element.getSimpleName().toString(),
                                                propertyName,
                                                targetClass));
                                    }
                                }
                            } 

                        }
                    } else if  (element.getKind().equals(ElementKind.METHOD)){
                        //TODO: implement property access
                    }
                }
            }
        }, false);
        
        return result;
    }
    
    static class Reference {
        
        /**
         * The FQN of the class to which the property being refactored points to.
         */ 
        private final String className;
        /**
         * The name of the property in the target class that possibly has a reference
         * to the property being refactored.
         */ 
        private final String propertyName;
        /**
         * The name of the property that is being refactored.
         */ 
        private final String sourceProperty;
        
        public Reference(String propertyName, String sourceProperty, String clazz){
            this.propertyName = propertyName;
            this.sourceProperty = sourceProperty;
            this.className = clazz;
        }
        
        /**
         * @see #propertyName
         */
        public String getPropertyName(){
            return propertyName;
        }
        
        /**
         * @see #className
         */
        public String getClassName(){
            return className;
        }
        
        /**
         * @see #sourceProperty
         */
        public String getSourceProperty() {
            return sourceProperty;
        }
        
        
    }
}

