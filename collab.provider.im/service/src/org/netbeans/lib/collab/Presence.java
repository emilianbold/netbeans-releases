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
import org.netbeans.lib.collab.util.XMLUtil;
import org.netbeans.lib.collab.util.XMLProcessingException;


/**
 *
 * @since version 0.1
 *
 */
public class Presence implements java.io.Serializable
{

    /**
     * custom markup
     */
    private HashMap _notes;

    private LinkedList _tuples = new LinkedList();

    //In case of XMPP presence format only one tuple is required.
    private PresenceTuple xmpp_tuple;

    /**
     * presentity id / url
     */
    public String url;
    
    public Presence(String id) {
        url = id;

	// add default namespaces
	addNamespace("icp", PresenceHelper.XMLNS_ICP_URI);

    }

    public Presence(PresenceTuple tuple) {
        xmpp_tuple = tuple;
    }
        
    /**
     * do not modify result
     */
    public java.util.Collection getTuples() {
        if (xmpp_tuple == null) return _tuples;
        ArrayList ret = new ArrayList();
        ret.add(xmpp_tuple);
        return ret;
    }

    /**
     * Add a tuple to this presentity
     * If a tuple with the same tuple id already exists in this
     * presentity, the existing tuple is removed and replaces
     * with the one specified here.
     * If custom XML namespaces have been declared in the 
     * tuple, they are automatically taken into account when 
     * building the serialized presence document.
     * @param t tuple to add
     */
    public synchronized void addTuple(PresenceTuple t)
    {
        if (xmpp_tuple != null) return;
	if (_tuples.contains(t)) {
	    _tuples.remove(t);
	}
	t.presentityID = url;
	t.presentity = this;

	// add namespaces declared in tuple
	if (t._namespaces != null) {
	    if (_namespaces == null) {
		_namespaces = t._namespaces;
	    } else {
		_namespaces.putAll(t._namespaces);
	    }
	}

	_tuples.add(t);
    }

    public synchronized void removeTuple(PresenceTuple t)
    {
        if (xmpp_tuple != null) return;
	t.presentityID = null;
	t.presentity = null;
	_tuples.remove(t);
    }

    public void addTuple(PresenceTuple[] t)
    {
        if (xmpp_tuple != null) return;
	for (int i = 0; i < t.length; i++) {
	    t[i].presentityID = url;
	    addTuple(t[i]);
	}
    }

    public String toString()
    {
        if (xmpp_tuple != null) return null;
	StringBuffer buf = new StringBuffer();
	try {
	    copyTo(buf);
	} catch (Exception e) {
	    e.printStackTrace();	    
	}
	return buf.toString();
    }

    protected void copyTo(StringBuffer buf) throws Exception
    {
//        if (xmpp_tuple != null) return;
	buf.append("<presence entity='");
	buf.append(url);
	buf.append("' ");

// 	// add default PIDF namespace
// 	buf.append("xmlns='");
// 	buf.append(PresenceHelper.XMLNS_PIDF_URI);
// 	buf.append("' ");

// 	// add ICP namespace
// 	buf.append("xmlns:icp='");
// 	buf.append(PresenceHelper.XMLNS_ICP_URI);
// 	buf.append("' ");

	// add custom namespaces
	String s = getNamespaces();
	if (s != null) {
	    buf.append(s);
	}
	buf.append(">");
       
	for (Iterator i = getTuples().iterator(); i.hasNext(); ) {
	    PresenceTuple t = (PresenceTuple)i.next();
	    t.copyTo(buf);
	}

        if (_notes != null && _notes.size() > 0) {
	    for (Iterator i = _notes.entrySet().iterator(); i.hasNext(); ) {
		java.util.Map.Entry entry = (java.util.Map.Entry)i.next();
		String lang = (String)entry.getKey();
		String note = (String)entry.getValue();
		XMLUtil.appendElementTag(buf, PresenceHelper.PIDF_XMLNS,
					 PresenceHelper.ELEMENT_NOTE,
					 false, false);
		buf.append(" ");
		buf.append(PresenceHelper.ATTRIBUTE_LANG);
		buf.append("='");
		buf.append(lang);
		buf.append("'>");
		
		buf.append(note);
		XMLUtil.appendElementTag(buf, PresenceHelper.PIDF_XMLNS,
					 PresenceHelper.ELEMENT_NOTE,
					 true, true);
	    }
        }

	s = getSerializedCustomElements();
        if (s != null) {
             buf.append(s);
        }

	buf.append("</presence>\n");	    
    }

    // namespace URI to namespace prefix map
    private HashMap _namespaces = null;
    private String _sNamespaces = null;

    /**
     * add a namespace declaration to be used in elements of the 
     * presence document.
     * @return prefix to use in the XML in case a conflict was detected.
     * null if no prefix conlict was found.
     */
    protected String addNamespace(String prefix, String nsURI)
    {
	String uniquePrefix = prefix;
	String res = null;

	if (_namespaces == null) _namespaces = new HashMap();

	// look for a possible prefix conflict
	for (int i=0; ; i++) {
	    String origURI;
	    origURI = (String)_namespaces.get(uniquePrefix);

	    if (origURI != null && !origURI.equalsIgnoreCase(nsURI)) {
		// conflict
		uniquePrefix = Integer.toString(nsURI.hashCode() + i);
		res = uniquePrefix;
	    } else {
		break;
	    }
	}

	_namespaces.put(uniquePrefix, nsURI);

	return res;
    }

    protected boolean hasNamespaceURI(String prefix)
    {
	return (_namespaces.get(prefix) != null);
    }

    protected String getNamespaceURI(String s)
    {
 	if (s.indexOf(':') < 0) {
	    // prefix
 	    return (String)_namespaces.get(s);
 	} else {
 	    return s;
 	}
    }

    /**
     * set the namespace list for this presentity.
     * This method is used by the presence store to generate 
     * Presence object based on information stored in the
     * database.
     * @param namespaceList String containing all necessary
     * XML namespace declarations separated by whitespaces.
     */
    public void setNamespaces(String namespaceList)
    {
	_sNamespaces = namespaceList;
    }

    
    /**
     * returns string containing all necessary namespace declarations
     * for this presentity.  The resulting String is ready to be included
     * in a serialized presence element.  This method is used by the
     * presence store to generate a String that can be stored in the 
     * database.  Example:
     * <ul><code>
     * xmlns:ext1='urn:example:pidf-1' xmlns:ext2='urn:example:pidf-2'
     * </ul></code>
     */
    public String getNamespaces()
    {
	if (_sNamespaces != null) {
	    return _sNamespaces;
	}
	
	// else build from namespace list
	StringBuffer buf = new StringBuffer();
	if (_namespaces != null) {
	    for (Iterator i = _namespaces.keySet().iterator(); i.hasNext(); ) {
		String prefix = (String)i.next();
                String urn = (String)_namespaces.get(prefix); 
		buf.append("xmlns");
		if (prefix != null && prefix.length() > 0) {
                    buf.append(":" + prefix);
                }
		buf.append("='");
		buf.append(urn);
		buf.append("' ");
	    }
	}
	_sNamespaces = buf.toString();

	return _sNamespaces;
    }

    private HashMap customElements;
    private StringBuffer serializedCustomElements;

    /**
     * add a custom element as a Node.  This is used by PresenceHelper
     * to populate the Presence data object during parsing.
     * @param element custom tuple sub-element to add
     */
    protected void addCustomElement(Element element) throws XMLProcessingException
    {
        if (customElements == null) customElements = new HashMap();
	String key = (String)_namespaces.get(XMLUtil.getNamespacePrefix(element));
	List list = (List)customElements.get(key);
	if (list == null) {
	    list = new LinkedList();
	    list.add(element);
	    customElements.put(key, list);
	} else {
	    list.add(element);
	}
    }

    /**
     * add a serialized custom element as a Node.
     * This is used by application to add custom presence sub-elements
     * while buiding a presence document.
     * Note: elements added using this method may not be
     * retrieved immediately using getCustomElement.  To do
     * this the presence document needs to be built and then parsed.
     * @param sElement custom tuple sub-element to add in serialized
     * form.
     * @param validateXML if true validate the XML and validate that 
     * namespaces prefixes being used correspond to known namespaces.
     *
     * @exception XMLProcessingException ill-formed XML or missing or
     * unrecognized namespace prefix
     */
    public void addSerializedCustomElement(String sElement,
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

        if (serializedCustomElements == null) {
	    serializedCustomElements = new StringBuffer(s);
	} else {
	    serializedCustomElements.append(s);
	}
    }

    /**
     * return a concatenation of all custom elements.  This method is 
     * used by the presence store to obtain a serialized XML fragment
     * containing all custom elements of this tuple. 
     */
    public String getSerializedCustomElements() throws XMLProcessingException
    {
	if (customElements == null) return null;
	StringBuffer buf = serializedCustomElements;
	for (Iterator i = customElements.values().iterator(); i.hasNext(); ) {
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
     * they are interested in and ignore other custom extentions.
     * @param namespace namespace URI or prefix.
     * @return list of org.w3c.dom.Element objects.
     */
    public List getCustomElements(String namespace)
    {
	if (customElements == null) return null;

	String nsURI = getNamespaceURI(namespace);
	if (nsURI == null) return null;

	return (List)customElements.get(nsURI);
    }

    /**
     * return a serialized XML fragment which is a concatenation of all
     * sub-elements belonging to the specified namespace.  
     * This allows applications to retrieve only elements
     * they are interested in and ignore other custom extentions.
     * @param namespace XML namespace URI or prefix
     */
    public String getSerializedCustomElements(String namespace) throws XMLProcessingException
    {
	if (customElements == null) return null;

	String nsURI = getNamespaceURI(namespace);
	if (nsURI == null) return null;

	List list = getCustomElements(nsURI);
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


    /**
     * get the note corresponding to a specified language
     * @param lang language tag
     * @return note
     */
    public String getNote(String lang)
    {
        if (xmpp_tuple != null) {
            return xmpp_tuple.getNote(lang);
        }
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
        if (xmpp_tuple != null) {
            return xmpp_tuple.getNote();
        }
        if (_notes == null) return null;
        String text = getNote(System.getProperty("user.language"));
        if (text == null && _notes.size() == 1) {
            text = (String)_notes.values().iterator().next();
        }
        return text;
    }

    /**
     * set the note for a specified language
     * @param lang language tag
     * @param text note content
     */
    public void addNote(String lang, String text)
    {
        if (xmpp_tuple != null) {
            xmpp_tuple.addNote(lang,text);
            return;
        }
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
}
