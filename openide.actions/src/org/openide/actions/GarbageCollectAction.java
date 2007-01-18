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
package org.openide.actions;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CallableSystemAction;

import java.awt.*;
import java.awt.event.*;

import java.text.Format;
import java.text.MessageFormat;

import javax.swing.*;


// Toolbar presenter like MemoryMeterAction, except:
// 1. Does not have a mark etc.
// 2. But pressing it runs GC.
// 3. Slim profile fits nicely in the menu bar (at top level).
// 4. Displays textual memory usage directly, not via tooltip.
// Intended to be unobtrusive enough to leave on for daily use.

/**
 * Perform a system garbage collection.
 * @author Jesse Glick, Tim Boudreau
 */
public class GarbageCollectAction extends CallableSystemAction {
    public String getName() {
        return NbBundle.getBundle(GarbageCollectAction.class).getString("CTL_GarbageCollect"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(GarbageCollectAction.class);
    }

    public void performAction() {
        gc();
    }

    private static void gc() {
        // Can be slow, would prefer not to block on it.
        RequestProcessor.getDefault().post(
            new Runnable() {
                public void run() {
                    System.gc();
                    System.runFinalization();
                    System.gc();
                }
            }
        );
    }

    protected boolean asynchronous() {
        return false;
    }

    public Component getToolbarPresenter() {
        return new HeapViewWrapper();
//        return new MemButton();
    }

    private static final class HeapViewWrapper extends JComponent {
        public HeapViewWrapper() {
            add(new HeapView());
            setLayout(null);
        }
        
        public boolean isOpaque() {
            return false;
        }
        
        public Dimension getMinimumSize() {
            return calcPreferredSize();
        }

        public Dimension getPreferredSize() {
            return calcPreferredSize();
        }

        public Dimension getMaximumSize() {
            Dimension pref = calcPreferredSize();
            Container parent = getParent();
            if (parent != null && parent.getHeight() > 0) {
                pref.height = parent.getHeight();
            }
            return pref;
        }
        
        public Dimension calcPreferredSize() {
            Dimension pref = getHeapView().getPreferredSize();
            pref.height += 1;
            pref.width += 6;
            return pref;
        }

        public void layout() {
            int w = getWidth();
            int h = getHeight();
            HeapView heapView = getHeapView();
            heapView.setBounds(4, 2, w - 6, h - 4);
        }

        private HeapView getHeapView() {
            return (HeapView)getComponent(0);
        }
    }
    
}
