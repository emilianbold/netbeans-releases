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

package org.netbeans.modules.j2ee.earproject.ui;



import java.awt.Image;
import javax.swing.Action;

import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.loaders.DataFolder;
import org.openide.util.HelpCtx;
import org.openide.util.lookup.Lookups;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.Utilities;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.earproject.ui.actions.AddModuleAction;

import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;

import org.netbeans.spi.project.support.ant.AntProjectHelper;

/**
 * A node with some children.
 * The children are controlled by some underlying data model.
 * Edit this template to work with the classes and logic of your data model.
 * @author vkraemer
 * @author Ludovic Champenois
 */
public class LogicalViewNode extends AbstractNode {

    private static Image J2EE_MODULES_BADGE = Utilities.loadImage( "org/netbeans/modules/j2ee/earproject/ui/resources/application_16.gif", true ); // NOI18N
    private static Image FOLDER = Utilities.loadImage( "org/netbeans/modules/j2ee/earproject/ui/resources/folder.gif", true ); // NOI18N
    private static Image FOLDER_OPEN = Utilities.loadImage( "org/netbeans/modules/j2ee/earproject/ui/resources/folderOpen.gif", true ); // NOI18N
    
    private final AntProjectHelper model;
	
    public LogicalViewNode(AntProjectHelper model) {
        super(new LogicalViewChildren(model), Lookups.fixed( new Object[] { model }));
        this.model = model;
        // Set FeatureDescriptor stuff:
        setName("preferablyUniqueNameForThisNodeAmongSiblings"); // or, super.setName if needed
        setDisplayName(NbBundle.getMessage(LogicalViewNode.class, "LBL_LogicalViewNode"));
        setShortDescription(NbBundle.getMessage(LogicalViewNode.class, "HINT_LogicalViewNode"));
        // Add cookies, e.g.:
        /*
        getCookieSet().add(new OpenCookie() {
                public void open() {
                    // Open something useful...
                    // typically using the data model.
                }
            });
         */
        // Make reorderable (typically will pass in the data model):
        // getCookieSet().add(new ReorderMe());
    }
    
	public Image getIcon( int type ) {        
		return computeIcon( false, type );
	}

	public Image getOpenedIcon( int type ) {
		return computeIcon( true, type );
	}

	private Image computeIcon( boolean opened, int type ) {
            Image image = opened ? FOLDER_OPEN : FOLDER;
            return Utilities.mergeImages( image, J2EE_MODULES_BADGE, 7, 7 );
	}
    
    // Create the popup menu:
    public Action[] getActions(boolean context) {
        return new Action[] {
            SystemAction.get(AddModuleAction.class),
        };
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // When you have help, change to:
        // return new HelpCtx(LogicalViewNode.class);
    }
       
    // Handle copying and cutting specially:
    /**/
    public boolean canCopy() {
        return false;
    }
   
}
