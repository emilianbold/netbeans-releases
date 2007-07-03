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

package org.netbeans.modules.refactoring.ruby.ui.tree;

import java.awt.Image;
import java.beans.BeanInfo;
import java.lang.ref.WeakReference;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.refactoring.spi.ui.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Utilities;

/**
 *
 * @author Jan Becicka
 */
public class SourceGroupTreeElement implements TreeElement {
    
    private WeakReference<SourceGroup> sg;
    private FileObject dir;
    private Icon icon;
    private String displayName;
    
    private static String PACKAGE_BADGE = "org/netbeans/modules/ruby/rubyproject/ui/packageBadge.gif"; // NOI18N

    SourceGroupTreeElement(SourceGroup sg) {
        this.sg = new WeakReference<SourceGroup>(sg);
        dir = sg.getRootFolder();
 
        icon = sg.getIcon(false);
        if ( icon == null ) {
            try {
                Image image = DataObject.find(sg.getRootFolder()).getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
                image = Utilities.mergeImages( image, Utilities.loadImage(PACKAGE_BADGE), 7, 7 );
                icon = new ImageIcon(image);
            } catch (DataObjectNotFoundException d) {
            }
        }
        displayName = sg.getDisplayName();
    }

    public TreeElement getParent(boolean isLogical) {
        return TreeElementFactory.getTreeElement(FileOwnerQuery.getOwner(dir));
    }

    public Icon getIcon() {
        return icon;
    }

    public String getText(boolean isLogical) {
        return displayName;
    }

    public Object getUserObject() {
        SourceGroup s = sg.get();
        if (s==null) {
            s = FolderTreeElement.getSourceGroup(dir);
        }
        return s;
    }
}

