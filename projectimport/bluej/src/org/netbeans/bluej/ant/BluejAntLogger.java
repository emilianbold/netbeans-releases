/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.bluej.ant;

import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.bluej.BluejProject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mkleint
 */
public class BluejAntLogger extends AntLogger {
    
    /** Creates a new instance of BluejAntLogger */
    public BluejAntLogger() {
    }

    /**
     * Mark which kinds of targets this logger is interested in.
     * This applies to both target start and finish events, as well as any other
     * events for which {@link AntEvent#getTargetName} is not null, such as task
     * start and finish events, and message log events.
     * If {@link #NO_TARGETS}, no events with specific targets will be sent to it.
     * If a specific list, only events with defined target names included in the list
     * will be sent to it.
     * If {@link #ALL_TARGETS}, all events not otherwise excluded will be sent to it.
     * 
     * @param session the relevant session
     * @return a nonempty (and non-null) list of target names; by default, {@link #NO_TARGETS}
     */
    public String[] interestedInTargets(AntSession session) {
        return AntLogger.ALL_TARGETS;
    }

    /**
     * Mark whether this logger is interested in a given Ant session.
     * 
     * @param session a session which is about to be start
     * @return true to receive events about it; by default, false
     */
    public boolean interestedInSession(AntSession session) {
        return true;
    }

    /**
     * Mark which kinds of message log events this logger is interested in.
     * This applies only to message log events and no others.
     * Only events with log levels included in the returned list will be delivered.
     * 
     * @param session the relevant session
     * @return a list of levels such as {@link AntEvent#LOG_INFO}; by default, an empty list
     * @see AntSession#getVerbosity
     */
    public int[] interestedInLogLevels(AntSession session) {
        int[] retValue;
        
        retValue = super.interestedInLogLevels(session);
        return retValue;
    }

    /**
     * Mark whether this logger is interested in any Ant script.
     * If true, no events will be masked due to the script location.
     * Note that a few events have no defined script and so will only
     * be delivered to loggers interested in all scripts; typically this
     * applies to debugging messages when a project is just being configured.
     * 
     * @param session the relevant session
     * @return true to receive events for all scripts; by default, false
     */
    public boolean interestedInAllScripts(AntSession session) {
        return true;
    }

    /**
     * Fired when a target is started.
     * It is <em>not</em> guaranteed that {@link AntEvent#getTargetName}
     * will be non-null (as can happen in some circumstances with
     * <code>&lt;import&gt;</code>, for example).
     * The default implementation does nothing.
     * 
     * @param event the associated event object
     */
    public void targetStarted(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        event.consume();
        super.targetStarted(event);
    }

    /**
     * Fired once when a build is started.
     * The default implementation does nothing.
     * 
     * @param event the associated event object
     */
    public void buildStarted(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        super.buildStarted(event);
    }

    /**
     * Fired when a target is finished.
     * It is <em>not</em> guaranteed that {@link AntEvent#getTargetName}
     * will be non-null.
     * The default implementation does nothing.
     * 
     * @param event the associated event object
     */
    public void targetFinished(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        event.consume();
        super.targetFinished(event);
    }

    /**
     * Fired once when a build is finished.
     * The default implementation does nothing.
     * 
     * @param event the associated event object
     * @see AntEvent#getException
     */
    public void buildFinished(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        super.buildFinished(event);
    }

    /**
     * Fired only if the build could not even be started.
     * {@link AntEvent#getException} will be non-null.
     * The default implementation does nothing.
     * 
     * @param event the associated event object
     */
    public void buildInitializationFailed(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        super.buildInitializationFailed(event);
    }

    /**
     * Fired when a message is logged.
     * The task and target fields may or may not be defined.
     * The default implementation does nothing.
     * 
     * @param event the associated event object
     */
    public void messageLogged(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        super.messageLogged(event);
    }

    /**
     * Fired when a task is started.
     * It is <em>not</em> guaranteed that {@link AntEvent#getTaskName} or
     * {@link AntEvent#getTaskStructure} will be non-null, though they will
     * usually be defined.
     * {@link AntEvent#getTargetName} might also be null.
     * The default implementation does nothing.
     * 
     * @param event the associated event object
     */
    public void taskStarted(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        super.taskStarted(event);
    }

    /**
     * Fired when a task is finished.
     * It is <em>not</em> guaranteed that {@link AntEvent#getTaskName} or
     * {@link AntEvent#getTaskStructure} will be non-null.
     * {@link AntEvent#getTargetName} might also be null.
     * The default implementation does nothing.
     * 
     * @param event the associated event object
     */
    public void taskFinished(AntEvent event) {
        if (event.isConsumed()) {
            return;
        }
        super.taskFinished(event);
    }

    /**
     * Mark whether this logger is interested in a given Ant script.
     * Called only if {@link #interestedInAllScripts} is false.
     * Only events with a defined script according to {@link AntEvent#getScriptLocation}
     * which this logger is interested in will be delivered.
     * Note that a few events have no defined script and so will only
     * be delivered to loggers interested in all scripts; typically this
     * applies to debugging messages when a project is just being configured.
     * Note also that a single session can involve many different scripts.
     * 
     * @param script a particular build script
     * @param session the relevant session
     * @return true to receive events sent from this script; by default, false
     */
    public boolean interestedInScript(File script, AntSession session) {
        File folder = script.getParentFile();
        Project prj = null;
        try {
            prj = ProjectManager.getDefault().findProject(FileUtil.toFileObject(folder));
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (prj != null && prj.getLookup().lookup(BluejProject.class) != null) {
            return true;
        }
        return false;
    }
    
}
