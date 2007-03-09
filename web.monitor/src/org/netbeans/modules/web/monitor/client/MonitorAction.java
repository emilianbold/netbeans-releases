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
            boolean initialized = TransactionView.getInstance().isInitialized();
            // If not initialized yet, this will cause the record to be loaded 
            // from disk, so we don't need to add it in this case
	    openTransactionView(); 
            if (!initialized) {
                return;
            }
	} 
	// Otherwise we add it to the current records
	Controller.getInstance().addTransaction(id); 
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

