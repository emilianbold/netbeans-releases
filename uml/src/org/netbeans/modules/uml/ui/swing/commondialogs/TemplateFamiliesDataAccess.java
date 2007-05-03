/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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


package org.netbeans.modules.uml.ui.swing.commondialogs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.netbeans.modules.uml.ui.support.ProductHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Craig Conover, craig.conover@sun.com
 */
public class TemplateFamiliesDataAccess
{
    private Document data;
    
    public final static String ELEMENT_FAMILY = "family";
    public final static String ELEMENT_DOMAIN_OBJECT = "domainObject";
    public final static String ELEMENT_MODEL_ELEMENT = "modelElement";
    public final static String ELEMENT_STEREOTYPE = "stereotype";
    public final static String ELEMENT_DESCRIPTION = "description";
    public final static String ELEMENT_TEMPLATE_FILE = "templateFile";
    public final static String ELEMENT_FILENAME_FORMAT = "filenameFormat";
    public final static String ELEMENT_FILE_EXTENSION = "fileExtension";
    public final static String ELEMENT_FOLDER_PATH = "folderPath";
    
    public final static String ATTRIBUTE_NAME = "name";
    
    public TemplateFamiliesDataAccess()
    {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        
        try
        {
            docBuilder = docBuilderFactory.newDocumentBuilder();
            data = docBuilder.parse(new File(retrieveTemplateFamiliesFile()));
            getData().getDocumentElement().normalize();
            
            System.out.println("Root element of the doc is " +
                getData().getDocumentElement().getNodeName());
            
        }
        
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        
        catch (SAXParseException err)
        {
            System.out.println("** Parsing error" + ", line "
                + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());
            
        }
        
        catch (SAXException e)
        {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();
            
        }
        
        catch (ParserConfigurationException ex)
        {
            ex.printStackTrace();
        }
    }
    
    
    public NodeList getFamilyNodes()
    {
        return getData().getElementsByTagName(ELEMENT_FAMILY);
    }
    
    public List<String> retrieveFamilies()
    {
        NodeList familyNodes = getFamilyNodes();
        
        if (familyNodes == null || familyNodes.getLength() == 0)
            return null;
        
        List<String> retlist = new ArrayList<String>(familyNodes.getLength());
        
        for (int i=0; i < familyNodes.getLength() ; i++)
        {
            Node family = familyNodes.item(i);
            
            if (family.getNodeType() == Node.ELEMENT_NODE)
            {
                retlist.add(((Element)family).getAttribute(ATTRIBUTE_NAME));
            }
        }
        
        return retlist;
    }
    
    
    public NodeList getFamilyTemplateNodes(String forFamily)
    {
        NodeList familyNodes = getData().getElementsByTagName(ELEMENT_FAMILY);
        
        for (int i=0; i < familyNodes.getLength() ; i++)
        {
            Node family = familyNodes.item(i);
            
            if (family.getNodeType() == Node.ELEMENT_NODE)
            {
                Element familyEle = (Element)family;
                if (forFamily.equals(familyEle.getAttribute(ATTRIBUTE_NAME)))
                {
                    return familyEle.getElementsByTagName(ELEMENT_DOMAIN_OBJECT);
                }
            }
        }
        
        return null;
    }
    
    public List<String> retrieveFamilyTemplates(String family)
    {
        NodeList familyNodes = getFamilyTemplateNodes(family);
        
        if (familyNodes == null || familyNodes.getLength() == 0)
            return null;
        
        List<String> retlist = new ArrayList<String>(familyNodes.getLength());
        
        for (int i=0; i < familyNodes.getLength() ; i++)
        {
            Node template = familyNodes.item(i);
            
            if (template.getNodeType() == Node.ELEMENT_NODE)
            {
                Element templateEle = (Element)template;
                retlist.add(templateEle.getAttribute(ATTRIBUTE_NAME));
            }
        }
        
        return retlist;
    }

    public Document getData()
    {
        return data;
    }

    public int getFamilyTotal()
    {
        return getFamilyNodes().getLength();
    }

    public String getFamilyName(int i)
    {
        return retrieveFamilies().get(i);
    }

    public int getTemplateTotal(String familyName)
    {
        return getFamilyTemplateNodes(familyName).getLength();
    }

    public String getTemplateName(String familyName, int i)
    {
        return retrieveFamilyTemplates(familyName).get(i);
    }
    
    
    private String retrieveTemplateFamiliesFile()
    {
        return ProductHelper.getConfigManager()
            .getDefaultConfigLocation() + "TemplateFamilies.etc";
    }

    public String getModelElementText(String familyName, String domainObjectName)
    {
        return getDomainChildElementText(
            familyName, domainObjectName, ELEMENT_MODEL_ELEMENT);
    }
    
    public String getStereotypeText(String familyName, String domainObjectName)
    {
        return getDomainChildElementText(
            familyName, domainObjectName, ELEMENT_STEREOTYPE);
    }
    
    public String getFilenameText(String familyName, String domainObjectName)
    {
        return getDomainChildElementText(
            familyName, domainObjectName, ELEMENT_TEMPLATE_FILE);
    }
    
    public String getDescriptionText(String familyName, String domainObjectName)
    {
        return getDomainChildElementText(
            familyName, domainObjectName, ELEMENT_DESCRIPTION);
    }
    
    public NodeList getDomainNodeChildren(
        String forFamily, String andDomain)
    {
        NodeList templateNodes = getFamilyTemplateNodes(forFamily);
        
        for (int i=0; i < templateNodes.getLength() ; i++)
        {
            Node template = templateNodes.item(i);
            
            if (template.getNodeType() == Node.ELEMENT_NODE)
            {
                Element templateEle = (Element)template;
                if (andDomain.equals(templateEle.getAttribute(ATTRIBUTE_NAME)))
                {
                    return templateEle.getChildNodes();
                }
            }
        }
        
        return null;
    }

    private String getDomainChildElementText(
        String familyName, String domainObjectName, String elementName)
    {
        NodeList templateChildNodes = 
            getDomainNodeChildren(familyName, domainObjectName);
        
        for (int i=0; i < templateChildNodes.getLength() ; i++)
        {
            Node child = templateChildNodes.item(i);
            
            if (child.getNodeType() == Node.ELEMENT_NODE)
            {
                Element childEle = (Element)child;
                if (elementName.equals(childEle.getTagName()))
                {
                    return childEle.getTextContent();
                }
            }
        }        
        
        return "";
    }
    
    public String[][] getTemplatesData(String forFamily, String andDomain)
    {
        String[][] templateData = null;
        List<String[]> templateRows = new ArrayList<String[]>();
        
        NodeList templateNodes = 
            getDomainNodeChildren(forFamily, andDomain);
        
        for (int i=0; i < templateNodes.getLength() ; i++)
        {
            Node child = templateNodes.item(i);
            
            if (child.getNodeType() == Node.ELEMENT_NODE)
            {
                Element childEle = (Element)child;
                if (childEle.getTagName().equals("template"))
                {
                    NodeList templateChildNodes = childEle.getChildNodes();
                    String[] templateRow = new String[4];
                    int col = 0;
                    
                    for (int j=0; j < templateChildNodes.getLength() ; j++)
                    {
                        Node templateChild = templateChildNodes.item(j);
                        
                        if (templateChild.getNodeType() == Node.ELEMENT_NODE)
                        {
                            Element templateChildEle = (Element)templateChild;
                            
                            if (templateChildEle.getTagName().equals("filenameFormat"))
                                col = 0;
                            
                            else if (templateChildEle.getTagName().equals("fileExtension"))
                                col = 1;
                            
                            else if (templateChildEle.getTagName().equals("folderPath"))
                                col = 2;
                            
                            else if (templateChildEle.getTagName().equals("templateFile"))
                                col = 3;

                            else 
                                col = -1;
                            
                            if (col > -1)
                            {
                                templateRow[col] = 
                                    templateChildEle.getTextContent();
                            }
                        } // if ELEMENT_NODE
                    } // for templateChildNodes; j
                    
                    templateRows.add(templateRow);
                }
            }
        }
        
        templateData = new String[templateRows.size()][4];

        if (templateRows.size() > 0)
            templateRows.toArray(templateData);
        
        return templateData;
    }

}
