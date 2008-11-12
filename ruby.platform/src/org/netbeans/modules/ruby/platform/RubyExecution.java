/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.ruby.platform;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.execution.RubyExecutionDescriptor;
import org.netbeans.modules.ruby.platform.execution.ExecutionService;
import org.netbeans.modules.ruby.platform.execution.RegexpOutputRecognizer;
import org.openide.filesystems.FileObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * Execution service for Ruby. Performs some Ruby specific setup like
 * setting environment required for JRuby, or enabling I/O syncing for
 * native Ruby.
 * 
 * @todo Set HTTP_PROXY in the launched process, as is done for GemManager?
 *   See issue 111680 for details - http://www.netbeans.org/issues/show_bug.cgi?id=111680
 *
 * @author Tor Norbye
 */
public class RubyExecution extends ExecutionService {
    
    private static final String WINDOWS_DRIVE = "(?:\\S{1}:[\\\\/])"; // NOI18N
    private static final String FILE_CHAR = "[^\\s\\[\\]\\:\\\"]"; // NOI18N
    private static final String FILE = "((?:" + FILE_CHAR + "*))"; // NOI18N
    private static final String FILE_WIN = "(" + WINDOWS_DRIVE + "(?:" + FILE_CHAR + ".*))"; // NOI18N
    private static final String LINE = "([1-9][0-9]*)"; // NOI18N
    private static final String ROL = ".*\\s?"; // NOI18N
    private static final String SEP = "\\:"; // NOI18N
    private static final String STD_SUFFIX = FILE + SEP + LINE + ROL;
    
    private static List<RegexpOutputRecognizer> stdRubyRecognizers;

    private static final RegexpOutputRecognizer RUBY_COMPILER =
        new RegexpOutputRecognizer(".*?" + STD_SUFFIX); // NOI18N

    private static final RegexpOutputRecognizer RUBY_COMPILER_WIN_MY =
        new RegexpOutputRecognizer(".*?" + FILE_WIN + SEP + LINE + ROL); // NOI18N

    /* Keeping old one. Get rid of this with more specific recongizers? */
    private static final RegexpOutputRecognizer RUBY_COMPILER_WIN =
        new RegexpOutputRecognizer("^(?:(?:\\[|\\]|\\-|\\:|[0-9]|\\s|\\,)*)(?:\\s*from )?" + FILE_WIN + SEP + LINE + ROL); // NOI18N

    private static final RegexpOutputRecognizer RAILS_RECOGNIZER =
        new RegexpOutputRecognizer(".*#\\{RAILS_ROOT\\}/" + STD_SUFFIX); // NOI18N

    public static final RegexpOutputRecognizer RUBY_TEST_OUTPUT =
        new RegexpOutputRecognizer("\\s*test.*\\[" + STD_SUFFIX); // NOI18N
    
    // TODO - add some more recognizers here which recognize the prefix path to Ruby (gems, GEM_HOME, etc.) such that I
    // can hyperlink to errors in the "rake", "rails" etc. load scripts

    /** When not set (the default) do stdio syncing for native Ruby binaries */
    private static final boolean SYNC_RUBY_STDIO = System.getProperty("ruby.no.sync-stdio") == null; // NOI18N

    /** Set to suppress using the -Kkcode flag in case you're using a weird interpreter which doesn't support it */
    //private static final boolean SKIP_KCODE = System.getProperty("ruby.no.kcode") == null; // NOI18N
    private static final boolean SKIP_KCODE = true;
    
    /** When not set (the default) bypass the JRuby launcher unix/ba-file scripts and launch VM directly */
    public static final boolean LAUNCH_JRUBY_SCRIPT =
        System.getProperty("ruby.use.jruby.script") != null; // NOI18N

    private String charsetName;
    
    public RubyExecution(RubyExecutionDescriptor descriptor) {
        super(descriptor);
        
        assert descriptor != null : "null descriptor";

        if (descriptor.getCmd() == null) {
            descriptor.cmd(descriptor.getPlatform().getInterpreterFile());
        }

        descriptor.addBinPath(true);
    }

    /** Create a Ruby execution service with the given source-encoding charset */
    public RubyExecution(RubyExecutionDescriptor descriptor, String charsetName) {
        this(descriptor);
        this.charsetName = charsetName;
    }
    
    public synchronized static List<? extends RegexpOutputRecognizer> getStandardRubyRecognizers() {
        if (stdRubyRecognizers == null) {
            stdRubyRecognizers = new LinkedList<RegexpOutputRecognizer>();
            stdRubyRecognizers.add(RubyExecution.RAILS_RECOGNIZER);
            stdRubyRecognizers.add(RubyExecution.RUBY_COMPILER_WIN_MY);
            stdRubyRecognizers.add(RubyExecution.RUBY_COMPILER);
            stdRubyRecognizers.add(RubyExecution.RUBY_COMPILER_WIN);
        }
        return stdRubyRecognizers;
    }

    /**
     * Returns the basic Ruby interpreter command and associated flags (not
     * application arguments)
     */
    public static List<? extends String> getRubyArgs(final RubyPlatform platform) {
        return new RubyExecution(new RubyExecutionDescriptor(platform)).getRubyArgs(platform.getHome().getAbsolutePath(),
                platform.getInterpreterFile().getName(), null);
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

    @Override
    protected List<? extends String> buildArgs() {
        List<String> argvList = new ArrayList<String>();
        String rubyHome = descriptor.getCmd().getParentFile().getParent();
        String cmdName = descriptor.getCmd().getName();
        argvList.addAll(getRubyArgs(rubyHome, cmdName, descriptor));
        argvList.addAll(super.buildArgs());
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
