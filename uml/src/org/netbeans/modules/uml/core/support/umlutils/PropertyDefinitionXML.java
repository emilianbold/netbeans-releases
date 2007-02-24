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

package org.netbeans.modules.uml.core.support.umlutils;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//import org.apache.xpath.XPathAPI;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import java.util.List;

import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PropertyDefinitionXML extends PropertyDefinition implements IPropertyDefinitionXML{

  private String m_File = null;

  public PropertyDefinitionXML() {
  }

  public String getFile()
  {
    return m_File;
  }

  public void setFile(String val)
  {
    m_File = val;
  }

  /**
   * Save the definition to a specified file as a xml node.
   *
   * @return HRESULT
   */
  public void save()
  {
    try {
      Document doc = getDOMDocument(m_File);
      if (doc != null) {
        //Node mainNode = XPathAPI.selectSingleNode(doc.getDocumentElement(), "PropertyDefinitions");
        List list = doc.selectNodes("PropertyDefinitions");
        if (list != null && list.size() > 0)
        {
        	Node mainNode = (Node)list.get(0);
			if (mainNode != null) {
			  String path = getPath();
			  if (path.length() > 0) {
				String xpath = turnPathIntoXPath(path);
				if (xpath.length() > 0) {
				  org.dom4j.Node exactNode = XMLManip.selectSingleNode(mainNode, xpath);
				  if (exactNode != null)
					update(exactNode);
				  else
					create(mainNode);
				}
			  }
			  else
				create(mainNode);

				//save the xml file now.
			}
        }
      }
    } catch (Exception e)
    {}
  }

  /**
   * Get a DOM Document for the passed in file.
   *
   *
   * @param file[in]		The file that needs to be queried
   * @param pDoc[out]		The returned DOM document
   *
   * @return HRESULT
   *
   */
  private Document getDOMDocument(String fileName)
  {
    Document doc = null;
    try {
      File file = new File(fileName);
      if (file.canWrite()) {
        //DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        //DocumentBuilder db = dbf.newDocumentBuilder();
        if (file.exists()) {
          doc = XMLManip.getDOMDocument(fileName);//db.parse(file);
        }
        else {
          boolean created = file.createNewFile();
          if (created )
          {
            doc = XMLManip.getDOMDocument();//db.newDocument();
            Element xmlEle = doc.addElement("PropertyDefinitions");
            //doc.appendChild(xmlEle);
            
            //xmlEle.setParent(doc);
            //save the document now.
          }
        }
      }
    } catch (Exception e)
    {

    }
    return doc;
  }

  /**
   * Take the "|" delimited string and build a xpath query from it.
   *
   * @param path[in]		String to turn into a xpath query
   * @param pVal[out]		The xpath string
   *
   * @return HRESULT
   */
  private String turnPathIntoXPath(String path)
  {
    String xpath = new String();
    if (path.length() >0 )
    {
    	if (path.indexOf("|") >= 0)
    	{
			String[] strs = path.split("|");
			if (strs != null && strs.length > 0) {
			  xpath += "//PropertyDefinition";
			  xpath += "[@name=\'";
			  xpath += strs[0];
			  xpath += "\']";
			  for (int i = 1; i < strs.length; i++) {
				xpath += "/aDefinition[@name=\'";
				xpath += strs[i];
				xpath += "\']";
			  }
			}
    	}
    }
    return xpath;
  }

  /**
   * Update this definition.  Currently not needed.
   *
   * @param pNode[in]		The DOM node representing this xml definition
   *
   * @return HRESULT
   */
  private void update(Node pNode)
  {
    setModified(false);
  }

//  private void update(org.dom4j.Node pNode)
//  {
//	setModified(false);
//  }

  /**
   * Create a DOM node from this property definition using the passed in DOM node as the
   * parent.
   *
   * @param parentNode[in]	The parent DOM Node
   *
   * @return HRESULT
   */
//  private void create(org.dom4j.Node pNode)
//  {
//    String name = getName();
//    String path = getPath();
//
//    if (path.length() > 0)
//    {
//    	if (path.indexOf("|") >= 0)
//    	{
//			String[] strs = path.split("|");
//			if (strs != null )
//			{
//			  if (strs.length > 1)
//			  {
//				// if there is more than one parent in my path, then we need
//				// to create a sub definition node
//				String nodeName = "aDefinition";
//				org.dom4j.Node addNode = getNodeToAddTo(pNode, path);
//				if (addNode != null)
//				{
//				  Create2(addNode, nodeName, this);
//				}
//			  }
//			}
//    	}
//		else
//		{
//		  // there is not a parent, so this should be a top level definition
//		  String nodeName = "PropertyDefinition";
//		  Create2(pNode, nodeName, this);
//		}
//    }
//  }

  private void create(Node pNode)
  {
	String name = getName();
	String path = getPath();

	if (path.length() > 0)
	{
		if (path.indexOf("|") >= 0)
		{
			String[] strs = path.split("|");
			if (strs != null )
			{
			  if (strs.length > 1)
			  {
				// if there is more than one parent in my path, then we need
				// to create a sub definition node
				String nodeName = "aDefinition";
				org.dom4j.Node addNode = getNodeToAddTo(pNode, path);
				if (addNode != null)
				{
				  Create2(addNode, nodeName, this);
				}
			  }
			}
		}
		else
		{
		  // there is not a parent, so this should be a top level definition
		  String nodeName = "PropertyDefinition";
		  Create2(pNode, nodeName, this);
		}
	}
  }

  /**
 * Create a new DOM node in the xml.
 *
 * @param parentNode[in]		The DOM node that is to be the parent of the newly created
 * @param nodeName[in]			The node name to create
 * @param pXML[in]				The COM object representing this impl
 *
 * @return HRESULT
 */
//private Node Create2(Node pNode, String name, IPropertyDefinitionXML pElem)
//{
//  Node retNode = null;
//  Document doc = pNode.getOwnerDocument();
//  if (doc != null)
//  {
//    Element nodeElem = doc.createElement(name);
//    if (nodeElem != null)
//    {
//      //set name attribute
//      String eName = pElem.getName();
//      nodeElem.setAttribute("name", eName);
//
//      //set displayName attribute
//      String dispName = pElem.getDisplayName();
//      nodeElem.setAttribute("displayName", dispName);
//
//      //set required attribute
//      boolean required = pElem.isRequired();
//      String reqStr = "false";
//      if (required)
//        reqStr = "true";
//      nodeElem.setAttribute("required", reqStr);
//
//      //set multiplicity attribute
//      long mult = pElem.getMultiplicity();
//      String multStr = "1";
//      if (mult > 1)
//        multStr = "*";
//      nodeElem.setAttribute("multiplicity", multStr);
//
//      //set controlType attribute
//      String contType = pElem.getControlType();
//      nodeElem.setAttribute("controlType", contType);
//
//      //set values attribute
//      String values = pElem.getValidValues();
//      nodeElem.setAttribute("values", values);
//
//      setModified(false);
//      retNode = pNode.appendChild(nodeElem);
//    }
//  }
//  return retNode;
//}

private Node Create2(org.dom4j.Node pNode, String name, IPropertyDefinitionXML pElem)
{
  Node retNode = null;
  org.dom4j.Document doc = pNode.getDocument();
  if (doc != null)
  {
    org.dom4j.Element nodeElem = doc.addElement(name);
    if (nodeElem != null)
    {
      //set name attribute
      String eName = pElem.getName();
      nodeElem.addAttribute("name", eName);

      //set displayName attribute
      String dispName = pElem.getDisplayName();
      nodeElem.addAttribute("displayName", dispName);

      //set required attribute
      boolean required = pElem.isRequired();
      String reqStr = "false";
      if (required)
        reqStr = "true";
      nodeElem.addAttribute("required", reqStr);

      //set multiplicity attribute
      long mult = pElem.getMultiplicity();
      String multStr = "1";
      if (mult > 1)
        multStr = "*";
      nodeElem.addAttribute("multiplicity", multStr);

      //set controlType attribute
      String contType = pElem.getControlType();
      nodeElem.addAttribute("controlType", contType);

      //set values attribute
      String values = pElem.getValidValues();
      nodeElem.addAttribute("values", values);

      setModified(false);
      nodeElem.setParent((org.dom4j.Element)pNode);
      //retNode = pNode.appendChild(nodeElem);
    }
  }
  return retNode;
}

/**
 * Search the sub nodes of the passed-in DOM node looking for the node
 * that matches the path.
 *
 * @param pNode[in]				The DOM Node to search
 * @param path[in]				The "|" delimited string to search for (each | is another level of sub nodes)
 * @param pReturnNode[out]		The found DOM Node
 *
 * @return HRESULT
 */
//private org.dom4j.Node getNodeToAddTo(org.dom4j.Node pNode, String path)
//{
//  org.dom4j.Node retNode = null;
//  try {
//    if (path.length() > 0) {
//      retNode = pNode;
//      if(path.indexOf("|") >=0 )
//      {
//		String[] strs = path.split("|");
//		if (strs != null && strs.length > 0) {
//		  int count = 0;
//		  for (int i = 0; i < strs.length; i++) {
//			if (retNode != null) {
//			  String pattern = "aDefinition";
//			  if (count == 0)
//				pattern = "PropertyDefinition";
//
//			  pattern += "[@name=\'";
//			  pattern += strs[i];
//			  pattern += "\']";
//
//			  org.dom4j.Node newNode = XMLManip.selectSingleNode(retNode, pattern);
//			  if (newNode != null)
//				retNode = newNode;
//			  count++;
//			}
//		  }
//		}
//      }
//    }
//  } catch (Exception e)
//  {}
//  return retNode;
//}

private org.dom4j.Node getNodeToAddTo(Node pNode, String path)
{
  org.dom4j.Node retNode = null;
  try {
	if (path.length() > 0) {
	  //retNode = pNode;
	  if(path.indexOf("|") >=0 )
	  {
		String[] strs = path.split("|");
		if (strs != null && strs.length > 0) {
		  int count = 0;
		  for (int i = 0; i < strs.length; i++) {
			if (retNode != null) {
			  String pattern = "aDefinition";
			  if (count == 0)
				pattern = "PropertyDefinition";

			  pattern += "[@name=\'";
			  pattern += strs[i];
			  pattern += "\']";

			  org.dom4j.Node newNode = XMLManip.selectSingleNode(retNode, pattern);
			  if (newNode != null)
				retNode = newNode;
			  count++;
			}
		  }
		}
	  }
	}
  } catch (Exception e)
  {}
  return retNode;
}

  public void remove()
  {

  }

}