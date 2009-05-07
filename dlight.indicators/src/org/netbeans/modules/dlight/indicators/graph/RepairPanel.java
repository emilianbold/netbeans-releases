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
package org.netbeans.modules.dlight.indicators.graph;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.netbeans.modules.dlight.api.execution.ValidationStatus;
import org.netbeans.modules.dlight.util.UIUtilities;
import org.openide.util.NbBundle;

/**
 * @author Alexey Vladykin
 */
public class RepairPanel extends JPanel {

    private static final int MARGIN = 2;
    private final JEditorPane label;
    private JButton button;

    public RepairPanel(ValidationStatus status, ActionListener action) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createVerticalGlue());
        //String text = NbBundle.getMessage(RepairPanel.class, "RepairPanel.Label.Text");
        String text = status == null ?  NbBundle.getMessage(RepairPanel.class, "RepairPanel.Label.Text") : status.getReason();
      
        label = UIUtilities.createJEditorPane(text, false, GraphConfig.TEXT_COLOR);
        if (!status.isKnown()){
            label.setToolTipText(NbBundle.getMessage(RepairPanel.class, "RepairPanel.Label.Tooltip", text));//NOI18N
        }else{
            label.setToolTipText(text);
        }
        add(label);
        add(Box.createVerticalStrut(MARGIN));
        if (!status.isKnown()){
            button = new JButton(NbBundle.getMessage(RepairPanel.class, "RepairPanel.Repair.Text"));//NOI18N
            button.setAlignmentX(0.5f);
            button.addActionListener(action);
            add(button);            
        }
        add(Box.createVerticalGlue());
        
    }

    @Override
    public void doLayout() {
        Dimension size = new Dimension(getWidth(), Math.min(
                getHeight() - (button == null ? 0 : button.getPreferredSize().height) - MARGIN, label.getMinimumSize().height));
        label.setMaximumSize(size);
        label.setPreferredSize(size);
        super.doLayout();
    }
}
