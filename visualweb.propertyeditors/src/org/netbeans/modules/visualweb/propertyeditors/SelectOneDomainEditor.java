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

import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.propertyeditors.domains.Domain;
import com.sun.rave.propertyeditors.domains.EditableDomain;
import com.sun.rave.propertyeditors.domains.Element;
import java.awt.Component;

/**
 * A property editor for domains, that allows exactly one element from a domain to
 * be selected as the property value. Element labels are treated as the "text"
 * view of an element, and the currently selected element's value property is
 * treated as the editor's current value. This editor typically manifests itself as
 * an in-line drop-down selector of element labels. If a domain is large, a
 * custom property editor will also be available, which facilitates searching
 * through long lists (see {@link SelectOneDomainPanel}).
 *
 * @author gjmurphy
 */
public class SelectOneDomainEditor extends DomainPropertyEditor {

    // Default maximum time in milliseconds that the most recently fetched list
    // of elements will be used before being refreshed
    static final long FETCH_TIME_LIMIT = 2500;

    // Non-editable domains that have more than this many elements will not be
    // displayed for editing int the in-line drop-down widget. Instead, they
    // will be displayed in a pop-up custom editor that supports searching by
    // label prefix and uses a large, scrollable list.
    static final int MAX_DOMAIN_SIZE = 8;

    // The domain element that represent's the property's current value. A
    // SelectOne domain can have only one value selected at a time.
    Element element;


    /**
     * Creates a new instance of DomainPropertyEditor, that will pick up its
     * domain later, from the property descriptor.
     */
    public SelectOneDomainEditor() {
        super();
    }

    /**
     * Creates a new instance of DomainPropertyEditor, with the domain
     * specified.
     */
    public SelectOneDomainEditor(Domain domain) {
        super(domain);
    }

    public Object getValue() {
        return this.element == null ? null : this.element.getValue();
    }

    public void setValue(Object value) {
        Domain domain = this.getDomain();
        if (domain == null)
            return;
        Element[] elements = getElements();
        Element e = null;
        for (int i = 0; i < elements.length && e == null; i++) {
            Object v = elements[i].getValue();
            if ((value == v) || (v != null && v.equals(value))) {
                e = elements[i];
            }
        }
        if( e == null )
            this.element = super.defaultElement;
        else
            this.element = e;
    }

    public String getAsText() {
        if (this.element == null)
            this.element = super.defaultElement;
        return this.element.getLabel();
    }

    public void setAsText(String text) throws IllegalArgumentException {
        Domain domain = this.getDomain();
        if (domain == null)
            return;
        if (text == null || text.trim().length() == 0) {
            this.element = super.defaultElement;
        } else {
            Element[] elements = getElements();
            Element e = null;
            for (int i = 0; i < elements.length && e == null; i++) {
                if (elements[i].getLabel().equals(text)) {
                    e = elements[i];
                }
            }
            if( e != null ) {
                this.element = e;
            }
        }
    }
    
    public String[] getTags() {
        Domain domain = this.getDomain();
        if (domain == null)
            return new String[0];
        if (domain instanceof EditableDomain) {
            // Reset this editors's domain's design property, just in case there
            // have been changes to the domain since this editor was first attached
            // to its design context.
            ((EditableDomain) domain).setDesignProperty(this.getDesignProperty());
        }
        // If domain is too large, shouldn't be displayed as in-line drop-down, so
        // return null
        if (domain.getSize() > MAX_DOMAIN_SIZE)
            return null;
        return getElementLabels();
    }
    
    public boolean isPaintable() {
        return false;
    }
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
    /**
     * If domain is editable, returns an instance of SelectOneEditableDomainPanel;
     * otherwise, if the property is bindable, or if it is too large for
     * convenient display in an in-line drop down, returns an instance of
     * SelectOneDomainPanel.
     */
    public Component getCustomEditor() {
        Domain domain = this.getDomain();
        if (domain instanceof EditableDomain) {
            // Reset this editors's domain's design property, just in case there
            // have been changes to the domain since this editor was first attached
            // to its design context.
            EditableDomain editableDomain = (EditableDomain) domain;
            editableDomain.setDesignProperty(this.getDesignProperty());
        }
        SelectOneDomainPanel panel = new SelectOneDomainPanel(this);
        return panel;
    }
    
    /**
     * Set the currently selected element. This is the element that supplies
     * the values returned by <code>getValue()</code> and
     * <code>getAsText()</code>.
     */
    public void setElement(Element element) {
        this.element = element;
    }
    
    /**
     * Get the currently selected element.
     */
    public Element getElement() {
        return this.element;
    }
    
    public void setDesignProperty(DesignProperty designProperty) {
        super.setDesignProperty(designProperty);
        // If a domain was just picked up from the design property, and no value
        // has been set yet, set it explicitly to null now so that the current
        // element is correctly initialized (some domains have special elements
        // for representing null values).
        if (getDomain() != null && getValue() == null)
            setValue(null);
    }
    
    public String getJavaInitializationString() {
        return this.element.getJavaInitializationString();
    }
    
    private long elementsFetchTime;
    private Element[] elements;
    private String[] elementLabels;
    
    /**
     * A wrapper method for fetching the backing domain's elements, that uses a simple
     * time delay to avoid to many calls to Domain.getElements() in a short span of
     * time. This is necessary because during paint operations, the NetBeans property
     * sheet will make many successive calls to PropertyEditor.getTags().
     */
    private Element[] getElements() {
        if (elements == null || System.currentTimeMillis() - elementsFetchTime > FETCH_TIME_LIMIT) {
            elements = this.domain.getElements();
            elementsFetchTime = System.currentTimeMillis();
        }
        return elements;
    }
    
    private String[] getElementLabels() {
        if (elementLabels == null || System.currentTimeMillis() - elementsFetchTime > FETCH_TIME_LIMIT) {
            elements = this.getElements();
            int i = 0;
            int j = 0;
            if (domain.isRequired()) {
                elementLabels = new String[elements.length];
            } else {
                elementLabels = new String[elements.length + 1];
                elementLabels[i++] = this.EMPTY_ELEMENT.getLabel();
            }
            while (j < elements.length) {
                elementLabels[i++] = elements[j++].getLabel();
            }
        }
        return elementLabels;
    }
    
}
