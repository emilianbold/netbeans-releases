/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.ui.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.AbstractAction;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.ui.debugging.KeyboardPopupSwitcher;
import org.netbeans.modules.debugger.jpda.ui.debugging.ThreadsHistoryAction;

import org.openide.awt.Actions;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 *
 * @author martin
 */
public class SetCurrentThreadFromHistoryAction extends AbstractAction implements Presenter.Menu, Presenter.Popup {

    public SetCurrentThreadFromHistoryAction() {
        putValue(NAME, NbBundle.getMessage(ThreadsHistoryAction.class, "CTL_ThreadsHistoryAction"));
    }

    public void actionPerformed(ActionEvent e) {
        new ThreadsHistoryAction().actionPerformed(e);
    }

    public JMenuItem getMenuPresenter() {
        final JMenuItem item = new JMenuItem();
        Mnemonics.setLocalizedText(item, NbBundle.getMessage(SetCurrentThreadFromHistoryAction.class, "CTL_SetCurrentThreadFromHistoryAction"));
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showWindow();
            }
        });
        item.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ("ancestor".equals(evt.getPropertyName())) {
                    item.setEnabled(ThreadsHistoryAction.getThreads().size() > 0);
                }
            }
        });
        //item.setAccelerator(KeyStroke.getKeyStroke('T', InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
        return item;
    }

    public JMenuItem getPopupPresenter() {
        return getMenuPresenter();
    }

    private void showWindow() {
        List<JPDAThread> threads = ThreadsHistoryAction.getThreads();
        int threadsCount = threads.size();
        if (threadsCount < 1) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        int triggerKey = KeyEvent.VK_DOWN;
        int releaseKey = KeyEvent.VK_ENTER;
        KeyboardPopupSwitcher.selectItem(
                ThreadsHistoryAction.createSwitcherItems(threads),
                releaseKey, triggerKey, true, true);
    }

}
