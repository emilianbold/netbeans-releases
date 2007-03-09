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

package org.netbeans.modules.compapp.casaeditor.palette;

import java.awt.Image;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author rdara
 */
public class CasaPaletteRootNode extends AbstractNode {
    
    
    public CasaPaletteRootNode(Children children, Lookup lookup) {
        super(children, lookup);
        setName("root");
        setDisplayName("root");
    }
    
    public Image getIcon(int type) {
        return Utilities.loadImage("org/netbeans/myfirstexplorer/right-rectangle.png");     // NOI18N
    }
    
    public Image getOpenedIcon(int type) {
        return Utilities.loadImage("org/netbeans/myfirstexplorer/down-rectangle.png");      // NOI18N
    }
    
    public boolean canCut() {
        return false;
    }
    
    public boolean canCopy() {
        return false;
    }
    
    public boolean canRename() {
        return false;
    }
    
    public boolean canDestroy() {
        return false;
    }
}
