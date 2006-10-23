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

    /** Create new DocumentsAction instance. */
    public DocumentsAction() {
        super(menuPath, null, "org.netbeans.core.windows.actions.DocumentsAction");
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
