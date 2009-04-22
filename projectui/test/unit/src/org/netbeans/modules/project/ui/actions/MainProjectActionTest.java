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

package org.netbeans.modules.project.ui.actions;

import java.awt.EventQueue;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class MainProjectActionTest extends NbTestCase {
    private Prj prj;

    public MainProjectActionTest(String n) {
        super(n);
    }

    @Override
    public void setUp() {
        prj = new Prj();
    }

    @Override
    public void tearDown() {
    }

    public void testConstructedFromNonAWTThread() throws Exception {
        OpenProjects.getDefault().open(new Project[] { prj }, false);
        OpenProjects.getDefault().setMainProject(prj);
        assertEquals("My project is main", prj, OpenProjects.getDefault().getMainProject());

        assertFalse("Not in AWT thread yet", EventQueue.isDispatchThread());

        class R implements Runnable {
            Action a;

            public void init() {
                a = Actions.buildMainProject();
            }

            public void run() {
                assertTrue("Let the action be enabled", a.isEnabled());
            }

            public void finish() {

            }
        }

        R run = new R();
        run.init();
        assertEquals("No calls to the Project when outside of AWT", 0, prj.counter);
        EventQueue.invokeAndWait(run);
        assertEquals("One call to the Project from AWT", 2, prj.counter);
        run.finish();
    }

    public static final class Prj implements Project, ActionProvider {
        private FileObject fo;
        private Lookup lkp;

        int counter;

        public Prj() {
            fo = FileUtil.createMemoryFileSystem().getRoot();
            lkp = Lookups.fixed(this);
        }

        public FileObject getProjectDirectory() {
            return fo;
        }

        public Lookup getLookup() {
            return lkp;
        }

        public String[] getSupportedActions() {
            counter++;
            assertTrue("Need to be called from AWT thread", EventQueue.isDispatchThread());
            return new String[] { ActionProvider.COMMAND_BUILD };
        }

        public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
            assertTrue("Need to be called from AWT thread", EventQueue.isDispatchThread());
        }

        public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
            assertTrue("Need to be called from AWT thread", EventQueue.isDispatchThread());
            return true;
        }
    }
}