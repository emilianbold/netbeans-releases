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

package org.netbeans.modules.soa.mapper.common.palette;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

/**
 * Class PaletteDialog is used for displaying the Functoid Palette Dialog
 * for collaboration editors.
 *
 *
 * @author Tientien Li
 */
class PaletteDialog
        extends javax.swing.JDialog
        implements java.awt.event.ActionListener {

    /**
     * the cancel button
     */
    private javax.swing.JButton mBtnCancle = new javax.swing.JButton(
        PaletteManager.getBundle().getString("CTL_Close"));

    /**
     * the Palette Panel
     */
    private PalettePanel mPanel = null;

    /**
     * the showName check box
     */
    private javax.swing.JCheckBox mCheckName = new javax.swing.JCheckBox(
        PaletteManager.getBundle().getString("CTL_ShowNames"), true);

    /**
     * show palette item names
     */
    private boolean mShowName = true;

    /**
     * show Dialog
     *
     *
     * @param p component to display next to
     *
     */
    public void showDialog(java.awt.Component p) {
        setLocationRelativeTo(p);
        setVisible(true);
    }

    /**
     * show Dialog
     */
    public void showDialog() {
        setVisible(true);
    }

    /**
     * Constructor PaletteDialog
     *
     *
     * @param aFrame the parent frame
     * @param pp the palette panel
     *
     */
    public PaletteDialog(java.awt.Frame aFrame, PalettePanel pp) {

        super(aFrame, true);

        mPanel = pp;
        setTitle(PaletteManager.getBundle().getString("CTL_DialogTitle"));

        JPanel pane = new JPanel();

        pane.setLayout(new BorderLayout(10, 10));
        pane.add(pp, BorderLayout.CENTER);

        JPanel bp = new JPanel();
        bp.setLayout(new BorderLayout(10, 10));

        JPanel bp2 = new JPanel();
        mCheckName.addActionListener(this);

//        mBtnSave.setEnabled(false);
//        mBtnSave.addActionListener(this);
        mBtnCancle.addActionListener(this);
//        bp2.add(mBtnSave);
        bp2.add(mBtnCancle);
        bp.add(bp2, BorderLayout.EAST);
        bp.add(mCheckName, BorderLayout.WEST);

        pane.add(bp, BorderLayout.SOUTH);
        int w = 400;
        int h = 300;
        setSize(w, h);
        setContentPane(pane);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        Dimension dim = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int x = (dim.width - w) / 2;
        int y = (dim.height - h) / 2;
        setLocation(x, y);

        addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent we) {
                setVisible(false);
            }
        });

    }

    /**
     * Process the specified UI item action event.
     *
     * @param e the action event
     *
     */
    public void actionPerformed(java.awt.event.ActionEvent e) {

        Object src = e.getSource();

        if (src == mCheckName) {
            mShowName = mCheckName.isSelected();
            mPanel.updateShowNames(mShowName);
            //typedText = "save...";
            return;

        } else {

            //typedText = null;
        }

        this.setVisible(false);
    }
}
