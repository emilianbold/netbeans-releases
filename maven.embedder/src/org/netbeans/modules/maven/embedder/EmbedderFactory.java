/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.maven.embedder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import org.apache.maven.artifact.UnknownRepositoryLayoutException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelBuildingEvent;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingListener;
import org.apache.maven.model.building.ModelCache;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.ModelResolver;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import java.util.prefs.Preferences;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequestPopulator;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingResult;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.component.discovery.ComponentDiscoverer;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryEvent;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryListener;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
<<<<<<< local
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.cli.CommandLineUtils;
=======
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import hidden.org.codehaus.plexus.util.cli.CommandLineUtils;
import java.util.Enumeration;
import java.util.prefs.Preferences;
import org.netbeans.modules.maven.embedder.exec.MyLifecycleExecutor;
import org.netbeans.modules.maven.embedder.exec.NBBuildPlanner;
import org.netbeans.modules.maven.embedder.exec.ProgressTransferListener;
>>>>>>> other
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 *  Factory for creating MavenEmbedder instances for various purposes.
 * 
 * @author mkleint
 */
public final class EmbedderFactory {

    private static MavenEmbedder project;
    private static MavenEmbedder online;
    private static SettingsFileListener fileListener = new SettingsFileListener();
    private static Logger LOG = Logger.getLogger(EmbedderFactory.class.getName());

    public static MavenEmbedder createExecuteEmbedder() {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    /** Creates a new instance of EmbedderFactory */
    private EmbedderFactory() {
    }

    /**
     * embedder seems to cache some values..
     */
    public synchronized static void resetProjectEmbedder() {
    }

    private static void setLocalRepoPreference(EmbedderConfiguration req) {
        Preferences prefs = NbPreferences.root().node("org/netbeans/modules/maven"); //NOI18N
        String localRepo = prefs.get("localRepository", null); //NOI18N
        if (localRepo != null) {
            File file = new File(localRepo);
            if (file.exists() && file.isDirectory()) {
                req.setLocalRepository(file);
            } else if (!file.exists()) {
                file.mkdirs();
                req.setLocalRepository(file);
            }
        }
    }

<<<<<<< local
    public static MavenEmbedder createProjectLikeEmbedder() throws PlexusContainerException {
        final String mavenCoreRealmId = "plexus.core";
        ContainerConfiguration dpcreq = new DefaultContainerConfiguration()
            .setClassWorld( new ClassWorld(mavenCoreRealmId, EmbedderFactory.class.getClassLoader()) )
            .setName("mavenCore");
=======
    private static Properties copySystemProperties() {
        Properties props = new Properties();
        Enumeration <Object> keys = System.getProperties().keys();
        while(keys.hasMoreElements()) {
            Object o = keys.nextElement();
            props.put(o, System.getProperties().get(o));
        }
        return props;
    }
>>>>>>> other

<<<<<<< local

    dpcreq.addComponentDiscoverer(new ComponentDiscoverer() {

      public List<ComponentSetDescriptor> findComponents(Context context, ClassRealm classRealm)
          throws PlexusConfigurationException {

        List<ComponentSetDescriptor> componentSetDescriptors = new ArrayList<ComponentSetDescriptor>();

        if (mavenCoreRealmId.equals(classRealm.getId())) {
          ComponentSetDescriptor componentSetDescriptor = new ComponentSetDescriptor();

//          // register EclipseClassRealmManagerDelegate
//          ComponentDescriptor componentDescriptor = new ComponentDescriptor();
//          componentDescriptor.setRealm(classRealm);
//          componentDescriptor.setRole(ClassRealmManagerDelegate.class.getName());
//          componentDescriptor.setImplementationClass(EclipseClassRealmManagerDelegate.class);
//          ComponentRequirement plexusRequirement = new ComponentRequirement();
//          plexusRequirement.setRole("org.codehaus.plexus.PlexusContainer");
//          plexusRequirement.setFieldName("plexus");
//          componentDescriptor.addRequirement(plexusRequirement );
//          componentSetDescriptor.addComponentDescriptor(componentDescriptor);

//          componentDescriptor = new ComponentDescriptor();
//          componentDescriptor.setRealm(classRealm);
//          componentDescriptor.setRole(LocalRepositoryMaintainer.class.getName());
//          componentDescriptor.setImplementationClass(EclipseLocalRepositoryMaintainer.class);
//          componentSetDescriptor.addComponentDescriptor(componentDescriptor);
//
//          componentSetDescriptors.add(componentSetDescriptor);
        }

        return componentSetDescriptors;
      }

    });

        dpcreq.addComponentDiscoveryListener(new ComponentDiscoveryListener() {
            @SuppressWarnings("unchecked")
            public void componentDiscovered(ComponentDiscoveryEvent event) {
                ComponentSetDescriptor set = event.getComponentSetDescriptor();
                for (ComponentDescriptor desc : set.getComponents()) {
                    if (MavenExecutionRequestPopulator.class.getName().equals(desc.getRole())) {
                        desc.setImplementationClass(NbExecutionRequestPopulator.class);
                    }
                }
            }
        });
        DefaultPlexusContainer dpc = new DefaultPlexusContainer(dpcreq);

        EmbedderConfiguration req = new EmbedderConfiguration();
        req.setContainer(dpc);
=======
    public static MavenEmbedder createProjectLikeEmbedder() {
        Configuration req = new DefaultConfiguration();
        req.setClassLoader(EmbedderFactory.class.getClassLoader());
>>>>>>> other
        setLocalRepoPreference(req);

<<<<<<< local
//        //TODO remove explicit activation
//        req.addActiveProfile("netbeans-public").addActiveProfile("netbeans-private"); //NOI18N
        Properties props = new Properties();
        props.putAll(System.getProperties());
=======
        //TODO remove explicit activation
        req.addActiveProfile("netbeans-public").addActiveProfile("netbeans-private"); //NOI18N
        Properties props = copySystemProperties();
        req.setSystemProperties(fillEnvVars(props));
        File userSettingsPath = MavenEmbedder.DEFAULT_USER_SETTINGS_FILE;
        File globalSettingsPath = InstalledFileLocator.getDefault().locate("maven2/settings.xml", "org.netbeans.modules.maven.embedder", false); //NOI18N

        //validating  Configuration
        ConfigurationValidationResult cvr = MavenEmbedder.validateConfiguration(req);
        Exception userSettingsException = cvr.getUserSettingsException();
        if (userSettingsException != null) {
            Exceptions.printStackTrace(Exceptions.attachMessage(userSettingsException,
                    "Maven Settings file cannot be properly parsed. Until it's fixed, it will be ignored."));
        }
        if (cvr.isValid()) {
            req.setUserSettingsFile(userSettingsPath);
        } else {
            LOG.info("Maven settings file is corrupted. See http://www.netbeans.org/issues/show_bug.cgi?id=96919"); //NOI18N
            req.setUserSettingsFile(globalSettingsPath);
        }

        req.setGlobalSettingsFile(globalSettingsPath);
        req.setMavenEmbedderLogger(new NullEmbedderLogger());
        req.setConfigurationCustomizer(new ContainerCustomizer() {

            public void customize(PlexusContainer plexusContainer) {
                ComponentDescriptor desc = plexusContainer.getComponentDescriptor(ArtifactFactory.ROLE);
                desc.setImplementation(NbArtifactFactory.class.getName()); //NOI18N

                desc = plexusContainer.getComponentDescriptor("org.apache.maven.extension.ExtensionManager");
                desc.setImplementation(NbExtensionManager.class.getName()); //NOI18N

                desc = plexusContainer.getComponentDescriptor("org.apache.maven.workspace.MavenWorkspaceStore");
                desc.setImplementation(NbMavenWorkspaceStore.class.getName()); //NOI18N

                desc = plexusContainer.getComponentDescriptor(ArtifactResolver.ROLE);
                desc.setImplementation(NbArtifactResolver.class.getName()); //NOI18N

                desc = plexusContainer.getComponentDescriptor(WagonManager.ROLE);
                desc.setImplementation(NbWagonManager.class.getName()); //NOI18N

                //MEVENIDE-634
                desc = plexusContainer.getComponentDescriptor(KnownHostsProvider.ROLE, "file"); //NOI18N
                desc.getConfiguration().getChild("hostKeyChecking").setValue("no"); //NOI18N

                //MEVENIDE-634
                desc = plexusContainer.getComponentDescriptor(KnownHostsProvider.ROLE, "null"); //NOI18N
                desc.getConfiguration().getChild("hostKeyChecking").setValue("no"); //NOI18N
                }
        });
        MavenEmbedder embedder = null;
        try {
            embedder = new MavenEmbedder(req);
            try {
                //MEVENIDE-634 make all instances non-interactive
                WagonManager wagonManager = (WagonManager) embedder.getPlexusContainer().lookup(WagonManager.ROLE);
                wagonManager.setInteractive(false);
            } catch (ComponentLookupException ex) {
                ErrorManager.getDefault().notify(ex);
            }

        } catch (MavenEmbedderException e) {
            ErrorManager.getDefault().notify(e);
        }
        return embedder;
    }


    public synchronized static MavenEmbedder getProjectEmbedder() /*throws MavenEmbedderException*/ {
        MavenEmbedder projectEmbedder;
        //since yarda's introduction of lazy project loading, mutliple threads use the embedder at startup at once.
        // that breaks embedder/plexus in many ways..
        // introducing ThreadLocal project embedder instance to workaround the problem
        // 
        if (!wasReset) {
            projectEmbedder = projectTL.get();
        } else {
            projectEmbedder = project;
            projectTL.remove();
        }
        if (projectEmbedder == null) {
            MavenEmbedder embedder = createProjectLikeEmbedder();
            if (!wasReset) {
                projectTL.set(embedder);
            } else {
                project = embedder;
            }
            projectEmbedder = embedder;
        }
        return projectEmbedder;
    }

    public synchronized static MavenEmbedder getOnlineEmbedder() {
        return createOnlineEmbedder();
//        if (online == null) {
//            online = createOnlineEmbedder();
//        }
//        return online;

    }

    /*public*/ static MavenEmbedder createOnlineEmbedder() {
        Configuration req = new DefaultConfiguration();
        req.setClassLoader(EmbedderFactory.class.getClassLoader());
        setLocalRepoPreference(req);

        //TODO remove explicit activation
        req.addActiveProfile("netbeans-public").addActiveProfile("netbeans-private"); //NOI18N

        File userSettingsPath = MavenEmbedder.DEFAULT_USER_SETTINGS_FILE;
        File globalSettingsPath = InstalledFileLocator.getDefault().locate("maven2/settings.xml", "org.netbeans.modules.maven.embedder", false); //NOI18N

        //validating  Configuration
        ConfigurationValidationResult cvr = MavenEmbedder.validateConfiguration(req);
        Exception userSettingsException = cvr.getUserSettingsException();
        if (userSettingsException != null) {
            Exceptions.printStackTrace(Exceptions.attachMessage(userSettingsException,
                    "Maven Settings file cannot be properly parsed. Until it's fixed, it will be ignored."));
        }
        if (cvr.isValid()) {
            req.setUserSettingsFile(userSettingsPath);
        } else {
            LOG.info("Maven settings file is corrupted. See http://www.netbeans.org/issues/show_bug.cgi?id=96919"); //NOI18N

            req.setUserSettingsFile(globalSettingsPath);
        }
        req.setGlobalSettingsFile(globalSettingsPath);
        
        Properties props = copySystemProperties();
>>>>>>> other
        req.setSystemProperties(fillEnvVars(props));
        
        File userSettingsPath = MavenEmbedder.DEFAULT_USER_SETTINGS_FILE;
        File globalSettingsPath = InstalledFileLocator.getDefault().locate("maven2/settings.xml", null, false); //NOI18N

//        //validating  Configuration
//        ConfigurationValidationResult cvr = MavenEmbedder.validateConfiguration(req);
//        Exception userSettingsException = cvr.getUserSettingsException();
//        if (userSettingsException != null) {
//            Exceptions.printStackTrace(Exceptions.attachMessage(userSettingsException,
//                    "Maven Settings file cannot be properly parsed. Until it's fixed, it will be ignored."));
//        }
//        if (cvr.isValid()) {
//            req.setUserSettingsFile(userSettingsPath);
//        } else {
//            LOG.info("Maven settings file is corrupted. See http://www.netbeans.org/issues/show_bug.cgi?id=96919"); //NOI18N
//            req.setUserSettingsFile(globalSettingsPath);
//        }
//
//        req.setGlobalSettingsFile(globalSettingsPath);
//        req.setMavenEmbedderLogger(new NullEmbedderLogger());
//        req.setConfigurationCustomizer(new ContainerCustomizer() {
//
//            public void customize(PlexusContainer plexusContainer) {
//                ComponentDescriptor desc = plexusContainer.getComponentDescriptor(ArtifactFactory.ROLE);
//                desc.setImplementation(NbArtifactFactory.class.getName()); //NOI18N
//
//                desc = plexusContainer.getComponentDescriptor("org.apache.maven.extension.ExtensionManager");
//                desc.setImplementation(NbExtensionManager.class.getName()); //NOI18N
//
//                desc = plexusContainer.getComponentDescriptor("org.apache.maven.workspace.MavenWorkspaceStore");
//                desc.setImplementation(NbMavenWorkspaceStore.class.getName()); //NOI18N
//
//                desc = plexusContainer.getComponentDescriptor(ArtifactResolver.ROLE);
//                desc.setImplementation(NbArtifactResolver.class.getName()); //NOI18N
//
//                desc = plexusContainer.getComponentDescriptor(WagonManager.ROLE);
//                desc.setImplementation(NbWagonManager.class.getName()); //NOI18N
//
//                //MEVENIDE-634
//                desc = plexusContainer.getComponentDescriptor(KnownHostsProvider.ROLE, "file"); //NOI18N
//                desc.getConfiguration().getChild("hostKeyChecking").setValue("no"); //NOI18N
//
//                //MEVENIDE-634
//                desc = plexusContainer.getComponentDescriptor(KnownHostsProvider.ROLE, "null"); //NOI18N
//                desc.getConfiguration().getChild("hostKeyChecking").setValue("no"); //NOI18N
//                }
//        });
        MavenEmbedder embedder = null;
        try {
            embedder = new MavenEmbedder(req);
            //MEVENIDE-634 make all instances non-interactive
//            WagonManager wagonManager = (WagonManager) embedder.getPlexusContainer().lookup(WagonManager.ROLE);
//            wagonManager.setInteractive(false);
        } catch (ComponentLookupException ex) {
            ErrorManager.getDefault().notify(ex);
        }

        return embedder;
    }


<<<<<<< local
    public synchronized static MavenEmbedder getProjectEmbedder() /*throws MavenEmbedderException*/ {
        if (project == null) {
            try {
                project = createProjectLikeEmbedder();
            } catch (PlexusContainerException ex) {
                Exceptions.printStackTrace(ex);
=======
        ClassWorld world = new ClassWorld();
        File rootPackageFolder = InstalledFileLocator.getDefault().locate("maven2/rootpackage", null, false); //NOI18N
        if (rootPackageFolder != null) {
            rootPackageFolder = FileUtil.normalizeFile(rootPackageFolder);
        }
        // kind of separation layer between the netbeans classloading world and maven classworld.
        try {
            ClassRealm nbRealm = world.newRealm("netbeans", loader); //NOI18N
            //MEVENIDE-647
            ClassRealm plexusRealm = world.newRealm("plexus.core", loader.getParent()); //NOI18N
            //loader.getParent() contains rt.jar+tools.jar (what's what we want) but also openide.modules, openide.util and startup (that's what we don't want but probably can live with)

            // these are all packages that are from the embedder jar..
            plexusRealm.importFrom(nbRealm.getId(), "org.codehaus.doxia"); //NOI18N
            plexusRealm.importFrom(nbRealm.getId(), "org.codehaus.plexus"); //NOI18N
            plexusRealm.importFrom(nbRealm.getId(), "org.codehaus.classworlds"); //NOI18N
            plexusRealm.importFrom(nbRealm.getId(), "org.apache.maven"); //NOI18N
            plexusRealm.importFrom(nbRealm.getId(), "org.apache.commons"); //NOI18N
            plexusRealm.importFrom(nbRealm.getId(), "org.apache.log4j"); //NOI18N
            plexusRealm.importFrom(nbRealm.getId(), "org.apache.xbean"); //NOI18N
            plexusRealm.importFrom(nbRealm.getId(), "org.apache.xerces"); //NOI18N
            plexusRealm.importFrom(nbRealm.getId(), "META-INF/maven"); //NOI18N
            plexusRealm.importFrom(nbRealm.getId(), "META-INF/plexus"); //NOI18N
            plexusRealm.importFrom(nbRealm.getId(), "com.jcraft.jsch"); //NOI18N
            plexusRealm.importFrom(nbRealm.getId(), "org.aspectj"); //NOI18N
            plexusRealm.importFrom(nbRealm.getId(), "org.cyberneko"); //NOI18N
            plexusRealm.importFrom(nbRealm.getId(), "org.easymock"); //NOI18N
            plexusRealm.importFrom(nbRealm.getId(), "hidden.org.codehaus.plexus"); //NOI18N

            // from netbeans allow just Lookup and the mevenide bridges
            plexusRealm.importFrom(nbRealm.getId(), "org.openide.util"); //NOI18N
            plexusRealm.importFrom(nbRealm.getId(), "org.netbeans.modules.maven.bridges"); //NOI18N
            //have custom lifecycle executor to collect all projects in reactor..
            plexusRealm.importFrom(nbRealm.getId(), "org.netbeans.modules.maven.embedder.exec"); //NOI18N

            if (rootPackageFolder != null) { //#154108 well, the broken embedder is more broken in jnlp based netbeans..
                //hack to enable reports, default package is EVIL!
                plexusRealm.addURL(rootPackageFolder.toURI().toURL());
            }
        } catch (NoSuchRealmException ex) {
            ex.printStackTrace();
        } catch (DuplicateRealmException ex) {
            ex.printStackTrace();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        Configuration req = new DefaultConfiguration();
        req.setClassWorld(world);
        req.setMavenEmbedderLogger(logger);
        setLocalRepoPreference(req);

        //TODO remove explicit activation
        req.addActiveProfile("netbeans-public").addActiveProfile("netbeans-private"); //NOI18N
        File userSettingsPath = MavenEmbedder.DEFAULT_USER_SETTINGS_FILE; //NOI18N
        File globalSettingsPath = InstalledFileLocator.getDefault().locate("maven2/settings.xml", "org.netbeans.modules.maven.embedder", false); //NOI18N
        
        //validating  Configuration
        ConfigurationValidationResult cvr = MavenEmbedder.validateConfiguration(req);
        Exception userSettingsException = cvr.getUserSettingsException();
        if (userSettingsException != null) {
            Exceptions.printStackTrace(Exceptions.attachMessage(userSettingsException,
                    "Maven Settings file cannot be properly parsed. Until it's fixed, it will be ignored."));
        }
        if (userSettingsPath.exists()) {
            if (cvr.isValid()) {
                req.setUserSettingsFile(userSettingsPath);
            } else {
                LOG.info("Maven settings file is corrupted. See http://www.netbeans.org/issues/show_bug.cgi?id=96919"); //NOI18N
                req.setUserSettingsFile(globalSettingsPath);
>>>>>>> other
            }
        }
        return project;
    }

    public synchronized static MavenEmbedder getOnlineEmbedder() {
        if (online == null) {
            try {
                online = createOnlineEmbedder();
            } catch (PlexusContainerException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return online;

    }

    /*public*/ static MavenEmbedder createOnlineEmbedder() throws PlexusContainerException {
        final String mavenCoreRealmId = "plexus.core";
        ContainerConfiguration dpcreq = new DefaultContainerConfiguration()
            .setClassWorld( new ClassWorld(mavenCoreRealmId, EmbedderFactory.class.getClassLoader()) )
            .setName("mavenCore");


    dpcreq.addComponentDiscoverer(new ComponentDiscoverer() {

      public List<ComponentSetDescriptor> findComponents(Context context, ClassRealm classRealm)
          throws PlexusConfigurationException {

        List<ComponentSetDescriptor> componentSetDescriptors = new ArrayList<ComponentSetDescriptor>();

        if (mavenCoreRealmId.equals(classRealm.getId())) {
          ComponentSetDescriptor componentSetDescriptor = new ComponentSetDescriptor();

//          // register EclipseClassRealmManagerDelegate
//          ComponentDescriptor componentDescriptor = new ComponentDescriptor();
//          componentDescriptor.setRealm(classRealm);
//          componentDescriptor.setRole(ClassRealmManagerDelegate.class.getName());
//          componentDescriptor.setImplementationClass(EclipseClassRealmManagerDelegate.class);
//          ComponentRequirement plexusRequirement = new ComponentRequirement();
//          plexusRequirement.setRole("org.codehaus.plexus.PlexusContainer");
//          plexusRequirement.setFieldName("plexus");
//          componentDescriptor.addRequirement(plexusRequirement );
//          componentSetDescriptor.addComponentDescriptor(componentDescriptor);

//          componentDescriptor = new ComponentDescriptor();
//          componentDescriptor.setRealm(classRealm);
//          componentDescriptor.setRole(LocalRepositoryMaintainer.class.getName());
//          componentDescriptor.setImplementationClass(EclipseLocalRepositoryMaintainer.class);
//          componentSetDescriptor.addComponentDescriptor(componentDescriptor);
//
//          componentSetDescriptors.add(componentSetDescriptor);
        }

        return componentSetDescriptors;
      }

    });

        dpcreq.addComponentDiscoveryListener(new ComponentDiscoveryListener() {
            @SuppressWarnings("unchecked")
            public void componentDiscovered(ComponentDiscoveryEvent event) {
                ComponentSetDescriptor set = event.getComponentSetDescriptor();
                for (ComponentDescriptor desc : set.getComponents()) {
                    if (MavenExecutionRequestPopulator.class.getName().equals(desc.getRole())) {
                        desc.setImplementationClass(NbExecutionRequestPopulator.class);
                    }
                }
            }
        });
        DefaultPlexusContainer dpc = new DefaultPlexusContainer(dpcreq);

        EmbedderConfiguration req = new EmbedderConfiguration();
        req.setContainer(dpc);
        setLocalRepoPreference(req);

//        //TODO remove explicit activation
//        req.addActiveProfile("netbeans-public").addActiveProfile("netbeans-private"); //NOI18N
        Properties props = new Properties();
        props.putAll(System.getProperties());
        req.setSystemProperties(fillEnvVars(props));


//        req.setConfigurationCustomizer(new ContainerCustomizer() {
//
//            public void customize(PlexusContainer plexusContainer) {
//                    //MEVENIDE-634
//                    ComponentDescriptor desc = plexusContainer.getComponentDescriptor(KnownHostsProvider.ROLE, "file"); //NOI18N
//                    desc.getConfiguration().getChild("hostKeyChecking").setValue("no"); //NOI18N
//
//                    //MEVENIDE-634
//                    desc = plexusContainer.getComponentDescriptor(KnownHostsProvider.ROLE, "null"); //NOI18N
//                    desc.getConfiguration().getChild("hostKeyChecking").setValue("no"); //NOI18N
//            }
//        });

        MavenEmbedder embedder = null;
        try {
            embedder = new MavenEmbedder(req);
            //MEVENIDE-634 make all instances non-interactive
//            WagonManager wagonManager = (WagonManager) embedder.getPlexusContainer().lookup(WagonManager.ROLE);
//            wagonManager.setInteractive(false);
        } catch (ComponentLookupException ex) {
            ErrorManager.getDefault().notify(ex);
        }
//            try {
//                //MEVENIDE-634 make all instances non-interactive
//                WagonManager wagonManager = (WagonManager) embedder.getPlexusContainer().lookup(WagonManager.ROLE);
//                wagonManager.setInteractive( false );
//                wagonManager.setDownloadMonitor(new ProgressTransferListener());
//            } catch (ComponentLookupException ex) {
//                ErrorManager.getDefault().notify(ex);
//            }

        return embedder;
    }
//
//    public static MavenEmbedder createExecuteEmbedder(MavenEmbedderLogger logger) /*throws MavenEmbedderException*/ {
//        ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
//
//
//        ClassWorld world = new ClassWorld();
//        File rootPackageFolder = InstalledFileLocator.getDefault().locate("maven2/rootpackage", null, false); //NOI18N
//        if (rootPackageFolder != null) {
//            rootPackageFolder = FileUtil.normalizeFile(rootPackageFolder);
//        }
//        // kind of separation layer between the netbeans classloading world and maven classworld.
//        try {
//            ClassRealm nbRealm = world.newRealm("netbeans", loader); //NOI18N
//            //MEVENIDE-647
//            ClassRealm plexusRealm = world.newRealm("plexus.core", loader.getParent()); //NOI18N
//            //loader.getParent() contains rt.jar+tools.jar (what's what we want) but also openide.modules, openide.util and startup (that's what we don't want but probably can live with)
//
//            // these are all packages that are from the embedder jar..
//            plexusRealm.importFrom(nbRealm.getId(), "org.codehaus.doxia"); //NOI18N
//            plexusRealm.importFrom(nbRealm.getId(), "org.codehaus.plexus"); //NOI18N
//            plexusRealm.importFrom(nbRealm.getId(), "org.codehaus.classworlds"); //NOI18N
//            plexusRealm.importFrom(nbRealm.getId(), "org.apache.maven"); //NOI18N
//            plexusRealm.importFrom(nbRealm.getId(), "org.apache.commons"); //NOI18N
//            plexusRealm.importFrom(nbRealm.getId(), "org.apache.log4j"); //NOI18N
//            plexusRealm.importFrom(nbRealm.getId(), "org.apache.xbean"); //NOI18N
//            plexusRealm.importFrom(nbRealm.getId(), "org.apache.xerces"); //NOI18N
//            plexusRealm.importFrom(nbRealm.getId(), "META-INF/maven"); //NOI18N
//            plexusRealm.importFrom(nbRealm.getId(), "META-INF/plexus"); //NOI18N
//            plexusRealm.importFrom(nbRealm.getId(), "com.jcraft.jsch"); //NOI18N
//            plexusRealm.importFrom(nbRealm.getId(), "org.aspectj"); //NOI18N
//            plexusRealm.importFrom(nbRealm.getId(), "org.cyberneko"); //NOI18N
//            plexusRealm.importFrom(nbRealm.getId(), "org.easymock"); //NOI18N
//            plexusRealm.importFrom(nbRealm.getId(), "hidden.org.codehaus.plexus"); //NOI18N
//
//            // from netbeans allow just Lookup and the mevenide bridges
//            plexusRealm.importFrom(nbRealm.getId(), "org.openide.util"); //NOI18N
//            plexusRealm.importFrom(nbRealm.getId(), "org.netbeans.modules.maven.bridges"); //NOI18N
//            //have custom lifecycle executor to collect all projects in reactor..
//            plexusRealm.importFrom(nbRealm.getId(), "org.netbeans.modules.maven.embedder.exec"); //NOI18N
//
//            if (rootPackageFolder != null) { //#154108 well, the broken embedder is more broken in jnlp based netbeans..
//                //hack to enable reports, default package is EVIL!
//                plexusRealm.addURL(rootPackageFolder.toURI().toURL());
//            }
//        } catch (NoSuchRealmException ex) {
//            ex.printStackTrace();
//        } catch (DuplicateRealmException ex) {
//            ex.printStackTrace();
//        } catch (MalformedURLException ex) {
//            ex.printStackTrace();
//        }
//        Configuration req = new DefaultConfiguration();
//        req.setClassWorld(world);
//        req.setMavenEmbedderLogger(logger);
//        setLocalRepoPreference(req);
//
//        //TODO remove explicit activation
//        req.addActiveProfile("netbeans-public").addActiveProfile("netbeans-private"); //NOI18N
//        File userSettingsPath = MavenEmbedder.DEFAULT_USER_SETTINGS_FILE; //NOI18N
//        File globalSettingsPath = InstalledFileLocator.getDefault().locate("maven2/settings.xml", null, false); //NOI18N
//
//        //validating  Configuration
//        ConfigurationValidationResult cvr = MavenEmbedder.validateConfiguration(req);
//        Exception userSettingsException = cvr.getUserSettingsException();
//        if (userSettingsException != null) {
//            Exceptions.printStackTrace(Exceptions.attachMessage(userSettingsException,
//                    "Maven Settings file cannot be properly parsed. Until it's fixed, it will be ignored."));
//        }
//        if (userSettingsPath.exists()) {
//            if (cvr.isValid()) {
//                req.setUserSettingsFile(userSettingsPath);
//            } else {
//                LOG.info("Maven settings file is corrupted. See http://www.netbeans.org/issues/show_bug.cgi?id=96919"); //NOI18N
//                req.setUserSettingsFile(globalSettingsPath);
//            }
//        }
//
//        req.setGlobalSettingsFile(globalSettingsPath);
//
//        req.setConfigurationCustomizer(new ContainerCustomizer() {
//
//            public void customize(PlexusContainer plexusContainer) {
//                //have custom lifecycle executor to collect all projects in reactor..
//                ComponentDescriptor desc = plexusContainer.getComponentDescriptor(LifecycleExecutor.ROLE);
//                desc.setImplementation(MyLifecycleExecutor.class.getName()); //NOI18N
//                try {
//                    PlexusConfiguration oldConf = desc.getConfiguration();
//                    XmlPlexusConfiguration conf = new XmlPlexusConfiguration(oldConf.getName());
//                    copyConfig(oldConf, conf);
//                    desc.setConfiguration(conf);
//                } catch (PlexusConfigurationException ex) {
//                    ex.printStackTrace();
//                }
//
//                desc = plexusContainer.getComponentDescriptor(BuildPlanner.class.getName());
//                desc.setImplementation(NBBuildPlanner.class.getName()); //NOI18N
//                try {
//                    PlexusConfiguration oldConf = desc.getConfiguration();
//                    XmlPlexusConfiguration conf = new XmlPlexusConfiguration(oldConf.getName());
//                    copyConfig(oldConf, conf);
//                    desc.setConfiguration(conf);
//                } catch (PlexusConfigurationException ex) {
//                    ex.printStackTrace();
//                }
//            }
//        });
//
//        MavenEmbedder embedder = null;
//        try {
//            embedder = new MavenEmbedder(req);
//        } catch (MavenEmbedderException e) {
//            ErrorManager.getDefault().notify(e);
//        }
//        return embedder;
//    }

    public static ArtifactRepository createRemoteRepository(MavenEmbedder embedder, String url, String id) {
        try {
            ArtifactRepositoryFactory fact = (ArtifactRepositoryFactory) embedder.getPlexusContainer().lookup(ArtifactRepositoryFactory.ROLE);
            ArtifactRepositoryPolicy snapshotsPolicy = new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS, ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN);
            ArtifactRepositoryPolicy releasesPolicy = new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS, ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN);
            return fact.createArtifactRepository(id, url, ArtifactRepositoryFactory.DEFAULT_LAYOUT_ID, snapshotsPolicy, releasesPolicy);
        } catch (UnknownRepositoryLayoutException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ComponentLookupException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public static List<Model> createModelLineage(File pom, MavenEmbedder embedder) throws ModelBuildingException {
        //TODO
        ModelBuilder mb;
        try {
            mb = embedder.getPlexusContainer().lookup(ModelBuilder.class);
        } catch (ComponentLookupException ex) {
            Exceptions.printStackTrace(ex);
            return Collections.<Model>emptyList();
        }
        ModelBuildingRequest req = new DefaultModelBuildingRequest();
        req.setPomFile(pom);
        req.setProcessPlugins(false);
        req.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        //TODO
        req.setModelResolver(null);

        ModelBuildingResult res = mb.build(req);
        List<Model> toRet = new ArrayList<Model>();

        for (String id : res.getModelIds()) {
            Model m = res.getRawModel(id);
            toRet.add(m);
        }
//        for (ModelProblem p : res.getProblems()) {
//            System.out.println("problem=" + p);
//            if (p.getException() != null) {
//                p.getException().printStackTrace();
//            }
//        }
        return toRet;
    }


//    /**
//     * creates model lineage for the given pom file.
//     * Useful to be able to locate where certain elements are defined.
//     *
//     * @param pom
//     * @param embedder
//     * @param allowStubs
//     * @return
//     */
//    public static ModelLineage createModelLineage(File pom, MavenEmbedder embedder, boolean allowStubs) throws ProjectBuildingException {
//        try {
//            ModelLineageBuilder bldr = (ModelLineageBuilder) embedder.getPlexusContainer().lookup(ModelLineageBuilder.class);
//            ProfileActivationContext context = new DefaultProfileActivationContext(new Properties(), true); //TODO shall we pass some execution props in here?
//            ProfileManager manager = new DefaultProfileManager(embedder.getPlexusContainer(), context);
//            DefaultProjectBuilderConfiguration conf = new DefaultProjectBuilderConfiguration();
//            conf.setGlobalProfileManager(manager);
//            conf.setExecutionProperties(new Properties());
//            conf.setLocalRepository(embedder.getLocalRepository());
//            conf.setUserProperties(new Properties());
//            return bldr.buildModelLineage(pom, conf, new ArrayList(), allowStubs, true);
//        } catch (ComponentLookupException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//        return new DefaultModelLineage();
//    }

//    private static void copyConfig(PlexusConfiguration old, XmlPlexusConfiguration conf) throws PlexusConfigurationException {
//        conf.setValue(old.getValue());
//        String[] attrNames = old.getAttributeNames();
//        if (attrNames != null && attrNames.length > 0) {
//            for (int i = 0; i < attrNames.length; i++) {
//                conf.setAttribute(attrNames[i], old.getAttribute(attrNames[i]));
//            }
//        }
//        if ("lifecycle".equals(conf.getName())) { //NOI18N
//            conf.setAttribute("implementation", "org.apache.maven.lifecycle.Lifecycle"); //NOI18N
//        }
//        for (int i = 0; i < old.getChildCount(); i++) {
//            PlexusConfiguration oldChild = old.getChild(i);
//            XmlPlexusConfiguration newChild = new XmlPlexusConfiguration(oldChild.getName());
//            conf.addChild(newChild);
//            copyConfig(oldChild, newChild);
//        }
//    }


    /**
     * Maven assumes the env vars are included in execution properties with the "env." prefix.
     * 
     * @param properties
     * @return 
     */
    public static Properties fillEnvVars(Properties properties) {
        try
        {
            Properties envVars = CommandLineUtils.getSystemEnvVars();
            Iterator i = envVars.entrySet().iterator();
            while ( i.hasNext() )
            {
                Map.Entry e = (Map.Entry) i.next();
                properties.setProperty( "env." + e.getKey().toString(), e.getValue().toString() );
            }
        }
        catch ( IOException e )
        {
            Exceptions.printStackTrace(e);
        }
        return properties;
    }
    

    private static class SettingsFileListener extends FileChangeAdapter {

        private FileObject dir;

        public SettingsFileListener() {
            File userLoc = FileUtil.normalizeFile(MavenEmbedder.DEFAULT_USER_SETTINGS_FILE.getParentFile());
            try {
                dir = FileUtil.toFileObject(userLoc);
                if (dir == null) {
                    dir = FileUtil.createFolder(userLoc);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (dir != null) {
                dir.addFileChangeListener(this);
                FileObject settings = dir.getFileObject("settings.xml"); //NOI18N
                if (settings != null) {
                    settings.addFileChangeListener(this);
                }
            }
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            if ("settings.xml".equals(fe.getFile().getNameExt())) { //NOI18N
                fe.getFile().removeFileChangeListener(this);
                synchronized (EmbedderFactory.class) {
                    online = null;
                    project = null;
                }
            }
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            if ("settings.xml".equals(fe.getFile().getNameExt())) { //NOI18N
                fe.getFile().addFileChangeListener(this);
                synchronized (EmbedderFactory.class) {
                    online = null;
                    project = null;
                }
            }
        }

        @Override
        public void fileChanged(FileEvent fe) {
            if ("settings.xml".equals(fe.getFile().getNameExt())) { //NOI18N
                synchronized (EmbedderFactory.class) {
                    online = null;
                    project = null;
                }
            }
        }
    }
}
