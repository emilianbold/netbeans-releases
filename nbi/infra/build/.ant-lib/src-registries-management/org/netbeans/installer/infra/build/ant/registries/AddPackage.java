/*
 * AddPackageTask.java
 * 
 * Created on 30.05.2007, 11:40:59
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
public class AddPackage extends Task {
    private File root;
    private File archive;
    private String parentUid;
    private String parentVersion;
    private String parentPlatforms;
    
    public void setRoot(final File root) {
        this.root = root;
    }

    public void setArchive(final File archive) {
        this.archive = archive;
    }

    public void setUid(final String parentUid) {
        this.parentUid = parentUid;
    }

    public void setVersion(final String parentVersion) {
        this.parentVersion = parentVersion;
    }

    public void setPlatforms(final String parentPlatforms) {
        this.parentPlatforms = parentPlatforms;
    }
    
    @Override
    public void execute() throws BuildException {
        try {
            new RegistriesManagerImpl().addPackage(
                    root, 
                    archive, 
                    parentUid, 
                    parentVersion, 
                    parentPlatforms);
        } catch (ManagerException e) {
            throw new BuildException(e);
        }
    }
}
