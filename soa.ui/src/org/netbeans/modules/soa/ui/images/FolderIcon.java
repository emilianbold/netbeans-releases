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
package org.netbeans.modules.soa.ui.images;

import java.awt.Image;
import java.beans.BeanInfo;
import java.util.concurrent.atomic.AtomicReference;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 *
 */
public class FolderIcon {

    private static AtomicReference<Image> CLOSED_FOLDER_ICON =
            new AtomicReference<Image>();
    
    private static AtomicReference<Image> OPENED_FOLDER_ICON =
            new AtomicReference<Image>();

    private FolderIcon() {
    }
    
    public static Image getOpenedIcon() {
        if (OPENED_FOLDER_ICON.get() == null) {
            Image image = getSystemFolderImage(true);
            OPENED_FOLDER_ICON.compareAndSet(null,image);
        }
        return OPENED_FOLDER_ICON.get();
    }
    
    public static Image getClosedIcon() {
        if (CLOSED_FOLDER_ICON.get() == null) {
            Image image = getSystemFolderImage(false);
            CLOSED_FOLDER_ICON.compareAndSet(null,image);
        }
        return CLOSED_FOLDER_ICON.get();
    }
    
    private static Image getSystemFolderImage(boolean isOpened) {
            Node n = DataFolder.findFolder(Repository.getDefault()
                                .getDefaultFileSystem().getRoot()).getNodeDelegate();
            return isOpened ? n.getOpenedIcon(BeanInfo.ICON_COLOR_16x16) : 
                n.getIcon(BeanInfo.ICON_COLOR_16x16);
    }
    
    public static Image getIcon(int type) {
        return getSystemFolderImage(false);
    }

    public static Image getOpenedIcon(int type) {
        return getSystemFolderImage(true);
    }
}
