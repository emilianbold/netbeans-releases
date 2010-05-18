/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.ldap.browser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;
import org.openide.util.NbBundle;

/**
 *
 * @author anjeleevich
 */
public class ConnectingPanel extends JPanel {
    private JProgressBar progressBar;
    private JLabel label;

    public ConnectingPanel() {
        setBorder(new EmptyBorder(32, 32, 32, 32));
        setOpaque(true);
        setBackground(Color.WHITE);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        label = new JLabel(NbBundle.getMessage(ConnectingPanel.class, 
                "ConnectionPanel.label.text")); // NOI18N

        add(label);
        add(progressBar);
    }

    @Override
    public Dimension getPreferredSize() {
        synchronized (getTreeLock()) {
            Insets insets = getInsets();
            Dimension labelSize = label.getPreferredSize();
            Dimension progressSize = progressBar.getPreferredSize();

            Dimension result = new Dimension();

            result.width = insets.left + Math.max(PREFRERRED_WIDTH,
                    Math.max(labelSize.width, progressSize.width))
                    + insets.right;

            result.height = insets.top + labelSize.height + VGAP 
                    + progressSize.height + insets.bottom;

            return result;
        }
    }

    @Override
    public void doLayout() {
        synchronized (getTreeLock()) {
            Insets insets = getInsets();
            Dimension labelSize = label.getPreferredSize();
            Dimension progressSize = progressBar.getPreferredSize();

            int contentWidth = Math.max(PREFRERRED_WIDTH, Math
                    .max(labelSize.width, progressSize.width));
            int contentHeight = labelSize.height + VGAP + progressSize.height;

            int w = getWidth() - insets.left - insets.right;
            int h = getHeight() - insets.right - insets.top;

            int y = insets.top + Math.max(0, h - contentHeight >> 1);
            
            contentWidth = Math.max(Math.min(w, contentWidth), MIN_WIDTH);

            int x = insets.left + Math.max(0, w - contentWidth >> 1);

            label.setBounds(x, y, Math.min(labelSize.width, contentWidth), 
                    labelSize.height);

            y += labelSize.height + VGAP;

            progressBar.setBounds(x, y, contentWidth, progressSize.height);
        }
    }

    private static final int MIN_WIDTH = 50;
    private static final int PREFRERRED_WIDTH = 300;
    private static final int VGAP = 5;
}
