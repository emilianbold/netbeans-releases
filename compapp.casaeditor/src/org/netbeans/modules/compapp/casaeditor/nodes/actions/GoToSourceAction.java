/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.compapp.casaeditor.nodes.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.EndpointNode;
import org.netbeans.modules.compapp.casaeditor.nodes.ServiceUnitProcessNode;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.TopComponent;

/**
 *
 * @author jqian
 */
public class GoToSourceAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length < 1) {
            return;
        }

        CasaServiceEngineServiceUnit sesu = null;
        String filePath = null;
        
        if (activatedNodes[0] instanceof ServiceUnitProcessNode) {
            ServiceUnitProcessNode node = ((ServiceUnitProcessNode) activatedNodes[0]);
            sesu = (CasaServiceEngineServiceUnit) node.getServiceEngineServiceUnit();
            filePath = node.getFilePath();
        } else if (activatedNodes[0] instanceof EndpointNode) {
            EndpointNode node = ((EndpointNode) activatedNodes[0]);
            CasaEndpointRef endpointRef = (CasaEndpointRef) node.getData();
            sesu = (CasaServiceEngineServiceUnit) endpointRef.getParent();
            filePath = endpointRef.getFilePath();
        }
        
        if (sesu != null && filePath != null) {
            Project ownerProject = getOwnerProject(sesu);
            FileObject ownerProjectDir = ownerProject.getProjectDirectory();

            if (filePath != null && filePath.length() > 0) {
                filePath = "src/" + filePath; // NOI18N
                FileObject fileObject = ownerProjectDir.getFileObject(filePath);
                //System.out.println("Opening " + fileObject.getPath());
                try {
                    final DataObject dataObject = DataObject.find(fileObject);
                    EditCookie editCookie = dataObject.getCookie(EditCookie.class);
                    if (editCookie != null) {
                        editCookie.edit();
                    } else {
                        EditorCookie editorCookie = dataObject.getCookie(EditorCookie.class);
                        if (editorCookie != null) {
                            editorCookie.open();
                        } else {
                            OpenCookie openCookie = dataObject.getCookie(OpenCookie.class);
                            if (openCookie != null) {
                                openCookie.open();
                            }
                        }
                    }
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            requestViewOpen(dataObject);
                        }
                    });
                } catch (DataObjectNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // This is a slightly modified version based on BpelMultiViewSupport.
    private void requestViewOpen(DataObject targetDO) {

        List<TopComponent> associatedTCs = new ArrayList<TopComponent>();
        TopComponent activeTC = TopComponent.getRegistry().getActivated();
        if (targetDO == (DataObject) activeTC.getLookup().lookup(DataObject.class)) {
            associatedTCs.add(activeTC);
        }
        Set openTCs = TopComponent.getRegistry().getOpened();
        for (Object tc : openTCs) {
            TopComponent topComponent = (TopComponent) tc;
            if (targetDO == (DataObject) topComponent.getLookup().lookup(DataObject.class)) {
                associatedTCs.add(topComponent);
            }
        }

        // Use the first TC in the list that has the desired perspective
        for (TopComponent targetTC : associatedTCs) {
            MultiViewHandler handler = MultiViews.findMultiViewHandler(targetTC);
            if (handler == null) {
                continue;
            }
            MultiViewPerspective[] p = handler.getPerspectives();
            for (MultiViewPerspective mvp : p) {
                // Try to make it a little bit more generic.
                // if (!mvp.preferredID().equals("bpelsource")) {
                if (!mvp.preferredID().toLowerCase().contains("source")) { // NOI18N
                    handler.requestActive(mvp);
                    return;
                }
            }
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(GoToSourceAction.class, "LBL_GoToSourceAction_Name"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Gets the owner project for the given endpoint reference.
     */
    private Project getOwnerProject(CasaServiceEngineServiceUnit sesu) {
        CasaWrapperModel model = (CasaWrapperModel) sesu.getModel();

        String unitName = sesu.getUnitName();

        Project jbiProject = model.getJBIProject();
        SubprojectProvider subprojectProvider =
                jbiProject.getLookup().lookup(SubprojectProvider.class);
        for (Project subproject : subprojectProvider.getSubprojects()) {
            ProjectInformation projectInfo =
                    subproject.getLookup().lookup(ProjectInformation.class);
            if (projectInfo.getName().equals(unitName)) {
                return subproject;
            }
        }

        return null;
    }
}
