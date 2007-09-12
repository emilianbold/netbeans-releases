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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
