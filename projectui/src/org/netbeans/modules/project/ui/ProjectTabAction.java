/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
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
        super( name, new ImageIcon( Utilities.loadImage( iconResource ) ) );
        this.type = type;
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
        
        TopComponent tc = ProjectTab.findDefault( type == 1 ? ProjectTab.ID_LOGICAL : ProjectTab.ID_PHYSICAL );
        
        tc.open();
        tc.requestActive();

        
    }
    
}
