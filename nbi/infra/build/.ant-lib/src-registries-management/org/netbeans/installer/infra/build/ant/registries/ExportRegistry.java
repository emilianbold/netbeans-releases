/*
 * ExportRegistry.java
 * 
 * Created on 30.05.2007, 16:49:37
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
public class ExportRegistry extends Task {
    private File root;
    private File destination;
    private String codebase;

    public void setRoot(final File root) {
        this.root = root;
    }

    public void setDestination(final File destination) {
        this.destination = destination;
    }

    public void setCodebase(final String codebase) {
        this.codebase = codebase;
    }
    
    @Override
    public void execute() throws BuildException {
        try {
            new RegistriesManagerImpl().exportRegistry(
                    root, 
                    destination, 
                    codebase);
        } catch (ManagerException e) {
            throw new BuildException(e);
        }
    }
}
