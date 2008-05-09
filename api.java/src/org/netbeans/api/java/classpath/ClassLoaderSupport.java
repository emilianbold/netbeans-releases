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

package org.netbeans.api.java.classpath;

import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.*;

import org.openide.execution.NbClassLoader;
import org.openide.filesystems.*;
import org.openide.util.WeakListeners;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/** Classloader for the filesystem pool. Attaches itself as a listener to
 * each file a class has been loaded from. If such a file is deleted, modified
 * or renamed clears the global variable that holds "current" classloader, so
 * on next request for current one new is created.
 *
 * @author Jaroslav Tulach
 */
class ClassLoaderSupport extends NbClassLoader
    implements FileChangeListener, PropertyChangeListener {
    
    public static ClassLoader create(ClassPath cp) {
        return create (cp, ClassLoader.getSystemClassLoader());
    }
    
    static ClassLoader create (final ClassPath cp, final ClassLoader parentClassLoader) {
        try {
            return new ClassLoaderSupport(cp, parentClassLoader);
        } catch (FileStateInvalidException e) {
            // Should not happen, we already trimmed unused roots:
            throw new AssertionError(e);
        }
    }

    /** change listener */
    private org.openide.filesystems.FileChangeListener listener;

    /** PropertyChangeListener */
    private java.beans.PropertyChangeListener propListener;

    /** contains AllPermission */
    private static java.security.PermissionCollection allPermission;

    private static boolean firstTime = true;

    /**
     * The ClassPath to load classes from.
     */
    private ClassPath   classPath;

    /** Constructor that attaches itself to the filesystem pool.
    */
    private ClassLoaderSupport (final ClassPath cp, final ClassLoader parentClassLoader) throws FileStateInvalidException {
        super(cp.getRoots(), parentClassLoader, null);
        this.classPath = cp;

        setDefaultPermissions(getAllPermissions());
        listener = FileUtil.weakFileChangeListener(this, null);
        propListener = WeakListeners.propertyChange (this, null);
        cp.addPropertyChangeListener(propListener);
    }

    /**
     * Tries to locate the .class file on the ClassPath
     * @param name
     * @return
     * @throws ClassNotFoundException
     */
    protected Class findClass (String name) throws ClassNotFoundException {
        Class c = super.findClass (name);
        if (c != null) {
            org.openide.filesystems.FileObject fo;
            String resName = name.replace('.', '/') + ".class"; // NOI18N
            fo = classPath.findResource(resName);
            if (fo != null) {
                // if the file is from the file system pool,
                // register to catch its changes
                fo.addFileChangeListener (listener);
            }
        }
        return c;
    }
    
    /**
     * Tries to locate the resource on the ClassPath
     * @param name
     * @return URL of the resource
     */
    public URL findResource (String name) {
        URL url = super.findResource (name);
        if (url != null) {
            FileObject fo = classPath.findResource(name);
            if (fo != null) {
                // if the file is from the file system pool,
                // register to catch its changes
                fo.addFileChangeListener (listener);
            }
        }
        return url;
    }

    /** Tests whether this object is current loader and if so,
    * clears the loader.
    * @param fo file object that initiated the action
    */
    private void test (org.openide.filesystems.FileObject fo) {
        classPath.resetClassLoader(this);
        fo.removeFileChangeListener (listener);
    }

    /** Resets the loader, removes it from listneing on all known objects.
    */
    private void reset () {
        classPath.resetClassLoader(this);
    }

    /** If this object is not current classloader, removes it from
    * listening on given file object.
    */
    private void testRemove (org.openide.filesystems.FileObject fo) {
        fo.removeFileChangeListener (listener);
    }

    /** Fired when a new folder has been created. This action can only be
    * listened in folders containing the created file up to the root of
    * file system.
    *
    * @param fe the event describing context where action has taken place
    */
    public void fileFolderCreated (org.openide.filesystems.FileEvent fe) {
        testRemove (fe.getFile ());
    }

    /** Fired when a new file has been created. This action can only be
    * listened in folders containing the created file up to the root of
    * file system.
    *
    * @param fe the event describing context where action has taken place
    */
    public void fileDataCreated (org.openide.filesystems.FileEvent fe) {
        testRemove (fe.getFile ());
    }

    /** Fired when a file has been changed.
    * @param fe the event describing context where action has taken place
    */
    public void fileChanged (org.openide.filesystems.FileEvent fe) {
        test (fe.getFile ());
    }

    /** Fired when a file has been deleted.
    * @param fe the event describing context where action has taken place
    */
    public void fileDeleted (org.openide.filesystems.FileEvent fe) {
        test (fe.getFile ());
    }

    /** Fired when a file has been renamed.
    * @param fe the event describing context where action has taken place
    *           and the original name and extension.
    */
    public void fileRenamed (org.openide.filesystems.FileRenameEvent fe) {
        test (fe.getFile ());
    }

    /** Fired when a file attribute has been changed.
    * @param fe the event describing context where action has taken place,
    *           the name of attribute and old and new value.
    */
    public void fileAttributeChanged (org.openide.filesystems.FileAttributeEvent fe) {
        testRemove (fe.getFile ());
    }
    
    /** Getter for allPermissions */
    static synchronized java.security.PermissionCollection getAllPermissions() {
        if (allPermission == null) {
            allPermission = new java.security.Permissions();
            allPermission.add(new java.security.AllPermission());
        }
        return allPermission;
    }

    /**
     * This method gets called when a bound property is changed.
     * @param evt A PropertyChangeEvent object describing the event source 
     *  	and the property that has changed.
     */
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (ClassPath.PROP_ROOTS.equals(evt.getPropertyName()))
            reset();
    }
}
