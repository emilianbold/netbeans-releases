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
