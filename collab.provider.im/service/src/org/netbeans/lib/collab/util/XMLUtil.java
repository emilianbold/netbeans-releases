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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.lib.collab.util;

import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;

import org.netbeans.lib.collab.util.StringUtility;

/**
 * XML parsing utilities
 *
 *
 *
 *
 */
public class XMLUtil extends Object
{


    private XMLUtil() {
    }

    private static DocumentBuilderFactory _fac;

    static {
        _fac = DocumentBuilderFactory.newInstance();
        _fac.setValidating(false);
        _fac.setNamespaceAware(false);
	_fac.setIgnoringElementContentWhitespace(true);
	_fac.setIgnoringComments(true);
	_fac.setCoalescing(true);
    }
    
    public static Document parse(java.io.InputStream in) throws XMLProcessingException, IOException
    {
	try {
	    DocumentBuilder builder = _fac.newDocumentBuilder();
	    return builder.parse(in);
	} catch (SAXException se) {
	    //se.printStackTrace();
	    throw new XMLProcessingException("Error while parsing the stream : " + se.toString());
	} catch (ParserConfigurationException pce) {
	    //pce.printStackTrace();
	    throw new XMLProcessingException("Error while parsing the stream : " + pce.toString());
	}
    }
    
    public static Document parse(InputSource in) throws XMLProcessingException, IOException
    {
	try {
	    DocumentBuilder builder = _fac.newDocumentBuilder();
	    return builder.parse(in);
	} catch (SAXException se) {
	    //se.printStackTrace();
	    throw new XMLProcessingException("Error while parsing the stream : " + se.toString());
	} catch (ParserConfigurationException pce) {
	    //pce.printStackTrace();
	    throw new XMLProcessingException("Error while parsing the stream : " + pce.toString());
	}
    }
    
    public static Document parse(String in) throws XMLProcessingException
    {
	try {
	    //	    System.out.println("PARSING XML DOC: " + in);

 	    StringReader strReader = new StringReader(in);
 	    strReader.ready();
 	    InputSource inputSrc = new InputSource(strReader);
 	    return parse(inputSrc);

// 	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
// 	    OutputStreamWriter osw = new OutputStreamWriter(baos, "UTF-8");
// 	    char[] c = in.toCharArray();
// 	    osw.write(c, 0, c.length);
// 	    osw.flush();
// 	    byte[] b = baos.toByteArray();
// 	    ByteArrayInputStream bais = new ByteArrayInputStream(b);
// 	    return parse(bais);
	    
	} catch (IOException ioe) {
	    throw new XMLProcessingException("Error while parsing the stream : " + ioe.toString());
	}
    }



    public static void appendElementTag(StringBuffer buf,
					 String namespace,
					 String tagName,
					 boolean endTag,
					 boolean closeTag)
    {
	if (endTag) {
	    buf.append("</");
	} else {
	    buf.append("<");
	}
	buf.append(namespace);
	buf.append(tagName);
	if (endTag || closeTag) {
	    buf.append(">");
	}
    }

    public static String getElementText(Element e)
    {
	NodeList nl = e.getChildNodes();
	for (int i = 0 ; i < nl.getLength() ; i++) {
	    Node n = (Node)nl.item(i);
	    if (n.getNodeType() == Node.TEXT_NODE) {
		return n.getNodeValue();
	    }
	}
	return null;
    }

    public static String getNamespacePrefix(Element elt) throws XMLProcessingException
    {
	String s = elt.getTagName();
	int c = s.indexOf(':');
	if (c < 0) { 
	    throw new XMLProcessingException("unexpected element: " + s);
	} else {
	    return s.substring(c);
	}
    }


    public static String dumpNodeAsString(Node node) throws XMLProcessingException
    {
	try {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    dumpNode(node, baos);
	    return baos.toString();
	} catch (IOException e) {
	    throw new XMLProcessingException(e.toString());
	}
    }




    /**
     * This method allows to dump a Document/Node to an outputStream.
     * @param node     The Node to dump.
     * @param outputStream Where to dump it.
     * @throws XMLProcessingException
     */
    public static void dumpNode(Node node, OutputStream outputStream)
                   throws XMLProcessingException, IOException
    {
        BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        unparse(node, outWriter, 0); 
        outWriter.write("\n");
        outWriter.flush();
    }

    /**
     * This method allows to dump a Document/Node to a file.
     * @param node     The Node to dump.
     * @param filename Where to dump it.
     * @throws XMLProcessingException
     */
    public static void dumpNode(Node node, String filename)
                   throws XMLProcessingException, IOException
    {
	FileOutputStream stream = new FileOutputStream(filename);
        BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8"));
        unparse(node, outWriter, 0);
        outWriter.flush();
        stream.flush();
        stream.close();
    }


    /**
     * This method dump all the childrens of a Document/Node to an outputStream but not the Node itself
     * @param node     The Node to dump.
     * @param outputStream Where to dump it.
     * @throws XMLProcessingException
     */
    public static void dumpChildrenOfNode(Node node, OutputStream outputStream)
                   throws XMLProcessingException, IOException
    {
        BufferedWriter outWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        Node child = node.getFirstChild();

        while (child != null) {
          unparse(child, outWriter, 0); 
          outWriter.write("\n");
          outWriter.flush();
          child = child.getNextSibling();
        }
    }
    
    /**
     * This method dump all the childrens of a Document/Node to an outputStream but not the Node itself
     * @param node     The Node to dump.
     * @param filename Where to dump it.
     * @throws XMLProcessingException
     */
    public static void dumpChildrenOfNode(Node node, String filename)
                   throws XMLProcessingException, IOException
    {
	FileOutputStream stream = new FileOutputStream(filename);
        dumpChildrenOfNode(node, stream);
        stream.flush();
        stream.close();
    }    

    /**
     * This method allows to unparse an xml node to a stream
     * @param node the document/node to unparse
     * @param out  Where to dump the unparsed document
     * @param processSiblings tell wether or not we have to process siblings of node
     * @return A boolean indicating whether last printed line contains a carriage return or not, this is for internal use only
     */
    private static void unparse(Node node,
                                Writer outWriter,
                                int indent)
                             throws XMLProcessingException
    {
        int i;
        int type = node.getNodeType();
        try {
          switch (type) {
            case Node.ATTRIBUTE_NODE:
              String attribValue=node.getNodeValue();
              if (attribValue == null) {
                outWriter.write(" "+node.getNodeName()+"=\"!!!null!!!\"");
              }
              else {
                /* Escape the double quote character in attribute value */
                outWriter.write(" "+node.getNodeName()+"=\""+escape(attribValue, true)+"\"");
              }
              break;
            case Node.CDATA_SECTION_NODE:
              throw new XMLProcessingException("Tag exception : CDATA");
            case Node.COMMENT_NODE:
              throw new XMLProcessingException("Tag exception : COMMENT");
            case Node.DOCUMENT_FRAGMENT_NODE:
              throw new XMLProcessingException("Tag exception : DOC_FRAG");
            case Node.DOCUMENT_NODE:
              outWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
              if (node.hasChildNodes()) {
                Node curChild = node.getFirstChild();
                while (curChild != null) {
                  outWriter.write("\n");
                  unparse(curChild, outWriter, indent);
                  curChild = curChild.getNextSibling();
                }
              }
              break;
            case Node.DOCUMENT_TYPE_NODE:
                outWriter.write("<!DOCTYPE "+node.getNodeName()+">\n");
                break;
            case Node.ELEMENT_NODE:
              outWriter.write("<"+node.getNodeName());
              NamedNodeMap attribsnode=node.getAttributes();
              for (i=0; i<attribsnode.getLength(); i++) {
                Node attrNode = attribsnode.item(i);
                String attrValue=attrNode.getNodeValue();
                if (attrValue == null) {
                  outWriter.write(" "+attrNode.getNodeName()+"=\"!!!null!!!\"");
                }
                else {
                  /* Escape the double quote character in attribute value */
                  outWriter.write(" "+attrNode.getNodeName()+"=\""+escape(attrValue, true)+"\"");
                }
              }
              if (node.hasChildNodes()) {
                outWriter.write(">");

                Node curChild = node.getFirstChild();
                Node firstChild = node.getFirstChild();
                Node lastChild  = node.getLastChild();
                
                while (curChild != null) {
                  if (curChild.getNodeType() != Node.TEXT_NODE) {
                    if (curChild == firstChild) {
                      outWriter.write("\n");
                      outputIndentation(outWriter, indent+1);
                    } 
                    else 
                      if ((curChild.getPreviousSibling() != null) &&
                          (curChild.getPreviousSibling().getNodeType() != Node.TEXT_NODE)) {
                        outWriter.write("\n");
                        outputIndentation(outWriter, indent+1);
                      }
                  }
                  unparse(curChild, outWriter, indent+1);
                  curChild = curChild.getNextSibling();
                }
                if ((firstChild.getNodeType() != Node.TEXT_NODE) && 
                    (lastChild.getNodeType() != Node.TEXT_NODE)) {
                  outWriter.write("\n");
                  outputIndentation(outWriter, indent);
                }                  
                outWriter.write("</"+node.getNodeName()+">");
              }
              else {
                outWriter.write(" />"); 
              }
              break;
            case Node.ENTITY_NODE:
              throw new XMLProcessingException("Tag exception : ENTITY");
            case Node.ENTITY_REFERENCE_NODE:
              throw new XMLProcessingException("Tag exception : ENTTIY_REF");
            case Node.NOTATION_NODE:
              throw new XMLProcessingException("Tag exception : NOTATION");
            case Node.PROCESSING_INSTRUCTION_NODE:
              outputIndentation(outWriter, indent);
              outWriter.write("<?"+node.getNodeName()+" "+node.getNodeValue());
              if (node.hasChildNodes()) {
                  outWriter.write(">");
                  
                  unparse(node.getFirstChild(), outWriter, indent+1);
                  
                  outWriter.write("\n");
                  outputIndentation(outWriter, indent);
                  
                  outWriter.write("</"+node.getNodeName()+">");
              }
              else outWriter.write(" ?>\n");
              break;
            case Node.TEXT_NODE:
              String  nodeValue = node.getNodeValue();
              outWriter.write(escape(nodeValue));
              break;
            default:
              throw new XMLProcessingException("Tag exception : UNSUPPORTED NODE : "+type);            
          }
      } catch(Exception e) {
	  throw new XMLProcessingException("An error occured while dumping : "+e.toString());
      } 
    }
                  
    private static final String basicIndent = " ";	
    /**
     * Indent to the current level in multiples of basicIndent
     */
    private static void outputIndentation(Writer outWriter, int indent) 
            throws XMLProcessingException, IOException
    {
        for (int i = 0; i < indent; i++) {
          outWriter.write(""+basicIndent);
        }
    }
    
    private static Node stripWhiteSpaceNodes(Node node) 
    {
        boolean lastNodeRemoved=false;
        Node curNode=node.getFirstChild();
//        System.out.println("Entering stripWhiteSpaceNodes : "+curNode.getNodeType());
        while (curNode != null)
        {
            if (curNode.hasChildNodes())
                stripWhiteSpaceNodes(curNode);
            if (curNode.getNodeType()==Node.TEXT_NODE)
            {
//                System.out.println("  Text node found, value='"+curNode.getNodeValue()+"'");
                if (curNode.getNodeValue()!=null)
                    if ((curNode.getNodeValue().trim().length()==0)
                        ||(curNode.getNodeValue().trim().endsWith("\n"))
                        ||(curNode.getNodeValue().trim().startsWith("\n")))
                    {
//                        System.out.println("    This is a whitespace node");                    
                        if ((curNode.getPreviousSibling()!=null) || (curNode.getNextSibling()!=null))
                        {
                            node.removeChild(curNode);
                            lastNodeRemoved=true;
//                            System.out.println("      Just removed a node");
                        }
                    }
            }
            curNode=(lastNodeRemoved)?node.getFirstChild():curNode.getNextSibling();
            lastNodeRemoved=false;
        }
        return node;
    }
    
    private static String escape(final String original, boolean escapeQuotes)
    {
        String str2Return=StringUtility.substitute(original, "&", "&amp;");
        str2Return=StringUtility.substitute(str2Return, "<", "&lt;");
        str2Return=StringUtility.substitute(str2Return, ">", "&gt;");
        if (escapeQuotes) {
          //str2Return=StringUtility.substitute(str2Return, "'", "&apos;");
          str2Return=StringUtility.substitute(str2Return, "\"", "&quot;");        
        }
        return(str2Return);        
    }
    private static String escape(final String original)
    {
      return (escape(original, false));
    }


    /**
     * This method given a myDoc, myNode and string, parse the latter 
     * and attach to the myNode of myDoc the nodes representing the content of the string
     * @param doc The Document which owe the Node node
     * @param node The Node in which append nodes represented by the string
     * @param str String reprensenting one or more nodes to parse
     * @throws XMLProcessingException
     */
    public static void insertNodeFromString(Document doc, Node node, String str) throws XMLProcessingException, IOException
    {
	Document dummyDoc;
	Node curNode;
	Node parentNode;
	
        dummyDoc = createDocFromString(str);
        if (dummyDoc != null) {
	    parentNode=dummyDoc.getFirstChild();
	    if (parentNode != null) {
		curNode=parentNode.getFirstChild();
		while (curNode != null) {
		    node.appendChild(doc.importNode(curNode, true));
		    curNode = curNode.getNextSibling(); 
		}
	    }
        }
    }
    
    
    /**
     * This method given a string, parse it and returns a document containing nodes
     * representing the content of the string
     * @param str The String to parse.
     * @return A dummy Document containing the nodes representing the string
     * @throws XMLProcessingException
     */
    public static Document createDocFromString(String str) throws XMLProcessingException
    {
	Document dummyDoc = null;
	StringBuffer strBuff   = new StringBuffer();
	
	strBuff.append("<?xml version=\"1.0\"?>");
	strBuff.append("<dummyDocument>");
	strBuff.append(str);
	strBuff.append("</dummyDocument>");      

	return parse(strBuff.toString());
    }
    

    public static String getPrefixFromXMLString(String sElement) throws XMLProcessingException
    {
	int ix1 = sElement.indexOf('<');
	if (ix1 >= 0) {
	    int ix2 = sElement.indexOf(':');
	    if (ix2 > ix1+1) {
		// todo analyze all characters
		return sElement.substring(ix1+1, ix2);
	    } else {
		throw new XMLProcessingException("invalid XML: " + sElement);
	    }
	} else {
	    throw new XMLProcessingException("invalid XML: " + sElement);
	}
    }


    public static boolean isNamespaceAttribute(Node n)
    {
	String prefix = n.getPrefix();
	if (prefix != null) {
	    return prefix.equals("xmlns");
	} else {
	    //	    if (n.getLocalName() != null) {
	    //		return n.getLocalName().equals("xmlns"); // This implies a null Name part!
	    //	    }
	    return n.getNodeName().startsWith("xmlns:");
	}
    }

    public static String replacePrefix(String in, String oldPrefix, String newPrefix)
    {
	String out = in;
	out = StringUtility.substitute(out, "<" + oldPrefix + ":", "<" + newPrefix + ":");
	out = StringUtility.substitute(out, "</" + oldPrefix + ":", "</" + newPrefix + ":");
	return out;
    }

}
