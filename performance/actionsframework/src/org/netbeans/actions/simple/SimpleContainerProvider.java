/*
 * SimpleContainerProvider.java
 *
 * Created on January 25, 2004, 3:07 PM
 */

package org.netbeans.actions.simple;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.netbeans.actions.spi.ActionProvider;
import org.netbeans.actions.spi.ContainerProvider;

/**
 *
 * @author  tim
 */
public class SimpleContainerProvider extends ContainerProvider {
    private Interpreter interp;
    ResourceBundle bundle;
    /** Creates a new instance of SimpleContainerProvider */
    public SimpleContainerProvider(Interpreter interp, ResourceBundle bundle) {
        this.interp = interp;
        this.bundle = bundle;
    }
    
    public int getContainerState(Object containerType, String containerCtx, java.util.Map context) {
        return ActionProvider.STATE_ENABLED | ActionProvider.STATE_VISIBLE;
    }
    
    public String getDisplayName(Object containerType, String containerCtx) {
        try {
            return bundle.getString(containerCtx);
        } catch (MissingResourceException mre) {
            mre.printStackTrace();
            return containerCtx;
        }
    }
    
    public String[] getMenuContainerContexts() {
        return interp.getMenus();
    }
    
    public String[] getToolbarContainerContexts() {
        return interp.getToolbars();
    }
    
}
