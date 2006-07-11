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

package org.netbeans.modules.project.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

public class ProjectTabAction extends AbstractAction {

    private static final String ICON1 = "org/netbeans/modules/project/ui/resources/projectTab.gif"; //NOI18N
    private static final String ICON2 = "org/netbeans/modules/project/ui/resources/filesTab.gif"; //NOI18N
    
    private static final String PHYSICAL_NAME = NbBundle.getMessage( ProjectTabAction.class, "LBL_ProjectsPhysicalTabAction_Name" ); // NOI18N
    private static final String LOGICAL_NAME = NbBundle.getMessage( ProjectTabAction.class, "LBL_ProjectsLogicalTabAction_Name" ); // NOI18N
    
    private int type;
    
    public static Action projectsPhysical() {
        return new ProjectTabAction( PHYSICAL_NAME, ICON2, 0 );
    }
    
    public static Action projectsLogical() {
        return new ProjectTabAction( LOGICAL_NAME, ICON1, 1 );
    }
    
    /** Creates a new instance of BrowserAction */
    public ProjectTabAction( String name, String iconResource, int type ) {
        super( name );
        putValue("iconBase", iconResource); // NOI18N
        this.type = type;
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
        
        TopComponent tc = ProjectTab.findDefault( type == 1 ? ProjectTab.ID_LOGICAL : ProjectTab.ID_PHYSICAL );
        
        tc.open();
        tc.requestActive();

        
    }
    
}
