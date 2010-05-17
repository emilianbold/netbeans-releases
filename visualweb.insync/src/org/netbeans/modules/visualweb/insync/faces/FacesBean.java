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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.visualweb.insync.beans.Bean;
import org.netbeans.modules.visualweb.insync.beans.EventSet;
import org.netbeans.modules.visualweb.insync.beans.Property;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.visualweb.insync.java.JavaClass.UsageStatus;

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
    FacesBean(FacesPageUnit unit, BeanInfo beanInfo, String name, Element element) {
        super(unit, beanInfo, name, element);
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
        setProperty(ID_ATTR, null, getName());
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
                    ((UIComponent)parent).getFacets().put(facetName, (UIComponent)instance);
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
    void setBindingProperty() {
        String binding = getCompBinding();
        setAttr(BINDING_ATTR, binding);  // not a real property--just set the attr
    }

    void clearBindingProperty() {
        String binding = getCompBinding();
        removeAttr(BINDING_ATTR);  // not a real property--just set the attr
    }
    
    /**
     * Directly set the markup=>java bean binding attribute(s) and also the live instances
     * @param liveBean
     */
    void setBindingPropertyLive(DesignBean liveBean) {
        //Update the bindings only if the binding already exists for the bean
        if(getAttr(BINDING_ATTR) != null) {
            String binding = getCompBinding();
            setAttr(BINDING_ATTR, binding);  // not a real property--just set the attr
            updateBindingLive(liveBean.getInstance());
        }
    }
    
    /**
     * Set the name of this bean, making sure also to set the binding attr
     * @see org.netbeans.modules.visualweb.insync.beans.Bean#setName(java.lang.String, boolean, com.sun.rave.designtime.DesignBean)
     */
    public String setName(String name, boolean autoNumber, DesignBean liveBean) {
        String oldname = getName();
        String newname = super.setName(name, autoNumber, liveBean);
        if (newname != null && !newname.equals(oldname)) {
            setBindingPropertyLive(liveBean);
            liveBean.getProperty(ID_ATTR).setValue(getName());
        }
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

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.Bean#newCreatedProperty(java.beans.PropertyDescriptor)
     */
    protected Property newCreatedProperty(PropertyDescriptor pd) {
         if (!isMarkupProperty(pd)) {
             addBinding();             
         }
         return super.newCreatedProperty(pd);
    }

    public void addBinding() {
        if (!isInserted()) {
            unit.addBindingBean(getName());
        }
        setBindingProperty();
    }
    
    /*
     * @return The usage info
     */
    public UsageInfo getUsageInfo() {
        UsageStatus status = UsageStatus.NOT_USED;
        if(isUsedInBindingExpression()) {
            status = UsageStatus.USED;
        }
        if(status != UsageStatus.USED) {
            List<FileObject> fObjs = new ArrayList<FileObject>();
            fObjs.add(unit.getJavaUnit().getFileObject());
            status = unit.getThisClass().isPropertyUsed(getName(), fObjs);
            if(status == UsageStatus.INIT_USE_ONLY) {
                Set<String> props = new HashSet<String>();
                for (Property p : getProperties()) {
                    if (p.isInserted()) {
                        props.add(p.getName());
                    }
                }
                return new UsageInfo(status, props);
            }
        }
  
        return new UsageInfo(status, Collections.<String>emptySet());
    }
    /*
     * Class UsageInfo has the usage status and it has the list of properties 
     * being initialized if the binding bean is only used to initialize them in 
     * the _init() method
     */
    public class UsageInfo {
        private UsageStatus useStatus;
        private Set<String> props;
        public UsageInfo(UsageStatus status, Set<String> props) {
            this.useStatus = status;
            this.props = props;
        }
        public UsageStatus getUsageStatus() {
            return useStatus;
        }
        
        /*
         * @return the properties initialized in _init() method, set will be empty
         * if the UseStatus is not init_use_only
         */        
        public Set<String> getInitializedProperties() {
            return props;
        }
    }
    
    /*
     * @return true if the bean is used in an EL expression inside .jsp 
     */    
    private boolean isUsedInBindingExpression(){
        for (Bean b : unit.getBeans()) {
            for (Property p : b.getProperties()) {
                if (!p.isInserted()) {
                    String vbExpression = "#{" + unit.getThisClass().getShortName() + "." + getName()+ "}";
                    String vbExpression1 = "#{" + unit.getThisClass().getShortName() + "." + getName()+ ".";
                    String ps = p.getValueSource();
                    if (ps != null && (ps.contains(vbExpression) || ps.contains(vbExpression1))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /*
     * @return the list of properties that needs to be unset because of removing
     *         the binding
     */ 
    public List<String> removeBinding() {
        List<String> propsToBeDeleted = new ArrayList<String>();
        if (isInserted()) {
            unit.removeBindingBean(getName());
            for (Property p : getProperties()) {
                if (p.isInserted()) {
                    propsToBeDeleted.add(p.getName());
                }
            }
        }
        clearBindingProperty();
        return propsToBeDeleted;
    }
 }
