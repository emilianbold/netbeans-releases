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

package org.netbeans.modules.vmd.palette;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.netbeans.modules.vmd.api.model.Debug;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 *
 * @author Anton Chechel
 */
public class PaletteItemDataObject extends MultiDataObject implements FileChangeListener {
    private String producerID;
    private String displayName;
    private String toolTip;
    private String icon;
    private String bigIcon;
    
    public PaletteItemDataObject(FileObject fileObject, PaletteItemDataLoader loader) throws DataObjectExistsException, IOException {
        super(fileObject, loader);
        
        FileChangeListener fileChangeListener = WeakListeners.create(FileChangeListener.class, this, fileObject);
        fileObject.addFileChangeListener(fileChangeListener);
        
        readProperties(fileObject);
    }
    
    @Override
    protected Node createNodeDelegate() {
        return new PaletteItemDataNode(this);
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
    
    private void readProperties(FileObject pf) throws IOException {
        Properties props = new Properties();
        InputStream is = null;
        try {
            is = pf.getInputStream();
            props.load(is);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Debug.warning(e.toString());
                }
            }
        }
        producerID = props.getProperty("producerID"); // NOI18N
        displayName = props.getProperty("displayName"); // NOI18N
        toolTip = props.getProperty("toolTip"); // NOI18N
        icon = props.getProperty("icon"); // NOI18N
        if (icon != null && icon.length() == 0) {
            icon = null;
        }
        bigIcon = props.getProperty("bigIcon"); // NOI18N
        if (bigIcon != null && bigIcon.length() == 0) {
            bigIcon = null;
        }
    }
    
    String getProducerID() {
        return producerID;
    }
    
    String getDisplayName() {
        return displayName;
    }
    
    String getToolTip() {
        return toolTip;
    }
    
    String getIcon() {
        return icon;
    }
    
    String getBigIcon() {
        return bigIcon;
    }
    
    String getProjectType() {
        String path = getPrimaryFile().getPath();
        return path.substring(0, path.indexOf('/')); // NOI18N
    }

    public void fileFolderCreated(FileEvent fe) {
    }

    public void fileDataCreated(FileEvent fe) {
    }

    public void fileChanged(FileEvent fe) {
        try {
            readProperties(fe.getFile());
        } catch (IOException ex) {
            Debug.warning(ex);
        }
    }

    public void fileDeleted(FileEvent fe) {
    }

    public void fileRenamed(FileRenameEvent fe) {
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
    }
}
