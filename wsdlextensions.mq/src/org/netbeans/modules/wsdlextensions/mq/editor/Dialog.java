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
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.wsdlextensions.mq.editor;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * Abstract, modal dialog for MQ Configuration Editor needs.  Concrete
 * implementations provide the view (the 'form' to display in the dialog) and
 * commit/cancel semantics, and this class takes care of the rest.
 *
 * @author Noel.Ang@sun.com
 */
abstract class Dialog extends JDialog {
    private Form form;
    private JComponent[] buttons;

    /**
     * Creates a General Dialog.
     *
     * @param title Value to display in the dialog's title bar.
     * @param form User view/model
     *
     * @throws java.awt.HeadlessException if {@link java.awt.GraphicsEnvironment#isHeadless()}
     * returns true
     */
    protected Dialog(String title, Form form) throws HeadlessException {
        super((JFrame) null, title, true);
        if (form == null) {
            throw new NullPointerException("view");
        }
        this.form = form;
        init();
    }

    public Form getForm() {
        return form;
    }
    
    @Override
    public void pack() {
        super.pack();
        Utils.equalizeSizes(buttons);
    }

    /**
     * This method is called when the "OK" action is performed, signaling to the
     * implementation to perform whatever commit semantic is applicable.
     */
    protected abstract void commit();

    /**
     * This method is called when the "Cancel" action is performed, signaling to
     * the implementation to perform whatever rollback semantic is applicable.
     */
    protected abstract void cancel();

    private void init() {
        // Layout:
        // The user content will occupy the entire dialog frame except
        // a minimal horizontal area at the bottom of the dialog to house
        // the "OK" and "Cancel" buttons of the dialog.
        Container container = getContentPane();
        container.setLayout(new GridBagLayout());
        GridBagConstraints cons = new GridBagConstraints();
        cons.gridwidth = GridBagConstraints.REMAINDER;
        cons.insets = new Insets(2, 4, 2, 4);

        // User content:
        cons.anchor = GridBagConstraints.CENTER;
        cons.fill = GridBagConstraints.BOTH;
        cons.weightx = 1.0;
        cons.weighty = 1.0;
        container.add(getForm().getComponent(), cons);

        // Separator between user content and the buttons:
        cons.anchor = GridBagConstraints.CENTER;
        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.weightx = 1.0;
        cons.weighty = 0;
        container.add(new JSeparator(), cons);

        // The buttons' panel:
        JPanel panel = new JPanel();
        cons.anchor = GridBagConstraints.CENTER;
        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.weightx = 1.0;
        cons.weighty = 0;
        container.add(panel, cons);

        // The buttons:
        Action cancelAction = new CancelAction();
        JButton okButton = new JButton(new OkAction());
        JButton cancelButton = new JButton(cancelAction);
        org.openide.awt.Mnemonics.setLocalizedText(okButton,
                org.openide.util.NbBundle.getMessage(Dialog.class,
                        "Dialog.OK")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(cancelButton,
                org.openide.util.NbBundle.getMessage(Dialog.class,
                        "Dialog.CANCEL")); // NOI18N
        buttons = new JComponent[]{
                okButton, cancelButton,
        };
        panel.setLayout(new GridBagLayout());
        cons.gridwidth = GridBagConstraints.RELATIVE;
        cons.gridheight = GridBagConstraints.REMAINDER;
        cons.fill = GridBagConstraints.NONE;
        cons.weightx = cons.weighty = 0.0;
        cons.insets = new Insets(2, 2, 2, 2);
        for (JComponent button : buttons) {
            panel.add(button, cons);
        }
        
        getRootPane().setDefaultButton(okButton);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE,
                        0,
                        true
                ), "dismiss"
                );
        getRootPane().getActionMap().put("dismiss", cancelAction);
    }

    /** OK button handler */
    private class OkAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            commit();
            setVisible(false);
        }
    }

    /** Cancel button handler */
    private class CancelAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            cancel();
            setVisible(false);
        }
    }
}
