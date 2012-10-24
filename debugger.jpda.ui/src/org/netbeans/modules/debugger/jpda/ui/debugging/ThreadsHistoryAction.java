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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.DeadlockDetector.Deadlock;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.ui.models.DebuggingNodeModel;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;


public final class ThreadsHistoryAction extends AbstractAction {

    /** Creates a new instance of ThreadsHistoryAction */
    public ThreadsHistoryAction() {
        putValue(NAME, NbBundle.getMessage(ThreadsHistoryAction.class, "CTL_ThreadsHistoryAction"));
    }
    
    @Override
    public void actionPerformed(ActionEvent evt) {
        List<JPDAThread> threads = getThreads();
        int threadsCount = threads.size();
        if (threadsCount < 1) {
            Toolkit.getDefaultToolkit().beep();
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
                                releaseKey, triggerKey, true, true); // (evt.getModifiers() & KeyEvent.SHIFT_MASK) == 0
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
    
    public static SwitcherTableItem[] createSwitcherItems(List<JPDAThread> threads) {
        ThreadsListener threadsListener = ThreadsListener.getDefault();
        JPDADebugger debugger = threadsListener.getDebugger();
        JPDAThread currentThread = debugger != null ? debugger.getCurrentThread() : null;
        // collect all deadlocked threads
        Set<Deadlock> deadlocks = debugger != null ? debugger.getThreadsCollector().getDeadlockDetector().getDeadlocks()
                : Collections.EMPTY_SET;
        if (deadlocks == null) {
            deadlocks = Collections.EMPTY_SET;
        }
        Set<JPDAThread> deadlockedThreads = new HashSet<JPDAThread>();
        for (Deadlock deadlock : deadlocks) {
            deadlockedThreads.addAll(deadlock.getThreads());
        }
        
        SwitcherTableItem[] items = new SwitcherTableItem[threads.size()];
        int i = 0;
        for (JPDAThread thread : threads) {
            String name;
            try {
                name = DebuggingNodeModel.getDisplayName(thread, false);
                Method method = thread.getClass().getMethod("getDebugger"); // [TODO]
                JPDADebugger deb = (JPDADebugger)method.invoke(thread);
                method = deb.getClass().getMethod("getSession");
                Session session = (Session) method.invoke(deb);
                Session currSession = DebuggerManager.getDebuggerManager().getCurrentSession();
                if (session != currSession) {
                    String str = NbBundle.getMessage(ThreadsHistoryAction.class, "CTL_Session",
                            session.getName());
                    name = name.charAt(0) + str + ", " + name.substring(1);
                }
            } catch (Exception e) { // [TODO]
                name = thread.getName();
            }
            String htmlName = name;
            String description = ""; // tc.getToolTipText();
            Image image = ImageUtilities.loadImage(DebuggingNodeModel.getIconBase(thread));
            Icon icon = null;
            if (image != null) {
                boolean isCurrent = thread == currentThread;
                boolean isAtBreakpoint = threadsListener.isBreakpointHit(thread);
                boolean isInDeadlock = deadlockedThreads.contains(thread);
                icon = new ThreadStatusIcon(image, isCurrent, isAtBreakpoint, isInDeadlock);
            }
            items[i] = new SwitcherTableItem(
                    new ActivatableElement(thread),
                    name,
                    htmlName,
                    icon,
                    false,
                    description != null ? description : name
            );
            i++;
        }
        return items;
    }
    
    private static class ActivatableElement implements SwitcherTableItem.Activatable {
        
        JPDAThread thread;
        
        private ActivatableElement(JPDAThread thread) {
            this.thread = thread;
        }
        @Override
        public void activate() {
            thread.makeCurrent();
        }
    }
    
    public static List<JPDAThread> getThreads() {
        ThreadsListener threadsListener = ThreadsListener.getDefault();
        if (threadsListener == null) {
            return Collections.emptyList();
        }
        List<JPDAThread> history = threadsListener.getCurrentThreadsHistory();
        List<JPDAThread> allThreads = threadsListener.getThreads();
        Set<JPDAThread> hitsSet = new HashSet<JPDAThread>();
        for (JPDAThread hit : threadsListener.getHits()) {
            hitsSet.add(hit);
        }
        Set set = new HashSet(history);
        List<JPDAThread> result = new LinkedList<JPDAThread>();
        result.addAll(history);
        for (JPDAThread thread : allThreads) {
            if (!set.contains(thread) && thread.isSuspended()) {
                result.add(thread);
            }
        }
        if (result.size() > 1 && hitsSet.size() > 0) {
            int index = 1;
            int size = result.size();
            for (int x = 1; x < size; x++) {
                JPDAThread t = result.get(x);
                if (hitsSet.contains(t)) {
                    if (x > index) {
                        result.remove(x);
                        result.add(index, t);
                    }
                    index++;
                }
            } // for
        }
        return result;
    }
    
    private static class ThreadStatusIcon implements Icon {
        
        private Image image;
        private ImageIcon iconBase;
        private boolean isCurrent;
        private boolean isAtBreakpoint;
        private boolean isInDeadlock;

        ThreadStatusIcon(Image image, boolean isCurrent, boolean isAtBreakpoint, boolean isInDeadlock) {
            this.image = image;
            this.isCurrent = isCurrent;
            this.isAtBreakpoint = isAtBreakpoint;
            this.isInDeadlock = isInDeadlock;
            iconBase = new ImageIcon(image);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            int width = iconBase.getIconWidth();
            int height = iconBase.getIconHeight();
            Color primaryColor = null;
            Color secondaryColor = null;
            if (isInDeadlock) {
                primaryColor = DebuggingView.deadlockColor;
            } else if (isCurrent) {
                primaryColor = DebuggingView.greenBarColor;
            } else if (isAtBreakpoint) {
                primaryColor = DebuggingView.hitsBarColor;
            }
            if (isCurrent && isInDeadlock) {
                secondaryColor = DebuggingView.greenBarColor;
            }
            
            Color originalColor = g.getColor();
            g.setColor(c.getBackground());
            g.fillRect(x, y, width, height);
            g.drawImage(image, x + width, y, iconBase.getImageObserver());
            if (primaryColor != null) {
                g.setColor(primaryColor);
                g.fillRect(x, y, DebuggingView.BAR_WIDTH, height);
            }
            if (secondaryColor != null) {
                g.setColor(secondaryColor);
                int w = DebuggingView.BAR_WIDTH / 2 + 1;
                g.fillRect(x + DebuggingView.BAR_WIDTH - w, y, w, height);
            }
            g.setColor(originalColor);
        }

        @Override
        public int getIconWidth() {
            return 2 * iconBase.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return iconBase.getIconHeight();
        }
        
    }
    
}

