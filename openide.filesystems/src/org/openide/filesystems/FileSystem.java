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

package org.openide.filesystems;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.openide.util.Exceptions;
import org.openide.util.actions.SystemAction;

/** Interface that provides basic information about a virtual
* filesystem. Classes that implement it
* should follow JavaBean conventions because when a new
* instance of a filesystem class is inserted into the system, it should
* permit the user to modify it with standard Bean properties.
* <P>
* Implementing classes should also have associated subclasses of {@link FileObject}.
* <p>Although the class is serializable, only the {@link #isHidden hidden state} and {@link #getSystemName system name}
* are serialized, and the deserialized object is by default {@link #isValid invalid} (and may be a distinct
* object from a valid filesystem in the Repository). If you wish to safely deserialize a file
* system, you should after deserialization try to replace it with a filesystem of the
* {@link Repository#findFileSystem same name} in the Repository.
* @author Jaroslav Tulach
*/
public abstract class FileSystem implements Serializable {
    /** generated Serialized Version UID */
    private static final long serialVersionUID = -8931487924240189180L;

    /** Property name indicating validity of filesystem. */
    public static final String PROP_VALID = "valid"; // NOI18N

    /**
     * Property name indicating whether filesystem is hidden.
     * @deprecated The property is now hidden.
     */
    @Deprecated
    public static final String PROP_HIDDEN = "hidden"; // NOI18N

    /**
     * Property name giving internal system name of filesystem.
     * @deprecated This system name should now be avoided in favor of identifying files persistently by URL.
     */
    @Deprecated
    public static final String PROP_SYSTEM_NAME = "systemName"; // NOI18N

    /** Property name giving display name of filesystem.
     * @since 2.1
     */
    public static final String PROP_DISPLAY_NAME = "displayName"; // NOI18N    

    /** Property name giving root folder of filesystem. */
    public static final String PROP_ROOT = "root"; // NOI18N

    /** Property name giving read-only state. */
    public static final String PROP_READ_ONLY = "readOnly"; // NOI18N

    /** Property name giving capabilities state. @deprecated No more capabilities. */
    static final String PROP_CAPABILITIES = "capabilities"; // NOI18N    

    /** Used for synchronization purpose*/
    private static final Object internLock = new Object();
    private transient static ThreadLocal<EventControl> thrLocal = new ThreadLocal<EventControl>();

    /** Empty status */
    private static final Status STATUS_NONE = new Status() {
            public String annotateName(String name, Set<? extends FileObject> files) {
                return name;
            }

            public Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
                return icon;
            }
        };

    /** is this filesystem valid?
    * It can be invalid if there is another filesystem with the
    * same name in the filesystem pool.
    */
    transient private boolean valid = false;

    /** True if the filesystem is assigned to pool.
    * Is modified from Repository methods.
    */
    transient boolean assigned = false;

    /**Repository that contains this FileSystem or null*/
    private transient Repository repository = null;
    private transient FCLSupport fclSupport;

    /** Describes capabilities of the filesystem.
    */
    @Deprecated // have to store it for compat
    // XXX JDK #6460147: javac still reports it even though @Deprecated, and @SuppressWarnings("deprecation") does not help either
    private FileSystemCapability capability;

    /** property listener on FileSystemCapability. */
    private transient PropertyChangeListener capabilityListener;

    /** hidden flag */
    private boolean hidden = false;

    /** system name */
    private String systemName = "".intern(); // NOI18N

    /** Utility field used by event firing mechanism. */
    private transient ListenerList<FileStatusListener> fileStatusList;
    private transient ListenerList<VetoableChangeListener> vetoableChangeList;
    private transient PropertyChangeSupport changeSupport;

    /** Default constructor. */
    public FileSystem() {
    }

    /** Should check for external modifications. All existing FileObjects will be
     * refreshed. For folders it should reread the content of disk,
     * for data file it should check for the last time the file has been modified.
     *
     * The default implementation is to do nothing, in contradiction to the rest
     * of the description. Unless subclasses override it, the method does not work.
     *
     * @param expected should the file events be marked as expected change or not?
     * @see FileEvent#isExpected
     * @since 2.16
     */
    public void refresh(boolean expected) {
    }

    /** Test whether filesystem is valid.
    * Generally invalidity would be caused by a name conflict in the filesystem pool.
    * @return true if the filesystem is valid
    */
    public final boolean isValid() {
        return valid;
    }

    /** Setter for validity. Accessible only from filesystem pool.
    * @param v the new value
    */
    final void setValid(boolean v) {
        if (v != valid) {
            valid = v;
            firePropertyChange(
                PROP_VALID, (!v) ? Boolean.TRUE : Boolean.FALSE, v ? Boolean.TRUE : Boolean.FALSE, Boolean.FALSE
            );
        }
    }

    /** Set hidden state of the object.
     * A hidden filesystem is not presented to the user in the Repository list (though it may be present in the Repository Settings list).
    *
    * @param hide <code>true</code> if the filesystem should be hidden
     * @deprecated This property is now useless.
    */
    @Deprecated
    public final void setHidden(boolean hide) {
        if (hide != hidden) {
            hidden = hide;
            firePropertyChange(PROP_HIDDEN, (!hide) ? Boolean.TRUE : Boolean.FALSE, hide ? Boolean.TRUE : Boolean.FALSE);
        }
    }

    /** Getter for the hidden property.
     * @return the hidden property.
     * @deprecated This property is now useless.
    */
    @Deprecated
    public final boolean isHidden() {
        return hidden;
    }

    /** Tests whether filesystem will survive reloading of system pool.
    * If true then when
    * {@link Repository} is reloading its content, it preserves this
    * filesystem in the pool.
    * <P>
    * This can be used when the pool contains system level and user level
    * filesystems. The system ones should be preserved when the user changes
    * the content (for example when he is loading a new project).
    * <p>The default implementation returns <code>false</code>.
    *
    * @return true if the filesystem should be persistent
     * @deprecated This property is long since useless.
    */
    @Deprecated
    protected boolean isPersistent() {
        return false;
    }

    /** Provides a name for the system that can be presented to the user.
    * <P>
    * This call should <STRONG>never</STRONG> be used to attempt to identify the file root
    * of the filesystem. On some systems it may happen to look the same but this is a
    * coincidence and may well change in the future. Either check whether
    * you are working with a {@link LocalFileSystem} or similar implementation and use
    * {@link LocalFileSystem#getRootDirectory}; or better, try
    * {@link FileUtil#toFile} which is designed to do this correctly.
    * <p><strong>Note:</strong> for most purposes it is probably a bad idea to use
    * this method. Instead look at {@link FileUtil#getFileDisplayName}.
    * @return user presentable name of the filesystem
    */
    public abstract String getDisplayName();

    /** Internal (system) name of the filesystem.
    * Should uniquely identify the filesystem, as it will
    * be used during serialization of its files. The preferred way of doing this is to concatenate the
    * name of the filesystem type (e.g. the class) and the textual form of its parameters.
    * <P>
    * A change of the system name should be interpreted as a change of the internal
    * state of the filesystem. For example, if the root directory is moved to different
    * location, one should rebuild representations for all files
    * in the system.
    * <P>
    * This call should <STRONG>never</STRONG> be used to attempt to identify the file root
    * of the filesystem. On Unix systems it may happen to look the same but this is a
    * coincidence and may well change in the future. Either check whether
    * you are working with a {@link LocalFileSystem} or similar implementation and use
    * {@link LocalFileSystem#getRootDirectory}; or better, try
    * {@link FileUtil#toFile} which is designed to do this correctly.
    * @return string with system name
     * @deprecated The system name should now be avoided in favor of identifying files persistently by URL.
    */
    @Deprecated
    public final String getSystemName() {
        return systemName;
    }

    /** Changes system name of the filesystem.
    * This property is bound and constrained: first of all
    * all vetoable listeners are asked whether they agree with the change. If so,
    * the change is made and all change listeners are notified of
    * the change.
    *
    * <p><em>Warning:</em> this method is protected so that only subclasses can change
    *    the system name.
    *
    * @param name new system name
    * @exception PropertyVetoException if the change is not allowed by a listener
     * @deprecated The system name should now be avoided in favor of identifying files persistently by URL.
    */
    @Deprecated
    protected final void setSystemName(String name) throws PropertyVetoException {
        synchronized (Repository.class) {
            if (systemName.equals(name)) {
                return;
            }

            // I must be the only one who works with system pool (that is listening)
            // on this interface
            fireVetoableChange(PROP_SYSTEM_NAME, systemName, name);

            String old = systemName;
            systemName = name.intern();

            firePropertyChange(PROP_SYSTEM_NAME, old, systemName);

            /** backward compatibility for FileSystems that don`t fire
             * PROP_DISPLAY_NAME*/
            firePropertyChange(PROP_DISPLAY_NAME, null, null);
        }
    }

    /** Returns <code>true</code> if the filesystem is default.
     * @return true if this is {@link Repository#getDefaultFileSystem}
    */
    public final boolean isDefault() {
        FileSystem fs = null;
        try {
            fs = FileUtil.getConfigRoot().getFileSystem();
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
        return this == fs;
    }

    /** Test if the filesystem is read-only or not.
    * @return true if the system is read-only
    */
    public abstract boolean isReadOnly();

    /** Getter for root folder in the filesystem.
    *
    * @return root folder of whole filesystem
    */
    public abstract FileObject getRoot();

    /** Finds file in the filesystem by name.
    * <P>
    * The default implementation converts dots in the package name into slashes,
    * concatenates the strings, adds any extension prefixed by a dot and calls
    * the {@link #findResource findResource} method.
    *
    * <p><em>Note:</em> when both of <code>name</code> and <code>ext</code> are <CODE>null</CODE> then name and
    *    extension should be ignored and scan should look only for a package.
    *
    * @param aPackage package name where each package component is separated by a dot
    * @param name name of the file (without dots) or <CODE>null</CODE> if
    *    one wants to obtain a folder (package) and not a file in it
    * @param ext extension of the file (without leading dot) or <CODE>null</CODE> if one needs
    *    a package and not a file
    *
    * @return a file object that represents a file with the given name or
    *   <CODE>null</CODE> if the file does not exist
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead, or use {@link #findResource} if you are not interested in classpaths.
    */
    @Deprecated
    public FileObject find(String aPackage, String name, String ext) {
        assert false : "Deprecated.";

        StringBuffer bf = new StringBuffer();

        // append package and name
        if (!aPackage.equals("")) { // NOI18N

            String p = aPackage.replace('.', '/');
            bf.append(p);
            bf.append('/');
        }

        // append name
        if (name != null) {
            bf.append(name);
        }

        // append extension if there is one
        if (ext != null) {
            bf.append('.');
            bf.append(ext);
        }

        return findResource(bf.toString());
    }

    /** Finds a file given its full resource path.
    * @param name the resource path, e.g. "dir/subdir/file.ext" or "dir/subdir" or "dir"
    * @return a file object with the given path or
    *   <CODE>null</CODE> if no such file exists
    */
    public abstract FileObject findResource(String name);

    /** Returns an array of actions that can be invoked on any file in
    * this filesystem.
    * These actions should preferably
    * support the {@link org.openide.util.actions.Presenter.Menu Menu},
    * {@link org.openide.util.actions.Presenter.Popup Popup},
    * and {@link org.openide.util.actions.Presenter.Toolbar Toolbar} presenters.
    *
    * @return array of available actions
    */
    public abstract SystemAction[] getActions();

    /**
     * Get actions appropriate to a certain file selection.
     * By default, returns the same list as {@link #getActions()}.
     * @param foSet one or more files which may be selected
     * @return zero or more actions appropriate to those files
     */
    public SystemAction[] getActions(Set<FileObject> foSet) {
        return this.getActions();
    }

    /** Reads object from stream and creates listeners.
    * @param in the input stream to read from
    * @exception IOException error during read
    * @exception ClassNotFoundException when class not found
    */
    private void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, java.lang.ClassNotFoundException {
        in.defaultReadObject();

        if (capability != null) {
            capability.addPropertyChangeListener(getCapabilityChangeListener());
        }
    }

    @Override
    public String toString() {
        return getSystemName() + "[" + super.toString() + "]"; // NOI18N
    }

    /** Allows filesystems to set up the environment for external execution
    * and compilation.
    * Each filesystem can add its own values that
    * influence the environment. The set of operations that can modify
    * environment is described by the {@link Environment} interface.
    * <P>
    * The default implementation throws an exception to signal that it does not
    * support external compilation or execution.
    *
    * @param env the environment to setup
    * @exception EnvironmentNotSupportedException if external execution
    *    and compilation cannot be supported
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public void prepareEnvironment(Environment env) throws EnvironmentNotSupportedException {
        throw new EnvironmentNotSupportedException(this);
    }

    /** Get a status object that can annotate a set of files by changing the names or icons
    * associated with them.
    * <P>
    * The default implementation returns a status object making no modifications.
    *
    * @return the status object for this filesystem
    */
    public Status getStatus() {
        return STATUS_NONE;
    }

    /** The object describing capabilities of this filesystem.
     * Subclasses cannot override it.
     * @return object describing capabilities of this filesystem.
     * @deprecated Capabilities are no longer used.
     */
    @Deprecated
    public final FileSystemCapability getCapability() {
        if (capability == null) {
            capability = new FileSystemCapability.Bean();
            capability.addPropertyChangeListener(getCapabilityChangeListener());
        }

        return capability;
    }

    /** Allows subclasses to change a set of capabilities of the
    * filesystem.
    * @param capability the capability to use
     * @deprecated Capabilities are no longer used.
    */
    @Deprecated
    protected final void setCapability(FileSystemCapability capability) {
        if (this.capability != null) {
            this.capability.removePropertyChangeListener(getCapabilityChangeListener());
        }

        this.capability = capability;

        if (this.capability != null) {
            this.capability.addPropertyChangeListener(getCapabilityChangeListener());
        }
    }

    /** Executes atomic action. The atomic action represents a set of
    * operations constituting one logical unit. It is guaranteed that during
    * execution of such an action no events about changes in the filesystem
    * will be fired.
    * <P>
    * <em>Warning:</em> the action should not take a significant amount of time, and should finish as soon as
    * possible--otherwise all event notifications will be blocked.
    * <p><strong>Warning:</strong> do not be misled by the name of this method;
    * it does not require the filesystem to treat the changes as an atomic block of
    * commits in the database sense! That is, if an exception is thrown in the middle
    * of the action, partial results will not be undone (in general this would be
    * impossible to implement for all filesystems anyway).
    * @param run the action to run
    * @exception IOException if there is an <code>IOException</code> thrown in the actions' {@link AtomicAction#run run}
    *    method
    */
    public final void runAtomicAction(final AtomicAction run)
    throws IOException {
        getEventControl().runAtomicAction(run);
    }

    /**
     * Begin of block, that should be performed without firing events.
     * Firing of events is postponed after end of block .
     * There is strong necessity to use always both methods: beginAtomicAction
     * and finishAtomicAction. It is recomended use it in try - finally block.
     * @param run Events fired from this atomic action will be marked as events
     * that were fired from this run.
     */
    void beginAtomicAction(FileSystem.AtomicAction run) {
        getEventControl().beginAtomicAction(run);
    }

    void beginAtomicAction() {
        beginAtomicAction(null);
    }

    /**
     * End of block, that should be performed without firing events.
     * Firing of events is postponed after end of block .
     * There is strong necessity to use always both methods: beginAtomicAction
     * and finishAtomicAction. It is recomended use it in try - finally block.
     */
    void finishAtomicAction() {
        getEventControl().finishAtomicAction();
    }

    /**
     *  Inside atomicAction adds an event dispatcher to the queue of FS events
     *  and firing of events is postponed. If not event handlers are called directly.
     * @param run dispatcher to run
     */
    void dispatchEvent(EventDispatcher run) {
        getEventControl().dispatchEvent(run);
    }

    /** returns property listener on FileSystemCapability. */
    private synchronized PropertyChangeListener getCapabilityChangeListener() {
        if (capabilityListener == null) {
            capabilityListener = new PropertyChangeListener() {
                        public void propertyChange(java.beans.PropertyChangeEvent propertyChangeEvent) {
                            firePropertyChange(
                                PROP_CAPABILITIES, propertyChangeEvent.getOldValue(), propertyChangeEvent.getNewValue()
                            );
                        }
                    };
        }

        return capabilityListener;
    }

    private final EventControl getEventControl() {
        EventControl evnCtrl = thrLocal.get();

        if (evnCtrl == null) {
            thrLocal.set(evnCtrl = new EventControl());
        }

        return evnCtrl;
    }

    /** Registers FileStatusListener to receive events.
    * The implementation registers the listener only when getStatus () is
    * overriden to return a special value.
    *
    * @param listener The listener to register.
    */
    public final void addFileStatusListener(FileStatusListener listener) {
        synchronized (internLock) {
            // JST: Ok? Do not register listeners when the fs cannot change status?
            if (getStatus() == STATUS_NONE) {
                return;
            }

            if (fileStatusList == null) {
                fileStatusList = new ListenerList<FileStatusListener>();
            }

            fileStatusList.add(listener);
        }
    }

    /** Removes FileStatusListener from the list of listeners.
     *@param listener The listener to remove.
     */
    public final void removeFileStatusListener(FileStatusListener listener) {
        if (fileStatusList == null) {
            return;
        }

        fileStatusList.remove(listener);
    }

    /** Notifies all registered listeners about change of status of some files.
    *
    * @param event The event to be fired
    */
    protected final void fireFileStatusChanged(FileStatusEvent event) {
        if (fileStatusList == null) {
            return;
        }

        List<FileStatusListener> listeners = fileStatusList.getAllListeners();
        dispatchEvent(new FileStatusDispatcher(listeners, event));
    }

    /** Adds listener for the veto of property change.
    * @param listener the listener
    */
    public final void addVetoableChangeListener(VetoableChangeListener listener) {
        synchronized (internLock) {
            if (vetoableChangeList == null) {
                vetoableChangeList = new ListenerList<VetoableChangeListener>();
            }

            vetoableChangeList.add(listener);
        }
    }

    /** Removes listener for the veto of property change.
    * @param listener the listener
    */
    public final void removeVetoableChangeListener(VetoableChangeListener listener) {
        if (vetoableChangeList == null) {
            return;
        }

        vetoableChangeList.remove(listener);
    }

    /** Fires property vetoable event.
    * @param name name of the property
    * @param o old value of the property
    * @param n new value of the property
    * @exception PropertyVetoException if an listener vetoed the change
    */
    protected final void fireVetoableChange(String name, Object o, Object n)
    throws PropertyVetoException {
        if (vetoableChangeList == null) {
            return;
        }

        PropertyChangeEvent e = null;

        for (VetoableChangeListener l : vetoableChangeList.getAllListeners()) {
            if (e == null) {
                e = new PropertyChangeEvent(this, name, o, n);
            }

            l.vetoableChange(e);
        }
    }

    /** Registers PropertyChangeListener to receive events.
    *@param listener The listener to register.
    */
    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        synchronized (internLock) {
            if (changeSupport == null) {
                changeSupport = new PropertyChangeSupport(this);
            }
        }

        changeSupport.addPropertyChangeListener(listener);
    }

    /** Removes PropertyChangeListener from the list of listeners.
    *@param listener The listener to remove.
    */
    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        if (changeSupport != null) {
            changeSupport.removePropertyChangeListener(listener);
        }
    }

    /** Fires property change event.
    * @param name name of the property
    * @param o old value of the property
    * @param n new value of the property
    */
    protected final void firePropertyChange(String name, Object o, Object n) {
        firePropertyChange(name, o, n, null);
    }

    final void firePropertyChange(String name, Object o, Object n, Object propagationId) {
        if (changeSupport == null) {
            return;
        }

        if ((o != null) && (n != null) && o.equals(n)) {
            return;
        }

        PropertyChangeEvent e = new PropertyChangeEvent(this, name, o, n);
        e.setPropagationId(propagationId);
        changeSupport.firePropertyChange(e);
    }

    /** Notifies this filesystem that it has been added to the repository.
    * Various initialization tasks could go here. The default implementation does nothing.
    * <p>Note that this method is <em>advisory</em> and serves as an optimization
    * to avoid retaining resources for too long etc. Filesystems should maintain correct
    * semantics regardless of whether and when this method is called.
    */
    public void addNotify() {
    }

    /** Notifies this filesystem that it has been removed from the repository.
    * Concrete filesystem implementations could perform clean-up here.
    * The default implementation does nothing.
    * <p>Note that this method is <em>advisory</em> and serves as an optimization
    * to avoid retaining resources for too long etc. Filesystems should maintain correct
    * semantics regardless of whether and when this method is called.
    */
    public void removeNotify() {
    }

    /** getter for Repository
    * @return Repository that contains this FileSystem or null if FileSystem
    * is not part of any Repository
    */
    final Repository getRepository() {
        return repository;
    }

    void setRepository(Repository rep) {
        repository = rep;
    }

    final FCLSupport getFCLSupport() {
        synchronized (FCLSupport.class) {
            if (fclSupport == null) {
                fclSupport = new FCLSupport();
            }
        }

        return fclSupport;
    }

    /** Add new listener to this object.
    * @param fcl the listener
    * @since 2.8
    */
    public final void addFileChangeListener(FileChangeListener fcl) {
        getFCLSupport().addFileChangeListener(fcl);
    }

    /** Remove listener from this object.
    * @param fcl the listener
    * @since 2.8
    */
    public final void removeFileChangeListener(FileChangeListener fcl) {
        getFCLSupport().removeFileChangeListener(fcl);
    }

    /** An action that it is to be called atomically with respect to filesystem event notification.
    * During its execution (via {@link FileSystem#runAtomicAction runAtomicAction})
    * no events about changes in filesystems are fired.
     * <p><strong>Nomenclature warning:</strong> the action is by no means "atomic"
     * in the usual sense of the word, i.e. either running to completion or rolling
     * back. There is no rollback support. The actual semantic property here is
     * close to "isolation" - the action appears as a single operation as far as
     * listeners are concerned - but not quite, since it is perfectly possible for
     * some other thread to see half of the action if it happens to run during
     * that time. Generally it is a mistake to assume that using AtomicAction gives
     * you any kind of consistency guarantees; rather, it avoids producing change
     * events too early and thus causing listener code to run before it should.
    */
    public static interface AtomicAction {
        /** Executed when it is guaranteed that no events about changes
        * in filesystems will be notified.
        *
        * @exception IOException if there is an error during execution
        */
        public void run() throws IOException;
    }

    /** Allows a filesystem to annotate a group of files (typically comprising a data object) with additional markers.
     * <p>This could be useful, for
    * example, for a filesystem supporting version control.
    * It could annotate names and icons of data nodes according to whether the files were current, locked, etc.
    */
    public static interface Status {
        /** Annotate the name of a file cluster.
        * @param name the name suggested by default
        * @param files an immutable set of {@link FileObject}s belonging to this filesystem
        * @return the annotated name (may be the same as the passed-in name)
        * @exception ClassCastException if the files in the set are not of valid types
        */
        public String annotateName(String name, Set<? extends FileObject> files);

        /** Annotate the icon of a file cluster.
         * <p>Please do <em>not</em> modify the original; create a derivative icon image,
         * using a weak-reference cache if necessary.
        * @param icon the icon suggested by default
        * @param iconType an icon type from {@link java.beans.BeanInfo}
        * @param files an immutable set of {@link FileObject}s belonging to this filesystem
        * @return the annotated icon (may be the same as the passed-in icon)
        * @exception ClassCastException if the files in the set are not of valid types
        */
        public Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files);
    }

    /** Extension interface for Status provides HTML-formatted annotations.
     * Principally this is used to deemphasize status text by presenting
     * it in a lighter color, by placing it inside
     * &lt;font color=!controlShadow&gt; tags.  Note that it is preferable to
     * use logical colors (such as controlShadow) which are resolved by calling
     * UIManager.getColor(key) - this way they will always fit with the
     * look and feel.  To use a logical color, prefix the color name with a
     * ! character.
     * <p>
     * Please use only the limited markup subset of HTML supported by the
     * lightweight HTML renderer.
     * @see <a href="@org-openide-awt@/org/openide/awt/HtmlRenderer.html"><code>HtmlRenderer</code></a>
     * @since 4.30
     */
    public static interface HtmlStatus extends Status {
        /** Annotate a name such that the returned value contains HTML markup.
         * The return value less the HTML content should typically be the same
         * as the return value from <code>annotateName()</code>.  This is used,
         * for example, by VCS filesystems to deemphasize the status information
         * included in the file name by using a light grey font color.
         * <p>
         * For consistency with <code>Node.getHtmlDisplayName()</code>,
         * filesystems that proxy other filesystems (and so must implement
         * this interface to supply HTML annotations) should return null if
         * the filesystem they proxy does not provide an implementation of
         * {@link FileSystem.HtmlStatus}.
         *
         * @param name the name suggested by default. It cannot contain HTML
         * markup tags but must escape HTML metacharacters. For example
         * "&lt;default package&gt;" is illegal but "&amp;lt;default package&amp;gt;"
         * is fine.
         * @param files an immutable set of {@link FileObject}s belonging to this filesystem
         * @return the annotated name. It may be the same as the passed-in name.
         * It may be null if getStatus returned status that doesn't implement
         * HtmlStatus but plain Status.
         *
         * @since 4.30
         * @see <a href="@org-openide-loaders@/org/openide/loaders/DataNode.html#getHtmlDisplayName()"><code>DataNode.getHtmlDisplayName()</code></a>
         * @see <a href="@org-openide-nodes@/org/openide/nodes/Node.html#getHtmlDisplayName"><code>Node.getHtmlDisplayName()</code></a>
         **/
        public String annotateNameHtml(String name, Set<? extends FileObject> files);
    }

    /** Interface that allows filesystems to set up the Java environment
    * for external execution and compilation.
    * Currently just used to append entries to the external class path.
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public static abstract class Environment extends Object {
        /** Deprecated. */
        public Environment() {
            assert false : "Deprecated.";
        }

        /** Adds one element to the class path environment variable.
        * @param classPathElement string representing the one element
        * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
        */
        @Deprecated
        public void addClassPath(String classPathElement) {
        }
    }

    /** Class used to notify events for the filesystem.
    */
    static abstract class EventDispatcher extends Object implements Runnable {
        public final void run() {
            dispatch(false, null);
        }

        /** @param onlyPriority if true then invokes only priority listeners
         *  else all listeners are invoked.
         */
        protected abstract void dispatch(boolean onlyPriority, Collection<Runnable> postNotify);

        /** @param propID  */
        protected abstract void setAtomicActionLink(EventControl.AtomicActionLink propID);
    }

    private static class FileStatusDispatcher extends EventDispatcher {
        private List<FileStatusListener> listeners;
        private FileStatusEvent fStatusEvent;

        public FileStatusDispatcher(List<FileStatusListener> listeners, FileStatusEvent fStatusEvent) {
            this.listeners = listeners;
            this.fStatusEvent = fStatusEvent;
        }

        protected void dispatch(boolean onlyPriority, Collection<Runnable> postNotify) {
            if (onlyPriority) {
                return;
            }

            for (FileStatusListener fStatusListener : listeners) {
                fStatusListener.annotationChanged(fStatusEvent);
            }
        }

        protected void setAtomicActionLink(EventControl.AtomicActionLink propID) {
            /** empty no fireFrom in FileStatusEvent*/
        }
    }
}
