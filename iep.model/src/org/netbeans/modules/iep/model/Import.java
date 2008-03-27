/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.iep.model;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement.StringAttribute;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;

/**
 *
 * @author radval
 */
public interface Import extends IEPComponent {

    static final String LOCATIION_PROPERTY = "location";

    static final String NAMESPACE_PROPERTY = "namespace";

    static final String IMPORT_TYPE_PROPERTY = "importType";                              // NOI18N
    
    /**
     * This type should be used for xsd document.
     */
    String SCHEMA_IMPORT_TYPE ="http://www.w3.org/2001/XMLSchema";  // NOI18N
    
    /**
     * This type should be used for wsdl document.
     */
    String WSDL_IMPORT_TYPE = "http://schemas.xmlsoap.org/wsdl/";   // NOI18N

    
    static final Attribute ATTR_LOCATION = new StringAttribute(LOCATIION_PROPERTY);

    static final Attribute ATTR_NAMESPACE = new StringAttribute(NAMESPACE_PROPERTY);
        
    static final Attribute ATTR_IMPORT_TYPE = new StringAttribute(IMPORT_TYPE_PROPERTY);
    
    String getLocation();
    
    void setLocation(String location);
    
    String getNamespace();
    
    void setNamespace(String namespace);
    
    /**
     * Getter for ""importType" attribute.
     * 
     * @return "importType" attribute value.
     */
    String getImportType();

    /**
     * Setter for ""importType" attribute.
     * 
     * @param value
     *            New "importType" attribute value.
     */
    void setImportType( String value );
    
    
    Component getParentComponent();
    
    /**
     * Returns the imported WSDL model.
     *
     * @return a WSDL model object if the import location or namespace resolves 
     * into a model source and the model source is well-formed; 
     * @exception DepResolverException if location or namespace values cannot resolve;
     */    
    WSDLModel getImportedWSDLModel() throws CatalogModelException;
}
