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

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation.EntityMappingsMetadataModelFactory;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author Erno Mononen
 */
public class EntityAssociationResolverTest extends SourceTestSupport {
    
    private static final String PKG  = "entities.";
    private static final String CUSTOMER = PKG + "Customer";
    private static final String ORDER = PKG +  "Order";
    private static final String DEPARTMENT = PKG + "Department";
    private static final String EMPLOYEE = PKG + "Employee";
    private static final String USER = PKG + "User";
    private static final String GROUP = PKG + "Group";
    
    public EntityAssociationResolverTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    private String getPath(){
        return getDataDir().getAbsoluteFile().toString();
    }
    
    private FileObject getJavaFile(String name){
        return FileUtil.toFileObject(new File(getPath() +"/" + name.replace('.', '/') + ".java"));
    }
    
    protected FileObject[] getClassPathRoots(){
        return new FileObject[]{FileUtil.toFileObject(new File(getPath()))};
    }
    
    private TreePathHandle getTreePathHandle(final String fieldName, String className) throws IOException{
        return RefactoringUtil.getTreePathHandle(fieldName, className, getJavaFile(className));
    }
    
    private MetadataModel<EntityMappingsMetadata> createModel() throws IOException, InterruptedException{
        FileObject src = FileUtil.toFileObject(new File(getPath()));
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(src, src).await();
        return  EntityMappingsMetadataModelFactory.createMetadataModel(
                ClassPath.getClassPath(src, ClassPath.BOOT),
                ClassPath.getClassPath(src, ClassPath.COMPILE),
                ClassPath.getClassPath(src, ClassPath.SOURCE));
    }
    
    public void testGetTarget() throws Exception {
        EntityAssociationResolver resolver = new EntityAssociationResolver(getTreePathHandle("customer", ORDER), createModel());
        List<EntityAssociationResolver.Reference> orderRefs = resolver.getReferringProperties();
        assertEquals(2, orderRefs.size());
        
        EntityAssociationResolver.Reference fieldRef = orderRefs.get(0);
        assertEquals(CUSTOMER, fieldRef.getClassName());
        assertEquals("orders", fieldRef.getPropertyName());
        assertEquals("customer", fieldRef.getSourceProperty());
        
        EntityAssociationResolver.Reference propertyRef = orderRefs.get(1);
        assertEquals(CUSTOMER, propertyRef.getClassName());
        assertEquals("getOrders", propertyRef.getPropertyName());
        assertEquals("customer", propertyRef.getSourceProperty());
        
        
    }
    
    public void testResolveReferences() throws Exception {
        EntityAssociationResolver resolver = new EntityAssociationResolver(getTreePathHandle("customer", ORDER), createModel());
        List<EntityAnnotationReference> result = resolver.resolveReferences();
        assertEquals(1, result.size());
        EntityAnnotationReference reference = result.get(0);
        assertEquals(EntityAssociationResolver.ONE_TO_MANY, reference.getAnnotation());
        assertEquals("entities.Customer", reference.getEntity());
        assertEquals(EntityAssociationResolver.MAPPED_BY, reference.getAttribute());
        assertEquals("customer", reference.getAttributeValue());
        
    }
    
    public void testGetTreePathHandle() throws Exception{
        final TreePathHandle handle  = RefactoringUtil.getTreePathHandle("orders", CUSTOMER, getJavaFile(CUSTOMER));
        JavaSource source = JavaSource.forFileObject(handle.getFileObject());
        source.runUserActionTask(new CancellableTask<CompilationController>(){
            
            public void cancel() {
            }
            
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                Element element = handle.resolveElement(parameter);
                assertEquals("orders", element.getSimpleName().toString());
                for (AnnotationMirror annotation : element.getAnnotationMirrors()){
                    assertEquals(EntityAssociationResolver.ONE_TO_MANY, annotation.getAnnotationType().toString());
                }
            }
        }, true);
        
    }
    
}
