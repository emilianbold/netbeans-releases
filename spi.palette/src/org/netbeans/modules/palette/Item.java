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
import org.openide.util.Lookup;

/**
 * Interaface representing palette item.
 *
 * @author S. Aubrecht
 */
public interface Item {
    
    String getName();
    
    String getDisplayName();
    
    String getShortDescription();
    
    Image getIcon(int type);
    
    /**
     * Actions to construct item's popup menu.
     */
    Action[] getActions();
    
    /**
     * Invoked when user double-clicks the item or hits enter key.
     */
    void invokePreferredAction( ActionEvent e );
    
    /**
     * @return Lookup that hold object(s) that palette clients are looking for
     * when user inserts/drops palette item into editor.
     */
    Lookup getLookup();
    
    Transferable drag();
    
    Transferable cut();
}
