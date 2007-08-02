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
 * The Original Software is the Accelerators module.
 * The Initial Developer of the Original Software is Andrei Badea.
 * Portions Copyright 2005-2006 Andrei Badea.
 * All Rights Reserved.
 *
 * Contributor(s): Andrei Badea
 *                 Petr Hrebejk
 */

package org.netbeans.modules.jumpto.file;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
/**
 *
 * @author Andrei Badea, Petr Hrebejk
 */
public class FileSearchAction extends AbstractAction {
    
    private Dialog dialog;
    private JButton openBtn;
    private FileSearchPanel panel;
    
    public FileSearchAction() {
        super( NbBundle.getMessage(FileSearchAction.class, "CTL_FileSearchAction") );
        // XXX this should be in initialize()?
        putValue("PopupMenuText", NbBundle.getBundle(FileSearchAction.class).getString("editor-popup-CTL_FileSearchAction")); // NOI18N
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public boolean isEnabled() {
        return OpenProjects.getDefault().getOpenProjects().length > 0;
    }
    
    
    
    public void actionPerformed(ActionEvent arg0) {
    
        openBtn = new JButton();
        openBtn.setEnabled( false );
        Mnemonics.setLocalizedText(openBtn, NbBundle.getMessage(FileSearchAction.class, "CTL_Open"));
        JButton selectInPrjBtn = null;
        
        Object[] buttons;
        buttons = new Object[] { openBtn, DialogDescriptor.CANCEL_OPTION};
        
        String title = NbBundle.getMessage(FileSearchAction.class, "MSG_FileSearchDlgTitle");
        panel = new FileSearchPanel(this);
        DialogDescriptor d = new DialogDescriptor(panel, title, true, buttons, openBtn, DialogDescriptor.DEFAULT_ALIGN, null, new DialogButtonListener(panel));
        d.setClosingOptions(new Object[] {openBtn, DialogDescriptor.CANCEL_OPTION});
        dialog = DialogDisplayer.getDefault().createDialog(d);
                
        // Set size
        dialog.setPreferredSize( new Dimension(  FileSearchOptions.getWidth(),
                                                 FileSearchOptions.getHeight() ) );
        
        // Center the dialog after the size changed.
        Rectangle r = Utilities.getUsableScreenBounds();
        int maxW = (r.width * 9) / 10;
        int maxH = (r.height * 9) / 10;
        Dimension dim = dialog.getPreferredSize();
        dim.width = Math.min(dim.width, maxW);
        dim.height = Math.min(dim.height, maxH);
        dialog.setBounds(Utilities.findCenterBounds(dim));
                
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                cleanup();
            }
        });
        
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                dialog.setVisible(true);
            }
        } );
    }
    
    /** For original of this code look at:
     *  org.netbeans.modules.project.ui.actions.ActionsUtil
     */
    public static Project findCurrentProject( ) {
        Lookup lookup = Utilities.actionsGlobalContext();

        // Maybe the project is in the lookup
        for (Project p : lookup.lookupAll(Project.class)) {
            return p;
        }
        // Now try to guess the project from dataobjects
        for (DataObject dObj : lookup.lookupAll(DataObject.class)) {
            FileObject fObj = dObj.getPrimaryFile();
            Project p = FileOwnerQuery.getOwner(fObj);
            if ( p != null ) {
                return p;
            }
        }
        
        return null;
    }
    
    public void closeDialog() {
        dialog.setVisible( false );
        cleanup();
    }
    
    JButton getOpenButton() {
        return openBtn;
    }

    private void cleanup() {
        
        FileSearchOptions.flush();
        
        if (panel != null ) { // Closing event for some reson sent twice
            panel.cleanup();
            panel = null;
        }
        
        if (dialog != null ) { // Closing event for some reson sent twice
            dialog.dispose();
            dialog = null;
            
        }
    }
    
    private class DialogButtonListener implements ActionListener {
        
        private FileSearchPanel panel;
        
        public DialogButtonListener(FileSearchPanel panel) {
            this.panel = panel;
        }
        
        public void actionPerformed(ActionEvent e) {       
            if ( e.getSource() == openBtn) {
                panel.openSelectedItems();
            }
        }
        
    }
    
}
