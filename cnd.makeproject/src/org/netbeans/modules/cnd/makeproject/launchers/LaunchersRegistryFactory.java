/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.cnd.makeproject.launchers;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mtishkov
 */
public final class LaunchersRegistryFactory {
    private static final HashMap<FileObject, LaunchersRegistry> instances = new HashMap<>();
    private static final Logger LOG = Logger.getLogger("LaunchersRegistry");//NOI18N

    public static synchronized LaunchersRegistry getInstance(FileObject projectDirectory) {
        LaunchersRegistry launcherRegistryInstance = instances.get(projectDirectory);
        if (launcherRegistryInstance == null) {
            try {
                launcherRegistryInstance = new LaunchersRegistry();
                instances.put(projectDirectory, launcherRegistryInstance);
            } catch (Exception e) {
                LOG.log(Level.INFO, "LauncherList - getInstance - e {0}", e); //FIXUP //NOI18N
                LOG.info("Cannot restore LauncherList ..."); //FIXUP //NOI18N
            }
        }
        return launcherRegistryInstance;
    }    
    
    
}
