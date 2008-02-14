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
package org.netbeans.modules.compapp.casaeditor.properties.extension;

import java.lang.reflect.Constructor;
import java.util.Map;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.netbeans.modules.compapp.casaeditor.properties.spi.ExtensionPropertyClassProvider;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author jqian
 */
public class ExtensionPropertyFactory {

    public static Node.Property getProperty(
            CasaNode node,
            CasaComponent extensionPointComponent,
            CasaExtensibilityElement firstEE,
            CasaExtensibilityElement lastEE,
            String propertyType, //[WRITABLE]
            String attrType,
            String attributeName,
            String displayName,
            String description) {

        Class propertyClass = null;

        try {
            if (attrType.equalsIgnoreCase("integer")) { // NOI18N
                propertyClass = IntegerExtensionProperty.class;
            } else if (attrType.equalsIgnoreCase("qname")) { // NOI18N
                propertyClass = QNameExtensionProperty.class;
            } else if (attrType.equalsIgnoreCase("string")) { // NOI18N
                propertyClass = StringExtensionProperty.class;
            } else if (attrType.equalsIgnoreCase("boolean")) { // NOI18N
                propertyClass = BooleanExtensionProperty.class;
            } else {
                Lookup.Result result = Lookup.getDefault().lookup(
                        new Lookup.Template<ExtensionPropertyClassProvider>(ExtensionPropertyClassProvider.class));
                
                for (Object obj : result.allInstances()) {
                    ExtensionPropertyClassProvider provider = (ExtensionPropertyClassProvider) obj;
                    propertyClass = provider.getExtensionPropertyClass(attrType);
                    if (propertyClass != null) {
                        break;
                    }
                }
            }

            if (propertyClass != null) {
                Constructor constructor = propertyClass.getConstructor(
                        CasaNode.class, CasaComponent.class,
                        CasaExtensibilityElement.class,
                        CasaExtensibilityElement.class,
                        String.class,
                        String.class,
                        String.class,
                        String.class);

                return (Node.Property) constructor.newInstance(node, 
                        extensionPointComponent,
                        firstEE, lastEE, propertyType, 
                        attributeName, displayName, description);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }

    public static ChoiceExtensionProperty getProperty(
            CasaNode node,
            CasaComponent extensionPointComponent,
            CasaExtensibilityElement firstEE,
            CasaExtensibilityElement lastEE,
            String propertyType,
            String attributeName,
            String displayName,
            String discription,
            Map<String, CasaExtensibilityElement> choiceMap,
            String defaultChoice) {

        return new ChoiceExtensionProperty(
                node,
                extensionPointComponent,
                firstEE,
                lastEE,
                propertyType,
                attributeName,
                displayName,
                discription,
                choiceMap, 
                defaultChoice);
    }
}
