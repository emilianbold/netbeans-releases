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
