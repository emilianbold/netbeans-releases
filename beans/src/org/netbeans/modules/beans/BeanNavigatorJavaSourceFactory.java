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
package org.netbeans.modules.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.api.java.source.support.LookupBasedJavaSourceTaskFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Lahoda, Petr Hrebejk
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.api.java.source.JavaSourceTaskFactory.class)
public final class BeanNavigatorJavaSourceFactory extends LookupBasedJavaSourceTaskFactory {
            
    private BeanPanelUI ui;
    private static final CancellableTask<CompilationInfo> EMPTY_TASK = new CancellableTask<CompilationInfo>() {

        public void cancel() {}

        public void run(CompilationInfo parameter) throws Exception {}
    };
    
    static BeanNavigatorJavaSourceFactory getInstance() {
        for (JavaSourceTaskFactory f : Lookup.getDefault().lookupAll(JavaSourceTaskFactory.class)) {
            if (f instanceof BeanNavigatorJavaSourceFactory) {
                return (BeanNavigatorJavaSourceFactory) f;
            }
        }
        throw new IllegalStateException();
    }
    
    public BeanNavigatorJavaSourceFactory() {        
        super(Phase.ELEMENTS_RESOLVED, Priority.NORMAL);
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
        List<FileObject> result = new ArrayList<FileObject>();

        // Filter uninteresting files from the lookup
        for( FileObject fileObject : super.getFileObjects() ) {
            if (!"text/x-java".equals(FileUtil.getMIMEType(fileObject)) && !"java".equals(fileObject.getExt())) {  //NOI18N
                continue;
            }
            result.add(fileObject);
        }
        
        if (result.size() == 1)
            return result;

        return Collections.emptyList();
    }

    public synchronized void setLookup(Lookup l, BeanPanelUI ui) {
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
