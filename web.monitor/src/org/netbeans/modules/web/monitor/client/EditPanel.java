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
 * EditPanel.java
 *
 *
 * Created: Mon Feb  5 13:34:46 2001
 *
 * @author Ana von Klopp
 * @version
 */

/* 
 * TO DO FOR THIS CLASS: 
 *
 * For PUT requests, the only option on the data panel should be to
 * upload a file. 
 *
 * For POST requests, the user should be able to choose between
 * uploading a file or editing parameters. 
 *
 */

package org.netbeans.modules.web.monitor.client; 

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.*;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File; //debugging only
import java.io.PrintWriter; // debugging only

import java.net.*;
import java.text.*;

import javax.swing.*;
import javax.swing.event.*;

import java.util.*;

import org.openide.ErrorManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.modules.web.monitor.data.*;


class EditPanel extends javax.swing.JPanel implements
    ActionListener, ChangeListener {

    private final static boolean debug = false;

    //
    // Code to get the displaying of the tabbed panels correct.
    //
    private int displayType = 0;
    private static final int DISPLAY_TYPE_QUERY   = 0;
    private static final int DISPLAY_TYPE_REQUEST = 1;
    private static final int DISPLAY_TYPE_COOKIES = 2;
    private static final int DISPLAY_TYPE_SERVER  = 3; 
    private static final int DISPLAY_TYPE_HEADERS = 4;

    private transient  Dimension tabD = new Dimension(450,280);

    private EditPanelQuery   queryPanel;
    private EditPanelRequest requestPanel;
    private EditPanelCookies cookiesPanel;
    private EditPanelServer  serverPanel;
    private EditPanelHeaders headersPanel;

    private MonitorData monitorData = null;
    
    // Do we need this to close it?
    private Dialog dialog = null; 
    private DialogDescriptor editDialog = null;
    
    private JButton sendButton;
    private JButton okButton;
    private JButton cancelButton; 

    /* These buttons were used for the feature that allows the user to
     * specify whether the browser's cookie should be used or whether
     * to replace it. In 3.6 ("Promotion B"), it is not
     * possible to configure the monitor to use user-specified
     * cookies, but I leave the method, in case it becomes possible in
     * the future. Basically, we can no longer set the cookie on the
     * server side (the Servlet APIs does not provide any method for
     * doing this) but we could technically tell the browser that
     * issues the replay request to send another cookie (the APIs for
     * that are not there now). If so, the feature can be
     * reintroduced. 
     * 
     * See also (PENDING) for other changes required to reintroduce
     * this feature. 
     */ 
    //private JToggleButton browserCookieButton, savedCookieButton; 
    //private static boolean useBrowserCookie = true;
    
    final static String METHOD = "method"; //NOI18N
    final static String GET = "GET";       //NOI18N
    final static String POST = "POST";     //NOI18N
    final static String PUT = "PUT";       //NOI18N

    private static EditPanel instance = null; 

    static void displayEditPanel(TransactionNode node) { 
	if(debug) log(":: displayEditPanel()"); 
	MonitorData md = null;	    
        // We retrieve the data from the file system, not from the 
        // cache
        md = Controller.getInstance().getMonitorData((TransactionNode)node, 
                                                     false,  // from file
                                                     false); // don't cache
        if (md == null) {
	    // We couldn't get the data. 
            String msg = NbBundle.getMessage(EditPanel.class, "MSG_NoMonitorData");
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, msg);
	    return; 
	}

	if(md.getRequestData().getAttributeValue(METHOD).equals(POST)) 
	    Util.removeParametersFromQuery(md.getRequestData());

	md.getRequestData().deleteCookie("jsessionid"); 

	if(instance == null) instance = new EditPanel(); 
	
	//useBrowserCookie = MonitorAction.getController().getUseBrowserCookie();
	instance.showDialog(md); 
    } 

    static synchronized EditPanel getInstance() { 
	if(instance == null) instance = new EditPanel(); 
	return instance; 
    } 

    private EditPanel() {

	if(debug) log("::CONSTRUCTOR"); 

	createDialogButtons();

	this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

	queryPanel   = new EditPanelQuery();
	requestPanel = new EditPanelRequest();
	cookiesPanel = new EditPanelCookies();
	serverPanel  = new EditPanelServer();
	headersPanel = new EditPanelHeaders();
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(EditPanel.class,"ACS_MON_Replay_panel"));
	JTabbedPane tabs = new JTabbedPane();
        tabs.getAccessibleContext().setAccessibleName(NbBundle.getBundle(EditPanel.class).getString("ACS_MON_Replay_tabsName"));
        tabs.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(EditPanel.class).getString("ACS_MON_Replay_tabsDesc"));

	tabs.setPreferredSize(tabD);
	tabs.addTab(NbBundle.getBundle(EditPanel.class).getString("MON_Query_Panel_Tab"),   queryPanel);
	tabs.addTab(NbBundle.getBundle(EditPanel.class).getString("MON_Request_Panel_Tab"),
		    requestPanel);
	tabs.addTab(NbBundle.getBundle(EditPanel.class).getString("MON_Cookies_Panel_Tab"), cookiesPanel);
	tabs.addTab(NbBundle.getBundle(EditPanel.class).getString("MON_Server_Panel_Tab"),  serverPanel);
	tabs.addTab(NbBundle.getBundle(EditPanel.class).getString("MON_Headers_Panel_Tab"), headersPanel);
	tabs.addChangeListener(this);

	this.add(tabs);
	this.add(Box.createGlue());
	this.add(Box.createVerticalStrut(5));
	// Housekeeping
	this.setMaximumSize(this.getPreferredSize()); 
    }

    void showDialog(MonitorData md) {

	if(debug) { 
	    log("::showDialog"); 
	    log("\tMonitor data is:"); // NOI18N
	    log("t" + md.dumpBeanNode()); // NOI18N
	}

	this.monitorData = md; 

	queryPanel.setData(monitorData);
	requestPanel.setData(monitorData);
	cookiesPanel.setData(monitorData);
	serverPanel.setData(monitorData);
	headersPanel.setData(monitorData);

	Object[] options = {
	    //createSessionButtonPanel(),
	    sendButton,
	    cancelButton,
	};
	
	editDialog = new DialogDescriptor(this, 
					  NbBundle.getBundle(EditPanel.class).getString("MON_EditReplay_panel"),
					  false, 
					  options,
					  options[0],
					  DialogDescriptor.BOTTOM_ALIGN,
					  new HelpCtx("monitor_resend"), //NOI18N
					  this);
        
	dialog = DialogDisplayer.getDefault().createDialog(editDialog);
	dialog.pack();
	dialog.show();
    }
    

    /**
     * Handle user input...
     */

    public void actionPerformed(ActionEvent e) {
	
	boolean debug = false;
	
	if(debug) log("actionPerformed()"); //NOI18N
	 
	String str = new String();
        Object value = editDialog.getValue();
        if (value == null)
            return;
        if (value instanceof JButton)
            str = ((JButton)value).getText();
        else
            str = value.toString();
	if(str.equals(NbBundle.getBundle(EditPanel.class).getString("MON_Send"))) {
	 
	    if(debug) log(" got SEND"); //NOI18N

	    String method =
		monitorData.getRequestData().getAttributeValue(METHOD); 

	    if(method.equals(GET)) 
		Util.composeQueryString(monitorData.getRequestData());

	    /*
	    if(debug) {
		log(" useBrowserCookie is " + //NOI18N
		    String.valueOf(useBrowserCookie));
	    }
	    
	    if(!useBrowserCookie) 
		monitorData.getRequestData().setReplaceSessionCookie(true);

	    if(debug) {
		log(" md.getRD.getReplace is " + //NOI18N
		    String.valueOf(monitorData.getRequestData().getReplaceSessionCookie()));				   
	    }
	    */
	    try {
		MonitorAction.getController().replayTransaction(monitorData);
		dialog.dispose();
	    }
	    catch(UnknownHostException uhe) {
		// Notify the user that there is no host

		Object[] options = {
                    okButton
//		    NbBundle.getBundle(EditPanel.class).getString("MON_OK"),
		};

		NotifyDescriptor noServerDialog = 
		    new NotifyDescriptor
			(NbBundle.getMessage(EditPanel.class, "MON_Exec_server_wrong", monitorData.getServerName()),
			 NbBundle.getBundle(EditPanel.class).getString("MON_Exec_server"),
			 NotifyDescriptor.DEFAULT_OPTION,
			 NotifyDescriptor.INFORMATION_MESSAGE,
			 options,
			 options[0]);
		DialogDisplayer.getDefault().notify(noServerDialog);
		displayType = DISPLAY_TYPE_SERVER;
		showData();
	    }
	    catch(IOException ioe) {
		// Notify the user that the server is not running
		Object[] options = {
		    NbBundle.getBundle(EditPanel.class).getString("MON_OK"),
		};

		Object[] args = {
		    monitorData.getServerName(), 
		    monitorData.getServerPortAsString(), 
		};

		NotifyDescriptor noServerDialog = 
		    new NotifyDescriptor
			(NbBundle.getMessage(EditPanel.class, "MON_Exec_server_start", args),
			 NbBundle.getBundle(EditPanel.class).getString("MON_Exec_server"),
			 NotifyDescriptor.DEFAULT_OPTION,
			 NotifyDescriptor.INFORMATION_MESSAGE,
			 options,
			 options[0]);
		DialogDisplayer.getDefault().notify(noServerDialog);
	    }
	}
	else if(str.equals(NbBundle.getBundle(EditPanel.class).getString("MON_Cancel")))
	    dialog.dispose();
    }

    /**
     * Listens to events from the tab pane, displays different
     * categories of data accordingly. 
     */
    public void stateChanged(ChangeEvent e) {
	if (debug) 
	    log("stateChanged. e = " + e); //NOI18N
	JTabbedPane p = (JTabbedPane)e.getSource();
	displayType = p.getSelectedIndex();

	if(debug) {
	    log("stateChanged. displayType = " + displayType); //NOI18N
	    try {
		StringBuffer buf = new StringBuffer
		    (System.getProperty("java.io.tmpdir")); // NOI18N
		buf.append(System.getProperty("file.separator")); // NOI18N
		buf.append("tab.xml"); // NOI18N
		File file = new File(buf.toString()); 
		FileOutputStream fout = new FileOutputStream(file);
		PrintWriter pw2 = new PrintWriter(fout);
		monitorData.write(pw2);
		pw2.close();
		fout.close();
		log("Wrote replay data to " + // NOI18N 
		    file.getAbsolutePath()); 
	    }
	    catch(Throwable t) {
	    }
	}
	showData();
    }
    

    void showData() {

	if(debug) { 
	    log("Now in showData()"); //NOI18N
	    log("displayType:" //NOI18N
			       + String.valueOf(displayType));
	}

	if (displayType == DISPLAY_TYPE_QUERY)
	    queryPanel.setData(monitorData);
	else if (displayType == DISPLAY_TYPE_REQUEST)
	    requestPanel.setData(monitorData);
	else if (displayType == DISPLAY_TYPE_COOKIES)
	    cookiesPanel.setData(monitorData);
	else if (displayType == DISPLAY_TYPE_SERVER)
	    serverPanel.setData(monitorData);
	else if (displayType == DISPLAY_TYPE_HEADERS)
	    headersPanel.setData(monitorData);

	if(debug) log("Finished showData()"); //NOI18N
    }


    private void createDialogButtons() {

	// Button used by the dialog descriptor
	sendButton = new JButton(NbBundle.getBundle(EditPanel.class).getString("MON_Send"));
	sendButton.setMnemonic(NbBundle.getBundle(EditPanel.class).getString("MON_Send_Mnemonic").charAt(0));
	sendButton.setToolTipText(NbBundle.getBundle(EditPanel.class).getString("ACS_MON_SendA11yDesc"));

	okButton = new JButton(NbBundle.getBundle(EditPanel.class).getString("MON_OK"));
	okButton.setMnemonic(NbBundle.getBundle(EditPanel.class).getString("MON_OK_Mnemonic").charAt(0));
	okButton.setToolTipText(NbBundle.getBundle(EditPanel.class).getString("ACS_MON_OKA11yDesc"));

	cancelButton = new JButton(NbBundle.getBundle(EditPanel.class).getString("MON_Cancel"));
	cancelButton.setMnemonic(NbBundle.getBundle(EditPanel.class).getString("MON_Cancel_Mnemonic").charAt(0));
	cancelButton.setToolTipText(NbBundle.getBundle(EditPanel.class).getString("ACS_MON_CancelA11yDesc"));
    }
    

    // See comment for browserCookieButton above
    /*
    private JToolBar createSessionButtonPanel() { 

	JToolBar buttonPanel = new JToolBar();
	buttonPanel.setFloatable (false);

	// Do we use the browser's cookie or the saved cookie? 
	browserCookieButton = 
	    new JToggleButton(TransactionView.browserCookieIcon,
				    useBrowserCookie); 
	browserCookieButton.setToolTipText(NbBundle.getBundle(EditPanel.class).getString("MON_Browser_cookie"));
	browserCookieButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    browserCookieButton.setSelected(true);
		    savedCookieButton.setSelected(false);
		    useBrowserCookie = true; 
		}

	    });

	savedCookieButton = 
	    new JToggleButton(TransactionView.savedCookieIcon,
				    !useBrowserCookie); 
	savedCookieButton.setToolTipText(NbBundle.getBundle(EditPanel.class).getString("MON_Saved_cookie"));
	savedCookieButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    savedCookieButton.setSelected(true);
		    browserCookieButton.setSelected(false);
		    useBrowserCookie = false; 
		}
	    });

	buttonPanel.add(browserCookieButton);
	buttonPanel.add(savedCookieButton);
	buttonPanel.setSize(buttonPanel.getMinimumSize());
	return buttonPanel;
    }
    */
    
    private Component createSeparator() { 
	JPanel sep = new JPanel() {
		public float getAlignmentX() {
		    return 0;
		}
		public float getAlignmentY() {
		    return 0;
		}
	    };
	sep.setMinimumSize(new Dimension(10, 10));
	return sep;
    }

    private static void log(String s) {
	System.out.println("EditPanel::" + s); //NOI18N
    }
        
} // EditPanel
