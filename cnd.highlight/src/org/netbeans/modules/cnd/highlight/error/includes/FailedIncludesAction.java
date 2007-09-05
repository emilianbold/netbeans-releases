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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.highlight.error.includes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.highlight.error.BadgeProvider;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Alexander Simon
 */
public class FailedIncludesAction extends NodeAction {
    
    public FailedIncludesAction(){
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
 
    private String i18n(String id) {
        return NbBundle.getMessage(FailedIncludesAction.class,id);
    }

    protected void performAction(Node[] activatedNodes) {
        List<NativeProject> projects = getNativeProjects(activatedNodes);
        if( projects == null || projects.size() != 1) {
            return;
        }
        NativeProject nativeProject = projects.get(0);
        Set<CsmFile> list = new HashSet<CsmFile>();
        Set<CsmUID<CsmFile>> set = BadgeProvider.getInstance().getFailedFiles(nativeProject);
        if (set != null) {
            for (CsmUID<CsmFile> fileUID : set) {
                CsmFile csmFile = fileUID.getObject();
                assert csmFile != null;
                if (csmFile != null) {
                    list.add(csmFile);
                }
            }
        }
        ErrorIncludeDialog.showErrorIncludeDialog(list);
    }

    protected boolean enable(Node[] activatedNodes) {
        List<NativeProject> projects = getNativeProjects(activatedNodes);
        if( projects == null || projects.size() != 1) {
            return false;
        }
        return BadgeProvider.getInstance().hasFailedFiles(projects.get(0));
    }
    
    private List<NativeProject> getNativeProjects(Node[] nodes) {
        List<NativeProject> projects = new ArrayList<NativeProject>();
        for (int i = 0; i < nodes.length; i++) {
            Project project = nodes[i].getLookup().lookup(Project.class);
            if(project == null) {
                return null;
            }
            NativeProject nativeProject = project.getLookup().lookup(NativeProject.class);
            if(nativeProject == null) {
                return null;
            }
            projects.add(nativeProject);
        }
        return projects;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    public String getName() {
        return i18n("ErrorIncludeMenu_Title");
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}