/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.glassfish.common.wizards;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.NbBundle;

import java.net.URL;
import org.openide.awt.HtmlBrowser.URLDisplayer;

/**
 * @author pblaha
 * @author Peter Williams
 */
public class AddServerLocationVisualPanel extends javax.swing.JPanel implements Retriever.Updater {
    
    private static final String V3_DOWNLOAD_URL = 
            "http://java.net/download/javaee5/v3/releases/preview/glassfish-snapshot-v3-preview1.zip";
//    private static final String JRUBY_DOWNLOAD_URL = 
//            "http://dist.codehaus.org/jruby/jruby-bin-1.0.0RC3.zip";
    
    private final Set <ChangeListener> listeners = new HashSet<ChangeListener>();
    private Retriever retriever;
    private volatile String statusText;

    public AddServerLocationVisualPanel() {
        initComponents();
        initUserComponents();
    }
    
    private void initUserComponents() {
        readlicenseButton.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        readlicenseButton.setHorizontalAlignment(JButton.LEADING); // optional
        readlicenseButton.setBorderPainted(false);
        readlicenseButton.setContentAreaFilled(false);
        readlicenseButton.setText(
                "<html><body><span style=\"text-decoration: underline;\">" +
                org.openide.util.NbBundle.getMessage(AddServerLocationVisualPanel.class, 
                        "LBL_ReadLicenseText") +
                "</span></body></html>");
        
        downloadButton.setEnabled(false);
        
        setName(NbBundle.getMessage(AddServerLocationVisualPanel.class, "TITLE_ServerLocation"));
        
//        hk2HomeTextField.setText(System.getProperty("user.home") + "/glassfishV3preview");
        hk2HomeTextField.setText("/space/tools/v3latest");
        hk2HomeTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                fireChangeEvent();
            }
            public void insertUpdate(DocumentEvent e) {
                fireChangeEvent();
            }
            public void removeUpdate(DocumentEvent e) {
                fireChangeEvent();
            }                    
        });
        updateCancelState(false);
        updateMessageText("");
    }
    
    /**
     * 
     * @return 
     */
    public String getHk2HomeLocation() {
        return hk2HomeTextField.getText();
    }
    
    /**
     * 
     * @return
     */
    public String getStatusText() {
        return statusText;
    }
    
    /**
     * 
     * @param l 
     */
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    /**
     * 
     * @param l 
     */
    public void removeChangeListener(ChangeListener l ) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    private void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged (ev);
        }
    }
    
    private String browseHomeLocation(){
        String hk2Location = null;
        JFileChooser chooser = getJFileChooser();
        int returnValue = chooser.showDialog(this, NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ChooseButton")); //NOI18N
        
        if(returnValue == JFileChooser.APPROVE_OPTION){
            hk2Location = chooser.getSelectedFile().getAbsolutePath();
        }
        return hk2Location;
    }
    
    private JFileChooser getJFileChooser(){
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ChooserName")); //NOI18N
        chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setApproveButtonMnemonic("Choose_Button_Mnemonic".charAt(0)); //NOI18N
        chooser.setMultiSelectionEnabled(false);
        chooser.addChoosableFileFilter(new DirFilter());
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setApproveButtonToolTipText(NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ChooserName")); //NOI18N
        chooser.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ChooserName")); //NOI18N
        chooser.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ChooserName")); //NOI18N

        // set the current directory
        File currentLocation = new File(hk2HomeTextField.getText());
        if (currentLocation.exists() && currentLocation.isDirectory()) {
            chooser.setCurrentDirectory(currentLocation.getParentFile());
            chooser.setSelectedFile(currentLocation);
        }
        
        return chooser;
    }   
    
    public void removeNotify() {
        // !PW Is there a better place for this?  If the retriever is still running
        // the user must have hit cancel on the wizard, so tell the retriever thread
        // to shut down and clean up.
        if(retriever != null) {
            retriever.stopRetrieval();
        }
        super.removeNotify();
    }
    
    // ------------------------------------------------------------------------
    // Updater implementation
    // ------------------------------------------------------------------------
    public void updateMessageText(final String msg) {
        if(SwingUtilities.isEventDispatchThread()) {
            downloadStatusLabel.setText(msg);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    downloadStatusLabel.setText(msg);
                }
            });
        }
    }
    
    public void updateStatusText(final String status) {
        statusText = status;
        fireChangeEvent();
    }

    public void clearCancelState() {
        updateCancelState(false);
        retriever = null;
    }
    
    private void updateCancelState(boolean state) {
        final String buttonText = state ? "Cancel Download" : "Download V3 Now...";
        if(SwingUtilities.isEventDispatchThread()) {
            downloadButton.setText(buttonText);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    downloadButton.setText(buttonText);
                }
            });
        }
    }
    
    // ------------------------------------------------------------------------
    
    private static class DirFilter extends javax.swing.filechooser.FileFilter {
        
        public boolean accept(File f) {
            if(!f.exists() || !f.canRead() || !f.isDirectory() ) {
                return false;
            }else{
                return true;
            }
        }
        
        public String getDescription() {
            return NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_DirType");
        }
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        hk2HomeLabel = new javax.swing.JLabel();
        hk2HomeTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        downloadButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        downloadStatusLabel = new javax.swing.JLabel();
        agreeCheckBox = new javax.swing.JCheckBox();
        readlicenseButton = new javax.swing.JButton();

        hk2HomeLabel.setLabelFor(hk2HomeTextField);
        org.openide.awt.Mnemonics.setLocalizedText(hk2HomeLabel, org.openide.util.NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_InstallLocation")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_BrowseButton")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        downloadButton.setText("[download/cancel]"); // NOI18N
        downloadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadButtonActionPerformed(evt);
            }
        });

        downloadStatusLabel.setText("[download status]"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(downloadStatusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(downloadStatusLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        agreeCheckBox.setMargin(new java.awt.Insets(4, 4, 4, 4));
        agreeCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                agreeCheckBoxStateChanged(evt);
            }
        });

        readlicenseButton.setText(org.openide.util.NbBundle.getMessage(AddServerLocationVisualPanel.class, "LBL_ReadLicenseText")); // NOI18N
        readlicenseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readlicenseButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(hk2HomeLabel)
                .addContainerGap(255, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(downloadButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(agreeCheckBox)
                .add(2, 2, 2)
                .add(readlicenseButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(hk2HomeTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(browseButton)
                .add(8, 8, 8))
            .add(layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(hk2HomeLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(browseButton)
                    .add(hk2HomeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(downloadButton)
                    .add(agreeCheckBox)
                    .add(readlicenseButton))
                .add(18, 18, 18)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(115, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void agreeCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_agreeCheckBoxStateChanged
    downloadButton.setEnabled(agreeCheckBox.isSelected());
}//GEN-LAST:event_agreeCheckBoxStateChanged

private void readlicenseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readlicenseButtonActionPerformed
        try{
            URLDisplayer.getDefault().showURL(new URL("https://glassfish.dev.java.net/public/CDDLv1.0.html"));//NOI18N
        } catch (Exception eee){
            return;//nothing much to do
            
        }
}//GEN-LAST:event_readlicenseButtonActionPerformed

private void downloadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadButtonActionPerformed
        if(retriever == null) {
            retriever = new Retriever(new File(hk2HomeTextField.getText()), 
                    V3_DOWNLOAD_URL, this);
            new Thread(retriever).start();
            updateCancelState(true);
        } else {
            retriever.stopRetrieval();
        }
}//GEN-LAST:event_downloadButtonActionPerformed

private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        String newLoc = browseHomeLocation();
        if(newLoc != null && newLoc.length() > 0) {
            hk2HomeTextField.setText(newLoc);
        }
}//GEN-LAST:event_browseButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox agreeCheckBox;
    private javax.swing.JButton browseButton;
    private javax.swing.JButton downloadButton;
    private javax.swing.JLabel downloadStatusLabel;
    private javax.swing.JLabel hk2HomeLabel;
    private javax.swing.JTextField hk2HomeTextField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton readlicenseButton;
    // End of variables declaration//GEN-END:variables
    
}
