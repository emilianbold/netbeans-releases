package org.netbeans.modules.groovy.grails;

import java.util.logging.Logger;
import org.netbeans.modules.groovy.grails.settings.Settings;

import org.openide.modules.ModuleInstall;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

    private static final Logger LOG = Logger.getLogger(Installer.class.getName());
    private Settings settings;
    
    @Override
    public void restored() {
        settings = Settings.getInstance();
        //LOG.log(Level.WARNING, "Hello, World from my module ...");
        
        
    
    }
}