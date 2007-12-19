/*
 * CommandLineOptionsPanel.java
 *
 * Created on 19 Сентябрь 2007 г., 14:13
 */

package org.netbeans.modules.php.project.options;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.php.project.customizer.CustomizerCommandLine;
import org.netbeans.modules.php.project.ui.actions.SystemPackageFinder;
import org.netbeans.modules.php.project.wizards.NewPhpProjectWizardIterator;
import org.openide.util.NbBundle;

/**
 *
 * @author  avk
 */
public class CommandLineOptionsPanel extends JPanel {

    private static final String SEARCH = "SEARCH"; // NOI18N
    private static final String BROWSE = "BROWSE"; // NOI18N
    private static final String SELECT_PHP_LOCATION = "LBL_SelectPhpLocation"; // NOI18N
    private static final String MSG_ILLEGAL_PHP_PATH = "MSG_IllegalPhpPath";   // NOI18N
    private static final String MSG_ABSENT_FILE = "MSG_AbsentFile";            // NOI18N
    private static final String MSG_NO_SEARCH_RESULTS = "MSG_NoSearchResults"; // NOI18N

    /** Creates new form CommandLineOptionsPanel */
    public CommandLineOptionsPanel(CommandLineOptionsPanelController controller) {
        myControler = controller;
        initComponents();
        addListeners();
    }

    void load() {
        myLoaded = false;

        String path = CommandLinePreferences.getInstance().getPhpInterpreter();
        if (path != null) {
            myInterpreterPath.setText(path);
        }
        myLoaded = true;
    }

    void store() {
        interpreterPathIsValid();
        CommandLinePreferences.getInstance().setPhpInterpreter(myInterpreterPath.getText());
    }

    void cancel() {
        // nothing to do
    }

    private void addListeners() {
        myInterpreterPath.getDocument().addDocumentListener(new PathListener());
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
            interpreterPathIsValid();
            myControler.changed();
        }
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

        interpreterContainer = new javax.swing.JPanel();
        myPaneLabel = new javax.swing.JLabel();
        myInterpreterLbl = new javax.swing.JLabel();
        myInterpreterPath = new javax.swing.JTextField();
        myProgressContainer = new javax.swing.JPanel();
        myPhpProgramBrowse = new javax.swing.JButton();
        myPhpProgramSearch = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        myMessageContainer = new javax.swing.JPanel();
        myMessagePanel = new javax.swing.JTextPane();
        spacer = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        myPaneLabel.setText(org.openide.util.NbBundle.getMessage(CommandLineOptionsPanel.class, "LBL_Pane_Label")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(myInterpreterLbl, org.openide.util.NbBundle.getMessage(CommandLineOptionsPanel.class, "LBL_CommandPath")); // NOI18N

        myProgressContainer.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(myPhpProgramBrowse, org.openide.util.NbBundle.getMessage(CommandLineOptionsPanel.class, "LBL_BrowseLocation_Button")); // NOI18N
        myPhpProgramBrowse.setActionCommand(BROWSE);
        myPhpProgramBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myPhpProgramBrowsebrowsePressed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(myPhpProgramSearch, org.openide.util.NbBundle.getMessage(CommandLineOptionsPanel.class, "LBL_SearchLocation_Button")); // NOI18N
        myPhpProgramSearch.setActionCommand(SEARCH);
        myPhpProgramSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myPhpProgramSearchActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout interpreterContainerLayout = new org.jdesktop.layout.GroupLayout(interpreterContainer);
        interpreterContainer.setLayout(interpreterContainerLayout);
        interpreterContainerLayout.setHorizontalGroup(
            interpreterContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(interpreterContainerLayout.createSequentialGroup()
                .addContainerGap()
                .add(interpreterContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(interpreterContainerLayout.createSequentialGroup()
                        .add(interpreterContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(interpreterContainerLayout.createSequentialGroup()
                                .add(myInterpreterLbl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 84, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(myInterpreterPath, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE))
                            .add(myProgressContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE))
                        .add(10, 10, 10)
                        .add(interpreterContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(myPhpProgramSearch, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(myPhpProgramBrowse, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .add(interpreterContainerLayout.createSequentialGroup()
                        .add(myPaneLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)))
                .addContainerGap())
        );
        interpreterContainerLayout.setVerticalGroup(
            interpreterContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(interpreterContainerLayout.createSequentialGroup()
                .addContainerGap()
                .add(interpreterContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(myPaneLabel)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(interpreterContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(myPhpProgramBrowse)
                    .add(myInterpreterPath, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(myInterpreterLbl))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(interpreterContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(myProgressContainer, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                    .add(myPhpProgramSearch))
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
        myMessagePanel.setText(org.openide.util.NbBundle.getMessage(CommandLineOptionsPanel.class, "CommandLineOptionsPanel.myMessagePanel.text")); // NOI18N
        myMessagePanel.setFocusable(false);
        myMessagePanel.setMinimumSize(new java.awt.Dimension(6, 10));
        myMessagePanel.setOpaque(false);
        myMessagePanel.setPreferredSize(new java.awt.Dimension(0, 0));

        org.jdesktop.layout.GroupLayout myMessageContainerLayout = new org.jdesktop.layout.GroupLayout(myMessageContainer);
        myMessageContainer.setLayout(myMessageContainerLayout);
        myMessageContainerLayout.setHorizontalGroup(
            myMessageContainerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, myMessageContainerLayout.createSequentialGroup()
                .addContainerGap()
                .add(myMessagePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE))
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
            .add(0, 449, Short.MAX_VALUE)
        );
        spacerLayout.setVerticalGroup(
            spacerLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 171, Short.MAX_VALUE)
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

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle(NbBundle.getMessage(CustomizerCommandLine.class, SELECT_PHP_LOCATION));
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
                File projectDir = chooser.getSelectedFile();
                myInterpreterPath.setText(projectDir.getAbsolutePath());
            }
        }
    }//GEN-LAST:event_myPhpProgramBrowsebrowsePressed

    private void myPhpProgramSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myPhpProgramSearchActionPerformed
        if (SEARCH.equals(evt.getActionCommand())) {

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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel interpreterContainer;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel myInterpreterLbl;
    private javax.swing.JTextField myInterpreterPath;
    private javax.swing.JPanel myMessageContainer;
    private javax.swing.JTextPane myMessagePanel;
    private javax.swing.JLabel myPaneLabel;
    private javax.swing.JButton myPhpProgramBrowse;
    private javax.swing.JButton myPhpProgramSearch;
    private javax.swing.JPanel myProgressContainer;
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

    private CommandLineOptionsPanelController myControler;
    private boolean myLoaded = false;
    private JComponent myProgress;
}