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


package org.netbeans.modules.bpel.design.model.patterns;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bpel.design.geometry.FBounds;
import org.netbeans.modules.bpel.design.model.elements.InvokeOperationElement;
import org.netbeans.modules.bpel.design.model.elements.ReceiveOperationElement;

import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.bpel.design.layout.LayoutManager;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.elements.ProcessBorder;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.modules.bpel.nodes.synchronizer.ModelSynchronizer;
import org.netbeans.modules.bpel.nodes.synchronizer.SynchronisationListener;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.ErrorManager;
import org.openide.util.WeakListeners;
//import org.netbeans.modules.xml.xam.GlobalReference;

/**
 *
 * @author Alexey Yarmolenko
 */
public class PartnerlinkPattern extends CompositePattern implements SynchronisationListener {
    
    
//    private HashMap<VisualElement, PortType> porttypes =
//            new HashMap<VisualElement, PortType>();
//
    private ModelSynchronizer synchronizer;
    
    public PartnerlinkPattern(DiagramModel model) {
        super(model);
        synchronizer = new ModelSynchronizer(this);
    }
    
    
    public VisualElement getFirstElement() {
        return null;
    }
    
    public VisualElement getLastElement() {
        return null;
    }
    
    public boolean isDraggable() {
        return false;
    }
    
    protected void onAppendPattern(Pattern p) {}
    protected void onRemovePattern(Pattern p) {}
    
    
    public Operation getOperation(VisualElement e){
        String name = e.getText();
        PartnerLink pl = (PartnerLink) getOMReference();
        
        if (pl.getPartnerLinkType() == null){
            return null;
        }
        
        PartnerLinkType plt = pl.getPartnerLinkType().get();
        
        if (plt == null){
            return null;
        }
        
        
        Operation result = findOperationInRole(plt.getRole1(), name);
        
        return (result != null)?
            result : findOperationInRole(plt.getRole2(), name);
    }
    
    public VisualElement getElement(WSDLReference<Operation> op_ref){
        String name = op_ref.getRefString();
        for (VisualElement e: getElements()){
            if (name.equals(e.getText())){
                return e;
            }
        }
        return null;
        
    }
    
    public void setParent(CompositePattern newParent) {
        super.setParent(newParent);
        if (newParent == null){
            synchronizer.unsubscribe();
        }
    }
    
    
    
    public FBounds layoutPattern(LayoutManager manager) {
        
        Collection<Pattern> patterns = getNestedPatterns();
        
        double ypos = 0;
        
        double x, y = 0, width = 0, height;
        
        for (VisualElement e : getElements()){
            e.setLocation(-e.getWidth() / 2, ypos);
            ypos += e.getHeight() + LayoutManager.VSPACING;
            width = Math.max(width, e.getWidth());
        }
        
        height = ypos - LayoutManager.VSPACING;
        
        width = Math.max(width, 72);
        height = Math.max(height, 24);
        
        x = -width / 2;
        getBorder().setClientRectangle(x, y, width, height);
        
        return getBorder().getBounds();
    }
    
    
    public void reloadOperations(){
        PartnerLink pl = (PartnerLink)getOMReference();
        
        if (pl == null){
            //this may happen if orphaned pattern still listens to WSDL model changes
            return;
        }
        
        getBorder().setLabelText(pl.getName());
        
        
        
        removeAllElements();
        
        if (pl.getPartnerLinkType() == null){
            return;
        }
        
        if (pl.isInDocumentModel()){
            PartnerLinkType plt = pl.getPartnerLinkType().get();
            
            
            if (plt != null) {
                synchronizer.subscribe(plt.getModel());
                processPortType(plt.getRole1(),  pl.getMyRole(), pl.getPartnerRole());
                processPortType(plt.getRole2(),  pl.getMyRole(), pl.getPartnerRole());
            }
        }
        
    }
    
    
    protected void createElementsImpl() {
        PartnerLink pl = (PartnerLink)getOMReference();
        
        setBorder(new ProcessBorder());
        registerTextElement(getBorder());
        reloadOperations();
    }
    
    
    private void processPortType(Role role, WSDLReference<Role> myRole, WSDLReference<Role> partnerRole){
        if (role == null) {
            return;
        }
        
        String roleName = role.getName();
        if (roleName == null){
            return;
        }
        //
        
        boolean isMyRole = false;
        
        if (myRole != null){
            isMyRole = myRole.references(role);
        } else if (partnerRole !=null){
            isMyRole = !partnerRole.references(role);
        } else {
            //ignore PLs which have no roles settings;
            return;
        }
        
        NamedComponentReference<PortType> ptReference = role.getPortType();
        if (ptReference == null) {
            return;
        }
        //
        PortType ptype = ptReference.get();
        if (ptype == null) {
            return;
        }
        
        
        //
        for (Operation op: ptype.getOperations()) {
            VisualElement e = (isMyRole)
                    ? new InvokeOperationElement()
                    : new ReceiveOperationElement();
            
            e.setLabelText(op.getName());
            appendElement(e);
            
        }
    }
    
    
    public NodeType getNodeType() {
        return NodeType.PARTNER_LINK;
        
    }
    
    
    public void reconnectElements(){
        List<Connection> scheduledForRemove = new ArrayList<Connection>();
        
        for(VisualElement element : getElements()) {
            for (Connection connection : element.getOutcomingConnections()) {
                if (isElementDisconnected(connection.getTarget())) {
                    scheduledForRemove.add(connection);
                }
            }
            
            for (Connection connection : element.getIncomingConnections()) {
                if (isElementDisconnected(connection.getSource())) {
                    scheduledForRemove.add(connection);
                }
            }
        }
        
        for (Connection connection : scheduledForRemove) {
            connection.remove();
        }
    }
    
    
    private boolean isElementDisconnected(VisualElement element) {
//        for (Pattern pattern = element.getPattern();
//        !(pattern instanceof ProcessPattern);
//        pattern = pattern.getParent()) {
//            if (pattern == null) {
//                return true;
//            }
//        }
//
        return false;
    }
    
    private Operation findOperationInRole(Role role, String name) {
        if (role == null){
            return null;
        }
        
        if (role.getPortType() == null){
            return null;
        }
        
        PortType pt = role.getPortType().get();
        Collection<Operation> ops = pt.getOperations();
        for (Operation op: ops){
            if (name.equals(op.getName())){
                return op;
            }
        }
        return null;
    }
    
    
    
    private void updatePartnerLinkType(){
        //fix for bug xxx: check if this pattern belongs to dead view
        if (getModel().getView() == null || getParent() == null){
            return;
        }
        try {
            reloadOperations();
            getModel().getView().diagramChanged();
        } catch (Exception ex){
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
        }
    }
    
    public void componentUpdated(Component component) {
        updatePartnerLinkType();
    }
    
    public void childrenUpdated(Component component) {
        updatePartnerLinkType();
    }
    
    
    
}
