/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.uml.core.support.umlutils;

import java.util.StringTokenizer;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PropertyElementXML extends PropertyElement implements IPropertyElementXML{
  public PropertyElementXML() {
    super();
  }

  /**
   * Save the element to a specified file as a xml node
   *
   * @return HRESULT
   */
  public boolean save( Document pDoc ) {
  	boolean saved = false;
    try {
      IPropertyElementManager mgr = getPropertyElementManager();
      if (mgr != null && pDoc != null) {
        org.dom4j.Node n = XMLManip.selectSingleNode(pDoc,
                                           "PropertyElements");
        if (n != null)
        {
          String strPath = getPath();
          if (strPath.length() > 0)
          {
            String xpath = turnPathIntoXPath(strPath);
            if (xpath.length() >0 )
            {
              org.dom4j.Node exactNode = XMLManip.selectSingleNode(n, xpath);
              if (exactNode != null)
                update(exactNode);
              else
                create(n);
            }
          }
          else
          {
            create(n);
          }
        }
      }
      saved = true;
    } catch (Exception e)
    {}
    return saved;
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
    if (path.indexOf("|") >= 0 )
    {
    	StringTokenizer tokenizer = new StringTokenizer(path, "|");
    	if (tokenizer.countTokens() > 0)
    	{
    		String token = tokenizer.nextToken();
			xpath += "//PropertyElement";
			xpath += "[@name=\'";
			xpath += token;
			xpath += "\']";
			while (tokenizer.hasMoreTokens())
			{
				token = tokenizer.nextToken();
				xpath += "/aElement[@name=\'";
				xpath += token;
				xpath += "\']";
			}
    	}
    	
//		String[] strs = path.split("|");
//		if (strs != null && strs.length > 0)
//		{
//		  xpath += "//PropertyElement";
//		  xpath += "[@name=\'";
//		  xpath += strs[0];
//		  xpath += "\']";
//		  for(int i=1; i<strs.length; i++)
//		  {
//			xpath += "/aElement[@name=\'";
//			xpath += strs[i];
//			xpath += "\']";
//		  }
//		}
    }
    else
    {
		xpath += "//PropertyElement";
		xpath += "[@name=\'";
		xpath += path;
		xpath += "\']";
    }
    return xpath;
  }

  /**
   * Update this element.
   *
   * @param pNode[in]		The DOM node representing this element
   *
   * @return HRESULT
   */
//  private void update(Node n)
//  {
//    String value = getValue();
//    NamedNodeMap map = n.getAttributes();
//    Node node = map.getNamedItem("name");
//    node.setNodeValue(value);
//    setOrigValue(value);
//    setModified(false);
//  }

  private void update(org.dom4j.Node n)
  {
	String value = getValue();
	if (n.getNodeType() == org.dom4j.Node.ELEMENT_NODE)
	{
		org.dom4j.Element ele = (org.dom4j.Element)n;
		Attribute attNode = ele.attribute("value");
		if (attNode != null)
		{
			attNode.setValue(value);
			setOrigValue(value);
			setModified(false);
		}
	}
  }

  /**
   * Create a DOM node from this property definition using the passed-in DOM node as the
   * parent.
   *
   * @param parentNode[in]	The parent DOM Node
   *
   * @return HRESULT
   */
//  private void create(Node n)
//  {
//    if (n != null)
//    {
//      String name = getName();
//      String path = getPath();
//      if (path.length() > 0)
//      {
//      	if (path.indexOf("|") >= 0)
//      	{
//			String[] strs = path.split("|");
//			if (strs != null )
//			{
//			  if (strs.length > 1)
//			  {
//				// if there is more than one parent in my path, then we need
//				// to create a sub definition node
//				String nodeName = "aElement";
//				Node addNode = getNodeToAddTo(n, path);
//				if (addNode != null)
//				{
//				  Create2(addNode, nodeName, this);
//				}
//			  }
//			}
//      	}
//		else
//		{
//		  // there is not a parent, so this should be a top level definition
//		  String nodeName = "PropertyElement";
//		  Create2(n, nodeName, this);
//		}
//      }
//    }
//  }

  private void create(org.dom4j.Node n)
  {
	if (n != null)
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
				String nodeName = "aElement";
				org.dom4j.Node addNode = getNodeToAddTo(n, path);
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
		  String nodeName = "PropertyElement";
		  Create2(n, nodeName, this);
		}
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
//  private Node Create2(Node n, String name, IPropertyElementXML pElem)
//  {
//    Node retNode = null;
//    Document doc = n.getOwnerDocument();
//    if (doc != null)
//    {
//      Element nodeElem = doc.createElement(name);
//      if (nodeElem != null)
//      {
//        String eName = pElem.getName();
//        String value = pElem.getValue();
//        pElem.setOrigValue(value);
//        nodeElem.setAttribute("name", eName);
//        nodeElem.setAttribute("value", value);
//        setModified(false);
//        retNode = n.appendChild(nodeElem);
//      }
//    }
//    return retNode;
//  }

  private Node Create2(org.dom4j.Node n, String name, IPropertyElementXML pElem)
  {
	Node retNode = null;
	org.dom4j.Document doc = n.getDocument();
	if (doc != null)
	{
	  org.dom4j.Element nodeElem = doc.addElement(name);
	  if (nodeElem != null)
	  {
		String eName = pElem.getName();
		String value = pElem.getValue();
		pElem.setOrigValue(value);
		nodeElem.setAttributeValue("name", eName);
		nodeElem.setAttributeValue("value", value);
		setModified(false);
		//retNode = n.appendChild(nodeElem);
	  }
	}
	return retNode;
  }

  /**
   * Search the sub nodes of the passed-in DOM node looking for the node
   * that matches the heading.
   *
   * @param pNode[in]				The DOM Node to search
   * @param heading[in]			The "|" delimited string to search for (each | is another level of sub nodes)
   * @param pReturnNode[out]		The found DOM Node
   *
   * @return HRESULT
   */
//  private Node getNodeToAddTo(Node n, String path)
//  {
//    Node retNode = null;
//    try {
//      if (path.length() > 0) {
//        retNode = n;
//        String[] strs = path.split("|");
//        if (strs != null && strs.length > 0) {
//          int count = 0;
//          for (int i = 0; i < strs.length; i++) {
//            if (retNode != null) {
//              String pattern = "aElement";
//              if (count == 0)
//                pattern = "PropertyElement";
//
//              pattern += "[@name=\'";
//              pattern += strs[i];
//              pattern += "\']";
//
//              org.dom4j.Node newNode = XMLManip.selectSingleNode(retNode, pattern);
//              if (newNode != null)
//                //retNode = newNode;
//              count++;
//            }
//          }
//        }
//      }
//    } catch (Exception e)
//    {}
//    return retNode;
//  }

  private org.dom4j.Node getNodeToAddTo(org.dom4j.Node n, String path)
  {
	org.dom4j.Node retNode = null;
	try {
	  if (path.length() > 0) {
		retNode = n;
		String[] strs = path.split("|");
		if (strs != null && strs.length > 0) {
		  int count = 0;
		  for (int i = 0; i < strs.length; i++) {
			if (retNode != null) {
			  String pattern = "aElement";
			  if (count == 0)
				pattern = "PropertyElement";

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
	} catch (Exception e)
	{}
	return retNode;
  }

  /**
   * Removes the xml node representing this impl.
   *
   * @return HRESULT
   */
  public void remove( Document pDoc ) {
    try {
      if (pDoc != null) {
        String path = getPath();
        if (path.length() > 0) {
          String xpath = turnPathIntoXPath(path);
          if (xpath.length() > 0) {
            org.dom4j.Node n = XMLManip.selectSingleNode(pDoc, xpath);
            if (n != null) {
              org.dom4j.Node pNode = n.getParent();
              if (pNode != null)
              {
				//pNode.removeChild(n);
				n.detach();
              }
            }
          }
        }
      }
    } catch (Exception e)
    {}
  }

}
