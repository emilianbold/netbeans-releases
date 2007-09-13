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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.swingapp;

import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.support.EditorAwareJavaSourceTaskFactory;
import org.openide.filesystems.FileObject;

/**
 *
 * @author joshy
 */
public class ActionRescanJavaSourceTaskFactoryImpl extends EditorAwareJavaSourceTaskFactory {

    public ActionRescanJavaSourceTaskFactoryImpl() {
        super(Phase.RESOLVED, Priority.LOW); //getPhase(),getPriority());
    }

    public CancellableTask<CompilationInfo> createTask(FileObject file) {
        return new RescanTask(file);
    }

    public Priority getPriority() {
        return Priority.LOW;
    }

    public Phase getPhase() {
        return Phase.RESOLVED;
    }
}


class RescanTask implements CancellableTask<CompilationInfo> {
    FileObject file;
    public RescanTask(FileObject file) {
        this.file = file;
    }
    public void cancel() {
//        System.out.println("footask cancel called");
    }
    public void run(CompilationInfo info) throws Exception {
//        System.out.println("footask run called");
//        System.out.println("file = " + file.getName() + " " + file.getPath());
        ActionManager am = ActionManager.getActionManager(file);
        if(am != null) {
            //System.out.println("got an action manager");
            am.lazyRescan(file);
        }
    }
}
