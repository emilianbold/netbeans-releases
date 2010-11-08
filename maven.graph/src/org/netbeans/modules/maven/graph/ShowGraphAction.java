/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.graph;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.indexer.api.ui.ArtifactViewer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;

/**
 *
 * @author mkleint
 */
@ActionID(id = "org.netbeans.modules.maven.graph.ShowGraphAction", category = "Project")
@ActionRegistration(displayName = "#ACT_Show_Graph")
@ActionReferences({
    @ActionReference(position = 1650, path = "Projects/org-netbeans-modules-maven/Actions", separatorAfter=1655),
    @ActionReference(position = 113, path = "Editors/text/x-maven-pom+xml/Popup", separatorAfter=213)
})
public class ShowGraphAction extends AbstractAction implements ContextAwareAction {
    public ShowGraphAction() {
        putValue(Action.NAME, org.openide.util.NbBundle.getMessage(ShowGraphAction.class, "ACT_Show_Graph"));
    }
    
    public ShowGraphAction(Project prj) {
        this();
        if (prj != null) {
            putValue("prj", prj); //NOI18N
        }
    }

    @Override
    public boolean isEnabled() {
        Project project = (Project) getValue("prj"); //NOI18N
        return project != null && project.getLookup().lookup(NbMavenProject.class) != null;
    }
    
    public void actionPerformed(ActionEvent e) {
        final Project project = (Project) getValue("prj"); //NOI18N
        ArtifactViewer.showArtifactViewer(project, ArtifactViewer.HINT_GRAPH);
    }
    
    public Action createContextAwareInstance(Lookup lookup) {
        Project prj = lookup.lookup(Project.class);
        if (prj == null) {
            FileObject fo = lookup.lookup(FileObject.class);
            if (fo != null) {
                prj = FileOwnerQuery.getOwner(fo);
            }
        }
        return new ShowGraphAction(prj);
    }
}
