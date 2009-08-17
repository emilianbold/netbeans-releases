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

import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;


import org.netbeans.modules.compapp.javaee.sunresources.generated.graph.GraphType;
import org.netbeans.modules.compapp.javaee.sunresources.generated.graph.ObjectFactory;
import org.netbeans.modules.compapp.javaee.sunresources.tool.archive.FileUtil;

/**
 * @author echou
 *
 */
public class JAXBHandler {

    private JAXBContext jc;
    private JAXBElement<?> graphElement;
    private List<GraphType.Node> nodeList;
    private Marshaller marshaller;
    private File xmlFile;
    
    public JAXBHandler(File xmlFile) throws Exception {
        this.xmlFile = xmlFile;

        jc = JAXBContext.newInstance("com.sun.wasilla.jaxb.graph", // NOI18N
                this.getClass().getClassLoader());
        
        boolean unmarshalSuccess = true;
        // unmarshal it to memory
        try {
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            graphElement = (JAXBElement<?>) unmarshaller.unmarshal(xmlFile);
            nodeList = Collections.synchronizedList(
                    ((GraphType)graphElement.getValue()).getNode());
        } catch (Exception e) {
            unmarshalSuccess = false;
        }
        
        // if unable to marshal xml, then create a new empty one
        if(!unmarshalSuccess){
            // construct JAXB tree
            ObjectFactory factory = new ObjectFactory();
            GraphType graph = factory.createGraphType();
            graphElement = factory.createGraph(graph);
            nodeList = Collections.synchronizedList(
                    ((GraphType)graphElement.getValue()).getNode());
            
        }
        
    }
    
    public GraphType.Node findNode(String logicalname) {
        for (int i = 0; i < nodeList.size(); i++) {
            GraphType.Node curNode = nodeList.get(i);
            if (logicalname.equals(curNode.getLogicalname())) {
                return curNode;
            }
        }
        
        return null;
    }
    
    public void addNode(GraphType.Node node) {
        nodeList.add(node);
    }
    
    public void updateNode(String logicalname, Point p) {
        GraphType.Node node = findNode(logicalname);
        if (node == null) {
            node = new GraphType.Node();
            node.setLogicalname(logicalname);
            node.setLocX(String.valueOf(p.x));
            node.setLocY(String.valueOf(p.y));
            addNode(node);
        } else {
            node.setLocX(String.valueOf(p.x));
            node.setLocY(String.valueOf(p.y));
        }
    }
    
    public void saveXML() throws Exception {
        if (marshaller == null) {
            marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE); // NOI18N
        }
        // write the prolog here
        FileOutputStream fos = new FileOutputStream(xmlFile);
        PrintWriter out = new PrintWriter(fos);
        try {
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); // NOI18N
            out.println("<!DOCTYPE resources PUBLIC \"-//Sun Microsystems Inc.//DTD Application Server 9.0 Domain//EN\" \"http://www.sun.com/software/appserver/dtds/sun-resources_1_3.dtd\">"); // NOI18N
            marshaller.marshal(graphElement, out);
        } finally {
            FileUtil.safeclose(out);
            FileUtil.safeclose(fos);
        }
    }
    
    /*
     * test of this class
     */
    public static void main(String[] args) {
        try {
            String xmlFileName = "C:/testcode/wasilla/graph2.xml"; // NOI18N
            File xmlFile = new File(xmlFileName);
            JAXBHandler h = new JAXBHandler(xmlFile);
            h.saveXML();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
