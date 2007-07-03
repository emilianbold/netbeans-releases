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

package org.netbeans.api.gsf;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.text.JTextComponent;

/**
 * Interface for actions that should be added into the set of
 * actions managed by the editor kit (which can then be bound to
 * editor keybindings rathr than global shortcuts, etc.)
 * 
 * @todo Provide a way to set the updateMask in BaseAction?
 * 
 * @author Tor Norbye
 */
public interface EditorAction extends Action {
    /** Action was invoked from an editor */
    void actionPerformed(ActionEvent evt, final JTextComponent target);
    String getName();
    Class getShortDescriptionBundleClass();
}
