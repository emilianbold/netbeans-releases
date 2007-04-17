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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * File:           DDTreeWalker.java
 * Date:           August 2, 2005  10:33 AM
 *
 * @author  Nitya Doraisamy
 */

package org.netbeans.modules.j2ee.sun.dd.impl;

import org.netbeans.modules.j2ee.sun.dd.impl.transform.*;
import org.netbeans.modules.j2ee.sun.dd.api.app.SunApplication;
import org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient;
import org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMappings;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;

import java.util.Vector;
import java.util.Arrays;
import java.util.HashSet;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * 
 * This is a scanner of DOM tree.
 * 
 * Example:
 *     DDTreeWalker scanner = new DDTreeWalker (document);
 *     scanner.visitDocument();
 * 
 * 
 * @see org.w3c.dom.Document
 * @see org.w3c.dom.Element
 * @see org.w3c.dom.NamedNodeMap
 */
public class DDTreeWalker {
    
    Document document;
    String downgradeVersion;
    String currentVersion;
    Transform transInfo;
    String DATAFILE = "org/netbeans/modules/j2ee/sun/dd/impl/transform/transform.xml";  //NOI18N
    
    /**
     * Create new DDTreeWalker with org.w3c.dom.Document.
     */
    public DDTreeWalker(Document document, String version, String currVersion) {
        this.document = document;
        this.downgradeVersion = version;
        this.currentVersion = currVersion;
    }
    
    public void downgradeSunWebAppDocument(){
        this.transInfo = getTransformInfo();
        if(transInfo != null) {
            Vector<ModElement> modElementsList = new Vector<ModElement>();
            Xmltype type = null;
            if(currentVersion.equals(SunWebApp.VERSION_2_5_0)){
                type = getXmlType(transInfo, "sunWebApp41");
                modElementsList = updateModElementsList(modElementsList, type);
            }
            if(this.downgradeVersion.equals(SunWebApp.VERSION_2_4_0) || this.downgradeVersion.equals(SunWebApp.VERSION_2_3_0)){
                type = getXmlType(transInfo, "sunWebApp40");
                modElementsList = updateModElementsList(modElementsList, type);
            }
            if(this.downgradeVersion.equals(SunWebApp.VERSION_2_3_0)){
                type = getXmlType(transInfo, "sunWebApp30");
                modElementsList = updateModElementsList(modElementsList, type);
            }
            processDocument(modElementsList);
        }
    }
    
    public void downgradeSunEjbJarDocument(){
        this.transInfo = getTransformInfo();
        if(transInfo != null) {
            Vector<ModElement> modElementsList = new Vector<ModElement>();
            Xmltype type = null;
            if(currentVersion.equals(SunEjbJar.VERSION_3_0_0)){
                type = getXmlType(transInfo, "sunEjb211");
                modElementsList = updateModElementsList(modElementsList, type);
            }
            if(this.downgradeVersion.equals(SunEjbJar.VERSION_2_1_0) || this.downgradeVersion.equals(SunEjbJar.VERSION_2_0_0)){
                type = getXmlType(transInfo, "sunEjb210");
                modElementsList = updateModElementsList(modElementsList, type);
            }
            if(this.downgradeVersion.equals(SunEjbJar.VERSION_2_0_0)){
                type = getXmlType(transInfo, "sunEjb200");
                modElementsList = updateModElementsList(modElementsList, type);
            }
            processDocument(modElementsList);
        }
    }
    
    // No need for downgradeSunApplicationDocument due to nature of it's DTD history.
    
    public void downgradeSunClientDocument(){
        this.transInfo = getTransformInfo();
        if(transInfo != null) {
            Vector<ModElement> modElementsList = new Vector<ModElement>();
            Xmltype type = null;
            if(currentVersion.equals(SunApplicationClient.VERSION_5_0_0)){
                type = getXmlType(transInfo, "sunClient41");
                modElementsList = updateModElementsList(modElementsList, type);
            }
            if(this.downgradeVersion.equals(SunApplicationClient.VERSION_1_4_0) || this.downgradeVersion.equals(SunApplicationClient.VERSION_1_3_0)){
                type = getXmlType(transInfo, "sunClient40");
                modElementsList = updateModElementsList(modElementsList, type);
            }
            if(this.downgradeVersion.equals(SunApplicationClient.VERSION_1_3_0)){
                type = getXmlType(transInfo, "sunClient30");
                modElementsList = updateModElementsList(modElementsList, type);
            }
            processDocument(modElementsList);
        }
    }
    
    // TODO add entries to transform.xml to support these two methods.    
    public void downgradeSunCmpMappingsDocument(){
        throw new UnsupportedOperationException();
//        this.transInfo = getTransformInfo();
//        if(transInfo != null) {
//            Vector modElementsList = new Vector();
//            Xmltype type = null;
//            if(currentVersion.equals(SunCmpMappings.VERSION_1_2)){
//                type = getXmlType(transInfo, "sunCmpMappings12");
//                modElementsList = updateModElementsList(modElementsList, type);
//            }
//            if(this.downgradeVersion.equals(SunCmpMappings.VERSION_1_1)){
//                type = getXmlType(transInfo, "sunCmpMappings11");
//                modElementsList = updateModElementsList(modElementsList, type);
//            }
//            if(this.downgradeVersion.equals(SunCmpMappings.VERSION_1_0)){
//                type = getXmlType(transInfo, "sunCmpMappings10");
//                modElementsList = updateModElementsList(modElementsList, type);
//            }
//            processDocument(modElementsList);
//        }
    }
            
    private Vector<ModElement> updateModElementsList(Vector<ModElement> modElementsList, Xmltype type){
        if(type != null){
            ModElement[] elementsList = type.getModElement();
            modElementsList.addAll(new HashSet<ModElement>(Arrays.asList(elementsList)));
        } 
        return modElementsList;
    } 
    
    private void processDocument(Vector<ModElement> elements){
        Element element = document.getDocumentElement();
        visitElement(element, elements);
    }
    
    private void visitElement(Element element, Vector<ModElement> elements) {
        walkElement(element, elements);
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            switch (node.getNodeType()) {
                case Node.ELEMENT_NODE:
                    Element nodeElement = (Element)node;
                    String nodeElementName = nodeElement.getTagName();
                    walkElement(nodeElement, elements);
                    visitElement(nodeElement, elements);
                    continue;
            }
        }
    }
    
    private void walkElement(Element element, Vector<ModElement> elements){
        ModElement [] elementsList = elements.toArray(new ModElement [elements.size()]);
        for(int i=0; i<elementsList.length; i++){
            ModElement eachElement = elementsList[i];
            if ((element != null) && element.getTagName().equals(eachElement.getName())) {
                ModAttribute[] attrList = eachElement.getModAttribute();
                for(int j=0; j<attrList.length; j++){
                    removeAttribute(element, attrList[j].getName());
                }
                SubElement[] subelements = eachElement.getSubElement();
                for(int l=0; l<subelements.length; l++){
                    String removeElement = subelements[l].getName();
                    removeElement(element, removeElement);
                }
            }
        }//for
    }
    
    private void removeAttribute(Element element, String attrName){
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int in = 0; in < attrs.getLength(); in++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(in);
            if (attr.getName().equals(attrName)) {
                element.removeAttributeNode(attr);
            }
        }
    }
    
    private void removeElement(Element element, String elementName){
        NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            switch (node.getNodeType()) {
                case Node.ELEMENT_NODE:
                    Element nodeElement = (Element)node;
                    if (nodeElement.getTagName().equals(elementName)) {
                        element.removeChild(node);
                    }
                    break;
            }
        }//for
    }
    
    public Xmltype getXmlType(Transform transformInfo, String moduleVersion) {
        Xmltype[] types = transformInfo.getXmltype();
        for (int i = 0; i < types.length; i++) {
            if (types[i].getName().equals(moduleVersion)){
                return types[i];
            }    
        }
        return null;
    }
    
     public Transform getTransformInfo(){
        try{
            java.io.InputStream in = Transform.class.getClassLoader().getResourceAsStream(DATAFILE);
            this.transInfo = Transform.createGraph(in);
            in.close();
        }catch(Exception ex){
            //System.out.println("Unable to get transInfo");
        }
        return this.transInfo;
    }
    
    
}
