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

import java.util.LinkedList;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;


/**
 * @author Peter Williams
 */
public class DDSectionNodeView extends SectionNodeView {
    
    protected RootInterface rootDD;
    protected ASDDVersion version;
    
    public DDSectionNodeView(SunDescriptorDataObject dataObject) {
        super(dataObject);
        
        rootDD = dataObject.getDDRoot();
        version = dataObject.getASDDVersion();
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
    
    public void setChildren(LinkedList<SectionNode> children) {
        if(children.peek() != null) {
            SectionNode firstNode = children.removeFirst();
            setRootNode(firstNode);

            if(children.peek() != null) {
                SectionNode [] remainingNodes = children.toArray(new SectionNode[0]);
                
                Node rootNode = getRoot();
                rootNode.getChildren().add(remainingNodes);
                for(int i = 0; i < remainingNodes.length; i++) {
                    addSection(remainingNodes[i].getSectionNodePanel());
                }
            }
        }
    }
    
    public XmlMultiViewDataSynchronizer getModelSynchronizer() {
        return ((SunDescriptorDataObject) getDataObject()).getModelSynchronizer();
    }
    
    // ------------------------------------------------------------------------
    // Overrides required to properly support multiple rootNodes
    //   Taken from SectionNodeView and enhanced for to handle rootNode[]
    // ------------------------------------------------------------------------
    private final RequestProcessor.Task ddRefreshTask = RequestProcessor.getDefault().create(new Runnable() {
        public void run() {
            refreshView();
        }
    });

    private static final int DD_REFRESH_DELAY = 20;
    
    @Override
    public void refreshView() {
        Node [] rootNodes = getRoot().getChildren().getNodes();
        if(rootNodes != null) {
            for(Node n: rootNodes) {
                if(n instanceof SectionNode) {
                    ((SectionNode) n).refreshSubtree();
                }
            }
        }
    }
    
    @Override
    public void scheduleRefreshView() {
        ddRefreshTask.schedule(DD_REFRESH_DELAY);
    }
    
    @Override
    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
//        System.out.println("DDSectionNodeView [" + this.getClass().getSimpleName() + "] .dataModelPropertyChange: " + 
//                source + ", " + propertyName + ", " + oldValue + ", " + newValue);
        Node [] rootNodes = getRoot().getChildren().getNodes();
        if(rootNodes != null) {
            for(Node n: rootNodes) {
                if(n instanceof SectionNode) {
                    ((SectionNode) n).dataModelPropertyChange(source, propertyName, oldValue, newValue);
                }
            }
        }
    }

//    /** Override this if required by derived classes.  Called before refreshView()
//     *  to ensure child nodes are up to date.
//     */
//    protected void checkChildren() {
//        // As long as NamedGroups have setExpanded = true, this is required to
//        // ensure initialization of the child nodes in the group.
//        final Children children = getRoot().getChildren();
//        final Node[] nodes = children.getNodes();
//        for(Node node: nodes) {
//            if(node instanceof NamedBeanGroupNode) {
//                System.out.println(node.getClass().getSimpleName() + ".checkChildren() called by " + this.getClass().getSimpleName() + ".checkChildren()");
//                ((NamedBeanGroupNode) node).checkChildren(null);
//            }
//        }
//    }
    
}
