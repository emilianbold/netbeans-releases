/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup.layers;

import org.openide.util.WeakListeners;
import java.awt.Image;
import java.awt.Toolkit;
import java.beans.*;
import java.io.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/** The system FileSystem - represents system files under $NETBEANS_HOME/system.
*
* @author Jan Jancura, Ian Formanek, Petr Hamernik
*/
public final class SystemFileSystem extends MultiFileSystem implements FileSystem.Status {
    // Must be public for BeanInfo to work: #11186.

    /** generated Serialized Version UID */
    static final long serialVersionUID = -7761052280240991668L;


    /** Resource for all localized strings in jar file system. */
    //org.openide.util.NbBundle.getBundle(SystemFileSystem.class);

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

    /** message to format file in netbeans.home */
    private static MessageFormat homeFormat;
    /** message to format file in netbeans.user */
    private static MessageFormat userFormat;

    /** @param fss list of file systems to delegate to
    */
    private SystemFileSystem (FileSystem[] fss) throws PropertyVetoException {
        super (fss);
        user = (ModuleLayeredFileSystem) fss[0];
        home = fss.length > 2 ? (ModuleLayeredFileSystem) fss[1] : null;

        setSystemName (SYSTEM_NAME);
        setHidden (true);
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
        Set s = new HashSet ();
        for (int i = 0; i < arr.length; i++)
            if (s.contains (arr[i]))
                throw new IllegalArgumentException ("Overlap in filesystem layers"); // NOI18N
            else
                s.add (arr[i]);

        // create own internal copy of passed filesystems
        setDelegates ((FileSystem []) arr.clone ());
        firePropertyChange ("layers", null, null); // NOI18N
    }
    
    /** Getter for the array of filesystems that are currently used 
    * in the IDE.
    *
    * @return array of filesystems
    */
    public FileSystem[] getLayers() {
        // don't return reference to internal buffer
        return (FileSystem []) getDelegates ().clone ();
    }

    protected FileSystem createWritableOnForRename (String oldName, String newName) throws IOException {        
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
    
    private static boolean isWritableOn(String name, FileSystem fs) {
        if (fs.isReadOnly()) return false;
        if (fs instanceof ModuleLayeredFileSystem) {
            // Check if the file is really on a writable delegate.
            FileSystem[] fss = ((ModuleLayeredFileSystem)fs).getLayers();
            for (int i = 0; i < fss.length; i++) {
                if (fss[i].findResource(name) != null) {
                    return ! fss[i].isReadOnly();
                }
            }
            throw new IllegalArgumentException("did not find " + name + " on " + fs); // NOI18N
        } else {
            // No harm done, just write it somewhere here.
            return true;
        }
    }
    
    /** This filesystem cannot be removed from pool, it is persistent.
    */
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
                    ResourceBundle b = NbBundle.getBundle (bundleName, Locale.getDefault (),
                        (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class)); // systemclassloader
                    try {
                        return b.getString (fo.getPath());
                    } catch (MissingResourceException ex) {
                        // ignore--normal
                    }
                } catch (MissingResourceException ex) {
                    ErrorManager.getDefault().annotate(ex, ErrorManager.UNKNOWN, "Computing display name for " + fo, null, null, null); // NOI18N
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    // ignore
                }
            }
            
            String fixedName = FixedFileSystem.deflt.annotateName(fo.getPath());
            if (fixedName != null) return fixedName;
        }

        return annotateNameNoLocalization (s, set);
    }

    /** Annotate name but do not consider using localized name. */
    private String annotateNameNoLocalization (String s, Set set) {

        if (home == null || user == null) {
            // no annotation if not running as multiuser
            return s;
        }

        Iterator it = set.iterator ();
        int cnt = 0;
        while (it.hasNext ()) {
            FileObject fo = (FileObject)it.next ();
            if (!fo.isRoot ()) {
                cnt++;
            }
            if (findSystem (fo) == home) {
                return getHomeFormat ().format (new Object[] { s });
            }
        }

        if (cnt == 0) {
            // only roots
            return s;
        }

        return getUserFormat ().format (new Object[] { s });
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
                    ErrorManager.getDefault().log(ErrorManager.WARNING, "Attribute " + attr + " on " + fo + " expected to be a URL or Image; was: " + value);
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
            LocalFileSystem l = new LocalFileSystemEx ();
            l.setRootDirectory (userDir);
            user = l;
        } else {
            // use some replacement
            user = FileUtil.createMemoryFileSystem ();
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
        arr[0] = new ModuleLayeredFileSystem(user, new FileSystem[0], null);
        if (home != null) {
            File cachedir = new File(new File (userDir.getParentFile(), "var"), "cache"); // NOI18N
            arr[1] = new ModuleLayeredFileSystem(home, extras, cachedir);
        }
        FixedFileSystem.deflt = new FixedFileSystem
            ("org.netbeans.core.projects.FixedFileSystem", "Automatic Manifest Installation"); // NOI18N
        arr[home == null ? 1 : 2] = FixedFileSystem.deflt;

        return new SystemFileSystem (arr);
    }

    /** Getter for message.
    */
    private static MessageFormat getUserFormat () {
        if (userFormat == null) {
            userFormat = new MessageFormat (NbBundle.getMessage(SystemFileSystem.class, "CTL_UserFile"));
        }
        return userFormat;
    }

    /** Getter for message.
    */
    private static MessageFormat getHomeFormat () {
        if (homeFormat == null) {
            homeFormat = new MessageFormat (NbBundle.getMessage(SystemFileSystem.class, "CTL_HomeFile"));
        }
        return homeFormat;
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
    private Object writeReplace () throws ObjectStreamException {
        new NotSerializableException ("WARNING - SystemFileSystem is not designed to be serialized").printStackTrace (); // NOI18N
        return new SingletonSerializer ();
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
