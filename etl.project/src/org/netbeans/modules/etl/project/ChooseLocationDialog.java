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
package org.netbeans.modules.etl.project;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import javax.swing.JFileChooser;

public class ChooseLocationDialog extends javax.swing.JDialog {

    private String objDefn = null;
    private String dbLocn = null;
    private String dbName = null;

    /** Creates new form NewJDialog */
    public ChooseLocationDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setLocation(300, 300);
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        panel = new javax.swing.JPanel();
        objDefnLabel = new javax.swing.JLabel();
        dbLocationTextField = new javax.swing.JTextField();
        dbLocationLabel = new javax.swing.JLabel();
        dbLocationBtn = new javax.swing.JButton();
        objDefnTextField = new javax.swing.JTextField();
        objDefnBtn = new javax.swing.JButton();
        dbNameTextField = new javax.swing.JTextField();
        dbLabel = new javax.swing.JLabel();
        btnPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Choose DB Location");
        setModal(true);
        setResizable(false);

        panel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panel.setMaximumSize(new java.awt.Dimension(500, 150));
        panel.setMinimumSize(new java.awt.Dimension(500, 150));
        panel.setPreferredSize(new java.awt.Dimension(500, 150));
        panel.setLayout(new java.awt.GridBagLayout());

        objDefnLabel.setText("Master Index Object Definition");
        objDefnLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        objDefnLabel.setMaximumSize(new java.awt.Dimension(150, 25));
        objDefnLabel.setMinimumSize(new java.awt.Dimension(150, 25));
        objDefnLabel.setPreferredSize(new java.awt.Dimension(150, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        panel.add(objDefnLabel, gridBagConstraints);

        dbLocationTextField.setMaximumSize(new java.awt.Dimension(200, 25));
        dbLocationTextField.setMinimumSize(new java.awt.Dimension(200, 25));
        dbLocationTextField.setPreferredSize(new java.awt.Dimension(200, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        panel.add(dbLocationTextField, gridBagConstraints);

        dbLocationLabel.setText("Staging Database Location");
        dbLocationLabel.setMaximumSize(new java.awt.Dimension(150, 25));
        dbLocationLabel.setMinimumSize(new java.awt.Dimension(150, 25));
        dbLocationLabel.setPreferredSize(new java.awt.Dimension(150, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        panel.add(dbLocationLabel, gridBagConstraints);

        dbLocationBtn.setText("Browse");
        dbLocationBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbLocationActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        panel.add(dbLocationBtn, gridBagConstraints);

        objDefnTextField.setMaximumSize(new java.awt.Dimension(200, 25));
        objDefnTextField.setMinimumSize(new java.awt.Dimension(200, 25));
        objDefnTextField.setPreferredSize(new java.awt.Dimension(200, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        panel.add(objDefnTextField, gridBagConstraints);

        objDefnBtn.setText("Browse");
        objDefnBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                objDefnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        panel.add(objDefnBtn, gridBagConstraints);

        dbNameTextField.setPreferredSize(new java.awt.Dimension(200, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        panel.add(dbNameTextField, gridBagConstraints);

        dbLabel.setText("Database Name");
        dbLabel.setMaximumSize(new java.awt.Dimension(150, 25));
        dbLabel.setMinimumSize(new java.awt.Dimension(150, 25));
        dbLabel.setPreferredSize(new java.awt.Dimension(150, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        panel.add(dbLabel, gridBagConstraints);

        getContentPane().add(panel, java.awt.BorderLayout.CENTER);

        okButton.setText("Ok");
        okButton.setPreferredSize(new java.awt.Dimension(75, 25));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        btnPanel.add(okButton);

        cancelButton.setText("Cancel");
        cancelButton.setPreferredSize(new java.awt.Dimension(75, 25));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        btnPanel.add(cancelButton);

        getContentPane().add(btnPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void objDefnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_objDefnActionPerformed
        JFileChooser chooser = new JFileChooser();
        //chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        FileFilter currentFilter = chooser.getFileFilter();
        chooser.addChoosableFileFilter(new Filter(new String[] {".xml"}));
        chooser.setFileFilter(currentFilter);
        int value = chooser.showOpenDialog(this);
        if (value == JFileChooser.APPROVE_OPTION) {
            objDefnTextField.setText(chooser.getSelectedFile().toString());//getCurrentDirectory().toString());
        } else {
            objDefnTextField.setText("");
        }
    //setObjectDefinition(chooser.getCurrentDirectory().getAbsolutePath());
}//GEN-LAST:event_objDefnActionPerformed

    private void setObjectDefinition(String str) {
        this.objDefn = str;
    }

    public String getObjectDefinition() {
        return objDefn;
    }

    private void dbLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbLocationActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int value = chooser.showOpenDialog(this);
        if (value == JFileChooser.APPROVE_OPTION) {
            dbLocationTextField.setText(chooser.getSelectedFile().toString());
        } else {
            dbLocationTextField.setText("");
        }
    //setDBLocation(dbLocationTextField.getText());
}//GEN-LAST:event_dbLocationActionPerformed

    private void setDBLocation(String str) {
        this.dbLocn = str;
    }

    public String getDBLocation() {
        return dbLocn;
    }

    private void setDBName(String str) {
        this.dbName = str;
    }

    public String getDBName() {
        return dbName;
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.dispose();
        System.exit(0);
}//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        setDBLocation(dbLocationTextField.getText());
        String str = objDefnTextField.getText();
        if(str.endsWith("object.xml")){
          str = str.replace("object.xml", "");          
        }else{
            System.out.println("Choose object.xml");
        }
        setObjectDefinition(str);
        setDBName(dbNameTextField.getText());
        this.dispose();       
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    /*public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                ChooseLocationDialog dialog = new ChooseLocationDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }*/
    class Filter extends javax.swing.filechooser.FileFilter {
        private String[] extensions;
        public Filter(String[] extensions) {            
            this.extensions = new String[extensions.length];
            for (int i = 0; i < extensions.length; i++) {
                this.extensions[i] = extensions[i].toUpperCase();
            }
        }
        
         @Override
        public boolean accept(File file) {
            //String filename = file.getName();
            //return filename.endsWith(".xml");
             if (file.isDirectory()) {
                return true;
            }
            for (int i = 0; i < extensions.length; i++) {
                if (file.getName().toUpperCase().endsWith(extensions[i])) {
                    return true;
                }
            }
            
            return false;
        }
        public String getDescription() {
            return "*.xml";
        }
       
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel btnPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel dbLabel;
    private javax.swing.JButton dbLocationBtn;
    private javax.swing.JLabel dbLocationLabel;
    private javax.swing.JTextField dbLocationTextField;
    private javax.swing.JTextField dbNameTextField;
    private javax.swing.JButton objDefnBtn;
    private javax.swing.JLabel objDefnLabel;
    private javax.swing.JTextField objDefnTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel panel;
    // End of variables declaration//GEN-END:variables
}