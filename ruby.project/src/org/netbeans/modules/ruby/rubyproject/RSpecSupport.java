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

import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors.FileLocator;
import org.netbeans.modules.ruby.platform.execution.RubyExecutionDescriptor;
import org.netbeans.modules.ruby.platform.execution.RubyProcessCreator;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.netbeans.spi.project.ActionProvider;
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

    public RSpecSupport(final Project project) {
        this.project = project;
    }

    public static boolean hasRSpecInstalled(final RubyPlatform platform) {
        return getLatestVersion(platform.getGemManager()) != null;
    }
    
    private static String getLatestVersion(final GemManager gemManager) {
        return gemManager == null ? null : gemManager.getLatestVersion(RSPEC_GEM_NAME);
    }
    
    private String getLatestVersion() {
        return getLatestVersion(RubyPlatform.gemManagerFor(project));
    }
    
    public boolean isRSpecInstalled() {
        if (getLatestVersion() != null) {
            return true;
        }

        // Rails plugin
        if (project != null) {
            FileObject projectDir = project.getProjectDirectory();
            if ((projectDir != null) && (projectDir.getFileObject(PLUGIN_SPEC_PATH) != null)) {

                return true;
            }
        }

        return false;
    }

    public static boolean isSpecFile(FileObject fo) {
//        return RubyUtils.isSpecFile(fo);
        if (!fo.getMIMEType().equals(RubyInstallation.RUBY_MIME_TYPE)) {
            return false;
        }

        return fo.getName().endsWith("_spec"); // NOI18N
    }

    private String getSpecBinary() {
        assert isRSpecInstalled();

        GemManager gemManager = RubyPlatform.gemManagerFor(project);

        String version = getLatestVersion();

        if (version != null) {
            String libGemDir = gemManager.getGemHome();

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
                FileObject rspec = projectDir.getFileObject(PLUGIN_SPEC_PATH);

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

        RubyPlatform platform = RubyPlatform.platformFor(project);
        if (!platform.isValid(warn)) {
            return;
        }

        String spec = getSpecBinary();

        if (spec == null) {
            return;
        }

        List<String> additionalArgs = new ArrayList<String>();

        // See if there's a spec.opts to be included
        if (projectDir != null) {
            // First look for a NetBeans-specific options file, in case you want different
            // options when running under the IDE (for example, no --color since the 
            // color escape codes don't work under our terminal)
            FileObject specOpts = projectDir.getFileObject(NETBEANS_SPEC_OPTS);

            if (specOpts == null) {
                specOpts = projectDir.getFileObject(SPEC_OPTS);
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

        RubyExecutionDescriptor desc = null;
        String charsetName = null;
        if (project != null) {
            PropertyEvaluator evaluator = project.getLookup().lookup(PropertyEvaluator.class);
            if (evaluator != null) {
                charsetName = evaluator.getProperty(SharedRubyProjectProperties.SOURCE_ENCODING);
            }

            ActionProvider provider = project.getLookup().lookup(ActionProvider.class);
            if (provider instanceof ScriptDescProvider) { // Lookup ScriptDescProvider directly?
                ScriptDescProvider descProvider = (ScriptDescProvider)provider;
                String target = spec;
                LineConvertor convertor = new TestNotifierLineConvertor(true, true);
                desc = descProvider.getScriptDescriptor(pwd, null/*specFile?*/, target, displayName, project.getLookup(), debug, convertor);
                
                // Override args
                desc.additionalArgs(additionalArgs.toArray(new String[additionalArgs.size()]));
            }
        } else {
            desc = new RubyExecutionDescriptor(platform, displayName, pwd, spec);

            desc. additionalArgs(additionalArgs.toArray(new String[additionalArgs.size()]));
            desc.debug(debug);
            desc.allowInput();
            desc.fileLocator(fileLocator);
            desc.addStandardRecognizers();
            LineConvertor convertor = new TestNotifierLineConvertor(true, true);
            desc.addOutConvertor(convertor);
            desc.addErrConvertor(convertor);

        }
        
        if (desc != null) {
            ExecutionService.newService(new RubyProcessCreator(desc, charsetName), 
                    desc.toExecutionDescriptor(), 
                    displayName).run();
        }
    }
}
