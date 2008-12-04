/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * OptionsPanel.java
 *
 * Created on Aug 28, 2008, 9:40:22 PM
 */

package org.netbeans.modules.python.options;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.netbeans.modules.python.api.PythonOptions;
import org.netbeans.modules.python.debugger.actions.JpyDbgView;

/**
 *
 * @author alley
 * @author jean-yves
 */
public class OptionsPanel extends javax.swing.JPanel
{
    private OptionsOptionsPanelController controller;

    /** Creates new form OptionsPanel */
    public OptionsPanel() {
        initComponents();
    }

    OptionsPanel(OptionsOptionsPanelController ctrl) {
        this();
        this.controller = ctrl;
    }

    private void displayButton( JButton button ,
                                Color foreground
                              ) {
    Font curFont = fontBtn.getFont()  ;
      button.setForeground(foreground) ;
      button.setFont(curFont);
    }

    void load() {
        PythonOptions pyOptions = PythonOptions.getInstance() ;
        chkPrompt.setSelected(pyOptions.getPromptForArgs());
        // debug options
        dbgLstnPortStartField.setText(Integer.toString(pyOptions.getPythonDebuggingPort())) ;
        stopAtFLineCheck.setSelected(pyOptions.getStopAtFirstLine() ) ;
        // coloring shell options
        fontBtn.setFont(pyOptions.getDbgShellFont() ) ; // populate initially fontBtn with stored font
        displayButton(backgroundColorBtn , pyOptions.getDbgShellBackground() ) ;
        displayButton(infoColorBtn , pyOptions.getDbgShellInfoColor() ) ;
        displayButton(warningColorBtn , pyOptions.getDbgShellWarningColor() ) ;
        displayButton(errorColorBtn , pyOptions.getDbgShellErrorColor() ) ;
        displayButton(headerColorBtn , pyOptions.getDbgShellHeaderColor() ) ;
        displayButton(fontBtn , pyOptions.getDbgShellInfoColor() ) ;

        // pylint options
        checkUsePyLint.setSelected(pyOptions.getUsePylint());
        checkError.setSelected(pyOptions.getPylintError());
        checkFatal.setSelected(pyOptions.getPylintFatal());
        checkWarnings.setSelected(pyOptions.getPylintWarning());
        checkConvention.setSelected(pyOptions.getPylintConvention());
        checkRefactor.setSelected(pyOptions.getPylintRefactor());
        pylintLocationField.setText(pyOptions.getPylintLocation());
        pylintCompOptField.setText(pyOptions.getPylintOptions());
    }

    void store() {
        PythonOptions pyOptions = PythonOptions.getInstance() ;
        pyOptions.setPromptForArgs(chkPrompt.isSelected());
        // debug options
        pyOptions.setPythonDebuggingPort(Integer.parseInt(dbgLstnPortStartField.getText())) ;
        pyOptions.setStopAtFirstLine( stopAtFLineCheck.isSelected() ) ;
        // pylint options
        pyOptions.setUsePyLint(checkUsePyLint.isSelected());
        pyOptions.setPyLintFatal(checkFatal.isSelected());
        pyOptions.setPyLintError(checkError.isSelected());
        pyOptions.setPyLintWarning(checkWarnings.isSelected());
        pyOptions.setPyLintConvention(checkConvention.isSelected());
        pyOptions.setPyLintRefactor(checkRefactor.isSelected());
        pyOptions.setPyLintLocation(pylintLocationField.getText());
        pyOptions.setPyLintOptions(pylintCompOptField.getText());
        // coloring shell options
        pyOptions.setDbgShellFont(fontBtn.getFont());
        pyOptions.setDbgShellBackground(backgroundColorBtn.getForeground());
        pyOptions.setDbgShellInfoColor(infoColorBtn.getForeground());
        pyOptions.setDbgShellErrorColor(errorColorBtn.getForeground());
        pyOptions.setDbgShellWarningColor(warningColorBtn.getForeground());
        pyOptions.setDbgShellHeaderColor(headerColorBtn.getForeground());
        // populate to listening sollicitors
        shellDbgOptionsChanged(pyOptions) ;
    }

    /** populate changed options color to dbgview shell if needed */
    private void shellDbgOptionsChanged( PythonOptions options )
    {
    JpyDbgView dbgView = JpyDbgView.getCurrentView() ;

      if (dbgView != null )
         dbgView.applyColorChanges(options);

    }


    private void chooseColor( JButton button )
    {
    Color c = JColorChooser.showDialog(  OptionsPanel.this,
                                         "Debug Shell Color chooser" ,
                                         button.getForeground());
      if(c != null)
        displayButton( button ,c  ) ;

    }

    private void updateFonts( Font font )
    {
      fontBtn.setFont(font) ;
      infoColorBtn.setFont(font) ;
      warningColorBtn.setFont(font) ;
      errorColorBtn.setFont(font) ;
      headerColorBtn.setFont(font) ;
    }

    private void chooseFont( JButton button )
    {
    Font font;

	    font = new FontSelectorDialog(
					  JOptionPane.getFrameForComponent(
					  button) , button.getFont() ).getSelectedFont() ;

	    if(font != null)
	    {
		     setFont(font);
		     updateFonts( font );
	    }

    }

    boolean valid() {
        return true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane6 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        chkPrompt = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        dbgLstnPortStartField = new javax.swing.JTextField();
        jLayeredPane1 = new javax.swing.JLayeredPane();
        errorColorBtn = new javax.swing.JButton();
        fontBtn = new javax.swing.JButton();
        backgroundColorBtn = new javax.swing.JButton();
        infoColorBtn = new javax.swing.JButton();
        headerColorBtn = new javax.swing.JButton();
        warningColorBtn = new javax.swing.JButton();
        stopAtFLineCheck = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        checkUsePyLint = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        pylintLocationField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        pylintCompOptField = new javax.swing.JTextField();
        checkFatal = new javax.swing.JCheckBox();
        checkError = new javax.swing.JCheckBox();
        checkWarnings = new javax.swing.JCheckBox();
        checkConvention = new javax.swing.JCheckBox();
        checkRefactor = new javax.swing.JCheckBox();

        chkPrompt.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.chkPrompt1.text")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(chkPrompt)
                .addContainerGap(214, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(chkPrompt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(247, Short.MAX_VALUE))
        );

        jTabbedPane6.addTab(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.jLabel1.text")); // NOI18N

        dbgLstnPortStartField.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.dbgLstnPortStartField.text")); // NOI18N

        jLayeredPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.jLayeredPane1.border.title"))); // NOI18N

        errorColorBtn.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.errorColorBtn.text")); // NOI18N
        errorColorBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                errorColorBtnActionPerformed(evt);
            }
        });
        errorColorBtn.setBounds(20, 130, 320, 20);
        jLayeredPane1.add(errorColorBtn, javax.swing.JLayeredPane.DEFAULT_LAYER);

        fontBtn.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.fontBtn.text")); // NOI18N
        fontBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontBtnActionPerformed(evt);
            }
        });
        fontBtn.setBounds(20, 30, 320, 20);
        jLayeredPane1.add(fontBtn, javax.swing.JLayeredPane.DEFAULT_LAYER);

        backgroundColorBtn.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.backgroundColorBtn.text")); // NOI18N
        backgroundColorBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backgroundColorBtnActionPerformed(evt);
            }
        });
        backgroundColorBtn.setBounds(20, 50, 320, 20);
        jLayeredPane1.add(backgroundColorBtn, javax.swing.JLayeredPane.DEFAULT_LAYER);

        infoColorBtn.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.infoColorBtn.text")); // NOI18N
        infoColorBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infoColorBtnActionPerformed(evt);
            }
        });
        infoColorBtn.setBounds(20, 70, 320, 20);
        jLayeredPane1.add(infoColorBtn, javax.swing.JLayeredPane.DEFAULT_LAYER);

        headerColorBtn.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.headerColorBtn.text")); // NOI18N
        headerColorBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                headerColorBtnActionPerformed(evt);
            }
        });
        headerColorBtn.setBounds(20, 90, 320, 20);
        jLayeredPane1.add(headerColorBtn, javax.swing.JLayeredPane.DEFAULT_LAYER);

        warningColorBtn.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.warningColorBtn.text")); // NOI18N
        warningColorBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                warningColorBtnActionPerformed(evt);
            }
        });
        warningColorBtn.setBounds(20, 110, 320, 20);
        jLayeredPane1.add(warningColorBtn, javax.swing.JLayeredPane.DEFAULT_LAYER);

        stopAtFLineCheck.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.stopAtFLineCheck.text")); // NOI18N
        stopAtFLineCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopAtFLineCheckActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(11, 11, 11)
                        .add(jLayeredPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE))
                    .add(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(dbgLstnPortStartField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 155, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(stopAtFLineCheck, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 292, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(16, 16, 16)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(dbgLstnPortStartField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLayeredPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 161, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(stopAtFLineCheck)
                .addContainerGap(38, Short.MAX_VALUE))
        );

        jLayeredPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.jLayeredPane1.AccessibleContext.accessibleName")); // NOI18N

        jTabbedPane6.addTab(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N

        checkUsePyLint.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.checkUsePyLint.text")); // NOI18N

        jLabel5.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.jLabel5.text")); // NOI18N

        pylintLocationField.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.pylintLocationField.text")); // NOI18N
        pylintLocationField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pylintLocationFieldActionPerformed(evt);
            }
        });

        browseButton.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.browseButton.text")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        jLabel6.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.jLabel6.text")); // NOI18N

        pylintCompOptField.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.pylintCompOptField.text")); // NOI18N

        checkFatal.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.checkFatal.text")); // NOI18N

        checkError.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.checkError.text")); // NOI18N

        checkWarnings.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.checkWarnings.text")); // NOI18N
        checkWarnings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkWarningsActionPerformed(evt);
            }
        });

        checkConvention.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.checkConvention.text")); // NOI18N

        checkRefactor.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.checkRefactor.text")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(checkUsePyLint)
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(jLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pylintLocationField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton))
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(checkFatal)
                        .addContainerGap(336, Short.MAX_VALUE))
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(checkError)
                        .addContainerGap(329, Short.MAX_VALUE))
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(checkWarnings)
                        .addContainerGap(308, Short.MAX_VALUE))
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(checkConvention)
                        .addContainerGap(294, Short.MAX_VALUE))
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(checkRefactor)
                        .addContainerGap(315, Short.MAX_VALUE))
                    .add(jPanel5Layout.createSequentialGroup()
                        .add(jLabel6)
                        .add(1, 1, 1)
                        .add(pylintCompOptField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(checkUsePyLint)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(browseButton)
                    .add(pylintLocationField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(pylintCompOptField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(checkFatal)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(checkError)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(checkWarnings)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(checkConvention)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(checkRefactor)
                .addContainerGap(59, Short.MAX_VALUE))
        );

        jTabbedPane6.addTab(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.jPanel5.TabConstraints.tabTitle"), jPanel5); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void pylintLocationFieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_pylintLocationFieldActionPerformed
    {//GEN-HEADEREND:event_pylintLocationFieldActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_pylintLocationFieldActionPerformed

    private void checkWarningsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_checkWarningsActionPerformed
    {//GEN-HEADEREND:event_checkWarningsActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_checkWarningsActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_browseButtonActionPerformed
    {//GEN-HEADEREND:event_browseButtonActionPerformed
        final JFileChooser fc = new JFileChooser();
        fc.setFileHidingEnabled(false);
        int returnVal = fc.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION){
            pylintLocationField.setText(fc.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    private void errorColorBtnActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_errorColorBtnActionPerformed
    {//GEN-HEADEREND:event_errorColorBtnActionPerformed
        // TODO add your handling code here:
        chooseColor( (JButton) evt.getSource() ) ;
}//GEN-LAST:event_errorColorBtnActionPerformed

    private void backgroundColorBtnActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_backgroundColorBtnActionPerformed
    {//GEN-HEADEREND:event_backgroundColorBtnActionPerformed
        // TODO add your handling code here:
        chooseColor( (JButton) evt.getSource() ) ;
}//GEN-LAST:event_backgroundColorBtnActionPerformed

    private void infoColorBtnActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_infoColorBtnActionPerformed
    {//GEN-HEADEREND:event_infoColorBtnActionPerformed
        // TODO add your handling code here:
        chooseColor( (JButton) evt.getSource() ) ;
}//GEN-LAST:event_infoColorBtnActionPerformed

    private void headerColorBtnActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_headerColorBtnActionPerformed
    {//GEN-HEADEREND:event_headerColorBtnActionPerformed
        // TODO add your handling code here:
        chooseColor( (JButton) evt.getSource() ) ;
}//GEN-LAST:event_headerColorBtnActionPerformed

    private void warningColorBtnActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_warningColorBtnActionPerformed
    {//GEN-HEADEREND:event_warningColorBtnActionPerformed
        // TODO add your handling code here:
        chooseColor( (JButton) evt.getSource() ) ;
}//GEN-LAST:event_warningColorBtnActionPerformed

    private void stopAtFLineCheckActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_stopAtFLineCheckActionPerformed
    {//GEN-HEADEREND:event_stopAtFLineCheckActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_stopAtFLineCheckActionPerformed

    private void fontBtnActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_fontBtnActionPerformed
    {//GEN-HEADEREND:event_fontBtnActionPerformed
        // TODO add your handling code here:
        chooseFont( (JButton) evt.getSource() ) ;
    }//GEN-LAST:event_fontBtnActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton backgroundColorBtn;
    private javax.swing.JButton browseButton;
    private javax.swing.JCheckBox checkConvention;
    private javax.swing.JCheckBox checkError;
    private javax.swing.JCheckBox checkFatal;
    private javax.swing.JCheckBox checkRefactor;
    private javax.swing.JCheckBox checkUsePyLint;
    private javax.swing.JCheckBox checkWarnings;
    private javax.swing.JCheckBox chkPrompt;
    private javax.swing.JTextField dbgLstnPortStartField;
    private javax.swing.JButton errorColorBtn;
    private javax.swing.JButton fontBtn;
    private javax.swing.JButton headerColorBtn;
    private javax.swing.JButton infoColorBtn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLayeredPane jLayeredPane1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JTabbedPane jTabbedPane6;
    private javax.swing.JTextField pylintCompOptField;
    private javax.swing.JTextField pylintLocationField;
    private javax.swing.JCheckBox stopAtFLineCheck;
    private javax.swing.JButton warningColorBtn;
    // End of variables declaration//GEN-END:variables

}
