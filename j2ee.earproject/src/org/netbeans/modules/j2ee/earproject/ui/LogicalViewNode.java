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
    
    private final AntProjectHelper model;
    private final DataFolder aFolder;
	
    public LogicalViewNode(AntProjectHelper model, DataFolder folder) {
        super(new LogicalViewChildren(model), Lookups.fixed( new Object[] { model }));
        this.model = model;
        this.aFolder = folder;
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
		if(aFolder != null) {
			Node folderNode = aFolder.getNodeDelegate();
			Image image = opened ? folderNode.getOpenedIcon( type ) : folderNode.getIcon( type );
			return Utilities.mergeImages( image, J2EE_MODULES_BADGE, 7, 7 );
		} else {
			// !PW FIXME We need a guarranteed folder node resource to avoid this edge case.
			return J2EE_MODULES_BADGE;
		}
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
