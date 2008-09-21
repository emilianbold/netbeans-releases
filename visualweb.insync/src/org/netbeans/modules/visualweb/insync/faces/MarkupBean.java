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
package org.netbeans.modules.visualweb.insync.faces;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Iterator;

import org.netbeans.modules.visualweb.extension.openide.util.Trace;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.Position;
import com.sun.rave.designtime.markup.AttributeDescriptor;
import com.sun.rave.designtime.markup.MarkupPosition;
import java.beans.BeanDescriptor;
import org.netbeans.modules.visualweb.insync.beans.Bean;
import org.netbeans.modules.visualweb.insync.beans.Property;

/**
 * Abstract superclass for a Bean that may have entries in markup source as well as Java source
 *
 * @author cquinn
 */
public abstract class MarkupBean extends Bean {

    MarkupBean parent;        // markup parent bean--may be set later, after constructor
    final Element element;    // underlying DOM JSP element
    final ArrayList children; // optional list of children--null if this bean is not parent capable

    //--------------------------------------------------------------------------------- Construction

    /**
     * Construct a bean bound to existing field & accessor methods, and page element. Parent will be
     * set later using bindParent().
     *
     * @param unit
     * @param beanInfo
     * @param name
     * @param field
     * @param getter
     * @param setter
     */
    public MarkupBean(FacesPageUnit unit, BeanInfo beanInfo, String name, Element element) {
        super(unit, beanInfo, name);
        this.element = element;
        children = isParentCapableBean(beanInfo) ? new ArrayList() : null;

        // bind properties now since we have our element and it is easy to do here
        NamedNodeMap attrmap = element.getAttributes();
        for (int i = 0; i < attrmap.getLength(); i++) {
            Attr attr = (Attr)attrmap.item(i);
            PropertyDescriptor pd = getPropertyDescriptorForAttribute(attr.getName());
            if (pd != null && isMarkupProperty(pd)) {
                MarkupProperty p = new MarkupProperty(this, pd, element, attr);
                properties.add(p);
            }
        }
    }

    /**
     * Construct a new bean, creating the underlying field and accessor methods and using given page
     * element
     *
     * @param unit
     * @param beanInfo
     * @param name
     * @param parent
     */
    public MarkupBean(FacesPageUnit unit, BeanInfo beanInfo, String name, MarkupBean parent, Element element) {
        super(unit, beanInfo, name);
        this.parent = parent;
        this.element = element;
        children = isParentCapableBean(beanInfo) ? new ArrayList() : null;
    }

    /**
     * @return true iff a bean defined by beanInfo is capable of having children.
     */
    private static boolean isParentCapableBean(BeanInfo beanInfo) {
        // XXX #139640 BeanDescriptor can be null (according to javadoc).
        BeanDescriptor beanDescriptor = beanInfo.getBeanDescriptor();
        if (beanDescriptor == null) {
            return false;
        }
        Object ico = beanDescriptor.getValue("isContainer");
        return !(ico instanceof Boolean) || ((Boolean)ico).booleanValue();
    }

    /**
     * Remove this bean's field, methods and statements from the host class. This bean instance is
     * dead & should not be used.
     *
     * @return true iff the source entry for this bean was actually removed.
     */
    public boolean removeEntry() {
        Node parent = element.getParentNode();
        assert Trace.trace("insync.faces", "FB removeEntry: " + this);
        boolean removed = parent != null && parent.removeChild(element) != null;
        removed |= super.removeEntry();
        return removed;
    }

    //------------------------------------------------------------------------------------ Parenting

    /**
     * @return the parent of this bean, null if top-level bean or dead
     */
    public Bean getParent() {
        return parent;
    }

    /**
     * Take the opportinuty to scan for and bind to this bean's parent
     *
     * @return the parent of this bean iff not previously bound
     */
    public Bean bindParent() {
        if (parent == null) {
            // walk up element tree to find our parent
            for (Node e = element.getParentNode(); e instanceof Element; e = e.getParentNode()) {
                parent = ((FacesPageUnit)unit).getMarkupBean((Element)e);
                if (parent != null)
                    return parent;
            }
        }
        return null;
    }

    /**
     * @return true iff this is capable of having children.
     */
    public boolean isParentCapable() {
        return children != null;
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.Bean#addChild(org.netbeans.modules.visualweb.insync.beans.Bean, com.sun.rave.designtime.Position)
     */
    public void addChild(Bean child, Position pos) {
        if (children != null && child instanceof MarkupBean) {
            if (pos != null) {
                // for markup positions mising the index, try to recompute it
                if (pos instanceof MarkupPosition && pos.getIndex() < 0) {
                    Node before = ((MarkupPosition)pos).getBeforeSibling();
                    if (before != null) {
                        Element eparent = getElement();
                        Node n = eparent.getFirstChild();
                        for (int i = 0; i < children.size(); i++) {
                            Bean sib = (Bean)children.get(i);
                            if (sib instanceof MarkupBean) {
                                Element esib = ((MarkupBean)sib).getElement();
                                while (n != before && n != esib && n != null)
                                    n = n.getNextSibling();
                                if (n == before) {
                                    pos.setIndex(i);
                                    break;
                                }
                            }
                        }
                    }
                }
                // if we've got an index one way or another, use it
                if (pos.getIndex() >= 0) {
                    children.add(pos.getIndex(), child);
                    return;
                }
            }
            // plain old add at the end
            children.add(child);
        }
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.Bean#removeChild(org.netbeans.modules.visualweb.insync.beans.Bean)
     */
    public void removeChild(Bean child) {
        if (children != null)
            children.remove(child);
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.Bean#getChildren()
     */
    public Bean[] getChildren() {
        return children != null ? (Bean[])children.toArray(EMPTY_ARRAY) : null;
    }

    //------------------------------------------------------------------------------------ Accessors

    /**
     * Set a given attribute identified by name to a given value, creating the attr as needed.
     *
     * @param name
     * @param value
     */
    void setAttr(String name, String value) {
        Attr attr = element.getAttributeNode(name);
        if (attr == null)
            element.setAttribute(name, value);
        else if (!attr.getValue().equals(value))
            attr.setValue(value);
    }
    
    /**
     * Returns a attribute identified by name.
     *
     * @param name
     * @return value
     */
    public Attr getAttr(String name) {
        return element.getAttributeNode(name);
    }    

    /**
     * Remove a given attribute identified by name.
     *
     * @param name
     */
    void removeAttr(String name) {
        Attr attr = element.getAttributeNode(name);
        if (attr != null) {
            element.removeAttribute(name);
        }
    }
    
    /*
     * @see org.netbeans.modules.visualweb.insync.beans.Bean#getElement()
     */
    public Element getElement() {
        return element;
    }

    //----------------------------------------------------------------------------------- Properties

    /**
     * Get the PropertyDescriptor for a property of this bean indicated by the attribute name
     *
     * @param attributeName the attribute name to look for
     * @return the PropertyDescriptor for the property
     */
    public PropertyDescriptor getPropertyDescriptorForAttribute(String attributeName) {
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < pds.length; i++) {
            PropertyDescriptor propertyDescriptor = pds[i];
            AttributeDescriptor attributeDescriptor = (AttributeDescriptor) propertyDescriptor.getValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR);
            if (attributeDescriptor != null && attributeDescriptor.getName().equals(attributeName)) {
                return propertyDescriptor;
            }
        }
        return null;
    }
    
    /*
     * @see org.netbeans.modules.visualweb.insync.beans.Bean#newCreatedProperty(java.beans.PropertyDescriptor)
     */
    protected Property newCreatedProperty(PropertyDescriptor pd) {
         if (isMarkupProperty(pd))
             return new MarkupProperty(this, pd, element);
         else
             return super.newCreatedProperty(pd);
     }

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.Bean#isMarkupProperty(java.beans.PropertyDescriptor)
     */
    public boolean isMarkupProperty(PropertyDescriptor pd) {
         return true;
     }

     //-------------------------------------------------------------------------------------- Object

     /*
     * @see org.netbeans.modules.visualweb.insync.beans.BeansNode#toString(java.lang.StringBuffer)
     */
    public void toString(StringBuffer sb) {
         //sb.append(" prnt:");
         //sb.append(parent);
         sb.append(" elem:");
         sb.append(element);
         super.toString(sb);
     }

}
