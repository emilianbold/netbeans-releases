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
 * upload a file. You might not want to go ahead with that one yet,
 * because the DTD doesn't support it. :) We can submit an RFE for
 * this and only allow GET and POST in Pilsen as far as I am concerned 
 * :)
 *
 * For POST requests, the user should be able to choose between
 * uploading a file or editing parameters. 
 *
 */

package org.netbeans.modules.web.monitor.client; 

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.*;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.net.*;
import java.text.*;

import javax.swing.*;
import javax.swing.event.*;

import java.util.*;

import org.openide.TopManager;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.awt.ToolbarToggleButton;

import org.netbeans.modules.web.monitor.data.*;


public class EditPanel extends javax.swing.JPanel implements
    ActionListener, ChangeListener {

    private final static boolean debug = false;
    private static final ResourceBundle msgs =
	NbBundle.getBundle(TransactionView.class);
    
    private static final Dimension size = new Dimension(500, 375);
    private static final Dimension reqSize = new Dimension(450, 100);
    private static final Dimension tableSize = new Dimension(450, 100);

    //
    // Code to get the displaying of the tabbed panels correct.
    //
    private int displayType = 0;
    private static final int DISPLAY_TYPE_QUERY   = 0;
    private static final int DISPLAY_TYPE_REQUEST = 1;
    private static final int DISPLAY_TYPE_COOKIES = 2;
    private static final int DISPLAY_TYPE_SERVER  = 3; 
    private static final int DISPLAY_TYPE_HEADERS = 4;

    private transient  Dimension tabD = new Dimension(450,327);

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

    private ToolbarToggleButton browserCookieButton, savedCookieButton; 
    private static boolean useBrowserCookie = true;
    
    final public static String METHOD = "method"; //NOI18N
    final public static String GET = "GET";       //NOI18N
    final public static String POST = "POST";     //NOI18N
    final public static String PUT = "PUT";       //NOI18N
    
    public EditPanel(MonitorData md) {
	super();
	this.monitorData = md;
	createDataPanel(md);
	createDialogButtons();
    }
    

    public void createDataPanel(MonitorData md) {

	this.removeAll();
	this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	//this.add(createButtonPanel());
	
	// Replace the session cookie with the actual value of the
	// session
	Util.setSessionCookieHeader(md);
	if(md.getRequestData().getAttributeValue(METHOD).equals(POST)) 
	    Util.removeParametersFromQuery(md.getRequestData());

	queryPanel   = new EditPanelQuery(md, this);
	requestPanel = new EditPanelRequest(md, this);
	cookiesPanel = new EditPanelCookies(md, this);
	serverPanel  = new EditPanelServer(md, this);
	headersPanel = new EditPanelHeaders(md, this);

	if(debug) System.out.println("in (new) EditPanel.setData()"); //NOI18N

	
	JTabbedPane tabs = new JTabbedPane();
	tabs.setPreferredSize(tabD);
	tabs.addTab(msgs.getString("MON_Query_Panel_Tab"),   queryPanel);
	tabs.addTab(msgs.getString("MON_Request_Panel_Tab"),
		    requestPanel);
	tabs.addTab(msgs.getString("MON_Cookies_Panel_Tab"), cookiesPanel);
	tabs.addTab(msgs.getString("MON_Server_Panel_Tab"),  serverPanel);
	tabs.addTab(msgs.getString("MON_Headers_Panel_Tab"), headersPanel);
	tabs.addChangeListener(this);

	this.add(tabs);
	this.add(Box.createGlue());
	this.add(Box.createVerticalStrut(5));
	// Housekeeping
	this.setMaximumSize(this.getPreferredSize()); 
    }

    public void setData(MonitorData md) {

	this.monitorData = md;
	
	if(debug) System.out.println("in (new) EditPanel.setData()"); //NOI18N

	queryPanel.setData(md);
	requestPanel.setData(md);
	cookiesPanel.setData(md);
	serverPanel.setData(md);
	headersPanel.setData(md);
	useBrowserCookie = MonitorAction.getController().getUseBrowserCookie();
    }

    public void resetQueryPanelData() {
	resetQueryPanelData(monitorData);
    }

    public void resetQueryPanelData(MonitorData monitorData) {
	queryPanel.redisplayData();
    }

    public void showDialog() {

	Object[] options = {
            sendButton,
            cancelButton
	};
	
	editDialog = new DialogDescriptor(this, 
					  msgs.getString("MON_EditReplay"),
					  false, 
					  options,
					  options[0],
					  DialogDescriptor.BOTTOM_ALIGN,
					  null,
					  this);
	
	dialog = TopManager.getDefault().createDialog(editDialog);
	dialog.pack();
	dialog.setSize(size);
	dialog.show();
    }
    

    /**
     * Handle user input...
     */

    public void actionPerformed(ActionEvent e) {
	
	boolean debug = false;
	
	if(debug) System.out.println("EditPanel got action"); //NOI18N
	 
	String str = new String();
        Object value = editDialog.getValue();
        if (value == null)
            return;
        if (value instanceof JButton)
            str = ((JButton)value).getText();
        else
            str = value.toString();
	if(str.equals(msgs.getString("MON_Send"))) {
	 
	    if(debug) System.out.println("EditPanel got SEND"); //NOI18N

	    String method =
		monitorData.getRequestData().getAttributeValue(METHOD); 

	    if(method.equals(GET)) 
		Util.composeQueryString(monitorData.getRequestData());

	    if(debug) {
		System.out.println("useBrowserCookie is " + //NOI18N
				   String.valueOf(useBrowserCookie));
	    }
	    
	    if(!useBrowserCookie) 
		monitorData.getRequestData().setReplaceSessionCookie(true);

	    if(debug) {
		System.out.println("md.getRD.getReplace is " + //NOI18N
				   String.valueOf(monitorData.getRequestData().getReplaceSessionCookie()));				   
	    }

	    try {
		MonitorAction.getController().replayTransaction(monitorData);
		dialog.dispose();
	    }
	    catch(UnknownHostException uhe) {
		// Notify the user that there is no host

		Object[] options = {
                    okButton
//		    msgs.getString("MON_OK"),
		};

		Object[] args = {
		    monitorData.getServletData().getAttributeValue("serverName"), //NOI18N
		};
		
		MessageFormat msgFormat = new MessageFormat
		    (msgs.getString("MON_Exec_server_wrong")); 

		NotifyDescriptor noServerDialog = 
		    new NotifyDescriptor
			(msgFormat.format(args),
			 msgs.getString("MON_Exec_server"),
			 NotifyDescriptor.DEFAULT_OPTION,
			 NotifyDescriptor.INFORMATION_MESSAGE,
			 options,
			 options[0]);
		TopManager.getDefault().notify(noServerDialog);
		displayType = DISPLAY_TYPE_SERVER;
		showData();
	    }
	    catch(IOException ioe) {
		// Notify the user that the server is not running
		Object[] options = {
		    msgs.getString("MON_OK"),
		};

		Object[] args = {
		    monitorData.getServletData().getAttributeValue("serverName"), //NOI18N
		    monitorData.getServletData().getAttributeValue("serverPort"), //NOI18N
		};

		MessageFormat msgFormat = new MessageFormat
		    (msgs.getString("MON_Exec_server_start")); 

		NotifyDescriptor noServerDialog = 
		    new NotifyDescriptor
			(msgFormat.format(args),
			 msgs.getString("MON_Exec_server"),
			 NotifyDescriptor.DEFAULT_OPTION,
			 NotifyDescriptor.INFORMATION_MESSAGE,
			 options,
			 options[0]);
		TopManager.getDefault().notify(noServerDialog);
	    }
	}
	else if(str.equals(msgs.getString("MON_Cancel")))
	    dialog.dispose();
    }

    /*
    public HelpCtx getHelpContext() {
	// PENDING
	String helpID = msgs.getString("MON_Edit_Panel_Help_ID"); // NOI18N
	return new HelpCtx( helpID );
    }
    */

    private void setParameters(Param[] newParams) {
	queryPanel.setParameters(newParams);
    }

    private void setHeaders(Param[] newParams) {
	headersPanel.setHeaders(newParams);
    }
    
    /**
     * Listens to events from the tab pane, displays different
     * categories of data accordingly. 
     */
    public void stateChanged(ChangeEvent e) {
	if (debug) 
	    System.out.println("EditPanel::statChanged. e = " + e); //NOI18N
	JTabbedPane p = (JTabbedPane)e.getSource();
	displayType = p.getSelectedIndex();
	if (debug) System.out.println("EditPanel::statChanged. displayType = " + displayType); //NOI18N
	showData();
    }
    

    void showData() {

	if(debug) { 
	    System.out.println("Now in showData()"); //NOI18N
	    System.out.println("displayType:" //NOI18N
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

	if(debug) System.out.println("Finished showData()"); //NOI18N
    }


    private void createDialogButtons() {

	// Button used by the dialog descriptor
	sendButton = new JButton(msgs.getString("MON_Send"));
	sendButton.setMnemonic(msgs.getString("MON_Send_Mnemonic").charAt(0));
	sendButton.setToolTipText(msgs.getString("ACS_MON_SendA11yDesc"));

	okButton = new JButton(msgs.getString("MON_OK"));
	okButton.setMnemonic(msgs.getString("MON_OK_Mnemonic").charAt(0));
	okButton.setToolTipText(msgs.getString("ACS_MON_OKA11yDesc"));

	cancelButton = new JButton(msgs.getString("MON_Cancel"));
	cancelButton.setMnemonic(msgs.getString("MON_Cancel_Mnemonic").charAt(0));
	cancelButton.setToolTipText(msgs.getString("ACS_MON_CancelA11yDesc"));
    }
    

    JToolBar createSessionButtonPanel() { 

	JToolBar buttonPanel = new JToolBar();
	buttonPanel.setFloatable (false);

	// Do we use the browser's cookie or the saved cookie? 
	browserCookieButton = 
	    new ToolbarToggleButton(TransactionView.browserCookieIcon,
				    useBrowserCookie); 
	browserCookieButton.setToolTipText(msgs.getString("MON_Browser_cookie"));
	browserCookieButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {

		    if(!((ToolbarToggleButton)e.getSource()).isSelected())
			return;
		    else {
			savedCookieButton.setSelected(false);
			useBrowserCookie = true; 
		    }

		}});

	savedCookieButton = 
	    new ToolbarToggleButton(TransactionView.savedCookieIcon,
				    !useBrowserCookie); 
	savedCookieButton.setToolTipText(msgs.getString("MON_Saved_cookie"));
	savedCookieButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    
		    if(!((ToolbarToggleButton)e.getSource()).isSelected())
			return;
		    else {
			browserCookieButton.setSelected(false);
			useBrowserCookie = false; 
		    }
		}});

	buttonPanel.add(browserCookieButton);
	buttonPanel.add(savedCookieButton);
	return buttonPanel;
    }
    


} // EditPanel
