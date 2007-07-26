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
