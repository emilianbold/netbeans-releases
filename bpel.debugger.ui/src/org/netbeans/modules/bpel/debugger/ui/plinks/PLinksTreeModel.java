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

package org.netbeans.modules.bpel.debugger.ui.plinks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
import org.netbeans.modules.bpel.debugger.api.RuntimePartnerLink;
import org.netbeans.modules.bpel.debugger.api.pem.PemEntity;
import org.netbeans.modules.bpel.debugger.api.pem.ProcessExecutionModel;
import org.netbeans.modules.bpel.debugger.ui.plinks.models.EndpointWrapper;
import org.netbeans.modules.bpel.debugger.ui.plinks.models.PartnerLinkWrapper;
import org.netbeans.modules.bpel.debugger.ui.plinks.models.RoleRefWrapper;
import org.netbeans.modules.bpel.debugger.ui.plinks.models.RoleRefWrapper.RoleType;
import org.netbeans.modules.bpel.debugger.ui.util.VariablesUtil;
import org.netbeans.modules.bpel.debugger.ui.util.XmlUtil;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkContainer;
import org.netbeans.modules.bpel.model.api.Scope;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Tree model supporting the BPEL Partner Links view.
 * 
 * @author Kirill Sorokin
 */
public class PLinksTreeModel implements TreeModel {
    
    private BpelDebugger myDebugger;
    private ProcessInstance myInstance;
    
    private PositionListener myListener;
    private Vector myListeners = new Vector();
    
    /**
     * Creates a new instance of PLinksTreeModel.
     *
     * @param contextProvider debugger context
     */
    public PLinksTreeModel(
            final ContextProvider contextProvider) {
        myDebugger = contextProvider.lookupFirst(null, BpelDebugger.class);
    }
    
    /**{@inheritDoc}*/
    public Object getRoot() {
        return ROOT;
    }
    
    /**{@inheritDoc}*/
    public Object[] getChildren(
            final Object object, 
            final int from, 
            final int to) throws UnknownTypeException {
        
        if (myInstance == null) {
            if (object.equals(ROOT)) {
                return new Object[] {
                    new Dummy()
                };
            }
            
            if (object instanceof Dummy) {
                return new Object[0];
            }
            
            throw new UnknownTypeException(object);
        }
        
        if (ROOT.equals(object)) {
            return getPartnerLinks();
        }
        
        if (object instanceof PartnerLinkWrapper) {
            final List<Object> children = new ArrayList<Object>(3);
            final PartnerLinkWrapper pLink = (PartnerLinkWrapper) object;
            
            if (pLink.getMyRoleRef() != null) {
                children.add(new RoleRefWrapper(
                        pLink.getMyRoleRef(), RoleType.MY));
            }
            
            if (pLink.getPartnerRoleRef() != null) {
                children.add(new RoleRefWrapper(
                        pLink.getPartnerRoleRef(), RoleType.PARTNER));
                        
                children.add(new EndpointWrapper(pLink));
            }
            
            return children.toArray();
        }
        
        if (object instanceof RoleRefWrapper) {
            return new Object[0];
        }
        
        if (object instanceof EndpointWrapper) {
            final EndpointWrapper epWrapper = (EndpointWrapper) object;
            final String value = epWrapper.getSerializedValue();
            
            if (value != null) {
                final Element element = XmlUtil.parseXmlElement(value);
                
                if (element != null) {
                    return new Object[]{element};
                } else {
                    return new Object[0];
                }
            }
        }
        
        if (object instanceof Node) {
            if (object instanceof Element) {
                final Element element = (Element) object;
                final NamedNodeMap attributes = element.getAttributes();
                final NodeList childNodes = element.getChildNodes();
                
                final List<Object> children = new LinkedList<Object>();
                
                for (int i = 0; i < attributes.getLength(); i++) {
                    final Node childNode = attributes.item(i);
                    
                    if (childNode.getNodeName().startsWith("xmlns")) { // NOI18N
                        continue;
                    }
                    
                    children.add(childNode);
                }
                
                if ((childNodes.getLength() > 1) || 
                        (childNodes.item(0).getNodeType() != Node.TEXT_NODE)) {
                    for (int i = 0; i < childNodes.getLength(); i++) {
                        final Node childNode = childNodes.item(i);
                        
                        if ((childNode.getNodeType() == Node.TEXT_NODE) &&
                                childNode.getNodeValue().trim().equals("")) {
                            continue;
                        }
                        
                        children.add(childNode);
                    }
                }
                
                return children.toArray();
            }
            
            return new Object[0];
        }
        
        throw new UnknownTypeException(object);
    }
    
    /**{@inheritDoc}*/
    public int getChildrenCount(
            final Object object) throws UnknownTypeException {
        return getChildren(object, 0, 0).length;
    }
    
    /**{@inheritDoc}*/
    public boolean isLeaf(
            final Object object) throws UnknownTypeException {
        return getChildrenCount(object) == 0;
    }
    
    /**{@inheritDoc}*/
    public void addModelListener(
            final ModelListener listener) {
        
        myListeners.add(listener);
        
        if ((myListener == null) && (myDebugger != null)) {
            myListener = new PositionListener(this, myDebugger);
        }
    }
    
    /**{@inheritDoc}*/
    public void removeModelListener(
            final ModelListener listener) {
        
        myListeners.remove(listener);
        
        if ((myListeners.size() == 0) && (myListener != null)) {
            myListener.destroy();
            myListener = null;
        }
    }
    
    // Package /////////////////////////////////////////////////////////////////
    void setProcessInstance(
            final ProcessInstance instance) {
        myInstance = instance;
    }
    
    // Private /////////////////////////////////////////////////////////////////
    private void fireTreeChanged() {
        final Vector clone = (Vector) myListeners.clone();
        
        for (int i = 0; i < clone.size(); i++) {
            ((ModelListener) clone.get(i)).modelChanged(
                    new ModelEvent.TreeChanged(this));
        }
    }
    
    private Object[] getPartnerLinks() {
        if (myInstance == null) {
            return new Object[0];
        }
        
        final PartnerLink[] pLinks = getStaticPartnerLinks();
        final RuntimePartnerLink[] rLinks = 
                myInstance.getRuntimePartnerLinks();
        
        final PartnerLinkWrapper[] result = 
                new PartnerLinkWrapper[pLinks.length];
        
        for (int i = 0; i < pLinks.length; i++) {
            result[i] = new PartnerLinkWrapper(pLinks[i], null);
            
            for (int j = 0; j < rLinks.length; j++) {
                if (rLinks[j].getName().equals(pLinks[i].getName())) {
                    result[i] = new PartnerLinkWrapper(pLinks[i], rLinks[j]);
                    break;
                }
            }
        }
        
        return result;
    }
    
    private PartnerLink[] getStaticPartnerLinks() {
        final VariablesUtil helper = new VariablesUtil(myDebugger);
        final BpelModel model = helper.getBpelModel();
        
        if (model == null) {
            return new PartnerLink[0];
        }
        
        final List<PartnerLink> pLinks = new LinkedList<PartnerLink>();
        
        // Add the variables from the process
        PartnerLinkContainer pLinksContainer = 
                model.getProcess().getPartnerLinkContainer();
        if ((pLinksContainer != null) && 
                (pLinksContainer.sizeOfPartnerLink() > 0)) {
            
            pLinks.addAll(Arrays.asList(pLinksContainer.getPartnerLinks()));
        }
        
        final ProcessInstance currentInstance = 
                myDebugger.getCurrentProcessInstance();
        if (currentInstance == null) {
            return pLinks.toArray(new PartnerLink[pLinks.size()]);
        }
        
        final ProcessExecutionModel peModel = 
                currentInstance.getProcessExecutionModel();
        if (peModel == null) {
            return pLinks.toArray(new PartnerLink[pLinks.size()]);
        }
        
        final PemEntity lastStartedEntity = peModel.getLastStartedEntity();
        if (lastStartedEntity == null) {
            return pLinks.toArray(new PartnerLink[pLinks.size()]);
        }
        
        final String xpath = lastStartedEntity.getPsmEntity().getXpath();
        
        int scopeIndex = xpath.indexOf("scope"); // NOI18N
        while (scopeIndex != -1) {
            final int index = xpath.indexOf("/", scopeIndex); // NOI18N
            
            final String scopeXpath = 
                    index == -1 ? xpath : xpath.substring(0, index);
            
            final Scope scope = helper.getScopeEntity(scopeXpath);
            if (scope != null) {
                pLinksContainer = scope.getPartnerLinkContainer();
                if ((pLinksContainer != null) && 
                        (pLinksContainer.sizeOfPartnerLink() > 0)) {
                        
                    pLinks.addAll(
                            Arrays.asList(pLinksContainer.getPartnerLinks()));
                }
            }
            
            scopeIndex = index == -1 ? 
                    index : 
                    xpath.indexOf("scope", index); // NOI18N
        }
        
        return pLinks.toArray(new PartnerLink[pLinks.size()]);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private static class PositionListener implements PropertyChangeListener {
        
        private BpelDebugger myDebugger;
        private WeakReference myModel;
        
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        
        private PositionListener(
                final PLinksTreeModel model, 
                final BpelDebugger debugger) {
            myDebugger = debugger;
            myModel = new WeakReference(model);
            
            myDebugger.addPropertyChangeListener(this);
            getModel().setProcessInstance(
                    myDebugger.getCurrentProcessInstance());
        }

        private void destroy() {
            myDebugger.removePropertyChangeListener(this);
            
            if (task != null) {
                // cancel old task
                task.cancel();
                task = null;
            }
        }

        private PLinksTreeModel getModel() {
            final PLinksTreeModel model =
                    (PLinksTreeModel) myModel.get();
            
            if (model == null) {
                destroy();
            }
            
            return model;
        }
        
        /**{@inheritDoc}*/
        public void propertyChange(
                final PropertyChangeEvent event) {
            
            if (BpelDebugger.PROP_CURRENT_PROCESS_INSTANCE.equals(
                    event.getPropertyName())) {
                getModel().setProcessInstance(
                        myDebugger.getCurrentProcessInstance());
            }
            
            if (BpelDebugger.PROP_CURRENT_POSITION.equals(
                    event.getPropertyName())) {
                final PLinksTreeModel model = getModel();
                
                if (model == null) {
                    return;
                }
                
                if (task != null) {
                    // cancel old task
                    task.cancel();
                    task = null;
                }
                
                task = RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        model.fireTreeChanged();
                    }
                }, 500);
            }
        }
    }
    
    static class Dummy {
        // Empty, stub class
    }
}
