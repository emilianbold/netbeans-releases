/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.lib.editor.codetemplates;

import java.awt.event.ActionEvent;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;

/**
 * Action that jumps to next/previous bookmark.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class AbbrevKitInstallAction extends BaseAction {
    
    static final long serialVersionUID = -0L;
    
    public static final AbbrevKitInstallAction INSTANCE = new AbbrevKitInstallAction();
    
    AbbrevKitInstallAction() {
        super("abbrev-kit-install"); // NOI18N
        putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);        
    }

    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        assert (target != null);
        // Initialize the abbreviation detection for the given component
        AbbrevDetection.get(target); // Initialize the bookmark list
    }

}


