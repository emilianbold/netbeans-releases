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

package org.netbeans.modules.web.frameworks.facelets.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.netbeans.modules.web.frameworks.facelets.taglib.FaceletsCatalog;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Petr Pisl
 */
public class TLDParser {
    
    public static class Result {
        private String prefix;
        private String uri;
        private Map<String, TagInfo> tagInfos;

        public Result (String prefix, String uri, Map<String, TagInfo> tagInfos){
            this.prefix = prefix;
            this.uri = uri;
            this.tagInfos = tagInfos;
        }
        
        public String getPrefix() {
            return prefix;
        }

        public String getUri() {
            return uri;
        }

        public Map<String, TagInfo> getTagInfos() {
            return tagInfos;
        }
        
    }
    
    public static TLDParser.Result parse(InputStream tld, TagLibraryInfo libInfo){
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        Document doc =  null;
        try {
            docBuilder = docBuilderFactory.newDocumentBuilder();
            docBuilder.setEntityResolver(new FaceletsCatalog());
            doc = docBuilder.parse (tld);
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        String tagName = "";
        String tagDescription = "";
        String prefix = "";
        String uri = "";
                
        ArrayList <TagAttributeInfo> attrs = null;
        Hashtable <String, TagInfo> tagMap = new Hashtable();
        if (doc != null){
            NodeList prefixes = doc.getElementsByTagName("short-name"); //NOI18N
            if (prefixes != null && prefixes.getLength() > 0)
                prefix = prefixes.item(0).getTextContent().trim();
            NodeList uris = doc.getElementsByTagName("uri"); //NOI18N
            if (uris != null && uris.getLength() > 0)
                uri = uris.item(0).getTextContent().trim();
            NodeList tags = doc.getElementsByTagName("tag"); //NOI18N
            if (tags != null){
                for (int i = 0; i < tags.getLength(); i++){
                    attrs = new ArrayList();
                    NodeList childs = tags.item(i).getChildNodes();
                    for (int j = 0; j < childs.getLength(); j++ ){
                        Node child = childs.item(j);
                        if (child.getNodeName().equals("name")) tagName = child.getTextContent().trim();
                        else if (child.getNodeName().equals("description")) tagDescription = child.getTextContent().trim();
                        else if (child.getNodeName().equals("attribute")) attrs.add(getAttribute(child));
                    }
                    TagAttributeInfo[] ats = new TagAttributeInfo[attrs.size()];
                    ats = attrs.toArray(ats);
                    tagMap.put(tagName, new TagInfo(tagName, "", "",  tagDescription, libInfo, null, ats));
                }
            }
        }
        return new Result(prefix, uri, tagMap);
    }
    
    private static TagAttributeInfo getAttribute(Node node){
        String name = "";
        String required = "";
        String description = "";

        for (int i = 0; i < node.getChildNodes().getLength(); i++){
            Node child = node.getChildNodes().item(i); 
            if (child.getNodeName().equals("name")) name = child.getTextContent().trim();
            else if (child.getNodeName().equals("required")) required = child.getTextContent().trim();
            else if (child.getNodeName().equals("description")) description = child.getTextContent().trim();
        }
        
        return new TagAttributeInfo(name, required.trim().equals("true"), "", false, false, description
                , false, false, "", "");
    }
        
        
    
}
