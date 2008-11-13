/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.modelui.trace;

import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelState;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 * A common abstract parent for  test actions on projects
 * @author Vladimir Kvashin
 */
public abstract class TestProjectActionBase extends NodeAction {

    protected final static boolean TEST_XREF = Boolean.getBoolean("test.xref.action"); // NOI18N
    
    protected enum State {
        Enabled, Disabled, Indeterminate
    }
    
    private boolean running;
    private JMenuItem presenter;
    private boolean inited = false;

    public TestProjectActionBase() {
    }

    protected abstract void performAction(Collection<NativeProject> projects);
    
    @Override
    public JMenuItem getMenuPresenter() {
        return getPresenter();
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return getPresenter();
    }

    private JMenuItem getPresenter() {
        if (!this.inited) {
            this.presenter = new JMenuItem();
            org.openide.awt.Actions.connect(this.presenter, (Action) this, true);
            this.inited = true;
        }
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
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean enable(Node[] activatedNodes) {
        if (!TEST_XREF) {
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
        CsmModelAccessor.getModel().enqueue(new Runnable() {

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
    
    /** 
     * Gets the collection of native projects that correspond the given nodes.
     * @return in the case all nodes correspond to native projects -
     * collection of native projects; otherwise null
     */
    protected Collection<NativeProject> getNativeProjects(Node[] nodes) {
        Collection<NativeProject> projects = new ArrayList<NativeProject>();
        for (int i = 0; i < nodes.length; i++) {
            Object o = nodes[i].getValue("Project"); // NOI18N 
            if (!(o instanceof Project)) {
                return null;
            }
            NativeProject nativeProject = ((Project) o).getLookup().lookup(NativeProject.class);
            if (nativeProject == null) {
                return null;
            }
            projects.add(nativeProject);
        }
        return projects;
    }

    protected State getState(Collection<NativeProject> projects) {
        CsmModel model = CsmModelAccessor.getModel();
        if (model == null || model.getState() != CsmModelState.ON) {
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
        CsmProject csmPrj = CsmModelAccessor.getModel().getProject(p);
        return csmPrj != null && csmPrj.isStable(null) ? State.Enabled : State.Disabled;
    }
    
}
