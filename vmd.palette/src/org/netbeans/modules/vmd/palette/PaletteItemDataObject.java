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

package org.netbeans.modules.vmd.palette;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.netbeans.modules.vmd.api.model.Debug;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Node;

/**
 *
 * @author Anton Chechel
 */
public class PaletteItemDataObject extends MultiDataObject {
    private String producerID;
    private String displayName;
    private String toolTip;
    private String icon;
    private String bigIcon;
    
    public PaletteItemDataObject(FileObject pf, PaletteItemDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        readProperties(pf);
    }
    
    protected Node createNodeDelegate() {
        return new PaletteItemDataNode(this);
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
}
