package org.netbeans.modules.groovy.grails;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.openide.modules.ModuleInstall;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

    private static final Logger LOG = Logger.getLogger(Installer.class.getName());
    
    @Override
    public void restored() {
        
        //LOG.log(Level.WARNING, "Hello, World from my module ...");
    
    }
}