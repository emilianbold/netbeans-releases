/*
 * ContextMenuFactory.java
 *
 * Created on January 24, 2004, 1:07 AM
 */

package org.netbeans.actions.engine.spi;

import java.util.Map;
import javax.swing.JPopupMenu;

/**
 *
 * @author  Tim Boudreau
 */
public interface ContextMenuFactory {

    //XXX move me into engine/spe
    
    public JPopupMenu createMenu(Map context);
    
}
