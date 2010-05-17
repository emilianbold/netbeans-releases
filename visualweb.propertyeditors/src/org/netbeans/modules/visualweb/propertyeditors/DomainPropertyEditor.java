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
