/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.autoupdate.pluginimporter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Jiri Rechtacek
 */
public class PluginImporter {

    private final Collection<UpdateUnit> plugins;
    private boolean inspected = false;
    private Collection<UpdateElement> installed = null;
    private Collection<UpdateElement> toInstall = null;
    private Collection<UpdateElement> toImport = null;
    private Collection<UpdateElement> broken = null;

    private static final String TRACKING_FILE_NAME = "update_tracking"; // NOI18N
    private static final String ELEMENT_MODULE = "module"; // NOI18N
    private static final String ELEMENT_VERSION = "module_version"; // NOI18N
    private static final String ATTR_LAST = "last"; // NOI18N
    private static final String ATTR_FILE_NAME = "name"; // NOI18N
    private static final String MODULES = "Modules"; // NOI18N
    private static final String LAST_MODIFIED = ".lastModified"; // NOI18N

    private static Logger LOG = Logger.getLogger (PluginImporter.class.getName ());

    public PluginImporter (Collection<UpdateUnit> foundPlugins) {
        plugins = foundPlugins;
    }

    public void reinspect () {
        inspected = false;
        inspect();
    }
    private void inspect () {
        if (inspected) {
            return ;
        }
        long start = System.currentTimeMillis();
        installed = new HashSet<UpdateElement> ();
        toImport = new HashSet<UpdateElement> ();
        toInstall = new HashSet<UpdateElement> ();
        broken = new HashSet<UpdateElement> ();

        Collection<UpdateElement> candidate2import = new HashSet<UpdateElement> ();
        List<UpdateUnit> updateUnits = UpdateManager.getDefault ().getUpdateUnits (UpdateManager.TYPE.MODULE);
        Map<String, UpdateUnit> cnb2uu = new HashMap<String, UpdateUnit> (updateUnits.size ());
        for (UpdateUnit u : updateUnits) {
            cnb2uu.put (u.getCodeName (), u);
        }

        for (UpdateUnit unit : plugins) {

            // save information about plugins on common Update Center
            UpdateUnit remoteUnit = cnb2uu.get (unit.getCodeName ());
            UpdateElement remoteElement = null;
            SpecificationVersion remoteSpec = null;
            if (remoteUnit != null && ! remoteUnit.getAvailableUpdates ().isEmpty ()) {
                remoteElement = remoteUnit.getAvailableUpdates ().get (0);
                remoteSpec = new SpecificationVersion (remoteElement.getSpecificationVersion ());
            }

            if (unit.getInstalled () != null) {
                if (! unit.getAvailableUpdates ().isEmpty ()) {
                    UpdateElement el = unit.getAvailableUpdates ().get (0);
                    if (remoteElement != null) {
                        SpecificationVersion spec = new SpecificationVersion (el.getSpecificationVersion ());
                        if (spec.compareTo (remoteSpec) > 0) {
                            candidate2import.add (el);
                        }
                    } else {
                        candidate2import.add (el);
                    }
                }
                installed.add (unit.getInstalled ());
            } else if (unit.isPending()) {
                LOG.log(Level.INFO, "Plugin " + unit.getCodeName() + " is not installed but is in pending state - i.e. will be installed upon restart, skipping");
            } else {
                assert ! unit.getAvailableUpdates ().isEmpty () : "If " + unit + " isn't installed thus has available updates.";
                UpdateElement el = unit.getAvailableUpdates ().get (0);
                if (remoteElement != null) {
                    SpecificationVersion spec = new SpecificationVersion (el.getSpecificationVersion ());
                    if (spec.compareTo (remoteSpec) > 0) {
                        candidate2import.add (el);
                    } else {
                        toInstall.add (remoteElement);
                    }
                } else {
                    candidate2import.add (el);
                }
            }
        }
        for (UpdateElement el : candidate2import) {
            OperationContainer<InstallSupport> oc = el.getUpdateUnit ().getInstalled () == null ?
                OperationContainer.createForInstall () :
                OperationContainer.createForUpdate ();
            try {
                OperationContainer.OperationInfo info = oc.add (el);
                oc.add (candidate2import);
                if (info.getBrokenDependencies ().isEmpty ()) {
                    toImport.add (el);
                } else {
                    LOG.log (Level.INFO, "Plugin " + el + // NOI18N
                            " cannot be install because not all dependencies can be match: " + info.getBrokenDependencies ()); // NOI18N
                    broken.add (el);
                }
            } catch (IllegalArgumentException iae) {
                LOG.log (Level.INFO, iae.getLocalizedMessage (), iae);
                broken.add (el);
            }
        }
        long end = System.currentTimeMillis();
        LOG.log (Level.INFO, "Inspecting plugins took " + (end - start) + " ms"); // NOI18N

        inspected = true;
    }

    public Collection<UpdateElement> getPluginsToImport () {
        inspect ();
        return toImport;
    }

    public Collection<UpdateElement> getInstalledPlugins () {
        inspect ();
        return installed;
    }

    public Collection<UpdateElement> getPluginsAvailableToInstall () {
        inspect ();
        return toInstall;
    }

    public Collection<UpdateElement> getBrokenPlugins () {
        inspect ();
        return broken;
    }

    public void importPlugins (Collection<UpdateElement> plugins, File src, File dest) throws IOException {
        List<String> configs = new ArrayList<String> (plugins.size ());
        for (UpdateElement el : plugins) {
            String cnb = el.getCodeName ();

            // 1. find all plugin's resources
            Collection<String> toCopy = getPluginFiles (src, cnb, locateUpdateTracking (cnb, src));

            // 2. copy them
            for (String path : toCopy) {
                copy (path, src, dest);
            }

            // 3. find config file
            String path = "config/Modules/" + cnb.replace ('.', '-') + ".xml"; // NOI18N
            configs.add (path);
        }

        // 4. find and copy config files in the end
        for (String path : configs) {
            copy (path, src, dest);
        }

        // 5. don't forget to call refreshModuleList - XXX
        refreshModuleList ();
    }

    private static void copy (String path, File sourceFolder, File destFolder) throws IOException {
        LOG.finest ("Copy " + path + " from " + sourceFolder + " to " + destFolder);
        File src = new File (sourceFolder, path);
        assert src.exists () : src + " exists.";
        src = FileUtil.normalizeFile (src);
        FileObject srcFO = FileUtil.toFileObject (src);

        File destFO = new File (destFolder, path);
        destFO.getParentFile ().mkdirs ();
        File dest = destFO.getParentFile ();
        dest = FileUtil.normalizeFile (dest);
        FileObject destFolderFO = FileUtil.toFileObject (dest);

        File destFile;
        if ((destFile = new File (dest, srcFO.getNameExt ())).exists ()) {
            if (! destFile.delete ()) {
                // if failed delete of the destFile => don't copy, otherwise will cause #159188
                return ;
            }
        }
        FileObject res = FileUtil.copyFile (srcFO, destFolderFO, srcFO.getName ());
        LOG.finest (srcFO + " was copied to " + destFolderFO + ". Result is: " + res);
    }

    private static Collection<String> getPluginFiles (File cluster, String cnb, File updateTracking) {
        Collection<String> res = new HashSet<String> ();
        LOG.log(Level.FINE, "Read update_tracking " + updateTracking + " file.");
        Set<String> moduleFiles = readModuleFiles (getUpdateTrackingConf (updateTracking));
        String configFile = "config/Modules/" + cnb.replace ('.', '-') + ".xml"; // NOI18N

        moduleFiles.remove (configFile);

        for (String fileName : moduleFiles) {
            File file = new File (cluster, fileName);
            if (! file.exists ()) {
                LOG.log (Level.WARNING, "File " + file + " doesn't exist for module " + cnb);
                continue;
            }
            if (file.equals (updateTracking)) {
                continue;
            }
            res.add (fileName);
        }

        res.add (TRACKING_FILE_NAME + '/' + cnb.replace ('.', '-') + ".xml"); // NOI18N);

        LOG.log(Level.FINEST, cnb + " has files: " + res);
        return res;
    }

    private static File locateUpdateTracking (String cnb, File cluster) {
        String fileNameToFind = TRACKING_FILE_NAME + '/' + cnb.replace ('.', '-') + ".xml"; // NOI18N
        File ut = new File (cluster, fileNameToFind);
        if (ut.exists ()) {
            return ut;
        }
        throw new IllegalArgumentException (ut + " doesn't exist."); // NOI18N
    }

    private static Node getUpdateTrackingConf (File moduleUpdateTracking) {
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
            LOG.log(Level.WARNING, null, saxe);
            return null;
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
        }

        assert document.getDocumentElement () != null : "File " + moduleUpdateTracking + " must contain <module> element.";
        return getModuleElement (document.getDocumentElement ());
    }

    private static Node getModuleElement (Element element) {
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

    private static Node getModuleLastVersion (Node version) {
        Node attrLast = version.getAttributes ().getNamedItem (ATTR_LAST);
        assert attrLast != null : "ELEMENT_VERSION must contain ATTR_LAST attribute.";
        if (Boolean.valueOf (attrLast.getNodeValue ()).booleanValue ()) {
            return version;
        } else {
            return null;
        }
    }

    private static Set<String> readModuleFiles (Node version) {
        Set<String> files = new HashSet<String> ();
        NodeList fileNodes = version.getChildNodes ();
        for (int i = 0; i < fileNodes.getLength (); i++) {
            if (fileNodes.item (i).hasAttributes ()) {
                NamedNodeMap map = fileNodes.item (i).getAttributes ();
                files.add (map.getNamedItem (ATTR_FILE_NAME).getNodeValue ());
                LOG.log(Level.FINE,
                        "File for import: " +
                        map.getNamedItem(ATTR_FILE_NAME).getNodeValue());
            }
        }
        return files;
    }

    private static void refreshModuleList () {
        // XXX: the modules list should be refresh automatically when config/Modules/ changes
        final FileObject modulesRoot = FileUtil.getConfigFile(MODULES);
        LOG.log (Level.FINE,
                "It\'s a hack: Call refresh on " + modulesRoot +
                " file object.");
        if (modulesRoot != null) {
            try {
                FileUtil.runAtomicAction (new FileSystem.AtomicAction () {

                    public void run () throws IOException {
                        modulesRoot.getParent ().refresh ();
                        modulesRoot.refresh ();
                    }
                });
            } catch (IOException ex) {
                Exceptions.printStackTrace (ex);
            }
        }
    }

    public static void touchLastModified (File cluster) {
        try {
            File stamp = new File (cluster, LAST_MODIFIED);
            if (! stamp.createNewFile ()) {
                stamp.setLastModified (System.currentTimeMillis ());
                if (! stamp.setLastModified (System.currentTimeMillis ())) {
                    stamp.delete ();
                    stamp = new File (cluster, LAST_MODIFIED);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace ();
        }
    }
}
