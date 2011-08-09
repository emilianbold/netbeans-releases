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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.UnknownRepositoryLayoutException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.model.building.ModelBuildingException;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import java.util.prefs.Preferences;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuilder;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingResult;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;

import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.logging.BaseLoggerManager;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.spi.connector.RepositoryConnector;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.transfer.NoRepositoryConnectorException;

/**
 *
 *  Factory for creating MavenEmbedder instances for various purposes.
 * 
 * @author mkleint
 */
public final class EmbedderFactory {

    private static final Logger LOG = Logger.getLogger(EmbedderFactory.class.getName());

    private static MavenEmbedder project;
    private static MavenEmbedder online;

    private EmbedderFactory() {
    }

    /**
     * embedder seems to cache some values..
     */
    public synchronized static void resetProjectEmbedder() {
        project = null;
        online = null;
    }

    private static File localRepoPreference() {
        Preferences prefs = NbPreferences.root().node("org/netbeans/modules/maven"); //NOI18N
        String localRepo = prefs.get("localRepository", null); //NOI18N
        if (localRepo != null) {
            File file = new File(localRepo);
            if (file.exists() && file.isDirectory()) {
                return file;
            } else if (!file.exists()) {
                if (!file.mkdirs()) {
                    LOG.log(Level.WARNING, "Could not create {0}", file);
                }
                return file;
            }
        }
        return null;
    }

   

    private static <T> void addComponentDescriptor(DefaultPlexusContainer container, Class<T> roleClass, Class<? extends T> implementationClass, String roleHint) {
        ComponentDescriptor<T> componentDescriptor = new ComponentDescriptor<T>();
        componentDescriptor.setRoleClass(roleClass);
        componentDescriptor.setImplementationClass(implementationClass.asSubclass(roleClass));
        componentDescriptor.setRoleHint(roleHint);
        container.addComponentDescriptor(componentDescriptor);
    }

    /**
     * #191267: suppresses logging from embedded Maven, since interesting results normally appear elsewhere.
     */
    private static class NbLoggerManager extends BaseLoggerManager {
        protected @Override org.codehaus.plexus.logging.Logger createLogger(String name) {
            int level = levelOf(LOG).intValue();
            return new NbLogger(level <= Level.FINEST.intValue() ? org.codehaus.plexus.logging.Logger.LEVEL_DEBUG :
                  level <= Level.FINER.intValue() ? org.codehaus.plexus.logging.Logger.LEVEL_INFO :
                  level <= Level.FINE.intValue() ? org.codehaus.plexus.logging.Logger.LEVEL_WARN :
                  org.codehaus.plexus.logging.Logger.LEVEL_DISABLED,
                name);
        }
        private Level levelOf(Logger log) {
            Level lvl = log.getLevel();
            if (lvl != null) {
                return lvl;
            } else {
                Logger par = log.getParent();
                if (par != null) {
                    return levelOf(par);
                } else {
                    return Level.INFO;
                }
            }
        }
        private static class NbLogger extends org.codehaus.plexus.logging.AbstractLogger {
            NbLogger(int threshold, String name) {
                super(threshold, name);
                LOG.log(Level.FINEST, "created Plexus logger {0} at threshold {1}", new Object[] {name, threshold});
            }
            private Logger logger() {
                return Logger.getLogger(LOG.getName() + "." + getName());
            }
            public @Override void debug(String m, Throwable t) {
                logger().log(Level.FINEST, m, t);
            }
            public @Override void info(String m, Throwable t) {
                logger().log(Level.FINER, m, t);
            }
            public @Override void warn(String m, Throwable t) {
                logger().log(Level.FINE, m, t);
            }
            public @Override void error(String m, Throwable t) {
                logger().log(Level.FINE, m, t);
            }
            public @Override void fatalError(String m, Throwable t) {
                logger().log(Level.FINE, m, t);
            }
            public @Override org.codehaus.plexus.logging.Logger getChildLogger(String name) {
                return new NbLogger(getThreshold(), getName() + "." + name);
            }
        }
    }

    public static MavenEmbedder createProjectLikeEmbedder() throws PlexusContainerException {
        final String mavenCoreRealmId = "plexus.core";
        ContainerConfiguration dpcreq = new DefaultContainerConfiguration()
            .setClassWorld( new ClassWorld(mavenCoreRealmId, EmbedderFactory.class.getClassLoader()) )
            .setName("maven");
        
        DefaultPlexusContainer pc = new DefaultPlexusContainer(dpcreq);
        pc.setLoggerManager(new NbLoggerManager());

        addComponentDescriptor(pc, RepositoryConnectorFactory.class, OfflineConnector.class, "offline");
       
        Properties props = new Properties();
        props.putAll(System.getProperties());
        EmbedderConfiguration configuration = new EmbedderConfiguration(pc, localRepoPreference(), fillEnvVars(props), true);
        
//        File userSettingsPath = MavenEmbedder.DEFAULT_USER_SETTINGS_FILE;
//        File globalSettingsPath = InstalledFileLocator.getDefault().locate("modules/ext/maven/settings.xml", "org.netbeans.modules.maven.embedder", false); //NOI18N
//
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
            embedder = new MavenEmbedder(configuration);
            //MEVENIDE-634 make all instances non-interactive
//            WagonManager wagonManager = (WagonManager) embedder.getPlexusContainer().lookup(WagonManager.ROLE);
//            wagonManager.setInteractive(false);
        } catch (ComponentLookupException ex) {
            ErrorManager.getDefault().notify(ex);
        }

        return embedder;
    }

    public static final class OfflineConnector implements RepositoryConnectorFactory {
        @Override public RepositoryConnector newInstance(RepositorySystemSession session, RemoteRepository repository) throws NoRepositoryConnectorException {
            // Throwing NoRepositoryConnectorException is ineffective because DefaultRemoteRepositoryManager will just skip to WagonRepositoryConnectorFactory.
            // (No apparent way to suppress WRCF from the Plexus container; using "wagon" as the role hint does not work.)
            // Could also return a no-op RepositoryConnector which would perform no downloads.
            // But we anyway want to ensure that related code is consistently setting the offline flag on all Maven structures that require it.
            throw new AssertionError();
        }
        @Override public int getPriority() {
            return Integer.MAX_VALUE;
        }
    }

    public synchronized static MavenEmbedder getProjectEmbedder() /*throws MavenEmbedderException*/ {
        if (project == null) {
            try {
                project = createProjectLikeEmbedder();
            } catch (PlexusContainerException ex) {
                Exceptions.printStackTrace(ex);
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
            .setName("maven");

        DefaultPlexusContainer pc = new DefaultPlexusContainer(dpcreq);
        pc.setLoggerManager(new NbLoggerManager());

        Properties props = new Properties();
        props.putAll(System.getProperties());
        EmbedderConfiguration req = new EmbedderConfiguration(pc, localRepoPreference(), fillEnvVars(props), false);

//        //TODO remove explicit activation
//        req.addActiveProfile("netbeans-public").addActiveProfile("netbeans-private"); //NOI18N


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

    public static ArtifactRepository createRemoteRepository(MavenEmbedder embedder, String url, String id) {
        try {
            ArtifactRepositoryFactory fact = embedder.lookupComponent(ArtifactRepositoryFactory.class);
            assert fact!=null : "ArtifactRepositoryFactory component not found in maven";
            ArtifactRepositoryPolicy snapshotsPolicy = new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS, ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN);
            ArtifactRepositoryPolicy releasesPolicy = new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS, ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN);
            return fact.createArtifactRepository(id, url, ArtifactRepositoryFactory.DEFAULT_LAYOUT_ID, snapshotsPolicy, releasesPolicy);
        } catch (UnknownRepositoryLayoutException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    /**
     * Creates a list of POM models in an inheritance lineage.
     * Each resulting model is "raw", so contains no interpolation or inheritance.
     * In particular beware that groupId and/or version may be null if inherited from a parent; use {@link Model#getParent} to resolve.
     * @param pom a POM to inspect
     * @param embedder an embedder to use
     * @return a list of models, starting with the specified POM, going through any parents, finishing with the Maven superpom (with a null artifactId)
     * @throws ModelBuildingException if the POM or parents could not even be parsed; warnings are not reported
     */
    public static List<Model> createModelLineage(File pom, MavenEmbedder embedder) throws ModelBuildingException {
        ModelBuilder mb = embedder.lookupComponent(ModelBuilder.class);
        assert mb!=null : "ModelBuilder component not found in maven";
        ModelBuildingRequest req = new DefaultModelBuildingRequest();
        req.setPomFile(pom);
        req.setProcessPlugins(false);
        req.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        req.setModelResolver(new NBRepositoryModelResolver(embedder));
        req.setSystemProperties(embedder.getSystemProperties());
        
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
            Properties envVars = CommandLineUtils.getSystemEnvVars(); // XXX what is wrong with System.getenv()?
            for (Map.Entry<Object,Object> e : envVars.entrySet()) {
                properties.setProperty( "env." + e.getKey().toString(), e.getValue().toString() );
            }
        }
        catch ( IOException e )
        {
            Exceptions.printStackTrace(e);
        }
        return properties;
    }
}
