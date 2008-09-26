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

package org.netbeans.modules.compapp.javaee.sunresources.tool.graph;

import java.awt.Image;
import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.compapp.javaee.sunresources.tool.archive.Archive;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

import org.netbeans.modules.compapp.javaee.sunresources.tool.archive.ApplicationArchive;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.CMapNode;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.CMapNode.CMapNodeType;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.ResourceNode.ResourceType;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.EJBDepend;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.EJBNode;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.MDBNode;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.ResourceDepend;
import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.ResourceNode;
import org.netbeans.modules.compapp.javaee.sunresources.generated.graph.GraphType;


/**
 *
 * @author echou
 */
public class VisualUtil {
    
    // images
    public final static Image SESSION_BEAN_IMG = 
        ImageUtilities.loadImage ("org/netbeans/modules/compapp/javaee/sunresources/resources/session_bean.png").getScaledInstance(32, 32, Image.SCALE_DEFAULT); // NOI18N
    public final static Image MDB_IMG = 
        ImageUtilities.loadImage ("org/netbeans/modules/compapp/javaee/sunresources/resources/message_bean.png").getScaledInstance(32, 32, Image.SCALE_DEFAULT); // NOI18N
    public final static Image ENTITY_BEAN_IMG = 
        ImageUtilities.loadImage ("org/netbeans/modules/compapp/javaee/sunresources/resources/entity_bean.png"); // NOI18N
    public final static Image WS_IMG = 
        ImageUtilities.loadImage ("org/netbeans/modules/compapp/javaee/sunresources/resources/web_service_one.png"); // NOI18N
    public final static Image RES_IMG = 
        ImageUtilities.loadImage ("org/netbeans/modules/compapp/javaee/sunresources/resources/Servers.png"); // NOI18N
    public final static Image DEFAULT_IMG = 
        ImageUtilities.loadImage ("org/netbeans/modules/compapp/javaee/sunresources/resources/default.png"); // NOI18N
    
    public final static int MAX_WIDTH = 800;
    public final static int MAX_HEIGHT = 600;
    public final static int WIDTH_INCR = 50;
    public final static int HEIGHT_INCR = 100;
    
    public static Point currentLoc = new Point(100, 100);
    
    public static CMapScene constructCMapScene(Archive archive) {
        HashMap<CMapNode, Widget> cmapNodeToWidgetMap = 
            new HashMap<CMapNode, Widget> ();
        HashMap<ResourceNode, Widget> resNodeToWidgetMap = 
            new HashMap<ResourceNode, Widget> ();
        
        CMapScene cmapScene = new CMapScene(archive.getName());
        
        // draw cmap nodes
        for (Iterator<CMapNode> iter = archive.getCMap().getNodes(); iter.hasNext(); ) {
            CMapNode curNode = iter.next();
            Image image = null;
            if (curNode.getType() == CMapNodeType.STATELESS ||
                    curNode.getType() == CMapNodeType.STATEFUL) {
                EJBNode ejbNode = (EJBNode) curNode;
                if (ejbNode.isWebService()) {
                    image = WS_IMG;
                } else {
                    image = SESSION_BEAN_IMG;
                }
            } else if (curNode.getType() == CMapNodeType.MDB) {
                image = MDB_IMG;
            } else if (curNode.getType() == CMapNodeType.SERVLET) {
                image = WS_IMG;
            } else {
                image = DEFAULT_IMG;
            }
            
            Widget widget = cmapScene.addCMapNode(curNode, image);
            
            setWidgetLocation(widget, curNode.getLogicalName(), archive.getJAXBHandler());
            
            cmapNodeToWidgetMap.put(curNode, widget);
        }
        
        // draw resource nodes
        for (Iterator<ResourceNode> iter = archive.getCMap().getResNodes(); iter.hasNext(); ) {
            ResourceNode curNode = iter.next();
            Image image = null;
            if (curNode.getNodeType() == ResourceType.JMS) {
                image = RES_IMG;
            } else if (curNode.getNodeType() == ResourceType.WEBSERVICE) {
                image = WS_IMG;
            } else {
                image = DEFAULT_IMG;
            }
            Widget widget = cmapScene.addCMapNode(curNode, image);
            
            setWidgetLocation(widget, curNode.getLogicalName(), archive.getJAXBHandler());
            
            resNodeToWidgetMap.put(curNode, widget);
        }
        
        // draw edges
        for (Iterator<CMapNode> iter = archive.getCMap().getNodes(); iter.hasNext(); ) {
            CMapNode curNode = iter.next();
            
            // draw ejb depend
            for (Iterator<EJBDepend> iter2 = curNode.getEjbDepends().iterator();
                iter2.hasNext(); ) {
                EJBDepend curDepend = iter2.next();
                cmapScene.addCMapEdge(String.valueOf(curDepend.hashCode()), 
                        cmapNodeToWidgetMap.get(curNode), 
                        cmapNodeToWidgetMap.get(curDepend.getTarget()));
            }
            
            // draw resource depend
            for (Iterator<ResourceDepend> iter2 = curNode.getResDepends().iterator();
                iter2.hasNext();) {
                ResourceDepend curDepend = iter2.next();
                cmapScene.addCMapEdge(String.valueOf(curDepend.hashCode()), 
                        cmapNodeToWidgetMap.get(curNode), 
                        resNodeToWidgetMap.get(curDepend.getTarget()));
            }
            
            // handle MDB listerner
            if (curNode.getType() == CMapNode.CMapNodeType.MDB) {
                ResourceNode resNode = ((MDBNode) curNode).getTargetListenNode();
                if (resNode != null) {
                    cmapScene.addCMapEdge(
                        String.valueOf(resNode.hashCode() + String.valueOf(curNode.hashCode())), 
                        resNodeToWidgetMap.get(resNode),
                        cmapNodeToWidgetMap.get(curNode));
                }
            }
        }
        
        return cmapScene;
    }
    
    private static void setWidgetLocation(Widget widget, String logicalName, JAXBHandler jaxbHandler) {
        if (jaxbHandler == null) {
            widget.setPreferredLocation(currentLoc);
            currentLoc = nextLocation(widget.getPreferredBounds().getWidth());
        } else {
            // try to find corresponding node in metadata xml file
            GraphType.Node node = jaxbHandler.findNode(logicalName);
            if (node == null) {
                // if node is not found, create a new node, and add to jaxb tree
                node = new GraphType.Node();
                node.setLogicalname(logicalName);
                node.setLocX(String.valueOf(currentLoc.x));
                node.setLocY(String.valueOf(currentLoc.y));
                jaxbHandler.addNode(node);
                widget.setPreferredLocation(currentLoc);
                currentLoc = nextLocation(widget.getPreferredBounds().getWidth());
            } else {
                // if node is found, retrieve location info from jaxb tree
                widget.setPreferredLocation(new Point(
                        Integer.parseInt(node.getLocX()), Integer.parseInt(node.getLocY())));
                
            }
        }
        
    }
    
    private static Point nextLocation(double widgetWidth) {
        double x;
        double y = currentLoc.getY();
        if (currentLoc.getX() + widgetWidth + WIDTH_INCR > MAX_WIDTH - 150) {
            x = 100;
            y = y + HEIGHT_INCR;
        } else {
            x = currentLoc.getX() + widgetWidth + WIDTH_INCR;
        }
        
        return new Point((int)x, (int)y);
    }
    
    /*
    public static void main(String[] args) {
        CMapScene4 cmap = new CMapScene4 ("Connectivity Map of EAR file:");
        String node1 = "SL1";
        String node2 = "SL2";
        String edge1 = "edge1";
        
        Widget n1 = cmap.addCMapNode(node1, 50, 50);
        Widget n2 = cmap.addCMapNode(node2, 250, 50);
        Widget e1 = cmap.addCMapEdge(edge1, n1, n2);
        
        SceneSupport.show (cmap, 800, 600);
    }
    */
    
}