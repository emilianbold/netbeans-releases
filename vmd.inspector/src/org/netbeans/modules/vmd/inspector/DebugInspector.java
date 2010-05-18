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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
