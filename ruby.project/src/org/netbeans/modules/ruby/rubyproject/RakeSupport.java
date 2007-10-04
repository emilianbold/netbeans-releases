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
package org.netbeans.modules.ruby.rubyproject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;

import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.rubyproject.api.RubyExecution;
import org.netbeans.modules.ruby.rubyproject.execution.ExecutionDescriptor;
import org.netbeans.modules.ruby.rubyproject.execution.FileLocator;
import org.netbeans.modules.ruby.rubyproject.execution.OutputRecognizer;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Various methods for supporting Rake execution.
 *
 * @author Tor Norbye
 */
public class RakeSupport {
    
    /** Standard names used for Rakefile. */
    static final String[] RAKEFILE_NAMES = new String[] {
        "rakefile", "Rakefile", "rakefile.rb", "Rakefile.rb" // NOI18N
    };
    
    private boolean test;
    private Project project;

    public RakeSupport(Project project) {
        this.project = project;
    }

    /**
     * Set whether the rake target should be run as a test (through the test runner etc.)
     */
    public void setTest(boolean test) {
        this.test = test;
    }

    /**
     * Tries to find Rakefile for a given project.
     * 
     * @param project project to be searched
     * @return found Rakefile or <code>null</code> if not found
     */
    public static FileObject getRakeFile(final Project project) {
        FileObject pwd = project.getProjectDirectory();

        // See if we're in the right directory
        for (String s : RakeSupport.RAKEFILE_NAMES) {
            FileObject f = pwd.getFileObject(s);
            if (f != null) {
                return f;
            }
        }

        // Try to adjust the directory to a folder which contains a rakefile
        Sources src = ProjectUtils.getSources(project);
        //TODO: needs to be generified
        SourceGroup[] rubygroups = src.getSourceGroups(RubyProject.SOURCES_TYPE_RUBY);
        if (rubygroups != null && rubygroups.length > 0) {
            for (SourceGroup group : rubygroups) {
                FileObject f = group.getRootFolder();
                for (String s : RakeSupport.RAKEFILE_NAMES) {
                    FileObject r = f.getFileObject(s);
                    if (r != null) {
                        return r;
                    }
                }
            }
        }

        return null;
    }

    public static boolean isRakeFile(FileObject fo) {
        if (!fo.getMIMEType().equals(RubyInstallation.RUBY_MIME_TYPE)) {
            return false;
        }

        String name = fo.getName().toLowerCase();

        if (name.equals("rakefile")) { // NOI18N

            return true;
        }

        String ext = fo.getExt().toLowerCase();

        if (ext.equals("rake")) { // NOI18N

            return true;
        }

        return false;
    }

    /**
     *  Run rake in the given directory. Optionally, run the given rakefile.
     * @param pwd If you specify the rake file, you can pass null as the directory,
     *    and the directory containing the rakeFile will be used, otherwise
     *    it specifies the dir to run rake in
     * @param warn If true, produce popups if Ruby or Rake are not configured
     *  correctly.
     * @param rakeFile The filename to be run
     * @param displayName The displayname to be shown in the output window
     * @param fileLocator The file locator to be used to resolve output hyperlinks
     * @param rakeParameters Additional parameters to pass to rake
     */
    public void runRake(File pwd, FileObject rakeFile, String displayName,
        FileLocator fileLocator, boolean warn, boolean debug, String... rakeParameters) {
        if (pwd == null) {
            assert rakeFile != null;
            pwd = FileUtil.toFile(rakeFile.getParent());
        }

        if (!RubyInstallation.getInstance().isValidRuby(warn) ||
                !RubyInstallation.getInstance().isValidRake(warn)) {
            return;
        }

        String rake = RubyInstallation.getInstance().getRake();
        ExecutionDescriptor desc;

        List<String> additionalArgs = new ArrayList<String>();

        if (rakeFile != null) {
            additionalArgs.add("-f"); // NOI18N
            additionalArgs.add(FileUtil.toFile(rakeFile).getAbsolutePath());
        }

        if ((rakeParameters != null) && (rakeParameters.length > 0)) {
            for (String parameter : rakeParameters) {
                additionalArgs.add(parameter);
            }
        }
        
        String charsetName = null;
        String classPath = null;
        String extraArgs = null;
        
        if (project != null) {
            PropertyEvaluator evaluator = project.getLookup().lookup(PropertyEvaluator.class);
            if (evaluator != null) {
                charsetName = evaluator.getProperty(SharedRubyProjectProperties.SOURCE_ENCODING);
                classPath = evaluator.getProperty(SharedRubyProjectProperties.JAVAC_CLASSPATH);
                extraArgs = evaluator.getProperty(SharedRubyProjectProperties.RAKE_ARGS);
            }
        }
        
        if (extraArgs != null) {
            String[] args = Utilities.parseParameters(extraArgs);
            if (args != null) {
                for (String arg : args) {
                    additionalArgs.add(arg);
                }
            }
        }

        if (additionalArgs.size() > 0) {
            desc = new ExecutionDescriptor(displayName, pwd, rake).additionalArgs(additionalArgs.toArray(
                        new String[additionalArgs.size()])); // NOI18N
        } else {
            desc = new ExecutionDescriptor(displayName, pwd, rake);
        }

        desc.allowInput();
        desc.classPath(classPath); // Applies only to JRuby
        desc.fileLocator(fileLocator);
        desc.addStandardRecognizers();

        if (test) {
            desc.addOutputRecognizer(new TestNotifier(true, true));
        }
        
        desc.addOutputRecognizer(new RakeErrorRecognizer(desc, charsetName)).debug(debug);

        new RubyExecution(desc, charsetName).run();
    }

    private class RakeErrorRecognizer extends OutputRecognizer implements Runnable {
        private ExecutionDescriptor desc;
        private String charsetName;

        RakeErrorRecognizer(ExecutionDescriptor desc, String charsetName) {
            this.desc = desc;
            this.charsetName = charsetName;
        }

        @Override
        public RecognizedOutput processLine(String line) {
            if (line.indexOf("(See full trace by running task with --trace)") != -1) {
                return new OutputRecognizer.ActionText(new String[] { line }, 
                        new String[] { NbBundle.getMessage(RakeSupport.class, "RerunRakeWithTrace") }, 
                        new Runnable[] { RakeErrorRecognizer.this }, null);
            }

            return null;
        }

        public void run() {
            String[] additionalArgs = desc.getAdditionalArgs();
            if (additionalArgs != null) {
                List<String> args = new ArrayList<String>();
                boolean found = false;
                for (String s : additionalArgs) {
                    args.add(s);
                    if (s.equals("--trace")) {
                        found = true;
                    }
                }
                if (!found) {
                    args.add(0, "--trace");
                }
                desc.additionalArgs(args.toArray(new String[args.size()]));
            } else {
                desc.additionalArgs("--trace");
            }
            new RubyExecution(desc, charsetName).run();
        }
    }
}
