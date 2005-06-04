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

import java.io.*;
import java.util.HashMap;

import org.openide.ErrorManager;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/** Session manager.
 *
 * @author  Jan Pokorsky
 */
public final class SessionManager {
    /** session is opened */
    public static final String PROP_OPEN = "session_open"; // NOI18N
    /** session is closed */
    public static final String PROP_CLOSE = "session_close"; // NOI18N
    /** session layer */
    public static final String LAYER_SESSION = "session"; // NOI18N
    /** instalation layer */
    public static final String LAYER_INSTALL = "install"; // NOI18N
    
    private static SessionManager sm = null;
    /** default system filesystem */
    private SystemFileSystem systemFS;
    private HashMap layers = new HashMap(); //<layer_key, fs>
    private boolean isOpen = false;
    
    /** Utility field holding list of PropertyChangeListeners. */
    private transient java.util.ArrayList propertyChangeListeners;
    
    /** Creates new SessionManager */
    private SessionManager() {
    }
    
    /** get default one */
    public static SessionManager getDefault() {
        if (sm == null) {
            sm = new SessionManager();
        }
        return sm;
    }
    
    /** Initializes and creates new repository. This repository's system fs is
    * based on the content of ${HOME_DIR}/system and ${USER_DIR}/system directories
    *
    * @param userDir directory where user can write 
    * @param homeDir directory where netbeans has been installed, user need not have write access
    * @param extradirs 0+ extra dirs to add; cf. #27151
    * @return repository
    * @exception PropertyVetoException if something fails
    */
    public FileSystem create(File userDir, File homeDir, File[] extradirs)
    throws java.beans.PropertyVetoException, IOException {
        systemFS = SystemFileSystem.create(userDir, homeDir, extradirs);
        layers.put(LAYER_INSTALL, systemFS.getInstallationLayer());
        layers.put(LAYER_SESSION, systemFS.getUserLayer());
        return systemFS;
    }
    
    // The following may be of historical interest and may be useful when impl #26338:
    /** set new project layer into the session
     * @param project subdirectory of the project's directory which is
     * used as a writable filesystem for a project layer in DefaultFileSystem, can be <code>null</code>
     * /
    public void setProjectLayer(FileObject project) throws IOException {
        FilterFileSystem currentPL = (FilterFileSystem) layers.get(LAYER_PROJECT);
        FileSystem newPL = null;
        FileObject current = currentPL == null ? null : currentPL.getRootFileObject ();
        if (project == null && current == null) return;

        if (project != null) {
            newPL = createProjectLayer (project);
        }

        // do not throw Exceptions in the code below, switch the project layer safely
        // otherwise system gets unstable
        if (isOpen) {
            firePropertyChange(PROP_CLOSE);
        }

        waitForLocks ();

        FileSystem[] fss = systemFS.getLayers();
        if (layers.get(LAYER_PROJECT) == null) {
            // add new project layer
            FileSystem[] setfss = new FileSystem[fss.length + 1];
            for (int i = 0; i < fss.length; i++) {
                setfss[i + 1] = fss[i];
            }
            fss = setfss;
        }
        
        if (project == null) {
            // remove project layer
            FileSystem[] setfss = new FileSystem[fss.length - 1];
            for (int i = 0; i < setfss.length; i++) {
                setfss[i] = fss[i + 1];
            }
            fss = setfss;
        } else {
            // switch project layer
            fss[0] = newPL;
        }

        final FileSystem[] fss_1 = fss;
        final FileSystem fs_1 = newPL;
        
        try {
            systemFS.runAtomicAction (new FileSystem.AtomicAction () {
                public void run () {
                    systemFS.setLayers(fss_1);
                    layers.put(LAYER_PROJECT, fs_1);
                }
            });
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
        }

        // wait for filesystems repository to be up to date
        org.netbeans.core.AutomountSupport.initialize ().waitFinished ();

        isOpen = true;
        firePropertyChange(PROP_OPEN);
    }

    /** Creates new project layer according to the root FileObject passed in. 
     * @param project root of the project layer newly created by this method
     * @return FileSystem which can be used as a project layer
     * /
    private FileSystem createProjectLayer (FileObject project) throws IOException {
        FileObject fo = null; // root of project layer

        // if original project layer root doesn't came from DefaultFileSystem then use it
        if (project.getFileSystem () != systemFS) {
            fo = project;
        } else {
            // otherwise try to find it's delegate (prevent cyclic references)
            String name = project.getPath(); //NOI18N
            fo = FileUtil.createFolder (systemFS.getUserLayer ().getLayers () [0].getRoot (), name);
        }
        
        
        // specified FileObject can't be used as a root of the project layer
        if (fo == null)
            throw new IOException ("FileObject " + project + " can't be used as a root of project layer."); //NOI81N
        
        return new FilterFileSystem (fo);
    }
    */

    /** Close session */
    public void close() {
        firePropertyChange(PROP_CLOSE);
        waitForLocks ();
    }
    
    /** get a layer associated with the name
     * @param name layer name (LAYER_SESSION, ...)
     * @return layer, can be <code>null</null>
     */
    public FileSystem getLayer(String name) {
        return (FileSystem) layers.get(name);
    }

    /** Registers PropertyChangeListener to receive events.
     * @param listener The listener to register.
     */
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
        if (propertyChangeListeners == null ) {
            propertyChangeListeners = new java.util.ArrayList();
        }
        propertyChangeListeners.add(listener);
    }
    
    /** Removes PropertyChangeListener from the list of listeners.
     * @param listener The listener to remove.
     */
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
        if (propertyChangeListeners != null ) {
            propertyChangeListeners.remove(listener);
        }
    }
    
    /** Notifies all registered listeners about the event.
     * @param name the name to be fired
     */
    private void firePropertyChange(String name) {
        java.util.ArrayList list;
        synchronized (this) {
            if (propertyChangeListeners == null || propertyChangeListeners.size() == 0) return;
            list = (java.util.ArrayList)propertyChangeListeners.clone();
        }
        java.beans.PropertyChangeEvent event = new java.beans.PropertyChangeEvent(this, name, null, null);
        for (int i = 0; i < list.size(); i++) {
            try {
                ((java.beans.PropertyChangeListener)list.get(i)).propertyChange(event);
            } catch (RuntimeException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    private void waitForLocks () {
        int count = 50; // 5 secs.
        
        try {
            while (LocalFileSystemEx.hasLocks () && 0 < count) {
                Thread.currentThread ().sleep (100);
                count--;
            }
        } catch (InterruptedException e) {
            // ignore
        }
        
        if (LocalFileSystemEx.hasLocks ()) {
//            new Throwable ("SessionManager.waitForLocks callers thread.").printStackTrace ();

            // timed out!
            String locks [] = LocalFileSystemEx.getLocks ();
            StringBuffer msg = new StringBuffer (256);
            msg.append ("Settings saving "); //NOI18N
            msg.append (count == 0 ? "timeout!" : "interrupted!"); //NOI18N
            msg.append ("\nList of pending locks:\n"); //NOI18N
            for (int i = 0; i < locks.length; i++) {
                msg.append (locks[i]);
                msg.append ("\n"); //NOI18N
/*                
                Throwable source = LocalFileSystemEx.getLockSource (locks[i]);
                if (source != null) {
                    StringWriter sw = new StringWriter (1024);
                    PrintWriter w = new PrintWriter (sw);
                    source.printStackTrace (w);
                    w.close ();

                    msg.append (sw.getBuffer ());
                    msg.append ("\n"); //NOI18N
                }
 */
            }
            System.err.println(msg.toString ());
        }
    }
}
