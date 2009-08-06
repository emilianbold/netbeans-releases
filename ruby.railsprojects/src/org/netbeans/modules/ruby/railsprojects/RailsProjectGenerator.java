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

package org.netbeans.modules.ruby.railsprojects;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.ruby.railsprojects.ui.customizer.RailsProjectProperties;
import org.netbeans.modules.ruby.platform.execution.RubyExecutionDescriptor;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.platform.execution.DirectoryFileLocator;
import org.netbeans.modules.ruby.platform.execution.RubyProcessCreator;
import org.netbeans.modules.ruby.railsprojects.database.RailsDatabaseConfiguration;
import org.netbeans.modules.ruby.railsprojects.server.ServerRegistry;
import org.netbeans.modules.ruby.railsprojects.server.spi.RubyInstance;
import org.netbeans.modules.ruby.rubyproject.Util;
import org.netbeans.modules.ruby.rubyproject.rake.RakeSupport;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectHelper;
import org.netbeans.modules.ruby.spi.project.support.rake.ProjectGenerator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creates a RailsProject from scratch according to some initial configuration.
 * 
 * @todo Take the "README" file in the Rails project and run it through rdoc and
 *   display in internal HTML viewer?
 */
public class RailsProjectGenerator {

    private static final Logger LOGGER = Logger.getLogger(RailsProjectGenerator.class.getName());

    static final Pattern RAILS_GENERATOR_PATTERN = Pattern.compile("^   (   create|    force|identical|     skip)\\s+([\\w|/]+\\.\\S+)\\s*$"); // NOI18N

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
            final String rails = platform.getRails();
            final File railsF = new File(rails);
            final FileObject railsFO = FileUtil.toFileObject(railsF);
            boolean runThroughRuby = railsFO != null ? RubyUtils.isRubyFile(railsFO) : false;

            RubyExecutionDescriptor desc = null;
            String displayName = NbBundle.getMessage(RailsProjectGenerator.class, "GenerateRails");

            String railsDbArg = railsDb.railsGenerationParam() == null ? null : "--database=" + railsDb.railsGenerationParam();
            String railsVersionArg = data.getRailsVersion() == null ? null : "_" + data.getRailsVersion() + "_";
            File pwd = data.getDir().getParentFile();
            List<String> argList = new ArrayList<String>();
            if (railsVersionArg != null) {
                argList.add(railsVersionArg);
            }
            if (runThroughRuby) {
                argList.add(data.getName());
            }
            if (railsDbArg != null) {
                argList.add(railsDbArg);
            }
            if (data.getOptions() != null) {
                argList.add(data.getOptions());
            }
            String[] args = argList.toArray(new String[argList.size()]);
            if (runThroughRuby) {
                desc = new RubyExecutionDescriptor(platform, displayName, pwd, rails);
                desc.additionalArgs(args);
            } else {
                desc = new RubyExecutionDescriptor(platform, displayName, pwd, data.getName());
                desc.additionalArgs(args);
                desc.cmd(railsF);
            }

            desc.runThroughRuby(runThroughRuby);
            desc.fileLocator(new DirectoryFileLocator(dirFO));

            LineConvertor convertor = LineConvertors.filePattern(desc.getFileLocator(), RAILS_GENERATOR_PATTERN, null, 2, -1);
            desc.addStandardRecognizers();
            desc.addErrConvertor(convertor);
            desc.addOutConvertor(convertor);

            RubyProcessCreator rpc = new RubyProcessCreator(desc);

            org.netbeans.api.extexecution.ExecutionService es =
                    org.netbeans.api.extexecution.ExecutionService.newService(rpc, desc.toExecutionDescriptor(), displayName);
            try {
                es.run().get();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }

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
        
        
        // Install Warbler as a plugin if the user wants rake tasks for
        // creating .war files
        if (data.isDeploy()) {
            runWarblePluginize(platform, p);
        }

        RakeSupport.refreshTasks(p);
        String railsVersion = data.getRailsVersion() != null
                ? data.getRailsVersion()
                : platform.getGemManager().getLatestVersion("rails"); // NOI18N
        Util.logUsage(RailsProjectGenerator.class, "USG_PROJECT_CREATE_RAILS", // NOI18N
                platform.getInfo().getKind(),
                platform.getInfo().getPlatformVersion(),
                platform.getInfo().getGemVersion(),
                getServerIdForLogging(data.getServerInstanceId(), platform),
                data.getDatabase().getDisplayName(),
                railsVersion);

        return h;
    }

    // see #150975
    private static String getServerIdForLogging(String serverURI, RubyPlatform platform) {
        RubyInstance serverInstance = ServerRegistry.getDefault().getServer(serverURI, platform);
        return serverInstance != null ? serverInstance.getDisplayName() : "";
    }

    private static void runWarblePluginize(RubyPlatform platform, Project project) {
        String warble = platform.findExecutable("warble"); //NOI18N
        if (warble == null) {
            // at this point the rails wizard should have already checked 
            // that warble exists, so just logging
            LOGGER.warning("Could not find warble executable, platform: " + platform);
            return;
        }
        RubyExecutionDescriptor desc = new RubyExecutionDescriptor(platform,
                NbBundle.getMessage(RailsProjectGenerator.class, "WarblePluginize"),
                FileUtil.toFile(project.getProjectDirectory()),
                new File(warble).getAbsolutePath());
        desc.additionalArgs("pluginize"); //NOI18N

        RubyProcessCreator processCreator = new RubyProcessCreator(desc);
        desc.addStandardRecognizers();

        ExecutionService.newService(processCreator,
                desc.toExecutionDescriptor(),
                NbBundle.getMessage(RailsProjectGenerator.class, "WarblePluginize")).run();
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

        RubyInstance instance = ServerRegistry.getDefault().getServer(createData.getServerInstanceId(), platform);
        int port = instance != null ? instance.getRailsPort() : 3000;
        ep.setProperty(RailsProjectProperties.RAILS_PORT, String.valueOf(port));

        Charset enc = FileEncodingQuery.getDefaultEncoding();
        ep.setProperty(RailsProjectProperties.SOURCE_ENCODING, enc.name());

        h.putPrimaryConfigurationData(data, true);
        RailsProjectProperties.storePlatform(ep, platform);

        h.putProperties(RakeProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties);
        h.putProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        return h;
    }

}


