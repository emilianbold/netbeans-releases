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

package org.netbeans.modules.websvc.editor.hints.fixes;

import java.io.IOException;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.websvc.editor.hints.common.Utilities;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @author Ajit.Bhate@Sun.COM
 */
public class RemoveAnnotation implements Fix {
    private FileObject fileObject;
    private Element element;
    private AnnotationMirror annMirror;
    
    /** Creates a new instance of RemoveAnnotation */
    public RemoveAnnotation(FileObject fileObject, Element element,
            AnnotationMirror annMirror) {
        this.element = element;
        this.fileObject = fileObject;
        this.annMirror = annMirror;
    }
    
    public ChangeInfo implement(){
        CancellableTask<WorkingCopy> task = new CancellableTask<WorkingCopy>(){
            public void cancel() {}
            
            public void run(WorkingCopy workingCopy) throws Exception {
                Utilities.removeAnnotation(workingCopy, element, annMirror);
            }
        };
        
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        
        try{
            javaSource.runModificationTask(task).commit();
        } catch (IOException e){
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
        String annotationLabel = annMirror.getAnnotationType().asElement().getSimpleName().toString();
        return NbBundle.getMessage(RemoveAnnotation.class, "LBL_RemoveAnnotation",annotationLabel);
    }
}
