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

package org.netbeans.modules.websvc.registry.ui;

import org.netbeans.modules.websvc.registry.jaxrpc.Wsdl2Java;
import org.netbeans.modules.websvc.registry.model.WebServiceData;
import org.netbeans.modules.websvc.registry.model.WSPort;
import org.netbeans.modules.websvc.registry.model.WebServiceGroup;
import org.netbeans.modules.websvc.registry.model.WebServiceListModel;
import org.netbeans.modules.websvc.registry.nodes.WebServiceGroupCookie;
import org.netbeans.modules.websvc.registry.util.Util;
import org.netbeans.modules.websvc.registry.wsdl.WSDLInfo;

import com.sun.xml.rpc.processor.model.java.JavaMethod;
import com.sun.xml.rpc.processor.model.java.JavaParameter;
import com.sun.xml.rpc.processor.model.Port;
import com.sun.xml.rpc.processor.model.Operation;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.Dialog;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.awt.EventQueue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.LinkedList;

import java.lang.reflect.InvocationTargetException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.Position.Bias;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTML;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.JOptionPane;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.awt.Mnemonics;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;






/**
 * Enables searching for Web Services, via an URL, on the local file system
 * or in some uddiRegistry (UDDI)
 * @author  Jeff Hoffman, Octavian Tanase, Winston Prakash
 */
public class AddWebServiceDlg extends JPanel implements ActionListener, HyperlinkListener {
    
    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;
    
    private DialogDescriptor dlg = null;
    private String addString =  "ADD"; //NOI18N
    private String cancelString =  "CANCEL"; //NOI18N
    private String copyString =  "COPY"; //NOI18N
    private String clearString =  "CLEAR"; //NOI18N
    
    /**
     * currentMessages is a running string of what's displayed in the "results" JEditorPane.
     */
    private String currentMessages ="";
    
    /**
     * currentWSDL is used to determine if the user is processing a new WSDL for reporting in the Results
     */
    private String currentWSDL="";
    /**
     * previousWSDL is used to determine if the user is processing a new WSDL for reporting in the Results
     */
    private String previousWSDL="";
    
    /** Keep a list of WSDL files in a properties files */
    static final String  WS_URL_PROPS = "ws_urls.xml"; // NOI18N
    
    private static BufferedWriter outFile;
    
    private WSDLInfo currentWSDLInfo = null;
    
    private Dialog dialog;
    
    private Node invokingNode;
    
    Set webServicesToProcess = null;
    
    private static JFileChooser wsdlFileChooser;
    
    private String URL_WSDL_MSG = NbBundle.getMessage(AddWebServiceDlg.class, "URL_WSDL_MSG");
    private String LOCAL_WSDL_MSG = NbBundle.getMessage(AddWebServiceDlg.class, "LOCAL_WSDL_MSG");
    
    MRUPersistenceManager persistenceManager = new MRUPersistenceManager();
    
    /**
     * This HashMap will serve as a cache of Jar files for the WebServiceData for testing purposes so we
     * don't have to regenerate the Jar file everytime we test a different method.
     * This cache will only be used for testing the client.
     *
     * The cache is updated everytime a jar file has to be generated for a WebServiceData.  The key for
     * the cache will be the WSDL URL.
     */
    
    HashMap jarCache = new HashMap();
    
    private JPanel emptyPanel = new JPanel();
    private JPanel infoTextPanel = new JPanel();
    private JPanel selectionContainer = new JPanel();
    private JPanel selectionPanel = new JPanel();
    private JPanel buttonPanel = new JPanel();
    private JPanel buttonPanelContainer = new JPanel();
    private JPanel centerPanel = new JPanel();
    private JPanel messagePanel = new JPanel();
    
    private JButton cancelButton = new JButton();
    private JButton localFileButton = new JButton();
    private JButton addButton = new JButton();
    private JButton uddiRegistryButton = new JButton();
    private JButton getInfoButton = new JButton();
    
    private JButton httpProxyButton = new JButton();
    
    private JComboBox localFileComboBox = new JComboBox();
    private JComboBox uddiRegistryComboBox = new JComboBox();
    private JComboBox urlComboBox = new JComboBox();
    
    private JEditorPane infoTextArea = new JEditorPane();
    private JScrollPane infoTextScrollPane = new JScrollPane();
    
    private JEditorPane messageTextArea = new JEditorPane();
    private JScrollPane messageTextScrollPane = new JScrollPane();
    
    private JLabel infoTextlabel = new JLabel();
    private JLabel messageTextlabel = new JLabel();
    private JLabel jLabel1 = new JLabel();
    
    private JRadioButton localFileRadioButton = new JRadioButton();
    private JRadioButton uddiRegistryRadioButton = new JRadioButton();
    private JRadioButton urlRadioButton = new JRadioButton();
    
    private JPopupMenu resultsPopup = new JPopupMenu();
    private JMenuItem copyMenuItem = new JMenuItem();
    private JMenuItem clearMenuItem = new JMenuItem();
    
    private ButtonGroup selectionGroup = new ButtonGroup();
    
    private int returnStatus = RET_CANCEL;
    
    private WebServiceGroup websvcGroup;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    
    private HTMLDocument document = new HTMLDocument();
    
        
    private void initAccessibility() {
        infoTextlabel.setLabelFor(infoTextArea);
        messageTextlabel.setLabelFor(messageTextArea);
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "ADD_WEB_SERVICE"));
        localFileButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "BROWSE"));
        urlRadioButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "RADIO_URL"));
        localFileRadioButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "RADIO_LOCAL_FILE"));
        httpProxyButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "SET_HTTP_PROXY"));
        getInfoButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "GET"));
    }
    
    /** Default Constructor, create a modal dialog */
    public AddWebServiceDlg() {
        this(true,null);
    }
    
    /** Creates a dialog that facilitates the selection (and test) of a W/S */
    public AddWebServiceDlg(boolean modal, Node node) {
        invokingNode = node;
        //websvcGroup = wsGroup;
        initComponents();
        initAccessibility();
        
        uddiRegistryComboBox.setRenderer(new WSListCellRenderer());
        wsdlFileChooser = new JFileChooser();
        WSDLFileFilter myFilter = new WSDLFileFilter();
        wsdlFileChooser.setFileFilter(myFilter);
        
        enableControls();
        persistenceManager.loadMRU();
        setDefaults();
    }
    
    public void displayDialog(){
        
        dlg = new DialogDescriptor(this, NbBundle.getMessage(AddWebServiceDlg.class, "ADD_WEB_SERVICE"),
        false, NotifyDescriptor.OK_CANCEL_OPTION, DialogDescriptor.CANCEL_OPTION,
        DialogDescriptor.DEFAULT_ALIGN, this.getHelpCtx(), this);
        addButton.setEnabled(false);
        dlg.setOptions(new Object[] { addButton, cancelButton });
        dialog = DialogDisplayer.getDefault().createDialog(dlg);
        /**
         * After the window is opened, set the focus to the Get information button.
         */
        
        final JPanel thisPanel = this;
        dialog.addWindowListener( new WindowAdapter(){
            public void windowOpened( WindowEvent e ){
                SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        getInfoButton.requestFocus();
                        thisPanel.getRootPane().setDefaultButton(getInfoButton);
                    }
                });
            }
        });
        
        dialog.show();
    }
    
    /** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus() {
        return returnStatus;
    }
    
    private void cancelButtonAction(ActionEvent evt) {
        returnStatus = RET_CANCEL;
        closeDialog();
    }
    
    private void closeDialog() {
        
        persistenceManager.saveMRU();
        dialog.dispose();
        
    }
    private void copyMenuItemAction(ActionEvent evt) {
        messageTextArea.copy();
    }
    private void clearMenuItemAction(ActionEvent evt) {
        currentMessages="";
        messageTextArea.setText("");
    }
    
    /** XXX once we implement context sensitive help, change the return */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(AddWebServiceDlg.class);
    }
    
    private void initComponents() {
        GridBagConstraints gridBagConstraints;
        
        setLayout(new BorderLayout(5, 1));
        
        selectionContainer.setLayout(new BorderLayout());
        
        selectionContainer.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        jLabel1.setText(NbBundle.getMessage(AddWebServiceDlg.class, "SELECT_SOURCE"));
        selectionContainer.add(jLabel1, BorderLayout.NORTH);
        
        selectionPanel.setLayout(new GridBagLayout());
        
        selectionPanel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        Mnemonics.setLocalizedText(urlRadioButton,NbBundle.getMessage(AddWebServiceDlg.class, "RADIO_URL") );
        urlRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                enableControls();
            }
        });
        urlRadioButton.setSelected(true);
        selectionGroup.add(urlRadioButton);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        selectionPanel.add(urlRadioButton, gridBagConstraints);
        
        urlComboBox.setEditable(true);
        
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        selectionPanel.add(urlComboBox, gridBagConstraints);
        
        Mnemonics.setLocalizedText(localFileRadioButton,NbBundle.getMessage(AddWebServiceDlg.class, "RADIO_LOCAL_FILE") );
        localFileRadioButton.setSelected(false);
        localFileRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                enableControls();
            }
        });
        selectionGroup.add(localFileRadioButton);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        selectionPanel.add(localFileRadioButton, gridBagConstraints);
        
        localFileComboBox.setEditable(true);
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        selectionPanel.add(localFileComboBox, gridBagConstraints);
        
        Mnemonics.setLocalizedText(localFileButton,NbBundle.getMessage(AddWebServiceDlg.class, "BROWSE"));
        localFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                localFileButtonAction(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        selectionPanel.add(localFileButton, gridBagConstraints);
        
        
        Mnemonics.setLocalizedText(getInfoButton,NbBundle.getMessage(AddWebServiceDlg.class, "GET"));
        getInfoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                getInfoButtonAction(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 2, 5, 2);
        selectionPanel.add(getInfoButton, gridBagConstraints);
        
        Mnemonics.setLocalizedText(httpProxyButton,NbBundle.getMessage(AddWebServiceDlg.class, "SET_HTTP_PROXY"));
        httpProxyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                new ProxySelectionDialog().show();
            }
        });
        
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(5, 2, 5, 2);
        selectionPanel.add(httpProxyButton, gridBagConstraints);
        
        selectionContainer.add(selectionPanel, BorderLayout.CENTER);
        
        add(selectionContainer, BorderLayout.NORTH);
        
        infoTextPanel.setLayout(new BorderLayout(5, 5));
        
        infoTextPanel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        infoTextArea.setEditable(false);
        /**
         * The focus settings are to allow the text input fields to get focus during tabbing.
         */
        infoTextArea.setFocusable(false);
        infoTextArea.setFocusCycleRoot(false);
        infoTextArea.setContentType("text/html");
        infoTextArea.addHyperlinkListener(this);
        
        infoTextScrollPane.setViewportView(infoTextArea);
        
        infoTextPanel.add(infoTextScrollPane, BorderLayout.CENTER);
        
        Mnemonics.setLocalizedText(infoTextlabel,NbBundle.getMessage(AddWebServiceDlg.class, "WEBSERVICE_INFORMATION"));
        infoTextPanel.add(infoTextlabel, BorderLayout.NORTH);
        
        messagePanel.setLayout(new BorderLayout(5,5));
        messagePanel.setBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        
        messageTextArea.setEditable(false);
        messageTextArea.setContentType("text/html");
        messageTextArea.setToolTipText(NbBundle.getMessage(AddWebServiceDlg.class, "TOOLTIP_MESSAGEAREA"));
        messageTextScrollPane.setViewportView(messageTextArea);
        
        messagePanel.add(messageTextScrollPane, BorderLayout.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(messageTextlabel, NbBundle.getMessage(AddWebServiceDlg.class, "RESULTS"));
        
        messagePanel.add(messageTextlabel, BorderLayout.NORTH);
        
        centerPanel.setLayout(new BorderLayout(5,5));
        
        centerPanel.add(infoTextPanel, BorderLayout.CENTER);
        centerPanel.add(messagePanel, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);
        
        Mnemonics.setLocalizedText(addButton,NbBundle.getMessage(AddWebServiceDlg.class, "Add"));
        addButton.setActionCommand(addString);
        Mnemonics.setLocalizedText(cancelButton,NbBundle.getMessage(AddWebServiceDlg.class, "CANCEL"));
        cancelButton.setActionCommand(cancelString);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-400)/2, (screenSize.height-400)/2, 400, 400);
        
        
        /**
         * set up the context menu for the results area.
         */
        copyMenuItem.setText(NbBundle.getMessage(AddWebServiceDlg.class, "CONTEXTMENU_COPY"));
        copyMenuItem.setActionCommand(copyString);
        copyMenuItem.addActionListener(this);
        resultsPopup.add(copyMenuItem);
        
        clearMenuItem.setText(NbBundle.getMessage(AddWebServiceDlg.class, "CONTEXTMENU_CLEAR"));
        clearMenuItem.setActionCommand(clearString);
        clearMenuItem.addActionListener(this);
        resultsPopup.add(clearMenuItem);
        MouseListener popupListener = new PopupListener();
        messageTextArea.addMouseListener(popupListener);
        
        
    }
    
    private void enableControls(){
        boolean enabled = false;
        enabled = urlRadioButton.isSelected();
        urlComboBox.setEnabled(enabled);
        
        enabled = localFileRadioButton.isSelected();
        localFileComboBox.setEnabled(enabled);
        localFileButton.setEnabled(enabled);
        
        enabled = uddiRegistryRadioButton.isSelected();
        uddiRegistryComboBox.setEnabled(enabled);
        uddiRegistryButton.setEnabled(enabled);
        
    }
    
    
    private String fixFileURL(String inFileURL) {
        String returnFileURL = inFileURL;
        if(returnFileURL.substring(0,1).equalsIgnoreCase("/")) {
            returnFileURL = "file://" + returnFileURL;
        } else {
            returnFileURL = "file:///" + returnFileURL;
        }
        
        return returnFileURL;
    }
    
    private String fixWsdlURL(String inURL) {
        String returnWsdlURL = inURL;
        if (!returnWsdlURL.toLowerCase().endsWith("wsdl")) { // NOI18N
            /**
             * If the user has left the ending withoug WSDL, they are pointing to the
             * web service representation on a web which will if suffixed by a ?WSDL
             * will return the WSDL.  This is true for web services created with JWSDP
             * - David Botterill 3/25/2004
             */
            returnWsdlURL += "?WSDL";
        }
        
        return returnWsdlURL;
    }
    
    private void localFileButtonAction(ActionEvent evt) {
        // pickup user dir from Rave user dir rather than Windows
        File curDir = null;
        File curSelection = null;
        if(localFileComboBox.getSelectedIndex() >= 0){
            curSelection = new File((String)localFileComboBox.getSelectedItem());
            if (curSelection.exists()){
                if (curSelection.isDirectory()) {
                    curDir = curSelection;
                } else {
                    curDir = curSelection.getParentFile();
                }
            }
        }
        
        if (curDir == null) curDir = new File(System.getProperty("netbeans.user"), "websvc");
        if (curSelection != null && curSelection.exists()) wsdlFileChooser.setSelectedFile(curSelection);
        wsdlFileChooser.setCurrentDirectory(curDir);
        
        if (wsdlFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            String localFile = wsdlFileChooser.getSelectedFile().getAbsolutePath();
            if(((DefaultComboBoxModel)localFileComboBox.getModel()).getIndexOf(localFile) == -1) {
                localFileComboBox.addItem(localFile);
            }
            localFileComboBox.setSelectedItem(localFile);
            
            processWSDL(this.fixFileURL(localFile));
        }
    }
    
    private void getInfoButtonAction(ActionEvent evt) {
        /**
         * Determine which radio button is selected Make sure it's not the default.
         */
        
        String urlString = null;
        
        if(this.urlRadioButton.isSelected()) {
            final String urlStr = ((String) urlComboBox.getSelectedItem()).trim();
            /**
             * Make sure we don't have the default.
             */
            if(null != urlStr && urlStr.equalsIgnoreCase(this.URL_WSDL_MSG)) {
                displayInfo("<FONT COLOR=\"RED\">" + NbBundle.getMessage(AddWebServiceDlg.class, "PROCESSING_ERROR") + "</FONT>");
                displayError(NbBundle.getMessage(AddWebServiceDlg.class, "ERROR_MUST_SPECIFY_URL"));
                
            } else {
                
                if(((DefaultComboBoxModel)urlComboBox.getModel()).getIndexOf(urlStr) == -1) {
                    urlComboBox.addItem(urlStr);
                }
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        processWSDL(fixWsdlURL(urlStr));
                    } 
                });
                
            }
        } else if(this.localFileRadioButton.isSelected()) {
            
            final String chosenFile = (String)this.localFileComboBox.getSelectedItem();
            /**
             * Make sure we don't have the default.
             */
            
            if(chosenFile != null && chosenFile.equalsIgnoreCase(this.LOCAL_WSDL_MSG)) {
                displayInfo("<FONT COLOR=\"RED\">" + NbBundle.getMessage(AddWebServiceDlg.class, "PROCESSING_ERROR") + "</FONT>");
                displayError(NbBundle.getMessage(AddWebServiceDlg.class, "ERROR_MUST_LOCAL_FILE"));
            } else {
                /**
                 * If the selection isn't already in the pulldown list, add it.
                 */
                if(((DefaultComboBoxModel)localFileComboBox.getModel()).getIndexOf(chosenFile) == -1) {
                    localFileComboBox.addItem(chosenFile);
                }
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        processWSDL(fixFileURL(chosenFile));
                    }
                });
            }
        }
        
        
        
    }
    
    private void processWSDL(String inWSDLURL) {
        
        
        Cursor normalCursor = dialog.getCursor();
        dialog.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        
        /**
         * First set the currentWSDL that we are processing
         */
        setCurrentWSDL(inWSDLURL);
        
        if(null == inWSDLURL) {
            return;
        }
        URL wsdlUrl = null;
        try {
            wsdlUrl = new URL(inWSDLURL);
        } catch(MalformedURLException mue) {
            displayInfo("<FONT COLOR=\"RED\">" + NbBundle.getMessage(AddWebServiceDlg.class, "PROCESSING_ERROR") + "</FONT>");
            displayError(NbBundle.getMessage(AddWebServiceDlg.class, "INVALID_URL"));
            dialog.setCursor(normalCursor);
            return;
        }
        
        /**
         * generate the WSDL information
         */
        currentWSDLInfo = generateWsdlInfo(wsdlUrl);
        if(null != currentWSDLInfo) {
            /**
             * Set the list of services to process
             */
            this.setWebServicesToProcess(currentWSDLInfo.getWebServices());
            /**
             * diplay the WSDL information
             */
            displayWSDLInfo(currentWSDLInfo.getWebServices());
            /**
             * Scroll the display area to the top
             */
            infoTextArea.setCaretPosition(1);
            
            if (!currentWSDLInfo.hasOperations()) {
                displayError("<FONT COLOR=\"RED\">" + NbBundle.getMessage(AddWebServiceDlg.class, "WARNING_SOLICIT") + "</FONT>");
            }   
        }
        
        dialog.setCursor(normalCursor);
        
    }
    
    
    private String  getCurrentWSDL() {
        return currentWSDL;
    }
    private void  setCurrentWSDL(String inWSDL) {
        currentWSDL = inWSDL;
    }
    
    
    private void uddiRegistryButtonAction(ActionEvent evt) {
    }
    
    private WSDLInfo generateWsdlInfo(final URL wsdlUrl) {
        Set webServices = null;
        
        /**
         * Bugid: 5035872. Need to set the button to false until the WSDL is
         * successfully parsed.
         */
        addButton.setEnabled(false);
        
        WSDLInfo returnWSDLInfo = new  WSDLInfo();
        if (dlg != null) dlg.setValid(false);
        infoTextlabel.setEnabled(true);
        infoTextArea.setEnabled(true);
        
        
        StringWriter infoWriter = new StringWriter();
        infoWriter.write(NbBundle.getMessage(AddWebServiceDlg.class, "PARSING_MSG"));
        infoWriter.write("WSDL - " + wsdlUrl.toString());
        
        displayMessage(infoWriter.toString());
        
        returnWSDLInfo.setWsdlUrl(wsdlUrl);
        Date date = new Date();
        File tmpOutputDir = null;
        try{
            File tempFile = File.createTempFile("wstemp","ws");
            tmpOutputDir = new File(tempFile.getParentFile(), "wstemp" + date.getTime());
            if (tmpOutputDir.exists()) tmpOutputDir.mkdirs();
        }catch (IOException exc){
            displayError(exc.getLocalizedMessage());
            return null;
        }
        
        returnWSDLInfo.setOutputDirectory(tmpOutputDir.getAbsolutePath());
        returnWSDLInfo.setPackageName("webservice");
        returnWSDLInfo.setRemoveGeneratedFiles(false);
        
        if(!returnWSDLInfo.create()){
            /**
             * If we had a problem parsing the WSDL, display the probelm and reset the defaults.
             */
            displayInfo("<FONT COLOR=\"RED\">" + NbBundle.getMessage(AddWebServiceDlg.class, "PROCESSING_ERROR") + "</FONT>");
            displayError(returnWSDLInfo.getErrorMessage());
            return null;
        }
        
        webServices = returnWSDLInfo.getWebServices();
        if(webServices == null){
            displayInfo("<FONT COLOR=\"RED\">" + NbBundle.getMessage(AddWebServiceDlg.class, "PROCESSING_ERROR") + "</FONT>");
            displayError(NbBundle.getMessage(AddWebServiceDlg.class, "WSDL_PARSE_ERROR"));
            return null;
        }
        
        addButton.setEnabled(true);
        /**
         * make sure the get information button maintains the focus and default status.
         */
        getInfoButton.requestFocus();
        this.getRootPane().setDefaultButton(getInfoButton);
        
        
        infoWriter = new StringWriter();
        infoWriter.write(NbBundle.getMessage(AddWebServiceDlg.class, "FINISHED_PARSING_MSG"));
        
        displayMessage(infoWriter.toString());
        
        return returnWSDLInfo;
    }
    
    private void displayWSDLInfo(Set inWebServices) {
        Iterator serviceIterator = inWebServices.iterator();
        WebServiceData webServiceData = null;
        String displayInfo = "";
        while(serviceIterator.hasNext()) {
            Object wsObj = serviceIterator.next();
            if(null != wsObj && wsObj instanceof WebServiceData && null != currentWSDLInfo) {
                webServiceData = (WebServiceData) wsObj;
                String serviceInfo = currentWSDLInfo.getServiceInfo(webServiceData.getName());
                String changedServiceInfo = setDisplayName(webServiceData,serviceInfo);
                displayInfo += changedServiceInfo;
            }
        }
        if(!displayInfo.equals("")) {
            displayInfo(displayInfo);
        }
        
    }
    /**
     * This method will set the display name in the HTML text to a unique name based
     * on the web service name.  This method will check the existing web service names
     * to see if the service name has already been used as a display name.  If it has, it will
     * append a number, starting with 1 and increment that number until a unique name is found.
     *
     */
    private String setDisplayName(WebServiceData inWSData, String inHTMLString) {
        
        WebServiceListModel wsListModel = WebServiceListModel.getInstance();
        String currentDisplayName = inWSData.getName();
        String newDisplayName = currentDisplayName;
        /**
         * The WebServiceData by default has the display name set to the service name.
         */
        for(int ii = 1;wsListModel.webServiceExists(inWSData);ii++) {
            newDisplayName = currentDisplayName + Integer.toString(ii);
            inWSData.setDisplayName(newDisplayName);
        }
        int tokenLocation = inHTMLString.indexOf(WSDLInfo.SERVICE_DISPLAYNAME_TOKEN);
        
        String firstPart = "";
        String lastPart = "";
        if(-1 != tokenLocation) {
            firstPart = inHTMLString.substring(0,tokenLocation );
            lastPart = inHTMLString.substring(tokenLocation + WSDLInfo.SERVICE_DISPLAYNAME_TOKEN.length());
        }
        
        return firstPart + newDisplayName + lastPart;
    }
    
    public void deleteDirectory(File dir){
        File[] children = dir.listFiles();
        for(int i=0; i< children.length; i++){
            if(children[i].isDirectory()){
                deleteDirectory(children[i]);
            }else{
                children[i].delete();
            }
        }
        dir.delete();
    }
    
    private void displayInfo(final String info){
        
        String htmlStart = "<HTML><HEAD>" +
        "<style type=\"text/css\">" +
// !HIE        "body { font-family: Verdana, sans-serif; font-size: 14; }" +
        "body { font-family: Verdana, sans-serif; font-size: 11; }" +
        "</style>" +
        "</HEAD>" +
        "<BODY>";
        String htmlEnd = "</BODY></HTML>";
        
        infoTextArea.setText(htmlStart + info + htmlEnd);
        
    }
    
    
    private void displayError(String inError) {
        displayMessage("<FONT COLOR=\"RED\">" + inError + "</FONT>");
    }
    
    private void displayMessage(String message){
        this.displayMessage(message,true);
    }
    
    private void displayMessage(String message,boolean useTimeStamp){
        /**
         * concatonate the new message onto the existing.
         */
        
        String dateStamp = dateFormat.format(new Date());
        
        String htmlStart = "<HTML><HEAD>" +
        "<style type=\"text/css\">" +
        "body { font-family: Verdana, sans-serif; font-size: 12; }" +
        "</style>" +
        "</HEAD>" +
        "<BODY>";
        String htmlEnd = "</BODY></HTML>";
        
        /**
         * Check to see if the WSDL has changed, if so print a marker
         */
        
        if(!previousWSDL.equals(getCurrentWSDL())) {
            /**
             * Make sure this isn't the first time when the previousWSDL will be blank.
             */
            if(!previousWSDL.equals("")) {
                currentMessages += "<BR><FONT COLOR=\"BLUE\">" + NbBundle.getMessage(AddWebServiceDlg.class, "MESSAGE_END") +
                " " + previousWSDL + "</FONT>";
            }
            currentMessages += "<BR><FONT COLOR=\"BLUE\">" + NbBundle.getMessage(AddWebServiceDlg.class, "MESSAGE_START") +
            " " + getCurrentWSDL() + "</FONT>";
            previousWSDL = getCurrentWSDL();
        }
        
        
        if(useTimeStamp) {
            currentMessages += "<BR><B><I>" + dateStamp + ":&nbsp</I></B>"  + message;
        } else {
            currentMessages += "<BR>"  + message;
        }
        
        messageTextArea.setText(htmlStart + currentMessages + htmlEnd);
    }
    
    private void setWebServicesToProcess(Set inWebServices) {
        this.webServicesToProcess = inWebServices;
    }
    
    private Set getWebServicesToProcess() {
        return this.webServicesToProcess;
    }
    
    /**
     * This represents the event on the "Add" button
     */
    private void addButtonAction(ActionEvent evt) {
        
        
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                Cursor normalCursor = dialog.getCursor();
                dialog.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                addWebServices();
                dialog.setCursor(normalCursor);
            }
        });
        
        
    }
    
    private void addWebServices() {
        
        Set webServices = getWebServicesToProcess();
        if(webServices == null) return;
        Iterator iter =  webServices.iterator();
        boolean duplicateFound = false;
        WebServiceListModel wsListModel = WebServiceListModel.getInstance();
        /**
         * Assume that all of the web services adds failed.  For each success,
         * remove the web service from the failed set.
         */
        HashSet addFailedWebServices = new HashSet();
        addFailedWebServices.addAll(webServices);
        
        while(iter.hasNext()){
            
            WebServiceData wsData = (WebServiceData) iter.next();
            /**
             * First set the display name based on what the user possibly changed it to.
             */
            findValuesInHTML(wsData);
            
            /**
             * Now check the displayname and package name.
             */
            
            if(null == wsData.getDisplayName() || wsData.getDisplayName().equals(" ") ||
            wsData.getDisplayName().length() == 0) {
                displayError(wsData.getName() + ":" + NbBundle.getMessage(AddWebServiceDlg.class, "ERROR_BLANK_WS_DISPLAYNAME"));
            } else if(!Util.isValidIdentifier(wsData.getDisplayName())) {
                displayError(wsData.getDisplayName() + ":" + NbBundle.getMessage(AddWebServiceDlg.class, "ERROR_INVALID_DISPLAYNAME"));
            }else if(null == wsData.getPackageName() || wsData.getPackageName().length() == 0) {
                displayError(wsData.getDisplayName() + ":" + NbBundle.getMessage(AddWebServiceDlg.class, "ERROR_BLANK_WS_PACKAGENAME"));
            } else if(!Util.isValidPackageName(wsData.getPackageName())) {
                displayError(wsData.getPackageName() + ":" + NbBundle.getMessage(AddWebServiceDlg.class, "ERROR_INVALID_PACKAGENAME"));
                
            } else
                
                
                /**
                 * Make sure the web service doesn't already exist in the list model.
                 */
                if(!wsListModel.webServiceExists(wsData)){
                    /**
                     * Now create the client code for the web service.
                     */
                    String jarFileName = System.getProperty("netbeans.user") +"/websvc/" + "webservice" + new Date().getTime() + ".jar";
                    
                    if(!compileWebService(wsData, jarFileName)) {
                        /**
                         * Fix for BUGID: 5048904
                         * -David Botterill
                         */
                        addButton.setEnabled(false);
                        return;
                    }
                    
                    /**
                     * Add it to the list of web services for Server Navigator
                     */
					// !PW Fixed rave code that was using casts instead of 
					// cookies on node objects.
                    wsListModel.addWebService(wsData);
					
                    if(invokingNode != null) {
                        WebServiceGroupCookie groupCookie = (WebServiceGroupCookie) invokingNode.getCookie(WebServiceGroupCookie.class);
                        if(groupCookie != null) {
                            groupCookie.getWebServiceGroup().add(wsData.getId());
                            wsData.setGroupId(groupCookie.getWebServiceGroup().getId());
                        } else {
                            // !PW someone added this action to a node that does
                            // not implement the group cookie interface -- don't do that.
                            
                            // !PW Sometimes the invokingNode is 'WebServicesRootNodeNetBeansSide", which
                            // does not currently expose this cookie.  Not sure why this is happening, but
                            // for now, if we get here, add the service to default and log it.
                            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Add Web Service to Registry :: Invoking node w/ no GroupCookie :: class " + invokingNode.getClass());
                            WebServiceGroup wsGroup = wsListModel.getWebServiceGroup("default");
                            wsGroup.add(wsData.getId());
                            wsData.setGroupId(wsGroup.getId());
                        }
                    } else {
                        // If no selected node on invocation, put service in 'default'.
                        WebServiceGroup wsGroup = wsListModel.getWebServiceGroup("default");
            if(wsGroup == null) wsGroup = new WebServiceGroup("default");
            wsListModel.addWebServiceGroup(wsGroup);
                        wsGroup.add(wsData.getId());
                        wsData.setGroupId(wsGroup.getId());
                    }
					
                    this.displayMessage(wsData.getDisplayName() + " " + NbBundle.getMessage(AddWebServiceDlg.class, "WS_SUCCESSFULLY_ADDED"));
                    
                    //Node node = new WebServicesNode(wsData);
                    //invokingNode.getChildren().add(new Node[]{node});
                    addFailedWebServices.remove(wsData);
                    
                } else{
                    String msg = wsData.getDisplayName() + " " + NbBundle.getMessage(AddWebServiceDlg.class, "WS_ALREADY_EXIST_MSG");
                    NotifyDescriptor d = new Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                }
        }
        
        /**
         * If not all the web services in the WSDL were added, set them up to display again.
         */
        if(addFailedWebServices.size() > 0) {
            displayWSDLInfo(addFailedWebServices);
            this.setWebServicesToProcess(addFailedWebServices);
            
        } else {
            closeDialog();
        }
        
    }
    
    
    
    private boolean compileWebService(WebServiceData inWSData, String inJarFileName) {
            /**
             * Create a piped reader so we can get the output of the client creation.
             *
             */
            PipedOutputStream pos = null;
            BufferedInputStream in = null;
            try {
                pos = new PipedOutputStream();
                in = new BufferedInputStream(new PipedInputStream(pos));
                
                /**
                 * IMPORTANT - Collect the output in a separate thread.  This MUST be done
                 * to avoid a deadlock with the Piped streams.
                 */
                PipedReaderTask readerTask = new PipedReaderTask(in,this.messageTextArea);
                this.displayMessage(NbBundle.getMessage(AddWebServiceDlg.class, "WSCOMPILE_START"));
                Thread pipeThread = new Thread(readerTask);
                pipeThread.start();
                
                if (!Util.createWSJar(inWSData,pos,inJarFileName)) {
                    ErrorManager.getDefault().log(AddWebServiceDlg.class.getName() + ": " + NbBundle.getMessage(AddWebServiceDlg.class, "PROXY_GEN_ERROR"));
                    displayInfo("<FONT COLOR=\"RED\">" + NbBundle.getMessage(AddWebServiceDlg.class, "PROCESSING_ERROR") + "</FONT>");
                    displayError(NbBundle.getMessage(AddWebServiceDlg.class, "PROXY_GEN_ERROR"));
                    return false;
                }
                inWSData.setProxyJarFileName(inJarFileName);
                this.displayMessage(NbBundle.getMessage(AddWebServiceDlg.class, "WSCOMPILE_END"));
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
                displayInfo("<FONT COLOR=\"RED\">" + NbBundle.getMessage(AddWebServiceDlg.class, "PROCESSING_ERROR") + "</FONT>");
                this.displayError(NbBundle.getMessage(AddWebServiceDlg.class, "PROXY_GEN_ERROR"));
                return false;
            } finally {
                if (pos != null) {
                    try {
                        pos.close();
                    } catch (IOException ioe) {
                        // TODO Log this somewhere 
                    }
                }
            }

            return true;
    }
    
    private void findValuesInHTML(WebServiceData inWSData) {
        document = (HTMLDocument)infoTextArea.getDocument();
        Element rootElement = document.getDefaultRootElement();
        Element formElement = null;
        Element inputElement = null;
        /**
         * First, get the Element for the form.
         */
        formElement=document.getElement(rootElement,Attribute.NAME,"service_form." + inWSData.getName());
        /**
         * Next, get the input element named "service_name." + wsdata.getName()
         */
        inputElement = document.getElement(formElement,Attribute.NAME, "service_name." + inWSData.getName());
        if(null != inputElement) {
            
            /**
             * Simply going through the HTMLDocument won't suffice because any textfield changes in an <INPUT>
             * HTML field are not shown.  So, we have to go through the view of the JeditorPane starting at the offset of
             * the named element.
             */
            
            int offset=inputElement.getStartOffset();
            View view=infoTextArea.getUI().getRootView(infoTextArea);
            Component currentComponent=null;
            while (view.getViewCount()>0) {
                if (view instanceof ComponentView) {
                    ComponentView cv=(ComponentView)view;
                    currentComponent=cv.getComponent();
                    break;
                }
                int index=view.getViewIndex(offset,Bias.Forward);
                view=view.getView(index);
            }
            
            if (view instanceof ComponentView) {
                ComponentView cv=(ComponentView)view;
                currentComponent=cv.getComponent();
            }
            
            if (currentComponent instanceof JTextField) {
                JTextField tf=(JTextField)currentComponent;
                inWSData.setDisplayName(tf.getText());
            }
            
            
        }
        /**
         * Next, get the input element named "package_name." + wsdata.getName()
         */
        inputElement = document.getElement(formElement,Attribute.NAME, "package_name." + inWSData.getName());
        if(null != inputElement) {
            
            /**
             * Simply going through the HTMLDocument won't suffice because any textfield changes in an <INPUT>
             * HTML field are not shown.  So, we have to go through the view of the JeditorPane starting at the offset of
             * the named element.
             */
            
            int offset=inputElement.getStartOffset();
            View view=infoTextArea.getUI().getRootView(infoTextArea);
            Component currentComponent=null;
            while (view.getViewCount()>0) {
                if (view instanceof ComponentView) {
                    ComponentView cv=(ComponentView)view;
                    currentComponent=cv.getComponent();
                    break;
                }
                int index=view.getViewIndex(offset,Bias.Forward);
                view=view.getView(index);
            }
            
            if (view instanceof ComponentView) {
                ComponentView cv=(ComponentView)view;
                currentComponent=cv.getComponent();
            }
            
            if (currentComponent instanceof JTextField) {
                JTextField tf=(JTextField)currentComponent;
                inWSData.setPackageName(tf.getText());
            }
            
            
        }
        
        
    }
    
    private void setDefaults() {
        addButton.setEnabled(false);
        urlRadioButton.setSelected(true);
        localFileRadioButton.setSelected(false);
        urlComboBox.setSelectedIndex(0);
        localFileComboBox.setSelectedIndex(0);
        displayInfo("<BR><BR><BR><BR><B>" +NbBundle.getMessage(AddWebServiceDlg.class, "INSTRUCTIONS") + "</B>");
        enableControls();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("http.proxyHost", "webcache.sfbay.sun.com");
        System.setProperty("http.proxyPort", "8080");
        
        // myWS.firstTimeStart();
        System.setProperty("netbeans.home", "D:\\rave-slim\\nbbuild\rave");
        System.setProperty("netbeans.user", "D:\\rave-slim\\userdir");
        AddWebServiceDlg myWSPanel =  new AddWebServiceDlg();
        JDialog dialog = new JDialog();
        dialog.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                System.out.println("Saving ...");
                //persistenceManager.saveMRU();
            }
        });
        dialog.getContentPane().add(myWSPanel);
        dialog.pack();
        dialog.show();
    }
    
    public void actionPerformed(ActionEvent evt) {
        String actionCommand = evt.getActionCommand();
        if(actionCommand.equalsIgnoreCase(addString)) {
            addButtonAction(evt);
        } else if(actionCommand.equalsIgnoreCase(cancelString)) {
            cancelButtonAction(evt);
        } else if(actionCommand.equalsIgnoreCase(copyString)) {
            copyMenuItemAction(evt);
        } else if(actionCommand.equalsIgnoreCase(clearString)) {
            clearMenuItemAction(evt);
        }
        
    }
    
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if(e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
            String link = e.getDescription();
            
            
            /**
             * Parse the link to find out what action to take.
             */
            String [] parts = link.split(WSDLInfo.TOKEN_SEPARATOR);
            if(null == parts) {
                return;
            }
            String actionName = parts[0];
            
            if(actionName.equalsIgnoreCase("test")) {
                testMethod(link);
            }
        }
    }
    
    
    private void testMethod(String inHyperLink) {
        
        /**
         * Parse the link for the service, port, and method.  The patterns is
         * action@service@port@method@|returntype!parametertype:parametertype:..:
         */
        String [] parts = inHyperLink.split(WSDLInfo.TOKEN_SEPARATOR);
        if(null == parts) {
            return;
        }
        
        String actionName = parts[0];
        String serviceName = parts[1];
        String portName = parts[2];
        String methodName = parts[3];
        
        /**
         * Since the neither JavaParameter nor Operation override "equals()", we need to jump through
         * some hoops to compare the method signatures for methods with the same name.
         * -David Botterill 4/22/2004
         */
        
        /**
         * The  signature patter will look like this from the HTML link.
         *
         * returnType!parameter type:parameter type: ...:
         */
        String [] patternParts = inHyperLink.split("\\" + WSDLInfo.SIG_SEPARATOR);
        if(null == patternParts) {
            return;
        }
        String signaturePattern = patternParts[1];
        
        
        
        
        /**
         * Now find the right WebServiceData, Port, and JavaMethod.
         */
        Set services =currentWSDLInfo.getWebServices();
        Iterator serviceIterator = services.iterator();
        JavaMethod method = null;
        WebServiceData wsData = null;
        while(serviceIterator.hasNext()) {
            WebServiceData currentWSData = (WebServiceData)serviceIterator.next();
            if(currentWSData.getName().equals(serviceName)) {
                Port [] ports = currentWSData.getPorts();
                for(int ii=0; null != ports && ii < ports.length; ii++) {
                    if(ports[ii].getName().getLocalPart().equals(portName)) {
                        Iterator operatorIterator = ports[ii].getOperations();
                        while(operatorIterator.hasNext()) {
                            Operation currentOperation = (Operation)operatorIterator.next();
                            JavaMethod currentMethod = currentOperation.getJavaMethod();
                            if(currentMethod.getName().equals(methodName) &&
                            signatureEqual(currentMethod,signaturePattern)) {
                                method = currentMethod;
                                wsData = currentWSData;
                                break;
                            }
                        }
                        if(null != method) {
                            break;
                        }
                    }
                }
            }
            if(null != method) {
                break;
            }
        }
        /**
         * If we found the method, make sure the WebServiceData has a Jar file, if not
         * we need to generate it.
         */
        if(null != wsData) {
            
            if(null == wsData.getProxyJarFileName() || wsData.getProxyJarFileName().length() == 0) {
                
                /**
                 * See if we have a jar in the cache for this WSDL
                 */
                
                String searchWSDL = wsData.getURL();
                String jarFileName = null;
                Object jarFileNameObject = jarCache.get(searchWSDL);
                if(null == jarFileNameObject) {
                    
                    /**
                     * Create a temporary file for the jar.
                     */
                    File tempFile = null;
                    try{
                        tempFile = File.createTempFile("wstemp",".jar");
                        tempFile.deleteOnExit();
                    }catch (IOException ioe){
                        ErrorManager.getDefault().notify(ioe);
                        return;
                    }
                    
                    jarFileName = tempFile.getAbsolutePath();
                    
                    Cursor normalCursor = dialog.getCursor();
                    dialog.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                    
                    boolean compiledOK = compileWebService(wsData,jarFileName);
                    
                    dialog.setCursor(normalCursor);
                    
                    
                    if(!compiledOK) {
                        return;
                    }
                    
                    /**
                     * Add this jarFileName to the cache.
                     */
                    jarCache.put(searchWSDL, jarFileName);
                    
                } else {
                    jarFileName = (String)jarFileNameObject;
                }
                
                /**
                 * set the jar file for this WebServiceData
                 */
                wsData.setProxyJarFileName(jarFileName);
            }
            
            TestWebServiceMethodDlg dlg = new TestWebServiceMethodDlg(wsData, method, portName);
            dlg.displayDialog();
        }
        
        
    }
    private boolean  signatureEqual(JavaMethod inMethod,String inPattern) {
        
        if(null == inMethod || null == inPattern) return false;
        /**
         * first parse the signature into return type and parameters.
         */
        String [] parts = inPattern.split(WSDLInfo.RETURN_SEPARATOR);
        if(null == parts) {
            return false;
        }
        String returnType = parts[0];
        
        /**
         * If we only have one part, then we have no parameters.
         */
        if(parts.length == 1) {
            if(inMethod.getParameterCount() > 0) {
                return false;
            } else {
                return true;
            }
        }
        /**
         * If the return types are different, return false now.
         */
        if(!returnType.equals(inMethod.getReturnType().getRealName())) {
            return false;
        }
        
        StringTokenizer tokenizer = new StringTokenizer(parts[1],WSDLInfo.PARAM_SEPARATOR);
        /**
         * Create an LinkedList to store the parameter types to compare against the
         * JavaMethod.getParameterList().
         *
         *
         */
        LinkedList compareList = new LinkedList();
        
        while(tokenizer.hasMoreTokens()) {
            compareList.add((String)tokenizer.nextToken());
        }
        
        /**
         * Now create a LinkedList of the paramter types.
         */
        Iterator paramIterator = inMethod.getParameters();
        LinkedList inList = new LinkedList();
        while(paramIterator.hasNext()) {
            inList.add((String)((JavaParameter)paramIterator.next()).getType().getRealName());
        }
        
        
        if(!inList.equals(compareList)) {
            return false;
        }
        
        
        
        return true;
    }
    
    private class MRUPersistenceManager {
        File mruFile = new File(System.getProperty("netbeans.user"), WS_URL_PROPS);
        
        public void loadMRU() {
            XMLDecoder decoder = null;
            urlComboBox.addItem(URL_WSDL_MSG);
            localFileComboBox.addItem(LOCAL_WSDL_MSG);
            try {
                decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(mruFile)));
                if (mruFile.exists())  {
                    int wsDataNums = ((Integer)decoder.readObject()).intValue();
                    for(int i=0; i< wsDataNums; i++){
                        String urlItem = (String)decoder.readObject();
                        if(!urlItem.equals(URL_WSDL_MSG))urlComboBox.addItem( urlItem);
                        
                    }
                    
                    wsDataNums = ((Integer)decoder.readObject()).intValue();
                    for(int i=0; i< wsDataNums; i++){
                        String lfItem = (String) decoder.readObject();
                        if(!lfItem.equals(LOCAL_WSDL_MSG))localFileComboBox.addItem(lfItem);
                    }
                    decoder.close();
                }
            } catch (Exception e) {
                //e.printStackTrace();
                if(decoder != null) decoder.close();
            }
        }
        
        public void saveMRU() {
            if (mruFile.exists()) mruFile.delete();
            XMLEncoder encoder = null;
            try {
                encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(mruFile)));
                int itemCount =  urlComboBox.getItemCount();
                encoder.writeObject(new Integer(itemCount));
                for(int i=0; i< itemCount; i++){
                    String urlItem = ((String) urlComboBox.getItemAt(i)).trim();
                    encoder.writeObject(urlItem);
                }
                itemCount =  localFileComboBox.getItemCount();
                encoder.writeObject(new Integer(itemCount));
                for(int i=0; i< itemCount; i++){
                    String lfItem = (String) localFileComboBox.getItemAt(i);
                    encoder.writeObject(lfItem);
                }
                encoder.close();
                
            } catch (Exception e) {
                e.printStackTrace();
                if(encoder != null) encoder.close();
            }
        }
    }
    /**
     * This is the listener for the context menu in the results text area.
     */
    class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }
        
        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                resultsPopup.show(e.getComponent(),
                e.getX(), e.getY());
            }
        }
    }
    
    
    public class PipedReaderTask implements Runnable {
        
        private BufferedInputStream inputStream;
        
        private String pipedOutput = "";
        private JEditorPane messagePane;
        
        public PipedReaderTask(BufferedInputStream inBufferedInput, JEditorPane inMessagePane) {
            inputStream = inBufferedInput;
            messagePane = inMessagePane;
        }
        
        public void run() {
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String currentLine = null;
                while((currentLine =reader.readLine()) != null) {
                    pipedOutput += currentLine;
                    messagePane.setText(pipedOutput);
                }
                reader.close();
            } catch(IOException ioe) {
                pipedOutput += "IOException=" + ioe;
            }
            
        }
        
        public String getPipedOutput() {
            return pipedOutput;
        }
        
        
        
    }
    
}
