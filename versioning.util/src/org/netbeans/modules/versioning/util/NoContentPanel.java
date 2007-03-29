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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.util;

import javax.swing.*;
import java.awt.BorderLayout;

/**
 * Centered label mostly used to indicate that there is nothing in a view.
 *
 * @author Maros Sandor
 */
public class NoContentPanel extends JPanel  {

    private JLabel label = new JLabel();

    public NoContentPanel(String text) {
        this();
        label.setText(text);
    }

    public NoContentPanel() {
        this.setBackground(UIManager.getColor("TextArea.background")); // NOI18N
        setLayout(new BorderLayout());
        label.setHorizontalAlignment(JLabel.CENTER);
        add(label, BorderLayout.CENTER);
        label.setEnabled(false);
    }
    
    public void setLabel(String text) {
        label.setText(text);
    }
}
