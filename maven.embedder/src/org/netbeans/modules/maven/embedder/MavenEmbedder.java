/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.embedder;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.Maven;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.DefaultMavenExecutionResult;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequestPopulationException;
import org.apache.maven.execution.MavenExecutionRequestPopulator;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.lifecycle.LifecycleExecutor;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.io.ModelReader;
import org.apache.maven.model.io.ModelWriter;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.project.DefaultProjectBuilder;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public final class MavenEmbedder {
    public static final String userHome = System.getProperty( "user.home" );
    public static final File userMavenConfigurationHome = new File( userHome, ".m2" );
    public static final File defaultUserLocalRepository = new File( userMavenConfigurationHome, "repository" );
    public static final File DEFAULT_USER_SETTINGS_FILE = new File( userMavenConfigurationHome, "settings.xml" );
    public static final File DEFAULT_GLOBAL_SETTINGS_FILE =
        new File( System.getProperty( "maven.home", System.getProperty( "user.dir", "" ) ), "conf/settings.xml" );



    private final PlexusContainer plexus;
    private final Maven maven;
    private final ProjectBuilder projectBuilder;
    private final ModelReader modelReader;
    private final ModelWriter modelWriter;
    private final RepositorySystem repositorySystem;
    private final MavenExecutionRequestPopulator populator;
    private final SettingsBuilder settingsBuilder;
    private final LifecycleExecutor lifecycleExecutor;
    private final BuildPluginManager pluginManager;
    private final EmbedderConfiguration request;

    MavenEmbedder(EmbedderConfiguration req) throws ComponentLookupException {
        request = req;
        plexus = req.getContainer();
        this.maven = plexus.lookup(Maven.class);
        this.projectBuilder = plexus.lookup(ProjectBuilder.class);
        this.modelReader = plexus.lookup(ModelReader.class);
        this.modelWriter = plexus.lookup(ModelWriter.class);
        this.repositorySystem = plexus.lookup(RepositorySystem.class);
        this.settingsBuilder = plexus.lookup(SettingsBuilder.class);
        this.populator = plexus.lookup(MavenExecutionRequestPopulator.class);
        this.pluginManager = plexus.lookup(BuildPluginManager.class);
        this.lifecycleExecutor = plexus.lookup(LifecycleExecutor.class);
    }

    public PlexusContainer getPlexusContainer() {
        return plexus;
    }

    public MavenExecutionResult readProject(MavenExecutionRequest request) {
        File pomFile = request.getPom();
        MavenExecutionResult result = new DefaultMavenExecutionResult();
        try {
            populator.populateDefaults(request);
            ProjectBuildingRequest configuration = request.getProjectBuildingRequest();
            configuration.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
            ProjectBuildingResult projectBuildingResult = projectBuilder.build(pomFile, configuration);
            result.setProject(projectBuildingResult.getProject());
            result.setArtifactResolutionResult(projectBuildingResult.getArtifactResolutionResult());
        } catch (ProjectBuildingException ex) {
            return result.addException(ex);
        } catch (MavenExecutionRequestPopulationException ex) {
            return result.addException(ex);
        }
        return result;
    }

    private String getLocalRepositoryPath() {
        if (request.getLocalRepository() != null) {
            return request.getLocalRepository().getAbsolutePath();
        }
        return getSettings().getLocalRepository();
    }

    public ArtifactRepository getLocalRepository() {
        try {
            String localRepositoryPath = getLocalRepositoryPath();
            if (localRepositoryPath != null) {
                return repositorySystem.createLocalRepository(new File(localRepositoryPath));
            }
            return repositorySystem.createLocalRepository(RepositorySystem.defaultUserLocalRepository);
        } catch (InvalidRepositoryException ex) {
            // can't happen
            throw new IllegalStateException(ex);
        }
    }

  public Settings getSettings() {
    SettingsBuildingRequest req = new DefaultSettingsBuildingRequest();
    req.setGlobalSettingsFile(DEFAULT_GLOBAL_SETTINGS_FILE);
    req.setUserSettingsFile(DEFAULT_USER_SETTINGS_FILE);
    req.setSystemProperties(request.getSystemProperties());
    try {
      return settingsBuilder.build(req).getEffectiveSettings();
    } catch(SettingsBuildingException ex) {
        Exceptions.printStackTrace(ex);
        return new Settings();
    }
  }

// Can this be removed?
//    public SettingsValidationResult validateSettings(File settingsFile) {
//        SettingsValidationResult result = new SettingsValidationResult();
//        if (settingsFile != null) {
//            if (settingsFile.canRead()) {
//                @SuppressWarnings("unchecked")
//                List<String> messages = settingsBuilder.validateSettings(settingsFile).getMessages();
//                for (String message : messages) {
//                    result.addMessage(message);
//                }
//            } else {
//                result.addMessage("Can not read settings file " + settingsFile.getAbsolutePath());
//            }
//        }
//
//        return result;
//    }

    public MavenExecutionResult readProjectWithDependencies(MavenExecutionRequest req) {
        File pomFile = req.getPom();
        MavenExecutionResult result = new DefaultMavenExecutionResult();
        try {
            populator.populateDefaults(req);
            ProjectBuildingRequest configuration = req.getProjectBuildingRequest();
            configuration.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
            configuration.setResolveDependencies(true);
            ProjectBuildingResult projectBuildingResult = projectBuilder.build(pomFile, configuration);
            result.setProject(projectBuildingResult.getProject());
            result.setArtifactResolutionResult(projectBuildingResult.getArtifactResolutionResult());
        } catch (ProjectBuildingException ex) {
            //don't add the exception here. this should come out as a build marker, not fill
            //the error logs with msgs
            return result.addException(ex);
        } catch (MavenExecutionRequestPopulationException ex) {
            return result.addException(ex);
        }
        return result;
    }

    //TODO maybe remove in favour of the Request one
    public MavenProject readProject(File fallback) {
        try {
          MavenExecutionRequest req = new DefaultMavenExecutionRequest();
          populator.populateDefaults(req);
          ProjectBuildingRequest configuration = req.getProjectBuildingRequest();
          configuration.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
          return projectBuilder.build(fallback, configuration).getProject();
        } catch(ProjectBuildingException ex) {
            Exceptions.printStackTrace(ex);
            return new MavenProject();
        } catch(MavenExecutionRequestPopulationException ex) {
            Exceptions.printStackTrace(ex);
            return new MavenProject();
        }
    }

    public Artifact createArtifactWithClassifier(String groupId, String artifactId, String version, String type, String classifier) {
        return repositorySystem.createArtifactWithClassifier(groupId, artifactId, version, type, classifier);
    }
    
    public Artifact createArtifact(String groupId, String artifactId, String version, String extension, String type) {
        return repositorySystem.createArtifact(groupId, artifactId, version, type);
    }

    public void resolve(Artifact sources, List<ArtifactRepository> remoteRepositories, ArtifactRepository localRepository) throws ArtifactResolutionException, ArtifactNotFoundException{

        ArtifactResolutionRequest req = new ArtifactResolutionRequest();
        req.setLocalRepository(localRepository);
        req.setRemoteRepositories(remoteRepositories);
        req.setArtifact(sources);
        //TODO
//        req.setTransferListener(createArtifactTransferListener(monitor));

        ArtifactResolutionResult result = repositorySystem.resolve(req);

//        setLastUpdated(localRepository, req.getRemoteRepositories(), sources);
    }


    //TODO possibly rename.. build sounds like something else..
    public ProjectBuildingResult buildProject(Artifact art, ProjectBuildingRequest req) throws ProjectBuildingException {
        try {
            DefaultProjectBuilder bldr = getPlexusContainer().lookup(DefaultProjectBuilder.class);
            if (req.getLocalRepository() == null) {
                req.setLocalRepository(getLocalRepository());
            }
            //TODO some default population of request?
            req.setProcessPlugins(false);
            
            return bldr.build(art, req);
        } catch (ComponentLookupException ex) {
//            Exceptions.printStackTrace(ex);
            throw new ProjectBuildingException(art.getId(), "Component lookup failed", ex);
        }
    }

    public List<String> getLifecyclePhases() {
        ///TODO
        return Arrays.asList(new String[] {
            "phase1",
            "phase2",
            "phasefoo",
            "changeme"
        });
    }

    public MavenExecutionResult execute(MavenExecutionRequest req) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    

}
