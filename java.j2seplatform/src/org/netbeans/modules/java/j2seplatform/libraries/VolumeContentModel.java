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

package org.netbeans.modules.java.j2seplatform.libraries;

import javax.swing.AbstractListModel;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.net.MalformedURLException;

import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.ErrorManager;

class VolumeContentModel extends AbstractListModel/*<String>*/ {

    private LibraryImplementation impl;
    private String volumeType;
    private List/*<URL>*/ content;

    public VolumeContentModel (LibraryImplementation impl, String volumeType) {
        //TODO: Should listen on the impl
        this.impl = impl;
        this.volumeType = volumeType;
        List l = this.impl.getContent (volumeType);
        if (l != null) {
            this.content = new ArrayList(l);
        }
        else {
            content = new ArrayList();
        }
    }

    public int getSize() {
        return this.content.size();
    }

    public Object getElementAt(int index) {
        if (index < 0 || index >= this.content.size())
            throw new IllegalArgumentException();
        return ((URL)this.content.get (index)).toExternalForm();
    }

    public void addResource (URL resource) {
        if (FileUtil.isArchiveFile(resource)) {
            resource = FileUtil.getArchiveRoot(resource);
        }
        else if (!resource.toExternalForm().endsWith("/")){
            try {
                resource = new URL (resource.toExternalForm()+"/");
            } catch (MalformedURLException mue) {
                ErrorManager.getDefault().notify(mue);
            }
        }
        this.content.add (resource);
        int index = this.content.size()-1;
        this.impl.setContent (this.volumeType, content);
        this.fireIntervalAdded(this,index,index);
    }

    public void removeResource (int index) {
        this.content.remove(index);
        this.impl.setContent (this.volumeType, content);
        this.fireIntervalRemoved(this,index,index);
    }

    public void moveUp (int index) {
        Object value = this.content.remove(index);
        this.content.add(index-1,value);
        this.impl.setContent (this.volumeType, content);
        this.fireContentsChanged(this,index-1,index);
    }

    public void moveDown (int index) {
        Object value = this.content.remove(index);
        this.content.add(index+1,value);
        this.impl.setContent (this.volumeType, content);
        this.fireContentsChanged(this,index,index+1);
    }

}
