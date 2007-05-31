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
package org.netbeans.modules.j2ee.jpa.verification.fixes;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.logging.Level;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemFinder;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
abstract class AbstractCreateAnnotationHint implements Fix {
    private FileObject fileObject;
    private ElementHandle<TypeElement> classHandle;
    private ElementHandle<Element> elemHandle;
    private String annotationClass;
    
    public AbstractCreateAnnotationHint(FileObject fileObject,
            ElementHandle<TypeElement> classHandle,
            ElementHandle<Element> elemHandle,
            String annotationClass) {
        this.classHandle = classHandle;
        this.fileObject = fileObject;
        this.annotationClass = annotationClass;
        this.elemHandle = elemHandle;
    }
    
    public ChangeInfo implement(){
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>(){
            public void cancel() {}
            
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement clazz = classHandle.resolve(workingCopy);
                
                if (clazz != null){
                    GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy, clazz);
                    
                    AnnotationTree annTree = genUtils.createAnnotation(annotationClass);
                    
                    Tree elemTree = workingCopy.getTrees().getTree(elemHandle.resolve(workingCopy));
                    Tree newElemTree = null;
                    
                    switch (elemTree.getKind()){
                    case CLASS:
                        newElemTree = genUtils.addAnnotation((ClassTree)elemTree, annTree);
                        break;
                    case METHOD:
                        newElemTree = genUtils.addAnnotation((MethodTree)elemTree, annTree);
                        break;
                    case VARIABLE:
                        newElemTree = genUtils.addAnnotation((VariableTree)elemTree, annTree);
                        break;
                    }
                    
                    if (newElemTree != null){
                        workingCopy.rewrite(elemTree, newElemTree);
                    } else{
                        throw new IllegalStateException("Unsupported element type");
                    }
                }
            }
        };
        
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        
        try{
            javaSource.runModificationTask(task).commit();
        } catch (IOException e){
            JPAProblemFinder.LOG.log(Level.SEVERE, e.getMessage(), e);
        }
        
        return null;
    }
}

