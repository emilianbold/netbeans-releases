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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * NavigationCaseNode.java
 *
 * Created on March 17, 2007, 9:56 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation;

import java.awt.Image;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.web.jsf.navigation.graph.PageFlowSceneElement;
import org.openide.ErrorManager;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author joelle
 */
public final class NavigationCaseEdge extends PageFlowSceneElement  {
    private final NavigationCase navCase;
    private final String toViewID;
    private final PageFlowController pc;
    
    
    
    public NavigationCaseEdge(PageFlowController pc , NavigationCase navCase) {
        super();
        this.navCase = navCase;
        //        toViewID = navCase.getToViewId();
        toViewID = FacesModelUtility.getToViewIdFiltered(navCase);
        this.pc = pc;
        
        //            createProperties(navCase, new NavigationCaseBeanInfo());
    }
    
    @Override
    public String toString() {
        return new String("NavigationCaseEdge[FromOutcome=" + getFromOuctome() + " ToViewId="+ getToViewId() + " FromViewId="+ getFromViewId() + "] ");
    }
    
    
    public String getToViewId() {
        //        assert  navCase.getToViewId().equals(toViewID);
        return toViewID;
    }
    
    public String getFromOuctome() {
        if( navCase != null && navCase.getModel() != null ) {
            return navCase.getFromOutcome();
        }
        return null;
    }
    
    public String getFromAction() {
        if( navCase != null && navCase.getModel() != null ) {
            return navCase.getFromAction();
        }
        return null;
    }
    
    public boolean isRedirected() {
        return navCase.isRedirected();
    }
    
    public String getFromViewId() {
        if( navCase !=  null  && navCase.getModel() != null ) {
            NavigationRule navRule = (NavigationRule)(navCase.getParent());
            if( navRule != null ) {
                //                return navRule.getFromViewId();
                return FacesModelUtility.getFromViewIdFiltered(navRule);
            }
        }
        return null;
    }
    
    
    
    
    public boolean canRename() {
        return true;
    }
    
    
    public void setName(String newName) {
        Pin pin = pc.getView().getEdgeSourcePin(this);
        if( pin != null && !pin.isDefault()){
            pin.setFromOutcome(newName);
        }        
        pc.setModelNavigationCaseName(navCase, newName);        
        super.setName(newName);
    }
    
    public String getName() {
        if( navCase.getModel() != null ) {
            return ( navCase.getFromOutcome() != null ? navCase.getFromOutcome() : navCase.getFromAction());
        }
        return "";
    }
    
    public boolean canDestroy() {
        return true;
    }
    
    public void destroy() throws IOException {
        boolean deleteRuleTo = false;
        pc.removeModelNavigationCase(navCase);        
        if( navNode != null ){
            navNode.destroy();
        }
    }
    
    
    public HelpCtx getHelpCtx() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Image getIcon(int type) {
        return null;
    }
    
    public Node getNode() {
        if( navNode == null ) {
            navNode = new NavNode(this);
        }
        return navNode;
    }
    
    Node navNode;
    private class NavNode extends AbstractNode{
        NavigationCaseEdge edge;
        public NavNode(NavigationCaseEdge edge) {
            super(Children.LEAF);
            this.edge = edge;
        }
        
        @Override
        protected Sheet createSheet() {
            Sheet s = Sheet.createDefault();
            Set ss = s.get("general"); // NOI18N
            if (ss == null) {
                ss = new Sheet.Set();
                ss.setName("general"); // NOI18N
                ss.setDisplayName(NbBundle.getMessage(NavigationCaseEdge.class, "General")); // NOI18N
                ss.setShortDescription(NbBundle.getMessage(NavigationCaseEdge.class, "GeneralHint")); // NOI18N
                s.put(ss);
            }
            Set gs = ss;
            
            try {
                PropertySupport.Reflection<String> p;
                
                p = new ModelProperty(navCase, String.class, "getFromOutcome", "setFromOutcome"); // NOI18N
                p.setName("fromOutcome"); // NOI18N
                p.setDisplayName(NbBundle.getMessage(NavigationCaseEdge.class, "Outcome")); // NOI18N
                p.setShortDescription(NbBundle.getMessage(NavigationCaseEdge.class, "OutcomeHint")); // NOI18N
                ss.put(p);
                
                p = new ModelProperty(navCase, String.class, "getFromAction", "setFromAction"); // NOI18N
                p.setName("fromView"); // NOI18N
                p.setDisplayName(NbBundle.getMessage(NavigationCaseEdge.class, "FromAction")); // NOI18N
                p.setShortDescription(NbBundle.getMessage(NavigationCaseEdge.class, "FromActionHint")); // NOI18N
                //                p.setValue(PageSelector.PROPERTY_NAVDOC, document);
                //                p.setPropertyEditorClass(PageSelector.class);
                ss.put(p);
                
                p = new ModelProperty(navCase, String.class, "getToViewId", "setToViewId"); // NOI18N
                p.setName("toViewId"); // NOI18N
                p.setDisplayName(NbBundle.getMessage(NavigationCaseEdge.class, "ToViewId")); // NOI18N
                p.setShortDescription(NbBundle.getMessage(NavigationCaseEdge.class, "ToViewHint")); // NOI18N
                //                p.setValue(PageSelector.PROPERTY_NAVDOC, document);
                //                p.setPropertyEditorClass(PageSelector.class);
                ss.put(p);
                
            } catch (NoSuchMethodException nsme) {
                ErrorManager.getDefault().notify(nsme);
            }
            
            return s;
        }
        
//        public void save() throws IOException {
//            //            pc.getConfigDataObject().getEditorSupport().saveDocument();
//            getCookie(SaveCookie.class).save();
//            
//            pc.serializeNodeLocations();
//        }
        
        
        @SuppressWarnings("unchecked")
        public <T extends Cookie> T getCookie(Class<T> type) {
            if( type.equals(SaveCookie.class)) {
                pc.serializeNodeLocations();
                return pc.getConfigDataObject().getCookie(type);
            } else if ( type.equals(OpenCookie.class)){
                return (T) new OpenCookie() {
                    public void open() {
                        pc.openNavigationCase(edge);
                    }
                };
            }
            return null;
        }

        @Override
        public boolean canRename() {
            return isModifiable();
        }

        @Override
        public String getName() {            
            return edge.getName();
        }

        @Override
        public void setName(String s) {            
            super.setName(s);
            edge.setName(s);
        }
        
        
    }
    
    
    public class ModelProperty extends PropertySupport.Reflection<String>{
        
        public ModelProperty(Object instance, Class<String> valueType, String getter, String setter ) throws NoSuchMethodException {
            super(instance, valueType, getter, setter);
        }
        
        @Override
        public void setValue(String val) throws IllegalAccessException,
                IllegalArgumentException,
                InvocationTargetException {
            
            JSFConfigModel model = navCase.getModel();
                model.startTransaction();
                super.setValue(val);
                model.endTransaction();
            try {
                model.sync();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            
        }
        
    }
    

}
