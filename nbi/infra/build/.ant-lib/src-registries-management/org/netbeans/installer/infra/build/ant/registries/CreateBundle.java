/*
 * CreateBundleTask.java
 * 
 * Created on 30.05.2007, 11:46:42
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.installer.infra.build.ant.registries;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.Utils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.infra.lib.registries.ManagerException;
import org.netbeans.installer.infra.lib.registries.impl.RegistriesManagerImpl;

/**
 *
 * @author ks152834
 */
public class CreateBundle extends Task {
    private List<Component> componentObjects = new LinkedList<Component>();    
    
    private File root;
    private File target;
    private Platform platform;

    public void setRoot(final File root) {
        this.root = root;
    }

    public void setPlatform(final String platform) {
        try {
            this.platform = StringUtils.parsePlatform(platform);
        } catch (ParseException e) {
            log(e.getMessage());
        }
    }

    public void setTarget(final File target) {
        this.target = target;
    }
    
    public Component createComponent() {
        final Component component = new Component();
        
        componentObjects.add(component);
        return component;
    }
    
    @Override
    public void execute() throws BuildException {
        try {
            final List<String> components = new LinkedList<String>();
            for (Component component: componentObjects) {
                components.add(component.getUid() + "," + component.getVersion());
            }
            
            System.out.println(
                    "Creating bundle: " + platform + ": " + components);
            
            final File bundle = new RegistriesManagerImpl().createBundle(
                    root, 
                    platform, 
                    components.toArray(new String[components.size()]));
            
            Utils.copy(bundle, target);
        } catch (ManagerException e) {
            throw new BuildException(e);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
    
    public static class Component {
        private String uid;
        private String version;

        public void setUid(final String uid) {
            this.uid = uid;
        }

        public String getUid() {
            return uid;
        }

        public void setVersion(final String version) {
            this.version = version;
        }

        public String getVersion() {
            return version;
        }
    }
}
