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
package org.netbeans.modules.compapp.casaeditor.nodes.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.nodes.EndpointNode;
import org.netbeans.modules.compapp.casaeditor.nodes.ServiceUnitProcessNode;
import org.netbeans.modules.compapp.casaeditor.nodes.WSDLEndpointNode;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLSourceMultiViewElement;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.windows.TopComponent;

/**
 * Action to go to the source of a BPEL Process, a WSDL Port, etc.
 *
 * @author jqian
 */
public class GoToSourceAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length < 1) {
            return;
        }

        Node activatedNode = activatedNodes[0];

        if (activatedNode instanceof ServiceUnitProcessNode) {
            ServiceUnitProcessNode node = ((ServiceUnitProcessNode) activatedNode);
            CasaServiceEngineServiceUnit sesu = node.getServiceEngineServiceUnit();
            String filePath = node.getFilePath();
            gotoProcess(sesu, filePath);
        } else if (activatedNode instanceof EndpointNode) {
            EndpointNode node = ((EndpointNode) activatedNode);
            CasaEndpointRef endpointRef = (CasaEndpointRef) node.getData();
            CasaServiceEngineServiceUnit sesu =
                    (CasaServiceEngineServiceUnit) endpointRef.getParent();
            String filePath = endpointRef.getFilePath();
            gotoProcess(sesu, filePath);
        } else if (activatedNode instanceof WSDLEndpointNode) {
            WSDLEndpointNode node = ((WSDLEndpointNode) activatedNode);
            CasaPort casaPort = (CasaPort) node.getData();
            gotoWSDLPort(casaPort);
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
     * Goes to the source of a process (e.x., a BPEL Process).
     *
     * @param sesu      a CASA service engine service unit
     * @param filePath  path of the process artifact relative to the
     *                  SU project's source directory
     */
    private static void gotoProcess(CasaServiceEngineServiceUnit sesu, String filePath) {

        if (sesu == null || filePath == null) {
            return;
        }

        Project ownerProject = getOwningSUProject(sesu);
        if (ownerProject == null) {
            NotifyDescriptor d = new NotifyDescriptor.Message(
                    NbBundle.getMessage(GoToSourceAction.class,
                    "MSG_SuProjectNotFound", sesu.getUnitName()), // NOI18N
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return;
        }

        FileObject ownerProjectDir = ownerProject.getProjectDirectory();

        if (filePath != null && filePath.length() > 0) {
            filePath = "src/" + filePath.replaceAll("\\\\", "/"); // NOI18N  #171994
            FileObject fileObject = ownerProjectDir.getFileObject(filePath);
            //System.out.println("Opening " + fileObject.getPath());
            try {
                final DataObject dataObject = DataObject.find(fileObject);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        openDataObject(dataObject);
                        requestViewOpen(dataObject);
                    }
                });
            } catch (DataObjectNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Goes to the source of a WSDL Port.
     *
     * @param casaPort  a CASA port
     */
    private static void gotoWSDLPort(final CasaPort casaPort) {
        if (casaPort == null) {
            return;
        }

        CasaWrapperModel casaModel = (CasaWrapperModel)casaPort.getModel();
        final Port port = casaModel.getLinkedWSDLPort(casaPort);
        if (port == null) {
            String message = NbBundle.getMessage(GoToSourceAction.class,
                    "MSG_LinkedWSDLPortNotAvailable"); // NOI18N
            NotifyDescriptor d = new NotifyDescriptor.Message(message,
                    NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return;
        }

        FileObject wsdlFO =
                port.getModel().getModelSource().getLookup().lookup(FileObject.class);
        if (wsdlFO != null) {
            String casaPortLinkHref = casaPort.getLink().getHref();
            if (casaPortLinkHref.startsWith("../jbiServiceUnits/")) { // NOI18N
                String suProjectName = casaPortLinkHref.substring(19);
                suProjectName = suProjectName.substring(0, suProjectName.indexOf("/")); // NOI18N
                NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(GoToSourceAction.class,
                        "MSG_OpenReadOnlyCopyOfWSDL", // NOI18N
                        wsdlFO.getNameExt(),
                        suProjectName),
                        NbBundle.getMessage(GoToSourceAction.class,
                        "TTL_OpenReadOnlyCopyOfWSDL"), // NOI18N
                        NotifyDescriptor.OK_CANCEL_OPTION);

                if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.OK_OPTION) {
                    return;
                }
            }

            try {
                final DataObject dataObject = DataObject.find(wsdlFO);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        WSDLSourceMultiViewElement.gotoSource(port, dataObject);
//                      ViewComponentCookie cookie = dataObject.getCookie(ViewComponentCookie.class);
//                      if (cookie != null && cookie.canView(View.STRUCTURE, port)) {
//                          cookie.view(View.STRUCTURE, port);
//                      }
                    }
                });
            } catch (DataObjectNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Tries to open the given data object using EditCookie, EditorCookie
     * or OpenCookie.
     *
     * @param dataObject    a data object
     */
    private static void openDataObject(DataObject dataObject) {
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
    }

    // This is a slightly modified version based on BpelMultiViewSupport.
    private static void requestViewOpen(DataObject targetDO) {

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

    /**
     * Gets the owning SU project for the given service engine service unit.
     */
    private static Project getOwningSUProject(CasaServiceEngineServiceUnit sesu) {
        CasaWrapperModel model = (CasaWrapperModel) sesu.getModel();
        Project jbiProject = (Project) model.getJBIProject();

        String unitName = sesu.getUnitName();

        SubprojectProvider subprojectProvider =
                jbiProject.getLookup().lookup(SubprojectProvider.class);
        for (Project subproject : subprojectProvider.getSubprojects()) {
            ProjectInformation projectInfo =
                    subproject.getLookup().lookup(ProjectInformation.class);
            if (projectInfo.getName().equals(unitName)) {
                return subproject;
            }
        }

        // The above will not work right after a rename of the SU project! (#152355)
        // Try using the unitName as a project reference to find the
        // corresponding SU project.
        AntProjectHelper antProjectHelper =
                jbiProject.getLookup().lookup(AntProjectHelper.class);
        EditableProperties projectProperties =
                antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String suProjectPath =
                projectProperties.getProperty("project." + unitName); // NOI18N
        if (suProjectPath != null) {
            FileObject suProjectFO = jbiProject.getProjectDirectory().getFileObject(suProjectPath);
            if (suProjectFO != null) {
                try {
                    return ProjectManager.getDefault().findProject(suProjectFO);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IllegalArgumentException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        return null;
    }
}
