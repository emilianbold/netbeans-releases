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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.design.multiview;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.text.DataEditorSupport;
import org.openide.loaders.DataObject;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.modules.websvc.core.MultiViewCookie;

/**
 * Class for creating the Multiview
 * @author Ajit Bhate
 */
public class MultiViewSupport implements MultiViewCookie, Serializable {
    
    static final java.awt.Image SERVICE_BADGE = org.openide.util.Utilities.loadImage(
            "org/netbeans/modules/websvc/core/webservices/ui/resources/XMLServiceDataIcon.gif" ); //NOI18N
    static final long serialVersionUID = 1L;
    private DataObject dataObject;
    private transient Service service;
    
    public static String SOURCE_UNSAFE_CLOSE = "SOURCE_UNSAFE_CLOSE";
    private static String DESIGN_UNSAFE_CLOSE = "DESIGN_UNSAFE_CLOSE";

    /**
     * MultiView enum
     */
    public enum View {
        /**
         * Source multiview
         */
        SOURCE,
        /**
         * Design multiview
         */
        DESIGN,
    }

    /**
     * Constructor for deserialization
     */
    public MultiViewSupport() {
    }

    /**
     * Constructor
     * @param displayName 
     * @param dataObject 
     */
    MultiViewSupport(Service service, DataObject dataObject) {
        this.dataObject = dataObject;
        this.service = service;
    }

    public void open() {
        view(View.DESIGN);
    }

    public void edit() {
        view(View.SOURCE);
    }
    
    DataObject getDataObject() {
        return dataObject;
    }

    Service getService() {
        return service;
    }

    public boolean equals(Object obj) {
        if(obj==this) {
            return true;
        }
        if(obj instanceof MultiViewSupport) {
            MultiViewSupport sup = (MultiViewSupport)obj;
            return getDataObject().equals(sup.getDataObject());
        }
        return false;
    }

    /**
     * Ensures that the Multiview is created and is active.
      */
    private TopComponent ensureMultiViewActive() {
        Mode editorMode = WindowManager.getDefault().findMode(
                DataEditorSupport.EDITOR_MODE);
        if (editorMode != null && editorMode.getSelectedTopComponent() != null) {
            TopComponent activeTC = editorMode.getSelectedTopComponent();
            if(equals(activeTC.getLookup().lookup(MultiViewSupport.class))) {
                return activeTC;
            }
            for(TopComponent openedTC:editorMode.getTopComponents()) {
                if(equals(openedTC.getLookup().lookup(MultiViewSupport.class))) {
                    return openedTC;
                }
            }
        }
        CloneableTopComponent tc = createMultiView();
        tc.requestActive();
        return tc;
    }

    /**
     * Create the Multiview, doc into the editor window and open it.
     * @return CloneableTopComponent new multiview.
     */
    private CloneableTopComponent createMultiView() {
        MultiViewDescription views[] = new MultiViewDescription[2];
        
        // Put the source element first so that client code can find its
        // CloneableEditorSupport.Pane implementation.
        views[0] = new SourceMultiViewDesc(this);
        views[1] = new DesignMultiViewDesc(this);
        
        
        // Make the column view the default element.
        CloneableTopComponent multiview =
                MultiViewFactory.createCloneableMultiView(
                views,
                views[0]
                , new CloseHandler(dataObject)
                );
        
        String displayName = getServiceDisplayName();
        multiview.setDisplayName(displayName);
        multiview.setName(displayName);
        
        Mode editorMode = WindowManager.getDefault().findMode(
                DataEditorSupport.EDITOR_MODE);
        if (editorMode != null) {
            editorMode.dockInto(multiview);
        }
        multiview.open();
        return multiview;
    }
    
    
    /**
     * 
     * @param view 
     * @param param 
     */
    public void view(final View view, final Object... param) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    viewInSwingThread(view,dataObject,param);
                }
            });
        } else {
            viewInSwingThread(view,dataObject,param);
        }
    }
    
    private void viewInSwingThread(View view, Object... parameters) {
        TopComponent activeTC = ensureMultiViewActive();
        switch(view) {
        case SOURCE:
            requestMultiviewActive(activeTC,SourceMultiViewDesc.PREFERRED_ID);
            break;
        case DESIGN:
            requestMultiviewActive(activeTC,DesignMultiViewDesc.PREFERRED_ID);
            break;
        }
    }

    /**
     * Shows the desired multiview element. Must be called after the editor
     * has been opened (i.e. SchemaEditorSupport.open()) so the TopComponent
     * will be the active one in the registry.
     *
     * @param  id      identifier of the multiview element.
     * @param  activeTc MultiView CloneableTopComponent
     */
    private static void requestMultiviewActive(TopComponent activeTC, String id) {
        MultiViewHandler handler = MultiViews.findMultiViewHandler(activeTC);
        if (handler != null) {
            MultiViewPerspective[] perspectives = handler.getPerspectives();
            for (MultiViewPerspective perspective : perspectives) {
                if (perspective.preferredID().equals(id)) {
                    handler.requestActive(perspective);
                }
            }
        }
    }

    /**
     * Returns true if the given TopComponent is the last one in the
     * set of cloneable windows.
     *
     * @param  tc  TopComponent.
     * @return  -1 if not a cloneabletopcomponent 
     *          otherwise number of clones including self
     */
    public static int getNumberOfClones(TopComponent tc) {
        if (!(tc instanceof CloneableTopComponent)) {
            return -1;
        }
        return Collections.list(((CloneableTopComponent)tc).getReference().getComponents()).size();
    }
    
    /**
     * creates display name for service
     **/
    private String getServiceDisplayName() {
        if (service.getWsdlUrl()!=null)
            return service.getServiceName()+"["+service.getPortName()+"]";
        return service.getName();
    } 
    
    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {
        out.writeObject(dataObject);
        out.writeObject(service.getImplementationClass());
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        Object firstObject = in.readObject();
        if(firstObject instanceof DataObject) {
            dataObject = (DataObject) firstObject;
        }
        Object secondObject = in.readObject();
        if(secondObject instanceof String) {
            String implClass = (String)secondObject;
            Project project = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
            JaxWsModel model = (JaxWsModel) project.getLookup().lookup(JaxWsModel.class);
            service = model.findServiceByImplementationClass(implClass);
        }
    }

    /**
     * Implementation of CloseOperationHandler for multiview. Ensures the
     * editors correctly closed, data object is saved, etc. Holds a
     * reference to DataObject only - to be serializable with the multiview
     * TopComponent without problems.
     */
    public static class CloseHandler implements CloseOperationHandler, Serializable {
        private static final long serialVersionUID = -3838395157610633251L;
        private DataObject sourceDataObject;
        
        private CloseHandler() {
            super();
        }
        
        public CloseHandler(DataObject sourceDataObject) {
            this.sourceDataObject = sourceDataObject;
        }
        
        private DataEditorSupport getEditorSupport() {
            return sourceDataObject == null ? null :
                (DataEditorSupport) sourceDataObject.getLookup().lookup(
                DataEditorSupport.class);
        }
        
        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            StringBuffer message = new StringBuffer();
            for(CloseOperationState state:elements) {
                if(state.getCloseWarningID().equals(SOURCE_UNSAFE_CLOSE)) {
                    message.append(NbBundle.getMessage(DataObject.class, 
                            "MSG_SaveFile", // NOI18N
                            sourceDataObject.getPrimaryFile().getNameExt()));
                    message.append("\n");
                }
            }
            NotifyDescriptor desc = new NotifyDescriptor.Confirmation(message.toString().trim());
            Object retVal = DialogDisplayer.getDefault().notify(desc);
            for(CloseOperationState state:elements) {
                Action act =  null;
                if (retVal == NotifyDescriptor.YES_OPTION) {
                    act = state.getProceedAction();
                } else if (retVal == NotifyDescriptor.NO_OPTION) {
                    act = state.getDiscardAction();
                } else {
                    return false;
                }
                if (act != null) {
                    act.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
                }
            }
            return true;
        }
    }
}
