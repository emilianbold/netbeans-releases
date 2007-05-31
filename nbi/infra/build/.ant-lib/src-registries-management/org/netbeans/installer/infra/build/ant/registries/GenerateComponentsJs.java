/*
 * GenerateComponentsJs.java
 * 
 * Created on 30.05.2007, 18:43:25
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.installer.infra.build.ant.registries;

import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.Utils;
import org.netbeans.installer.infra.lib.registries.ManagerException;
import org.netbeans.installer.infra.lib.registries.impl.RegistriesManagerImpl;

/**
 *
 * @author ks152834
 */
public class GenerateComponentsJs extends Task {
    private File root;
    private File file;

    public void setRoot(final File root) {
        this.root = root;
    }

    public void setFile(final File file) {
        this.file = file;
    }
    
    @Override
    public void execute() throws BuildException {
        try {
            final String contents = 
                    new RegistriesManagerImpl().generateComponentsJs(root);
            
            Utils.write(file, contents);
        } catch (ManagerException e) {
            throw new BuildException(e);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
}
