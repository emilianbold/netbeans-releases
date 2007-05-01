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
package org.netbeans.modules.sql.framework.ui.graph.view.impl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.openide.util.NbBundle;

/**
 * Displays the palette of available operators for the Collaboration Definition Editor.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class OperatorPaletteDialog extends JDialog implements ActionListener {

    /* flag on whether to show palette item names */
    private static boolean showName = true;

    private static Dimension minSize = null;    

    /* cancel button */
    private JButton mBtnCancel = new JButton(NbBundle.getMessage(OperatorPaletteDialog.class, "BTN_Close"));

    /* "Show name" check box */
    private JCheckBox mCheckName = new JCheckBox(NbBundle.getMessage(OperatorPaletteDialog.class, "CTL_ShowNames"), showName);

    /* selection (tabbed) panel */
    private OperatorSelectionPanel mPanel = null;

    /**
     * Constructs a new instance of OperatorPaletteDialog with the given Frame as parent
     * and the given panel as internal content.
     * 
     * @param aFrame parent Frame to block
     * @param sp OperatorSelectionPanel instance with operator selection controls
     */
    public OperatorPaletteDialog(Frame aFrame, OperatorSelectionPanel sp) {
        super(aFrame, true);

        mPanel = sp;

        setTitle(NbBundle.getMessage(OperatorPaletteDialog.class, "CTL_DialogTitle"));

        JPanel pane = new JPanel();

        pane.setLayout(new BorderLayout(10, 10));
        pane.add(sp, BorderLayout.CENTER);

        JPanel bp = new JPanel();
        bp.setLayout(new BorderLayout(10, 10));

        JPanel bp2 = new JPanel();
        mCheckName.addActionListener(this);
        mBtnCancel.addActionListener(this);

        bp2.add(mBtnCancel);
        bp.add(bp2, BorderLayout.EAST);
        bp.add(mCheckName, BorderLayout.WEST);

        pane.add(bp, BorderLayout.SOUTH);

        setContentPane(pane);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);
    }

    /**
     * Process the specified UI item action event.
     * 
     * @param e the action event
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {
        Object src = e.getSource();

        if (src == mCheckName) {
            showName = mCheckName.isSelected();
            mPanel.updateShowNames(showName);
            this.repaint();
            return;
        }

        this.setVisible(false);
    }

    /**
     * Show this dialog.
     */
    public void showDialog() {
        mPanel.updateShowNames(showName);
        if (minSize == null) {
            pack();
            minSize = getSize();
        } else {
            setSize(minSize);
        }
        setVisible(true);
    }

    /**
     * Show dialog in an appropriate position relative to the given component.
     * 
     * @param p component relative to which we will display dialog in an appropriate
     *        position (e.g., centered)
     */
    public void showDialog(Component p) {
        setLocationRelativeTo(p);
        showDialog();
    }
}

