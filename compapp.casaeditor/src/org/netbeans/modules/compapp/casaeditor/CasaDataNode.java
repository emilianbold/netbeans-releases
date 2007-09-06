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

package org.netbeans.modules.compapp.casaeditor;

import org.openide.loaders.DataNode;
import org.openide.nodes.Children;

import java.awt.Image;


/**
 *
 * @author tli
 */
public class CasaDataNode extends DataNode {

    private static Image icon = org.openide.util.Utilities.loadImage(
            CasaDataObject.CASA_ICON_BASE_WITH_EXT);
    
    private CasaDataObject obj = null;
    
    
    public CasaDataNode(CasaDataObject obj) {
        this(obj, Children.LEAF);
    }

    public CasaDataNode(CasaDataObject obj, Children ch) {
        super(obj, ch);

        this.obj = obj;
        this.getCookieSet().add( obj.getEditorSupport() );
    }

    /**
     * Gets the Icon to represent a module object.
     *
     * @param type The type of Icon to get.
     *
     * @return The icon for a module in the Repository explorer.
     */
    public Image getIcon(int type) {
        return icon;
    }

    /**
     * Gets the opened Icon for our module object. If module is checked in get the mLockedIcon
     * Suresh's change
     *
     * @param type The type of icon.
     *
     * @return The opened icon
     */
    public Image getOpenedIcon(int type) {
        return icon;
    }

    /* (non-Javadoc)
     * @see org.openide.nodes.Node#canRename()
     */
    public boolean canRename() {
        return false;
    }
}
