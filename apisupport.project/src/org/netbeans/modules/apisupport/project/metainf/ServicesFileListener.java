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
