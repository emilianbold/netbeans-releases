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
 * @see <a href="@ANT@/org/apache/tools/ant/module/api/support/ActionUtils.html"><code>ActionUtils</code></a>
 * @author Jesse Glick
 */
public interface ActionProvider {
    
    /**
     * Standard command to incrementally build the project.
     */
    String COMMAND_BUILD = "build"; // NOI18N
    
    /** 
     * Standard command for compiling set of files
     */
    String COMMAND_COMPILE_SINGLE = "compile.single"; // NOI18N
        
    /**
     * Standard command to clean build products.
     */
    String COMMAND_CLEAN = "clean"; // NOI18N
    
    /**
     * Standard command to do a "clean" (forced) rebuild.
     */
    String COMMAND_REBUILD = "rebuild"; // NOI18N
        
    /** 
     * Standard command for running the project
     */
    String COMMAND_RUN = "run"; // NOI18N

    /** 
     * Standard command for running one file
     */
    String COMMAND_RUN_SINGLE = "run.single"; // NOI18N
    
    /** 
     * Standard command for running tests on given projects
     */
    String COMMAND_TEST = "test"; // NOI18N
    
    /** 
     * Standard command for running one test file
     */    
    String COMMAND_TEST_SINGLE = "test.single";  // NOI18N
    
    /**
     * Standard command for running the project in debugger
     */    
    String COMMAND_DEBUG = "debug"; // NOI18N
    
    /**
     * Standard command for running single file in debugger
     */    
    String COMMAND_DEBUG_SINGLE = "debug.single"; // NOI18N
    
    /** 
     * Standard command for running one test in debugger
     */
    String COMMAND_DEBUG_TEST_SINGLE = "debug.test.single"; // NOI18N
    
    /** 
     * Standard command for starting app in debugger and stopping at the 
     * beginning of app whatever that means.
     */
    String COMMAND_DEBUG_STEP_INTO = "debug.stepinto"; // NOI18N
    
    /**
     * Standard command for deleting the project.
     *
     * @since 1.6
     */
    String COMMAND_DELETE = "delete"; // NOI18N
    
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
