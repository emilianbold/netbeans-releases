/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.actions;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.nodes.Node;

/**
 * Save action.
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