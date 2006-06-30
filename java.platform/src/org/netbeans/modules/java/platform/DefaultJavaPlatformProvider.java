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
    private JavaPlatform defaultPlatform;

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
                        ErrorManager.getDefault().log(ErrorManager.WARNING,"DefaultPlatformStorage: The file: "+    //NOI18N
                            platfomDefinitions[i].getNameExt()+" hasn't InstanceCookie");                           //NOI18N
                        continue;
                    }
                    else  if (ic instanceof InstanceCookie.Of) {
                        if (((InstanceCookie.Of)ic).instanceOf(JavaPlatform.class)) {
                            platforms.add (ic.instanceCreate());
                        }
                        else {
                            ErrorManager.getDefault().log(ErrorManager.WARNING,"DefaultPlatformStorage: The file: "+    //NOI18N
                                platfomDefinitions[i].getNameExt()+" isn't instance of JavaPlatform");                  //NOI18N
                        }
                    }
                    else {
                        Object instance = ic.instanceCreate();
                        if (instance instanceof JavaPlatform) {
                            platforms.add (instance);
                        }
                        else {
                            ErrorManager.getDefault().log(ErrorManager.WARNING,"DefaultPlatformStorage: The file: "+    //NOI18N
                                platfomDefinitions[i].getNameExt()+" isn't instance of JavaPlatform");                  //NOI18N
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
        if (this.defaultPlatform == null) {
            JavaPlatform[] allPlatforms = this.getInstalledPlatforms();
            for (int i=0; i< allPlatforms.length; i++) {
                if ("default_platform".equals(allPlatforms[i].getProperties().get("platform.ant.name"))) {  //NOI18N
                    defaultPlatform = allPlatforms[i];
                    break;
                }
            }
        }
        return this.defaultPlatform;
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
