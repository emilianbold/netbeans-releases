/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.visual.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.visual.RemoteScreenshot;
import org.netbeans.modules.debugger.jpda.visual.RemoteScreenshot.RetrievalException;
import org.netbeans.modules.debugger.jpda.visual.ui.ScreenshotComponent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;

/**
 * Grabs screenshot of remote application.
 * 
 * @author Martin Entlicher
 */
@ActionID(id = "org.netbeans.modules.debugger.jpda.visual.actions.ScreenshotGrabAction", category = "Debug")
@ActionRegistration(displayName = "CTL_ScreenshotGrabAction")
@ActionReference(path = "Menu/RunProject", position = 1850)
public class ScreenshotGrabAction extends AbstractAction implements Runnable, Presenter.Popup, Presenter.Menu {
    
    private static boolean isVisualDBG = Boolean.getBoolean("visualDebugger");
    
    private RequestProcessor rp = new RequestProcessor(ScreenshotGrabAction.class.getName(), 1);

    public ScreenshotGrabAction () {
        // When changed, update also mf-layer.xml, where are the properties duplicated because of Actions.alwaysEnabled()
        putValue (
            Action.NAME, 
            NbBundle.getMessage (ScreenshotGrabAction.class, "CTL_ScreenshotGrabAction")
        );
        //putValue("iconbase", "org/netbeans/modules/debugger/jpda/visual/resources/screenshot.png"); // NOI18N
    }
    
    @Override
    public boolean isEnabled() {
        DebuggerEngine engine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (engine != null) {
            JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
            return debugger != null;
        } else {
            return false;
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        rp.post(this);
    }
    
    @Override
    public void run() {
        try {
            final RemoteScreenshot[] screenshots = RemoteScreenshot.takeCurrent();
            for (int i = 0; i < screenshots.length; i++) {
                final RemoteScreenshot screenshot = screenshots[i];
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        ScreenshotComponent sc = new ScreenshotComponent(screenshot);
                        sc.open();
                        sc.requestActive();
                    }
                });
            }
        } catch (RetrievalException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        if (isVisualDBG) {
            addEngineListener();
            return new JMenuItem(this);
        } else {
            return null;
        }
    }

    @Override
    public JMenuItem getMenuPresenter() {
        if (isVisualDBG) {
            addEngineListener();
            return new JMenuItem(this);
        } else {
            return null;
        }
    }
    
    private void addEngineListener() {
        DebuggerManager.getDebuggerManager().addDebuggerListener(
                DebuggerManager.PROP_CURRENT_ENGINE,
                new DebuggerManagerAdapter() {
                    
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        firePropertyChange("enabled", null, null);
                    }
                });
    }
    
}
