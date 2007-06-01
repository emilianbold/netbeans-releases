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

package org.netbeans.modules.j2ee.jpa.verification.fixes;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.logging.Level;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.jpa.model.AccessType;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemFinder;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.ManyToMany;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.OneToMany;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.OneToOne;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public abstract class AbstractCreateRelationshipHint implements Fix {
    private FileObject fileObject;
    private ElementHandle<TypeElement> classHandle;
    private AccessType accessType;
    private String targetEntityClassName;
    private String localAttrName;
    
    private String annotationClass;
    private String complimentaryAnnotationClassName;
    private String relationName;
    
    public AbstractCreateRelationshipHint(FileObject fileObject,
            ElementHandle<TypeElement> classHandle,
            AccessType accessType,
            String localAttrName,
            String targetEntityClassName,
            String annotationClass,
            String complimentaryAnnotationClassName) {
        
        this.classHandle = classHandle;
        this.fileObject = fileObject;
        this.accessType = accessType;
        this.targetEntityClassName = targetEntityClassName;
        this.annotationClass = annotationClass;
        this.complimentaryAnnotationClassName = complimentaryAnnotationClassName;
        this.localAttrName = localAttrName;
        
        int dotPos = annotationClass.lastIndexOf('.');
        relationName = dotPos > -1 ? annotationClass.substring(dotPos+1) : annotationClass;
    }
    
    public ChangeInfo implement(){
        String mappedBy = getExistingFieldInRelation();
        
        if (mappedBy == null){
           // display dialog 
        }
        
        if (mappedBy != null){
            modifyFiles(mappedBy);
        }
        
        return null;
    }
    
    private String getExistingFieldInRelation(){
        String mappedBy = null;
        try {
            MetadataModel<EntityMappingsMetadata> emModel = ModelUtils.getModel(fileObject);
            mappedBy = emModel.runReadAction(new MetadataModelAction<EntityMappingsMetadata, String>() {
                
                public String run(EntityMappingsMetadata metadata) {
                    Entity remoteEntity = ModelUtils.getEntity(metadata, targetEntityClassName);
                    assert remoteEntity != null;
                    
                    if (complimentaryAnnotationClassName.equals(JPAAnnotations.ONE_TO_ONE)){
                        return getMappedByFromOneToOne(remoteEntity);
                    }
                    
                    if (complimentaryAnnotationClassName.equals(JPAAnnotations.MANY_TO_MANY)){
                        return getMappedByFromManyToMany(remoteEntity);
                    }
                    
                    if (complimentaryAnnotationClassName.equals(JPAAnnotations.ONE_TO_MANY)){
                        return getMappedByFromOneToMany(remoteEntity);
                    }
                    
                    return null;
                }
            });
        } catch (MetadataModelException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return mappedBy;
    }
    
    private String getMappedByFromOneToOne(Entity remoteEntity){
        for (OneToOne one2one : remoteEntity.getAttributes().getOneToOne()){
            if (classHandle.getQualifiedName().equals(one2one.getTargetEntity())
                    && localAttrName.equals(one2one.getMappedBy())){
                return one2one.getName();
            }
        }
        
        return null;
    }
    
    private String getMappedByFromManyToMany(Entity remoteEntity){
        for (ManyToMany many2many : remoteEntity.getAttributes().getManyToMany()){
            if (classHandle.getQualifiedName().equals(many2many.getTargetEntity())
                    && localAttrName.equals(many2many.getMappedBy())){
                return many2many.getName();
            }
        }
        
        return null;
    }
    
    private String getMappedByFromOneToMany(Entity remoteEntity){
        for (OneToMany oneToMany : remoteEntity.getAttributes().getOneToMany()){
            if (classHandle.getQualifiedName().equals(oneToMany.getTargetEntity())
                    && localAttrName.equals(oneToMany .getMappedBy())){
                return oneToMany .getName();
            }
        }
        
        return null;
    }
    
    private void modifyFiles(String mappedBy){
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>(){
            public void cancel() {}
            
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                
                TypeElement localClass = classHandle.resolve(workingCopy);
                
                GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy, localClass);
                
                AnnotationTree ann = genUtils.createAnnotation(annotationClass);
                
                if (accessType == AccessType.FIELD){
                    VariableElement field = ModelUtils.getField(localClass, localAttrName);
                    VariableTree fieldTree = (VariableTree) workingCopy.getTrees().getTree(field);
                    VariableTree modifiedTree = genUtils.addAnnotation(fieldTree, ann);
                    workingCopy.rewrite(fieldTree, modifiedTree);
                } else { // accessType == AccessType.PROPERTY
                    ExecutableElement accesor = ModelUtils.getAccesor(localClass, localAttrName);
                    MethodTree fieldTree = (MethodTree) workingCopy.getTrees().getTree(accesor);
                    MethodTree modifiedTree = genUtils.addAnnotation(fieldTree, ann);
                    workingCopy.rewrite(fieldTree, modifiedTree);
                }
                
            }
        };
        
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        
        try{
            javaSource.runModificationTask(task).commit();
        } catch (IOException e){
            JPAProblemFinder.LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    public String getText(){
        return NbBundle.getMessage(AbstractCreateRelationshipHint.class,
                "LBL_CreateRelationHint", relationName);
    }
    
}