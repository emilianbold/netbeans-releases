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

package org.netbeans.modules.mobility.cldcplatform.customwizard;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.Document;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.modules.mobility.cldcplatform.DetectPanel;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.*;

/**
 *
 * @author  dave
 */
public class CommandLinesPanel extends javax.swing.JPanel implements WizardPanel.ComponentDescriptor {
    
    protected WizardPanel wizardPanel;
    private String fileChooserValue;
    private static final Set<Character> INVALID_CHARACTERS = new HashSet();
    {
        for (char c : "\\^$?*+-!.;:,=<>|/\"'[]{}()".toCharArray()) INVALID_CHARACTERS.add(c); //NOI18N
    }
    /** Creates new form CommandLinesPanel
     * @param detectWizardPanel*/
    public CommandLinesPanel() {
        initComponents();
        infoPanel.setEditorKit(new HTMLEditorKit());
        
        int i = 1;
        String platformNameString =  NbBundle.getMessage(CommandLinesPanel.class, "Preset_CmdLinesPanel_Custom_Platform");//NOI18N
        for (;;) {
            JavaPlatform[] foundPlatforms = JavaPlatformManager.getDefault().getPlatforms(platformNameString + i, null);
            if (foundPlatforms == null  ||  foundPlatforms.length <= 0)
                break;
            i ++;
        }
        platformName.setText(platformNameString + i);
        deviceName.setText(NbBundle.getMessage(CommandLinesPanel.class, "Preset_CmdLinesPanel_Custom_Device"));//NOI18N
        
        DocumentListener documentListener = new DocumentListener() {
            public void changedUpdate(@SuppressWarnings("unused")
			final DocumentEvent e) {
                wizardPanel.fireChanged();
            }
            
            public void insertUpdate(@SuppressWarnings("unused")
			final DocumentEvent e) {
                wizardPanel.fireChanged();
            }
            
            public void removeUpdate(@SuppressWarnings("unused")
			final DocumentEvent e) {
                wizardPanel.fireChanged();
            }
        };
        platformHome.getDocument().addDocumentListener(documentListener);
        platformName.getDocument().addDocumentListener(documentListener);
        deviceName.getDocument().addDocumentListener(documentListener);
        
        FocusListener focusListener = new FocusListener() {
            @SuppressWarnings("synthetic-access")
			public void focusGained(final FocusEvent e) {
                Component component = e.getComponent();
                URL descURL = null;
                try {
                    if (component == preverifyCommand)
                        descURL = new URL("nbresloc:/org/netbeans/modules/mobility/cldcplatform/customwizard/preverifyinfo.html");
                    else if (component == executionCommand)
                        descURL = new URL("nbresloc:/org/netbeans/modules/mobility/cldcplatform/customwizard/executioninfo.html");
                    else if (component == debuggerCommand)
                        descURL = new URL("nbresloc:/org/netbeans/modules/mobility/cldcplatform/customwizard/debuggerinfo.html");
                } catch (MalformedURLException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
                if (descURL != null) {
                    try {
                        //this.description.setPage (descURL);
                        // Set page does not work well if there are mutiple calls to that
                        // see issue #49067. This is a hotfix for the bug which causes
                        // synchronous loading of the content. It should be improved later
                        // by doing it in request processor.
                        
                        //this.description.read( descURL.openStream(), descURL );
                        // #52801: handlig changed charset
                        String charset = findEncodingFromURL(descURL.openStream());
                        ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, "Url " + descURL + " has charset " + charset); // NOI18N
                        if (charset != null) {
                            infoPanel.putClientProperty("charset", charset); // NOI18N
                        }
                        infoPanel.read( descURL.openStream(), descURL );
                    } catch (ChangedCharSetException x) {
                        Document doc = infoPanel.getEditorKit().createDefaultDocument();
                        doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE); // NOI18N
                        try {
                            infoPanel.read(descURL.openStream(), doc);
                        } catch (IOException ioe) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                            infoPanel.setText(NbBundle.getBundle(CommandLinesPanel.class).getString("TXT_NoDescription")); // NOI18N
                        }
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        infoPanel.setText(NbBundle.getBundle(CommandLinesPanel.class).getString("TXT_NoDescription")); // NOI18N
                    }
                } else {
                    infoPanel.setText(NbBundle.getBundle(CommandLinesPanel.class).getString("TXT_NoDescription")); // NOI18N
                }
                infoPanel.setCaretPosition(0);
            }
            
            @SuppressWarnings("synthetic-access")
			public void focusLost(final FocusEvent e) {
                Component oppositeComponent = e.getOppositeComponent();
                if (oppositeComponent != null
                        &&  oppositeComponent != preverifyCommand
                        &&  oppositeComponent != executionCommand
                        &&  oppositeComponent != debuggerCommand
                        &&  oppositeComponent != infoPanel)
                    infoPanel.setText(""); // NOI18N
            }
        };
        preverifyCommand.addFocusListener(focusListener);
        executionCommand.addFocusListener(focusListener);
        debuggerCommand.addFocusListener(focusListener);
        
        FocusListener selectAllFocusListener = new FocusListener() {
            public void focusGained(final FocusEvent e) {
                ((JTextField) e.getComponent()).selectAll();
            }
            public void focusLost(@SuppressWarnings("unused")
			final FocusEvent e) {
            }
        };
        FocusListener zeroCaretPositionFocusListener = new FocusListener() {
            public void focusGained(final FocusEvent e) {
                ((JTextField) e.getComponent()).setCaretPosition(0);
            }
            public void focusLost(@SuppressWarnings("unused")
			final FocusEvent e) {
            }
        };
        platformHome.addFocusListener(selectAllFocusListener);
        platformName.addFocusListener(selectAllFocusListener);
        deviceName.addFocusListener(selectAllFocusListener);
        preverifyCommand.addFocusListener(zeroCaretPositionFocusListener);
        executionCommand.addFocusListener(zeroCaretPositionFocusListener);
        debuggerCommand.addFocusListener(zeroCaretPositionFocusListener);
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        platformHome = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        platformName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        deviceName = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        preverifyCommand = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        executionCommand = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        debuggerCommand = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        infoPanel = new javax.swing.JTextPane();

        setName(NbBundle.getMessage(CommandLinesPanel.class, "LBL_CmdLinesPanel_General_Information")); // NOI18N
        setPreferredSize(new java.awt.Dimension(600, 400));
        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(CommandLinesPanel.class, "LBL_CmdLinePanel_Platform_Home")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CommandLinesPanel.class, "ACSD_CmdLinePanel_Platform_Home")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CommandLinesPanel.class, "ACSD_CmdLinePanel_Platform_Home")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(platformHome, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, NbBundle.getMessage(CommandLinesPanel.class, "LBL_CmdLinesPanel_Browse")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(browseButton, gridBagConstraints);
        browseButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CommandLinesPanel.class, "ACSD_CmdLinesPanel_Browse")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CommandLinesPanel.class, "ACSD_CmdLinesPanel_Browse")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, NbBundle.getMessage(CommandLinesPanel.class, "LBL_CmdLinesPanel_Platform_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 6);
        add(jLabel2, gridBagConstraints);
        jLabel2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CommandLinesPanel.class, "ACSD_CmdLinesPanel_Platform_Name")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CommandLinesPanel.class, "ACSD_CmdLinesPanel_Platform_Name")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(platformName, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, NbBundle.getMessage(CommandLinesPanel.class, "LBL_CmdLinesPanel_Device_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 6);
        add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CommandLinesPanel.class, "ACSD_CmdLinesPanel_Device_Name")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CommandLinesPanel.class, "ACSD_CmdLinesPanel_Device_Name")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(deviceName, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, NbBundle.getMessage(CommandLinesPanel.class, "LBL_CmdLinesPanel_Preverify_Command")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 6);
        add(jLabel4, gridBagConstraints);
        jLabel4.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CommandLinesPanel.class, "ACSD_CmdLinesPanel_Preverify_Command")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CommandLinesPanel.class, "ACSD_CmdLinesPanel_Preverify_Command")); // NOI18N

        preverifyCommand.setText("\"{platformhome}{/}bin{/}preverify\" {classpath|-classpath \"{classpath}\"} -d \"{destdir}\" \"{srcdir}\"");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(preverifyCommand, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, NbBundle.getMessage(CommandLinesPanel.class, "LBL_CmdLinesPanel_Execution_Command")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 6);
        add(jLabel5, gridBagConstraints);
        jLabel5.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CommandLinesPanel.class, "ACSD_CmdLinesPanel_Execution_Command")); // NOI18N
        jLabel5.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CommandLinesPanel.class, "ACSD_CmdLinesPanel_Execution_Command")); // NOI18N

        executionCommand.setText("\"{platformhome}{/}bin{/}emulator\" {device|-Xdevice:\"{device}\"} {jadfile|-Xdescriptor:\"{jadfile}\"} {securitydomain|-Xdomain:{securitydomain}} {cmdoptions}");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(executionCommand, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, NbBundle.getMessage(CommandLinesPanel.class, "LBL_CmdLinesPanel_Debugger_Command")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 6);
        add(jLabel6, gridBagConstraints);
        jLabel6.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CommandLinesPanel.class, "ACSD_CmdLinesPanel_Debugger_Command")); // NOI18N
        jLabel6.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CommandLinesPanel.class, "ACSD_CmdLinesPanel_Debugger_Command")); // NOI18N

        debuggerCommand.setText("\"{platformhome}{/}bin{/}emulator\" {device|-Xdevice:\"{device}\"} {jadfile|-Xdescriptor:\"{jadfile}\"} {securitydomain|-Xdomain:{securitydomain}} {debug|-Xdebug -Xrunjdwp:transport={debugtransport},server={debugserver},suspend={debugsuspend},address={debugaddress}} {cmdoptions}");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(debuggerCommand, gridBagConstraints);

        infoPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        infoPanel.setEditable(false);
        jScrollPane1.setViewportView(infoPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jScrollPane1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        final String value = browseFolder(NbBundle.getMessage(CommandLinesPanel.class, "TITLE_CommandLinesPanel_BrowseHome")); // NOI18N
        if (value == null)
            return;
        platformHome.setText(value);
    }//GEN-LAST:event_browseButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JTextField debuggerCommand;
    private javax.swing.JTextField deviceName;
    private javax.swing.JTextField executionCommand;
    private javax.swing.JTextPane infoPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField platformHome;
    private javax.swing.JTextField platformName;
    private javax.swing.JTextField preverifyCommand;
    // End of variables declaration//GEN-END:variables
    
    public void setWizardPanel(final WizardPanel wizardPanel) {
        this.wizardPanel = wizardPanel;
    }
    
    public void readSettings(final WizardDescriptor wizardDescriptor) {
        final J2MEPlatform platform = (J2MEPlatform) wizardDescriptor.getProperty(DetectPanel.PLATFORM);
        if (platform == null)
            return;
        platformHome.setText(platform.getHomePath());
        platformName.setText(platform.getDisplayName());
        deviceName.setText(platform.getDevices()[0].getName());
        preverifyCommand.setText(platform.getPreverifyCmd());
        executionCommand.setText(platform.getRunCmd());
        debuggerCommand.setText(platform.getDebugCmd());
    }
    
    public void storeSettings(final WizardDescriptor wizardDescriptor) {
        final J2MEPlatform platform = new J2MEPlatform(
                J2MEPlatform.computeUniqueName(platformName.getText()),
                platformHome.getText(),
                "CUSTOM", // NOI18N
                platformName.getText(),
                null,
                null,
                preverifyCommand.getText(),
                executionCommand.getText(),
                debuggerCommand.getText(),
                new J2MEPlatform.Device[] {
            new J2MEPlatform.Device(deviceName.getText(), null, null, new J2MEPlatform.J2MEProfile[0], null)
        }
        );
        wizardDescriptor.putProperty(DetectPanel.PLATFORM, platform);
    }
    
    public JComponent getComponent() {
        return this;
    }
    
    public boolean isPanelValid() {
        String message = null;
        
        final JavaPlatform[] foundPlatforms = JavaPlatformManager.getDefault().getPlatforms(platformName.getText(), null);
        if (foundPlatforms != null  &&  foundPlatforms.length > 0)
            message = "ERR_PlatformExists"; // NOI18N
        
        if (deviceName.getText().length() <= 0)
            message = "ERR_EnterDeviceName"; // NOI18N

        if (platformName.getText().length() <= 0)
            message = "ERR_EnterPlatformName"; // NOI18N

        for (char c : platformName.getText().toCharArray())
            if (INVALID_CHARACTERS.contains(c)) 
                message = "ERR_InvalidPlatformName"; //NOI18N
                        
        for (char c : deviceName.getText().toCharArray()) 
            if (INVALID_CHARACTERS.contains(c)) 
                message = "ERR_InvalidDeviceName"; //NOI18N
                        
        
        final String platformHomeString = platformHome.getText();
        if (platformHomeString == null  ||  ! new File(platformHomeString).isDirectory())
            message = "ERR_InvalidPlatformHome"; // NOI18N
        
        wizardPanel.setErrorMessage(CommandLinesPanel.class, message);
        return message == null;
    }
    
    public boolean isFinishPanel() {
        return false;
    }
    
    
    private String browseFolder(final String title) {
        final String oldValue = fileChooserValue;
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.exists() && f.canRead() && f.isDirectory();
            }
            
            public String getDescription() {
                return NbBundle.getMessage(CommandLinesPanel.class, "TXT_FolderFilter"); // NOI18N
            }
        });
        if (oldValue != null)
            chooser.setSelectedFile(new File(oldValue));
        chooser.setDialogTitle(title);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileChooserValue = chooser.getSelectedFile().getAbsolutePath();
            return fileChooserValue;
        }
        return null;
    }
    
    // encoding support; copied from html/HtmlEditorSupport
    private static String findEncodingFromURL(final InputStream stream) {
        try {
            final byte[] arr = new byte[4096];
            final int len = stream.read(arr, 0, arr.length);
            final String txt = new String(arr, 0, (len>=0)?len:0).toUpperCase();
            // encoding
            return findEncoding(txt);
        } catch (Exception x) {
            x.printStackTrace();
        }
        return null;
    }
    
    /** Tries to guess the mime type from given input stream. Tries to find
     *   <em>&lt;meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"&gt;</em>
     * @param txt the string to search in (should be in upper case)
     * @return the encoding or null if no has been found
     */
    private static String findEncoding(final String txt) {
        int headLen = txt.indexOf("</HEAD>"); // NOI18N
        if (headLen == -1) headLen = txt.length();
        
        final int content = txt.indexOf("CONTENT-TYPE"); // NOI18N
        if (content == -1 || content > headLen) {
            return null;
        }
        
        final int charset = txt.indexOf("CHARSET=", content); // NOI18N
        if (charset == -1) {
            return null;
        }
        
        int charend = txt.indexOf('"', charset);
        final int charend2 = txt.indexOf('\'', charset);
        if (charend == -1 && charend2 == -1) {
            return null;
        }
        
        if (charend2 != -1) {
            if (charend == -1 || charend > charend2) {
                charend = charend2;
            }
        }
        
        return txt.substring(charset + "CHARSET=".length(), charend); // NOI18N
    }
}
