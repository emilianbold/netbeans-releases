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

package org.apache.tools.ant.module.spi;

import java.io.File;
import java.util.Set;

/**
 * One event delivered to an {@link AntLogger}.
 * <p>
 * Note that one event is shared across all listeners.
 * </p>
 * <p>
 * The information available from the event represents a best effort to gather
 * information from the Ant run. Some versions of Ant may not support all of
 * these capabilities, in which case the event method will simply return null
 * or whatever the documented fallback value is. For example, Ant 1.5 does
 * not permit details of task structure to be introspected, but 1.6 does.
 * </p>
 * <p>
 * SPI clients are forbidden to implement this interface;
 * new methods may be added in the future.
 * </p>
 * @author Jesse Glick
 * @since org.apache.tools.ant.module/3 3.12
 */
public interface AntEvent {
    
    /**
     * Error log level.
     */
    int LOG_ERR = 0;
    
    /**
     * Warning log level.
     */
    int LOG_WARN = 1;
    
    /**
     * Information log level.
     */
    int LOG_INFO = 2;
    
    /**
     * Verbose log level.
     */
    int LOG_VERBOSE = 3;
    
    /**
     * Debugging log level.
     */
    int LOG_DEBUG = 4;
    
    /**
     * Get the associated session.
     * @return the session object
     */
    AntSession getSession();
    
    /**
     * Mark an event as consumed to advise other loggers not to handle it.
     * @throws IllegalStateException if it was already consumed
     */
    void consume() throws IllegalStateException;
    
    /**
     * Test whether this event has already been consumed by some other logger.
     * @return true if it has already been consumed
     */
    boolean isConsumed();
    
    /**
     * Get the location of the Ant script producing this event.
     * @return the script location, or null if unknown
     */
    File getScriptLocation();
    
    /**
     * Get the line number in {@link #getScriptLocation} corresponding to this event.
     * Line numbers start at one.
     * @return the line number, or -1 if unknown
     */
    int getLine();
    
    /**
     * Get the name of the target in {@link #getScriptLocation} producing this event.
     * Some events occur outside targets and so there will be no target name.
     * @return the target name (never empty), or null if unknown or inapplicable
     */
    String getTargetName();
    
    /**
     * Get the name of the task producing this event.
     * XXX semantics w.r.t. namespaces, taskdefs, etc.?
     * Some events occur outside of tasks and so there will be no name.
     * @return the task name (never empty), or null if unknown or inapplicable
     */
    String getTaskName();
    
    /**
     * Get the configured XML structure of the task producing this event.
     * Some events occur outside of tasks and so there will be no information.
     * @return the task structure, or null if unknown or inapplicable
     */
    TaskStructure getTaskStructure();
    
    /**
     * Get the name of the message being logged.
     * Applies only to {@link AntLogger#messageLogged}.
     * @return the message, or null if inapplicable
     */
    String getMessage();
    
    /**
     * Get the log level of the message.
     * Applies only to {@link AntLogger#messageLogged}.
     * Note that lower numbers are higher priority.
     * @return the log level (e.g. LOG_INFO), or -1 if inapplicable
     */
    int getLogLevel();
    
    /**
     * Get a terminating exception.
     * Applies only to {@link AntLogger#buildFinished}
     * and {@link AntLogger#buildInitializationFailed}.
     * @return an exception ending the build, or null for normal completion or if inapplicable
     */
    Throwable getException();
    
    /**
     * Get a property set on the current Ant project.
     * @param name the property name
     * @return its value, or null
     */
    String getProperty(String name);
    
    /**
     * Get a set of property names defined on the current Ant project.
     * @return a set of property names; may be empty but not null
     */
    Set/*<String>*/ getPropertyNames();
    
    /**
     * Evaluate a string with possible substitutions according to defined properties.
     * @param text the text to evaluate
     * @return its value (may be the same as the incoming text), never null
     * @see TaskStructure#getAttribute
     * @see TaskStructure#getText
     */
    String evaluate(String text);
    
}
