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

package org.netbeans.modules.uml.core.support.umlsupport;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;

//import org.apache.xpath.XPathAPI;
import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;

import java.util.List;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class URILocator {

  private static String m_DocLocation = "";
  private String m_NodeXPath;
  public static String URIDECL = "__uri_";

  public URILocator() {
  }

  public static String decorateURI( String uri ){
    return URIDECL + uri;
  }

  public static String stripURIDeclaration( String docLoc ) {
    String doc = docLoc;
    if (docLoc != null)
    {
		int pos = docLoc.indexOf(URIDECL);
		if (pos >= 0)
		{
		  doc = docLoc.substring(pos+URIDECL.length());
		}
    }
    return doc;
  }

//  public static String uriParts( String hrefValue, String nodeLoc ) {
//    String docLoc = null;
//    int pos = hrefValue.indexOf("#");
//    if (pos >= 0 )
//    {
//      docLoc = stripURIDeclaration(hrefValue.substring(0, pos));
//    }
//    return docLoc;
//  }

  public static Node retrieveNode( String hrefValue, Document doc) 
  {
    Node n = null;
	ETPairT<String, String> retObj = uriparts(hrefValue);
	String docLoc = retObj.getParamOne();
	String nodeLoc = retObj.getParamTwo();
	if (doc == null)
	{
		doc = retrieveDocument(docLoc);
	}
	
    if (doc != null)
    {
    	n = doc.selectSingleNode(nodeLoc);
    }
    return n;
  }

  public static Document retrieveDocument( String hrefValue) 
  {
    Document doc = null;
    try {
      String docLocation = hrefValue.indexOf("#") != -1? 
            hrefValue.substring(0, hrefValue.indexOf("#")) : hrefValue;
      if (docLocation != null && !docLocation.equals("")) 
      {
      	docLocation = stripURIDeclaration(docLocation);
      	m_DocLocation = docLocation;
        //DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        //DocumentBuilder db = dbf.newDocumentBuilder();
        doc = XMLManip.getDOMDocument(docLocation);//db.parse(new File(docLocation));
      }
    } catch (Exception e) {}
    return doc;
  }
  public String getDocLocation( ) {
    return m_DocLocation;
  }

  public String getNodeXPath()
 {
    return m_NodeXPath;
  }
  /**
   *
   * Makes sure the the passed in URI is encode appropriately, so that it
   * can be put into an IDREFS attribute.
   *
   * @param uri[in] THe URI to validate
   *
   * @return The validated URI 
   *
   */
  public static String encodeURI( String uri ) 
  {
  	String validated = uri;
  	validated = StringUtilities.replaceAllSubstrings(uri, " ", "\t");
    return validated;
  }
  
  /**
   *
   * Decodes a URI that has previously been encoded.
   *
   * @param uri[in] The URI to decode.
   *
   * @return  The decoded uri.
   *
   */
  public static String decodeURI( String uri ) 
  {
  	String validated = uri;
  	validated = StringUtilities.replaceAllSubstrings(uri, "\t", " ");
    return validated;
  }

/**
 *
 * Retrieves the parts that make up the URI. Specifically, the document location
 * and the node location with that document
 *
 * @param hrefValue[in] the href to retrieve from. Generally in this form:
 *                      c:\temp\cameron.xml#UML:ModelElement
 * @param nodeLoc[out] In the example above, this would be "UML:ModelElement"
 *
 * @return The document location.  The return value is a pair of information.
 *         The first value is the document location and the second value is the
 *         node location.
 *
 */
public static ETPairT<String,String> uriparts(String href) 
{
	String docLoc = "";
	String nodeLoc = "";
	int pos = href.indexOf("#");
	if (pos >= 0)
	{
		docLoc = stripURIDeclaration(href.substring(0, pos));
		nodeLoc = href.substring(pos+1);
	}
	ETPairT<String, String> retObj = new ETPairT<String, String>(docLoc, nodeLoc);
	return retObj;
}

/**
 *
 * Retrieves the node found in the hrefValue. 
 * 
 * @param hrefValue[in] the href to retrieve from. Generally in this form:
 *                      c:\temp\cameron.xml#UML:ModelElement
 * @param doc[in]    the document to retrieve from. If null, the 
 *							hrefValue will be used to try and pull the document
 * @param node[out] the found node.
 *
 * @return UL_E_DOCUMENT_NOT_FOUND if the document could not be found, else
 *         S_OK or other potential HRESULTs
 *
 */
  public static org.dom4j.Node retrieveNode(String href) {
	org.dom4j.Node retNode = null;
	ETPairT<String, String> obj = uriparts(href);
	String nodeLoc = obj.getParamTwo();
	String docLoc = obj.getParamOne();
	
	Document doc = retrieveDocument(docLoc);
	if (doc != null)
	{
            String xmiid = retrieveRawID(nodeLoc);
            String query = ".//*[@xmi.id=\"" + xmiid + "\"]";
		retNode = XMLManip.selectSingleNode(doc, query);
	}
	return retNode;
  }

/**
 * @return
 */
public static String docLocation()
{
	return m_DocLocation;
}

/**
 *
 * Retrieves the actual XMI ID, even if decorated within a URI.
 *
 * @param uri[in] The id coming in that may or may not 
 *
 * @return The XMI id.
 *
 */
public static String retrieveRawID(String uri)
{
	String actual = uri;
	// This is an example of a DCE XMI id.
	// DCE.4C54B80B-66C6-4663-B59E-07F535E77E28
	// It is always 40 characters long. However, if it is less, 
	// that is ok, as we have a preference that allows non DCE ids to be
	// generated.
	if (actual.length() > 40)
	{
		// We need to find the xmi.id clause of the xpath query.
		// The problem is that we can't just do a search for DCE.
		// cause the user may have changed the id generator.
		int pos = actual.indexOf("xmi.id");
		if (pos < 0)
		{
			// Make one more check, looking for "#id('"
			pos = actual.indexOf("id('");
		}
		
		if (pos >= 0)
		{
			char quoteChar = '"';
			int start = actual.indexOf(quoteChar, pos);
			if (start < 0)
			{
				quoteChar = '\'';
				start = actual.indexOf(quoteChar, pos);
			}
			if (start >= 0)
			{
				int end = actual.indexOf(quoteChar, start+1);
				String foundId = actual.substring(start+1, end);
				
				if (foundId.length() > 0)
				{
					actual = foundId;
				}
			}
		}
	}
	
	return actual;
}
  
}
