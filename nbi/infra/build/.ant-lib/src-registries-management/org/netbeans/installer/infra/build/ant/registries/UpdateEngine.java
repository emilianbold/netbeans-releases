/*
 * UpdateEngineTask.java
 * 
 * Created on 30.05.2007, 11:23:38
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.installer.infra.build.ant.registries;

import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.lib.registries.ManagerException;
import org.netbeans.installer.infra.lib.registries.impl.RegistriesManagerImpl;

/**
 *
 * @author ks152834
 */
public class UpdateEngine extends Task {
    private File root;
    private File archive;
    
    public void setRoot(final File root) {
        this.root = root;
    }
    
    public void setArchive(final File archive) {
        this.archive = archive;
    }
    
    @Override
    public void execute() throws BuildException {
        try {
            new RegistriesManagerImpl().updateEngine(root, archive);
        } catch (ManagerException e) {
            throw new BuildException(e);
        }
    }
}
