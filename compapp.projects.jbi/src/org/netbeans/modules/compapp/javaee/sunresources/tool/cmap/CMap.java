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

package org.netbeans.modules.compapp.javaee.sunresources.tool.cmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 * @author echou
 *
 */
public class CMap {
    
    private String name;
    
    /* contains list of nodes in the CMap, entry in map
     * <Class, CMapNode>, key is "qualified classname"
     */
    private HashMap<String, CMapNode> nodes = new HashMap<String, CMapNode> ();
    
    /* contains list of resource nodes in the CMap, entry in map
     * <String, ResourceNode>, key is "resType+resJndiName"
     */
    private HashMap<String, ResourceNode> resNodes = 
        new HashMap<String, ResourceNode> ();
    
    public CMap(String name) {
        this.name = name;
    }

    /**
     * @param cls
     * @return
     */
    public boolean hasNode(String clsName) {
        return nodes.containsKey(clsName);
    }
    
    /**
     * @param cls
     */
    public CMapNode findNode(String clsName) {
        return nodes.get(clsName);
    }
    
    /**
     * @param cls
     * @param node
     */
    public void addNode(String clsName, CMapNode node) {
        nodes.put(clsName, node);
    }
    
    public Iterator<CMapNode> getNodes() {
        return nodes.values().iterator();
    }
    
    public ResourceNode findResNode(ResourceDepend resDepend) {
        return resNodes.get(resDepend.getTargetResType() + "+" + // NOI18N
                resDepend.getTargetResJndiName());
    }
    
    public ResourceNode findResNodeByLogicalName(String logicalName) {
        for (Iterator<ResourceNode> iter = resNodes.values().iterator(); iter.hasNext(); ) {
            ResourceNode resNode = iter.next();
            if (resNode.getLogicalName().equals(logicalName)) {
                return resNode;
            }
        }
        return null;
    }
    
    public void addResNode(ResourceNode resNode) {
        resNodes.put(resNode.getResType() + "+" + resNode.getResJndiName(),  // NOI18N
                resNode);
    }
    
    public Iterator<ResourceNode> getResNodes() {
        return resNodes.values().iterator();
    }
    
    public void postProcess() throws Exception {
        // find all the target node for EJB dependency
        for (Iterator<CMapNode> iter = getNodes(); iter.hasNext(); ) {
            CMapNode node = iter.next();
            ArrayList<EJBDepend> ejbDepends = node.getEjbDepends();
            for (int i = 0; i < ejbDepends.size(); i++) {
                EJBDepend ejbDepend = ejbDepends.get(i);
                CMapNode targetNode = findEJBNodeImplIntf(ejbDepend.getTargetIntfName());
                ejbDepend.setTarget(targetNode);
            }
        }
        
        // find all the target node for Resource dependency, if none exists,
        // create one, since resource nodes are abstract
        for (Iterator<CMapNode> iter = getNodes(); iter.hasNext(); ) {
            CMapNode node = iter.next();
            ArrayList<ResourceDepend> resDepends = node.getResDepends();
            for (int i = 0; i < resDepends.size(); i++) {
                ResourceDepend resDepend = resDepends.get(i);
                ResourceNode resNode = findResNode(resDepend);
                if (resNode == null) {
                    String logicalName;
                    if (resDepend.getMappedName() == null || 
                            resDepend.getMappedName().equals("")) { // NOI18N
                        logicalName = resDepend.getTargetResJndiName();
                    } else {
                        logicalName = resDepend.getMappedName();
                    }
                    resNode = new ResourceNode(logicalName, 
                            resDepend.getTargetResType(),
                            resDepend.getTargetResJndiName(), 
                            resDepend.getType(),
                            resDepend.getProps());
                    addResNode(resNode);
                }
                resDepend.setTarget(resNode);
            }
        }
        
        // after all the Resource nodes are created, we need to find out
        // which resource node is each MDB listening to
        for (Iterator<CMapNode> iter = getNodes(); iter.hasNext(); ) {
            CMapNode node = iter.next();
            if (node instanceof MDBNode) {
                MDBNode mdbNode = (MDBNode) node;
                ResourceNode resNode = findResNodeByLogicalName(mdbNode.getMappedName());
                if (resNode == null) {
                    // if the resource node does not exist, then create it
                    resNode = new ResourceNode(mdbNode.getMappedName(),
                            null,
                            null,
                            ResourceNode.ResourceType.JMS,
                            null);
                    addResNode(resNode);
                }
                mdbNode.setTargetListenNode(resNode);
            }
        }
    }
    
    private EJBNode findEJBNodeImplIntf(String targetIntfName) {
        for (Iterator<CMapNode> iter = getNodes(); iter.hasNext(); ) {
            CMapNode node = iter.next();
            if (node instanceof EJBNode) {
                EJBNode ejbNode = (EJBNode) node;
                if (ejbNode.implementsInterface(targetIntfName)) {
                    return ejbNode;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("CMap: " + name + "\n"); // NOI18N
        for (Iterator<CMapNode> iter = nodes.values().iterator(); iter.hasNext(); ) {
            sb.append("Node: " + iter.next() + "\n"); // NOI18N
        }
        for (Iterator<ResourceNode> iter = resNodes.values().iterator(); iter.hasNext(); ) {
            sb.append("Node: " + iter.next() + "\n"); // NOI18N
        }
        return sb.toString();
    }
    
}
