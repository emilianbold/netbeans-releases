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

package org.netbeans.modules.compapp.javaee.sunresources.actions;

import org.netbeans.api.visual.action.EditProvider;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.javaee.sunresources.tool.graph.CMapScene;


/**
 * @author echou
 *
 */
public class WidgetEditProvider implements EditProvider {

    private CMapScene scene;
    
    public WidgetEditProvider(CMapScene scene) {
        this.scene = scene;
    }
    
    public void edit(Widget widget) {
        /*
        CMapNode node = scene.findNode(widget);
        if (node == null) {
            System.out.println("Error, clicked on non-existing node");
            return;
        }
        
        if (node.getType() == CMapNodeType.STATELESS ||
                node.getType() == CMapNodeType.STATEFUL ||
                node.getType() == CMapNodeType.MDB) {
            // find and open Java source file
            String nodeClass = node.getNodeClass();
            com.sun.wasilla.netbeans.actions.VisualUtil.openSourceFile(tc.getProject(), nodeClass);
        } else if (node.getType() == CMapNodeType.RESOURCE) {
            ResourceNode resNode = (ResourceNode) node;
            if (resNode.getNodeType() == ResourceType.WEBSERVICE) {
                // find and open WSDL file
                String serviceName = resNode.getLogicalName();
                String wsdlFile = resNode.getProps().getProperty("wsdlLocation");
                com.sun.wasilla.netbeans.actions.VisualUtil.openWSDLFile(tc.getProject(), serviceName, wsdlFile);
            }
        }
         **/
    }

    
}
