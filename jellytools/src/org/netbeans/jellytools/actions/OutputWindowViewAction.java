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

/** Used to call "Window|Output" main menu item,
 * "org.netbeans.core.output.OutputWindowAction" or Ctrl+4 shortcut.
 * @see Action
 */
public class OutputWindowViewAction extends Action {
    private static final String menu =
        Bundle.getStringTrimmed("org.netbeans.core.Bundle",
                                "Menu/Window") +
        "|" +
        Bundle.getStringTrimmed("org.netbeans.core.output2.Bundle",
                                "OutputWindow");
    private static final Shortcut shortcut = 
        new Shortcut(KeyEvent.VK_4, KeyEvent.CTRL_MASK);

    /** Creates new instance. */    
    public OutputWindowViewAction() {
        super(menu, null, "org.netbeans.core.output2.OutputWindowAction", shortcut);
    }
}
