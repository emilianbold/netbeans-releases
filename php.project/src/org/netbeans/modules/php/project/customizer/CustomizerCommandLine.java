/*
 * CustomizerCommandLine.java
 *
 * Created on 18 Сентябрь 2007 г., 18:28
 */

package org.netbeans.modules.php.project.customizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.options.CommandLinePreferences;
import org.netbeans.modules.php.project.options.PhpOptionsCategory;
import org.netbeans.modules.php.project.ui.actions.SystemPackageFinder;
import org.openide.util.NbBundle;

/**
 *
 * @author  avk
 */
public class CustomizerCommandLine extends javax.swing.JPanel {

    private static final String CONFIGURE = "CONFIGURE"; // NOI18N
    private static final String SEARCH = "SEARCH"; // NOI18N
    private static final String BROWSE = "BROWSE"; // NOI18N
    private static final String SELECT_PHP_LOCATION = "LBL_SelectPhpLocation"; // NOI18N
    private static final String MSG_ILLEGAL_PHP_PATH = "MSG_IllegalPhpPath"; // NOI18N
    private static final String MSG_ABSENT_FILE = "MSG_AbsentFile"; // NOI18N
    private static final String MSG_NO_SEARCH_RESULTS = "MSG_NoSearchResults"; // NOI18N

    /** Creates new form CustomizerCommandLine */
    public CustomizerCommandLine(PhpProjectProperties uiProperties) {
        initComponents();

        load(uiProperties);

        myInterpreterPath.getDocument().addDocumentListener(new PathListener());

        RadioListener listener = new RadioListener();
        myDefaultRadio.addChangeListener(listener);
        mySpecialRadio.addChangeListener(listener);
    }

    private void load(PhpProjectProperties uiProperties) {
        myProps = uiProperties;

        loadDefaultInterpreterPath();

        String path = uiProperties.getProperty(PhpProject.COMMAND_PATH);
        if (path == null) {
            myDefaultRadio.setSelected(true);
        } else {
            mySpecialRadio.setSelected(true);
            myInterpreterPath.setText(path);
        }
    }

    private void loadDefaultInterpreterPath() {
        String defaultPath = CommandLinePreferences.getInstance().getPhpInterpreter();
        if (defaultPath != null) {
            myInterpreterPathDefault.setText(defaultPath);
        }
    }

    private class PathListener implements DocumentListener {

        /* (non-Javadoc)
         * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
         */
        public void changedUpdate(DocumentEvent e) {
            fireChange();
        }

        /* (non-Javadoc)
         * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
         */
        public void insertUpdate(DocumentEvent e) {
            fireChange();
        }

        /* (non-Javadoc)
         * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
         */
        public void removeUpdate(DocumentEvent e) {
            fireChange();
        }

        private void fireChange() {
            interpreterPathChanged();
        }
    }

    private class DefaultPathListener implements PreferenceChangeListener {

        public void preferenceChange(PreferenceChangeEvent evt) {
            loadDefaultInterpreterPath();
            CommandLinePreferences.getInstance().removePreferenceChangeListener(DefaultPathListener.this);
        }
    }

    private class RadioListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            Object obj = e.getSource();
            if (obj == myDefaultRadio && myDefaultRadio.isSelected()) {
                useDefaultInterpreterPath();
            } else if (obj == mySpecialRadio && mySpecialRadio.isSelected()) {
                interpreterPathChanged();
            }
        }
    }

    private void interpreterPathChanged() {
        mySpecialRadio.setSelected(true);
        interpreterPathIsValid();
        String path = myInterpreterPath.getText();
        getProperties().setProperty(PhpProject.COMMAND_PATH, path);
    }

    private void useDefaultInterpreterPath() {
        getProperties().remove(PhpProject.COMMAND_PATH);
    }

    boolean interpreterPathIsValid() {
        File destFolder = new File(myInterpreterPath.getText()).getAbsoluteFile();
        File file = getCanonicalFile(destFolder);
        if (file == null) {
            String message = NbBundle.getMessage(this.getClass(), MSG_ABSENT_FILE);
            //setErrorMessage(message);
            setMessage(message);
            return false;
        }
        if (!file.isFile()) {
            String message = NbBundle.getMessage(this.getClass(), MSG_ILLEGAL_PHP_PATH);
            //setErrorMessage(message);
            setMessage(message);
            return false;
        }

        setMessage("");
        return true;
    }

    public static File getCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            return null;
        }
    }

    private void configureProgressPanel(JComponent progressComponent) {
        if (myProgress != null) {
            myProgressContainer.remove(myProgress);
        }

        myProgress = progressComponent == null 
                ? new JPanel() 
                : progressComponent;
        myProgressContainer.add(myProgress, BorderLayout.CENTER);

        myProgressContainer.validate();
        validate();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        myRadioGroup = new javax.swing.ButtonGroup();
        interpreterContainer = new javax.swing.JPanel();
        myProgressContainer = new javax.swing.JPanel();
        myInterpreterLbl = new javax.swing.JLabel();
        myInterpreterPath = new javax.swing.JTextField();
        myPhpProgramBrowse = new javax.swing.JButton();
        myPhpProgramSearch = new javax.swing.JButton();
        myDefaultRadio = new javax.swing.JRadioButton();
        mySpecialRadio = new javax.swing.JRadioButton();
        myInterpreterPathDefault = new javax.swing.JTextField();
        myPaneLabel = new javax.swing.JLabel();
        myInterpreterLbl1 = new javax.swing.JLabel();
        myConfigureDefault = new javax.swing.JButton();
        myMessageContainer = new javax.swing.JPanel();
        myMessagePanel = new javax.swing.JTextPane();
        spacer = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        myProgressContainer.setLayout(new java.awt.BorderLayout());

        myInterpreterLbl.setLabelFor(myInterpreterPath);
        org.openide.awt.Mnemonics.setLocalizedText(myInterpreterLbl, org.openide.util.NbBundle.getMessage(CustomizerCommandLine.class, "LBL_CommandPath")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(myPhpProgramBrowse, org.openide.util.NbBundle.getMessage(CustomizerCommandLine.class, "LBL_BrowseLocation_Button")); // NOI18N
        myPhpProgramBrowse.setActionCommand(BROWSE);
        myPhpProgramBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myPhpProgramBrowsebrowsePressed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(myPhpProgramSearch, org.openide.util.NbBundle.getMessage(CustomizerCommandLine.class, "LBL_SearchLocation_Button")); // NOI18N
        myPhpProgramSearch.setActionCommand(SEARCH);
        myPhpProgramSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myPhpProgramSearchActionPerformed(evt);
            }
        });

        myRadioGroup.add(myDefaultRadio);
        myDefaultRadio.setSelected(true);
        myDefaultRadio.setText(org.openide.util.NbBundle.getMessage(CustomizerCommandLine.class, "LBL_Default_Radio")); // NOI18N

        myRadioGroup.add(mySpecialRadio);
        mySpecialRadio.setText(org.openide.util.NbBundle.getMessage(CustomizerCommandLine.class, "LBL_Special_Radio")); // NOI18N

        myInterpreterPathDefault.setEditable(false);

        myPaneLabel.setText(org.openide.util.NbBundle.getMessage(CustomizerCommandLine.class, "LBL_Pane_Label")); // NOI18N

        myInterpreterLbl1.setLabelFor(myInterpreterPathDefault);
        org.openide.awt.Mnemonics.setLocalizedText(myInterpreterLbl1, org.openide.util.NbBundle.getMessage(CustomizerCommandLine.class, "LBL_CommandPathDefault")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(myConfigureDefault, org.openide.util.NbBundle.getMessage(CustomizerCommandLine.class, "LBL_ConfigureDefault_Button")); // NOI18N
        myConfigureDefault.setActionCommand(CONFIGURE);
        myConfigureDefault.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myConfigureDefaultActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout interpreterContainerLayout = new org.jdesktop.layout.GroupLayout(interpreterContainer);
        interpreterContainer.setLayout(interpreterContainerLayout);
        interpreterContainerLayout.setHorizontalGroup(
            interpreterContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, interpreterContainerLayout.createSequentialGroup()
                .addContainerGap()
                .add(interpreterContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(myPaneLabel)
                    .add(myDefaultRadio)
                    .add(mySpecialRadio)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, interpreterContainerLayout.createSequentialGroup()
                        .add(interpreterContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(interpreterContainerLayout.createSequentialGroup()
                                .add(21, 21, 21)
                                .add(interpreterContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, interpreterContainerLayout.createSequentialGroup()
                                        .add(myInterpreterLbl1)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(myInterpreterPathDefault, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE))
                                    .add(interpreterContainerLayout.createSequentialGroup()
                                        .add(myInterpreterLbl)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(myInterpreterPath, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE))))
                            .add(myProgressContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE))
                        .add(10, 10, 10)
                        .add(interpreterContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(myConfigureDefault, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, myPhpProgramBrowse, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, myPhpProgramSearch))))
                .addContainerGap())
        );
        interpreterContainerLayout.setVerticalGroup(
            interpreterContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(interpreterContainerLayout.createSequentialGroup()
                .addContainerGap()
                .add(myPaneLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(myDefaultRadio)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(interpreterContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myInterpreterLbl1)
                    .add(myInterpreterPathDefault, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(myConfigureDefault))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mySpecialRadio)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(interpreterContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myPhpProgramBrowse)
                    .add(myInterpreterPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(myInterpreterLbl))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(interpreterContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(myProgressContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(myPhpProgramSearch, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(interpreterContainer, gridBagConstraints);

        myMessagePanel.setEditable(false);
        myMessagePanel.setText(org.openide.util.NbBundle.getMessage(CustomizerCommandLine.class, "CustomizerCommandLine.myMessagePanel.text")); // NOI18N
        myMessagePanel.setFocusable(false);
        myMessagePanel.setMinimumSize(new java.awt.Dimension(6, 10));
        myMessagePanel.setOpaque(false);
        myMessagePanel.setPreferredSize(new java.awt.Dimension(0, 0));

        org.jdesktop.layout.GroupLayout myMessageContainerLayout = new org.jdesktop.layout.GroupLayout(myMessageContainer);
        myMessageContainer.setLayout(myMessageContainerLayout);
        myMessageContainerLayout.setHorizontalGroup(
            myMessageContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, myMessagePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE)
        );
        myMessageContainerLayout.setVerticalGroup(
            myMessageContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(myMessageContainerLayout.createSequentialGroup()
                .add(myMessagePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(myMessageContainer, gridBagConstraints);

        org.jdesktop.layout.GroupLayout spacerLayout = new org.jdesktop.layout.GroupLayout(spacer);
        spacer.setLayout(spacerLayout);
        spacerLayout.setHorizontalGroup(
            spacerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 438, Short.MAX_VALUE)
        );
        spacerLayout.setVerticalGroup(
            spacerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 27, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(spacer, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void myPhpProgramBrowsebrowsePressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myPhpProgramBrowsebrowsePressed
        if (BROWSE.equals(evt.getActionCommand())) {
            mySpecialRadio.setSelected(true);

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(NbBundle.getMessage(CustomizerCommandLine.class, SELECT_PHP_LOCATION));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
                File projectDir = chooser.getSelectedFile();
                myInterpreterPath.setText(projectDir.getAbsolutePath());
            }
            interpreterPathChanged();
        }
    }//GEN-LAST:event_myPhpProgramBrowsebrowsePressed

    private void myPhpProgramSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myPhpProgramSearchActionPerformed
        if (SEARCH.equals(evt.getActionCommand())) {
            mySpecialRadio.setSelected(true);

            //String title = NbBundle.getMessage(CustomizerCommandLine.class, "LBL_BTN_Perform_Auto");
            ProgressHandle progress = ProgressHandleFactory.createHandle(SEARCH); // NOI18N
            JComponent progressComponent = ProgressHandleFactory.createProgressComponent(progress);
            configureProgressPanel(progressComponent);
            myPhpProgramSearch.setEnabled(false);

            String interpreter = SystemPackageFinder.getPhpInterpreterUserChoice(progress);
            if (interpreter != null) {
                myInterpreterPath.setText(interpreter);
            } else {
                String message = NbBundle.getMessage(this.getClass(), MSG_NO_SEARCH_RESULTS);
                setMessage(message);
            }

            myPhpProgramSearch.setEnabled(true);
            configureProgressPanel(null);
        }
}//GEN-LAST:event_myPhpProgramSearchActionPerformed

    private void myConfigureDefaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myConfigureDefaultActionPerformed
        if (CONFIGURE.equals(evt.getActionCommand())) {
            myDefaultRadio.setSelected(true);
            CommandLinePreferences.getInstance().addPreferenceChangeListener(new DefaultPathListener());
            PhpOptionsCategory.displayPhpOptions();
        }
        // TODO add your handling code here:
    }//GEN-LAST:event_myConfigureDefaultActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel interpreterContainer;
    private javax.swing.JButton myConfigureDefault;
    private javax.swing.JRadioButton myDefaultRadio;
    private javax.swing.JLabel myInterpreterLbl;
    private javax.swing.JLabel myInterpreterLbl1;
    private javax.swing.JTextField myInterpreterPath;
    private javax.swing.JTextField myInterpreterPathDefault;
    private javax.swing.JPanel myMessageContainer;
    private javax.swing.JTextPane myMessagePanel;
    private javax.swing.JLabel myPaneLabel;
    private javax.swing.JButton myPhpProgramBrowse;
    private javax.swing.JButton myPhpProgramSearch;
    private javax.swing.JPanel myProgressContainer;
    private javax.swing.ButtonGroup myRadioGroup;
    private javax.swing.JRadioButton mySpecialRadio;
    private javax.swing.JPanel spacer;
    // End of variables declaration//GEN-END:variables

    public void setMessage(String msg) {
        myMessagePanel.setText(msg);
        myMessagePanel.setForeground(Color.DARK_GRAY);
    }

    public void setErrorMessage(String msg) {
        myMessagePanel.setText(msg);
        myMessagePanel.setForeground(Color.RED);
    }

    private PhpProjectProperties getProperties() {
        return myProps;
    }
    private PhpProjectProperties myProps;
    private JComponent myProgress;
}