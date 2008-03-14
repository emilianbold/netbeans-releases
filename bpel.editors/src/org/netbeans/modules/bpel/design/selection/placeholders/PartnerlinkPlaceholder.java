/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.bpel.design.selection.placeholders;

import java.awt.Cursor;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
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
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

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
        final Object dndCookie = pl.getCookie(DnDHandler.class);


        RequestProcessor rp = getRequestProcessor();

        //Handle the case of dropped WS node.
        //dndCookie contains the URL of deployed web service
        if (dndCookie instanceof FileObject) {

            FileObject fo = ((FileObject) dndCookie);

            if (!isInOurProject(fo)) {

                try {
                    URL url = fo.getURL();
                    String name = fo.getName();
                    rp.post(new RetrieveWSDLTask(url, name, pl, false));
                } catch (FileStateInvalidException ex) {
                    assert false;
                }

            } else {
                pl.setCookie(DnDHandler.class, fo);

            }
            rp.post(new AddPartnerLinkTask(pl, pattern));

        } else if (dndCookie instanceof WebServiceReference) {
            URL url = ((WebServiceReference) dndCookie).getWsdlURL();
            String name = ((WebServiceReference) dndCookie).getWebServiceName();
            if (url != null) {
                rp.post(new RetrieveWSDLTask(url, name, pl, true));
                rp.post(new AddPartnerLinkTask(pl, pattern));
            } else {
                //
                String messageText = NbBundle.getMessage(ProcessPattern.class,
                        "LBL_J2EEWS_NOT_DEPLOYED", // NOI18N
                        "");
                UserNotification.showMessageAsinc(messageText);

            }
        } else {
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

        return (my_project != null) && my_project.equals(other_project);
    }

    class RetrieveWSDLTask implements Runnable {

        private URL url;
        private String name;
        private PartnerLink pl;
        private boolean retrieveToFlat;

        public RetrieveWSDLTask(URL url, String name, PartnerLink pl, boolean retrieveToFlat) {
            this.url = url;
            this.name = name;
            this.pl = pl;
            this.retrieveToFlat = retrieveToFlat;
        }

        public void run() {
            DesignView view = diagramView.getDesignView();
            Cursor oldCursor = view.getCursor();
            view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            FileObject fo = new PartnerLinkHelper(view.getModel()).retrieveWSDL(url, name, retrieveToFlat);
            pl.setCookie(DnDHandler.class, fo);

            view.setCursor(oldCursor);

        }
        }

    class AddPartnerLinkTask implements Runnable {

        private PartnerLink pLink;
        private Pattern pattern;

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
                                
                                //lets check if we acception DnD of wsdl which already have 1 PLT inside, 
                                //so we can just assign roles, based on which swimlane it was droped on and dont 
                                //show customizer dialog
                                PartnerLinkType plt = getFirstPLT();
                                if (plt != null){
                                    WSDLReference<PartnerLinkType> plt_ref = 
                                            pLink.createWSDLReference(plt, PartnerLinkType.class);
                                    
                                    pLink.setPartnerLinkType(plt_ref);
                                    new ImportRegistrationHelper(pLink.getBpelModel())
                                            .addImport(plt.getModel());
                                }

                                PartnerLinkHelper.setPartnerlinkRole(pLink,
                                        ((PartnerlinksView) diagramView).getMode());

                                if (pLink.getPartnerLinkType() == null) {
                                    if (!diagramView.getDesignView().showCustomEditor(
                                            pattern, CustomNodeEditor.EditingMode.CREATE_NEW_INSTANCE)) {
                                        plc.remove(pLink);
                                        if (isPlContainerCreated) {
                                            process.removePartnerLinkContainer();
                                        }
                                    }
                                }


                                return null;
                            }

                            private PartnerLinkType getFirstPLT() {
                                FileObject fo = (FileObject) pLink.getCookie(DnDHandler.class);

                                if (fo == null) {
                                    return null;
                                }

                                WSDLModel model = PartnerLinkHelper.getWSDLModel(fo);
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
    }

    private synchronized RequestProcessor getRequestProcessor() {
        if (wsdlDnDRequestProcessor == null) {
            wsdlDnDRequestProcessor = new RequestProcessor(getClass().getName());
        }
        return wsdlDnDRequestProcessor;

    }
    private RequestProcessor wsdlDnDRequestProcessor;
}
