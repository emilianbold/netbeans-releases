/*
 * Parser2.java
 *
 * Created on December 9, 2006, 11:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.frameworks.facelets.parser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author petr
 */
public class Parser2 {
    
    public static void analyseDoc(Document doc){
        Element root = doc.getDocumentElement();
        System.out.println("root element: " + root.getNodeName());
        System.out.println(root.getTagName());
        NamedNodeMap attrs = root.getAttributes();
        System.out.println("Namespaces" + attrs.getLength());
        for (int i = 0; i < attrs.getLength(); i++){
            System.out.println("atrrs item: " + attrs.item(i).getNodeName());
        }
        System.out.println("count inset: " + doc.getElementsByTagName("ui:insert").getLength());
        NodeList list = doc.getElementsByTagName("ui:insert");
        for (int i = 0; i < list.getLength(); i++){
            Node node = list.item(i);
            System.out.println("node - nodeName: " + node.getNodeName());
            System.out.println("node - localName: " + node.getLocalName());
            System.out.println("node - namespaceURI: " + node.getNamespaceURI());
            System.out.println("node - baseURI: " + node.getBaseURI());
            System.out.println("node - prefix: " + node.getPrefix());
            
        }
    }
    
}
