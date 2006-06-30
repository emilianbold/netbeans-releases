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

/** Used to call "Clean Project" popup menu item on project's root node.
 * @see Action
 * @see org.netbeans.jellytools.nodes.ProjectRootNode
 * @author Jiri.Skrivanek@sun.com
 */
public class CleanProjectAction extends Action {

    private static final String cleanProjectPopup = Bundle.getString("org.netbeans.modules.java.j2seproject.ui.Bundle", "LBL_CleanAction_Name");

    /** creates new CleanProjectAction instance */
    public CleanProjectAction() {
        super(null, cleanProjectPopup);
    }
}