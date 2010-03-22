/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.platform.execution;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.openide.filesystems.FileObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * Utility methods for external execution.
 * <p>
 * <i>Most of the methods here
 * used to be in <code>RubyExecution</code>.</i>
 *
 * @author Erno Mononen
 */
public final class ExecutionUtils {

    private static final Logger LOGGER = Logger.getLogger(ExecutionUtils.class.getName());

    private static final Pattern[] LOCATION_RECOGNIZER_PATTERNS = new Pattern[]{
        RubyLineConvertorFactory.RAILS_RECOGNIZER,
        RubyLineConvertorFactory.RUBY_COMPILER_WIN_MY,
        RubyLineConvertorFactory.JRUBY_COMPILER,
        RubyLineConvertorFactory.RUBY_COMPILER,
        RubyLineConvertorFactory.RUBY_COMPILER_WIN};

    /** When not set (the default) do stdio syncing for native Ruby binaries */
    private static final boolean SYNC_RUBY_STDIO = System.getProperty("ruby.no.sync-stdio") == null; // NOI18N
    /** Set to suppress using the -Kkcode flag in case you're using a weird interpreter which doesn't support it */
    private static final boolean SKIP_KCODE = Boolean.getBoolean("ruby.no.kcode"); // NOI18N
    /** When not set (the default) bypass the JRuby launcher unix/ba-file scripts and launch VM directly */
    private static final boolean LAUNCH_JRUBY_SCRIPT = System.getProperty("ruby.use.jruby.script") != null; // NOI18N

    private ExecutionUtils() {
    }

    /** When not set (the default) bypass the JRuby launcher unix/ba-file scripts and launch VM directly */
    public static boolean launchJRubyScript() {
        return LAUNCH_JRUBY_SCRIPT;
    }
    /**
     * Returns the basic Ruby interpreter command and associated flags (not
     * application arguments)
     */
    public static List<? extends String> getRubyArgs(final RubyPlatform platform) {
        RubyExecutionDescriptor desc = new RubyExecutionDescriptor(platform);
        return getRubyArgs(platform.getHome().getAbsolutePath(),
                platform.getInterpreterFile().getName(), desc, null);
    }

    static List<? extends String> getRubyArgs(String rubyHome, String cmdName, RubyExecutionDescriptor descriptor, String charsetName) {
        List<String> argvList = new ArrayList<String>();
        // Decide whether I'm launching JRuby, and if so, take a shortcut and launch
        // the VM directly. This is important because killing JRuby via the launcher script
        // is not working right; now that JRuby on Unix exec's the VM that part is okay but
        // on Windows there are still problems.
        if (!launchJRubyScript() && cmdName.startsWith("jruby")) { // NOI18N
            String javaHome = getJavaHome();

            argvList.add(javaHome + File.separator + "bin" + File.separator + // NOI18N
                    "java"); // NOI18N
            // XXX Do I need java.exe on Windows?

            // Additional execution flags specified in the JRuby startup script:
            argvList.add("-Xverify:none"); // NOI18N
            argvList.add("-da"); // NOI18N

            String javaMemory = "-Xmx512m"; // NOI18N
            String javaStack = "-Xss1024k"; // NOI18N
            // use the client mode by default
            String jvmMode = "-client";

            String[] jvmArgs = descriptor == null ? null : descriptor.getJVMArguments();
            if (jvmArgs != null) {
                for (String arg : jvmArgs) {
                    if (arg.contains("-Xmx")) { // NOI18N
                        javaMemory = null;
                    }
                    if (arg.contains("-Xss")) { // NOI18N
                        javaStack = null;
                    }
                    if ("-client".equals(arg) || "-server".equals(arg)) { //NOI18N
                        jvmMode = null;
                    }
                    argvList.add(arg);
                }
            }

            if (jvmMode != null) {
                argvList.add(1, jvmMode);
            }
            if (javaMemory != null) {
                argvList.add(javaMemory);
            }
            if (javaStack != null) {
                argvList.add(javaStack);
            }

            // Classpath
            argvList.add("-classpath"); // NOI18N

            File rubyHomeDir = null;

            try {
                rubyHomeDir = new File(rubyHome);
                rubyHomeDir = rubyHomeDir.getCanonicalFile();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }

            if (!rubyHomeDir.isDirectory()) {
                throw new IllegalArgumentException(rubyHomeDir.getAbsolutePath() + " does not exist."); // NOI18N
            }

            File jrubyLib = new File(rubyHomeDir, "lib"); // NOI18N
            if (!jrubyLib.isDirectory()) {
                throw new AssertionError('"' + jrubyLib.getAbsolutePath() + "\" exists (\"" + descriptor.getCmd() + "\" is not valid JRuby executable?)");
            }

            argvList.add(computeJRubyClassPath(
                    descriptor == null ? null : descriptor.getClassPath(), jrubyLib));

            argvList.add("-Djruby.base=" + rubyHomeDir); // NOI18N
            argvList.add("-Djruby.home=" + rubyHomeDir); // NOI18N
            argvList.add("-Djruby.lib=" + jrubyLib); // NOI18N

            // TODO - turn off verifier?

            if (Utilities.isWindows()) {
                argvList.add("-Djruby.shell=\"cmd.exe\""); // NOI18N
                argvList.add("-Djruby.script=jruby.bat"); // NOI18N
            } else {
                argvList.add("-Djruby.shell=/bin/sh"); // NOI18N
                argvList.add("-Djruby.script=jruby"); // NOI18N
            }

            // Main class
            argvList.add("org.jruby.Main"); // NOI18N

        // TODO: JRUBYOPTS

        // Application arguments follow
        }

        if (!SKIP_KCODE && cmdName.startsWith("ruby")) { // NOI18N
            String cs = charsetName;
            if (cs == null) {
                // Add project encoding flags
                FileObject fo = descriptor.getFileObject();
                if (fo != null) {
                    Charset charset = FileEncodingQuery.getEncoding(fo);
                    if (charset != null) {
                        cs = charset.name();
                    }
                }
            }

            if (cs != null) {
                if (cs.equals("UTF-8")) { // NOI18N
                    argvList.add("-Ku"); // NOI18N
                //} else if (cs.equals("")) {
                // What else???
                }
            }
        }

        // Is this a native Ruby process? If so, do sync-io workaround.
        if (SYNC_RUBY_STDIO && cmdName.startsWith("ruby")) { // NOI18N

            int dot = cmdName.indexOf('.');

            if ((dot == -1) || (dot == 4) || (dot == 5)) { // 5: rubyw

                InstalledFileLocator locator = InstalledFileLocator.getDefault();
                File f =
                        locator.locate("modules/org-netbeans-modules-ruby-project.jar", // NOI18N
                        null, false); // NOI18N

                if (f == null) {
                    throw new RuntimeException("Can't find cluster"); // NOI18N
                }

                f = new File(f.getParentFile().getParentFile().getAbsolutePath() + File.separator +
                        "sync-stdio.rb"); // NOI18N

                try {
                    f = f.getCanonicalFile();
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }

                argvList.add("-r" + f.getAbsolutePath()); // NOI18N
            }
        }
        return argvList;
    }

    public static String getExtraClassPath(String extraCp) {
        if (extraCp != null && File.pathSeparatorChar != ':') {
            // Ugly hack - getClassPath has mixed together path separator chars
            // (:) and filesystem separators, e.g. I might have C:\foo:D:\bar but
            // obviously only the path separator after "foo" should be changed to ;
            StringBuilder p = new StringBuilder();
            int pathOffset = 0;
            for (int i = 0; i < extraCp.length(); i++) {
                char c = extraCp.charAt(i);
                if (c == ':' && pathOffset != 1) {
                    p.append(File.pathSeparatorChar);
                    pathOffset = 0;
                    continue;
                } else {
                    pathOffset++;
                }
                p.append(c);
            }
            return p.toString();
        }
        return extraCp;
    }

    /** Package-private for unit test. */
    static String computeJRubyClassPath(String extraCp, final File jrubyLib) {
        StringBuilder cp = new StringBuilder();
        File[] libs = jrubyLib.listFiles();

        for (File lib : libs) {
            if (lib.getName().endsWith(".jar")) { // NOI18N

                if (cp.length() > 0) {
                    cp.append(File.pathSeparatorChar);
                }

                cp.append(lib.getAbsolutePath());
            }
        }

        // Add in user-specified jars passed via JRUBY_EXTRA_CLASSPATH
        extraCp = getExtraClassPath(extraCp);

        if (extraCp == null) {
            extraCp = System.getenv("JRUBY_EXTRA_CLASSPATH"); // NOI18N
        }

        if (extraCp != null) {
            if (cp.length() > 0) {
                cp.append(File.pathSeparatorChar);
            }
            //if (File.pathSeparatorChar != ':' && extraCp.indexOf(File.pathSeparatorChar) == -1 &&
            //        extraCp.indexOf(':') != -1) {
            //    extraCp = extraCp.replace(':', File.pathSeparatorChar);
            //}
            cp.append(extraCp);
        }
        return Utilities.isWindows() ? "\"" + cp.toString() + "\"" : cp.toString(); // NOI18N
    }

    public static void setupProcessEnvironment(Map<String, String> env, final String pwd, boolean appendJdkToPath) {
        String path = pwd;
        if (!Utilities.isWindows()) {
            path = path.replace(" ", "\\ "); // NOI18N
        }

        // Find PATH environment variable - on Windows it can be some other
        // case and we should use whatever it has.
        String pathName = "PATH"; // NOI18N

        if (Utilities.isWindows()) {
            pathName = "Path"; // NOI18N

            for (String key : env.keySet()) {
                if ("PATH".equals(key.toUpperCase())) { // NOI18N
                    pathName = key;

                    break;
                }
            }
        }

        String currentPath = env.get(pathName);

        if (currentPath == null) {
            currentPath = "";
        }

        currentPath = path + File.pathSeparator + currentPath;

        if (appendJdkToPath) {
            // jruby.java.home always points to jdk(?)
            String jdkHome = System.getProperty("jruby.java.home"); // NOI18N

            if (jdkHome == null) {
                // #115377 - add jdk bin to path
                jdkHome = System.getProperty("jdk.home"); // NOI18N
            }

            String jdkBin = jdkHome + File.separator + "bin"; // NOI18N
            if (!Utilities.isWindows()) {
                jdkBin = jdkBin.replace(" ", "\\ "); // NOI18N
            }
            currentPath = currentPath + File.pathSeparator + jdkBin;
        }

        env.put(pathName, currentPath); // NOI18N
    }

    public static String getJavaHome() {
        String javaHome = System.getProperty("jruby.java.home"); // NOI18N

        if (javaHome == null) {
            javaHome = System.getProperty("java.home"); // NOI18N
        }

        return javaHome;
    }

    /** Just helper method for logging. */
    public static void logProcess(final ProcessBuilder pb) {
        if (LOGGER.isLoggable(Level.FINE)) {
            File dir = pb.directory();
            String basedir = dir == null ? "" : "(basedir: " + dir.getAbsolutePath() + ") ";
            LOGGER.fine("Running: " + basedir + '"' + getProcessAsString(pb.command()) + '"');
            LOGGER.fine("Environment: " + pb.environment());
        }
    }

    /** Just helper method for logging. */
    private static String getProcessAsString(List<? extends String> process) {

        StringBuilder sb = new StringBuilder();
        for (String arg : process) {
            sb.append(arg).append(' ');
        }
        return sb.toString().trim();
    }

    /**
     * Creates a new thread factory that prefixes the names of the threads it
     * creates with the given <code>namePrefix</code>.
     *
     * @param namePrefix the prefix to set.
     * @return
     */
    public static ThreadFactory namedThreadFactory(String namePrefix) {
        return new NamedThreadFactory(namePrefix);
    }

    private static class NamedThreadFactory implements ThreadFactory {

        private final ThreadFactory delegate;
        private final String namePrefix;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        public NamedThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
            this.delegate = Executors.defaultThreadFactory();
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread result = delegate.newThread(r);
            result.setName(namePrefix + "-" + threadNumber.getAndIncrement());
            return result;
        }
    }

    // TODO: find a better place for this method (doesn't have anything to
    // do with external execution)
    public static FileLocation getLocation(String line) {

        final int fileGroup = 1;
        final int lineGroup = 2;

        if (line.length() > 400) {
            return null;
        }

        for (Pattern pattern : LOCATION_RECOGNIZER_PATTERNS) {
            Matcher match = pattern.matcher(line);

            if (match.matches()) {
                String file = null;
                int lineno = -1;

                if (fileGroup != -1) {
                    file = match.group(fileGroup);
                    // Make some adjustments - easier to do here than in the regular expression
                    // (See 109721 and 109724 for example)
                    if (file.startsWith("\"")) { // NOI18N
                        file = file.substring(1);
                    }
                    if (file.startsWith("./")) { // NOI18N
                        file = file.substring(2);
                    }
                    if (!(RubyLineConvertorFactory.EXT_RE.matcher(file).matches() || new File(file).isFile())) {
                        return null;
                    }
                }

                if (lineGroup != -1) {
                    String linenoStr = match.group(lineGroup);

                    try {
                        lineno = Integer.parseInt(linenoStr);
                    } catch (NumberFormatException nfe) {
                        Exceptions.printStackTrace(nfe);
                        lineno = 0;
                    }
                }

                return new FileLocation(file, lineno);
            }
        }

        return null;
    }

    // TODO: move somewhere else (doesn't have anything to
    // do with external execution)
    public static final class FileLocation {

        public final String file;
        public final int line;

        public FileLocation(String file, int line) {
            this.file = file;
            this.line = line;
        }
    }

}
