/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.versioning.util;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Tomas Stupka
 */
public abstract class ExportDiffSupport {
    private ExportDiffPanel panel;
    private DialogDescriptor dd;
    private Preferences preferences;
    private Dialog dialog;
    private ExportDiffProvider edp;

    public ExportDiffSupport(File[] files, final Preferences preferences) {
        this.preferences = preferences;
        edp = Lookup.getDefault().lookup(ExportDiffProvider.class);
        String currentFilePath = preferences.get("ExportDiff.saveFolder", System.getProperty("user.home"));
        if(edp == null) {
            dd = new DialogDescriptor(createFileChooser(new File(currentFilePath)), NbBundle.getMessage(ExportDiffSupport.class, "CTL_Export_Title"));
            dd.setOptions(new Object[0]);
        } else {
            edp.setContext(files);
            panel = new ExportDiffPanel(edp.getComponent());
            panel.fileTextField.setText(currentFilePath);
            panel.browseButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    onChooseFile(new File(panel.fileTextField.getText()));
                }
            });
            dd = new DialogDescriptor(panel, NbBundle.getMessage(ExportDiffSupport.class, "CTL_Export_Title"));
            edp.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if(evt.getPropertyName().equals(ExportDiffProvider.EVENT_DATA_CHANGED)) {
                        validate();
                    }
                }
            });
            panel.fileTextField.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e)  { validate(); }
                public void removeUpdate(DocumentEvent e)  { validate(); }
                public void changedUpdate(DocumentEvent e) { validate(); }
            });
            panel.asFileRadioButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { validate(); }
            });
            panel.attachRadioButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { validate(); }
            });
        }
        validate();
    }

    private void validate() {
        assert panel != null;
        if(panel.asFileRadioButton.isSelected()) {
            dd.setValid(!panel.fileTextField.getText().trim().equals(""));
        } else {
            dd.setValid(edp.isValid());
        }
    }

    public void export() {
        dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        if(dd.getValue() == DialogDescriptor.OK_OPTION) {
            if(edp == null || panel.asFileRadioButton.isSelected()) {
                File toFile = new File(panel.fileTextField.getText());
                writeDiffFile(toFile);
            } else {
                final Task[] t = new Task[1];
                Cancellable c = new Cancellable() {
                    public boolean cancel() {
                        if(t[0] != null) {
                            return t[0].cancel();
                        }
                        return true; 
                    }
                };
                final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ExportDiffSupport.class, "CTL_Attaching"), c);
                handle.start();
                t[0] = Utils.createTask(new Runnable() {
                    public void run() {
                        try {
                            File toFile;
                            try {
                                toFile = File.createTempFile("vcs-diff.patch", null);
                            } catch (IOException ex) {
                                // XXX
                                return;
                            }
                            toFile.deleteOnExit();
                            writeDiffFile(toFile);
                            edp.handeDiffFile(toFile);
                        } finally {
                            handle.finish();
                        }
                    }
                });
                t[0].schedule(0);
            }
        }
    }

    /**
     * Synchronously writtes the changes to the given file
     * @param file
     */
    public abstract void writeDiffFile(File file);

    private void onChooseFile(File currentDir) {
        final JFileChooser chooser = createFileChooser(currentDir);

        DialogDescriptor dd = new DialogDescriptor(chooser, NbBundle.getMessage(ExportDiffSupport.class, "CTL_Export_Title"));
        dd.setOptions(new Object[0]);
        dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
    }

    private JFileChooser createFileChooser(File curentDir) {
        final JFileChooser chooser = new AccessibleJFileChooser(NbBundle.getMessage(ExportDiffSupport.class, "ACSD_Export"));
        chooser.setDialogTitle(NbBundle.getMessage(ExportDiffSupport.class, "CTL_Export_Title"));
        chooser.setMultiSelectionEnabled(false);
        javax.swing.filechooser.FileFilter[] old = chooser.getChoosableFileFilters();
        for (int i = 0; i < old.length; i++) {
            javax.swing.filechooser.FileFilter fileFilter = old[i];
            chooser.removeChoosableFileFilter(fileFilter);

        }
        chooser.setCurrentDirectory(curentDir); // NOI18N
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.getName().endsWith("diff") || f.getName().endsWith("patch") || f.isDirectory();  // NOI18N
            }
            public String getDescription() {
                return NbBundle.getMessage(ExportDiffSupport.class, "BK3002");
            }
        });

        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setApproveButtonMnemonic(NbBundle.getMessage(ExportDiffSupport.class, "MNE_Export_ExportAction").charAt(0));
        chooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String state = e.getActionCommand();
                if (state.equals(JFileChooser.APPROVE_SELECTION)) {
                    File destination = chooser.getSelectedFile();
                    String name = destination.getName();
                    boolean requiredExt = false;
                    requiredExt |= name.endsWith(".diff");  // NOI18N
                    requiredExt |= name.endsWith(".dif");   // NOI18N
                    requiredExt |= name.endsWith(".patch"); // NOI18N
                    if (requiredExt == false) {
                        File parent = destination.getParentFile();
                        destination = new File(parent, name + ".patch"); // NOI18N
                    }

                    if (destination.exists()) {
                        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(ExportDiffSupport.class, "BK3005", destination.getAbsolutePath()));
                        nd.setOptionType(NotifyDescriptor.YES_NO_OPTION);
                        DialogDisplayer.getDefault().notify(nd);
                        if (nd.getValue().equals(NotifyDescriptor.OK_OPTION) == false) {
                            return;
                        }
                    }
                    preferences.put("ExportDiff.saveFolder", destination.getParent()); // NOI18N
                    panel.fileTextField.setText(destination.getAbsolutePath());
                }
                if(dialog != null) {
                    dialog.dispose();
                }
            }
        });
        return chooser;
    }

    public static abstract class ExportDiffProvider {
        private PropertyChangeSupport support = new PropertyChangeSupport(this);
        private final static String EVENT_DATA_CHANGED = "ExportDiff.data.changed";

        /**
         * Sets the files for which this provider should provide
         * @return
         */
        protected abstract void setContext(File[] files);

        /**
         * Handles the given diff file
         * @param file
         */
        public abstract void handeDiffFile(File file);

        /**
         * Return a visual component representing this ExportDiffProvider
         * @return
         */
        public abstract JComponent getComponent();

        /**
         * Returns true if the user intput in this ExportDiffProvider-s
         * component isValid, oherwise false
         * @return
         */
        public abstract boolean isValid();

        /**
         * To be called if there was a change made in this ExportDiffProvider-s
         * visual components data
         */
        protected void fireDataChanged() {
            support.firePropertyChange(EVENT_DATA_CHANGED, null, null);
        }
        public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
            support.removePropertyChangeListener(listener);
        }
        public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
            support.addPropertyChangeListener(listener);
        }
    }
}
