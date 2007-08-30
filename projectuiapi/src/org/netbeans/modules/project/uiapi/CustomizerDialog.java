/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.uiapi;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/** Implementation of standard customizer dialog.
 *
 * @author Petr Hrebejk
 */
public class CustomizerDialog {

    /** Factory class only
     */
    private CustomizerDialog() {}

    // Option indexes
    private static final int OPTION_OK = 0;
    private static final int OPTION_CANCEL = OPTION_OK + 1;

    // Option command names
    private static final String COMMAND_OK = "OK";          // NOI18N
    private static final String COMMAND_CANCEL = "CANCEL";  // NOI18N

    public static Dialog createDialog( ActionListener okOptionListener, ActionListener storeListener, final CustomizerPane innerPane,
            HelpCtx helpCtx, final ProjectCustomizer.Category[] categories, 
           //#97998 related
            ProjectCustomizer.CategoryComponentProvider componentProvider ) {

        ListeningButton okButton = new ListeningButton(
                NbBundle.getMessage(CustomizerDialog.class, "LBL_Customizer_Ok_Option"), // NOI18N
                categories);
        okButton.setEnabled(CustomizerDialog.checkValidity(categories));

        // Create options
        JButton options[] = {
            okButton,
            new JButton( NbBundle.getMessage( CustomizerDialog.class, "LBL_Customizer_Cancel_Option" ) ) , // NOI18N
        };

        // Set commands
        options[ OPTION_OK ].setActionCommand( COMMAND_OK );
        options[ OPTION_CANCEL ].setActionCommand( COMMAND_CANCEL );

        //A11Y
        options[ OPTION_OK ].getAccessibleContext().setAccessibleDescription ( NbBundle.getMessage( CustomizerDialog.class, "AD_Customizer_Ok_Option") ); // NOI18N
        options[ OPTION_CANCEL ].getAccessibleContext().setAccessibleDescription ( NbBundle.getMessage( CustomizerDialog.class, "AD_Customizer_Cancel_Option") ); // NOI18N


        // RegisterListener
        ActionListener optionsListener = new OptionListener(okOptionListener, storeListener, categories , componentProvider);
        options[ OPTION_OK ].addActionListener( optionsListener );
        options[ OPTION_CANCEL ].addActionListener( optionsListener );

        innerPane.getAccessibleContext().setAccessibleName( NbBundle.getMessage( CustomizerDialog.class, "AN_ProjectCustomizer") ); //NOI18N
        innerPane.getAccessibleContext().setAccessibleDescription( NbBundle.getMessage( CustomizerDialog.class, "AD_ProjectCustomizer") ); //NOI18N

        if ( helpCtx == null ) {
            helpCtx = HelpCtx.DEFAULT_HELP;
        }

        DialogDescriptor dialogDescriptor = new DialogDescriptor(
            innerPane,                             // innerPane
            NbBundle.getMessage( CustomizerDialog.class, "LBL_Customizer_Title" ), // NOI18N // displayName
            false,                                  // modal
            options,                                // options
            options[OPTION_OK],                     // initial value
            DialogDescriptor.BOTTOM_ALIGN,          // options align
            helpCtx,                                // helpCtx
            null );                                 // listener

        innerPane.addPropertyChangeListener( new HelpCtxChangeListener( dialogDescriptor, helpCtx ) );
        if ( innerPane instanceof HelpCtx.Provider ) {
            dialogDescriptor.setHelpCtx( ((HelpCtx.Provider)innerPane).getHelpCtx() );
        }
        dialogDescriptor.setClosingOptions( new Object[] { options[ OPTION_OK ], options[ OPTION_CANCEL ] } );

        Dialog dialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );

        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                innerPane.clearPanelComponentCache();
                List<ProjectCustomizer.Category> queue = new LinkedList<ProjectCustomizer.Category>(Arrays.asList(categories));

                while (!queue.isEmpty()) {
                    ProjectCustomizer.Category category = queue.remove(0);

                    Utilities.removeCategoryChangeSupport(category);

                    if (category.getSubcategories() != null) {
                        queue.addAll(Arrays.asList(category.getSubcategories()));
                    }
                }
            }
        });
        
        return dialog;

    }

    /** Returns whether all given categories are valid or not. */
    private static boolean checkValidity(ProjectCustomizer.Category[] categories) {
        for (ProjectCustomizer.Category c : categories) {
            if (!c.isValid()) {
                return false;
            }
            ProjectCustomizer.Category[] subCategories = c.getSubcategories();
            if (subCategories != null) {
                if (!checkValidity(subCategories)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Listens to the actions on the Customizer's option buttons */
    private static class OptionListener implements ActionListener {

        private ActionListener okOptionListener;
        private ActionListener storeListener;
        private ProjectCustomizer.Category[] categories;
        private Lookup.Provider prov;

        OptionListener( ActionListener okOptionListener, ActionListener storeListener, ProjectCustomizer.Category[] categs, 
                ProjectCustomizer.CategoryComponentProvider componentProvider) {
            this.okOptionListener = okOptionListener;
            this.storeListener = storeListener;
            categories = categs;
            //#97998 related
            if (componentProvider instanceof Lookup.Provider) {
                prov = (Lookup.Provider)componentProvider;
            }
        }
        
        public void actionPerformed( final ActionEvent e ) {
            String command = e.getActionCommand();

            if ( COMMAND_OK.equals( command ) ) {
                // Call the OK option listener
                ProjectManager.mutex().writeAccess(new Mutex.Action<Object>() {
                    public Object run() {
                        okOptionListener.actionPerformed( e ); // XXX maybe create new event
                        actionPerformed(e, categories);
                        return null;
                    }
                });
                
                final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(CustomizerDialog.class, "LBL_Saving_Project_data_progress"));
                JComponent component = ProgressHandleFactory.createProgressComponent(handle);
                Frame mainWindow = WindowManager.getDefault().getMainWindow();
                final JDialog dialog = new JDialog(mainWindow, 
                        NbBundle.getMessage(CustomizerDialog.class, "LBL_Saving_Project_data"), true);
                SavingProjectDataPanel panel = new SavingProjectDataPanel(component);
                
                dialog.getContentPane().add(panel);
                dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                dialog.pack();
                
                Rectangle bounds = mainWindow.getBounds();
                int middleX = bounds.x + bounds.width / 2;
                int middleY = bounds.y + bounds.height / 2;
                Dimension size = dialog.getPreferredSize();
                dialog.setBounds(middleX - size.width / 2, middleY - size.height / 2, size.width, size.height);
                
                // Call storeListeners out of AWT EQ
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        try {
                            ProjectManager.mutex().writeAccess(new Mutex.Action<Object>() {
                                public Object run() {
                                    handle.start();
                                    if (storeListener != null) {
                                        storeListener.actionPerformed(e);
                                    }
                                    storePerformed(e, categories);
                                    // #97998 related
                                    saveModifiedProject();
                                    return null;
                                }
                            });
                        } finally {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    dialog.setVisible(false);
                                    dialog.dispose();
                                }
                            });
                        }
                    }
                });
                
                dialog.setVisible(true);
                
            }
        }
        
        private void actionPerformed(ActionEvent e, ProjectCustomizer.Category[] categs) {
            for (ProjectCustomizer.Category category : categs) {
                ActionListener list = category.getOkButtonListener();
                if (list != null) {
                    list.actionPerformed(e);// XXX maybe create new event
                }
                if (category.getSubcategories() != null) {
                    actionPerformed(e, category.getSubcategories());
                }
            }
        }
        
        private void storePerformed(ActionEvent e, ProjectCustomizer.Category[] categories) {
            for (ProjectCustomizer.Category category : categories) {
                ActionListener listener = category.getStoreListener();
                if (listener != null) {
                    listener.actionPerformed(e); // XXX maybe create new event
                }
                if (category.getSubcategories() != null) {
                    storePerformed(e, category.getSubcategories());
                }
            }
        }
        
        private void saveModifiedProject() {
            if (prov != null) {
                Project prj = prov.getLookup().lookup(Project.class);
                if (ProjectManager.getDefault().isModified(prj)) {
                    try {
                        ProjectManager.getDefault().saveProject(prj);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IllegalArgumentException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        
    }

    private static class HelpCtxChangeListener implements PropertyChangeListener {

        DialogDescriptor dialogDescriptor;
        HelpCtx defaultHelpCtx;

        HelpCtxChangeListener( DialogDescriptor dialogDescriptor, HelpCtx defaultHelpCtx ) {
            this.dialogDescriptor = dialogDescriptor;
            this.defaultHelpCtx = defaultHelpCtx;
        }

        public void propertyChange( PropertyChangeEvent evt ) {

            if ( CustomizerPane.HELP_CTX_PROPERTY.equals( evt.getPropertyName() ) ) {
                HelpCtx newHelp = (HelpCtx)evt.getNewValue();
                dialogDescriptor.setHelpCtx( newHelp == null  || newHelp == HelpCtx.DEFAULT_HELP  ? defaultHelpCtx : newHelp );
            }

        }

    }

    private static class ListeningButton extends JButton implements PropertyChangeListener {

        private ProjectCustomizer.Category[] categories;

        public ListeningButton(String label, ProjectCustomizer.Category[] categories) {
            super(label);
            this.categories = categories;
            for (ProjectCustomizer.Category c : categories) {
                Utilities.getCategoryChangeSupport(c).addPropertyChangeListener(this);
            }

        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == CategoryChangeSupport.VALID_PROPERTY) {
                boolean valid = (Boolean) evt.getNewValue();
                // enable only if all categories are valid
                setEnabled(valid && CustomizerDialog.checkValidity(categories));
            }
        }

    }

}
