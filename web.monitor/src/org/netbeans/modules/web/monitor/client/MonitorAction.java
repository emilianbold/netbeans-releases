/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 * MonitorAction.java
 *
 *
 * Created: Wed Feb  2 15:37:25 2000
 *
 * @author Ana von Klopp Lemon
 * @version
 */

package  org.netbeans.modules.web.monitor.client;

import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


public class MonitorAction extends CallableSystemAction {

    static transient TransactionView tv = null; 
    static transient Controller controller = null;
    private static final boolean debug = false;
     
    public MonitorAction() {
	if(controller == null) controller = new Controller();
    }
  
    protected static Controller getController() {
	return controller;
    }
    
    public String getName () {
	return NbBundle.getBundle(MonitorAction.class).getString("MON_HTTP_Transaction_13");
    }
  
    /** No help yet. */
    public HelpCtx getHelpCtx () {
	return new HelpCtx (MonitorAction.class);
    }

    protected String iconResource () {
	return "/org/netbeans/modules/web/monitor/client/icons/menuitem.gif"; //NOI18N

    }

    /**
     * Starts a monitor window. This method is used by the menu item,
     * so it should verify that the execution server is running, and
     * if it isn't, restart it. 
     */
  
    public void performAction() {
	
	if (tv == null) { 
	    if(debug) 
		System.out.println("Transaction view to be created by: " + //NOI18N 
				   "performAction"); //NOI18N
	    tv = new TransactionView(controller);
	    if(debug) 
		System.out.println("Transaction view was created by: " + //NOI18N
				   "performAction"); //NOI18N
	} 
	tv.open(); 
    }
    

    /**
     * This method is used by the JSP/servlet debugger to start the
     * monitor. 
     */
    public static void runMonitor() {
	if (tv == null) 
	    tv = new TransactionView(controller);
	tv.open(); 
    }  

    /**
     * This method is used by the executor to set the hostname and the
     * port name for the monitor, for those situations where the user
     * starts it from the debugging menu. 
     */
    public static void setProperties(String server, int port) {
	if(controller != null) controller.setServer(server, port); 
    }  

    /**
     * This method is used by the executor to set the hostname and the
     * port name for the monitor, for those situations where the user
     * starts it from the debugging menu. 
     */
    public static void cleanupMonitor() {
	// Controller is null when running headless
	if(controller != null) controller.cleanup(); 
    }
}

