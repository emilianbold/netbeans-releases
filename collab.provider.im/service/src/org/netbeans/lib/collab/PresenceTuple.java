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
import javax.xml.parsers.*;

import org.netbeans.lib.collab.PresenceService;
import org.netbeans.lib.collab.util.XMLUtil;
import org.netbeans.lib.collab.util.XMLProcessingException;

/**
 *
 *
 * @since version 0.1
 *
 */
public class PresenceTuple extends java.lang.Object {

    /**
     * notes
     */
    protected HashMap _notes;

    /**
     * Tuple ID
     */
    protected String id;

    /**
     * Presence ID
     */
    protected String presentityID;
    
    /**
     * custom markup
     */
    protected float priority;
    
    /**
     * communication address
     */
    protected String contact;
    
    /** 
     * extended presence status
     */
    protected String status;
    
    /**
     * PIDF basic status (OPEN or CLOSED)
     */
    protected String basicStatus;

    /**
     */
    protected Presence presentity;


    protected HashMap _namespaces;

    private String _lastUpdated;

    /**
     * constructor without specified priority
     * (no priority attribute is added in pidf:tuple)
     * 
     * @param id tuple id.  A tuple id must be unique within a presentity.
     * This argument is used to generate the id attribute of the PIDF
     * tuple element.
     * 
     */
    public PresenceTuple(String id) 
    {
	this.id = id;
    }

    /**
     * constructor without specified priority or id
     * should be used when the presence is defined in XMPP format
     * 
     * 
     */
    public PresenceTuple() 
    {
    }

    /**
     * constructor without specified priority
     * (no priority attribute is added in pidf:tuple)
     * 
     * @param id tuple id.  A tuple id must be unique within a presentity.
     * This argument is used to generate the id attribute of the PIDF
     * tuple element.
     * @param contact contact address associated with this tuple. 
     * This argument is used as the text inside the PIDF contact element.
     * @param status status string.  Must be one of the status values 
     * defined in org.netbeans.lib.collab.PresenceService.  This value 
     * is used to create the contents of the PIDF basic element and the 
     * ICP xstatus element.
     *
     * @exception IllegalArgumentException unknown status value
     * or invalid address format
     * 
     * @see org.netbeans.lib.collab.PresenceService
     */
    public PresenceTuple(String id, String contact, String status) throws IllegalArgumentException
    {
	this.id = id;
	setContact(contact, 0);
	setStatus(status);
    }
    
    /**
     * constructor with specified priority
     *
     * @param id tuple id.  A tuple id must be unique within a presentity.
     * This argument is used to generate the id attribute of the PIDF
     * tuple element.
     * @param contact contact address associated with this tuple. 
     * This argument is used as the text inside the PIDF contact element.
     * @param status status string.  Must be one of the status values 
     * defined in org.netbeans.lib.collab.PresenceService.  This value 
     * is used to create the contents of the PIDF basic element and the 
     * ICP xstatus element.
     * @param priority priority to use as value the PIDF priority attribute
     * (part of tuple element). 
     *
     * @exception IllegalArgumentException priority not within the 0-1 range,
     * or unknown status value
     * or invalid address format
     * 
     */
    public PresenceTuple(String id, String contact, String status, float priority)  throws IllegalArgumentException
    {
	this.id = id;
	setContact(contact, priority);
	setStatus(status);
    }
    
    /**
     * get the note corresponding to a specified language
     * @param lang language tag
     * @return note
     */
    public String getNote(String lang)
    {
        if (_notes != null) {
            return (String)_notes.get(lang);
        } else {
            return null;
        }
    }

    /**
     * get the note for the default language.
     * if there is no note for the default language and there is
     * only one note, this unique note is returned.
     * @return note
     */
    public String getNote()
    {
        if (_notes == null) return null;
	String text = getNote(System.getProperty("user.language"));
        if (text == null && _notes.size() == 1) {
            text = (String)_notes.values().iterator().next();
        }
        return text;
    }

    /**
     * get all the notes in all the language.
     * @return notes
     */
    public Map getNotes()
    {
        return _notes;
    }
    
    /**
     * set the note for a specified language
     * @param lang language tag
     * @param text note content
     */
    public void addNote(String lang, String text)
    {
        if (_notes == null) _notes = new HashMap(3);
        _notes.put(lang, text);
    }

    /**
     * add a note in the default language.  This overrides any existing
     * note added for the default language
     * @return note
     */
    public void addNote(String text)
    {
        addNote(System.getProperty("user.language"), text);
    }


    /**
     * Get the last-update timestamp in XML dateTime format
     * @return last-update timestamp if any has been provided, null
     * otherwise
     */
     public String getLastUpdateTimeStamp()
     {
        return _lastUpdated;
    }
    

    /**
     * Set the last-update timestamp in XML dateTime format
     * @param ts new timestamp
     */
     public void setLastUpdateTimeStamp(String ts)
     {
         _lastUpdated = ts;
    }

    /**
     * @return Tuple ID
     */
    public String getId()
    {
	return id;
    }
    
    /**
     * @return containing presentity ID
     */
    public String getPresenceURL()
    {
	return presentityID;
    }
    
    /**
     * @return priority
     */
    public float getPriority()
    {
	return priority;
    }
    
    /**
     * @return communication address
     */
    public String getContact()
    {
	return contact;
    }
    
    /** 
     * @return presence status
     * @see org.netbeans.lib.collab.PresenceService
     */
    public String getStatus()
    {
	return this.status;
    }

    /**
     * @return basic status (OPEN or CLOSED)
     */
    public String getBasicStatus()
    {
	return this.basicStatus;
    }


    /**
     * @return priority
     * @param priority priority to use as value the PIDF priority attribute
     * (part of tuple element).
     * @exception IllegalArgumentException priority not within the 0-1 range
     */
    public void setPriority(float priority) throws IllegalArgumentException
    {
	if (priority > 1 || priority < 0) {
	    throw new IllegalArgumentException("tuple priority must range from 0 to 1");
	}
	this.priority = priority;
    }
    
    /**
     * set communication address
     * @param contact contact address associated with this tuple. 
     * This argument is used as the text inside the PIDF contact element.
     * @param priority priority to use as value the PIDF priority attribute
     * (part of tuple element).
     * @exception IllegalArgumentException invalid address format
     */
    public void setContact(String contact, float priority) throws IllegalArgumentException
    {
	this.contact = contact;
        setPriority(priority);
    }
    
    /**
     * set communication address
     * @param contact contact address associated with this tuple.
     * This argument is used as the text inside the PIDF contact element.
     * @exception IllegalArgumentException invalid address format
     */
    public void setContact(String contact) throws IllegalArgumentException
    {
        this.contact = contact;
    }


    /** 
     * set ICP and PIDF presence status
     * @param status status string.  Must be one of the status values 
     * defined in org.netbeans.lib.collab.PresenceService.  This value 
     * is used to create the contents of the PIDF basic element and the 
     * ICP xstatus element.
     * @exception IllegalArgumentException unknown status value
     * @see org.netbeans.lib.collab.PresenceService
     */
    public void setStatus(String status) throws IllegalArgumentException
    {
	this.status = status;
        if (id == null) return;
	if (status.equals(PresenceService.STATUS_CLOSED) ||
	    status.equals(PresenceService.STATUS_FORWARDED)) {
	    basicStatus = PresenceService.STATUS_CLOSED;
	} else if (status.equals(PresenceService.STATUS_OPEN) ||
		   status.equals(PresenceService.STATUS_IDLE) ||
		   status.equals(PresenceService.STATUS_BUSY) ||
		   status.equals(PresenceService.STATUS_AWAY) ||
                   /*status.equals(PresenceService.STATUS_XA) ||*/
                   status.equals(PresenceService.STATUS_CHAT)){
	    basicStatus = PresenceService.STATUS_OPEN;
	} else {
	    throw new IllegalArgumentException("unknown status value: " + status);
	}
    }




    /**
     * returns an serialized XML fragment for this tuple
     */
    public String toString()
    {
        if (id == null) return "Not Implemented";
	StringBuffer buf = new StringBuffer();
	try {
	    copyTo(buf);
	} catch (Exception e) {
	    e.printStackTrace();	    
	}
	return buf.toString();
    }

    /**
     * append tag to StringBuffer
     * @param buf buffer to copy to
     */
    protected void copyTo(StringBuffer buf) throws Exception
    {
	String s;
	XMLUtil.appendElementTag(buf, PresenceHelper.PIDF_XMLNS,
				 PresenceHelper.ELEMENT_TUPLE,
				 false, false);
	buf.append(" " + PresenceHelper.ATTRIBUTE_ID + "='" + id + "'>");

	XMLUtil.appendElementTag(buf, PresenceHelper.PIDF_XMLNS,
				 PresenceHelper.ELEMENT_CONTACT,
				 false, false);
	if (priority != 0.0) {
	  buf.append(" " + PresenceHelper.ATTRIBUTE_PRIORITY + "='" +
			priority + "'>");
	}
	else {
	  buf.append(">");
	}
	buf.append(contact);
	XMLUtil.appendElementTag(buf, PresenceHelper.PIDF_XMLNS,
				 PresenceHelper.ELEMENT_CONTACT,
				 true, true);

	XMLUtil.appendElementTag(buf, PresenceHelper.PIDF_XMLNS,
				 PresenceHelper.ELEMENT_STATUS,
				 false, true);
	if (basicStatus != null) {
	    XMLUtil.appendElementTag(buf, PresenceHelper.PIDF_XMLNS,
				     PresenceHelper.ELEMENT_BASIC,
				     false, true);
	    buf.append(basicStatus);
	    XMLUtil.appendElementTag(buf, PresenceHelper.PIDF_XMLNS,
				     PresenceHelper.ELEMENT_BASIC,
				     true, true);
	}

	XMLUtil.appendElementTag(buf, PresenceHelper.ICP_XMLNS,
				 PresenceHelper.ELEMENT_XSTATUS,
				 false, true);
	buf.append(status);
	XMLUtil.appendElementTag(buf, PresenceHelper.ICP_XMLNS,
				 PresenceHelper.ELEMENT_XSTATUS,
				 true, true);

	XMLUtil.appendElementTag(buf, PresenceHelper.PIDF_XMLNS,
				 PresenceHelper.ELEMENT_STATUS,
				 true, true);
	
	if (_notes != null && _notes.size() > 0) {
	  for (Iterator i = _notes.entrySet().iterator(); i.hasNext(); ) {
            java.util.Map.Entry entry = (java.util.Map.Entry)i.next();
            String lang = (String)entry.getKey();
            String note = (String)entry.getValue();
	    XMLUtil.appendElementTag(buf, PresenceHelper.PIDF_XMLNS,
				     PresenceHelper.ELEMENT_NOTE,
				     false, false);
	    buf.append(" " + PresenceHelper.ATTRIBUTE_LANG + "='" + lang + 
			"'>" + note);
	    XMLUtil.appendElementTag(buf, PresenceHelper.PIDF_XMLNS,
				     PresenceHelper.ELEMENT_NOTE,
				     true, true);
	  }
	}

        s = getSerializedCustomTupleElements();
	if (s != null) {
             buf.append(s);
        }

	XMLUtil.appendElementTag(buf, PresenceHelper.PIDF_XMLNS,
				 PresenceHelper.ELEMENT_TUPLE,
				 true, true);
    }


    public boolean equals(Object o)
    {
        if (id == null) return false;
	PresenceTuple other = (PresenceTuple)o;
	return other.getId().equals(getId());
    }


    /**
     * add a namespace declaration to be used in elements of the 
     * presence document.
     * @return namespace prefix used in case a conflict was detected.
     * null if no conflict was detected.
     */
    private String addNamespace(String prefix, String nsURI)
    {
	String uniquePrefix = prefix;
	String res = null;

	if (_namespaces == null) _namespaces = new HashMap();

	// look for a possible prefix conflict
	for (int i=0; ; i++) {
	    String origURI;
	    if (presentity == null) {
		origURI = (String)_namespaces.get(uniquePrefix);
	    } else {
		origURI = presentity.getNamespaceURI(uniquePrefix);
	    }

	    if (origURI != null && !origURI.equalsIgnoreCase(nsURI)) {
		// conflict
		uniquePrefix = Integer.toString(nsURI.hashCode() + i);
		res = uniquePrefix;
	    } else {
		break;
	    }
	}

	_namespaces.put(uniquePrefix, nsURI);

	// add to containing presentity
	if (presentity != null) {
	    presentity.addNamespace(uniquePrefix, nsURI);
	}

	return res;
    }

    // s can be a prefix or a URI
    private String getNamespaceURI(String s)
    {
	if (s.indexOf(':') > 0) return s;

	String nsURI = null;
	if (_namespaces != null) {
	    nsURI = (String)_namespaces.get(s);
	    if (nsURI != null) return nsURI;
	}

	
	if (nsURI == null && presentity != null) {
	    nsURI = presentity.getNamespaceURI(s);
	}

	return nsURI;
    }
    
    // ---------------------------------------------------------
    // custom tuple elements
    // ---------------------------------------------------------

    private HashMap customTupleElements;
    private StringBuffer serializedCustomTupleElements;

    /**
     * add a custom tuple element as a Node.  This is used by PresenceHelper
     * to populate the PresenceTuple during parsing.
     * @param element custom tuple sub-element to add
     */
    protected void addCustomTupleElement(Element element)  throws XMLProcessingException
    {
        if (customTupleElements == null) customTupleElements = new HashMap();
	List list = (List)customTupleElements.get(element.getNamespaceURI());
	if (list == null) {
	    list = new LinkedList();
	    list.add(element);
	    customTupleElements.put(element.getNamespaceURI(), list);
	} else {
	    list.add(element);
	}
    }

    /**
     * Add a serialized custom tuple element as a Node.
     * This is used by application to add custom tuple sub-elements
     * while buiding a presence tuple.  The input xml string must 
     * not contain the XML header.
     * <p>
     * Elements added using this method may not be
     * retrieved immediately as a Node using getCustomTupleElement.  To do
     * this the presence document needs to be built and then parsed.
     * </p>
     *
     * @param sElement custom tuple sub-element to add in serialized
     * form.
     * @param validateXML verify that sElement contains well-formed
     * XML.
     * @param namespaceURI XML namespace URI corresponding to the 
     * prefix used in the XML fragment.  Note that only one custom
     * namespace may be used in the xml fragment.
     *
     * @exception XMLProcessingException ill-formed XML or missing or
     * unrecognized namespace prefix
     */
    public void addSerializedCustomTupleElement(String sElement,
						String namespaceURI,
						boolean validateXML) throws XMLProcessingException
    {
	String prefix = null;
	Document dummyDoc = null;
	String s = sElement;

	if (validateXML) {
	    // validate XML
	    dummyDoc = XMLUtil.createDocFromString(sElement);
	}

	if (namespaceURI != null) {
	    if (validateXML) {
		if (dummyDoc != null) {
		    Node parentNode = dummyDoc.getFirstChild();
		    if (parentNode != null) {
			Node curNode = parentNode.getFirstChild();
			prefix = XMLUtil.getNamespacePrefix((Element)curNode);
		    }
		}
	    } else {
		// get the prefix manually
		prefix = XMLUtil.getPrefixFromXMLString(sElement);
	    }
	    
	    String newPrefix = addNamespace(namespaceURI, prefix);
	    if (newPrefix != null) {
		s = XMLUtil.replacePrefix(sElement, prefix, newPrefix);
	    }
	}


        if (serializedCustomTupleElements == null) {
	    serializedCustomTupleElements = new StringBuffer(s);
	} else {
	    serializedCustomTupleElements.append(s);
	}
    }

    /**
     * return a concatenation of all custom tuple elements.  This method is 
     * used by the presence store to obtain a serialized XML fragment
     * containing all custom tuple elements of this tuple. 
     */
    public String getSerializedCustomTupleElements() throws XMLProcessingException
    {
	if (customTupleElements == null) return null;
	StringBuffer buf = serializedCustomTupleElements;
	for (Iterator i = customTupleElements.values().iterator(); i.hasNext(); ) {
	    List list = (List)i.next();
	    if (list != null) {
		for (Iterator j = list.iterator(); j.hasNext(); ) {
		    Element elt = (Element)j.next();
		    String s = XMLUtil.dumpNodeAsString(elt);
		    if (buf == null) {
			buf = new StringBuffer(s);
		    } else {
			buf.append(s);
		    }
		}
	    }
	}

        if (buf != null) {
	    return buf.toString();
	} else {
	    return null;
	}
    }

    /**
     * returns a list of Element objects belonging to a specific XML
     * namespace.  This allows applications to retrieve only elements
     * they are interested in and ignore other custom tuple extentions.
     * @param namespace namespace URI or prefix.
     */
    public List getCustomTupleElements(String namespace)
    {
	if (customTupleElements == null) return null;

	String nsURI = getNamespaceURI(namespace);
	if (nsURI == null) return null;

	return (List)customTupleElements.get(nsURI);
    }

    /**
     * return a serialized XML fragment which is a concatenation of all
     * tuple sub-elements belonging to the specified namespace.  
     * This allows applications to retrieve only elements
     * they are interested in and ignore other custom extentions.
     */
    public String getSerializedCustomTupleElements(String namespace) throws XMLProcessingException
    {
	if (customTupleElements == null) return null;

	String nsURI = getNamespaceURI(namespace);
	if (nsURI == null) return null;

	List list = getCustomTupleElements(nsURI);
	StringBuffer buf = null;
	if (list != null) {
	    for (Iterator j = list.iterator(); j.hasNext(); ) {
		Element elt = (Element)j.next();
		String s = XMLUtil.dumpNodeAsString(elt);
		if (buf == null) {
		    buf = new StringBuffer(s);
		} else {
		    buf.append(s);
		}
	    }
	}
        if (buf != null) {
	    return buf.toString();
	} else {
	    return null;
	}
    }



    // ---------------------------------------------------------
    // custom status elements
    // ---------------------------------------------------------

    private HashMap customStatusElements;
    private StringBuffer serializedCustomStatusElements;

    /**
     * add a custom status element as a Node.  This is used by PresenceHelper
     * to populate the PresenceTuple during parsing.
     * @param element custom tuple sub-element to add
     */
    protected void addCustomStatusElement(Element element) throws XMLProcessingException
    {
        if (customStatusElements == null) customStatusElements = new HashMap();
	List list = (List)customStatusElements.get(element.getNamespaceURI());
	if (list == null) {
	    list = new LinkedList();
	    list.add(element);
	    customStatusElements.put(element.getNamespaceURI(), list);
	} else {
	    list.add(element);
	}
    }

    /**
     * Add a serialized custom status element as a Node.
     * This is used by application to add custom status sub-elements
     * while buiding a presence tuple.  The input xml string must 
     * not contain the XML header.
     * <p>
     * Elements added using this method may not be
     * retrieved immediately as a Node using getCustomStatusElement.  To do
     * this the presence document needs to be built and then parsed.
     * </p>
     *
     * @param sElement custom status sub-element to add in serialized
     * form.
     * @param validateXML verify that sElement contains well-formed
     * XML.
     * @param namespaceURI XML namespace URI corresponding to the 
     * prefix used in the XML fragment.  Note that only one custom
     * namespace may be used in the xml fragment.
     *
     * @exception XMLProcessingException ill-formed XML or missing or
     * unrecognized namespace prefix
     */
    public void addSerializedCustomStatusElement(String sElement,
						 String namespaceURI,
						 boolean validateXML) throws XMLProcessingException
    {
	String prefix = null;
	Document dummyDoc = null;
	String s = sElement;

	if (validateXML) {
	    // validate XML
	    dummyDoc = XMLUtil.createDocFromString(sElement);
	}
		
	if (namespaceURI != null) {
	    if (validateXML) {
		if (dummyDoc != null) {
		    Node parentNode = dummyDoc.getFirstChild();
		    if (parentNode != null) {
			Node curNode = parentNode.getFirstChild();
			prefix = XMLUtil.getNamespacePrefix((Element)curNode);
		    }
		}
	    } else {
		// get the prefix manually
		prefix = XMLUtil.getPrefixFromXMLString(sElement);
	    }

	    String newPrefix = addNamespace(namespaceURI, prefix);
	    if (newPrefix != null) {
		s = XMLUtil.replacePrefix(sElement, prefix, newPrefix);
	    }
	}

        if (serializedCustomStatusElements == null) {
	    serializedCustomStatusElements = new StringBuffer(s);
	} else {
	    serializedCustomStatusElements.append(s);
	}
    }


    /**
     * return a concatenation of all custom status elements.  This method is 
     * used by the presence store to obtain a serialized XML fragment
     * containing all custom status elements of this tuple. 
     */
    public String getSerializedCustomStatusElements() throws XMLProcessingException
    {
	if (customStatusElements == null) return null;
	StringBuffer buf = serializedCustomStatusElements;
	for (Iterator i = customStatusElements.values().iterator(); i.hasNext(); ) {
	    List list = (List)i.next();
	    if (list != null) {
		for (Iterator j = list.iterator(); j.hasNext(); ) {
		    Element elt = (Element)j.next();
		    String s = XMLUtil.dumpNodeAsString(elt);
		    if (buf == null) {
			buf = new StringBuffer(s);
		    } else {
			buf.append(s);
		    }
		}
	    }
	}

        if (buf != null) {
	    return buf.toString();
	} else {
	    return null;
	}
    }

    /**
     * returns a list of Element objects belonging to a specific XML
     * namespace.  This allows applications to retrieve only elements
     * they are interested in and ignore other custom tuple extentions.
     * @param namespace namespace URI or prefix.
     */
    public List getCustomStatusElements(String namespace)
    {
	if (customTupleElements == null) return null;

	String nsURI = getNamespaceURI(namespace);
	if (nsURI == null) return null;

	return (List)customStatusElements.get(nsURI);
    }

    /**
     * return a serialized XML fragment which is a concatenation of all
     * tuple sub-elements belonging to the specified namespace.  
     * This allows applications to retrieve only elements
     * they are interested in and ignore other custom extentions.
     */
    public String getSerializedCustomStatusElements(String namespace) throws XMLProcessingException
    {
	if (customStatusElements == null) return null;

	String nsURI = getNamespaceURI(namespace);
	if (nsURI == null)  return null;

	List list = getCustomStatusElements(nsURI);
	StringBuffer buf = null;
	if (list != null) {
	    for (Iterator j = list.iterator(); j.hasNext(); ) {
		Element elt = (Element)j.next();
		String s = XMLUtil.dumpNodeAsString(elt);
		if (buf == null) {
		    buf = new StringBuffer(s);
		} else {
		    buf.append(s);
		}
	    }
	}
        if (buf != null) {
	    return buf.toString();
	} else {
	    return null;
	}
    }
}
