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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.navigation.hierarchy;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.navigation.classhierarchy.ClassHierarchyPanel;
import org.netbeans.modules.cnd.navigation.includeview.IncludeHierarchyPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author Alexander Simon
 */
class HierarchyDialog {
    private static Rectangle lastBounds;
    private static boolean firstInvocation;
    static
    {
        Dimension dimensions = Toolkit.getDefaultToolkit().getScreenSize();
        lastBounds = new Rectangle(((dimensions.width / 2) - 200), ((dimensions.height / 2) - 300), 400, 600);
        firstInvocation = true;
    }

    /**
     * Show the hierarchy of the types.
     */
    public static void show(CsmClass decl) {
        if (decl != null) {
            ClassHierarchyPanel panel = new ClassHierarchyPanel(false);
            String title = decl.getName() + " - " + NbBundle.getMessage(HierarchyDialog.class, "CTL_ClassHierarchyDialogTitle"); // NOI18N
            Dialog dialog = createDialog(panel, title);
            panel.setClose(new DialogClose(panel));
            panel.setClass(decl);
            addKeyListeners(panel.getTreeView(), (ExplorerManager.Provider)panel);
            dialog.setVisible(true);
        }
    }    

    /**
     * Show the hierarchy of the includes.
     */
    public static void show(CsmFile decl) {
        if (decl != null) {
            IncludeHierarchyPanel panel = new IncludeHierarchyPanel(false);
            String title = decl.getName() + " - " + NbBundle.getMessage(HierarchyDialog.class, "CTL_IncludeHierarchyDialogTitle"); // NOI18N
            Dialog dialog = createDialog(panel, title);
            panel.setClose(new DialogClose(panel));
            panel.setFile(decl);
            addKeyListeners(panel.getTreeView(), (ExplorerManager.Provider)panel);
            dialog.setVisible(true);
        }
    }    
    
    private static void addKeyListeners(final BeanTreeView tree, final ExplorerManager.Provider panel){
// This is a fix context menu by Shift+F10.
// This code does not work right. Because pop up menu is shown after editor context menu.
//        tree.registerKeyboardAction(new ActionListener(){
//            public void actionPerformed(ActionEvent e) {
//                Node[] nodes = panel.getExplorerManager().getSelectedNodes();
//                    JPopupMenu popup = NodeOp.findContextMenu(nodes);
//                    if (popup != null){
//                        popup.show(tree, 10, 10);
//                    }
//            }
//        }, KeyStroke.getKeyStroke(KeyEvent.VK_F10,KeyEvent.SHIFT_MASK,true), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }
    
    private static JPanel createPanel(JPanel panel){
        JPanel outter = new JPanel();
        outter.setLayout(new java.awt.GridBagLayout());
        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(10, 10, 0, 10);
        JPanel border = new JPanel();
        border.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("SplitPane.shadow"))); // NOI18N
        outter.add(border, gridBagConstraints);
        border.setLayout(new java.awt.BorderLayout());
        border.add(panel, BorderLayout.CENTER);
        return outter;
    }
    
    private static Dialog createDialog(final JPanel panel, String title) {
        JButton okButton = new JButton (NbBundle.getMessage(HierarchyDialog.class, "CTL_OK")); // NOI18N
        DialogDescriptor dialogDescriptor = new DialogDescriptor(
            createPanel(panel), // innerPane
            title, // displayName
            true,
            new Object[] {okButton, DialogDescriptor.CANCEL_OPTION},
            okButton,
            DialogDescriptor.DEFAULT_ALIGN,
            HelpCtx.DEFAULT_HELP,
            new DialogButtonListener((ExplorerManager.Provider)panel, okButton));                                 // Action listener
        
        dialogDescriptor.setClosingOptions(new Object[] {okButton, DialogDescriptor.CANCEL_OPTION});
            
        Dialog dialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );
        
        if (firstInvocation) {
            Rectangle rect = WindowManager.getDefault().getMainWindow().getBounds();
            lastBounds.setLocation(rect.x + (rect.width-lastBounds.width)/2, rect.y + (rect.height-lastBounds.height)/2);
            firstInvocation = false;
        }
        dialog.setBounds(lastBounds);
        dialog.addWindowListener(new DialogBoundsKeeper(panel));
        return dialog;
    } 

    private static class DialogBoundsKeeper extends WindowAdapter{
        private JPanel panel;
        public DialogBoundsKeeper(JPanel panel){
            this.panel = panel;
        }

        @Override
        public void windowOpened(WindowEvent e) {
            panel.requestFocusInWindow();
        }

        @Override
        public void windowClosed(WindowEvent e) {
            lastBounds = e.getWindow().getBounds();
        }
    }
    
    private static class DialogButtonListener implements ActionListener {
        private ExplorerManager.Provider panel;
        private JButton okButton;
        
        public DialogButtonListener(ExplorerManager.Provider panel, JButton okButton) {
            this.panel = panel;
            this.okButton = okButton;
        }
        
        public void actionPerformed(ActionEvent e) {            
            if ( e.getSource() == okButton) {
                Node[] nodes = panel.getExplorerManager().getSelectedNodes();
                if (nodes.length > 0) {
                    Action action = nodes[0].getPreferredAction();
                    if (action != null) {
                        action.actionPerformed(null);
                        Window window = SwingUtilities.getWindowAncestor((Component)panel);
                        if (window != null) {
                            window.setVisible(false);
                        }
                    }
                }
            }
        }
    }   
    
    private static class DialogClose extends AbstractAction {
        private ExplorerManager.Provider panel;
        
        public DialogClose(ExplorerManager.Provider panel) {
            this.panel = panel;
        }
        
        public void actionPerformed(ActionEvent e) {
            Window window = SwingUtilities.getWindowAncestor((Component) panel);
            if (window != null) {
                window.setVisible(false);
            }
        }
    }
}
