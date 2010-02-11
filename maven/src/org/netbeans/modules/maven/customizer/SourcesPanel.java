/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.customizer;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.maven.api.ModelUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Customizer panel for setting source level and encoding.
 * in future possibly also source roots and resource roots.
 * @author mkleint
 */
public class SourcesPanel extends JPanel {
    
    
    private String encoding;
    private String defaultEncoding;
    private String defaultSourceLevel = "1.3";//NOI18N
    private String sourceLevel;
    private ModelHandle handle;

    public SourcesPanel( ModelHandle handle, NbMavenProjectImpl project ) {
        initComponents();
        this.handle = handle;
        FileObject projectFolder = project.getProjectDirectory();
        File pf = FileUtil.toFile( projectFolder );
        txtProjectFolder.setText( pf == null ? "" : pf.getPath() ); // NOI18N
        
        
        comSourceLevel.setEditable(false);
        sourceLevel = SourceLevelQuery.getSourceLevel(project.getProjectDirectory());
        comSourceLevel.setModel(new DefaultComboBoxModel(new String[] {
            "1.3", "1.4", "1.5", "1.6" //NOI18N
        }));
        
        comSourceLevel.setSelectedItem(sourceLevel);
        String enc = project.getOriginalMavenProject().getProperties().getProperty(Constants.ENCODING_PROP);
        if (enc == null) {
            enc = PluginPropertyUtils.getPluginProperty(project,
                    Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, Constants.ENCODING_PARAM, null);
        }
        encoding = enc;
        if (enc != null) {
            try {
                Charset chs = Charset.forName(enc);
                encoding = chs.name();
            } catch (Exception e) {
                Logger.getLogger(this.getClass().getName()).info("IllegalCharsetName: " + enc); //NOI18N
            }
        }
        // TODO oh well, we fallback to default platform encoding.. that's correct
        // for times before the http://docs.codehaus.org/display/MAVENUSER/POM+Element+for+Source+File+Encoding
        // proposal. this proposal defines the default value as ISO-8859-1
        
        if (encoding == null) {
            encoding = Charset.defaultCharset().toString();
        }
        defaultEncoding = Charset.defaultCharset().toString();
        
        comEncoding.setModel(new EncodingModel(encoding));
        comEncoding.setRenderer(new EncodingRenderer());
        
        comSourceLevel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSourceLevelChange();
            }
        });
        
        comEncoding.addActionListener(new ActionListener () {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleEncodingChange();
            }            
        });
        txtSrc.setText(project.getOriginalMavenProject().getBuild().getSourceDirectory());
        txtTestSrc.setText(project.getOriginalMavenProject().getBuild().getTestSourceDirectory());
    }
    
    private void handleSourceLevelChange() {
        sourceLevel = (String)comSourceLevel.getSelectedItem();
        ModelUtils.checkSourceLevel(handle, sourceLevel);
        if (defaultSourceLevel.equals(sourceLevel)) {
            lblSourceLevel.setFont(lblSourceLevel.getFont().deriveFont(Font.PLAIN));
        } else {
            lblSourceLevel.setFont(lblSourceLevel.getFont().deriveFont(Font.BOLD));
        }
    }

    
    
    private void handleEncodingChange () {
        Charset enc = (Charset) comEncoding.getSelectedItem();
        String encName;
        if (enc != null) {
            encName = enc.name();
        } else {
            encName = encoding;
        }
        ModelUtils.checkEncoding(handle, encName);
        if (defaultEncoding.equals(encName)) {
            lblEncoding.setFont(lblEncoding.getFont().deriveFont(Font.PLAIN));
        } else {
            lblEncoding.setFont(lblEncoding.getFont().deriveFont(Font.BOLD));
        }
    }
    
    

    private static class EncodingRenderer extends DefaultListCellRenderer {
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            assert value instanceof Charset; 
            return super.getListCellRendererComponent(list, ((Charset)value).displayName(), index, isSelected, cellHasFocus);
        }
    }
    
    private static class EncodingModel extends DefaultComboBoxModel {
        
        public EncodingModel (String originalEncoding) {
            Charset defEnc = null;
            for (Charset c : Charset.availableCharsets().values()) {
                if (c.name().equals(originalEncoding)) {
                    defEnc = c;
                }
                addElement(c);
            }
            if (defEnc == null) {
                //Create artificial Charset to keep the original value
                //May happen when the project was set up on the platform
                //which supports more encodings
                try {
                    defEnc = new UnknownCharset (originalEncoding);
                    addElement(defEnc);
                } catch (IllegalCharsetNameException e) {
                    //The source.encoding property is completely broken
                    Logger.getLogger(this.getClass().getName()).info("IllegalCharsetName: " + originalEncoding); //NOI18N
                }
            }
            if (defEnc == null) {
                defEnc = FileEncodingQuery.getDefaultEncoding();
            }
            setSelectedItem(defEnc);
        }
    }
    
    private static class UnknownCharset extends Charset {
        
        UnknownCharset (String name) {
            super (name, new String[0]);
        }
    
        @Override
        public boolean contains(Charset c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharsetDecoder newDecoder() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharsetEncoder newEncoder() {
            throw new UnsupportedOperationException();
        }
}
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblProjectFolder = new javax.swing.JLabel();
        txtProjectFolder = new javax.swing.JTextField();
        lblSrc = new javax.swing.JLabel();
        txtSrc = new javax.swing.JTextField();
        lblTestSrc = new javax.swing.JLabel();
        txtTestSrc = new javax.swing.JTextField();
        lblGenerated = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        lblSourceLevel = new javax.swing.JLabel();
        comSourceLevel = new javax.swing.JComboBox();
        lblEncoding = new javax.swing.JLabel();
        comEncoding = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();

        lblProjectFolder.setLabelFor(txtProjectFolder);
        org.openide.awt.Mnemonics.setLocalizedText(lblProjectFolder, org.openide.util.NbBundle.getBundle(SourcesPanel.class).getString("CTL_ProjectFolder")); // NOI18N

        txtProjectFolder.setEditable(false);

        lblSrc.setLabelFor(txtSrc);
        org.openide.awt.Mnemonics.setLocalizedText(lblSrc, org.openide.util.NbBundle.getBundle(SourcesPanel.class).getString("SourcesPanel.lblSrc.text")); // NOI18N

        txtSrc.setEditable(false);

        lblTestSrc.setLabelFor(txtTestSrc);
        org.openide.awt.Mnemonics.setLocalizedText(lblTestSrc, org.openide.util.NbBundle.getBundle(SourcesPanel.class).getString("SourcesPanel.lblTestSrc.text")); // NOI18N

        txtTestSrc.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(lblGenerated, org.openide.util.NbBundle.getBundle(SourcesPanel.class).getString("SourcesPanel.lblGenerated.text")); // NOI18N
        lblGenerated.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        lblSourceLevel.setLabelFor(comSourceLevel);
        org.openide.awt.Mnemonics.setLocalizedText(lblSourceLevel, org.openide.util.NbBundle.getMessage(SourcesPanel.class, "TXT_SourceLevel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        jPanel1.add(lblSourceLevel, gridBagConstraints);

        comSourceLevel.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1.4", "1.5" }));
        comSourceLevel.setMinimumSize(this.comSourceLevel.getPreferredSize());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(comSourceLevel, gridBagConstraints);
        comSourceLevel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(SourcesPanel.class).getString("AN_SourceLevel")); // NOI18N
        comSourceLevel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourcesPanel.class, "SourcesPanel.comSourceLevel.AccessibleContext.accessibleDescription")); // NOI18N

        lblEncoding.setLabelFor(comEncoding);
        org.openide.awt.Mnemonics.setLocalizedText(lblEncoding, org.openide.util.NbBundle.getMessage(SourcesPanel.class, "TXT_Encoding")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        jPanel1.add(lblEncoding, gridBagConstraints);

        comEncoding.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        jPanel1.add(comEncoding, gridBagConstraints);
        comEncoding.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourcesPanel.class, "SourcesPanel.comEncoding.AccessibleContext.accessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jPanel2, gridBagConstraints);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblProjectFolder)
                    .add(lblSrc)
                    .add(lblTestSrc))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(txtTestSrc, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                    .add(txtSrc, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                    .add(txtProjectFolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)))
            .add(lblGenerated, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblProjectFolder)
                    .add(txtProjectFolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblSrc)
                    .add(txtSrc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblTestSrc)
                    .add(txtTestSrc, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(lblGenerated, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 135, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 148, Short.MAX_VALUE)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        txtProjectFolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourcesPanel.class, "SourcesPanel.txtProjectFolder.AccessibleContext.accessibleDescription")); // NOI18N
        txtSrc.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourcesPanel.class, "SourcesPanel.txtSrc.AccessibleContext.accessibleDescription")); // NOI18N
        txtTestSrc.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SourcesPanel.class, "SourcesPanel.txtTestSrc.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox comEncoding;
    private javax.swing.JComboBox comSourceLevel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblEncoding;
    private javax.swing.JLabel lblGenerated;
    private javax.swing.JLabel lblProjectFolder;
    private javax.swing.JLabel lblSourceLevel;
    private javax.swing.JLabel lblSrc;
    private javax.swing.JLabel lblTestSrc;
    private javax.swing.JTextField txtProjectFolder;
    private javax.swing.JTextField txtSrc;
    private javax.swing.JTextField txtTestSrc;
    // End of variables declaration//GEN-END:variables
    
}
