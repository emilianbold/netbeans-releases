/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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


package org.netbeans.modules.debugger.jpda.ui.debugging;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.ui.models.DebuggingNodeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


public final class ThreadsHistoryAction extends AbstractAction {
    
    /** Creates a new instance of ThreadsHistoryAction */
    public ThreadsHistoryAction() {
        putValue(NAME, NbBundle.getMessage(ThreadsHistoryAction.class, "CTL_ThreadsHistoryAction"));
    }
    
    public void actionPerformed(ActionEvent evt) {
        List<JPDAThread> threads = getThreads();
        int threadsCount = threads.size();
        if (threadsCount < 1) {
            return;
        }
        
        if(!"immediately".equals(evt.getActionCommand()) && // NOI18N
                !(evt.getSource() instanceof javax.swing.JMenuItem)) {
            // #46800: fetch key directly from action command
            KeyStroke keyStroke = Utilities.stringToKey(evt.getActionCommand());
            
            if(keyStroke != null) {
                int triggerKey = keyStroke.getKeyCode();
                int releaseKey = 0;
                
                int modifiers = keyStroke.getModifiers();
                if((InputEvent.CTRL_MASK & modifiers) != 0) {
                    releaseKey = KeyEvent.VK_CONTROL;
                } else if((InputEvent.ALT_MASK & modifiers) != 0) {
                    releaseKey = KeyEvent.VK_ALT;
                } else if((InputEvent.META_MASK & modifiers) != 0) {
                    releaseKey = InputEvent.META_MASK;
                }
                
                if(releaseKey != 0) {
                    if (!KeyboardPopupSwitcher.isShown()) {
                        KeyboardPopupSwitcher.selectItem(
                                createSwitcherItems(threads),
                                releaseKey, triggerKey, (evt.getModifiers() & KeyEvent.SHIFT_MASK) == 0);
                    }
                    return;
                }
            }
        }
        
        if (threadsCount == 1) {
            threads.get(0).makeCurrent();
        } else {
            int index = (evt.getModifiers() & KeyEvent.SHIFT_MASK) == 0 ? 1 : threadsCount - 1;
            threads.get(index).makeCurrent();
        }
    }
    
    private SwitcherTableItem[] createSwitcherItems(List<JPDAThread> threads) {
        SwitcherTableItem[] items = new SwitcherTableItem[threads.size()];
        int i = 0;
        for (JPDAThread thread : threads) {
            String name;
            try {
                name = DebuggingNodeModel.getDisplayName(thread, false);
            } catch (UnknownTypeException e) {
                name = thread.getName();
            }
            String htmlName = name;
            Image image = Utilities.loadImage(DebuggingNodeModel.getIconBase(thread));
            String description = ""; // tc.getToolTipText();
            ImageIcon imageIcon = (image != null ? new ImageIcon(image) : null);
            items[i] = new SwitcherTableItem(
                    new ActivatableElement(thread),
                    name,
                    htmlName,
                    imageIcon,
                    false,
                    description != null ? description : name
            );
            i++;
        }
        return items;
    }
    
    private class ActivatableElement implements SwitcherTableItem.Activatable {
        
        JPDAThread thread;
        
        private ActivatableElement(JPDAThread thread) {
            this.thread = thread;
        }
        public void activate() {
            thread.makeCurrent();
        }
    }
    
    private List<JPDAThread> getThreads() {
        List<JPDAThread> history = ThreadsListener.getDefault().getCurrentThreadsHistory();
        List<JPDAThread> allThreads = ThreadsListener.getDefault().getThreads();
        Set set = new HashSet(history);
        List<JPDAThread> result = new ArrayList<JPDAThread>(allThreads.size());
        result.addAll(history);
        for (JPDAThread thread : allThreads) {
            if (!set.contains(thread) && thread.isSuspended()) {
                result.add(thread);
            }
        }
        return result;
    }
}

