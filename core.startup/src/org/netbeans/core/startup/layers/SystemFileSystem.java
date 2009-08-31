/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.core.startup.layers;

import java.awt.Image;
import java.awt.Toolkit;
import java.beans.BeanInfo;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/** The system FileSystem - represents system files under $NETBEANS_HOME/system.
*
* @author Jan Jancura, Ian Formanek, Petr Hamernik
*/
public final class SystemFileSystem extends MultiFileSystem 
implements FileSystem.Status, FileChangeListener {
    // Must be public for BeanInfo to work: #11186.

    /** generated Serialized Version UID */
    static final long serialVersionUID = -7761052280240991668L;

    /** system name of this filesystem */
    private static final String SYSTEM_NAME = "SystemFileSystem"; // NOI18N

    /** name of file attribute with localizing bundle */
    private static final String ATTR_BUNDLE = "SystemFileSystem.localizingBundle"; // NOI18N

    /** name of file attribute with URL to 16x16 color icon */
    private static final String ATTR_ICON_16 = "SystemFileSystem.icon"; // NOI18N
    /** name of file attribute with URL to 32x32 color icon */
    private static final String ATTR_ICON_32 = "SystemFileSystem.icon32"; // NOI18N

    private static final Logger LOG = Logger.getLogger(SystemFileSystem.class.getName());

    /** user fs */
    private ModuleLayeredFileSystem user;
    /** home fs */
    private ModuleLayeredFileSystem home;

    /** @param fss list of file systems to delegate to
    */
    @SuppressWarnings("deprecation")
    private SystemFileSystem (FileSystem[] fss) throws PropertyVetoException {
        super (fss);
        user = (ModuleLayeredFileSystem) fss[0];
        home = fss.length > 1 ? (ModuleLayeredFileSystem) fss[1] : null;
        
        setSystemName(SYSTEM_NAME);
        setHidden(true);
        addFileChangeListener(this);
    }


    /** Name of the system */
    public @Override String getDisplayName() {
        return NbBundle.getMessage(SystemFileSystem.class, "CTL_SystemFileSystem"); // NOI18N
    }
    
    /** Getter for the instalation layer filesystem.
     * May be null if there is none such.
    */
    public ModuleLayeredFileSystem getInstallationLayer () {
        return home;
    }
    
    /** Getter for the user layer filesystem.
    */
    public ModuleLayeredFileSystem getUserLayer () {
        return user;
    }
    
    /** Changes layers to provided values.
     * @param arr the new layers
     * @throws IllegalArgumentException if there is an overlap
     */
    public final void setLayers (FileSystem[] arr) throws IllegalArgumentException {
        Set<FileSystem> s = new HashSet<FileSystem> ();
        for (int i = 0; i < arr.length; i++)
            if (s.contains (arr[i]))
                throw new IllegalArgumentException ("Overlap in filesystem layers"); // NOI18N
            else
                s.add (arr[i]);

        // create own internal copy of passed filesystems
        setDelegates(arr.clone());
        firePropertyChange ("layers", null, null); // NOI18N
    }
    
    /** Getter for the array of filesystems that are currently used 
    * in the IDE.
    *
    * @return array of filesystems
    */
    public FileSystem[] getLayers() {
        // don't return reference to internal buffer
        return getDelegates().clone();
    }
    
    protected @Override FileSystem createWritableOnForRename(String oldName, String newName) throws IOException {
        return createWritableOn (oldName);
    }
    
    protected @Override FileSystem createWritableOn(String name) throws IOException {
        FileSystem[] fss = getDelegates ();
        for (int index = 0; index < fss.length; index++) {
            if (! fss[index].isReadOnly ())
                return fss[index];
        }
        // Can really happen if invoked from e.g. org.netbeans.core.Plain.
        throw new IOException("No writable filesystems in our delegates"); // NOI18N
    }
    
    protected @Override Set<? extends FileSystem> createLocksOn(String name) throws IOException {
        LocalFileSystemEx.potentialLock (name);
        return super.createLocksOn (name);
    }
    
    /** This filesystem cannot be removed from pool, it is persistent.
    */
    @Deprecated
    public @Override boolean isPersistent() {
        return true;
    }

    public @Override FileSystem.Status getStatus() {
        return this;
    }
    
    static final String annotateName(FileObject fo) {

        String bundleName = (String) fo.getAttribute(ATTR_BUNDLE); // NOI18N
        if (bundleName != null) {
            try {
                bundleName = org.openide.util.Utilities.translate(bundleName);
                ResourceBundle b = NbBundle.getBundle(bundleName);
                try {
                    return b.getString(fo.getPath());
                } catch (MissingResourceException ex) {
                    // ignore--normal
                    }
            } catch (MissingResourceException ex) {
                Exceptions.attachMessage(ex, warningMessage(bundleName, fo));
                ModuleLayeredFileSystem.err.log(Level.WARNING, null, ex);
                // ignore
            }
        }
        return (String)fo.getAttribute("displayName"); // NOI18N
    }
    private static String warningMessage(String name, FileObject fo) {
        Object by = fo.getAttribute("layers"); // NOI18N
        if (by instanceof Object[]) {
            by = Arrays.toString((Object[])by);
        }
        return "Cannot load " + name + " for " + fo + " defined by " + by; // NOI18N
    }

    /** Annotate name
    */
    public String annotateName(String s, Set<? extends FileObject> files) {

        // Look for a localized file name.
        // Note: all files in the set are checked. But please only place the attribute
        // on the primary file, and use this primary file name as the bundle key.
        for (FileObject fo : files) {
            // annotate a name
            String displayName = annotateName(fo);
            if (displayName != null) {
                return displayName;
            }
        }

        return s;
    }
    
    static Image annotateIcon(FileObject fo, int type) {
        String attr = null;
        if (type == BeanInfo.ICON_COLOR_16x16) {
            attr = ATTR_ICON_16;
        } else if (type == BeanInfo.ICON_COLOR_32x32) {
            attr = ATTR_ICON_32;
        }

        if (attr != null) {
            Object value = fo.getAttribute(attr);
            if (value != null) {
                if (value instanceof URL) {
                    return Toolkit.getDefaultToolkit().getImage((URL) value);
                } else if (value instanceof Image) {
                    // #18832
                    return (Image) value;
                } else {
                    ModuleLayeredFileSystem.err.warning("Attribute " + attr + " on " + fo + " expected to be a URL or Image; was: " + value);
                }
            }
        }

        String base = (String) fo.getAttribute("iconBase"); // NOI18N
        if (base != null) {
            if (type == BeanInfo.ICON_COLOR_16x16) {
                return ImageUtilities.loadImage(base, true);
            } else if (type == BeanInfo.ICON_COLOR_32x32) {
                return ImageUtilities.loadImage(insertBeforeSuffix(base, "_32"), true); // NOI18N
            }
        }
        return null;
    }

    private static String insertBeforeSuffix(String path, String toInsert) {
        String withoutSuffix = path;
        String suffix = ""; // NOI18N

        if (path.lastIndexOf('.') >= 0) {
            withoutSuffix = path.substring(0, path.lastIndexOf('.'));
            suffix = path.substring(path.lastIndexOf('.'), path.length());
        }

        return withoutSuffix + toInsert + suffix;
    }

    /** Annotate icon
    */
    public Image annotateIcon(Image im, int type, Set<? extends FileObject> files) {
        for (FileObject fo : files) {
            Image img = annotateIcon(fo, type);
            if (img != null) {
                return img;
            }
        }
        return im;
    }

    /** Initializes and creates new repository. This repository's system fs is
    * based on the content of ${HOME_DIR}/system and ${USER_DIR}/system directories
    *
    * @param userDir directory where user can write, or null to do it in memory
    * @param homeDir directory where netbeans has been installed, user need not have write access, or null if none
    * @param extradirs 0+ additional directories to use like homeDir
    * @return repository
    * @exception PropertyVetoException if something fails
    */
    static SystemFileSystem create (File userDir, File homeDir, File[] extradirs)
    throws java.beans.PropertyVetoException, IOException {
        FileSystem user;
        LocalFileSystem home;

        if (userDir != null) {
            // only one file system
            if (!userDir.exists ()) {
                userDir.mkdirs ();
            }
            LocalFileSystem l = new LocalFileSystemEx ( true );
            l.setRootDirectory (userDir);
            user = l;
        } else {
            // use some replacement
            String customFSClass = System.getProperty("org.netbeans.core.systemfilesystem.custom"); // NOI18N
            if (customFSClass != null) {
                try {
                    Class clazz = Class.forName(customFSClass);
                    Object instance = clazz.newInstance();
                    user = (FileSystem)instance;
                } catch (Exception x) {
                    ModuleLayeredFileSystem.err.log(
                        Level.WARNING,
                        "Custom system file system writable layer init failed ", x); // NOI18N
                    user = FileUtil.createMemoryFileSystem ();
                }
            } else {
                user = FileUtil.createMemoryFileSystem ();
            }
        }

        if (homeDir == null || !homeDir.isDirectory()) {
            home = null;
        } else {
            home = new LocalFileSystemEx ();
            home.setRootDirectory (homeDir);
            home.setReadOnly (true);                        
        }
        LocalFileSystem[] extras = new LocalFileSystem[extradirs.length];
        for (int i = 0; i < extradirs.length; i++) {
            extras[i] = new LocalFileSystemEx();
            extras[i].setRootDirectory(extradirs[i]);
            extras[i].setReadOnly(true);
        }

        FileSystem[] arr = new FileSystem[home == null ? 1 : 2];
        arr[0] = new ModuleLayeredFileSystem(user, true, new FileSystem[0], false);
        if (home != null) {
            arr[1] = new ModuleLayeredFileSystem(home, false, extras, true);
        }
        return new SystemFileSystem (arr);
    }

    /** Notification that a file has migrated from one file system
    * to another. Usually when somebody writes to file on readonly file
    * system and the file has to be copied to write one. 
    * <P>
    * This method allows subclasses to fire for example FileSystem.PROP_STATUS
    * change to notify that annotation of this file should change.
    *
    * @param fo file object that change its actual file system
    */
    @Override
    protected void notifyMigration (FileObject fo) {
        fireFileStatusChanged (new FileStatusEvent (this, fo, false, true));
    }

    public void fileFolderCreated(FileEvent fe) {
        log("fileFolderCreated", fe); // NOI18N
    }

    public void fileDataCreated(FileEvent fe) {
        log("fileDataCreated", fe); // NOI18N
    }

    public void fileChanged(FileEvent fe) {
        log("fileChanged", fe); // NOI18N
    }

    public void fileDeleted(FileEvent fe) {
        log("fileDeleted", fe); // NOI18N
    }

    public void fileRenamed(FileRenameEvent fe) {
        log("fileDeleted", fe); // NOI18N
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        log("fileAttributeChanged", fe); // NOI18N
    }

    private static void log(String type, FileEvent fe) {
        if (LOG.isLoggable(Level.FINER)) {
            LogRecord r = new LogRecord(Level.FINER, "LOG_FILE_EVENT");
            r.setLoggerName(LOG.getName());
            r.setParameters(new Object[] {
                type,
                fe.getFile().getPath(),
                fe.getFile(),
                fe
            });
            r.setResourceBundle(NbBundle.getBundle(SystemFileSystem.class));
            LOG.log(r);
        }
    }

    // --- SAFETY ---
    private Object writeReplace() throws ObjectStreamException {
        new NotSerializableException("WARNING - SystemFileSystem is not designed to be serialized").printStackTrace(); // NOI18N
        return new SingletonSerializer();
    }
    
    private static final class SingletonSerializer extends Object implements Serializable {
        private static final long serialVersionUID = 6436781994611L;
        SingletonSerializer() {}
        private Object readResolve () throws ObjectStreamException {
            try {
                return FileUtil.getConfigRoot().getFileSystem();
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
    }
    // --- SAFETY ---

}
