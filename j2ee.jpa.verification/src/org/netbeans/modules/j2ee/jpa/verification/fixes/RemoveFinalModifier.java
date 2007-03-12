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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ModifiersTree;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemFinder;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @see org.netbeans.modules.j2ee.jpa.verification.rules.entity.SerializableClass
 * @author Tomasz.Slota@Sun.COM
 */
public class RemoveFinalModifier implements Fix {
        private FileObject fileObject;
    private ElementHandle<TypeElement> classHandle;
    
    /** Creates a new instance of ImplementSerializable */
    public RemoveFinalModifier(FileObject fileObject, ElementHandle<TypeElement> classHandle) {
        this.classHandle = classHandle;
        this.fileObject = fileObject;
    }
    
    public ChangeInfo implement(){
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>(){
            public void cancel() {}
            
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement clazz = classHandle.resolve(workingCopy);
                
                if (clazz != null){    
                    ClassTree clazzTree = workingCopy.getTrees().getTree(clazz);
                    TreeMaker make = workingCopy.getTreeMaker();
                    
                    Set<Modifier> flags = new HashSet<Modifier>(clazzTree.getModifiers().getFlags());
                    flags.remove(Modifier.FINAL);
                    ModifiersTree newModifiers = make.Modifiers(flags, clazzTree.getModifiers().getAnnotations());
                    workingCopy.rewrite(clazzTree.getModifiers(), newModifiers);
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
        return NbBundle.getMessage(CreatePersistenceUnit.class, "LBL_RemoveFinalModifier");
    }
}
