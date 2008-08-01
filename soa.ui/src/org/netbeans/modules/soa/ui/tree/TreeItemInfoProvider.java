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

package org.netbeans.modules.soa.ui.tree;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.Utilities;

/**
 * The SPI interface for rendering tree items.
 * 
 * An external code can provide an instance of such interface 
 * to perform required view of tree items. 
 * 
 * @author nk160297
 */
public interface TreeItemInfoProvider {
    String getDisplayName(TreeItem treeItem);
    Icon getIcon(TreeItem treeItem);
    String getToolTipText(TreeItem treeItem);
    
    Icon UNKNOWN_IMAGE = new ImageIcon(Utilities.loadImage(
            "org/netbeans/modules/soa/ui/tree/UNKNOWN_ICON.png"));
    
}
