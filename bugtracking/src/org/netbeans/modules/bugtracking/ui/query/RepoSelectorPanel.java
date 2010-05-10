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

package org.netbeans.modules.bugtracking.ui.query;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jdesktop.layout.Baseline;
import org.jdesktop.layout.GroupLayout;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import static org.jdesktop.layout.GroupLayout.BASELINE;
import static org.jdesktop.layout.GroupLayout.DEFAULT_SIZE;
import static org.jdesktop.layout.GroupLayout.PREFERRED_SIZE;
import static org.jdesktop.layout.LayoutStyle.RELATED;

/**
 * Part of the {@code RepoPanel}. Contains the combo-box for selection
 * of a bug-tracking repository, plus accessories
 * (label, button &quot;New&quot).
 *
 * @author Marian Petras
 */
public class RepoSelectorPanel extends JPanel implements FocusListener {

    private final JLabel repoSelectorLabel;
    private final JComponent repoSelector;
    private final JComponent newRepoButton;

    RepoSelectorPanel(JComponent repoSelector,
                      JComponent newRepoButton) {
        super(null);
        repoSelectorLabel = new JLabel();

        repoSelectorLabel.setLabelFor(repoSelector);
        repoSelectorLabel.setFocusCycleRoot(true);

        Mnemonics.setLocalizedText(
               repoSelectorLabel,
               NbBundle.getMessage(getClass(),
                                   "QueryTopComponent.repoLabel.text"));//NOI18N

        this.repoSelector = repoSelector;
        this.newRepoButton = newRepoButton;

        setOpaque(false);

        newRepoButton.addFocusListener(this);
        repoSelector.addFocusListener(this);

        GroupLayout layout;
        setLayout(layout = new GroupLayout(this));
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .add(repoSelectorLabel)
                        .addPreferredGap(RELATED)
                        .add(repoSelector)
                        .addPreferredGap(RELATED)
                        .add(newRepoButton));
        layout.setVerticalGroup(
                layout.createParallelGroup(BASELINE)
                        .add(repoSelectorLabel)
                        .add(repoSelector, DEFAULT_SIZE,
                                           DEFAULT_SIZE,
                                           PREFERRED_SIZE)
                        .add(newRepoButton));
    }

    /*
     * To make it work correctly with GroupLayout.
     */
    @Override
    public int getBaseline(int width, int height) {
        return getBaseline();
    }

    /*
     * To make it work correctly with GroupLayout.
     */
    public int getBaselineResizeBehaviorInt() {
        return Baseline.BRB_CONSTANT_ASCENT;
    }

    int getBaseline() {
        int baseline = max(Baseline.getBaseline(repoSelectorLabel),
                           Baseline.getBaseline(repoSelector),
                           Baseline.getBaseline(newRepoButton))
                       + getInsets().top;
        return baseline;
    }

    private static int max(int a, int b, int c) {
        return Math.max(a, Math.max(b, c));
    }

    @Override
    public void focusGained(FocusEvent e) {
        final Component c = e.getComponent();
        if (c instanceof JComponent) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    RepoSelectorPanel.this.scrollRectToVisible(c.getBounds());
                }
            });
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        //do nothing
    }

}
