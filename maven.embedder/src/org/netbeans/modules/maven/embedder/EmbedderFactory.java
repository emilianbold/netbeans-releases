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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.*;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.BaseLoggerManager;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.maven.embedder.impl.ExtensionModule;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Factory for creating {@link MavenEmbedder}s.
 */
public final class EmbedderFactory {

    private static final String PROP_COMMANDLINE_PATH = "commandLineMavenPath";
    
    //same prop constant in MavenSettings.java
    static final String PROP_DEFAULT_OPTIONS = "defaultOptions"; 

    private static final Logger LOG = Logger.getLogger(EmbedderFactory.class.getName());

    private static MavenEmbedder project;
    private static final Object PROJECT_LOCK = new Object();
    private static MavenEmbedder online;
    private static final Object ONLINE_LOCK = new Object();
    
    private static final RequestProcessor RP = new RequestProcessor("Maven Embedder warmup");
    
    private static final RequestProcessor.Task warmupTask = RP.create(new Runnable() {
            @Override
            public void run() {
                //#211158 after being reset, recreate the instance for followup usage. 
                //makes the performance stats of the project embedder after resetting more predictable
                getProjectEmbedder();
            }
        });

    private EmbedderFactory() {
    }

    /**
     * embedder seems to cache some values..
     */
    public static void resetCachedEmbedders() {
        synchronized (PROJECT_LOCK) {
            project = null;
        }
        synchronized (ONLINE_LOCK) {
            online = null;
        }
        //just delay a bit in case both MavenSettings.setDefaultOptions and Embedderfactory.setMavenHome are called..
        RP.post(warmupTask, 100);
    }

    public static File getDefaultMavenHome() {
        return InstalledFileLocator.getDefault().locate("maven", "org.netbeans.modules.maven.embedder", false);
    }

    private static Preferences getPreferences() { // compatibility; used to be in MavenSettings
        return NbPreferences.root().node("org/netbeans/modules/maven");
    }

    public static @NonNull File getMavenHome() {
        String str =  getPreferences().get(PROP_COMMANDLINE_PATH, null);
        if (str != null) {
            return FileUtil.normalizeFile(new File(str));
        } else {
            return getDefaultMavenHome();
        }
    }

    public static void setMavenHome(File path) {
        File oldValue = getMavenHome();
        File defValue = getDefaultMavenHome();
        if (oldValue.equals(path) || path == null && oldValue.equals(defValue)) {
            //no change happened, prevent resetting the embedders
            return;
        }
        if (path == null || path.equals(defValue)) {
            getPreferences().remove(PROP_COMMANDLINE_PATH);
        } else {
            getPreferences().put(PROP_COMMANDLINE_PATH, FileUtil.normalizeFile(path).getAbsolutePath());
        }
        resetCachedEmbedders();
    }
    
    
    static Map<String, String> getCustomSystemProperties() {
        Map<String, String> toRet = new HashMap<String, String>();
        String options = getPreferences().get(PROP_DEFAULT_OPTIONS, "");
        try {
            
            String[] cmdlines = CommandLineUtils.translateCommandline(options);
            if (cmdlines != null) {
                for (String cmd : cmdlines) {
                    if (cmd != null && cmd.startsWith("-D")) {
                        cmd = cmd.substring("-D".length());
                        int ind = cmd.indexOf('=');
                        if (ind > -1) {
                            String key = cmd.substring(0, ind);
                            String val = cmd.substring(ind + 1);
                            toRet.put(key, val);
                        }
                    }
                }
            }
            return toRet;
        } catch (Exception ex) {
            LOG.log(Level.FINE, "cannot parse " + options, ex);
            return Collections.emptyMap();
        }
    }

    private static File getSettingsXml() {
        return new File(getMavenHome(), "conf/settings.xml");
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

    public static @NonNull MavenEmbedder createProjectLikeEmbedder() throws PlexusContainerException {
        final String mavenCoreRealmId = "plexus.core";
        ContainerConfiguration dpcreq = new DefaultContainerConfiguration()
            .setClassWorld( new ClassWorld(mavenCoreRealmId, EmbedderFactory.class.getClassLoader()) )
            .setName("maven");
        
        DefaultPlexusContainer pc = new DefaultPlexusContainer(dpcreq, new ExtensionModule());
        pc.setLoggerManager(new NbLoggerManager());

        Properties props = new Properties();
        props.putAll(System.getProperties());
        props.putAll(getCustomSystemProperties());
        EmbedderConfiguration configuration = new EmbedderConfiguration(pc, fillEnvVars(props), true, getSettingsXml());
        
        try {
            return new MavenEmbedder(configuration);
            //MEVENIDE-634 make all instances non-interactive
//            WagonManager wagonManager = (WagonManager) embedder.getPlexusContainer().lookup(WagonManager.ROLE);
//            wagonManager.setInteractive(false);
        } catch (ComponentLookupException ex) {
            throw new PlexusContainerException(ex.toString(), ex);
        }
    }

    private static void rethrowThreadDeath(Throwable t) { // #201098
        if (t instanceof ThreadDeath) {
            throw (ThreadDeath) t;
        }
        Throwable t2 = t.getCause();
        if (t2 != null) {
            rethrowThreadDeath(t2);
        }
    }

    public static @NonNull MavenEmbedder getProjectEmbedder() {
        synchronized (PROJECT_LOCK) {
            if (project == null) {
                try {
                    project = createProjectLikeEmbedder();
                } catch (PlexusContainerException ex) {
                    rethrowThreadDeath(ex);
                    throw new IllegalStateException(ex);
                }
            }
            return project;
        }
    }

    public static @NonNull MavenEmbedder getOnlineEmbedder() {
        synchronized (ONLINE_LOCK) {
            if (online == null) {
                try {
                    online = createOnlineEmbedder();
                } catch (PlexusContainerException ex) {
                    rethrowThreadDeath(ex);
                    throw new IllegalStateException(ex);
                }
            }
            return online;
        }
    }

    /*public*/ @NonNull static MavenEmbedder createOnlineEmbedder() throws PlexusContainerException {
        final String mavenCoreRealmId = "plexus.core";
        ContainerConfiguration dpcreq = new DefaultContainerConfiguration()
            .setClassWorld( new ClassWorld(mavenCoreRealmId, EmbedderFactory.class.getClassLoader()) )
            .setName("maven");

        DefaultPlexusContainer pc = new DefaultPlexusContainer(dpcreq);
        pc.setLoggerManager(new NbLoggerManager());

        Properties props = new Properties();
        props.putAll(System.getProperties());
        props.putAll(getCustomSystemProperties());        
        EmbedderConfiguration req = new EmbedderConfiguration(pc, fillEnvVars(props), false, getSettingsXml());

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

        try {
            return new MavenEmbedder(req);
            //MEVENIDE-634 make all instances non-interactive
//            WagonManager wagonManager = (WagonManager) embedder.getPlexusContainer().lookup(WagonManager.ROLE);
//            wagonManager.setInteractive(false);
        } catch (ComponentLookupException ex) {
            throw new PlexusContainerException(ex.toString(), ex);
        }
//            try {
//                //MEVENIDE-634 make all instances non-interactive
//                WagonManager wagonManager = (WagonManager) embedder.getPlexusContainer().lookup(WagonManager.ROLE);
//                wagonManager.setInteractive( false );
//                wagonManager.setDownloadMonitor(new ProgressTransferListener());
//            } catch (ComponentLookupException ex) {
//                ErrorManager.getDefault().notify(ex);
//            }
    }

    /**
     * using this method one creates an ArtifactRepository instance with injected mirrors and proxies
     * @param embedder
     * @param url
     * @param id
     * @return 
     * @deprecated use MavenEmbedder.createRemoteRepository
     */
    @Deprecated
    public static ArtifactRepository createRemoteRepository(MavenEmbedder embedder, String url, String id) {
        return embedder.createRemoteRepository(url, id);
    }

    /**
     * Creates a list of POM models in an inheritance lineage.
     * Each resulting model is "raw", so contains no interpolation or inheritance.
     * In particular beware that groupId and/or version may be null if inherited from a parent; use {@link Model#getParent} to resolve.
     * @param pom a POM to inspect
     * @param embedder an embedder to use
     * @return a list of models, starting with the specified POM, going through any parents, finishing with the Maven superpom (with a null artifactId)
     * @throws ModelBuildingException if the POM or parents could not even be parsed; warnings are not reported
     * @deprecated use MavenEmbedder.createModelLineage
     */
    @Deprecated
    public static List<Model> createModelLineage(File pom, MavenEmbedder embedder) throws ModelBuildingException {
        return embedder.createModelLineage(pom);
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
     * @see EnvironmentUtils#addEnvVars
     */
    public static Properties fillEnvVars(Properties properties) {
        for (Map.Entry<String,String> entry : System.getenv().entrySet()) {
            String key = entry.getKey();
            if (Utilities.isWindows()) {
                key = key.toUpperCase(Locale.ENGLISH);
            }
            properties.setProperty("env." + key, entry.getValue());
        }
        return properties;
    }
}
