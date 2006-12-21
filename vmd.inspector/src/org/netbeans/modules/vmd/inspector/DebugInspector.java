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

package org.netbeans.modules.vmd.inspector;

import org.netbeans.modules.vmd.api.inspector.InspectorFolderPath;
import org.openide.nodes.Node;

/**
 *
 * @author Karol Harezlak
 */
final class DebugInspector {

    private static  String tabs = "";  // NOI18N
    private static final String space = "     "; // NOI18N
    private static Node childTest;

    static void printPath(InspectorFolderPath path){
        System.out.println("InspectorPath" + path.toString());  // NOI18N
    }

    static void printFoldersTree(InspectorFolderWrapper folderWrapper){
        tabs = "";  // NOI18N
        System.out.println(folderWrapper.getNode());
        System.out.println(tabs +"  -Node children =:"+ folderWrapper.getNode().getChildren().getNodesCount());  // NOI18N
        deepDive(folderWrapper);
    }
    
    private static void deepDive(InspectorFolderWrapper parentFolderWrapper){
        if (parentFolderWrapper.getChildren() == null)
            return;
        tabs = tabs + space;
        for (InspectorFolderWrapper folderWrapper : parentFolderWrapper.getChildren()){
            System.out.println(tabs +"|-"+ folderWrapper);  // NOI18N
            System.out.println(tabs +"  -Node children =:"+ folderWrapper.getNode().getChildren().getNodesCount());  // NOI18N
            if (folderWrapper.getChildren() != null)
                deepDive(folderWrapper);
        }
        tabs = tabs.replaceFirst(space,"");  // NOI18N
    }
    
    static void printNodesTree(Node parentNode){
        tabs = "";  // NOI18N
        System.out.println(parentNode);
        System.out.println(tabs +"  -Node children =:"+ parentNode.getChildren().getNodesCount());  // NOI18N
        deepDive(parentNode);
    }
    
    private static void deepDive(Node parentNode){
        tabs = tabs + space;
        for (Node node : parentNode.getChildren().getNodes()){
            System.out.println(tabs +"|-"+ node.getDisplayName()+" "+ node);  // NOI18N
            System.out.println(tabs +"  -Node children =:"+ node.getChildren().getNodesCount());  // NOI18N
            if (node.getChildren().getNodes().length > 0)
                deepDive(node);
        }
        tabs = tabs.replaceFirst(space,"");  // NOI18N
    }
    
    static void checkRootNode(Node rootNode, Node child){
        childTest = child;
        System.out.println("Test start: root node: "+ rootNode +" child: "+ child); //NOI18N
        checkParent(rootNode, child);
    }
    
    private static void checkParent(Node rootNode, Node child){
        if (child.getParentNode() != null)
            checkParent(rootNode, child.getParentNode());
        else if (child == rootNode)
            System.out.println("Node: " + rootNode + " is root node of node : " + childTest);//NOI18N
        else
            System.out.println("Node: " + rootNode + " is NOT root node of node : " + childTest +" real root node: "+ child);//NOI18N
    }
    
    
}
