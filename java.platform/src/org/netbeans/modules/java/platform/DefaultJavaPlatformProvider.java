/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.platform;

import org.netbeans.api.java.platform.JavaPlatform;
import org.openide.filesystems.*;
import org.openide.cookies.InstanceCookie;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;

import java.util.*;
import java.io.IOException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.openide.util.Lookup;

public class DefaultJavaPlatformProvider implements JavaPlatformProvider, FileChangeListener {

    private static final String PLATFORM_STORAGE = "Services/Platforms/org-netbeans-api-java-Platform";  //NOI18N

    private HashSet listeners;
    private FileObject storage;

    public DefaultJavaPlatformProvider () {
        storage = Repository.getDefault().getDefaultFileSystem().findResource(PLATFORM_STORAGE);
        if (storage == null) {
            // Turn this off since it can confuse unit tests running w/o layer merging.
            //assert false : "Cannot find platforms storage";
        }
        else {
            storage.addFileChangeListener (this);
        }
    }

    public JavaPlatform[] getInstalledPlatforms() {
        List platforms = new ArrayList ();
        if (storage != null) {
            try {
                FileObject[] platfomDefinitions = storage.getChildren();
                for (int i=0; i<platfomDefinitions.length;i++) {
                    DataObject dobj = DataObject.find (platfomDefinitions[i]);
                    InstanceCookie ic = (InstanceCookie) dobj.getCookie(InstanceCookie.class);
                    if (ic == null) {
                        continue;
                    }
                    else  if (ic instanceof InstanceCookie.Of) {
                        if (((InstanceCookie.Of)ic).instanceOf(JavaPlatform.class)) {
                            platforms.add (ic.instanceCreate());
                        }
                    }
                    else {
                        Object instance = ic.instanceCreate();
                        if (instance instanceof JavaPlatform) {
                            platforms.add (instance);
                        }
                    }
                }
            }catch (ClassNotFoundException cnf) {
                ErrorManager.getDefault().notify (cnf);
            }
            catch (IOException ioe) {
                ErrorManager.getDefault().notify (ioe);
            }
        }
        return (JavaPlatform[])platforms.toArray(new JavaPlatform[platforms.size()]);
    }
    
    public JavaPlatform getDefaultPlatform() {
        return (JavaPlatform)Lookup.getDefault().lookup(JavaPlatform.class);
    }
    

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (this.listeners == null) {
            this.listeners = new HashSet ();
        }
        this.listeners.add (listener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if (this.listeners == null) {
            return;
        }
        this.listeners.remove (listener);
    }


    public void fileFolderCreated(FileEvent fe) {
    }

    public void fileDataCreated(FileEvent fe) {
        firePropertyChange ();
    }

    public void fileChanged(FileEvent fe) {
        firePropertyChange ();
    }

    public void fileDeleted(FileEvent fe) {
        firePropertyChange ();
    }

    public void fileRenamed(FileRenameEvent fe) {
        firePropertyChange ();
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    private void firePropertyChange () {
        Iterator it = null;
        synchronized (this) {
            if (this.listeners == null) {
                return;
            }
            it = ((Set)this.listeners.clone()).iterator();
        }
        PropertyChangeEvent event = new PropertyChangeEvent (this, PROP_INSTALLED_PLATFORMS, null, null);
        while (it.hasNext()) {
            ((PropertyChangeListener)it.next()).propertyChange(event);
        }
    }       
    
}
