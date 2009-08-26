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

package org.netbeans.modules.autoupdate.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.util.jar.JarEntry;
import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.core.startup.Main;
import org.netbeans.core.startup.TopLogging;
import org.netbeans.modules.autoupdate.updateprovider.DummyModuleInfo;
import org.netbeans.spi.autoupdate.KeyStoreProvider;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.updater.ModuleDeactivator;
import org.netbeans.updater.ModuleUpdater;
import org.netbeans.updater.UpdateTracking;
import org.netbeans.updater.UpdaterDispatcher;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Dependency;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Jiri Rechtacek, Radek Matous
 */
public class Utilities {

    private Utilities() {}

    public static final String UPDATE_DIR = "update"; // NOI18N
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String DOWNLOAD_DIR = UPDATE_DIR + FILE_SEPARATOR + "download"; // NOI18N
    public static final String NBM_EXTENTSION = ".nbm";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat ("yyyy/MM/dd"); // NOI18N
    public static final String ATTR_VISIBLE = "AutoUpdate-Show-In-Client";
    public static final String ATTR_ESSENTIAL = "AutoUpdate-Essential-Module";
    
    private static final String USER_KS_KEY = "userKS";
    private static final String USER_KS_FILE_NAME = "user.ks";
    private static final String KS_USER_PASSWORD = "open4user";
    private static Lookup.Result<KeyStoreProvider> result;
    private static Logger err = null;
    private static ModuleManager mgr = null;
    
    
    public static Collection<KeyStore> getKeyStore () {
        if (result == null) {            
            result = Lookup.getDefault ().lookup (
                    new Lookup.Template<KeyStoreProvider> (KeyStoreProvider.class));
            result.addLookupListener (new KeyStoreProviderListener ());
        }
        Collection<? extends KeyStoreProvider> c = result.allInstances ();
        if (c == null || c.isEmpty ()) {
            return Collections.emptyList ();
        }
        List<KeyStore> kss = new ArrayList<KeyStore> ();
        
        for (KeyStoreProvider provider : c) {
            KeyStore ks = provider.getKeyStore ();
            if (ks != null) {
                kss.add (ks);
            }
        }
        
        return kss;
    }
    
    static private class KeyStoreProviderListener implements LookupListener {
        private KeyStoreProviderListener () {
        }
        
        public void resultChanged (LookupEvent ev) {
            result = null;
        }
    }
    
    private static final String ATTR_NAME = "name"; // NOI18N
    private static final String ATTR_SPEC_VERSION = "specification_version"; // NOI18N
    private static final String ATTR_SIZE = "size"; // NOI18N
    private static final String ATTR_NBM_NAME = "nbm_name"; // NOI18N
    
    private static File getInstallLater(File root) {
        File file = new File(root.getPath() + FILE_SEPARATOR + DOWNLOAD_DIR + FILE_SEPARATOR + ModuleUpdater.LATER_FILE_NAME);
        return file;
    }

    public static void deleteAllDoLater() {
        List<File> clusters = UpdateTracking.clusters(true);
        assert clusters != null : "Clusters cannot be empty."; // NOI18N
        for (File cluster : clusters) {
            for (File doLater : findDoLater (cluster)) {
                doLater.delete ();
            }
        }                                
    }
    
    private static Collection<File> findDoLater (File cluster) {
        if (! cluster.exists ()) {
            return Collections.emptySet ();
        } else {
            Collection<File> res = new HashSet<File> ();
            if (getInstallLater (cluster).exists ()) {
                res.add (getInstallLater (cluster));
            }
            if (ModuleDeactivator.getDeactivateLater (cluster).exists ()) {
                res.add (ModuleDeactivator.getDeactivateLater (cluster));
            }
            return res;
        }
    }
    
    public static void writeInstallLater (Map<UpdateElementImpl, File> updates) {
        // loop for all clusters and write if needed
        List<File> clusters = UpdateTracking.clusters(true);
        assert clusters != null : "Clusters cannot be empty."; // NOI18N
        for (File cluster : clusters) {
            writeInstallLaterToCluster (cluster, updates);
        }
    }
    
    private static void writeInstallLaterToCluster (File cluster, Map<UpdateElementImpl, File> updates) {
        Document document = XMLUtil.createDocument(UpdateTracking.ELEMENT_MODULES, null, null, null);                
        
        Element root = document.getDocumentElement();

        if (updates.isEmpty ()) {
            return ;
        }
        
        boolean isEmpty = true;
        for (UpdateElementImpl elementImpl : updates.keySet ()) {
            File c = updates.get(elementImpl);
            // pass this module to given cluster ?
            if (cluster.equals (c)) {
                Element module = document.createElement(UpdateTracking.ELEMENT_MODULE);
                module.setAttribute(UpdateTracking.ATTR_CODENAMEBASE, elementImpl.getCodeName());
                module.setAttribute(ATTR_NAME, elementImpl.getDisplayName());
                module.setAttribute(ATTR_SPEC_VERSION, elementImpl.getSpecificationVersion().toString());
                module.setAttribute(ATTR_SIZE, Long.toString(elementImpl.getDownloadSize()));
                module.setAttribute(ATTR_NBM_NAME, InstallSupportImpl.getDestination(cluster, elementImpl.getCodeName(), true).getName());

                root.appendChild( module );
                isEmpty = false;
            }
        }
        
        if (isEmpty) {
            return ;
        }
        
        writeXMLDocumentToFile (document, getInstallLater (cluster));
    }
    
    private static void writeXMLDocumentToFile (Document doc, File dest) {
        doc.getDocumentElement ().normalize ();

        dest.getParentFile ().mkdirs ();
        InputStream is = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream ();
        OutputStream fos = null;
        try {
            try {
                XMLUtil.write (doc, bos, "UTF-8"); // NOI18N
                if (bos != null) {
                    bos.close ();
                }
                fos = new FileOutputStream (dest);
                is = new ByteArrayInputStream (bos.toByteArray ());
                FileUtil.copy (is, fos);
            } finally {
                if (is != null) {
                    is.close ();
                }
                if (fos != null) {
                    fos.close ();
                }
                if (bos != null) {
                    bos.close ();
                }
            }
        } catch (java.io.FileNotFoundException fnfe) {
            Exceptions.printStackTrace (fnfe);
        } catch (java.io.IOException ioe) {
            Exceptions.printStackTrace (ioe);
        } finally {
            if (bos != null) {
                try {
                    bos.close ();
                } catch (Exception x) {
                    Exceptions.printStackTrace (x);
                }
            }
        }
    }

    public static void writeDeactivateLater (Collection<File> files) {
        File userdir = InstallManager.getUserDir ();
        assert userdir != null && userdir.exists (): "Userdir " + userdir + " found and exists."; // NOI18N
        writeMarkedFilesToFile (files, ModuleDeactivator.getDeactivateLater (userdir));
    }
    
    public static void writeFileMarkedForDelete (Collection<File> files) {
        writeMarkedFilesToFile (files, ModuleDeactivator.getControlFileForMarkedForDelete (InstallManager.getUserDir ()));
    }
    
    public static void writeFileMarkedForDisable (Collection<File> files) {
        writeMarkedFilesToFile (files, ModuleDeactivator.getControlFileForMarkedForDisable (InstallManager.getUserDir ()));
    }
    
    private static void writeMarkedFilesToFile (Collection<File> files, File dest) {
        // don't forget for content written before
        String content = "";
        if (dest.exists ()) {
            content += ModuleDeactivator.readStringFromFile (dest);
        }
        
        for (File f : files) {
            content += f.getAbsolutePath () + UpdateTracking.PATH_SEPARATOR;
        }
        
        if (content == null || content.length () == 0) {
            return ;
        }
        
        dest.getParentFile ().mkdirs ();
        assert dest.getParentFile ().exists () && dest.getParentFile ().isDirectory () : "Parent of " + dest + " exists and is directory.";
        InputStream is = null;
        OutputStream fos = null;            
        
        try {
            try {
                fos = new FileOutputStream (dest);
                is = new ByteArrayInputStream (content.getBytes());
                FileUtil.copy (is, fos);
            } finally {
                if (is != null) is.close();
                if (fos != null) fos.close();
            }                
        } catch (java.io.FileNotFoundException fnfe) {
            Exceptions.printStackTrace(fnfe);
        } catch (java.io.IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }

    }
    
    public static void writeAdditionalInformation (Map<UpdateElementImpl, File> updates) {
        // loop for all clusters and write if needed
        List<File> clusters = UpdateTracking.clusters (true);
        assert clusters != null : "Clusters cannot be empty."; // NOI18N
        for (File cluster : clusters) {
            writeAdditionalInformationToCluster (cluster, updates);
        }
    }
    
    public static File locateUpdateTracking (ModuleInfo m) {
        String fileNameToFind = UpdateTracking.TRACKING_FILE_NAME + '/' + m.getCodeNameBase ().replace ('.', '-') + ".xml"; // NOI18N
        return InstalledFileLocator.getDefault ().locate (fileNameToFind, m.getCodeNameBase (), false);
    }
    
    public static String readSourceFromUpdateTracking (ModuleInfo m) {
        String res = null;
        File ut = locateUpdateTracking (m);
        if (ut != null) {
            Node n = getModuleConfiguration (ut);
            if (n != null) {
                Node attrOrigin = n.getAttributes ().getNamedItem (UpdateTracking.ATTR_ORIGIN);
                assert attrOrigin != null : "ELEMENT_VERSION must contain ATTR_ORIGIN attribute.";
                if (! (UpdateTracking.UPDATER_ORIGIN.equals (attrOrigin.getNodeValue ()) ||
                        UpdateTracking.INSTALLER_ORIGIN.equals (attrOrigin.getNodeValue ()))) {
                    // ignore default value
                    res = attrOrigin.getNodeValue ();
                }
            }
        }
        return res;
    }
    
    public static Date readInstallTimeFromUpdateTracking (ModuleInfo m) {
        Date res = null;
        String time = null;
        File ut = locateUpdateTracking (m);
        if (ut != null) {
            Node n = getModuleConfiguration (ut);
            if (n != null) {
                Node attrInstallTime = n.getAttributes ().getNamedItem (UpdateTracking.ATTR_INSTALL);
                assert attrInstallTime != null : "ELEMENT_VERSION must contain ATTR_INSTALL attribute.";
                time = attrInstallTime.getNodeValue ();
            }
        }
        if (time != null) {
            try {
                long lTime = Long.parseLong (time);
                res = new Date (lTime);
            } catch (NumberFormatException nfe) {
                getLogger ().log (Level.INFO, nfe.getMessage (), nfe);
            }
        }
        return res;
    }
    
    static void writeUpdateOfUpdaterJar (JarEntry updaterJarEntry, JarFile updaterJar, File targetCluster) {
        File dest = new File (targetCluster, UpdaterDispatcher.UPDATE_DIR + // updater
                                UpdateTracking.FILE_SEPARATOR + UpdaterDispatcher.NEW_UPDATER_DIR + // new_updater
                                UpdateTracking.FILE_SEPARATOR + ModuleUpdater.UPDATER_JAR
                                );
        
        dest.getParentFile ().mkdirs ();
        assert dest.getParentFile ().exists () && dest.getParentFile ().isDirectory () : "Parent of " + dest + " exists and is directory.";
        InputStream is = null;
        OutputStream fos = null;            
        
        try {
            try {
                fos = new FileOutputStream (dest);
                is = updaterJar.getInputStream (updaterJarEntry);
                FileUtil.copy (is, fos);
            } finally {
                if (is != null) is.close();
                if (fos != null) fos.close();
            }                
        } catch (java.io.FileNotFoundException fnfe) {
            getLogger ().log (Level.INFO, fnfe.getLocalizedMessage (), fnfe);
        } catch (java.io.IOException ioe) {
            getLogger ().log (Level.INFO, ioe.getLocalizedMessage (), ioe);
        }
    }
    
    static void cleanUpdateOfUpdaterJar () {
        // loop for all clusters and clean if needed
        List<File> clusters = UpdateTracking.clusters (true);
        assert clusters != null : "Clusters cannot be empty."; // NOI18N
        for (File cluster : clusters) {
            File updaterDir = new File (cluster, UpdaterDispatcher.UPDATE_DIR + UpdateTracking.FILE_SEPARATOR + UpdaterDispatcher.NEW_UPDATER_DIR);
            if (updaterDir.exists () && updaterDir.isDirectory ()) {
                for (File f : updaterDir.listFiles ()) {
                    f.delete ();
                }
                updaterDir.delete ();
            }
        }
    }

    static Module toModule(UpdateUnit uUnit) {
        return getModuleInstance(uUnit.getCodeName(), null); // XXX
    }
    
    public static Module toModule(String codeNameBase, SpecificationVersion specificationVersion) {
        return getModuleInstance(codeNameBase, specificationVersion);
    }
    
    public static Module toModule (ModuleInfo info) {
        return getModuleInstance (info.getCodeNameBase(), info.getSpecificationVersion ());
    }
    
    public static boolean isFixed (ModuleInfo info) {
        Module m = toModule (info);
        assert ! info.isEnabled () || m != null : "Module found for enabled " + info;
        return m == null ? false : m.isFixed ();
    }
    
    public static boolean isValid (ModuleInfo info) {
        Module m = toModule (info);
        assert ! info.isEnabled () || m != null : "Module found for enabled " + info;
        return m == null ? false : m.isValid ();
    }
    
    static UpdateUnit toUpdateUnit(Module m) {
        return UpdateManagerImpl.getInstance().getUpdateUnit(m.getCodeNameBase());
    }
    
    static UpdateUnit toUpdateUnit(String codeNameBase) {
        return UpdateManagerImpl.getInstance().getUpdateUnit(codeNameBase);
    }
    
    public static Set<UpdateElement> findRequiredUpdateElements (UpdateElement element,
            Collection<ModuleInfo> infos,
            Set<Dependency> brokenDependencies) {
        UpdateElementImpl el = Trampoline.API.impl(element);
        Set<UpdateElement> retval = new HashSet<UpdateElement> ();
        switch (el.getType ()) {
        case KIT_MODULE :
        case MODULE :
            Set<Dependency> deps = new HashSet<Dependency> (((ModuleUpdateElementImpl) el).getModuleInfo ().getDependencies ());
            Set<ModuleInfo> availableInfos = new HashSet<ModuleInfo> (infos);
            Set<Dependency> newones;
            while (! (newones = processDependencies (deps, retval, availableInfos, brokenDependencies, element)).isEmpty ()) {
                deps = newones;
            }
            
            Set<Dependency> moreBroken = new HashSet<Dependency> ();
            Set<ModuleInfo> tmp = new HashSet<ModuleInfo> (availableInfos);
            
            Set<UpdateElement> more;
            while (retval.addAll (more = handleBackwardCompatability (tmp, moreBroken))) {
                if (! moreBroken.isEmpty ()) {
                    brokenDependencies.addAll (moreBroken);
                    break;
                }
                tmp = new HashSet<ModuleInfo> ();
                for (UpdateElement e : more) {
                    //infos.addAll (Trampoline.API.impl (el).getModuleInfos ());
                    tmp.add (((ModuleUpdateElementImpl) Trampoline.API.impl (e)).getModuleInfo ());
                }
            }
            if (! moreBroken.isEmpty ()) {
                brokenDependencies.addAll (moreBroken);
            }
            
            break;
        case STANDALONE_MODULE :
        case FEATURE :
            FeatureUpdateElementImpl feature = (FeatureUpdateElementImpl) el;
            for (ModuleUpdateElementImpl module : feature.getContainedModuleElements ()) {
                retval.addAll (findRequiredUpdateElements (module.getUpdateElement (), infos, brokenDependencies));
            }
            break;
        case CUSTOM_HANDLED_COMPONENT :
            getLogger ().log (Level.INFO, "CUSTOM_HANDLED_COMPONENT doesn't care about required elements."); // XXX
            break;
        default:
            assert false : "Not implement for type " + el.getType () + " of UpdateElement " + el;
        }
        return retval;
    }
    
    public static Set<UpdateElement> handleBackwardCompatability (Set<ModuleInfo> forInstall, Set<Dependency> brokenDependencies) {
        Set<UpdateElement> moreRequested = new HashSet<UpdateElement> ();
        // backward compatibility
        for (ModuleInfo mi : forInstall) {
            UpdateUnit u = UpdateManagerImpl.getInstance ().getUpdateUnit (mi.getCodeNameBase ());
            // invalid codenamebase (in unit tests)
            if (u == null) {
                continue;
            }
            // not installed, not need to handle backward compatability
            UpdateElement i = u.getInstalled ();
            if (i == null) {
                continue;
            }

            // Dependency.TYPE_MODULE
            Collection<Dependency> dependencies = new HashSet<Dependency> ();
            dependencies.addAll(Dependency.create (Dependency.TYPE_MODULE, mi.getCodeName ()));

            SortedSet<String> newTokens = new TreeSet<String> (Arrays.asList (mi.getProvides ()));
            SortedSet<String> oldTokens = new TreeSet<String> (Arrays.asList (((ModuleUpdateElementImpl) Trampoline.API.impl (i)).getModuleInfo ().getProvides ()));
            oldTokens.removeAll (newTokens);
            // handle diff
            for (String tok : oldTokens) {
                // don't care about provider of platform dependency here
                if (tok.startsWith ("org.openide.modules.os")) { // NOI18N
                    continue;
                }
                dependencies.addAll (Dependency.create (Dependency.TYPE_REQUIRES, tok));
                dependencies.addAll (Dependency.create (Dependency.TYPE_NEEDS, tok));
            }

            for (Dependency d : dependencies) {
                DependencyAggregator deco = DependencyAggregator.getAggregator (d);
                if (deco != null) {
                    for (ModuleInfo depMI : deco.getDependening ()) {
                        Module depM = Utilities.toModule (depMI);
                        if (depM == null) {
                            continue;
                        }
                        if (depM.getProblems () != null && ! depM.getProblems ().isEmpty ()) {
                            // skip this module because it has own problems already
                            continue;
                        }
                        for (Dependency toTry : depM.getDependencies ()) {
                            // check only relevant deps
                            if (deco.equals (DependencyAggregator.getAggregator (toTry)) &&
                                    ! DependencyChecker.checkDependencyModule (toTry, mi)) {
                                UpdateUnit tryUU = UpdateManagerImpl.getInstance ().getUpdateUnit (depM.getCodeNameBase ());
                                if (! tryUU.getAvailableUpdates ().isEmpty ()) {
                                    UpdateElement tryUE = tryUU.getAvailableUpdates ().get (0);
                                    
                                    ModuleInfo tryUpdated = ((ModuleUpdateElementImpl) Trampoline.API.impl (tryUE)).getModuleInfo ();
                                    Set<Dependency> deps = new HashSet<Dependency> (tryUpdated.getDependencies ());
                                    Set<ModuleInfo> availableInfos = new HashSet<ModuleInfo> (forInstall);
                                    Set<Dependency> newones;
                                    while (! (newones = processDependencies (deps, moreRequested, availableInfos, brokenDependencies, tryUE)).isEmpty ()) {
                                        deps = newones;
                                    }
                                    moreRequested.add (tryUE);
                                }
                            }
                        }
                    }
                }
            }
        }
        return moreRequested;
    }

    private static Set<Dependency> processDependencies (final Set<Dependency> original,
            Set<UpdateElement> retval,
            Set<ModuleInfo> availableInfos,
            Set<Dependency> brokenDependencies,
            UpdateElement el) {
        Set<Dependency> res = new HashSet<Dependency> ();
        for (Dependency dep : original) {
            UpdateElement req = handleDependency (el, dep, availableInfos, brokenDependencies, true);
            if (req != null) {
                ModuleUpdateElementImpl reqM = (ModuleUpdateElementImpl) Trampoline.API.impl (req);
                availableInfos.add (reqM.getModuleInfo ());
                retval.add (req);
                res.addAll (reqM.getModuleInfo ().getDependencies ());
            }
        }
        res.removeAll (original);
        return res;
    }
    
    public static UpdateElement handleDependency (UpdateElement el,
            Dependency dep,
            Collection<ModuleInfo> availableInfos,
            Set<Dependency> brokenDependencies,
            boolean aggressive) {
        UpdateElement requested = null;
        
        switch (dep.getType ()) {
            case Dependency.TYPE_JAVA :
                if (! DependencyChecker.matchDependencyJava (dep)) {
                    brokenDependencies.add (dep);
                }
                break;
            case Dependency.TYPE_PACKAGE :
                if (! DependencyChecker.matchPackageDependency (dep)) {
                    brokenDependencies.add (dep);
                }
                break;
            case Dependency.TYPE_MODULE :
                UpdateUnit u = DependencyAggregator.getRequested (dep);
                boolean updateMatched = false;
                boolean installMatched = false;
                boolean availableMatched = false;
                if (u != null) {
                    // follow aggressive updates strategy
                    // if new module update is available, promote it even though installed one suites all the dependendencies
                    UpdateElement reqEl = u.getAvailableUpdates().isEmpty() ? null : u.getAvailableUpdates().get(0);
                    if (reqEl != null) {
                        UpdateElementImpl reqElImpl = Trampoline.API.impl(reqEl);
                        ModuleUpdateElementImpl reqModuleImpl = (ModuleUpdateElementImpl) reqElImpl;
                        ModuleInfo info = reqModuleImpl.getModuleInfo();
                        if (DependencyChecker.checkDependencyModule(dep, info)) {
                            if (!availableInfos.contains(info)) {
                                requested = reqEl;
                                updateMatched = true;
                            }
                        }
                    }

                    if (u.getInstalled() != null) {
                            UpdateElementImpl reqElImpl = Trampoline.API.impl(u.getInstalled());
                            installMatched = DependencyChecker.checkDependencyModule(dep, ((ModuleUpdateElementImpl) reqElImpl).getModuleInfo());
                    }                    
                }

                for (ModuleInfo m : availableInfos) {
                    if (DependencyChecker.checkDependencyModule(dep, m)) {
                        availableMatched = true;
                        break;
                    }
                }
                if(updateMatched && installMatched && !aggressive) {
                    requested = null;
                }
                if (updateMatched && installMatched && aggressive) {
                    if (requested.getUpdateUnit().getType().equals(UpdateManager.TYPE.KIT_MODULE)) {
                        //requested = null;
                    } else if (/*Trampoline.API.impl(el).isEager() &&*/
                            !el.getUpdateUnit().getType().equals(UpdateManager.TYPE.KIT_MODULE)) {
                        //requested = null;
                    }
                }
                
                if (!installMatched && !availableMatched && !updateMatched) {
                    brokenDependencies.add(dep);
                }

                break;
            case Dependency.TYPE_REQUIRES :
            case Dependency.TYPE_NEEDS :
            case Dependency.TYPE_RECOMMENDS :
                if (DummyModuleInfo.TOKEN_MODULE_FORMAT1.equals (dep.getName ()) ||
                        DummyModuleInfo.TOKEN_MODULE_FORMAT2.equals (dep.getName ())) {
                    // these tokens you can ignore here
                    break;
                }
                UpdateUnit p = DependencyAggregator.getRequested (dep);
                boolean passed = false;
                if (p == null) {
                    // look on availableInfos as well
                    for (ModuleInfo m : availableInfos) {
                        if (Arrays.asList (m.getProvides ()).contains (dep.getName ())) {
                            passed = true;
                            break;
                        }
                    }
                } else {
                    passed = true;
                    if (! p.getAvailableUpdates ().isEmpty ()) {
                        requested = p.getAvailableUpdates ().get (0);
                    }
                }
                if (! passed && Dependency.TYPE_RECOMMENDS != dep.getType ()) {
                    brokenDependencies.add (dep);
                }
                break;
        }
        return requested;
    }
    
    static Set<String> getBrokenDependencies (UpdateElement element, List<ModuleInfo> infos) {
        assert element != null : "UpdateElement cannot be null";
        Set<Dependency> brokenDependencies = new HashSet<Dependency> ();
        // create init collection of brokenDependencies
        Utilities.findRequiredUpdateElements (element, infos, brokenDependencies);
        // backward compatibility
        for (ModuleInfo mi : infos) {
            UpdateUnit u = UpdateManagerImpl.getInstance ().getUpdateUnit (mi.getCodeNameBase ());
            // invalid codenamebase (in unit tests)
            if (u == null) {
                continue;
            }
            // not installed, not need to handle backward compatability
            UpdateElement i = u.getInstalled ();
            if (i == null) {
                continue;
            }
            // maybe newer version is processed as a update
            if (! u.getAvailableUpdates ().isEmpty ()) {
                UpdateElement ue = u.getAvailableUpdates ().get (0);
                ModuleInfo newerMI = ((ModuleUpdateElementImpl) Trampoline.API.impl (ue)).getModuleInfo ();
                if (infos.contains (newerMI)) {
                    continue;
                }
            }
            // Dependency.TYPE_MODULE
            for (Dependency d : Dependency.create (Dependency.TYPE_MODULE, mi.getCodeName ())) {
                DependencyAggregator deco = DependencyAggregator.getAggregator (d);
                if (deco != null) {
                    for (ModuleInfo depMI : deco.getDependening ()) {
                        Module depM = Utilities.toModule (depMI);
                        if (depM == null) {
                            continue;
                        }
                        if (depM.getProblems () != null && ! depM.getProblems ().isEmpty ()) {
                            // skip this module because it has own problems already
                            continue;
                        }
                        for (Dependency toTry : depM.getDependencies ()) {
                            // check only relevant deps
                            if (deco.equals (DependencyAggregator.getAggregator (toTry)) &&
                                    ! DependencyChecker.checkDependencyModule (toTry, mi)) {
                                brokenDependencies.add (toTry);
                            }
                        }
                    }
                }
            }
            // Dependency.TYPE_REQUIRES
            // Dependency.TYPE_NEEDS
            SortedSet<String> newTokens = new TreeSet<String> (Arrays.asList (mi.getProvides ()));
            SortedSet<String> oldTokens = new TreeSet<String> (Arrays.asList (((ModuleUpdateElementImpl) Trampoline.API.impl (i)).getModuleInfo ().getProvides ()));
            oldTokens.removeAll (newTokens);
            // handle diff
            for (String tok : oldTokens) {
                Collection<Dependency> deps = new HashSet<Dependency> (Dependency.create (Dependency.TYPE_REQUIRES, tok));
                deps.addAll (Dependency.create (Dependency.TYPE_NEEDS, tok));
                for (Dependency d : deps) {
                    DependencyAggregator deco = DependencyAggregator.getAggregator (d);
                    if (deco != null) {
                        for (ModuleInfo depMI : deco.getDependening ()) {
                            Module depM = Utilities.toModule (depMI);
                            if (depM == null) {
                                continue;
                            }
                            if (depM.getProblems () != null && ! depM.getProblems ().isEmpty ()) {
                                // skip this module because it has own problems already
                                continue;
                            }
                            for (Dependency toTry : depM.getDependencies ()) {
                                // check only relevant deps
                                if (deco.equals (DependencyAggregator.getAggregator (toTry))) {
                                    brokenDependencies.add (toTry);
                                }
                            }
                        }
                    }
                }
            }
        }
        Set<String> retval = new HashSet<String> (brokenDependencies.size ());
        for (Dependency dep : brokenDependencies) {
            retval.add (dep.toString ());
        }
        return retval;
    }
    
    static Set<String> getBrokenDependenciesInInstalledModules (UpdateElement element) {
        assert element != null : "UpdateElement cannot be null";
        Set<Dependency> deps = new HashSet<Dependency> ();
        for (ModuleInfo m : getModuleInfos (Collections.singleton (element))) {
            deps.addAll (DependencyChecker.findBrokenDependenciesTransitive (m,
                    InstalledModuleProvider.getInstalledModules ().values (),
                    new HashSet<ModuleInfo> ()));
        }
        Set<String> retval = new HashSet<String> ();
        for (Dependency dep : deps) {
            retval.add (dep.toString ());
        }
        return retval;
    }
    
    private static List<ModuleInfo> getModuleInfos (Collection<UpdateElement> elements) {
        List<ModuleInfo> infos = new ArrayList<ModuleInfo> (elements.size ());
        for (UpdateElement el : elements) {
            if (el.getUpdateUnit () != null && el.getUpdateUnit ().isPending ()) {
                // cannot depend of UpdateElement in pending state
                continue;
            }
            UpdateElementImpl impl = Trampoline.API.impl (el);
            infos.addAll (impl.getModuleInfos ());
        }
        return infos;
    }
    
    private static Module getModuleInstance(String codeNameBase, SpecificationVersion specificationVersion) {
        if (mgr == null) {
            mgr = Main.getModuleSystem().getManager();
        }
        assert mgr != null;
        if (mgr == null || specificationVersion == null) {
            return mgr != null ? mgr.get(codeNameBase) : null;
        } else {
            Module m = mgr.get(codeNameBase);
            if (m == null) {
                return null;
            } else {
                if (m.getSpecificationVersion () == null) {
                    return null;
                }
                return m.getSpecificationVersion ().compareTo (specificationVersion) >= 0 ? m : null;
            }
        }
    }
    
    public static boolean isAutomaticallyEnabled(String codeNameBase) {
        Module m = getModuleInstance(codeNameBase, null);
        return m != null ? (m.isAutoload() || m.isEager() || m.isFixed()) : false;
    }
    
    public static ModuleInfo takeModuleInfo (UpdateElement el) {
        UpdateElementImpl impl = Trampoline.API.impl (el);
        assert impl instanceof ModuleUpdateElementImpl;
        return ((ModuleUpdateElementImpl) impl).getModuleInfo ();
    }
    
    private static String productVersion = null;
    
    public static String getProductVersion () {
        if (productVersion == null) {
            String buildNumber = System.getProperty ("netbeans.buildnumber"); // NOI18N
            productVersion = NbBundle.getMessage (TopLogging.class, "currentVersion", buildNumber); // NOI18N
        }
        return productVersion;
    }
    
    private static Node getModuleConfiguration (File moduleUpdateTracking) {
        Document document = null;
        InputStream is;
        try {
            is = new BufferedInputStream (new FileInputStream (moduleUpdateTracking));
            InputSource xmlInputSource = new InputSource (is);
            document = XMLUtil.parse (xmlInputSource, false, false, null, org.openide.xml.EntityCatalog.getDefault ());
            if (is != null) {
                is.close ();
            }
        } catch (SAXException saxe) {
            getLogger ().log (Level.INFO, "SAXException when reading " + moduleUpdateTracking, saxe);
            return null;
        } catch (IOException ioe) {
            getLogger ().log (Level.INFO, "IOException when reading " + moduleUpdateTracking, ioe);
            return null;
        }

        assert document.getDocumentElement () != null : "File " + moduleUpdateTracking + " must contain <module> element.";
        if (document.getDocumentElement () == null) {
            return null;
        }
        return getModuleElement (document.getDocumentElement ());
    }
    
    private static Node getModuleElement (Element element) {
        Node lastElement = null;
        assert UpdateTracking.ELEMENT_MODULE.equals (element.getTagName ()) : "The root element is: " + UpdateTracking.ELEMENT_MODULE + " but was: " + element.getTagName ();
        NodeList listModuleVersions = element.getElementsByTagName (UpdateTracking.ELEMENT_VERSION);
        for (int i = 0; i < listModuleVersions.getLength (); i++) {
            lastElement = getModuleLastVersion (listModuleVersions.item (i));
            if (lastElement != null) {
                break;
            }
        }
        return lastElement;
    }
    
    private static Node getModuleLastVersion (Node version) {
        Node attrLast = version.getAttributes ().getNamedItem (UpdateTracking.ATTR_LAST);
        assert attrLast != null : "ELEMENT_VERSION must contain ATTR_LAST attribute.";
        if (Boolean.valueOf (attrLast.getNodeValue ()).booleanValue ()) {
            return version;
        } else {
            return null;
        }
    }
    
    private static File getAdditionalInformation (File root) {
        File file = new File (root.getPath () + FILE_SEPARATOR + DOWNLOAD_DIR + 
                FILE_SEPARATOR + UpdateTracking.ADDITIONAL_INFO_FILE_NAME);
        return file;
    }

    private static void writeAdditionalInformationToCluster (File cluster, Map<UpdateElementImpl, File> updates) {
        if (updates.isEmpty ()) {
            return ;
        }
        
        Document document = XMLUtil.createDocument (UpdateTracking.ELEMENT_ADDITIONAL, null, null, null);                
        Element root = document.getDocumentElement ();
        boolean isEmpty = true;
        
        for (UpdateElementImpl impl : updates.keySet ()) {
            File c = updates.get (impl);
            // pass this module to given cluster ?
            if (cluster.equals (c)) {
                Element module = document.createElement (UpdateTracking.ELEMENT_ADDITIONAL_MODULE);
                module.setAttribute(ATTR_NBM_NAME,
                        InstallSupportImpl.getDestination (cluster, impl.getCodeName(), true).getName ());
                module.setAttribute (UpdateTracking.ATTR_ADDITIONAL_SOURCE, impl.getSource ());
                root.appendChild( module );
                isEmpty = false;
            }
        }
        
        if (isEmpty) {
            return ;
        }
        
        writeXMLDocumentToFile (document, getAdditionalInformation (cluster));
    }
    
    public static UpdateItem createUpdateItem (UpdateItemImpl impl) {
        assert Trampoline.SPI != null;
        return Trampoline.SPI.createUpdateItem (impl);
    }
    
    public static UpdateItemImpl getUpdateItemImpl (UpdateItem item) {
        assert Trampoline.SPI != null;
        return Trampoline.SPI.impl (item);
    }
    
    public static boolean canDisable (Module m) {
        return m != null &&  m.isEnabled () && ! isEssentialModule (m) && ! m.isAutoload () && ! m.isEager ();
    }
    
    public static boolean canEnable (Module m) {
        return m != null && !m.isEnabled () && ! m.isAutoload () && ! m.isEager ();
    }
    
    public static boolean isElementInstalled (UpdateElement el) {
        assert el != null : "Invalid call isElementInstalled with null parameter.";
        if (el == null) {
            return false;
        }
        return el.equals (el.getUpdateUnit ().getInstalled ());
    }
    
    public static boolean isKitModule (ModuleInfo mi) {
        // XXX: it test can break simple modules mode
        // should find corresponing UpdateElement and check its type
        Object o = mi.getAttribute (ATTR_VISIBLE);
        return o == null || Boolean.parseBoolean (o.toString ());
    }
    
    public static boolean isEssentialModule (ModuleInfo mi) {
        Object o = mi.getAttribute (ATTR_ESSENTIAL);
        return isFixed (mi) || (o != null && Boolean.parseBoolean (o.toString ()));
    }
    
    private static Logger getLogger () {
        if (err == null) {
            err = Logger.getLogger (Utilities.class.getName ());
        }
        return err;
    }
    
    /** Finds modules depending on given module.
     * @param m a module to start from; may be enabled or not, but must be owned by this manager
     * @return a set (possibly empty) of modules managed by this manager, never including m
     */
    public static Set<Module> findRequiredModules (Module m, ModuleManager mm, Map<Module, Set<Module>> m2reqs) {
        Set<Module> res = null;
        if (m2reqs != null) {
            res = m2reqs.get (m);
            if (res == null) {
                res = mm.getModuleInterdependencies(m, false, false);
                m2reqs.put (m, res);
            }
        } else {
            res = mm.getModuleInterdependencies(m, false, false);
        }
        return res;
    }
    
    /** Finds for modules given module depends upon.
     * @param m a module to start from; may be enabled or not, but must be owned by this manager
     * @return a set (possibly empty) of modules managed by this manager, never including m
     */
    public static Set<Module> findDependingModules (Module m, ModuleManager mm, Map<Module, Set<Module>> m2deps) {
        Set<Module> res = null;
        if (m2deps != null) {
            res = m2deps.get (m);
            if (res == null) {
                res = mm.getModuleInterdependencies(m, true, false);
                m2deps.put (m, res);
            }
        } else {
            res = mm.getModuleInterdependencies(m, true, false);
        }
        return res;
    }
    
    public static String formatDate(Date date) {
        synchronized(DATE_FORMAT) {
            return DATE_FORMAT.format(date);
        }
    }

    public static Date parseDate(String date) throws ParseException {
        synchronized(DATE_FORMAT) {
            return DATE_FORMAT.parse(date);
        }
    }    
    
    public static boolean canWriteInCluster (File cluster) {
        assert cluster != null : "dir cannot be null";
        if (cluster == null) {
            return false;
        }
        if (cluster.exists () && cluster.isDirectory ()) {
            File dir4test = null;
            File update = new File (cluster, UPDATE_DIR);
            File download = new File (cluster, DOWNLOAD_DIR);
            if (download.exists ()) {
                dir4test = download;
            } else if (update.exists ()) {
                dir4test = update;
            } else {
                dir4test = cluster;
            }
            // workaround the bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4420020
            if (dir4test.canWrite () && dir4test.canRead ()) {
                boolean canWrite = canWrite (dir4test);
                getLogger ().log (Level.FINE, "Can write into " + dir4test + "? " + canWrite);
                return canWrite;
            } else {
                getLogger ().log (Level.FINE, "Can write into " + dir4test + "? " + dir4test.canWrite ());
                return dir4test.canWrite ();
            }
        }
        
        cluster.mkdirs ();
        getLogger ().log (Level.FINE, "Can write into new cluster " + cluster + "? " + cluster.canWrite ());
        return cluster.canWrite ();
    }
    
    public static boolean canWrite (File f) {
        // workaround the bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4420020
        if (org.openide.util.Utilities.isWindows ()) {
            if (f.isFile ()) {
                FileWriter fw = null;
                try {
                    fw = new FileWriter (f, true);
                    getLogger ().log (Level.FINE, f + " has write permission");
                } catch (IOException ioe) {
                    // just check of write permission
                    getLogger ().log (Level.FINE, f + " has no write permission", ioe);
                    return false;
                } finally {
                    try {
                        if (fw != null) {
                            fw.close ();
                        }
                    } catch (IOException ex) {
                        getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
                    }
                }
                return true;
            } else {
                try {
                    File dummy = File.createTempFile ("dummy", null, f);
                    dummy.delete ();
                    getLogger ().log (Level.FINE, f + " has write permission");
                } catch (IOException ioe) {
                    getLogger ().log (Level.FINE, f + " has no write permission", ioe);
                    return false;
                }
                return true;
            }
        } else {
            return f.canWrite ();
        }
    }
    
    public static KeyStore loadKeyStore () {
        String fileName = getPreferences ().get (USER_KS_KEY, null);
        if (fileName == null) {
            return null;
        } else {
            InputStream is = null;
            KeyStore ks = null;
            try {
                File f = new File (getCacheDirectory (), fileName);
                if (! f.exists ()) {
                    return null;
                }
                is = new BufferedInputStream (new FileInputStream (f));
                ks = KeyStore.getInstance (KeyStore.getDefaultType ());
                ks.load (is, KS_USER_PASSWORD.toCharArray ());
            } catch (IOException ex) {
                getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
            } catch (NoSuchAlgorithmException ex) {
                getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
            } catch (CertificateException ex) {
                getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
            } catch (KeyStoreException ex) {
                getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
            } finally {
                try {
                    if (is != null) {
                        is.close ();
                    }
                } catch (IOException ex) {
                    getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
                }
            }
            return ks;
        }
    }
    
    private static void storeKeyStore (KeyStore ks) {
        OutputStream os = null;
        try {
            File f = new File (getCacheDirectory (), USER_KS_FILE_NAME);
            os = new BufferedOutputStream (new FileOutputStream (f));
            ks.store (os, KS_USER_PASSWORD.toCharArray ());
            getPreferences ().put (USER_KS_KEY, USER_KS_FILE_NAME);
        } catch (KeyStoreException ex) {
            getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
        } catch (IOException ex) {
            getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
        } catch (NoSuchAlgorithmException ex) {
            getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
        } catch (CertificateException ex) {
            getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
            } finally {
                try {
                    if (os != null) {
                        os.close ();
                    }
                } catch (IOException ex) {
                    getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
                }
            }
    }
    
    public static void addCertificates (Collection<Certificate> certs) {
        KeyStore ks = loadKeyStore ();
        if (ks == null) {
            try {
                ks = KeyStore.getInstance (KeyStore.getDefaultType ());
                ks.load (null, KS_USER_PASSWORD.toCharArray ());
            } catch (IOException ex) {
                getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
                return ;
            } catch (NoSuchAlgorithmException ex) {
                getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
                return ;
            } catch (CertificateException ex) {
                getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
                return ;
            } catch (KeyStoreException ex) {
                getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
                return ;
            }
        }

        for (Certificate c : certs) {
            
            try {
                // don't add certificate twice
                if (ks.getCertificateAlias (c) != null) {
                    continue;
                }

                // Find free alias name
                String alias = null;
                for (int i = 0; i < 9999; i++) {
                    alias = "genAlias" + i; // NOI18N
                    if (! ks.containsAlias (alias)) {
                        break;
                    }
                }
                if (alias == null) {
                    getLogger ().log (Level.INFO, "Too many certificates with " + c);
                }

                ks.setCertificateEntry (alias, c);
            } catch (KeyStoreException ex) {
                getLogger ().log (Level.INFO, ex.getLocalizedMessage (), ex);
            }
            
        }
        
        storeKeyStore (ks);
    }

    private static File getCacheDirectory () {
        File cacheDir = null;
        String userDir = System.getProperty ("netbeans.user"); // NOI18N
        if (userDir != null) {
            cacheDir = new File (new File (new File (userDir, "var"), "cache"), "catalogcache"); // NOI18N
        } else {
            File dir = FileUtil.toFile (FileUtil.getConfigRoot());
            cacheDir = new File (dir, "catalogcache"); // NOI18N
        }
        cacheDir.mkdirs();
        return cacheDir;
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.root ().node ("/org/netbeans/modules/autoupdate"); // NOI18N
    }    
    
}
