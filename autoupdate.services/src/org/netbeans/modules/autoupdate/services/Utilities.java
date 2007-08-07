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

package org.netbeans.modules.autoupdate.services;

import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.core.startup.Main;
import org.netbeans.core.startup.TopLogging;
import org.netbeans.spi.autoupdate.KeyStoreProvider;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.updater.ModuleUpdater;
import org.netbeans.updater.UpdateTracking;
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
    
    public static final String UPDATE_DIR = "update"; // NOI18N
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String DOWNLOAD_DIR = UPDATE_DIR + FILE_SEPARATOR + "download"; // NOI18N
    public static final String NBM_EXTENTSION = ".nbm";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat ("yyyy/MM/dd"); // NOI18N
    public static final String ATTR_VISIBLE = "AutoUpdate-Show-In-Client";
    public static final String ATTR_ESSENTIAL = "AutoUpdate-Essential-Module";
    
    
    private static Lookup.Result<KeyStoreProvider> result;
    private static Logger err = null;
    private static ModuleManager mgr = null;
    private static Reference<Map<Module, Set<Module>>> mapModule2dependingModules = new WeakReference<Map<Module, Set<Module>>> (new HashMap<Module, Set<Module>> ());
    private static Reference<Map<Module, Set<Module>>> mapModule2requiredModules = new WeakReference<Map<Module, Set<Module>>> (new HashMap<Module, Set<Module>> ());
    
    
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
    
    private static File getInstall_Later(File root) {
        File file = new File(root.getPath() + FILE_SEPARATOR + DOWNLOAD_DIR + FILE_SEPARATOR + ModuleUpdater.LATER_FILE_NAME);
        return file;
    }

    public static void deleteInstall_Later() {
        List<File> clusters = UpdateTracking.clusters(true);
        assert clusters != null : "Clusters cannot be empty."; // NOI18N
        Iterator iter =  clusters.iterator();
        while (iter.hasNext()) {
            File installLaterFile = getInstall_Later((File)iter.next());
            if (installLaterFile != null && installLaterFile.exists()) {
                installLaterFile.delete();                
            }
        }                                
    }
    
    public static void writeInstall_Later(Map<UpdateElementImpl, File> updates) {
        // loop for all clusters and write if needed
        List<File> clusters = UpdateTracking.clusters(true);
        assert clusters != null : "Clusters cannot be empty."; // NOI18N
        for (File cluster : clusters) {
            writeToCluster (cluster, updates);
        }
    }
    
    private static void writeToCluster (File cluster, Map<UpdateElementImpl, File> updates) {
        Document document = XMLUtil.createDocument(UpdateTracking.ELEMENT_MODULES, null, null, null);                
        
        Element root = document.getDocumentElement();

        if (updates.isEmpty ()) {
            return ;
        }
        
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
            }
        }
        
        document.getDocumentElement().normalize();
                
        File installLaterFile = getInstall_Later (cluster);
        installLaterFile.getParentFile ().mkdirs ();
        InputStream is = null;
        ByteArrayOutputStream  bos = new ByteArrayOutputStream ();        
        OutputStream fos = null;            
            try {
                try {
                    XMLUtil.write(document, bos, "UTF-8"); // NOI18N
                    if (bos != null) bos.close();
                    fos = new FileOutputStream(installLaterFile);
                    is = new ByteArrayInputStream(bos.toByteArray());
                    FileUtil.copy(is,fos);
                } finally {
                    if (is != null) is.close();
                    if (fos != null) fos.close();
                    if (bos != null) bos.close();
                }                
            } catch (java.io.FileNotFoundException fnfe) {
                Exceptions.printStackTrace(fnfe);
            } catch (java.io.IOException ioe) {
                Exceptions.printStackTrace(ioe);
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (Exception x) {
                        Exceptions.printStackTrace(x);
                    }
                }
            }
            
        }
    
    public static void writeAdditionalInformation (Map<UpdateElementImpl, File> updates) {
        // loop for all clusters and write if needed
        List<File> clusters = UpdateTracking.clusters (true);
        assert clusters != null : "Clusters cannot be empty."; // NOI18N
        Iterator iter =  clusters.iterator ();
        while (iter.hasNext ()) {
            writeAdditionalInformationToCluster ((File) iter.next (), updates);
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
    
    static Module toModule(UpdateUnit uUnit) {
        return getModuleInstance(uUnit.getCodeName(), null); // XXX
    }
    
    public static Module toModule(String codeNameBase, String specificationVersion) {
        return getModuleInstance(codeNameBase, specificationVersion);
    }
    
    public static Module toModule (ModuleInfo info) {
        return getModuleInstance (info.getCodeNameBase(), info.getSpecificationVersion ().toString ());
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
    
    private static Set<Dependency> takeDependencies(UpdateElement el) {
        UpdateElementImpl i = Trampoline.API.impl(el);
        assert UpdateManager.TYPE.MODULE == i.getType () || UpdateManager.TYPE.KIT_MODULE == i.getType () : "Only for UpdateElement for modules.";
        return takeModuleInfo (el).getDependencies();
    }
    
    @SuppressWarnings ("deprecation") //Dependency.TYPE_IDE must be handled for backward compatability
    private static UpdateElement findRequiredModule (Dependency dep, Collection<ModuleInfo> installedModules) {
        switch (dep.getType ()) {
            case (Dependency.TYPE_REQUIRES) :
                // find if some module fit the dependency
                ModuleInfo info = DependencyChecker.findModuleMatchesDependencyRequires (dep, installedModules);
                if (info != null) {
                    // it's Ok, no module is required
                } else {
                    // find corresponding UpdateUnit
                    for (UpdateUnit unit : UpdateManagerImpl.getInstance ().getUpdateUnits (UpdateManager.TYPE.MODULE)) {
                        assert unit != null : "UpdateUnit for " + info.getCodeName() + " found.";
                        // find correct UpdateElement
                        // installed module can ignore here
                        if (unit.getAvailableUpdates ().size () > 0) {
                            for (UpdateElement el : unit.getAvailableUpdates ()) {
                                UpdateElementImpl impl = Trampoline.API.impl (el);
                                List<ModuleInfo> moduleInfos = impl.getModuleInfos ();
                                for (ModuleInfo moduleInfo : moduleInfos) {
                                    if (Arrays.asList (moduleInfo.getProvides ()).contains (dep.getName ())) {
                                        return el;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                break;
            case (Dependency.TYPE_MODULE) :
                String moduleName = dep.getName();
                UpdateUnit unit = UpdateManagerImpl.getInstance ().getUpdateUnit(moduleName);

                // there is no fit module
                if (unit == null) {
                    return null;
                }

                if (unit.getInstalled () != null) {
                    // check if version match
                    UpdateElement installedEl = unit.getInstalled ();
                    UpdateElementImpl installedElImpl = Trampoline.API.impl(installedEl);
                    List<ModuleInfo> installedModuleInfos = installedElImpl.getModuleInfos ();
                    for (ModuleInfo installedModuleInfo : installedModuleInfos) {
                        if (DependencyChecker.checkDependencyModule (dep, installedModuleInfo)) {
                            return null;
                        }
                    }

                }

                // find available modules
                List<UpdateElement> elements = unit.getAvailableUpdates();
                for (UpdateElement el : elements) {
                    UpdateElementImpl impl = Trampoline.API.impl(el);
                    if (impl instanceof ModuleUpdateElementImpl) {
                        ModuleInfo moduleInfo = impl.getModuleInfos ().get (0);
                        if (DependencyChecker.checkDependencyModule (dep, moduleInfo)) {
                            return el;
                        }
                    } else {
                        // XXX: maybe useful later, now I don't need it
                        assert false : "Not implemented yet.";
                        //FeatureItem i = (FeatureItem) impl.getUpdateItemImpl();
                        //i.getDependenciesToModules();
                    }
                }

                break;
            case (Dependency.TYPE_IDE) :
                getLogger ().log (Level.FINE, "Check dependency on IDE. Dependency: " + dep);
                break;
            case (Dependency.TYPE_JAVA) :
                getLogger ().log (Level.FINE, "Check dependency on Java platform. Dependency: " + dep);
                break;
            default:
                //assert false : "Unknown type of Dependency, was " + dep.getType ();
                getLogger ().log (Level.FINE, "Uncovered Dependency " + dep);                    
                break;
        }
        return null;
    }
    
    static Set<UpdateElement> findRequiredModules(Set<Dependency> deps, Collection<ModuleInfo> installedModules) {
        Set<UpdateElement> requiredElements = new HashSet<UpdateElement> ();
        for (Dependency dep : deps) {
            UpdateElement el = findRequiredModule (dep, installedModules);
            if (el != null) {
                UpdateElementImpl elImpl = Trampoline.API.impl(el);
                List<ModuleInfo> mInfos = elImpl.getModuleInfos ();
                assert mInfos != null;
                if (!installedModules.containsAll (mInfos)) {
                    requiredElements.add(el);
                    installedModules.add(takeModuleInfo(el));
        }
            }
        }
        // check dependencies of extended modules as well
        for (UpdateElement el : new HashSet<UpdateElement> (requiredElements)) {
            requiredElements.addAll(findRequiredModules(takeDependencies(el), installedModules));
        }
        
        return requiredElements;
    }
    
    private static List<ModuleInfo> getInstalledModules() {
        return new ArrayList<ModuleInfo> (InstalledModuleProvider.getInstalledModules().values());
    }
    
    public static Set<UpdateElement> findRequiredUpdateElements (UpdateElement element, List<ModuleInfo> infos) {
        UpdateElementImpl el = Trampoline.API.impl(element);
        Set<UpdateElement> retval = new HashSet<UpdateElement> ();
        switch (el.getType ()) {
        case KIT_MODULE :
        case MODULE :
            final Set<Dependency> deps = ((ModuleUpdateElementImpl) el).getModuleInfo ().getDependencies ();
            final List<ModuleInfo> extendedModules = getInstalledModules ();
            extendedModules.addAll (infos);
            final Set<Dependency> brokenDeps = DependencyChecker.findBrokenDependencies (deps, extendedModules);
            retval = findRequiredModules (brokenDeps, extendedModules);
            break;
        case STANDALONE_MODULE :
        case FEATURE :
            FeatureUpdateElementImpl feature = (FeatureUpdateElementImpl) el;
            for (ModuleUpdateElementImpl module : feature.getContainedModuleElements ()) {
                retval.addAll (findRequiredUpdateElements (module.getUpdateElement (), infos));
            }
            break;
        default:
            assert false : "Not implement for type " + el.getType () + " of UpdateElement " + el;
        }
        return retval;
    }
    
    public static Set<Dependency> findBrokenDependencies(UpdateElement element, List<ModuleInfo> infos) {
        UpdateElementImpl el = Trampoline.API.impl (element);
        assert el != null : "UpdateElementImpl found for UpdateElement " + element;
        Set<Dependency> retval = Collections.emptySet ();
        List<ModuleInfo> mInfos = null;
        switch (el.getType ()) {
        case KIT_MODULE :
        case MODULE :
            mInfos = el.getModuleInfos ();
            break;
        case STANDALONE_MODULE :
        case FEATURE :
            mInfos = el.getModuleInfos ();
            break;
        case CUSTOM_HANDLED_COMPONENT : // XXX: CUSTOM_HANDLED_COMPONENT should support UpdateItem<->UpdateItem dependencies
            mInfos = Collections.emptyList ();
            getLogger ().log (Level.INFO, "CUSTOM_HANDLED_COMPONENT should support UpdateItem<->UpdateItem dependencies.");
            break;
        default:
            assert false : "Unsupported for " + element + "[impl: " + el.getClass() + "]";
        }
        final Set<Dependency> deps = new HashSet<Dependency> ();
        for (ModuleInfo info : mInfos) {
            deps.addAll (info.getDependencies ());
        }
        List<ModuleInfo> extendedModules = getInstalledModules();
        extendedModules.addAll(infos);
        final Set<Dependency> brokenDeps = DependencyChecker.findBrokenDependencies(deps, extendedModules);
        Set<UpdateElement> reqs = findRequiredModules(brokenDeps, extendedModules);
        extendedModules.addAll (getModuleInfos (reqs));
        retval = DependencyChecker.findBrokenDependencies(deps, extendedModules);
        return retval;
    }
    
    static Set<String> getBrokenDependencies (UpdateElement element, List<ModuleInfo> infos) {
        assert element != null : "UpdateElement cannot be null";
        Set<String> retval = new HashSet<String> ();
        for (Dependency dep : findBrokenDependencies (element, infos)) {
            retval.add (dep.toString ());
        }
        return retval;
    }
    
    static List<ModuleInfo> getModuleInfos (Collection<UpdateElement> elements) {
        List<ModuleInfo> infos = new ArrayList<ModuleInfo> (elements.size ());
        for (UpdateElement el : elements) {
            UpdateElementImpl impl = Trampoline.API.impl (el);
            infos.addAll (impl.getModuleInfos ());
        }
        return infos;
    }
    
    private static Module getModuleInstance(String codeNameBase, String specificationVersion) {
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
                return m.getSpecificationVersion ().compareTo (new SpecificationVersion (specificationVersion)) >= 0 ? m : null;
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
    
    public static String getProductVersion () {
        String buildNumber = System.getProperty ("netbeans.buildnumber"); // NOI18N
        return  NbBundle.getMessage (TopLogging.class, "currentVersion", buildNumber ); // NOI18N
    }
    
    private static Node getModuleConfiguration (File moduleUpdateTracking) {
        Document document = null;
        InputStream is;
        try {
            is = new FileInputStream (moduleUpdateTracking);
            InputSource xmlInputSource = new InputSource (is);
            document = XMLUtil.parse (xmlInputSource, false, false, null, org.openide.xml.EntityCatalog.getDefault ());
            if (is != null) {
                is.close ();
            }
        } catch (SAXException saxe) {
            getLogger ().log (Level.WARNING, null, saxe);
            return null;
        } catch (IOException ioe) {
            getLogger ().log (Level.WARNING, null, ioe);
        }

        assert document.getDocumentElement () != null : "File " + moduleUpdateTracking + " must contain <module> element.";
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
        for (UpdateElementImpl impl : updates.keySet ()) {
            File c = updates.get (impl);
            // pass this module to given cluster ?
            if (cluster.equals (c)) {
                Element module = document.createElement (UpdateTracking.ELEMENT_ADDITIONAL_MODULE);
                module.setAttribute(ATTR_NBM_NAME,
                        InstallSupportImpl.getDestination (cluster, impl.getCodeName(), true).getName ());
                module.setAttribute (UpdateTracking.ATTR_ADDITIONAL_SOURCE, impl.getSource ());
                root.appendChild( module );
            }
        }

        document.getDocumentElement ().normalize ();
                
        File additionalFile = getAdditionalInformation (cluster);
        additionalFile.getParentFile ().mkdirs ();
        InputStream is = null;
        ByteArrayOutputStream  bos = new ByteArrayOutputStream ();        
        OutputStream fos = null;            
        try {
            try {
                XMLUtil.write (document, bos, "UTF-8"); // NOI18N
                fos = new FileOutputStream (additionalFile);
                is = new ByteArrayInputStream (bos.toByteArray ());
                FileUtil.copy (is, fos);
            } finally {
                if (is != null) is.close();
                if (fos != null) fos.close();
                if (bos != null) bos.close();
            }                
        } catch (java.io.FileNotFoundException fnfe) {
            Exceptions.printStackTrace(fnfe);
        } catch (java.io.IOException ioe) {
            Exceptions.printStackTrace(ioe);
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (Exception x) {
                    Exceptions.printStackTrace(x);
                }
            }
        }

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
            return m != null && !m.isEnabled () && !m.isFixed () && !m.isAutoload () && !m.isEager ();
    }
    
    public static boolean isElementInstalled (UpdateElement el) {
        assert el != null : "Invalid call isElementInstalled with null parameter.";
        if (el == null) {
            return false;
        }
        return el.equals (el.getUpdateUnit ().getInstalled ());
    }
    
    private static boolean isLeafModule (ModuleInfo mi) {
        return isKitModule (mi) || isEssentialModule (mi);
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
    
    /** Finds modules depending on given module and recursive over module's dependencies.
     * @param m a module to start from; may be enabled or not, but must be owned by this manager
     * @return a set (possibly empty) of modules managed by this manager, never including m
     */
    public static Set<Module> findDependingModules (Module m, ModuleManager mm) {
        synchronized (Utilities.class) {
            if (mapModule2dependingModules.get () == null) {
                mapModule2dependingModules = new WeakReference<Map<Module, Set<Module>>> (new HashMap<Module, Set<Module>> ());
                getLogger ().log (Level.FINEST, "Was created new reference for mapModule2dependingModules");
            }
        }
        if (! mapModule2dependingModules.get ().containsKey (m)) {
            mapModule2dependingModules.get ().put (m, doFindDependingModules (m, mm));
        }
        return mapModule2dependingModules.get ().get (m);
    }

    /** Finds for modules given module depends upon. Finding is recursive over module's dependencies.
     * Finding ends on KIT_MODULE or ESSENTIAL_MODULE. Found KIT_MODULE is also included.
     * @param m a module to start from; may be enabled or not, but must be owned by this manager
     * @return a set (possibly empty) of modules managed by this manager, never including m
     */
    public static Set<Module> findRequiredModules (Module m, ModuleManager mm, boolean forceToGoDeep) {
        synchronized (Utilities.class) {
            if (mapModule2requiredModules.get () == null) {
                mapModule2requiredModules = new WeakReference<Map<Module, Set<Module>>> (new HashMap<Module, Set<Module>> ());
                getLogger ().log (Level.FINEST, "Was created new reference for mapModule2requiredModules");
            }
        }
        if (! mapModule2requiredModules.get ().containsKey (m)) {
            mapModule2requiredModules.get ().put (m, doFindRequiredModules (m, mm, forceToGoDeep));
        }
        return mapModule2requiredModules.get ().get (m);
    }

    private static Set<Module> doFindRequiredModules (Module m, ModuleManager mm, boolean forceToGoDeep) {
        Set<Module> res = Collections.emptySet ();
        if (forceToGoDeep || ! isLeafModule (m)) {
            res = new HashSet<Module> ();
            for (Object depO : mm.getModuleInterdependencies (m, false, false)) {
                assert depO instanceof Module : depO + " is instanceof Module";
                Module depM = (Module) depO;
                if (! isEssentialModule (depM)) {
                    res.add (depM);
                    res.addAll (findRequiredModules (depM, mm, false));
                }
            }
        }
        return res;
    }
    
    private static Set<Module> doFindDependingModules (Module m, ModuleManager mm) {
        Set<Module> res = new HashSet<Module> ();
        for (Object depO : mm.getModuleInterdependencies (m, true, true)) {
            assert depO instanceof Module : depO + " is instanceof Module";
            Module depM = (Module) depO;
            res.add (depM);
        }
        return res;
    }
    
}
