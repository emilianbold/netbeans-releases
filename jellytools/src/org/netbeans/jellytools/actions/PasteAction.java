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
import javax.swing.KeyStroke;
import org.netbeans.jellytools.Bundle;

/** Used to call "Paste" popup menu item, "Edit|Paste" main menu item,
 * "org.openide.actions.PasteAction" or Ctrl+V shortcut.
 * @see Action
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class PasteAction extends Action {

    protected static final String POPUP = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Paste");
    protected static final String MENU = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Edit")
                                            + "|" + POPUP;
    protected static final KeyStroke KEYSTROKE = System.getProperty("os.name").toLowerCase().indexOf("mac") > -1 ?
            KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_MASK) :
            KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK);
    protected static final String SYSTEMCLASS = "org.openide.actions.PasteAction";

    /** creates new PasteAction instance */    
    public PasteAction() {
        super(MENU, POPUP, SYSTEMCLASS, KEYSTROKE);
    }
}
