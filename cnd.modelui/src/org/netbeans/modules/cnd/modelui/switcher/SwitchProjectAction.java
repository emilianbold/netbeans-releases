/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.modelui.switcher;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
/**
 *
 * @author Vladimir Kvashin
 */
public class SwitchProjectAction extends NodeAction {
    
    private JCheckBoxMenuItem presenter;
    private ModelImpl model;
    
    private enum State {
        Enabled, Disabled, Indeterminate
    }
    
    public SwitchProjectAction() {
        presenter = new JCheckBoxMenuItem(getName());
        presenter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onActionPerformed();
            }
        });
        CsmModel model = CsmModelAccessor.getModel();
        if( model instanceof ModelImpl ) {
            this.model = (ModelImpl) model;
        }
    }
    
    public String getName() {
	return i18n("CTL_SwitchProjectAction"); // NOI18N
    }
    
    private String i18n(String id) {
        return NbBundle.getMessage(SwitcherDialogAction.class, id);
    }
    
    public HelpCtx getHelpCtx() {
	return HelpCtx.DEFAULT_HELP;
    }
    
    public JMenuItem getMenuPresenter() {
        return getPresenter();
    }    
    
    public JMenuItem getPopupPresenter() {
        return getPresenter();
    }
    
    private JMenuItem getPresenter() {
        final Collection<NativeProject> projects = getNativeProjects(getActivatedNodes());
        if( projects == null ) {
            presenter.setEnabled(false);
            presenter.setSelected(false);
        }
        else {
            State state = getState(projects);
            if( state == State.Indeterminate ) {
                presenter.setEnabled(false);
                presenter.setSelected(false);
            }
            else {
                presenter.setEnabled(true);
                presenter.setSelected(state == State.Enabled);
            }
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
            if( ! (o instanceof  Project) ) {
                return null;
            }
            NativeProject nativeProject = (NativeProject) ((Project) o).getLookup().lookup(NativeProject.class);
            if( nativeProject == null ) {
                return null;
            }
            projects.add(nativeProject);
        }
        return projects;
    }
    
    private State getState(Collection<NativeProject> projects) {
        if( model == null ) {
            return State.Indeterminate;
        }
        State state = State.Indeterminate;
        for( NativeProject p : projects ) {
            State curr = getState(p);
            if( state == State.Indeterminate ) {
                state = curr;
            }
            else {
                if( state != curr ) {
                    return State.Indeterminate;
                }
            }
        }
        return state;
    }
    
    private State getState(NativeProject p) {
        return model.isProjectEnabled(p) ? State.Enabled : State.Disabled;
    }
    
    protected boolean enable(Node[] activatedNodes)  {
        if( model == null ) {
            return false;
        }
        Collection<NativeProject> projects = getNativeProjects(getActivatedNodes());
        if( projects == null) {
            return false;
        }
        return getState(projects) != State.Indeterminate;
    }

    private void onActionPerformed() {
        performAction(getActivatedNodes());
    }
    
    /** Actually nobody but us call this since we have a presenter. */
    public void performAction(Node[] activatedNodes) {
        performAction(getNativeProjects(getActivatedNodes()));
    }
    
    private void performAction(Collection<NativeProject> projects) {
        if( projects != null ) {
            State state = getState(projects);
            switch( state ) {
                case Enabled:
                    for( NativeProject p : projects ) {
                        model.disableProject(p);
                    }
                    break;
                case Disabled:
                    for( NativeProject p : projects ) {
                        model.enableProject(p);
                    }
                    break;
            }
        }
    }
    
    protected boolean asynchronous () {
        return false;
    }
}
