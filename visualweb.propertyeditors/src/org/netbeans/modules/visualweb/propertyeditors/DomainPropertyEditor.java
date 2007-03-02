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
package org.netbeans.modules.visualweb.propertyeditors;

import java.beans.PropertyDescriptor;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.propertyeditors.domains.AttachedDomain;
import com.sun.rave.propertyeditors.domains.Domain;
import com.sun.rave.propertyeditors.domains.Element;

/**
 * An abstract property editor base class, for building editors that allow
 * a property's value to be selected from a pre-configured domain of values.
 * A domain is a class that extends {@link com.sun.rave.propertyeditors.domains.Domain}.
 * The editor's domain may be set in one of two ways:
 * <ul>
 *   <li>A domain object may be specified as a constructor parameter.
 *   <li>The domain class may be specified as the value of the property
 *   descriptor attribute <code>DomainPropertyEditor.DOMAIN_CLASS</code>, e.g.
 *   <pre>
 *     propertyDescriptor.setValue(DomainPropertyEditor.DOMAIN_CLASS, MyEditor.class);
 *   </pre>
 * </ul>
 * If a domain is supplied via a constructor, the design property will not be
 * searched for an attribute specifying a domain class name. If the domain is an
 * instance of {@link com.sun.rave.propertyeditors.domains.AttachedDomain}, it's
 * design property will be set as soon as the editor's design property is set.
 *
 * @author gjmurphy
 * @see com.sun.rave.propertyeditors.domains.Domain
 */
public abstract class DomainPropertyEditor extends PropertyEditorBase {

    /**
     * Key used to specify a domain class within a property descriptor.
     */
    public final static String DOMAIN_CLASS =
            "com.sun.rave.propertyeditors.DOMAIN_CLASS"; //NOI18N

    // Used to represent a "null" or "empty" property value
    static final Element EMPTY_ELEMENT = new Element( null, "" );

    // The domain element that corresponds to this property's default or "unset"
    // value. This is set by default to the empty element, but will be updated
    // to reflect the property's unset value as soon as the property descriptor
    // is passed in.
    protected Element defaultElement = EMPTY_ELEMENT;

    Domain domain;

    DomainPropertyEditor() {
        this.domain = null;
    }

    DomainPropertyEditor(Domain domain) {
        this.domain = domain;
    }

    public void setDesignProperty(DesignProperty designProperty) {
        super.setDesignProperty(designProperty);
        PropertyDescriptor descriptor = designProperty.getPropertyDescriptor();
        if (this.domain == null) {
            Object domainClassValue = descriptor.getValue(this.DOMAIN_CLASS);
            if (domainClassValue == null)
                throw new IllegalArgumentException(
                        bundle.getMessage("DomainPropertyEditor.domainMissing",
                        designProperty.getPropertyDescriptor().getDisplayName()));
            if (!(domainClassValue instanceof Class))
                throw new IllegalArgumentException(
                        bundle.getMessage("DomainPropertyEditor.domainValueNotClass",
                        designProperty.getPropertyDescriptor().getDisplayName()));
            Class domainClass = (Class) domainClassValue;
            try {
                Domain domain = (Domain) domainClass.newInstance();
                this.domain = domain;
            } catch( InstantiationException e ) {
                throw new IllegalArgumentException(
                        bundle.getMessage("DomainPropertyEditor.domainError", //NOI18N
                        domainClass.toString(), designProperty.getPropertyDescriptor().getDisplayName()));
            } catch( IllegalAccessException e ) {
                throw new IllegalArgumentException(
                        bundle.getMessage("DomainPropertyEditor.domainError", //NOI18N
                        domainClass.toString(), designProperty.getPropertyDescriptor().getDisplayName()));
            }
        }
        if (this.domain instanceof AttachedDomain)
            ((AttachedDomain) this.domain).setDesignProperty(designProperty);
        Object defaultValue = designProperty.getUnsetValue();
        if (defaultValue != null) {
            Element[] elements = domain.getElements();
            for (int i = 0; i < elements.length; i++) {
                if (defaultValue.equals(elements[i].getValue()))
                    defaultElement = elements[i];
            }
        }
    }
    
    protected Domain getDomain() {
        return this.domain;
    }
    
    protected String getPropertyHelpId() {
        if (this.domain != null)
            return this.domain.getPropertyHelpId();
        return null;
    }
    
}
