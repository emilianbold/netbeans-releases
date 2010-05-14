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
package org.netbeans.modules.compapp.casaeditor.properties.spi;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;

/**
 * <code>Node.Property</code> for CASA configuration extension.
 * 
 * @author jqian
 */
public class ExtensionProperty<T> extends BaseCasaProperty<T> {

    /**
     * A CASA extension point element, that is, a non-extensibility element 
     * that is the parent of extensibility elements, 
     * e.x., casa:connection
     */
    protected CasaComponent extensionPointComponent;
    /**
     * The top-level extensiblity elements under a CASA extension point element,
     * e.x., redelivery:redelivery
     */
    protected CasaExtensibilityElement firstEE;

    public ExtensionProperty(
            CasaNode node,
            CasaComponent extensionPointComponent,
            CasaExtensibilityElement firstEE,
            CasaExtensibilityElement lastEE,
            String propertyType,
            Class<T> valueType,
            String propertyName,
            String displayName,
            String description) {
        super(node, lastEE, propertyType, valueType,
                propertyName, displayName, description);

        this.extensionPointComponent = extensionPointComponent;
        this.firstEE = firstEE;
    }

    @SuppressWarnings("unchecked")
    public T getValue() throws IllegalAccessException, InvocationTargetException {
        return (T) getStringValue();
    }
    
    protected String getStringValue() {
        CasaExtensibilityElement casaEE = (CasaExtensibilityElement) getComponent();
        String ret = casaEE.getAttribute(getName());
        return ret == null ? "" : ret;  // NOI18N
    }

    public void setValue(T value)
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        CasaExtensibilityElement lastEE = (CasaExtensibilityElement) getComponent();
        if (firstEE.getParent() == null) {
            // The extensibility element does not exist in the CASA model yet.

            // 1. Set the attribute value out of a transaction context.
            lastEE.setAttribute(getName(), value.toString());

            // 2. Add the first extensibility element with the new attribute  
            // value into the CASA model.
            getModel().addExtensibilityElement(extensionPointComponent, firstEE);
        } else {
            String stringValue = value == null ? "" : value.toString(); // NOI18N
            getModel().setExtensibilityElementAttribute(lastEE, getName(), stringValue);
        }
    }
}