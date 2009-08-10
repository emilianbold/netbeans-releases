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
package org.netbeans.modules.dlight.tha;

import java.util.List;
import javax.swing.JComponent;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.ui.CallStackPanel;

/**
 * @author Alexey Vladykin
 */
public final class StackPanelFactory {

    public static JComponent newStackPanel(List<FunctionCall> stack) {
        return new CallStackPanel(stack);
//        JPanel panel = new JPanel();
//        GroupLayout layout = new GroupLayout(panel);
//        panel.setLayout(layout);
//
//        MouseListener mouseListener = new MouseAdapter() {
//            @Override
//            public void mouseEntered(MouseEvent e) {
//                ((JButton)e.getComponent()).setContentAreaFilled(true);
//            }
//            @Override
//            public void mouseExited(MouseEvent e) {
//                ((JButton)e.getComponent()).setContentAreaFilled(false);
//            }
//        };
//
//        List<JButton> buttons = new ArrayList<JButton>();
//        for (FunctionCall call : stack) {
//            JButton button = new JButton(call.getDisplayedName());
//            button.setBorder(BorderFactory.createEmptyBorder());
//            button.setContentAreaFilled(false);
//            button.setForeground(Color.BLUE);
//            button.addMouseListener(mouseListener);
//            buttons.add(button);
//        }
//
//        SequentialGroup verticalGroup = layout.createSequentialGroup();
//        for (int i = buttons.size() - 1; 0 <= i; --i) {
//            verticalGroup.add(buttons.get(i));
//        }
//        layout.setVerticalGroup(verticalGroup);
//
//        ParallelGroup horizontalGroup = layout.createParallelGroup();
//        for (int i = 0; i < buttons.size(); ++i) {
//            horizontalGroup.add(buttons.get(i));
//        }
//        layout.setHorizontalGroup(horizontalGroup);
//
//        return panel;
    }
}
