/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.apache.tools.ant.module.bridge.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.module.bridge.AntBridge;
import org.apache.tools.ant.module.run.Hyperlink;
import org.apache.tools.ant.module.run.LoggerTrampoline;
import org.apache.tools.ant.module.run.StandardLogger;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.apache.tools.ant.module.spi.TaskStructure;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakSet;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * NetBeans-sensitive build logger.
 * Just delegates all events to the registered SPI loggers
 * through an abstraction layer.
 * Synchronization: all callbacks are synchronized, both to protect access to logger
 * caches, and to prevent AntBridge.suspend/resumeDelegation from being called with
 * dynamic overlaps.
 * @author Jesse Glick
 */
final class NbBuildLogger implements BuildListener, LoggerTrampoline.AntSessionImpl {
    
    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance(NbBuildLogger.class.getName());
    /** hack for debugging unit tests */
    private static final int EM_LEVEL = Boolean.getBoolean(NbBuildLogger.class.getName() + ".LOG_AT_WARNING") ? // NOI18N
        ErrorManager.WARNING : ErrorManager.INFORMATIONAL;
    private static final boolean LOGGABLE = ERR.isLoggable(EM_LEVEL);
    
    final AntSession thisSession;
    
    private final File origScript;
    private String[] targets = null;
    final OutputWriter out;
    final OutputWriter err;
    final InputOutput io;
    private final int verbosity;
    private final String displayName;
    private final Runnable interestingOutputCallback;
    private final ProgressHandle handle;
    private boolean insideRunTask = false; // #95201
    private final RequestProcessor.Task sleepTask = RequestProcessor.getDefault().create(new Runnable() {
        public void run() {
            handle.suspend(insideRunTask ? NbBundle.getMessage(NbBuildLogger.class, "MSG_sleep_running") : "");
        }
    });
    private static final int SLEEP_DELAY = 5000;
    
    private final Map<AntLogger,Object> customData = new HashMap<AntLogger,Object>();
    
    private List<AntLogger> interestedLoggers = null;
    private Map<File/*|null*/,Collection<AntLogger>> interestedLoggersByScript = new HashMap<File,Collection<AntLogger>>();
    private Map<String/*|null*/,Collection<AntLogger>> interestedLoggersByTarget = new HashMap<String,Collection<AntLogger>>();
    private Map<String/*|null*/,Collection<AntLogger>> interestedLoggersByTask = new HashMap<String,Collection<AntLogger>>();
    private Map<Integer,Collection<AntLogger>> interestedLoggersByLevel = new HashMap<Integer,Collection<AntLogger>>();
    
    private final Set<Project> projectsWithProperties = Collections.synchronizedSet(new WeakSet<Project>());
    
    private final Set<Throwable> consumedExceptions = new WeakSet<Throwable>();
    
    /** whether this process should be halted at the next safe point */
    private boolean stop = false;
    /** whether this process is thought to be still running */
    private boolean running = true;
    
    /**
     * Map from master build scripts to maps from imported target names to imported locations.
     * Hack for lack of Target.getLocation() in Ant 1.6.2 and earlier.
     * Unused if targetGetLocation is not null.
     */
    private final Map<String,Map<String,String>> knownImportedTargets = Collections.synchronizedMap(new HashMap<String,Map<String,String>>());
    /**
     * Main script known to be being parsed at the moment.
     * Unused if targetGetLocation is not null.
     */
    private String currentlyParsedMainScript = null;
    /**
     * Imported script known to be being parsed at the moment.
     * Unused if targetGetLocation is not null.
     */
    private String currentlyParsedImportedScript = null;
    /**
     * Last task which was known to be running. Heuristic. Cf. #49464.
     */
    private Task lastTask = null;
    private synchronized Task getLastTask() {
        return lastTask;
    }
    private synchronized void setLastTask(Task lastTask) {
        this.lastTask = lastTask;
    }
    
    public NbBuildLogger(File origScript, OutputWriter out, OutputWriter err, int verbosity, String displayName,
            Runnable interestingOutputCallback, ProgressHandle handle, InputOutput io) {
        thisSession = LoggerTrampoline.ANT_SESSION_CREATOR.makeAntSession(this);
        this.origScript = origScript;
        this.out = out;
        this.err = err;
        this.io = io;
        this.verbosity = verbosity;
        this.displayName = displayName;
        this.interestingOutputCallback = interestingOutputCallback;
        this.handle = handle;
        if (LOGGABLE) {
            ERR.log(EM_LEVEL, "---- Initializing build of " + origScript + " \"" + displayName + "\" at verbosity " + verbosity + " ----");
        }
    }
    
    /** Try to stop running at the next safe point. */
    public void stop() {
        stop = true;
    }
    
    /** Stop the build now if requested. Also restarts sleep timer. */
    private void checkForStop() {
        if (stop) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(NbBuildLogger.class, "MSG_stopped", displayName));
            throw new ThreadDeath();
        }
        if (running) {
            handle.switchToIndeterminate();
            sleepTask.schedule(SLEEP_DELAY);
        }
    }

    /**
     * Notify this process that it has been shut down.
     * Refuse any further queries on AntEvent etc.
     * @see "#71266"
     */
    public void shutdown() {
        running = false;
        out.close();
        err.close();
        handle.finish();
        sleepTask.cancel();
    }
    
    private void verifyRunning() {
        if (!running) {
            throw new IllegalStateException("AntSession/AntEvent/TaskStructure method called after completion of Ant process"); // NOI18N
        }
    }
    
    /**
     * Compute a list of loggers to use for this session.
     * Do not do it in the constructor since the actual targets will not have been
     * set and some loggers may care about the targets. However if buildInitializationFailed
     * is called before then, initialize them anyway.
     */
    private synchronized void initInterestedLoggers() {
        if (interestedLoggers == null) {
            interestedLoggers = new ArrayList<AntLogger>();
            for (AntLogger l : Lookup.getDefault().lookupAll(AntLogger.class)) {
                if (l.interestedInSession(thisSession)) {
                    interestedLoggers.add(l);
                }
            }
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "getInterestedLoggers: loggers=" + interestedLoggers);
            }
        }
    }

    @SuppressWarnings("unchecked") // could use List<Collection<AntLogger>> but too slow?
    private final Collection<AntLogger>[] interestedLoggersByVariousCriteria = new Collection[4];
    private static final Comparator<Collection<AntLogger>> INTERESTED_LOGGERS_SORTER = new Comparator<Collection<AntLogger>>() {
        public int compare(Collection<AntLogger> c1, Collection<AntLogger> c2) {
            int x = c1.size() - c2.size(); // reverse sort by size
            if (x != 0) {
                return x;
            } else {
                return System.identityHashCode(c1) - System.identityHashCode(c2);
            }
        }
    };
    /**
     * Get those loggers interested in a given event.
     */
    private Collection<AntLogger> getInterestedLoggersByEvent(AntEvent e) {
        File scriptLocation = e.getScriptLocation();
        String targetName = e.getTargetName();
        String taskName = e.getTaskName();
        int logLevel = e.getLogLevel();
        synchronized (this) { // #132945: <parallel> can deadlock if you block on event info here
            initInterestedLoggers();
            // Start with the smallest one and go down.
            interestedLoggersByVariousCriteria[0] = getInterestedLoggersByScript(scriptLocation);
            interestedLoggersByVariousCriteria[1] = getInterestedLoggersByTarget(targetName);
            interestedLoggersByVariousCriteria[2] = getInterestedLoggersByTask(taskName);
            interestedLoggersByVariousCriteria[3] = getInterestedLoggersByLevel(logLevel);
            Arrays.sort(interestedLoggersByVariousCriteria, INTERESTED_LOGGERS_SORTER);
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "getInterestedLoggersByVariousCriteria: event=" + e + " loggers=" + Arrays.asList(interestedLoggersByVariousCriteria));
            }
            // XXX could probably be even a bit more efficient by iterating on the fly...
            // and by skipping the sorting which is probably overkill for a small number of a loggers (or hardcode the sort)
            List<AntLogger> loggers = new LinkedList<AntLogger>(interestedLoggersByVariousCriteria[0]);
            for (int i = 1; i < 4; i++) {
                loggers.retainAll(interestedLoggersByVariousCriteria[i]);
            }
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "getInterestedLoggersByEvent: event=" + e + " loggers=" + loggers);
            }
            return loggers;
        }
    }
    
    private synchronized Collection<AntLogger> getInterestedLoggersByScript(File script) {
        Collection<AntLogger> c = interestedLoggersByScript.get(script);
        if (c == null) {
            c = new LinkedHashSet<AntLogger>(interestedLoggers.size());
            interestedLoggersByScript.put(script, c);
            for (AntLogger l : interestedLoggers) {
                if (l.interestedInAllScripts(thisSession) || (script != null && l.interestedInScript(script, thisSession))) {
                    c.add(l);
                }
            }
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "getInterestedLoggersByScript: script=" + script + " loggers=" + c);
            }
        }
        return c;
    }
    
    private synchronized Collection<AntLogger> getInterestedLoggersByTarget(String target) {
        Collection<AntLogger> c = interestedLoggersByTarget.get(target);
        if (c == null) {
            c = new LinkedHashSet<AntLogger>(interestedLoggers.size());
            interestedLoggersByTarget.put(target, c);
            for (AntLogger l : interestedLoggers) {
                String[] interestingTargets = l.interestedInTargets(thisSession);
                if (interestingTargets == AntLogger.ALL_TARGETS ||
                        (target != null && Arrays.asList(interestingTargets).contains(target)) ||
                        (target == null && interestingTargets == AntLogger.NO_TARGETS)) {
                    c.add(l);
                }
            }
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "getInterestedLoggersByTarget: target=" + target + " loggers=" + c);
            }
        }
        return c;
    }
    
    private synchronized Collection<AntLogger> getInterestedLoggersByTask(String task) {
        Collection<AntLogger> c = interestedLoggersByTask.get(task);
        if (c == null) {
            c = new LinkedHashSet<AntLogger>(interestedLoggers.size());
            interestedLoggersByTask.put(task, c);
            for (AntLogger l : interestedLoggers) {
                String[] tasks = l.interestedInTasks(thisSession);
                if (tasks == AntLogger.ALL_TASKS ||
                        (task != null && Arrays.asList(tasks).contains(task)) ||
                        (task == null && tasks == AntLogger.NO_TASKS)) {
                    c.add(l);
                }
            }
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "getInterestedLoggersByTask: task=" + task + " loggers=" + c);
            }
        }
        return c;
    }
    
    private synchronized Collection<AntLogger> getInterestedLoggersByLevel(int level) {
        Collection<AntLogger> c = interestedLoggersByLevel.get(level);
        if (c == null) {
            c = new LinkedHashSet<AntLogger>(interestedLoggers.size());
            interestedLoggersByLevel.put(level, c);
            for (AntLogger l : interestedLoggers) {
                if (level == -1) {
                    c.add(l);
                } else {
                    int[] levels = l.interestedInLogLevels(thisSession);
                    for (int _level : levels)  {
                        if (_level == level) {
                            c.add(l);
                            break;
                        }
                    }
                }
            }
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "getInterestedLoggersByLevel: level=" + level + " loggers=" + c);
            }
        }
        return c;
    }
    
    synchronized void setActualTargets(String[] targets) {
        this.targets = targets;
    }
    
    void buildInitializationFailed(BuildException be) {
        initInterestedLoggers();
        AntEvent ev = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(be));
        if (LOGGABLE) {
            ERR.log(EM_LEVEL, "buildInitializationFailed: " + ev);
        }
        for (AntLogger l : getInterestedLoggersByScript(null)) {
            l.buildInitializationFailed(ev);
        }
        interestingOutputCallback.run();
    }
    
    public void buildStarted(BuildEvent ev) {
        AntBridge.suspendDelegation();
        try {
            checkForStop();
            initInterestedLoggers();
            AntEvent e = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(ev, false));
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "buildStarted: " + e);
            }
            for (AntLogger l : interestedLoggers) {
                try {
                    l.buildStarted(e);
                } catch (RuntimeException x) {
                    ERR.notify(EM_LEVEL, x);
                }
            }
        } finally {
            AntBridge.resumeDelegation();
        }
    }
    
    public void buildFinished(BuildEvent ev) {
        AntBridge.suspendDelegation();
        try {
            // #82160: do not call checkForStop() here
            stop = false; // do not throw ThreadDeath on messageLogged from BridgeImpl cleanup code
            setLastTask(null);
            initInterestedLoggers(); // just in case
            AntEvent e = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(ev, false));
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "buildFinished: " + e);
                if (e.getException() != null) {
                    ERR.notify(EM_LEVEL, e.getException());
                }
            }
            for (AntLogger l : interestedLoggers) {
                try {
                    l.buildFinished(e);
                } catch (RuntimeException x) {
                    ERR.notify(EM_LEVEL, x);
                } catch (Error x) {
                    ERR.notify(EM_LEVEL, x);
                }
            }
            if (e.getException() != null) {
                interestingOutputCallback.run();
            }
        } finally {
            AntBridge.resumeDelegation();
        }
    }
    
    public void targetStarted(BuildEvent ev) {
        AntBridge.suspendDelegation();
        try {
            checkForStop();
            setLastTask(null);
            AntEvent e = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(ev, false));
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "targetStarted: " + e);
            }
            for (AntLogger l : getInterestedLoggersByEvent(e)) {
                try {
                    l.targetStarted(e);
                } catch (RuntimeException x) {
                    ERR.notify(EM_LEVEL, x);
                }
            }
            // Update progress handle label so user can see what is being run.
            Project p = ev.getProject();
            String projectName = null;
            if (p != null) {
                projectName = p.getName();
            }
            String targetName = e.getTargetName();
            if (targetName != null) {
                String message;
                if (projectName != null) {
                    message = NbBundle.getMessage(NbBuildLogger.class, "MSG_progress_target", projectName, targetName);
                } else {
                    message = targetName;
                }
                /*
                if (message.equals(displayName)) {
                    // Redundant in this case.
                    message = "";
                }
                 */
                handle.progress(message);
            }
        } finally {
            AntBridge.resumeDelegation();
        }
    }
    
    public void targetFinished(BuildEvent ev) {
        AntBridge.suspendDelegation();
        try {
            checkForStop();
            setLastTask(null);
            AntEvent e = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(ev, false));
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "targetFinished: " + e);
            }
            for (AntLogger l : getInterestedLoggersByEvent(e)) {
                try {
                    l.targetFinished(e);
                } catch (RuntimeException x) {
                    ERR.notify(EM_LEVEL, x);
                }
            }
        } finally {
            AntBridge.resumeDelegation();
        }
    }
    
    public void taskStarted(BuildEvent ev) {
        AntBridge.suspendDelegation();
        try {
            checkForStop();
            setLastTask(ev.getTask());
            AntEvent e = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(ev, false));
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "taskStarted: " + e);
            }
            for (AntLogger l : getInterestedLoggersByEvent(e)) {
                try {
                    l.taskStarted(e);
                } catch (RuntimeException x) {
                    ERR.notify(EM_LEVEL, x);
                }
            }
            if ("input".equals(e.getTaskName())) { // #81139; NOI18N
                TaskStructure s = e.getTaskStructure();
                if (s != null) {
                    String def = s.getAttribute("defaultvalue"); // NOI18N
                    if (def != null) {
                        NbInputHandler.setDefaultValue(e.evaluate(def));
                    }
                }
            }
            if (isRunTask(e)) {
                insideRunTask = true;
            }
        } finally {
            AntBridge.resumeDelegation();
        }
    }

    private boolean isRunTask(AntEvent event) { // #95201
        String taskName = event.getTaskName();
        return "java".equals(taskName) || "exec".equals(taskName); // NOI18N
    }

    public void taskFinished(BuildEvent ev) {
        AntBridge.suspendDelegation();
        try {
            checkForStop();
            setLastTask(null);
            AntEvent e = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(ev, false));
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "taskFinished: " + e);
            }
            for (AntLogger l : getInterestedLoggersByEvent(e)) {
                try {
                    l.taskFinished(e);
                } catch (RuntimeException x) {
                    ERR.notify(EM_LEVEL, x);
                }
            }
            NbInputHandler.setDefaultValue(null);
            if (isRunTask(e)) {
                insideRunTask = false;
            }
        } finally {
            AntBridge.resumeDelegation();
        }
    }
    
    /**
     * Pattern matching an Ant message logged when it is parsing a build script.
     * Hack for lack of Target.getLocation() in Ant 1.6.2 and earlier.
     * Captured groups:
     * <ol>
     * <li>absolute path of build script
     * </ol>
     */
    private static final Pattern PARSING_BUILDFILE_MESSAGE =
        Pattern.compile("parsing buildfile (.+) with URI = (?:.+)"); // NOI18N
    
    /**
     * Pattern matching an Ant message logged when it is importing a build script.
     * Hack for lack of Target.getLocation() in Ant 1.6.2 and earlier.
     * Captured groups:
     * <ol>
     * <li>absolute path of build script which is doing the importing
     * </ol>
     */
    private static final Pattern IMPORTING_FILE_MESSAGE =
        Pattern.compile("Importing file (?:.+) from (.+)"); // NOI18N
    
    /**
     * Pattern matching an Ant message logged when it has encountered a target in some build script.
     * Hack for lack of Target.getLocation() in Ant 1.6.2 and earlier.
     * Captured groups:
     * <ol>
     * <li>target name
     * </ol>
     */
    private static final Pattern PARSED_TARGET_MESSAGE =
        Pattern.compile(" \\+Target: (.+)"); // NOI18N
    
    public void messageLogged(BuildEvent ev) {
        AntBridge.suspendDelegation();
        try {
            checkForStop();
            AntEvent e = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(ev, true));
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "messageLogged: " + e);
            }
            for (AntLogger l : getInterestedLoggersByEvent(e)) {
                try {
                    l.messageLogged(e);
                } catch (RuntimeException x) {
                    ERR.notify(EM_LEVEL, x);
                }
            }
            // Let the hacks begin!
            String msg = ev.getMessage();
            if (msg.contains("ant.PropertyHelper") || /* #71816 */ msg.contains("ant.projectHelper")) { // NOI18N
                // Only after this has been defined can we get any properties.
                // Even trying earlier will give a recursion error since this pseudoprop
                // is set lazily, which produces a new message logged event.
                projectsWithProperties.add(ev.getProject());
            }
            if (targetGetLocation == null) {
                // Try to figure out which imported targets belong to which actual scripts.
                // XXX consider keeping a singleton Matcher for each pattern and reusing it
                // or just doing string comparisons
                Matcher matcher;
                if ((matcher = PARSING_BUILDFILE_MESSAGE.matcher(msg)).matches()) {
                    if (currentlyParsedMainScript != null) {
                        currentlyParsedImportedScript = matcher.group(1);
                    }
                    if (LOGGABLE) {
                        ERR.log(EM_LEVEL, "Got PARSING_BUILDFILE_MESSAGE: " + currentlyParsedImportedScript);
                    }
                    setLastTask(null);
                } else if ((matcher = IMPORTING_FILE_MESSAGE.matcher(msg)).matches()) {
                    currentlyParsedMainScript = matcher.group(1);
                    currentlyParsedImportedScript = null;
                    if (LOGGABLE) {
                        ERR.log(EM_LEVEL, "Got IMPORTING_FILE_MESSAGE: " + currentlyParsedMainScript);
                    }
                    setLastTask(null);
                } else if ((matcher = PARSED_TARGET_MESSAGE.matcher(msg)).matches()) {
                    if (currentlyParsedMainScript != null && currentlyParsedImportedScript != null) {
                        Map<String,String> targetLocations = knownImportedTargets.get(currentlyParsedMainScript);
                        if (targetLocations == null) {
                            targetLocations = new HashMap<String,String>();
                            knownImportedTargets.put(currentlyParsedMainScript, targetLocations);
                        }
                        targetLocations.put(matcher.group(1), currentlyParsedImportedScript);
                    }
                    if (LOGGABLE) {
                        ERR.log(EM_LEVEL, "Got PARSED_TARGET_MESSAGE: " + matcher.group(1));
                    }
                    setLastTask(null);
                }
            }
        } finally {
            AntBridge.resumeDelegation();
        }
    }
    
    public File getOriginatingScript() {
        verifyRunning();
        return origScript;
    }
    
    public String[] getOriginatingTargets() {
        verifyRunning();
        return targets != null ? targets : new String[0];
    }
    
    public synchronized Object getCustomData(AntLogger logger) {
        verifyRunning();
        return customData.get(logger);
    }
    
    public synchronized void putCustomData(AntLogger logger, Object data) {
        verifyRunning();
        customData.put(logger, data);
    }
    
    public void println(String message, boolean error, OutputListener listener) {
        verifyRunning();
        if (LOGGABLE) {
            ERR.log(EM_LEVEL, "println: error=" + error + " listener=" + listener + " message=" + message);
        }
        OutputWriter ow = error ? err : out;
        try {
            if (listener != null) {
                // Loggers wishing for more control can use getIO and do it themselves.
                boolean important = StandardLogger.isImportant(message);
                ow.println(message, listener, important);
                interestingOutputCallback.run();
            } else {
                ow.println(message);
            }
        } catch (IOException e) {
            ERR.notify(e);
        }
    }
    
    public void deliverMessageLogged(AntEvent originalEvent, String message, int level) {
        verifyRunning();
        if (originalEvent == null) {
            throw new IllegalArgumentException("Must pass an original event to deliverMessageLogged"); // NOI18N
        }
        if (message == null) {
            throw new IllegalArgumentException("Must pass a message to deliverMessageLogged"); // NOI18N
        }
        if (level < AntEvent.LOG_ERR || level > AntEvent.LOG_DEBUG) {
            throw new IllegalArgumentException("Unknown log level for reposted log event: " + level); // NOI18N
        }
        if (LOGGABLE) {
            ERR.log(EM_LEVEL, "deliverMessageLogged: level=" + level + " message=" + message);
        }
        AntEvent newEvent = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new RepostedEvent(originalEvent, message, level));
        for (AntLogger l : getInterestedLoggersByEvent(newEvent)) {
            try {
                l.messageLogged(newEvent);
            } catch (RuntimeException x) {
                ERR.notify(EM_LEVEL, x);
            }
        }
    }
    
    public synchronized void consumeException(Throwable t) throws IllegalStateException {
        verifyRunning();
        if (isExceptionConsumed(t)) {
            throw new IllegalStateException("Already consumed " + t); // NOI18N
        }
        consumedExceptions.add(t);
    }
    
    public synchronized boolean isExceptionConsumed(Throwable t) {
        verifyRunning();
        if (consumedExceptions.contains(t)) {
            return true;
        }
        // Check for nested exceptions too.
        Throwable nested = t.getCause();
        if (nested != null && isExceptionConsumed(nested)) {
            // cache that
            consumedExceptions.add(t);
            return true;
        }
        return false;
    }
    
    public int getVerbosity() {
        verifyRunning();
        return verbosity;
    }
    
    String getDisplayNameNoLock() {
        return displayName;
    }
    
    public String getDisplayName() {
        verifyRunning();
        return displayName;
    }

    public OutputListener createStandardHyperlink(URL file, String message, int line1, int column1, int line2, int column2) {
        verifyRunning();
        return new Hyperlink(file, message, line1, column1, line2, column2);
    }

    public InputOutput getIO() {
        return io;
    }
    
    // Accessors for stuff which is specific to particular versions of Ant.
    private static final Method targetGetLocation; // 1.6.2+
    private static final Method locationGetFileName; // 1.6+
    private static final Method locationGetLineNumber; // 1.6+
    private static final Method runtimeConfigurableGetAttributeMap; // 1.6+
    private static final Method runtimeConfigurableGetChildren; // 1.6+
    private static final Method runtimeConfigurableGetText; // 1.6+
    static {
        Method _targetGetLocation = null;
        try {
            _targetGetLocation = Target.class.getMethod("getLocation"); // NOI18N
            if (AntBridge.getInterface().getAntVersion().indexOf("1.6.2") != -1) { // NOI18N
                // Unfortunately in 1.6.2 the method exists but it doesn't work (Ant #28599):
                _targetGetLocation = null;
            }
        } catch (NoSuchMethodException e) {
            // OK
        } catch (Exception e) {
            ERR.notify(EM_LEVEL, e);
        }
        targetGetLocation = _targetGetLocation;
        Method _locationGetFileName = null;
        try {
            _locationGetFileName = Location.class.getMethod("getFileName"); // NOI18N
        } catch (NoSuchMethodException e) {
            // OK
        } catch (Exception e) {
            ERR.notify(EM_LEVEL, e);
        }
        locationGetFileName = _locationGetFileName;
        Method _locationGetLineNumber = null;
        try {
            _locationGetLineNumber = Location.class.getMethod("getLineNumber"); // NOI18N
        } catch (NoSuchMethodException e) {
            // OK
        } catch (Exception e) {
            ERR.notify(EM_LEVEL, e);
        }
        locationGetLineNumber = _locationGetLineNumber;
        Method _runtimeConfigurableGetAttributeMap = null;
        try {
            _runtimeConfigurableGetAttributeMap = RuntimeConfigurable.class.getMethod("getAttributeMap"); // NOI18N
        } catch (NoSuchMethodException e) {
            // OK
        } catch (Exception e) {
            ERR.notify(EM_LEVEL, e);
        }
        runtimeConfigurableGetAttributeMap = _runtimeConfigurableGetAttributeMap;
        Method _runtimeConfigurableGetChildren = null;
        try {
            _runtimeConfigurableGetChildren = RuntimeConfigurable.class.getMethod("getChildren"); // NOI18N
        } catch (NoSuchMethodException e) {
            // OK
        } catch (Exception e) {
            ERR.notify(EM_LEVEL, e);
        }
        runtimeConfigurableGetChildren = _runtimeConfigurableGetChildren;
        Method _runtimeConfigurableGetText = null;
        try {
            _runtimeConfigurableGetText = RuntimeConfigurable.class.getMethod("getText"); // NOI18N
        } catch (NoSuchMethodException e) {
            // OK
        } catch (Exception e) {
            ERR.notify(EM_LEVEL, e);
        }
        runtimeConfigurableGetText = _runtimeConfigurableGetText;
    }

    /**
     * Try to find the location of an Ant target.
     * @param project if not null, the main project from which this target might have been imported
     */
    private Location getLocationOfTarget(Target target, Project project) {
        if (targetGetLocation != null) {
            try {
                return (Location) targetGetLocation.invoke(target);
            } catch (Exception e) {
                ERR.notify(EM_LEVEL, e);
            }
        }
        // For Ant 1.6.2 and earlier, hope we got the right info from the hacks above.
        if (LOGGABLE) {
            ERR.log(EM_LEVEL, "knownImportedTargets: " + knownImportedTargets);
        }
        if (project != null) {
            String file = project.getProperty("ant.file"); // NOI18N
            if (file != null) {
                Map<String,String> targetLocations = knownImportedTargets.get(file);
                if (targetLocations != null) {
                    String importedFile = targetLocations.get(target.getName());
                    if (importedFile != null) {
                        // Have no line number, note.
                        return new Location(importedFile);
                    }
                }
            }
        }
        // Dunno.
        return null;
    }
    
    private static String getFileNameOfLocation(Location loc) {
        if (locationGetFileName != null) {
            try {
                return (String) locationGetFileName.invoke(loc);
            } catch (Exception e) {
                ERR.notify(EM_LEVEL, e);
            }
        }
        // OK, using Ant 1.5.x.
        String locs = loc.toString();
        // Format: "$file:$line: " or "$file: " or ""
        int x = locs.indexOf(':');
        if (x != -1) {
            return locs.substring(0, x);
        } else {
            return null;
        }
    }
    
    private static int getLineNumberOfLocation(Location loc) {
        if (locationGetLineNumber != null) {
            try {
                return (Integer) locationGetLineNumber.invoke(loc);
            } catch (Exception e) {
                ERR.notify(EM_LEVEL, e);
            }
        }
        // OK, using Ant 1.5.x.
        String locs = loc.toString();
        // Format: "$file:$line: " or "$file: " or ""
        int x = locs.indexOf(':');
        if (x != -1) {
            int x2 = locs.indexOf(':', x + 1);
            if (x2 != -1) {
                String line = locs.substring(x + 1, x2);
                try {
                    return Integer.parseInt(line);
                } catch (NumberFormatException e) {
                    // ignore?
                }
            }
        }
        return 0;
    }
    
    private static Map<String,String> getAttributeMapOfRuntimeConfigurable(RuntimeConfigurable rc) {
        Map<String, String> m = new HashMap<String, String>();
        if (runtimeConfigurableGetAttributeMap != null) {
            try {
                for (Map.Entry entry : ((Map<?,?>) runtimeConfigurableGetAttributeMap.invoke(rc)).entrySet()) {
                    m.put(((String) entry.getKey()).toLowerCase(Locale.ENGLISH), (String) entry.getValue());
                }
            } catch (Exception e) {
                ERR.notify(EM_LEVEL, e);
            }
        }
        return m;
    }

    @SuppressWarnings("unchecked")
    private static Enumeration<RuntimeConfigurable> getChildrenOfRuntimeConfigurable(RuntimeConfigurable rc) {
        if (runtimeConfigurableGetChildren != null) {
            try {
                return (Enumeration<RuntimeConfigurable>) runtimeConfigurableGetChildren.invoke(rc);
            } catch (Exception e) {
                ERR.notify(EM_LEVEL, e);
            }
        }
        return Collections.enumeration(Collections.<RuntimeConfigurable>emptySet());
    }
    
    private static String getTextOfRuntimeConfigurable(RuntimeConfigurable rc) {
        if (runtimeConfigurableGetText != null) {
            try {
                return ((StringBuffer) runtimeConfigurableGetText.invoke(rc)).toString();
            } catch (Exception e) {
                ERR.notify(EM_LEVEL, e);
            }
        }
        return "";
    }
    
    /**
     * Standard event implemention, delegating to the Ant BuildEvent and Project.
     */
    private final class Event implements LoggerTrampoline.AntEventImpl {
        
        private boolean consumed = false;
        private final BuildEvent e;
        private final Throwable exception;
        private final int level;
        private File scriptLocation;
        
        /**
         * Create a new regular event.
         * @param e the Ant build event
         * @param msgLogged true for logged events
         */
        public Event(BuildEvent e, boolean msgLogged) {
            this.e = e;
            exception = e.getException();
            if (msgLogged) {
                level = e.getPriority();
            } else {
                level = -1;
            }
        }
        
        /**
         * Create a new event for buildInitializationFailed.
         * @param exception the problem
         */
        public Event(Throwable exception) {
            e = null;
            this.exception = exception;
            level = -1;
        }
        
        public AntSession getSession() {
            verifyRunning();
            return thisSession;
        }

        public void consume() throws IllegalStateException {
            verifyRunning();
            if (consumed) {
                throw new IllegalStateException("Event already consumed"); // NOI18N
            }
            consumed = true;
        }

        public boolean isConsumed() {
            verifyRunning();
            return consumed;
        }

        public File getScriptLocation() {
            verifyRunning();
            if (scriptLocation != null) {
                return scriptLocation;
            }
            if (e == null) {
                return null;
            }
            Task task = e.getTask();
            if (task != null) {
                Location l = task.getLocation();
                if (l != null) {
                    String file = getFileNameOfLocation(l);
                    if (file != null) {
                        return scriptLocation = new File(file);
                    }
                }
            }
            Target target = e.getTarget();
            Project project = getProjectIfPropertiesDefined();
            if (target != null) {
                Location l = getLocationOfTarget(target, project);
                if (l != null) {
                    String file = getFileNameOfLocation(l);
                    if (file != null) {
                        return scriptLocation = new File(file);
                    }
                }
            }
            // #49464: guess at task.
            Task lastTask = getLastTask();
            if (lastTask != null) {
                Location l = lastTask.getLocation();
                if (l != null) {
                    String file = getFileNameOfLocation(l);
                    if (file != null) {
                        return scriptLocation = new File(file);
                    }
                }
            }
            // #104103: lastTask is more likely to be accurate.
            // Consider a call to Project.log from within a task run in an imported script.
            if (project != null) {
                String file = project.getProperty("ant.file"); // NOI18N
                if (file != null) {
                    return scriptLocation = new File(file);
                }
            }
            // #57153 suggests using SubBuildListener, but is it really necessary?
            return null;
        }
        
        private Project getProjectIfPropertiesDefined() {
            Project project = e.getProject();
            if (project != null && projectsWithProperties.contains(project)) {
                return project;
            } else {
                return null;
            }
        }

        public int getLine() {
            verifyRunning();
            if (e == null) {
                return -1;
            }
            Task task = e.getTask();
            if (task != null) {
                Location l = task.getLocation();
                if (l != null) {
                    int line = getLineNumberOfLocation(l);
                    if (line > 0) {
                        return line;
                    }
                }
            }
            Target target = e.getTarget();
            if (target != null) {
                Location l = getLocationOfTarget(target, getProjectIfPropertiesDefined());
                if (l != null) {
                    int line = getLineNumberOfLocation(l);
                    if (line > 0) {
                        return line;
                    }
                }
            }
            // #49464: guess at task.
            Task lastTask = getLastTask();
            if (lastTask != null) {
                Location l = lastTask.getLocation();
                if (l != null) {
                    int line = getLineNumberOfLocation(l);
                    if (line > 0) {
                        return line;
                    }
                }
            }
            return -1;
        }

        public String getTargetName() {
            verifyRunning();
            if (e == null) {
                return null;
            }
            Target target = e.getTarget();
            if (target != null) {
                String name = target.getName();
                if (name != null && name.length() > 0) {
                    return name;
                }
            }
            // #49464: guess at task.
            Task lastTask = getLastTask();
            if (lastTask != null) {
                target = lastTask.getOwningTarget();
                if (target != null) {
                    String name = target.getName();
                    if (name != null && name.length() > 0) {
                        return name;
                    }
                }
            }
            return null;
        }

        public String getTaskName() {
            verifyRunning();
            if (e == null) {
                return null;
            }
            Task task = e.getTask();
            if (task != null) {
                return task.getRuntimeConfigurableWrapper().getElementTag();
            }
            // #49464: guess at task.
            Task lastTask = getLastTask();
            if (lastTask != null) {
                return lastTask.getRuntimeConfigurableWrapper().getElementTag();
            }
            return null;
        }

        public TaskStructure getTaskStructure() {
            verifyRunning();
            Task task = e.getTask();
            if (task != null) {
                return LoggerTrampoline.TASK_STRUCTURE_CREATOR.makeTaskStructure(new TaskStructureImpl(task.getRuntimeConfigurableWrapper()));
            }
            // #49464: guess at task.
            Task lastTask = getLastTask();
            if (lastTask != null) {
                return LoggerTrampoline.TASK_STRUCTURE_CREATOR.makeTaskStructure(new TaskStructureImpl(lastTask.getRuntimeConfigurableWrapper()));
            }
            return null;
        }

        public String getMessage() {
            verifyRunning();
            if (e == null) {
                return null;
            }
            return e.getMessage();
        }

        public int getLogLevel() {
            verifyRunning();
            return level;
        }

        public Throwable getException() {
            verifyRunning();
            return exception;
        }

        public String getProperty(String name) {
            verifyRunning();
            Project project = getProjectIfPropertiesDefined();
            if (project != null) {
                Object o = project.getProperty(name);
                if (o instanceof String) {
                    return (String) o;
                } else {
                    o = project.getReference(name);
                    if (o != null) {
                        return o.toString();
                    } else {
                        return null;
                    }
                }
            } else {
                return null;
            }
        }

        public Set<String> getPropertyNames() {
            verifyRunning();
            Project project = getProjectIfPropertiesDefined();
            if (project != null) {
                Set<String> s = new HashSet<String>();
                s.addAll(NbCollections.checkedSetByFilter(project.getProperties().keySet(), String.class, true));
                s.addAll(NbCollections.checkedSetByFilter(project.getReferences().keySet(), String.class, true));
                return s;
            } else {
                return Collections.emptySet();
            }
        }

        public String evaluate(String text) {
            verifyRunning();
            Project project = getProjectIfPropertiesDefined();
            if (project != null) {
                return project.replaceProperties(text);
            } else {
                return text;
            }
        }
        
        @Override
        public String toString() {
            return "Event[target=" + getTargetName() + ",task=" + getTaskName() + ",message=" + getMessage() + ",scriptLocation=" + getScriptLocation() + ",exception=" + exception + ",level=" + level + ",consumed=" + consumed + "]"; // NOI18N
        }
        
    }
    
    /**
     * Reposted event delegating to an original one except for message and level.
     * @see #deliverMessageLogged
     */
    private final class RepostedEvent implements LoggerTrampoline.AntEventImpl {
        
        private final AntEvent originalEvent;
        private final String message;
        private final int level;
        private boolean consumed = false;
        
        public RepostedEvent(AntEvent originalEvent, String message, int level) {
            this.originalEvent = originalEvent;
            this.message = message;
            this.level = level;
        }
        
        public void consume() throws IllegalStateException {
            verifyRunning();
            if (consumed) {
                throw new IllegalStateException("Event already consumed"); // NOI18N
            }
            consumed = true;
        }

        public boolean isConsumed() {
            verifyRunning();
            return consumed;
        }
        
        public AntSession getSession() {
            return originalEvent.getSession();
        }
        
        public File getScriptLocation() {
            return originalEvent.getScriptLocation();
        }
        
        public int getLine() {
            return originalEvent.getLine();
        }
        
        public String getTargetName() {
            return originalEvent.getTargetName();
        }
        
        public String getTaskName() {
            return originalEvent.getTaskName();
        }
        
        public TaskStructure getTaskStructure() {
            return originalEvent.getTaskStructure();
        }
        
        public String getMessage() {
            verifyRunning();
            return message;
        }
        
        public int getLogLevel() {
            verifyRunning();
            return level;
        }
        
        public Throwable getException() {
            verifyRunning();
            return null;
        }
        
        public String getProperty(String name) {
            return originalEvent.getProperty(name);
        }
        
        public Set<String> getPropertyNames() {
            return originalEvent.getPropertyNames();
        }
        
        public String evaluate(String text) {
            return originalEvent.evaluate(text);
        }
        
        @Override
        public String toString() {
            return "RepostedEvent[consumed=" + consumed + ",level=" + level + ",message=" + message + /*",orig=" + originalEvent +*/ "]"; // NOI18N
        }
        
    }
    
    /**
     * Implementation of TaskStructure based on an Ant Task.
     * @see Event#getTaskStructure
     */
    private final class TaskStructureImpl implements LoggerTrampoline.TaskStructureImpl {
        
        private final RuntimeConfigurable rc;
        
        public TaskStructureImpl(RuntimeConfigurable rc) {
            this.rc = rc;
        }
        
        public String getName() {
            verifyRunning();
            String name = rc.getElementTag();
            if (name != null) {
                return name;
            } else {
                // What does this mean?
                return "";
            }
        }
        
        public String getAttribute(String name) {
            verifyRunning();
            return getAttributeMapOfRuntimeConfigurable(rc).get(name.toLowerCase(Locale.ENGLISH));
        }
        
        public Set<String> getAttributeNames() {
            verifyRunning();
            return getAttributeMapOfRuntimeConfigurable(rc).keySet();
        }
        
        public String getText() {
            verifyRunning();
            String s = getTextOfRuntimeConfigurable(rc);
            if (s.length() > 0) {
                // XXX is it appropriate to trim() this? probably not
                return s;
            } else {
                return null;
            }
        }
        
        public TaskStructure[] getChildren() {
            verifyRunning();
            List<TaskStructure> structures = new ArrayList<TaskStructure>();
            for (RuntimeConfigurable subrc : NbCollections.iterable(getChildrenOfRuntimeConfigurable(rc))) {
                structures.add(LoggerTrampoline.TASK_STRUCTURE_CREATOR.makeTaskStructure(new TaskStructureImpl(subrc)));
            }
            return structures.toArray(new TaskStructure[structures.size()]);
        }
        
    }

}
