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

package org.netbeans.installer;

import com.installshield.product.service.product.ProductService;
import com.installshield.util.Log;
import com.installshield.util.MnemonicString;
import com.installshield.wizard.OptionsTemplateEntry;
import com.installshield.wizard.WizardBean;
import com.installshield.wizard.WizardBeanEvent;
import com.installshield.wizard.WizardBuilderSupport;
import com.installshield.wizard.console.ConsoleWizardPanelImpl;
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizard.service.WizardServicesUI;
import com.installshield.wizard.swing.JFlowLabel;
import com.installshield.wizard.swing.SwingWizardUI;
import com.installshield.wizardx.panels.ExtendedWizardPanel;

import java.awt.BorderLayout;
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
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class InstallDirSelectionPanel extends ExtendedWizardPanel implements ActionListener{
    
    private JTextField nbInstallDirTF;
    private JButton    nbBrowseButton;
    private JLabel     nbInputLabel;
    //private JTextArea  nbListLabel;

    private JTextField jdkInstallDirTF;
    private JButton    jdkBrowseButton;
    private JLabel     jdkInputLabel;
    //private JTextArea  j2seListLabel;

    private JPanel mainPanel;
    private JPanel displayPanel;
    private JPanel inputPanel;
    private JFlowLabel infoLabel;
    
    private String installedJdk;
    private String installedVersion = "";
    private String backupDirName    = "";
    private String nbLabel          = null;
    private String jdkLabel        = null;
    private String j2seDir          = null;

    private static int NB_INSTALL_DIR   = 1;
    private static int J2SE_INSTALL_DIR = 2;

    private boolean emptyExistingDirNB   = false;
    private boolean emptyExistingDirJ2SE = false;
    
    private static final String BUNDLE = "$L(org.netbeans.installer.Bundle,";
    
    private String nbDestination = "";
    
    private String jdkDestination = "";
    
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
            
            if (!Util.isWindowsOS()) {
                File root = new File("/");
                if (!root.canWrite()) {
                    service.setProductBeanProperty(
                    ProductService.DEFAULT_PRODUCT_SOURCE,
                    null,
                    "installLocation",
                    resolveString(BUNDLE + "Product.installLocationForNonRoot)"));
                }
            } else {
                service.setProductBeanProperty(
                ProductService.DEFAULT_PRODUCT_SOURCE,
                null,
                "installLocation",
                resolveString(BUNDLE + "Product.installLocationWindows)"));
            }
        } catch (ServiceException e) {
            logEvent(this, Log.ERROR, e);
        }
        
        return super.queryEnter(event);
    }
    
    public String getNbDestination () {
        return nbDestination;
    }
    
    public void setNbDestination (String nbDestination) {
        this.nbDestination = nbDestination;
    }
    
    public String getJdkDestination () {
        return jdkDestination;
    }
    
    public void setJdkDestination (String jdkDestination) {
        this.jdkDestination = jdkDestination;
    }
    
    /** We do not localize following text. */
    public OptionsTemplateEntry[] getOptionsTemplateEntries (int i) {
        String s = "InstallDirSelectionPanel";
        String s1 = "Destination directory for NetBeans IDE.";
        String s2 = "-W " + getBeanId() + ".nbDestination=";
        if (i == WizardBean.TEMPLATE_VALUE) {
            s2 = s2 + getOptionsFileTemplateValueStr();
        } else {
            s2 = s2 + getNbDestination();
        }
        OptionsTemplateEntry op1 = new OptionsTemplateEntry(s, s1, s2);
        
        s = "InstallDirSelectionPanel";
        s1 = "Destination directory for JDK.";
        s2 = "-W " + getBeanId() + ".jdkDestination=";
        if (i == WizardBean.TEMPLATE_VALUE) {
            s2 = s2 + getOptionsFileTemplateValueStr();
        } else {
            s2 = s2 + getJdkDestination();
        }
        OptionsTemplateEntry op2 = new OptionsTemplateEntry(s, s1, s2);
        
        return (new OptionsTemplateEntry[] {op1, op2});
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
        
        infoLabel = new JFlowLabel();	
        
        String desc;
        if (Util.isJDKAlreadyInstalled()) {
            if (Util.isJREAlreadyInstalled()) {
                desc = resolveString(BUNDLE + "InstallLocationPanel.dirSelectionDescErr,"
                + BUNDLE + "JDK.shortName),"
                + installedJdk + ","
                + BUNDLE + "Product.displayName))");
            } else {
                //Warning message when JDK is installed but public JRE is NOT installed
                desc = resolveString(BUNDLE + "InstallLocationPanel.dirSelectionDescErrJRE,"
                + BUNDLE + "JDK.shortName),"
                + installedJdk + ","
                + BUNDLE + "Product.displayName),"
                + BUNDLE + "JRE.shortName))");
            }
        } else {
            desc = resolveString(BUNDLE + "InstallLocationPanel.description,"
            + BUNDLE + "JDK.shortName),"
            + BUNDLE + "Product.displayName))");
        }
        
        infoLabel.setText(desc);
        mainPanel.add(infoLabel, BorderLayout.NORTH);
        
        inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        
	//----------------------------------------------------------------------
	// netbeans install dir components
	String nbInstallDir = resolveString("$P(absoluteInstallLocation)");
	nbLabel = resolveString(BUNDLE + "InstallLocationPanel.nbInstallDirectoryLabel)");
        MnemonicString nbInputLabelMn = new MnemonicString(nbLabel);
        nbLabel = nbInputLabelMn.toString();
        nbInputLabel = new JLabel(nbLabel);
        if (nbInputLabelMn.isMnemonicSpecified()) {
            nbInputLabel.setDisplayedMnemonic(nbInputLabelMn.getMnemonicChar());
        }
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
        nbInputLabel.setLabelFor(nbInstallDirTF);
        inputPanel.add(nbInstallDirTF, gridBagConstraints);

        String nbBrowseButtonText = resolveString(BUNDLE + "InstallLocationPanel.nbBrowseButtonLabel)");
        MnemonicString nbBrowseMn = new MnemonicString(nbBrowseButtonText);
        nbBrowseButtonText = nbBrowseMn.toString();
        nbBrowseButton = new JButton(nbBrowseButtonText);
        if (nbBrowseMn.isMnemonicSpecified()) {
            nbBrowseButton.setMnemonic(nbBrowseMn.getMnemonicChar());
        }
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
        + resolveString(BUNDLE + "JDK.defaultInstallDirectory)");
	/*j2seDir = resolveString(BUNDLE + "JDK.defaultInstallDirectory)");
	String j2seInstallDir = null;
	if (Util.isWindowsOS()) {
	    j2seInstallDir = sysDrive + j2seDir;
	} else {
	    j2seInstallDir = tempPath + File.separator + j2seDir;
	}*/
	jdkLabel = resolveString(BUNDLE + "InstallLocationPanel.jdkInstallDirectoryLabel)");
        MnemonicString jdkInputLabelMn = new MnemonicString(jdkLabel);
        jdkLabel = jdkInputLabelMn.toString();

	if (!Util.isJDKAlreadyInstalled()) {
            jdkInputLabel = new JLabel(jdkLabel);
            if (jdkInputLabelMn.isMnemonicSpecified()) {
                jdkInputLabel.setDisplayedMnemonic(jdkInputLabelMn.getMnemonicChar());
            }
	    jdkInputLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
	    gridBagConstraints = new GridBagConstraints();
	    gridBagConstraints.insets = new Insets(0, 25, 3, 0);
	    gridBagConstraints.gridx = 0;
	    gridBagConstraints.gridy = 3;
	    gridBagConstraints.weightx = 0.5;
	    gridBagConstraints.gridheight = 1;
	    gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
	    gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
	    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
	    inputPanel.add(jdkInputLabel, gridBagConstraints);
	    
	    jdkInstallDirTF = new JTextField(j2seInstallDir);
	    gridBagConstraints = new GridBagConstraints();
	    gridBagConstraints.insets = new Insets(0, 25, 3, 0);
	    gridBagConstraints.gridx = 0;
	    gridBagConstraints.gridy = 4;
	    gridBagConstraints.weightx = 0.5;
	    gridBagConstraints.gridheight = 1;
	    gridBagConstraints.gridwidth = 1;
	    gridBagConstraints.anchor = GridBagConstraints.WEST;
	    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            jdkInputLabel.setLabelFor(jdkInstallDirTF);
	    inputPanel.add(jdkInstallDirTF, gridBagConstraints);
	    
            String jdkBrowseButtonText = resolveString(BUNDLE + "InstallLocationPanel.jdkBrowseButtonLabel)");
            MnemonicString jdkBrowseMn = new MnemonicString(jdkBrowseButtonText);
            jdkBrowseButtonText = jdkBrowseMn.toString();
            jdkBrowseButton = new JButton(jdkBrowseButtonText);
            if (jdkBrowseMn.isMnemonicSpecified()) {
                jdkBrowseButton.setMnemonic(jdkBrowseMn.getMnemonicChar());
            }
	    jdkBrowseButton.setActionCommand("j2se"); 
	    jdkBrowseButton.addActionListener(this);
	    gridBagConstraints = new GridBagConstraints();
	    gridBagConstraints.insets = new Insets(0, 12, 3, 0);
	    gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
	    gridBagConstraints.gridy = 4;
	    gridBagConstraints.gridheight = 1;
	    gridBagConstraints.gridwidth = 1;
	    gridBagConstraints.anchor = GridBagConstraints.WEST;
	    inputPanel.add(jdkBrowseButton, gridBagConstraints);

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

	    //jdkBrowseButton.setBackground(getContentPane().getBackground());
	    //jdkInputLabel.setBackground(getContentPane().getBackground());
	    //j2seListLabel.setBackground(getContentPane().getBackground());
	}

	//----------------------------------------------------------------------

        mainPanel.add(inputPanel, BorderLayout.CENTER);

	displayPanel.add(mainPanel, BorderLayout.CENTER);

        //displayPanel.setBackground(getContentPane().getBackground());
        //mainPanel.setBackground(getContentPane().getBackground());
        //inputPanel.setBackground(getContentPane().getBackground());
        //infoLabel.setBackground(mainPanel.getBackground());
        //nbBrowseButton.setBackground(getContentPane().getBackground());
        //nbInputLabel.setBackground(getContentPane().getBackground());
        //nbListLabel.setBackground(getContentPane().getBackground());
	
        getContentPane().add(displayPanel, BorderLayout.CENTER);
    }
    
    public boolean queryExit(WizardBeanEvent event) {
	String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;
        
        String nbInstallDir = nbInstallDirTF.getText().trim();
        File instDirFile = new File(nbInstallDir);
	//Only get the canonical path if the install dir is not an empty string.
	//getCanonicalPath returns the directory the installer is run from if the
	//install dir is an empty string.
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
	
 	// Check the j2se directory
        String j2seMsgStart = null;
        index = jdkLabel.lastIndexOf(':');
        if (index > 0) {
            j2seMsgStart = jdkLabel.substring(0, index);
        } else {
            j2seMsgStart = jdkLabel;
        }
        String j2seInstallDir = null;
        if (Util.isJDKAlreadyInstalled() && Util.isWindowsOS()) {
            //Code moved to execute() and exited()
        } else {
            // There is no jdk already installed so check the j2se directory.
            j2seInstallDir = jdkInstallDirTF.getText().trim();
            instDirFile = new File(j2seInstallDir);
            //Only get the canonical path if the install dir is not an empty string.
            //getCanonicalPath returns the directory the installer is run from if the
            //install dir is an empty string.
            if (j2seInstallDir.length() > 0) {
                try { // cleanup any misc chars in path such as "."
                    j2seInstallDir = instDirFile.getCanonicalPath();
                    instDirFile = new File(j2seInstallDir);
                } catch (IOException ioerr) {
                    System.out.println("IOException: Could not get canonical path: " + ioerr);
                    j2seInstallDir = instDirFile.getAbsolutePath();
                }
            }
            jdkInstallDirTF.setText(j2seInstallDir);
            
            // If there is a problem with the specified directory, then return false
            if (!checkInstallDir(j2seInstallDir, J2SE_INSTALL_DIR, j2seMsgStart)) {
                return false;
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
        
        if (Util.isJDKAlreadyInstalled() && Util.isWindowsOS()) {
            //Code moved to execute() and exited()
        } else {
            // Last thing to do is create the J2SE directory unless the
            // directory exists and is empty.
            if (!emptyExistingDirJ2SE) {
                if (!createDirectory(j2seInstallDir, j2seMsgStart)) {
                    return false;
                }
            }
        }
        
        nbDestination = nbInstallDir;
        jdkDestination = j2seInstallDir;
        
	return true;
    }
    
    /**
     * Check if J2SE and NB installation dirs are the same or not.
     * Do not allow installation NB into JDK folder and JDK into NB folder.
     */
    private boolean checkBothInstallDirs(String nbDir, String j2seDir) {
	String dialogTitle = resolveString(BUNDLE + "InstallLocationPanel.directoryDialogTitle)");
        String dialogMsg = "";
        
        //Check if one dir is not inside another dir
        //First check if JDK install dir is not inside of NB install dir
        String nbAbsolutePath = new File(nbDir).getAbsolutePath();
        String j2seAbsolutePath = new File(j2seDir).getAbsolutePath();
        logEvent(this,Log.DBG,"nbAbsolutePath: " + nbAbsolutePath);
        logEvent(this,Log.DBG,"j2seAbsolutePath: " + j2seAbsolutePath);
        if (nbAbsolutePath.equals(j2seAbsolutePath)) {
            dialogMsg = resolveString(BUNDLE + "InstallLocationPanel.directoryNBJDKTheSame)");
            showErrorMsg(dialogTitle,dialogMsg);
            return false;
        } else {
            String nbParent = new File(nbAbsolutePath).getParent();
            String j2seParent = new File(j2seAbsolutePath).getParent();
            if (nbParent.equals(j2seParent)) {
                //nbDir and asDir are not the same (checked above) but they have
                //the same parent. It is ok.
                return true;
            }
            if (j2seAbsolutePath.startsWith(nbAbsolutePath)) {
                //JDK is inside NB
                dialogMsg = resolveString(BUNDLE + "InstallLocationPanel.JDKDirCannotBeInNBDir)");
                showErrorMsg(dialogTitle,dialogMsg);
                return false;
            }
            if (nbAbsolutePath.startsWith(j2seAbsolutePath)) {
                //NB is inside JDK
                dialogMsg = resolveString(BUNDLE + "InstallLocationPanel.NBDirCannotBeInJDKDir)");
                showErrorMsg(dialogTitle,dialogMsg);
                return false;
            }
            return true;
        }
    }
    
    /** Called in GUI mode. */
    public void exited (WizardBeanEvent event) {
        logEvent(this, Log.DBG, "exited ENTER");
        super.exited(event);
	try {
            String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;
	    ProductService service = (ProductService)getService(ProductService.NAME);
	    service.setRetainedProductBeanProperty(productURL,
            Names.CORE_IDE_ID, "installLocation", nbDestination);
            service.setRetainedProductBeanProperty(productURL, null, "installLocation", nbDestination);
            service.setRetainedProductBeanProperty(productURL, null, "absoluteInstallLocation", nbDestination);
            //JDK part
            if (Util.isJDKAlreadyInstalled() && Util.isWindowsOS()) {
                //On Windows given JDK version can be installed only once so do not
                //install it.
                service.setRetainedProductBeanProperty(productURL,
                Names.J2SE_ID, "active", Boolean.FALSE);
            } else {
                if (Util.isWindowsOS()) {
                    service.setRetainedProductBeanProperty(productURL,
                    Names.J2SE_ID, "installLocation", nbDestination);
                } else {
                    service.setRetainedProductBeanProperty(productURL,
                    Names.J2SE_ID, "installLocation", jdkDestination);
                }
            }
	} catch (ServiceException e) {
	    logEvent(this, Log.ERROR, e);
        }
	logEvent(this, Log.DBG, "User specified nbInstallDir: " + nbDestination);
        Util.setJdkHome(jdkDestination);
        Util.setJ2SEInstallDir(jdkDestination);
        logEvent(this, Log.DBG, "User specified j2seInstallDir: " + jdkDestination);
    }
    
    /** Called in silent mode. */
    public void execute (WizardBeanEvent event) {
        logEvent(this, Log.DBG, "execute ENTER");
        super.execute(event);
	try {
            String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;
	    ProductService service = (ProductService)getService(ProductService.NAME);
	    service.setRetainedProductBeanProperty(productURL,
            Names.CORE_IDE_ID, "installLocation", nbDestination);
            service.setRetainedProductBeanProperty(productURL, null, "installLocation", nbDestination);
            service.setRetainedProductBeanProperty(productURL, null, "absoluteInstallLocation", nbDestination);
            //JDK part
            if (Util.isJDKAlreadyInstalled() && Util.isWindowsOS()) {
                //On Windows given JDK version can be installed only once so do not
                //install it.
                service.setRetainedProductBeanProperty(productURL,
                Names.J2SE_ID, "active", Boolean.FALSE);
            } else {
                if (Util.isWindowsOS()) {
                    service.setRetainedProductBeanProperty(productURL,
                    Names.J2SE_ID, "installLocation", nbDestination);
                } else {
                    service.setRetainedProductBeanProperty(productURL,
                    Names.J2SE_ID, "installLocation", jdkDestination);
                }
            }
	} catch (ServiceException e) {
	    logEvent(this, Log.ERROR, e);
        }
        Util.setJdkHome(jdkDestination);
        Util.setJ2SEInstallDir(jdkDestination);
        logEvent(this, Log.DBG, "User specified j2seInstallDir: " + jdkDestination);
    }
    
    /* Check the installation directory to see if it has illegal chars,
     * or if it already exits or if it's empty then create it.
     */ 
    private boolean checkInstallDir(String dir, int installDirType, String msgPrefix) {
	String dialogTitle = resolveString(BUNDLE + "InstallLocationPanel.directoryDialogTitle)");
        StringTokenizer st = new StringTokenizer(dir);
        String dialogMsg = "";

        if (dir.length() == 0) {
            //Directory must be specified
            dialogMsg = msgPrefix + " "
            + resolveString(BUNDLE + "InstallLocationPanel.directoryNotSpecifiedMessage)");
            showErrorMsg(dialogTitle,dialogMsg);
            return false;
        } else if (st.countTokens() > 1) {
            //Check for spaces in path - it is not supported by JDK installer on Linux/Solaris
	    if (!Util.isWindowsOS() && installDirType == J2SE_INSTALL_DIR) {
		dialogMsg = msgPrefix + " "
		+ resolveString(BUNDLE + "InstallLocationPanel.directoryHasSpaceMessage)");
                showErrorMsg(dialogTitle,dialogMsg);
		return false;
	    }
	} else if ((Util.isWindowsOS() && (dir.indexOf("@") != -1 || dir.indexOf("&") != -1 ||
					 dir.indexOf("%") != -1 || dir.indexOf("#") != -1)) ||
	    dir.indexOf("+") != -1 || dir.indexOf("\"") != -1) {
            //Check for illegal characters in a windows path name
	    dialogMsg = resolveString(BUNDLE + "InstallLocationPanel.directoryIllegalCharsMessage)");
            showErrorMsg(dialogTitle,dialogMsg);
	    return false;
	}
        
        File dirFile = new File(dir);
        if (dirFile.exists()) {
	    // This File object should be a directory
            if (!dirFile.isDirectory()) {
                dialogMsg = msgPrefix + " "
		+ resolveString(BUNDLE + "InstallLocationPanel.directoryInvalidMessage)");
                showErrorMsg(dialogTitle,dialogMsg);
                return false;
            }
	    // This File object must have write permissions
            if (!(dirFile.canWrite())) {
                dialogMsg = msgPrefix + " "
		+ resolveString(BUNDLE + "InstallLocationPanel.directoryNoWritePermissionMessage)");
                showErrorMsg(dialogTitle,dialogMsg);
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
		        + resolveString(BUNDLE + "InstallLocationPanel.directoryNoWritePermissionMessage)");
                        showErrorMsg(dialogTitle,dialogMsg);
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
		        + resolveString(BUNDLE + "InstallLocationPanel.directoryNoWritePermissionMessage)");
                        showErrorMsg(dialogTitle,dialogMsg);
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
		+ resolveString(BUNDLE + "InstallLocationPanel.directorySpecifyEmptyMessage)")
                + "\n\n" + installedVersion;
                showErrorMsg(dialogTitle,dialogMsg);
                return false;
            }
        }
        return true;
    }

    /** Create directory. Do not ask user. Report only error when it fails. */
    private boolean createDirectory(String path, String msgPrefix) {
	String dialogTitle = resolveString(BUNDLE + "InstallLocationPanel.directoryDialogTitle)");
	String dialogMsg;
	File dirFile  = new File(path);
	try {
	    logEvent(this, Log.DBG, "CheckInstallDir() dirFile:" + dirFile.getAbsolutePath());
	    if(!dirFile.mkdirs()) {
		dialogMsg = msgPrefix + " "
		+ resolveString(BUNDLE + "InstallLocationPanel.directoryCouldNotCreateMessage)");
                showErrorMsg(dialogTitle,dialogMsg);
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
            SwingWizardUI wizardUI = (SwingWizardUI) getWizard().getUI();
            if (wizardUI != null) {
                wizardUI.restoreDefaultColors();
            }
	    String str = event.getActionCommand();
	    JTextField tf = null;
	    if (str.equals("nb")) {
		str = nbInstallDirTF.getText();
		tf = nbInstallDirTF;
	    } else {
		str =jdkInstallDirTF.getText();
		tf = jdkInstallDirTF;
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
            if (wizardUI != null) {
                wizardUI.setWizardColors();
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
    
    protected void showErrorMsg(String title, String msg) {
        try {
            getWizard().getServices().displayUserMessage(title, msg, WizardServicesUI.ERROR);
        } catch (Exception e) {
            throw new Error();
        }
    }
}
