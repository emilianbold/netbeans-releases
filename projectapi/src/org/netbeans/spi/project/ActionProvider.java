/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project;

import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;

/**
 * Ability for a project to have various actions (e.g. Build) invoked on it.
 * Should be registered in a project's lookup and will be used by UI infrastructure.
 * @see org.netbeans.api.project.Project#getLookup
 * @see <a href="@ANT/PROJECT@/org/netbeans/spi/project/support/ant/ActionHelper.html"><code>ActionHelper</code></a>
 * @author Jesse Glick
 */
public interface ActionProvider {
    
    /**
     * Standard command to incrementally build the project.
     */
    String COMMAND_BUILD = "build";
    
    /**
     * Standard command to clean build products.
     */
    String COMMAND_CLEAN = "clean";
    
    /**
     * Standard command to do a "clean" (forced) rebuild.
     */
    String COMMAND_REBUILD = "rebuild";
    
    /**
     * Get a list of all commands which this project supports.
     * @return a list of command names suitable for {@link #invokeAction}
     * @see #COMMAND_BUILD
     * @see #COMMAND_CLEAN
     * @see #COMMAND_REBUILD
     */
    String[] getSupportedActions();
    
    /**
     * Run a project command.
     * Will be invoked in the event thread.
     * The context may be ignored by some commands, but some may need it in order
     * to get e.g. the selected source file to build by itself, etc.
     * @param command a predefined command name (must be among {@link #getSupportedActions})
     * @param context any action context, e.g. for a node selection
     *                (as in {@link ContextAwareAction})
     * @throws IllegalArgumentException if the requested command is not supported
     */
    void invokeAction(String command, Lookup context) throws IllegalArgumentException;
    
    /**
     * Tells whether the command can be invoked in given context and thus if
     * actions representing this command should be enabled or disabled.
     * The context may be ignored by some commands, but some may need it in order
     * to get e.g. the selected source file to build by itself, etc.
     * @param command a predefined command name (must be among {@link #getSupportedActions})
     * @param context any action context, e.g. for a node selection
     *                (as in {@link ContextAwareAction})
     * @throws IllegalArgumentException if the requested command is not supported
     */
    boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException;
    
}
