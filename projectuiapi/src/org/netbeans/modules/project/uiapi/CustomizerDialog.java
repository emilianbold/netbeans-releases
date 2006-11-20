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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

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

    public static Dialog createDialog( ActionListener okOptionListener, JPanel innerPane,
            HelpCtx helpCtx, final ProjectCustomizer.Category[] categories ) {

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
        ActionListener optionsListener = new OptionListener( okOptionListener, categories );
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
        private ProjectCustomizer.Category[] categories;

        OptionListener( ActionListener okOptionListener, ProjectCustomizer.Category[] categs) {
            this.okOptionListener = okOptionListener;
            categories = categs;
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
            }
        }
        
        private void actionPerformed(ActionEvent e, ProjectCustomizer.Category[] categs) {
            for (int i = 0; i < categs.length; i++) {
                ActionListener list = categs[i].getOkButtonListener();
                if (list != null) {
                    list.actionPerformed(e);// XXX maybe create new event
                }
                if (categs[i].getSubcategories() != null) {
                    actionPerformed(e, categs[i].getSubcategories());
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
