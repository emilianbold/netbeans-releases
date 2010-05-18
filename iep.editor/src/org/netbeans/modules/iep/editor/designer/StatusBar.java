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

package org.netbeans.modules.iep.editor.designer;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import org.openide.util.NbBundle;

/**
 * This class does something useful.
 *
 * @author Bing Lu
 */
public class StatusBar extends JPanel {
        
    private JLabel mLabel;
    private JLabel mDirtyLabel = new JLabel("");
    private JLabel mLockedLabel = new JLabel("");
    private JPanel mDirtyLockedPanel;
    
    public StatusBar() {
        super();
        mDirtyLockedPanel = new JPanel();
        mDirtyLockedPanel.setLayout(new GridLayout(1, 2));
        mLabel = new JLabel(" ");
        mDirtyLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        mLockedLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        mLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        mDirtyLockedPanel.add(mDirtyLabel);
        mDirtyLockedPanel.add(mLockedLabel);
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, mLabel);
        add(BorderLayout.EAST, mDirtyLockedPanel);
    }
    
    public void setText(String s) {
        mLabel.setText(" " + s);
        validate();
        repaint();
    }
    
    public void setLocked(boolean b) {
        if (b) {
            mLockedLabel.setText(NbBundle.getMessage(StatusBar.class,"StatusBar.R_W"));
        } else {
            mLockedLabel.setText(NbBundle.getMessage(StatusBar.class,"StatusBar.R"));
        }
        validate();
        repaint();
    }
    
    public void setDirty(boolean b) {
        if (b) {
            mDirtyLabel.setText(NbBundle.getMessage(StatusBar.class,"StatusBar.MODIFIED"));
        } else {
            mDirtyLabel.setText("");
        }
        validate();
        repaint();
    }
}
