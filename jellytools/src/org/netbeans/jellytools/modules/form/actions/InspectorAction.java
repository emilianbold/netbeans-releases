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
package org.netbeans.jellytools.modules.form.actions;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.Action;

/** Used to call "Window|GUI Editor|Inspector" main menu item,
 * "org.netbeans.modules.form.actions.InspectorAction" or Ctrl+Shift+2 shortcut.
 * @see Action
 * @author Jiri.Skrivanek@sun.com
 */
public class InspectorAction extends Action {

    // Window|GUI Editor|Inspector
    private static final String inspectorMenu =
        Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Window")+
        "|" +
        Bundle.getStringTrimmed("org.netbeans.modules.form.resources.Bundle", 
                                "Menu/Window/Form")+
        "|" +
        Bundle.getStringTrimmed("org.netbeans.modules.form.actions.Bundle", 
                                "CTL_InspectorAction");
    
    private static final Shortcut shortcut = 
        new Shortcut(KeyEvent.VK_2, KeyEvent.CTRL_MASK|KeyEvent.SHIFT_MASK);

    /** Creates new InspectorAction instance */    
    public InspectorAction() {
        super(inspectorMenu, null, "org.netbeans.modules.form.actions.InspectorAction", shortcut);
    }
}
