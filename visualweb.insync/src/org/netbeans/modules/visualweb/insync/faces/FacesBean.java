/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.insync.faces;

import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.PropertyDescriptor;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.el.ValueBinding;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.rave.designtime.markup.AttributeDescriptor;
import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.Position;
import org.netbeans.modules.visualweb.insync.beans.Bean;
import org.netbeans.modules.visualweb.insync.beans.EventSet;
import org.netbeans.modules.visualweb.insync.beans.Property;

/**
 * A MarkupBean for a JSF component that lives in a FacesPageUnit.
 */
public class FacesBean extends MarkupBean {

    public static final String BINDING_ATTR = "binding";  // NOI18N
    public static final String ID_ATTR = "id";  // NOI18N

    Element facetElement;   // if we are a facet, this is our wrapping facet element

    //--------------------------------------------------------------------------------- Construction

    /**
     * Construct a bean bound to existing field & accessor methods, and page element.
     *
     * @param unit
     * @param beanInfo
     * @param name
     * @param field
     * @param getter
     * @param setter
     * @param element
     */
    FacesBean(FacesPageUnit unit, BeanInfo beanInfo, String name, Object/*VariableElement*/ field, Object/*ExecutableElement*/ getter,
              Object/*ExecutableElement*/ setter, Element element) {
        super(unit, beanInfo, name, field, getter, setter, element);
    }

    /**
     * Construct a new bean, creating the underlying field and accessor methods and using given page
     * element
     *
     * @param unit
     * @param beanInfo
     * @param name
     * @param parent
     * @param element
     */
    FacesBean(FacesPageUnit unit, BeanInfo beanInfo, String name, MarkupBean parent, Element element) {
        super(unit, beanInfo, name, parent, element);

        // our DOM parent must be a facet tag if it is not our faces parent
        if (parent != null && parent.element != element.getParentNode() &&
            parent.element.getNodeType() == Node.ELEMENT_NODE) {
            // Parent might be a facet tag
            Element parentElement = (Element)element.getParentNode();
            if (FacesPageUnit.URI_JSF_CORE.equals(parentElement.getNamespaceURI()))
                this.facetElement = parentElement;
        }
    }

    //------------------------------------------------------------------------------------ Parenting

    /**
     * Take the opportinuty to scan for and bind to this bean's parent.
     * !CQ TODO: maybe share code with superclass...
     *
     * @return the parent of this bean iff not previously bound
     */
    public Bean bindParent() {
        if (parent == null) {
            //!CQ walk up element tree to find our parent
            for (Node e = element.getParentNode(); e instanceof Element; e = e.getParentNode()) {
                parent = ((FacesPageUnit)unit).getMarkupBean((Element)e);
                if (parent != null)
                    return parent;
                if (e.getLocalName().equals("facet") &&
                        e.getNamespaceURI().equals(FacesPageUnit.URI_JSF_CORE))
                    facetElement = (Element)e;
            }
        }
        return null;
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.Bean#performInstanceParenting(java.lang.Object, java.lang.Object, com.sun.rave.designtime.Position)
     */
    public boolean performInstanceParenting(Object instance, Object parent, Position pos) {
        if (instance instanceof UIComponent) {
            if (parent instanceof UIComponent) {
                String facetName = this.getFacetName();
                if (facetName != null) {
                    ((UIComponent)parent).getFacets().put(facetName, instance);
                }
                else {
                    int index = pos != null ? pos.getIndex() : -1;
                    List kids = ((UIComponent)parent).getChildren();
                    if (index >= 0 && index <= kids.size())
                        kids.add(index, instance);
                    else
                        kids.add(instance);
                }
                updateBindingLive(instance);
                return true;  // got a good parent
            }
            return false;  // skip this one & try an ancestor
        }
        return true;  // something is funky--just stop now
    }

    /**
     * Update the live faces value binding for this component
     * @param instance
     */
    public void updateBindingLive(Object instance) {
        if (instance instanceof UIComponent) {
            UIComponent uic = (UIComponent)instance;
            ValueBinding vb = uic.getValueBinding(BINDING_ATTR);
            String binding = getCompBinding();
            if (vb == null || !vb.getExpressionString().equals(binding)) {
                vb = ((FacesPageUnit)unit).getFacesApplication().createValueBinding(binding);
                uic.setValueBinding(BINDING_ATTR, vb);
            }
        }
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.Bean#performInstanceUnparenting(java.lang.Object, java.lang.Object)
     */
    public void performInstanceUnparenting(Object instance, Object parent) {
        if (parent instanceof UIComponent && instance instanceof UIComponent)
            ((UIComponent)parent).getChildren().remove(instance);
    }

    //------------------------------------------------------------------------------------ Accessors

    /**
     * Directly set the markup=>java bean binding attribute(s)
     */
    void setBindingProperties() {
        String binding = getCompBinding();
        setAttr(BINDING_ATTR, binding);  // not a real property--just set the attr
        setProperty(ID_ATTR, null, getName());
    }

    /**
     * Directly set the markup=>java bean binding attribute(s) and also the live instances
     * @param liveBean
     */
    void setBindingPropertiesLive(DesignBean liveBean) {
        String binding = getCompBinding();
        setAttr(BINDING_ATTR, binding);  // not a real property--just set the attr
        updateBindingLive(liveBean.getInstance());
        liveBean.getProperty(ID_ATTR).setValue(getName());
    }

    /**
     * Set the name of this bean, making sure also to set the binding attr
     * @see org.netbeans.modules.visualweb.insync.beans.Bean#setName(java.lang.String, boolean, com.sun.rave.designtime.DesignBean)
     */
    public String setName(String name, boolean autoNumber, DesignBean liveBean) {
        String oldname = getName();
        String newname = super.setName(name, autoNumber, liveBean);
        if (newname != null && !newname.equals(oldname))
            setBindingPropertiesLive(liveBean);
        return newname;
    }

    /**
     * Get the binding expression string for this bean.
     * @return the binding expression string for this bean.
     */
    public String getCompBinding() {
        return ((FacesPageUnit)unit).getCompBinding(getName());
    }

    /**
     * Get the facet element if this bean is a facet.
     * @return the facet element if this bean is a facet, or null if it is not.
     */
    public Element getFacetElement() {
        return facetElement;
    }

    /**
     * Get the facet name if this bean is a facet.
     * @return the facet name if this bean is a facet, or null if it is not.
     */
    public String getFacetName() {
        return facetElement != null ? facetElement.getAttribute("name") : null;
    }


    //----------------------------------------------------------------------------------- Properties

    /**
     * Determine if a given property should be managed in markup based on it having an attribute
     * descriptor.
     *
     * @param pd The propertty's descriptor.
     * @return The attribute descriptor if there is one, else null.
     */
    public static AttributeDescriptor getAttributeDescriptor(PropertyDescriptor pd) {
        Object ad = pd.getValue(com.sun.rave.designtime.Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR);
        if (ad instanceof AttributeDescriptor)
            return (AttributeDescriptor)ad;
        return null;
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.Bean#isMarkupProperty(java.beans.PropertyDescriptor)
     */
    public boolean isMarkupProperty(PropertyDescriptor pd) {
        return getAttributeDescriptor(pd) != null;
    }

    //------------------------------------------------------------------------------------ EventSets

    /**
     * Perform markup-based event wiring here on a per-bean basis.
     */
    protected void bindEventSets() {
        for (Iterator pi = properties.iterator(); pi.hasNext(); ) {
            Property p = (Property)pi.next();
            if (p instanceof MarkupProperty && MethodBindEventSet.isMethodBindProperty(p)) {
                MarkupProperty mp = (MarkupProperty)p;
                PropertyDescriptor pd = p.getDescriptor();
                EventSetDescriptor[] esds = beanInfo.getEventSetDescriptors();
                for (int i = 0; i < esds.length; i++) {
                    Object epdO = esds[i].getValue(Constants.EventSetDescriptor.BINDING_PROPERTY);
                    if (pd.equals(epdO))
                        eventSets.add(new MethodBindEventSet(this, esds[i], mp));
                }
            }
        }
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.Bean#newCreatedEventSet(java.beans.EventSetDescriptor)
     */
    protected EventSet newCreatedEventSet(EventSetDescriptor esd) {
        Object pdO = esd.getValue(Constants.EventSetDescriptor.BINDING_PROPERTY);
        if (pdO instanceof PropertyDescriptor) {
            //!CQ check for attr descriptor too?
            return new MethodBindEventSet(this, esd, (PropertyDescriptor)pdO);
        }
        return super.newCreatedEventSet(esd);
    }

}
