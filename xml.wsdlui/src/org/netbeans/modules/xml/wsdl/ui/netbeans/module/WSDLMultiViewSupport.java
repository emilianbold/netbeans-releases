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
package org.netbeans.modules.xml.wsdl.ui.netbeans.module;

import java.awt.EventQueue;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.validation.ui.ShowCookie;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

/**
 * Implementation of various cookie interfaces, in particular
 * ViewComponentCookie and ShowCookie.
 * The instance of this class is in the WSDLDataObject cookie set.
 * 
 * @author Ajit Bhate
 * @author Nathan Fiedler
 */
public class WSDLMultiViewSupport implements ViewComponentCookie, ShowCookie {
    /** The data object */
    private WSDLDataObject dobj;
    /** Set of components that are shown in the Partner (design) view. */
    private final Class[] DESIGNABLE_COMPONENTS = new Class[] {
        org.netbeans.modules.xml.wsdl.model.Message.class,
        // Includes: Notifcation, OneWay, RequestResponse, SolicitResponse
        org.netbeans.modules.xml.wsdl.model.Operation.class,
        // Includes: Fault, Input, Output
        org.netbeans.modules.xml.wsdl.model.OperationParameter.class,
        org.netbeans.modules.xml.wsdl.model.Part.class,
        org.netbeans.modules.xml.wsdl.model.PortType.class,
        org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType.class,
        org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role.class,
    };
    
    /**
     * Creates a new instance of WSDLMultiViewSupport.
     *
     * @param  dobj  the data object.
     */
    public WSDLMultiViewSupport(WSDLDataObject dobj) {
        this.dobj = dobj;
    }
    
    public void view(final View view, final Component component,
            final Object... parameters) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    viewInSwingThread(view, component, parameters);
                }
            });
        } else {
            viewInSwingThread(view, component, parameters);
        }
    }

    // see schemamultiviewsupport for implementation
    private void viewInSwingThread(View view, Component component,
            Object... parameters) {
        if (canView(view,component)) {
            WSDLEditorSupport editor = dobj.getWSDLEditorSupport();
            editor.open();
            if (view != null) {
                switch (view) {
                    case SOURCE:
                        WSDLMultiViewFactory.requestMultiviewActive(
                                WSDLSourceMultiviewDesc.PREFERRED_ID);
                        break;
                    case STRUCTURE:
                        WSDLMultiViewFactory.requestMultiviewActive(
                                WSDLTreeViewMultiViewDesc.PREFERRED_ID);
                        break;
                    case DESIGN:
                        WSDLMultiViewFactory.requestMultiviewActive(
                                WSDLDesignMultiViewDesc.PREFERRED_ID);
                        break;
                }
            }
            TopComponent activeTC = TopComponent.getRegistry().getActivated();
            ShowCookie showCookie = activeTC.getLookup().lookup(ShowCookie.class);
            ResultItem resultItem = null;
            if (parameters != null && parameters.length != 0) {
                for (Object o : parameters) {
                    if (o instanceof ResultItem) {
                        resultItem = (ResultItem) o;
                        break;
                    }
                }
            }
            if (showCookie != null && component != null) {
                if (resultItem == null) {
                    resultItem = new ResultItem(null, null, component, null);
                }
                showCookie.show(resultItem);
            }
        }
    }

    // see schemamultiviewsupport for implementation
    public boolean canView(ViewComponentCookie.View view, Component component) {
        if (view != null) {
            switch (view) {
            case SOURCE:
                if (!WSDLSourceMultiviewDesc.PREFERRED_ID.equals(
                        getMultiviewActive()) ||
                        !getActiveComponents().contains(component)) {
                    return true;
                }
                break;
            case STRUCTURE:
                if (!(component instanceof SchemaComponent ||
                        component instanceof WSDLComponent)) {
                    return false;
                }
                if (WSDLTreeViewMultiViewDesc.PREFERRED_ID.equals(
                        getMultiviewActive()) &&
                        getActiveComponents().contains(component)) {
                    TopComponent activeTC = TopComponent.getRegistry().getActivated();
                    WSDLDataObject wdobj = activeTC.getLookup().lookup(WSDLDataObject.class);
                    return wdobj != dobj;
                }
                return true;
            case CURRENT:
            case SUPER:
                return true;
            case DESIGN:
                //Determine if this type of component is displayed
                // in the partner view or not.
                boolean okay = false;
                for (Class type : DESIGNABLE_COMPONENTS) {
                    if (type.isInstance(component)) {
                        okay = true;
                        break;
                    }
                }
                if (WSDLDesignMultiViewDesc.PREFERRED_ID.equals(
                        getMultiviewActive()) && getActiveComponents().contains(component)) {
                    if (okay) {
                        TopComponent activeTC = TopComponent.getRegistry().getActivated();
                        WSDLDataObject wdobj = activeTC.getLookup().lookup(WSDLDataObject.class);
                        return wdobj != dobj;
                    }
                }
                return okay;
            }
        }
        return false;
    }
        
    public void show(ResultItem resultItem) {
        Component component = resultItem.getComponents();
        if (component == null || component.getModel() == null ||
                component.getModel().getState() == WSDLModel.State.NOT_WELL_FORMED) {
            view(View.SOURCE,component,resultItem);
        } else {
            if (component instanceof DocumentComponent) {
                UIUtilities.annotateSourceView(dobj, (DocumentComponent) component,
                        resultItem.getDescription(), false);
            }

            String activeMultiviewPreferredId = getMultiviewActive();
            if (WSDLTreeViewMultiViewDesc.PREFERRED_ID.equals(activeMultiviewPreferredId)) {
                view(View.STRUCTURE, component, resultItem);
            } else if (WSDLSourceMultiviewDesc.PREFERRED_ID.equals(activeMultiviewPreferredId)) {
                view(View.SOURCE, component, resultItem);
            } else if (WSDLDesignMultiViewDesc.PREFERRED_ID.equals(activeMultiviewPreferredId)) {
                if (canView(View.DESIGN, component)) {
                    view(View.DESIGN, component, resultItem);
                } else {
                    view(View.STRUCTURE, component, resultItem);
                }
            }
        }
    }

    /**
     * Finds the preferredID of active multiview element. If activated
     * TopComponent is not MultiViewTopComponent, returns null.
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
     * Finds the activated components of active TopComponent.
     *
     * @return  collection of the active components.
     */
    private static Collection<Component> getActiveComponents() {
        TopComponent activeTC = TopComponent.getRegistry().getActivated();
        Collection<Component> activeComponents = Collections.emptySet();
        for (Node node : activeTC.getActivatedNodes()) {
            Component component = node.getLookup().lookup(Component.class);
            if (component != null) {
                if (activeComponents.isEmpty()) {
                    activeComponents = new HashSet<Component>();
                }
                activeComponents.add(component);
            }
        }
        return activeComponents;
    }
}
