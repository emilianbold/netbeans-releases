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
package org.netbeans.modules.websvc.core.webservices.action;

import javax.swing.*;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;


public class WebServiceActionGroup extends NodeAction implements Presenter.Popup
{
    public String getName() {
        return NbBundle.getMessage(WebServiceActionGroup.class, "LBL_WebServiceActionGroup");
    }
    
    /** List of system actions to be displayed within this one's toolbar or submenu. */
    private static final SystemAction[] grouped() {
        return new SystemAction[] {
            SystemAction.get(AddOperationAction.class),
        };
    }
    
    public JMenuItem getPopupPresenter() {
        Node[] activatedNodes = getActivatedNodes();
        if (activatedNodes.length == 1 && 
            hasWebService()){
            return new LazyMenu();
        }
        JMenuItem i = super.getPopupPresenter();
        i.setVisible(false);
        return i;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(PromoteBusinessMethodAction.class);
    }
    
    protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
        return true;
    }
    
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
         assert false : "Should never be called: ";
    }
    
    /**
     * See if there is a webservices.xml. If this DD exists, it indicates
     * that a web service exists in the project.
     */    
    private boolean hasWebService() {
        Node[] activatedNodes = getActivatedNodes();
        DataObject dobj = (DataObject)activatedNodes[0].getLookup().lookup(DataObject.class);
        if(dobj != null)
		{
			WebServicesSupport wsSupport = WebServicesSupport.getWebServicesSupport(dobj.getPrimaryFile());
            if(wsSupport != null)
			{
				return (wsSupport.getWebservicesDD() != null);
			}
        }
        return false;
    }


    /**
     * Avoids constructing submenu until it will be needed.
     */
    private final class LazyMenu extends JMenu {
        
        public LazyMenu() {
            super(WebServiceActionGroup.this.getName());
        }
        
        public JPopupMenu getPopupMenu() {
            if (getItemCount() == 0) {
                SystemAction[] grouped = grouped();
                for (int i = 0; i < grouped.length; i++) {
                    SystemAction action = grouped[i];
                    if (action == null) {
                        addSeparator();
                    } else if (action instanceof Presenter.Popup) {
                        add(((Presenter.Popup)action).getPopupPresenter());
                    } else {
                        assert false : "Action had no popup presenter: " + action;
                    }
                }
            }
            return super.getPopupMenu();
        }
 
    }
}
