package org.netbeans.modules.uihandler.exceptionreporter;

import org.netbeans.modules.uihandler.api.Controller;
import org.openide.modules.ModuleInstall;

/** Enable and disable exception reporting.
 */
public class Installer extends ModuleInstall {
    
    public void restored() {
        Controller.getDefault().setEnableExceptionHandler(true);
    }

    public void uninstalled() {
        Controller.getDefault().setEnableExceptionHandler(false);
    }

    public void close() {
        Controller.getDefault().setEnableExceptionHandler(false);
    }
    
}
