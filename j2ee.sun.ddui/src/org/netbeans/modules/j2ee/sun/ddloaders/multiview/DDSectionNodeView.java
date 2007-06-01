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
 */
package org.netbeans.modules.j2ee.sun.ddloaders.multiview;

import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.NamedBeanGroupNode;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;


/**
 * @author Peter Williams
 */
public class DDSectionNodeView extends SectionNodeView {
    
    protected RootInterface rootDD;
    protected ASDDVersion version;
    
    public DDSectionNodeView(SunDescriptorDataObject dataObject) {
        super(dataObject);
        
        rootDD = dataObject.getDDRoot();
        version = DDProvider.getASDDVersion(rootDD);
    }
    
    /** API to set the child nodes (subpanels) of this view node without creating
     *  an extra top level root node.
     */
    public void setChildren(SectionNode [] children) {
        int size = children.length;
        if(size > 0) {
            setRootNode(children[0]);
            
            if(--size > 0) {
                SectionNode [] remainingNodes = new SectionNode[size];
                System.arraycopy(children, 1, remainingNodes, 0, size);
                
                Node rootNode = getRoot();
                rootNode.getChildren().add(remainingNodes);
                for(int i = 0; i < size; i++) {
                    addSection(remainingNodes[i].getSectionNodePanel());
                }
            }
        }
    }
    
    @Override
    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        super.dataModelPropertyChange(source, propertyName, oldValue, newValue);
    }
    
    public XmlMultiViewDataSynchronizer getModelSynchronizer() {
        return ((SunDescriptorDataObject) getDataObject()).getModelSynchronizer();
    }
    
    @Override
    public void refreshView() {
        checkChildren();
        super.refreshView();
    }
    
    /** Override this if required by derived classes.  Called before refreshView()
     *  to ensure child nodes are up to date.
     */
    protected void checkChildren() {
        // As long as NamedGroups have setExpanded = true, this is required to
        // ensure initialization of the child nodes in the group.
        final Children children = getRoot().getChildren();
        final Node[] nodes = children.getNodes();
        for(Node node: nodes) {
            if(node instanceof NamedBeanGroupNode) {
                ((NamedBeanGroupNode) node).checkChildren(null);
            }
        }
    }
    
}
