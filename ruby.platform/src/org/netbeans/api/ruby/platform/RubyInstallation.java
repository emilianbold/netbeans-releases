/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.api.ruby.platform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.ruby.platform.Util;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 * Information about a Ruby installation.
 *
 * @author Tor Norbye
 */
public class RubyInstallation {
    
    private static final Logger LOGGER = Logger.getLogger(RubyInstallation.class.getName());
    
    /** NOTE: Keep this in sync with ruby/jruby/nbproject/project.properties */
    private static final String JRUBY_RELEASE = "1.3.1"; // NOI18N

    /** NOTE: Keep this in sync with ruby/jruby/nbproject/project.properties */
    private static final String JRUBY_RELEASEDIR = "jruby-" + JRUBY_RELEASE; // NOI18N
    
    /**
     * MIME type for Ruby. Don't change this without also consulting the various XML files
     * that cannot reference this value directly, as well as RUBY_MIME_TYPE in the editing plugin
     */
    public static final String RUBY_MIME_TYPE = "text/x-ruby"; // NOI18N
    public static final String RHTML_MIME_TYPE = "application/x-httpd-eruby"; // NOI18N
    public static final String YAML_MIME_TYPE = "text/x-yaml"; // NOI18N
    private static final RubyInstallation INSTANCE = new RubyInstallation();

    private String jrubyHome;

    private RubyInstallation() {
    }
    
    public static RubyInstallation getInstance() {
        return INSTANCE;
    }

    // Ensure that JRuby can find its libraries etc.
    public void setJRubyLoadPaths() {
        String jh = getJRubyHome();
        if (jh != null) {
            System.setProperty("jruby.home", jh); // NOI18N
        }
    }

    public String getJRuby() {
        String binDir = getJRubyBin();
        if (binDir == null) {
            return null;
        }

        String binary = Utilities.isWindows() ? "jruby.bat" : "jruby"; // NOI18N
        String jruby = binDir + File.separator + binary;

        // Normalize path
        try {
            jruby = new File(jruby).getCanonicalFile().getAbsolutePath();
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }

        if (!new File(jruby).isFile()) {
            return null;
        }
        
        return jruby;
    }
    
    static void displayRubyOptions() {
        OptionsDisplayer.getDefault().open("RubyOptions"); // NOI18N
    }
    
    /**
     * Returns directory where bundle JRuby is installed. Return <tt>null</tt> if
     * not installed.
     * 
     * @return might be <tt>null</tt>
     */
    public String getJRubyHome() {
        if (jrubyHome == null) {
            File jrubyDir =
                InstalledFileLocator.getDefault()
                                    .locate(JRUBY_RELEASEDIR, "org.netbeans.modules.ruby.project", // NOI18N
                    false); // NOI18N

            if ((jrubyDir == null) || !jrubyDir.isDirectory()) {
                // The JRuby distribution may not be installed
                return null;
            }

            jrubyHome = jrubyDir.getPath();
        }

        return jrubyHome;
    }

    private String getJRubyBin() {
        String jh = getJRubyHome();
        if (jh != null) {
            return jh + File.separator + "bin"; // NOI18N
        } else {
            return null;
        }
    }

    /**
     * AutoUpdate may not set execute permissions on the bundled JRuby files,
     * so try to fix that here
     * @todo Do this lazily before trying to actually execute any of these bits?
     */
    public void ensureExecutable() {
        // No excute permissions on Windows. On Unix and Mac, try.
        if (Utilities.isWindows()) {
            return;
        }

        String binDirPath = getJRubyBin();
        if (binDirPath == null) {
            return;
        }

        File binDir = new File(binDirPath);
        if (!binDir.exists()) {
            // No JRuby bundled installation?
            return;
        }

        // Ensure that the binaries are installed as expected
        // The following logic is from CLIHandler in core/bootstrap:
        File chmod = new File("/bin/chmod"); // NOI18N

        if (!chmod.isFile()) {
            // Linux uses /bin, Solaris /usr/bin, others hopefully one of those
            chmod = new File("/usr/bin/chmod"); // NOI18N
        }

        if (chmod.isFile()) {
            try {
                List<String> argv = new ArrayList<String>();
                argv.add(chmod.getAbsolutePath());
                argv.add("u+rx"); // NOI18N

                String[] files = binDir.list();

                for (String file : files) {
                    argv.add(file);
                }

                ProcessBuilder pb = new ProcessBuilder(argv);
                pb.directory(binDir);
                Util.adjustProxy(pb);

                Process process = pb.start();

                int chmoded = process.waitFor();

                if (chmoded != 0) {
                    throw new IOException("could not run " + argv + " : Exit value=" + chmoded); // NOI18N
                }
            } catch (Throwable e) {
                // 108252 - no loud complaints
                LOGGER.log(Level.INFO, "Can't chmod+x JRuby bits", e);
            }
        }
    }

//    public String getShortName() {
//        String r = Util.getPreferences().get(KEY_RUBY, null);
//        final String BUILTIN_JRUBY = NbBundle.getMessage(RubyInstallation.class, "BuiltinRuby");
//        if (r == null) {
//            return BUILTIN_JRUBY;
//        } else {
//            r = getRuby();
//            if (r == null) {
//                return "";
//            }
//
//            final String jh = getJRubyHome();
//            if (jh != null & r.startsWith(jh)) {
//                return BUILTIN_JRUBY;
//            }
//            if (isJRubySet()) {
//                return NbBundle.getMessage(RubyInstallation.class, "JRuby"); // TODO version
//            }
//        }
//
//        // How do I summary other interpreters?? For now, just use path
//        final File rubyFile = new File(r);
//        String basename = rubyFile.getName();
//        return NbBundle.getMessage(RubyInstallation.class, "RubyInPath", basename, rubyFile.getParent());
//    }

}
