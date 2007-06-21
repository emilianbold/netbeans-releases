package org.netbeans.modules.websvc.rest;

import java.beans.Introspector;
import java.io.IOException;
import org.netbeans.modules.websvc.rest.component.palette.RestPaletteFactory;
import org.netbeans.modules.websvc.rest.component.palette.RestPaletteListener;
import org.netbeans.spi.palette.PaletteController;
import org.openide.filesystems.FileObject;
import org.openide.modules.ModuleInstall;
import org.openide.windows.TopComponent;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class RestModuleInstall extends ModuleInstall {

    private static final long serialVersionUID = 1L; // DO NOT CHANGE!
    
    private static RestPaletteListener listener = null;
    
    /**
     *
     *
     */
    public RestModuleInstall() {
        super();
    }
    
    /**
     *
     *
     */
    public void installed() {
        Introspector.flushCaches();
        restored();
    }
    
    /**
     *
     *
     */
    public void restored() {
        TopComponent.getRegistry().removePropertyChangeListener(listener);
        
        listener = new RestPaletteListener();        
        TopComponent.getRegistry().addPropertyChangeListener(listener);
        
        try {
            FileObject restCompFolder = RestPaletteFactory.getRestComponentsFolder();
            restCompFolder.addFileChangeListener(listener);
        } catch (IOException ex) {
        }
    }
    
    public static PaletteController getPaletteController() {
        return listener.getController();
    }
}
