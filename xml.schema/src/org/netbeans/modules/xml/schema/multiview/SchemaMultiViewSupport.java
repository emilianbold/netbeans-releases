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

package org.netbeans.modules.xml.schema.multiview;

import java.awt.EventQueue;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.schema.SchemaDataObject;
import org.netbeans.modules.xml.schema.SchemaEditorSupport;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ui.cookies.GetComponentCookie;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie.View;
import org.netbeans.modules.xml.validation.ShowCookie;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.nodes.Node;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/**
 * Class for creating the Schema Multiview and implementation
 * of ViewComponentCookie, which has methods to open the multiview elements
 * of the Schema Multiview. The instance of this class is in the
 * SchemaDataObject cookie set.
 *
 *
 *
 * @author Jeri Lockhart
 * @author Ajit Bhate
 */
public class SchemaMultiViewSupport implements ViewComponentCookie, ShowCookie {
    public static final String SCHEMA_EDITOR_HELP_ID = "SCHEMA_EDITOR_HELP_ID";
    /** The dataobject it supports. */
    private SchemaDataObject dobj;
    
    /**
     * Constructor
     */
    public SchemaMultiViewSupport(SchemaDataObject dobj) {
        this.dobj = dobj;
    }
    
    /**
     * Create the Schema Multiview
     */
    public static CloneableTopComponent createMultiView(
            SchemaDataObject schemaDataObject) {
        MultiViewDescription views[] = new MultiViewDescription[3];
        
        // Put the source element first so that client code can find its
        // CloneableEditorSupport.Pane implementation.
        views[0] = getSchemaSourceMultiviewDesc(schemaDataObject);
        views[1] = getSchemaColumnViewMultiViewDesc(schemaDataObject);
        views[2] = getSchemaABEMultiviewDesc(schemaDataObject);
        
        
        // Make the column view the default element.
        CloneableTopComponent multiview =
                MultiViewFactory.createCloneableMultiView(
                views,
                views[0],
                new SchemaEditorSupport.CloseHandler(schemaDataObject));
        
        // This handles the "show file extensions" option automatically.
        String name = schemaDataObject.getNodeDelegate().getDisplayName();
        multiview.setDisplayName(name);
        multiview.setName(name);
        return multiview;
    }
    
    private static MultiViewDescription getSchemaColumnViewMultiViewDesc(
            final SchemaDataObject schemaDataObject) {
        return new SchemaColumnViewMultiViewDesc(schemaDataObject);
    }
    
    
    private static MultiViewDescription getSchemaSourceMultiviewDesc(
            final SchemaDataObject schemaDataObject) {
        return new SchemaSourceMultiViewDesc(schemaDataObject);
    }
    
    private static MultiViewDescription getSchemaABEMultiviewDesc(
            final SchemaDataObject schemaDataObject) {
        return new SchemaABEViewMultiViewDesc(schemaDataObject);
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
     * has been opened (i.e. SchemaEditorSupport.open()) so the TopComponent
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
            if(view==View.SUPER) {
                // preserve the view
                TopComponent activeTC = TopComponent.getRegistry().getActivated();
                SchemaDataObject sdobj = activeTC.getLookup().
                        lookup(SchemaDataObject.class);
                if(dobj!=sdobj) // must be multifile
                {
                    String activeMVTCId = getMultiviewActive();
                    view = View.STRUCTURE;
                    if(SchemaABEViewMultiViewDesc.PREFERRED_ID.equals(activeMVTCId))
                        view = View.DESIGN;
                    else if(SchemaSourceMultiViewDesc.PREFERRED_ID.equals(activeMVTCId))
                        view = View.SOURCE;
                }
            }
            SchemaEditorSupport editor = dobj.getSchemaEditorSupport();
            editor.open();
            // activate view if needed
            switch (view) {
                case STRUCTURE:
                    if(component instanceof AXIComponent)
                        component = ((AXIComponent)component).getPeer();
                    requestMultiviewActive(
                            SchemaColumnViewMultiViewDesc.PREFERRED_ID);
                    break;
                case DESIGN:
                    requestMultiviewActive(
                            SchemaABEViewMultiViewDesc.PREFERRED_ID);
                    break;
                case SOURCE:
                    if(component instanceof AXIComponent)
                        component = ((AXIComponent)component).getPeer();
                    requestMultiviewActive(
                            SchemaSourceMultiViewDesc.PREFERRED_ID);
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
            SchemaDataObject sdobj = activeTC.getLookup().
                    lookup(SchemaDataObject.class);
            if(dobj==sdobj) { // we can preserve the view
                String activeMVTCId = getMultiviewActive();
                if(SchemaABEViewMultiViewDesc.PREFERRED_ID.equals(activeMVTCId)) {
                    AXIModel axiModel = null;
                    if(component instanceof AXIComponent) {
                        axiModel = ((AXIComponent)component).getModel();
                    } else if(component instanceof SchemaComponent) {
                        axiModel = AXIModelFactory.getDefault().getModel(
                                ((SchemaComponent)component).getModel());
                    }
                    if(axiModel!=null && axiModel.getRoot() != null &&
                            axiModel.getState()==AXIModel.State.VALID) {
                        view = View.DESIGN;
                    }
                }
                else if(SchemaColumnViewMultiViewDesc.PREFERRED_ID.equals(activeMVTCId))
                    view = View.STRUCTURE;
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
                if(!SchemaSourceMultiViewDesc.PREFERRED_ID.equals(
                        getMultiviewActive()) ||
                        !getActiveComponents().contains(component)) {
                    if(component instanceof AXIComponent)
                        return ((AXIComponent)component).getPeer()!=null;
                    return true;
                } else return false;
            case STRUCTURE:
                if(!(component instanceof SchemaComponent ||
                        component instanceof AXIComponent))
                    return false;
                if(component instanceof AXIComponent &&
                        ((AXIComponent)component).getPeer()==null)
                    return false;
                if(SchemaColumnViewMultiViewDesc.PREFERRED_ID.equals(
                        getMultiviewActive()) &&
                        getActiveComponents().contains(component)) {
                    TopComponent activeTC = TopComponent.getRegistry().getActivated();
                    SchemaDataObject sdobj = activeTC.getLookup().
                            lookup(SchemaDataObject.class);
                    return sdobj!=dobj;
                } else return true;
            case DESIGN:
                if(SchemaABEViewMultiViewDesc.PREFERRED_ID.equals(
                        getMultiviewActive()))
                    return false;
                AXIModel axiModel = null;
                if(component instanceof AXIComponent) {
                    axiModel = ((AXIComponent)component).getModel();
                } else if(component instanceof SchemaComponent) {
                    axiModel = AXIModelFactory.getDefault().getModel(
                            ((SchemaComponent)component).getModel());
                    if(!axiModel.canView((SchemaComponent)component))
                        return false;
                }
                if(axiModel!=null && axiModel.getRoot() != null &&
                        axiModel.getState()==AXIModel.State.VALID) {
                    return true;
                }
                return false;
            case SUPER:
                return true;
        }
        return false;
    }
    
}
