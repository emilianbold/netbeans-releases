/*
 * ToolbarFactory.java
 *
 * Created on January 24, 2004, 1:06 AM
 */

package org.netbeans.actions.engine.spi;

import javax.swing.JToolBar;

/**
 *
 * @author  tim
 */
public abstract class ToolbarFactory {
    
    public abstract JToolBar createToolbar (String containerCtx);
    public abstract void update (String containerCtx);
}
