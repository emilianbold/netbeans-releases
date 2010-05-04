/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.gsfret.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.napi.gsfret.source.CompilationInfo;
import org.netbeans.napi.gsfret.source.Source.Priority;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.netbeans.napi.gsfret.source.Phase;
import org.netbeans.napi.gsfret.source.SourceTaskFactory;
import org.netbeans.napi.gsfret.source.support.LookupBasedSourceTaskFactory;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 * <p>
 *
 * @author Jan Lahoda, Petr Hrebejk
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.napi.gsfret.source.SourceTaskFactory.class)
public final class ClassMemberNavigatorSourceFactory extends LookupBasedSourceTaskFactory {
            
    private ClassMemberPanelUI ui;
    private static final CancellableTask<CompilationInfo> EMPTY_TASK = new CancellableTask<CompilationInfo>() {

        public void cancel() {}

        public void run(CompilationInfo parameter) throws Exception {}
    };
    
    static ClassMemberNavigatorSourceFactory getInstance() {
        for(SourceTaskFactory t : Lookup.getDefault().lookupAll(SourceTaskFactory.class)) {
            if (t instanceof ClassMemberNavigatorSourceFactory) {
                return (ClassMemberNavigatorSourceFactory) t;
            }
        }
        return null;
    }
    
    public ClassMemberNavigatorSourceFactory() {        
        super(Phase.ELEMENTS_RESOLVED, Priority.LOW);
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
        LanguageRegistry registry = LanguageRegistry.getInstance();
        for( FileObject fileObject : super.getFileObjects() ) {
            if (!registry.isSupported(FileUtil.getMIMEType(fileObject))) {
                continue;
            }
            result.add(fileObject);
        }
        
        if (result.size() == 1)
            return result;

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
