package org.netbeans.modules.websvc.rest;

import org.netbeans.modules.websvc.rest.component.palette.RestPaletteListener;
import org.netbeans.spi.palette.PaletteController;
import org.openide.modules.ModuleInstall;
import org.openide.windows.TopComponent;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class RestModuleInstall extends ModuleInstall {
    
    private static RestPaletteListener listener = null;
    
    public void restored() {
        if(listener == null)
            listener = new RestPaletteListener();
        TopComponent.getRegistry().addPropertyChangeListener(listener);
    }
    
    public static PaletteController getPaletteController() {
        return listener.getController();
    }
}
