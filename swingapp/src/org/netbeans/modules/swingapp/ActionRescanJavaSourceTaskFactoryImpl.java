/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
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

    private static class RescanTask implements CancellableTask<CompilationInfo> {
        FileObject file;

        public RescanTask(FileObject file) {
            this.file = file;
        }

        public void cancel() {
        }

        public void run(CompilationInfo info) throws Exception {
            ActionManager am = ActionManager.getActionManager(file);
            if(am != null && AppFrameworkSupport.getClassNameForFile(file) != null) {
                am.lazyRescan(file);
            }
        }
    }
}
