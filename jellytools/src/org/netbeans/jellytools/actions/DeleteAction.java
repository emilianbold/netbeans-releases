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

/** Used to call "Delete" popup menu item, "Edit|Delete" main menu item,
 * "org.openide.actions.DeleteAction" or Delete shortcut.
 * @see Action
 * @see ActionNoBlock
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class DeleteAction extends ActionNoBlock {

    private static final String deletePopup = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Delete");
    private static final String deleteMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Edit")
                                            + "|" + deletePopup;
    private static final Shortcut deleteShortcut = new Shortcut(KeyEvent.VK_DELETE);

    /** creates new DeleteAction instance */    
    public DeleteAction() {
        super(deleteMenu, deletePopup, "org.openide.actions.DeleteAction", deleteShortcut);
    }
}