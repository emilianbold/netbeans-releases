/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 * MonitorAction.java
 *
 *
 * Created: Wed Feb  2 15:37:25 2000
 *
 * @author Ana von Klopp
 * @version
 */

package  org.netbeans.modules.web.monitor.client;

import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.Workspace;
import org.openide.windows.WindowManager;


public class MonitorAction extends CallableSystemAction {

    static transient Controller controller = null;
    private static final boolean debug = false;
     
    public MonitorAction() {
    }

    protected static Controller getController() {
	return Controller.getInstance();
    }
    
    public String getName () {
	return NbBundle.getBundle(MonitorAction.class).getString("MON_HTTP_Transaction_13");
    }
  
    public HelpCtx getHelpCtx () {
	return new HelpCtx (MonitorAction.class);
    }

    protected String iconResource () {
	return "org/netbeans/modules/web/monitor/client/icons/menuitem.gif"; //NOI18N

    }

    protected boolean asynchronous() {
        return false;
    }
    
    /**
     * Starts a monitor window. This method is used by the menu item,
     * so it should verify that the execution server is running, and
     * if it isn't, restart it. 
     */
  
    public void performAction() {
	openTransactionView(); 
    }
   
    static void addTransaction(String id) { 
	if(!TransactionView.getInstance().isOpened()) {
	    openTransactionView(); 
	    // This will cause the record to be loaded from disk, so
	    // we don't need to add it in this case
	} 
	// Otherwise we add it to the current records
	else Controller.getInstance().addTransaction(id); 
    } 

    private static void openTransactionView() {

	TransactionView tv = TransactionView.getInstance(); 
        WindowManager wm = WindowManager.getDefault();
	Mode mode = wm.findMode(tv);
        
        if(mode == null) {
            mode = wm.findMode("output"); // NOI18N
            if(mode != null) {
                mode.dockInto(tv);
            }
        }
	tv.open();
        tv.requestVisible();
        tv.requestActive();        
    }

    public static void log(String s) {
	log("MonitorAction::" + s); //NOI18N
    }
}

