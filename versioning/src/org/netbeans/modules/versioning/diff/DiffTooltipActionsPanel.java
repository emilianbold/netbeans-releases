/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.versioning.diff;

import org.netbeans.api.diff.Difference;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.openide.util.NbBundle;

/**
 * Contains Diff actions toolbar: Goto Previous, Goto Next, Rollback, Diff.
 * 
 * @author Maros Sandor
 */
class DiffTooltipActionsPanel extends JToolBar implements ActionListener {
    
    private final Icon iconPrevious = new ImageIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/versioning/diff/diff-prev.png")); // NOI18N
    private final Icon iconNext = new ImageIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/versioning/diff/diff-next.png")); // NOI18N
    private final Icon iconDiff = new ImageIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/versioning/diff/diff.png")); // NOI18N
    private final Icon iconRollback = new ImageIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/versioning/diff/rollback.png")); // NOI18N

    private final DiffActionTooltipWindow master;
    private final Difference              diff;
    
    private final JButton prevButton;
    private final JButton nextButton;
    private final JButton rollButton;
    private final JButton diffButton;

    public DiffTooltipActionsPanel(DiffActionTooltipWindow master, Difference diff) {
        this.master = master;
        this.diff = diff;

        Color tooltipBackround = UIManager.getColor("ToolTip.background"); // NOI18N
        if (tooltipBackround == null) tooltipBackround = Color.WHITE;
        
        setRollover(true);
        setFloatable(false);
        setBackground(tooltipBackround);

        prevButton = new JButton(iconPrevious);
        nextButton = new JButton(iconNext);
        rollButton = new JButton(iconRollback);
        diffButton = new JButton(iconDiff);
        
        prevButton.setToolTipText(NbBundle.getMessage(DiffTooltipActionsPanel.class, "TT_GoToPreviousDifference"));
        nextButton.setToolTipText(NbBundle.getMessage(DiffTooltipActionsPanel.class, "TT_GoToNextDifference"));
        diffButton.setToolTipText(NbBundle.getMessage(DiffTooltipActionsPanel.class, "TT_Open_Diff_Window"));
        if (diff.getType() == Difference.ADD) {
            rollButton.setToolTipText(NbBundle.getMessage(DiffTooltipActionsPanel.class, "TT_Delete_Added_Text"));
        } else if (diff.getType() == Difference.CHANGE) {
            rollButton.setToolTipText(NbBundle.getMessage(DiffTooltipActionsPanel.class, "TT_Replace_With_Original_Text"));
        } else {
            rollButton.setToolTipText(NbBundle.getMessage(DiffTooltipActionsPanel.class, "TT_Restore_Original_Text"));
        }
        
        prevButton.addActionListener(this);
        nextButton.addActionListener(this);
        rollButton.addActionListener(this);
        diffButton.addActionListener(this);

        prevButton.setBackground(tooltipBackround);
        nextButton.setBackground(tooltipBackround);
        rollButton.setBackground(tooltipBackround);
        diffButton.setBackground(tooltipBackround);

        add(prevButton);
        add(nextButton);
        add(rollButton);
        add(diffButton);

        Difference [] diffs = master.getMaster().getCurrentDiff();
        prevButton.setEnabled(diffs[0] != diff);
        nextButton.setEnabled(diffs[diffs.length - 1] != diff);
        rollButton.setEnabled(master.getMaster().canRollback(diff));
        
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == prevButton) {
            master.shutdown();
            master.getMaster().onPrevious(diff);
        } else if (e.getSource() == nextButton) {
            master.shutdown();
            master.getMaster().onNext(diff);
        } if (e.getSource() == rollButton) {
            master.shutdown();
            master.getMaster().onRollback(diff);
        } else if (e.getSource() == diffButton) {
            master.shutdown();
            master.getMaster().onDiff(diff);
        }
    }
}
