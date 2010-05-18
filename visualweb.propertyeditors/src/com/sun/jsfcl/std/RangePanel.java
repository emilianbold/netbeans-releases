/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
