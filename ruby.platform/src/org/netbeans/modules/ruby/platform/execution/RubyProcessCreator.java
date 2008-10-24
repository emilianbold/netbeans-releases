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
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.extexecution.api.ExecutionDescriptor;
import org.netbeans.modules.extexecution.api.ExecutionDescriptor.LineConvertorFactory;
import org.netbeans.modules.extexecution.api.ExternalProcessBuilder;
import org.netbeans.modules.extexecution.api.print.LineConvertor;
import org.netbeans.modules.extexecution.api.print.LineConvertors;
import org.netbeans.modules.ruby.platform.RubyExecution;
import org.netbeans.modules.ruby.platform.spi.RubyDebuggerImplementation;
import org.openide.filesystems.FileObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 * A helper class for migrating from the ruby execution API to the new execution API (extexecution).
 * Contains a lot of copy-pasted code from <code>RubyExecution</code> (which will eventually
 * be removed).
 *
 * @author Erno Mononen
 */
public class RubyProcessCreator implements Callable<Process> {

    private static final String WINDOWS_DRIVE = "(?:\\S{1}:[\\\\/])"; // NOI18N
    private static final String FILE_CHAR = "[^\\s\\[\\]\\:\\\"]"; // NOI18N
    private static final String FILE = "((?:" + FILE_CHAR + "*))"; // NOI18N
    private static final String FILE_WIN = "(" + WINDOWS_DRIVE + "(?:" + FILE_CHAR + ".*))"; // NOI18N
    private static final String LINE = "([1-9][0-9]*)"; // NOI18N
    private static final String ROL = ".*\\s?"; // NOI18N
    private static final String SEP = "\\:"; // NOI18N
    private static final String STD_SUFFIX = FILE + SEP + LINE + ROL;

    private static final Pattern RUBY_COMPILER = Pattern.compile(".*?" + STD_SUFFIX); // NOI18N

    private static final Pattern RUBY_COMPILER_WIN_MY = Pattern.compile(".*?" + FILE_WIN + SEP + LINE + ROL); // NOI18N

    /* Keeping old one. Get rid of this with more specific recongizers? */
    private static final Pattern RUBY_COMPILER_WIN =
        Pattern.compile("^(?:(?:\\[|\\]|\\-|\\:|[0-9]|\\s|\\,)*)(?:\\s*from )?" + FILE_WIN + SEP + LINE + ROL); // NOI18N
    private static final Pattern RAILS_RECOGNIZER =
        Pattern.compile(".*#\\{RAILS_ROOT\\}/" + STD_SUFFIX); // NOI18N

    private static final Pattern RUBY_TEST_OUTPUT = Pattern.compile("\\s*test.*\\[" + STD_SUFFIX); // NOI18N
    /** When not set (the default) do stdio syncing for native Ruby binaries */
    private static final boolean SYNC_RUBY_STDIO = System.getProperty("ruby.no.sync-stdio") == null; // NOI18N
    /** Set to suppress using the -Kkcode flag in case you're using a weird interpreter which doesn't support it */
    //private static final boolean SKIP_KCODE = System.getProperty("ruby.no.kcode") == null; // NOI18N
    private static final boolean SKIP_KCODE = true;
    /** When not set (the default) bypass the JRuby launcher unix/ba-file scripts and launch VM directly */
    private static final boolean LAUNCH_JRUBY_SCRIPT = System.getProperty("ruby.use.jruby.script") != null; // NOI18N
    private final RubyExecutionDescriptor descriptor;
    private final String charsetName;
    /** Regexp. for extensions. */
    public static final Pattern EXT_RE = Pattern.compile(".*\\.(rb|rake|mab|rjs|rxml|builder)"); // NOI18N
    private final LineConvertorFactory lineConvertorFactory;

    private static LineConvertorFactory getLineConvertorFactory(final FileLocator locator) {
        LineConvertors.FileLocator wrapper = wrap(locator);
        LineConvertor rubyCompilerWin = LineConvertors.filePattern(null, wrapper, RUBY_COMPILER_WIN, EXT_RE, 1, 2);
        LineConvertor rubyCompiler = LineConvertors.filePattern(rubyCompilerWin, wrapper, RUBY_COMPILER, EXT_RE, 1, 2);
        LineConvertor rubyCompilerWinMy = LineConvertors.filePattern(rubyCompiler, wrapper, RUBY_COMPILER_WIN_MY, EXT_RE, 1, 2);
        final LineConvertor railsRecognizer = LineConvertors.filePattern(rubyCompilerWinMy, wrapper, RAILS_RECOGNIZER, EXT_RE, 1, 2);

        return new LineConvertorFactory() {

            public LineConvertor newLineConvertor() {
                return railsRecognizer;
            }
        };

    }

    /**
     * Wraps the given locator as a LineConvertors.FileLocator. Just a temp utility 
     * method to ease the migration to extexecution.
     */
    public static LineConvertors.FileLocator wrap(final FileLocator locator) {
        LineConvertors.FileLocator wrapper = new LineConvertors.FileLocator() {

            public FileObject find(String filename) {
                return locator.find(filename);
            }
        };
        return wrapper;
    }

    //XXX: to be removed once the extexecution api supports chaining convertors
    public static LineConvertor getConvertor(FileLocator locator) {
        return getLineConvertorFactory(locator).newLineConvertor();
    }

    public RubyProcessCreator(RubyExecutionDescriptor descriptor) {
        this(descriptor, null, null);
    }

    public RubyProcessCreator(RubyExecutionDescriptor descriptor, String charsetName) {
        this(descriptor, null, null);
    }

    public RubyProcessCreator(RubyExecutionDescriptor descriptor, LineConvertorFactory lineConvertorFactory) {
        this(descriptor, lineConvertorFactory, null);
    }

    public RubyProcessCreator(RubyExecutionDescriptor descriptor, LineConvertorFactory lineConvertorFactory, String charsetName) {
        if (descriptor.getCmd() == null) {
            descriptor.cmd(descriptor.getPlatform().getInterpreterFile());
        }
        descriptor.addBinPath(true);
        this.descriptor = descriptor;
        this.charsetName = charsetName;
        if (lineConvertorFactory != null) {
            this.lineConvertorFactory = lineConvertorFactory;
        } else {
            this.lineConvertorFactory = getLineConvertorFactory(descriptor.getFileLocator());
        }
    }

    public Process call() throws Exception {
        if (descriptor.debug) {
            RubyDebuggerImplementation debugger = Lookup.getDefault().lookup(RubyDebuggerImplementation.class);
            debugger.describeProcess(descriptor);
            if (debugger == null || !debugger.canDebug()) {
                assert false; //
                return null;
            }
            return debugger.debug();
        }
        ExternalProcessBuilder builder = null;
        List<? extends String> args = buildArgs();
        if (!descriptor.cmd.getName().startsWith("jruby") || RubyExecution.LAUNCH_JRUBY_SCRIPT) { // NOI18N
            builder = new ExternalProcessBuilder(descriptor.cmd.getPath());
        } else {
            builder = new ExternalProcessBuilder(args.get(0));
            args.remove(0);
        }

        for (String arg : args) {
            builder = builder.addArgument(arg);

        }
        builder = builder.workingDirectory(descriptor.getPwd());
        for (Entry<String, String> entry : descriptor.getAdditionalEnvironment().entrySet()) {
            builder = builder.addEnvironmentVariable(entry.getKey(), entry.getValue());
        }

        return builder.call();
    }

    public ExecutionDescriptor buildExecutionDescriptor() {
        ExecutionDescriptor result = new ExecutionDescriptor()
            .showProgress(descriptor.showProgress)
            .controllable(descriptor.isRerun())
            .inputVisible(descriptor.inputVisible)
            .frontWindow(descriptor.frontWindow)
            .showSuspended(descriptor.showSuspended)
            .postExecution(descriptor.postBuildAction)
            .errConvertorFactory(lineConvertorFactory)
            .outConvertorFactory(lineConvertorFactory);
        
        return result;
    }

    private List<? extends String> getRubyArgs(String rubyHome, String cmdName, RubyExecutionDescriptor descriptor) {
        List<String> argvList = new ArrayList<String>();
        // Decide whether I'm launching JRuby, and if so, take a shortcut and launch
        // the VM directly. This is important because killing JRuby via the launcher script
        // is not working right; now that JRuby on Unix exec's the VM that part is okay but
        // on Windows there are still problems.
        if (!LAUNCH_JRUBY_SCRIPT && cmdName.startsWith("jruby")) { // NOI18N
            String javaHome = getJavaHome();

            argvList.add(javaHome + File.separator + "bin" + File.separator + // NOI18N
                    "java"); // NOI18N
            // XXX Do I need java.exe on Windows?

            // Additional execution flags specified in the JRuby startup script:
            argvList.add("-Xverify:none"); // NOI18N
            argvList.add("-da"); // NOI18N

            String javaMemory = "-Xmx512m"; // NOI18N
            String javaStack = "-Xss1024k"; // NOI18N

            String[] jvmArgs = descriptor == null ? null : descriptor.getJVMArguments();
            if (jvmArgs != null) {
                for (String arg : jvmArgs) {
                    if (arg.contains("-Xmx")) { // NOI18N
                        javaMemory = null;
                    }
                    if (arg.contains("-Xss")) { // NOI18N
                        javaStack = null;
                    }
                    argvList.add(arg);
                }
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

    /**
     * Retruns list of default arguments and options from the descriptor's
     * <code>initialArgs</code>, <code>script</code> and
     * <code>additionalArgs</code> in that order.
     */
    private List<? extends String> getCommonArgs() {
        List<String> argvList = new ArrayList<String>();
        File cmd = descriptor.cmd;
        assert cmd != null;

        if (descriptor.getInitialArgs() != null) {
            argvList.addAll(Arrays.asList(descriptor.getInitialArgs()));
        }

        if (descriptor.getScriptPrefix() != null) {
            argvList.add(descriptor.getScriptPrefix());
        }

        if (descriptor.script != null) {
            argvList.add(descriptor.script);
        }

        if (descriptor.getAdditionalArgs() != null) {
            argvList.addAll(Arrays.asList(descriptor.getAdditionalArgs()));
        }
        return argvList;
    }

    protected List<? extends String> buildArgs() {
        List<String> argvList = new ArrayList<String>();
        String rubyHome = descriptor.getCmd().getParentFile().getParent();
        String cmdName = descriptor.getCmd().getName();
        if (descriptor.isRunThroughRuby()) {
            argvList.addAll(getRubyArgs(rubyHome, cmdName, descriptor));
        }
        argvList.addAll(getCommonArgs());
        return argvList;
    }

    public static String getJavaHome() {
        String javaHome = System.getProperty("jruby.java.home"); // NOI18N

        if (javaHome == null) {
            javaHome = System.getProperty("java.home"); // NOI18N
        }

        return javaHome;
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
            extraCp = p.toString();
        }

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
}
