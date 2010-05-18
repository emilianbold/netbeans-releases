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

package org.netbeans.lib.collab;

import java.util.*;
import org.w3c.dom.*;
import java.io.IOException;
import org.netbeans.lib.collab.util.XMLUtil;
import org.netbeans.lib.collab.util.XMLProcessingException;

/**
 * Presence Document parser.  This class can be used to
 * parse the xml from a presence request and generate a Presence
 * object from it.  Conversely, it can be used to generate an
 * XML presence document from a Presence object.
 *
 * <p><b>Handling of custom PIDF extensions</b></p>
 * <p>
 * When parsing a PIDF doc, the attributes and elements defined
 * in urn:ietf:params:xml:ns:cpim-pidf aka PIDF and 
 * and 
 * urn:sun:icp:xml:ns:cpim-pidf aka ICP's proprietary PIDF extension
 * are extracted and used to populate the presentity object and its
 * presence tuple(s).
 * </p>
 * <p>
 * The presence document may contains fragments defined in other 
 * extensions.  These fragments are stored as XML blobs.  three flavors of 
 * custom PIDF extensions are supported:<ul>
 * <li>pidf:presence sub-element</li>
 * <li>pidf:tuple sub-element</li>
 * <li>pidf:status sub-element</li>
 * </ul>
 * </p>
 * <p>
 * Application MAY NOT rely on the ordering between various sub-elements
 * within any element.  In other words, if an application decides to parse
 * a presence document to obtain a presentity, and then serializes the 
 * presentity back to XML, the resulting XML is not guaranteed to be
 * equal to the original document.  In particular, the ordering of sub-elements
 * within a given element may not be preserved.
 * </p>
 *
 * 
 * @since version 0.1
 * 
 */
public class PresenceHelper
{
    private Presence _presentity = null;

    public final static String ELEMENT_PRESENCE = "presence";
    public final static String ELEMENT_TUPLE = "tuple";
    public final static String ELEMENT_NOTE = "note";
    public final static String ELEMENT_STATUS = "status";
    public final static String ELEMENT_CONTACT = "contact";
    public final static String ELEMENT_BASIC = "basic";
    public final static String ELEMENT_XSTATUS = "xstatus";
    public final static String ELEMENT_TIMESTAMP = "timestamp";

    public final static String ATTRIBUTE_ENTITY = "entity";
    public final static String ATTRIBUTE_ID = "id";
    public final static String ATTRIBUTE_LANG = "lang";
    public final static String ATTRIBUTE_PRIORITY = "priority";

    public final static String XMLNS_PIDF_URI = "urn:ietf:params:xml:ns:cpim-pidf";
    public final static String XMLNS_ICP_URI = "urn:sun:icp:xml:ns:cpim-pidf";
    
    public final static String PIDF_XMLNS = "";
    public final static String ICP_XMLNS = "icp:";
    
    /** 
     * Constructor used generate serialized presence information
     * from a presentity object
     * @param p the presentity object
     */
    public PresenceHelper(Presence p) throws XMLProcessingException
    {
	_presentity = p;
    }
    
    /** 
     * Constructor used to parse XML presence information stream
     * into a Presence object.
     * @param in presence information's input stream
     */
    public PresenceHelper(java.io.InputStream in) throws XMLProcessingException, IOException
    {
        Document doc = XMLUtil.parse(in);
	Element e = doc.getDocumentElement();
	_presentity = buildPresence(e);
    }
        
    /** 
     * Constructor used to parse XML presence information string
     * into a Presence object.
     * @param pi presence information in XML format
     */
    public PresenceHelper(String pi) throws XMLProcessingException 
    {
        Document doc = XMLUtil.parse(pi);
	Element e = doc.getDocumentElement();
	_presentity = buildPresence(e);
    }


    private Presence buildPresence(Element element) throws XMLProcessingException
    {
	Presence p = null;

	NamedNodeMap attrs = element.getAttributes();
	if (attrs == null) {
	    throw new XMLProcessingException("No attributes in root element");
	}

	// get the presentity ID
	Node n = attrs.getNamedItem(ATTRIBUTE_ENTITY);

	if (n != null) {
	    String id = n.getNodeValue();
	    p = new Presence(id);
	    // get namespaces
	    for (int i = 0; i < attrs.getLength() ; i++) {
		n = attrs.item(i);
		if (XMLUtil.isNamespaceAttribute(n)) {
		    String nsURI = n.getNodeValue();
		    if (nsURI.equals(XMLNS_PIDF_URI) ||
			nsURI.equals(XMLNS_ICP_URI)) {
			// those are added by default
			continue;
		    }
                    String name = n.getNodeName();
                    if (name.startsWith("xmlns:")) {
                       name = name.substring(6);
		       p.addNamespace(name, nsURI);
                    } else {
			throw new XMLProcessingException("Invalid namespace: " + name + "=" + nsURI);
                    }
		}
	    }

	    NodeList nl = element.getChildNodes();
	    for (int i = 0 ; i < nl.getLength() ; i++) {
		Node ni = (Node)nl.item(i);
                if (!(ni instanceof Element)) continue;
                Element elt = (Element)ni;

		// get the tuple elements
		// if (elt.getLocalName().equals(ELEMENT_TUPLE)) {
		if (elt.getNodeName().equals(ELEMENT_TUPLE)) {
		    p.addTuple(buildTuple(elt));

		// get the note
		// } else if (elt.getLocalName().equals(ELEMENT_NOTE)) {
		} else if (elt.getNodeName().equals(ELEMENT_NOTE)) {
                    // get language
                    NamedNodeMap nnm = elt.getAttributes();
                    Node nc = nnm.getNamedItem(ATTRIBUTE_LANG);
                    if (nc != null) {
                        String lang = nc.getNodeValue();
                        p.addNote(lang, XMLUtil.getElementText(elt));
                    } else {
		        p.addNote(XMLUtil.getElementText(elt));
                    }

		} else {
                    p.addCustomElement(elt);
             
                }
	    
	    }
	} else {
	    throw new XMLProcessingException("Missing entity attribute");
	}
	// System.err.println("BUILT: " + p.toString());
	return p;
    }
    

    private PresenceTuple buildTuple(Element e) throws XMLProcessingException
    {
	PresenceTuple t = null;

	NamedNodeMap attrs = e.getAttributes();

	// get the presence tuple ID
	Node n = attrs.getNamedItem(ATTRIBUTE_ID);

	if (n != null) {
	    String id = n.getNodeValue();
	    t = new PresenceTuple(id);

	    NodeList nl = e.getChildNodes();
	    for (int i = 0 ; i < nl.getLength() ; i++) {
                Node ni = (Node)nl.item(i);
                if (!(ni instanceof Element)) continue;
		Element elt = (Element)ni;
		// String name = elt.getLocalName();
		String name = elt.getNodeName();
		if (name.equals(ELEMENT_CONTACT)) {
                    // get priority
                    float priority = 0;
                    NamedNodeMap nnm = elt.getAttributes();
                    Node nc = nnm.getNamedItem(ATTRIBUTE_PRIORITY);
                    if (nc != null) {
                        String sPriority = nc.getNodeValue();
                        priority = Float.parseFloat(sPriority);
                    }
                    t.setContact(XMLUtil.getElementText(elt), priority);

		} else if (name.equals(ELEMENT_NOTE)) {
                    // get language
                    NamedNodeMap nnm = elt.getAttributes();
                    Node nc = nnm.getNamedItem(ATTRIBUTE_LANG);
                    if (nc != null) {
                        String lang = nc.getNodeValue();
                        t.addNote(lang, XMLUtil.getElementText(elt));
                    } else {
		        t.addNote(XMLUtil.getElementText(elt));
                    }

		} else if (name.equals(ELEMENT_STATUS)) {
		    readStatus(elt, t);

                } else if (name.equals(ELEMENT_TIMESTAMP)) {
                    t.setLastUpdateTimeStamp(XMLUtil.getElementText(elt));

		} else { // custom extension
                    t.addCustomTupleElement(elt);
		}

	    }
	
	} else {
	    throw new XMLProcessingException("Missing tuple id attribute");
	}

	return t;
    }

    // returns the basic status string from the status element
    private void readStatus(Element statusElement, PresenceTuple t) throws XMLProcessingException
    {
	NodeList nl = statusElement.getChildNodes();
	for (int i = 0 ; i < nl.getLength() ; i++) {
            Node ni = (Node)nl.item(i);
            if (!(ni instanceof Element)) continue;
	    Element elt = (Element)ni;
            // String name = elt.getLocalName();
	    String name = elt.getNodeName();
	    if (name.equals(ELEMENT_BASIC)) {
		// get the contact element
		t.basicStatus = XMLUtil.getElementText(elt);
		//
		// } else if (name.equals(ELEMENT_XSTATUS)) {
            } else if (name.equals(ICP_XMLNS + ELEMENT_XSTATUS)) {
                t.status = XMLUtil.getElementText(elt);

            } else { // custom extension
                t.addCustomStatusElement(elt);

	    }
	}	    
    }    

    
    public Presence getPresence()
    {
	return _presentity;
    }
        
    public Collection getTuples()
    {
	return _presentity.getTuples();
    }
    
    public void addTuple(PresenceTuple t)
    {
        _presentity.addTuple(t);
    }
    
    public String toString() 
    {
        StringBuffer buf = new StringBuffer("<?xml version='1.0'?>\n");
	try {
	    _presentity.copyTo(buf);
	} catch (Exception e) {
	    e.printStackTrace();
	}
        return buf.toString();
    }
        
    public java.io.InputStream getInputStream() throws IOException 
    {   
        byte[] bytes = this.toString().getBytes();
        return new java.io.ByteArrayInputStream(bytes, 0, bytes.length);
    }
    

    /**
     * for testing
     * @param arg array of arguments.  Only the first argument is 
     * used.  It is used to specify the name of a file containing
     * an XML presence document.
     */
    public static void main(String[] arg) throws Exception
    {
	java.io.FileInputStream in = new java.io.FileInputStream(arg[0]);
	PresenceHelper ph = new PresenceHelper(in);
	Presence p = ph.getPresence();
	System.out.println("Entity: " + p.url);
	for (Iterator i = p.getTuples().iterator(); i.hasNext(); ) {
	    PresenceTuple pt = (PresenceTuple)i.next();
	    System.out.println("    Contact          : " + pt.contact);
	    System.out.println("    Status           : " + pt.status);
	}
	System.out.println("\n\n << " + p.toString() + " >>");
    }

}
