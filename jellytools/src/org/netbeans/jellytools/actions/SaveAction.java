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
package org.netbeans.jellytools.actions;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.nodes.Node;

/** Used to call "Save" popup menu item, "File|Save" main menu item,
 * "org.openide.actions.SaveAction" or Ctrl+S shortcut.
 * @see Action
 * @author Jiri.Skrivanek@sun.com
 */
public class SaveAction extends Action {

    private static final String savePopup = Bundle.getStringTrimmed(
                                            "org.openide.actions.Bundle", "Save");
    private static final String saveMenu = Bundle.getStringTrimmed(
                                            "org.netbeans.core.Bundle", "Menu/File")
                                            + "|" + savePopup;
    private static final Shortcut saveShortcut = new Shortcut(KeyEvent.VK_S, KeyEvent.CTRL_MASK);
    
    /** Creates new SaveAction instance. */
    public SaveAction() {
        super(saveMenu, savePopup, "org.openide.actions.SaveAction", saveShortcut);
    }
    
    /** Throws UnsupportedOperationException because SaveAction doesn't have
     * popup representation on nodes.
     * @param nodes array of nodes
     */
    public void performPopup(Node[] nodes) {
        throw new UnsupportedOperationException("SaveAction doesn't have popup representation on node.");
    }
    
    /** Throws UnsupportedOperationException because SaveAction doesn't have
     * popup representation on node.
     * @param node a node
     */
    public void performPopup(Node node) {
        throw new UnsupportedOperationException("SaveAction doesn't have popup representation on node.");
    }
    
}