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


package org.netbeans.modules.options;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.options.classic.OptionsAction;
import org.netbeans.modules.options.export.OptionsChooserPanel;
import org.netbeans.spi.options.OptionsPanelController;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.SystemAction;


public class OptionsDisplayerImpl {
    /** Link to dialog, if its opened. */
    private static Dialog           dialog;
    /** weak link to options dialog DialogDescriptor. */
    private static WeakReference<DialogDescriptor>    descriptorRef = new WeakReference<DialogDescriptor> (null);
    private static String title = loc("CTL_Options_Dialog_Title");    
    private static Logger log = Logger.getLogger(OptionsDisplayerImpl.class.getName ());    
    private boolean modal;
    static final LookupListener lookupListener = new LookupListenerImpl();
    /** OK button. */
    private JButton bOK;
    /** Advanced Options button. */
    private JButton bClassic;
    /** Export Options button */
    private JButton btnExport;
    /** Import Options button */
    private JButton btnImport;
    
    public OptionsDisplayerImpl (boolean modal) {
        this.modal = modal;
        try {
            // 91106 - listen to default FS changes to update Advanced Options, Export and Import buttons
            FileUtil.getConfigRoot().getFileSystem().addFileChangeListener(new DefaultFSListener());
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public boolean isOpen() {
        return dialog != null;
    }

    /** Selects requested category or subcategory in opened Options dialog.
     * It is used in QuickSearchprovider.
     * @param path path of category and subcategories to be selected. Path is
     * composed from registration names divided by slash. See {OptionsDisplayer#open}.
     */
    static void selectCategory(String path) {
        DialogDescriptor descriptor = null;
        synchronized (lookupListener) {
            descriptor = (DialogDescriptor) descriptorRef.get();
        }
        if (descriptor != null) {
            OptionsPanel optionsPanel = (OptionsPanel) descriptor.getMessage();
            String categoryId = path.indexOf('/') == -1 ? path : path.substring(0, path.indexOf('/'));
            String subpath = path.indexOf('/') == -1 ? null : path.substring(path.indexOf('/') + 1);
            optionsPanel.initCurrentCategory(categoryId, subpath);
        }
        dialog.toFront();
    }

    public void showOptionsDialog (String categoryID, String subpath) {
        if (isOpen()) {
            // dialog already opened
            dialog.setVisible (true);
            dialog.toFront ();
            log.fine("Front Options Dialog"); //NOI18N
            return;
        }
        
        DialogDescriptor descriptor = null;
        synchronized(lookupListener) {
            descriptor = (DialogDescriptor)descriptorRef.get ();
        }

        OptionsPanel optionsPanel = null;
        if (descriptor == null) {
            optionsPanel = categoryID == null ? new OptionsPanel () : new OptionsPanel(categoryID);            
            bOK = (JButton) loc(new JButton(), "CTL_OK");//NOI18N
            bOK.getAccessibleContext().setAccessibleDescription(loc("ACS_OKButton"));//NOI18N
            bClassic = (JButton) loc(new JButton(), "CTL_Classic");//NOI18N
            bClassic.getAccessibleContext().setAccessibleDescription(loc("ACS_ClassicButton"));//NOI18N
            btnExport = (JButton) loc(new JButton(), "CTL_Export");//NOI18N
            btnExport.getAccessibleContext().setAccessibleDescription(loc("ACS_Export"));//NOI18N
            btnImport = (JButton) loc(new JButton(), "CTL_Import");//NOI18N
            btnImport.getAccessibleContext().setAccessibleDescription(loc("ACS_Import"));//NOI18N
            updateButtons();
            boolean isMac = Utilities.isMac();
            Object[] options = new Object[2];            
            options[0] = isMac ? DialogDescriptor.CANCEL_OPTION : bOK;
            options[1] = isMac ? bOK : DialogDescriptor.CANCEL_OPTION;
            descriptor = new DialogDescriptor(optionsPanel,title,modal,options,DialogDescriptor.OK_OPTION,DialogDescriptor.DEFAULT_ALIGN, null, null, false);
            descriptor.setAdditionalOptions(new Object[] {bClassic, btnExport, btnImport});
            descriptor.setHelpCtx(optionsPanel.getHelpCtx());
            OptionsPanelListener listener = new OptionsPanelListener(descriptor, optionsPanel, bOK, bClassic, btnExport, btnImport);
            descriptor.setButtonListener(listener);
            optionsPanel.addPropertyChangeListener(listener);
            synchronized(lookupListener) {
                descriptorRef = new WeakReference<DialogDescriptor>(descriptor);
            }
            log.fine("Create new Options Dialog"); //NOI18N
        } else {
            optionsPanel = (OptionsPanel) descriptor.getMessage ();            
            //TODO: 
            //just in case that switched from advanced
            optionsPanel.update();
            log.fine("Reopen Options Dialog"); //NOI18N
        }
        
        dialog = DialogDisplayer.getDefault ().createDialog (descriptor);
        optionsPanel.initCurrentCategory(categoryID, subpath);
        dialog.addWindowListener (new MyWindowListener (optionsPanel));
        Point userLocation = getUserLocation();
        if (userLocation != null) {
            dialog.setLocation(userLocation);
        }
        dialog.setVisible (true);     
    }

    /** Set visibility of Advanced Options button (AKA classic) according to
     * existence of advanced options. */
    private void updateButtons() {
        if(bClassic != null) {
            bClassic.setVisible(advancedOptionsNotEmpty());
        }
        boolean optionsExportNotEmpty = optionsExportNotEmpty();
        if (btnExport != null) {
            btnExport.setVisible(optionsExportNotEmpty);
        }
        if (btnImport != null) {
            btnImport.setVisible(optionsExportNotEmpty);
        }
    }

    /** Returns true if some non hidden advanced options are registered
     * under UI/Services folder.
     * @return true if exists some advanced options, false otherwise
     */
    private boolean advancedOptionsNotEmpty() {
        FileObject servicesFO = FileUtil.getConfigFile("UI/Services");  //NOI18N
        if(servicesFO != null) {
            FileObject[] advancedOptions = servicesFO.getChildren();
            for (FileObject advancedOption : advancedOptions) {
                Object hidden = advancedOption.getAttribute("hidden");  //NOI18N
                if(hidden == null || !(Boolean)hidden) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Returns true if some non hidden files are registered under OptionsExport
     * folder.
     * @return true if something is registered under OptionsExport, false otherwise
     */
    private boolean optionsExportNotEmpty() {
        FileObject optionsExportFO = FileUtil.getConfigFile("OptionsExport");  //NOI18N
        if(optionsExportFO != null) {
            FileObject[] categories = optionsExportFO.getChildren();
            for (FileObject category : categories) {
                Object hiddenCategory = category.getAttribute("hidden");  //NOI18N
                if (hiddenCategory != null && (Boolean)hiddenCategory) {
                    // skip hidden category folder
                    continue;
                }
                FileObject[] items = category.getChildren();
                for (FileObject item : items) {
                    Object hiddenItem = item.getAttribute("hidden");  //NOI18N
                    if(hiddenItem == null || !(Boolean)hiddenItem) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Point getUserLocation() {
        final Rectangle screenBounds = Utilities.getUsableScreenBounds();
        int x = NbPreferences.forModule(OptionsDisplayerImpl.class).getInt("OptionsX", Integer.MAX_VALUE);//NOI18N
        int y = NbPreferences.forModule(OptionsDisplayerImpl.class).getInt("OptionsY", Integer.MAX_VALUE);//NOI18N
        if (x > screenBounds.getWidth() || y > screenBounds.getHeight()) {
            return null;
        } else {
            return new Point(x, y);
        }
    }

    private static String loc (String key) {
        return NbBundle.getMessage (OptionsDisplayerImpl.class, key);
    }
    
    private static Component loc (Component c, String key) {
        if (c instanceof AbstractButton)
            Mnemonics.setLocalizedText (
                (AbstractButton) c, 
                loc (key)
            );
        else
            Mnemonics.setLocalizedText (
                (JLabel) c, 
                loc (key)
            );
        return c;
    }
    
    private class OptionsPanelListener implements PropertyChangeListener,
    ActionListener {
        private DialogDescriptor    descriptor;
        private OptionsPanel        optionsPanel;
        private JButton             bOK;
        private JButton             bClassic;
        private JButton             btnExport;
        private JButton             btnImport;
        
        
        OptionsPanelListener (
            DialogDescriptor    descriptor, 
            OptionsPanel        optionsPanel,
            JButton             bOK,
            JButton             bClassic,
            JButton             btnExport,
            JButton             btnImport
        ) {
            this.descriptor = descriptor;
            this.optionsPanel = optionsPanel;
            this.bOK = bOK;
            this.bClassic = bClassic;
            this.btnExport = btnExport;
            this.btnImport = btnImport;
        }
        
        public void propertyChange (PropertyChangeEvent ev) {
            if (ev.getPropertyName ().equals ("buran" + OptionsPanelController.PROP_HELP_CTX)) {               //NOI18N            
                descriptor.setHelpCtx (optionsPanel.getHelpCtx ());
            } else if (ev.getPropertyName ().equals ("buran" + OptionsPanelController.PROP_VALID)) {                  //NOI18N            
                bOK.setEnabled (optionsPanel.dataValid ());
            }
        }
        
        @SuppressWarnings("unchecked")
        public void actionPerformed (ActionEvent e) {
            if (!isOpen()) return; //WORKARROUND for some bug in NbPresenter
                // listener is called twice ...
            if (e.getSource () == bOK) {
                log.fine("Options Dialog - Ok pressed."); //NOI18N
                Dialog d = dialog;
                dialog = null;
                optionsPanel.save ();
                d.dispose ();
            } else
            if (e.getSource () == DialogDescriptor.CANCEL_OPTION ||
                e.getSource () == DialogDescriptor.CLOSED_OPTION
            ) {
                log.fine("Options Dialog - Cancel pressed."); //NOI18N
                Dialog d = dialog;
                dialog = null;
                optionsPanel.cancel ();
                bOK.setEnabled(true);
                d.dispose ();                
            } else
            if (e.getSource () == bClassic) {
                log.fine("Options Dialog - Classic pressed."); //NOI18N
                Dialog d = dialog;
                dialog = null;
                if (optionsPanel.isChanged ()) {
                    Confirmation confirmationDescriptor = new Confirmation (
                        loc ("CTL_Some_values_changed"), 
                        NotifyDescriptor.YES_NO_CANCEL_OPTION,
                        NotifyDescriptor.QUESTION_MESSAGE
                    );
                    Object result = DialogDisplayer.getDefault ().
                        notify (confirmationDescriptor);
                    if (result == NotifyDescriptor.YES_OPTION) {
                        optionsPanel.save ();
                        d.dispose ();                        
                    } else
                    if (result == NotifyDescriptor.NO_OPTION) {
                        optionsPanel.cancel ();
                        d.dispose ();                        
                    } else {
                        dialog = d;
                        return;
                    }
                } else {
                    d.dispose ();
                    optionsPanel.cancel ();
                }
                try {
                    CallableSystemAction a = SystemAction.get(OptionsAction.class);
                    a.putValue ("additionalActionName", loc ("CTL_Modern"));
                    a.putValue ("optionsDialogTitle", loc ("CTL_Classic_Title"));
                    a.putValue ("additionalActionListener", new OpenOptionsListener ()
                    );
                    a.performAction ();
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            } // classic
            else if (e.getSource() == btnExport) {
                OptionsChooserPanel.showExportDialog();
            } else if (e.getSource() == btnImport) {
                OptionsChooserPanel.showImportDialog();
            }
        }
    }
    
    private class MyWindowListener implements WindowListener {        
        private OptionsPanel optionsPanel;
        private Dialog originalDialog;

                
        MyWindowListener (OptionsPanel optionsPanel) {
            this.optionsPanel = optionsPanel;
            this.originalDialog = dialog;
        }
        
        public void windowClosing (WindowEvent e) {
            if (dialog == null) return;
            log.fine("Options Dialog - windowClosing "); //NOI18N
            optionsPanel.cancel ();
            bOK.setEnabled(true);
            if (this.originalDialog == dialog) {
                dialog = null;            
            }
        }

        public void windowClosed(WindowEvent e) {
            optionsPanel.storeUserSize();
            // store location of dialog
            NbPreferences.forModule(OptionsDisplayerImpl.class).putInt("OptionsX", originalDialog.getX());//NOI18N
            NbPreferences.forModule(OptionsDisplayerImpl.class).putInt("OptionsY", originalDialog.getY());//NOI18N
            if (optionsPanel.needsReinit()) {
                synchronized (lookupListener) {
                    descriptorRef = new WeakReference<DialogDescriptor>(null);
                }
            }
            if (this.originalDialog == dialog) {
                dialog = null;            
            }
            log.fine("Options Dialog - windowClosed"); //NOI18N
        }
        public void windowDeactivated (WindowEvent e) {}
        public void windowOpened (WindowEvent e) {}
        public void windowIconified (WindowEvent e) {}
        public void windowDeiconified (WindowEvent e) {}
        public void windowActivated (WindowEvent e) {}
    }
    
    class OpenOptionsListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            log.fine("Options Dialog - Back to modern."); //NOI18N
                            //OptionsDisplayerImpl.this.showOptionsDialog(null);
                            OptionsDisplayer.getDefault().open();
                        }
                    });
                }
            });
        }
    }
    
    private static class LookupListenerImpl implements LookupListener {
        public void resultChanged(LookupEvent ev) {
            synchronized (lookupListener) {
                descriptorRef = new WeakReference<DialogDescriptor>(null);
                // #156947 - close dialog when categories change
                if (dialog != null) {
                    dialog.setVisible(false);
                    dialog = null;
                }
            }
        }
        
    }

    /** 91106 - used to listen to default FS changes to update Advanced Options, Export and Import buttons. */
    private class DefaultFSListener implements FileChangeListener {

        public void fileRenamed(FileRenameEvent fe) {
            updateButtons();
        }

        public void fileChanged(FileEvent fe) {
            updateButtons();
        }

        public void fileFolderCreated(FileEvent fe) {
            updateButtons();
        }

        public void fileDataCreated(FileEvent fe) {
            updateButtons();
        }

        public void fileDeleted(FileEvent fe) {
            updateButtons();
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            updateButtons();
        }
    };
}

