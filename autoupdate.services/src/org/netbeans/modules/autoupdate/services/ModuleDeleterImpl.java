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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.services;

import java.util.logging.Logger;
import org.netbeans.Module;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/** Control if the module's file can be deleted and can delete them from disk.
 * <p> Deletes all files what are installed together with given module, info about
 * these files read from <code>update_tracking</code> file corresponed to the module.
 * If this <code>update_tracking</code> doesn't exist the files cannot be deleted.
 * The Deleter waits until the module is enabled before start delete its files.
 *
 * @author  Jiri Rechtacek
 */
public final class ModuleDeleterImpl  {
    private static final ModuleDeleterImpl INSTANCE = new ModuleDeleterImpl();
    private static final String ELEMENT_MODULE = "module"; // NOI18N
    private static final String ELEMENT_VERSION = "module_version"; // NOI18N
    private static final String ATTR_ORIGIN = "origin"; // NOI18N
    private static final String ATTR_LAST = "last"; // NOI18N
    private static final String ATTR_FILE_NAME = "name"; // NOI18N
    private static final String UPDATE_TRACKING = "update_tracking"; // NOI18N
    private static final String INST_ORIGIN = "updater"; // NOI18N
    private static final boolean ONLY_FROM_AUTOUPDATE = false;
    private static final int TIME_TO_CHECK = 2000;
    private static final int MAX_CHECKS_OF_STATE = 50;
    private static final int HOLD_ON_PROPAGATE_DISABLE = 1000;
    
    private Logger err = Logger.getLogger("org.netbeans.modules.autoupdate.catalog.ModuleDeleterImpl"); // NOI18N
    
    public static ModuleDeleterImpl getInstance() {
        return INSTANCE;
    }
    
    public boolean canDelete (Module module) {
        if (module.isFixed ()) {
            err.log(Level.FINE,
                    "Cannot delete module because module " +
                    module.getCodeName() + " isFixed.");
        } else if (module.isAutoload ()) {
            err.log(Level.FINE,
                    "Cannot delete module because module " +
                    module.getCodeName() + " isAutoload. See issue #74819.");
        } else if (module.isEager ()) {
            err.log(Level.FINE,
                    "Cannot delete module because module " +
                    module.getCodeName() + " isEager. See issue #74819.");
        }
        return isUninstallAllowed (module) && findUpdateTracking (module, ONLY_FROM_AUTOUPDATE);
    }
    
    public static boolean isUninstallAllowed(final Module m) {
        return ! (m.isAutoload () || m.isEager () || m.isFixed ());
    }
    
    public void delete (final Module[] modules) throws IOException {
        if (modules == null) {
            throw new IllegalArgumentException ("Module argument cannot be null.");
        }
        
        for (Module module : modules) {
            err.log(Level.FINE,"Locate and remove config file of " + module.getCodeNameBase ());           
            removeControlModuleFile(module);
        }

        new HackModuleListRefresher().run();
        int rerunWaitCount = 0;
        for (Module module : modules) {
            err.log(Level.FINE,"Locate and remove config file of " + module.getCodeNameBase ());                       
            for (; rerunWaitCount < 100 && !isModuleUninstalled(module) ;rerunWaitCount++) {
                try {
                    Thread.currentThread().sleep(100);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            removeModuleFiles(module); 
        }
    }
    
    private boolean isModuleUninstalled(Module module) {
        return (!module.isEnabled() && !module.isValid () && (ModuleProvider.getInstalledModules().get(module.getCodeNameBase()) == null));
    }

    private File locateControlFile (Module m) {
        String configFile = "config" + '/' + "Modules" + '/' + m.getCodeNameBase ().replace ('.', '-') + ".xml"; // NOI18N
        return InstalledFileLocator.getDefault ().locate (configFile, m.getCodeNameBase (), false);
    }
    
    private void removeControlModuleFile (Module m) throws IOException {
        File configFile = null;
        while ((configFile = locateControlFile (m)) != null) {
            if (configFile != null && configFile.exists ()) {
                err.log(Level.FINE, "Try delete the config File " + configFile);
                //FileUtil.toFileObject (configFile).delete ();
                configFile.delete();
            } else {
                err.log(Level.FINE,
                        "Warning: Config File " + configFile + " doesn\'t exist!");
            }
        }
    }
    
    private File locateUpdateTracking (Module m) {
        String fileNameToFind = UPDATE_TRACKING + '/' + m.getCodeNameBase ().replace ('.', '-') + ".xml"; // NOI18N
        return InstalledFileLocator.getDefault ().locate (fileNameToFind, m.getCodeNameBase (), false);
    }
    
    private boolean findUpdateTracking (Module module, boolean checkIfFromAutoupdate) {
        File updateTracking = locateUpdateTracking (module);
        if (updateTracking != null && updateTracking.exists ()) {
            //err.log ("Find UPDATE_TRACKING: " + updateTracking + " found.");
            // check the write permission
            if (! updateTracking.getParentFile ().canWrite ()) {
                err.log(Level.FINE,
                        "Cannot delete module " + module.getCodeName() +
                        " because no write permission to directory " +
                        updateTracking.getParent());
                return false;
            }
            if (checkIfFromAutoupdate) {
                boolean isFromAutoupdate = fromAutoupdate (getModuleConfiguration (updateTracking));
                err.log(Level.FINE,
                        "Is Module " + module.getCodeName() +
                        " installed by AutoUpdate? " + isFromAutoupdate);
                return isFromAutoupdate;
            } else {
                return true;
            }
        } else {
            err.log(Level.FINE,
                    "Cannot delete module " + module.getCodeName() +
                    " because no update_tracking file found.");
            return false;
        }
    }
            
    private boolean fromAutoupdate (Node moduleNode) {
        Node attrOrigin = moduleNode.getAttributes ().getNamedItem (ATTR_ORIGIN);
        assert attrOrigin != null : "ELEMENT_VERSION must contain ATTR_ORIGIN attribute.";
        String origin = attrOrigin.getNodeValue ();
        return INST_ORIGIN.equals (origin);
    }
    
    private void removeModuleFiles (Module m) throws IOException {
        err.log (Level.FINE, "Entry removing files of module " + m);
        File updateTracking = null;
        while ((updateTracking = locateUpdateTracking (m)) != null) {
            removeModuleFilesInCluster (m, updateTracking);
        }
        err.log (Level.FINE, "Exit removing files of module " + m);
    }
    
    private void removeModuleFilesInCluster (Module module, File updateTracking) throws IOException {
        err.log(Level.FINE, "Read update_tracking " + updateTracking + " file.");
        Set/*<String>*/ moduleFiles = readModuleFiles (getModuleConfiguration (updateTracking));
        String configFile = "config" + '/' + "Modules" + '/' + module.getCodeNameBase ().replace ('.', '-') + ".xml"; // NOI18N
        if (moduleFiles.contains (configFile)) {
            File file = InstalledFileLocator.getDefault ().locate (configFile, module.getCodeNameBase (), false);
            assert file == null || ! file.exists () : "Config file " + configFile + " must be already removed.";
        }
        Iterator it = moduleFiles.iterator ();
        while (it.hasNext ()) {
            String fileName = (String) it.next ();
            if (fileName.equals (configFile)) {
                continue;
            }
            File file = InstalledFileLocator.getDefault ().locate (fileName, module.getCodeNameBase (), false);
            if (file.equals (updateTracking)) {
                continue;
            }
            assert file.exists () : "File " + file + " exists.";
            if (file.exists ()) {
                err.log(Level.FINE, "File " + file + " is deleted.");
                FileLock lock = null;
                try {
                    FileObject fo = FileUtil.toFileObject (file);
                    lock = (fo != null) ? fo.lock() : null;
                    //assert fo != null || !file.exists() : file.getAbsolutePath();
                    file.delete();
                } catch (IOException ioe) {
                    assert false : "Waring: IOException " + ioe.getMessage () + " was caught. Propably file lock on the file.";
                    err.log(Level.FINE,
                            "Waring: IOException " + ioe.getMessage() +
                            " was caught. Propably file lock on the file.");
                    err.log(Level.FINE,
                            "Try call File.deleteOnExit() on " + file);
                    file.deleteOnExit ();
                } finally {
                    if (lock != null) {
                        lock.releaseLock();
                    }
                }
            }
        }
        FileObject trackingFo = FileUtil.toFileObject (updateTracking);
        FileLock lock = null;
        try {
        lock = (trackingFo != null) ? trackingFo.lock() : null;        
        updateTracking.delete ();
        } finally {
            if (lock != null) {
                lock.releaseLock();
            }
        }
        err.log(Level.FINE, "File " + updateTracking + " is deleted.");
    }
    
    private Node getModuleConfiguration (File moduleUpdateTracking) {
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
            err.log(Level.WARNING, null, saxe);
            return null;
        } catch (IOException ioe) {
            err.log(Level.WARNING, null, ioe);
        }

        assert document.getDocumentElement () != null : "File " + moduleUpdateTracking + " must contain <module> element.";
        return getModuleElement (document.getDocumentElement ());
    }
    
    private Node getModuleElement (Element element) {
        Node lastElement = null;
        assert ELEMENT_MODULE.equals (element.getTagName ()) : "The root element is: " + ELEMENT_MODULE + " but was: " + element.getTagName ();
        NodeList listModuleVersions = element.getElementsByTagName (ELEMENT_VERSION);
        for (int i = 0; i < listModuleVersions.getLength (); i++) {
            lastElement = getModuleLastVersion (listModuleVersions.item (i));
            if (lastElement != null) {
                break;
            }
        }
        return lastElement;
    }
    
    private Node getModuleLastVersion (Node version) {
        Node attrLast = version.getAttributes ().getNamedItem (ATTR_LAST);
        assert attrLast != null : "ELEMENT_VERSION must contain ATTR_LAST attribute.";
        if (Boolean.valueOf (attrLast.getNodeValue ()).booleanValue ()) {
            return version;
        } else {
            return null;
        }
    }
    
    private Set<String> readModuleFiles (Node version) {
        Set<String> files = new HashSet<String> ();
        NodeList fileNodes = version.getChildNodes ();
        for (int i = 0; i < fileNodes.getLength (); i++) {
            if (fileNodes.item (i).hasAttributes ()) {
                NamedNodeMap map = fileNodes.item (i).getAttributes ();
                files.add (map.getNamedItem (ATTR_FILE_NAME).getNodeValue ());
                err.log(Level.FINE,
                        "Mark to delete: " +
                        map.getNamedItem(ATTR_FILE_NAME).getNodeValue());
            }
        }
        return files;
    }

    private class ModuleStateChecker implements Runnable {
        RequestProcessor.Task cleaner;
        Module m;
        int checks;
        public ModuleStateChecker (Module module, RequestProcessor.Task filesCleaner) {
            cleaner = filesCleaner;
            m = module;
            checks = 0;
        }
        
        public void run () {
            checks ++;
            if (m.isEnabled () && m.isValid ()) {
                if (checks < MAX_CHECKS_OF_STATE) {
                    err.log(Level.FINE,
                            "Module " + m.getCodeNameBase() +
                            " is still valid, repost later.");
                    RequestProcessor.getDefault ().post (this, TIME_TO_CHECK);
                } else {
                    err.log(Level.FINE,
                            "Warning: Module " + m.getCodeNameBase() +
                            " is still valid but time-out. Task is terminated.");
                }
                return ;
            }
            
            // post clean task
            cleaner.schedule (HOLD_ON_PROPAGATE_DISABLE);
        }
        
    }
    
    private class HackModuleListRefresher implements Runnable {
        public void run () {
            // XXX: the modules list should be delete automatically when config/Modules/module.xml is removed
            FileObject modulesRoot = Repository.getDefault ().getDefaultFileSystem ().findResource ("Modules"); // NOI18N
            err.log(Level.FINE,
                    "It\'s a hack: Call refresh on " + modulesRoot +
                    " file object.");
            if (modulesRoot != null) {
                modulesRoot.refresh ();
            }
            // end of hack
        }
    }
}
