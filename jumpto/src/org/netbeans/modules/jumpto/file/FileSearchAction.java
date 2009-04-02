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
 * The Original Software is the Accelerators module.
 * The Initial Developer of the Original Software is Andrei Badea.
 * Portions Copyright 2005-2006 Andrei Badea.
 * All Rights Reserved.
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
        dialog.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FileSearchAction.class, "AN_FileSearchDialog"));
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FileSearchAction.class, "AD_FileSearchDialog"));
                
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
        if (dialog != null){
            dialog.setVisible( false );
        }
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
