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

package org.netbeans.modules.apisupport.project.metainf;

import java.io.IOException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.Util;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;

/**
 *  FileChangeListener for src/META-INF/services
 */
final class ServicesFileListener implements FileChangeListener {
    private static ServicesFileListener instance ;
    
    private ServicesFileListener() {
        
    }
    public void fileFolderCreated(FileEvent fe) {
        FileObject fo = fe.getFile();
        if (fo.getName().equals("services")) { // NOI18N
            if (fo.getParent().getName().equals("META-INF")) { // NOI18N
                fo.removeFileChangeListener(this);
                fo.addFileChangeListener(this);
            }     
        } else if (fo.getName().equals("META-INF")) { // NOI18N
            // check if the folder is really /src/META-INF
            Project prj = FileOwnerQuery.getOwner(fo);
           
            if (prj != null && prj instanceof NbModuleProject && ((NbModuleProject)prj).getSourceDirectory().equals(fo.getParent())) {
                 fo.removeFileChangeListener(this);
                 fo.addFileChangeListener(this);
            }
        }
    }
    public void fileDataCreated(FileEvent fe) {
        updateFile(fe.getFile()) ;
    }
    
    public void fileChanged(FileEvent fe) {
        updateFile(fe.getFile());
    }
    
    public void fileDeleted(FileEvent fe) {
        try {
            removeFile(fe.getFile());
        } catch (IOException ex) {
            Util.err.notify();
        }
    } 
    
    public void fileRenamed(FileRenameEvent fe)  {
        try {
            removeFile((FileObject)fe.getSource());
        } catch (IOException ex) {
            Util.err.notify();
        }
        updateFile(fe.getFile());
    }
    
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    private ServiceNodeHandler getHandler(FileObject fo) {
        FileObject parent =  fo.getParent();
        if (parent != null && parent.getName().equals("services") ) {
            parent = parent.getParent(); 
            if (parent != null &&  parent.getName().equals("META-INF")) { // NOI18N
                Project prj = FileOwnerQuery.getOwner(fo);
                return (prj == null) ? null : 
                    (ServiceNodeHandler)prj.getLookup().lookup(ServiceNodeHandler.class);
            }
        }
        return null;
    } 

    private void updateFile(FileObject fo) {
        ServiceNodeHandler handler = getHandler (fo) ;
        if (handler != null) {
            handler.updateFile(fo);
        }
    }
    
    public void removeFile(FileObject fo) throws IOException {
        ServiceNodeHandler handler = getHandler(fo);
        if (handler != null) {
            handler.removeFile(fo);
        }
    }
    
    public static ServicesFileListener getInstance() {
        if (instance == null) {
            instance = new ServicesFileListener();
        }
        return instance;
    }
    

}
