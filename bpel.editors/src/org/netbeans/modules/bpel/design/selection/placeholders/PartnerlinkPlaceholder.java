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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.bpel.design.selection.placeholders;

import java.awt.Cursor;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.DiagramView;
import org.netbeans.modules.bpel.design.DnDHandler;
import org.netbeans.modules.bpel.design.PartnerlinksView;
import org.netbeans.modules.bpel.design.model.PartnerLinkHelper;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.model.patterns.ProcessPattern;
import org.netbeans.modules.bpel.design.selection.PlaceHolder;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.properties.ImportRegistrationHelper;
import org.netbeans.modules.soa.ui.UserNotification;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.websvc.core.WebServiceReference;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.reference.ReferenceUtil;
import org.netbeans.api.project.Project;
import org.netbeans.modules.bpel.design.model.PartnerRole;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.nodes.Node;

public class PartnerlinkPlaceholder extends PlaceHolder {

    private DiagramView diagramView;
    private BpelEntity insertAfter;

    public PartnerlinkPlaceholder(DiagramView diagramView, BpelEntity insertAfter, Pattern dndPattern, double x, double y) {
        super(null, dndPattern, x, y);
        this.diagramView = diagramView;
        this.insertAfter = insertAfter;
    }

    public void drop() {
        final Pattern pattern = getDraggedPattern();
        final PartnerLink pl = (PartnerLink) pattern.getOMReference();
        final RequestProcessor rp = getRequestProcessor();
        final Object dndCookie = pl.getCookie(DnDHandler.class);

        if (dndCookie instanceof FileObject) {
            FileObject fo = ((FileObject) dndCookie);

            if (isInOurProject(fo)) {
                pl.setCookie(DnDHandler.class, fo);
                rp.post(new AddPartnerLinkTask(pl, pattern));
            }
            else {
                rp.post(new RetrieveWSDLTask(fo, pl, pattern));
            }
        }
        // vlv: dnd
        else if (dndCookie instanceof WebServiceReference) {
            FileObject fo = ReferenceUtil.generateWsdlFromJavaModule((FileObject) pl.getCookie(FileObject.class));
            rp.post(new RetrieveWSDLTask(fo, pl, pattern));
        }
        else {
            rp.post(new AddPartnerLinkTask(pl, pattern));
        }
    }

    private boolean isInOurProject(FileObject fo) {
        FileObject bpel_fo = (FileObject) diagramView.getDesignView().getBPELModel().getModelSource().getLookup().lookup(FileObject.class);

        if (bpel_fo == null) {
            return false;
        }
        Project my_project = FileOwnerQuery.getOwner(bpel_fo);
        Project other_project = FileOwnerQuery.getOwner(fo);
        return my_project != null && my_project.equals(other_project);
    }

    private class RetrieveWSDLTask implements Runnable {
        public RetrieveWSDLTask(FileObject file, PartnerLink pl, Pattern pattern) {
            myFile = file;
            myPL = pl;
            myPattern = pattern;
        }

        public void run() {
            DesignView view = diagramView.getDesignView();
            Cursor oldCursor = view.getCursor();
            view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            PartnerLinkHelper helper = new PartnerLinkHelper(view.getModel());
            FileObject result = helper.retrieveWSDL(myFile);

            if (result != null) {
              myPL.setCookie(DnDHandler.class, result);
            }
            new AddPartnerLinkTask(myPL, myPattern).run();
            view.setCursor(oldCursor);
        }

        private FileObject myFile;
        private PartnerLink myPL;
        private Pattern myPattern;
    }

    private class AddPartnerLinkTask implements Runnable {
        public AddPartnerLinkTask(PartnerLink pLink, Pattern pattern) {
            this.pLink = pLink;
            this.pattern = pattern;
        }

        public void run() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    final BpelModel model = diagramView.getDesignView().getBPELModel();
                    try {
                        model.invoke(new Callable() {
                            public Object call() throws Exception {
                                Process process = model.getProcess();
                                PartnerLinkContainer plc = process.getPartnerLinkContainer();
                                boolean isPlContainerCreated = false;

                                if (plc == null) {
                                    plc = model.getBuilder().createPartnerLinkContainer();
                                    process.setPartnerLinkContainer(plc);
                                    isPlContainerCreated = true;
                                }
                                plc.insertPartnerLink(pLink, getIndex(plc));
                                PartnerLinkType plt = getFirstPLT();

                                if (plt != null) {
                                    WSDLReference<PartnerLinkType> plt_ref = pLink.createWSDLReference(plt, PartnerLinkType.class);
                                    pLink.setPartnerLinkType(plt_ref);
                                    new ImportRegistrationHelper(pLink.getBpelModel()).addImport(plt.getModel());
                                }
                                PartnerLinkHelper.setPartnerlinkRole(pLink, ((PartnerlinksView) diagramView).getMode());
                                
                                pLink.setCookie(PartnerRole.class, ((PartnerlinksView) diagramView).getMode());
                                
                                if (pLink.getPartnerLinkType() == null) {
                                    if (!diagramView.getDesignView().showCustomEditor(pattern, CustomNodeEditor.EditingMode.CREATE_NEW_INSTANCE)) {
                                        plc.remove(pLink);

                                        if (isPlContainerCreated) {
                                            process.removePartnerLinkContainer();
                                        }
                                    }
                                }
                                return null;
                            }

                            private PartnerLinkType getFirstPLT() {
                                Object object = pLink.getCookie(DnDHandler.class);

                                if (object instanceof Node){
                                    object = ((Node) object).getLookup().lookup(FileObject.class);
                                    
                                }
                                if ( !(object instanceof FileObject) ) {
                                  return null;
                                }
                                WSDLModel model = PartnerLinkHelper.getWSDLModel((FileObject) object);

                                if (model == null || model.getDefinitions() == null) {
                                    return null;
                                }
                                List<PartnerLinkType> plts = model.getDefinitions().getExtensibilityElements(PartnerLinkType.class);

                                if (plts == null || plts.size() != 1) {
                                    return null;
                                }
                                return plts.get(0);
                            }

                            private int getIndex(PartnerLinkContainer plc) {
                                if (insertAfter != null) {
                                    PartnerLink pls[] = plc.getPartnerLinks();
                                    for (int n = 0; n < pls.length; n++) {
                                        if (pls[n] == insertAfter) {
                                            return n + 1;
                                        }
                                    }
                                }
                                return 0;
                            }
                            }, this);
                    } catch (Exception ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }
            });
        }

        private Pattern pattern;
        private PartnerLink pLink;
    }

    private synchronized RequestProcessor getRequestProcessor() {
        if (wsdlDnDRequestProcessor == null) {
            wsdlDnDRequestProcessor = new RequestProcessor(getClass().getName());
        }
        return wsdlDnDRequestProcessor;
    }

    private RequestProcessor wsdlDnDRequestProcessor;
}
