/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

/*
 * GeneralInfoPanel.java
 * Created on July 25, 2005, 10:37 AM
 */
package org.netbeans.modules.mobility.jsr172.multiview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.mobility.end2end.classdata.WSDLService;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.mobility.end2end.E2EDataObject;
import org.netbeans.modules.mobility.end2end.client.config.ClassDescriptor;
import org.netbeans.modules.mobility.end2end.client.config.ClientConfiguration;
import org.netbeans.modules.mobility.end2end.client.config.Configuration;
import org.openide.util.NbBundle;

/**
 *
 * @author  Michal Skvor,Sigal Duek
 *
 */

public class JSR172ServicePanel extends SectionInnerPanel implements PropertyChangeListener {
    
    private transient E2EDataObject dataObject;
    private Configuration configuration;
    private WSDLService wsdlService;
    private WsdlUpdater updater;
    
    public JSR172ServicePanel(){
        this(null,null,null);
    }
    
    
    
    /** Creates new form GeneralInfoPanel */
    
    public JSR172ServicePanel( SectionView sectionView, E2EDataObject dataObject , Configuration configuration) {
        
        super( sectionView );
        this.dataObject = dataObject;
        this.configuration = configuration;
        initComponents();
        if (configuration != null){
            initValues();
        }
        
        dataObject.addPropertyChangeListener(new PropertyChangeListener() {
            @SuppressWarnings("synthetic-access")
			public void propertyChange(final PropertyChangeEvent evt) {
                if (E2EDataObject.PROP_GENERATING.equals(evt.getPropertyName())){
                    buttonGenerate.setEnabled(!((Boolean)evt.getNewValue()).booleanValue());
                }
            }
        });
    }
    private void initValues() {
        wsdlService = (WSDLService)configuration.getServices().get(0);
        final String url = wsdlService.getUrl();
        if(  url == null || url.equals( "" )) {
//            System.err.println("ERR_UrlIsNotValid");
            getSectionView().getErrorPanel().setError(
                    new Error( Error.TYPE_FATAL, Error.ERROR_MESSAGE,
                    NbBundle.getMessage( JSR172ServicePanel.class, "ERR_UrlIsNotValid" ), textUrl ));
        }
        textUrl.setText( url );
    }
    
    public JComponent getErrorComponent( @SuppressWarnings("unused")
	final String errorId ) {
        return null;
    }
    
    public void linkButtonPressed( @SuppressWarnings("unused")
	final Object ddBean, @SuppressWarnings("unused")
	final String ddProperty ) {
    }
    
    public void setValue( @SuppressWarnings("unused")
	final JComponent source, @SuppressWarnings("unused")
	final Object value ) {
    }
    
    
    
    public void documentChanged( @SuppressWarnings("unused")
	final JTextComponent comp, @SuppressWarnings("unused")
	final String value) {
    }
    
    
    
    /** This method is called from within the constructor to
     *
     * initialize the form.
     *
     * WARNING: Do NOT modify this code. The content of this method is
     *
     * always regenerated by the Form Editor.
     *
     */
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        textUrl = new javax.swing.JTextField();
        refreshButton = new javax.swing.JButton();
        buttonGenerate = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(JSR172ServicePanel.class, "WSDL_URL_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        add(jLabel1, gridBagConstraints);

        textUrl.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 10);
        add(textUrl, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, org.openide.util.NbBundle.getMessage(JSR172ServicePanel.class, "Label_Refresh_WSDL")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        add(refreshButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(buttonGenerate, org.openide.util.NbBundle.getMessage(JSR172ServicePanel.class, "LBL_Generate")); // NOI18N
        buttonGenerate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonGenerateActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 10);
        add(buttonGenerate, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void buttonGenerateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonGenerateActionPerformed
        buttonGenerate.setEnabled(false);
        dataObject.generate();
    }//GEN-LAST:event_buttonGenerateActionPerformed
    
    
    
    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        
        //download wsdl and compare with checksum
        //if there is a differnece notify the user to update
        refreshButton.setEnabled(false);
        final String url = wsdlService.getUrl();
        //find wsdl file
        final ClientConfiguration configuration = dataObject.getConfiguration().getClientConfiguration();
        final ClassDescriptor cd = configuration.getClassDescriptor();
        final WSDLService service = (WSDLService) dataObject.getConfiguration().getServices().get(0);
        final String fileName = service.getFile();
        
        updater = new WsdlUpdater(url, cd.getPackageName().replace('.','/'), fileName, dataObject);
        updater.addPropertyChangeListener(this);
    }//GEN-LAST:event_refreshButtonActionPerformed
    
    
    
    public void propertyChange(final PropertyChangeEvent evt) {
        if (WsdlUpdater.PROP_UPDATE_FINISHED.equals(evt.getPropertyName())){
            refreshButton.setEnabled(true);
            updater.removePropertyChangeListener(this);
            updater = null;
        }
    }
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonGenerate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton refreshButton;
    private javax.swing.JTextField textUrl;
    // End of variables declaration//GEN-END:variables
    
}

