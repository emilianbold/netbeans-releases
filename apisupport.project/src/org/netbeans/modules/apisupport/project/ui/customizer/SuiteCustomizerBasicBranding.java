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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * Represents <em>Application</em> panel in Suite customizer.
 *
 * @author Radek Matous
 */
final class SuiteCustomizerBasicBranding extends NbPropertyPanel.Suite  {
    
    private URL iconSource;
    private BasicCustomizer.SubCategoryProvider prov;
    /**
     * Creates new form SuiteCustomizerLibraries
     */
    public SuiteCustomizerBasicBranding(final SuiteProperties suiteProps, ProjectCustomizer.Category cat, 
            BasicCustomizer.SubCategoryProvider prov) {
        super(suiteProps, SuiteCustomizerBasicBranding.class, cat);
        initComponents();        
        this.prov = prov;
        refresh(); 
        checkValidity();
        DocumentListener textFieldChangeListener = new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                checkValidity();
            }
        };
        nameValue.getDocument().addDocumentListener(textFieldChangeListener);
        titleValue.getDocument().addDocumentListener(textFieldChangeListener);                
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        if (prov != null) {
            showSubCategory(prov);
            // do preselect just once..
            prov = null;
        }
    }
    
    
    protected void checkValidity() {
        boolean panelValid = true;
        
        if (panelValid && nameValue.getText().trim().length() == 0) {
            category.setErrorMessage(NbBundle.getMessage(SuiteCustomizerBasicBranding.class, "ERR_EmptyName"));//NOI18N
            panelValid = false;
        }

        if (panelValid && !nameValue.getText().trim().matches("[a-z][a-z0-9]*(_[a-z][a-z0-9]*)*")) {//NOI18N
            category.setErrorMessage(NbBundle.getMessage(SuiteCustomizerBasicBranding.class, "ERR_InvalidName"));//NOI18N
            panelValid = false;
        }
        
        if (panelValid && titleValue.getText().trim().length() == 0) {
            category.setErrorMessage(NbBundle.getMessage(SuiteCustomizerBasicBranding.class, "ERR_EmptyTitle"));//NOI18N
            panelValid = false;
        }        
        
        if (panelValid) {        
            category.setErrorMessage(null);
        }
        category.setValid(panelValid);
    }
    
    void refresh() {
        getBrandingModel().brandingEnabledRefresh();        
        getBrandingModel().initName(true);
        getBrandingModel().initTitle(true);
        standaloneApp.setSelected(getBrandingModel().isBrandingEnabled());
        addOn.setSelected(!getBrandingModel().isBrandingEnabled());
        nameValue.setText(getBrandingModel().getName());
        titleValue.setText(getBrandingModel().getTitle());
        iconSource = getBrandingModel().getIconSource();
        if (iconSource != null) {
            ((ImagePreview)iconPreview).setImage(new ImageIcon(iconSource));
        }
        //iconLocation.setText(getBrandingModel().getIconLocation());
        
        enableOrDisableComponents();
        
    }
    
    public @Override void store() {
        //getBrandingModel().setBrandingEnabled(buildWithBranding.isSelected());        
        getBrandingModel().setName(nameValue.getText());
        getBrandingModel().setTitle(titleValue.getText());
        getBrandingModel().setIconSource(iconSource);
    }
    
    private void enableOrDisableComponents() {
        nameValue.setEnabled(standaloneApp.isSelected());
        name.setEnabled(standaloneApp.isSelected());        
        
        titleValue.setEnabled(standaloneApp.isSelected());
        title.setEnabled(standaloneApp.isSelected());
        
        browse.setEnabled(standaloneApp.isSelected());
        
        icon.setEnabled(standaloneApp.isSelected());
        
        iconPreview.setEnabled(standaloneApp.isSelected());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        title = new javax.swing.JLabel();
        titleValue = new javax.swing.JTextField();
        iconPreview = new ImagePreview(BasicBrandingModel.ICON_WIDTH, BasicBrandingModel.ICON_HEIGHT);
        name = new javax.swing.JLabel();
        nameValue = new javax.swing.JTextField();
        browse = new javax.swing.JButton();
        icon = new javax.swing.JLabel();
        addOn = new javax.swing.JRadioButton();
        standaloneApp = new javax.swing.JRadioButton();

        setLayout(new java.awt.GridBagLayout());

        title.setLabelFor(titleValue);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(title, bundle.getString("LBL_AppTitle")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(title, gridBagConstraints);
        title.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_Title")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(titleValue, gridBagConstraints);

        iconPreview.setLabelFor(iconPreview);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(23, 0, 0, 12);
        add(iconPreview, gridBagConstraints);

        name.setLabelFor(nameValue);
        org.openide.awt.Mnemonics.setLocalizedText(name, bundle.getString("LBL_AppName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 0);
        add(name, gridBagConstraints);
        name.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_Name")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 0);
        add(nameValue, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browse, bundle.getString("CTL_Browse")); // NOI18N
        browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(23, 0, 0, 0);
        add(browse, gridBagConstraints);
        browse.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_Browse")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(icon, bundle.getString("LBL_AppIcon")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(23, 0, 0, 12);
        add(icon, gridBagConstraints);

        buttonGroup1.add(addOn);
        org.openide.awt.Mnemonics.setLocalizedText(addOn, bundle.getString("LBL_AppAddOn")); // NOI18N
        addOn.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        addOn.setMargin(new java.awt.Insets(0, 0, 0, 0));
        addOn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addOnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(addOn, gridBagConstraints);
        addOn.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddOn")); // NOI18N

        buttonGroup1.add(standaloneApp);
        org.openide.awt.Mnemonics.setLocalizedText(standaloneApp, bundle.getString("LBL_AppStandAlone")); // NOI18N
        standaloneApp.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        standaloneApp.setMargin(new java.awt.Insets(0, 0, 0, 0));
        standaloneApp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                standaloneAppActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(standaloneApp, gridBagConstraints);
        standaloneApp.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_StandAloneApp")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void standaloneAppActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_standaloneAppActionPerformed
        //TODO: Exclude IDE Modules must be done
        enableOrDisableComponents();
        getBrandingModel().setBrandingEnabled(standaloneApp.isSelected());                
    }//GEN-LAST:event_standaloneAppActionPerformed

    private void addOnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addOnActionPerformed
        enableOrDisableComponents();
        getBrandingModel().setBrandingEnabled(standaloneApp.isSelected());                
    }//GEN-LAST:event_addOnActionPerformed
    
    private void browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseActionPerformed
        JFileChooser chooser = UIUtil.getIconFileChooser();
        int ret = chooser.showDialog(this, NbBundle.getMessage(getClass(), "LBL_Select")); // NOI18N
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file =  chooser.getSelectedFile();
            try {
                iconSource = file.toURI().toURL();
            } catch (MalformedURLException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            ((ImagePreview)iconPreview).setImage(new ImageIcon(iconSource));
        }
    }//GEN-LAST:event_browseActionPerformed
        
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton addOn;
    private javax.swing.JButton browse;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel icon;
    private javax.swing.JLabel iconPreview;
    private javax.swing.JLabel name;
    private javax.swing.JTextField nameValue;
    private javax.swing.JRadioButton standaloneApp;
    private javax.swing.JLabel title;
    private javax.swing.JTextField titleValue;
    // End of variables declaration//GEN-END:variables
    
    static class ImagePreview extends JLabel {
        private ImageIcon image = null;
        private int width;
        private int height;
//        private javax.swing.border.Border border;
        ImagePreview(int width, int height){
            //this.image = im;
            this.width = width;
            this.height = height;            
            //border = new TitledBorder(NbBundle.getMessage(getClass(),"LBL_IconPreview"));//NOI18N
            //setBorder(border);
        }
        
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2d = (Graphics2D)g;
            
            if (!isEnabled()) {
                g2d.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, 0.3f));
            }
            
            if ((getWidth() >  width) && (getHeight() > height) && image != null) {
                /*if (getBorder() == null) {
                    setBorder(border);
                }*/
                int x = 0;//(getWidth()/2)-(width/2);
                int y = 0;//(getHeight()/2)-(height/2);
                g.drawImage(image.getImage(),x, y, width, height, this.getBackground(),null);
            } /*else {
                if (getBorder() != null) {
                    setBorder(null);
                }
            }*/
        }
        
        private void setImage(ImageIcon image) {
            this.image = image;
            repaint();
        }
    }
    
    private BasicBrandingModel getBrandingModel() {
        return getProperties().getBrandingModel();
    }
    
    private void showSubCategory(BasicCustomizer.SubCategoryProvider prov) {
        if (SuiteCustomizer.APPLICATION.equals(prov.getCategory()) &&
            SuiteCustomizer.APPLICATION_CREATE_STANDALONE_APPLICATION.equals(prov.getSubcategory())) {
            standaloneApp.requestFocus();
        }
    }
    
}
