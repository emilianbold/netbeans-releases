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


package org.netbeans.core.windows.actions;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ui.KeyboardPopupSwitcher;
import org.netbeans.swing.popupswitcher.SwitcherTableItem;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

/**
 * Invokes Recent View List
 *
 * @author  Marek Slama
 */
public final class RecentViewListAction extends AbstractAction
        implements PropertyChangeListener {
    
    /** Creates a new instance of RecentViewListAction */
    public RecentViewListAction() {
        putValue(NAME, NbBundle.getMessage(RecentViewListAction.class, "CTL_RecentViewListAction"));
        TopComponent.getRegistry().addPropertyChangeListener(
                WeakListeners.propertyChange(this, TopComponent.getRegistry()));
        updateEnabled();
    }
    
    public void actionPerformed(ActionEvent evt) {
        TopComponent[] documents = getRecentDocuments();
        
        if (documents.length < 2) {
            return;
        }
        
        if(!"immediately".equals(evt.getActionCommand()) && // NOI18N
                !(evt.getSource() instanceof javax.swing.JMenuItem)) {
            // #46800: fetch key directly from action command
            KeyStroke keyStroke = Utilities.stringToKey(evt.getActionCommand());
            
            if(keyStroke != null) {
                int triggerKey = keyStroke.getKeyCode();
                int reverseKey = KeyEvent.VK_SHIFT;
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
                                createSwitcherItems(documents),
                                releaseKey, triggerKey, (evt.getModifiers() & KeyEvent.SHIFT_MASK) == 0);
                    }
                    return;
                }
            }
        }
        
        int documentIndex = (evt.getModifiers() & KeyEvent.SHIFT_MASK) == 0 ? 1 : documents.length-1;
        TopComponent tc = documents[documentIndex];
        // #37226 Unmaximized the other mode if needed.
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        ModeImpl mode = (ModeImpl) wm.findMode(tc);
        if(mode != null && mode != wm.getCurrentMaximizedMode()) {
            wm.switchMaximizedMode(null);
        }
        
        tc.requestActive();
    }
    
    private SwitcherTableItem[] createSwitcherItems(TopComponent[] tcs) {
        SwitcherTableItem[] items = new SwitcherTableItem[tcs.length];
        for (int i = 0; i < tcs.length; i++) {
            TopComponent tc = tcs[i];
            String name = tc.getDisplayName();
            if (name == null || name.trim().length() == 0) {
                name = tc.getName();
            }
            String htmlName = tc.getHtmlDisplayName();
            if (htmlName == null) {
                htmlName = name;
            }
            Image image = tc.getIcon();
            String description = tc.getToolTipText();
            ImageIcon imageIcon = (image != null ? new ImageIcon(image) : null);
            items[i] = new SwitcherTableItem(
                    new ActivatableTC(tc),
                    name,
                    htmlName,
                    imageIcon,
                    false,
                    description != null ? description : name);
        }
        return items;
    }
    
    private class ActivatableTC implements SwitcherTableItem.Activatable {
        private WeakReference<TopComponent> wtc;
        private ActivatableTC(TopComponent tc) {
            this.wtc = new WeakReference<TopComponent>(tc);
        }
        public void activate() {
            TopComponent tc = wtc.get();
            if (tc != null) {
                tc.requestActive();
            }
        }
    }
    
    
    public void propertyChange(PropertyChangeEvent evt) {
        if(TopComponent.Registry.PROP_OPENED.equals(evt.getPropertyName())) {
            updateEnabled();
        }
    }
    
    /** Only here for fix #41477:, called from layer.xml:
     * For KDE on unixes, Ctrl+TAB is occupied by OS,
     * so we also register Ctrl+BACk_QUOTE as recent view list action shortcut.
     * For other OS's, Ctrl+TAB is the only default, because we create link
     * not pointing to anything by returning null
     */
    public static String getStringRep4Unixes() {
        if (Utilities.isUnix()) {
            return "Actions/Window/org-netbeans-core-windows-actions-RecentViewListAction.instance"; //NOI18N
        }
        return null;
    }
    
    /**
     * Update enable state of this action.
     */
    private void updateEnabled() {
        setEnabled(isMoreThanOneDocOpened());
    }
    
    private boolean isMoreThanOneDocOpened() {
        for(Iterator it = WindowManagerImpl.getInstance().getModes().iterator(); it.hasNext(); ) {
            ModeImpl mode = (ModeImpl)it.next(); {
                if (mode.getKind() == Constants.MODE_KIND_EDITOR)
                    return (mode.getOpenedTopComponents().size() > 1);
            }
        }
        return false;
    }
    
    private TopComponent[] getRecentDocuments() {
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        TopComponent[] documents = wm.getRecentViewList();
        
        List<TopComponent> docsList = new ArrayList<TopComponent>();
        for (int i = 0; i < documents.length; i++) {
            TopComponent tc = documents[i];
            if (tc == null) {
                continue;
            }
            ModeImpl mode = (ModeImpl)wm.findMode(tc);
            if (mode == null) {
                continue;
            }
            
            if (mode.getKind() == Constants.MODE_KIND_EDITOR) {
                docsList.add(tc);
            }
        }
        return docsList.toArray(new TopComponent[0]);
    }
}

