/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.debugger.jpda.truffle.ast.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.options.TruffleOptions;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Manager of the ASTView.
 */
@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
public class ASTViewManager extends DebuggerManagerAdapter {

    private volatile boolean shouldOpenView;
    private volatile boolean isAtTruffleLocation;
    private final PropertyChangeListener propListenerHolder;    // Not to have the listener collected

    public ASTViewManager() {
        shouldOpenView = TruffleOptions.isLanguageDeveloperMode();
        propListenerHolder = propEvent -> {
            boolean develMode = TruffleOptions.isLanguageDeveloperMode();
            shouldOpenView = develMode;
            if (develMode) {
                openIfCan();
            } else {
                closeView();
            }
        };
        TruffleOptions.onLanguageDeveloperModeChange(propListenerHolder);
    }

    @Override
    public void sessionAdded(Session session) {
        JPDADebugger debugger = session.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) {
            return ;
        }
        debugger.addPropertyChangeListener(JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
    }

    @Override
    public void sessionRemoved(Session session) {
        JPDADebugger debugger = session.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) {
            return ;
        }
        debugger.removePropertyChangeListener(JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME.equals(evt.getPropertyName())) {
            CallStackFrame frame = (CallStackFrame) evt.getNewValue();
            if (frame != null) {
                CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(frame.getThread());
                isAtTruffleLocation = currentPCInfo != null;
                openIfCan();
            } else {
                isAtTruffleLocation = false;
            }
        }
    }

    private void openIfCan() {
        if (shouldOpenView && isAtTruffleLocation) {
            SwingUtilities.invokeLater(() -> {
                TopComponent tc = WindowManager.getDefault().findTopComponent(ASTView.AST_VIEW_NAME);
                tc.open();
                tc.requestVisible();
            });
            shouldOpenView = false;
        }
    }

    private void closeView() {
        SwingUtilities.invokeLater(() -> {
            TopComponent tc = WindowManager.getDefault().findTopComponent(ASTView.AST_VIEW_NAME);
            tc.close();
        });
    }
}
