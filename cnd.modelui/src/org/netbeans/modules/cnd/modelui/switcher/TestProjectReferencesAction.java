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
package org.netbeans.modules.cnd.modelui.switcher;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.trace.TraceXRef;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Vladirmir Voskresensky
 */
public class TestProjectReferencesAction extends NodeAction {

    private CsmModel model;
    private static boolean running = false;
    private final JMenuItem presenter;
    private final static boolean TEST_XREF = Boolean.getBoolean("test.xref.action"); // NOI18N

    private enum State {

        Enabled, Disabled, Indeterminate
    }

    public TestProjectReferencesAction() {
        presenter = new JMenuItem();
        model = CsmModelAccessor.getModel();
        org.openide.awt.Actions.connect(presenter, (Action) this, true);
    }

    protected boolean enable(Node[] activatedNodes) {
        if (!TEST_XREF) {
            return false;
        }
        if (model == null) {
            return false;
        }
        if (running) {
            return false;
        }
        Collection<NativeProject> projects = getNativeProjects(getActivatedNodes());
        if (projects == null) {
            return false;
        }
        return getState(projects) != State.Indeterminate;
    }
    public void performAction(final Node[] activatedNodes) {
        running = true;
        model.enqueue(new Runnable() {

            public void run() {
                try {
                    performAction(getNativeProjects(getActivatedNodes()));
                } finally {
                    running = false;
                }
            }
        }, "Testing xRef"); //NOI18N
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    public String getName() {
        return NbBundle.getMessage(getClass(), ("CTL_TestProjectReferencesAction")); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return getPresenter();
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return getPresenter();
    }

    private JMenuItem getPresenter() {
        final Collection<NativeProject> projects = getNativeProjects(getActivatedNodes());
        if (TEST_XREF) {
            if (projects == null) {
                this.setEnabled(!running);
                presenter.setVisible(false);
            } else {
                try {
                    presenter.setVisible(true);
                    this.setEnabled(!running);
                } catch (Throwable thr) {
                    // we are in awt thread;
                    // if exception occurs here, it doesn't allow even to close the project!
                    thr.printStackTrace();
                    this.setEnabled(false);
                }
            }
        } else {
            presenter.setVisible(false);
        }

        return presenter;
    }

    /** 
     * Gets the collection of native projects that correspond the given nodes.
     * @return in the case all nodes correspond to native projects -
     * collection of native projects; otherwise null
     */
    private Collection<NativeProject> getNativeProjects(Node[] nodes) {
        Collection<NativeProject> projects = new ArrayList<NativeProject>();
        for (int i = 0; i < nodes.length; i++) {
            Object o = nodes[i].getValue("Project"); // NOI18N 
            if (!(o instanceof Project)) {
                return null;
            }
            NativeProject nativeProject = (NativeProject) ((Project) o).getLookup().lookup(NativeProject.class);
            if (nativeProject == null) {
                return null;
            }
            projects.add(nativeProject);
        }
        return projects;
    }

    private State getState(Collection<NativeProject> projects) {
        if (model == null) {
            return State.Indeterminate;
        }
        State state = State.Indeterminate;
        for (NativeProject p : projects) {
            State curr = getState(p);
            if (state == State.Indeterminate) {
                state = curr;
            } else {
                if (state != curr) {
                    return State.Indeterminate;
                }
            }
        }
        return state;
    }

    private State getState(NativeProject p) {
        CsmProject csmPrj = model.getProject(p);
        return csmPrj != null && csmPrj.isStable(null) ? State.Enabled : State.Disabled;
    }

    private void performAction(Collection<NativeProject> projects) {
        if (projects != null) {
            for (NativeProject p : projects) {
                testProject(p);
            }
        }
    }

    
    private void testProject(NativeProject p) {
        String task = "xRef - " + p.getProjectDisplayName(); // NOI18N
        InputOutput io = IOProvider.getDefault().getIO(task, false);
        io.select();
        final ProgressHandle handle = ProgressHandleFactory.createHandle(task);
        handle.start();
        final PrintWriter out = io.getOut();
        final long[] time = new long[2];
        time[0] = System.currentTimeMillis();
        TraceXRef.traceProjectRefsStatistics(p, out, new CsmProgressAdapter() {
            private int handled = 0;
            @Override
            public void projectFilesCounted(CsmProject project, int filesCount) {
                out.println("Project " + project.getName() + " has " + filesCount + " files"); // NOI18N
                handle.switchToDeterminate(filesCount);
            }

            @Override
            public void fileParsingStarted(CsmFile file) {
                handle.progress("Analyzing " + file.getName(), handled++); // NOI18N
            }

            @Override
            public void projectParsingFinished(CsmProject project) {
                time[1] = System.currentTimeMillis();
            }
        });
        handle.finish();
        out.println("Analyzing " + p.getProjectDisplayName() + " took " + (time[1]-time[0]) + "ms"); // NOI18N
    }
}
