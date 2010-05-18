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

package org.netbeans.core.windows.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.Switches;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action perform undock or dock, either of given or active top component.
 * Undock means that TopCompoment is moved to new, separate floating window,
 * Dock means move into main window area.
 *
 */
public final class UndockWindowAction extends AbstractAction {

    private final TopComponent tc;

    /**
     * Creates instance of action to Undock/Dock of currently active top
     * component in the system. For use in main menu.
     */
    public UndockWindowAction () {
        this.tc = null;
    }

    /**
     * Undock/Dock of given TopComponent.
     * For use in the context menus.
     */
    public UndockWindowAction (TopComponent tc) {
        this.tc = tc;
    }
    
    public void actionPerformed (ActionEvent e) {
        // contextTC shound never be null thanks to isEnabled impl
        WindowManagerImpl wmi = WindowManagerImpl.getInstance();
        TopComponent contextTC = getTC2WorkWith();
        boolean isDocked = wmi.isDocked(contextTC);
        ModeImpl mode = (ModeImpl)wmi.findMode(contextTC);

        if (isDocked) {
            wmi.userUndockedTopComponent(contextTC, mode);
        } else {
            wmi.userDockedTopComponent(contextTC, mode);
        }
    }
    
    /** Overriden to share accelerator between instances of this action.
     */ 
    public void putValue(String key, Object newValue) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            ActionUtils.putSharedAccelerator("UndockWindowAction", newValue); //NOI18N
        } else {
            super.putValue(key, newValue);
        }
    }

    /** Overriden to share accelerator between instances of this action.
     */ 
    public Object getValue(String key) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            return ActionUtils.getSharedAccelerator("UndockWindowAction"); //NOI18N
        } else {
            return super.getValue(key);
        }
    }

    public boolean isEnabled() {
        updateName();
        return getTC2WorkWith() != null && Switches.isTopComponentUndockingEnabled()
                && Switches.isUndockingEnabled(getTC2WorkWith());
    }

    private void updateName() {
        TopComponent contextTC = getTC2WorkWith();
        boolean isDocked = contextTC != null ? WindowManagerImpl.getInstance().isDocked(contextTC) : true;
        putValue(Action.NAME,
                NbBundle.getMessage(UndockWindowAction.class,
                isDocked ? "CTL_UndockWindowAction" : "CTL_UndockWindowAction_Dock"));
    }

    private TopComponent getTC2WorkWith () {
        if (tc != null) {
            return tc;
        }
        return WindowManager.getDefault().getRegistry().getActivated();
    }

}
