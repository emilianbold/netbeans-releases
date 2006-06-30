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


