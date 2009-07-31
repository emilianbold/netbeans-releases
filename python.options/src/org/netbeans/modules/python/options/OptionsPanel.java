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
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import org.netbeans.modules.python.api.PythonOptions;
import org.netbeans.modules.python.debugger.actions.JpyDbgView;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

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

    }

    void store() {
        PythonOptions pyOptions = PythonOptions.getInstance() ;
        pyOptions.setPromptForArgs(chkPrompt.isSelected());
        // debug options
        pyOptions.setPythonDebuggingPort(Integer.parseInt(dbgLstnPortStartField.getText())) ;
        pyOptions.setStopAtFirstLine( stopAtFLineCheck.isSelected() ) ;
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

    private void chooseFont(JButton button) {
        Font font = button.getFont();
        PropertyEditor pe = PropertyEditorManager.findEditor(Font.class);
        if (pe != null) {
            // use NB font chooser
            if (font != null) {
                pe.setValue(font);
            }
            DialogDescriptor dd = new DialogDescriptor(pe.getCustomEditor(),
                    NbBundle.getMessage(OptionsPanel.class, "LBL_FontChooser"));
            DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
            if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                font = (Font) pe.getValue();
            }

        } else {
            // fallback to own font chooser
            font = new FontSelectorDialog(JOptionPane.getFrameForComponent(button),
                    button.getFont()).getSelectedFont();
        }

        if (font != null) {
            setFont(font);
            updateFonts(font);
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

    chkPrompt.setText(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.chkPrompt1.text")); // NOI18N

    org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
      jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
      .add(jPanel3Layout.createSequentialGroup()
        .addContainerGap()
        .add(chkPrompt)
        .addContainerGap(202, Short.MAX_VALUE))
    );
    jPanel3Layout.setVerticalGroup(
      jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
      .add(jPanel3Layout.createSequentialGroup()
        .addContainerGap()
        .add(chkPrompt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(233, Short.MAX_VALUE))
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
            .add(jLayeredPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE))
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
        .addContainerGap(25, Short.MAX_VALUE))
    );

    jLayeredPane1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.jLayeredPane1.AccessibleContext.accessibleName")); // NOI18N

    jTabbedPane6.addTab(org.openide.util.NbBundle.getMessage(OptionsPanel.class, "OptionsPanel.jPanel4.TabConstraints.tabTitle"), jPanel4); // NOI18N

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
  private javax.swing.JCheckBox chkPrompt;
  private javax.swing.JTextField dbgLstnPortStartField;
  private javax.swing.JButton errorColorBtn;
  private javax.swing.JButton fontBtn;
  private javax.swing.JButton headerColorBtn;
  private javax.swing.JButton infoColorBtn;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLayeredPane jLayeredPane1;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JPanel jPanel4;
  private javax.swing.JTabbedPane jTabbedPane6;
  private javax.swing.JCheckBox stopAtFLineCheck;
  private javax.swing.JButton warningColorBtn;
  // End of variables declaration//GEN-END:variables

}
