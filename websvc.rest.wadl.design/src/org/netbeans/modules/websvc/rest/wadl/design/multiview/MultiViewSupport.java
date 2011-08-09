/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.websvc.rest.wadl.design.multiview;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.openide.text.DataEditorSupport;
import org.openide.loaders.DataObject;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.modules.websvc.rest.wadl.design.cookies.GetComponentCookie;
import org.netbeans.modules.websvc.rest.wadl.design.loader.ShowCookie;
import org.netbeans.modules.websvc.rest.wadl.design.loader.WadlDataLoader;
import org.netbeans.modules.websvc.rest.wadl.design.loader.WadlDataObject;
import org.netbeans.modules.websvc.rest.wadl.design.loader.WadlEditorSupport;
import org.netbeans.modules.websvc.rest.wadl.model.WadlComponent;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.websvc.rest.wadl.model.WadlModel;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Class for creating the Multiview
 * @author Ayub Khan
 */
public class MultiViewSupport implements OpenCookie, EditCookie {

    static final long serialVersionUID = 1L;
    private WadlDataObject dataObject;
    public static String SOURCE_UNSAFE_CLOSE = "SOURCE_UNSAFE_CLOSE";           // NOI18N
    
    public static final String SOURCE_VIEW_ID = "websvc-rest-wadl-sourceview";  // NOI18N

    public static final String DESIGN_VIEW_ID = "websvc-rest-wadl-designview";   // NOI18N
    
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
        DESIGN
    }

    /**
     * Constructor for deserialization
     */
    public MultiViewSupport() {
    }
    static Logger l = Logger.getLogger(MultiViewSupport.class.getName());

    /**
     * Constructor
     * @param displayName
     * @param dataObject
     */
    public MultiViewSupport(WadlDataObject dataObject) {
        this.dataObject = dataObject;
    }

    @Override
    public void open() {
        view(View.DESIGN);
    }

    @Override
    public void edit() {
        view(View.SOURCE);
    }

    WadlDataObject getDataObject() {
        return dataObject;
    }

    private DataEditorSupport getEditorSupport() {
        return dataObject.getLookup().lookup(DataEditorSupport.class);
    }

    FileObject getImplementationBean() {
        return getDataObject().getPrimaryFile();
    }

    /**
     * Create the Multiview, doc into the editor window and open it.
     * @return CloneableTopComponent new multiview.
     */
    public CloneableTopComponent createMultiView() {
        /*MultiViewDescription views[] = new MultiViewDescription[2];

        // Put the source element first so that client code can find its
        // CloneableEditorSupport.Pane implementation.
        views[0] = new SourceMultiViewDesc(getDataObject());
        views[1] = new DesignMultiViewDesc(getDataObject());
        
        // Make the source view the default element.
        CloneableTopComponent multiview =
                MultiViewFactory.createCloneableMultiView(
                views,
                views[1], new WadlEditorSupport.CloseHandler(getDataObject()));
        */
        CloneableTopComponent multiview = MultiViews.createCloneableMultiView(
                WadlDataLoader.MIME_TYPE, dataObject);
        String displayName = getDataObject().getNodeDelegate().getDisplayName();
        multiview.setDisplayName(displayName);
        multiview.setName(displayName);

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

                @Override
                public void run() {
                    viewInSwingThread(view, param);
                }
            });
        } else {
            viewInSwingThread(view, param);
        }
    }

    private void viewInSwingThread(View view, Object... parameters) {
        getEditorSupport().open();
        switch (view) {
            case SOURCE:
                requestMultiviewActive(SOURCE_VIEW_ID);
                break;
            case DESIGN:
                requestMultiviewActive(DESIGN_VIEW_ID);
                break;
        }
        if (parameters != null && parameters.length > 0) {
            TopComponent activeTC = TopComponent.getRegistry().getActivated();
            ShowComponentCookie cake = activeTC.getLookup().lookup(ShowComponentCookie.class);
            if (cake != null) {
                cake.show(parameters[0]);
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
        return Collections.list(((CloneableTopComponent) tc).getReference().getComponents()).size();
    }

    /**
     * Returns true if the given TopComponent is the last one in the
     * set of cloneable windows.
     *
     * @param  tc  TopComponent.
     * @return  false if tc is not cloneable, or there are one or more
     *          clones; true if it is the last clone.
     */
    public static boolean isLastView(TopComponent tc) {
        if (!(tc instanceof CloneableTopComponent)) {
            return false;
        }
        boolean oneOrLess = true;
        Enumeration en = ((CloneableTopComponent) tc).getReference().getComponents();
        if (en.hasMoreElements()) {
            en.nextElement();
            if (en.hasMoreElements()) {
                oneOrLess = false;
            }
        }
        return oneOrLess;
    }

    /**
     * Shows the desired multiview element. Must be called after the editor
     * has been opened (i.e. WadlEditorSupport.open()) so the TopComponent
     * will be the active one in the registry.
     *
     * @param  id      identifier of the multiview element.
     */
    public static void requestMultiviewActive(String id) {
        TopComponent activeTC = TopComponent.getRegistry().getActivated();
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
     * Finds the preferredID of active multiview element.
     * If activated tc is not mvtc returns null;
     *
     * @return  identifier of the active multiview element.
     */
    private static String getMultiviewActive() {
        TopComponent activeTC = TopComponent.getRegistry().getActivated();
        MultiViewHandler handler = MultiViews.findMultiViewHandler(activeTC);
        if (handler != null) {
            return handler.getSelectedPerspective().preferredID();
        }
        return null;
    }
    
    /**
     * Finds the activated components of active top component.
     *
     * @return  collection of the active components.
     */
    private static Collection<Component> getActiveComponents() {
        TopComponent activeTC = TopComponent.getRegistry().getActivated();
        Collection<Component> activeComponents = Collections.emptySet();
        for(Node n:activeTC.getActivatedNodes()) {
            GetComponentCookie cake = n.getCookie(GetComponentCookie.class);
            Component component = null;
            if(cake!=null) component = cake.getComponent();
            if(component==null)
                component = n.getLookup().lookup(Component.class);
            if(component!=null) {
                if(activeComponents.isEmpty()) activeComponents = new HashSet<Component>();
                activeComponents.add(component);
            }
        }
        return activeComponents;
    }
    
    public void view(final View view, final Component component,
            final Object... parameters) {
        
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    viewInSwingThread(view, component, parameters);
                }
            });
        } else {
            viewInSwingThread(view, component, parameters);
        }
    }
    
    private void viewInSwingThread(View view, Component component,
            Object... parameters) {
        if(canView(view, component)) {
//            if(view==View.SUPER) {
//                // preserve the view
//                TopComponent activeTC = TopComponent.getRegistry().getActivated();
//                WadlDataObject sdobj = activeTC.getLookup().
//                        lookup(WadlDataObject.class);
//                if(dobj!=sdobj) // must be multifile
//                {
//                    String activeMVTCId = getMultiviewActive();
//                    view = View.STRUCTURE;
//                    if(DesignMultiViewDesc.PREFERRED_ID.equals(activeMVTCId))
//                        view = View.DESIGN;
//                    else if(SourceMultiViewDesc.PREFERRED_ID.equals(activeMVTCId))
//                        view = View.SOURCE;
//                }
//            }
            WadlEditorSupport editor = dataObject.getWadlEditorSupport();
            editor.open();
            // activate view if needed
            switch (view) {
            case SOURCE:
                requestMultiviewActive(SOURCE_VIEW_ID);
                break;
            case DESIGN:
                requestMultiviewActive(DESIGN_VIEW_ID);
                    break;
            }
            // show component in current multiview
            TopComponent activeTC = TopComponent.getRegistry().getActivated();
            ShowCookie showCookie = activeTC.getLookup().lookup(ShowCookie.class);
            ResultItem resultItem = null;
            if (parameters!=null&&parameters.length!=0) {
                for(Object o :parameters) {
                    if(o instanceof ResultItem) {
                        resultItem = (ResultItem)o;
                        break;
                    }
                }
            }
            if(resultItem == null) resultItem = new ResultItem(null,null,component,null);
            if(showCookie!=null) showCookie.show(resultItem);
        }
    }
    

    public void show(ResultItem resultItem) {
        View view = View.SOURCE;
        Component component = resultItem.getComponents();
        if(component != null && component.getModel() != null &&
                component.getModel().getState() == Model.State.VALID) {
            // preserve the view
            TopComponent activeTC = TopComponent.getRegistry().getActivated();
            WadlDataObject sdobj = activeTC.getLookup().
                    lookup(WadlDataObject.class);
            if(dataObject==sdobj) { // we can preserve the view
                String activeMVTCId = getMultiviewActive();
                if(DESIGN_VIEW_ID.equals(activeMVTCId)) {
                    WadlModel axiModel = null;
                    if(component instanceof WadlComponent) {
                        axiModel = ((WadlComponent)component).getModel();
                    }
                    if(axiModel!=null&&axiModel.getState()==WadlModel.State.VALID) {
                        view = View.DESIGN;
                    }
                }
            }
        }
        view(view, component, resultItem);
    }
    
    public boolean canView(View view, Component component) {
        if(view == null)
            return false;
        
        //if there is no component, just switch the view.
        //see http://www.netbeans.org/issues/show_bug.cgi?id=135537
        if(component == null)
            return true;
        
        switch(view) {
            case SOURCE:
                if(!SOURCE_VIEW_ID.equals(
                        getMultiviewActive()) ||
                        !getActiveComponents().contains(component)) {
                    return true;
                } else return false;
            case DESIGN:
                if(DESIGN_VIEW_ID.equals(
                        getMultiviewActive()) &&
                        getActiveComponents().contains(component)) {
                    TopComponent activeTC = TopComponent.getRegistry().getActivated();
                    WadlDataObject sdobj = activeTC.getLookup().
                            lookup(WadlDataObject.class);
                    return sdobj!=dataObject;
                } else return true;
        }
        return false;
    }
    
    /**
     * Implementation of CloseOperationHandler for multiview. Ensures the
     * editors correctly closed, data object is saved, etc. Holds a
     * reference to DataObject only - to be serializable with the multiview
     * TopComponent without problems.
     */
 /*   @MimeRegistration(mimeType=WadlDataLoader.MIME_TYPE, 
            service=CloseOperationHandler.class)
    public static class CloseHandler implements CloseOperationHandler, Serializable {

        private static final long serialVersionUID = -3838395157610633251L;
        private DataObject sourceDataObject;

        private CloseHandler() {
        }

        public CloseHandler(DataObject dataObject) {
            this.sourceDataObject = dataObject;
        }

        @Override
        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            StringBuilder message = new StringBuilder();
            for (CloseOperationState state : elements) {
                if (state.getCloseWarningID().equals(SOURCE_UNSAFE_CLOSE)) {
                    message.append(NbBundle.getMessage(DataObject.class,
                            "MSG_SaveFile", // NOI18N
                            sourceDataObject.getPrimaryFile().getNameExt()));
                    message.append("\n");
                }
            }
            NotifyDescriptor desc = new NotifyDescriptor.Confirmation(message.toString().trim());
            Object retVal = DialogDisplayer.getDefault().notify(desc);
            for (CloseOperationState state : elements) {
                Action act = null;
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
    }*/

}
