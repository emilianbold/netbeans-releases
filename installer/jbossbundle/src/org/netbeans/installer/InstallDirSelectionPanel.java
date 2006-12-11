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

public class InstallDirSelectionPanel extends ExtendedWizardPanel implements ActionListener {
    
    private JTextField nbInstallDirTF;
    private JButton    nbBrowseButton;
    private JLabel     nbInputLabel;
    
    private JTextField asInstallDirTF;
    private JButton    asBrowseButton;
    private JLabel     asInputLabel;
    
    private JPanel mainPanel;
    private JPanel displayPanel;
    private JPanel inputPanel;
    private JFlowLabel infoLabel;
    
    private String installedVersion = "";
    
    private String nbLabel          = null;
    private String asLabel          = null;
    
    private boolean emptyExistingDirNB = false;
    private boolean emptyExistingDirAS = false;
    
    private static final int NB_INSTALL_DIR = 1;
    private static final int AS_INSTALL_DIR = 2;
    
    /** 
     * Estimate for JBoss: As longest path inside JBoss install dir is about 110 chars long,
     * no more than 120 chars. Maximum path length on Windows is 256 chars.
     */
    private static final int AS_INSTALL_PATH_MAX_LENGTH = 130;
    
    private static final String BUNDLE = "$L(org.netbeans.installer.Bundle,";
    
    private String nbDestination = "";
    
    private String asDestination = "";
    
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
    
    public boolean entered(WizardBeanEvent event) {
        nbInstallDirTF.requestFocus();
        return true;
    }
    
    public String getNbDestination () {
        return nbDestination;
    }
    
    public void setNbDestination (String nbDestination) {
        this.nbDestination = nbDestination;
    }
    
    public String getAsDestination () {
        return asDestination;
    }
    
    public void setAsDestination (String asDestination) {
        this.asDestination = asDestination;
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
        s1 = "Destination directory for SJS AS.";
        s2 = "-W " + getBeanId() + ".asDestination=";
        if (i == WizardBean.TEMPLATE_VALUE) {
            s2 = s2 + getOptionsFileTemplateValueStr();
        } else {
            s2 = s2 + getAsDestination();
        }
        OptionsTemplateEntry op2 = new OptionsTemplateEntry(s, s1, s2);
        
        return (new OptionsTemplateEntry[] {op1, op2});
    }
    
    protected void initialize() {
        super.initialize();
	
        GridBagConstraints gridBagConstraints;
        mainPanel = new JPanel();        
        mainPanel.setLayout(new BorderLayout());
        
	displayPanel = new JPanel(new java.awt.BorderLayout(0,10));
	displayPanel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
        
        infoLabel = new JFlowLabel();	
        
        String desc = resolveString(BUNDLE + "InstallLocationPanel.description,"
        + BUNDLE + "Product.displayName),"
        + BUNDLE + "AS.name))");
        
        infoLabel.setText(desc);        
        mainPanel.add(infoLabel, BorderLayout.NORTH);
        
        inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
        
	//----------------------------------------------------------------------
	// netbeans install dir components
	String nbInstallDir;
        nbInstallDir = resolveString("$P(absoluteInstallLocation)");
	if (Util.isMacOSX()) {
            //Replace install dir
            nbInstallDir = nbInstallDir.substring(0,nbInstallDir.lastIndexOf(File.separator))
            + File.separator
            + resolveString(BUNDLE + "Product.installDirMacOSX)");
        }
        logEvent(this, Log.DBG, "nbInstallDir: " + nbInstallDir);
        
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

        String browseButtonText = resolveString(BUNDLE + "InstallLocationPanel.nbBrowseButtonLabel)");
        MnemonicString browseMn = new MnemonicString(browseButtonText);
        browseButtonText = browseMn.toString();
        nbBrowseButton = new JButton(browseButtonText);
        if (browseMn.isMnemonicSpecified()) {
            nbBrowseButton.setMnemonic(browseMn.getMnemonicChar());
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
	// SJS AS install dir components
        
	String asInstallDir;
	if (Util.isWindowsOS()) {
	    StringBuffer drive = new StringBuffer(" :\\");
	    drive.setCharAt(0, getWinSystemDrive());
	    String sysDrive = drive.toString();
	    asInstallDir = sysDrive + resolveString(BUNDLE + "AS.defaultInstallDirectory)");
        } else {
            File f = new File(nbInstallDir);
            if (f.getParent() != null) {
                asInstallDir = f.getParent() + resolveString("$J(file.separator)" + BUNDLE + "AS.defaultInstallDirectory)");
            } else {
                asInstallDir = resolveString("$D(home)$J(file.separator)" + BUNDLE + "AS.defaultInstallDirectory)");
            }
        }
        logEvent(this, Log.DBG, "asInstallDir: " + asInstallDir);
        
        asLabel = resolveString(BUNDLE + "InstallLocationPanel.asInstallDirectoryLabel)");
        MnemonicString asInputLabelMn = new MnemonicString(asLabel);
        asLabel = asInputLabelMn.toString();

        asInputLabel = new JLabel(asLabel);
        if (asInputLabelMn.isMnemonicSpecified()) {
            asInputLabel.setDisplayedMnemonic(asInputLabelMn.getMnemonicChar());
        }
        asInputLabel.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 25, 3, 0);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(asInputLabel, gridBagConstraints);

        asInstallDirTF = new JTextField(asInstallDir);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 25, 3, 0);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        asInputLabel.setLabelFor(asInstallDirTF);
        inputPanel.add(asInstallDirTF, gridBagConstraints);

        String asBrowseButtonText = resolveString(BUNDLE + "InstallLocationPanel.asBrowseButtonLabel)");
        MnemonicString asBrowseMn = new MnemonicString(asBrowseButtonText);
        asBrowseButtonText = asBrowseMn.toString();
        asBrowseButton = new JButton(asBrowseButtonText);
        if (asBrowseMn.isMnemonicSpecified()) {
            asBrowseButton.setMnemonic(asBrowseMn.getMnemonicChar());
        }
        asBrowseButton.setActionCommand("as"); 
        asBrowseButton.addActionListener(this);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 12, 3, 0);
        gridBagConstraints.gridx = GridBagConstraints.RELATIVE;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.gridwidth = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        inputPanel.add(asBrowseButton, gridBagConstraints);

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
        String nbInstallDir = nbInstallDirTF.getText().trim();
        File instDirFile = new File(nbInstallDir);
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
	
        String asInstallDir = asInstallDirTF.getText().trim();
        instDirFile = new File(asInstallDir);
	// Only get the canonical path if the install dir is not an empty string.
	// getCanonicalPath returns the directory the installer is run from if the
	// install dir is an empty string.
	if (asInstallDir.length() > 0) {
	    try { // cleanup any misc chars in path such as "."
		asInstallDir = instDirFile.getCanonicalPath();
		instDirFile = new File(asInstallDir);
	    } catch (IOException ioerr) {
		logEvent(this, Log.DBG, "IOException: Could not get canonical path: " + ioerr);
		asInstallDir = instDirFile.getAbsolutePath();
	    }
	}
        asInstallDirTF.setText(asInstallDir);
        
        
 	// Check the as directory
        String asMsgStart = null;
        index = asLabel.lastIndexOf(':');
        if (index > 0) {
            asMsgStart = asLabel.substring(0, index);
        } else {
            asMsgStart = asLabel;
        }
        if (!checkInstallDir(asInstallDir, AS_INSTALL_DIR, asMsgStart)) {
            return false;
	}
        
        if (!checkBothInstallDirs(nbInstallDir,asInstallDir)) {
            return false;
        }
        
	// Last thing to do is to create the NB directory unless the
	// directory exists and is empty.
	if (!emptyExistingDirNB) {
	    if (!createDirectory(nbInstallDir, nbMsgStart)) {
                return false;
            }
	}
        
	if (!emptyExistingDirAS) {
	    if (!createDirectory(asInstallDir, asMsgStart)) {
                return false;
            }
	}
        
        nbDestination = nbInstallDir;
        asDestination = asInstallDir;
        
	return true;
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
            
            service.setRetainedProductBeanProperty(productURL, Names.APP_SERVER_ID, "installLocation",
            nbDestination + File.separator + "_uninst" + File.separator + "jboss");
            
            service.setRetainedProductBeanProperty(productURL, null, "installLocation", nbDestination);
            service.setRetainedProductBeanProperty(productURL, null, "absoluteInstallLocation", nbDestination);
	} catch (ServiceException e) {
	    logEvent(this, Log.ERROR, e);
	}
        Util.setASInstallDir(asDestination);
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
            
            service.setRetainedProductBeanProperty(productURL, Names.APP_SERVER_ID, "installLocation",
            nbDestination + File.separator + "_uninst" + File.separator + "jboss");
            
            service.setRetainedProductBeanProperty(productURL, null, "installLocation", nbDestination);
            service.setRetainedProductBeanProperty(productURL, null, "absoluteInstallLocation", nbDestination);
	} catch (ServiceException e) {
	    logEvent(this, Log.ERROR, e);
	}
        Util.setASInstallDir(asDestination);
    }
    
    /* Check the installation directory to see if it has illegal chars,
     * or if it already exits or if it's empty then create it.
     */ 
    private boolean checkInstallDir (String dir, int installDirType, String msgPrefix) {
        String[] okString  = {resolveString("$L(com.installshield.wizard.i18n.WizardResources, ok)")};
	String dialogTitle = resolveString(BUNDLE + "InstallLocationPanel.directoryDialogTitle)");
        StringTokenizer st = new StringTokenizer(dir);
        String dialogMsg = "";
        
        if (dir.length() == 0) {
            //Directory must be specified
            dialogMsg = msgPrefix + " "
            + resolveString(BUNDLE + "InstallLocationPanel.directoryNotSpecifiedMessage)");
            showErrorMsg(dialogTitle,dialogMsg);
            return false;
	} else if (Util.isWindowsOS() && (installDirType == AS_INSTALL_DIR) && (dir.length() > AS_INSTALL_PATH_MAX_LENGTH)) {
            //Path is too long
            dialogMsg = msgPrefix + " "
            + resolveString(BUNDLE + "InstallLocationPanel.directoryTooLong," + AS_INSTALL_PATH_MAX_LENGTH 
	    + "," + dir.length() + ")");
            showErrorMsg(dialogTitle,dialogMsg);
            return false;
        } else if (st.countTokens() > 1) {
            //Check for spaces in path - it is not supported by AS installer on Linux/Solaris
	    //if (!Util.isWindowsOS() && (installDirType == AS_INSTALL_DIR)) {
            //JBoss AS installer does not support space in install path even on Windows now.
            if (installDirType == AS_INSTALL_DIR) {
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
		} else {
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
	    } else {
		emptyExistingDirAS = isEmpty;
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
    
    /** 
     * Check if AS and NB installation dirs are the same or not.
     * Do not allow installation NB into AS folder and AS into NB folder.
     */
    private boolean checkBothInstallDirs(String nbDir, String asDir) {
	String dialogTitle = resolveString(BUNDLE + "InstallLocationPanel.directoryDialogTitle)");
        String dialogMsg = "";
        
        //Check if one dir is not inside another dir
        //First check if AS install dir is not inside of NB install dir
        String nbAbsolutePath = new File(nbDir).getAbsolutePath();
        String asAbsolutePath = new File(asDir).getAbsolutePath();
        logEvent(this,Log.DBG,"nbAbsolutePath: " + nbAbsolutePath);
        logEvent(this,Log.DBG,"asAbsolutePath: " + asAbsolutePath);
        if (nbAbsolutePath.equals(asAbsolutePath)) {
            dialogMsg = resolveString(BUNDLE + "InstallLocationPanel.directoryNBASTheSame)");
            showErrorMsg(dialogTitle,dialogMsg);
            return false;
        } else {
            String nbParent = new File(nbAbsolutePath).getParent();
            String asParent = new File(asAbsolutePath).getParent();
            if (nbParent.equals(asParent)) {
                //nbDir and asDir are not the same (checked above) but they have
                //the same parent. It is ok.
                return true;
            }
            if (asAbsolutePath.startsWith(nbAbsolutePath)) {
                //AS is inside NB
                dialogMsg = resolveString(BUNDLE + "InstallLocationPanel.ASDirCannotBeInNBDir)");
                showErrorMsg(dialogTitle,dialogMsg);
                return false;
            }
            if (nbAbsolutePath.startsWith(asAbsolutePath)) {
                //NB is inside AS
                dialogMsg = resolveString(BUNDLE + "InstallLocationPanel.NBDirCannotBeInASDir)");
                showErrorMsg(dialogTitle,dialogMsg);
                return false;
            }
            return true;
        }
    }
    
    /** Create directory. Do not ask user. Report only error when it fails. */
    private boolean createDirectory(String path, String msgPrefix) {
	String dialogTitle = resolveString(BUNDLE + "InstallLocationPanel.directoryDialogTitle)");
	File dirFile  = new File(path);
	String dialogMsg;
	try {
	    logEvent(this, Log.DBG, "CheckInstallDir() dirFile:" + dirFile.getAbsolutePath());
	    if (!dirFile.mkdirs()) {
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
        if (obj instanceof JButton) {
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
		str = asInstallDirTF.getText();
		tf = asInstallDirTF;
	    }
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setSelectedFile(new File(str));
            chooser.setAcceptAllFileFilterUsed(false);
            int returnVal = chooser.showOpenDialog(new JFrame());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
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
            String sysLib = resolveString("$D(lib)"); // Resolve system library directory 
            logEvent(this, Log.DBG, "System Library directory is "+sysLib); 
            sysDrive = sysLib.charAt(0); // Resolve system drive letter
            logEvent(this, Log.DBG, "Found system drive is: " + String.valueOf(sysDrive));
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
