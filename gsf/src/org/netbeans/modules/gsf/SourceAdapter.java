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

package org.netbeans.modules.gsf;

import java.io.IOException;
import org.netbeans.api.gsf.CancellableTask;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.SourceModel;
import org.netbeans.api.retouche.source.CompilationController;
import org.netbeans.api.retouche.source.Phase;
import org.netbeans.api.retouche.source.Source;
import org.netbeans.modules.retouche.source.usages.RepositoryUpdater;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * An implementation of the SourceModel which just adapts the Retouche-copied
 * "Source" class
 * 
 * @author Tor Norbye
 */
public class SourceAdapter implements SourceModel {
    private Source source;
    
    /** Creates a new instance of SourceAdapter */
    public SourceAdapter(Source source) {
        this.source = source;
    }
    
    public void runUserActionTask(final CancellableTask<CompilationInfo> task,
                                  boolean shared) throws IOException {
        source.runUserActionTask(
            new CancellableTask<CompilationController>() {
                public void cancel() {
                    task.cancel();
                }

                public void run(CompilationController info) {
                    try {
                        info.toPhase(Phase.RESOLVED);
                        task.run(info);
                    } catch (Exception ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
        }, shared);
    }

    public FileObject getFileObject() {
        return source.getFileObjects().iterator().next();
    }

    public boolean isScanInProgress() {
        return RepositoryUpdater.getDefault().isScanInProgress();
    }
}
