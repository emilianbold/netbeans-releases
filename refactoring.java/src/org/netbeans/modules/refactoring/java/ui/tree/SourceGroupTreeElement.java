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

package org.netbeans.modules.refactoring.java.ui.tree;

import java.awt.Image;
import java.beans.BeanInfo;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.netbeans.modules.refactoring.spi.ui.*;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Utilities;

/**
 *
 * @author Jan Becicka
 */
public class SourceGroupTreeElement implements TreeElement {
    
    private SourceGroup sg;
    
    private static String PACKAGE_BADGE = "org/netbeans/spi/java/project/support/ui/packageBadge.gif"; // NOI18N

    SourceGroupTreeElement(SourceGroup sg) {
        this.sg = sg;
    }

    public TreeElement getParent(boolean isLogical) {
        return TreeElementFactory.getTreeElement(FileOwnerQuery.getOwner(sg.getRootFolder()));
    }

    public Icon getIcon() {
        Icon icon = sg.getIcon(false);
        if ( icon == null ) {
            try {
                Image image = DataObject.find(sg.getRootFolder()).getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
                image = Utilities.mergeImages( image, Utilities.loadImage(PACKAGE_BADGE), 7, 7 );
                icon = new ImageIcon(image);
            } catch (DataObjectNotFoundException d) {
            }
        }
        return icon;
    }

    public String getText(boolean isLogical) {
        return sg.getDisplayName();
    }

    public Object getUserObject() {
        return sg;
    }
}

