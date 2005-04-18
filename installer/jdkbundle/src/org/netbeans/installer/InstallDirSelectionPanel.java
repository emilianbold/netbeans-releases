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

package org.netbeans.installer;

import com.installshield.product.service.product.ProductService;
import com.installshield.util.Log;
import com.installshield.wizard.WizardBeanEvent;
import com.installshield.wizard.WizardBuilderSupport;
import com.installshield.wizard.awt.AWTWizardUI;
import com.installshield.wizard.awt.MessageDialog;
import com.installshield.wizard.console.ConsoleWizardPanelImpl;
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizardx.panels.ExtendedWizardPanel;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class InstallDirSelectionPanel extends ExtendedWizardPanel implements ActionListener{
    
    private JTextField nbInstallDirTF;
    private JButton    nbBrowseButton;
    private JLabel     nbInputLabel;
    //private JTextArea  nbListLabel;

    private JTextField j2seInstallDirTF;
    private JButton    j2seBrowseButton;
    private JLabel     j2seInputLabel;
    //private JTextArea  j2seListLabel;

    private JPanel mainPanel;
    private JPanel displayPanel;
    private JPanel inputPanel;
    private JTextArea infoTextArea;
    private JTextArea bottomTextArea;
    private JLabel exPathLabel;
    
    private String installedJdk;
    private String installedVersion = "";
    private String backupDirName    = "";
    private String nbLabel          = null;
    private String j2seLabel        = null;
    private String j2seDir          = null;

    private static int NB_INSTALL_DIR   = 1;
    private static int J2SE_INSTALL_DIR = 2;

    private boolean emptyExistingDirNB   = false;
    private boolean emptyExistingDirJ2SE = false;
    
    public void build(WizardBuilderSupport support) {
        super.build(support);
        try {
            support.putClass(Util.class.getName());
            support.putClass(ConsoleWizardPanelImpl.class.getName());
        } catch (Exception ex){
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
    
    public boolean queryEnter(WizardBeanEvent event) {
        try {
            ProductService service = (ProductService) getService(ProductService.NAME);
            String destination = (String) service.getProductBeanProperty(
            ProductService.DEFAULT_PRODUCT_SOURCE, null, "installLocation");
            
            if (! System.getProperty("os.name").startsWith("Windows")) {
                File root = new File("/");
                if (! root.canWrite()) {
                    service.setProductBeanProperty(
                    ProductService.DEFAULT_PRODUCT_SOURCE,
                    null,
                    "installLocation",
                    resolveString("$L(org.netbeans.installer.Bundle, Product.installLocationForNonRoot)"));
                }
            }
        } catch (ServiceException e) {
            logEvent(this, Log.ERROR, e);
        }
        
        return super.queryEnter(event);
    }
    
    public boolean entered(WizardBeanEvent event) {
        nbInstallDirTF.requestFocus();
        return true;
    }
    
    protected void initialize() {
        super.initialize();

	// if null, no previous jdk was found. Always null on unix platforms.
	installedJdk = (String) System.getProperties().get("installedJdk");
	logEvent(this, Log.DBG, "Installed JDK Found: " + installedJdk);

        backupDirName = (String)System.getProperties().get("backupDirName");
        if (backupDirName == null) {
		backupDirName = "backup";
        }

	String tempPath;
	String sysDrive = null;
	if (Util.isWindowsOS()) {
	    StringBuffer drive = new StringBuffer(" :\\");
	    drive.setCharAt(0, getWinSystemDrive());
	    sysDrive = drive.toString();
	    tempPath = resolveString("$D(install)") + "\\Java";
	} else {
	    // String isAdmin = (String)System.getProperties().get("isAdmin");
	    // if (isAdmin != null && isAdmin.equals("yes")) {  // if root user
	    if (Util.isAdmin()) {  // if root user
		tempPath = "/opt";
	    } else {
		tempPath = resolveString("$J(user.home)");
	    }
	}
	logEvent(this, Log.DBG, "Install tempPath: "+ tempPath);
        
        GridBagConstraints gridBagConstraints;
        mainPanel = new JPanel();        
        mainPanel.setLayout(new BorderLayout());
        
	displayPanel = new JPanel(new java.awt.BorderLayout(0,10));
	displayPanel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
        
        infoTextArea = new JTextArea();	
        
        String desc;
        if (Util.isJDKAlreadyInstalled()) {
            if (Util.isJREAlreadyInstalled()) {
                desc = resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.dirSelectionDescErr,"
                + "$L(org.netbeans.installer.Bundle,JDK.shortName),"
                + installedJdk + ","
                + "$L(org.netbeans.installer.Bundle,Product.displayName))");
            } else {
                //Warning message when JDK is installed but public JRE is NOT installed
                desc = resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.dirSelectionDescErrJRE,"
                + "$L(org.netbeans.installer.Bundle,JDK.shortName),"
                + installedJdk + ","
                + "$L(org.netbeans.installer.Bundle,Product.displayName),"
                + "$L(org.netbeans.installer.Bundle,JRE.shortName))");
            }
        } else {
            desc = resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.description,"
            + "$L(org.netbeans.installer.Bundle,JDK.shortName),"
            + "$L(org.netbeans.installer.Bundle,Product.displayName))");
        }
        
        infoTextArea.setText(desc);
        
        infoTextArea.setWrapStyleWord(true);
        infoTextArea.setLineWrap(true);
        infoTextArea.setEditable(false);
        mainPanel.add(infoTextArea, BorderLayout.NORTH);
        
        inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        
	//----------------------------------------------------------------------
	// netbeans install dir components
	String nbInstallDir = resolveString("$P(absoluteInstallLocation)");
	nbLabel = resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.nbInstallDirectoryLabel)");
        nbInputLabel = new JLabel(nbLabel);
	nbInputLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(15, 25, 3, 0);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.5;
	gridBagConstraints.gridheight = 1;
	gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(nbInputLabel, gridBagConstraints);
        
        nbInstallDirTF = new JTextField(nbInstallDir);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 25, 3, 0);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 0.5;
	gridBagConstraints.gridheight = 1;
	gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
	gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(nbInstallDirTF, gridBagConstraints);

        String browseButtonText = resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.browseButtonLabel)");
        nbBrowseButton = new JButton(browseButtonText);
	nbBrowseButton.setActionCommand("nb");
        nbBrowseButton.addActionListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 12, 3, 0);
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 1;
	gridBagConstraints.gridheight = 1;
	gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        inputPanel.add(nbBrowseButton, gridBagConstraints);

	/*String defaultDirLabel = resolveString("$L(com.sun.installer.InstallerResources,DEFAULT_DIR_LABEL)");
        nbListLabel = new JTextArea();	
	nbListLabel.setText("(" + defaultDirLabel + " " + nbInstallDir + ")");
        nbListLabel.setWrapStyleWord(true);
        nbListLabel.setLineWrap(true);
        nbListLabel.setEditable(false);*/
	
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 25, 20, 0);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        //inputPanel.add(nbListLabel, gridBagConstraints);

	//----------------------------------------------------------------------
	// j2se install dir components
        
        //#49200: On Windows JDK 1.5.0 installer uses default installation dir
        //"C:\Program Files\Java\jdk1.5.0" whereas JDK 1.4.2_X uses "C:\j2sdk1.4.2_X"
        //ie. "Program Files" is missing.
	String j2seInstallDir = tempPath + File.separator
        + resolveString("$L(org.netbeans.installer.Bundle,JDK.defaultInstallDirectory)");
	/*j2seDir = resolveString("$L(org.netbeans.installer.Bundle,JDK.defaultInstallDirectory)");
	String j2seInstallDir = null;
	if (Util.isWindowsOS()) {
	    j2seInstallDir = sysDrive + j2seDir;
	} else {
	    j2seInstallDir = tempPath + File.separator + j2seDir;
	}*/
	j2seLabel = resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.jdkInstallDirectoryLabel)");

	if (!Util.isJDKAlreadyInstalled()) {
	    j2seInputLabel = new JLabel(j2seLabel);
	    j2seInputLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
	    gridBagConstraints = new GridBagConstraints();
	    gridBagConstraints.insets = new Insets(0, 25, 3, 0);
	    gridBagConstraints.gridx = 0;
	    gridBagConstraints.gridy = 3;
	    gridBagConstraints.weightx = 0.5;
	    gridBagConstraints.gridheight = 1;
	    gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
	    gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
	    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
	    inputPanel.add(j2seInputLabel, gridBagConstraints);
	    
	    j2seInstallDirTF = new JTextField(j2seInstallDir);
	    gridBagConstraints = new GridBagConstraints();
	    gridBagConstraints.insets = new Insets(0, 25, 3, 0);
	    gridBagConstraints.gridx = 0;
	    gridBagConstraints.gridy = 4;
	    gridBagConstraints.weightx = 0.5;
	    gridBagConstraints.gridheight = 1;
	    gridBagConstraints.gridwidth = 1;
	    gridBagConstraints.anchor = GridBagConstraints.WEST;
	    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
	    inputPanel.add(j2seInstallDirTF, gridBagConstraints);
	    
	    j2seBrowseButton = new JButton();
	    j2seBrowseButton.setText(browseButtonText);
	    j2seBrowseButton.setActionCommand("j2se"); 
	    j2seBrowseButton.addActionListener(this);
	    gridBagConstraints = new GridBagConstraints();
	    gridBagConstraints.insets = new Insets(0, 12, 3, 0);
	    gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
	    gridBagConstraints.gridy = 4;
	    gridBagConstraints.gridheight = 1;
	    gridBagConstraints.gridwidth = 1;
	    gridBagConstraints.anchor = GridBagConstraints.WEST;
	    inputPanel.add(j2seBrowseButton, gridBagConstraints);

	    //j2seListLabel = new JTextArea();
	    //j2seListLabel.setText("(" + defaultDirLabel + " " + j2seInstallDir + ")");	
	    //j2seListLabel.setWrapStyleWord(true);
	    //j2seListLabel.setLineWrap(true);
	    //j2seListLabel.setEditable(false);
	    
	    gridBagConstraints = new GridBagConstraints();
	    gridBagConstraints.insets = new Insets(0, 25, 0, 0);
	    gridBagConstraints.gridx = 0;
	    gridBagConstraints.gridy = 5;
	    gridBagConstraints.gridheight = 1;
	    gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
	    gridBagConstraints.weightx = 0.5;
	    gridBagConstraints.anchor = GridBagConstraints.WEST;
	    gridBagConstraints.fill = GridBagConstraints.BOTH;
	    //inputPanel.add(j2seListLabel, gridBagConstraints);

	    //j2seBrowseButton.setBackground(getContentPane().getBackground());
	    //j2seInputLabel.setBackground(getContentPane().getBackground());
	    //j2seListLabel.setBackground(getContentPane().getBackground());
	}

	//----------------------------------------------------------------------

        mainPanel.add(inputPanel, BorderLayout.CENTER);

	displayPanel.add(mainPanel, BorderLayout.CENTER);

        //displayPanel.setBackground(getContentPane().getBackground());
        //mainPanel.setBackground(getContentPane().getBackground());
        //inputPanel.setBackground(getContentPane().getBackground());
        infoTextArea.setBackground(mainPanel.getBackground());
        //nbBrowseButton.setBackground(getContentPane().getBackground());
        //nbInputLabel.setBackground(getContentPane().getBackground());
        //nbListLabel.setBackground(getContentPane().getBackground());
	
        getContentPane().add(displayPanel, BorderLayout.CENTER);
    }
    
    public boolean queryExit(WizardBeanEvent event) {
        String nbInstallDir = nbInstallDirTF.getText().trim();
        File instDirFile = new File(nbInstallDir);
	String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;
	logEvent(this, Log.DBG, "queryExit productURL: " + productURL);
	// Only get the canonical path if the install dir is not an empty string.
	// getCanonicalPath returns the directory the installer is run from if the
	// install dir is an empty string.
	if (nbInstallDir.length() > 0) {
	    try { // cleanup any misc chars in path such as "."
		nbInstallDir = instDirFile.getCanonicalPath();
		instDirFile = new File(nbInstallDir);
	    } catch (IOException ioerr) {
		logEvent(this, Log.DBG, "IOException: Could not get canonical path: " + ioerr);
		nbInstallDir = instDirFile.getAbsolutePath();
	    }
	}
        nbInstallDirTF.setText(nbInstallDir);

	// Remove the colon at the end of the label strings which is used to prefix
	// the error/warning messages.
	String nbMsgStart = null;
	int index = nbLabel.lastIndexOf(':');
	if (index > 0) {
	    nbMsgStart = nbLabel.substring(0, index);
	} else {
	    nbMsgStart = nbLabel;
	}
        
	// If there is a problem with the specified directory, then return false
        if (!checkInstallDir(nbInstallDir, NB_INSTALL_DIR, nbMsgStart)) {
	    return false;
	}
	try {
	    ProductService service = (ProductService)getService(ProductService.NAME);
	    service.setRetainedProductBeanProperty(productURL, "beanNB", "installLocation", nbInstallDir);
            service.setRetainedProductBeanProperty(productURL, null, "installLocation", nbInstallDir);
            service.setRetainedProductBeanProperty(productURL, null, "absoluteInstallLocation", nbInstallDir);
            //Set install location for Storage Builder
            String sbDestination = nbInstallDir + File.separator + "_uninst" + File.separator + "storagebuilder"; 
            logEvent(this, Log.DBG, "Storage Builder Destination: " + sbDestination);
            service.setRetainedProductBeanProperty(productURL,
            Names.STORAGE_BUILDER_ID, "installLocation", sbDestination);
	} catch (ServiceException e) {
	    logEvent(this, Log.ERROR, e);
	}
	
	Util.setNbInstallDir(nbInstallDir);
	logEvent(this, Log.DBG, "User specified nbInstallDir: " + nbInstallDir);
	
 	// Check the j2se directory
        String j2seMsgStart = null;
        index = j2seLabel.lastIndexOf(':');
        if (index > 0) {
            j2seMsgStart = j2seLabel.substring(0, index);
        } else {
            j2seMsgStart = j2seLabel;
        }
        String j2seInstallDir = null;
        if (Util.isJDKAlreadyInstalled() && Util.isWindowsOS()) {
            try {
                ProductService service = (ProductService)getService(ProductService.NAME);
                service.setRetainedProductBeanProperty(productURL, "beanJ2SE", "active", Boolean.FALSE);
            }catch(ServiceException ex) {
                ex.printStackTrace();
                Util.logStackTrace(this,ex);
            }
        }
        else {
            // There is no jdk already installed so check the j2se directory.
            j2seInstallDir = j2seInstallDirTF.getText().trim();
            instDirFile = new File(j2seInstallDir);

            try { // cleanup any misc chars in path such as "."
                j2seInstallDir = instDirFile.getCanonicalPath();
                instDirFile = new File(j2seInstallDir);
            } catch (IOException ioerr) {
                System.out.println("IOException: Could not get canonical path: " + ioerr);
                j2seInstallDir = instDirFile.getAbsolutePath();
            }
            j2seInstallDirTF.setText(j2seInstallDir);

            // If there is a problem with the specified directory, then return false
            if (!checkInstallDir(j2seInstallDir, J2SE_INSTALL_DIR, j2seMsgStart)) {
                return false;
            }
            try {
                ProductService service = (ProductService)getService(ProductService.NAME);
                if (Util.isWindowsOS()) {
                    service.setRetainedProductBeanProperty(productURL, "beanJ2SE", "installLocation", nbInstallDir);
                } else {
                    service.setRetainedProductBeanProperty(productURL, "beanJ2SE", "installLocation", j2seInstallDir);
                }
            } catch (ServiceException e) {
                logEvent(this, Log.ERROR, e);
            }
            Util.setJdkHome(j2seInstallDir);
            Util.setJ2SEInstallDir(j2seInstallDir);
            logEvent(this, Log.DBG, "User specified j2seInstallDir: " + j2seInstallDir);

            // Last thing to do is create the J2SE directory unless the
            // directory exists and is empty.
            if (!emptyExistingDirJ2SE) {
                if (!createDirectory(j2seInstallDir, j2seMsgStart)) {
                    return false;
                }
            }
        }
        
        //#49348: Do not allow the same dir for JDK and NB.
        if (!checkBothInstallDirs(nbInstallDir,j2seInstallDir)) {
            return false;
        }
        
	// Last thing to do is create the NB directory unless the
	// directory exists and is empty. Creating directory as last thing
	// to do so as not to create  when having problems with checking
	// the j2se directory.
	if (!emptyExistingDirNB) {
	    if (!createDirectory(nbInstallDir, nbMsgStart)) {
                return false;
            }
	}
	return true;
    }
    
    /* Check if J2SE and NB installation dirs are the same or not.
     */
    private boolean checkBothInstallDirs(String nbDir, String j2seDir) {
        String[] okString  = {resolveString("$L(com.installshield.wizard.i18n.WizardResources, ok)")};
	String dialogTitle = resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryDialogTitle)");
        Frame parent = ((AWTWizardUI)getWizard().getUI()).getFrame();
        String dialogMsg = "";
        MessageDialog msgDialog = null;
        
        //#49348: Do not allow the same dir for JDK and NB.
        if (nbDir.equals(j2seDir)) {
            dialogMsg = resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryNBJDKTheSame)");
            msgDialog = new MessageDialog(parent, dialogMsg, dialogTitle, okString);
            msgDialog.setVisible(true);
            return false;
        }
        return true;
    }
    
    /* Check the installation directory to see if it has illegal chars,
     * or if it already exits or if it's empty then create it.
     */ 
    private boolean checkInstallDir(String dir, int installDirType, String msgPrefix) {

        String[] okString  = {resolveString("$L(com.installshield.wizard.i18n.WizardResources, ok)")};
	String dialogTitle = resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryDialogTitle)");
        StringTokenizer st = new StringTokenizer(dir);
        Frame parent = ((AWTWizardUI)getWizard().getUI()).getFrame();
        String dialogMsg = "";
        MessageDialog msgDialog = null;

        if (dir.length() == 0) {
            //Directory must be specified
            dialogMsg = msgPrefix + " "
            + resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryNotSpecifiedMessage)");
            msgDialog = new MessageDialog(parent, dialogMsg, dialogTitle, okString);
            msgDialog.setVisible(true);
            return false;
        } else if (st.countTokens() > 1) {
            //Check for spaces in path - it is not supported by JDK installer on Linux/Solaris
	    if (!Util.isWindowsOS() && installDirType == J2SE_INSTALL_DIR) {
		dialogMsg = msgPrefix + " "
		+ resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryHasSpaceMessage)");
		msgDialog = new MessageDialog(parent, dialogMsg, dialogTitle, okString);
		msgDialog.setVisible(true);
		return false;
	    }
	} else if ((Util.isWindowsOS() && (dir.indexOf("@") != -1 || dir.indexOf("&") != -1 ||
					 dir.indexOf("%") != -1 || dir.indexOf("#") != -1)) ||
	    dir.indexOf("+") != -1 || dir.indexOf("\"") != -1) {
            //Check for illegal characters in a windows path name
	    dialogMsg = resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryIllegalCharsMessage)");
	    msgDialog = new MessageDialog(parent, dialogMsg, dialogTitle, okString);
	    msgDialog.setVisible(true);
	    return false;
	}
        
        File dirFile = new File(dir);
        if (dirFile.exists()) {
	    // This File object should be a directory
            if (!dirFile.isDirectory()) {
                dialogMsg = msgPrefix + " "
		+ resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryInvalidMessage)");
                msgDialog = new MessageDialog(parent, dialogMsg, dialogTitle, okString);
                msgDialog.setVisible(true);
                return false;
            }
	    // This File object must have write permissions
            if (!(dirFile.canWrite())) {
                dialogMsg = msgPrefix + " "
		+ resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryNoWritePermissionMessage)");
                msgDialog = new MessageDialog(parent, dialogMsg, dialogTitle, okString);
                msgDialog.setVisible(true);
                return false;
            }
	    
	    // for some reason, the previous check was not enough to detect permissions on Windows
	    // thus, attempt to create a temporary file and delete it and return false if it fails
	    // if (os.startsWith("Windows")) {
	    if (Util.isWindowsOS()) {
		File testFile = new File(dirFile, "test.tmp");
		if (testFile.exists()) {
		    try {
			boolean result = testFile.delete();
			if (result == false) {
			    throw new IOException();
			}
		    } catch (Exception e) {
			dialogMsg = msgPrefix + " "
		        + resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryNoWritePermissionMessage)");
			msgDialog = new MessageDialog(parent, dialogMsg, dialogTitle, okString);
			msgDialog.setVisible(true);
			return false;
		    }
		}else {
		    try {
			testFile.createNewFile();
			boolean result = testFile.delete();
			if (result == false) {
			    throw new IOException();
			}		    
		    } catch (Exception e) {
			dialogMsg = msgPrefix + " "
		        + resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryNoWritePermissionMessage)");
			msgDialog = new MessageDialog(parent, dialogMsg, dialogTitle, okString);
			msgDialog.setVisible(true);
			return false;
		    }
		}	 
	    }
	    // We have write permissions, now see if the directory is empty
	    boolean isEmpty = checkNonEmptyDir(dir);
	    if (installDirType == NB_INSTALL_DIR) {
		emptyExistingDirNB = isEmpty;
	    }
	    else {
		emptyExistingDirJ2SE = isEmpty;
	    }
            if(!isEmpty) {
                // Another version installed. prompt to uninstall
                dialogMsg = msgPrefix + " "
		+ resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directorySpecifyEmptyMessage)")
                + "\n\n" + installedVersion;
		msgDialog = new MessageDialog(parent, dialogMsg, dialogTitle, okString);
                msgDialog.setVisible(true);
                return false;
            }
        }
        return true;
    }

    /** Create directory. Do not ask user. Report only error when it fails. */
    private boolean createDirectory(String path, String msgPrefix) {
	String dialogTitle = resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryDialogTitle)");
        String[] okString  = {resolveString("$L(com.installshield.wizard.i18n.WizardResources, ok)")};
        String btnYes = resolveString("$L(com.installshield.wizard.i18n.WizardResources, yes)");
        String btnNo  = resolveString("$L(com.installshield.wizard.i18n.WizardResources, no)");
	Object[] objs = new Object[] { btnYes, btnNo };
        Frame parent  = ((AWTWizardUI)getWizard().getUI()).getFrame();
	File dirFile  = new File(path);
        MessageDialog msgDialog = null;
	String dialogMsg = msgPrefix + " "
	+ resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryCreateMessage)");
	try {
	    //Object obj = getWizard().getServices().getUserInput(dialogTitle,dialogMsg, objs, btnYes);
	    //if (obj == btnNo) return false;
	    logEvent(this, Log.DBG, "CheckInstallDir() dirFile:" + dirFile.getAbsolutePath());
	    if(!dirFile.mkdirs()) {
		dialogMsg = msgPrefix + " "
		+ resolveString("$L(org.netbeans.installer.Bundle,InstallLocationPanel.directoryCouldNotCreateMessage)");
		msgDialog = new MessageDialog(parent, dialogMsg, dialogTitle, okString);
		msgDialog.setVisible(true);
		return false;
	    }
	} catch (Exception e) {
	    logEvent(this, Log.ERROR, e);
	}
	return true;
    }
    
    public boolean backupDirectory(java.io.File dir){
        File backupDir = new File(dir, backupDirName);
        Util.deleteDirectory(backupDir);
        backupDir.mkdirs();
        java.io.File[] list = dir.listFiles();
        for (int i=0; i < list.length ; i++) {
            if(!list[i].getName().equals(backupDir.getName())) {
                File newFile = new File(backupDir,list[i].getName());
                list[i].renameTo(newFile);
            }
        }
        list=dir.listFiles();
        if (list.length > 1) return false;
        if(!list[0].getName().equals(backupDir.getName()))
            return false;
        else
            return true;
    }

    private boolean checkNonEmptyDir(String instDir) {
        boolean empty = true;

	File f = new java.io.File(instDir);
	if (f.exists()) {
	    String[] list = f.list();
	    if (list == null) {
		// this is a file not a directory, for now just flag as false
		empty = false;
	    } else if (list.length > 0) {
		empty = false;
	    }
	}
        logEvent(this, Log.DBG, "empty install directory = " + empty);
        return empty;
    }
    
    public void actionPerformed(ActionEvent event) {
        Object obj = event.getSource();
        if(obj instanceof JButton) {
	    String str = event.getActionCommand();
	    JTextField tf = null;
	    if (str.equals("nb")) {
		str = nbInstallDirTF.getText();
		tf = nbInstallDirTF;
	    } else {
		str =j2seInstallDirTF.getText();
		tf = j2seInstallDirTF;
	    }
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setSelectedFile(new File(str));
            chooser.setBackground(((JButton)obj).getBackground());
            chooser.setAcceptAllFileFilterUsed(false);
            int returnVal = chooser.showOpenDialog(new JFrame());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                tf.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        }
    }

    private char getWinSystemDrive() {
	char sysDrive = 'C';
        try {
            String sysLib=resolveString("$D(lib)"); // Resolve system library directory 
            logEvent(this, Log.DBG, "System Library directory is "+sysLib); 
            sysDrive=sysLib.charAt(0); // Resolve system drive letter
            logEvent(this, Log.DBG, " Found system drive is: " + String.valueOf(sysDrive));
        } catch(Exception ex) {
            Util.logStackTrace(this,ex);
            return 'C';
        }        
	return sysDrive;
    }
}
