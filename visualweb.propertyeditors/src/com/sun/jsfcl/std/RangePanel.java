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
package com.sun.jsfcl.std;

import javax.swing.JPanel;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import com.sun.jsfcl.util.ComponentBundle;
import com.sun.rave.designtime.DesignProperty;

/**
 * @author octav
 */
public class RangePanel extends JPanel {
    protected boolean initializing = true;
    protected final RangePropertyEditor rpe;
    protected final DesignProperty prop; // for read-only referece only!

    private static final ComponentBundle bundle = ComponentBundle.getBundle(RangePanel.class);

    // UI elements
    private javax.swing.JTextField valueInput;
    private javax.swing.JLabel valueLabel;

    public RangePanel(RangePropertyEditor rpe, DesignProperty prop) {
        this.rpe = rpe;
        this.prop = prop;

        initView();
        valueInput.setText(rpe.getAsText());
        initializing = false;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initView() {
        java.awt.GridBagConstraints gridBagConstraints;

        valueLabel = new javax.swing.JLabel();
        valueInput = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        valueLabel.setText(bundle.getMessage("value")); //NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 36;
        gridBagConstraints.ipady = 5;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 0);
        add(valueLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 118;
        gridBagConstraints.insets = new java.awt.Insets(10, 20, 10, 10);
        add(valueInput, gridBagConstraints);

        valueInput.getDocument().addUndoableEditListener(new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent e) {
                if (initializing) {
                    return;
                }
                firePropertyChange(null, null, null);
                if (rpe != null) {
                    rpe.setAsText(valueInput.getText());
                }
            }
        });
    }
}
