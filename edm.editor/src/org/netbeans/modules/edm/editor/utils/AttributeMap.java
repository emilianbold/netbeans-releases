/*
 * BEGIN_HEADER - DO NOT EDIT
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */

/*
 * @(#)AttributeMap.java 
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

package org.netbeans.modules.edm.editor.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.netbeans.modules.edm.model.EDMException;

/**
 * Encapsulates the value of a state variable as a name-value tuple.
 * 
 * @author Ritesh Adval
 * @version 
 */
public class AttributeMap {

    /* Log4J category string */
    static final String LOG_CATEGORY = Attribute.class.getName();

    /**
     * Map of attributes; used by concrete implementations to store class-specific fields
     * without hardcoding them as member variables
     */
    protected Map attributes = new HashMap();

    /** Creates a default instance of Attribute */
    public AttributeMap() {
    }

    /**
     * Gets an attribute based on its name
     * 
     * @param attrName attribute Name
     * @return Attribute instance associated with attrName, or null if none exists
     */
    public Attribute get(String attrName) {
        return (Attribute) attributes.get(attrName);
    }

    public Object getAttributeValue(String attrName) {
        Attribute attr = get(attrName);
        return (attr != null) ? attr.getAttributeValue() : null;
    }


    public Collection keySet() {
        return attributes.keySet();
    }

    public void parseAttributeList(NodeList list) throws EDMException {
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) list.item(i);
                if (elem.getNodeName().equals(Attribute.TAG_ATTR)) {
                    Attribute attr = new Attribute();
                    attr.parseXMLString(elem);
                    this.attributes.put(attr.getAttributeName(), attr);
                }
            }
        }
    }

    public void put(String attrName, Object val) {
        Attribute attr = get(attrName);
        if (attr != null) {
            attr.setAttributeValue(val);
        } else {
            attr = new Attribute(attrName, val);
            attributes.put(attrName, attr);
        }
    }

    /**
     * Generates XML elements representing this object's associated attributes.
     * 
     * @param prefix Prefix string to be prepended to each element
     * @return String containing XML representation of attributes
     */
    public String toXMLString(String prefix) {
        StringBuffer buf = new StringBuffer(100);

        Iterator iter = attributes.values().iterator();
        while (iter.hasNext()) {
            Attribute attr = (Attribute) iter.next();
            if (attr.getAttributeValue() != null) {
                buf.append(attr.toXMLString(prefix + "\t"));
            }
        }

        return buf.toString();
    }

}
