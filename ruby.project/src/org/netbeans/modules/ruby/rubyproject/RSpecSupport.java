/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.ruby.rubyproject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;

import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.rubyproject.api.RubyExecution;
import org.netbeans.modules.ruby.rubyproject.execution.ExecutionDescriptor;
import org.netbeans.modules.ruby.rubyproject.execution.FileLocator;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 * Various methods for supporting RSpec execution
 * 
 * @todo Use an output recognizer which munges the output... Can it recognize the
 *   colors and do something to make that happen in the output window?
 * 
 * @author Tor Norbye
 */
public class RSpecSupport {
    private static final String PLUGIN_SPEC_PATH = "vendor/plugins/rspec/bin/spec"; // NOI18N
    private static final String SPEC_OPTS = "spec/spec.opts"; // NOI18N
    private static final String NETBEANS_SPEC_OPTS = SPEC_OPTS + ".netbeans"; // NOI18N
    private static final String RSPEC_GEM_NAME = "rspec"; // NOI18N
    private final Project project;

    public RSpecSupport(Project project) {
        this.project = project;
    }
    
    public boolean isRSpecInstalled() {
        RubyInstallation install = RubyInstallation.getInstance();

        if (install.getVersion(RSPEC_GEM_NAME) != null) { // NOI18N

            return true;
        }

        // Rails plugin
        if (project != null) {
            FileObject projectDir = project.getProjectDirectory();
            if ((projectDir != null) && (projectDir.getFileObject(PLUGIN_SPEC_PATH) != null)) { // NOI18N

                return true;
            }
        }

        return false;
    }

    public static boolean isSpecFile(FileObject fo) {
        if (!fo.getMIMEType().equals(RubyInstallation.RUBY_MIME_TYPE)) {
            return false;
        }

        return fo.getName().endsWith("_spec"); // NOI18N
    }

    private String getSpecBinary() {
        assert isRSpecInstalled();

        RubyInstallation install = RubyInstallation.getInstance();
        String version = install.getVersion(RSPEC_GEM_NAME); // NOI18N

        if (version != null) {
            String libGemDir = install.getRubyLibGemDir();

            if (libGemDir != null) {
                File gemDir = new File(libGemDir, "gems"); // NOI18N

                if (gemDir.exists()) {
                    File rspec =
                        new File(gemDir,
                            "rspec-" + version + File.separator + "bin" + File.separator + "spec"); // NOI18N

                    if (rspec.exists()) {
                        return rspec.getAbsolutePath();
                    }
                }
            }
        }

        // Rails plugin
        if (project != null) {
            FileObject projectDir = project.getProjectDirectory();
            if (projectDir != null) {
                FileObject rspec = projectDir.getFileObject(PLUGIN_SPEC_PATH); // NOI18N

                if (rspec != null) {
                    return FileUtil.toFile(rspec).getAbsolutePath();
                }
            }
        }

        return null;
    }

    /**
     * Run rspec on the given specfile.
     * (If you pass null as the directory, the project directory will be used, and if not set,
     * the directory containing the spec file.)
     * @param warn If true, produce popups if Ruby or RSpec are not configured
     *  correctly.
     */
    public void runRSpec(File pwd, FileObject specFile, String displayName,
        FileLocator fileLocator, boolean warn, boolean debug, String... parameters) {
        runRSpec(pwd, specFile, -1, displayName, fileLocator, warn, debug, parameters);
    }

    /**
     * Run rspec on the given specfile.
     * (If you pass null as the directory, the project directory will be used, and if not set,
     * the directory containing the spec file.)
     * @param lineNumber if not -1, run the spec at the given line
     * @param warn If true, produce popups if Ruby or RSpec are not configured
     *  correctly.
     */
    public void runRSpec(File pwd, FileObject specFile, int lineNumber, String displayName,
        FileLocator fileLocator, boolean warn, boolean debug, String... parameters) {
        FileObject projectDir = null;
        if (project != null) {
            projectDir = project.getProjectDirectory();
        }
        if (pwd == null) {
            FileObject pfo = (projectDir != null) ? projectDir : specFile.getParent();
            pwd = FileUtil.toFile(pfo);
        }

        if (!RubyInstallation.getInstance().isValidRuby(warn)) {
            return;
        }

        String spec = getSpecBinary();

        if (spec == null) {
            return;
        }

        ExecutionDescriptor desc;

        List<String> additionalArgs = new ArrayList<String>();

        // See if there's a spec.opts to be included
        if (projectDir != null) {
            // First look for a NetBeans-specific options file, in case you want different
            // options when running under the IDE (for example, no --color since the 
            // color escape codes don't work under our terminal)
            FileObject specOpts = projectDir.getFileObject(NETBEANS_SPEC_OPTS); // NOI18N

            if (specOpts == null) {
                specOpts = projectDir.getFileObject(SPEC_OPTS); // NOI18N
            }

            if (specOpts != null) {
                additionalArgs.add("--options"); // NOI18N
                additionalArgs.add(FileUtil.toFile(specOpts).getAbsolutePath());
            }
        }
        
        if (lineNumber != -1) {
            additionalArgs.add("--line");
            additionalArgs.add(Integer.toString(lineNumber));
        }

        if ((parameters != null) && (parameters.length > 0)) {
            for (String parameter : parameters) {
                additionalArgs.add(parameter);
            }
        }

        additionalArgs.add(FileUtil.toFile(specFile).getAbsolutePath());

        String charsetName = null;
        String classPath = null;
        
        if (project != null) {
            PropertyEvaluator evaluator = project.getLookup().lookup(PropertyEvaluator.class);
            if (evaluator != null) {
                charsetName = evaluator.getProperty(SharedRubyProjectProperties.SOURCE_ENCODING);
                classPath = evaluator.getProperty(SharedRubyProjectProperties.JAVAC_CLASSPATH);
            }
        }
        
        desc = new ExecutionDescriptor(displayName, pwd, spec).additionalArgs(additionalArgs.toArray(
                    new String[additionalArgs.size()])); // NOI18N

        desc.debug(debug);
        desc.allowInput();
        desc.classPath(classPath); // Applies only to JRuby
        desc.fileLocator(fileLocator);
        desc.addStandardRecognizers();
        desc.addOutputRecognizer(new TestNotifier(true, true));
        new RubyExecution(desc, charsetName).run();
    }
}
