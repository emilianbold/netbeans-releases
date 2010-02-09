/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.progress.ui;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JFrame;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.api.progress.ProgressRunnable;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.junit.MockServices;
import org.openide.windows.Workspace;

/**
 *
 * @author Tim Boudreau
 */
public class RunOffEDTImplTest {

    public RunOffEDTImplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        MockServices.setServices(WM.class, RunOffEDTImpl.class);
    }

    @After
    public void tearDown() {
    }

    private static boolean canTestGlassPane() {
        Map<?,?> hintsMap = (Map)(Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints")); //NOI18N
        //Avoid translucent painting on, for example, remote X terminal
        return hintsMap == null || !RenderingHints.VALUE_TEXT_ANTIALIAS_OFF.equals(hintsMap.get(RenderingHints.KEY_TEXT_ANTIALIASING));
    }

    @Test
    public void testShowProgressDialogAndRun_3args_1_EQ() throws Exception {
        EventQueue.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                testShowProgressDialogAndRun_3args_1();
            }
        });
    }

    @Test
    public void testShowProgressDialogAndRun_3args_2_EQ() throws Exception {
        EventQueue.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                testShowProgressDialogAndRun_3args_2();
            }
        });
    }

    @Test
    public void testShowProgressDialogAndRun_3args_1() {
        assertEquals ("Done", ProgressUtils.showProgressDialogAndRun(new CB(), "Doing Stuff", true));
    }

    @Test
    public void testShowProgressDialogAndRun_3args_2() {
        final JFrame jf = (JFrame) WindowManager.getDefault().getMainWindow();
        class R implements Runnable {
            boolean hasRun;
            public void run() {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (canTestGlassPane()) {
                    assertTrue (jf.getGlassPane() instanceof RunOffEDTImpl.TranslucentMask);
                }
                hasRun = true;
            }
        };
        R r = new R();
        ProgressUtils.showProgressDialogAndRun(r, "Something");
        assertTrue (r.hasRun);
    }

    private class CB implements ProgressRunnable<String> {

        @Override
        public String run(ProgressHandle handle) {
            handle.switchToDeterminate(5);
            for (int i= 0; i < 5; i++) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                handle.progress("Job " + i, i);
            }
            return "Done";
        }

    }

    public static final class WM extends WindowManager {
        private final JFrame jf = new JFrame ("Main Window");
        public WM() {
            jf.setVisible(true);
        }

        @Override
        public Mode findMode(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Mode findMode(TopComponent tc) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Set<? extends Mode> getModes() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Frame getMainWindow() {
            return jf;
        }

        @Override
        public void updateUI() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected Component createTopComponentManager(TopComponent c) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Workspace createWorkspace(String name, String displayName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Workspace findWorkspace(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Workspace[] getWorkspaces() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setWorkspaces(Workspace[] workspaces) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Workspace getCurrentWorkspace() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public TopComponentGroup findTopComponentGroup(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void topComponentOpen(TopComponent tc) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void topComponentClose(TopComponent tc) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void topComponentRequestActive(TopComponent tc) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void topComponentRequestVisible(TopComponent tc) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void topComponentDisplayNameChanged(TopComponent tc, String displayName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void topComponentHtmlDisplayNameChanged(TopComponent tc, String htmlDisplayName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void topComponentToolTipChanged(TopComponent tc, String toolTip) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void topComponentIconChanged(TopComponent tc, Image icon) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void topComponentActivatedNodesChanged(TopComponent tc, Node[] activatedNodes) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected boolean topComponentIsOpened(TopComponent tc) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected Action[] topComponentDefaultActions(TopComponent tc) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected String topComponentID(TopComponent tc, String preferredID) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public TopComponent findTopComponent(String tcID) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
