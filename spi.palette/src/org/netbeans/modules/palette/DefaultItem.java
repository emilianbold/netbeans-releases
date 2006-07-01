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

package org.netbeans.modules.palette;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.Action;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.nodes.*;


/**
 * Default implementation of PaletteItem interface based on Nodes.
 *
 * @author S. Aubrecht
 */
public class DefaultItem implements Item {

    private Node itemNode;

    /**
     * Creates a new instance of DefaultPaletteItem
     *
     * @param itemNode Node representing the palette item.
     */
    public DefaultItem( Node itemNode ) {
        this.itemNode = itemNode;
    }

    public String getName() {
        return itemNode.getName();
    }
    
    public Image getIcon(int type) {
        return itemNode.getIcon( type );
    }

    public Action[] getActions() {
        return itemNode.getActions( false );
    }

    public String getShortDescription() {
        return itemNode.getShortDescription();
    }

    public String getDisplayName() {
        return itemNode.getDisplayName();
    }

    public void invokePreferredAction( ActionEvent e ) {
        Action action = itemNode.getPreferredAction();
        if( null != action && action.isEnabled() ) {
            action.actionPerformed( e );
        }
    }

    public Lookup getLookup() {
        return itemNode.getLookup();
    }

    public boolean equals(Object obj) {
        if( null == obj || !(obj instanceof DefaultItem) )
            return false;
        
        return itemNode.equals( ((DefaultItem) obj).itemNode );
    }

    public Transferable drag() {
        try {
            return itemNode.drag();
        } catch( IOException ioE ) {
            ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, ioE );
        }
        return null;
    }

    public Transferable cut() {
        try {
            return itemNode.clipboardCut();
        } catch( IOException ioE ) {
            ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, ioE );
        }
        return null;
    }
    
    public String toString() {
        return itemNode.getDisplayName();
    }
}
