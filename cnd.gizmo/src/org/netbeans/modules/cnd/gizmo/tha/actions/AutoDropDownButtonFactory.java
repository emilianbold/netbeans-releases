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
package org.netbeans.modules.cnd.gizmo.tha.actions;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import org.openide.awt.Actions;
import org.openide.awt.DropDownButtonFactory;

public final class AutoDropDownButtonFactory {

    private AutoDropDownButtonFactory() {
    }

    public static JButton createDropDownButton(final Icon icon, final JPopupMenu dropDownMenu, final Action action) {
        final JButton button = DropDownButtonFactory.createDropDownButton(icon, dropDownMenu);

        final MouseListener[] listeners = button.getMouseListeners();

        for (MouseListener l : listeners) {
            button.removeMouseListener(l);
        }

        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (!action.isEnabled()) {
                    return;
                }
                
                for (MouseListener l : listeners) {
                    l.mousePressed(e);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                for (MouseListener l : listeners) {
                    l.mouseClicked(e);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                for (MouseListener l : listeners) {
                    l.mouseExited(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                for (MouseListener l : listeners) {
                    l.mouseReleased(e);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!action.isEnabled()) {
                    return;
                }

                if (e.getSource() != button) {
                    return;
                }

                int dx = button.getWidth() - e.getX() - 1;
                e.translatePoint(dx, 0);
                for (MouseListener l : listeners) {
                    l.mousePressed(e);
                }
            }
        });

        Actions.connect(button, action);

        return button;
    }
}
