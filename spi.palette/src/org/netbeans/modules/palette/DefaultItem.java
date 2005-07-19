/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.palette;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.io.IOException;
import javax.swing.Action;
import org.netbeans.modules.palette.Item;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteActions;
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

    public void invokePreferredAction( InputEvent e, String command ) {
        Action action = itemNode.getPreferredAction();
        if( null != action && action.isEnabled() ) {
            ActionEvent ae = new ActionEvent( e.getSource(), e.getID(), command, e.getModifiers() );
            action.actionPerformed( ae );
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
}
