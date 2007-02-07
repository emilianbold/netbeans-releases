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

package org.netbeans.modules.visualweb.insync.java;

import java.io.IOException;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.filesystems.FileObject;

/**
 *
 * @author jdeva
 */
public class WriteTaskWrapper implements CancellableTask<WorkingCopy> {
    interface Write {
        public Object run(WorkingCopy cinfo);
    }
    Write task;
    Object result;
    WriteTaskWrapper(Write task) {
        this.task = task;
    }
    public void cancel() {}
    
    public void run(WorkingCopy copy) throws Exception {
        try {
            copy.toPhase(Phase.RESOLVED);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        result = (Object)task.run(copy);
    }
    
    public static Object execute(Write task,  FileObject fObj) {
        WriteTaskWrapper taskWrapper = new WriteTaskWrapper(task);
        JavaSource js = JavaSource.forFileObject(fObj);
        ModificationResult result = null;
        try {
            result = js.runModificationTask(taskWrapper);
            if(result != null) {
                result.commit();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return taskWrapper.result;
    }
}

