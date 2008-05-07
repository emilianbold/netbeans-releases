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

package org.netbeans.modules.ruby.railsprojects;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.ruby.railsprojects.ui.customizer.RailsProjectProperties;
import org.netbeans.modules.ruby.platform.execution.ExecutionDescriptor;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.NbUtilities;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.platform.RubyExecution;
import org.netbeans.modules.ruby.rubyproject.RakeTargetsAction;
import org.netbeans.modules.ruby.platform.execution.DirectoryFileLocator;
import org.netbeans.modules.ruby.platform.execution.ExecutionService;
import org.netbeans.modules.ruby.platform.execution.RegexpOutputRecognizer;
import org.netbeans.modules.ruby.railsprojects.database.RailsDatabaseConfiguration;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectHelper;
import org.netbeans.modules.ruby.spi.project.support.rake.EditableProperties;
import org.netbeans.modules.ruby.spi.project.support.rake.ProjectGenerator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creates a RailsProject from scratch according to some initial configuration.
 * 
 * @todo Take the "README" file in the Rails project and run it through rdoc and
 *   display in internal HTML viewer?
 */
public class RailsProjectGenerator {
    
    public static final RegexpOutputRecognizer RAILS_GENERATOR =
        new RegexpOutputRecognizer("^   (   create|    force|identical|     skip)\\s+([\\w|/]+\\.\\S+)\\s*$", // NOI18N
            2, -1, -1);

    private RailsProjectGenerator() {}
    
    /**
     * Create a new empty Rails project.
     * 
     * @param data the data needed for creating the project.
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static RakeProjectHelper createProject(RailsProjectCreateData data) throws IOException {
        FileObject dirFO = FileUtil.createFolder(data.getDir());
        RubyPlatform platform = data.getPlatform();
        RailsDatabaseConfiguration railsDb = data.getDatabase();
        // Run Rails to generate the appliation skeleton
        if (data.isCreate()) {
            final String rails = platform.getGemManager().getRails();
            final File railsF = new File(rails);
            final FileObject railsFO = FileUtil.toFileObject(railsF);
            boolean runThroughRuby = railsFO != null ? RubyUtils.isRubyFile(railsFO) : false;

            ExecutionDescriptor desc = null;
            String displayName = NbBundle.getMessage(RailsProjectGenerator.class, "GenerateRails");

            String railsDbArg = railsDb.railsGenerationParam() == null ? null : "--database=" + railsDb.railsGenerationParam();
            File pwd = data.getDir().getParentFile();
            if (runThroughRuby) {
                desc = new ExecutionDescriptor(platform, displayName, pwd, rails);
                if (railsDbArg != null) {
                    desc.additionalArgs(data.getName(), railsDbArg);
                } else {
                    desc.additionalArgs(data.getName());
                }
            } else {
                desc = new ExecutionDescriptor(platform, displayName, pwd, data.getName());
                if (railsDbArg != null) {
                    desc.additionalArgs(railsDbArg);
                }
                desc.cmd(railsF);
            }
            desc.fileLocator(new DirectoryFileLocator(dirFO));
            desc.addOutputRecognizer(RAILS_GENERATOR);
            ExecutionService service = null;
            if (runThroughRuby) {
                service = new RubyExecution(desc);
            } else {
                // Try invoking the Rails script directly (probably a Linux distribution
                // with railties installed and rails is a Unix shell script rather 
                // than a Ruby program)
                service = new ExecutionService(desc);
            }
            Task task = service.run();
            task.waitFinished();
            
            // Precreate a spec directory if it doesn't exist such that my source root will work
            if (platform.getGemManager().getLatestVersion("rspec") != null) { // NOI18N
                File spec = new File(data.getDir(), "spec"); // NOI18N
                if (!spec.exists()) {
                    spec.mkdirs();
                }
            }
            
            dirFO.getFileSystem().refresh(true);

            // TODO - only do this if not creating from existing app?
        }

        RakeProjectHelper h = createProject(dirFO, platform, data); //NOI18N
        
        Project p = ProjectManager.getDefault().findProject(dirFO);
        railsDb.editConfig((RailsProject) p);
        
        ProjectManager.getDefault().saveProject(p);
        
        
        // Install goldspike if the user wants Rails deployment
        if (data.isDeploy()) {
            InstalledFileLocator locator = InstalledFileLocator.getDefault();
            File goldspikeFile = locator.locate("goldspike-1.6.zip", "org.netbeans.modules.ruby.railsprojects", false);
            if (goldspikeFile != null) {
                FileObject fo = FileUtil.toFileObject(goldspikeFile);
                if (fo != null) {
                    NbUtilities.extractZip(fo, p.getProjectDirectory());
                }
            }
        }

        // Run Rake -T silently to determine the available targets and write into private area
        RakeTargetsAction.refreshTargets(p);
        
        return h;
    }
    

    private static RakeProjectHelper createProject(FileObject dirFO, final RubyPlatform platform, RailsProjectCreateData createData) throws IOException {

        RakeProjectHelper h = ProjectGenerator.createProject(dirFO, RailsProjectType.TYPE);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(RailsProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(createData.getName()));
        data.appendChild(nameEl);
        
        // set the target server
        EditableProperties privateProperties = h.getProperties(RakeProjectHelper.PRIVATE_PROPERTIES_PATH);
        privateProperties.put(RailsProjectProperties.RAILS_SERVERTYPE, createData.getServerInstanceId());
        
        EditableProperties ep = h.getProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH);
        
        
        ep.setProperty(RailsProjectProperties.RAILS_PORT, "3000"); // NOI18N

        Charset enc = FileEncodingQuery.getDefaultEncoding();
        ep.setProperty(RailsProjectProperties.SOURCE_ENCODING, enc.name());
        
        h.putPrimaryConfigurationData(data, true);
        RailsProjectProperties.storePlatform(ep, platform);
        
        h.putProperties(RakeProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties);
        h.putProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH, ep);        
        return h;
    }

}


