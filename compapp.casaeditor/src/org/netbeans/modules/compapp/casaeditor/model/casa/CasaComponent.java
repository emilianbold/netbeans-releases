/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.compapp.casaeditor.model.casa;

import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * Interface for all the components in the CASA model.
 * 
 * @author jqian
 */
public interface CasaComponent extends DocumentComponent<CasaComponent> {
    
    public static final String EXTENSIBILITY_ELEMENT_PROPERTY = "extensibilityElement"; // NOI18N
    
    /**
     * Get the owner model of this component.
     * 
     * @return  the owner model
     */
    CasaModel getModel();
    
    void accept(CasaComponentVisitor visitor);
        
    /**
     * Creates a global reference to the given target CASA component.
     * 
     * @param target    the target CasaComponent
     * @param type      actual type of the target
     * 
     * @return the global reference.
     */
    <T extends ReferenceableCasaComponent> NamedComponentReference<T> 
            createReferenceTo(T target, Class<T> type);
    
    /**
     * Adds a child extensibility element.
     * 
     * @param ee    a new child extensibility element
     */
    void addExtensibilityElement(CasaExtensibilityElement ee);
    
    /**
     * Removes an existing child extensibility element.
     * 
     * @param ee    an existing child extensibility element
     */
    void removeExtensibilityElement(CasaExtensibilityElement ee);
    
    /**
     * Gets a list of all child extensibility elements.
     * 
     * @return  a list of all child extensibility elements
     */
    List<CasaExtensibilityElement> getExtensibilityElements();
    
    /**
     * Gets a list of child extensibility elements of the given type.
     * 
     * @param type  type of child extensibility elements
     * @return  a list of child extensibility elements of the given type
     */
    <T extends CasaExtensibilityElement> List<T> getExtensibilityElements(Class<T> type);
        
    /**
     * Gets the value of an attribute in any namespace.
     * 
     * @param qname attribute QName 
     * 
     * @return  attribute value
     */
    String getAnyAttribute(QName qname);
    
    /**
     * Sets the value of an attribute in any namespace.
     * 
     * @param qname attribute QName 
     * @param value new attribute value
     */
    void setAnyAttribute(QName qname, String value);
}
