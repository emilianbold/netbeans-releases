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

package org.netbeans.modules.uml.core.support.umlsupport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.parsers.ParserConfigurationException;
import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.IDResolver;
import org.dom4j.InvalidXPathException;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.XPath;
import org.dom4j.dom.DOMDocumentFactory;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultDocument;
import org.w3c.dom.DOMException;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.core.metamodel.structure.Project;
import org.netbeans.modules.uml.core.support.UMLLogger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class XMLManip
{
    private static final String loggerName = "uml.core.support.umlsupport.XMLManip";
    private static HashMap<String, Namespace> m_Namespaces = 
        new HashMap<String, Namespace>();
    private static Document m_XMIFragment;
   //static ResourceBundle messages =
   //		ResourceBundle.getBundle("org.netbeans.modules.uml.core.MessagesBundle");

   private static DocumentBuilderFactory dbf = null;
   private static DocumentBuilder db = null;
   private static XMLManip xmlManip = new XMLManip();

   //this holds the already loaded documents with their file names
   //there were certain cases when documents were required for a given file
   // and parsing happenned multiple times.
   //private static HashMap < String, Document > m_LoadedDocs = new HashMap < String, Document >();
   private static HashMap < String, WeakReference > m_LoadedDocs = new HashMap < String, WeakReference >();
   private static HashMap < String, Long > m_LoadedDocsTimestamps = new HashMap < String, Long >();

   public static XMLManip instance()
   {
      if (xmlManip == null)
      {
         xmlManip = new XMLManip();
      }
      return xmlManip;
   }

   private XMLManip()
   {
        try {
            dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            UMLLogger.logException(loggerName, ex, Level.WARNING);
        }
      
      
   }

   /**
    * Searches under the given element for an element with the xmi.id supplied.
    * 
    * @param branch The element to search under (Document or Element).
    * @param id     The xmi.id of the element to look for.
    * @return The Element if found, or null.
    */
   public static Element findElementByID(Branch branch, String id)
   {
       if (branch == null || id == null)
       	return null;
		Element pElement = branch.elementByID(id);
// For performance, move the chec to DefaultDocument
//		if (pElement != null && pElement.getDocument() == null)
//		{
//       	String pattern = ".//*[@xmi.id=\"" + id + "\"]";
//			pElement = (Element)selectSingleNode(branch, pattern);
//		}
		
		return pElement;
   }
   
   public static Document getDOMDocument(String fileName)
   {
      return getDOMDocument(fileName, new IDResolver("xmi.id"));
   }
   
    public static Document getDOMDocument(String fileName, IDResolver resolver) 
    {
        Document doc = null;
        if (fileName != null && fileName.length() > 0) 
        {
            File file = new File(fileName);
            if (!file.exists()) 
            {
                return doc;
            }
            InputStreamReader ins = null;
            FileObject fo = FileUtil.toFileObject(file);  
            if (fo != null && fo.getSize() > 0 ) 
            {
                try {
                    SAXReader reader = new SAXReader(DOMDocumentFactory.getInstance(), false);
                    reader.setIDResolver(resolver);
                    ins = new InputStreamReader(fo.getInputStream());
                    doc = reader.read(ins);

                } catch (Exception ex) {
                    UMLLogger.logException(loggerName, ex, Level.WARNING);
                } finally {
                    if (ins != null) {
                        try {
                            ins.close();
                        } catch (IOException ex) {
                            UMLLogger.logMessage(loggerName, ex.getMessage(), Level.WARNING);
                        }
                    }
                }
            }
        }
        return doc;
    }
   

   public static Document getDOMDocumentUseWeakCache(String fileName)
   {
      Document doc = null;
      
      if (fileName != null && fileName.length() > 0)
      {
	  WeakReference docRef = m_LoadedDocs.get(fileName);
	  if (docRef != null) 
	  {
	      doc = (Document)docRef.get();
	  }
          long lm = -1;
	  Long lmValue = m_LoadedDocsTimestamps.get(fileName);
          if (lmValue != null) 
          {
              lm = lmValue.longValue();
          }
          long currentLastModified = new File(fileName).lastModified();
	  //doc = m_LoadedDocs.get(fileName);
	  if (doc == null || (currentLastModified > lm))
	  {
	      doc = getDOMDocument(fileName);
	      m_LoadedDocs.put(fileName, new WeakReference(doc));
              m_LoadedDocsTimestamps.put(fileName, new Long(currentLastModified));
	  }
      }         
      return doc;
   }



   public static Document getDOMDocument()
   {
      return getDOMDocument(new IDResolver("xmi.id"));
   }
   
   public static Document getDOMDocument(IDResolver resolver)
   {
      Document doc = null;
      try
      {
         //SAXReader reader = new SAXReader();
         //doc = DocumentFactory.getInstance().createDocument();
         doc = DOMDocumentFactory.getInstance().createDocument();
         doc.setIDResolver(resolver);
         
         if (doc != null)
         {
			Element rootEle = DOMDocumentFactory.getInstance().createElement("");
			doc.setRootElement(rootEle);
			Namespace space1 = DOMDocumentFactory.getInstance().createNamespace("UML", "omg.org/UML/1.4");
			Namespace space2 = DOMDocumentFactory.getInstance().createNamespace("EMBT", "www.sun.com");
			rootEle.add(space1);
			rootEle.add(space2);
         }
         //doc = db.newDocument();
      }
      catch (Exception ex)
      {
      	UMLLogger.logException(loggerName, ex, Level.WARNING);
      }
      return doc;
   }

   public static Document getDOMDocumentFromString(String str)
   {
      Document doc = null;
      try
      {
         if (str != null && str.length() > 0)
         {
            //SAXReader reader = new SAXReader();
            SAXReader reader = new SAXReader(DOMDocumentFactory.getInstance(), false);
            doc = reader.read(str);
            //doc = db.parse(str);
         }
      }
      catch (Exception ex)
      {
          UMLLogger.logException(loggerName, ex, Level.WARNING);
      }
      return doc;
   }

   public static String getAttributeValue(Attribute attr)
   {
      String value = attr.getText();
      return value;
   }

   /**
    * Retreives a nodes attribute value as a string.  
    * 
    * @param n The node that is used to retrieve the attribute.
    * @param attr The name of the attribute value.
    */
   public static String getAttributeValue(org.dom4j.Node n, String str)
   {
      String value = null; //$NON-NLS-1$
      try
      {
         if (n != null && n.getNodeType() == org.dom4j.Node.ELEMENT_NODE)
         {
            org.dom4j.Element ele = (org.dom4j.Element)n;
            value = ele.attributeValue(str);
         }
      }
      catch (Exception e)
      {
      }
      return (value != null) ? value : "";
   }

   /**
    * Retreives a nodes attribute value as a boolean.  If the value of the 
    * attribute can not be converted to a boolean then <code>false</code> 
    * will be returned.
    * 
    * @param n The node that is used to retrieve the attribute.
    * @param str The name of the attribute value.
    * @return The boolean value of the attribute.
    */
   public static boolean getAttributeBooleanValue(Node n, String str)
   {
      return getAttributeBooleanValue( n, str, false );
   }


   /**
    * Retreives a nodes attribute value as a boolean.  If the value of the 
    * attribute can not be converted to a boolean then <code>false</code> 
    * will be returned.
    * 
    * @param n The node that is used to retrieve the attribute.
    * @param str The name of the attribute value.
    * @param defaultValue the value returned if the attribute is not found.
    * @return The boolean value of the attribute.
    */
   public static boolean getAttributeBooleanValue( Node n, String str, boolean defaultValue )
   {
      boolean retVal = defaultValue;
      if (n != null)
      {
         String val = getAttributeValue(n, str);
         if ((val != null) && (val.length() > 0) )
         {
            retVal = Boolean.valueOf(val).booleanValue();
         }
      }
      return retVal;
   }

   /**
    * Retreives a nodes attribute value as an integer.  If the value of the 
    * attribute can not be converted to an integer then zero will be returned.
    * 
    * @param n The node that is used to retrieve the attribute.
    * @param attr The name of the attribute value.
    * @return The integer value of the attribute.
    * @throws NumberFormatException if the attribute does not contain a 
    *                               parsable integer.
    */
   public static int getAttributeIntValue(Node n, String attr)
      throws NumberFormatException
   {
      int retVal = 0;
      if (n != null)
      {
         String val = getAttributeValue(n, attr);
         if( val.length() > 0 )
         {
            retVal = Integer.parseInt(val);
         }
      }
      return retVal;
   }

   /**
    * Retreives a nodes attribute value as an double.  If the value of the 
    * attribute can not be converted to an integer then zero will be returned.
    * 
    * @param n The node that is used to retrieve the attribute.
    * @param attr The name of the attribute value.
    * @return The integer value of the attribute.
    * @throws NumberFormatException if the attribute does not contain a 
    *                               parsable integer.
    */
   public static double getAttributeDoubleValue(Node n, String attr)
      throws NumberFormatException
   {
      double retVal = 0;
      if (n != null)
      {
         String val = getAttributeValue(n, attr);
         if( val.length() > 0 )
         {
            retVal = Double.parseDouble(val);
         } 
      }
      return retVal;
   }

   /**
    *
    * Sets the XML attribute that has the passed in name to the passed in value.
    *
    * @param element[in] the element to set that attribute on.
    * @param attrName[in] the name of the XML attribute to set.
    * @param value[in] the actual value to set.
    *
    * @return HRESULT
    *
    */
   public static void setAttributeValue(
      Element elem,
      String name,
      String value)
   {
     
     if (elem != null)
     {
		if (value != null)
		{
			checkForIllegals(value);
			elem.addAttribute(name, value);
		}
		else
		{
			Attribute attr = elem.attribute(name);
			if (attr != null)
				elem.remove(attr);
		}
     }
   }

   public static void setAttributeValue(Node n, String name, String value)
   {
      if (n instanceof org.dom4j.Element)
      {
         org.dom4j.Element elem = (org.dom4j.Element)n;
         setAttributeValue(elem, name, value);
      }
   }
   /**
    *
    * Makes sure that there are no invalid characters in the value. If
    * there are, they are converted appropriately. 
    * Specifically:
   	 '&' will be changed into &amp;
    * '<' will be changed into &lt;
    * '>' will be changed into &gt;
    *
    * @param value The value to modify if needed.
    *
    * @return HRESULT
    */
   public static String checkForIllegals(String val)
   {
      String newval = val;
      
      if (val.length() > 0)
      {
         int pos = val.indexOf('&');
         if (pos >= 0)
         {
            newval = processIllegalCharacter(newval, '&');
         }

         pos = val.indexOf('<');
         if (pos >= 0)
         {
            newval = processIllegalCharacter(newval, '<');
         }

         pos = val.indexOf('>');
         if (pos >= 0)
         {
            newval = processIllegalCharacter(newval, '>');
         }
      }

      return newval;
   }

   /**
    *
    * Makes sure that there are no invalid characters in the value. If
    * there are, they are converted appropriately. 
    * Specifically:
   	 '&' will be changed into &amp;
    * '<' will be changed into &lt;
    * '>' will be changed into &gt;
    * ''' will be changed into &apos; We are currently not processing this character
    * '"' will be changed into &quot; We are currently not processing this character
    *
    * @param value[in,out] The value to modify if needed.
    * @param ch[in] The character to replace
    *
    * @return HRESULT
    */
   private static String processIllegalCharacter(String str, char ch)
   {
      String newVal = str;
      if (ch == '&')
      {
         newVal = StringUtilities.splice(str, "&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
      }

      if (ch == '<')
      {
         newVal = StringUtilities.splice(str, "<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
      }

      if (ch == '>')
      {
         newVal = StringUtilities.splice(str, ">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
      }
      return newVal;
   }

   /**
    *
    * Generates and retrieves the string representation of the DCE UUID globally
    * unique identifier. See section 3.5.1 in the XMI spec.
    *
    * @return the string. The form is like this: "DCE.2fac1234-31f8-11b4-a222-08002b34c003"
    *    Perfect for the xmi.uuid attribute.
    *
    */
   public static String retrieveDCEID()
   {
      String retStr = ""; //$NON-NLS-1$
      RandomGUID guid = new RandomGUID();
      retStr = "DCE." + guid.toString(); //$NON-NLS-1$
      return retStr;
   }

   /**
	* Inserts a given child node at the given position in the parent's existing
	* child nodes. Note: to append the child node as the last child, it's easier
	* to directly call parent.add(child) than this method.
	* 
	* It is not necessary to detach the child before calling this method.
	* 
	* @param parent   The parent XML Element.
	* @param child    The child Node to be added to the parent.
	* @param position The number of the child node before which the new child
	*                  will be inserted.
	*/
   public static void insertNode(Element parent, Node child, int position)
   {
	   child.detach();
       
	   List children = parent.elements();
	   if (position >= children.size() || position < 0)
	   {
		   parent.add(child);
		   return ;
	   }
       
	   // The List dom4j gives us is backed by the element, so doing this 
	   // inserts child into parent's children.
	   children.add(position, child);
      
      if(parent.getDocument() instanceof DefaultDocument)
      {
         DefaultDocument parentDoc = (DefaultDocument)parent.getDocument();
       
         if (child instanceof DefaultDocument)
         {
            DefaultDocument childDoc = (DefaultDocument)child;
            parentDoc.setIDTable(childDoc);
         } 
         else if (child instanceof Element)
         {
//            List nodes = selectNodeList(child, "//*[@xmi.id]");
//            for (Iterator iter = nodes.iterator(); iter.hasNext();)
//            {
//               Element curElement = (Element)iter.next();
               Element curElement = (Element)child;
               Attribute attr = curElement.attribute("xmi.id");
               if(attr != null)
               {
                  parentDoc.addIDtoTable(attr.getValue(), curElement);
               }
//            }
         }
      }
      
   }
   
   /**
	* Inserts a given child node before the reference node in the parent's 
	* existing child nodes. Note: to append the child node as the last child, 
	* it's easier to directly call parent.add(child) than this method.
	* 
	* It is not necessary to detach the child before calling this method.
	* 
	* @param parent   The parent XML Element.
	* @param child    The child Node to be added to the parent.
	* @param position The number of the child node before which the new child
	*                  will be inserted.
	*/
   public static void insertNode(Element parent, Node child, Node ref)
   {
	   child.detach();
       
	   List children = parent.elements();
	   for (int i = 0, count = children.size(); i < count; ++i)
	   {
		   Node cur = (Node) children.get(i);
		   if (cur.equals(ref))
		   {
			   children.add(i, child);
			   return;
		   }
	   }
       
	   parent.add(child);
   }

   /**
    * @param node
    * @param string
    * @return
    */
   public static Node getAttribute(Node node, String name)
   {
      Node retNode = null;
      if (node instanceof org.dom4j.Element)
      {
         org.dom4j.Element ele = (org.dom4j.Element)node;
         retNode = ele.attribute(name);
      }
      return retNode;
   }

   /**
    * @param pNode
    * @param pattern
    * @return
    */
   //public static List selectNodeList(Node pNode, String pattern) {
   //	List list = null;
   //	/*if (pNode.getNodeType() == Node.ELEMENT_NODE)
   //	{
   //		org.dom4j.Element elem = (org.dom4j.Element)pNode;
   //		list = elem.getElementsByTagName(pattern);
   //	}*/
   //	
   //	//first get the document for this node.
   //	if (pNode != null)
   //	{
   //		Document doc = pNode.getOwnerDocument();
   //		
   //		//now create a Dom4j document from this.
   //		DOMReader reader = new DOMReader();
   //		org.dom4j.Document document = reader.read(doc);
   //		List myList = document.selectNodes(pattern);
   //	}
   //	return list;
   //}

   //public static org.dom4j.Node selectSingleNode(Node pNode, String pattern) {
   //	org.dom4j.Node node = null;
   //	/*try
   //	{
   //		if (pNode != null)
   //		{
   //			org.dom4j.Element elem = (org.dom4j.Element)pNode;
   //				node = XPathAPI.selectSingleNode(pNode, pattern);
   //			}
   //		}
   //	catch (TransformerException e)
   //	{
   //	}*/
   //	//first get the document for this node.
   //	if (pNode != null)
   //	{
   //		Document doc = null;
   //		if (pNode.getNodeType() == Node.DOCUMENT_NODE)
   //		{
   //			doc = (Document)pNode;
   //		}
   //		else
   //		{
   //			doc = pNode.getOwnerDocument();
   //		}
   //		
   //		//now create a Dom4j document from this.
   //		DOMReader reader = new DOMReader();
   //		org.dom4j.Document document = reader.read(doc);
   //		node = document.selectSingleNode(pattern);
   //	}
   //	return node;
   //}

   public static org.dom4j.Node selectSingleNode(
      org.dom4j.Node pNode,
      String pattern)
   {
      org.dom4j.Node node = null;

      if (pNode != null && pattern != null)
      {
         node = pNode.selectSingleNode(pattern);
      }
      return node;
   }

   public static List selectNodeList(org.dom4j.Node pNode, String pattern)
   {
      List list = null;

      if (pNode != null)
      {
      	try
      	{
         list = pNode.selectNodes(pattern);
      	}
      	catch(InvalidXPathException ex)
        {
            UMLLogger.logException(loggerName, ex, Level.WARNING);
      	}
      }
      return list;
   }

   public static List selectNodeListNS(Node pNode, String pattern)
   {
      List list = null;
      /*try {
      	list = XPathAPI.selectNodeList(pNode, pattern);
      } catch (TransformerException e) {
      }*/

      if (pNode != null)
      {
         Document doc = null;
         if (pNode.getNodeType() == Node.DOCUMENT_NODE)
         {
            doc = (Document)pNode;
         }
         else
         {
            doc = pNode.getDocument();
         }

         //now create a Dom4j document from this.
         //DOMReader reader = new DOMReader();
         //org.dom4j.Document document = reader.read(doc);
         list = doc.selectNodes(pattern);
      }

      return list;
   }

   /**
    * @param doc
    * @param header
    * @return
    */
   public static Document loadXML(String text)
   {
      return loadXML(text, false, new IDResolver("xmi.id"));
   }
   
    /**
     * Loads a DOM Document from the given XML text.
     * 
     * @param text     The XML text to parse and create a Document from.
     * @param validate <code>true</code> to validate the XML against any DTDs.
     * @return The <code>Document</code> created.
     */
    public static Document loadXML(String text, boolean validate)
    {
       return loadXML(text, validate, new IDResolver("xmi.id"));
    }

   /**
    * Loads a DOM Document from the given XML text.
    *
    * @param text     The XML text to parse and create a Document from.
    * @param The ID resolver used to determine IDs.
    * @return The <code>Document</code> created.
    */
   public static Document loadXML(String text, IDResolver resolver)
   {
      return loadXML(text, false, resolver);
   }
   
   /**
    * Loads a DOM Document from the given XML text.
    * 
    * @param text     The XML text to parse and create a Document from.
    * @param validate <code>true</code> to validate the XML against any DTDs.
    * @return The <code>Document</code> created.
    */
   public static Document loadXML(String text, boolean validate, IDResolver resolver)
   {
       try
       {
           //SAXReader reader = new SAXReader(validate);
           SAXReader reader = new SAXReader(DOMDocumentFactory.getInstance(), validate, resolver);
           return reader.read(new StringReader(text));
       }
       catch (Exception ex)
       {
           UMLLogger.logException(loggerName, ex, Level.WARNING);
       }
       return null;
   }
   
   /**
    * Returns the locale specific value for the String message.
    * @param message
    */
   public static String getString(String message)
   {
      try
      {
         //return messages.getString(message);
      }
      catch (java.util.MissingResourceException mr)
      {
         return "!!" + message + "!! not found"; //$NON-NLS-1$ //$NON-NLS-2$
      }
      return null;
   }

   /**
    *  Returns the locale specific message for the given message key, applying
    * a MessageFormat to it with the supplied parameters, if any.
    *
    * @param key       The resource bundle key for the desired message.
    * @param params    Additional parameters to customize the message.
    * @return String   The locale-specific text
    */
   public static String getString(String key, Object[] params)
   {
      String text = getString(key);
      return (params != null) ? MessageFormat.format(text, params) : text;
   }

   /**
    * Retrieves the text of a given node.
    *
    * @param curNode[in] The node to query
    * @param query[in] The query to perform on curNode to find the node in 
    *                  whose text value we require.
    * @param value[out] The text value
    *
    * @return HRESULTs
    */
   public static String retrieveNodeTextValue(Node curNode, String query)
   {
      try
      {
         Node node = curNode.selectSingleNode(query);
         //XPathAPI.selectSingleNode(curNode, query);
         if (node != null)
         {
            return node.getText();
         }
      }
      catch (Exception e)
      {
      }
      return "";//$NON-NLS-1$
   }
   

   ///////////////////////////////////////////////////////////////////////////////
   //
   // HRESULT XMLManip::RemoveChild( IXMLDOMNode* curNode, const xstring& query, IXMLDOMNode** removed )
   //
   // Removes the child node from the passed in node.
   //
   // INPUT:
   //    curNode  -  the node whose child we are removing
   //    query    -  query to perform to get the node
   //
   // OUTPUT:
   //    removed  -  the removed node if successful
   //
   // RETURN:
   //    HRESULTs
   //
   // CAVEAT:
   //    None.
   //
   ///////////////////////////////////////////////////////////////////////////////
   public static Node removeChild(Node node, String query)
   {
      Node remNode = null;
      try
      {
         Node n = node.selectSingleNode(query);
         //XPathAPI.selectSingleNode(node, query);
         if (n != null)
         {
            remNode = removeNode(node, n);
         }
      }
      catch (Exception e)
      {
      }
      return remNode;
   }

   ///////////////////////////////////////////////////////////////////////////////
   //
   // HRESULT XMLManip::RemoveNode( IXMLDOMNode* parent, IXMLDOMNode* node, IXMLDOMNode** removed )
   //
   // Performs the basic node removal
   //
   // INPUT:
   //		parent	-	the parent of the node that is being removed
   //		node		-	the node to remove
   //
   // OUTPUT:
   //		removed	-	holds the removed node if not 0
   //
   // RETURN:
   //		HRESULTs
   //
   // CAVEAT:
   //		None.
   //
   ///////////////////////////////////////////////////////////////////////////////
   private static Node removeNode(Node parent, Node node)
   {
      Node remNode = null;
      if (node != null && parent != null)
      {
         remNode = node.detach(); //parent.removeChild(node);
      }
      return remNode;
   }

   ///////////////////////////////////////////////////////////////////////////////
   //
   // xstring RetrieveSimpleName( IXMLDOMNode* node )
   //
   // Given an element name such as "UML:Model", "Model" will be returned.
   //
   // INPUT:
   //    node -   the node to retrieve its name and to filter. 
   //
   // OUTPUT:
   //    None.
   //
   // RETURN:
   //    The sliced name, else ""
   //
   // CAVEAT:
   //    None.
   //
   ///////////////////////////////////////////////////////////////////////////////
   //public static String retrieveSimpleName(Node node) {
   //	String retName = "";
   //	if (node != null)
   //	{
   //		String name = node.getNodeName();
   //		if (name != null && name.length() > 0)
   //		{
   //			int pos = name.indexOf(":");
   //			if (pos >= 0)
   //			{
   //				retName = name.substring(pos+1);
   //			}
   //		}
   //	}
   //	return retName;
   //}

   public static String retrieveSimpleName(org.dom4j.Node node)
   {
      String retName = ""; //$NON-NLS-1$
      if (node != null)
      {
         String name = node.getName();
         if (name != null && name.length() > 0)
         {
            int pos = name.indexOf(":"); //$NON-NLS-1$
            retName = pos >= 0? name.substring(pos + 1) : name;
         }
      }
      return retName;
   }

   /**
    * Sets the text value of a node.
    *
    * @param curNode[in] The node to query with
    * @param query[in] The query to find the node to set
    * @param value[in] The new value
    * @param useCData[in] true ( the default ) to automatically wrap value in a ![CDATA[...]] block,
    *                else false to just set the data.
    *
    * @return HRESULTs
    */
   public static void setNodeTextValue(
      Node curNode,
      String query,
      String newVal,
      boolean useCData)
   {	
      Node valNode = ensureNodeExists(curNode, query, query);    
      if (valNode != null)
      {
         valNode.setText(removeIllegalXMLChars(newVal));
      }
   }


    /**  
     *   removes illegal for XML 1.0 (Chapter 2.2) characters 
     *   in the 0x0-0x1F range (excluding 0x9, 0xA, 0xD)
     *
     *   @param input[in] The string the illegal chars to be removed from
     *
     *   @return The string with illegal chars removed
     */
    private static String removeIllegalXMLChars(String input) {
	if (input == null) {
	    return null;
	} else if (input.equals("")) {
	    return "";
	}
	StringBuffer output = new StringBuffer("");
	for(int i = 0; i < input.length(); i++ ) {
	    int ch = (int) input.charAt(i);
	    if ( (ch >= 0x20) || (ch == 0x9) || (ch == 0xA) || (ch == 0xD)) {
		output.append((char)ch);
	    }  
	}
	return output.toString();
    }
    

   ///////////////////////////////////////////////////////////////////////////////
   //
   // HRESULT EnsureElementExists( IXMLDOMNode* curNode,
   //                                           const xstring& name, 
   //                                           const xstring& query, 
   //                                           IXMLDOMNode** node )
   //
   // Makes sure that the node with the passed in name is present
   // under curNode. If it isn't, one is created.
   //
   // INPUT:
   //    curNode  - the node to append to.
   //    name     - name of the node to check for existence for. 
   //    query    - the query string to used to check for existence
   //
   // OUTPUT:
   //    node  - the node representing the element
   //
   // RETURN:
   //    HRESULTs
   //
   // CAVEAT:
   //    None.
   //
   ///////////////////////////////////////////////////////////////////////////////
   public static Node ensureNodeExists(
      Node curNode,
      String name,
      String query)
   {
      Node retNode = null;
      try
      {
         retNode = curNode.selectSingleNode(query);
         //XPathAPI.selectSingleNode(curNode, query);
         if (retNode == null)
         {
            // Node doesn't exist, so we need to create it.
            Document doc = curNode.getDocument();            
            if (doc != null)
            {
               retNode = doc.getRootElement().addElement(name);
               appendNewLineElement(curNode, doc);
               retNode.detach();
               ((Element) curNode).add(retNode);
            }
         }
      }
      catch (Exception ex)
      {
          UMLLogger.logException(loggerName, ex, Level.WARNING);
      }
      return retNode;
   }

    /**
     *
     * Appends a newline to the passed in element
     *
     * @param parent[in] The parent to own the text node
     *
     * @return HRESULT
     *
     */
    public static void appendNewLineElement(Node parent, Document doc)
    {
//        if (doc == null)
//        {
//            doc = parent.getDocument();
//        }
//
//        if (doc != null)
//        {
//            //         org.dom4j.Element newLine = doc.addElement("\r\n"); //$NON-NLS-1$
//            //         //parent.appendChild(newLine);
//            //         newLine.setParent((org.dom4j.Element)parent);
//        }
        if (parent instanceof Element)
        {
            ((Element) parent).addText("\n"); //$NON-NLS-1$
        }
    }

   /**
    *
    * Creates a new DOM Element in a given XML namspace. This is a lower
    * level routine than the CreateElement that does not take a namespace
    * as an argument. This call does NOT add the new element to the passed
    * in DOM document.
    *
    * @param element [in] The element that will recieve the new element.
    * @param name[in] The name of the element to create.
    * @param xmlNamespace[in] The XML namespace that the new element should
    *                         belong to. For example, "omg.org/UML/1.4". The
    *                         default is "omg.org/UML/1.4"
    *
    * @return The new DOM node.
    *
    */
   public static Element createElement(Element element, String name, String xmlNamespace)
   {
      Element retVal = null;

      if (name.length() <= 0)
      {
         throw new NullPointerException(UMLSupportResource.getString("INVALID_CREATE_ELEMENT_PARAM"));
      }
      
      int pos = name.indexOf(':');

      if ((xmlNamespace == null || xmlNamespace.length() == 0) &&
            pos > 0)
      {
          QName qname = getQName(element, name);
         retVal = DOMDocumentFactory.getInstance().createElement(qname);
      }
      else if ((xmlNamespace.length() > 0) && (pos > 0))
      {
         String prefix = name.substring(0, pos);
         Namespace namespace = DOMDocumentFactory.getInstance().createNamespace(prefix, xmlNamespace);
         QName qName = DOMDocumentFactory.getInstance().createQName(name.substring(pos + 1), namespace);
         retVal = DOMDocumentFactory.getInstance().createElement(qName);
      }
      else
      {
         retVal = DOMDocumentFactory.getInstance().createElement(name);
      }

      if((element != null) && (retVal != null))
      {
         element.add(retVal);
      }
      
      return retVal;
   }
   
   /**
    *
    * Creates a new DOM Element in a given XML namspace. This is a lower
    * level routine than the CreateElement that does not take a namespace
    * as an argument. This call does NOT add the new element to the passed
    * in DOM document.
    *
    * @param doc[in] The document that will receive the new element.
    * @param name[in] The name of the element to create.
    * @param xmlNamespace[in] The XML namespace that the new element should
    *                         belong to. For example, "omg.org/UML/1.4". The
    *                         default is "omg.org/UML/1.4"
    *
    * @return The new DOM node.
    *
    */
   public static Element createElement(Document doc, String name, String xmlNamespace)
   {
      Element retVal = null;

      if (name.length() <= 0)
      {
			throw new NullPointerException(UMLSupportResource.getString("INVALID_CREATE_ELEMENT_PARAM"));
      }
      
      int pos = name.indexOf(':');

      if ((xmlNamespace == null || xmlNamespace.length() == 0) &&
            pos > 0)
      {
          QName qname = getQName(doc.getRootElement(), name);
          //retVal = DOMDocumentHelper.createElement(qname);
         retVal = DOMDocumentFactory.getInstance().createElement(qname);
      }
      else if ((xmlNamespace.length() > 0) && (pos > 0))
      {
         // I am using StringBuffer because it is suppose to be 
         // faster when building strings.
         StringBuffer uri = new StringBuffer("http://"); //$NON-NLS-1$
         uri.append(xmlNamespace);

         String prefix = name.substring(0, pos);
//         Namespace namespace =
//            DocumentHelper.createNamespace(prefix, uri.toString());
         Namespace namespace = DOMDocumentFactory.getInstance().createNamespace(prefix, uri.toString());
         //QName qName = DocumentHelper.createQName(name.substring(pos + 1), namespace);
         QName qName = DOMDocumentFactory.getInstance().createQName(name.substring(pos + 1), namespace);
         //retVal = DocumentHelper.createElement(qName);
         retVal = DOMDocumentFactory.getInstance().createElement(qName);
      }
      else
      {
         //retVal = DocumentHelper.createElement(name);
         retVal = DOMDocumentFactory.getInstance().createElement(name);
      }

      return retVal;
   }
   
    /**
     * Obtains a DOM4J QName given a namespace prefixed nodeName.
     * @param nodeName The name of the node, probably qualified by a namespace
     *                 prefix.
     * @return A DOM4J QName, with the Namespace correctly set if the namespace
     *         prefix matched a known namespace.
     */
    public static QName getQName(Element ref, String nodeName)
    {
        int nsPrefix;
        if ((nsPrefix = nodeName.indexOf(':')) == -1)
            return new QName(nodeName);
        
        String prefix = nodeName.substring(0, nsPrefix);
        String shortName = nodeName.substring(nsPrefix + 1);
        
        Namespace ns = XMLManip.getNamespace(ref, prefix);
        return ns == null
           ? new QName(nodeName)
           : new QName(shortName, ns, nodeName);
    }
    
    /**
     * Obtains a DOM4J Namespace given a namespace prefix by querying the XMI
     * fragment.
     * @param namespacePrefix A namespace prefix.
     * @return A DOM4J Namespace, if the namespacePrefix was known to us.
     */
    public static Namespace getNamespace(Element ref, String namespacePrefix)
    {
    	Namespace retSpace = null;
        if (namespacePrefix == null)
            return null;
        if (m_Namespaces.containsKey(namespacePrefix))
            return m_Namespaces.get(namespacePrefix);
        
        if(ref != null)
        {
           return ref.getNamespaceForPrefix(namespacePrefix);
        }
        
        // Okay, have we loaded the XMI fragment?
        if (m_XMIFragment == null)
        {
            // TODO: This is an evil, dirty trick. Fix it.
            // Create a dummy project object.
            Project proj = new Project();
            proj.establishXMIHeaderInfo();  // Evil
            
            m_XMIFragment = proj.getDocument();
        }
        
        // Walk the tree, looking for 'namespacePrefix'
        if (m_XMIFragment != null)
        {
			retSpace =
				walkTree(m_XMIFragment.getRootElement(), namespacePrefix);
			m_Namespaces.put(namespacePrefix, retSpace);
        }
        return retSpace;
    }
    
    private static Namespace walkTree(Element elem, String prefix)
    {
        if (prefix.equals(elem.getNamespacePrefix()))
            return elem.getNamespace();
        Namespace ns = elem.getNamespaceForPrefix(prefix);
        if (ns != null)
            return ns;
        for (int i = elem.nodeCount() - 1; i >= 0; --i)
        {
            Node node = elem.node(i);
            if (node instanceof Element)
                walkTree((Element) node, prefix);
        }
        return null;
    }    

	public static Element createElement(Element parentNode, String name)
		throws DOMException
   {
      Element retVal = null;
      
      if((parentNode == null) || (name.length() <= 0))
      {
      	throw new NullPointerException(UMLSupportResource.getString("INVALID_CREATE_ELEMENT_PARAM"));
      }
      
      String xmlNamespace = "";
      
//      int pos = name.indexOf(':');
//      if(pos >= 0)
//      {
//         Namespace space = parentNode.getNamespaceForPrefix(name.substring(0, pos));
//         if(space != null)
//         {
//            xmlNamespace = space.getURI();
//         }
//      }
      
      retVal = createElement(parentNode.getDocument(), name, xmlNamespace);
      if(retVal != null)
      {
          retVal.detach();
      	//appendNewLineElement(retVal, parentNode.getDocument());
         
         
      	 parentNode.add(retVal);
      }
      
      return retVal;
   }
   
   public static Element createElement(Document doc, String name)
   {		
		if((doc == null) || (name.length() <= 0))
		{
			throw new NullPointerException(UMLSupportResource.getString("INVALID_CREATE_ELEMENT_PARAM"));
		}
		
		Element newNode = createElement(doc, name, "");
		if(newNode != null)
		{
			if (doc.getRootElement() == null)
				doc.add(newNode);
			else									
				doc.getRootElement().add(newNode);
		}
		
		return newNode;                               	
   }
   
   public static void saveNodePretty(Node node, String filename) 
   {
       if (filename != null && filename.trim().length() > 0 )
       {
           FileObject fo = FileUtil.toFileObject(new File(filename));
           if (fo != null) 
           {
               OutputStreamWriter out = null;
               XMLWriter xmlWriter = null;
               try {
                   out = new OutputStreamWriter(fo.getOutputStream());
                   xmlWriter = new XMLWriter(out,
                           OutputFormat.createPrettyPrint());
                   xmlWriter.write(node);
                   xmlWriter.flush();
               } catch (Exception ex) {
                   UMLLogger.logException(loggerName, ex, Level.WARNING);
               } finally {
                   try {
                       if (out != null) {
                           out.close();
                       }
                       if (xmlWriter != null) {
                           xmlWriter.close();
                       }
                   } catch (IOException ex) {
                       UMLLogger.logMessage(loggerName, ex.getMessage(), Level.WARNING);
                   }
               }
           }
       }
   }

   public static void saveNode(Node node, String filename)
   {
       if (filename != null && filename.trim().length() > 0 )
       {
           FileObject fo = FileUtil.toFileObject(new File(filename));
           if (fo != null) 
           {
               OutputStreamWriter out = null;
               XMLWriter xmlWriter = null;
               try {
                   out = new OutputStreamWriter(fo.getOutputStream());
                   OutputFormat format = new OutputFormat();
                   format.setNewlines(true);
                   xmlWriter = new XMLWriter(out, format);
                   xmlWriter.write(node);
                   xmlWriter.flush();
               } catch (Exception ex) {
                   UMLLogger.logException(loggerName, ex, Level.WARNING);
               }
               finally {
                    try {
                       if (out != null) {
                           out.close();
                       }
                       if (xmlWriter != null) {
                           xmlWriter.close();
                       }
                   } catch (IOException ex) {
                       UMLLogger.logMessage(loggerName, ex.getMessage(), Level.WARNING);
                   }
                }
           }
       }
   }

   /**
    * @param document
    * @param fileName
    */
   public static boolean save(Document document, String filename)
   {
       boolean successful = false;
       if (filename != null && filename.trim().length() > 0 )
       {
           OutputStreamWriter out = null;
           BufferedWriter xmlWriter = null;
           try {
               FileObject fo = FileUtil.createData(new File(filename));
               out = new OutputStreamWriter(fo.getOutputStream());
               xmlWriter = new BufferedWriter(out);
               xmlWriter.write(document.asXML(), 0, document.asXML().length());
               xmlWriter.flush();
               successful = true;
           } catch (Exception ex) {
               UMLLogger.logException(loggerName, ex, Level.WARNING);
           } finally {
               try {
                   if (out != null) {
                       out.close();
                   }
                   if (xmlWriter != null) {
                       xmlWriter.close();
                   }
               } catch (IOException ex) {
                   UMLLogger.logMessage(loggerName, ex.getMessage(), Level.WARNING);
               }
           }
       }
       return successful;
   }

   public static boolean savePretty(Document document, String filename)
   {
       boolean successful = false;
       if (filename != null && filename.trim().length() > 0 )
       {
           OutputStreamWriter out = null;
           XMLWriter xmlWriter = null;
           try {
               FileObject fo = FileUtil.createData(new File(filename));

               out = new OutputStreamWriter(fo.getOutputStream());
               xmlWriter = new XMLWriter(out, OutputFormat.createPrettyPrint());
               xmlWriter.write(document);
               xmlWriter.flush();
               successful = true;
           } catch (Exception ex) {
               UMLLogger.logException(loggerName, ex, Level.WARNING);
           } finally {
               try {
                   if (out != null) {
                       out.close();
                   }
                   if (xmlWriter != null) {
                       xmlWriter.close();
                   }
               } catch (IOException ex) {
                   UMLLogger.logMessage(loggerName, ex.getMessage(), Level.WARNING);
               }
           }
       }
       return successful;
   }
   
   /**
    * @param element
    * @param b
    */
   public static void DebugXML(Element element, boolean perform)
   {
      if(perform == true)
      {
      	ETSystem.out.println(element.asXML());
      }
      
   }

	/**
	 *
	 * Removes all child nodes from parentNode.
	 *
	 * @param parentNode[in] the element to modify.
	 * 
	 * @return HRESULTs
	 *
	 */
	public static void removeAllChildNodes(Element nodeElement)
	{
		if (nodeElement != null)
		{
			List children = nodeElement.selectNodes(".//*");
			if (children != null)
			{
				int count = children.size();
				for (int i=count-1; i>=0; i--)
				{
					Node node = (Node)children.get(i);
					nodeElement.remove(node);
				}
			}
		}
	}

    /**
     * Returns the first child node of the given XML node.
     * @param node The node in question. Only DOM4J Element Nodes have child
     *             nodes, so passing in any other Node will return null.
     * @return The first child node, or null if there are no children.
     */
    public static Node getFirstChild(Node node)
    {
        if (node instanceof Element)
        {    
            Element el = (Element) node;
            if (el.nodeCount() > 0)
                return el.node(0);
        }
        return null;
    }

    
    /**
     *  A query of "ancestor::*[@attrName='attrValue']" type
     *  
     *  @param node
     *  @param attrName
     *  @param attrValue
     *
     *  @return The list of ancestors with required attribute value. 
     */
    public static List selectAncestorNodesByAttribute(Node node, String attrName, String attrValue) {
	Vector result = new Vector();
	if (node != null 
	    && attrName != null && (! attrName.equals("")) 
	    && attrValue != null) 
	{
	    Node parent = node.getParent();
	    while(parent != null) {
		if (parent instanceof org.dom4j.Element) {
		    org.dom4j.Element parentElem = (org.dom4j.Element) parent;
		    String value = parentElem.attributeValue(attrName);
		    if (value != null 
			&& attrValue.equals(value)) 
		    {
			result.add(parentElem);			
		    }
		}	
		parent = parent.getParent();
	    }
	}
	return result;
    } 

    private static HashMap<String, XPath> cachedXPaths;

    public static XPath getCreateCachedXPath(String query) {
	if (cachedXPaths == null) 
	{
	    cachedXPaths = new HashMap<String, XPath>();
	}
	XPath res = cachedXPaths.get(query);
	if (res == null) 
	{	    	
	    DocumentFactory fact = DOMDocumentFactory.getInstance();
	    res = fact.createXPath(query);
	    if (res != null) 
	    {
		cachedXPaths.put(query, res);
	    }
	}
	return res;
    }

    public static void clearCachedXPaths() {
	cachedXPaths = null;
    }
}
