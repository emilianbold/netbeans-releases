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

package org.netbeans.modules.java.project;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 * Ant logger which handles Java- and Java-project-specific UI.
 * Specifically, handles stack traces hyperlinking and suppresses
 * hyperlinking of nbproject/build-impl.xml files.
 * @author Jesse Glick
 * @see "#42525"
 */
public final class JavaAntLogger extends AntLogger {
    
    // XXX handle Unicode elements as well
    // XXX handle unknown source too (#17734)? or don't bother?
    
    /**
     * Regexp matching one line (not the first) of a stack trace.
     * Captured groups:
     * <ol>
     * <li>package
     * <li>filename
     * <li>line number
     * </ol>
     */
    private static final Pattern STACK_TRACE = Pattern.compile(
    "(?:\t|\\[catch\\] )at ((?:[a-zA-Z_$][a-zA-Z0-9_$]*\\.)*)[a-zA-Z_$][a-zA-Z0-9_$]*\\.[a-zA-Z_$<][a-zA-Z0-9_$>]*\\(([a-zA-Z_$][a-zA-Z0-9_$]*\\.java):([0-9]+)\\)"); // NOI18N
    
    /**
     * Regexp matching the first line of a stack trace, with the exception message.
     * Captured groups:
     * <ol>
     * <li>unqualified name of exception class plus possible message
     * </ol>
     */
    private static final Pattern EXCEPTION_MESSAGE = Pattern.compile(
    "(?:Exception in thread \"main\" )?(?:(?:[a-zA-Z_$][a-zA-Z0-9_$]*\\.)+)([a-zA-Z_$][a-zA-Z0-9_$]*(?:: .+)?)"); // NOI18N
    
    /**
     * Regexp matching part of a Java task's invocation debug message
     * that specificies the classpath.
     * Hack to find the classpath an Ant task is using.
     * Cf. Commandline.describeArguments, issue #28190.
     * Captured groups:
     * <ol>
     * <li>the classpath
     * </ol>
     */
    private static final Pattern CLASSPATH_ARGS = Pattern.compile("\r?\n'-classpath'\r?\n'(.*)'\r?\n"); // NOI18N
    
    /**
     * Regexp matching part of a Java task's invocation debug message
     * that specificies java executable.
     * Hack to find JDK used for execution.
     */
    private static final Pattern JAVA_EXECUTABLE = Pattern.compile("^Executing '(.*)' with arguments:$", Pattern.MULTILINE); // NOI18N
    
    /**
     * Ant task names we will pay attention to.
     */
    private static final String[] TASKS_OF_INTEREST = {
        // XXX should this really be restricted? what about stack traces printed during shutdown?
        "java", // NOI18N
        // #44328: unit tests run a different task:
        "junit", // NOI18N
        // Nice to handle stack traces from e.g. NB's own build system too!
        "exec", // NOI18N
    };
    
    private static final int[] LEVELS_OF_INTEREST = {
        AntEvent.LOG_VERBOSE, // for CLASSPATH_ARGS
        AntEvent.LOG_INFO, // for some stack traces
        AntEvent.LOG_WARN, // for most stack traces
        AntEvent.LOG_ERR, // for some stack traces, incl. those redelivered from StandardLogger
    };
    
    /**
     * Data stored in the session.
     */
    private static final class SessionData {
        public ClassPath platformSources = null;
        public String classpath = null;
        public Collection/*<FileObject>*/ classpathSourceRoots = null;
        public String possibleExceptionText = null;
        public String lastExceptionMessage = null;
        public SessionData() {}
        public void setClasspath(String cp) {
            classpath = cp;
            classpathSourceRoots = null;
        }
        public void setPlatformSources(ClassPath platformSources) {
            this.platformSources = platformSources;
            classpathSourceRoots = null;
        }
    }
    
    /** Default constructor for lookup. */
    public JavaAntLogger() {}
    
    public boolean interestedInSession(AntSession session) {
        return true;
    }
    
    public boolean interestedInAllScripts(AntSession session) {
        return true;
    }
    
    public String[] interestedInTargets(AntSession session) {
        return AntLogger.ALL_TARGETS;
    }
    
    public String[] interestedInTasks(AntSession session) {
        return TASKS_OF_INTEREST;
    }
    
    public int[] interestedInLogLevels(AntSession session) {
        // XXX could exclude those in [INFO..ERR] greater than session.verbosity
        return LEVELS_OF_INTEREST;
    }
    
    private SessionData getSessionData(AntSession session) {
        SessionData data = (SessionData) session.getCustomData(this);
        if (data == null) {
            data = new SessionData();
            session.putCustomData(this, data);
        }
        return data;
    }
    
    public void messageLogged(AntEvent event) {
        AntSession session = event.getSession();
        int messageLevel = event.getLogLevel();
        int sessionLevel = session.getVerbosity();
        SessionData data = getSessionData(session);
        String line = event.getMessage();
        assert line != null;

        Matcher m = STACK_TRACE.matcher(line);
        if (m.matches()) {
            // We have a stack trace.
            String pkg = m.group(1);
            String filename = m.group(2);
            String resource = pkg.replace('.', '/') + filename;
            int lineNumber = Integer.parseInt(m.group(3));
            // Check to see if the class is listed in our per-task sourcepath.
            // XXX could also look for -Xbootclasspath etc., but probably less important
            Iterator it = getCurrentSourceRootsForClasspath(data).iterator();
            while (it.hasNext()) {
                FileObject root = (FileObject)it.next();
                // XXX this is apparently pretty expensive; try to use java.io.File instead
                FileObject source = root.getFileObject(resource);
                if (source != null) {
                    // Got it!
                    hyperlink(line, session, event, source, messageLevel, sessionLevel, data, lineNumber);
                    break;
                }
            }
            // Also check global sourcepath (sources of open projects, and sources
            // corresponding to compile or boot classpaths of open projects).
            // Fallback in case a JAR file is copied to an unknown location, etc.
            // In this case we can't be sure that this source file really matches
            // the .class used in the stack trace, but it is a good guess.
            if (!event.isConsumed()) {
                FileObject source = GlobalPathRegistry.getDefault().findResource(resource);
                if (source != null) {
                    hyperlink(line, session, event, source, messageLevel, sessionLevel, data, lineNumber);
                }
            }
        } else {
            // Track the last line which was not a stack trace - probably the exception message.
            data.possibleExceptionText = line;
            data.lastExceptionMessage = null;
        }
        
        // Look for classpaths.
        if (messageLevel == AntEvent.LOG_VERBOSE) {
            Matcher m2 = CLASSPATH_ARGS.matcher(line);
            if (m2.find()) {
                String cp = m2.group(1);
                data.setClasspath(cp);
            }
            // XXX should also probably clear classpath when taskFinished called
            m2 = JAVA_EXECUTABLE.matcher(line);
            if (m2.find()) {
                String executable = m2.group(1);
                ClassPath platformSources = findPlatformSources(executable);
                if (platformSources != null) {
                    data.setPlatformSources(platformSources);
                }
            }
        }
    }
    
    private ClassPath findPlatformSources(String javaExecutable) {
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
        for (int i=0; i<platforms.length; i++) {
            FileObject fo = platforms[i].findTool("java"); // NOI18N
            if (fo != null) {
                File f = FileUtil.toFile(fo);
                if (f.getAbsolutePath().startsWith(javaExecutable)) {
                    return platforms[i].getSourceFolders();
                }
            }
        }
        return null;
    }
    
    private static void hyperlink(String line, AntSession session, AntEvent event, FileObject source, int messageLevel, int sessionLevel, SessionData data, int lineNumber) {
        if (messageLevel <= sessionLevel && !event.isConsumed()) {
            event.consume();
            try {
                session.println(line, true, session.createStandardHyperlink(source.getURL(), guessExceptionMessage(data), lineNumber, -1, -1, -1));
            } catch (FileStateInvalidException e) {
                assert false : e;
            }
        }
    }
    
    /**
     * Finds source roots corresponding to the apparently active classpath
     * (as reported by logging from Ant when it runs the Java launcher with -cp).
     */
    private static Collection/*<FileObject>*/ getCurrentSourceRootsForClasspath(SessionData data) {
        if (data.classpath == null) {
            return Collections.EMPTY_SET;
        }
        if (data.classpathSourceRoots == null) {
            data.classpathSourceRoots = new LinkedHashSet();
            StringTokenizer tok = new StringTokenizer(data.classpath, File.pathSeparator);
            while (tok.hasMoreTokens()) {
                String binrootS = tok.nextToken();
                File f = FileUtil.normalizeFile(new File(binrootS));
                URL binroot;
                try {
                    binroot = f.toURI().toURL();
                } catch (MalformedURLException e) {
                    throw new AssertionError(e);
                }
                if (FileUtil.isArchiveFile(binroot)) {
                    URL root = FileUtil.getArchiveRoot(binroot);
                    if (root != null) {
                        binroot = root;
                    }
                }
                FileObject[] someRoots = SourceForBinaryQuery.findSourceRoots(binroot).getRoots();
                data.classpathSourceRoots.addAll(Arrays.asList((Object[]) someRoots));
            }
            if (data.platformSources != null) {
                data.classpathSourceRoots.addAll(Arrays.asList(data.platformSources.getRoots()));
            } else {
                // no platform found. use default one:
                JavaPlatform plat = JavaPlatform.getDefault();
                // in unit tests the default platform may be null:
                if (plat != null) {
                    data.classpathSourceRoots.addAll(Arrays.asList(plat.getSourceFolders().getRoots()));
                }
            }
        }
        return data.classpathSourceRoots;
    }
    
    private static String guessExceptionMessage(SessionData data) {
        if (data.possibleExceptionText != null) {
            if (data.lastExceptionMessage == null) {
                Matcher m = EXCEPTION_MESSAGE.matcher(data.possibleExceptionText);
                if (m.matches()) {
                    data.lastExceptionMessage = m.group(1);
                } else {
                    data.possibleExceptionText = null;
                }
            }
            return data.lastExceptionMessage;
        }
        return null;
    }
}
