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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.jpa.refactoring.rename;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.jpa.refactoring.EntityAnnotationReference;
import org.netbeans.modules.j2ee.jpa.refactoring.EntityAssociationResolver;
import org.netbeans.modules.j2ee.jpa.refactoring.JPARefactoring;
import org.netbeans.modules.j2ee.jpa.refactoring.RefactoringUtil;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScopes;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Handles renaming of properties that are referenced by
 * the mappedBy attribute.
 *
 * @author Erno Mononen
 */
public class EntityRename implements JPARefactoring{
    
    private final RenameRefactoring rename;
    private TreePathHandle treePathHandle;
    
    public EntityRename(RenameRefactoring rename) {
        this.rename = rename;
        try {
            this.treePathHandle = RefactoringUtil.resolveTreePathHandle(rename);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private PersistenceScope getPersistenceScope(){
        if (treePathHandle == null){
            return null;
        }
        Project project = FileOwnerQuery.getOwner(treePathHandle.getFileObject());
        if (project == null){
            return null;
        }
        
        PersistenceScopes scopes = PersistenceScopes.getPersistenceScopes(project);
        
        if (scopes == null){
            return null; // project of this type doesn't provide a list of persistence scopes
        }
        
        if (scopes.getPersistenceScopes().length == 0){
            return null;
        }
        
        return scopes.getPersistenceScopes()[0];
        
    }
    
    private MetadataModel<EntityMappingsMetadata> getEntityMappingsModel(){
        PersistenceScope scope = getPersistenceScope();
        // XXX should retrieve the model for each PU (see the javadoc of the 
        // the scope#getEMM(String) method), but it is currently not supported
        // by the persistence scope implementations.
        return scope != null ? scope.getEntityMappingsModel(null) : null;
    }

    public Problem prepare(RefactoringElementsBag refactoringElementsBag) {
        
        MetadataModel<EntityMappingsMetadata> emModel = getEntityMappingsModel();
        if (emModel == null){
            return null;
        }
        
        EntityAssociationResolver resolver = new EntityAssociationResolver(treePathHandle, emModel);
        try{
            List<EntityAnnotationReference> references = resolver.resolveReferences();
            for (EntityAnnotationReference ref : references){
                EntityRenameElement element = new EntityRenameElement(ref, rename);
                refactoringElementsBag.add(rename, element);
            }
        }catch (IOException ioe){
            Exceptions.printStackTrace(ioe);
        }
        return null;
    }
    
    
    public Problem preCheck() {
        return null;
    }
    
    private static class EntityRenameElement extends SimpleRefactoringElementImplementation{
        
        private final RenameRefactoring rename;
        private final EntityAnnotationReference reference;
        
        public EntityRenameElement(EntityAnnotationReference reference, RenameRefactoring rename) {
            this.reference = reference;
            this.rename = rename;
        }
        
        public String getText(){
            return getDisplayText();
        }
        
        public String getDisplayText() {
            Object[] args = new Object [] {reference.getHandle().getFileObject().getNameExt(), reference.getAttributeValue(), rename.getNewName()};
            return MessageFormat.format(NbBundle.getMessage(EntityRename.class, "TXT_EntityAnnotationRename"), args);
        }
        
        public void performChange() {
            try{
                JavaSource source = JavaSource.forFileObject(reference.getHandle().getFileObject());
                source.runModificationTask(new CancellableTask<WorkingCopy>(){
                    
                    public void cancel() {
                    }
                    
                    public void run(WorkingCopy workingCopy) throws Exception {
                        
                        workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                        Element element = reference.getHandle().resolveElement(workingCopy);
                        
                        for (AnnotationMirror annotation : element.getAnnotationMirrors()){
                            
                            if (!annotation.getAnnotationType().toString().equals(reference.getAnnotation())){
                                continue;
                            }
                            
                            Map<? extends ExecutableElement, ? extends AnnotationValue> values = annotation.getElementValues();

                            for(ExecutableElement each : values.keySet()){
                                if (each.getSimpleName().contentEquals(reference.getAttribute())){
                                    AnnotationValue value = values.get(each);
                                    if (reference.getAttributeValue().equals(value.getValue().toString())){
                                        TreeMaker make = workingCopy.getTreeMaker();
                                        ///XXX: should rewrite only assignments
                                        ExpressionTree argumentValueTree = make.Literal(rename.getNewName());
                                        AssignmentTree assignmentTree = make.Assignment(make.Identifier(reference.getAttribute()), argumentValueTree);
                                        TypeElement typeElement = workingCopy.getElements().getTypeElement(reference.getAnnotation());
                                        Tree newAt = make.Annotation(make.QualIdent(typeElement), Collections.singletonList(assignmentTree));
                                        Tree oldAt = workingCopy.getTrees().getTree(element, annotation, value);
                                        workingCopy.rewrite(oldAt, newAt);
                                    }
                                }
                            }
                        }
                        
                    }
                    
                }).commit();
                
            } catch (IOException ioe){
                Exceptions.printStackTrace(ioe);
            }
        }
        
        
        
        public Lookup getLookup() {
            return Lookups.singleton(reference.getHandle().getFileObject());
        }
        
        public FileObject getParentFile() {
            return reference.getHandle().getFileObject();
        }
        
        public PositionBounds getPosition() {
            
            return null;
        }
        
    }
}
