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

package org.netbeans.modules.websvc.registry.netbeans;

import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.Action;

/**
 * The top level node representing Web Services in the Server Navigator
 * @author Ludovic
 */
public interface WebServicesRootNodeInterface{    
    public Image getIcon(int type);
    public Image getOpenedIcon(int type);
    public Action[] getActions(boolean context);
    public Action getPreferredAction() ;
    public HelpCtx getHelpCtx();
    
}
