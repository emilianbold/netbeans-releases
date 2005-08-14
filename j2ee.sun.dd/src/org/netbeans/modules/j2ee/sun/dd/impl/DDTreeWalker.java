/*
 * File:           DDTreeWalker.java
 * Date:           August 2, 2005  10:33 AM
 *
 * @author  Nitya Doraisamy
 */

package org.netbeans.modules.j2ee.sun.dd.impl;

import org.netbeans.modules.j2ee.sun.dd.impl.transform.*;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;

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
            Vector modElementsList = new Vector();
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
            Vector modElementsList = new Vector();
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
    
    private Vector updateModElementsList(Vector modElementsList, Xmltype type){
        if(type != null){
            ModElement[] elementsList = type.getModElement();
            modElementsList.addAll(new HashSet(Arrays.asList(elementsList)));
        } 
        return modElementsList;
    } 
    
    private void processDocument(Vector elements){
        Element element = document.getDocumentElement();
        visitElement(element, elements);
    }
    /*void createXML(){
        try{
            Transform trans = Transform.createGraph();
            Xmltypee sunWebApp40 = trans.newType();
            sunWebApp40.setName("sunWebApp40");
            org.netbeans.modules.j2ee.sun.dd.impl.transform.Element sunwebapp = sunWebApp40.newElement();
            sunwebapp.setName("sun-web-app");
            org.netbeans.modules.j2ee.sun.dd.impl.transform.Attribute errUrl = sunwebapp.newAttribute();
            errUrl.setName("error-url");
            org.netbeans.modules.j2ee.sun.dd.impl.transform.SubElement idempotent = sunwebapp.newSubElement();
            idempotent.setName("idempotent-url-pattern");
            org.netbeans.modules.j2ee.sun.dd.impl.transform.SubElement parameter = sunwebapp.newSubElement();
            parameter.setName("parameter-encoding");
            sunwebapp.addSubElement(idempotent);
            sunwebapp.addSubElement(parameter);
            sunwebapp.addAttribute(errUrl);
            sunWebApp40.addElement(sunwebapp);
            
            org.netbeans.modules.j2ee.sun.dd.impl.transform.Element classloader = sunWebApp40.newElement();
            classloader.setName("class-loader");
            sunWebApp40.addElement(classloader);
            trans.addType(sunWebApp40);
            
            org.netbeans.modules.j2ee.sun.dd.impl.transform.Type sunWebApp41 = trans.newType();
            sunWebApp41.setName("sunWebApp41");
            trans.addType(sunWebApp41);
            
            System.out.println("sunWebApp40.dumpXml()");
            sunWebApp40.dumpXml();
            System.out.println(sunWebApp40.toString());
            java.io.File f = new java.io.File("C://test.xml");
            sunWebApp40.write(f);
            
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }*/
    private void visitElement(Element element, Vector elements) {
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
    
    private void walkElement(Element element, Vector elements){
        Object[] elementsList = elements.toArray();
        for(int i=0; i<elementsList.length; i++){
            ModElement eachElement = (ModElement)elementsList[i];
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
    
    public Xmltype getXmlType(Transform transformInfo, String webAppVersion) {
        Xmltype[] types = transformInfo.getXmltype();
        for (int i = 0; i < types.length; i++) {
            if (types[i].getName().equals(webAppVersion)){
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
