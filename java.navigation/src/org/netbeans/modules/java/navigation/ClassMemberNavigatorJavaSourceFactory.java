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
package org.netbeans.modules.java.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.support.LookupBasedJavaSourceTaskFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Lahoda, Petr Hrebejk
 */
public final class ClassMemberNavigatorJavaSourceFactory extends LookupBasedJavaSourceTaskFactory {
            
    private ClassMemberPanelUI ui;
    private static final CancellableTask<CompilationInfo> EMPTY_TASK = new CancellableTask<CompilationInfo>() {

        public void cancel() {}

        public void run(CompilationInfo parameter) throws Exception {}
    };
    
    static ClassMemberNavigatorJavaSourceFactory getInstance() {
        return Lookup.getDefault().lookup(ClassMemberNavigatorJavaSourceFactory.class);
    }
    
    public ClassMemberNavigatorJavaSourceFactory() {        
        super(Phase.ELEMENTS_RESOLVED, Priority.LOW, "text/x-java", "application/x-class-file");
    }

    public synchronized CancellableTask<CompilationInfo> createTask(FileObject file) {
        // System.out.println("CREATE TASK FOR " + file.getNameExt() );
        if ( ui == null) {
            return EMPTY_TASK;
        }
        else {
            return ui.getTask();
        }
    }

    public List<FileObject> getFileObjects() {
        List<FileObject> result = super.getFileObjects();

        if (result.size() == 1)
            return result;

        // System.out.println("Nothing to show");
        return Collections.emptyList();
    }

    public synchronized void setLookup(Lookup l, ClassMemberPanelUI ui) {
        this.ui = ui;
        super.setLookup(l);
    }

    @Override
    protected void lookupContentChanged() {
          // System.out.println("lookupContentChanged");
          if ( ui != null ) {
            ui.showWaitNode(); // Creating new task (file changed)
          }
    }    
    
}
