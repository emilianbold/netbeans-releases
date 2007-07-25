package org.netbeans.modules.visualweb.samples.bundled;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.netbeans.modules.visualweb.api.complib.ComplibException;
import org.netbeans.modules.visualweb.api.complib.ComplibService;

import org.openide.modules.ModuleInstall;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * Module that packages sample web apps, complibs, and code clips
 * that are bundled with the IDE.
 */
public class BundledModuleInstaller extends ModuleInstall {
    private static final String SAMPLES_BUNDLED_COMPLIBS = "samples.bundled.complibs";
    private static final String INSTALLED                = "installed";

    public void restored() {
    }
    
    public void close() {
    }
}
                                                                                                                                                                                                      
