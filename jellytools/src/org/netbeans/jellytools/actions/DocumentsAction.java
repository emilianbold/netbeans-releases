/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.actions;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.nodes.Node;

/** Used to call "Window|Documents" main menu item,
 * "org.netbeans.core.windows.actions.DocumentsAction" or
 * shortcut Shift+F4.
 * @see ActionNoBlock
 * @author Jiri.Skrivanek@sun.com
 */
public class DocumentsAction extends ActionNoBlock {

    /** Window main menu item. */
    private static final String windowItem = Bundle.getStringTrimmed("org.netbeans.core.Bundle", 
                                                                     "Menu/Window");

    /** "Documents..." main menu item. */
    private static final String menuPath = windowItem
                                            + "|"
                                            + Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
                                                                      "CTL_DocumentsAction");

    private static final Shortcut shortcut = new Shortcut(KeyEvent.VK_F4, KeyEvent.SHIFT_MASK);

    /** Create new DocumentsAction instance. */
    public DocumentsAction() {
        super(menuPath, null, "org.netbeans.core.windows.actions.DocumentsAction", shortcut);
    }
 
    /** Throws UnsupportedOperationException because DocumentsAction doesn't have
     * popup representation on nodes.
     * @param nodes array of nodes
     */
    public void performPopup(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "DocumentsAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because DocumentsAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performAPI(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "DocumentsAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because DocumentsAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performMenu(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "DocumentsAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because DocumentsAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performShortcut(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "DocumentsAction doesn't have popup representation on nodes.");
    }
    
}