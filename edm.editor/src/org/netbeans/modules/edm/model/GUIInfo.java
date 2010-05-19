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
package org.netbeans.modules.edm.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.netbeans.modules.edm.editor.utils.Attribute;
import org.openide.util.NbBundle;


/**
 * Stores UI attributes.
 * 
 * @author Ritesh Adval
 * @version $Revision$
 */
public class GUIInfo implements Cloneable {

    private static transient final Logger mLogger = Logger.getLogger(GUIInfo.class.getName());
    /** Attribute key: expanded state of UI element */
    public static final String ATTR_EXPANDED = "expanded";
    /** Attribute key: expanded state of UI element */
    public static final String ATTR_EXPANDED_HEIGHT = "expandedHeight";
    /** Attribute key: expanded state of UI element */
    public static final String ATTR_EXPANDED_WIDTH = "expandedWidth";
    /** Attribute key: height of UI element */
    public static final String ATTR_HEIGHT = "height";
    public static final String ATTR_VISIBLE = "visible";
    /** Attribute key: width of UI element */
    public static final String ATTR_WIDTH = "width";
    /** Attribute key: x-coordinate */
    public static final String ATTR_X = "x";
    /** Attribute key: y-coordinate */
    public static final String ATTR_Y = "y";
    /** XML element tag */
    public static String TAG_GUIINFO = "guiInfo";

    /* Log4J category string */
    static String logCategory = GUIInfo.class.getName();

    /* Log4J category string */
    private static final String LOG_CATEGORY = GUIInfo.class.getName();

    /* Map of attributes */
    private HashMap attrMap = new HashMap();

    /** Creates a new default instance of GUIInfo */
    public GUIInfo() {
    }

    /**
     * Creates a new instance of GUIInfo with content derived from the given DOM element.
     * 
     * @param element DOM element containing content info
     * @throws EDMException if error occurs while parsing element
     */
    public GUIInfo(Element element) throws EDMException {
        parseXML(element);
    }

    /**
     * New instance
     * 
     * @param src - src
     */
    public GUIInfo(GUIInfo src) {

        if (src == null) {
            throw new IllegalArgumentException(NbBundle.getMessage(GUIInfo.class, "ERROR_can_not_create_GUIInfo"));
        }

        copyFrom(src);
    }

    /**
     * Clone
     * 
     * @return cloned object
     */
    public Object clone() {
        return new GUIInfo(this);
    }

    /**
     * Indicates whether given object is functionally identical to this instance.
     * 
     * @param o Object to test for equality
     * @return true if functionally identical, false otherwise
     */
    public boolean equals(Object o) {
        boolean response = false;

        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        }

        if (o instanceof GUIInfo) {
            GUIInfo target = (GUIInfo) o;
            response = (getX() == target.getX());
            response &= (getY() == target.getY());
            response &= (getWidth() == target.getWidth());
            response &= (getHeight() == target.getHeight());
        }

        return response;
    }

    /**
     * Gets an attribute based on its name
     * 
     * @param attrName attribute Name
     * @return Attribute instance associated with attrName, or null if none exists
     */
    public Attribute getAttribute(String attrName) {
        return (Attribute) attrMap.get(attrName);
    }

    /**
     * @see SQLObject#getAttributeNames
     */
    public Collection getAttributeNames() {
        return attrMap.keySet();
    }

    /**
     * @see SQLObject#getAttributeObject
     */
    public Object getAttributeValue(String attrName) {
        Attribute attr = getAttribute(attrName);
        return (attr != null) ? attr.getAttributeValue() : null;
    }

    /**
     * Gets height attribute.
     * 
     * @return height
     */
    public int getHeight() {
        Attribute attr = getAttribute(ATTR_HEIGHT);
        if (attr != null && attr.getAttributeValue() != null) {
            Integer val = (Integer) attr.getAttributeValue();
            return val.intValue();
        }

        return -1;
    }

    /**
     * Gets width attribute.
     * 
     * @return width
     */
    public int getWidth() {
        Attribute attr = getAttribute(ATTR_WIDTH);
        if (attr != null && attr.getAttributeValue() != null) {
            Integer val = (Integer) attr.getAttributeValue();
            return val.intValue();
        }

        return -1;
    }

    /**
     * Gets x-coordinate attribute.
     * 
     * @return x-coordinate
     */
    public int getX() {
        Attribute attr = getAttribute(ATTR_X);
        if (attr != null && attr.getAttributeValue() != null) {
            Integer val = (Integer) attr.getAttributeValue();
            return val.intValue();
        }

        return -1;
    }

    /**
     * Gets y-coordinate attribute.
     * 
     * @return y-coordinate
     */
    public int getY() {
        Attribute attr = getAttribute(ATTR_Y);
        if (attr != null && attr.getAttributeValue() != null) {
            Integer val = (Integer) attr.getAttributeValue();
            return val.intValue();
        }

        return -1;
    }

    /**
     * Overrides hashCode to reflect internal content.
     * 
     * @return computed hashcode for this instance.
     */
    public int hashCode() {
        return getX() + getY() + getWidth() + getHeight();
    }

    /**
     * Gets expanded state of the gui element
     * 
     * @return expanded state
     */
    public boolean isExpanded() {
        Attribute attr = getAttribute(ATTR_EXPANDED);
        if (attr != null && attr.getAttributeValue() != null) {
            Boolean val = (Boolean) attr.getAttributeValue();
            return val.booleanValue();
        }

        return true;
    }

    public boolean isVisible() {
        Attribute attr = getAttribute(ATTR_VISIBLE);
        if (attr != null && attr.getAttributeValue() != null) {
            Boolean val = (Boolean) attr.getAttributeValue();
            return val.booleanValue();
        }

        return true;
    }

    /**
     * Parses the XML content, if any, using the given Element as a source for
     * reconstituting the member variables and collections of this instance.
     * 
     * @param xmlElement DOM element containing XML marshalled version of a GUIInfo
     *        instance
     * @exception EDMException thrown while parsing XML
     */
    public void parseXML(Element xmlElement) throws EDMException {
        if (xmlElement == null) {
            return;
        }

        NodeList nodes;
        nodes = xmlElement.getChildNodes();
        Element element;

        try {
            for (int i = 0; i < nodes.getLength(); i++) {
                if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    element = (Element) nodes.item(i);
                    Attribute attr = new Attribute();
                    attr.parseXMLString(element);
                    attrMap.put(attr.getAttributeName(), attr);
                }
            }
        } catch (Exception e) {
            throw new EDMException(e);
        }
    }

    /**
     * Sets an attribute name-value pair. The name of the Attribute should be one of the
     * String constants defined in this class.
     * 
     * @param attrName attribute Name
     * @param val value of the attribute
     */
    public void setAttribute(String attrName, Object val) {
        Attribute attr = getAttribute(attrName);
        if (attr != null) {
            attr.setAttributeValue(val);
        } else {
            attr = new Attribute(attrName, val);
            attrMap.put(attrName, attr);
        }
    }

    /**
     * set the expanded state of
     * 
     * @param expand expanded state of the gui element
     */
    public void setExpanded(boolean expand) {
        setAttribute(ATTR_EXPANDED, new Boolean(expand));
    }

    /**
     * Sets height attribute.
     * 
     * @param height height of UI object
     */
    public void setHeight(int height) {
        setAttribute(ATTR_HEIGHT, new Integer(height));
    }

    public void setVisible(boolean visible) {
        setAttribute(ATTR_VISIBLE, new Boolean(visible));
    }

    /**
     * Sets width attribute.
     * 
     * @param width width of UI object
     */
    public void setWidth(int width) {
        setAttribute(ATTR_WIDTH, new Integer(width));
    }

    /**
     * Sets x-coordinate attribute.
     * 
     * @param x x-coordinate of UI object
     */
    public void setX(int x) {
        setAttribute(ATTR_X, new Integer(x));
    }

    /**
     * Sets y-coordinate attribute.
     * 
     * @param y y-coordinate of UI object
     */
    public void setY(int y) {
        setAttribute(ATTR_Y, new Integer(y));
    }

    /**
     * Gets XML representation of this object's set of Attributes, appending the given
     * String to the beginning of each line in the XML document.
     * 
     * @param prefix Prefix string to be appended to each line of the generated XML
     *        document
     * @return XML document representing contents of this object
     */
    public String toXMLString(String prefix) {
        StringBuilder xml = new StringBuilder(prefix + "<" + TAG_GUIINFO + ">\n");
        Iterator it = attrMap.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            Attribute attr = (Attribute) entry.getValue();
            if (attr != null) {
                xml.append(attr.toXMLString(prefix + "\t"));
            }
        }

        xml.append(prefix).append("</" + TAG_GUIINFO + ">\n");

        return xml.toString();
    }

    private void copyFrom(GUIInfo source) {
        // clone attributes
        Collection attrNames = source.getAttributeNames();
        Iterator it = attrNames.iterator();

        while (it.hasNext()) {
            String name = (String) it.next();
            Attribute attr = source.getAttribute(name);
            if (attr != null) {
                try {
                    Attribute copiedAttr = (Attribute) attr.clone();
                    this.attrMap.put(name, copiedAttr);
                } catch (CloneNotSupportedException ex) {
                    mLogger.log(Level.INFO,NbBundle.getMessage(GUIInfo.class, "LOG.INFO_Failed_to_copy_source_guinfo_attributes{0}",new Object[] {LOG_CATEGORY}),ex);
                }
            }
        }
    }
}

