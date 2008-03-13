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

import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * Displays the palette of available operators for the Collaboration Definition Editor.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class OperatorPaletteDialog extends JDialog implements ActionListener {

    /* flag on whether to show palette item names */
    private static boolean showName = true;
    private static transient final Logger mLogger = Logger.getLogger(OperatorPaletteDialog.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private static Dimension minSize = null;

    /* cancel button */
    String nbBundle = mLoc.t("BUND327: Close");
    private JButton mBtnCancel = new JButton(nbBundle.substring(15));

    /* "Show name" check box */
    String nbBundle1 = mLoc.t("BUND328: Show Names");
    private JCheckBox mCheckName = new JCheckBox(nbBundle1.substring(15), showName);

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

        String nbBundle2 = mLoc.t("BUND329: Operator Palette");
        setTitle(nbBundle2.substring(15));

        JPanel pane = new JPanel();

        pane.setLayout(new BorderLayout(10, 10));
        pane.add(sp, BorderLayout.CENTER);

        JPanel bp = new JPanel();
        bp.setLayout(new BorderLayout(10, 10));

        JPanel bp2 = new JPanel();
        mCheckName.addActionListener(this);
        mBtnCancel.addActionListener(this);
        mBtnCancel.getAccessibleContext().setAccessibleName(nbBundle.substring(15));
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

