/*+
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.mercurial;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.mercurial.ui.diff.Setup;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.util.NbBundle;
import javax.swing.JOptionPane;
import java.util.prefs.Preferences;

/**
 * Main entry point for Mercurial functionality, use getInstance() to get the Mercurial object.
 * 
 * @author Maros Sandor
 */
public class Mercurial {
    public static final String MERCURIAL_OUTPUT_TAB_TITLE = org.openide.util.NbBundle.getMessage(Mercurial.class, "CTL_Mercurial_MainMenu");
    public static final String CHANGESET_STR = "changeset:";

    static final String PROP_ANNOTATIONS_CHANGED = "annotationsChanged";
    static final String PROP_VERSIONED_FILES_CHANGED = "versionedFilesChanged";

    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.mercurial");

    /* 
     * Cache the name of the file currently being deleted and whether it is
     * a directory so we do the correct thing after we get notification that
     * deletion has happened.
     */
    public static String deletedFile;
    public static Boolean isDirectory;

    private static final int STATUS_DIFFABLE =
            FileInformation.STATUS_VERSIONED_UPTODATE |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_CONFLICT |
            FileInformation.STATUS_VERSIONED_MERGE |
            FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY;

    private static final String MERCURIAL_GOOD_VERSION = "0.9.3";
    private static Mercurial instance;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    
    public static synchronized Mercurial getInstance() {
        if (instance == null) {
            instance = new Mercurial();
            instance.init();
        }
        return instance;
    }
    
    private MercurialAnnotator   mercurialAnnotator;
    private MercurialInterceptor mercurialInterceptor;
    private FileStatusCache     fileStatusCache;
    private HashMap<String, RequestProcessor>   processorsToUrl;
    private boolean goodVersion;

    private Mercurial() {
    }
    
    
    private void init() {
        checkVersion();
        fileStatusCache = new FileStatusCache();
        fileStatusCache.cleanUp();
        mercurialAnnotator = new MercurialAnnotator();
        mercurialInterceptor = new MercurialInterceptor();
    }

    private void checkVersion() {
        // Set default executable location for mercurial on mac
        if (System.getProperty("os.name").equals("Mac OS X")) { // NOI18N
            String defaultPath = HgModuleConfig.getDefault().getExecutableBinaryPath ();
            if (defaultPath == null || defaultPath.length() == 0) {
                HgModuleConfig.getDefault().setExecutableBinaryPath ("/Library/Frameworks/Python.framework/Versions/Current/bin"); // NOI18N
            }
        }
        String version = HgCommand.getHgVersion();
        LOG.log(Level.FINE, "version: {0}", version);
        if (version != null) {
            goodVersion = version.startsWith(MERCURIAL_GOOD_VERSION);
            if (!goodVersion) {
                Preferences prefs = HgModuleConfig.getDefault().getPreferences();
                String runVersion = prefs.get(HgModuleConfig.PROP_RUN_VERSION, null);
                if (runVersion == null || !runVersion.equals(version)) {
                    int response = JOptionPane.showOptionDialog(null,
                                    NbBundle.getMessage(Mercurial.class,"MSG_VERSION_CONFIRM_QUERY", version, MERCURIAL_GOOD_VERSION),
                                    NbBundle.getMessage(Mercurial.class,"MSG_VERSION_CONFIRM"),
                                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,null, null, null);

                     if (response == JOptionPane.YES_OPTION) {
                         goodVersion = true;
                         prefs.put(HgModuleConfig.PROP_RUN_VERSION, version);
                     } else {
                         prefs.remove(HgModuleConfig.PROP_RUN_VERSION);
                     }
                } else {
                   goodVersion = true;
                }
            }
        } else {
            goodVersion = false;
        }
    }

    public MercurialAnnotator getMercurialAnnotator() {
        return mercurialAnnotator;
    }

    MercurialInterceptor getMercurialInterceptor() {
        return mercurialInterceptor;
    }

    /**
     * Gets the File Status Cache for the mercurial repository
     * 
     * @return FileStatusCache for the repository  
     */
    public FileStatusCache getFileStatusCache() {
        return fileStatusCache;
    }
    
   /**
     * Tests <tt>.hg</tt> directory itself.  
     */
    public boolean isAdministrative(File file) {
        String name = file.getName();
        return isAdministrative(name) && file.isDirectory();
    }

    public boolean isAdministrative(String fileName) {
        return fileName.equals(".hg"); // NOI18N
    }
    /**
     * Tests whether a file or directory should receive the STATUS_NOTVERSIONED_NOTMANAGED status. 
     * All files and folders that have a parent with CVS/Repository file are considered versioned.
     * 
     * @param file a file or directory
     * @return false if the file should receive the STATUS_NOTVERSIONED_NOTMANAGED status, true otherwise
     */ 
    public boolean isManaged(File file) {
        return VersioningSupport.getOwner(file) instanceof MercurialVCS && !HgUtils.isPartOfMercurialMetadata(file);
    }

    public File getTopmostManagedParent(File file) {
        if (HgUtils.isPartOfMercurialMetadata(file)) {
            for (;file != null; file = file.getParentFile()) {
                if (isAdministrative(file)) {
                    file = file.getParentFile();
                    break;
                }
            }
        }
        File topmost = null;
        for (;file != null; file = file.getParentFile()) {
            if (org.netbeans.modules.versioning.util.Utils.isScanForbidden(file)) break;
            if (new File(file, ".hg").canWrite()){ // NOI18N
                topmost =  file;
                break;
            }
        }
        return topmost;
    }

    public HgFileNode [] getNodes(VCSContext context, int includeStatus) {
        File [] files = fileStatusCache.listFiles(context, includeStatus);
        HgFileNode [] nodes = new HgFileNode[files.length];
        for (int i = 0; i < files.length; i++) {
            nodes[i] = new HgFileNode(files[i]);
        }
        return nodes;
    }

   /**
     * Uses content analysis to return the mime type for files.
     *
     * @param file file to examine
     * @return String mime type of the file (or best guess)
     */
    public String getMimeType(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        String foMime;
        if (fo == null) {
            foMime = "content/unknown";
        } else {
            foMime = fo.getMIMEType();
            if ("content/unknown".equals(foMime)) {
                foMime = "text/plain";
            }
        }
        if ((fileStatusCache.getStatus(file).getStatus() & FileInformation.STATUS_VERSIONED) == 0) {
            return HgUtils.isFileContentBinary(file) ? "application/octet-stream" : foMime;
        } else {
            return foMime;
        }
    }

    public boolean isGoodVersion() {
        return goodVersion;
    }

    public void versionedFilesChanged() {    
        support.firePropertyChange(PROP_VERSIONED_FILES_CHANGED, null, null);
    }
    public void refreshAllAnnotations() {
        support.firePropertyChange(PROP_ANNOTATIONS_CHANGED, null, null);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void getOriginalFile(File workingCopy, File originalFile) {
        FileInformation info = fileStatusCache.getStatus(workingCopy);
        if ((info.getStatus() & STATUS_DIFFABLE) == 0) return;

        try {
            File original = VersionsCache.getInstance().getFileRevision(workingCopy, Setup.REVISION_BASE);
            if (original == null) {
                Logger.getLogger(Mercurial.class.getName()).log(Level.INFO, "Unable to get original file {0}", workingCopy);
                 return;
            }
            org.netbeans.modules.versioning.util.Utils.copyStreamsCloseAll(new FileOutputStream(originalFile), new FileInputStream(original));
        } catch (IOException e) {
            Logger.getLogger(Mercurial.class.getName()).log(Level.INFO, "Unable to get original file", e);
        }
    }

    /**
     * Serializes all Hg requests (moves them out of AWT).
     */
    public RequestProcessor getRequestProcessor() { 
        return getRequestProcessor((String)null);
    }

    /**
     * Serializes all Hg requests (moves them out of AWT).
     */
    public RequestProcessor getRequestProcessor(File file) {
        return getRequestProcessor(file.getAbsolutePath());
    }

    public RequestProcessor getRequestProcessor(String url) {
        if(processorsToUrl == null) {
            processorsToUrl = new HashMap<String, RequestProcessor>();
        }

        String key;
        if(url != null) {
            key = url;
        } else {
            key = "ANY_URL"; // NOI18N
        }

        RequestProcessor rp = processorsToUrl.get(key);
        if(rp == null) {
            rp = new RequestProcessor("Mercurial - " + key, 1, true); // NOI18N
            processorsToUrl.put(key, rp);
        }
        return rp;
    }

}
