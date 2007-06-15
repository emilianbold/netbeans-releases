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
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemFinder;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @author Tomasz.Slota@Sun.COM
 */
public class CreateTemporalAnnotationHint implements Fix {
        private FileObject fileObject;
    private ElementHandle<Element> elementHandle;
    
    /** Creates a new instance of ImplementSerializable */
    public CreateTemporalAnnotationHint(FileObject fileObject, ElementHandle<Element> elementHandle) {
        this.elementHandle = elementHandle;
        this.fileObject = fileObject;
    }
    
    public ChangeInfo implement(){
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>(){
            public void cancel() {}
            
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                Element elem = elementHandle.resolve(workingCopy);
                
                if (elem != null){    
                    Tree tree = workingCopy.getTrees().getTree(elem);
                    TreeMaker make = workingCopy.getTreeMaker();
                    
                    ModifiersTree modifiersTree = null;
                    
                    if (tree.getKind() == Tree.Kind.VARIABLE){
                        modifiersTree = ((VariableTree)tree).getModifiers();
                    }
                    else if (tree.getKind() == Tree.Kind.METHOD){
                        modifiersTree = ((MethodTree)tree).getModifiers();
                    }
                    else{
                        throw new IllegalStateException();
                    }
                    
                    TypeElement temporalAnnType = workingCopy.getElements().getTypeElement(JPAAnnotations.TEMPORAL);
                    Tree annType = make.QualIdent(temporalAnnType);
                    AnnotationTree temporalAnn = make.Annotation(annType, Collections.singletonList(make.Identifier("javax.persistence.TemporalType.DATE")));
                    
                    List<AnnotationTree> newAnnots = new ArrayList<AnnotationTree>();
                    newAnnots.addAll(modifiersTree.getAnnotations());
                    newAnnots.add(temporalAnn);
                    
                    ModifiersTree newModifiers = make.Modifiers(modifiersTree, newAnnots);
                    workingCopy.rewrite(modifiersTree, newModifiers);
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
    
    public int hashCode(){
        return 1;
    }
    
    public boolean equals(Object o){
        // TODO: implement equals properly
        return super.equals(o);
    }
    
    public String getText(){
        return NbBundle.getMessage(CreatePersistenceUnit.class, "LBL_CreateTemporalAnnotationHint");
    }
}
