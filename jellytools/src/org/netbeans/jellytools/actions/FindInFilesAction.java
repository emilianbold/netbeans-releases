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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ProjectsTabOperator;

/** Used to call "Find" popup menu item, "Edit|Find in Projects" main menu item,
 * "org.netbeans.modules.search.FindInFilesAction".
 * @see Action
 * @see ActionNoBlock
 */
public class FindInFilesAction extends ActionNoBlock {
    // "Edit|Find in Projects..."
    private static final String menu =
            Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Edit") +
            "|" +
            Bundle.getStringTrimmed("org.netbeans.modules.search.Bundle", "LBL_Action_FindInProjects");
    // "Find"
    private static final String popup =
            Bundle.getStringTrimmed("org.openide.actions.Bundle", "Find");
    
    /** Creates new instance. */
    public FindInFilesAction() {
        super(menu, popup, "org.netbeans.modules.search.FindInFilesAction");
    }
    
    /** Performs action through API. It selects a node first.
     * @throws UnsupportedOperationException when action does not support API mode */
    public void performAPI() {
        new ProjectsTabOperator().tree().selectRow(0);
        super.performAPI();
    }
}
