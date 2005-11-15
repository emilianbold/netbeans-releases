/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * AddPointBaseMenus.java
 *
 * Created on Nov 14, 2005, 12:57 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee.db;
import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

import org.openide.util.Lookup;

/**
 *
 * @author ludo
 */
 class AddPointBaseMenus {
    
    /** Creates a the pointbase menus for start/stop actions, dynamically
     * If we did that in the layer file, it would always be there.
     * Now the menu is optional...
     */
    static void  execute() {
        final Repository rep = (Repository) Lookup.getDefault().lookup(Repository.class);
        FileObject pbFolder = rep.getDefaultFileSystem().findResource("/Menu/Tools/PointbaseMenu"); //NOI18N
        if (pbFolder!=null){
            return;
        }
        final FileObject ToolsFolder = rep.getDefaultFileSystem().findResource("/Menu/Tools");//NOI18N
        try {
            ToolsFolder.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    FileObject pointbaseFolder = ToolsFolder.createFolder("PointbaseMenu"); //NOI18N
                    pointbaseFolder.setAttribute("SystemFileSystem.localizingBundle","org.netbeans.modules.j2ee.sun.ide.j2ee.db.Bundle");//NOI18N
                    FileObject start = pointbaseFolder.createData("org.netbeans.modules.j2ee.sun.ide.j2ee.db.StartAction","instance");//NOI18N
                    FileObject stop  = pointbaseFolder.createData("org.netbeans.modules.j2ee.sun.ide.j2ee.db.StopAction" ,"instance");//NOI18N
                    ToolsFolder.setAttribute("OpenIDE-Folder-Order","org.netbeans.modules.j2ee.sun.ide.j2ee.db.StartAction.instance/org.netbeans.modules.j2ee.sun.ide.j2ee.db.StopAction.instance");//NOI18N


                }
            });
        } catch (FileStateInvalidException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }        
    }
    
}
