/*
 * ActionFactory.java
 *
 * Created on January 24, 2004, 1:28 AM
 */

package org.netbeans.actions.engine.spi;

import java.util.Map;
import javax.swing.Action;

/** Creates actions on demand only when they need to be invoked.
 *
 * @author  tim
 */
public interface ActionFactory {
    
    //XXX get rid of the containerCtx param, it's not used
    
    /** Construct an invokable Swing action.  The action should only be 
     * constructed when a user-gesture to invoke has been made and needs to
     * be fulfilled */
    public Action getAction (String action, String containerCtx, Map context);
    
}
