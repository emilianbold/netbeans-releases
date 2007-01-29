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
package org.netbeans.modules.websvc.core.webservices.action;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.openide.util.Lookup;

/**
 *
 * @author Peter Williams
 */
public class WebServiceClientActionGroup extends NodeAction implements Presenter.Popup {
	
	public String getName() {
            return NbBundle.getMessage(WebServiceClientActionGroup.class, "LBL_WebServiceClientActionGroup"); // NOI18N
	}

	/** List of system actions to be displayed within this one's toolbar or submenu. */
	private static final SystemAction[] grouped() {
		return new SystemAction[] {
			SystemAction.get(InvokeOperationAction.class),
		};
	}

	public JMenuItem getPopupPresenter() {
		Node[] activatedNodes = getActivatedNodes();
		if(activatedNodes.length == 1 && hasWebServiceClient()) {
			return new LazyMenu();
		}
		JMenuItem i = super.getPopupPresenter();
		i.setVisible(false);
		return i;
	}

	public HelpCtx getHelpCtx() {
		// If you will provide context help then use:
		// return new HelpCtx(PromoteBusinessMethodAction.class);
		return HelpCtx.DEFAULT_HELP;
	}

	protected boolean enable(org.openide.nodes.Node[] activatedNodes) {
		return true;
	}

	protected void performAction(org.openide.nodes.Node[] activatedNodes) {
		assert false : "Should never be called: ";
	}

	/**
	 * Returns true if this node is in a project that has any web service clients
	 * added to it.
	 */    
	private boolean hasWebServiceClient() {
		Node[] activatedNodes = getActivatedNodes();
		DataObject dobj = (DataObject)activatedNodes[0].getLookup().lookup(DataObject.class);
		if(dobj != null) {
			WebServicesClientSupport clientSupport = WebServicesClientSupport.getWebServicesClientSupport(dobj.getPrimaryFile());
			if(clientSupport != null) {
				// !PW FIXME add code to confirm that the project actually has
				// clients added to it.
				return true;
			}
		}
		return false;
	}

    public Action createContextAwareInstance(Lookup actionContext) {
        boolean enable = enable(actionContext.lookup(new Lookup.Template<Node>(Node.class)).allInstances().<Node>toArray(new Node[0]));
        return enable ? this : null;
    }
    
    /**
     * Avoids constructing submenu until it will be needed.
     */
    private final class LazyMenu extends JMenu {
        
        public LazyMenu() {
            super(WebServiceClientActionGroup.this.getName());
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
