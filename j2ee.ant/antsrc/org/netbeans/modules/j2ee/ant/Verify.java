/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ant;

import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Calling entry into J2EE platform verifier support by integration plugin.
 *
 * @author nn136682
 */

public class Verify extends Task {
    
    private String file;
    public void setFile(String file) {
        this.file = file;
    }
    public String getFile() {
        return file;
    }
    
    public void execute() throws BuildException { 
        File f = getProject().resolveFile(file);
        FileObject targetFO = FileUtil.toFileObject(f);
        if (targetFO == null) {
            log(NbBundle.getMessage(Verify.class, "MSG_FileNotFound", file));
        }
        try {
            FileObject fo = FileUtil.toFileObject(getProject().getBaseDir());
            Project project = FileOwnerQuery.getOwner(fo);
            J2eeModuleProvider jmp = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
            jmp.verify(targetFO, new LogOutputStream(this, 0));
        } catch (Exception ex) {
            throw new BuildException(ex);
        }
    }    
}
