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

/*
 * GoToAction.java
 *
 * Created on May 30, 2006, 11:24 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.xam.ui.actions;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ui.cookies.GetComponentCookie;
import org.netbeans.modules.xml.xam.ui.cookies.GetSuperCookie;

import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.CookieAction;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.windows.WindowManager;

/**
 * Action which just holds a few other SystemAction's for grouping purposes.
 * @author Ajit Bhate
 */
public class GoToAction extends CookieAction {
    private static final long serialVersionUID = 1L;
    private static ActSubMenuModel model = new ActSubMenuModel(null);
    
    /**
     * Get a human presentable name of the action.
     * This may be
     * presented as an item in a menu.
     * <p>Using the normal menu presenters, an included ampersand
     * before a letter will be treated as the name of a mnemonic.
     *
     * @return the name of the action
     */
    public String getName() {
	return model.createName();
    }
    
    /** List of system actions to be displayed within this one's toolbar or submenu. */
    private static final SystemAction[] grouped() {
	return new SystemAction[] {
	    SystemAction.get(ShowSourceAction.class),
	    SystemAction.get(ShowSchemaAction.class),
	    SystemAction.get(ShowDesignAction.class),
	    SystemAction.get(ShowSuperAction.class),
	};
    }
    
    public HelpCtx getHelpCtx() {
	return HelpCtx.DEFAULT_HELP;
    }
    
    protected void performAction(org.openide.nodes.Node[] activatedNodes) {
	model.performActionAt(0);
    }
    
    public JMenuItem getMenuPresenter() {
	return new Actions.SubMenu(this, model, false);
    }
    
    public JMenuItem getPopupPresenter() {
	return new Actions.SubMenu(this, model, true);
    }
    
    protected int mode() {
	return MODE_EXACTLY_ONE;
    }
    
    protected Class[] cookieClasses() {
	return new Class[]{
	    GetComponentCookie.class,
	    Component.class,
	    GetSuperCookie.class,
	};
    }
    
    private static SystemAction[] getEnabledActions(Node[] nodes) {
	ArrayList<SystemAction> actions = new ArrayList<SystemAction>(grouped().length);
	SystemAction[] grouped = grouped();
	for (int i = 0; i < grouped.length; i++) {
	    SystemAction action = grouped[i];
	    if(action instanceof AbstractShowComponentAction  &&
		((AbstractShowComponentAction)action).enable(nodes))
		actions.add(action);
	}
	return actions.toArray(new SystemAction[actions.size()]);
    }
    
    protected boolean asynchronous() {
	return false;
    }
    
    public Action createContextAwareInstance(Lookup actionContext) {
	return new DelegateAction(this, actionContext);
    }
    
    /** Implementation of ActSubMenuModel */
    private static class ActSubMenuModel extends EventListenerList implements Actions.SubMenuModel {
	static final long serialVersionUID = -4273674308662494596L;
	
	private transient Lookup lookup;
	
	ActSubMenuModel(Lookup lookup) {
	    //this.lookup = lookup;
	}
	
	private Node[] nodes() {
	    if (lookup != null) {
		java.util.Collection c = lookup.lookup(new Lookup.Template(Node.class)).allItems();
		
		if (c.size() == 1) {
		    java.util.Iterator it = c.iterator();
		    
		    while (it.hasNext()) {
			Lookup.Item item = (Lookup.Item) it.next();
			Node n = (Node) item.getInstance();
			
			if (n != null) {
			    return new Node[]{n};
			}
		    }
		}
	    }
	    return WindowManager.getDefault().getRegistry().getCurrentNodes();
	}
	
	private String createName() {
	    SystemAction[] actions = getEnabledActions(nodes());
	    if ((actions != null) && (actions.length == 1)) {
		return NbBundle.getMessage(GoToAction.class,
		    "LBL_GoTo_Name",actions[0].getName());
	    } else {
		return NbBundle.getMessage(GoToAction.class, "LBL_GoTo_Name", "");
	    }
	}
	
	public int getCount() {
	    return getEnabledActions(nodes()).length;
	}
	
	public String getLabel(int index) {
	    SystemAction[] actions = getEnabledActions(nodes());
	    if (actions.length <= index) {
		return null;
	    } else {
		return actions[index].getName();
	    }
	}
	
	public HelpCtx getHelpCtx(int index) {
	    SystemAction[] actions = getEnabledActions(nodes());
	    if (actions.length <= index) {
		return null;
	    } else {
		return actions[index].getHelpCtx();
	    }
	}
	
	public void performActionAt(int index) {
	    Node[] nodes = nodes();
	    SystemAction[] actions = getEnabledActions(nodes);
	    if (actions.length <= index) {
		return;
	    } else {
		SystemAction action = actions[index];
		if(action instanceof AbstractShowComponentAction)
		    ((AbstractShowComponentAction)action).performAction(nodes);
	    }
	}
	
	/** Adds change listener for changes of the model.
	 */
	public void addChangeListener(ChangeListener l) {
	    add(ChangeListener.class, l);
	}
	
	/** Removes change listener for changes of the model.
	 */
	public void removeChangeListener(ChangeListener l) {
	    remove(ChangeListener.class, l);
	}
    }
    // end of ActSubMenuModel
    
    /** A delegate action that is usually associated with a specific lookup and
     * extract the nodes it operates on from it. Otherwise it delegates to the
     * regular NodeAction.
     */
    private static final class DelegateAction extends Object implements javax.swing.Action, Presenter.Menu,
	Presenter.Popup {
	/** Action to delegate to. */
	private final CookieAction delegate;
	
	/** Associated model to use. */
	private final ActSubMenuModel model;
	
	public DelegateAction(CookieAction a, Lookup actionContext) {
	    this.delegate = a;
	    this.model = new ActSubMenuModel(actionContext);
	}
	
	/** Overrides superclass method, adds delegate description. */
	public String toString() {
	    return super.toString() + "[delegate=" + delegate + "]"; // NOI18N
	}
	
	/** Invoked when an action occurs.
	 */
	public void actionPerformed(java.awt.event.ActionEvent e) {
	    model.performActionAt(0);
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
	}
	
	public void putValue(String key, Object o) {
	}
	
	public Object getValue(String key) {
	    if (javax.swing.Action.NAME.equals(key)) {
		return model.createName();
	    } else {
		return delegate.getValue(key);
	    }
	}
	
	public boolean isEnabled() {
	    return model.getCount() > 0;
	}
	
	public void setEnabled(boolean b) {
	}
	
	public javax.swing.JMenuItem getMenuPresenter() {
	    return new Actions.SubMenu(this, model, false);
	}
	
	public javax.swing.JMenuItem getPopupPresenter() {
	    return new Actions.SubMenu(this, model, true);
	}
    }
    // end of DelegateAction
}
