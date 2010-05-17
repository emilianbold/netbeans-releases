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

package org.netbeans.modules.wsdleditorapi.generator;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.openide.nodes.Node;

/**
 * This is a SPI for WSDL extensibility elements provider to implement in order to configure the way
 * wsdl editor behaves with the extensibility elements.
 *
 * @author skini
 *
 */

public abstract class ExtensibilityElementConfigurator {


    /**
     * Should return the qname of the element(s) supported by this configurator.
     * 
     * @return collection of QName(s)
     */
    public abstract Collection<QName> getSupportedQNames();
    
    
    /**
     * Return the Node.Property for the attribute name to be shown on the property sheet in wsdl editor.
     * The Property.setName() should be done if a different name is to be shown.
     * Setting value of extensibility element may result in exception if already in transaction in this method.
     * 
     * By default, name is set as value of attributeName and the description set to the documentation provided
     * in the schema or as the qname of the element.
     * If both have not been provided, then it should be set on the Node.Property before returning it.  
     * 
     * @param extensibilityElement the extensibility element
     * @param qname the qname of the element interested in
     * @param attributeName the name of the attribute
     * @return Node.Property instance for this attribute
     */
    public abstract Node.Property getProperty(ExtensibilityElement extensibilityElement, QName qname, String attributeName);
    
    
    /**
     * Specifies whether this attribute should be hidden (in the property sheet).
     * If returns true, then the attribute is not shown on the property sheet.
     * 
     * This method can be used to hide attributes and create a Property editor for multiple properties.
     * For e.g. Part can have element or type but not both, so set type as hidden and create a node.property 
     * for element with name as "Element or Type" and appropriately change the setValue or getValue to set appropriate
     * attribute.
     * 
     * @param qname qname of the element
     * @param attributeName name of the attribute
     * @return true if to be hidden
     */
    public boolean isHidden(ExtensibilityElement extensibilityElement, QName qname, String attributeName) {
        return false;
    }
    
    /**
     * Specifies the name of the attribute, the value of which will be as the display name for this element.
     * 
     * @param extensibilityElement
     * @param qname
     * @return the attribute name.
     */
    public abstract String getDisplayAttributeName(ExtensibilityElement extensibilityElement, QName qname);
    
    /**
     * Returns a initial value for this attribute. return a value which will be used as a prefix and
     * unique names generated by wsdl editor. prefix1, prefix2 etc..
     * 
     * 
     * @param qname
     * @param attributeName
     * @return String either default value(if boolean or enumerated), otherwise prefix
     */
    
    public abstract String getAttributeUniqueValuePrefix(ExtensibilityElement extensibilityElement, QName qname, String attributeName);
    
    /**
     * Gives a default value for the attribute.
     * 
     * 
     * @param qname
     * @param attributeName
     * @return
     */
    public abstract String getDefaultValue(ExtensibilityElement extensibilityElement, QName qname, String attributeName);
    
    public abstract String getTypeDisplayName(ExtensibilityElement extensibilityElement, QName qname);
    

    public String getHtmlDisplayNameDecoration(ExtensibilityElement construct, QName name) {
        return null;
    }
    
}
