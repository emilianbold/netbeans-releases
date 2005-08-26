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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
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
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.apache.tools.ant.module.spi.TaskStructure;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakSet;
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
    
    private final AntSession thisSession;
    
    private final File origScript;
    private String[] targets = null;
    private final OutputWriter out;
    private final OutputWriter err;
    private final int verbosity;
    private final String displayName;
    private final Runnable interestingOutputCallback;
    
    private final Map/*<AntLogger,Object>*/ customData = new HashMap();
    
    private List/*<AntLogger>*/ interestedLoggers = null;
    private Map/*<File|null,Collection<AntLogger>>*/ interestedLoggersByScript = new HashMap();
    private Map/*<String|null,Collection<AntLogger>>*/ interestedLoggersByTarget = new HashMap();
    private Map/*<String|null,Collection<AntLogger>>*/ interestedLoggersByTask = new HashMap();
    private Map/*<int,Collection<AntLogger>>*/ interestedLoggersByLevel = new HashMap();
    
    private final Set/*<Project>*/ projectsWithProperties = new WeakSet();
    
    private final Set/*<Throwable>*/ consumedExceptions = new WeakSet();
    
    /** whether this process should be halted at the next safe point */
    private boolean stop = false;
    
    /**
     * Map from master build scripts to maps from imported target names to imported locations.
     * Hack for lack of Target.getLocation() in Ant 1.6.2 and earlier.
     * Unused if targetGetLocation is not null.
     */
    private final Map/*<String,Map<String,String>>*/ knownImportedTargets = new HashMap();
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
    
    public NbBuildLogger(File origScript, OutputWriter out, OutputWriter err, int verbosity, String displayName, Runnable interestingOutputCallback) {
        thisSession = LoggerTrampoline.ANT_SESSION_CREATOR.makeAntSession(this);
        this.origScript = origScript;
        this.out = out;
        this.err = err;
        this.verbosity = verbosity;
        this.displayName = displayName;
        this.interestingOutputCallback = interestingOutputCallback;
        if (LOGGABLE) {
            ERR.log(EM_LEVEL, "---- Initializing build of " + origScript + " \"" + displayName + "\" at verbosity " + verbosity + " ----");
        }
    }
    
    /** Try to stop running at the next safe point. */
    public void stop() {
        stop = true;
    }
    
    /** Stop the build now if requested. */
    private void checkForStop() {
        if (stop) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(NbBuildLogger.class, "MSG_stopped", displayName));
            throw new ThreadDeath();
        }
    }
    
    /**
     * Compute a list of loggers to use for this session.
     * Do not do it in the constructor since the actual targets will not have been
     * set and some loggers may care about the targets. However if buildInitializationFailed
     * is called before then, initialize them anyway.
     */
    private void initInterestedLoggers() {
        assert Thread.holdsLock(this);
        if (interestedLoggers == null) {
            interestedLoggers = new ArrayList();
            Iterator it = Lookup.getDefault().lookup(new Lookup.Template(AntLogger.class)).allInstances().iterator();
            while (it.hasNext()) {
                AntLogger l = (AntLogger)it.next();
                if (l.interestedInSession(thisSession)) {
                    interestedLoggers.add(l);
                }
            }
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "getInterestedLoggers: loggers=" + interestedLoggers);
            }
        }
    }

    private final Collection/*<AntLogger>*/[] interestedLoggersByVariousCriteria = new Collection[4];
    private static final Comparator/*<Collection<AntLogger>>*/ INTERESTED_LOGGERS_SORTER = new Comparator() {
        public int compare(Object o1, Object o2) {
            Collection/*<AntLogger>*/ c1 = (Collection/*<AntLogger>*/) o1;
            Collection/*<AntLogger>*/ c2 = (Collection/*<AntLogger>*/) o2;
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
    private Iterator/*<AntLogger>*/ getInterestedLoggersByEvent(AntEvent e) {
        assert Thread.holdsLock(this);
        initInterestedLoggers();
        // Start with the smallest one and go down.
        interestedLoggersByVariousCriteria[0] = getInterestedLoggersByScript(e.getScriptLocation());
        interestedLoggersByVariousCriteria[1] = getInterestedLoggersByTarget(e.getTargetName());
        interestedLoggersByVariousCriteria[2] = getInterestedLoggersByTask(e.getTaskName());
        interestedLoggersByVariousCriteria[3] = getInterestedLoggersByLevel(e.getLogLevel());
        Arrays.sort(interestedLoggersByVariousCriteria, INTERESTED_LOGGERS_SORTER);
        if (LOGGABLE) {
            ERR.log(EM_LEVEL, "getInterestedLoggersByVariousCriteria: event=" + e + " loggers=" + Arrays.asList(interestedLoggersByVariousCriteria));
        }
        // XXX could probably be even a bit more efficient by iterating on the fly...
        // and by skipping the sorting which is probably overkill for a small number of a loggers (or hardcode the sort)
        List/*<AntLogger>*/ loggers = new LinkedList(interestedLoggersByVariousCriteria[0]);
        for (int i = 1; i < 4; i++) {
            loggers.retainAll(interestedLoggersByVariousCriteria[i]);
        }
        if (LOGGABLE) {
            ERR.log(EM_LEVEL, "getInterestedLoggersByEvent: event=" + e + " loggers=" + loggers);
        }
        return loggers.iterator();
    }
    
    private Collection/*<AntLogger>*/ getInterestedLoggersByScript(File script) {
        assert Thread.holdsLock(this);
        Collection c = (Collection)interestedLoggersByScript.get(script);
        if (c == null) {
            c = new LinkedHashSet(interestedLoggers.size());
            interestedLoggersByScript.put(script, c);
            Iterator it = interestedLoggers.iterator();
            while (it.hasNext()) {
                AntLogger l = (AntLogger)it.next();
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
    
    private Collection/*<AntLogger>*/ getInterestedLoggersByTarget(String target) {
        assert Thread.holdsLock(this);
        Collection c = (Collection)interestedLoggersByTarget.get(target);
        if (c == null) {
            c = new LinkedHashSet(interestedLoggers.size());
            interestedLoggersByTarget.put(target, c);
            Iterator it = interestedLoggers.iterator();
            while (it.hasNext()) {
                AntLogger l = (AntLogger)it.next();
                String[] targets = l.interestedInTargets(thisSession);
                if (targets == AntLogger.ALL_TARGETS ||
                        (target != null && Arrays.asList(targets).contains(target)) ||
                        (target == null && targets == AntLogger.NO_TARGETS)) {
                    c.add(l);
                }
            }
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "getInterestedLoggersByTarget: target=" + target + " loggers=" + c);
            }
        }
        return c;
    }
    
    private Collection/*<AntLogger>*/ getInterestedLoggersByTask(String task) {
        assert Thread.holdsLock(this);
        Collection c = (Collection)interestedLoggersByTask.get(task);
        if (c == null) {
            c = new LinkedHashSet(interestedLoggers.size());
            interestedLoggersByTask.put(task, c);
            Iterator it = interestedLoggers.iterator();
            while (it.hasNext()) {
                AntLogger l = (AntLogger)it.next();
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
    
    private Collection/*<AntLogger>*/ getInterestedLoggersByLevel(int level) {
        assert Thread.holdsLock(this);
        Integer i = new Integer(level);
        Collection c = (Collection)interestedLoggersByLevel.get(i);
        if (c == null) {
            c = new LinkedHashSet(interestedLoggers.size());
            interestedLoggersByLevel.put(i, c);
            Iterator it = interestedLoggers.iterator();
            while (it.hasNext()) {
                AntLogger l = (AntLogger)it.next();
                if (level == -1) {
                    c.add(l);
                } else {
                    int[] levels = l.interestedInLogLevels(thisSession);
                    for (int j = 0; j < levels.length; j++) {
                        if (levels[j] == level) {
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
    
    synchronized void buildInitializationFailed(BuildException be) {
        initInterestedLoggers();
        AntEvent ev = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(be));
        if (LOGGABLE) {
            ERR.log(EM_LEVEL, "buildInitializationFailed: " + ev);
        }
        Iterator it = getInterestedLoggersByScript(null).iterator();
        while (it.hasNext()) {
            AntLogger l = (AntLogger)it.next();
            l.buildInitializationFailed(ev);
        }
    }
    
    public synchronized void buildStarted(BuildEvent ev) {
        AntBridge.suspendDelegation();
        try {
            checkForStop();
            initInterestedLoggers();
            AntEvent e = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(ev, false));
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "buildStarted: " + e);
            }
            Iterator it = interestedLoggers.iterator();
            while (it.hasNext()) {
                AntLogger l = (AntLogger)it.next();
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
    
    public synchronized void buildFinished(BuildEvent ev) {
        AntBridge.suspendDelegation();
        try {
            checkForStop();
            lastTask = null;
            initInterestedLoggers(); // just in case
            AntEvent e = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(ev, false));
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "buildFinished: " + e);
                if (e.getException() != null) {
                    ERR.notify(EM_LEVEL, e.getException());
                }
            }
            Iterator it = interestedLoggers.iterator();
            while (it.hasNext()) {
                AntLogger l = (AntLogger)it.next();
                try {
                    l.buildFinished(e);
                } catch (RuntimeException x) {
                    ERR.notify(EM_LEVEL, x);
                }
            }
        } finally {
            AntBridge.resumeDelegation();
        }
    }
    
    public synchronized void targetStarted(BuildEvent ev) {
        AntBridge.suspendDelegation();
        try {
            checkForStop();
            lastTask = null;
            AntEvent e = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(ev, false));
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "targetStarted: " + e);
            }
            Iterator it = getInterestedLoggersByEvent(e);
            while (it.hasNext()) {
                AntLogger l = (AntLogger)it.next();
                try {
                    l.targetStarted(e);
                } catch (RuntimeException x) {
                    ERR.notify(EM_LEVEL, x);
                }
            }
        } finally {
            AntBridge.resumeDelegation();
        }
    }
    
    public synchronized void targetFinished(BuildEvent ev) {
        AntBridge.suspendDelegation();
        try {
            checkForStop();
            lastTask = null;
            AntEvent e = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(ev, false));
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "targetFinished: " + e);
            }
            Iterator it = getInterestedLoggersByEvent(e);
            while (it.hasNext()) {
                AntLogger l = (AntLogger)it.next();
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
    
    public synchronized void taskStarted(BuildEvent ev) {
        AntBridge.suspendDelegation();
        try {
            checkForStop();
            lastTask = ev.getTask();
            AntEvent e = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(ev, false));
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "taskStarted: " + e);
            }
            Iterator it = getInterestedLoggersByEvent(e);
            while (it.hasNext()) {
                AntLogger l = (AntLogger)it.next();
                try {
                    l.taskStarted(e);
                } catch (RuntimeException x) {
                    ERR.notify(EM_LEVEL, x);
                }
            }
        } finally {
            AntBridge.resumeDelegation();
        }
    }
    
    public synchronized void taskFinished(BuildEvent ev) {
        AntBridge.suspendDelegation();
        try {
            checkForStop();
            lastTask = null;
            AntEvent e = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(ev, false));
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "taskFinished: " + e);
            }
            Iterator it = getInterestedLoggersByEvent(e);
            while (it.hasNext()) {
                AntLogger l = (AntLogger)it.next();
                try {
                    l.taskFinished(e);
                } catch (RuntimeException x) {
                    ERR.notify(EM_LEVEL, x);
                }
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
    
    public synchronized void messageLogged(BuildEvent ev) {
        AntBridge.suspendDelegation();
        try {
            checkForStop();
            AntEvent e = LoggerTrampoline.ANT_EVENT_CREATOR.makeAntEvent(new Event(ev, true));
            if (LOGGABLE) {
                ERR.log(EM_LEVEL, "messageLogged: " + e);
            }
            Iterator it = getInterestedLoggersByEvent(e);
            while (it.hasNext()) {
                AntLogger l = (AntLogger)it.next();
                try {
                    l.messageLogged(e);
                } catch (RuntimeException x) {
                    ERR.notify(EM_LEVEL, x);
                }
            }
            // Let the hacks begin!
            String msg = ev.getMessage();
            if (msg.indexOf("ant.PropertyHelper") != -1) { // NOI18N
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
                    lastTask = null;
                } else if ((matcher = IMPORTING_FILE_MESSAGE.matcher(msg)).matches()) {
                    currentlyParsedMainScript = matcher.group(1);
                    currentlyParsedImportedScript = null;
                    if (LOGGABLE) {
                        ERR.log(EM_LEVEL, "Got IMPORTING_FILE_MESSAGE: " + currentlyParsedMainScript);
                    }
                    lastTask = null;
                } else if ((matcher = PARSED_TARGET_MESSAGE.matcher(msg)).matches()) {
                    if (currentlyParsedMainScript != null && currentlyParsedImportedScript != null) {
                        Map/*<String,String>*/ targetLocations = (Map) knownImportedTargets.get(currentlyParsedMainScript);
                        if (targetLocations == null) {
                            targetLocations = new HashMap();
                            knownImportedTargets.put(currentlyParsedMainScript, targetLocations);
                        }
                        targetLocations.put(matcher.group(1), currentlyParsedImportedScript);
                    }
                    if (LOGGABLE) {
                        ERR.log(EM_LEVEL, "Got PARSED_TARGET_MESSAGE: " + matcher.group(1));
                    }
                    lastTask = null;
                }
            }
        } finally {
            AntBridge.resumeDelegation();
        }
    }
    
    public File getOriginatingScript() {
        assert Thread.holdsLock(this);
        return origScript;
    }
    
    public String[] getOriginatingTargets() {
        assert Thread.holdsLock(this);
        return targets != null ? targets : new String[0];
    }
    
    public Object getCustomData(AntLogger logger) {
        assert Thread.holdsLock(this);
        return customData.get(logger);
    }
    
    public void putCustomData(AntLogger logger, Object data) {
        assert Thread.holdsLock(this);
        customData.put(logger, data);
    }
    
    public void println(String message, boolean error, OutputListener listener) {
        assert Thread.holdsLock(this);
        if (LOGGABLE) {
            ERR.log(EM_LEVEL, "println: error=" + error + " listener=" + listener + " message=" + message);
        }
        OutputWriter ow = error ? err : out;
        try {
            if (listener != null) {
                // XXX factor out into AntLogger API!
                boolean important = (message.indexOf("[deprecation]") == -1 && 
                                     message.indexOf("warning") == -1 && 
                                     message.indexOf("stopped") == -1);
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
        assert Thread.holdsLock(this);
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
        Iterator it = getInterestedLoggersByEvent(newEvent);
        while (it.hasNext()) {
            AntLogger l = (AntLogger)it.next();
            try {
                l.messageLogged(newEvent);
            } catch (RuntimeException x) {
                ERR.notify(EM_LEVEL, x);
            }
        }
    }
    
    public void consumeException(Throwable t) throws IllegalStateException {
        assert Thread.holdsLock(this);
        if (isExceptionConsumed(t)) {
            throw new IllegalStateException("Already consumed " + t); // NOI18N
        }
        consumedExceptions.add(t);
    }
    
    public boolean isExceptionConsumed(Throwable t) {
        assert Thread.holdsLock(this);
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
        assert Thread.holdsLock(this);
        return verbosity;
    }
    
    String getDisplayNameNoLock() {
        return displayName;
    }
    
    public String getDisplayName() {
        assert Thread.holdsLock(this);
        return displayName;
    }

    public OutputListener createStandardHyperlink(URL file, String message, int line1, int column1, int line2, int column2) {
        assert Thread.holdsLock(this);
        return new Hyperlink(file, message, line1, column1, line2, column2);
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
            _targetGetLocation = Target.class.getMethod("getLocation", null); // NOI18N
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
            _locationGetFileName = Location.class.getMethod("getFileName", null); // NOI18N
        } catch (NoSuchMethodException e) {
            // OK
        } catch (Exception e) {
            ERR.notify(EM_LEVEL, e);
        }
        locationGetFileName = _locationGetFileName;
        Method _locationGetLineNumber = null;
        try {
            _locationGetLineNumber = Location.class.getMethod("getLineNumber", null); // NOI18N
        } catch (NoSuchMethodException e) {
            // OK
        } catch (Exception e) {
            ERR.notify(EM_LEVEL, e);
        }
        locationGetLineNumber = _locationGetLineNumber;
        Method _runtimeConfigurableGetAttributeMap = null;
        try {
            _runtimeConfigurableGetAttributeMap = RuntimeConfigurable.class.getMethod("getAttributeMap", null); // NOI18N
        } catch (NoSuchMethodException e) {
            // OK
        } catch (Exception e) {
            ERR.notify(EM_LEVEL, e);
        }
        runtimeConfigurableGetAttributeMap = _runtimeConfigurableGetAttributeMap;
        Method _runtimeConfigurableGetChildren = null;
        try {
            _runtimeConfigurableGetChildren = RuntimeConfigurable.class.getMethod("getChildren", null); // NOI18N
        } catch (NoSuchMethodException e) {
            // OK
        } catch (Exception e) {
            ERR.notify(EM_LEVEL, e);
        }
        runtimeConfigurableGetChildren = _runtimeConfigurableGetChildren;
        Method _runtimeConfigurableGetText = null;
        try {
            _runtimeConfigurableGetText = RuntimeConfigurable.class.getMethod("getText", null); // NOI18N
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
                return (Location) targetGetLocation.invoke(target, null);
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
                Map/*<String,String>*/ targetLocations = (Map) knownImportedTargets.get(file);
                if (targetLocations != null) {
                    String importedFile = (String) targetLocations.get(target.getName());
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
                return (String) locationGetFileName.invoke(loc, null);
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
                return ((Integer) locationGetLineNumber.invoke(loc, null)).intValue();
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
    
    private static Map/*<String,String>*/ getAttributeMapOfRuntimeConfigurable(RuntimeConfigurable rc) {
        if (runtimeConfigurableGetAttributeMap != null) {
            try {
                return (Map) runtimeConfigurableGetAttributeMap.invoke(rc, null);
            } catch (Exception e) {
                ERR.notify(EM_LEVEL, e);
            }
        }
        return Collections.EMPTY_MAP;
    }
    
    private static Enumeration/*<RuntimeConfigurable>*/ getChildrenOfRuntimeConfigurable(RuntimeConfigurable rc) {
        if (runtimeConfigurableGetChildren != null) {
            try {
                return (Enumeration) runtimeConfigurableGetChildren.invoke(rc, null);
            } catch (Exception e) {
                ERR.notify(EM_LEVEL, e);
            }
        }
        return Collections.enumeration(Collections.EMPTY_SET);
    }
    
    private static String getTextOfRuntimeConfigurable(RuntimeConfigurable rc) {
        if (runtimeConfigurableGetText != null) {
            try {
                return ((StringBuffer) runtimeConfigurableGetText.invoke(rc, null)).toString();
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
            return thisSession;
        }

        public void consume() throws IllegalStateException {
            if (consumed) {
                throw new IllegalStateException("Event already consumed"); // NOI18N
            }
            consumed = true;
        }

        public boolean isConsumed() {
            return consumed;
        }

        public File getScriptLocation() {
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
            if (project != null) {
                String file = project.getProperty("ant.file"); // NOI18N
                if (file != null) {
                    return scriptLocation = new File(file);
                }
            }
            // #49464: guess at task.
            synchronized (NbBuildLogger.this) {
                if (lastTask != null) {
                    Location l = lastTask.getLocation();
                    if (l != null) {
                        String file = getFileNameOfLocation(l);
                        if (file != null) {
                            return scriptLocation = new File(file);
                        }
                    }
                }
            }
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
            synchronized (NbBuildLogger.this) {
                if (lastTask != null) {
                    Location l = lastTask.getLocation();
                    if (l != null) {
                        int line = getLineNumberOfLocation(l);
                        if (line > 0) {
                            return line;
                        }
                    }
                }
            }
            return -1;
        }

        public String getTargetName() {
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
            synchronized (NbBuildLogger.this) {
                if (lastTask != null) {
                    target = lastTask.getOwningTarget();
                    if (target != null) {
                        String name = target.getName();
                        if (name != null && name.length() > 0) {
                            return name;
                        }
                    }
                }
            }
            return null;
        }

        public String getTaskName() {
            if (e == null) {
                return null;
            }
            Task task = e.getTask();
            if (task != null) {
                return task.getRuntimeConfigurableWrapper().getElementTag();
            }
            // #49464: guess at task.
            synchronized (NbBuildLogger.this) {
                if (lastTask != null) {
                    return lastTask.getRuntimeConfigurableWrapper().getElementTag();
                }
            }
            return null;
        }

        public TaskStructure getTaskStructure() {
            Task task = e.getTask();
            if (task != null) {
                return LoggerTrampoline.TASK_STRUCTURE_CREATOR.makeTaskStructure(new TaskStructureImpl(task.getRuntimeConfigurableWrapper()));
            }
            // #49464: guess at task.
            synchronized (NbBuildLogger.this) {
                if (lastTask != null) {
                    return LoggerTrampoline.TASK_STRUCTURE_CREATOR.makeTaskStructure(new TaskStructureImpl(lastTask.getRuntimeConfigurableWrapper()));
                }
            }
            return null;
        }

        public String getMessage() {
            if (e == null) {
                return null;
            }
            return e.getMessage();
        }

        public int getLogLevel() {
            return level;
        }

        public Throwable getException() {
            return exception;
        }

        public String getProperty(String name) {
            Project project = getProjectIfPropertiesDefined();
            if (project != null) {
                Object o = project.getProperty(name);
                if (o instanceof String) {
                    return (String) o;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        public Set/*<String>*/ getPropertyNames() {
            Project project = getProjectIfPropertiesDefined();
            if (project != null) {
                // XXX could exclude those without String values
                return project.getProperties().keySet();
            } else {
                return Collections.EMPTY_SET;
            }
        }

        public String evaluate(String text) {
            Project project = getProjectIfPropertiesDefined();
            if (project != null) {
                return project.replaceProperties(text);
            } else {
                return text;
            }
        }
        
        public String toString() {
            return "Event[target=" + getTargetName() + ",task=" + getTaskName() + ",message=" + getMessage() + ",scriptLocation=" + getScriptLocation() + ",exception=" + exception + ",level=" + level + ",consumed=" + consumed + "]"; // NOI18N
        }
        
    }
    
    /**
     * Reposted event delegating to an original one except for message and level.
     * @see #deliverMessageLogged
     */
    private static final class RepostedEvent implements LoggerTrampoline.AntEventImpl {
        
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
            if (consumed) {
                throw new IllegalStateException("Event already consumed"); // NOI18N
            }
            consumed = true;
        }

        public boolean isConsumed() {
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
            return message;
        }
        
        public int getLogLevel() {
            return level;
        }
        
        public Throwable getException() {
            return null;
        }
        
        public String getProperty(String name) {
            return originalEvent.getProperty(name);
        }
        
        public Set/*<String>*/ getPropertyNames() {
            return originalEvent.getPropertyNames();
        }
        
        public String evaluate(String text) {
            return originalEvent.evaluate(text);
        }
        
        public String toString() {
            return "RepostedEvent[consumed=" + consumed + ",level=" + level + ",message=" + message + /*",orig=" + originalEvent +*/ "]"; // NOI18N
        }
        
    }
    
    /**
     * Implementation of TaskStructure based on an Ant Task.
     * @see Event#getTaskStructure
     */
    private static final class TaskStructureImpl implements LoggerTrampoline.TaskStructureImpl {
        
        private final RuntimeConfigurable rc;
        
        public TaskStructureImpl(RuntimeConfigurable rc) {
            this.rc = rc;
        }
        
        public String getName() {
            String name = rc.getElementTag();
            if (name != null) {
                return name;
            } else {
                // What does this mean?
                return "";
            }
        }
        
        public String getAttribute(String name) {
            return (String) getAttributeMapOfRuntimeConfigurable(rc).get(name);
        }
        
        public Set/*<String>*/ getAttributeNames() {
            return getAttributeMapOfRuntimeConfigurable(rc).keySet();
        }
        
        public String getText() {
            String s = getTextOfRuntimeConfigurable(rc);
            if (s.length() > 0) {
                // XXX is it appropriate to trim() this? probably not
                return s;
            } else {
                return null;
            }
        }
        
        public TaskStructure[] getChildren() {
            List/*<TaskStructure>*/ structures = new ArrayList();
            Enumeration/*<RuntimeConfigurable>*/ children = getChildrenOfRuntimeConfigurable(rc);
            while (children.hasMoreElements()) {
                RuntimeConfigurable subrc = (RuntimeConfigurable) children.nextElement();
                structures.add(LoggerTrampoline.TASK_STRUCTURE_CREATOR.makeTaskStructure(new TaskStructureImpl(subrc)));
            }
            return (TaskStructure[]) structures.toArray(new TaskStructure[structures.size()]);
        }
        
    }

}
