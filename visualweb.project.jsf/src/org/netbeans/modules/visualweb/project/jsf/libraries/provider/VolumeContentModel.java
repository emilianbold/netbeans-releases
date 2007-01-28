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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.project.jsf.libraries.provider;

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
        return this.content.get (index);
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

    public void removeResources (int[] indices) {
        for (int i=indices.length-1; i>=0; i--) {
            this.content.remove(indices[i]);
        }
        this.impl.setContent (this.volumeType, content);
        this.fireIntervalRemoved(this,indices[0],indices[indices.length-1]);
    }

    public void moveUp (int[] indices) {
        for (int i=0; i< indices.length; i++) {
            Object value = this.content.remove(indices[i]);
            this.content.add(indices[i]-1,value);
        }
        this.impl.setContent (this.volumeType, content);
        this.fireContentsChanged(this,indices[0]-1,indices[indices.length-1]);
    }

    public void moveDown (int[] indices) {
        for (int i=indices.length-1; i>=0; i--) {
            Object value = this.content.remove(indices[i]);
            this.content.add(indices[i]+1,value);
        }
        this.impl.setContent (this.volumeType, content);
        this.fireContentsChanged(this,indices[0],indices[indices.length-1]+1);
    }

}
