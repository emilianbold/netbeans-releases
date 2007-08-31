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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/** The system FileSystem - represents system files under $NETBEANS_HOME/system.
*
* @author Jan Jancura, Ian Formanek, Petr Hamernik
*/
public final class SystemFileSystem extends MultiFileSystem 
implements FileSystem.Status {
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
        home = fss.length > 2 ? (ModuleLayeredFileSystem) fss[1] : null;
        
        setSystemName(SYSTEM_NAME);
        setHidden(true);
    }


    /** Name of the system */
    public String getDisplayName () {
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
    
    protected FileSystem createWritableOnForRename(String oldName, String newName) throws IOException {
        return createWritableOn (oldName);
    }
    
    protected FileSystem createWritableOn (String name) throws IOException {
        FileSystem[] fss = getDelegates ();
        for (int index = 0; index < fss.length; index++) {
            if (! fss[index].isReadOnly ())
                return fss[index];
        }
        // Can really happen if invoked from e.g. org.netbeans.core.Plain.
        throw new IOException("No writable filesystems in our delegates"); // NOI18N
    }
    
    protected java.util.Set createLocksOn (String name) throws IOException {
        LocalFileSystemEx.potentialLock (name);
        return super.createLocksOn (name);
    }
    
    /** This filesystem cannot be removed from pool, it is persistent.
    */
    @Deprecated
    public boolean isPersistent () {
        return true;
    }

    public FileSystem.Status getStatus () {
        return this;
    }

    /** Annotate name
    */
    public String annotateName (String s, Set set) {

        // Look for a localized file name.
        // Note: all files in the set are checked. But please only place the attribute
        // on the primary file, and use this primary file name as the bundle key.
        Iterator it = set.iterator ();
        while (it.hasNext ()) {
            // annotate a name
            FileObject fo = (FileObject) it.next ();

            String bundleName = (String)fo.getAttribute (ATTR_BUNDLE); // NOI18N
            if (bundleName != null) {
                try {
                    bundleName = org.openide.util.Utilities.translate(bundleName);
                    ResourceBundle b = NbBundle.getBundle(bundleName);
                    try {
                        return b.getString (fo.getPath());
                    } catch (MissingResourceException ex) {
                        // ignore--normal
                    }
                } catch (MissingResourceException ex) {
                    ModuleLayeredFileSystem.err.log(
                        Level.WARNING,
                        "Computing display name for " + fo, ex); // NOI18N
                    // ignore
                }
            }
            
            String fixedName = FixedFileSystem.deflt.annotateName(fo.getPath());
            if (fixedName != null) return fixedName;
        }

        return s;
    }

    /** Annotate icon
    */
    public Image annotateIcon (Image im, int type, Set s) {
        String attr;
        if (type == BeanInfo.ICON_COLOR_16x16) {
            attr = ATTR_ICON_16;
        } else if (type == BeanInfo.ICON_COLOR_32x32) {
            attr = ATTR_ICON_32;
        } else {
            // mono icons not supported
            return im;
        }
        Iterator it = s.iterator ();
        while (it.hasNext ()) {
            FileObject fo = (FileObject) it.next ();
            Object value = fo.getAttribute (attr);
            if (value != null) {
                if (value instanceof URL) {
                    return Toolkit.getDefaultToolkit ().getImage ((URL) value);
                } else if (value instanceof Image) {
                    // #18832
                    return (Image)value;
                } else {
                    ModuleLayeredFileSystem.err.warning("Attribute " + attr + " on " + fo + " expected to be a URL or Image; was: " + value);
                }
            }
            Image anntIm = FixedFileSystem.deflt.annotateIcon(fo.getPath());
            if (anntIm != null) {
                return anntIm;
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

        if (homeDir == null) {
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

        FileSystem[] arr = new FileSystem[home == null ? 2 : 3];
        arr[0] = new ModuleLayeredFileSystem(user, true, new FileSystem[0], null);
        if (home != null) {
            File cachedir = new File(new File (userDir.getParentFile(), "var"), "cache"); // NOI18N
            arr[1] = new ModuleLayeredFileSystem(home, false, extras, cachedir);
        }
        FixedFileSystem.deflt = new FixedFileSystem
            ("org.netbeans.core.projects.FixedFileSystem", "Automatic Manifest Installation"); // NOI18N
        arr[home == null ? 1 : 2] = FixedFileSystem.deflt;

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
    protected void notifyMigration (FileObject fo) {
        fireFileStatusChanged (new FileStatusEvent (this, fo, false, true));
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
            return Repository.getDefault().getDefaultFileSystem ();
        }
    }
    // --- SAFETY ---

}
